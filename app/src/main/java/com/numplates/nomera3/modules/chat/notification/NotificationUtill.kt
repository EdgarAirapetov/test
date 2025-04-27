package com.numplates.nomera3.modules.chat.notification

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.dp
import com.numplates.nomera3.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

const val PUSH_AVATAR_SIZE = 64

fun CoroutineScope.getAvatarBitmapForNotification(
    context: Context,
    avatarSmall: String?,
    listener: (bitmapAvatar: Bitmap) -> Unit
) {
    val futureTarget = Glide.with(context)
        .asBitmap()
        .load(avatarSmall)
        .override(PUSH_AVATAR_SIZE.dp)
        .apply(
            RequestOptions
                .circleCropTransform()
                .placeholder(R.drawable.fill_8_round)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        ).submit()
    launch (Dispatchers.IO){
        val bitmap = try {
            futureTarget.get()
        } catch (e: Exception) {
            Timber.e("Get bitmap failed ${e.message}")
            BitmapFactory.decodeResource(context.resources, R.drawable.fill_8_round)
        }
        withContext(Dispatchers.Main) {
            listener(bitmap)
        }
    }
}
