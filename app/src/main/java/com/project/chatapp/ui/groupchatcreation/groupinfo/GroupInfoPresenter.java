package com.project.chatapp.ui.groupchatcreation.groupinfo;

import com.project.chatapp.data.model.User;
import com.project.chatapp.data.source.ChatRoomRepository;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;

import java.util.List;

public class GroupInfoPresenter {
    private View view;
    private ChatRoomRepository chatRoomRepository;

    public GroupInfoPresenter(View view, ChatRoomRepository chatRoomRepository) {
        this.view = view;
        this.chatRoomRepository = chatRoomRepository;
    }

    public void createGroup(String name, List<User> members) {
        view.showLoading();
        chatRoomRepository.createGroupChatRoom(name, members,
                qiscusChatRoom -> {
                    view.dismissLoading();
                    view.showGroupChatRoomPage(qiscusChatRoom);
                },
                throwable -> {
                    view.dismissLoading();
                    view.showErrorMessage(throwable.getMessage());
                }
        );
    }

    public interface View {
        void showLoading();

        void dismissLoading();

        void showGroupChatRoomPage(QiscusChatRoom chatRoom);

        void showErrorMessage(String errorMessage);
    }
}
