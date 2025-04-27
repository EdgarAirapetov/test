package com.numplates.nomera3.modules.userprofile.ui.meerafriends.incoming

import android.view.ViewGroup
import com.meera.core.adapters.baserecycleradapter.BaseAsyncAdapter
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.adapters.baserecycleradapter.RecyclerData
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemIncomingFriendBinding
import com.numplates.nomera3.presentation.view.adapter.newfriends.FriendModel

class MeeraIncomingFriendsAdapter(
    private val onItemClicked: (FriendModel) -> Unit,
    private val onRejectClicked: (FriendModel) -> Unit,
    private val onConfirmClicked: (FriendModel) -> Unit
) : BaseAsyncAdapter<String, IncomingFriendsRecyclerData>() {
    override fun getHolderType(viewType: Int, parent: ViewGroup): BaseVH<IncomingFriendsRecyclerData, *> {
        return when (viewType) {
            TYPE_DATA -> MeeraFriendVH(
                binding = parent.toBinding(),
                onItemClicked = onItemClicked,
                onRejectClicked = onRejectClicked,
                onConfirmClicked = onConfirmClicked
            )

            else -> throw RuntimeException("Missing data adapter type")
        }
    }

}


class MeeraFriendVH(
    private val binding: MeeraItemIncomingFriendBinding,
    private val onItemClicked: (FriendModel) -> Unit,
    private val onRejectClicked: (FriendModel) -> Unit,
    private val onConfirmClicked: (FriendModel) -> Unit
) : BaseVH<IncomingFriendsRecyclerData, MeeraItemIncomingFriendBinding>(binding) {
    override fun bind(data: IncomingFriendsRecyclerData) {
        val friendModel = data as IncomingFriendsRecyclerData.RecyclerData

        with(binding.uikitCellUser) {
            setTitleValue(friendModel.friendModel.userSimple?.name ?: "")
            cellTitleVerified = friendModel.friendModel.userModel.approved.toBoolean()

            val avatarUrl = friendModel.friendModel.userModel.avatar
            if (avatarUrl.isNullOrBlank().not()) {
                setLeftUserPicConfig(UserpicUiModel(userAvatarUrl = avatarUrl))
            } else {
                setLeftUserPicConfig(UserpicUiModel(userAvatarRes = R.drawable.fill_8_round))
            }

            friendModel.friendModel.userSimple?.uniqueName?.lowercase()?.let { uName ->
                if (uName.isNotEmpty()) {
                    val formattedUniqueName = "@$uName"
                    binding.uikitCellUser.setDescriptionValue(formattedUniqueName)
                }
            }
            setCityValue(friendModel.friendModel.userModel.city ?: "")




            rootView.setOnClickListener { onItemClicked.invoke(friendModel.friendModel) }
        }

        binding.buttonApprove.setThrottledClickListener {
            binding.tvDone.setText(R.string.request_acepted)
            binding.ivMark.visible()
            binding.vActionDone.visible()
            binding.tvDone.visible()

            onConfirmClicked.invoke(friendModel.friendModel)
        }
        binding.buttonDecline.setThrottledClickListener {
            binding.tvDone.setText(R.string.request_rejected)
            binding.ivMark.gone()
            binding.vActionDone.visible()
            binding.tvDone.visible()
            onRejectClicked.invoke(friendModel.friendModel)
        }
    }
}


const val TYPE_DATA = 1


sealed interface IncomingFriendsRecyclerData : RecyclerData<String, IncomingFriendsRecyclerData> {


    data class RecyclerData(
        val id: Long,
        val friendModel: FriendModel,
    ) : IncomingFriendsRecyclerData {

        override fun getItemId() = id.toString()
        override fun contentTheSame(newItem: IncomingFriendsRecyclerData) = this.equals(newItem)
        override fun itemViewType() = TYPE_DATA
    }

}
