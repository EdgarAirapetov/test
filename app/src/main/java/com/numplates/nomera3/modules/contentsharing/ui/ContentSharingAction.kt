package com.numplates.nomera3.modules.contentsharing.ui

import android.net.Uri
import com.meera.core.base.viewmodel.Action

sealed class ContentSharingAction : Action {

    class ScheduleLoadingUri(val uris: List<Uri>) : ContentSharingAction()

    class ScheduleSendLink(val link: String) : ContentSharingAction()

    class UpdateSharingState(val sharingState: SharingState) : ContentSharingAction()

    data object CloseWithAnError : ContentSharingAction()

    data object CloseWithoutAnError : ContentSharingAction()

    data object ShowNetworkError : ContentSharingAction()

    data object ShowVideoDurationError : ContentSharingAction()

    data object CheckSharingState : ContentSharingAction()
}
