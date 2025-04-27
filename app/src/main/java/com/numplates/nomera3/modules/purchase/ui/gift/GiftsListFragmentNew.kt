package com.numplates.nomera3.modules.purchase.ui.gift

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.utils.checkAppRedesigned
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentGiftListBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.billing.BillingClientWrapper
import com.numplates.nomera3.modules.purchase.ui.model.GiftCategoryUiModel
import com.numplates.nomera3.modules.purchase.ui.model.GiftItemUiModel
import com.numplates.nomera3.modules.purchase.ui.send.SendGiftFragment
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GIFT_MODEL
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GIFT_MODEL_COFFEE_LIKE_MODE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GIFT_SEND_WHERE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SCROLL_TO_BOTTOM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SEND_BACK
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_DATE_OF_BIRTH
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_NAME
import com.numplates.nomera3.presentation.view.fragments.UserGiftsFragment
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.viewevents.GiftListEvents
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class GiftsListFragmentNew : BaseFragmentNew<FragmentGiftListBinding>() {

    private val viewModel by viewModels<GiftsListViewModel> { App.component.getViewModelFactory() }

    private lateinit var adapterCategory: GiftsCategoryListAdapterNew
    private var userId: Long? = null
    private var userName = ""
    private var userDateOfBirth: Long? = null
    private var scrollToBottom = false
    private var isFromSendBackClick = false
    private var where = AmplitudePropertyWhere.OTHER

    @Inject
    lateinit var billingClientWrapper: BillingClientWrapper

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGiftListBinding
        get() = FragmentGiftListBinding::inflate

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
        viewModel.init()
    }

    private fun initView() {
        binding?.loadingCircle?.visible()
        initToolbar()
    }

    private fun initToolbar() {
        binding?.toolbar?.setNavigationIcon(R.drawable.arrowback)
        binding?.toolbar?.setNavigationOnClickListener {
            act.onBackPressed()
        }

        binding?.tvGiftListTitle?.text =
            if (userId == viewModel.getUserUid()) {
                getString(R.string.gifts_send_gift_to_me)
            } else {
                val name = if (userName.length > UserGiftsFragment.TOOLBAR_TITLE_NAME_LENGTH)
                    "${userName.take(UserGiftsFragment.TOOLBAR_TITLE_NAME_LENGTH)}..."
                else userName
                getString(R.string.gifts_send_gift_to, name)
            }
        val layoutParamsStatusBar = binding?.statusBarGiftList?.layoutParams as AppBarLayout.LayoutParams
        layoutParamsStatusBar.height = context.getStatusBarHeight()
        binding?.statusBarGiftList?.layoutParams = layoutParamsStatusBar
    }

    private fun initRecycler() {
        binding?.rvGifts?.layoutManager = LinearLayoutManager(context)
        adapterCategory = GiftsCategoryListAdapterNew(clickListener = { data, type ->
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
        checkAppRedesigned(
            isRedesigned = {
//                add(
//                    MeeraSendGiftFragment(),
//                    Act.LIGHT_STATUSBAR_NOT_TRANSPARENT,
//                    Arg(ARG_USER_NAME, userName),
//                    Arg(ARG_USER_ID, userId),
//                    Arg(ARG_GIFT_MODEL, gift),
//                    Arg(ARG_SEND_BACK, isFromSendBackClick),
//                    Arg(ARG_USER_DATE_OF_BIRTH, userDateOfBirth),
//                    Arg(ARG_GIFT_SEND_WHERE, where)
//                )
            },
            isNotRedesigned = {
                add(
                    SendGiftFragment(),
                    Act.LIGHT_STATUSBAR,
                    Arg(ARG_USER_NAME, userName),
                    Arg(ARG_USER_ID, userId),
                    Arg(ARG_GIFT_MODEL, gift),
                    Arg(ARG_SEND_BACK, isFromSendBackClick),
                    Arg(ARG_USER_DATE_OF_BIRTH, userDateOfBirth),
                    Arg(ARG_GIFT_SEND_WHERE, where)
                )
            }
        )
    }

    private fun handleCoffeeLikeClick(data: GiftItemUiModel) {
        if (userId == null) userId = viewModel.getUserUid()
        checkAppRedesigned(
            isRedesigned = {
//                add(
//                    MeeraSendGiftFragment(),
//                    Act.LIGHT_STATUSBAR_NOT_TRANSPARENT,
//                    Arg(ARG_USER_NAME, userName),
//                    Arg(ARG_USER_ID, userId),
//                    Arg(ARG_GIFT_MODEL, data),
//                    Arg(ARG_GIFT_MODEL_COFFEE_LIKE_MODE, true),
//                    Arg(ARG_USER_DATE_OF_BIRTH, userDateOfBirth),
//                    Arg(ARG_GIFT_SEND_WHERE, where)
//                )
            },
            isNotRedesigned = {
                add(
                    SendGiftFragment(),
                    Act.LIGHT_STATUSBAR,
                    Arg(ARG_USER_NAME, userName),
                    Arg(ARG_USER_ID, userId),
                    Arg(ARG_GIFT_MODEL, data),
                    Arg(ARG_GIFT_MODEL_COFFEE_LIKE_MODE, true),
                    Arg(ARG_USER_DATE_OF_BIRTH, userDateOfBirth),
                    Arg(ARG_GIFT_SEND_WHERE, where)
                )
            }
        )
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
        adapterCategory.addItems(gifts)
        if (scrollToBottom) binding?.rvGifts?.scrollToPosition(gifts.size - 1)
    }
}
