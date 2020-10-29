package com.project.chatapp.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.project.chatapp.R;
import com.project.chatapp.ui.adapter.OnItemClickListener;
import com.project.chatapp.ui.adapter.ParticipantsRoomInfoAdapter;
import com.project.chatapp.ui.addmember.AddGroupMemberActivity;
import com.project.chatapp.ui.view.QiscusProgressView;
import com.project.chatapp.util.QiscusImageUtil;
import com.project.chatapp.util.QiscusPermissionsUtil;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.local.QiscusCacheManager;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;
import com.qiscus.sdk.chat.core.util.QiscusFileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import id.zelory.compressor.Compressor;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RoomInfoActivity extends AppCompatActivity implements OnItemClickListener, QiscusApi.MetaRoomParticipantsListener, QiscusPermissionsUtil.PermissionCallbacks {
    protected static final int TAKE_PICTURE_REQUEST = 3;
    protected static final int RC_CAMERA_PERMISSION = 128;
    private static final String CHAT_ROOM_DATA = "chat_room_data";
    private static final int RC_ADD_PARTICIPANTS = 133;
    private static final int REQUEST_PICK_IMAGE = 1;
    private static final int REQUEST_FILE_PERMISSION = 2;
    private static final String[] FILE_PERMISSION = {
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE"
    };
    private static final String[] CAMERA_PERMISSION = {
            "android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
    };
    private QiscusChatRoom chatRoom;
    private TextView tvRoomName;
    private ImageView ivEditRoomName, ivEditAvatarRoom, ivAvatar, bt_back;
    private RecyclerView recyclerView;
    private LinearLayout linUI;
    private LinearLayout llAddParticipant;
    private QiscusProgressView progress;
    private ParticipantsRoomInfoAdapter participantAdapter;
    private PopupWindow mPopupWindow;

    public static Intent generateIntent(Context context, QiscusChatRoom chatRoom) {
        Intent intent = new Intent(context, RoomInfoActivity.class);
        intent.putExtra(CHAT_ROOM_DATA, chatRoom);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_info);
        resolveChatRoom(savedInstanceState);

        chatRoom = getIntent().getParcelableExtra(CHAT_ROOM_DATA);
        if (chatRoom == null) {
            finish();
            return;
        }

        setupUI();
        loadData();

    }

    private void setupUI() {
        ivEditAvatarRoom = findViewById(R.id.ivEditAvatarRoom);
        ivEditRoomName = findViewById(R.id.ivEditRoomName);
        tvRoomName = findViewById(R.id.tvRoomName);
        ivAvatar = findViewById(R.id.ivAvatar);
        bt_back = findViewById(R.id.bt_back);
        linUI = findViewById(R.id.linUI);
        progress = findViewById(R.id.qiscusCircleProgress);
        llAddParticipant = findViewById(R.id.ll_add_participant);

        recyclerView = findViewById(R.id.recycleViewRoomInfo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        participantAdapter = new ParticipantsRoomInfoAdapter(this);
        participantAdapter.setOnItemClickListener(this);

        recyclerView.setAdapter(participantAdapter);

        ivEditAvatarRoom.setOnClickListener(v -> {
            // Initialize a new instance of LayoutInflater service
            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

            // Inflate the custom layout/view
            View customView = inflater.inflate(R.layout.popup_window_profile, null);

            // Initialize a new instance of popup window
            mPopupWindow = new PopupWindow(
                    customView,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );

            // Set an elevation value for popup window
            // Call requires API level 21
            if (Build.VERSION.SDK_INT >= 21) {
                mPopupWindow.setElevation(5.0f);
            }

            // Get a reference for the custom view close button
            LinearLayout close = customView.findViewById(R.id.linCancel);
            LinearLayout linImageGallery = customView.findViewById(R.id.linImageGallery);
            LinearLayout linTakePhoto = customView.findViewById(R.id.linTakePhoto);

            linImageGallery.setOnClickListener(v1 -> {
                //gallery
                if (QiscusPermissionsUtil.hasPermissions(getApplication(), FILE_PERMISSION)) {
                    pickImage();
                    mPopupWindow.dismiss();
                } else {
                    requestReadFilePermission();
                }
            });

            linTakePhoto.setOnClickListener(v12 -> {
                //camera
                if (QiscusPermissionsUtil.hasPermissions(getApplication(), CAMERA_PERMISSION)) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getApplication().getPackageManager()) != null) {
                        File photoFile = null;
                        try {
                            photoFile = QiscusImageUtil.createImageFile();
                        } catch (IOException ex) {
                            Toast.makeText(getApplication(), "Failed to write temporary picture!", Toast.LENGTH_SHORT).show();
                        }

                        if (photoFile != null) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                            } else {
                                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                        FileProvider.getUriForFile(getApplication(), QiscusCore.getApps().getPackageName() + ".qiscus.sdk.provider", photoFile));
                            }
                            startActivityForResult(intent, TAKE_PICTURE_REQUEST);
                        }
                        mPopupWindow.dismiss();
                    }
                } else {
                    requestCameraPermission();
                }
            });

            // Set a click listener for the popup window close button
            close.setOnClickListener(view -> {
                // Dismiss the popup window
                mPopupWindow.dismiss();
            });

            mPopupWindow.setAnimationStyle(R.style.popup_window_animation);

            mPopupWindow.showAtLocation(linUI, Gravity.BOTTOM, 0, 0);
        });

        ivEditRoomName.setOnClickListener(v -> {
            startActivity(EditRoomNameActivity.generateIntent(getApplication(), chatRoom));
            finish();
        });

        bt_back.setOnClickListener(v -> onBackPressed());

        llAddParticipant.setOnClickListener(view -> startActivityForResult(AddGroupMemberActivity.generateIntent(RoomInfoActivity.this, chatRoom), RC_ADD_PARTICIPANTS));

    }

    @Override
    public void onItemClick(int position) {
        String emailParticipant = participantAdapter.getData().get(position).getEmail();
        String[] arrEmail = {
                emailParticipant
        };

        QiscusApi.getInstance().removeParticipants(chatRoom.getId(), Arrays.asList(arrEmail))
                .doOnNext(chatRoom -> QiscusCore.getDataStore().addOrUpdate(chatRoom))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(newChatRoom -> {
                    //success
                    chatRoom = newChatRoom;
                    //update
                    participantAdapter.remove(position);
                }, throwable -> {
                    //error
                    Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(GroupChatRoomActivity.generateIntent(getApplication(), chatRoom));
        finish();
    }

    private void loadData() {
        //load data from local db
        tvRoomName.setText(chatRoom.getName());

        Nirmana.getInstance().get()
                .setDefaultRequestOptions(new RequestOptions()
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .dontAnimate())
                .load(chatRoom.getAvatarUrl())
                .into(ivAvatar);

        QiscusApi.getInstance().getParticipants(chatRoom.getUniqueId(), 1, 100, null,this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(participants -> {
                    participantAdapter.addOrUpdate(participants);
                }, throwable -> {
                    throwable.printStackTrace();
                });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    private void requestReadFilePermission() {
        if (!QiscusPermissionsUtil.hasPermissions(this, FILE_PERMISSION)) {
            QiscusPermissionsUtil.requestPermissions(this, getString(R.string.qiscus_permission_request_title),
                    REQUEST_FILE_PERMISSION, FILE_PERMISSION);
        }
    }

    protected void requestCameraPermission() {
        if (!QiscusPermissionsUtil.hasPermissions(this, CAMERA_PERMISSION)) {
            QiscusPermissionsUtil.requestPermissions(this, getString(R.string.qiscus_permission_request_title),
                    RC_CAMERA_PERMISSION, CAMERA_PERMISSION);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            try {
                File imageFile = QiscusFileUtil.from(data.getData());
                updateAvatar(imageFile);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to open image file!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == TAKE_PICTURE_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                File imageFile = QiscusFileUtil.from(Uri.parse(QiscusCacheManager.getInstance().getLastImagePath()));
                updateAvatar(imageFile);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to read picture data!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else if (requestCode == RC_ADD_PARTICIPANTS && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                chatRoom = data.getParcelableExtra(CHAT_ROOM_DATA);
                loadData();
            }

        }
    }

    protected void resolveChatRoom(Bundle savedInstanceState) {
        chatRoom = getIntent().getParcelableExtra(CHAT_ROOM_DATA);
        if (chatRoom == null && savedInstanceState != null) {
            chatRoom = savedInstanceState.getParcelable(CHAT_ROOM_DATA);
        }
    }


    public void updateAvatar(File file) {
        File compressedFile = file;
        if (QiscusFileUtil.isImage(file.getPath()) && !file.getName().endsWith(".gif")) {
            try {
                compressedFile = new Compressor(QiscusCore.getApps()).compressToFile(file);
            } catch (NullPointerException | IOException e) {
                Toast.makeText(this, "Can not read file, please make sure that is not corrupted file!", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            compressedFile = QiscusFileUtil.saveFile(compressedFile);
        }

        if (!file.exists()) { //File have been removed, so we can not upload it anymore
            Toast.makeText(this, "Can not read file, please make sure that is not corrupted file!", Toast.LENGTH_SHORT).show();
            return;
        }

        QiscusApi.getInstance()
                .upload(compressedFile, percentage ->
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //show percentage
                            int i = (int) percentage;
                            progress.setProgress(i);
                            progress.setVisibility(View.VISIBLE);
                        }
                    });

                })
                .doOnError(throwable -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to update avatar room!", Toast.LENGTH_SHORT).show();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(uri -> {
                    QiscusApi.getInstance().updateChatRoom(chatRoom.getId(), null, uri.toString(), null)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(newChatroom -> {
                                progress.setVisibility(View.GONE);
                                chatRoom = newChatroom;
                                loadData();
                            }, throwable -> {
                                throwable.printStackTrace();
                            });
                }, throwable -> {
                    throwable.printStackTrace();
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to update avatar room!", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onMetaReceived(int currentOffset, int perPage, int total) {

    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (requestCode == REQUEST_FILE_PERMISSION) {
            pickImage();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        QiscusPermissionsUtil.checkDeniedPermissionsNeverAskAgain(this, getString(R.string.qiscus_permission_message),
                R.string.qiscus_grant, R.string.qiscus_denny, perms);
    }
}
