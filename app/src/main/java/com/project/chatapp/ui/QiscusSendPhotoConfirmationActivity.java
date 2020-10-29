/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.project.chatapp.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.project.chatapp.R;
import com.project.chatapp.ui.adapter.OnItemClickListener;
import com.project.chatapp.ui.adapter.QiscusPhotoAdapter;
import com.project.chatapp.ui.adapter.QiscusPhotoPagerAdapter;
import com.project.chatapp.ui.fragment.QiscusPhotoFragment;
import com.qiscus.jupuk.JupukConst;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusPhoto;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QiscusSendPhotoConfirmationActivity extends RxAppCompatActivity implements ViewPager.OnPageChangeListener {
    public static final String EXTRA_QISCUS_PHOTOS = "qiscus_photos";
    public static final String EXTRA_CAPTIONS = "captions";
    private static final String EXTRA_ROOM = "room_data";

    private ViewGroup rootView;
    private EditText messageEditText;
    private ViewPager viewPager;
    private QiscusChatRoom qiscusChatRoom;
    private List<QiscusPhoto> qiscusPhotos;
    private Map<String, String> captions;
    private int position = -1;

    private RecyclerView recyclerView;
    private QiscusPhotoAdapter photoAdapter;

    public static Intent generateIntent(Context context, QiscusChatRoom room, List<QiscusPhoto> qiscusPhotos) {
        Intent intent = new Intent(context, QiscusSendPhotoConfirmationActivity.class);
        intent.putExtra(EXTRA_ROOM, room);
        intent.putParcelableArrayListExtra(EXTRA_QISCUS_PHOTOS, (ArrayList<QiscusPhoto>) qiscusPhotos);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onSetStatusBarColor();
        setContentView(R.layout.activity_qiscus_send_photo_confirmation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView tvTitle = findViewById(R.id.tv_title);
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());
        setSupportActionBar(toolbar);

        qiscusChatRoom = getIntent().getParcelableExtra(EXTRA_ROOM);
        if (qiscusChatRoom == null) {
            finish();
            return;
        }

        tvTitle.setText(qiscusChatRoom.getName());

        viewPager = findViewById(R.id.view_pager);
        messageEditText = findViewById(R.id.field_message);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        photoAdapter = new QiscusPhotoAdapter(this);
        photoAdapter.setOnItemClickListener(position -> viewPager.setCurrentItem(position));
        recyclerView.setAdapter(photoAdapter);

        messageEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                confirm();
                return true;
            }
            return false;
        });

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (position >= 0 && position < qiscusPhotos.size()) {
                    QiscusPhoto currentPhoto = qiscusPhotos.get(position);
                    if (currentPhoto != null) {
                        captions.put(currentPhoto.getPhotoFile().getAbsolutePath(),
                                messageEditText.getText().toString());
                    }
                }
            }
        });

        viewPager.addOnPageChangeListener(this);

        if (savedInstanceState != null) {
            captions = (Map<String, String>) savedInstanceState.getSerializable("saved_captions");
        }

        if (captions == null) {
            captions = new HashMap<>();
        }

        qiscusPhotos = getIntent().getParcelableArrayListExtra(EXTRA_QISCUS_PHOTOS);
        if (qiscusPhotos != null) {
            initPhotos();
        } else {
            finish();
            return;
        }

        ImageView sendButton = findViewById(R.id.button_send);
        sendButton.setOnClickListener(v -> confirm());

        findViewById(R.id.field_message_container).setVisibility(View.VISIBLE);
        findViewById(R.id.button_container).setVisibility(View.GONE);
        findViewById(R.id.button_container_divider).setVisibility(View.GONE);
        recyclerView.setBackgroundColor(Color.WHITE);

        findViewById(R.id.submit).setOnClickListener(v -> confirm());
        findViewById(R.id.cancel).setOnClickListener(v -> onBackPressed());
    }

    protected void onSetStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.qiscus_green));
        }
    }

    private void confirm() {
        Intent data = new Intent();
        data.putParcelableArrayListExtra(EXTRA_QISCUS_PHOTOS, (ArrayList<QiscusPhoto>) qiscusPhotos);
        data.putExtra(EXTRA_CAPTIONS, (HashMap<String, String>) captions);
        setResult(RESULT_OK, data);
        finish();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.qiscus_send_photo_action, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int i = item.getItemId();
//        if (i == R.id.action_add_images) {
//            JupukBuilder jupukBuilder = new JupukBuilder().setMaxCount(10);
//            for (QiscusPhoto qiscusPhoto : qiscusPhotos) {
//                Jupuk.getInstance().add(qiscusPhoto.getPhotoFile().getAbsolutePath(), JupukConst.FILE_TYPE_MEDIA);
//            }
//            jupukBuilder.enableVideoPicker(true)
//                    .setColorPrimary(ContextCompat.getColor(this, chatConfig.getAppBarColor()))
//                    .setColorPrimaryDark(ContextCompat.getColor(this, chatConfig.getStatusBarColor()))
//                    .setColorAccent(ContextCompat.getColor(this, chatConfig.getAccentColor()))
//                    .pickPhoto(this);
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void initPhotos() {
        List<QiscusPhotoFragment> fragments = new ArrayList<>();
        for (int i = 0; i < qiscusPhotos.size(); i++) {
            QiscusPhoto qiscusPhoto = qiscusPhotos.get(i);
            fragments.add(QiscusPhotoFragment.newInstance(qiscusPhoto.getPhotoFile()));
        }
        if (position == -1) {
            position = 0;
        }
        QiscusPhotoPagerAdapter pagerAdapter = new QiscusPhotoPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(position);
        photoAdapter.refreshWithData(qiscusPhotos);
        recyclerView.setVisibility(qiscusPhotos.size() > 1 ? View.VISIBLE : View.GONE);
        updateRecycleViewPosition(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        this.position = position;
        updateRecycleViewPosition(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void updateRecycleViewPosition(int position) {
        photoAdapter.updateSelected(position);
        recyclerView.smoothScrollToPosition(position);
        updateCaption(position);
    }

    private void updateCaption(int position) {
        if (position >= 0 && position < qiscusPhotos.size()) {
            QiscusPhoto currentPhoto = qiscusPhotos.get(position);
            if (currentPhoto != null) {
                String caption = captions.get(currentPhoto.getPhotoFile().getAbsolutePath());
                if (caption == null) {
                    caption = "";
                }
                messageEditText.setText(caption);
                messageEditText.post(() -> messageEditText.setSelection(messageEditText.getText().length()));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == JupukConst.REQUEST_CODE_PHOTO && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                showError(getString(R.string.qiscus_chat_error_failed_open_picture));
                return;
            }
            ArrayList<String> paths = data.getStringArrayListExtra(JupukConst.KEY_SELECTED_MEDIA);
            if (paths.size() > 0) {
                List<QiscusPhoto> qiscusPhotos = new ArrayList<>(paths.size());
                for (String path : paths) {
                    qiscusPhotos.add(new QiscusPhoto(new File(path)));
                }
                this.qiscusPhotos = qiscusPhotos;
                getIntent().putParcelableArrayListExtra(EXTRA_QISCUS_PHOTOS, (ArrayList<QiscusPhoto>) this.qiscusPhotos);
                position = 0;
                initPhotos();
            }
        }
    }

    public void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("saved_captions", (HashMap<String, String>) captions);
    }
}
