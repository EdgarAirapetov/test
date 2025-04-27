package com.numplates.nomera3.modules.gifservice.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.gifservice.ui.entity.GiphyEntity
import com.meera.core.extensions.inflate
import com.meera.core.extensions.loadGlideProgressive


class GiphyListAdapter(
    private val longClickListener: (id: String, preview: String, url: String, ratio: Double) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val asyncDiffer = AsyncListDiffer(this,
            object : DiffUtil.ItemCallback<GiphyEntity>(){
        override fun areItemsTheSame(oldItem: GiphyEntity,
                                     newItem: GiphyEntity) = oldItem == newItem

        override fun areContentsTheSame(oldItem: GiphyEntity,
                                        newItem: GiphyEntity) = oldItem == newItem
    })

    fun submitList(list: List<GiphyEntity?>, submitReady: () -> Unit = {}) {
        val newList = asyncDiffer.currentList.toMutableList()
        newList.addAll(list)
        asyncDiffer.submitList(newList, submitReady)
    }

    fun clearAndSubmitList(list: List<GiphyEntity?>, submitReady: () -> Unit = {}) {
        clearList {
            submitList(list, submitReady)
        }
    }

    fun clearList(submitReady: () -> Unit = {}) {
        val list = asyncDiffer.currentList.toMutableList()
        list.clear()
        asyncDiffer.submitList(list, submitReady)
    }

    fun currentList() = asyncDiffer.currentList

    internal var clickListener: (GiphyEntity?) -> Unit = { _ -> }

    override fun getItemCount(): Int = asyncDiffer.currentList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent.inflate(R.layout.giphy_image_item))


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as ViewHolder
        vh.bind(asyncDiffer.currentList[position], clickListener, longClickListener)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val image: ImageView = itemView.findViewById(R.id.ivPicture)

        fun bind(
            data: GiphyEntity?,
            clickListener: (GiphyEntity?) -> Unit,
            longClickListener: (id: String, preview: String, url: String, ratio: Double) -> Unit
        ) {
            image.loadGlideProgressive(data?.smallUrl)
            itemView.setOnClickListener { clickListener(data) }
            itemView.setOnLongClickListener {
                data?.let {
                    longClickListener.invoke(it.id, it.smallUrl, it.originalUrl, data.originalAspectRatio)
                    return@setOnLongClickListener true
                }
                return@setOnLongClickListener false
            }
        }
    }

}
