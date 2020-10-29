package com.project.chatapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.chatapp.MyApplication;
import com.project.chatapp.R;
import com.project.chatapp.data.model.User;
import com.project.chatapp.presenter.ContactPresenter;
import com.project.chatapp.ui.adapter.ContactAdapter;
import com.project.chatapp.ui.adapter.OnItemClickListener;
import com.project.chatapp.ui.groupchatcreation.GroupChatCreationActivity;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;

import java.util.List;

public class ContactActivity extends AppCompatActivity implements ContactPresenter.View, OnItemClickListener {
    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    private LinearLayout llCreateGroupChat;
    private ContactPresenter contactPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());
        recyclerView = findViewById(R.id.recyclerview);
        llCreateGroupChat = findViewById(R.id.ll_create_group_chat);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        contactAdapter = new ContactAdapter(this);
        contactAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(contactAdapter);

        contactPresenter = new ContactPresenter(this,
                MyApplication.getInstance().getComponent().getUserRepository(),
                MyApplication.getInstance().getComponent().getChatRoomRepository());
        contactPresenter.loadContacts(1,100, "");

        llCreateGroupChat.setOnClickListener(view -> createGroupChat());

    }

    @Override
    public void showContacts(List<User> contacts) {
        contactAdapter.clear();
        contactAdapter.addOrUpdate(contacts);
    }

    @Override
    public void showChatRoomPage(QiscusChatRoom chatRoom) {
        startActivity(ChatRoomActivity.generateIntent(this, chatRoom));
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(int position) {
        contactPresenter.createRoom(contactAdapter.getData().get(position));
    }

    public void createGroupChat() {
        Intent intent = new Intent(this, GroupChatCreationActivity.class);
        startActivity(intent);
    }
}
