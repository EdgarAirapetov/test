package com.numplates.nomera3.modules.upload.data.post

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.MediaPositioningDto
import com.numplates.nomera3.modules.uploadpost.ui.data.AttachmentPostType
import kotlinx.parcelize.Parcelize

@Parcelize
data class UploadMediaModel(
    val mediaType: AttachmentPostType,
    val mediaUriPath: String,
    val initialStringUri: String,
    val editedStringUri: String?,
    val dtoPositioning: MediaPositioningDto,
    val mediaWasCompressed: Boolean = false,
    val uploadMediaId: String? = null
) : Parcelable

@Parcelize
data class EditedAssetModel(
    @SerializedName("asset_id")
    val assetId: String? = null,
    @SerializedName("upload_id")
    val uploadId: String? = null,
    @SerializedName("media_positioning")
    val mediaPositioning: MediaPositioningDto
) : Parcelable
