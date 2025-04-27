package com.meera.core.adapters.baserecycleradapter

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding


/**
 * На случай если кто-то засунет не дата класс
 * */
interface RecyclerData<I, T> {
    fun getItemId(): I
    fun contentTheSame(newItem: T): Boolean
    fun itemViewType(): Int
}

abstract class BaseAsyncAdapter<I, T : RecyclerData<I, T>>(
    diffUtil: DiffUtil.ItemCallback<T> = BaseDiffUtil()
) : ListAdapter<T, BaseVH<T, *>>(AsyncDifferConfig.Builder(diffUtil).build()) {

    abstract fun getHolderType(viewType: Int, parent: ViewGroup): BaseVH<T, *>

    override fun getItemViewType(position: Int): Int {
        return currentList[position].itemViewType()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseVH<T, *> {
        return getHolderType(viewType, parent)
    }

    override fun onBindViewHolder(holder: BaseVH<T, *>, position: Int) {
        onBindViewHolder(holder, position, mutableListOf())
    }

    override fun onBindViewHolder(holder: BaseVH<T, *>, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            holder.bind(currentList[position])
        } else {
            holder.update(payloads[0] as Bundle)
        }
    }
}

abstract class BaseVH<T, B : ViewBinding>(
    binding: B
) : ViewHolder(binding.root) {
    abstract fun bind(data: T)
    open fun update(bundle: Bundle) {}
}

open class BaseDiffUtil<I, T : RecyclerData<I, T>> : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.getItemId() == newItem.getItemId()
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem.contentTheSame(newItem)
    }
}

inline fun <reified V : ViewBinding> ViewGroup.toBinding(): V {
    return V::class.java.getMethod(
        "inflate",
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.java
    ).invoke(null, LayoutInflater.from(context), this, false) as V
}
