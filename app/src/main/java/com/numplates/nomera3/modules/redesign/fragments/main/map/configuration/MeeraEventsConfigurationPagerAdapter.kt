package com.numplates.nomera3.modules.redesign.fragments.main.map.configuration

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationEvent
import com.numplates.nomera3.modules.maps.ui.events.model.EventDateItemUiModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventTypeItemUiModel
import java.time.LocalTime

class MeeraEventsConfigurationPagerAdapter(
    fragment: Fragment,
    private val onEvent: ((EventConfigurationEvent) -> Unit)?
) : FragmentStateAdapter(fragment) {

    private val currentFragmentsList = mutableListOf<Fragment>()
    private val tabsName = mutableListOf<String>()

    override fun getItemCount(): Int = currentFragmentsList.size

    override fun createFragment(position: Int): Fragment {
        val currentFragment = currentFragmentsList[position]
        when(currentFragment) {
            is MeeraEventConfigurationTypeFragment -> {
                currentFragment.setCallback(onEvent)
            }
            is MeeraConfigurationDateFragment -> {
                currentFragment.setCallback(onEvent)
            }
            is MeeraConfigurationTimeFragment -> {

            }
        }
        return currentFragment
    }

    fun addPeopleOnboardingFragments(fragments: List<Fragment>, tabNames: List<String>) {
        currentFragmentsList.clear()
        currentFragmentsList.addAll(fragments)
        tabsName.clear()
        tabsName.addAll(tabNames)
        notifyDataSetChanged()
    }

    fun getName(position: Int): String = tabsName.getOrNull(position).orEmpty()

    fun getItemByPosition(position: Int) = try {
        currentFragmentsList[position]
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    fun submitTypeItems(eventTypeItems: List<EventTypeItemUiModel>) {
        currentFragmentsList.forEach {
            if(it is MeeraEventConfigurationTypeFragment) {
                it.submitList(eventTypeItems)
                return@forEach
            }
        }
    }

    fun submitDateItems(eventTypeItems: List<EventDateItemUiModel>) {
        currentFragmentsList.forEach {
            if(it is MeeraConfigurationDateFragment) {
                it.submitList(eventTypeItems)
                return@forEach
            }
        }
    }

    fun setTimeConfiguretion(uiModel: LocalTime, param: ((LocalTime) -> Unit)?) {
        currentFragmentsList.forEach {
            if(it is MeeraConfigurationTimeFragment) {
                it.setTimeConfig(uiModel, param)
                return@forEach
            }
        }
    }

}
