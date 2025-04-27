package com.numplates.nomera3.modules.user.domain.usecase

import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE
import com.numplates.nomera3.modules.fileuploads.data.model.PartialUploadSourceType
import com.numplates.nomera3.modules.fileuploads.domain.usecase.PartialFileUploadUseCase
import com.numplates.nomera3.modules.user.data.entity.UploadAvatarResponse
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import javax.inject.Inject

class UpdateUserAvatarUseCase @Inject constructor(
    private val repository: UserRepository,
    private val partialFileUploadUseCase: PartialFileUploadUseCase
) {

    suspend fun invoke(
        imagePath: String,
        animation: String?,
        createAvatarPost: Int,
        saveSettings: Int
    ): UploadAvatarResponse {

        val imageFile = File(imagePath)
        val uploadId = partialFileUploadUseCase.invoke(
            fileToUpload = imageFile,
            mediaType = MEDIA_TYPE.toMediaTypeOrNull(),
            sourceType = PartialUploadSourceType.AVATAR
        )

        return repository.uploadAvatarSuspend(
            uploadId = uploadId,
            animation = animation,
            createAvatarPost = createAvatarPost,
            saveSettings = saveSettings
        )
    }
}
