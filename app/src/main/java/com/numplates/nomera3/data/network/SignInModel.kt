package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SignInModel(
        @SerializedName("key") var key: String?,                                    //old api
        @SerializedName("token") var token: String?,                             //new api
        @SerializedName("uid") var uid: Long?,                                   //new api
        @SerializedName("anonym_id") var anonymId: Long?,                                   //new api
        @SerializedName("hide___content") var hideContent: Int,                     //old api
        @SerializedName("date") var date: Int,                                      //old api
        @SerializedName("AD_ENABLE") var adEnable: Int,                             //old api
        @SerializedName("version_state") var versionState: String?,                 //old api
        @SerializedName("state_id") var stateId: Int,                               //old api
        @SerializedName("user_states") var userStates: List<UserStateModel?>?,      //old api
        @SerializedName("event_types") var eventTypes: List<EventTypeModel?>?,      //old api
        @SerializedName("has_empty_profile") var hasEmptyProfile: Boolean?,     //new api
        @SerializedName("twins") var twins: List<OldUser?>?                     //new api
) : Serializable {
    override fun toString(): String {
        return "SignInModel(key=$key, token=$token, uid=$uid, hideContent=$hideContent, " +
            "date=$date, adEnable=$adEnable, versionState=$versionState, stateId=$stateId, " +
            "userStates=$userStates, eventTypes=$eventTypes, hasEmptyProfile=$hasEmptyProfile, twins=$twins)"
    }
}
