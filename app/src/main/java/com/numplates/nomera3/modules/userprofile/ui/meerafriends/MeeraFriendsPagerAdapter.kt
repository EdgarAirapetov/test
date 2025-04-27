package com.numplates.nomera3.modules.userprofile.ui.meerafriends

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MeeraFriendsPagerAdapter(fragmentActivity: FragmentActivity, private val pages:List<Fragment>) : FragmentStateAdapter(fragmentActivity) {


    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment {
        return pages[position]
    }
}
