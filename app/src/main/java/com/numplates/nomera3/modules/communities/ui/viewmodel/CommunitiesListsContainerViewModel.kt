package com.numplates.nomera3.modules.communities.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleSectionChangeProperty
import javax.inject.Inject

class CommunitiesListsContainerViewModel @Inject constructor(
    private val amplitudePeopleAnalytics: AmplitudePeopleAnalytics
) : ViewModel() {

    fun logPeopleSection() {
        amplitudePeopleAnalytics.setSectionChanged(AmplitudePeopleSectionChangeProperty.PEOPLE)
    }
}
