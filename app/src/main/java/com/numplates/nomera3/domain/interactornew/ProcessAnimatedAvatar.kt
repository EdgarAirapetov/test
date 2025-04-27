package com.numplates.nomera3.domain.interactornew

import android.graphics.Bitmap

interface ProcessAnimatedAvatar {
    fun createBitmap(avatarState: String): Bitmap
    fun saveInFile(bitmap: Bitmap): String
    fun saveInFileWithWaterMark(bitmap: Bitmap): String
    fun saveInFileWithWaterMarkWithUniqueName(bitmap: Bitmap, uniqueName: String?): String
}
