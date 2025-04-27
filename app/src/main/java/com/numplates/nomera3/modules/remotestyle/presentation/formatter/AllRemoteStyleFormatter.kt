package com.numplates.nomera3.modules.remotestyle.presentation.formatter

import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.remotestyle.data.posts.PostOnlyTextRemoteStyle

/**
 * Главная утилита для форматирования View по правилам бэка
 *
 * Как использовать:
 * В метод format передать view (обычно RecyclerView.ViewHolder) и если для него пришли правила форматирования с бэка,
 * то эти правила применяются
 */
class AllRemoteStyleFormatter(private val settings: Settings?) {
    fun format(view: Any) {
        if (settings != null) {
            settings.postRules?.postOnlyTextTextStyle?.getFormatter()?.apply(view)
        }
    }

    fun formatDefault(view: Any) {
        if (settings != null) {
            settings.postRules?.postOnlyTextTextStyle?.getFormatter()?.applyDefault(view)
        }
    }

    private fun PostOnlyTextRemoteStyle.getFormatter(): PostOnlyTextFormatter {
        return PostOnlyTextFormatter(rules, styles)
    }
}
