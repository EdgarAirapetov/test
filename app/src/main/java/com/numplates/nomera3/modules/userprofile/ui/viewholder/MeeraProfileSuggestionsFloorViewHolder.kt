package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setVisible
import com.meera.core.extensions.visible
import com.numplates.nomera3.databinding.MeeraItemProfileSuggestionsFloorBinding
import com.numplates.nomera3.modules.userprofile.ui.adapter.MeeraProfileSuggestionsAdapter
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionUiModels
import com.numplates.nomera3.modules.userprofile.ui.fragment.MeeraSuggestionShimmerAdapter
import com.numplates.nomera3.modules.userprofile.ui.fragment.UserInfoRecyclerData
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction

private const val HORIZONTAL_ITEM_PADDING_DP = 8
private const val COUNT_SHIMMER_ITEM = 3

class MeeraProfileSuggestionsFloorViewHolder(
    private val binding: MeeraItemProfileSuggestionsFloorBinding,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit = { _ -> },
) : BaseVH<UserInfoRecyclerData, MeeraItemProfileSuggestionsFloorBinding>(binding) {

    private var adapter: MeeraProfileSuggestionsAdapter? = null
    private val shimmerAdapter = MeeraSuggestionShimmerAdapter()
    private val listShimmer = List(COUNT_SHIMMER_ITEM) { "" }

    init {
        binding.rvProfileSuggestionsShimmer.adapter = shimmerAdapter
        binding.rvProfileSuggestionsShimmer.layoutManager =
            LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        shimmerAdapter.submitList(listShimmer)

        adapter = MeeraProfileSuggestionsAdapter(profileUIActionHandler)
        val layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvProfileSuggestions.adapter = adapter
        binding.rvProfileSuggestions.layoutManager = layoutManager
        binding.rvProfileSuggestions.isNestedScrollingEnabled = false
        binding.rvProfileSuggestions.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) = with(outRect) {
                left = HORIZONTAL_ITEM_PADDING_DP.dp
            }
        })
    }


    override fun bind(data: UserInfoRecyclerData) {
        data as UserInfoRecyclerData.ProfileSuggestionFloor

        if (data.suggestions.isEmpty()){
            showShimmer(true)
        } else {
            showShimmer(false)
        }

        binding.btnShowMore.setThrottledClickListener {
            profileUIActionHandler(UserProfileUIAction.OnShowMoreSuggestionsClicked)
        }
        adapter?.submitList(data.suggestions)
        binding.rvProfileSuggestionsShimmer.gone()
        binding.rvProfileSuggestions.visible()
    }

    fun updateList(newList: List<ProfileSuggestionUiModels>) {
        adapter?.submitList(newList)
    }

    private fun showShimmer(visible: Boolean){
        binding.rvProfileSuggestionsShimmer.setVisible(visible)
        binding.rvProfileSuggestions.setVisible(visible)
    }
}
