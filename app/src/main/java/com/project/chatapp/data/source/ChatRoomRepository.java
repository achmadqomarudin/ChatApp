package com.project.chatapp.data.source;

import com.project.chatapp.data.model.User;
import com.project.chatapp.util.Action;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;

import java.util.List;

public interface ChatRoomRepository {
    void getChatRooms(Action<List<QiscusChatRoom>> onSuccess, Action<Throwable> onError);

    void createChatRoom(User user, Action<QiscusChatRoom> onSuccess, Action<Throwable> onError);

    void createGroupChatRoom(String name, List<User> members, Action<QiscusChatRoom> onSuccess, Action<Throwable> onError);

    void addParticipant(long roomId, List<User> members, Action<Void> onSuccess, Action<Throwable> onError);

    void removeParticipant(long roomId, List<User> members, Action<Void> onSuccess, Action<Throwable> onError);
}
