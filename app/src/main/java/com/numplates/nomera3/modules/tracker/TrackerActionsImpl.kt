package com.numplates.nomera3.modules.tracker

import com.numplates.nomera3.BuildConfig
import timber.log.Timber

class TrackerActionsImpl : ITrackerActions {

    override fun trackWriteComments() = trackEvent(MyTrackerEvents.WRITE_COMMENT.state)


    override fun trackBip() = trackEvent(MyTrackerEvents.BIP.state)

    override fun trackRepostChat() = trackEvent(MyTrackerEvents.REPOST_CHAT.state)


    override fun trackRepostRoad() = trackEvent(MyTrackerEvents.REPOST_ROAD.state)


    override fun trackRepostGroup() = trackEvent(MyTrackerEvents.REPOST_GROUP.state)


    override fun trackGift() = trackEvent(MyTrackerEvents.GIFT.state)


    override fun trackSubscribe() = trackEvent(MyTrackerEvents.SUBSCRIBE.state)


    override fun trackToFriendRequest() = trackEvent(MyTrackerEvents.TO_FRIEND_REQUEST.state)


    override fun trackFriendAccept() = trackEvent(MyTrackerEvents.TO_FRIEND_ACCEPT.state)


    override fun trackNewPost() = trackEvent(MyTrackerEvents.CREATE_POST.state)


    override fun trackNewTextPost() = trackEvent(MyTrackerEvents.CREATE_TEXT_POST.state)


    override fun trackNewImagePost() = trackEvent(MyTrackerEvents.CREATE_IMAGE_POST.state)


    override fun trackNewVideoPost() = trackEvent(MyTrackerEvents.CREATE_VIDEO_POST.state)


    override fun trackOwnRoadPost() = trackEvent(MyTrackerEvents.CREATE_PERSONAL_POST.state)


    private fun trackEvent(eventName: String) {
        Timber.d("Fake track (Not Implemented) eventName = $eventName")
        try {
            if (!BuildConfig.DEBUG) {
                //Временно отключаем логирование событий
                //https://nomera.atlassian.net/browse/BR-7936
                //MyTracker.trackEvent(eventName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
