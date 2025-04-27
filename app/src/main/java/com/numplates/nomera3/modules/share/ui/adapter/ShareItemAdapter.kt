package com.numplates.nomera3.modules.share.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.modules.share.ui.ShareItemsCallback
import com.numplates.nomera3.modules.share.ui.entity.UIShareItem
import com.numplates.nomera3.modules.share.ui.holder.ShareItemHolder

class ShareItemAdapter(
    private val shareItemCallback: ShareItemsCallback
): RecyclerView.Adapter<ShareItemHolder>() {

    private val asyncDiffer =
        AsyncListDiffer(this,
            object : DiffUtil.ItemCallback<UIShareItem>() {
                override fun areItemsTheSame(
                    oldItem: UIShareItem,
                    newItem: UIShareItem
                ): Boolean = oldItem == newItem

                override fun areContentsTheSame(
                    oldItem: UIShareItem,
                    newItem: UIShareItem
                ): Boolean = oldItem == newItem
            })

    fun submitList(cardsData: List<UIShareItem>) = asyncDiffer.submitList(cardsData)

    fun submitList(cardsData: List<UIShareItem>, callback: () -> Unit) = asyncDiffer.submitList(cardsData, callback)

    fun currentItems(): List<UIShareItem> = asyncDiffer.currentList

    fun addListener(listener: AsyncListDiffer.ListListener<UIShareItem>) {
        asyncDiffer.addListListener(listener)
    }

    fun removeListener(listener: AsyncListDiffer.ListListener<UIShareItem>) {
        asyncDiffer.removeListListener(listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ShareItemHolder(parent, shareItemCallback)

    override fun onBindViewHolder(holder: ShareItemHolder, position: Int) {
        holder.bind(asyncDiffer.currentList[position])
    }

    override fun getItemCount() = asyncDiffer.currentList.size

    override fun getItemId(position: Int): Long {
        return asyncDiffer.currentList[position].id.filter { it.isDigit() }.toLong()
    }
}
