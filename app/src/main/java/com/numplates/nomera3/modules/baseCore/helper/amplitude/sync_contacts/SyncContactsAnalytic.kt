package com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

interface SyncContactsAnalytic {
    fun logSyncContactsStart(
        where: AmplitudeSyncContactsWhereProperty,
        userId: Long
    )

    fun logSyncContactsAction(
        actionType: AmplitudeSyncContactsActionTypeProperty,
        numberOfPopup: Int,
        where: AmplitudeSyncContactsWhereProperty,
        userId: Long
    )

    fun logSyncContactsToggleChanged(
        positionProperty: SyncContactsToggleProperty,
        userId: Long
    )

    fun logContactsSyncFinished(
        actionTypeProperty: SyncContactsSuccessActionTypeProperty,
        userId: Long, syncCount: Int
    )
}

class SyncContactsAnalyticImpl @Inject constructor(
    private val amplitudeEventDelegate: AmplitudeEventDelegate
) : SyncContactsAnalytic {

    override fun logSyncContactsStart(
        where: AmplitudeSyncContactsWhereProperty,
        userId: Long
    ) {
        amplitudeEventDelegate.logEvent(
            eventName = AmplitudeSyncContactsEventName.SYNC_CONTACTS_START,
            properties = {
                it.apply {
                    addProperty(where)
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                }
            }
        )
    }

    override fun logSyncContactsAction(
        actionType: AmplitudeSyncContactsActionTypeProperty,
        numberOfPopup: Int,
        where: AmplitudeSyncContactsWhereProperty,
        userId: Long
    ) {
        amplitudeEventDelegate.logEvent(
            eventName = AmplitudeSyncContactsEventName.SYNC_CONTACTS_ACTION,
            properties = {
                it.apply {
                    addProperty(actionType)
                    addProperty(SyncContactsConst.NUMBER_OF_POPUP, numberOfPopup)
                    addProperty(where)
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                }
            }
        )
    }

    override fun logSyncContactsToggleChanged(
        positionProperty: SyncContactsToggleProperty,
        userId: Long
    ) {
        amplitudeEventDelegate.logEvent(
            eventName = AmplitudeSyncContactsEventName.SYNC_CONTACTS_TOGGLE,
            properties = {
                it.apply {
                    addProperty(positionProperty)
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                }
            }
        )
    }

    override fun logContactsSyncFinished(
        actionTypeProperty: SyncContactsSuccessActionTypeProperty,
        userId: Long,
        syncCount: Int
    ) {
        amplitudeEventDelegate.logEvent(
            eventName = AmplitudeSyncContactsEventName.CONTACTS_SYNC_FINISH,
            properties = {
                it.apply {
                    addProperty(actionTypeProperty)
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                    addProperty(AmplitudePropertyNameConst.SYNC_COUNT, syncCount)
                }
            }
        )
    }
}
