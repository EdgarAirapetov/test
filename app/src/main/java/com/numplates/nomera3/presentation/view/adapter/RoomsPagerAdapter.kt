package com.numplates.nomera3.presentation.view.adapter

import android.os.Parcelable
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


class RoomsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val roomsFragmentsList = mutableListOf<Fragment>()
    private val roomsTitleList = mutableListOf<String>()

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
    }

    fun addFragments(list: List<Fragment>) {
        roomsFragmentsList.clear()
        roomsFragmentsList.addAll(list)
        notifyDataSetChanged()
    }

    fun addFragmentByIndex(index: Int, fragment: Fragment?) {
        if (count == index && fragment != null) {
            roomsFragmentsList.add(index, fragment)
            notifyDataSetChanged()
        }
    }

    fun removeFragmentByIndex(index: Int) {
        if (count != index) {
            roomsFragmentsList.removeAt(index)
            notifyDataSetChanged()
        }
    }

    fun addTitleOfFragment(title: String) {
        roomsTitleList.add(title)
    }

    override fun saveState(): Parcelable? {
        return null
    }

    override fun getItem(position: Int): Fragment {
        return roomsFragmentsList[position]
    }

    override fun getCount(): Int {
        return roomsFragmentsList.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return roomsTitleList[position]
    }
}
