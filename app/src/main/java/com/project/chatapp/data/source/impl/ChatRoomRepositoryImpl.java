package com.project.chatapp.data.source.impl;

import com.project.chatapp.data.model.User;
import com.project.chatapp.data.source.ChatRoomRepository;
import com.project.chatapp.util.Action;
import com.project.chatapp.util.AvatarUtil;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ChatRoomRepositoryImpl implements ChatRoomRepository {

    @Override
    public void getChatRooms(Action<List<QiscusChatRoom>> onSuccess, Action<Throwable> onError) {
        Observable.from(QiscusCore.getDataStore().getChatRooms(100))
                .filter(chatRoom -> chatRoom.getLastComment() != null)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess::call, onError::call);

        QiscusApi.getInstance()
                .getAllChatRooms(true, false, true, 1, 100)
                .flatMap(Observable::from)
                .doOnNext(qiscusChatRoom -> QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom))
                .filter(chatRoom -> chatRoom.getLastComment().getId() != 0)
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess::call, onError::call);
    }

    @Override
    public void createChatRoom(User user, Action<QiscusChatRoom> onSuccess, Action<Throwable> onError) {
        QiscusChatRoom savedChatRoom = QiscusCore.getDataStore().getChatRoom(user.getId());
        if (savedChatRoom != null) {
            onSuccess.call(savedChatRoom);
            return;
        }

        QiscusApi.getInstance()
                .chatUser(user.getId(), null)
                .doOnNext(chatRoom -> QiscusCore.getDataStore().addOrUpdate(chatRoom))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess::call, onError::call);
    }

    @Override
    public void createGroupChatRoom(String name, List<User> members, Action<QiscusChatRoom> onSuccess, Action<Throwable> onError) {
        List<String> ids = new ArrayList<>();
        for (User member : members) {
            ids.add(member.getId());
        }

        QiscusApi.getInstance()
                .createGroupChat(name, ids, AvatarUtil.generateAvatar(name), null)
                .doOnNext(chatRoom -> QiscusCore.getDataStore().addOrUpdate(chatRoom))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess::call, onError::call);
    }

    @Override
    public void addParticipant(long roomId, List<User> members, Action<Void> onSuccess, Action<Throwable> onError) {
        List<String> ids = new ArrayList<>();
        for (User member : members) {
            ids.add(member.getId());
        }
        QiscusApi.getInstance().addParticipants(roomId, ids)
                .doOnNext(chatRoom -> QiscusCore.getDataStore().addOrUpdate(chatRoom))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRoom -> onSuccess.call(null), onError::call);
    }

    @Override
    public void removeParticipant(long roomId, List<User> members, Action<Void> onSuccess, Action<Throwable> onError) {
        List<String> ids = new ArrayList<>();
        for (User member : members) {
            ids.add(member.getId());
        }
        QiscusApi.getInstance().addParticipants(roomId, ids)
                .doOnNext(chatRoom -> QiscusCore.getDataStore().addOrUpdate(chatRoom))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chatRoom -> onSuccess.call(null), onError::call);
    }

}
