package com.numplates.nomera3.modules.redesign.fragments.main.map.participant

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.setBackgroundTintColor
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.groupusersrow.GroupUsersRowViewConfig
import com.meera.uikit.widgets.groupusersrow.UsersRowIconSize
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraViewEventParticipantsBinding
import com.numplates.nomera3.modules.maps.ui.events.participants.view.model.EventParticipantsUiAction
import com.numplates.nomera3.modules.maps.ui.events.participants.view.model.EventParticipantsUiModel

private const val CLICK_DELAY = 2000L
private const val PLUS = "+"
private const val PADDING_BUTTON_FILLED_TYPE = 3

class MeeraEventParticipantsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private var onAction: ((EventParticipantsUiAction) -> Unit)? = null

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.meera_view_event_participants, this, false)
        .apply(::addView)
        .let(MeeraViewEventParticipantsBinding::bind)

    private var uiModel: EventParticipantsUiModel? = null
    private var mapBgColor: Int? = null

    fun setActionListener(onAction: ((EventParticipantsUiAction) -> Unit)) {
        this.onAction = onAction
    }

    fun setModel(uiModel: EventParticipantsUiModel) {
        if (this.uiModel == uiModel) return
        this.uiModel = uiModel
        bindParticipants(uiModel)
        bindParticipation(uiModel)
        bindMap(uiModel)
        setupActions(uiModel)
        enableBtnEventParticipants(enable = uiModel.isFinished.not())
    }

    fun clearResources() {
        with(binding) {
            onAction = null
            btnEventParticipantsParticipation.setOnClickListener(null)
            ukgurEventParticipantsUsers.setOnClickListener(null)
            ivEventParticipantsMap.setOnClickListener(null)
            cpHostAvatarEvent.setConfig(UserpicUiModel())
            ivEventParticipantsMap.background = null
        }
    }

    fun enableBtnEventParticipants(enable: Boolean = true) {
        binding.btnEventParticipantsParticipation.isEnabled = enable
    }

    private fun bindParticipants(uiModel: EventParticipantsUiModel) {
        binding.ukgurEventParticipantsUsers.setConfig(
            GroupUsersRowViewConfig(
                isLegacy = false,
                isVip = false,
                iconSize = UsersRowIconSize.SIZE_32,
                count = uiModel.participation.participantsCount,
                iconUrls = uiModel.participantsAvatars.filterNotNull()
            )
        )
        uiModel.hostAvatar?.let {
            binding.cpHostAvatarEvent.setConfig(
                UserpicUiModel(userAvatarUrl = it)
            )
        } ?: binding.cpHostAvatarEvent.gone()
    }

    private fun bindParticipation(uiModel: EventParticipantsUiModel) {
        val participation = uiModel.participation
        val canJoin = participation.isHost.not() && participation.isParticipant.not()
        val participationStringRes = participationButtonText(uiModel, canJoin)
        binding.btnEventParticipantsParticipation.text = context.getString(participationStringRes)
        post { requestLayout() }
    }

    private fun participationButtonText(uiModel: EventParticipantsUiModel, canJoin: Boolean): Int {
        val participation = uiModel.participation
        return when {
            participation.isHost -> {
                hasHostEvent(
                    uiModel = uiModel,
                    countVisible = participation.newParticipants > 0
                )
                R.string.map_events_participants_new
            }

            canJoin -> {
                hasHostEvent(uiModel, false)
                binding.btnEventParticipantsParticipation.buttonType = ButtonType.OUTLINE
                R.string.map_events_participants_join
            }

            else -> {
                hasHostEvent(uiModel, false)
                binding.btnEventParticipantsParticipation.buttonType = ButtonType.FILLED
                R.string.map_events_participants_is_participant
            }
        }
    }

    private fun hasHostEvent(uiModel: EventParticipantsUiModel, countVisible: Boolean) {
        val newParticipantsCount = uiModel.participation.newParticipants
        if (countVisible) {
            if (newParticipantsCount > 0) {
                binding.btnEventParticipantsParticipation.buttonType = ButtonType.FILLED
                binding.btnEventParticipantsParticipation.setCustomPadding(
                    left = PADDING_BUTTON_FILLED_TYPE.dp,
                    right = PADDING_BUTTON_FILLED_TYPE.dp,
                    top = 0.dp,
                    bottom = 0.dp
                )
                binding.btnEventParticipantsParticipation.setCounter(PLUS + newParticipantsCount)
            } else {
                binding.btnEventParticipantsParticipation.buttonType = ButtonType.OUTLINE
                binding.btnEventParticipantsParticipation.setCounter(newParticipantsCount.toString())
            }
        } else {
            binding.btnEventParticipantsParticipation.setCounter(String.empty())
        }
    }

    private fun bindMap(uiModel: EventParticipantsUiModel) {
        if (uiModel.showMap) {
            binding.ivEventParticipantsMap.visible()
            if (uiModel.participation.isParticipant && uiModel.participation.isHost.not()) {
//                binding.ivEventParticipantsMap.imageTintList =
//                    ColorStateList.valueOf(getPrimaryContentColor(uiModel.isVip))
                binding.ivEventParticipantsMap.setBackgroundTintColor(context.getColor(getPrimaryBgColor(uiModel.isVip)))
            } else {
//                val hasNew = uiModel.participation.newParticipants == 0
//                binding.ivEventParticipantsMap.imageTintList =
//                    ColorStateList.valueOf(
//                        getSecondaryContentMapColor(
//                            uiModel.isVip,
//                            uiModel.isFinished,
//                            uiModel.participation.isParticipant,
//                            hasNew
//                        )
//                    )
                mapBgColor?.let { context.getColor(it) }?.let {
                    binding.ivEventParticipantsMap.setBackgroundTintColor(
                        it
                    )
                }
            }
        } else {
            binding.ivEventParticipantsMap.gone()
        }
    }

    private fun setupActions(uiModel: EventParticipantsUiModel) {
        val isHost = uiModel.participation.isHost
        val canJoin = isHost.not() && uiModel.participation.isParticipant.not()
        val action = when {
            canJoin -> EventParticipantsUiAction.JoinEvent
            isHost -> EventParticipantsUiAction.ShowEventParticipants
            else -> EventParticipantsUiAction.LeaveEvent
        }
        if (action != null) {
            binding.btnEventParticipantsParticipation.setThrottledClickListener(CLICK_DELAY) {
                onAction?.invoke(action)
            }
        } else {
            binding.btnEventParticipantsParticipation.setOnClickListener(null)
        }
        binding.ukgurEventParticipantsUsers.setThrottledClickListener {
            onAction?.invoke(EventParticipantsUiAction.ShowEventParticipants)
        }
        binding.ivEventParticipantsMap.setThrottledClickListener {
            onAction?.invoke(EventParticipantsUiAction.ShowEventOnMap)
        }
        binding.cpHostAvatarEvent.setThrottledClickListener {
            onAction?.invoke(EventParticipantsUiAction.ShowEventCreator)
        }
    }

    private fun getPrimaryBgColor(isVip: Boolean) = if (isVip) R.color.vip_gold else R.color.uiKitColorAccentPrimary

}
