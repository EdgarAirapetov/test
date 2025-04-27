package com.numplates.nomera3.modules.maps.data.model

import com.google.gson.annotations.SerializedName
import com.meera.db.models.message.ParsedUniquename
import com.meera.db.models.message.UniquenameEntity
import com.numplates.nomera3.modules.userprofile.data.entity.UserSimpleDto

data class EventDto(
    @SerializedName("title") val title: String,
    @SerializedName("tags") val tags: List<UniquenameEntity>,
    @SerializedName("id") val id: Long,
    @SerializedName("post_id") val postId: Long,
    @SerializedName("address_info") val address: EventAddressDto,
    @SerializedName("date") val startTime: String,
    @SerializedName("type") val eventType: Int,
    @SerializedName("participants") val participants: List<UserSimpleDto>,
    @SerializedName("membership") val participation: ParticipationDto,
    var tagSpan: ParsedUniquename? = null,
)
