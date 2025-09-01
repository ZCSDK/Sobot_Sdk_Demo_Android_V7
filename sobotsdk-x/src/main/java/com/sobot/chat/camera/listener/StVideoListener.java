package com.sobot.chat.camera.listener;

public interface StVideoListener {

    void onStart();
    void onPrepared();
    void onEnd();
    void onError();
    void onCancel();
}
