package com.numplates.nomera3.data.network;

public class VehicleTypeEntity {

    public int iconResId;
    public int iconSecectedResId;
    public int placeHolderId;

    public VehicleTypeEntity(  int iconResId, int iconSecectedResId, int placeHolderId) {
        this.placeHolderId = placeHolderId;
        this.iconResId = iconResId;
        this.iconSecectedResId = iconSecectedResId;
    }


}
