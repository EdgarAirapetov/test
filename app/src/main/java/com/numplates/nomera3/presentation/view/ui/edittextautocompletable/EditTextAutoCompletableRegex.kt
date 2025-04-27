package com.numplates.nomera3.presentation.view.ui.edittextautocompletable

class EditTextAutoCompletableRegex {

    /**
     * Regex для проверки хэштега
     * */
    val hashtag: Regex
        get() = "(\\#[а-яА-Яa-zA-Z_0-9\\ud83c\\udf00-\\ud83d\\ude4f\\ud83d\\ude80-\\ud83d\\udeff]*)(?!\\S)".toRegex()

    /**
     * Regex для поиска слов разделенных пробелами
     * */
    val allWordsDividedBySpace: Regex
        get() = "\\S*[a-zA-Z@а-яА-ЯёЁ]+\\S*".toRegex()

    /**
     * Регулярка для проверки уникального имени
     * https://nomeraworkspace.slack.com/files/UTWAAJW1E/F01PK105J9M/image.png
     * */
    val uniqueName: Regex
        get() = Regex("^[a-zA-Z0-9._@]+$", RegexOption.IGNORE_CASE)
}
