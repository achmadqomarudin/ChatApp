package com.project.chatapp.ui.groupchatcreation;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.chatapp.MyApplication;
import com.project.chatapp.R;
import com.project.chatapp.data.model.User;
import com.project.chatapp.ui.groupchatcreation.groupinfo.GroupInfoFragment;

import java.util.ArrayList;
import java.util.List;

public class GroupChatCreationActivity extends AppCompatActivity implements GroupChatCreationPresenter.View {
    private static final String TAG = "GroupChatCreationActivity";
    private RecyclerView selectedContactRecyclerView;
    private ContactAdapter contactAdapter;
    private SelectedContactAdapter selectedContactAdapter;
    private ImageView imgNext;
    private GroupChatCreationPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_creation);

        RecyclerView contactRecyclerView = findViewById(R.id.recyclerViewAlumni);
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactRecyclerView.setHasFixedSize(true);

        imgNext = findViewById(R.id.img_next);

        contactAdapter = new ContactAdapter(this, position -> {
            presenter.selectContact(contactAdapter.getData().get(position));
        });

        contactRecyclerView.setAdapter(contactAdapter);

        selectedContactRecyclerView = findViewById(R.id.recyclerViewSelected);
        selectedContactRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        selectedContactRecyclerView.setHasFixedSize(true);

        selectedContactAdapter = new SelectedContactAdapter(this, position -> {
            presenter.selectContact(selectedContactAdapter.getData().get(position));
        });
        selectedContactRecyclerView.setAdapter(selectedContactAdapter);

        imgNext.setOnClickListener(v -> {
                if (selectedContactIsMoreThanOne()) {
                    List<User> contacts = new ArrayList<>();
                    int size = selectedContactAdapter.getData().size();
                    for (int i = 0; i < size; i++) {
                        contacts.add(selectedContactAdapter.getData().get(i).getUser());
                    }

                    Fragment fragment = GroupInfoFragment.newInstance(contacts);
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.frameLayout, fragment)
                            .addToBackStack("tag")
                            .commit();
                } else {
                    Toast.makeText(this, "select at least one", Toast.LENGTH_SHORT).show();
                }
        });

        presenter = new GroupChatCreationPresenter(this, MyApplication.getInstance().getComponent().getUserRepository());
        presenter.loadContacts(1,100, "");

        findViewById(R.id.back).setOnClickListener(view -> finish());

    }

    private boolean isFragmentOn() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        return !(currentFragment == null || !currentFragment.isVisible());
    }

    private boolean selectedContactIsMoreThanOne() {
        return selectedContactAdapter.getData().size() > 0;
    }

    private void onReturn() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if (currentFragment == null || !currentFragment.isVisible()) {
            finish();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(currentFragment)
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        onReturn();
    }


    @Override
    public void showContacts(List<SelectableUser> contacts) {
        contactAdapter.clear();
        contactAdapter.addOrUpdate(contacts);
    }

    @Override
    public void onSelectedContactChange(SelectableUser contact) {
        contactAdapter.addOrUpdate(contact);
        if (contact.isSelected()) {
            selectedContactAdapter.addOrUpdate(contact);
        } else {
            selectedContactAdapter.remove(contact);
        }

        selectedContactRecyclerView.setVisibility(selectedContactAdapter.getItemCount() > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showErrorMessage(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
