package com.sobot.chat.widget.refresh.layout.listener;

import androidx.annotation.NonNull;

import com.sobot.chat.widget.refresh.layout.api.RefreshLayout;

/**
 * 加载更多监听器
 * Created by scwang on 2017/5/26.
 */
public interface OnLoadMoreListener {
    void onLoadMore(@NonNull RefreshLayout refreshLayout);
}
