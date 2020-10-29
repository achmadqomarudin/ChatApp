package com.project.chatapp.ui.addmember;

import com.project.chatapp.data.model.User;
import com.project.chatapp.data.source.ChatRoomRepository;
import com.project.chatapp.data.source.UserRepository;
import com.project.chatapp.ui.groupchatcreation.SelectableUser;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddGroupMemberPresenter {
    private View view;
    private UserRepository userRepository;
    private ChatRoomRepository chatRoomRepository;
    private List<QiscusRoomMember> members;
    private List<SelectableUser> contacts;

    public AddGroupMemberPresenter(View view,
                                   UserRepository userRepository,
                                   ChatRoomRepository chatRoomRepository,
                                   List<QiscusRoomMember> members) {
        this.view = view;
        this.userRepository = userRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.members = members;
        contacts = new ArrayList<>();
    }

    public void loadContacts(long page, int limit, String query) {
        userRepository.getUsers(page, limit, query, users -> {
            for (User user : users) {
                SelectableUser selectableUser = new SelectableUser(user);
                int index = contacts.indexOf(selectableUser);
                if (index >= 0) {
                    contacts.get(index).setUser(user);
                } else {
                    contacts.add(selectableUser);
                }
            }
            view.showContacts(contacts);
        }, throwable -> {
            view.showErrorMessage(throwable.getMessage());
        });
    }

    public void search(String keyword) {
        Observable.from(contacts)
                .filter(user -> user.getUser().getName().toLowerCase().contains(keyword.toLowerCase()))
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(users -> view.showContacts(users), throwable -> view.showErrorMessage(throwable.getMessage()));

    }

    public void selectContact(SelectableUser contact) {
        int index = contacts.indexOf(contact);
        if (index >= 0) {
            contacts.get(index).setSelected(!contacts.get(index).isSelected());
            view.onSelectedContactChange(contacts.get(index));
        }
    }

    public void loadContacts(int page, int limit, String query) {
        userRepository.getUsers(page, limit, query, users -> {
                    Observable.from(users)
                            .filter(user -> {
                                QiscusRoomMember member = new QiscusRoomMember();
                                member.setEmail(user.getId());
                                return !members.contains(member);
                            })
                            .toList()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(newUsers -> {
                                        for (User user : users) {
                                            SelectableUser selectableUser = new SelectableUser(user);
                                            int index = contacts.indexOf(selectableUser);
                                            if (index >= 0) {
                                                contacts.get(index).setUser(user);
                                            } else {
                                                contacts.add(selectableUser);
                                            }
                                        }
                                        view.showContacts(contacts);
                                    },
                                    throwable -> view.showErrorMessage(throwable.getMessage()));
                },
                throwable -> view.showErrorMessage(throwable.getMessage())
        );
    }


    public void addParticipant(long roomId, List<User> participants) {
        view.showLoading();

        chatRoomRepository.addParticipant(roomId, participants, aVoid -> {
            view.onParticipantAdded(participants);
            view.dismissLoading();
        }, throwable -> {
            view.showErrorMessage(throwable.getMessage());
            view.dismissLoading();
        });
    }

    public interface View {

        void showContacts(List<SelectableUser> contacts);

        void onParticipantAdded(List<User> user);

        void showLoading();

        void dismissLoading();

        void onSelectedContactChange(SelectableUser selectableUser);

        void showErrorMessage(String errorMessage);
    }
}
