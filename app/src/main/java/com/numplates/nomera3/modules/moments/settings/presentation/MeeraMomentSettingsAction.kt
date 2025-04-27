package com.numplates.nomera3.modules.moments.settings.presentation

sealed class MeeraMomentSettingsAction {
    class ShowOnlyFriends(val isCheck: Boolean): MeeraMomentSettingsAction()
    class HideFrom(val count: Int): MeeraMomentSettingsAction()
    class HideMoment(val count: Int): MeeraMomentSettingsAction()
    class AllowComments(val variant: Int): MeeraMomentSettingsAction()
    class SaveGallery(val isCheck: Boolean): MeeraMomentSettingsAction()
}
