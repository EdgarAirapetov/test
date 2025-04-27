package com.numplates.nomera3.presentation.view.adapter.newfriends

import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.invisible
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.people.ApprovedIconSize
import com.meera.uikit.widgets.people.TopAuthorApprovedUserModel
import com.meera.uikit.widgets.people.UiKitUsernameView
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraIncomingUserItemBinding
import com.numplates.nomera3.presentation.view.utils.inflateBinding

class MeeraIncomingFriendsListAdapter(
    private val actionClickListener: (action: FriendsListAction) -> Unit
) : ListAdapter<FriendModel, MeeraIncomingFriendsListAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflateBinding(MeeraIncomingUserItemBinding::inflate))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class ViewHolder(val binding: MeeraIncomingUserItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: FriendModel) {

            initClickListener(friend)
            friend.userSimple?.profileVerified?.let {
                binding.vFriends.cellTitleVerified = it.toBoolean()
            }

            binding.vFriends.setLeftUserPicConfig(
                UserpicUiModel(
                    userAvatarUrl = friend.userModel.avatar
                )
            )
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

        private fun initClickListener(friend: FriendModel) {
            binding.vRejectBtn.setThrottledClickListener {
                actionClickListener.invoke(FriendsListAction.RejectFriendClick(friend))
                binding.requestBtnContainer.invisible()
                ResourcesCompat.getColorStateList(binding.root.resources, R.color.uiKitColorForegroundSecondary, null)
                    ?.let {
                        binding.vRequestReject.updateContentColor(it)
                    }

                binding.vRequestReject.visible()
            }

            binding.vConfirmBtn.setThrottledClickListener {
                actionClickListener.invoke(FriendsListAction.ConfirmFriendClick(friend))
                binding.requestBtnContainer.invisible()
                binding.vRequestAcepted.visible()
            }

            binding.vFriends.setThrottledClickListener {
                actionClickListener.invoke(FriendsListAction.OpenProfileClick(friend.userModel.userId))
            }
        }
    }
}

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FriendModel>() {
    override fun areContentsTheSame(oldItem: FriendModel, newItem: FriendModel): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: FriendModel, newItem: FriendModel): Boolean {
        return oldItem.userModel.userId == newItem.userModel.userId
    }
}
