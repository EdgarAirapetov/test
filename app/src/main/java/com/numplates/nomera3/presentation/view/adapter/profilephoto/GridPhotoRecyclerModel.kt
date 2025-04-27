package com.numplates.nomera3.presentation.view.adapter.profilephoto

import com.numplates.nomera3.data.newmessenger.response.Photo

class GridPhotoRecyclerModel(val type:Int) {
    var date: String? = null
    var photo: Photo? = null

    companion object{
        const val TYPE_PHOTO = 415
        const val TYPE_TITLE = 234
    }
}
