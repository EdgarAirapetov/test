package com.numplates.nomera3.modules.communities.ui.fragment

sealed class MeeraCommunityEditAction {
    class AddPhoto(val selectUrl: (photoUrl: String) -> Unit) : MeeraCommunityEditAction()
    class EditNameCommunity(val name: String, val validationErrorState: (textError: String) -> Unit) :
        MeeraCommunityEditAction()

    class EditDescriptionCommunity(val description: String, val validationErrorState: (textError: String) -> Unit) :
        MeeraCommunityEditAction()

    class OpenCommunity(val isEnable: Boolean) : MeeraCommunityEditAction()
    class CloseCommunity(val isEnable: Boolean) : MeeraCommunityEditAction()
    class OnlyAdministrationWrites(val isEnable: Boolean) : MeeraCommunityEditAction()
    class EditPhoto(val imageUrl: String, val isLoadDevices: Boolean, val selectUrl: (photoUrl: String) -> Unit) :
        MeeraCommunityEditAction()

    class OpenPicker(val selectUrl: (photoUrl: String) -> Unit) : MeeraCommunityEditAction()
    class DeletePhoto() : MeeraCommunityEditAction()
}
