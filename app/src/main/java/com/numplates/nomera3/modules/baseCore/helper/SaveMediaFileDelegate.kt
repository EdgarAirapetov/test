package com.numplates.nomera3.modules.baseCore.helper

import android.app.Activity
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import com.meera.core.permission.PermissionDelegate
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.router.BaseAct
import com.meera.core.extensions.returnReadExternalStoragePermissionAfter33
import com.meera.core.extensions.returnWriteExternalStoragePermissionAfter33
import com.meera.core.utils.graphics.NGraphics.saveImageToDevice
import com.numplates.nomera3.presentation.utils.runOnUiThread
import com.numplates.nomera3.presentation.view.utils.NToast.Companion.with
import timber.log.Timber

interface SaveMediaFileDelegate {

    fun saveImageOrVideoFile(
        imageUrl: String,
        act: Activity,
        viewLifecycleOwner: LifecycleOwner,
        successListener: (uri:Uri) -> Unit,
        saveToCache:Boolean = false
    )
}

class SaveMediaFileDelegateImpl: SaveMediaFileDelegate {

    override fun saveImageOrVideoFile(
        imageUrl: String,
        act: Activity,
        viewLifecycleOwner: LifecycleOwner,
        successListener: (uri: Uri) -> Unit,
        saveToCache: Boolean
    ) {
        PermissionDelegate(
            activity = act,
            viewLifecycleOwner = viewLifecycleOwner
        ).setPermissions(
            listener = object : PermissionDelegate.Listener {
                override fun onGranted() {
                    saveImageToDevice(
                        context = act,
                        imageUrl = imageUrl,
                        onSaved = { uri ->
                            Timber.d("On successfully saved image:$uri")
                            runOnUiThread { successListener(uri) }
                        },
                        onLoadFailed = {
                            Timber.e("On fail save image:$imageUrl")
                            showErrorToast(act, R.string.error_check_internet)
                        },
                        saveToCache = saveToCache
                    )
                }

                override fun onDenied() {
                    showErrorToast(act, R.string.you_must_grant_permissions)
                }

                override fun onError(error: Throwable?) {
                    Timber.e("ERROR get Permissions: $error")
                }

            },
            returnReadExternalStoragePermissionAfter33(),
            returnWriteExternalStoragePermissionAfter33(),
        )
    }

    private fun showErrorToast(activity: Activity, @StringRes message: Int) {
        with(activity as BaseAct)
            .text(activity.getString(message))
            .durationLong()
            .show()
    }
}
