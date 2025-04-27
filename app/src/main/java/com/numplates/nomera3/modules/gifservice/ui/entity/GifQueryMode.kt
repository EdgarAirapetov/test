package com.numplates.nomera3.modules.gifservice.ui.entity

import com.meera.core.extensions.empty

sealed class GifQueryMode {

    object Recent: GifQueryMode()

    class Trending(val query: String = String.empty()): GifQueryMode()

    class Search(val query: String = String.empty()): GifQueryMode()

    class Emoji(val query: String): GifQueryMode()
}