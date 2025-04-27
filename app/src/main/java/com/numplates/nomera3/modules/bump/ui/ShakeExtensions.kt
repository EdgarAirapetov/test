package com.numplates.nomera3.modules.bump.ui

import androidx.fragment.app.Fragment
import com.numplates.nomera3.modules.bump.ui.fragment.MeeraShakeFriendRequestsFragment
import com.numplates.nomera3.modules.bump.ui.fragment.ShakeFriendRequestsFragment
import com.numplates.nomera3.modules.feedviewcontent.presentation.fragment.ViewContentFragment
import com.numplates.nomera3.modules.onboarding.OnboardingContentFragment
import com.numplates.nomera3.modules.onboarding.OnboardingFragment
import com.numplates.nomera3.modules.purchase.ui.send.SendGiftFragment
import com.numplates.nomera3.modules.uploadpost.ui.AddMultipleMediaPostFragment
import com.numplates.nomera3.modules.uploadpost.ui.AddPostFragment
import com.numplates.nomera3.presentation.view.fragments.profilephoto.ProfilePhotoViewerFragment
import com.numplates.nomera3.telecom.CallFragment

/**
 * По flow в данных экранах [com.numplates.nomera3.modules.bump.hardware.ShakeEventListener] не будет зарегистрирован
 * https://nomera.atlassian.net/wiki/spaces/NOM/pages/3134128129
 */
internal fun Fragment.isAllowToRegisterShakeInCurrentScreen(): Boolean {
    return this !is AddPostFragment
        && this !is AddMultipleMediaPostFragment
        && this !is CallFragment
        && this !is OnboardingFragment
        && this !is OnboardingContentFragment
        && this !is SendGiftFragment
        && this !is ShakeFriendRequestsFragment
        && this !is MeeraShakeFriendRequestsFragment
        && this !is ProfilePhotoViewerFragment
        && this !is ViewContentFragment
}
