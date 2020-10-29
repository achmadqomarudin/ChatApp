package com.project.chatapp.ui.addmember;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.chatapp.AppComponent;
import com.project.chatapp.MyApplication;
import com.project.chatapp.R;
import com.project.chatapp.data.model.User;
import com.project.chatapp.ui.groupchatcreation.ContactAdapter;
import com.project.chatapp.ui.groupchatcreation.SelectableUser;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;

import java.util.ArrayList;
import java.util.List;

public class AddGroupMemberActivity extends AppCompatActivity implements AddGroupMemberPresenter.View {
    protected static final String CHAT_ROOM_DATA = "chat_room_data";
    protected QiscusChatRoom qiscusChatRoom;
    private ProgressDialog progressDialog;
    private AddGroupMemberPresenter presenter;
    private ImageView imgNext;
    private ContactAdapter contactAdapter;

    public static Intent generateIntent(Context context, QiscusChatRoom qiscusChatRoom) {
        Intent intent = new Intent(context, AddGroupMemberActivity.class);
        intent.putExtra(CHAT_ROOM_DATA, qiscusChatRoom);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room_participant);
        resolveChatRoom(savedInstanceState);

        if (qiscusChatRoom == null) {
            finish();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait!");

        RecyclerView contactRecyclerView = findViewById(R.id.recyclerContact);
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactRecyclerView.setHasFixedSize(true);

        imgNext = findViewById(R.id.img_next);

        contactAdapter = new ContactAdapter(this, position -> {
            presenter.selectContact(contactAdapter.getData().get(position));
        });

        contactRecyclerView.setAdapter(contactAdapter);

        AppComponent appComponent = MyApplication.getInstance().getComponent();
        presenter = new AddGroupMemberPresenter(this, appComponent.getUserRepository(),
                appComponent.getChatRoomRepository(), qiscusChatRoom.getMember());
        presenter.loadContacts(1, 100, "");

        imgNext.setOnClickListener(view -> {
            if (selectedContactIsMoreThanOne()) {
                List<User> contacts = new ArrayList<>();
                int size = contactAdapter.getData().size();
                for (int i = 0; i < size; i++) {
                    if (contactAdapter.getData().get(i).isSelected()) {
                        contacts.add(contactAdapter.getData().get(i).getUser());
                    }
                }
                presenter.addParticipant(qiscusChatRoom.getId(), contacts);
            } else {
                Toast.makeText(AddGroupMemberActivity.this, "select at least one", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.back).setOnClickListener(view -> finish());
    }

    protected void resolveChatRoom(Bundle savedInstanceState) {
        qiscusChatRoom = getIntent().getParcelableExtra(CHAT_ROOM_DATA);
        if (qiscusChatRoom == null && savedInstanceState != null) {
            qiscusChatRoom = savedInstanceState.getParcelable(CHAT_ROOM_DATA);
        }
    }


    @Override
    public void showContacts(List<SelectableUser> contacts) {
        contactAdapter.clear();
        contactAdapter.addOrUpdate(contacts);
    }


    @Override
    public void onParticipantAdded(List<User> users) {
        QiscusRoomMember member = new QiscusRoomMember();
        for (User user : users) {
            member.setEmail(user.getId());
            member.setAvatar(user.getAvatarUrl());
            member.setUsername(user.getName());
            qiscusChatRoom.getMember().add(member);
        }

        Intent data = new Intent();
        data.putExtra(CHAT_ROOM_DATA, qiscusChatRoom);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    private boolean selectedContactIsMoreThanOne() {
        return contactAdapter.getData().size() > 0;
    }

    @Override
    public void showLoading() {
        progressDialog.show();
    }

    @Override
    public void dismissLoading() {
        progressDialog.dismiss();
    }

    @Override
    public void onSelectedContactChange(SelectableUser selectableUser) {
        contactAdapter.addOrUpdate(selectableUser);
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
