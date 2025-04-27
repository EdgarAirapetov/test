package com.numplates.nomera3.presentation.view.ui.edittextautocompletable

sealed class HashtagValidationStrategy : TagValidationStrategy {

    companion object {
        private val validLength = 2..100
    }

    private val _regexContainer by lazy { EditTextAutoCompletableRegex() }

    protected fun isMatchGeneralRule(tag: String): Boolean {
        return !tag.contains("..")
                && !tag.endsWith(".")
                && tag.filter { it == '#' }.length == 1
                && tag.trim().startsWith('#')
                && tag.matches(_regexContainer.hashtag)
    }

    object IsHighlightable : HashtagValidationStrategy() {
        override fun validate(tag: String): Boolean = tag.length in validLength && isMatchGeneralRule(tag)
    }
}
