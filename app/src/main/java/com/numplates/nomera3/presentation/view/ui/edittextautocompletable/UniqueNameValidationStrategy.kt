package com.numplates.nomera3.presentation.view.ui.edittextautocompletable

sealed class UniqueNameValidationStrategy : TagValidationStrategy {

    companion object {
        private val validLengthGroupChat = 1..26
        private val validLengthAddPost = 2..26
        private val validLengthAddComment = 2..26
        private val validLengthForHighlight = 4..26
    }

    private val _regexContainer by lazy { EditTextAutoCompletableRegex() }

    protected fun isMatchGeneralRule(tag: String): Boolean {
        return !tag.contains("..")
                && tag.filter { it == '@' }.length == 1
                && tag.trim().startsWith('@')
                && tag.trim().matches(_regexContainer.uniqueName)
    }

    object GroupChat : UniqueNameValidationStrategy() {
        override fun validate(tag: String): Boolean = tag.length in validLengthGroupChat && isMatchGeneralRule(tag)
    }

    object AddPost : UniqueNameValidationStrategy() {
        override fun validate(tag: String): Boolean = tag.length in validLengthAddPost && isMatchGeneralRule(tag)
    }

    object AddComment : UniqueNameValidationStrategy() {
        override fun validate(tag: String): Boolean = tag.length in validLengthAddComment && isMatchGeneralRule(tag)
    }

    object isHighlightable : UniqueNameValidationStrategy() {
        override fun validate(tag: String): Boolean = tag.length in validLengthForHighlight && isMatchGeneralRule(tag)
    }
}
