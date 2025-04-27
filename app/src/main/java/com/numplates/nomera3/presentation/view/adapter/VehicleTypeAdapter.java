package com.numplates.nomera3.presentation.view.adapter;

import static com.meera.core.extensions.CommonKt.dpToPx;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.numplates.nomera3.R;
import com.numplates.nomera3.data.network.VehicleType;
import com.numplates.nomera3.presentation.view.callback.VehickeTypeCallback;
import com.numplates.nomera3.presentation.view.holder.VehicleTypeHolder;
import com.numplates.nomera3.presentation.view.utils.NGraphics;

import java.util.List;

/**
 * Created by abelov
 */
public class VehicleTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE = 1;
    private static final int EMPTY = 0;

    private final List<VehicleType> data;
    private final VehickeTypeCallback callback;
    private final Context context;
    public boolean scrollable = true;
    private final int itemWidth;


    public VehicleTypeAdapter(Builder builder) {
        this.context = builder.context;
        this.data = builder.data;
        this.callback = builder.callback;
        this.itemWidth = builder.itemWidth;
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position) == null) {
            return EMPTY;
        }
        return TYPE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicvle_type, parent, false);
        v.getLayoutParams().width = itemWidth;


        v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // run scale animation and make it bigger
                } else {
                    // run scale animation and make it smaller
                }
            }
        });

        return new VehicleTypeHolder(v, callback);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        VehicleTypeHolder h = (VehicleTypeHolder) holder;
        VehicleType item = data.get(position);
        if (item == null) {
            h.getIvIcon().setImageDrawable(null);
            h.getTvText().setText(null);
            h.getCv_vehicle_type_container().setVisibility(View.INVISIBLE);
            return;
        }
        if (item.getSelected()) {
            h.getIvIcon().setImageResource(item.getSelectedIcon(NGraphics.getVehicleTypeMap()));
            h.getCv_vehicle_type_container().getLayoutParams().height = dpToPx(136);
            h.getCv_vehicle_type_container().getLayoutParams().width = dpToPx(136);
            h.getCv_vehicle_type_container().setRadius(dpToPx(68));
            h.getCv_vehicle_type_container().setCardBackgroundColor(h.getCv_vehicle_type_container()
                    .getContext().getResources().getColor(R.color.ui_purple));
            h.getTvText().setText(item.getName());
            h.getLlContent().requestLayout();
        } else {
            h.getIvIcon().setImageResource(item.getIcon(NGraphics.getVehicleTypeMap()));
            h.getCv_vehicle_type_container().getLayoutParams().height = dpToPx(78);
            h.getCv_vehicle_type_container().getLayoutParams().width = dpToPx(78);
            h.getCv_vehicle_type_container().setRadius(dpToPx(39));
            h.getCv_vehicle_type_container().setCardBackgroundColor(h.getCv_vehicle_type_container()
                    .getContext().getResources().getColor(R.color.ui_gray));
            h.getTvText().setText(item.getName());
            h.getLlContent().requestLayout();
        }
    }

    @Override
    public int getItemCount() {
        if (data == null) {
            return 0;
        }
        return data.size();
    }

    public void addItems(List<VehicleType> result) {
        data.addAll(result);
        notifyDataSetChanged();
    }

    public void addItem(VehicleType result) {
        data.add(result);
        notifyDataSetChanged();
    }

    public VehicleType getItem(int position) {
        int positionInList = position % data.size();
        return data.get(positionInList);
    }

    public void setSelected(int pos) {
        if (data == null || data.size() == 0 || pos < 0 || pos >= data.size()) {
            return;
        }
        for (int i = 0; i < data.size(); i++) {

            if (i == pos) {
                if (data.get(i) != null) {
                    data.get(i).setSelected(true);
                }
            } else {
                if (data.get(i) != null) {
                    data.get(i).setSelected(false);
                }
            }
        }
        callback.onSelected(pos);
        notifyDataSetChanged();
    }

    public List<VehicleType> getData() {
        return data;
    }

    public int getRealCount() {
        return data.size();
    }

    public static class Builder {

        private final Context context;
        private VehickeTypeCallback callback;
        int visibleAmount;
        List<VehicleType> data;

        int itemWidth;


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

        public Builder callback(VehickeTypeCallback callback) {
            this.callback = callback;
            return this;
        }

        public Builder data(List<VehicleType> data) {
            this.data = data;
            return this;
        }

        public VehicleTypeAdapter build() {
            return new VehicleTypeAdapter(this);
        }
    }
}
