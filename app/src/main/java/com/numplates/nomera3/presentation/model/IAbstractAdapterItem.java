package com.numplates.nomera3.presentation.model;

import androidx.annotation.IntRange;

public interface IAbstractAdapterItem {
    int ITEM_HEADER = 0;
    int ITEM_DATA = 1;
    @IntRange(from = 0, to = 1) int getItemType();
}
