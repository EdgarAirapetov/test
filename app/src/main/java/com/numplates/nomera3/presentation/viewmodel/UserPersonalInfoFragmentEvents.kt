package com.numplates.nomera3.presentation.viewmodel

import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.modules.user.data.entity.UserEmail
import com.numplates.nomera3.modules.user.data.entity.UserPhone
import com.numplates.nomera3.presentation.viewmodel.userpersonalinfo.NicknameValidationResult
import com.numplates.nomera3.presentation.viewmodel.userpersonalinfo.UniqueUsernameValidationResult

sealed class UserPersonalInfoFragmentEvents {
    class OnPhotoUploaded(val userId: Long) : UserPersonalInfoFragmentEvents()
    class OnShowCityDialog(val cities: List<City>) : UserPersonalInfoFragmentEvents()
    class OnNicknameValidated(val result: NicknameValidationResult, val nickname: String? = null) :
        UserPersonalInfoFragmentEvents()

    object OnShowCountryDialog : UserPersonalInfoFragmentEvents()
    class OnUniquenameValidated(val result: UniqueUsernameValidationResult, val userName: String? = null) :
        UserPersonalInfoFragmentEvents()

    class OnProfileUploaded(val result: Boolean, val exit: Boolean) : UserPersonalInfoFragmentEvents()
    class OnRandomAvatarGenerated(val animatedAvatar: String, val file: String) : UserPersonalInfoFragmentEvents()
    class OnAccountEmailUpdated(val userEmail: UserEmail) : UserPersonalInfoFragmentEvents()
    class OnAccountPhoneUpdated(val userPhone: UserPhone) : UserPersonalInfoFragmentEvents()
    class OnProfileSaveResult(val isSuccess: Boolean, val isExit: Boolean) : UserPersonalInfoFragmentEvents()
}
