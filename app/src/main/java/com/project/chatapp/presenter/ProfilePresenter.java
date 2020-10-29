package com.project.chatapp.presenter;


import com.project.chatapp.data.source.UserRepository;

public class ProfilePresenter {

    private View view;
    private UserRepository userRepository;

    public ProfilePresenter(View view, UserRepository userRepository) {
        this.view = view;
        this.userRepository = userRepository;
    }

    public void logout() {
        userRepository.logout();
        view.logout();
    }

    public interface View {
        void logout();
    }
}

