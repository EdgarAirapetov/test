package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.utils.checkAppRedesigned
import com.meera.core.utils.showCommonError
import com.meera.db.models.userprofile.GiftEntity
import com.meera.uikit.widgets.buttons.ButtonType
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraGiftUserFragmentBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rating.AmplitudePropertyRatingWhere
import com.numplates.nomera3.modules.holidays.data.entity.HolidayVisitsEntity
import com.numplates.nomera3.modules.holidays.ui.calendar.HolidayCalendarBottomDialog
import com.numplates.nomera3.modules.holidays.ui.calendar.MeeraHolidayCalendarBottomDialogBuilder
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayVisits
import com.numplates.nomera3.modules.rateus.presentation.PopUpRateUsDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.model.adaptermodel.UserGiftsUiEntity
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GIFT_SEND_WHERE
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.MeeraConfirmDialogUserDeleteGift
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.MeeraUserGiftsListAdapter
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.MeeraUserGiftsListAdapterClickListener
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.UserGiftsHolidayAdapter
import com.numplates.nomera3.presentation.view.utils.SwipeToDeleteUtils
import com.numplates.nomera3.presentation.viewmodel.UserGiftsViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.UserGiftEvents
import timber.log.Timber

/**
 * Example transit to fragment
 *             findNavController().safeNavigate(
 *                 resId = R.id.meeraGiftsListFragment,
 *                 bundle = Bundle().apply {
 *                     putLong(ARG_USER_ID, it)
 *                     putString(IArgContainer.ARG_USER_NAME, userName)
 *                     putBoolean(IArgContainer.ARG_SCROLL_TO_BOTTOM, true)
 *                     putLong(IArgContainer.ARG_USER_DATE_OF_BIRTH, dateOfBirth)
 *                     putSerializable(IArgContainer.ARG_GIFT_SEND_WHERE, AmplitudePropertyWhere.NOTIFICATION)
 *                 },
 *                 navBuilder = {
 *                     it.addAnimationTransitionByDefault()
 *                     it
 *                 }
 *             )
 */
class MeeraUserGiftsFragment :
    MeeraBaseDialogFragment(layout = R.layout.meera_gift_user_fragment, ScreenBehaviourState.Full),
    SwipeRefreshLayout.OnRefreshListener {

    private val viewModel by viewModels<UserGiftsViewModel>()

    private var giftListAdapter: MeeraUserGiftsListAdapter? = null

    private var userId: Long? = null

    private var isWorthToShowRateDialog = false
    private var isFromPush = false
    private var userName: String? = null
    private var where = AmplitudePropertyWhere.OTHER

    private val binding by viewBinding(MeeraGiftUserFragmentBinding::bind)

    override val containerId: Int
        get() = R.id.fragment_second_container_view

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
        initToolbar()
        setupGiftList(userId)
        setupLiveObservables()
        setupViewActions()
        initGiftSentListener()

        giftListAdapter?.longClickListener = { position, gift ->
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

    override fun onRefresh() {
        viewModel.refreshGifts()
    }

    private fun initToolbar() {
        binding?.apply {
            rvGiftUserList.let { recycler -> binding?.vGiftUserNavView?.addScrollableView(recycler) }
            vGiftUserNavView.backButtonClickListener = { findNavController().popBackStack() }
        }
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

    private fun setupGiftList(userId: Long?) {

        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding?.rvGiftUserList?.setHasFixedSize(true)
        binding?.rvGiftUserList?.layoutManager = layoutManager

        giftListAdapter = MeeraUserGiftsListAdapter(
            viewModel.getUserUid(),
            userId,
            adapterClickListener
        )

        giftListAdapter?.avatarClickListener = { userIdAvatar ->
            openProfile(userIdAvatar)
        }

        giftListAdapter?.sendGiftBackClickListener = { id, name, dateOfBirth ->
            viewModel.amplitudeHelper.logSendGiftBack()
            id?.let {
                findNavController().safeNavigate(
                    resId = R.id.action_meeraUserGiftsFragment_to_meeraGiftsListFragment,
                    bundle = Bundle().apply {
                        putLong(IArgContainer.ARG_USER_ID, id)
                        putString(IArgContainer.ARG_USER_NAME, name)
                        putBoolean(IArgContainer.ARG_SEND_BACK, true)
                        putLong(IArgContainer.ARG_USER_DATE_OF_BIRTH, dateOfBirth)
                        putSerializable(ARG_GIFT_SEND_WHERE, where)
                    }
                )
            }
        }

        val holidayVisits = viewModel.getHolidayVisits()

        binding?.rvGiftUserList?.adapter = if (holidayVisits != null && userId == viewModel.getUserUid()) {
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
        Timber.i("Open profile click userId = $userId")
//        add(
//            UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
//            Arg(IArgContainer.ARG_USER_ID, userId),
//            Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.GIFTS.property)
//        )
    }

    private val adapterClickListener = object : MeeraUserGiftsListAdapterClickListener {

        override fun onLongClick(position: Int, data: UserGiftsUiEntity?) {
            if (viewModel.getUserUid() == userId) {
                showDeleteGiftBottomDialog(position, data?.giftEntity)
            }
        }

        override fun onBirthdayTextClicked() {
            Timber.i("Birthday Text Clicked")
        }
    }

    private fun handleSwipeToDelete() {
        if (viewModel.getUserUid() == userId) {
            ItemTouchHelper(
                SwipeToDeleteUtils(requireContext(), SwipeToDeleteUtils.SwipeType.FULL).apply {
                    giftListAdapter?.let { meeraUserGiftListAdapter ->
                        onFullSwiped = { position ->
                            val item = meeraUserGiftListAdapter.currentList[position]
                            showDeleteGiftAlertDialog(position, item.giftEntity, true)
                        }
                    }
                }
            ).attachToRecyclerView(binding?.rvGiftUserList)
        }
    }

    private fun setupLiveObservables() {
        viewModel.liveGetGifts.observe(viewLifecycleOwner) { gifts ->

            if (gifts.isNotEmpty()) {
                binding?.ivEmptyGift?.gone()
                binding?.tvEmptyGift?.gone()
            } else {
                binding?.rvGiftUserList?.gone()
                binding?.ivEmptyGift?.gone()
                binding?.tvEmptyGift?.gone()
            }
            giftListAdapter?.submitList(gifts)
        }

        viewModel.liveEvents.observe(viewLifecycleOwner) {
            when (it) {
                is UserGiftEvents.UserClearAdapterEvent -> giftListAdapter?.submitList(emptyList())

                is UserGiftEvents.ErrorRequestEvent ->
                    showCommonError(getText(R.string.error_try_later), requireView())

                is UserGiftEvents.OwnUserProfileEvent -> {
                    userName = it.userName ?: ""
                    binding?.tvSentMeGift?.isClickable = true
                    openGiftListFragment()
                }

                is UserGiftEvents.FailDeleteGift ->
                    handleErrorGiftDelete(it.position, it.shouldRefreshItem)

                is UserGiftEvents.SuccessDeleteGift -> giftListAdapter?.notifyItemRemoved(it.position)

                else -> {
                    Timber.i("Unknown type message")
                }
            }
        }
    }

    private fun setupViewActions() {
        if (userId == viewModel.getUserUid()) {
            binding?.tvSentMeGift?.setThrottledClickListener {
                binding?.tvSentMeGift?.isClickable = false
                viewModel.requestOwnProfileDao()
            }
            binding?.tvSentMeGift?.visible()
        }
    }

    private fun initGiftSentListener() {
        setFragmentResultListener(KEY_GIFT_SENT) { _, _ ->
            viewModel.refreshGifts()
        }
    }

    private fun openGiftListFragment() {
        userId?.let { id ->
            findNavController().safeNavigate(
                resId = R.id.action_meeraUserGiftsFragment_to_meeraGiftsListFragment,
                bundle = Bundle().apply {
                    putLong(IArgContainer.ARG_USER_ID, id)
                    putString(IArgContainer.ARG_USER_NAME, userName)
                    putSerializable(ARG_GIFT_SEND_WHERE, where)
                }
            )
        }
    }

    private fun showDeleteGiftAlertDialog(
        position: Int,
        gift: GiftEntity?,
        shouldRefreshItem: Boolean,
    ) {
        if (!isAdded) return
        MeeraConfirmDialogBuilder()
            .setHeader(getString(R.string.gifts_delete_question))
            .setDescription(getString(R.string.meera_gifts_delete_description))
            .setTopBtnText(getString(R.string.general_delete))
            .setTopBtnType(ButtonType.FILLED)
            .setBottomBtnText(getString(R.string.cancel))
            .setTopClickListener {
                viewModel.deleteGift(position, gift, shouldRefreshItem)
            }
            .show(childFragmentManager)
    }

    private fun showDeleteGiftBottomDialog(position: Int, gift: GiftEntity?) {
        val dialog = MeeraConfirmDialogUserDeleteGift()
        dialog.setClickDeleteAction {
            showDeleteGiftAlertDialog(
                position = position,
                gift = gift,
                shouldRefreshItem = true
            )
        }
        dialog.show(parentFragmentManager, "MeeraUserGiftsFragment")
    }

    private fun handleErrorGiftDelete(position: Int, shouldRefreshItem: Boolean) {
        if (shouldRefreshItem) giftListAdapter?.notifyItemChanged(position)
        showCommonError(getText(R.string.gifts_delete_error), requireView())
    }

    companion object {

        const val KEY_GIFT_SENT = "gift sent"
    }
}
