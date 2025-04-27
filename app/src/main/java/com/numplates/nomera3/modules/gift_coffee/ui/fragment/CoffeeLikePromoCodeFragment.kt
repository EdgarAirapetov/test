package com.numplates.nomera3.modules.gift_coffee.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.extensions.click
import com.meera.core.extensions.drawable
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.string
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleGone
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues
import com.meera.db.models.userprofile.GiftEntity
import com.meera.db.models.userprofile.GiftSenderUser
import com.numplates.nomera3.databinding.FragmentGiftGivenCoffeeLikeBinding
import com.numplates.nomera3.modules.gift_coffee.ui.viewmodel.CoffeeLikePromoCodeViewModel
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.TYPE_GIFT_COFFEE_LIKE
import com.numplates.nomera3.presentation.view.utils.NTime
import com.numplates.nomera3.presentation.view.utils.NToast


class CoffeeLikePromoCodeFragment
    : BaseFragmentNew<FragmentGiftGivenCoffeeLikeBinding>() {

    private val viewModel by viewModels<CoffeeLikePromoCodeViewModel>()

    private var data: GiftEntity? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGiftGivenCoffeeLikeBinding
        get() = FragmentGiftGivenCoffeeLikeBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gettingData()
        initObservables()
        initViews()
        startActions()
    }

    private fun gettingData() {
        arguments?.let {
            it.get(FRAGMENT_DATA_KEY)?.let { argData ->
                data = argData as GiftEntity
            }
        }
    }

    private fun initObservables() {
        viewModel.event.observe(viewLifecycleOwner) {
            binding?.promoCode?.text = it
        }

        viewModel.failure.observe(viewLifecycleOwner) {
            NToast.with(view)
                    .text(getString(R.string.error_try_later))
                    .show()
        }
    }

    private fun startActions() {
        data?.giftId?.let {
            viewModel.markGiftAsViewed(it)
        }

        data?.metadata?.coffeeCode?.let {
            binding?.promoCode?.text = it
        }
    }

    private fun initViews() {
        data ?: requireActivity().onBackPressed()

        data?.let { giftEntity ->
            setupToolbar()

            binding?.tvWhereCanGetCoffee?.click {
                add(GiftListPlacesFragment(), Act.LIGHT_STATUSBAR)
            }

            if(giftEntity.hasCustomCoffeeDrawable()) {
                val customCoffeeDrawable = giftEntity.metadata?.coffeeCustomDrawable!!
                binding?.ivGift?.setImageDrawable(ContextCompat.getDrawable(requireContext(), customCoffeeDrawable))
            } else {
                binding?.ivGift?.loadGlide(giftEntity.imageBig)
            }

            binding?.tvDateWhenSentGift?.text = NTime.timeAgo(giftEntity.addedAt)

            giftEntity.comment?.let { comment ->
                if (comment.isNotEmpty()) {
                    binding?.tvMessageGift?.visible()
                    binding?.tvMessageGift?.text = comment
                } else
                    binding?.tvMessageGift?.gone()
            } ?: kotlin.run { binding?.tvMessageGift?.gone() }

            binding?.labelNew?.visibility = (
                    giftEntity.typeId == TYPE_GIFT_COFFEE_LIKE && giftEntity.metadata?.isViewed?.not() ?: true
                    ).visibleGone()

            giftEntity.senderUser?.let { senderUser ->    // Not anonymous
                showSenderInfo(senderUser)
            } ?: kotlin.run {   // Anonymous
                showSenderAnonymousInfo()
            }
        }
    }

    private fun showSenderInfo(sender: GiftSenderUser) {
        if (sender.accountColor != null && sender.accountType != null) {
            binding?.ivUserWhoSentGift?.setUp(
                    requireContext(),
                    sender.avatar?.avatarSmall,
                    sender.accountType ?: 1,
                    sender.accountColor ?: INetworkValues.ACCOUNT_TYPE_REGULAR,
                    hasShadow = false
            )
            binding?.ivUserWhoSentGift?.visible()
        } else {
            binding?.ivUserWhoSentGift?.gone()
        }

        sender.name?.let { name ->
            binding?.tvUserWhoSentGift?.text = name
            binding?.tvUserWhoSentGift?.visible()
        } ?: kotlin.run {
            binding?.tvUserWhoSentGift?.gone()
        }
    }

    private fun showSenderAnonymousInfo() {
        binding?.ivUserWhoSentGift?.setUp(
                requireContext(),
                requireContext().drawable(R.drawable.incognita),
                INetworkValues.ACCOUNT_TYPE_REGULAR,
                INetworkValues.ACCOUNT_TYPE_REGULAR,
                hasShadow = false
        )

        binding?.ivUserWhoSentGift?.visible()
        binding?.tvUserWhoSentGift?.text = requireContext().string(R.string.anonymously)
        binding?.tvUserWhoSentGift?.visible()
    }

    private fun setupToolbar() {
        binding?.toolbar?.setNavigationIcon(R.drawable.arrowback)

        binding?.toolbar?.navClick {
            act.onBackPressed()
        }

        binding?.tvSendGiftToolbar?.text = requireContext().string(R.string.gift)
        val layoutParamsStatusBar = binding?.statusBarSendGift?.layoutParams as AppBarLayout.LayoutParams
        layoutParamsStatusBar.height = context.getStatusBarHeight()
        binding?.statusBarSendGift?.layoutParams = layoutParamsStatusBar
    }

    private fun Toolbar.navClick(click: (View) -> Unit) {
        setNavigationOnClickListener { click(it) }
    }

    companion object {
        const val FRAGMENT_DATA_KEY = "CoffeeLikePromoCodeFragment_FRAGMENT_DATA_KEY"
    }
}
