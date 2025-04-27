package com.numplates.nomera3.modules.newroads

import android.os.Parcelable
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment

class MeeraRoadsViewPagerAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm) {

    val roadsListFragments = mutableListOf<MeeraBaseFragment>()
    val roadsListTitles = mutableListOf<String>()

    fun addFragment(list: MutableList<MeeraBaseFragment>) {
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

    override fun getPageTitle(position: Int): CharSequence {
        return roadsListTitles[position]
    }

    override fun saveState(): Parcelable? {
        return null
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
    }

    fun clearResources() {
        roadsListFragments.clear()
        roadsListTitles.clear()
        notifyDataSetChanged()
    }
}
