package com.project.chatapp.data.source.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.project.chatapp.R;
import com.project.chatapp.data.model.User;
import com.project.chatapp.data.source.UserRepository;
import com.project.chatapp.util.Action;
import com.project.chatapp.util.AvatarUtil;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import rx.Emitter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class UserRepositoryImpl implements UserRepository {

    private Context context;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public UserRepositoryImpl(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    @Override
    public void login(String email, String password, String name, Action<User> onSuccess, Action<Throwable> onError) {
        QiscusCore.setUser(email, password)
                .withUsername(name)
                .withAvatarUrl(AvatarUtil.generateAvatar(name))
                .save()
                .map(this::mapFromQiscusAccount)
                .doOnNext(this::setCurrentUser)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess::call, onError::call);
    }

    @Override
    public void getCurrentUser(Action<User> onSuccess, Action<Throwable> onError) {
        getCurrentUserObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess::call, onError::call);
    }

    @Override
    public void getUsers(long page, int limit, String searchUsername, Action<List<User>> onSuccess, Action<Throwable> onError) {
        QiscusApi.getInstance().getUsers(searchUsername, page, limit)
                .flatMap(Observable::from)
                .filter(user -> !user.equals(getCurrentUser()))
                .filter(user -> !user.getUsername().equals(""))
                .map(this::mapFromQiscusAccount)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess::call, onError::call);

    }

    @Override
    public void updateProfile(String name, Action<User> onSuccess, Action<Throwable> onError) {
        QiscusCore.updateUserAsObservable(name, getCurrentUser().getAvatarUrl())
                .map(this::mapFromQiscusAccount)
                .doOnNext(this::setCurrentUser)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess::call, onError::call);
    }

    @Override
    public void logout() {
        QiscusCore.removeDeviceToken(getCurrentDeviceToken());
        QiscusCore.clearUser();
        sharedPreferences.edit().clear().apply();
    }

    @Override
    public void setDeviceToken(String token) {
        setCurrentDeviceToken(token);
    }

    private Observable<User> getCurrentUserObservable() {
        return Observable.create(subscriber -> {
            try {
                subscriber.onNext(getCurrentUser());
            } catch (Exception e) {
                subscriber.onError(e);
            } finally {
                subscriber.onCompleted();
            }
        }, Emitter.BackpressureMode.BUFFER);
    }

    private User getCurrentUser() {
        return gson.fromJson(sharedPreferences.getString("current_user", ""), User.class);
    }

    private void setCurrentUser(User user) {
        sharedPreferences.edit()
                .putString("current_user", gson.toJson(user))
                .apply();
    }

    private String getCurrentDeviceToken() {
        return sharedPreferences.getString("current_device_token", "");
    }

    private void setCurrentDeviceToken(String token) {
        sharedPreferences.edit()
                .putString("current_device_token", token)
                .apply();
    }

    private Observable<List<User>> getUsersObservable() {
        return Observable.create(subscriber -> {
            try {
                List<User> users = gson.fromJson(getUsersData(), new TypeToken<List<User>>() {
                }.getType());
                subscriber.onNext(users);
            } catch (Exception e) {
                subscriber.onError(e);
            } finally {
                subscriber.onCompleted();
            }
        }, Emitter.BackpressureMode.BUFFER);
    }

    private String getUsersData() throws IOException, JSONException {
        Resources resources = context.getResources();
        InputStream inputStream = resources.openRawResource(R.raw.users);

        byte[] bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        return new String(bytes);
    }

    private User mapFromQiscusAccount(QiscusAccount qiscusAccount) {
        User user = new User();
        user.setId(qiscusAccount.getEmail());
        user.setName(qiscusAccount.getUsername());
        user.setAvatarUrl(qiscusAccount.getAvatar());
        return user;
    }
}
