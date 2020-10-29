package com.project.chatapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.project.chatapp.MyApplication;
import com.project.chatapp.R;
import com.project.chatapp.presenter.HomePresenter;
import com.project.chatapp.ui.adapter.ChatRoomAdapter;
import com.project.chatapp.ui.adapter.OnItemClickListener;
import com.project.chatapp.util.FirebaseUtil;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.event.QiscusCommentReceivedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements HomePresenter.View, OnItemClickListener {
    private RecyclerView recyclerView;
    private LinearLayout linEmptyChatRooms;
    private ChatRoomAdapter chatRoomAdapter;
    private ImageView createChat, avatarProfile;
    private Button btStartChat;
    private HomePresenter homePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseUtil.sendCurrentToken();

        linEmptyChatRooms = findViewById(R.id.linEmptyChatRooms);
        createChat = findViewById(R.id.create_chat);
        avatarProfile = findViewById(R.id.avatar_profile);
        btStartChat = findViewById(R.id.bt_start_chat);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        chatRoomAdapter = new ChatRoomAdapter(this);
        chatRoomAdapter.setOnItemClickListener(this);

        createChat.setOnClickListener(view -> startActivity(new Intent(HomeActivity.this, ContactActivity.class)));

        btStartChat.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ContactActivity.class)));

        Nirmana.getInstance().get()
                .setDefaultRequestOptions(new RequestOptions()
                        .placeholder(R.drawable.ic_qiscus_avatar)
                        .error(R.drawable.ic_qiscus_avatar)
                        .dontAnimate())
                .load(QiscusCore.getQiscusAccount().getAvatar())
                .into(avatarProfile);

        avatarProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        recyclerView.setAdapter(chatRoomAdapter);

        homePresenter = new HomePresenter(this,
                MyApplication.getInstance().getComponent().getChatRoomRepository(),
                MyApplication.getInstance().getComponent().getUserRepository());

//        Used for fixing realtime issue in API below Lollipop(5.0)
        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        homePresenter.loadChatRooms();
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onCommentReceivedEvent(QiscusCommentReceivedEvent event) {
        homePresenter.loadChatRooms();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void showChatRooms(List<QiscusChatRoom> chatRooms) {
        if (chatRooms.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            linEmptyChatRooms.setVisibility(View.VISIBLE);
        } else {
            linEmptyChatRooms.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        chatRoomAdapter.addOrUpdate(chatRooms);
    }

    @Override
    public void showChatRoomPage(QiscusChatRoom chatRoom) {
        startActivity(ChatRoomActivity.generateIntent(this, chatRoom));
    }

    @Override
    public void showGroupChatRoomPage(QiscusChatRoom chatRoom) {
        startActivity(GroupChatRoomActivity.generateIntent(this, chatRoom));
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(int position) {
        homePresenter.openChatRoom(chatRoomAdapter.getData().get(position));
    }
}
