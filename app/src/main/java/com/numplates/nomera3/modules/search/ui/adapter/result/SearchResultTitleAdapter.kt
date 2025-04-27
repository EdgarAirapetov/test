package com.numplates.nomera3.modules.search.ui.adapter.result

import android.widget.TextView
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.meera.core.extensions.visible
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.buttons.UiKitButton

/**
 * Элемент "Заголовок"
 */
fun searchResultTitleAdapterDelegate(buttonCallback: (() -> Unit)? = null) =
    adapterDelegate<SearchItem.Title, SearchItem>(
        R.layout.search_result_title_item
    ) {
        val nameText: TextView = findViewById(R.id.name_text)
        val clearButton: TextView = findViewById(R.id.clear_button)

        clearButton.setOnClickListener {
            buttonCallback?.invoke()
        }

        bind {
            nameText.text = context.getString(item.titleResource)

            if (item.buttonLabelResource != null) {
                clearButton.visible()
                clearButton.text = context.getString(item.buttonLabelResource!!)
            } else {
                clearButton.gone()
            }
        }
    }

fun meeraSearchResultTitleAdapterDelegate(buttonCallback: (() -> Unit)? = null) =
    adapterDelegate<SearchItem.Title, SearchItem>(
        R.layout.meera_item_search_result_title
    ) {
        val tvTitle: TextView = findViewById(R.id.tv_search_result_title)
        val btnClear: UiKitButton = findViewById(R.id.btn_search_result_clear)

        btnClear.setThrottledClickListener { buttonCallback?.invoke() }

        bind {
            tvTitle.text = context.getString(item.titleResource)

            if (item.buttonLabelResource != null) {
                btnClear.visible()
                btnClear.text = context.getString(item.buttonLabelResource!!)
            } else {
                btnClear.gone()
            }
        }
    }
