package com.project.chatapp.ui.groupchatcreation;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.project.chatapp.R;
import com.project.chatapp.ui.adapter.OnItemClickListener;
import com.qiscus.nirmana.Nirmana;

public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private TextView itemName;
    private ImageView picture;
    private View viewCheck;
    private OnItemClickListener onItemClickListener;

    public ContactViewHolder(View itemView, OnItemClickListener onItemClickListener) {
        super(itemView);
        this.onItemClickListener = onItemClickListener;
        itemView.setOnClickListener(this);

        itemName = itemView.findViewById(R.id.name);
        picture = itemView.findViewById(R.id.avatar);
        viewCheck = itemView.findViewById(R.id.img_view_check);
    }

    public void bind(SelectableUser selectableUser) {
        Nirmana.getInstance().get()
                .setDefaultRequestOptions(new RequestOptions()
                        .placeholder(R.drawable.ic_qiscus_avatar)
                        .error(R.drawable.ic_qiscus_avatar)
                        .dontAnimate())
                .load(selectableUser.getUser().getAvatarUrl())
                .into(picture);
        itemName.setText(selectableUser.getUser().getName());
        viewCheck.setVisibility(selectableUser.isSelected() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(getAdapterPosition());
        }
    }
}
