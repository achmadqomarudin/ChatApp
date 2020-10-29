package com.project.chatapp.ui.groupchatcreation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.project.chatapp.R;
import com.project.chatapp.ui.adapter.OnItemClickListener;
import com.project.chatapp.ui.adapter.SortedRecyclerViewAdapter;

public class ContactAdapter extends SortedRecyclerViewAdapter<SelectableUser, ContactViewHolder> {
    private Context context;
    private OnItemClickListener onItemClickListener;

    public ContactAdapter(Context context, OnItemClickListener onItemClickListener) {
        super();
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    protected Class<SelectableUser> getItemClass() {
        return SelectableUser.class;
    }

    @Override
    protected int compare(SelectableUser item1, SelectableUser item2) {
        return item1.getUser().getName().compareTo(item2.getUser().getName());
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false), onItemClickListener
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        holder.bind(getData().get(position));
    }
}
