package com.numplates.nomera3.presentation.view.adapter.newchat.chatimage

data class PostImage(
    val url: String? = null,
    val isShowGiphyWatermark: Boolean = false
) {
    override fun toString(): String {
        return "PostImage(url=$url, isShowGiphyWatermark=$isShowGiphyWatermark)"
    }
}
