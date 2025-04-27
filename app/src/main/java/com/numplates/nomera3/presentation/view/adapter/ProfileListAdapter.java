package com.numplates.nomera3.presentation.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.numplates.nomera3.R;
import com.numplates.nomera3.presentation.view.callback.ProfileListCallback;
import com.numplates.nomera3.presentation.view.holder.ProfileListHolder;
import com.numplates.nomera3.presentation.view.view.ProfileListItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abelov
 */
public class ProfileListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_COMMON = 1;
    private static final int TYPE_ZERO = 0;
    public boolean scrollable = true;
    public int finalOffset;
    int visibleAmount;
    int itemWidth;
    private final List<ProfileListItem> data;
    private final ProfileListCallback callback;
    private final Context context;


    public ProfileListAdapter(Builder builder) {

        this.visibleAmount = builder.visibleAmount;
        this.itemWidth = builder.itemWidth;
        this.context = builder.context;
        this.callback = builder.callback;

        this.data = new ArrayList<>();
        this.data.addAll(builder.data);


    }

    @Override
    public int getItemViewType(int position) {
        if (data != null && data.size() > 0 && data.get(0) != null) {
            return TYPE_COMMON;
        } else {
            return TYPE_ZERO;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feed_map, parent, false);
        return callback.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ProfileListHolder) {
//            int positionInList = position % data.size();
            ((ProfileListHolder) holder).bind(data.get(position));
        }
    }

    //    @Override
//    public int getItemCount() {
//        return App.MAX_ITEMS_INFINITY_RECYCLER;
//    }
    public void add(List<ProfileListItem> items) {
        data.addAll(items);
        notifyDataSetChanged();
    }

    public void addItem(ProfileListItem items) {
        data.add(items);
        notifyDataSetChanged();
    }

    public void setData(List<ProfileListItem> items) {
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    public void setSingleItem(ProfileListItem item) {
        data.clear();
        data.add(item);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public ProfileListItem getItem(int position) {
//        int positionInList = position % data.size();
//        return data.get(positionInList);
        if (position < 0)
            return null;
        if (position > data.size())
            return null;
        return data.get(position);
    }

    public List<ProfileListItem> getItems() {
        return data;
    }

    public void removeAt(int position) {
        data.remove(position);
        notifyDataSetChanged();
    }


    public static class Builder<T> {

        private final Context context;
        int visibleAmount;
        int itemWidth;
        private ProfileListCallback callback;
        private List<T> data;


        public Builder(Context context) {
            this.context = context;
        }

        public Builder visibleAmount(int visibleAmount) {
            this.visibleAmount = visibleAmount;
            return this;
        }

        public Builder itemWidth(int itemWidth) {
            this.itemWidth = itemWidth;
            return this;
        }

        public Builder data(List<T> data) {
            this.data = data;
            return this;
        }

        public Builder callback(ProfileListCallback callback) {
            this.callback = callback;
            return this;
        }

        public ProfileListAdapter build() {
            return new ProfileListAdapter(this);
        }
    }
}
