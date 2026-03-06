package com.sobot.chat.utils;

import android.content.Context;
import android.text.TextUtils;

import com.sobot.chat.api.ZhiChiApi;
import com.sobot.chat.api.model.UploadInitModel;
import com.sobot.network.http.callback.SobotResultCallBack;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 分片上传管理器
 * 负责文件的分片上传逻辑，包括初始化、分片上传、完成确认
 * 优化版本：流式读取 + 单分片内存缓存，避免临时文件，减少内存占用
 */
public class SobotChunkedUploadManager {

    // 默认分片大小：10MB
    public static final long DEFAULT_CHUNK_SIZE = 10 * 1024 * 1024;

    private final Context context;
    private final ZhiChiApi zhiChiApi;
    private final long chunkSize;
    private UploadState currentState;
    private String REQUEST_TAG;

    /**
     * 上传状态
     */
    private static class UploadState {
        final File file;
        final String fileKey;
        final long totalSize;
        final int totalChunks;
        final String companyId;
        final String fileName;
        RandomAccessFile raf;

        UploadState(File file, String fileKey, long totalSize, int totalChunks, String companyId) {
            this.file = file;
            this.fileKey = fileKey;
            this.totalSize = totalSize;
            this.totalChunks = totalChunks;
            this.companyId = companyId;
            this.fileName = file.getName();
        }

        void open() throws IOException {
            this.raf = new RandomAccessFile(file, "r");
        }

        void close() {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    // ignore
                }
                raf = null;
            }
        }

        /**
         * 读取指定分片的数据
         */
        byte[] readChunk(int chunkIndex) throws IOException {
            long start = (chunkIndex - 1) * DEFAULT_CHUNK_SIZE;
            long end = Math.min(start + DEFAULT_CHUNK_SIZE, totalSize);
            int chunkLength = (int) (end - start);

            byte[] buffer = new byte[chunkLength];
            raf.seek(start);
            raf.readFully(buffer);

            return buffer;
        }
    }

    /**
     * 上传回调接口
     */
    public interface UploadCallback {
        /**
         * 上传成功
         * @param result 服务器返回的结果
         */
        void onSuccess(UploadInitModel result);

        /**
         * 上传失败
         * @param errorMsg 错误信息
         */
        void onFailure(String errorMsg);

        /**
         * 上传进度
         * @param currentChunk 当前上传的分片（从1开始）
         * @param totalChunks 总分片数
         */
        void onProgress(int currentChunk, int totalChunks);
    }

    public SobotChunkedUploadManager(Context context, ZhiChiApi zhiChiApi) {
        this(context, zhiChiApi, DEFAULT_CHUNK_SIZE);
    }

    public SobotChunkedUploadManager(Context context, ZhiChiApi zhiChiApi, long chunkSize) {
        this.context = context.getApplicationContext();
        this.zhiChiApi = zhiChiApi;
        this.chunkSize = chunkSize;
    }

    /**
     * 上传文件（自动分片）
     * @param file 要上传的文件
     * @param companyId 公司ID
     * @param callback 上传回调
     */
    public void uploadFile(File file, String companyId, UploadCallback callback) {
        if (file == null || !file.exists()) {
            notifyFailure(callback, "文件不存在");
            return;
        }

        long fileSize = file.length();
        LogUtils.d("文件大小：" + fileSize);
        if (fileSize == 0) {
            notifyFailure(callback, "文件大小为0");
            return;
        }

        int chunkCount = calculateChunkCount(fileSize);
        REQUEST_TAG = "SobotChunkedUploadManager" + chunkCount;
        // 初始化上传
        zhiChiApi.uploadInit(REQUEST_TAG, file.getName(), fileSize, chunkCount, 1, companyId,
                new SobotResultCallBack<UploadInitModel>() {
                    @Override
                    public void onSuccess(UploadInitModel uploadInitModel) {
                        if (uploadInitModel == null || TextUtils.isEmpty(uploadInitModel.getFileKey())) {
                            notifyFailure(callback, "上传初始化失败：返回数据为空");
                            return;
                        }

                        // 创建上传状态并开始上传
                        currentState = new UploadState(file, uploadInitModel.getFileKey(),
                                fileSize, chunkCount, companyId);
                        uploadChunks(currentState, 1, callback);
                    }

                    @Override
                    public void onFailure(Exception e, String des) {
                        String errorMsg = TextUtils.isEmpty(des) ?
                                (e != null ? e.getMessage() : "上传初始化失败") : des;
                        notifyFailure(callback, errorMsg);
                    }
                });
    }

    /**
     * 异常结束时，应调用此方法
     */
    public void finishUpload() {
        if (null != currentState) {
            uploadComplete(currentState, null);
        }
    }

    /**
     * 计算分片数量
     */
    private int calculateChunkCount(long fileSize) {

        return (int) (fileSize % chunkSize == 0 ? fileSize / chunkSize : fileSize / chunkSize + 1);
    }

    /**
     * 顺序上传分片（递归方式）
     * 每次只读取一个分片到内存，上传后立即释放
     */
    private void uploadChunks(UploadState state, int currentChunk, UploadCallback callback) {
        // 打开文件
        if (currentChunk == 1) {
            try {
                state.open();
            } catch (IOException e) {
                notifyFailure(callback, "打开文件失败: " + e.getMessage());
                return;
            }
        }

        // 所有分片上传完成
        if (currentChunk > state.totalChunks) {
            state.close();
            uploadComplete(state, callback);
            return;
        }

        // 上报进度
        notifyProgress(callback, currentChunk, state.totalChunks);

        // 读取当前分片数据
        final byte[] chunkData;
        try {
            chunkData = state.readChunk(currentChunk);
        } catch (IOException e) {
            state.close();
            notifyFailure(callback, "读取分片失败: " + e.getMessage());
            return;
        }

        // 上传当前分片
        String chunkName = state.fileName;
        zhiChiApi.uploadPart(REQUEST_TAG, state.fileKey, currentChunk, state.companyId, chunkName, chunkData,
                new SobotResultCallBack<String>() {
                    @Override
                    public void onSuccess(String result) {
                        // 分片数据立即释放（帮助GC）
                        // 继续上传下一个分片
                        uploadChunks(state, currentChunk + 1, callback);
                    }

                    @Override
                    public void onFailure(Exception e, String des) {
                        state.close();
                        String errorMsg = "第" + currentChunk + "/" + state.totalChunks + "个分片上传失败: " +
                                (TextUtils.isEmpty(des) ? (e != null ? e.getMessage() : "未知错误") : des);
                        finishUpload();
                        notifyFailure(callback, errorMsg);
                    }
                });
    }

    /**
     * 上传完成确认
     */
    private void uploadComplete(UploadState state, UploadCallback callback) {
        zhiChiApi.uploadComplete(REQUEST_TAG, state.fileKey, state.fileName, state.totalSize, state.companyId,
                new SobotResultCallBack<UploadInitModel>() {
                    @Override
                    public void onSuccess(UploadInitModel result) {
                        if (callback != null) {
                            notifySuccess(callback, result);
                        }
                    }

                    @Override
                    public void onFailure(Exception e, String des) {
                        String errorMsg = TextUtils.isEmpty(des) ?
                                (e != null ? e.getMessage() : "上传完成确认失败") : des;
                        if (callback != null) {
                            notifyFailure(callback, errorMsg);
                        }
                    }
                });
    }

    /**
     * 通知成功
     */
    private void notifySuccess(UploadCallback callback, UploadInitModel result) {
        if (callback != null) {
            callback.onSuccess(result);
        }
    }

    /**
     * 通知失败
     */
    private void notifyFailure(UploadCallback callback, String errorMsg) {
        if (callback != null) {
            callback.onFailure(errorMsg);
        }
    }

    /**
     * 通知进度
     */
    private void notifyProgress(UploadCallback callback, int currentChunk, int totalChunks) {
        if (callback != null) {
            callback.onProgress(currentChunk, totalChunks);
        }
    }
}
