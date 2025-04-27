package com.numplates.nomera3.modules.peoples.ui.onboarding.adapter

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.numplates.nomera3.modules.peoples.ui.onboarding.MeeraPeopleOnboardingSecondStepFragment


class MeeraPeopleOnboardingPagerAdapter(
    fragment: Fragment,
    private val supportUserId: Long
) : FragmentStateAdapter(fragment) {

    private val currentFragmentsList = mutableListOf<Fragment>()

    override fun getItemCount(): Int = currentFragmentsList.size

    override fun createFragment(position: Int): Fragment {
        val currentFragment = currentFragmentsList[position]
        if (currentFragment is MeeraPeopleOnboardingSecondStepFragment) {
            currentFragment.arguments = bundleOf(
                MeeraPeopleOnboardingSecondStepFragment.BUNDLE_KEY_SUPPORT_USER_ID to supportUserId
            )
        }
        return currentFragment
    }

    fun addPeopleOnboardingFragments(fragments: List<Fragment>) {
        currentFragmentsList.clear()
        currentFragmentsList.addAll(fragments)
        notifyDataSetChanged()
    }

    fun getItemByPosition(position: Int) = try {
        currentFragmentsList[position]
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
