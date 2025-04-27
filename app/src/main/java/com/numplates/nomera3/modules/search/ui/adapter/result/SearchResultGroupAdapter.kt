package com.numplates.nomera3.modules.search.ui.adapter.result

import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.meera.core.extensions.clickAnimate
import com.meera.core.extensions.gone
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setVisible
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.buttons.UiKitButton
import com.meera.uikit.widgets.userpic.UiKitUserpicImage
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.search.ui.entity.SearchItem

/**
 * Элемент "Группа"
 */
fun searchResultGroupItemAdapterDelegate(
    selectCallback: (SearchItem.Group) -> Unit,
    addGroup: (SearchItem.Group) -> Unit,
) = adapterDelegate<SearchItem.Group, SearchItem>(R.layout.search_result_group_item) {

    val groupImage: ImageView = findViewById(R.id.group_image)
    val nameText: TextView = findViewById(R.id.name_text)
    val participantCount: TextView = findViewById(R.id.participant_count)
    val lockIconView: ImageView = findViewById(R.id.lock_icon_view)
    val addGroupButton: ImageView = findViewById(R.id.add_to_group_image)

    itemView.setOnClickListener {
        selectCallback(item)
    }

    addGroupButton.setOnClickListener { button ->
        button.clickAnimate()
        addGroup(item)
    }

    bind {
        Glide.with(itemView.context)
            .load(item.image)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(R.drawable.ic_group_avatar_new)
            .into(groupImage)

        nameText.text = item.name
        participantCount.text = itemView.context
            .pluralString(R.plurals.group_members_plural, item.participantCount)
        lockIconView.setVisible(item.isClosedGroup)

        when (item.buttonState) {
            SearchItem.Group.ButtonState.Show -> {
                addGroupButton.visible()
            }
            SearchItem.Group.ButtonState.Hide -> {
                addGroupButton.gone()
            }
        }
    }
}

fun meeraSearchGroupShimmerAdapterDelegate() =
    adapterDelegate<SearchItem.GroupShimmer, SearchItem>(R.layout.meera_item_search_group_shimmer) {}

fun meeraSearchResultGroupItemAdapterDelegate(
    selectCallback: (SearchItem.Group) -> Unit,
    addGroup: (SearchItem.Group) -> Unit,
) = adapterDelegate<SearchItem.Group, SearchItem>(R.layout.meera_item_search_result_group) {

    val upiGroup: UiKitUserpicImage = findViewById(R.id.upi_search_result_group)
    val ivPlaceholder: ImageView = findViewById(R.id.iv_community_placeholder_image)
    val tvName: TextView = findViewById(R.id.tv_search_result_group_name)
    val tvCount: TextView = findViewById(R.id.tv_search_result_group_count)
    val btnAdd: UiKitButton = findViewById(R.id.btn_search_result_group_add)

    itemView.setThrottledClickListener { selectCallback(item) }

    btnAdd.setThrottledClickListener { addGroup(item) }

    bind {
        if (item.image.isNullOrBlank()) {
            upiGroup.gone()
            ivPlaceholder.visible()
        } else {
            upiGroup.visible()
            ivPlaceholder.gone()
            upiGroup.setConfig(UserpicUiModel(userAvatarUrl = item.image))
        }

        tvName.text = item.name
        tvCount.text = itemView.context
            .pluralString(R.plurals.group_members_plural, item.participantCount)
        if (item.isClosedGroup) {
            tvCount.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_outlined_lock_m,
                0,
                0,
                0
            )
        } else {
            tvCount.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
        }
        when (item.buttonState) {
            SearchItem.Group.ButtonState.Show -> {
                btnAdd.visible()
            }
            SearchItem.Group.ButtonState.Hide -> {
                btnAdd.gone()
            }
        }
    }
}
