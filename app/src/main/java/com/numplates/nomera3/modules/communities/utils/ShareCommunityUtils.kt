package com.numplates.nomera3.modules.communities.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent

private const val COMMUNITY_LINK_LABEL = "community_link"
private const val SHARE_LINK_MIME_TYPE = "text/html"


fun copyCommunityLink(
    context: Context?,
    link: String,
    result: () -> Unit
) {
    val clipData = ClipData.newPlainText(COMMUNITY_LINK_LABEL, link)
    val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(clipData)
    result.invoke()
}

fun shareLinkOutside(
    context: Context?,
    link: String
) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, link)
        type = SHARE_LINK_MIME_TYPE
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    context?.startActivity(shareIntent)
}