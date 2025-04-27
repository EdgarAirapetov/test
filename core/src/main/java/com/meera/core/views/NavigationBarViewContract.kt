package com.meera.core.views

interface NavigationBarViewContract {

    fun showUnreadEventsCounter(needToShow: Boolean)

    fun updateUnreadBadge(needToShowBadge: Boolean)

    fun updateChatCounter(chatCounter: Int, callCounter: Long)

    fun updateProfileIndicator(needToShow: Boolean)

    fun updatePeopleBadge(needToShow: Boolean)

    fun updateNavBarBasedOnHoliday()

    fun setListener(listener: NavigatonBarListener)

    interface NavigatonBarListener {
        fun onClickChat()
        fun onClickMessages(roomId: Long?) {}
        fun onClickRoad(isLaunchedAutomatically: Boolean = false)
        fun onClickMap()
        fun onClickProfile()
        fun onClickEvent() {}
        fun onClickPeoples(userId: Long? = null)
    }
}
