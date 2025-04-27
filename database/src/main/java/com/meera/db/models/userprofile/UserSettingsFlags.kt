package com.meera.db.models.userprofile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class UserSettingsFlags(

    // я включил тоггл для звонков пользователю
    @SerializedName("i_can_call")
    val iCanCall: Int? = 0,

    // пользователь разрешил нам звонить ему
    @SerializedName("is_in_call_blacklist")
    var isInCallBlacklist: Int? = 0,

    // мы добавили пользователя в белый лист звонков
    @SerializedName("is_in_call_whitelist")
    var isInCallWhitelist: Int? = 0,

    // мы добавили пользователя в блэклист звонков
    @SerializedName("user_can_call_me")
    val userCanCallMe: Int? = 0,

    //Отключаем уведомления от пользователя
    @SerializedName("notifications_off")
    var notificationsOff: Int? = 0,

    // статус подписки на пользователя
    @SerializedName("subscription_on")
    var subscription_on: Int? = 0,

    @SerializedName("subscribed_to_me")
    var subscribedToMe: Int? = 0,

    //
    @SerializedName("subscription_notify")
    var subscription_notify: Int? = 0,

    //скрыли ли мы дорогу юзера
    @SerializedName("hide_road_posts")
    var hideRoadPosts: Int? = 0,

    @SerializedName("friend_status")
    val friendStatus: Int? = 0,

    @SerializedName("i_can_chat")
    val iCanChat: Int? = 0,

    @SerializedName("user_can_chat_me")
    val userCanChatMe: Int? = 0,

    @SerializedName("is_in_chat_blacklist")
    val isInChatBlackList: Int? = 0,

    @SerializedName("is_in_chat_whitelist")
    val isInChatWhiteList: Int? = 0,

    @SerializedName("i_can_greet")
    var iCanGreet: Int? = 0,

    @SerializedName("i_can_comment_moments")
    var iCanCommentMoments: Int = 0
) : Serializable, Parcelable

