package com.numplates.nomera3.data.network

import com.meera.db.models.dialog.UserChat

data class ContactsModel(

        var contact: Contact? = null,
        var user: UserChat?,
        var holderType: Int

) {
    data class Contact(
            var contactId: Long?,
            var phone: String?,
            var name: String?,
            var avatar: String?
    )
}
