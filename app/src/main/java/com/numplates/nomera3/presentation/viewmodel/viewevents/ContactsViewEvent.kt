package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class ContactsViewEvent {
    object FailedToSendContacts: ContactsViewEvent()
    object ClearAdapter: ContactsViewEvent()
    object FailedToRequestIsVerified: ContactsViewEvent()

    object UserVerified: ContactsViewEvent()
    object UserIsNotVerified: ContactsViewEvent()
}