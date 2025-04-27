package com.numplates.nomera3.modules.userprofile.ui.controller

import androidx.core.content.ContextCompat
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.databinding.FragmentUserInfoBinding
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum

class ProfileThemeController {

    fun setupTheme(accountType: Int?, accountColor: Int?, binding: FragmentUserInfoBinding?) {
        when (accountType) {
            AccountTypeEnum.ACCOUNT_TYPE_VIP.value -> {
                setupVipTheme(binding)
            }
            AccountTypeEnum.ACCOUNT_TYPE_PREMIUM.value -> {
                setupPremiumTheme(accountColor, binding)
            }
            else -> {
                setupCommonTheme(binding)
            }
        }
    }

    private fun setupVipTheme(binding: FragmentUserInfoBinding?) {
        val context = binding?.root?.context ?: return
        binding.srlUserProfile.setBackgroundColor(ContextCompat.getColor(context, R.color.colorVipPostGoldBlack))
        binding.profileGradientBottom.setBackgroundResource(R.drawable.avatar_gradient_bottom_purple)
        binding.ivVipBg.visible()
    }

    private fun setupPremiumTheme(accountColor: Int?, binding: FragmentUserInfoBinding?) {
        setupCommonTheme(binding)
        binding?.ivVipBg?.visible()
        val gradientRes = when (accountColor) {
            INetworkValues.COLOR_RED -> R.drawable.avatar_gradient_bottom_red
            INetworkValues.COLOR_GREEN -> R.drawable.avatar_gradient_bottom_green
            INetworkValues.COLOR_BLUE -> R.drawable.avatar_gradient_bottom_blue
            INetworkValues.COLOR_PINK -> R.drawable.avatar_gradient_bottom_pink
            INetworkValues.COLOR_PURPLE -> R.drawable.avatar_gradient_bottom_purple
            else -> R.drawable.avatar_gradient_bottom
        }
        binding?.profileGradientBottom?.setBackgroundResource(gradientRes)
    }

    private fun setupCommonTheme(binding: FragmentUserInfoBinding?) {
        val context = binding?.root?.context ?: return
        binding.srlUserProfile.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite))
        binding.ivVipBg.gone()
    }
}
