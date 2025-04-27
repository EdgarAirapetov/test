package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.utils.checkAppRedesigned
import com.meera.db.models.userprofile.GiftEntity
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.websocket.updatePromoCode
import com.numplates.nomera3.databinding.FragmentGiftUserBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudePropertyRatingWhere
import com.numplates.nomera3.modules.gift_coffee.data.entity.CoffeeType
import com.numplates.nomera3.modules.gift_coffee.data.entity.PromoCodeEntity
import com.numplates.nomera3.modules.gift_coffee.ui.coffee_select.CoffeeGiftBottomDialog
import com.numplates.nomera3.modules.gift_coffee.ui.fragment.CoffeeLikePromoCodeFragment
import com.numplates.nomera3.modules.gift_coffee.ui.fragment.CoffeeLikePromoCodeFragment.Companion.FRAGMENT_DATA_KEY
import com.numplates.nomera3.modules.holidays.data.entity.HolidayVisitsEntity
import com.numplates.nomera3.modules.holidays.ui.calendar.HolidayCalendarBottomDialog
import com.numplates.nomera3.modules.holidays.ui.calendar.MeeraHolidayCalendarBottomDialogBuilder
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayVisits
import com.numplates.nomera3.modules.purchase.ui.gift.GiftsListFragmentNew
import com.numplates.nomera3.modules.rateus.presentation.PopUpRateUsDialogFragment
import com.numplates.nomera3.presentation.model.adaptermodel.UserGiftsUiEntity
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GIFT_SEND_WHERE
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.UserGiftsHolidayAdapter
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.UserGiftsListAdapter
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.UserGiftsListAdapterClickListener
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.utils.SwipeToDeleteUtils
import com.numplates.nomera3.presentation.view.widgets.BannerLayoutManager
import com.numplates.nomera3.presentation.viewmodel.UserGiftsViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.UserGiftEvents

class UserGiftsFragment
    : BaseFragmentNew<FragmentGiftUserBinding>(),
        SwipeRefreshLayout.OnRefreshListener,
        CoffeeGiftBottomDialog.CoffeeGiftBottomDialogCallback {

    private val viewModel by viewModels<UserGiftsViewModel>()

    private lateinit var giftListAdapter: UserGiftsListAdapter


    private var userId: Long? = null

    private var isWorthToShowRateDialog = false
    private var isFromPush = false
    private lateinit var userName: String
    private var where = AmplitudePropertyWhere.OTHER

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGiftUserBinding
        get() = FragmentGiftUserBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isWorthToShowRateDialog = arguments?.getBoolean(
            IArgContainer.ARG_IS_WORTH_TO_SHOW_DIALOG, false
        ) ?: false
        userId = arguments?.getLong(IArgContainer.ARG_USER_ID, viewModel.getUserUid())
        userName = arguments?.getString(IArgContainer.ARG_USER_NAME) ?: String.empty()
        isFromPush = arguments?.getBoolean(IArgContainer.ARG_IS_FROM_PUSH, false) ?: false
        arguments?.getSerializable(ARG_GIFT_SEND_WHERE)?.let { where ->
            this.where = where as AmplitudePropertyWhere
        }
        viewModel.dateOfBirth = arguments?.getLong(IArgContainer.ARG_USER_DATE_OF_BIRTH, 0)

        viewModel.setUserId(userId)
        setupToolbar(view)
        setupGiftList(userId)
        setupLiveObservables()
        setupViewActions()
        initGiftSentListener()

        giftListAdapter.longClickListener = { position, gift ->
            if (viewModel.getUserUid() == userId) {
                showDeleteGiftBottomDialog(position, gift?.giftEntity)
            }
        }

        if (isWorthToShowRateDialog) {
            Handler().postDelayed({
                tryToShowRateUsDialog()
            }, 300)
        }
    }


    override fun onCoffeeSelect(gift: GiftEntity, promocode: PromoCodeEntity, type: CoffeeType) {
        val updatedGift = gift.updatePromoCode(promocode.code, type)
        viewModel.updateCoffeeSelected(updatedGift)
        add(CoffeeLikePromoCodeFragment(), Act.LIGHT_STATUSBAR, Arg(FRAGMENT_DATA_KEY, updatedGift))
    }

    private fun openCoffeeLikePromoCode(gift: GiftEntity) {
        add(CoffeeLikePromoCodeFragment(), Act.LIGHT_STATUSBAR, Arg(FRAGMENT_DATA_KEY, gift))
    }

    private fun tryToShowRateUsDialog() {
        if (viewModel.isWorthToShowDialog()) {
            childFragmentManager.let {
                PopUpRateUsDialogFragment.show(
                    manager = it,
                    tag = "rateUsDialog",
                    where = AmplitudePropertyRatingWhere.SETTINGS
                )
            }
        }
    }

    private fun setupToolbar(view: View) {
        val statusBar = view.findViewById<View>(R.id.status_bar_gift_user)
        val params = statusBar.layoutParams as AppBarLayout.LayoutParams
        params.height = context.getStatusBarHeight()
        statusBar.layoutParams = params

        binding?.srlUserGifts?.setOnRefreshListener(this)

        binding?.toolbarGiftUser?.setNavigationIcon(R.drawable.arrowback)
        binding?.toolbarGiftUser?.setNavigationOnClickListener { act.onBackPressed() }
        if (userId != viewModel.getUserUid() && ::userName.isInitialized && userName.isNotEmpty()) {
            binding?.toolbarTitle?.text = getString(R.string.user_gifts) + ": " +
                    if (userName.length > TOOLBAR_TITLE_NAME_LENGTH)
                        "${userName.take(TOOLBAR_TITLE_NAME_LENGTH)}..."
                    else userName
        }
    }

    private fun setupGiftList(userId: Long?) {
        val zeroDataText = if (userId != viewModel.getUserUid())
            getString(R.string.profile_user_gifts_empty)
        else getString(R.string.profile_gifts_empty)

        val layoutManager = BannerLayoutManager(act, RecyclerView.VERTICAL, false)
        binding?.rvGiftUser?.setHasFixedSize(true)
        binding?.rvGiftUser?.layoutManager = layoutManager

        giftListAdapter = UserGiftsListAdapter(
                zeroDataText,
                viewModel.getUserUid(),
                userId,
                adapterClickListener
        )

        giftListAdapter.avatarClickListener = { userIdAvatar ->
            openProfile(userIdAvatar)
        }

        giftListAdapter.sendGiftBackClickListener = { id, name, dateOfBirth ->
            viewModel.amplitudeHelper.logSendGiftBack()
            checkAppRedesigned(
                isRedesigned = {
//                    add(
//                        MeeraGiftsListFragment(),
//                        Act.LIGHT_STATUSBAR_NOT_TRANSPARENT,
//                        Arg(IArgContainer.ARG_USER_ID, id),
//                        Arg(IArgContainer.ARG_USER_NAME, name),
//                        Arg(IArgContainer.ARG_SEND_BACK, true),
//                        Arg(IArgContainer.ARG_USER_DATE_OF_BIRTH, dateOfBirth),
//                        Arg(ARG_GIFT_SEND_WHERE, where)
//                    )
                },
                isNotRedesigned = {
                    add(
                        GiftsListFragmentNew(),
                        Act.LIGHT_STATUSBAR,
                        Arg(IArgContainer.ARG_USER_ID, id),
                        Arg(IArgContainer.ARG_USER_NAME, name),
                        Arg(IArgContainer.ARG_SEND_BACK, true),
                        Arg(IArgContainer.ARG_USER_DATE_OF_BIRTH, dateOfBirth),
                        Arg(ARG_GIFT_SEND_WHERE, where)
                    )
                }
            )
        }

        val holidayVisits = viewModel.getHolidayVisits()

        binding?.rvGiftUser?.adapter = if (holidayVisits != null && userId == viewModel.getUserUid()) {
            binding?.sendMeGiftSeparator?.gone()
            ConcatAdapter(UserGiftsHolidayAdapter().apply {
                setVisits(holidayVisits)
                showMoreButtonClicked = {
                    checkAppRedesigned(
                        isRedesigned = {
                            MeeraHolidayCalendarBottomDialogBuilder()
                                .setVisits(
                                    createHolidayVisits(holidayVisits)
                                )
                                .show(childFragmentManager)
                        },
                        isNotRedesigned = {
                            val dialog = HolidayCalendarBottomDialog().apply {
                                setVisits(
                                    createHolidayVisits(holidayVisits)
                                )
                            }
                            dialog.show(childFragmentManager, dialog.tag)
                        }
                    )
                }
            }, giftListAdapter)
        } else {
            binding?.sendMeGiftSeparator?.visible()
            giftListAdapter
        }
        handleSwipeToDelete()

        if (!isFromPush) viewModel.getUserGifts(userId)
        else viewModel.getGiftsFromPush(userId)
    }

    private fun createHolidayVisits(holidayVisits: Int) =
        HolidayVisits(
            goalDays = HolidayCalendarBottomDialog.DAY_COUNT_7,
            status = if (holidayVisits == HolidayCalendarBottomDialog.DAY_COUNT_7) {
                HolidayVisitsEntity.STATUS_ACHIEVED
            } else {
                HolidayVisitsEntity.STATUS_IN_PROGRESS
            },
            visitDays = holidayVisits.toString()
        )

    private fun openProfile(userId: Long) {
        add(UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
                Arg(IArgContainer.ARG_USER_ID, userId),
                Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.GIFTS.property)
        )
    }

    private val adapterClickListener = object : UserGiftsListAdapterClickListener {

        override fun onClickToCoffeeLikeGift(gift: UserGiftsUiEntity) {
            openCoffeeLikePromoCode(gift.giftEntity)
        }

        override fun onChooseCoffee(gift: UserGiftsUiEntity) {
            CoffeeGiftBottomDialog.show(childFragmentManager, gift.giftEntity)
        }

        override fun onLongClick(position: Int, data: UserGiftsUiEntity?) {
            if (viewModel.getUserUid() == userId) {
                showDeleteGiftBottomDialog(position, data?.giftEntity)
            }
        }

        override fun onBirthdayTextClicked() {
            act?.showFireworkAnimation {
            }
        }
    }

    private fun handleSwipeToDelete() {
        if (viewModel.getUserUid() == userId) {
            ItemTouchHelper(
                    SwipeToDeleteUtils(act, SwipeToDeleteUtils.SwipeType.FULL).apply {
                        onFullSwiped = { position ->
                            val item = giftListAdapter.getItemForPosition(position)
                            showDeleteGiftAlertDialog(position, item.giftEntity, true)
                        }
                        onSwipeProgress = { isSwipeNow ->
                            binding?.srlUserGifts?.isEnabled = !isSwipeNow
                        }
                    }
            ).attachToRecyclerView(binding?.rvGiftUser)
        }
    }

    private fun setupLiveObservables() {
        viewModel.liveGetGifts.observe(viewLifecycleOwner) { gifts ->
            val params = binding?.btSendGift?.layoutParams as FrameLayout.LayoutParams

            if (gifts.isNotEmpty()) {
                binding?.llUserGiftsEmpty?.gone()
            } else {
                binding?.rvGiftUser?.gone()
                binding?.llUserGiftsEmpty?.visible()
            }

            giftListAdapter.submitList(gifts)
            params.gravity = Gravity.BOTTOM
            params.bottomMargin = 20.dp

            if (userId != viewModel.getUserUid()) binding?.btSendGift?.visible()
            binding?.srlUserGifts?.isRefreshing = false
        }

        viewModel.liveEvents.observe(viewLifecycleOwner) {
            when (it) {
                is UserGiftEvents.UserClearAdapterEvent -> giftListAdapter.submitList(emptyList())

                is UserGiftEvents.ErrorRequestEvent ->
                    NToast.with(view)
                            .text(getString(R.string.error_try_later))
                            .show()

                is UserGiftEvents.OwnUserProfileEvent -> {
                    userName = it.userName ?: ""
                    binding?.sendMeGiftLayout?.isClickable = true
                    openGiftListFragmentNew()
                }

                is UserGiftEvents.FailDeleteGift ->
                    handleErrorGiftDelete(it.position, it.shouldRefreshItem)

                is UserGiftEvents.SuccessDeleteGift -> giftListAdapter.notifyItemRemoved(it.position)
                else -> {}
            }
        }
    }

    private fun setupViewActions() {
        if (userId == viewModel.getUserUid()) {
            binding?.sendMeGiftLayout?.click {
                binding?.sendMeGiftLayout?.isClickable = false
                viewModel.requestOwnProfileDao()
            }
            binding?.sendMeGiftLayout?.visible()
        }
        binding?.btSendGift?.click {
            if (userId == viewModel.getUserUid()) {
                binding?.btSendGift?.isClickable = false
                viewModel.requestOwnProfileDao()
            } else {
                openGiftListFragmentNew()
            }
        }
    }

    private fun initGiftSentListener() {
        setFragmentResultListener(KEY_GIFT_SENT) { _, _ ->
            viewModel.refreshGifts()
        }
    }

    private fun openGiftListFragmentNew() {
        checkAppRedesigned(
            isRedesigned = {
//                add(
//                    MeeraGiftsListFragment(),
//                    Act.LIGHT_STATUSBAR_NOT_TRANSPARENT,
//                    Arg(IArgContainer.ARG_USER_ID, userId),
//                    Arg(IArgContainer.ARG_USER_NAME, userName),
//                    Arg(ARG_GIFT_SEND_WHERE, where)
//                )
            },
            isNotRedesigned = {
                add(
                    GiftsListFragmentNew(),
                    Act.LIGHT_STATUSBAR,
                    Arg(IArgContainer.ARG_USER_ID, userId),
                    Arg(IArgContainer.ARG_USER_NAME, userName),
                    Arg(ARG_GIFT_SEND_WHERE, where)
                )
            }
        )
    }

    override fun onRefresh() {
        viewModel.refreshGifts()
    }

    private fun showDeleteGiftAlertDialog(
            position: Int,
            gift: GiftEntity?,
            shouldRefreshItem: Boolean,
    ) {
        if (!isAdded) return
        ConfirmDialogBuilder()
                .setHeader(getString(R.string.gifts_delete_question))
                .setDescription(getString(R.string.gifts_delete_description))
                .setLeftBtnText(getString(R.string.general_cancel))
                .setRightBtnText(getString(R.string.general_delete))
                .setRightClickListener { viewModel.deleteGift(position, gift, shouldRefreshItem) }
                .setLeftClickListener { giftListAdapter.notifyItemChanged(position) }
                .setCancelable(false)
                .show(childFragmentManager)
    }

    private fun showDeleteGiftBottomDialog(position: Int, gift: GiftEntity?) {
        MeeraMenuBottomSheet(requireContext()).apply {
            addItem(R.string.gifts_delete, R.drawable.ic_delete_menu_red) {
                showDeleteGiftAlertDialog(position, gift, false)
            }
        }.show(parentFragmentManager)
    }

    private fun handleErrorGiftDelete(position: Int, shouldRefreshItem: Boolean) {
        if (shouldRefreshItem) giftListAdapter.notifyItemChanged(position)

        NToast.with(act)
                .typeError()
                .text(getString(R.string.gifts_delete_error))
                .show()
    }

    companion object {
        const val TOOLBAR_TITLE_NAME_LENGTH = 16

        const val KEY_GIFT_SENT = "gift sent"
    }
}
