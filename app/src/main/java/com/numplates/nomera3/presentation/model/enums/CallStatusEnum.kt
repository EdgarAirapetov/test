package com.numplates.nomera3.presentation.model.enums

/**
 * calling - начался вызов
 * accepted - вызов состоялся, происходит в текущий момент
 * rejected - вызов отклонен пользователем, который получил звонок
 * declined - вызов отклонен инициатором звонка
 * missed - пропущен
 * stopped - звонок состоялся и завершен
 * */
enum class CallStatusEnum(val status: String) {
    CALLING("calling"),
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    DECLINED("declined"),
    MISSED("missed"),
    STOPPED("stopped");

    companion object {
        val map = entries.associateBy { it.status }
        operator fun get(value: String) = map[value]
    }
}
