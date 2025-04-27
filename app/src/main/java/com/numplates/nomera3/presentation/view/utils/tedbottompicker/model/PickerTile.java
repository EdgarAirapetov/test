package com.numplates.nomera3.presentation.view.utils.tedbottompicker.model;

import android.net.Uri;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public class PickerTile implements Cloneable {

    public static final int IMAGE = 1;
    public static final int CAMERA = 2;
    public static final int GALLERY = 3;
    public static final int VIDEO = 4;

    private boolean isOverlaid = false;
    private boolean isSelected = false;
    private long duration;
    private int counter;

    private final Uri imageUri;
    private final @TileType int tileType;

    public PickerTile(@NonNull Uri imageUri) {
        this(imageUri, IMAGE);
    }

    public PickerTile(@SpecialTileType int tileType) {
        this(null, tileType);
    }

    public PickerTile(@Nullable Uri imageUri, @TileType int tileType) {
        this(imageUri, tileType, 0L, 0);
    }

    public PickerTile(@Nullable Uri imageUri, @TileType int tileType, long duration, int counter) {
        this.imageUri = imageUri;
        this.tileType = tileType;
        this.duration = duration;
        this.counter = counter;
    }

    @NonNull
    @Override
    public PickerTile clone() throws CloneNotSupportedException {
        return new PickerTile(imageUri, tileType, duration, counter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PickerTile)) return false;
        PickerTile that = (PickerTile) o;
        return isOverlaid() == that.isOverlaid() &&
            isSelected() == that.isSelected() &&
            getDuration() == that.getDuration() &&
            getCounter() == that.getCounter() &&
            getTileType() == that.getTileType() &&
            Objects.equals(getImageUri(), that.getImageUri());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isOverlaid(), isSelected(), getDuration(), getCounter(), getImageUri(), getTileType());
    }

    public boolean isOverlaid() {
        return isOverlaid;
    }

    public void setOverlaid(boolean overlaid) {
        isOverlaid = overlaid;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public int getTileType() {
        return tileType;
    }

    public boolean isImageTile() {
        return tileType == IMAGE;
    }

    public boolean isCameraTile() {
        return tileType == CAMERA;
    }

    public boolean isGalleryTile() {
        return tileType == GALLERY;
    }

    public boolean isVideoTile() {
        return tileType == VIDEO;
    }

    @IntDef({IMAGE, CAMERA, GALLERY, VIDEO})
    @Retention(RetentionPolicy.SOURCE)
    private @interface TileType {
    }

    @IntDef({CAMERA, GALLERY})
    @Retention(RetentionPolicy.SOURCE)
    private @interface SpecialTileType {
    }
}
