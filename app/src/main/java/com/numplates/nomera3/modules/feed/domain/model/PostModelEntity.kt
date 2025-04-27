package com.numplates.nomera3.modules.feed.domain.model

import com.meera.db.models.message.UniquenameEntity
import com.numplates.nomera3.data.network.Asset
import com.numplates.nomera3.data.network.MediaAssetDto
import com.numplates.nomera3.data.network.MediaPositioningDto
import com.numplates.nomera3.data.network.Vehicle
import com.numplates.nomera3.modules.feed.data.entity.CityEntityResponse
import com.numplates.nomera3.modules.feed.data.entity.CountryEntityResponse
import com.numplates.nomera3.modules.feed.data.entity.UserEntityResponse
import com.numplates.nomera3.modules.maps.data.model.EventDto
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntity
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity

data class PostModelEntity(
    val id: Long,
    val aspect: Double,
    val commentsCount: Int,
    val date: Long,
    val editedAt: Long?,
    val deleted: Int,
    val groupId: Long,
    val info: Int,
    val itsSubscribed: Int,
    val mainVehicle: Vehicle?,
    val repostsCount: Int,
    val text: String,
    val image: String?,
    var smallImage: String?,
    var smallUrl: String?,
    val title: String,
    val user: UserEntityResponse?,
    val parentPost: PostModelEntity?,
    val asset: Asset?,
    val assets: List<MediaAssetDto>?,
    val createdAt: Long,
    val itemType: Int,
    val isAllowedToComment: Boolean,
    val isAdultContent: Boolean?,
    val refreshItem: Int,
    val parentPostId: Long,
    val privacy: String?,
    val groupName: String?,
    val groupAvatar: String?,
    val tags: List<UniquenameEntity?>?,
    val country: CountryEntityResponse?,
    val city: CityEntityResponse?,
    val videoDurationInSeconds: Int?,
    val video: String?,
    val videoPreview: String?,
    val media: MediaEntity?,
    val reactions: List<ReactionEntity>?,
    var groupType: Int?,
    var sourceType: String?,
    var type: Int,
    var commentAvailability: String?,
    var event: EventDto?,
    var backgroundUrl: String?,
    var backgroundId: Int?,
    var fontColor: String?,
    var fontSize: Int?,
    var mediaPositioning: MediaPositioningDto?,
    val isNewSubsPost: Int?,
    val postInEditProcess: Boolean,
    val isPostHidden: Boolean?
)
