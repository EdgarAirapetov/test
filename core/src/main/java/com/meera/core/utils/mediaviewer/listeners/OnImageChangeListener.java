package com.meera.core.utils.mediaviewer.listeners;

import com.meera.core.utils.mediaviewer.ImageViewerData;

public interface OnImageChangeListener {
    void onImageChange(int position);

    default void onImageAdded(ImageViewerData image){}

    default void onImageChecked(ImageViewerData image, boolean isChecked){}

    default void onImageEdited(ImageViewerData image) {}

}
