package com.numplates.nomera3.presentation.view.widgets

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.meera.core.extensions.clearText
import com.numplates.nomera3.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class SearchBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private var debounceJob: Job? = null
    private var textChangesFlow: Flow<Editable?>? = null
    private var scope: CoroutineScope = MainScope()
    private var searchEditText: EditText? = null

    init {
        inflate()
        initInputField()
        initClearInputButton()
    }

    fun clearText() {
        searchEditText?.clearText()
    }

    fun getTextChangesFlow(): Flow<Editable?> {
        return textChangesFlow ?: MutableSharedFlow<Editable?>().apply {
            searchEditText?.addTextChangedListener { input ->
                debounceJob?.cancel()
                debounceJob = scope.launch {
                    delay(TEXT_CHANGES_DEBOUNCE_TIME_MILLIS)
                    emit(input)
                }
            }
        }.also { textChangesFlow = it }
    }

    private fun inflate() = ViewGroup.inflate(context, R.layout.search_bar_view, this)

    private fun initInputField() {
        searchEditText = findViewById(R.id.et_search_input)
    }

    private fun initClearInputButton() {
        findViewById<ImageView>(R.id.iv_clear_input).apply {
            setOnClickListener { clearText() }
            searchEditText?.addTextChangedListener { input ->
                this.isVisible = input?.isNotEmpty() == true
            }
        }
    }

    companion object {
        private const val TEXT_CHANGES_DEBOUNCE_TIME_MILLIS = 500L
    }
}
