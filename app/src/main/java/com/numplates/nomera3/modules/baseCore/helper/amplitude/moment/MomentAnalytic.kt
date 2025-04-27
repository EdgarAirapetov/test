package com.numplates.nomera3.modules.baseCore.helper.amplitude.moment

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst
import javax.inject.Inject

interface AmplitudeMoment {
    fun onTapCreateMoment(entryPoint: AmplitudePropertyMomentEntryPoint)

    fun onCreateMoment(
        authorId: Long,
        momentDuration: Int,
        momentsCount: Int,
        momentNumber: Int,
        momentId: Long,
    )

    fun onMomentScreenOpen(
        whoseMomentOf: AmplitudePropertyMomentWhose,
        whereOpenFrom: AmplitudePropertyMomentScreenOpenWhere,
        isViewEarlier: Boolean?,
    )

    fun onMomentScreenClose(howScreenClosed: AmplitudePropertyMomentHowScreenClosed)

    fun onMomentStop(momentId: Long)

    fun onMomentFlip(howMomentFlipped: AmplitudePropertyMomentHowFlipped)

    fun onMomentMenuAction(
        whoseMomentOf: AmplitudePropertyMomentWhose,
        actionType: AmplitudePropertyMomentMenuActionType,
        userIdActionFrom: Long,
        momentAuthorId: Long,
    )

    fun onMomentDelete(momentId: Long)

    fun onMomentsEnd(momentsCount: Int, userIdFrom: Long)

}

class AmplitudeHelperMomentImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate,
) : AmplitudeMoment {

    override fun onTapCreateMoment(
        entryPoint: AmplitudePropertyMomentEntryPoint,
    ) {
        delegate.logEvent(
            eventName = AmplitudeMomentEventName.TAP_CREATE,
            properties = {
                it.apply {
                    addProperty(entryPoint)
                }
            }
        )
    }

    override fun onCreateMoment(
        authorId: Long,
        momentDuration: Int,
        momentsCount: Int,
        momentNumber: Int,
        momentId: Long,
    ) {
        delegate.logEvent(
            eventName = AmplitudeMomentEventName.CREATE,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.FROM, authorId)
                    addProperty(AmplitudePropertyMomentEventsConst.DURATION, authorId)
                    addProperty(AmplitudePropertyMomentEventsConst.MOMENT_COUNT, momentsCount)
                    addProperty(AmplitudePropertyMomentEventsConst.MOMENT_NUMBER, momentsCount)
                    addProperty(AmplitudePropertyNameConst.MOMENT_ID, momentsCount)
                }
            }
        )
    }

    override fun onMomentScreenOpen(
        whoseMomentOf: AmplitudePropertyMomentWhose,
        whereOpenFrom: AmplitudePropertyMomentScreenOpenWhere,
        isViewEarlier: Boolean?,
    ) {
        val viewedEarlier = isViewEarlier?.let {
            if (it) {
                AmplitudePropertyMomentIsViewEarlier.TRUE
            } else {
                AmplitudePropertyMomentIsViewEarlier.FALSE
            }
        } ?: AmplitudePropertyMomentIsViewEarlier.UNKNOWN

        delegate.logEvent(
            eventName = AmplitudeMomentEventName.SCREEN_OPEN,
            properties = {
                it.apply {
                    addProperty(whoseMomentOf)
                    addProperty(whereOpenFrom)
                    addProperty(viewedEarlier)
                }
            }
        )
    }

    override fun onMomentScreenClose(howScreenClosed: AmplitudePropertyMomentHowScreenClosed) {
        delegate.logEvent(
            eventName = AmplitudeMomentEventName.SCREEN_CLOSE,
            properties = {
                it.apply {
                    addProperty(howScreenClosed)
                }
            }
        )
    }

    override fun onMomentStop(momentId: Long) {
        delegate.logEvent(
            eventName = AmplitudeMomentEventName.STOP,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.MOMENT_ID, momentId)
                }
            }
        )
    }

    override fun onMomentFlip(howMomentFlipped: AmplitudePropertyMomentHowFlipped) {
        delegate.logEvent(
            eventName = AmplitudeMomentEventName.FLIP,
            properties = {
                it.apply {
                    addProperty(howMomentFlipped)
                }
            }
        )
    }

    override fun onMomentMenuAction(
        whoseMomentOf: AmplitudePropertyMomentWhose,
        actionType: AmplitudePropertyMomentMenuActionType,
        userIdActionFrom: Long,
        momentAuthorId: Long,
    ) {
        delegate.logEvent(
            eventName = AmplitudeMomentEventName.MENU_ACTION,
            properties = {
                it.apply {
                    addProperty(whoseMomentOf)
                    addProperty(actionType)
                    addProperty(AmplitudePropertyNameConst.FROM, userIdActionFrom)
                    addProperty(AmplitudePropertyMomentEventsConst.AUTHOR_ID, momentAuthorId)
                }
            }
        )
    }

    override fun onMomentDelete(momentId: Long) {
        delegate.logEvent(
            eventName = AmplitudeMomentEventName.DELETE,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.MOMENT_ID, momentId)
                }
            }
        )
    }

    override fun onMomentsEnd(momentsCount: Int, userIdFrom: Long) {
        delegate.logEvent(
            eventName = AmplitudeMomentEventName.DELETE,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyMomentEventsConst.MOMENT_COUNT, momentsCount)
                    addProperty(AmplitudePropertyNameConst.FROM, userIdFrom)
                }
            }
        )
    }

}

