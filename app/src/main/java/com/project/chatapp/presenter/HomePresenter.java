package com.project.chatapp.presenter;

import com.project.chatapp.data.source.ChatRoomRepository;
import com.project.chatapp.data.source.UserRepository;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;

import java.util.List;

public class HomePresenter {
    private View view;
    private ChatRoomRepository chatRoomRepository;
    private UserRepository userRepository;

    public HomePresenter(View view, ChatRoomRepository chatRoomRepository, UserRepository userRepository) {
        this.view = view;
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
    }

    public void loadChatRooms() {
        chatRoomRepository.getChatRooms(chatRooms -> view.showChatRooms(chatRooms),
                throwable -> view.showErrorMessage(throwable.getMessage()));
    }

    public void openChatRoom(QiscusChatRoom chatRoom) {
        if (chatRoom.isGroup()) {
            view.showGroupChatRoomPage(chatRoom);
            return;
        }
        view.showChatRoomPage(chatRoom);
    }

    public interface View {
        void showChatRooms(List<QiscusChatRoom> chatRooms);

        void showChatRoomPage(QiscusChatRoom chatRoom);

        void showGroupChatRoomPage(QiscusChatRoom chatRoom);

        void showErrorMessage(String errorMessage);
    }
}
