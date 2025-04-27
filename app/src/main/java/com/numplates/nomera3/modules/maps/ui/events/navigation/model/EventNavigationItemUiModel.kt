package com.numplates.nomera3.modules.maps.ui.events.navigation.model

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class EventNavigationItemUiModel(
    val appName: String,
    @DrawableRes
    val iconResId: Int,
    @StringRes
    val titleResId: Int,
    val navigatorIntent: Intent,
)
