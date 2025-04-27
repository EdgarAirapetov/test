package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import com.meera.db.models.userprofile.UserSettingsFlags
import com.meera.db.models.userprofile.UserSimple
import java.io.Serializable

data class UserModel(@SerializedName("user_id") var userId: Long,
                     @SerializedName("name") var name: String,
                     @SerializedName("birthday") var birthday: Long?,
                     @SerializedName("avatar") var avatar: String?,
                     @SerializedName("account_type") var accountType: Int?,
                     @SerializedName("account_color") var accountColor: Int?,
                     @SerializedName("gender") var gender: Int,
                     var isChecked: Boolean = false,
                     @SerializedName("city") var city: String? = "",
                     var settingsFlags: UserSettingsFlags? = null,
                     @SerializedName("uniqname") val uniqueName: String? = null,
                     @SerializedName("approved") val approved: Int = 0
) : Serializable{
    constructor(userSimple: UserSimple) : this(
            userSimple.userId, userSimple.name?:"", userSimple.birthday,
            userSimple.avatarSmall, userSimple.accountType?: 0,
            userSimple.accountColor, userSimple.gender?:0, false,
            userSimple.city?.name, userSimple.settingsFlags
    )
}
