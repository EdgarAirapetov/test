package com.numplates.nomera3.modules.communities.data.entity

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Community(
        @SerializedName("group") var community: CommunityEntity?
): Serializable