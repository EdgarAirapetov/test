package com.numplates.nomera3.presentation.view.adapter;

import com.numplates.nomera3.modules.userprofile.ui.model.PhotoModel;
import com.viewpagerindicator.IconPagerAdapter;

import java.util.List;

public interface InfinityAdapter extends IconPagerAdapter {

    String getPreviewUrl(int index);

    int getRealCount();

    List<PhotoModel> getGallery();
}
