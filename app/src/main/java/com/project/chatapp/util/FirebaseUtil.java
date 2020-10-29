package com.project.chatapp.util;

import com.project.chatapp.service.AppFirebaseMessagingService;

public class FirebaseUtil {

    public static void sendCurrentToken() {
        AppFirebaseMessagingService.getCurrentDeviceToken();
    }
}
