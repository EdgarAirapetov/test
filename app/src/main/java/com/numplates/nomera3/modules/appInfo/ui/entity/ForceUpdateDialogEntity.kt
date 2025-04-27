package com.numplates.nomera3.modules.appInfo.ui.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/**
 *  if one of the params is null dialog will use default value
 * */
@Parcelize
data class ForceUpdateDialogEntity (
    var title: String? = null,
    var subtitle: String? = null,
    var canBeClosed: Boolean = true,
    var btnTitle: String? = null
): Parcelable {
    var appVersion: String? = null
}