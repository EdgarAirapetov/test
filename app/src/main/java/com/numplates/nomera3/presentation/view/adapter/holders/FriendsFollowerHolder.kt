package com.numplates.nomera3.presentation.view.adapter.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.core.utils.getAge
import com.numplates.nomera3.R
import com.meera.db.models.userprofile.City
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.presentation.model.adaptermodel.FriendsFollowersUiModel
import com.numplates.nomera3.presentation.model.adaptermodel.SubscriptionType
import com.numplates.nomera3.presentation.view.adapter.SubscriberFriendActionCallback
import com.meera.core.utils.enableApprovedIcon
import com.numplates.nomera3.presentation.view.widgets.VipView

class FriendsFollowerHolder(
    itemView: View,
    private val actionCallback: SubscriberFriendActionCallback
) : RecyclerView.ViewHolder(itemView) {

    private val tvName: TextView = itemView.findViewById(R.id.tvName)
    private val vvImage: VipView = itemView.findViewById(R.id.vipView_friend_holder)
    private val tvAgeLoc: TextView = itemView.findViewById(R.id.tv_user_age_loc)
    private val uniqueNameTextView: TextView = itemView.findViewById(R.id.uniqueNameTextView)
    private val ivStatus: ImageView = itemView.findViewById(R.id.ivStatus)

    fun bind(model: FriendsFollowersUiModel) {
        setUserData(model)
        initListeners(model)
    }

    private fun setUserData(model: FriendsFollowersUiModel) {
        tvName.text = model.userSimple?.name
        initVerifiedAccount(model)
        vvImage.setUp(
            context = itemView.context,
            avatarLink = model.userSimple?.avatarSmall,
            accountType = model.userSimple?.accountType,
            frameColor = model.userSimple?.accountColor ?: 0
        )
        tvAgeLoc.visible()
        initBirthday(model.userSimple?.birthday)
        initCity(model.userSimple?.city)
        initUnitqueName(model.userSimple?.uniqueName)
        ivStatus.visible()
        setUserIconBySettings(model.subscriptionType)
    }

    private fun initUnitqueName(uniqueName: String?) {
        uniqueName?.let { uName: String ->
            if (uName.isNotEmpty()) {
                val formattedUniqueName = "@$uName"
                uniqueNameTextView.text = formattedUniqueName
                uniqueNameTextView.visible()
            } else {
                uniqueNameTextView.gone()
            }
        } ?: kotlin.run {
            uniqueNameTextView.gone()
        }
    }

    private fun initCity(city: City?) {
        city?.let { cityNotNull ->
            if (!cityNotNull.name.isNullOrEmpty()) {
                if (tvAgeLoc.text != "")
                    tvAgeLoc.text = String.format("${tvAgeLoc.text}, ${cityNotNull.name}")
                else tvAgeLoc.text = cityNotNull.name
            }
        }
    }

    private fun initBirthday(birthday: Long?) {
        birthday?.let { birthdayNotNull ->
            if (birthdayNotNull == -1L) tvAgeLoc.text = "" else
                tvAgeLoc.text = getAge(birthdayNotNull)
        } ?: run {
            tvAgeLoc.text = String.empty()
        }
    }

    private fun initListeners(model: FriendsFollowersUiModel) {
        itemView.setOnClickListener {
            actionCallback.onUserClicked(model)
        }
        ivStatus.setOnClickListener {
            actionCallback.onUserActionIconClicked(model)
        }
    }

    private fun setImageIcon(@DrawableRes iconRes: Int) {
        ivStatus.setImageResource(iconRes)
    }

    private fun setUserIconBySettings(subscriptionType: SubscriptionType) {
        when (subscriptionType) {
            SubscriptionType.TYPE_INCOMING_FRIEND_REQUEST ->
                setImageIcon(R.drawable.ic_friend_incoming_request)
            SubscriptionType.TYPE_FRIEND_NONE -> setImageIcon(R.drawable.ic_add_friend_purple)
            else -> ivStatus.gone()
        }
    }

    private fun initVerifiedAccount(model: FriendsFollowersUiModel) {
        tvName.enableApprovedIcon(
            enabled = model.userSimple?.approved.toBoolean(),
            isVip = model.userSimple?.accountType != INetworkValues.ACCOUNT_TYPE_REGULAR
        )
    }
}
