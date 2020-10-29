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

package com.project.chatapp.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.project.chatapp.R;
import com.project.chatapp.util.QiscusImageUtil;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.chat.core.data.model.QiscusPhoto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class QiscusPhotoAdapter extends RecyclerView.Adapter<QiscusPhotoAdapter.VH> {

    private Context context;
    private List<QiscusPhoto> qiscusPhotos;
    private OnItemClickListener onItemClickListener;

    public QiscusPhotoAdapter(Context context) {
        this.context = context;
        qiscusPhotos = new ArrayList<>();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(context).inflate(R.layout.item_qiscus_photo, parent, false),
                onItemClickListener);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        File imageFile = qiscusPhotos.get(position).getPhotoFile();
        if (QiscusImageUtil.isImage(imageFile)) {
            Nirmana.getInstance().get()
                    .setDefaultRequestOptions(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                    .load(imageFile)
                    .into(holder.imageView);
        } else {
            Nirmana.getInstance().get().load(imageFile).into(holder.imageView);
        }

        if (qiscusPhotos.get(position).isSelected()) {
            holder.imageView.setBackground(null);
        } else {
            holder.imageView.setBackground(null);
        }
    }

    @Override
    public int getItemCount() {
        return qiscusPhotos.size();
    }

    public List<QiscusPhoto> getQiscusPhotos() {
        return qiscusPhotos;
    }

    public void refreshWithData(List<QiscusPhoto> qiscusPhotos) {
        this.qiscusPhotos.clear();
        this.qiscusPhotos.addAll(qiscusPhotos);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void updateSelected(int position) {
        for (int i = 0; i < qiscusPhotos.size(); i++) {
            qiscusPhotos.get(i).setSelected(i == position);
        }
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageView;

        private OnItemClickListener itemClickListener;

        public VH(View itemView, OnItemClickListener itemClickListener) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.itemClickListener = itemClickListener;
            imageView = itemView.findViewById(R.id.image);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(getAdapterPosition());
            }
        }
    }
}
