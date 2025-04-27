package com.numplates.nomera3.modules.maps.ui.events.participants.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.meera.core.extensions.gone
import com.meera.core.extensions.setBackgroundTintColor
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.groupusersrow.GroupUsersRowViewConfig
import com.meera.uikit.widgets.groupusersrow.UsersRowIconSize
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewEventParticipantsBinding
import com.numplates.nomera3.modules.maps.ui.events.participants.view.model.EventParticipantsUiAction
import com.numplates.nomera3.modules.maps.ui.events.participants.view.model.EventParticipantsUiModel

private const val CLICK_DELAY = 2000L

class EventParticipantsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private var onAction: ((EventParticipantsUiAction) -> Unit)? = null

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_event_participants, this, false)
        .apply(::addView)
        .let(ViewEventParticipantsBinding::bind)

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
        post { calculateOffsets(uiModel) }
    }

    private fun calculateOffsets(uiModel: EventParticipantsUiModel) {
        if (uiModel.participation.isHost.not()) return
        val fullWidth = binding.vEventParticipantsParticipationBackground.width
        binding.tvEventParticipantsParticipationText.setMargins(start = (fullWidth * MARGIN_SCALING_FACTOR_TEXT).toInt())
        binding.vEventParticipantsParticipationDivider.setMargins(start = (fullWidth * MARGIN_SCALING_FACTOR_DIVIDER).toInt())
        binding.tvEventParticipantsParticipationCount.setMargins(start = (fullWidth * MARGIN_SCALING_FACTOR_COUNT).toInt())
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
    }

    private fun bindParticipation(uiModel: EventParticipantsUiModel) {
        val participation = uiModel.participation
        val isOwnEventWithParticipants = participation.isHost && participation.participantsCount > 0
        val isOwnEventWithOutParticipants = participation.isHost && participation.newParticipants == 0
        val canJoin = participation.isHost.not() && participation.isParticipant.not()
        val contentColor = if (isOwnEventWithOutParticipants && !uiModel.isVip) {
            context.getColor(R.color.ui_purple)
        } else if ((isOwnEventWithParticipants || canJoin)) {
            getPrimaryContentColor(uiModel.isVip)
        } else {
            getSecondaryContentColor(
                uiModel.isVip,
                uiModel.isFinished,
                uiModel.participation.isParticipant,
                isOwnEventWithOutParticipants
            )
        }

        var inverseBgColor: Int = 0
        val backgroundColor = if (isOwnEventWithOutParticipants && !uiModel.isVip) {
            inverseBgColor = R.color.light_purple
            context.getColor(inverseBgColor)
        } else if ((isOwnEventWithParticipants || canJoin)) {
            inverseBgColor = getPrimaryBgColor(uiModel.isVip)
            context.getColor(inverseBgColor)
        } else {
            getSecondaryBgColor(
                uiModel.isVip,
                uiModel.isFinished,
                uiModel.participation.isParticipant,
            )
        }
        mapBgColor = inverseBgMap(inverseBgColor)
        val dividerColor = context.getColor(
            when {
                participation.isHost && participation.newParticipants == 0 -> R.color.map_event_participation_divider_dark
                uiModel.isVip -> R.color.map_event_participation_divider_vip
                isOwnEventWithParticipants || canJoin -> R.color.map_event_participation_divider_light
                else -> R.color.map_event_participation_divider_dark
            }
        )
        binding.ivEventParticipantsParticipationImage.imageTintList = ColorStateList.valueOf(contentColor)
        binding.tvEventParticipantsParticipationText.setTextColor(contentColor)
        binding.vEventParticipantsParticipationBackground.setBackgroundTintColor(backgroundColor)
        binding.vEventParticipantsParticipationBackground.alpha =
            if (uiModel.isFinished) 0.5f else 1f
        val participationStringRes = when {
            participation.isHost && (uiModel.showMap || uiModel.isCompact) -> R.string.map_events_participants_new
            participation.isHost -> R.string.map_events_participants_new_participants
            canJoin -> R.string.map_events_participants_join
            else -> R.string.map_events_participants_is_participant
        }
        binding.tvEventParticipantsParticipationText.setText(participationStringRes)
        binding.vEventParticipantsParticipationDivider.isVisible = participation.isHost
        binding.tvEventParticipantsParticipationCount.isVisible = participation.isHost
        binding.tvEventParticipantsParticipationCount.setTextColor(contentColor)
        binding.vEventParticipantsParticipationDivider.backgroundTintList = ColorStateList.valueOf(dividerColor)
        if (participation.isHost) {
            binding.tvEventParticipantsParticipationCount.text = participation.newParticipants.toString()
        } else {
            binding.tvEventParticipantsParticipationCount.text = participation.participantsCount.toString()
        }

    }

    private fun bindMap(uiModel: EventParticipantsUiModel) {
        if (uiModel.showMap) {
            binding.ivEventParticipantsMap.visible()
            if (uiModel.participation.isParticipant && uiModel.participation.isHost.not()) {
                binding.ivEventParticipantsMap.imageTintList =
                    ColorStateList.valueOf(getPrimaryContentColor(uiModel.isVip))
                binding.ivEventParticipantsMap.setBackgroundTintColor(context.getColor(getPrimaryBgColor(uiModel.isVip)))
            } else {
                val hasNew = uiModel.participation.newParticipants == 0
                binding.ivEventParticipantsMap.imageTintList =
                    ColorStateList.valueOf(
                        getSecondaryContentMapColor(
                            uiModel.isVip,
                            uiModel.isFinished,
                            uiModel.participation.isParticipant,
                            hasNew
                        )
                    )
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
            canJoin && uiModel.isFinished -> null
            canJoin -> EventParticipantsUiAction.JoinEvent
            isHost -> EventParticipantsUiAction.ShowEventParticipants
            else -> EventParticipantsUiAction.LeaveEvent
        }
        if (action != null) {
            binding.vEventParticipantsParticipationBackground.setThrottledClickListener(CLICK_DELAY) {
                onAction?.invoke(action)
            }
        } else {
            binding.vEventParticipantsParticipationBackground.setOnClickListener(null)
        }
        binding.ukgurEventParticipantsUsers.setThrottledClickListener {
            onAction?.invoke(EventParticipantsUiAction.ShowEventParticipants)
        }
        binding.ivEventParticipantsMap.setThrottledClickListener {
            onAction?.invoke(EventParticipantsUiAction.ShowEventOnMap)
        }
    }

    private fun getPrimaryContentColor(isVip: Boolean) = context.getColor(if (isVip) R.color.black else R.color.white)

    private fun getPrimaryBgColor(isVip: Boolean) = if (isVip) R.color.vip_gold else R.color.ui_purple

    private fun getSecondaryContentColor(
        isVip: Boolean,
        isFinished: Boolean,
        isParticipant: Boolean,
        hasNew: Boolean
    ): Int = if (isVip) {
        context.getColor(R.color.black)
    } else if (isFinished || !isParticipant || hasNew) {
        context.getColor(R.color.white)
    } else {
        context.getColor(R.color.ui_purple)
    }

    private fun getSecondaryContentMapColor(
        isVip: Boolean,
        isFinished: Boolean,
        isParticipant: Boolean,
        hasNew: Boolean
    ): Int = if (isVip) {
        context.getColor(R.color.black)
    } else if(!isParticipant){
        context.getColor(R.color.ui_purple)
    } else if (isFinished || !isParticipant || hasNew) {
        context.getColor(R.color.white)
    } else {
        context.getColor(R.color.ui_purple)
    }

    private fun getSecondaryBgColor(
        isVip: Boolean,
        isFinished: Boolean,
        isParticipant: Boolean,
    ) = if (isVip) {
        context.getColor(R.color.colorGoldHoliday)
    } else if (isFinished || !isParticipant) {
        context.getColor(R.color.ui_purple)
    } else {
        context.getColor(R.color.light_purple)
    }

    private fun inverseBgMap(backgroundColor: Int) = when (backgroundColor) {
        R.color.colorGoldHoliday -> R.color.vip_gold
        R.color.vip_gold -> R.color.colorGoldHoliday
        R.color.ui_purple -> R.color.light_purple
        R.color.light_purple -> R.color.ui_purple
        else -> R.color.ui_purple
    }

    companion object {
        private const val MARGIN_SCALING_FACTOR_TEXT = 0.02f
        private const val MARGIN_SCALING_FACTOR_DIVIDER = 0.08f
        private const val MARGIN_SCALING_FACTOR_COUNT = 0.11f
    }
}
