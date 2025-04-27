package com.numplates.nomera3.presentation.viewmodel.viewevents

import android.net.Uri
import com.meera.core.utils.tedbottompicker.models.MediaUriModel
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentMediaModel
import com.numplates.nomera3.modules.uploadpost.ui.entity.NotAvailableReasonUiEntity
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum

sealed class AddPostViewEvent {
    object UploadStarting : AddPostViewEvent()
    data class NeedToShowRoadPrivacyDialog(val roadPrivacySetting: SettingsUserTypeEnum) : AddPostViewEvent()
    object NeedToShowModerationDialog : AddPostViewEvent()
    @Deprecated("BR-29237: выпиливается вместе со старым экраном создания поста: AddPostFragment")
    data class NeedToShowResetEditedMediaDialog(val uri: Uri? = null, val isAdding: Boolean = false, val openCamera: Boolean): AddPostViewEvent()
    data class ToShowResetEditedMediaDialog(val mediaModel: UIAttachmentMediaModel, val openCamera: Boolean = false): AddPostViewEvent()
    @Deprecated("BR-29237: выпиливается вместе со старым экраном создания поста AddPostFragment")
    data class SetAttachment(val uri: Uri, val afterEdit: Boolean): AddPostViewEvent()
    @Deprecated("BR-29237: выпиливается вместе со старым экраном создания поста AddPostFragment")
    object RemoveAttachment: AddPostViewEvent()
    data class OpenCamera(val afterResetMedia: Boolean): AddPostViewEvent()
    data class UploadVideo(val videoUrl: String): AddPostViewEvent()
    object ShowMediaPicker: AddPostViewEvent()
    object ShowMaxCountReachedWarning: AddPostViewEvent()
    object HideMediaPicker: AddPostViewEvent()
    object KeyboardHeightChanged: AddPostViewEvent()

    class ShowAvailabilityError(val reason: NotAvailableReasonUiEntity): AddPostViewEvent()
    //Empty добавлен для кейса, когда onActivityResult срабатывает первее onResume. Из BehaviorSubject в [AddPostViewModel]
    // при подписке необходимо получить последнее значение, но чтоб событие не дублировалось, дергаем после получения события Empty ивент
    object Empty: AddPostViewEvent()
    object Success: AddPostViewEvent()
    class MediaPagerChanges(val attachments: List<MediaUriModel>): AddPostViewEvent()
}
