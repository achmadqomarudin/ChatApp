package com.project.chatapp.presenter;


import com.project.chatapp.data.source.UserRepository;

public class LoginPresenter {
    private View view;
    private UserRepository userRepository;

    public LoginPresenter(View view, UserRepository userRepository) {
        this.view = view;
        this.userRepository = userRepository;
    }

    public void start() {
        userRepository.getCurrentUser(user -> {
            if (user != null) {
                view.showHomePage();
            }
        }, throwable -> view.showErrorMessage(throwable.getMessage()));
    }

    public void login(String email, String password, String name) {
        view.showLoading();
        userRepository.login(email, password, name,
                user -> {
                    view.dismissLoading();
                    view.showHomePage();
                },
                throwable -> {
                    view.dismissLoading();
                    view.showErrorMessage(throwable.getMessage());
                });
    }

    public interface View {
        void showHomePage();

        void showLoading();

        void dismissLoading();

        void showErrorMessage(String errorMessage);
    }
}
