package com.numplates.nomera3.modules.baseCore.helper.amplitude.people

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

interface AmplitudePeopleAnalytics {
    fun setPeopleSelected(
        where: AmplitudePeopleWhereProperty,
        which: AmplitudePeopleWhich
    )
    fun setSectionChanged(section: AmplitudePeopleSectionChangeProperty)
    fun setContentCard(
        cardWhereAction: AmplitudePeopleContentCardProperty,
        haveVideo: Boolean,
        havePhoto: Boolean,
        postId: Long,
        userId: Long,
        authorId: Long
    )
}

class AmplitudePeopleAnalyticsImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudePeopleAnalytics {

    override fun setPeopleSelected(
        where: AmplitudePeopleWhereProperty,
        which: AmplitudePeopleWhich
    ) {
        delegate.logEvent(
            eventName = AmplitudePeopleName.TAB_BAR_PEOPLE,
            properties = {
                it.apply {
                    addProperty(where)
                    addProperty(which)
                }
            }
        )
    }

    override fun setSectionChanged(section: AmplitudePeopleSectionChangeProperty) {
        delegate.logEvent(
            eventName = AmplitudePeopleSectionChangeEventName.PEOPLE_SECTION_CHANGE,
            properties = {
                it.apply {
                    addProperty(section)
                }
            }
        )
    }

    override fun setContentCard(
        cardWhereAction: AmplitudePeopleContentCardProperty,
        haveVideo: Boolean,
        havePhoto: Boolean,
        postId: Long,
        userId: Long,
        authorId: Long
    ) {
        delegate.logEvent(
            eventName = AmplitudePeopleContentCardEventName.PEOPLE_CONTENT_CARD,
            properties = {
                it.apply {
                    addProperty(PeopleConstants.HAVE_VIDEO, haveVideo)
                    addProperty(PeopleConstants.HAVE_PHOTO, havePhoto)
                    addProperty(PeopleConstants.POST_ID, postId)
                    addProperty(PeopleConstants.USER_ID, userId)
                    addProperty(PeopleConstants.AUTHOR_ID, authorId)
                }
            }
        )
    }
}
