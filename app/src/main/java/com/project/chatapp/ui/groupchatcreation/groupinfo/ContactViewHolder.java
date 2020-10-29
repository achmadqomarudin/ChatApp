package com.project.chatapp.ui.groupchatcreation.groupinfo;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.project.chatapp.R;
import com.project.chatapp.data.model.User;
import com.project.chatapp.ui.adapter.OnItemClickListener;
import com.qiscus.nirmana.Nirmana;

public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final ImageView imgRemoveParticipant;
    private TextView itemName;
    private ImageView picture;
    private boolean isRemovedParticipant;

    private OnItemClickListener onItemClickListener;

    public ContactViewHolder(View itemView, OnItemClickListener onItemClickListener) {
        super(itemView);
        this.onItemClickListener = onItemClickListener;
        itemView.setOnClickListener(this);

        itemName = itemView.findViewById(R.id.name);
        picture = itemView.findViewById(R.id.avatar);
        imgRemoveParticipant = itemView.findViewById(R.id.img_remove_contact);
    }

    public void bind(User user) {
        Nirmana.getInstance().get()
                .setDefaultRequestOptions(new RequestOptions()
                        .placeholder(R.drawable.ic_qiscus_add_image)
                        .error(R.drawable.ic_qiscus_add_image)
                        .dontAnimate())
                .load(user.getAvatarUrl())
                .into(picture);

        if (isRemovedParticipant) {
            imgRemoveParticipant.setVisibility(View.VISIBLE);
        } else {
            imgRemoveParticipant.setVisibility(View.GONE);
        }

        itemName.setText(user.getName());
    }

    @Override
    public void onClick(View v) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(getAdapterPosition());
        }
    }

    public void needRemoveParticipant(boolean isRemoved) {
        this.isRemovedParticipant = isRemoved;
    }
}
