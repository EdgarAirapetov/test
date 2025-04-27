package com.numplates.nomera3.presentation.view.fragments.userprofileinfo.holders

import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraUserPersonalInfoAvatarItemBinding
import com.numplates.nomera3.presentation.view.fragments.userprofileinfo.UserPersonalInfoAction
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoContainer

class MeeraUserPersonalInfoAvatarItemHolder(
    val binding: MeeraUserPersonalInfoAvatarItemBinding,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val actionListener: (UserPersonalInfoAction) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(userProfileContainer: UserPersonalInfoContainer) {

        loadUserImage(userProfileContainer.photo, userProfileContainer.avatarAnimation, userProfileContainer.isMale)

        binding.ibAddImage.setThrottledClickListener {
            actionListener.invoke(UserPersonalInfoAction.AvatarItemClick { avatarState ->
                loadUserImage(avatarState?.first, avatarState?.second, userProfileContainer.isMale)
            })
        }
    }

    private fun loadUserImage(avatar: String?, avatarAnimation: String?, isMail: Boolean) {
        when {
            avatar.isNullOrEmpty().not() -> {
                binding.vAvatarViewContainer.gone()
                binding.userImagePersonalInfo.setConfig(
                    UserpicUiModel(
                        userAvatarUrl = avatar
                    )
                )
            }

            avatarAnimation.isNullOrEmpty().not() -> {
                binding.vAvatarViewContainer.visible()
                avatarAnimation?.let { binding.vAvatarView.setStateAsync(avatarAnimation, lifecycleScope) }

            }

            else -> {
                binding.userImagePersonalInfo.setConfig(
                    UserpicUiModel(
                        userAvatarRes = if (isMail) {
                            R.drawable.ic_man_avatar_placeholder
                        } else {
                            R.drawable.ic_woman_avatar_placeholder
                        }
                    )
                )
            }
        }
    }
}
