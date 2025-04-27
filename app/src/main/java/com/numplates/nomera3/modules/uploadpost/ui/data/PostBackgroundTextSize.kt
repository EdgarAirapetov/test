package com.numplates.nomera3.modules.uploadpost.ui.data

enum class PostBackgroundTextSize(val relativeValue: Int, val absoluteValue: Float) {
    SIZE_5(5, 32f),
    SIZE_4(4, 28f),
    SIZE_3(3, 24f),
    SIZE_2(2, 20f),
    SIZE_1(1, 16f);

    companion object {
        fun findByRelativeValue(value: Int?): PostBackgroundTextSize {
            return value?.let { PostBackgroundTextSize.values().find { it.relativeValue == value } ?: SIZE_5 } ?: SIZE_5
        }
    }
}
