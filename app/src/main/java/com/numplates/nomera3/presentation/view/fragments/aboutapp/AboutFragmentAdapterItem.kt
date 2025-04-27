package com.numplates.nomera3.presentation.view.fragments.aboutapp

import androidx.annotation.DrawableRes

internal const val TYPE_CONTENT = 1
internal const val TYPE_SUPPORT = 3
internal const val TYPE_AGREEMENT = 4
internal const val TYPE_WEB_SITE = 5
internal const val TYPE_COLLABA = 6

sealed class AboutFragmentAdapterItem(
    val adapterType: Int,
    val text: String
) {
    class Content(@DrawableRes val imageId: Int, text: String) : AboutFragmentAdapterItem(TYPE_CONTENT, text)

    class WebSite(text: String) : AboutFragmentAdapterItem(TYPE_WEB_SITE, text)

    class Support(text: String) : AboutFragmentAdapterItem(TYPE_SUPPORT, text)

    class Collaba(text: String) : AboutFragmentAdapterItem(TYPE_COLLABA, text)

    class Agreement(text: String) : AboutFragmentAdapterItem(TYPE_AGREEMENT, text)
}
