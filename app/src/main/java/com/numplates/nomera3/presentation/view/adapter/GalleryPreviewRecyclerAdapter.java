package com.numplates.nomera3.presentation.view.adapter;

import android.content.Context;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.numplates.nomera3.R;
import com.numplates.nomera3.modules.userprofile.ui.model.PhotoModel;
import com.numplates.nomera3.presentation.view.callback.GalleryPreviewCallback;
import com.numplates.nomera3.presentation.view.holder.GalleryPreviewHolder;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import timber.log.Timber;

/**
 * Created by abelov
 */
public class GalleryPreviewRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<PhotoModel> data;
    private GalleryPreviewCallback callback;
    private Context context;
    boolean selectable;
    int visibleAmount;
    int itemWidth;

    private int selectedPosition = -1;

    public GalleryPreviewRecyclerAdapter(Builder builder) {
        Timber.e("GAL Preview recycler adapter");
        this.visibleAmount = builder.visibleAmount;
        if (this.itemWidth == 0 && builder.itemWidth != 0) {
            this.itemWidth = builder.itemWidth;
        }
        this.context = builder.context;
        this.callback = builder.callback;
        this.selectable = builder.selectable;
        this.data = new ArrayList<>();
        this.data.addAll(builder.data);

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_preview, parent, false);
        v.getLayoutParams().height = itemWidth;
        v.getLayoutParams().width = itemWidth;
        return new GalleryPreviewHolder(v, callback);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof GalleryPreviewHolder) {
            GalleryPreviewHolder h = (GalleryPreviewHolder) holder;
            int positionInList = position % data.size();
            PhotoModel item = data.get(positionInList);
            Glide.with(context)
                .load(item.getImageUrl())
                .into(h.getIvPicture());
            boolean isSelected = (position == selectedPosition);

            if(!selectable) {
                h.getIvPicture().setColorFilter(null);
            } else {
                if (isSelected ) {
                    h.getIvPicture().setColorFilter(null);
                } else {
                    ColorMatrix colorMatrix = new ColorMatrix();
                    colorMatrix.setSaturation(0);
                    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
                    h.getIvPicture().setColorFilter(filter);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public int getSelectedPosition(int fakePosition) {
        return fakePosition % data.size();
    }

    public int getRealCount(){
        return data.size();
    }

    public void setItemWidth(int itemWidth) {
        this.itemWidth = itemWidth;
    }

    public void setSelected(int position){
        selectedPosition = position;
        notifyDataSetChanged();
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public PhotoModel getItem(int position) {
        return data.get(position);
    }

    public void setData(List data){
        this.data = data;
        notifyDataSetChanged();
    }

    public static class Builder<T> {

        private final Context context;
        private GalleryPreviewCallback callback;
        private List<T> data;
        int visibleAmount;
        int itemWidth;
        boolean selectable;


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

        public Builder selectable(boolean selectable) {
            this.selectable = selectable;
            return this;
        }

        public Builder data(List<T> data) {
            this.data = data;
            return this;
        }

        public Builder callback(GalleryPreviewCallback callback) {
            this.callback = callback;
            return this;
        }

        public GalleryPreviewRecyclerAdapter build() {
            return new GalleryPreviewRecyclerAdapter(this);
        }
    }
}
