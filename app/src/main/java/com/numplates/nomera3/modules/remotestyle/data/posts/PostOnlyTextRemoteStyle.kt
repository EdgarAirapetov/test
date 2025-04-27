package com.numplates.nomera3.modules.remotestyle.data.posts

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Набор правил и стилей применимый только для Поста с одним текстом
 */
@Parcelize
data class PostOnlyTextRemoteStyle(
    @SerializedName("rules")
    val rules: List<Rule>,
    @SerializedName("styles")
    val styles: List<Style>
) : Parcelable {

    @Parcelize
    data class Rule(
        @SerializedName("length")
        val length: Int,
        @SerializedName("max_lines")
        val maxLines: Int,
        @SerializedName("style")
        val style: String,
        @SerializedName("type")
        val type: String
    ) : Parcelable

    @Parcelize
    data class Style(
        @SerializedName("fontColor")
        val fontColor: String,
        @SerializedName("fontColorVip")
        val fontColorVip: String,
        @SerializedName("fontSize")
        val fontSize: Int,
        @SerializedName("type")
        val type: String
    ) : Parcelable
}

