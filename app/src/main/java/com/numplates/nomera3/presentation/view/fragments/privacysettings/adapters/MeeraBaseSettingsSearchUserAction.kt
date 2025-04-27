package com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters

import com.meera.db.models.userprofile.UserSimple

sealed class MeeraBaseSettingsSearchUserAction {
    class UserChecked(val user: UserSimple): MeeraBaseSettingsSearchUserAction()
}
