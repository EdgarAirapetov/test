package com.numplates.nomera3.modules.reactionStatistics.ui

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.numplates.nomera3.modules.reactionStatistics.ui.entity.ReactionTabUiEntity

class ReactionsPagerAdapter(
    fragment: Fragment, private val entityId: Long, private val entityType: ReactionsEntityType
) : FragmentStateAdapter(fragment) {

    private val reactions = mutableListOf<ReactionTabUiEntity>()

    override fun getItemCount(): Int = reactions.size

    override fun createFragment(position: Int): Fragment {

        return ReactionsPageFragment.getInstance(
            entityId = entityId,
            entityType = entityType,
            reaction = if (reactions[position].reactions.size > 1) "all" else reactions[position].reactions.firstOrNull(),
            viewsPage = reactions[position].isViewersTab
        )
    }

    fun submitItems(items: List<ReactionTabUiEntity>) {
        reactions.clear()
        reactions.addAll(items)

        notifyDataSetChanged()
    }

    fun getItem(position: Int): ReactionTabUiEntity {
        return reactions[position]
    }
}
