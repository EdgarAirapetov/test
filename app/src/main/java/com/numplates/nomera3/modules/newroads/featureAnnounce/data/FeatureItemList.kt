package com.numplates.nomera3.modules.newroads.featureAnnounce.data

import com.meera.db.models.message.ParsedUniquename


data class FeatureItemList(
    var text: String?,
    val button: String?,
    val deepLink: String?,
    val id: Long,
    val hideable: Boolean,
    val tags: ParsedUniquename?,
    val aspect: Double,
    val videoDurationInSeconds: Int? = null,
    val video: String? = null,
    val videoPreview: String? = null,
    val image: String? = null,
    val smallImage: String? = null)
