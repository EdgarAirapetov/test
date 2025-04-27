package com.numplates.nomera3.presentation.view.adapter;

import android.app.Activity;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.numplates.nomera3.R;
import com.numplates.nomera3.data.network.UserInfoModel;
import com.numplates.nomera3.presentation.view.holder.MapUserInfoMarkerHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by artem on 03.11.17.
 */

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    Map<String, UserInfoModel> data;

    private final View mContents;

    private final Activity activity;

    public CustomInfoWindowAdapter(Activity activity) {
        this.activity = activity;
        data = new HashMap<>();
        mContents = activity.getLayoutInflater().inflate(R.layout.layout_map_userinfo, null);
    }

    public void clear(){
        data.clear();
    }

    public boolean add(String id, UserInfoModel userInfoModel){
        if(data.containsValue(userInfoModel)) {
            return false;
        } else {
            data.put(id, userInfoModel);
            int size = data.size();
            return true;
        }
    }

    public UserInfoModel get(Marker marker) {
        return data.get(marker.getId());
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        if (data != null && data.get(marker.getId()) != null) {
            render(marker, mContents);
            return mContents;
        }
        return null;
    }

    private void render(Marker marker, View view) {
        if (data != null && data.get(marker.getId()) != null) {

            UserInfoModel userInfoModel = data.get(marker.getId());

            MapUserInfoMarkerHolder holder = new MapUserInfoMarkerHolder(view);

            holder.bind(activity, userInfoModel, marker);
        }
    }

}
