package com.project.chatapp;

import androidx.multidex.MultiDexApplication;

import com.project.chatapp.util.PushNotificationUtil;
import com.qiscus.jupuk.Jupuk;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.one.EmojiOneProvider;

public class MyApplication extends MultiDexApplication {
    private static MyApplication instance;
    private AppComponent component;
    public static MyApplication getInstance() {
        return instance;
    }

    private static void initEmoji() {
        EmojiManager.install(new EmojiOneProvider());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        component = new AppComponent(this);

        Nirmana.init(this);
        QiscusCore.setup(this, BuildConfig.QISCUS_SDK_APP_ID);

        QiscusCore.getChatConfig()
                .enableDebugMode(true)
                .setNotificationListener(PushNotificationUtil::showNotification)
                .setEnableFcmPushNotification(true);
        initEmoji();
        Jupuk.init(this);
    }

    public AppComponent getComponent() {
        return component;
    }
}
