package com.numplates.nomera3.modules.purchase.ui.vip

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieDrawable
import com.android.billingclient.api.BillingClient
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAppearAnimate
import com.meera.core.utils.getAge
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.LOTTIE_LOADER_ANIMATION
import com.numplates.nomera3.LOTTIE_LOADER_SPEED
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.data.network.core.INetworkValues.ACCOUNT_TYPE_VIP
import com.numplates.nomera3.databinding.FragmentUpgradeToVipNewBinding
import com.numplates.nomera3.modules.billing.BillingClientWrapper
import com.numplates.nomera3.modules.purchase.ui.model.SimplePurchaseUiModel
import com.numplates.nomera3.modules.purchase.ui.model.UpgradeStatusUIState
import com.numplates.nomera3.modules.purchase.ui.vip.BottomSheetVipDialog.OnBottomSheetCallback
import com.numplates.nomera3.modules.tracker.ScreenNamesEnum
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.widgets.SwitcherVipGold.Companion.TYPE_GOLD
import com.numplates.nomera3.presentation.view.widgets.SwitcherVipGold.Companion.TYPE_VIP
import com.numplates.nomera3.presentation.viewmodel.viewevents.UpgradeToVipEvent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

class FragmentUpgradeToVipNew : BaseFragmentNew<FragmentUpgradeToVipNewBinding>() {

    private val viewModel by viewModels<UpgradeToVipViewModel> { App.component.getViewModelFactory() }
    private var isPremiumChosen = true
    private var isVipOnly = false
    private var isBuyGold = false

    private var dialog: ProgressDialog? = null

    @Inject
    lateinit var billingClientWrapper: BillingClientWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
        isVipOnly = arguments?.getBoolean(IArgContainer.ARG_UPGRADE_TO_VIP, false) ?: false
        isBuyGold = arguments?.getBoolean(IArgContainer.ARG_UPGRADE_TO_VIP_GOLD, false) ?: false
        lifecycle.addObserver(billingClientWrapper)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showProgress()
        initView()
        initObservables()
        doDelayed(150) {
            val ownUid = viewModel.getUserUid()
            if (ownUid != 0L) {
                viewModel.init(ownUid)
            } else {
                showError()
                act.onBackPressed()
            }
        }
    }

    private fun initObservables() {
        viewModel.liveUserProfile.observe(viewLifecycleOwner) {
            handleUserProfile(it)
            hideProgress()
        }

        viewModel.liveEvent
            .flowWithLifecycle(lifecycle)
            .onEach { event ->
                hideDialog()
                when (event) {
                    is UpgradeToVipEvent.ErrorEvent -> {
                        hideProgressMarket()
                        showError()
                    }
                    is UpgradeToVipEvent.ErrorMarketEvent -> {
                        hideProgressMarket()
                        showErrorMarket(event.message)
                    }
                    is UpgradeToVipEvent.SuccessPurchaseEvent -> {
                        hideProgressMarket()
                        showSuccess()
                    }
                }
            }
            .launchIn(lifecycleScope)

        viewModel.liveProducts.observe(viewLifecycleOwner) {
            hideProgressMarket()
            showBottom(it)
        }

        // Start purchase flow
        viewModel.purchaseEvent
            .flowWithLifecycle(lifecycle)
            .onEach { params -> billingClientWrapper.launchBillingFlow(act, params) }
            .launchIn(lifecycleScope)
    }

    private fun showSuccess() {
//        NToast.showSnackBar(view, NToast.SNACKBAR_TYPE_SUCCESS, "Покупка успешно совершена"){}
        NToast.with(view)
            .text(getString(R.string.update_to_vip_success))
            .typeSuccess()
            .show()
        replace(
            act.getFragmentsCount() - 1,
            UpdateStatusFragment(),
            Act.COLOR_STATUSBAR_BLACK_NAVBAR
        )
    }

    private fun handleUserProfile(state: UpgradeStatusUIState) {
        binding?.ufcUpgradeToVip?.setName(state.name)
        binding?.ufcUpgradeToVip?.setGradient(state.accountColor ?: INetworkValues.COLOR_RED)
        binding?.ufcUpgradeToVip?.setUserAvatar(state.avatar)
        var age = ""
        state.birthday?.let {
            if (state.hideBirthday != 1)
                age = getAge(it)
        }

        if (age.isEmpty() || state.hideBirthday == 1)
            binding?.ufcUpgradeToVip?.setAgeCity(state.cityName ?: "")
        else {
            if (state.cityName.isNullOrEmpty())
                binding?.ufcUpgradeToVip?.setAgeCity(age)
            else {
                binding?.ufcUpgradeToVip?.setAgeCity("$age, ${state.cityName}")
            }
        }
        binding?.ufcUpgradeToVip?.setUpNumplate(state.vehicles)

        if (isVipOnly) {
            setupGold()
            binding?.fdvUpgradeToVip?.gone()
            binding?.svgSwitcher?.gone()
        } else if (isBuyGold) {
            binding?.svgSwitcher?.switch(TYPE_GOLD)
            setupGold()
        }
    }

    private fun initView() {
        initToolbar()
        binding?.svgSwitcher?.clickListener = {
            when (it) {
                TYPE_VIP -> {
                    setupVip()
                }
                TYPE_GOLD -> {
                    setupGold()
                }
            }
        }

        binding?.fdvUpgradeToVip?.setUpCallback {
            binding?.ufcUpgradeToVip?.setGradient(it)
        }

        binding?.icCloseUpgradeToVip?.setOnClickListener {
            act.onBackPressed()
        }

        binding?.btnUpgrToVip?.setOnClickListener {
            showProgressMarket()
            viewModel.querySubscriptionSkuList(isPremiumChosen)
        }

    }

    private fun initToolbar() {
        binding?.statusBarBlUpgradeToVip?.layoutParams?.height = context.getStatusBarHeight()
        binding?.statusBarBlUpgradeToVip?.requestLayout()
    }

    private fun animateWhiteBlack(v: View) {
        context?.let { cntx ->
            val colorFrom = ContextCompat.getColor(cntx, R.color.colorWhite)
            val colorTo = ContextCompat.getColor(cntx, R.color.colorUpgradeBlack)
            val colorAnimation: ValueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
            colorAnimation.duration = 150 // milliseconds
            colorAnimation.addUpdateListener {
                v.setBackgroundColor(it.animatedValue as Int)
            }
            colorAnimation.start()
        }
    }

    private fun animateBlackWhite(v: View) {
        context?.let { cntx ->
            val colorFrom = ContextCompat.getColor(cntx, R.color.colorUpgradeBlack)
            val colorTo = ContextCompat.getColor(cntx, R.color.colorWhite)
            val colorAnimation: ValueAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
            colorAnimation.duration = 150 // milliseconds
            colorAnimation.addUpdateListener {
                v.setBackgroundColor(it.animatedValue as Int)
            }
            colorAnimation.start()
        }
    }

    private fun setupVip() {
        Timber.d("UpgradeToVip: setupVip")
        isPremiumChosen = true
        binding?.clContainer?.let { animateBlackWhite(it) }
        binding?.blUpgradeToVip?.let { animateBlackWhite(it) }
        binding?.tvUpgradeToVipHeader?.setTextColor(Color.BLACK)
        binding?.icCloseUpgradeToVip?.setColorFilter(Color.BLACK)
        binding?.tvMainTxtDesc?.text = getText(R.string.upgrade_choose_your_profile_color)
        context?.let {
            binding?.tvMainTxtDesc?.setTextColor(ContextCompat.getColor(it, R.color.ui_black))
            binding?.tvDescSmall1?.setTextColor(ContextCompat.getColor(it, R.color.ui_black_upgrade_to_vip))
            binding?.tvDescSmall2?.setTextColor(ContextCompat.getColor(it, R.color.ui_black_upgrade_to_vip))
            binding?.tvDescSmall3?.setTextColor(ContextCompat.getColor(it, R.color.ui_black_upgrade_to_vip))
            binding?.btnUpgrToVip?.background = ContextCompat.getDrawable(it, R.drawable.btn_silver_h)
            binding?.tvBtnBuySilver?.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.crown_silver, 0, 0, 0
            )
            binding?.tvBtnBuySilver?.setTextColor(ContextCompat.getColor(it, R.color.ui_white))
            binding?.cvBtnUpgradeToVip?.setCardBackgroundColor(ContextCompat.getColor(it, R.color.colorVipSilverLight))
        }
        binding?.cvBtnUpgradeToVip?.visibleAppearAnimate()
        binding?.fdvUpgradeToVip?.visible()
        binding?.goldShadow?.gone()
        binding?.vShadowUpdToVip?.setBackgroundResource(R.drawable.gradient_bottom_white)
        //        bottom_black_background.gone()
        //        bottom_black_gradient_background.gone()
        binding?.tvDescSmall1?.text = getText(R.string.upgrade_to_prem1)
        binding?.tvDescSmall2?.text = getText(R.string.upgrade_to_prem2)
        binding?.tvDescSmall3?.text = getText(R.string.upgrade_to_prem3)
        act.setLightStatusBar()
        binding?.ufcUpgradeToVip?.makeVip()
        binding?.ufcUpgradeToVip?.setGradient(binding?.fdvUpgradeToVip?.activeColor ?: INetworkValues.COLOR_RED)
        binding?.ivCheckUpgradeToVip?.gone()
        viewModel.logScreenForFragment(ScreenNamesEnum.VIP_SILVER)
    }

    private fun setupGold() {
        Timber.d("UpgradeToVip: setupGold")
        isPremiumChosen = false
        binding?.blUpgradeToVip?.let { animateWhiteBlack(it) }
        binding?.tvUpgradeToVipHeader?.setTextColor(Color.WHITE)
        binding?.icCloseUpgradeToVip?.setColorFilter(Color.WHITE)
        binding?.tvMainTxtDesc?.text = getText(R.string.upd_to_gold_txt_main)
        context?.let {
            binding?.tvMainTxtDesc?.setTextColor(ContextCompat.getColor(it, R.color.ui_yellow))
            binding?.tvDescSmall1?.setTextColor(ContextCompat.getColor(it, R.color.ui_white))
            binding?.tvDescSmall2?.setTextColor(ContextCompat.getColor(it, R.color.ui_white))
            binding?.tvDescSmall3?.setTextColor(ContextCompat.getColor(it, R.color.ui_white))
            binding?.btnUpgrToVip?.background = ContextCompat.getDrawable(it, R.drawable.btn)
            binding?.tvBtnBuySilver?.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.crown_golden, 0, 0, 0
            )
            binding?.tvBtnBuySilver?.setTextColor(ContextCompat.getColor(it, R.color.ui_black))
            binding?.cvBtnUpgradeToVip?.setCardBackgroundColor(ContextCompat.getColor(it, R.color.ui_yellow))
        }
        binding?.cvBtnUpgradeToVip?.visibleAppearAnimate()
        binding?.fdvUpgradeToVip?.gone()
        binding?.goldShadow?.visibleAppearAnimate()
        binding?.vShadowUpdToVip?.setBackgroundColor(Color.BLACK)
        binding?.clContainer?.setBackgroundResource(R.drawable.upgrade_to_vip_gradient_bottom)

        binding?.tvDescSmall1?.text = getText(R.string.upgrade_to_vip1)
        binding?.tvDescSmall2?.text = getText(R.string.upgrade_to_vip2)
        binding?.tvDescSmall3?.text = getText(R.string.upgrade_to_vip3)
        act.setColorStatusBar()
        binding?.ufcUpgradeToVip?.makeGold()
        binding?.ufcUpgradeToVip?.setGradient(INetworkValues.COLOR_PURPLE, ACCOUNT_TYPE_VIP)
        binding?.ivCheckUpgradeToVip?.visible()

        if (!isVipOnly) viewModel.logScreenForFragment(ScreenNamesEnum.VIP_GOLD)
    }

    private fun showError() {
        hideDialog()
        //NToast.showSnackBar(view, NToast.SNACKBAR_TYPE_ERROR, getString(R.string.error_try_later)){}
        NToast.with(view)
            .text(getString(R.string.error_try_later))
            .show()
    }

    private fun showErrorMarket(code: Int?) {
        hideDialog()
        when (code) {
            null -> {
                NToast.with(view)
                    .text(getString(R.string.error_iab_setup))
                    .show()
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                NToast.with(view)
                    .text(getString(R.string.operation_cancelled))
                    .show()
            }
            else -> {
                NToast.with(view)
                    .text(getString(R.string.error_iab_setup))
                    .show()
            }
        }
    }

    private fun showProgress() {
        binding?.pbUpgradeToVip?.visible()
        binding?.cvBtnUpgradeToVip?.gone()
        binding?.nsvUpgradeToVip?.gone()
    }

    private fun hideProgress() {
        //btn_upgr_to_vip.isClickable = true
        binding?.pbUpgradeToVip?.gone()
        binding?.cvBtnUpgradeToVip?.visible()
        binding?.nsvUpgradeToVip?.visible()
    }

    private fun showProgressMarket() {
        //btn_upgr_to_vip.isClickable = false
        binding?.tvBtnBuySilver?.gone()
        binding?.lavLoadingIndicator?.visible()
        binding?.lavLoadingIndicator?.setAnimation(LOTTIE_LOADER_ANIMATION)
        binding?.lavLoadingIndicator?.speed = LOTTIE_LOADER_SPEED
        binding?.lavLoadingIndicator?.repeatCount = LottieDrawable.INFINITE
        binding?.lavLoadingIndicator?.playAnimation()
    }

    private fun hideProgressMarket() {
        binding?.tvBtnBuySilver?.visible()
        binding?.lavLoadingIndicator?.gone()

    }

    private fun showBottom(purchases: List<SimplePurchaseUiModel>) {
        val simplePurchases = purchases.map { model ->
            model.copy(description = "${model.description} ${getString(R.string.upgrade_for_price)}")
        }
        val bottomDialog = BottomSheetVipDialog()
        bottomDialog.show(act.supportFragmentManager, "BottomDialog")
        bottomDialog.setCallback(object : OnBottomSheetCallback {
            override fun onDialogShow() {
                bottomDialog.renderViews(simplePurchases)
            }

            override fun onPurchaseVip(marketId: String) {
                bottomDialog.dismiss()
                showDialog()
                buyPrivilegeStatus(marketId)
            }
        })
    }

    private fun buyPrivilegeStatus(productId: String) {
        viewModel.buyPrivilegeStatus(productId, binding?.fdvUpgradeToVip?.activeColor)
    }

    private fun showDialog() {
        dialog = ProgressDialog(context)
        dialog?.setMessage(getString(R.string.please_wait))
        dialog?.setCancelable(false)
        dialog?.show()
    }

    private fun hideDialog() {
        dialog?.dismiss()
    }

    override fun onStartFragment() {
        if (!isVipOnly) {
            if (isPremiumChosen) viewModel.logScreenForFragment(ScreenNamesEnum.VIP_SILVER)
            else viewModel.logScreenForFragment(ScreenNamesEnum.VIP_GOLD)
        } else {
            viewModel.logScreenForFragment(ScreenNamesEnum.ENHANCE_TO_VIP)
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUpgradeToVipNewBinding
        get() = FragmentUpgradeToVipNewBinding::inflate
}
