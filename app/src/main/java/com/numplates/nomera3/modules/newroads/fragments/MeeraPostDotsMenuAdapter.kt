package com.numplates.nomera3.modules.newroads.fragments

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.cell.CellPosition
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraPostDotsMenuDialogItemBinding
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity

class MeeraPostDotsMenuAdapter(
    val postDotsMenuItemList: List<MeeraPostDotsMenuItemType>,
    val post: PostUIEntity,
    val menuItemClick: (action: MeeraPostDotsMenuAction) -> Unit,
    val dismissItemClick: () -> Unit
) : RecyclerView.Adapter<MeeraPostDotsMenuAdapter.PostDotsMenuItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostDotsMenuItemHolder {
        val binding = MeeraPostDotsMenuDialogItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostDotsMenuItemHolder(binding, parent.context.resources)
    }

    override fun getItemCount() = postDotsMenuItemList.size

    override fun onBindViewHolder(holder: PostDotsMenuItemHolder, position: Int) {
        when (postDotsMenuItemList[position]) {
            MeeraPostDotsMenuItemType.SAVE_TO_DEVICE -> {
                holder.bind(
                    title = R.string.save_to_device,
                    icon = R.drawable.ic_outlined_download_m,
                    itemType = MeeraPostDotsMenuAction.SaveToDevice,
                    isLast = postDotsMenuItemList.lastIndex == position,
                    isFirst = position == 0
                )
            }

            MeeraPostDotsMenuItemType.SUBSCRIBE_TO_POST -> {
                holder.bind(
                    title = if (post.isPostSubscribed) {
                        if (post.event != null) R.string.unsubscribe_event_post_txt else R.string.unsubscribe_post_txt
                    } else {
                        if (post.event != null) R.string.subscribe_event_post_txt else R.string.subscribe_post_txt
                    },
                    icon = R.drawable.ic_outlined_post_m,
                    itemType = MeeraPostDotsMenuAction.SubscribeToPost,
                    isLast = postDotsMenuItemList.lastIndex == position,
                    isFirst = position == 0
                )
            }

            MeeraPostDotsMenuItemType.SUBSCRIBE_TO_PROFILE -> {
                holder.bind(
                    title = R.string.subscribe_user_txt,
                    icon = R.drawable.ic_outlined_user_add_m,
                    itemType = MeeraPostDotsMenuAction.SubscribeToProfile,
                    isLast = postDotsMenuItemList.lastIndex == position,
                    isFirst = position == 0
                )
            }

            MeeraPostDotsMenuItemType.SHARE -> {
                holder.bind(
                    title = R.string.general_share,
                    icon = R.drawable.ic_outlined_repost_m,
                    itemType = MeeraPostDotsMenuAction.SharePost,
                    isLast = postDotsMenuItemList.lastIndex == position,
                    isFirst = position == 0
                )
            }

            MeeraPostDotsMenuItemType.COPY_LINK -> {
                holder.bind(
                    title = R.string.copy_link,
                    icon = R.drawable.ic_outlined_copy_m,
                    itemType = MeeraPostDotsMenuAction.CopyLink,
                    isLast = postDotsMenuItemList.lastIndex == position,
                    isFirst = position == 0
                )
            }

            MeeraPostDotsMenuItemType.HIDE_ALL_PROFILE_POST -> {
                holder.bind(
                    title = R.string.profile_complain_hide_all_posts,
                    icon = R.drawable.ic_outlined_eye_off_m,
                    isRed = true,
                    itemType = MeeraPostDotsMenuAction.HideAllProfilePost,
                    isLast = postDotsMenuItemList.lastIndex == position,
                    isFirst = position == 0
                )
            }

            MeeraPostDotsMenuItemType.COMPLAIN_POST -> {
                holder.bind(
                    title = if (post.event != null) R.string.complain_about_event_post else R.string.complain_about_post,
                    icon = R.drawable.ic_outlined_attention_m,
                    isRed = true,
                    itemType = MeeraPostDotsMenuAction.ComplainPost,
                    isLast = postDotsMenuItemList.lastIndex == position,
                    isFirst = position == 0
                )
            }

            MeeraPostDotsMenuItemType.EDIT_POST -> {
                holder.bind(
                    title = R.string.general_edit,
                    icon = R.drawable.ic_outlined_pencil_m,
                    itemType = MeeraPostDotsMenuAction.EditPost,
                    isLast = postDotsMenuItemList.lastIndex == position,
                    isFirst = position == 0
                )
            }

            MeeraPostDotsMenuItemType.DELETE_POST -> {
                holder.bind(
                    title = R.string.road_delete,
                    icon = R.drawable.ic_outlined_archive_m,
                    isRed = true,
                    itemType = MeeraPostDotsMenuAction.DeletePost,
                    isLast = postDotsMenuItemList.lastIndex == position,
                    isFirst = position == 0
                )
            }
        }
    }


    inner class PostDotsMenuItemHolder(
        val binding: MeeraPostDotsMenuDialogItemBinding,
        val resources: Resources,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            title: Int,
            icon: Int,
            isRed: Boolean = false,
            isFirst: Boolean = false,
            isLast: Boolean = false,
            itemType: MeeraPostDotsMenuAction
        ) {
            binding.vMenuItem.apply {
                setMarginStartDivider(8.dp)
                setTitleValue(resources.getString(title))
                setLeftIcon(icon)
                if (isRed) cellLeftIconAndTitleColor = R.color.uiKitColorAccentWrong
                if (isLast) cellPosition = CellPosition.BOTTOM
                if (isFirst) cellPosition = CellPosition.TOP
                setThrottledClickListener {
                    menuItemClick.invoke(itemType)
                    dismissItemClick.invoke()
                }
            }
        }
    }
}
