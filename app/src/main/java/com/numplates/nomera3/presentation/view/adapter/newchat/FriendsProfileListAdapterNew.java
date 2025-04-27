package com.numplates.nomera3.presentation.view.adapter.newchat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.numplates.nomera3.Act;
import com.numplates.nomera3.R;
import com.meera.db.models.chatmembers.ChatMember;
import com.numplates.nomera3.presentation.view.callback.ProfileVehicleListCallback;
import com.numplates.nomera3.presentation.view.holder.FriendProfileHolder;
import com.numplates.nomera3.presentation.view.holder.ZeroHolder;
import com.numplates.nomera3.presentation.view.widgets.VipView;

import java.util.ArrayList;
import java.util.List;



public class FriendsProfileListAdapterNew
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_COMMON = 1;
    private static final int TYPE_ZERO = 0;

    private final List<ChatMember> data;
    private final ProfileVehicleListCallback callback;
    private final Act act;

    int visibleAmount;
    int itemWidth;
    public boolean scrollable = true;



    public FriendsProfileListAdapterNew(Builder builder) {
        this.visibleAmount = builder.visibleAmount;
        this.itemWidth = builder.itemWidth;
        this.callback = builder.callback;
        this.data = new ArrayList<>();
        this.data.addAll( builder.data);
        this.act = builder.act;
    }

    @Override
    public int getItemViewType(int position) {
        if (data != null && data.size() > 0
                && data.get(position) !=null
                && data.get(position) instanceof ChatMember) {
            return TYPE_COMMON;
        } else {
            return TYPE_ZERO;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_COMMON) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_friend, parent, false);
            return new FriendProfileHolder(v, callback);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_zero, parent, false);
            return new ZeroHolder(v, callback);
        }

    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_COMMON) {
            ChatMember user = data.get(position);
            VipView vipView = ((FriendProfileHolder) holder).getVipView();
            vipView.setUp(act,
                    user.getUser().getAvatarSmall(),
                    user.getUser().getType(),
                    user.getUser().getColor());
        } else if(getItemViewType(position) == TYPE_ZERO) {
            ((ZeroHolder)holder).bind();
        }
    }


    public void add(List<ChatMember> items) {
        data.addAll(items);
        notifyDataSetChanged();
    }

    public void addItem(ChatMember items) {
        data.add(items);
        notifyDataSetChanged();
    }

    public void setData(List<ChatMember>  items) {
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public ChatMember getItem(int position){
        return data.get(position);
    }

    public List<ChatMember> getItems (){
        return data;
    }

    public void removeAt(int position) {
        data.remove(position);
        notifyDataSetChanged();
    }


    public static class Builder {

        private final Act act;
        private ProfileVehicleListCallback callback;
        private List<ChatMember> data;
        int visibleAmount;
        int itemWidth;


        public Builder(Act act) {
            this.act = act;
        }

        public Builder visibleAmount(int visibleAmount) {
            this.visibleAmount = visibleAmount;
            return this;
        }

        public Builder itemWidth(int itemWidth) {
            this.itemWidth = itemWidth;
            return this;
        }

        public Builder data(List<ChatMember> data) {
            this.data = data;
            return this;
        }

        public Builder callback(ProfileVehicleListCallback callback) {
            this.callback = callback;
            return this;
        }

        public FriendsProfileListAdapterNew build() {
            return new FriendsProfileListAdapterNew(this);
        }
    }


}
