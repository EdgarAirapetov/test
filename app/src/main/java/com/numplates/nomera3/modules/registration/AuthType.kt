package com.numplates.nomera3.modules.registration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class AuthType: Parcelable {
        @Parcelize
        object Phone : AuthType()
        @Parcelize
        object Email : AuthType()
}
