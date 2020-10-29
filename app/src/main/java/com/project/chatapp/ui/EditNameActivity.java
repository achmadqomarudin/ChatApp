package com.project.chatapp.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.project.chatapp.R;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;

public class EditNameActivity extends AppCompatActivity {
    EditText etName;
    ImageView btSave, btBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_name);

        etName = findViewById(R.id.eTName);
        btBack = findViewById(R.id.bt_back);
        btSave = findViewById(R.id.bt_save);

        etName.setText(QiscusCore.getQiscusAccount().getUsername());

        btSave.setOnClickListener(v -> {
            if (!etName.getText().toString().isEmpty()){
                QiscusCore.updateUser(etName.getText().toString(), null, null, new QiscusCore.SetUserListener() {
                    @Override
                    public void onSuccess(QiscusAccount qiscusAccount) {
                        //do anything after it successfully updated
                        finish();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        //do anything if error occurs
                        Toast.makeText(EditNameActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        btBack.setOnClickListener(v -> onBackPressed());
    }
}
