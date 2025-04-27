package com.numplates.nomera3.modules.purchase.ui.send

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.addSpanBoldRangesClickColored
import com.meera.core.extensions.click
import com.meera.core.extensions.color
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.utils.showCommonError
import com.meera.core.utils.showCommonSuccessMessage
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraSendGiftFragmentBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.billing.BillingClientWrapper
import com.numplates.nomera3.modules.purchase.ui.model.GiftItemUiModel
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GIFT_MODEL
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GIFT_SEND_WHERE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SEND_BACK
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_DATE_OF_BIRTH
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_NAME
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.TYPE_GIFT_HOLIDAY
import com.numplates.nomera3.presentation.view.fragments.UserGiftsFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val EMPTY_PRODUCT = "Empty product"
private const val DELAY_READ_COMMENT = 500L
private const val DELAY_SCROLL_KEYBOARD = 300L

class MeeraSendGiftFragment : MeeraBaseFragment(layout = R.layout.meera_send_gift_fragment) {

    private val viewModel by viewModels<SendGiftViewModel> { App.component.getViewModelFactory() }

    private var gift: GiftItemUiModel? = null
    private var userId: Long? = -1
    private var recepientName = ""
    private var comment: String = ""

    private var isFromSendBackClick = false
    private var where = AmplitudePropertyWhere.OTHER

    private val viewDisposables = CompositeDisposable()

    @Inject
    lateinit var billingClientWrapper: BillingClientWrapper
    private val binding by viewBinding(MeeraSendGiftFragmentBinding::bind)

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
                is SendGiftEvent.CancelledByUser -> {
                    binding?.pbLoadingCircle?.gone()
                    binding?.vSendGift?.isEnabled = true
                }
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
            binding?.vWriteGiftComment?.etInput?.addSpanBoldRangesClickColored(
                rangeList = rangesList,
                color = requireContext().color(R.color.ui_purple),
                onClickListener = {
                }
            )
        }
        binding?.vWriteGiftComment?.click {
            scrollToBottomDelayed()
        }

        binding?.vWriteGiftComment?.etInput?.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) scrollToBottomDelayed()
        }

        viewModel.purchaseEvent
            .flowWithLifecycle(lifecycle)
            .onEach { params ->
                billingClientWrapper.launchBillingFlow(requireActivity(), params)
            }
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
            RxTextView.textChanges(binding?.vWriteGiftComment?.etInput ?: return)
                .debounce(DELAY_READ_COMMENT, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ input ->
                    comment = input.toString()
                }, { e ->
                    Timber.e(e)
                })
        )
    }

    private fun scrollToBottomDelayed() {
        doDelayed(DELAY_SCROLL_KEYBOARD) {
            binding?.nsvContentGift?.requestChildFocus(binding?.vBottom, binding?.vBottom)
        }
    }

    private fun initView() {
        binding?.vSendGift?.setThrottledClickListener {
            viewModel.userId = userId
            viewModel.giftComment = comment

            buyGiftForOtherUser()
            binding?.pbLoadingCircle?.visible()
            binding?.vSendGift?.isEnabled = false
        }

        binding?.ivGiftPicture?.loadGlide(gift?.image)

        binding?.vSendGift?.text = getString(R.string.gifts_send_gift_for_price, gift?.price)

        initToolbar()

        if (gift?.type == TYPE_GIFT_HOLIDAY) {
            binding?.tvTitleGiftMessage?.visible()
            binding?.tvSubtitleGiftMessage?.visible()
            binding?.tvTitleGiftMessage?.text = gift?.customTitle
            binding?.tvSubtitleGiftMessage?.text = gift?.customDesc
        }
        binding?.vAnonimCheckbox?.cellCityText = false
        binding?.vAnonimCheckbox?.let {
            it.setThrottledClickListener {
                it.setCellRightElementChecked(!it.isCheckButton)
                viewModel.isSenderVisible = it.isCheckButton
            }
        }
    }

    private fun initToolbar() {
        binding?.apply {
            vGiftUserNavView.backButtonClickListener = { findNavController().popBackStack() }

            if (userId != viewModel.getUserUid()) {
                val name = if (recepientName.length > UserGiftsFragment.TOOLBAR_TITLE_NAME_LENGTH)
                    "${recepientName.take(UserGiftsFragment.TOOLBAR_TITLE_NAME_LENGTH)}..."
                else recepientName
                vGiftUserNavView.title = getString(R.string.gifts_send_gift_to, name)
            } else {
                vGiftUserNavView.title = getString(R.string.gifts_send_gift_to_me)
            }
        }
    }

    private fun buyGiftForOtherUser() {
        viewModel.purchaseGiftForAnotherUser(gift ?: error(EMPTY_PRODUCT))
    }

    private fun showMarketError() {
        showCommonError(getText(R.string.error_try_later), requireView())
    }

    private fun onSuccessPurchase() {
        showSuccessPurchase()
        findNavController().safeNavigate(
            resId = R.id.action_meeraSendGiftFragment_to_meeraSuccessSentGiftFragment,
            bundle = Bundle().apply{
                putString(ARG_GIFT_IMAGE, gift?.image)
            }
        )
        binding?.pbLoadingCircle?.gone()
        binding?.vSendGift?.isEnabled = true
    }

    private fun showSuccessPurchase() {
        showCommonSuccessMessage(getText(R.string.purchase_successfully_finished), requireView())
    }
}
