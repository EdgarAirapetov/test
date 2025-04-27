package com.numplates.nomera3.modules.music.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.AudioEventListener
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.music.ui.entity.event.UserActionEvent
import com.numplates.nomera3.modules.music.ui.entity.state.MusicViewModelState
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import javax.inject.Inject

class MeeraMusicViewModel @Inject constructor() : BaseViewModel() {

    @Inject
    lateinit var audioFeedHelper: AudioFeedHelper

    @Inject
    lateinit var analytics: AnalyticsInteractor

    private val _liveState = MutableLiveData<MusicViewModelState>()
    val liveState = _liveState as LiveData<MusicViewModelState>

    init {
        App.component.inject(this)
    }

    // Audio part
    fun handleUIAction(
        action: UserActionEvent,
        audioEventListener: AudioEventListener? = null,
        adapterPosition: Int? = null
    ) {
        when (action) {
            is UserActionEvent.AddClicked -> onAddClicked(action)

            UserActionEvent.ClearClicked -> onClearClicked()

            UserActionEvent.CloseClicked -> onCloseClicked()

            is UserActionEvent.PlayClicked ->
                if (audioEventListener != null && adapterPosition != null) onPlayClicked(
                    action,
                    audioEventListener,
                    adapterPosition
                )

            is UserActionEvent.StopClicked -> onStopClicked()

            UserActionEvent.UnSubscribe -> onUnSubscribe()

            is UserActionEvent.MoveMusicCell -> onMoveMusicCell()
        }
    }

    private fun onPlayClicked(
        action: UserActionEvent.PlayClicked,
        audioEventListener: AudioEventListener,
        adapterPosition: Int
    ) {
        action.entity.apply {
            audioFeedHelper.startPlaying(
                idPost,
                mediaEntity.trackPreviewUrl ?: "",
                audioEventListener,
                adapterPosition,
                action.musicView
            )
        }
    }

    private fun onMoveMusicCell() {
        audioFeedHelper.onScrolled()
    }

    private fun onUnSubscribe() {
        audioFeedHelper.stopPlaying(isLifecycleStop = true, isReset = true)
    }

    private fun onCloseClicked() = Unit
    private fun onClearClicked() = Unit

    private fun onAddClicked(action: UserActionEvent.AddClicked) {
        _liveState.postValue(MusicViewModelState.AddMusic(action.entity))
    }

    private fun onStopClicked() {
        audioFeedHelper.stopPlaying(needToLog = true, isStoppedFromPost = false)
    }

    fun logAddMusic() {
        analytics.logMusicAddPress()
    }
}
