package com.sobot.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sobot.chat.R;
import com.sobot.chat.api.model.SobotFileModel;
import com.sobot.chat.utils.CommonUtils;
import com.sobot.chat.utils.LogUtils;
import com.sobot.chat.widget.attachment.FileTypeConfig;
import com.sobot.chat.widget.image.SobotProgressImageView;

import java.util.ArrayList;

/**
 * 附件 适配器
 */
public class SobotUploadFileAdapter extends RecyclerView.Adapter<SobotUploadFileAdapter.ViewHolder> {

    private Context context;
    private ArrayList<SobotFileModel> arrayList;
    private Listener clickListener;
    private boolean isEdit = false;

    public SobotUploadFileAdapter(Context context, ArrayList<SobotFileModel> arrayList, boolean isEdit, Listener clickListener) {
        this.context = context;
        this.arrayList = arrayList;
        this.clickListener = clickListener;
        this.isEdit = isEdit;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.sobot_item_upload_file, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final SobotFileModel fileModel = arrayList.get(i);
        viewHolder.sobot_file_name.setText(fileModel.getFileName());
        LogUtils.e(i + "\t" + fileModel.getFileType() + "\t" + fileModel.getFileUrl());
//        viewHolder.sobot_file_size.setText(fileModel.getf);
        final int type=FileTypeConfig.getFileType(fileModel.getFileType());
        if (FileTypeConfig.getFileType(fileModel.getFileType()) == FileTypeConfig.MSGTYPE_FILE_PIC || FileTypeConfig.getFileType(fileModel.getFileType()) == FileTypeConfig.MSGTYPE_FILE_MP4) {
            viewHolder.sobot_file_thumbnail.setImageUrlWithScaleType(CommonUtils.encode(fileModel.getFileUrl()),ImageView.ScaleType.CENTER_INSIDE);
        } else {
            viewHolder.sobot_file_thumbnail.setImageLocal(FileTypeConfig.getFileIcon(context, type),ImageView.ScaleType.CENTER_INSIDE);
        }
        viewHolder.sobot_file_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.deleteFile(fileModel);
            }
        });
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type == FileTypeConfig.MSGTYPE_FILE_MP4) {
                    clickListener.previewMp4(fileModel);
                } else if (type == FileTypeConfig.MSGTYPE_FILE_PIC) {
                    clickListener.previewPic(fileModel.getFileUrl(), fileModel.getFileName());
                } else {
                    clickListener.downFileLister(fileModel);
                }
            }
        });
        if (isEdit) {
            viewHolder.sobot_file_delete.setVisibility(View.VISIBLE);
        } else {
            viewHolder.sobot_file_delete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (null == arrayList) {
            return 0;
        }
        return arrayList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private SobotProgressImageView sobot_file_thumbnail;
        private ImageView sobot_file_delete;
        private TextView sobot_file_name;
        private TextView sobot_file_size;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sobot_file_delete = itemView.findViewById(R.id.sobot_file_delete);
            sobot_file_thumbnail = itemView.findViewById(R.id.sobot_file_thumbnail);
            sobot_file_name = itemView.findViewById(R.id.sobot_file_name);
            sobot_file_size = itemView.findViewById(R.id.sobot_file_size);
        }
    }

    public interface Listener {
        void downFileLister(SobotFileModel model);
        void previewMp4(SobotFileModel fileModel);
        void deleteFile(SobotFileModel fileModel);

        void previewPic(String fileUrl, String fileName);
    }
}

