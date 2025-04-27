package com.numplates.nomera3.presentation.view.adapter.newfriends

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.people.ApprovedIconSize
import com.meera.uikit.widgets.people.TopAuthorApprovedUserModel
import com.meera.uikit.widgets.people.UiKitUsernameView
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraUserItemBinding
import com.numplates.nomera3.presentation.view.utils.inflateBinding

private const val MARGIN_START_DIVIDER = 8

class MeeraFriendsListAdapter(
    private val actionClickListener: (action: FriendsListAction) -> Unit
) : ListAdapter<FriendModel, MeeraFriendsListAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflateBinding(MeeraUserItemBinding::inflate))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position], position)
    }


    inner class ViewHolder(val binding: MeeraUserItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: FriendModel, pos: Int) {
            binding.vFriends.setMarginStartDivider(MARGIN_START_DIVIDER.dp)
            binding.vFriends.cellRightIconClickListener = {
                actionClickListener.invoke(FriendsListAction.DeleteUserClick(friend))
            }
            binding.vFriends.setThrottledClickListener {
                actionClickListener.invoke(FriendsListAction.OpenProfileClick(friend.userModel.userId))
            }
            if (pos == currentList.lastIndex) {
                binding.vFriends.cellPosition = CellPosition.BOTTOM
            } else {
                binding.vFriends.cellPosition = CellPosition.MIDDLE
            }
            initUserAvatar(friend.userModel.avatar, friend.userModel.gender)

            binding.vFriends.setTitleValue(friend.userModel.name)

            binding.vFriends.findViewById<UiKitUsernameView>(R.id.tv_title)?.apply {
                enableTopContentAuthorApprovedUser(
                    params = TopAuthorApprovedUserModel(
                        customIconTopContent = R.drawable.ic_approved_author_gold_10,
                        approvedIconSize = ApprovedIconSize.SMALL,
                        approved = friend.userModel.approved.toBoolean(),
                        interestingAuthor = friend.userSimple?.topContentMaker?.toBoolean() ?: false
                    )
                )
            }
            friend.userModel.city?.let {
                binding.vFriends.cellCityText = true
                binding.vFriends.setCityValue(it)
            }

            friend.userSimple?.uniqueName?.let { uName: String ->
                if (uName.isNotEmpty()) {
                    val formattedUniqueName = "@$uName"
                    binding.vFriends.setDescriptionValue(formattedUniqueName)
                }
            }
        }

        private fun initUserAvatar(avatarSmall: String?, gender: Int?) {
            avatarSmall?.let {
                binding.vFriends.setLeftUserPicConfig(
                    UserpicUiModel(
                        userAvatarUrl = avatarSmall,
                        userAvatarErrorPlaceholder = identifyAvatarByGender(gender)
                    )
                )
            } ?: {
                binding.vFriends.setLeftUserPicConfig(
                    UserpicUiModel(
                        userAvatarRes = identifyAvatarByGender(gender)
                    )
                )
            }
        }

        private fun identifyAvatarByGender(gender: Int?): Int {
            return if (gender.toBoolean()) {
                R.drawable.ic_man_avatar_placeholder
            } else {
                R.drawable.ic_woman_avatar_placeholder
            }
        }
    }
}

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FriendModel>() {
    override fun areContentsTheSame(oldItem: FriendModel, newItem: FriendModel): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: FriendModel, newItem: FriendModel): Boolean {
        return oldItem == newItem
    }
}
