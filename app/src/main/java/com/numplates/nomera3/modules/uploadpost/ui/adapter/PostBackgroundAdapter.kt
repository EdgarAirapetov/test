package com.numplates.nomera3.modules.uploadpost.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.appInfo.ui.entity.PostBackgroundItemUiModel
import com.numplates.nomera3.modules.uploadpost.ui.view.PostSelectBackgroundItemView

class PostBackgroundAdapter(
    private val onBackgroundSelected: (PostBackgroundItemUiModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val backgrounds = mutableListOf<PostBackgroundItemUiModel>()

    fun submitList(data: List<PostBackgroundItemUiModel>) {
        val diffCallback = PostBackgroundDiffCallback(backgrounds, data)
        DiffUtil.calculateDiff(diffCallback).dispatchUpdatesTo(this)
        backgrounds.clear()
        backgrounds.addAll(data)
    }

    fun selectItem(payload: PostBackgroundItemUiModel) {
        backgrounds.forEachIndexed { index, background ->
            if (background.id == payload.id) {
                updateModel(index, background.setSelected(true))
            } else {
                if (background.isSelected) {
                    updateModel(index, background.setSelected(false))
                }
            }
        }
    }

    fun selectItem(position: Int) {
        if (isCorrectPosition(position)) {
            updateModel(position, backgrounds[0].setSelected(true))
        }
    }

    fun getSelectedItem(): PostBackgroundItemUiModel? {
        return backgrounds.find { it.isSelected }
    }

    private fun updateModel(index: Int, payload: PostBackgroundItemUiModel) {
        if (isCorrectPosition(index)) {
            backgrounds[index] = payload
            notifyItemChanged(index, payload)
        }
    }

    private fun isCorrectPosition(pos: Int) = pos >= 0 && pos < backgrounds.size

    fun getItem(position: Int): PostBackgroundItemUiModel? =
        if (backgrounds.size > 0 && position < backgrounds.size) backgrounds[position] else null

    override fun getItemCount(): Int = backgrounds.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_post_select_background, parent, false)
        return PostSelectBackgroundViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty() && payloads[0] is PostBackgroundItemUiModel && holder is PostSelectBackgroundViewHolder) {
            holder.updatePayload(payloads[0] as PostBackgroundItemUiModel)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PostSelectBackgroundViewHolder) {
            val background = backgrounds[position]
            holder.bind(
                postBackgroundItemUiModel = background,
                onBackgroundSelected = onBackgroundSelected
            )
        }
    }

    private class PostBackgroundDiffCallback(
        private val oldList: List<PostBackgroundItemUiModel>,
        private val newList: List<PostBackgroundItemUiModel>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldPostItem = oldList[oldItemPosition]
            val newPostItem = newList[newItemPosition]

            return oldPostItem.id == newPostItem.id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldPost = oldList[oldItemPosition]
            val newPost = newList[newItemPosition]

            return oldPost == newPost && oldPost.isSelected == newPost.isSelected
        }
    }
}

class PostSelectBackgroundViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {
    fun bind(
        postBackgroundItemUiModel: PostBackgroundItemUiModel,
        onBackgroundSelected: (PostBackgroundItemUiModel) -> Unit
    ) {
        itemView.apply {
            findViewById<PostSelectBackgroundItemView>(R.id.item_post_select_background)
                .bind(
                    postBackgroundItemUiModel = postBackgroundItemUiModel,
                    onBackgroundSelected = onBackgroundSelected
                )
        }
    }

    fun updatePayload(postBackgroundItemUiModel: PostBackgroundItemUiModel) {
        itemView.apply {
            findViewById<PostSelectBackgroundItemView>(R.id.item_post_select_background)
                .updatePayload(postBackgroundItemUiModel = postBackgroundItemUiModel)
        }
    }
}
