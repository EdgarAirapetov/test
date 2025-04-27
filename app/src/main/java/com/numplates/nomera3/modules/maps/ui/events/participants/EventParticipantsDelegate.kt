package com.numplates.nomera3.modules.maps.ui.events.participants

import androidx.viewbinding.ViewBinding
import com.numplates.nomera3.Act
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereOpenMap
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.ui.events.navigation.EventNavigationBottomsheetDialogFragment
import com.numplates.nomera3.modules.maps.ui.events.navigation.model.EventNavigationInitUiModel
import com.numplates.nomera3.modules.maps.ui.events.participants.list.EventParticipantsListFragment
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.view.fragments.MapFragment

fun <T : ViewBinding> BaseFragmentNew<T>.openEventOnMap(post: PostUIEntity) {
    add(
        MapFragment(),
        Act.LIGHT_STATUSBAR,
        Arg(MapFragment.ARG_EVENT_POST, post),
        Arg(MapFragment.ARG_LOG_MAP_OPEN_WHERE, AmplitudePropertyWhereOpenMap.MAP_EVENT)
    )
}

fun <T : ViewBinding> BaseFragmentNew<T>.openEventParticipantsList(post: PostUIEntity) {
    post.event ?: return
    add(
        fragment = EventParticipantsListFragment(),
        isLightStatusBar = Act.LIGHT_STATUSBAR,
        args = arrayOf(
            Arg(EventParticipantsListFragment.ARG_EVENT_ID, post.event.id),
            Arg(EventParticipantsListFragment.ARG_POST_ID, post.postId),
            Arg(EventParticipantsListFragment.ARG_PARTICIPANTS_COUNT, post.event.participation.participantsCount)
        )
    )
}

fun <T : ViewBinding> BaseFragmentNew<T>.openEventNavigation(post: PostUIEntity) {
    val initUiModel = EventNavigationInitUiModel(
        event = post.event ?: return,
        authorId = post.user?.userId ?: return
    )
    EventNavigationBottomsheetDialogFragment.getInstance(initUiModel)
        .show(childFragmentManager, EventNavigationBottomsheetDialogFragment::class.java.name)
}
