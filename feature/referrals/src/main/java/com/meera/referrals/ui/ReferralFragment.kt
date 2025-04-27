package com.meera.referrals.ui

import android.animation.AnimatorInflater
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meera.core.base.BaseFragment
import com.meera.core.base.BaseInfoMessagesDelegate
import com.meera.core.base.BaseInfoMessagesDelegateImpl
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.common.ACCOUNT_TYPE_PREMIUM
import com.meera.core.common.ACCOUNT_TYPE_REGULAR
import com.meera.core.common.ACCOUNT_TYPE_VIP
import com.meera.core.di.CoreComponentProvider
import com.meera.core.extensions.applyRoundedOutline
import com.meera.core.extensions.click
import com.meera.core.extensions.copyToClipBoard
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.textColor
import com.meera.core.extensions.visible
import com.meera.core.utils.convertUnixDate
import com.meera.core.utils.getDateDDMMYYYY
import com.meera.referrals.R
import com.meera.referrals.databinding.FragmentReferralFriendsBinding
import com.meera.referrals.di.DaggerReferralFeatureComponent
import com.meera.referrals.ui.model.ReferralDataUIModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private const val TYPE_TEXT_PLAIN = "text/plain"
private const val FONT_SANS_PRO_BOLD = "fonts/source_sanspro_bold"

class ReferralFragment : BaseFragment(R.layout.fragment_referral_friends),
    BaseInfoMessagesDelegate by BaseInfoMessagesDelegateImpl() {

    private val binding by viewBinding(FragmentReferralFriendsBinding::bind)

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private val viewModel by viewModels<ReferralViewModel> { factory }

    private var accountType = 0

    private var vipUntilDate = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDaggerInjection()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        applyTopInsets()
        initLiveObservables()
    }

    private fun initDaggerInjection() {
        DaggerReferralFeatureComponent
            .factory()
            .create((activity?.application as CoreComponentProvider).getCoreComponent())
            .inject(this)
    }

    private fun setupToolbar() {
        with(binding) {
            ivReferralBack.setThrottledClickListener { requireActivity().onBackPressed() }
        }
    }

    private fun applyTopInsets() {
        val layoutParams =
            binding.tbReferralFriends.layoutParams as AppBarLayout.LayoutParams
        layoutParams.height = layoutParams.height + context.getStatusBarHeight()
        binding.tbReferralFriends.layoutParams = layoutParams
    }

    private fun initLiveObservables() {
        viewModel.liveReferralDataViewEvent.observe(viewLifecycleOwner, Observer { viewEvent ->
            handleViewEvents(viewEvent)
        })
    }

    private fun handleViewEvents(event: ReferralViewEvent) {
        when (event) {
            is ReferralViewEvent.OnGetAccountType -> this.accountType = event.type
            is ReferralViewEvent.OnSuccessGetReferralData -> {
                renderScreen(event.refData)
            }
            is ReferralViewEvent.OnShareProfile -> {
                val shareRefText = getString(
                    R.string.referral_share_code_text,
                    binding.tvReferralInviteCode.text.toString(),
                    event.shareUrl
                )
                shareReferralCode(shareRefText)
            }
            is ReferralViewEvent.OnFailGetReferralData -> {
                binding.pbLoading.gone()
                networkFailMessage()
            }
            is ReferralViewEvent.OnSuccessGetVip -> {
                hideGetVipButton()
                successGetVipMessage()
                viewModel.getReferral(
                    isVipRequest = true,
                    date = getDateDDMMYYYY(unixTime = vipUntilDate)
                )
            }
            is ReferralViewEvent.OnFailGetVip -> failGetVipMessage()
            else -> {}
        }
    }

    private fun renderScreen(data: ReferralDataUIModel) {
        binding.apply {
            pbLoading.gone()
            vgReferralInviteView.visible()
            tvReferralInviteTitle.text = data.title
            tvReferralInviteDescription.text = data.text
            tvReferralInviteCode.text = data.code
            btnShare.applyRoundedOutline(R.dimen.material8)
            handleReferralProgress(data)
            initClickListeners()
        }
    }

    private fun handleReferralProgress(data: ReferralDataUIModel) {
        binding.apply {
            if (data.availableVips > 0) { // limit == count
                showGetVipButton(data)
            } else {
                showPointsView(data)
            }
        }
    }

    private fun FragmentReferralFriendsBinding.showGetVipButton(data: ReferralDataUIModel) {
        cvReferralCounter.gone()
        cvReferralActivateVip.visible()
        cvReferralActivateVip.stateListAnimator = AnimatorInflater.loadStateListAnimator(
            requireContext(),
            R.animator.shadow_animator
        )
        cvReferralActivateVip.setMonths(data.availableVips)
        cvReferralActivateVip.applyRoundedOutline(R.dimen.material8)
        cvReferralActivateVip.click {
            when (accountType) {
                ACCOUNT_TYPE_REGULAR -> {
                    showAccountRegularDialog(data)
                }
                ACCOUNT_TYPE_PREMIUM -> {
                    showAccountPremiumDialog(data)
                }
                ACCOUNT_TYPE_VIP -> {
                    showDialogGetVipIfAlreadyVipExists(data.availableVips)
                }
            }
        }
    }

    private fun showAccountPremiumDialog(data: ReferralDataUIModel) {
        calculateVipUntilDate(data.availableVips)
        showDialogGetVip(
            getString(R.string.referral_get_vip_dialog_title),
            getString(
                R.string.referral_get_vip_dialog_description_premium,
                convertUnixDate(vipUntilDate)
            )
        )
    }

    private fun showAccountRegularDialog(data: ReferralDataUIModel) {
        calculateVipUntilDate(data.availableVips)
        showDialogGetVip(
            getString(R.string.referral_get_vip_dialog_title),
            getString(
                R.string.referral_get_vip_dialog_description_regular,
                convertUnixDate(vipUntilDate)
            )
        )
    }

    /**
     * current date + available vips
     */
    private fun calculateVipUntilDate(availableVips: Int) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.MONTH, availableVips)
        this.vipUntilDate = calendar.timeInMillis / 1000
    }

    /**
     * Current time VIP expiration + available vips months
     */
    private fun showDialogGetVipIfAlreadyVipExists(availableVips: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
            val userAccountTypeExpiration = viewModel.getUserAccountTypeExpiration()
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = userAccountTypeExpiration.times(1000)
            calendar.add(Calendar.MONTH, availableVips)
            vipUntilDate = calendar.timeInMillis / 1000
            showDialogGetVip(
                getString(R.string.referral_get_vip_dialog_title),
                getString(
                    R.string.referral_get_vip_dialog_description_premium,
                    convertUnixDate(vipUntilDate)
                )
            )
        }
    }

    private fun showDialogGetVip(title: String, description: String) {
        val dialog = MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(title)
            setMessage(description)
            setCancelable(false)
            setPositiveButton(R.string.general_activate) { dialog, _ ->
                viewModel.getVipReferral()
                dialog.cancel()
            }
            setNegativeButton(R.string.general_cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }.create()
        val message = dialog.findViewById<TextView>(android.R.id.message)
        message?.textColor(ContextCompat.getColor(requireContext(), R.color.colorGrayA7A5))
        message?.typeface = Typeface.createFromAsset(requireContext().assets, FONT_SANS_PRO_BOLD)
        dialog.show()
    }

    private fun FragmentReferralFriendsBinding.showPointsView(data: ReferralDataUIModel) {
        cvReferralActivateVip.gone()
        cvReferralCounter.visible()
        cvReferralCounter.applyRoundedOutline(R.dimen.material8)
        cvReferralCounter.setData(data)
    }


    private fun initClickListeners() {
        binding.btnShare.click {
            viewModel.onSharedClicked()
        }

        binding.tvReferralInviteCode.click {
            viewModel.onShareCodeCopied()
            requireContext().copyToClipBoard(
                text = binding.tvReferralInviteCode.text.toString(),
                success = { showSuccessCopyCodeMessage() }
            )
        }
    }

    private fun shareReferralCode(text: String) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = TYPE_TEXT_PLAIN
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun hideGetVipButton() = binding.cvReferralActivateVip.gone()

    private fun showSuccessCopyCodeMessage() {
        showSuccessMessage(view, R.string.referral_code_copy)
    }

    private fun successGetVipMessage() {
        showSuccessMessage(view, getString(R.string.referral_vip_activated_until, convertUnixDate(vipUntilDate)))
    }

    private fun networkFailMessage() {
        showErrorMessage(view, R.string.error_try_later)
    }

    private fun failGetVipMessage() {
        showErrorMessage(view, R.string.referral_vip_activated_fail)
    }

}
