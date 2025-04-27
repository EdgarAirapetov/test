package com.numplates.nomera3.presentation.view.holder

import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * Базовый класс для ViewHolder
 * T - Тип Ui entity для каждого holder.
 * Данный базовый holder решает проблему, когда необходимо обратиться к определенной ui entity за пределы
 * bind метода. Пример: нужно реализовать [android.view.View.setOnClickListener] в init блоке
 */
abstract class BaseItemViewHolder<T, in B : ViewBinding>(
    binding: B
) : RecyclerView.ViewHolder(binding.root) {

    private var _item: T? = null
    protected val item: T?
        get() = _item

    @CallSuper
    open fun bind(item: T) {
        this._item = item
    }
}
