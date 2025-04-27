package com.numplates.nomera3.data.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meera.db.models.dialog.UserChat
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Contact(
        @SerializedName("user")
        var userEntity: UserChat,

        @SerializedName("localId")
        var contactID: Long
) : Parcelable

@Parcelize
data class ContactResponse(
        @SerializedName("contacts")
        var contacts: List<Contact>
) : Parcelable
