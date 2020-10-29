package com.project.chatapp.presenter;

import com.project.chatapp.data.model.User;
import com.project.chatapp.data.source.UserRepository;

import java.util.List;

public class SelectContactPresenter {
    private View view;
    private UserRepository userRepository;

    public SelectContactPresenter(View view, UserRepository userRepository) {
        this.view = view;
        this.userRepository = userRepository;
    }

    public void loadContacts(int page, int limit, String query) {
        userRepository.getUsers(page, limit, query, users -> {
                view.showContacts(users);
                }, throwable -> {
                    view.showErrorMessage(throwable.getMessage());
                });
    }

    public void selectContacts(List<User> selectedContacts) {
        if (selectedContacts.isEmpty()) {
            view.showErrorMessage("Please select at least one contact!");
            return;
        }
        view.showCreateGroupPage(selectedContacts);
    }

    public interface View {
        void showContacts(List<User> contacts);

        void showCreateGroupPage(List<User> members);

        void showErrorMessage(String errorMessage);
    }
}
