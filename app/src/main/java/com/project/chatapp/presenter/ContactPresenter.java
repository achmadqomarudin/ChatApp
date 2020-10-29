package com.project.chatapp.presenter;

import com.project.chatapp.data.model.User;
import com.project.chatapp.data.source.ChatRoomRepository;
import com.project.chatapp.data.source.UserRepository;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ContactPresenter {
    private View view;
    private UserRepository userRepository;
    private ChatRoomRepository chatRoomRepository;
    private List<User> users;

    public ContactPresenter(View view, UserRepository userRepository, ChatRoomRepository chatRoomRepository) {
        this.view = view;
        this.userRepository = userRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    public void loadContacts(long page, int limit, String query) {
        userRepository.getUsers(page, limit, query, users -> {
            view.showContacts(users);
            this.users = users;
        }, throwable -> {
            view.showErrorMessage(throwable.getMessage());
        });
    }

    public void search(String keyword) {
        Observable.from(users)
                .filter(user -> user.getName().toLowerCase().contains(keyword.toLowerCase()))
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(users -> view.showContacts(users), throwable -> view.showErrorMessage(throwable.getMessage()));

    }

    public void createRoom(User contact) {
        chatRoomRepository.createChatRoom(contact,
                chatRoom -> view.showChatRoomPage(chatRoom),
                throwable -> view.showErrorMessage(throwable.getMessage()));
    }

    public interface View {
        void showContacts(List<User> contacts);

        void showChatRoomPage(QiscusChatRoom chatRoom);

        void showErrorMessage(String errorMessage);
    }
}
