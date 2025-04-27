package com.numplates.nomera3.modules.userprofile.profilestatistics.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ProfileStatisticsAdapter(
    containerFragment: Fragment
) : FragmentStateAdapter(containerFragment) {

    private val items = mutableListOf<Fragment>()

    override fun getItemCount(): Int {
        return items.size
    }

    override fun createFragment(position: Int): Fragment {
        return items[position]
    }

    fun setFragments(fragments: List<Fragment>) {
        items.clear()
        items += fragments
        notifyDataSetChanged()
    }
}
