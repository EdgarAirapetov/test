package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.fcm.IPushInfo
import com.numplates.nomera3.data.fcm.IPushInfo.FRIEND_CONFIRM
import com.numplates.nomera3.data.fcm.IPushInfo.FRIEND_REQUEST
import com.numplates.nomera3.data.fcm.IPushInfo.GIFT_RECEIVED_NOTIFICATION
import com.numplates.nomera3.data.fcm.IPushInfo.GROUP_COMMENT
import com.numplates.nomera3.data.fcm.IPushInfo.POST_COMMENT
import com.numplates.nomera3.data.fcm.IPushInfo.POST_COMMENT_REPLY
import com.numplates.nomera3.presentation.model.IAbstractAdapterItem
import com.numplates.nomera3.presentation.model.IAbstractAdapterItem.ITEM_DATA
import java.io.Serializable



data class NotificationResponse(@SerializedName("id") var id: Long,
                                @SerializedName("read") var read: Boolean,
                                @SerializedName("date") var date: Long,
                                @SerializedName("user") var user: UserModel?,
                                @SerializedName("type") var type: String,
                                @SerializedName("meta") var meta: NotificationMeta?
) : Serializable, IAbstractAdapterItem, IPushInfo {
    override fun getItemType(): Int {
        return ITEM_DATA
    }

    fun getStringType(): Int {
        return when(type){
            POST_COMMENT -> com.numplates.nomera3.R.string.notification_post_comment
            GROUP_COMMENT -> com.numplates.nomera3.R.string.notification_group_comment
            POST_COMMENT_REPLY -> com.numplates.nomera3.R.string.notification_post_comment_reply
            FRIEND_REQUEST -> com.numplates.nomera3.R.string.notification_friend_request
            FRIEND_CONFIRM -> com.numplates.nomera3.R.string.notification_friend_received
            GIFT_RECEIVED_NOTIFICATION -> com.numplates.nomera3.R.string.notification_gift_received
            else -> com.numplates.nomera3.R.string.general_unknown
        }
    }

    fun hasComment(): Boolean{
        return when(type){
            POST_COMMENT -> true
            GROUP_COMMENT -> true
            else -> false
        }
    }

    fun hasGift(): Boolean{
        return when(type){
            GIFT_RECEIVED_NOTIFICATION -> true
            else -> false
        }
    }
}
