package com.sobot.chat.widget.refresh.layout.listener;

import android.content.Context;

import androidx.annotation.NonNull;

import com.sobot.chat.widget.refresh.layout.api.RefreshFooter;
import com.sobot.chat.widget.refresh.layout.api.RefreshLayout;

/**
 * 默认Footer创建器
 * Created by scwang on 2018/1/26.
 */
public interface DefaultRefreshFooterCreator {
    @NonNull
    RefreshFooter createRefreshFooter(@NonNull Context context, @NonNull RefreshLayout layout);
}
