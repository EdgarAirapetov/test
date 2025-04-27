package com.numplates.nomera3.modules.feedviewcontent.presentation.adapter

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.numplates.nomera3.modules.feedviewcontent.presentation.data.ContentItemUiModel
import com.numplates.nomera3.modules.feedviewcontent.presentation.fragment.ARG_CONTENT_ITEM
import com.numplates.nomera3.modules.feedviewcontent.presentation.fragment.ViewContentPositionFragment
import com.numplates.nomera3.modules.feedviewcontent.presentation.fragment.ViewContentReactionsListener

class ViewContentAdapter(
    fragment: Fragment,
    val fragmentManager: FragmentManager,
    val viewContentReactionsListener: ViewContentReactionsListener
) : FragmentStateAdapter(fragment) {

    private val differ = AsyncListDiffer(this, ContentGroupDiffItemCallback())

    override fun createFragment(position: Int): Fragment {
        val data = differ.currentList[position]

        return ViewContentPositionFragment().apply {
            arguments = bundleOf(
                ARG_CONTENT_ITEM to data
            )
            bind(viewContentReactionsListener = viewContentReactionsListener)
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun getItemId(position: Int): Long {
        if (position < 0 || position >= differ.currentList.size) return RecyclerView.NO_ID
        return differ.currentList[position].id
    }

    override fun containsItem(itemId: Long): Boolean {
        return differ.currentList.find { it.id == itemId } != null
    }

    fun getItemPositionFromId(itemId: Long): Int {
        val currentList = differ.currentList
        return currentList.indexOf(currentList.find { it.id == itemId })
    }

    fun getItemFromPosition(position: Int): ContentItemUiModel? {
        val itemId = getItemId(position)
        val currentList = differ.currentList
        return currentList.find { it.id == itemId }
    }

    fun getCurrentList(): List<ContentItemUiModel> {
        return differ.currentList
    }

    fun getFragmentByItemId(itemId: Long): ViewContentPositionFragment? {
        return if (containsItem(itemId)) {
            val neededId = getItemId(getItemPositionFromId(itemId))
            return fragmentManager.fragments.filterIsInstance<ViewContentPositionFragment>().find {
                val args = it.arguments
                args != null && args.containsKey(ARG_CONTENT_ITEM) &&
                    args.getParcelable<ContentItemUiModel>(ARG_CONTENT_ITEM)?.id == neededId
            }
        } else {
            null
        }
    }

    fun submitList(list: List<ContentItemUiModel>, commitCallback: (() -> Unit)? = null) {
        differ.submitList(list) {
            commitCallback?.invoke()
        }
    }
}

private class ContentGroupDiffItemCallback : DiffUtil.ItemCallback<ContentItemUiModel>() {
    override fun areItemsTheSame(
        oldItem: ContentItemUiModel,
        newItem: ContentItemUiModel
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: ContentItemUiModel,
        newItem: ContentItemUiModel
    ): Boolean {
        return oldItem == newItem
    }
}

