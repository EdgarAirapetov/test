package com.numplates.nomera3.modules.baseCore.helper.amplitude.moment

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst

enum class AmplitudeMomentEventName(
    private val event: String,
) : AmplitudeName {
    TAP_CREATE("moment create tap"),
    CREATE("moment created"),
    SCREEN_OPEN("moment screen open"),
    SCREEN_CLOSE("moment screen close"),
    STOP("moment stop"),
    FLIP("moment flip"),
    MENU_ACTION("moment menu action"),
    DELETE("moment del"),
    END("moments end");

    override val eventName: String
        get() = event
}

/**
 * Отмечаем каким путём пользователь намеревается добавить момент:
 * my card - нажал плюсик "Создать" на карточке
 * button after scroll - нажал на кнопку, которая появляется, когда проскролливаем карусель моментов вправо
 * */
enum class AmplitudePropertyMomentEntryPoint(val property: String) : AmplitudeProperty {
    MY_CARD("my card"),
    BUTTON_AFTER_SCROLL("button after scroll");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyMomentEventsConst.ENTRY_POINT
}

/**
 * Отмечаем, чей момент
 * my moment - мой момент
 * user moment - чужой момент
 * unknown - нельзя определить
 * */
enum class AmplitudePropertyMomentWhose(val property: String) : AmplitudeProperty {
    MY_MOMENT("my moment"),
    USER_MOMENT("user moment"),
    UNKNOWN("unknown");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHOSE
}

/**
 * Отмечаем, с какого экрана производилось открытие момента.
 * main feed block- блок с прямоугольными моментами в главной дороге
 * main feed avatar - обводки на аватарах в ленте в главной дороге
 * follow feed block- блок с прямоугольными моментами в подписочной дороге
 * follow feed avatar - обводки на аватарах в ленте в подписочной дороге
 * self feed -  личная дорога
 * profile avatar - в моем профиле миниатюра аватара с моментами в правом нижнем углу основного аватара
 * profile feed - дорога в моем профиле
 * user profile avatar- в чужом профиле миниатюра аватара с моментами в правом нижнем углу основного аватара
 * user profile feed - дорога в чужом профиле
 * search - поиск
 * people rec - рекомендации в разделе Люди
 * group chat participants - участники группового чата
 * chat list - список чатов
 * tet-a-tet chat - аватар собеседника в шапке тет-а-тет чата
 * notifications - Уведомления на вкладке Общение
 * other - другое
 * */
enum class AmplitudePropertyMomentScreenOpenWhere(val property: String) : AmplitudeProperty {
    MAIN_FEED_BLOCK("main feed block"),
    MAIN_FEED_AVATAR("main feed avatar"),
    FOLLOW_FEED_BLOCK("follow feed block"),
    FOLLOW_FEED_AVATAR("follow feed avatar"),
    SELF_FEED("self feed"),
    PROFILE_AVATAR("profile avatar"),
    PROFILE_FEED("profile feed"),
    USER_PROFILE_AVATAR("user profile avatar"),
    USER_PROFILE_FEED("user profile feed"),
    SEARCH("search"),
    PEOPLE_REC("people rec"),
    GROUP_CHAT_PARTICIPANTS("group chat participants"),
    CHAT_LIST("chat list"),
    TET_A_TET_CHAT("tet-a-tet chat"),
    NOTIFICATIONS("notifications"),
    DEEPLINK("deeplink"),
    OTHER("other");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE
}

/**
 * Отмечаем, просмотренный ли это ранее момент
 * Если нет возможности узнать, просмотренный ли это ранее момент, присылать unknown
 * */
enum class AmplitudePropertyMomentIsViewEarlier(val property: String) : AmplitudeProperty {
    UNKNOWN(AmplitudePropertyMomentEventsConst.UNKNOWN_VALUE),
    TRUE(AmplitudePropertyMomentEventsConst.TRUE_VALUE),
    FALSE(AmplitudePropertyMomentEventsConst.FALSE_VALUE);

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyMomentEventsConst.VIEW_EARLIER
}

/**
 * Как пользователь закрыл экран просмотра момента
 * close button - кнопка "Закрыть"
 * swipe - свайпом вниз
 * */
enum class AmplitudePropertyMomentHowScreenClosed(val property: String) : AmplitudeProperty {
    CLOSE_BUTTON("close button"),
    SWIPE("swipe");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.HOW
}

/**
 * Отмечаем, как был перелистнут момент
 * next swipe - свайп к следующему момента
 * back swipe - свайп к предыдущему моменту
 * next tap - тап по экрану к следующему моменту
 * back tap - тап по экрану к предыдущему моменту
 * auto flip - время показа одного момента истекло и он автоматически переключается на следующий
 * */
enum class AmplitudePropertyMomentHowFlipped(val property: String) : AmplitudeProperty {
    NEXT_SWIPE("next swipe"),
    NEXT_TAP("next tap"),
    BACK_TAP("back tap"),
    AUTO_FLIP("auto flip"),
    BACK_SWIPE("back swipe");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.HOW
}

/**
 * Отмечаем, какое действие совершил пользователь
 * moment settings - тап на "Настройки момента"
 * save moments to gallery - тап на "Сохранить на устройстве"
 * allow comments - тап на "Разрешать комментарии"
 * moment delete - тап на "Удалить момент" (после подтверждения)
 * cancel - тап на кнопку "Отмена"
 * user moment hide - тап на кнопку "Скрыть моменты пользователя"(после подтверждения)
 * report moment - тап на "Пожаловаться на момент"
 * share - Поделиться
 * copy link - Скопировать ссылку
 * */
enum class AmplitudePropertyMomentMenuActionType(val property: String) : AmplitudeProperty {
    MOMENT_SETTINGS("moment settings"),
    SAVE_MOMENTS_TO_GALLERY("save moments to gallery"),
    ALLOW_COMMENTS("allow comments"),
    MOMENT_DELETE("moment delete"),
    CANCEL("cancel"),
    USER_MOMENT_HIDE("user moment hide"),
    REPORT_MOMENT("report moment"),
    SHARE("share"),
    COPY_LINK("copy link");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyMomentEventsConst.ACTION_TYPE
}

object AmplitudePropertyMomentEventsConst {
    const val ENTRY_POINT = "entry point"
    const val DURATION = "duration"
    const val MOMENT_COUNT = "moment count"
    const val MOMENT_NUMBER = "moment number"
    const val VIEW_EARLIER = "view earlier"
    const val UNKNOWN_VALUE = "unknown"
    const val TRUE_VALUE = "true"
    const val FALSE_VALUE = "false"
    const val ACTION_TYPE = "action type"
    const val AUTHOR_ID = "author id"
}
