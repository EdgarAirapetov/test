package com.numplates.nomera3.modules.remotestyle.presentation.formatter

import com.numplates.nomera3.modules.remotestyle.data.posts.PostOnlyTextRemoteStyle

private const val BIG_FONT_MAX_LENGTH_RULE = "big_font_max_length"
private const val DEFAULT_STYLE = "default"

/**
 * Форматирование ViewHolder-поста в которых содержится только текст
 *
 * Форматтер умеет обрабатывать следующие правила:
 *
 * BIG_FONT_MAX_LENGTH_RULE – если есть это правило, то текстовый пост проверяется на длинну текста
 * и в зависимости от длинны к текстовому посту применяются стили которые прислал бэк
 */
class PostOnlyTextFormatter(
    private val rules: List<PostOnlyTextRemoteStyle.Rule>,
    private val style: List<PostOnlyTextRemoteStyle.Style>
) {
    fun apply(view: Any) {
        if (view is PostOnlyTextRemoteStyleView) {
            rules.forEach { rule ->
                applyRule(rule, view)
            }
        }
    }

    fun applyDefault(view: Any) {
        if (view is PostOnlyTextRemoteStyleView) {
            val style = getDefaultStyle() ?: return
            view.bindStyle(style)
        }
    }

    private fun applyRule(rule: PostOnlyTextRemoteStyle.Rule, holder: PostOnlyTextRemoteStyleView) {
        when (rule.type) {
            BIG_FONT_MAX_LENGTH_RULE -> {
                val isLengthOk = holder.getTextLength() < rule.length
                val canRuleApply = isLengthOk
                    && holder.canApplyOnlyTextStyle()

                val style = if (canRuleApply) {
                    // правило сработало, пытаться найти стиль указанный в правиле
                    getStyleByRule(rule) ?: getDefaultStyle() ?: return
                } else {
                    // правило не сработало, пытаться найти стиль по-умолчанию
                    getDefaultStyle() ?: return
                }

                holder.bindStyle(style)
            }
        }
    }

    private fun getDefaultStyle(): PostOnlyTextRemoteStyle.Style? {
        return style.find { style ->
            style.type == DEFAULT_STYLE
        }
    }

    private fun getStyleByRule(rule: PostOnlyTextRemoteStyle.Rule): PostOnlyTextRemoteStyle.Style? {
        return style.find { style ->
            style.type == rule.style
        }
    }

    interface PostOnlyTextRemoteStyleView {
        fun bindStyle(style: PostOnlyTextRemoteStyle.Style)
        fun canApplyOnlyTextStyle(): Boolean
        fun getTextLength(): Int
        fun getLinesCount(): Int
    }

    companion object {
        fun createNullable(
            rules: List<PostOnlyTextRemoteStyle.Rule>?,
            styles: List<PostOnlyTextRemoteStyle.Style>?
        ): PostOnlyTextFormatter? {
            val rulesNonNull = rules ?: return null
            val styleNonNull = styles ?: return null

            return PostOnlyTextFormatter(rulesNonNull, styleNonNull)
        }
    }
}
