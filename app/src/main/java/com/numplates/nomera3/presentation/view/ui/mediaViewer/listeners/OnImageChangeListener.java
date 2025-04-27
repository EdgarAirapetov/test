package com.numplates.nomera3.presentation.view.ui.mediaViewer.listeners;


import com.numplates.nomera3.presentation.view.ui.mediaViewer.ImageViewerData;

public interface OnImageChangeListener {
    void onImageChange(int position);

    default void onImageAdded(ImageViewerData image){}

    default void onImageChecked(ImageViewerData image, boolean isChecked){}

}
