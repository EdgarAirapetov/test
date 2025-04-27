package com.numplates.nomera3.modules.userprofile.ui.meerafriends.outgoing

import android.view.ViewGroup
import com.meera.core.adapters.baserecycleradapter.BaseAsyncAdapter
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.adapters.baserecycleradapter.RecyclerData
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemOutgoingFriendBinding
import com.numplates.nomera3.presentation.view.adapter.newfriends.FriendModel

class MeeraOutgoingFriendsAdapter(
    private val onItemClicked: (FriendModel) -> Unit, private val onCancelClicked: (FriendModel) -> Unit
) : BaseAsyncAdapter<String, OutgoingFriendsRecyclerData>() {
    override fun getHolderType(viewType: Int, parent: ViewGroup): BaseVH<OutgoingFriendsRecyclerData, *> {
        return when (viewType) {
            TYPE_DATA -> MeeraFriendVH(
                binding = parent.toBinding(), onItemClicked = onItemClicked, onCancelClicked = onCancelClicked
            )

            else -> throw RuntimeException("Missing data adapter type")
        }
    }
}


class MeeraFriendVH(
    private val binding: MeeraItemOutgoingFriendBinding,
    private val onItemClicked: (FriendModel) -> Unit,
    private val onCancelClicked: (FriendModel) -> Unit
) : BaseVH<OutgoingFriendsRecyclerData, MeeraItemOutgoingFriendBinding>(binding) {
    override fun bind(data: OutgoingFriendsRecyclerData) {
        val friendModel = data as OutgoingFriendsRecyclerData.RecyclerData

        with(binding.uikitCellUser) {

            cellTitleVerified = friendModel.friendModel.userModel.approved.toBoolean()

            val avatarUrl = friendModel.friendModel.userModel.avatar
            if (avatarUrl.isNullOrBlank().not()) {
                setLeftUserPicConfig(UserpicUiModel(userAvatarUrl = avatarUrl))
            } else {
                setLeftUserPicConfig(UserpicUiModel(userAvatarRes = R.drawable.fill_8_round))
            }

            setTitleValue(friendModel.friendModel.userModel.name)
            friendModel.friendModel.userModel.uniqueName?.lowercase()?.let { uName ->
                if (uName.isNotEmpty()) {
                    val formattedUniqueName = "@$uName"
                    binding.uikitCellUser.setDescriptionValue(formattedUniqueName)
                }
            }

            setCityValue(friendModel.friendModel.userModel.city ?: "")

            rootView.setOnClickListener { onItemClicked.invoke(friendModel.friendModel) }
        }
        binding.buttonCancel.setText(R.string.request_cancel)
        binding.buttonCancel.setThrottledClickListener {
            binding.buttonCancel.setText(R.string.request_canceled)
            onCancelClicked.invoke(friendModel.friendModel)
        }
    }
}


private const val TYPE_DATA = 1


sealed interface OutgoingFriendsRecyclerData : RecyclerData<String, OutgoingFriendsRecyclerData> {


    data class RecyclerData(
        val id: Long,
        val friendModel: FriendModel,
    ) : OutgoingFriendsRecyclerData {
        override fun getItemId() = id.toString()
        override fun contentTheSame(newItem: OutgoingFriendsRecyclerData) = this.equals(newItem)
        override fun itemViewType() = TYPE_DATA
    }

}
