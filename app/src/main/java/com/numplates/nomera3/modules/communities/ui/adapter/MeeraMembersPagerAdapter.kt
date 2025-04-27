package com.numplates.nomera3.modules.communities.ui.adapter

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment

class MeeraMembersPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragments = mutableListOf<MeeraBaseDialogFragment>()
    private val titles = mutableListOf<String>()

    fun setFragments(list: List<MeeraBaseDialogFragment>) {
        fragments.clear()
        fragments.addAll(list)
        notifyDataSetChanged()
    }

    fun setTitles(list: List<String>) {
        titles.clear()
        titles.addAll(list)
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun saveState(): Parcelable? {
        return null
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}
