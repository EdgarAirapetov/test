package com.numplates.nomera3.modules.chat.helpers.resendmessage

import com.meera.db.models.message.MessageEntity

interface IResendResultCallback {

    fun onProgressResend(message: MessageEntity)

    fun onSuccessResend(message: MessageEntity)

    fun onFailResend(message: MessageEntity)

    /**
     * Не удалось отправить сообщение т.к. отсутствует файл
     * Необходимо поставить плейсхолдер на сообщение
     */
    fun onSetMediaPlaceholder(message: MessageEntity, notExistPaths: List<String?>)

    /**
     * Пометить сообщение как "Недоступное для отправки"
     */
    fun onDisableResend(message: MessageEntity)

}
