package com.numplates.nomera3.modules.remotestyle.data.posts

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostRulesRootEntity(
    @SerializedName("only_text")
    var postOnlyTextTextStyle: PostOnlyTextRemoteStyle?
) : Parcelable