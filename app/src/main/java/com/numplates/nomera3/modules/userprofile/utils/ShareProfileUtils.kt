package com.numplates.nomera3.modules.userprofile.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent

private const val PROFILE_LINK_LABEL = "profile_link"
private const val SHARE_LINK_MIME_TYPE = "text/html"

fun copyProfileLink(
    context: Context?,
    shareProfileUrl: String,
    uniqueName: String,
    result: () -> Unit
) {
    val clipData = ClipData.newPlainText(
        PROFILE_LINK_LABEL,
        "${shareProfileUrl}$uniqueName"
    )
    val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(clipData)
    result.invoke()
}

fun shareProfileOutside(
    context: Context?,
    shareProfileUrl: String,
    uniqueName: String
) {
    val shareLink = "${shareProfileUrl}$uniqueName"
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareLink)
        type = SHARE_LINK_MIME_TYPE
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    context?.startActivity(shareIntent)
}
