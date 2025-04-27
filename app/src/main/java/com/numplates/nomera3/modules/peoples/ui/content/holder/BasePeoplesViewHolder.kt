package com.numplates.nomera3.modules.peoples.ui.content.holder

import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeoplesContentUiEntity

/**
 * Базовый класс для ViewHolder, которые должны создаваться в пределах
 * [com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentAdapter]
 * T - Тип Ui entity для каждого holder.
 * Данный базовый holder решает проблему, когда необходимо обратиться к определенной ui entity за пределы
 * bind метода. Пример: нужно реализовать [android.view.View.setOnClickListener] в init блоке
 */
abstract class BasePeoplesViewHolder<T : PeoplesContentUiEntity, in B : ViewBinding>(
    binding: B
) : RecyclerView.ViewHolder(binding.root) {

    private var _item: T? = null
    protected val item: T?
        get() = _item

    /**
     * Необходимо переопределить данный bind метод и передать в него определенную entity, который должен реализовать
     * interface PeoplesContentUiEntity
     */
    @CallSuper
    open fun bind(item: T) {
        this._item = item
    }

    open fun onViewDetached() = Unit
}
