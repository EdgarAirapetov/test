package com.numplates.nomera3.modules.purchase.ui.vip

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
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAppearAnimate
import com.meera.core.utils.getAge
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.LOTTIE_LOADER_ANIMATION
import com.numplates.nomera3.LOTTIE_LOADER_SPEED
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues.ACCOUNT_TYPE_PREMIUM
import com.numplates.nomera3.data.network.core.INetworkValues.ACCOUNT_TYPE_VIP
import com.numplates.nomera3.data.network.core.INetworkValues.COLOR_PURPLE
import com.numplates.nomera3.data.network.core.INetworkValues.COLOR_RED
import com.numplates.nomera3.databinding.FragmentUpdateStatusBinding
import com.numplates.nomera3.modules.billing.BillingClientWrapper
import com.numplates.nomera3.modules.purchase.ui.model.SimplePurchaseUiModel
import com.numplates.nomera3.modules.purchase.ui.model.UpgradeStatusUIState
import com.numplates.nomera3.modules.purchase.ui.vip.BottomSheetVipDialog.OnBottomSheetCallback
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.viewevents.UpgradeToVipEvent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class UpdateStatusFragment : BaseFragmentNew<FragmentUpdateStatusBinding>() {

    private val viewModel by viewModels<UpgradeToVipViewModel> { App.component.getViewModelFactory() }
    private var isVipStatus: Boolean? = null

    private var dialog: ProgressDialog? = null

    @Inject
    lateinit var billingClientWrapper: BillingClientWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
        lifecycle.addObserver(billingClientWrapper)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showProgress()
        initView()
        initToolbar()
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

    private fun showError() {
        //NToast.showSnackBar(view, NToast.SNACKBAR_TYPE_ERROR, getString(R.string.error_try_later)) {}
        NToast.with(view)
            .text(getString(R.string.error_try_later))
            .show()
    }


    private fun initToolbar() {
        binding?.statusBarBlUpgradeToVip?.layoutParams?.height = context.getStatusBarHeight()
        binding?.statusBarBlUpgradeToVip?.requestLayout()
    }

    private fun initView() {
        binding?.tvUpdateSubscription?.setOnClickListener {
            replace(
                act.getFragmentsCount() - 1,
                FragmentUpgradeToVipNew(),
                Act.LIGHT_STATUSBAR
            )
        }

        binding?.icBackUpgradeToVip?.setOnClickListener {
            act.onBackPressed()
        }

        binding?.cvBtnUpgrade?.setOnClickListener {
            isVipStatus?.let {
                if (!it) {
                    replace(
                        act.getFragmentsCount() - 1,
                        FragmentUpgradeToVipNew(),
                        Act.LIGHT_STATUSBAR,
                        Arg(IArgContainer.ARG_UPGRADE_TO_VIP, true)
                    )
                } else {
                    showProgressMarket()
                    viewModel.querySubscriptionSkuList(false)
                }
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
        //NToast.showSnackBar(view, NToast.SNACKBAR_TYPE_SUCCESS, "Покупка успешно совершена"){}
        NToast.with(view)
            .text(getString(R.string.update_to_vip_success))
            .typeSuccess()
            .show()
        showProgress()
        viewModel.init(viewModel.getUserUid())
    }

    private fun handleUserProfile(user: UpgradeStatusUIState?) {
        user?.let { profile ->
            binding?.ufcUpdateToVip?.setName(user.name)
            binding?.ufcUpdateToVip?.setGradient(user.accountColor ?: COLOR_RED)
            binding?.ufcUpdateToVip?.setUserAvatar(user.avatar)
            var age = ""
            user.birthday?.let {
                age = getAge(it)
            }

            if (age.isEmpty() || user.hideBirthday == 1) {
                binding?.ufcUpdateToVip?.setAgeCity(user.cityName ?: "")
            } else {
                if (user.cityName.isNullOrEmpty())
                    binding?.ufcUpdateToVip?.setAgeCity(age)
                else {
                    binding?.ufcUpdateToVip?.setAgeCity("$age, ${user.cityName}")
                }
            }
            binding?.ufcUpdateToVip?.setUpNumplate(user.vehicles)
            setUpExpiration(profile)
            setUpTheme(profile)
        }
    }

    private fun setUpExpiration(user: UpgradeStatusUIState) {
        user.accountType?.let { accountType ->
            when (accountType) {
                ACCOUNT_TYPE_VIP -> {
                    user.accountTypeExpiration?.let { date ->
                        setExpirationDate(date)
                    }
                }
                ACCOUNT_TYPE_PREMIUM -> {
                    user.accountTypeExpiration?.let { date ->
                        setExpirationDate(date)
                    }
                }
                else -> {
                    Timber.e("Unknown account type")
                }
            }
        }
    }

    private fun setExpirationDate(expirationDate: Long) {
        val currentDate = Date(System.currentTimeMillis())
        val expiredDate = Date(expirationDate * 1000)
        val expiredDay = Date(daysBetween(currentDate, expiredDate))

        binding?.tvDaysTillEnd?.text = resources.getString(R.string.days_left, expiredDay.time.toString())
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        binding?.tvEndDate?.text = format.format(expiredDate)
    }


    private fun daysBetween(startDate: Date, endDate: Date): Long {
        val sDate = getDatePart(startDate)
        val eDate = getDatePart(endDate)

        var daysBetween: Long = 0
        while (sDate.before(eDate)) {
            sDate.add(Calendar.DAY_OF_MONTH, 1)
            daysBetween++
        }
        return daysBetween
    }

    private fun getDatePart(date: Date): Calendar {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    private fun setUpTheme(user: UpgradeStatusUIState) {
        user.accountType?.let { accountType ->
            when (accountType) {
                ACCOUNT_TYPE_PREMIUM -> {
                    setUpPremium(user)
                }
                ACCOUNT_TYPE_VIP -> {
                    setUpVip()
                }
            }
        }
    }

    private fun setUpVip() {
        isVipStatus = true
        binding?.tvUpgradeToVipHeaderUpdStat?.visible()
        binding?.icBackUpgradeToVip?.setColorFilter(Color.WHITE)
        binding?.tvMainTxtDescUpd?.text = getText(R.string.profile_vip)
        binding?.tvBtnBuyUpdProfile?.text = getString(R.string.upgrade_to_vip5)
        context?.let {
            binding?.clContainerUpdStat?.setBackgroundColor(ContextCompat.getColor(it, R.color.colorUpgradeBlack))
            binding?.blUpgradeToVipUpdStat?.setBackgroundColor(ContextCompat.getColor(it, R.color.colorUpgradeBlack))
            binding?.tvCurrentSubscription?.setTextColor(ContextCompat.getColor(it, R.color.ui_yellow_70))
            binding?.tvMainTxtDescUpd?.setTextColor(ContextCompat.getColor(it, R.color.ui_yellow))
            binding?.tvDescSmall1UpdProf?.setTextColor(ContextCompat.getColor(it, R.color.ui_white))
            binding?.tvDescSmall2UpdProf?.setTextColor(ContextCompat.getColor(it, R.color.ui_white))
            binding?.tvDaysTillEnd?.setTextColor(ContextCompat.getColor(it, R.color.ui_yellow))
            binding?.tvEndDate?.setTextColor(ContextCompat.getColor(it, R.color.ui_yellow))
            binding?.ivImgLogo?.loadGlide(R.drawable.round_gold_crown_with_shadow)
        }
        binding?.tvBtnBuyUpdProfile?.visibleAppearAnimate()
        binding?.goldShadowUpdProfile?.gone()
        binding?.vShadowUpdProfile?.setBackgroundResource(R.drawable.avatar_gradient_bottom)
        act.setColorStatusBar()
        binding?.ufcUpdateToVip?.makeGold()
        binding?.ufcUpdateToVip?.setGradient(COLOR_PURPLE, ACCOUNT_TYPE_VIP)
        binding?.tvUpdateSubscription?.gone()
        binding?.goldShadowUpdProfile?.visibleAppearAnimate()
    }

    private fun setUpPremium(user: UpgradeStatusUIState) {
        isVipStatus = false
        binding?.tvUpgradeToVipHeaderUpdStat?.gone()
        binding?.icBackUpgradeToVip?.setColorFilter(Color.BLACK)
        binding?.tvMainTxtDescUpd?.text = getText(R.string.premium_txt)
        context?.let {
            binding?.clContainerUpdStat?.setBackgroundColor(ContextCompat.getColor(it, R.color.ui_white))
            binding?.blUpgradeToVipUpdStat?.setBackgroundColor(ContextCompat.getColor(it, R.color.ui_white))
            binding?.tvCurrentSubscription?.setTextColor(ContextCompat.getColor(it, R.color.ui_black_70))
            binding?.tvMainTxtDescUpd?.setTextColor(ContextCompat.getColor(it, R.color.ui_black))
            binding?.tvDescSmall1UpdProf?.setTextColor(ContextCompat.getColor(it, R.color.ui_black))
            binding?.tvDescSmall2UpdProf?.setTextColor(ContextCompat.getColor(it, R.color.ui_black))
            binding?.tvDaysTillEnd?.setTextColor(ContextCompat.getColor(it, R.color.ui_black))
            binding?.tvEndDate?.setTextColor(ContextCompat.getColor(it, R.color.ui_black))
            binding?.ivImgLogo?.loadGlide(R.drawable.crown_silver_premium)
        }
        binding?.tvBtnBuyUpdProfile?.visibleAppearAnimate()
        binding?.vShadowUpdProfile?.setBackgroundResource(R.drawable.gradient_bottom_white)
        act.setLightStatusBar()
        binding?.ufcUpdateToVip?.makeVip()
        binding?.ufcUpdateToVip?.setGradient(user.accountColor ?: 0)
        binding?.tvUpdateSubscription?.visible()
        binding?.goldShadowUpdProfile?.gone()
    }

    private fun hideProgress() {
        binding?.pbUpgradeToVipUpdProfile?.gone()
        binding?.cvBtnUpgrade?.visibleAppearAnimate()
        binding?.nsvUpgradeProfileToVip?.visibleAppearAnimate()
    }

    private fun showProgress() {
        binding?.pbUpgradeToVipUpdProfile?.visible()
        binding?.cvBtnUpgrade?.gone()
        binding?.nsvUpgradeProfileToVip?.gone()
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
        viewModel.buyPrivilegeStatus(productId, COLOR_PURPLE)
    }

    private fun showErrorMarket(code: Int?) {
        hideDialog()
        when (code) {
            null -> {
                //NToast.showSnackBar(view, NToast.SNACKBAR_TYPE_ERROR, getString(R.string.error_iab_setup)) {}
                NToast.with(view)
                    .text(getString(R.string.error_iab_setup))
                    .show()
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                NToast.with(view)
                    .text(getString(R.string.operation_cancelled))
                    .show()
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                //NToast.showSnackBar(view, NToast.SNACKBAR_TYPE_ERROR, "Товар уже преобретен") {}
                NToast.with(view)
                    .text(getString(R.string.update_successfully_completed))
                    .show()
            }
            else -> {
                //NToast.showSnackBar(view, NToast.SNACKBAR_TYPE_ERROR, getString(R.string.error_iab_setup)){}
                NToast.with(view)
                    .text(getString(R.string.error_iab_setup))
                    .show()
            }
        }

    }

    private fun showProgressMarket() {
        binding?.cvBtnUpgrade?.isClickable = false
        binding?.tvBtnBuyUpdProfile?.gone()
        binding?.lavLoadingIndicatorUpd?.visible()
        binding?.lavLoadingIndicatorUpd?.setAnimation(LOTTIE_LOADER_ANIMATION)
        binding?.lavLoadingIndicatorUpd?.speed = LOTTIE_LOADER_SPEED
        binding?.lavLoadingIndicatorUpd?.repeatCount = LottieDrawable.INFINITE
        binding?.lavLoadingIndicatorUpd?.playAnimation()
    }

    private fun hideProgressMarket() {
        binding?.cvBtnUpgrade?.isClickable = true
        binding?.lavLoadingIndicatorUpd?.visible()
        binding?.lavLoadingIndicatorUpd?.gone()
        binding?.tvBtnBuyUpdProfile?.visible()
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

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUpdateStatusBinding
        get() = FragmentUpdateStatusBinding::inflate
}
