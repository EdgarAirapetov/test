package com.numplates.nomera3.modules.feedmediaview.ui.adapter

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.numplates.nomera3.MEDIA_IMAGE
import com.numplates.nomera3.MEDIA_VIDEO
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ARG_MULTIMEDIA_ITEM_ASSET_DATA
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ViewMultimediaActionListener
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ViewMultimediaImageItemFragment
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ViewMultimediaVideoItemFragment

class ViewMultimediaPagerAdapter(
    fragment: Fragment,
    val fragmentManager: FragmentManager,
    val actionListener: ViewMultimediaActionListener
) : FragmentStateAdapter(fragment) {

    private val differ = AsyncListDiffer(this, MultimediaAssetDiffItemCallback())

    override fun createFragment(position: Int): Fragment {
        val data = differ.currentList[position]

        val fragment = when (data?.type) {
            MEDIA_IMAGE -> ViewMultimediaImageItemFragment().apply {
                arguments = bundleOf(
                    ARG_MULTIMEDIA_ITEM_ASSET_DATA to data
                )
            }
            else -> ViewMultimediaVideoItemFragment().apply {
                arguments = bundleOf(
                    ARG_MULTIMEDIA_ITEM_ASSET_DATA to data
                ).also {
                    bind(actionListener = actionListener)
                }
            }
        }

        return fragment
    }

    override fun getItemCount(): Int = differ.currentList.size

    fun containsItem(itemId: String?): Boolean {
        return differ.currentList.find { it.id == itemId } != null
    }

    fun getItemIdByPosition(position: Int): String? {
        if (position < 0 || position >= differ.currentList.size) return null
        return differ.currentList[position].id
    }

    fun getItemPositionById(itemId: String?): Int {
        val currentList = differ.currentList
        return currentList.indexOf(currentList.find { it.id == itemId })
    }

    fun getItemFromPosition(position: Int): MediaAssetEntity? {
        val itemId = getItemIdByPosition(position)
        val currentList = differ.currentList
        return currentList.find { it.id == itemId }
    }

    fun getCurrentList(): List<MediaAssetEntity> {
        return differ.currentList
    }

    fun getFragmentByItemId(itemId: String?): Fragment? {
        return if (containsItem(itemId)) {
            val neededId = getItemIdByPosition(getItemPositionById(itemId))
            return fragmentManager.fragments.find {
                val args = it.arguments
                if (it is ViewMultimediaImageItemFragment || it is ViewMultimediaVideoItemFragment) {
                    args != null && args.containsKey(ARG_MULTIMEDIA_ITEM_ASSET_DATA) &&
                        args.getParcelable<MediaAssetEntity>(ARG_MULTIMEDIA_ITEM_ASSET_DATA)?.id == neededId
                } else false
            }
        } else {
            null
        }
    }

    fun isItemVideo(position: Int): Boolean {
        if (differ.currentList.isEmpty() || differ.currentList.size <= position) return false
        return differ.currentList[position].type == MEDIA_VIDEO
    }

    fun submitList(list: List<MediaAssetEntity>, commitCallback: (() -> Unit)? = null) {
        differ.submitList(list) {
            commitCallback?.invoke()
        }
    }
}

private class MultimediaAssetDiffItemCallback : DiffUtil.ItemCallback<MediaAssetEntity>() {
    override fun areItemsTheSame(
        oldItem: MediaAssetEntity,
        newItem: MediaAssetEntity
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: MediaAssetEntity,
        newItem: MediaAssetEntity
    ): Boolean {
        return oldItem == newItem
    }
}
