package com.project.chatapp.presenter;

import com.project.chatapp.data.model.User;
import com.project.chatapp.data.source.ChatRoomRepository;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;

import java.util.List;

public class CreateGroupPresenter {
    private View view;
    private ChatRoomRepository chatRoomRepository;

    public CreateGroupPresenter(View view, ChatRoomRepository chatRoomRepository) {
        this.view = view;
        this.chatRoomRepository = chatRoomRepository;
    }

    public void createGroup(String name, List<User> members) {
        view.showLoading();
        chatRoomRepository.createGroupChatRoom(name, members,
                chatRoom -> {
                    view.dismissLoading();
                    view.showGroupChatRoomPage(chatRoom);
                },
                throwable -> {
                    view.dismissLoading();
                    view.showErrorMessage(throwable.getMessage());
                });
    }

    public interface View {
        void showGroupChatRoomPage(QiscusChatRoom chatRoom);

        void showLoading();

        void dismissLoading();

        void showErrorMessage(String errorMessage);
    }
}
