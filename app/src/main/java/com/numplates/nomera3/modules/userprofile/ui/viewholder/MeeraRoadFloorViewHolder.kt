package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.text.InputType
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemRoadFloorBinding
import com.numplates.nomera3.modules.userprofile.ui.fragment.UserInfoRecyclerData
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction

class MeeraRoadFloorViewHolder(
    val binding: MeeraItemRoadFloorBinding, private val profileUIActionHandler: (UserProfileUIAction) -> Unit
) : BaseVH<UserInfoRecyclerData, MeeraItemRoadFloorBinding>(binding) {

    override fun bind(recyclerData: UserInfoRecyclerData) {
        val data = (recyclerData as UserInfoRecyclerData.UserEntityRoadFloor)
        binding.apply {
            if (data.postCount > 0) {
                tvPostsAmount.text = data.postCount.toString()
            } else {
                tvPostsAmount.text = String.empty()
            }

            addPost.inputFieldAddPost.apply {
                etInput.isFocusable = false
                etInput.inputType = InputType.TYPE_NULL
            }
        }

        if (data.isMe) {
            setupHolderForOwner(data)
        } else {
            setupHolderForUserAction(data)
        }

        initClickListeners()
    }

    private fun initClickListeners() {
        binding.apply {
            addPost.addPostClickContainer.setThrottledClickListener {
                profileUIActionHandler.invoke(
                    UserProfileUIAction.OnNewPostClicked(false)
                )
            }

            vEmptyRoad.setThrottledClickListener {
                profileUIActionHandler.invoke(
                    UserProfileUIAction.OnNewPostClicked(false)
                )
            }
        }
    }

    private fun setupHolderForUserAction(data: UserInfoRecyclerData.UserEntityRoadFloor) {
        binding.apply {
            addPost.root.gone()
            btnWritePostPlaceholder.gone()
            if (data.postCount == 0) {
                itemView.gone()
            } else {
                itemView.visible()
                gEmptyRoad.gone()
            }
        }
    }

    private fun setupHolderForOwner(data: UserInfoRecyclerData.UserEntityRoadFloor) {
        binding.apply {
            itemView.visible()
            addPost.root.visible()
            btnWritePostPlaceholder.visible()
            tvRoadPlaceholder.text = itemView.context.getText(R.string.meera_place_holder_user_post_list)
            if (data.postCount == 0) {
                gEmptyRoad.visible()
            } else {
                gEmptyRoad.gone()
                addPost.root.visible()
            }
        }
    }
}
