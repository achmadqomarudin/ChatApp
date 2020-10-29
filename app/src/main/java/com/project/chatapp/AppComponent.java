package com.project.chatapp;

import android.content.Context;

import com.project.chatapp.data.source.ChatRoomRepository;
import com.project.chatapp.data.source.UserRepository;
import com.project.chatapp.data.source.impl.ChatRoomRepositoryImpl;
import com.project.chatapp.data.source.impl.UserRepositoryImpl;

public class AppComponent {
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    AppComponent(Context context) {
        userRepository = new UserRepositoryImpl(context);
        chatRoomRepository = new ChatRoomRepositoryImpl();
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public ChatRoomRepository getChatRoomRepository() {
        return chatRoomRepository;
    }
}
