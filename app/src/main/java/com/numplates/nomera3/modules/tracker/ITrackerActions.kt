package com.numplates.nomera3.modules.tracker

/**
https://nomera.atlassian.net/browse/BR-4408
 */
interface ITrackerActions {
    fun trackWriteComments()

    fun trackBip()

    fun trackRepostChat()

    fun trackRepostRoad()

    fun trackRepostGroup()

    fun trackGift()

    fun trackSubscribe()

    fun trackToFriendRequest()

    fun trackFriendAccept()

    fun trackNewPost()

    fun trackNewTextPost()

    fun trackNewImagePost()

    fun trackNewVideoPost()

    fun trackOwnRoadPost()
}