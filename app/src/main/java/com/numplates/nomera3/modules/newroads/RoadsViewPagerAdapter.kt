package com.numplates.nomera3.modules.newroads

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.meera.core.base.BaseFragment

class RoadsViewPagerAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    val roadsListFragments = mutableListOf<BaseFragment>()
    val roadsListTitles = mutableListOf<String>()

    fun addFragment(list: MutableList<BaseFragment>) {
        roadsListFragments.clear()
        roadsListFragments.addAll(list)
        notifyDataSetChanged()
    }

    fun addTitleOfFragment(title: String) {
        roadsListTitles.add(title)
    }


    override fun getItem(position: Int): Fragment {
        return roadsListFragments[position]
    }

    override fun getCount(): Int {
        return roadsListFragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return roadsListTitles[position]
    }
}
