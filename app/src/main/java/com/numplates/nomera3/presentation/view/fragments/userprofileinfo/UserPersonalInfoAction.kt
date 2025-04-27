package com.numplates.nomera3.presentation.view.fragments.userprofileinfo

import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.modules.user.data.entity.UserEmail
import com.numplates.nomera3.modules.user.data.entity.UserPhone

sealed class UserPersonalInfoAction {
    class CountryItemClick(val country: (countryModel: RegistrationCountryModel) -> Unit) : UserPersonalInfoAction()
    class CityItemClick(val countryId: Long?, val city: (cityModel: City) -> Unit) : UserPersonalInfoAction()
    class DeleteItemClick : UserPersonalInfoAction()
    class BirthdayItemClick(val tag: Any?) : UserPersonalInfoAction()
    class InputFullName(val fullName: String, val errorAction: (errorText: String?) -> Unit) : UserPersonalInfoAction()
    class InputUniqueName(val uniqueName: String, val errorAction: (errorText: String?) -> Unit) :
        UserPersonalInfoAction()

    class GenderItemClick(val isMale: Boolean) : UserPersonalInfoAction()
    class PhoneItemClick(val phone: (userPhone: UserPhone) -> Unit) : UserPersonalInfoAction()
    class MailItemClick(val mail: (userEmail: UserEmail) -> Unit) : UserPersonalInfoAction()
    class AvatarItemClick(val avatar: (Pair<String?, String?>?) -> Unit) : UserPersonalInfoAction()
}
