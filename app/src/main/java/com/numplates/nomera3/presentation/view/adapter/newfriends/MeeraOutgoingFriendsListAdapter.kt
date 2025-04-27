package com.numplates.nomera3.presentation.view.adapter.newfriends

import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.db.models.userprofile.UserSimple
import com.meera.uikit.widgets.people.ApprovedIconSize
import com.meera.uikit.widgets.people.TopAuthorApprovedUserModel
import com.meera.uikit.widgets.people.UiKitUsernameView
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraOutgoingUserItemBinding
import com.numplates.nomera3.presentation.view.utils.inflateBinding

class MeeraOutgoingFriendsListAdapter(
    private val actionClickListener: (action: FriendsListAction) -> Unit
) : ListAdapter<UserSimple, MeeraOutgoingFriendsListAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflateBinding(MeeraOutgoingUserItemBinding::inflate))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class ViewHolder(val binding: MeeraOutgoingUserItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(friend: UserSimple) {

            initClickListener(friend)
            friend.profileVerified?.let {
                binding.vFriends.cellTitleVerified = it.toBoolean()
            }

            binding.vFriends.setLeftUserPicConfig(
                UserpicUiModel(
                    userAvatarUrl = friend.avatarSmall
                )
            )
            binding.vFriends.setTitleValue(friend.name ?: "")
            binding.vFriends.findViewById<UiKitUsernameView>(R.id.tv_title)?.apply {
                enableTopContentAuthorApprovedUser(
                    params = TopAuthorApprovedUserModel(
                        customIconTopContent = R.drawable.ic_approved_author_gold_10,
                        approvedIconSize = ApprovedIconSize.SMALL,
                        approved = friend.approved.toBoolean(),
                        interestingAuthor = friend.topContentMaker.toBoolean()
                    )
                )
            }
            friend.city?.let {
                binding.vFriends.cellCityText = true
                binding.vFriends.setCityValue(it.name ?: "")
            }
            ResourcesCompat.getColorStateList(binding.root.resources, R.color.uiKitColorForegroundSecondary, null)
                ?.let { binding.vCancelRequest.updateContentColor(it) }

            friend.uniqueName?.let { uName: String ->
                if (uName.isNotEmpty()) {
                    val formattedUniqueName = "@$uName"
                    binding.vFriends.setDescriptionValue(formattedUniqueName)
                }
            }
        }

        private fun initClickListener(friend: UserSimple) {
            binding.vCancelRequest.setThrottledClickListener {
                actionClickListener.invoke(FriendsListAction.CancelOutgoingFriendshipClick(friend))
                binding.vCancelRequest.text = binding.root.resources.getText(R.string.meera_canceled_friend_request)
            }

            binding.vFriends.setThrottledClickListener {
                actionClickListener.invoke(FriendsListAction.OpenProfileClick(friend.userId))
            }
        }
    }
}

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UserSimple>() {
    override fun areContentsTheSame(oldItem: UserSimple, newItem: UserSimple): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: UserSimple, newItem: UserSimple): Boolean {
        return oldItem == newItem
    }
}
