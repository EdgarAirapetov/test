package com.noomeera.nmravatarssdk

import android.content.Context
import android.os.Bundle
import com.noomeera.nmravatarssdk.data.AvatarParams
import com.noomeera.nmravatarssdk.ui.AvatarEditorFragment
import com.noomeera.nmravatarssdk.ui.MeeraAvatarEditorFragment
import com.noomeera.nmravatarssdk.utils.Decompress
import com.noomeera.nmravatarssdk.utils.KVStorage
import java.io.File
import java.io.InputStream

const val REQUEST_NMR_KEY_AVATAR = "NMR_AVATAR_RESULT"
const val NMR_AVATAR_STATE_JSON_KEY = "AVATAR_JSON_KEY"
const val REQUEST_NMR_BACK_PRESSED = "NMR_BACK_PRESSED"

object NMRAvatarsSDK {
    fun setResourcesSync(context: Context, zipStream: InputStream): Boolean {
        try {
            // remove last version
            val oldVersion = File(getResourcesDirPath(context))
            if (oldVersion.isDirectory) {
                oldVersion.deleteRecursively()
            }

            // unzip process
            Decompress(zipStream, getResourcesDirPath(context)).also {
                it.unzip()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            KVStorage(context).storeBoolean("synced", false)
            return false
        }

        KVStorage(context).storeBoolean("synced", true)
        return true
    }

    fun isSdkReady(context: Context): Boolean {
        return KVStorage(context).checkBoolean("synced") && File(getResourcesDirPath(context)).isDirectory
    }

    fun getResourcesDirPath(context: Context): String {
        return context.filesDir.toString() + "/" + Constants.DIRNAME + "/"
    }

    fun createAvatarFragment(params: AvatarParams?) = AvatarEditorFragment().apply {
        arguments = Bundle().apply {
            params?.avatarState?.let { putString(Constants.STATE_AVATAR_DATA_KEY, it) }
            params?.quality?.let { putFloat(Constants.QUALITY_AVATAR_DATA_KEY, it) }
        }
    }

    fun createMeeraAvatarFragment(params: AvatarParams?) = MeeraAvatarEditorFragment().apply {
        arguments = Bundle().apply {
            params?.avatarState?.let { putString(Constants.STATE_AVATAR_DATA_KEY, it) }
            params?.quality?.let { putFloat(Constants.QUALITY_AVATAR_DATA_KEY, it) }
        }
    }
}
