package com.numplates.nomera3.modules.upload.mapper

import com.meera.core.extensions.fromJson
import com.meera.db.models.UploadBundle
import com.meera.db.models.UploadItem
import com.meera.db.models.UploadType
import com.numplates.nomera3.modules.upload.data.moments.UploadMomentBundle
import com.numplates.nomera3.modules.upload.data.post.UploadPostBundle

object UploadBundleMapper {
    fun map(uploadItem: UploadItem): UploadBundle {
        return map(uploadItem.type, uploadItem.uploadBundleStringify)
    }

    fun map(type: UploadType, uploadBundleStringify: String): UploadBundle {
        return when (type) {
            UploadType.Post, UploadType.EditPost, UploadType.EventPost -> {
                uploadBundleStringify.fromJson<UploadPostBundle>(UploadPostBundle::class.java)
            }
            UploadType.Moment -> {
                uploadBundleStringify.fromJson<UploadMomentBundle>(UploadMomentBundle::class.java)
            }
        }
    }
}
