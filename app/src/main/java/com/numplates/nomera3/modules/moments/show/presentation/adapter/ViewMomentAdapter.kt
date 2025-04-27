package com.numplates.nomera3.modules.moments.show.presentation.adapter

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.numplates.nomera3.modules.moments.show.data.ARG_MOMENT_ID
import com.numplates.nomera3.modules.moments.show.domain.GetMomentDataUseCase
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupUiModel
import com.numplates.nomera3.modules.moments.show.presentation.fragment.ARG_MOMENTS_SOURCE
import com.numplates.nomera3.modules.moments.show.presentation.fragment.ARG_MOMENT_GROUP_ID
import com.numplates.nomera3.modules.moments.show.presentation.fragment.ARG_MOMENT_USER_ID
import com.numplates.nomera3.modules.moments.show.presentation.fragment.ARG_TARGET_MOMENT_ID
import com.numplates.nomera3.modules.moments.show.presentation.fragment.MeeraViewMomentPositionFragment
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_COMMENT_ID

class ViewMomentAdapter(
    fragment: Fragment,
    private val momentsSource: GetMomentDataUseCase.MomentsSource,
    private val targetMomentId: Long? = null,
    private val targetCommentId: Long? = null,
    private val singleMomentId: Long? = null
) : FragmentStateAdapter(fragment) {

    private val differ = AsyncListDiffer(this, MomentGroupDiffItemCallback())

    override fun createFragment(position: Int): Fragment {
        val data = differ.currentList[position]
        return MeeraViewMomentPositionFragment().apply {
            arguments = bundleOf(
                ARG_MOMENT_ID to singleMomentId,
                ARG_MOMENT_GROUP_ID to data.id,
                ARG_MOMENT_USER_ID to data.userId,
                ARG_MOMENTS_SOURCE to momentsSource,
                ARG_TARGET_MOMENT_ID to targetMomentId,
                ARG_COMMENT_ID to targetCommentId
            )
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun getItemId(position: Int): Long {
        return if (position < 0 || position >= differ.currentList.size) RecyclerView.NO_ID else differ.currentList[position].id
    }

    override fun containsItem(itemId: Long): Boolean {
        return differ.currentList.find { it.id == itemId } != null
    }

    fun getItem(position: Int): MomentGroupUiModel? {
        return if (position < 0 || position >= differ.currentList.size) null else differ.currentList[position]
    }

    fun getItem(itemId: Long): MomentGroupUiModel? {
        val position = getItemPositionFromId(itemId)
        return getItem(position)
    }

    fun getItemPositionFromId(itemId: Long): Int {
        val currentList = differ.currentList
        return currentList.indexOf(currentList.find { it.id == itemId })
    }

    fun getCurrentList(): List<MomentGroupUiModel> {
        return differ.currentList
    }

    fun submitList(list: List<MomentGroupUiModel>, commitCallback: (() -> Unit)? = null) {
        differ.submitList(list) {
            commitCallback?.invoke()
        }
    }
}

private class MomentGroupDiffItemCallback : DiffUtil.ItemCallback<MomentGroupUiModel>() {
    override fun areItemsTheSame(
        oldItem: MomentGroupUiModel,
        newItem: MomentGroupUiModel
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: MomentGroupUiModel,
        newItem: MomentGroupUiModel
    ): Boolean {
        return oldItem == newItem
    }
}
