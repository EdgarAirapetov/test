package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.meera.core.extensions.dp
import com.meera.core.extensions.setOnActionMoveListener
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.textColor
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.userprofile.ui.action.NestedRecyclerAction
import com.numplates.nomera3.modules.userprofile.ui.adapter.ProfileSuggestionsAdapter
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionsFloorUiEntity
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction

private const val HORIZONTAL_ITEM_PADDING_DP = 8

class ProfileSuggestionsFloorViewHolder(
    parent: ViewGroup,
    private val rvAction: NestedRecyclerAction,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit = { _ -> },
) : BaseUserViewHolder<ProfileSuggestionsFloorUiEntity>(parent, R.layout.item_profile_suggestions_floor) {

    private val tvRecommendations = itemView.findViewById<TextView>(R.id.tv_recommendations)
    private val tvShowMore = itemView.findViewById<TextView>(R.id.tv_show_more)
    private val rvProfileSuggestions = itemView.findViewById<RecyclerView>(R.id.rv_profile_suggestions)

    private var adapter: ProfileSuggestionsAdapter? = null

    init {
        adapter = ProfileSuggestionsAdapter(profileUIActionHandler)
        val layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
        rvProfileSuggestions.adapter = adapter
        rvProfileSuggestions.layoutManager = layoutManager
        rvProfileSuggestions.isNestedScrollingEnabled = false
        rvProfileSuggestions.addItemDecoration(object : ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) = with(outRect) {
                left = HORIZONTAL_ITEM_PADDING_DP.dp
            }
        })
        rvProfileSuggestions.setOnActionMoveListener {
            rvAction.onScroll(it)
        }
    }

    override fun bind(data: ProfileSuggestionsFloorUiEntity) {
        val isVip = data.userType == AccountTypeEnum.ACCOUNT_TYPE_VIP
        setupTheme(isVip)
        tvShowMore.setThrottledClickListener {
            profileUIActionHandler(UserProfileUIAction.OnShowMoreSuggestionsClicked)
        }
        adapter?.submitList(data.suggestions)
    }

    private fun setupTheme(isVip: Boolean) {
        if (isVip) {
            tvRecommendations.textColor(R.color.ui_white)
            tvShowMore.textColor(R.color.vip_gold)
        } else {
            tvRecommendations.textColor(R.color.ui_black)
            tvShowMore.textColor(R.color.ui_purple)
        }
    }
}
