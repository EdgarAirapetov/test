package com.numplates.nomera3.presentation.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.meera.core.base.BaseFragment

@Deprecated("Перенесено в модуль Search")
class SearchPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragments = mutableListOf<BaseFragment>()
    private val titles = mutableListOf<String>()

    fun addFragments(list: MutableList<BaseFragment>) {
        fragments.clear()
        fragments.addAll(list)
    }

    fun addTitles(list: MutableList<String>) {
        titles.clear()
        titles.addAll(list)
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}
