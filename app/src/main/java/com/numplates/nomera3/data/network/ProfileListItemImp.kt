package com.numplates.nomera3.data.network

import com.numplates.nomera3.presentation.view.view.ProfileListItem
import java.io.Serializable

data class ProfileListItemImp (var title_ : String?, var num_ : String?, var imageUrl_ : String?) : Serializable, ProfileListItem {
    override val caption: String?
        get() = title_
    override val imageUrl: String?
        get() = imageUrl_
    override val num: String?
        get() = num_
}