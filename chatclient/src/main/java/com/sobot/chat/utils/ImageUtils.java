
package com.sobot.chat.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtils {

    public static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                        Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) &&
                (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            LogUtils.e("uncaught", e);
        }
        return degree;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
        if (bitmap == null)
            return null;

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix mtx = new Matrix();

        if (rotate != 0) {
            mtx.postRotate(rotate, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);
        }
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    /**
     * <br>功能简述:4.4及以上获取图片的方法
     * <br>功能详细描述:
     * <br>注意:
     *
     * @param context
     * @param uri
     * @return
     */
    public static String getPath(final Context context, final Uri uri) {
        if (context == null) {
            return "";
        }
        if (!(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)) {
            return uriToFileApiQ(context, uri);
        }
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    //使用应用专属外部存储目录，兼容 Android 10+
                    File externalFilesDir = context.getExternalFilesDir(null);
                    if (externalFilesDir != null) {
                        return externalFilesDir.getPath() + "/" + split[1];
                    }
                    return null;
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                String id = DocumentsContract.getDocumentId(uri);
                if (!TextUtils.isEmpty(id)) {
                    if (id.startsWith("raw:")) {
                        return id.replaceFirst("raw:", "");
                    }
                    try {
                        final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                        return getDataColumn(context, contentUri, null, null);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isNewGooglePhotosUri(uri)) {
                if (uri != null && !TextUtils.isEmpty(uri.getPath()) && uri.getPath().contains("video")) {
                    BufferedOutputStream outStream = null;
                    BufferedInputStream reader = null;
                    InputStream inputStream = null;
                    try {
                        ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
                        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                        inputStream = new FileInputStream(fileDescriptor);
                        reader = new BufferedInputStream(inputStream);
                        String picDir = SobotPathManager.getInstance().getVideoDir();
                        IOUtils.createFolder(picDir);
                        String videoFileName = "v_" + System.currentTimeMillis() + ".mp4";
                        String videoPath = picDir + videoFileName;
                        LogUtils.i(videoPath);
                        outStream = new BufferedOutputStream(new FileOutputStream(videoPath));
                        byte[] buf = new byte[2048];
                        int len;
                        while ((len = reader.read(buf)) > 0) {
                            outStream.write(buf, 0, len);
                        }
                        return videoPath;
                    } catch (Exception e) {
                        LogUtils.e("uncaught", e);
                    } finally {
                        try {
                            if (inputStream != null) {
                                inputStream.close();
                            }
                        } catch (IOException e) {
                            LogUtils.e("uncaught", e);
                        }
                        try {
                            if (reader != null) {
                                reader.close();
                            }
                        } catch (IOException e) {
                            LogUtils.e("uncaught", e);
                        }
                        try {
                            if (outStream != null) {
                                outStream.close();
                            }
                        } catch (IOException e) {
                            LogUtils.e("uncaught", e);
                        }

                    }
                }
                Uri imageUrlWithAuthority = getImageUrlWithAuthority(context, uri);
                if (imageUrlWithAuthority == null) {
                    return "";
                }
                return getDataColumn(context, imageUrlWithAuthority, null, null);
            }
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            String tmpDataPath;
            try {
                tmpDataPath = getDataColumn(context, uri, null, null);
            } catch (Exception e) {
                tmpDataPath = uri.getPath();
                // 使用应用专属外部存储目录，兼容 Android 10+
                if (!TextUtils.isEmpty(tmpDataPath)) {
                    try {
                        File externalFilesDir = context.getExternalFilesDir(null);
                        if (externalFilesDir != null) {
                            String rootpath = externalFilesDir.getPath();
                            if (rootpath.length() < tmpDataPath.length()) {
                                int indexOf = tmpDataPath.indexOf(rootpath);
                                if (indexOf != -1) {
                                    tmpDataPath = tmpDataPath.substring(indexOf);
                                }
                            }
                        }
                    } catch (Exception exce) {
                        LogUtils.e("uncaught", exce);
                    }
                }
            }
            return tmpDataPath;
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * 异步获取文件路径的方法
     *
     * @param context  上下文
     * @param uri      URI对象
     * @param callback 回调接口，用于返回结果
     */
    public static void getPathAsync(final Context context, final Uri uri, final OnPathCallback callback) {
        if (context == null) {
            if (callback != null) {
                callback.onResult("");
            }
            return;
        }

        new Thread(() -> {
            String result;
            if (!(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)) {
                result = uriToFileApiQ(context, uri);
            } else {
                // DocumentProvider
                if (DocumentsContract.isDocumentUri(context, uri)) {
                    // ExternalStorageProvider
                    if (isExternalStorageDocument(uri)) {
                        final String docId = DocumentsContract.getDocumentId(uri);
                        final String[] split = docId.split(":");
                        final String type = split[0];

                        //使用应用专属外部存储目录，兼容 Android 10+
                        if ("primary".equalsIgnoreCase(type)) {
                            File externalFilesDir = context.getExternalFilesDir(null);
                            result = externalFilesDir != null ?
                                    externalFilesDir.getPath() + "/" + split[1] : null;
                        } else {
                            result = null;
                        }
                    }
                    // DownloadsProvider
                    else if (isDownloadsDocument(uri)) {
                        String id = DocumentsContract.getDocumentId(uri);
                        if (!TextUtils.isEmpty(id)) {
                            if (id.startsWith("raw:")) {
                                result = id.replaceFirst("raw:", "");
                            } else {
                                try {
                                    final Uri contentUri = ContentUris.withAppendedId(
                                            Uri.parse("content://downloads/public_downloads"),
                                            Long.valueOf(id));
                                    result = getDataColumn(context, contentUri, null, null);
                                } catch (NumberFormatException e) {
                                    result = null;
                                }
                            }
                        } else {
                            result = null;
                        }
                    }
                    // MediaProvider
                    else if (isMediaDocument(uri)) {
                        final String docId = DocumentsContract.getDocumentId(uri);
                        final String[] split = docId.split(":");
                        final String type = split[0];

                        Uri contentUri = null;
                        if ("image".equals(type)) {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        } else if ("video".equals(type)) {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        } else if ("audio".equals(type)) {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        }

                        final String selection = "_id=?";
                        final String[] selectionArgs = new String[]{split[1]};

                        result = getDataColumn(context, contentUri, selection, selectionArgs);
                    } else {
                        result = null;
                    }
                }
                // MediaStore (and general)
                else if ("content".equalsIgnoreCase(uri.getScheme())) {
                    if (isNewGooglePhotosUri(uri)) {
                        if (uri != null && !TextUtils.isEmpty(uri.getPath()) && uri.getPath().contains("video")) {
                            BufferedOutputStream outStream = null;
                            BufferedInputStream reader = null;
                            InputStream inputStream = null;
                            try {
                                ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver()
                                        .openFileDescriptor(uri, "r");
                                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                                inputStream = new FileInputStream(fileDescriptor);
                                reader = new BufferedInputStream(inputStream);
                                String picDir = SobotPathManager.getInstance().getVideoDir();
                                IOUtils.createFolder(picDir);
                                String videoFileName = "v_" + System.currentTimeMillis() + ".mp4";
                                String videoPath = picDir + videoFileName;
                                LogUtils.i(videoPath);
                                outStream = new BufferedOutputStream(new FileOutputStream(videoPath));
                                byte[] buf = new byte[2048];
                                int len;
                                while ((len = reader.read(buf)) > 0) {
                                    outStream.write(buf, 0, len);
                                }
                                result = videoPath;
                            } catch (Exception e) {
                                LogUtils.e("uncaught", e);
                                result = null;
                            } finally {
                                try {
                                    if (inputStream != null) {
                                        inputStream.close();
                                    }
                                } catch (IOException e) {
                                    LogUtils.e("uncaught", e);
                                }
                                try {
                                    if (reader != null) {
                                        reader.close();
                                    }
                                } catch (IOException e) {
                                    LogUtils.e("uncaught", e);
                                }
                                try {
                                    if (outStream != null) {
                                        outStream.close();
                                    }
                                } catch (IOException e) {
                                    LogUtils.e("uncaught", e);
                                }
                            }
                        } else {
                            Uri imageUrlWithAuthority = getImageUrlWithAuthority(context, uri);
                            result = imageUrlWithAuthority != null ?
                                    getDataColumn(context, imageUrlWithAuthority, null, null) : "";
                        }
                    } else if (isGooglePhotosUri(uri)) {
                        result = uri.getLastPathSegment();
                    } else {
                        String tmpDataPath;
                        try {
                            tmpDataPath = getDataColumn(context, uri, null, null);
                        } catch (Exception e) {
                            tmpDataPath = uri.getPath();
                            //使用应用专属外部存储目录，兼容 Android 10+
                            if (!TextUtils.isEmpty(tmpDataPath)) {
                                try {
                                    File externalFilesDir = context.getExternalFilesDir(null);
                                    if (externalFilesDir != null) {
                                        String rootpath = externalFilesDir.getPath();
                                        if (rootpath.length() < tmpDataPath.length()) {
                                            int indexOf = tmpDataPath.indexOf(rootpath);
                                            if (indexOf != -1) {
                                                tmpDataPath = tmpDataPath.substring(indexOf);
                                            }
                                        }
                                    }
                                } catch (Exception exce) {
                                    LogUtils.e("uncaught", exce);
                                }
                            }
                        }
                        result = tmpDataPath;
                    }
                } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                    result = uri.getPath();
                } else {
                    result = null;
                }
            }

            if (callback != null) {
                Handler mainHandler = new Handler(Looper.getMainLooper());
                String finalResult = result;
                mainHandler.post(() -> callback.onResult(finalResult));
            }
        }).start();
    }

    /**
     * 通过URI获取文件大小
     *
     * @param context 上下文
     * @param uri     文件URI
     * @return 文件大小（字节数），如果无法获取则返回-1
     */
    public static long getFileSizeFromUri(Context context, Uri uri) {
        if (context == null || uri == null) {
            return -1;
        }

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;

        try {
            cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex >= 0) {
                    return cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            LogUtils.e("uncaught", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            File file = new File(uri.getPath());
            return file.exists() ? file.length() : -1;
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            String authority = uri.getAuthority();
            if (authority != null) {
                if (authority.startsWith("com.android.providers.media")) {
                    try {
                        cursor = contentResolver.query(uri, new String[]{MediaStore.MediaColumns.SIZE}, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            int sizeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE);
                            if (sizeIndex >= 0) {
                                return cursor.getLong(sizeIndex);
                            }
                        }
                    } catch (Exception e) {
                        LogUtils.e("uncaught", e);
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            }

            try {
                ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    long size = new File(fd.toString()).length();
                    pfd.close();
                    return size;
                }
            } catch (Exception e) {
            }
        }

        return -1;
    }

    /**
     * 异步获取文件大小
     *
     * @param context  上下文
     * @param uri      文件URI
     * @param callback 回调接口，用于返回结果
     */
    public static void getFileSizeFromUriAsync(Context context, Uri uri, OnFileSizeCallback callback) {
        new Thread(() -> {
            long result = getFileSizeFromUri(context, uri);
            if (callback != null) {
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> callback.onResult(result));
            }
        }).start();
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static boolean isNewGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.contentprovider".equals(uri.getAuthority());
    }

    public static Uri getImageUrlWithAuthority(Context context, Uri uri) {
        InputStream is = null;
        if (uri.getAuthority() != null && context.getContentResolver() != null) {
            try {
                is = context.getContentResolver().openInputStream(uri);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                return writeToTempImageAndGetPathUri(context, bmp);
            } catch (FileNotFoundException e) {
                LogUtils.e("uncaught", e);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    LogUtils.e("uncaught", e);
                }
            }
        }
        return null;
    }

    // CWE-1108: MediaStore.Images.Media.insertImage 在 API 29+ 已废弃，OEM 上行为不稳定。
    // 改用 ContentValues + openOutputStream + IS_PENDING（Q+）/ 旧 ContentResolver.insert（pre-Q）。
    public static Uri writeToTempImageAndGetPathUri(Context inContext, Bitmap inImage) {
        if (inContext == null || inContext.getContentResolver() == null || inImage == null) {
            return null;
        }
        String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                    android.os.Environment.DIRECTORY_PICTURES + "/Sobot");
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);
        }
        Uri uri = inContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            return null;
        }
        java.io.OutputStream out = null;
        try {
            out = inContext.getContentResolver().openOutputStream(uri);
            if (out != null) {
                inImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear();
                values.put(MediaStore.MediaColumns.IS_PENDING, 0);
                inContext.getContentResolver().update(uri, values, null, null);
            }
            return uri;
        } catch (Exception e) {
            LogUtils.e("writeToTempImage failed", e);
            return null;
        } finally {
            try {
                if (out != null) out.close();
            } catch (Exception ignore) {
            }
        }
    }

    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            LogUtils.e("uncaught", e);
        }
        return null;
    }

    public static Uri getImageContentUri(Context context, String path) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID}, MediaStore.Images.Media.DATA + "=? ",
                new String[]{path}, null);
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (new File(path).exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, path);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public static Uri getMediaUriFromPath(Context context, String path) {
        Uri mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(mediaUri,
                null,
                null,
                null,
                null);

        Uri uri = null;
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                int index = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                if (index != -1) {
                    String path1 = cursor.getString(index);
                    LogUtils.e("path1 ==================> " + path1);
                    if (path1.equals(path)) {
                        uri = ContentUris.withAppendedId(mediaUri,
                                cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID)));
                        return uri;
                    }
                }

            }
        } else {
            return null;
        }
        cursor.close();
        return null;
    }

    public static Uri getUri(android.content.Intent intent, Context context) {
        Uri uri = intent.getData();
        String type = intent.getType();
        if (uri.getScheme().equals("file") && (type.contains("image/*"))) {
            String path = uri.getEncodedPath();
            if (path != null) {
                path = Uri.decode(path);
                ContentResolver cr = context.getContentResolver();
                StringBuffer buff = new StringBuffer();
                buff.append("(").append(MediaStore.Images.ImageColumns.DATA).append("=")
                        .append("'" + path + "'").append(")");
                Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.Images.ImageColumns._ID},
                        buff.toString(), null, null);
                int index = 0;
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                    index = cur.getInt(index);
                }
                if (index == 0) {
                } else {
                    Uri uri_temp = Uri.parse("content://media/external/images/media/" + index);
                    if (uri_temp != null) {
                        uri = uri_temp;
                    }
                }
            }
        }
        return uri;
    }

    /**
     * Android 10 以上适配
     *
     * @param context
     * @param uri
     * @return
     */
    private static String uriToFileApiQ(Context context, Uri uri) {
        File file = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            file = new File(uri.getPath());
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            try {
                ContentResolver contentResolver = context.getContentResolver();
                Cursor cursor = contentResolver.query(uri, null, null, null, null);
                if (cursor.moveToFirst()) {
                    int cIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (cIndex > -1) {
                        String displayName = cursor.getString(cIndex);
                        if (!TextUtils.isEmpty(displayName)) {
                            // 安全：displayName 来自外部 ContentProvider，强制取 basename，
                            // 防御 "/"、"\\"、".." 等路径分隔符注入（CWE-22）。空/".." 兜底为 UUID。
                            displayName = new File(displayName).getName();
                            if (TextUtils.isEmpty(displayName) || "..".equals(displayName) || ".".equals(displayName)) {
                                displayName = java.util.UUID.randomUUID().toString();
                            }
                            InputStream is = null;
                            FileOutputStream fos = null;
                            try {
                                is = contentResolver.openInputStream(uri);
                                // 添加空指针保护
                                File externalCacheDir = context.getExternalCacheDir();
                                if (externalCacheDir != null) {
                                    File cache = new File(externalCacheDir.getAbsolutePath(), System.currentTimeMillis() + displayName);
                                    fos = new FileOutputStream(cache);
                                    IOUtils.copyFileWithStream(fos, is);
                                    file = cache;
                                }
                            } catch (IOException e) {
                                LogUtils.e("uncaught", e);
                            } finally {
                                try {
                                    if (cursor != null) {
                                        cursor.close();
                                    }
                                    if (fos != null) {
                                        fos.close();
                                    }
                                } catch (IOException e) {
                                    LogUtils.e("uncaught", e);
                                }
                                try {
                                    if (is != null) {
                                        is.close();
                                    }
                                } catch (IOException e) {
                                    LogUtils.e("uncaught", e);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LogUtils.e("uncaught", e);
            }
        }
        if (file == null) {
            return null;
        } else {
            return file.getAbsolutePath();
        }
    }

    /**
     * Android Q及以上版本Uri转File路径的方法（异步方式）
     */
    public static void uriToFileApiQAsync(Context context, Uri uri, OnUriToFileCallback callback) {
        new Thread(() -> {
            String result = uriToFileApiQ(context, uri);
            if (callback != null) {
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> callback.onResult(result));
            }
        }).start();
    }

    public interface OnUriToFileCallback {
        void onResult(String filePath);
    }

    public interface OnPathCallback {
        void onResult(String path);
    }

    public interface OnFileSizeCallback {
        void onResult(long fileSize);
    }
}