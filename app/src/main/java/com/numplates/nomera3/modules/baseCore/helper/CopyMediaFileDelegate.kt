package com.numplates.nomera3.modules.baseCore.helper

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import com.meera.core.extensions.getAuthority
import com.meera.core.extensions.returnReadExternalStoragePermissionAfter33
import com.meera.core.extensions.returnWriteExternalStoragePermissionAfter33
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.graphics.NGraphics.shareImageToDevice
import com.meera.db.models.message.MessageAttachment
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.helpers.isNetworkUri
import com.numplates.nomera3.presentation.router.BaseAct
import com.numplates.nomera3.presentation.view.utils.NToast.Companion.with
import timber.log.Timber
import java.io.File


interface CopyMediaFileDelegate {

    fun shareImageFile(
        imageUrl: String,
        act: Activity,
        viewLifecycleOwner: LifecycleOwner,
        successListener: (Uri) -> Unit
    )

    fun copyImageFile(
        attachment: MessageAttachment,
        act: Activity,
        viewLifecycleOwner: LifecycleOwner,
        successListener: (Uri) -> Unit
    )
}

class CopyMediaFileDelegateImpl: CopyMediaFileDelegate {

    override fun copyImageFile(
        attachment: MessageAttachment,
        act: Activity,
        viewLifecycleOwner: LifecycleOwner,
        successListener: (Uri) -> Unit
    ) {
        PermissionDelegate(
            activity = act,
            viewLifecycleOwner = viewLifecycleOwner
        ).setPermissions(
            listener = object : PermissionDelegate.Listener {
                override fun onGranted() {
                    val imageUrl = attachment.url
                    val copiedUri = Uri.parse(imageUrl)
                    val clipUri = if (copiedUri.isNetworkUri()) {
                        copiedUri
                    } else {
                        FileProvider.getUriForFile(act, act.getAuthority(), File(copiedUri.path))
                    }
                    val clipboardManager = act.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val theClip = ClipData.newUri(act.contentResolver, "Image",clipUri)
                    clipboardManager.setPrimaryClip(theClip)
                    successListener(copiedUri)
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

    override fun shareImageFile(
        imageUrl: String,
        act: Activity,
        viewLifecycleOwner: LifecycleOwner,
        successListener: (Uri) -> Unit
    ) {
        PermissionDelegate(
            activity = act,
            viewLifecycleOwner = viewLifecycleOwner
        ).setPermissions(
            listener = object : PermissionDelegate.Listener {
                override fun onGranted() {
                    shareImageToDevice(
                        act = act,
                        imageUrl = imageUrl,
                        onSaved = { uri ->
                            successListener(uri)
                        },
                        onLoadFailed = {
                            Timber.e("On fail save image:$imageUrl")
                            showErrorToast(act, R.string.error_check_internet)
                        }
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
