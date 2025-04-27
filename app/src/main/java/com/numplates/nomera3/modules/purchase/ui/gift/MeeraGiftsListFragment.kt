package com.numplates.nomera3.modules.purchase.ui.gift

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.gone
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.visible
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraGiftListFragmentBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.billing.BillingClientWrapper
import com.numplates.nomera3.modules.purchase.ui.model.GiftCategoryUiModel
import com.numplates.nomera3.modules.purchase.ui.model.GiftItemUiModel
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GIFT_MODEL
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GIFT_MODEL_COFFEE_LIKE_MODE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GIFT_SEND_WHERE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SCROLL_TO_BOTTOM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SEND_BACK
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_DATE_OF_BIRTH
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_NAME
import com.numplates.nomera3.presentation.view.fragments.UserGiftsFragment
import com.numplates.nomera3.presentation.view.fragments.meerasettings.MeeraRateUsFlowController
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.viewevents.GiftListEvents
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class MeeraGiftsListFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_gift_list_fragment,
    ScreenBehaviourState.Full
) {

    private val viewModel by viewModels<GiftsListViewModel> { App.component.getViewModelFactory() }

    private var adapterCategory: MeeraGiftsCategoryListAdapter? = null
    private var userId: Long? = null
    private var userName = ""
    private var userDateOfBirth: Long? = null
    private var scrollToBottom = false
    private var isFromSendBackClick = false
    private var where = AmplitudePropertyWhere.OTHER

    @Inject
    lateinit var billingClientWrapper: BillingClientWrapper
    private val binding by viewBinding(MeeraGiftListFragmentBinding::bind)

    override val containerId: Int
        get() = R.id.fragment_second_container_view

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
        lifecycle.addObserver(billingClientWrapper)
        arguments?.let { args ->
            args.get(ARG_USER_ID)?.let {
                userId = it as Long
            }
            args.get(ARG_USER_NAME)?.let {
                userName = it as String
            }
            args.get(ARG_SCROLL_TO_BOTTOM)?.let {
                scrollToBottom = it as Boolean
            }
            args.get(ARG_SEND_BACK)?.let {
                isFromSendBackClick = it as Boolean
            }
            args.get(ARG_USER_DATE_OF_BIRTH)?.let { dateOfBirth ->
                userDateOfBirth = dateOfBirth as Long
            }
            args.getSerializable(ARG_GIFT_SEND_WHERE)?.let { where ->
                this.where = where as AmplitudePropertyWhere
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initRecycler()
        initObservers()
        initGiftSentListener()
        viewModel.init()
    }

    private fun initView() {
        binding?.loadingCircle?.visible()
        initToolbar()
    }

    private fun initToolbar() {
        binding?.apply {
            vGiftUserNavView.backButtonClickListener = { findNavController().popBackStack() }
            vGiftUserNavView.title = if (userId == viewModel.getUserUid()) {
                getString(R.string.gifts_send_gift_to_me)
            } else {
                val name = if (userName.length > UserGiftsFragment.TOOLBAR_TITLE_NAME_LENGTH)
                    "${userName.take(UserGiftsFragment.TOOLBAR_TITLE_NAME_LENGTH)}..."
                else userName
                getString(R.string.gifts_send_gift_to, name)
            }
        }
    }

    private fun initGiftSentListener() {
        setFragmentResultListener(SHOW_RATE_APP) { _, _ ->
            MeeraRateUsFlowController().startRateUsFlow(childFragmentManager)
        }
    }

    private fun initRecycler() {
        binding?.rvGifts?.layoutManager = LinearLayoutManager(context)
        adapterCategory = MeeraGiftsCategoryListAdapter(clickListener = { data, type ->
            when (type) {
                TYPE_INNER_COFFEE_LIKE -> handleCoffeeLikeClick(data)
                TYPE_INNER_DEF -> handleGiftClick(data)
                TYPE_INNER_HOLIDAY_GIFT -> handleGiftClick(data)
            }
        })
        binding?.rvGifts?.adapter = adapterCategory
    }

    private fun handleGiftClick(gift: GiftItemUiModel) {
        if (userId == null) userId = viewModel.getUserUid()
        userId?.let { usrId ->
            findNavController().safeNavigate(
                resId = R.id.action_meeraGiftsListFragment_to_meeraSendGiftFragment,
                bundle = Bundle().apply {
                    putLong(ARG_USER_ID, usrId)
                    putString(ARG_USER_NAME, userName)
                    putParcelable(ARG_GIFT_MODEL, gift)
                    putBoolean(ARG_SEND_BACK, isFromSendBackClick)
                    putLong(ARG_USER_DATE_OF_BIRTH, userDateOfBirth ?: 0)
                    putSerializable(ARG_GIFT_SEND_WHERE, where)
                }
            )
        }
    }

    private fun handleCoffeeLikeClick(data: GiftItemUiModel) {
        if (userId == null) userId = viewModel.getUserUid()
        userId?.let { usrId ->
            userDateOfBirth?.let { usrDateOfBirth ->
                findNavController().safeNavigate(
                    resId = R.id.action_meeraGiftsListFragment_to_meeraSendGiftFragment,
                    bundle = Bundle().apply {
                        putLong(ARG_USER_ID, usrId)
                        putString(ARG_USER_NAME, userName)
                        putParcelable(ARG_GIFT_MODEL, data)
                        putBoolean(ARG_GIFT_MODEL_COFFEE_LIKE_MODE, true)
                        putLong(ARG_USER_DATE_OF_BIRTH, usrDateOfBirth)
                        putSerializable(ARG_GIFT_SEND_WHERE, where)
                    }
                )
            }
        }
    }

    private fun initObservers() {
        viewModel.liveEvents
            .flowWithLifecycle(lifecycle)
            .onEach { event ->
                when (event) {
                    is GiftListEvents.OnMarketError -> {
                        binding?.loadingCircle?.gone()
                        NToast.with(view)
                            .text(getString(R.string.error_iab_setup))
                            .show()
                    }

                    is GiftListEvents.OnRequestGiftsError -> {
                        binding?.loadingCircle?.gone()
                        NToast.with(view)
                            .text(getString(R.string.error_try_later))
                            .show()
                    }
                }
            }.launchIn(lifecycleScope)

        viewModel.liveGiftList.observe(viewLifecycleOwner) {
            binding?.loadingCircle?.gone()
            handleGiftsList(it)
        }
    }

    private fun handleGiftsList(gifts: List<GiftCategoryUiModel>) {
        adapterCategory?.submitList(gifts)
        if (scrollToBottom) binding?.rvGifts?.scrollToPosition(gifts.size - 1)
    }

    companion object {
        const val SHOW_RATE_APP = "SHOW_RATE_APP"
    }
}
