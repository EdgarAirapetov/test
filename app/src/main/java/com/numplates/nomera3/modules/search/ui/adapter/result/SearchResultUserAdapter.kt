package com.numplates.nomera3.modules.search.ui.adapter.result

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.meera.core.extensions.clickAnimate
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideIfNullOrEmpty
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.core.utils.TopAuthorApprovedUserModel
import com.meera.core.utils.enableTopContentAuthorApprovedUser
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.numplates.nomera3.modules.search.ui.fragment.AT_SIGN
import com.numplates.nomera3.presentation.utils.setSmallSize
import com.numplates.nomera3.presentation.view.widgets.VipView
import com.numplates.nomera3.presentation.view.widgets.numberplateview.NumberPlateEditView

/**
 * Элемент "Пользователь"
 */
fun searchResultUserItemAdapterDelegate(
    selectCallback: (SearchItem.User) -> Unit,
    addCallback: (SearchItem.User) -> Unit,
    openMomentsCallback: (SearchItem.User, View?) -> Unit
) = adapterDelegate<SearchItem.User, SearchItem>(R.layout.search_result_user_item) {

    val addButton: ImageView = findViewById(R.id.add_button)
    val avatarView: VipView = findViewById(R.id.avatar_view)
    val nameText: TextView = findViewById(R.id.name_text)
    val tagText: TextView = findViewById(R.id.tag_text)
    val additionalInfo: TextView = findViewById(R.id.additional_info)
    val numberPlateView: NumberPlateEditView = findViewById(R.id.number_plate)

    addButton.setOnClickListener { button ->
        button.clickAnimate()
        addCallback(item)
    }

    itemView.setOnClickListener {
        selectCallback(item)
    }

    avatarView.setOnClickListener {
        when {
            item.hasMoments -> openMomentsCallback(item, avatarView)
            else -> selectCallback(item)
        }
    }

    fun renderVehicle(item: SearchItem.User) {
        val vehicle = item.vehicle

        if (vehicle?.number == null || vehicle.country == null) {
            numberPlateView.gone()
            return
        } else {
            numberPlateView.visible()
            NumberPlateEditView.Builder(numberPlateView)
                .setVehicleNew(vehicle.number, vehicle.country?.countryId, vehicle.type?.typeId)
                .build()

            numberPlateView.setBackgroundPlate(
                vehicle.type?.typeId,
                vehicle.country?.countryId,
                item.accountType.value,
                item.accountColor
            )

            numberPlateView.setSmallSize(vehicle.type?.typeId ?: 1)
            numberPlateView.visible()
        }
    }

    fun renderButton(buttonState: SearchItem.User.ButtonState) {
        when (buttonState) {
            SearchItem.User.ButtonState.Hide -> {
                addButton.gone()
            }
            SearchItem.User.ButtonState.ShowAdd -> {
                addButton.setImageResource(R.drawable.ic_add_friend_purple_32)
                addButton.visible()
            }
            SearchItem.User.ButtonState.ShowIncome -> {
                addButton.setImageResource(R.drawable.ic_incoming_friend_purple)
                addButton.visible()
            }
        }
    }

    fun setApprovedIcon(item: SearchItem.User) {
        nameText.enableTopContentAuthorApprovedUser(
            params = TopAuthorApprovedUserModel(
                approved = item.approved.toBoolean(),
                interestingAuthor = item.topContentMaker.toBoolean(),
                isVip = item.accountType != AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
                customIconTopContent = R.drawable.ic_approved_author_gold_10
            )
        )
    }

    fun renderAvatar(searchItem: SearchItem.User) {
        avatarView.setUp(
            context = context,
            avatarLink = searchItem.avatarImage,
            accountType = searchItem.accountType.value,
            frameColor = searchItem.accountColor,
            hasMoments = searchItem.hasMoments,
            hasNewMoments = searchItem.hasNewMoments
        )
    }

    bind { payloads ->
        if (payloads.isNotEmpty()) {
            val user =  payloads.first() as? SearchItem.User ?: return@bind
            renderAvatar(user)
            return@bind
        }

        renderVehicle(item)
        renderButton(item.buttonState)

        renderAvatar(item)

        nameText.text = item.name
        setApprovedIcon(item)
        tagText.hideIfNullOrEmpty("$AT_SIGN${item.tagName}")
        additionalInfo.hideIfNullOrEmpty(if (item.isMyProfile) getString(R.string.search_my_profile)
        else item.additionalInfo)
    }
}
