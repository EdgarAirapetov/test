package com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst

enum class AmplitudeSyncContactsEventName(
    private val event: String
) : AmplitudeName {
    SYNC_CONTACTS_START("sync contacts start"),
    SYNC_CONTACTS_ACTION("sync contacts action"),
    SYNC_CONTACTS_TOGGLE("sync contacts toggle"),
    CONTACTS_SYNC_FINISH("contacts sync finish");

    override val eventName: String
        get() = event
}

enum class AmplitudeSyncContactsActionTypeProperty(
    private val property: String
) : AmplitudeProperty {

    ALLOW("allow"),
    GO_TO_SETTINGS("go to settings"),
    CLOSE("close");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.ACTION_TYPE
}

enum class SyncContactsSuccessActionTypeProperty(
    private val property: String
) : AmplitudeProperty {

    GREAT("great"),
    CLOSE("close");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.ACTION_TYPE
}

enum class AmplitudeSyncContactsWhereProperty(
    private val property: String
) : AmplitudeProperty {
    PEOPLE("people"),
    SUGGEST("suggest"),
    MAIN_FEED("main feed"),
    SEARCH("search");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE
}

enum class SyncContactsToggleProperty(
    private val property: String
) : AmplitudeProperty {

    ON("on"),
    OFF("off");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.POSITION

    companion object {
        @JvmStatic
        fun valueOf(enabled: Boolean): SyncContactsToggleProperty {
            return if (enabled) ON else OFF
        }
    }
}

object SyncContactsConst {
    const val NUMBER_OF_POPUP = "number pop up"
    const val SYNC_COUNT = "sync count"

    // Отмечаем, какой это поп ап
    const val ALLOW_OR_LATTER = 1 // 1 - Разрешить/Позже
    const val GO_TO_SETTINGS_OR_LATTER = 2 // 2- Перейти в настройки/Позже
}
