package com.numplates.nomera3.modules.purchase.ui.send

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieDrawable
import com.google.android.material.appbar.AppBarLayout
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.extensions.addSpanBoldRangesClickColored
import com.meera.core.extensions.click
import com.meera.core.extensions.color
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.string
import com.meera.core.extensions.visible
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.LOTTIE_LOADER_ANIMATION
import com.numplates.nomera3.LOTTIE_LOADER_SPEED
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentSendGiftBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.billing.BillingClientWrapper
import com.numplates.nomera3.modules.gift_coffee.ui.fragment.GiftListPlacesFragment
import com.numplates.nomera3.modules.purchase.ui.model.GiftItemUiModel
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GIFT_MODEL
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GIFT_MODEL_COFFEE_LIKE_MODE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GIFT_SEND_WHERE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GO_BACK_TWICE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SEND_BACK
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_DATE_OF_BIRTH
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_NAME
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.TYPE_GIFT_HOLIDAY
import com.numplates.nomera3.presentation.view.fragments.UserGiftsFragment
import com.numplates.nomera3.presentation.view.utils.NToast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val DELAY_CHECKING_MESSAGE = 200L

class SendGiftFragment : BaseFragmentNew<FragmentSendGiftBinding>() {

    private val viewModel by viewModels<SendGiftViewModel> { App.component.getViewModelFactory() }

    private var gift: GiftItemUiModel? = null
    private var userId: Long? = -1
    private var recepientName = ""
    private var comment: String = ""

    private var isCoffeeLikeMode = false
    private var isFromSendBackClick = false
    private var where = AmplitudePropertyWhere.OTHER

    private var dialog: ProgressDialog? = null

    private val viewDisposables = CompositeDisposable()

    //переходим ли на 2 экрана назад после совершения покупок, иначе переходим на 1
    private var needToGoBackTwice = true

    @Inject
    lateinit var billingClientWrapper: BillingClientWrapper

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSendGiftBinding
        get() = FragmentSendGiftBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
        lifecycle.addObserver(billingClientWrapper)
        arguments?.let { args ->
            args.get(ARG_GIFT_MODEL)?.let {
                gift = it as GiftItemUiModel
            }

            args.get(ARG_USER_ID)?.let {
                userId = it as Long
            }

            args.get(ARG_USER_NAME)?.let {
                recepientName = it as String
            }

            args.get(ARG_GIFT_MODEL_COFFEE_LIKE_MODE)?.let {
                isCoffeeLikeMode = it as Boolean
            }

            args.getBoolean(ARG_GO_BACK_TWICE, true).let {
                needToGoBackTwice = it
            }

            args.get(ARG_SEND_BACK)?.let {
                isFromSendBackClick = it as Boolean
            }
            args.get(ARG_USER_DATE_OF_BIRTH)?.let { dateOfBirth ->
                Timber.d("Gift dateOfBirth: $dateOfBirth")
                viewModel.dateOfBirth = dateOfBirth as? Long
            }
            args.getSerializable(ARG_GIFT_SEND_WHERE)?.let { where ->
                this.where = where as AmplitudePropertyWhere
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideLottieAnimation()
        initView()
        initObservers()
        viewModel.appSettings.isHolidayShowNeeded = true
        viewModel.isFromSendBackClick = isFromSendBackClick
        viewModel.where = where
    }

    private fun initObservers() {
        viewModel.events.observe(viewLifecycleOwner) {
            when (it) {
                is SendGiftEvent.GiftError -> showMarketError()
                is SendGiftEvent.GiftSuccess -> onSuccessPurchase()
                is SendGiftEvent.CancelledByUser -> handleCancelledError()
            }
        }
        viewModel.giftViewEvent.observe(viewLifecycleOwner) {
            when (it) {
                is GiftViewEvent.OnSkuDetailsLoaded -> {
                    gift = it.giftItem
                    initView()
                }
            }
        }
        viewModel.birthdayRangesLiveData.observe(viewLifecycleOwner) { rangesList ->
            binding?.etWriteGiftComment?.addSpanBoldRangesClickColored(
                rangeList = rangesList,
                color = requireContext().color(R.color.ui_purple),
                onClickListener = {
                    act?.showFireworkAnimation {}
                }
            )
        }
        binding?.etWriteGiftComment?.click {
            scrollToBottomDelayed()
        }

        binding?.etWriteGiftComment?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) scrollToBottomDelayed()
        }

        // Start purchase flow
        viewModel.purchaseEvent
            .flowWithLifecycle(lifecycle)
            .onEach { params -> billingClientWrapper.launchBillingFlow(act, params) }
            .launchIn(lifecycleScope)
    }

    override fun onStart() {
        super.onStart()
        createTextWatcher()
    }

    override fun onStop() {
        super.onStop()
        viewDisposables.clear()
    }

    private fun createTextWatcher() {
        viewDisposables.add(
            RxTextView.textChanges(binding?.etWriteGiftComment ?: return)
                .debounce(DELAY_CHECKING_MESSAGE, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ input ->
                    comment = input.toString()
                    // TODO: 01.07.2022 BR-14727 Пока не планируется идти распознавание в подарках.
                    //  Вернуть после релиза 4-48
                    //viewModel.setGiftTextChanged(input.toString())
                }, { e ->
                    Timber.e(e)
                })
        )
    }

    private fun scrollToBottomDelayed() {
        doDelayed(300) {
            binding?.nsvContentGift
                ?.smoothScrollTo(
                    0,
                    kotlin.math.abs(binding?.nsvContentGift?.bottom ?: 0)
                )
        }
    }

    private fun initView() {
        binding?.cvSendGift?.click {
            viewModel.userId = userId
            viewModel.giftComment = comment

            buyGiftForOtherUser()
        }

        if (isCoffeeLikeMode) {
            binding?.ivPicture?.layoutParams?.width = 290.dp
            binding?.ivPicture?.layoutParams?.height = 290.dp

            binding?.clCoffeeOfChoice?.visible()
            binding?.tvWhereCanGetCoffee?.visible()
            binding?.flWhereCanGetCoffe?.visible()
        } else {
            binding?.clCoffeeOfChoice?.gone()
            binding?.tvWhereCanGetCoffee?.gone()
            binding?.flWhereCanGetCoffe?.gone()
        }

        binding?.ivPicture?.loadGlide(gift?.image)

        binding?.tvSend?.text = getString(R.string.gifts_send_gift_for_price, gift?.price)

        setupToolbar()

        if (gift?.type == TYPE_GIFT_HOLIDAY) {
            binding?.llHolidayGiftData?.visible()
            binding?.tvHolidayGiftTitle?.text = gift?.customTitle
            binding?.tvHolidayGiftDesc?.text = gift?.customDesc
        }

        binding?.cbVisibility?.setOnCheckedChangeListener { _, isChecked ->
            viewModel.isSenderVisible = !isChecked
        }

        binding?.tvWhereCanGetCoffee?.click {
            add(GiftListPlacesFragment(), Act.LIGHT_STATUSBAR)
        }
    }

    private fun setupToolbar() {
        binding?.toolbar?.setNavigationIcon(R.drawable.arrowback)

        binding?.toolbar?.setNavigationOnClickListener {
            act.onBackPressed()
        }
        when {
            !isCoffeeLikeMode -> setToolbarTitle()
            else -> binding?.tvSendGiftToolbar?.text = context?.string(R.string.gift) ?: getString(R.string.gift)
        }
        val layoutParamsStatusBar = binding?.statusBarSendGift?.layoutParams as AppBarLayout.LayoutParams
        layoutParamsStatusBar.height = context.getStatusBarHeight()
        binding?.statusBarSendGift?.layoutParams = layoutParamsStatusBar
    }

    private fun setToolbarTitle() {
        if (userId != viewModel.getUserUid()) {
            val name = if (recepientName.length > UserGiftsFragment.TOOLBAR_TITLE_NAME_LENGTH)
                "${recepientName.take(UserGiftsFragment.TOOLBAR_TITLE_NAME_LENGTH)}..."
            else recepientName
            binding?.tvSendGiftToolbar?.text = getString(R.string.gifts_send_gift_to, name)
        } else {
            binding?.tvSendGiftToolbar?.text = getString(R.string.gifts_send_gift_to_me)
        }
    }

    private fun buyGiftForOtherUser() {
        showLottieAnimation()
        showDialog()
        viewModel.purchaseGiftForAnotherUser(gift ?: error("Empty product"))
    }

    private fun handleCancelledError() {
        hideLottieAnimation()
        hideDialog()
    }

    private fun showMarketError() {
        hideLottieAnimation()
        hideDialog()
        NToast.with(view)
            .text(getString(R.string.error_try_later))
            .show()
    }

    private fun onSuccessPurchase() {
        setFragmentResult(UserGiftsFragment.KEY_GIFT_SENT, bundleOf())
        hideLottieAnimation()
        hideDialog()
        showSuccessPurchase()
        act?.onBackPressed()
        if (needToGoBackTwice) act?.onBackPressed()
    }

    private fun showSuccessPurchase() {
        val text = try {
            getString(R.string.purchase_successfully_finished)
        } catch (e: Exception) {
            e.printStackTrace()
            String.empty()
        }
        NToast.with(view)
            .text(text)
            .typeSuccess()
            .show()
    }

    private fun showLottieAnimation() {
        binding?.lavProgressPurchase?.visible()
        binding?.tvSend?.gone()
        binding?.lavProgressPurchase?.setAnimation(LOTTIE_LOADER_ANIMATION)
        binding?.lavProgressPurchase?.speed = LOTTIE_LOADER_SPEED
        binding?.lavProgressPurchase?.repeatCount = LottieDrawable.INFINITE
        binding?.lavProgressPurchase?.playAnimation()
    }

    private fun hideLottieAnimation() {
        binding?.lavProgressPurchase?.gone()
        binding?.tvSend?.post {
            binding?.tvSend?.visible()
        }
    }

    override fun onStopFragment() {
        super.onStopFragment()
        hideDialog()
    }

    private fun showDialog() {
        dialog = ProgressDialog(context)
        dialog?.setMessage(getString(R.string.please_wait))
        dialog?.setCancelable(true)
        dialog?.show()
    }

    private fun hideDialog() {
        dialog?.dismiss()
    }
}
