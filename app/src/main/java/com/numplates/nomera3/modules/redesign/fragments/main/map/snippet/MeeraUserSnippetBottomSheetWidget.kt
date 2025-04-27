package com.numplates.nomera3.modules.redesign.fragments.main.map.snippet

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.meera.core.base.BaseFragment
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.onMeasured
import com.meera.core.extensions.setBackgroundTint
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewUserSnippetBottomSheetBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.AmplitudePropertyMapSnippetOpenType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model.MapSnippetCloseMethod
import com.numplates.nomera3.modules.maps.domain.model.UserSnippetModel
import com.numplates.nomera3.modules.maps.domain.model.UserUpdateModel
import com.numplates.nomera3.modules.maps.ui.model.MapUserUiModel
import com.numplates.nomera3.modules.maps.ui.snippet.fragment.UserSnippetLoaderFragment
import com.numplates.nomera3.modules.maps.ui.snippet.fragment.UserSnippetStubFragment
import com.numplates.nomera3.modules.maps.ui.snippet.model.ContentState
import com.numplates.nomera3.modules.maps.ui.snippet.model.LoaderItem
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetEvent
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.maps.ui.snippet.model.UserPreviewItem
import com.numplates.nomera3.modules.maps.ui.snippet.model.UserSnippetItem
import com.numplates.nomera3.modules.maps.ui.snippet.model.UserSnippetUiModel
import com.numplates.nomera3.modules.maps.ui.snippet.view.ViewPagerBottomSheetBehavior
import com.numplates.nomera3.modules.userprofile.ui.ProfileUiUtils
import com.numplates.nomera3.modules.userprofile.ui.fragment.MeeraUserInfoFragment
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

class MeeraUserSnippetBottomSheetWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), IOnBackPressed {

    private var viewModel: MeeraUserSnippetViewModel? = null
    private var behaviour: ViewPagerBottomSheetBehavior<View>? = null
    private var binding: ViewUserSnippetBottomSheetBinding? = null
    private var listeners: MutableSet<Listener> = mutableSetOf()
    private var lastStableState: SnippetState.StableSnippetState = SnippetState.Closed
    private var onboardingInProgress = false
    private var lastSelectedPosition: Int? = null
    private var isFull: Boolean = false

    /**
     * Set view invisible to prevent user interaction before setup is complete and let layout happen
     */
    init {
        invisible()
        binding = LayoutInflater.from(context)
            .inflate(R.layout.view_user_snippet_bottom_sheet, this, false)
            .apply(::addView)
            .let(ViewUserSnippetBottomSheetBinding::bind)
            .apply {
                msvpSnippetItems.offscreenPageLimit = 1
                msvpSnippetItems.pageMargin = PAGE_MARGIN_DP.dp
                msvpSnippetItems.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

                    override fun onPageSelected(position: Int) {
                        lastSelectedPosition?.let {
//                            val itemEntry =
//                                (binding?.msvpSnippetItems?.adapter as? UserFragmentsPagerAdapter)?.findItemEntry(it)
//                            val userInfoFragment = itemEntry?.fragment as? MeeraUserInfoFragment
//                            userInfoFragment?.scrollAvatarsToStart()
                        }
                        lastSelectedPosition = position
                        behaviour?.invalidateScrollingChild(false)
                        viewModel?.setCurrentItem(position)

                        if (getLastStableState() == SnippetState.Preview) {
                            viewModel?.logSnippetOpen(AmplitudePropertyMapSnippetOpenType.SWIPE)
                        }
                    }

                    override fun onPageScrollStateChanged(state: Int) = Unit

                })
                msvpSnippetItems.endOverscrollCallback = {
                    viewModel?.onLastPageOverscroll()
                }
                layoutSnippetError.tvSnippetErrorAction.setThrottledClickListener {
                    viewModel?.onErrorAction()
                }
                layoutSnippetError.ibSnippetErrorClose.setThrottledClickListener {
                    viewModel?.setCloseMethod(MapSnippetCloseMethod.CLOSE_BUTTON)

                    setState(SnippetState.Closed)
                }
            }

        onMeasured {
            val snippetHeight = ProfileUiUtils.getSnippetHeight(context)
            behaviour = createBottomSheetBehaviour(snippetHeight)
            val errorContainerParams = binding?.layoutSnippetError?.root?.layoutParams
            errorContainerParams?.height = snippetHeight
            binding?.layoutSnippetError?.root?.layoutParams = errorContainerParams
        }
    }

    override fun onBackPressed(): Boolean {
        if (onboardingInProgress) return true
        val handledByPage = (findCurrentItemEntry()?.fragment as? IOnBackPressed)
            ?.onBackPressed()
            ?: false
        return handledByPage || handleBackPress()
    }

    fun initialize(hostFragment: Fragment, viewModel: MeeraUserSnippetViewModel) {
        binding?.msvpSnippetItems?.adapter = UserFragmentsPagerAdapter(hostFragment.childFragmentManager)
        this.viewModel = viewModel
        if (viewModel.isAuxSnippet()) {
            viewModel.liveAuxUiModel.observe(hostFragment.viewLifecycleOwner, ::handleUiModel)
        } else {
            viewModel.liveUiModel.observe(hostFragment.viewLifecycleOwner, ::handleUiModel)
        }
        viewModel.eventStateFlow
            .onEach(this@MeeraUserSnippetBottomSheetWidget::handleEvent)
            .launchIn(hostFragment.viewLifecycleOwner.lifecycleScope)
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun setUser(selectedUser: MapUserUiModel, isAuxSnippet: Boolean, fullSnippet: Boolean = false) {
        if (isAuxSnippet) {
            viewModel?.setAuxUser(selectedUser)
        } else {
            viewModel?.setSelectedUser(selectedUser, fullSnippet)
        }
        visible()
        setState(SnippetState.Preview)

        viewModel?.logSnippetOpen(AmplitudePropertyMapSnippetOpenType.TAP)
    }

    fun setState(snippetState: SnippetState.StableSnippetState) {
        if (onboardingInProgress) return
        behaviour?.state = snippetState.behaviorValue
    }

    fun setVerticalOffset(offset: Int) {
        if (offset >= 0) {
            behaviour?.invalidateScrollingChild(false)
        } else {
            behaviour?.invalidateScrollingChild(true)
        }
    }

    fun getLastStableState(): SnippetState.StableSnippetState {
        return lastStableState
    }

    fun updateUser(userUpdateModel: UserUpdateModel) {
        viewModel?.updateUserSnippet(userUpdateModel)
    }

    fun onStartFragment() {
        (findCurrentItemEntry()?.fragment as? BaseFragment)?.let {
            if (!it.isFragmentStarted) {
                it.onStartFragment()
            }
        }
    }

    fun onStopFragment() {
        (findCurrentItemEntry()?.fragment as? BaseFragment)?.let {
            if (it.isFragmentStarted) {
                it.onStopFragment()
            }
        }
    }

    private fun findCurrentItemEntry(): ItemEntry? {
        return binding?.msvpSnippetItems?.currentItem?.let { currentItem ->
            getAdapter()?.findItemEntry(currentItem)
        }
    }

    private fun handleUiModel(uiModel: UserSnippetUiModel) {
        when (uiModel.contentState) {
            ContentState.ITEMS -> {
                getAdapter()?.setItemModels(uiModel.items)
                showItems()
            }

            ContentState.ERROR -> {
                showError(uiModel.selectedUserIsVip == true)
            }
        }
        if (uiModel.isFull) {
            isFull = uiModel.isFull
            behaviour?.setExpandedStateRestricted(uiModel.expandedStateRestricted)
            setState(SnippetState.Expanded)
        } else {
            behaviour?.setExpandedStateRestricted(uiModel.expandedStateRestricted)
        }
        binding?.vUserSnippetFadeIn?.isVisible = uiModel.showFadeIn
    }

    private fun handleEvent(event: SnippetEvent) {
        when (event) {
            SnippetEvent.ShowOnboarding -> post { showOnboarding() }
            is SnippetEvent.DispatchNewSnippetState -> listeners.forEach {
                it.onUserSnippetStateChanged(event.snippetState)
            }

            is SnippetEvent.DispatchSnippetSlide -> listeners.forEach { it.onUserSnippetSlide(event.slideOffset) }
            is SnippetEvent.DispatchUserSelected -> listeners.forEach { it.onUserSelected(event.userSnippetModel) }
        }
    }

    /** TODO https://nomera.atlassian.net/browse/BR-18804
     * Переписать через вложенность фрагментов UserInfoFragment в страницы, содержащие BottomSheet
     */
    private fun createBottomSheetBehaviour(snippetHeight: Int): ViewPagerBottomSheetBehavior<View> {
        return ViewPagerBottomSheetBehavior.from(findViewById(R.id.vg_snippet_container)).apply {
            isHideable = true
            peekHeight = snippetHeight
            addBottomSheetCallback(object : ViewPagerBottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    onSnippetState(SnippetState.fromBehaviorValue(newState))
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    viewModel?.setSnippetSlideOffset(slideOffset)
                }
            })
            state = SnippetState.Closed.behaviorValue
        }
    }

    private fun onSnippetState(snippetState: SnippetState) {
        when (snippetState) {
            SnippetState.Closed -> viewModel?.logClose()
            SnippetState.DraggedByUser -> viewModel?.setCloseMethod(MapSnippetCloseMethod.SWIPE)
            else -> Unit
        }

        if (lastStableState == SnippetState.Expanded && isFull && snippetState == SnippetState.Preview) {
            (snippetState as? SnippetState.StableSnippetState)?.let {
                this.lastStableState = snippetState
            }
            setState(SnippetState.Closed)
            return
        }

        (snippetState as? SnippetState.StableSnippetState)?.let {
            this.lastStableState = snippetState
        }
        when (snippetState) {
            SnippetState.Preview -> {
                binding?.apply {
                    behaviour?.invalidateScrollingChild(false)
                    msvpSnippetItems.isPagingEnabled = true
                }
            }

            SnippetState.Expanded -> {
                binding?.apply {
                    msvpSnippetItems.isPagingEnabled = false
                }
            }

            else -> Unit
        }
        viewModel?.setSnippetState(snippetState)
    }

    private fun getAdapter(): UserFragmentsPagerAdapter? {
        return binding?.msvpSnippetItems?.adapter as? UserFragmentsPagerAdapter
    }

    private fun showOnboarding() {
        binding?.msvpSnippetItems?.apply {
            val animator: ValueAnimator = ValueAnimator.ofInt(0, ONBOARDING_DRAG_OFFSET_DP.dp)
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) = Unit
                override fun onAnimationEnd(animation: Animator) {
                    if (isFakeDragging) endFakeDrag()
                    viewModel?.setOnboardingShown()
                    onboardingInProgress = false
                }

                override fun onAnimationCancel(animation: Animator) {
                    animation.removeListener(this)
                    binding?.lavSnippetOnboarding?.cancelAnimation()
                    binding?.vgSnippetOnboarding?.gone()
                    onboardingInProgress = false
                }

                override fun onAnimationRepeat(animation: Animator) = Unit
            })
            animator.interpolator = AccelerateInterpolator()
            animator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
                private var oldDragPosition = 0
                override fun onAnimationUpdate(animation: ValueAnimator) {
                    if (behaviour?.state == SnippetState.Preview.behaviorValue && isFakeDragging) {
                        val dragPosition = animation.animatedValue as Int
                        val dragOffset = dragPosition - oldDragPosition
                        oldDragPosition = dragPosition
                        fakeDragBy(-dragOffset.toFloat())
                    } else {
                        animation.cancel()
                    }

                }
            })
            animator.duration = ONBOARDING_DURATION_MS
            if (behaviour?.state == SnippetState.Preview.behaviorValue && beginFakeDrag()) {
                onboardingInProgress = true
                binding?.vgSnippetOnboarding?.visible()
                binding?.lavSnippetOnboarding?.playAnimation()
                postDelayed(ONBOARDING_START_DELAY_MS) {
                    animator.start()
                }
                postDelayed(ONBOARDING_START_DELAY_MS + ONBOARDING_DURATION_MS + ONBOARDING_END_DELAY) {
                    binding?.vgSnippetOnboarding?.gone()
                }
            }
        }
    }

    private fun showItems() {
        binding?.apply {
            msvpSnippetItems.visible()
            layoutSnippetError.vgSnippetErrorLayout.gone()
        }
    }

    private fun showError(isVip: Boolean) {
        binding?.layoutSnippetError?.apply {
            val contentColor = ContextCompat.getColor(context, if (isVip) R.color.white else R.color.black)
            ibSnippetErrorClose.setColorFilter(contentColor, android.graphics.PorterDuff.Mode.SRC_ATOP)
            layoutSnippetErrorStub.tvSomethingWrongErrorTitle.setTextColor(contentColor)
            layoutSnippetErrorStub.tvSomethingWrongErrorMessage.setTextColor(contentColor)
            val backgroundColorRes = if (isVip) R.color.colorVipBlack else R.color.white
            vgSnippetErrorLayout.setBackgroundTint(backgroundColorRes)
            val btnBackgroundColorRes = if (isVip) R.color.ui_black_vip_background else R.color.purple_button
            tvSnippetErrorAction.setBackgroundTint(btnBackgroundColorRes)
            val btnTextColor = ContextCompat.getColor(context, if (isVip) R.color.ui_yellow else R.color.ui_purple)
            tvSnippetErrorAction.setTextColor(btnTextColor)
            vgSnippetErrorLayout.visible()
        }
        binding?.msvpSnippetItems?.invisible()
    }

    private fun handleBackPress(): Boolean {
        viewModel?.setCloseMethod(MapSnippetCloseMethod.BACK_BUTTON)

        return when (lastStableState) {
            SnippetState.Preview, SnippetState.HalfCollapsedPreview -> {
                setState(SnippetState.Closed)
                true
            }

            SnippetState.Expanded -> {
                setState(SnippetState.Preview)
                true
            }

            SnippetState.Closed -> false
        }
    }

    inner class UserFragmentsPagerAdapter(fragmentManager: FragmentManager) :
        FragmentStatePagerAdapter(fragmentManager) {
        /** TODO https://nomera.atlassian.net/browse/BR-18804
         * Переписать через поиск фрагментов во FragmentManager
         */
        private var items: List<UserSnippetItem> = listOf()
        private val itemEntryMap: MutableMap<Int, ItemEntry?> = mutableMapOf()

        override fun getCount(): Int {
            return getItemModels().size
        }

        override fun getItem(position: Int): Fragment {
            val fragment = when (val item = getItemModel(position)) {
                is LoaderItem -> UserSnippetLoaderFragment()
                is UserPreviewItem -> MeeraUserInfoFragment().apply {
                    isApplyNavigationConfig = false
                    Timber.e("item $item")
//                    arguments = Bundle().apply {
//                        putLong(ARG_USER_ID, item.uid)
//                        putString(ARG_TRANSIT_FROM, AmplitudePropertyWhere.USER_SNIPPET.property)
//                        when (item.payload) {
//                            is UserSnippetModel -> putParcelable(MeeraUserInfoFragment.ARG_USER_SNIPPET_DATA, item.payload)
//                            is MapUserUiModel -> putParcelable(MeeraUserInfoFragment.ARG_USER_PIN_DATA, item.payload)
//                        }
//                        putBoolean(
//                            MeeraUserInfoFragment.ARG_USER_SNIPPET_FOCUSED,
//                            position == binding?.msvpSnippetItems?.currentItem
//                        )
//                    }
//                    userSnippet = this@MeeraUserSnippetBottomSheetWidget
//                    addListener(this)
                }

                else -> UserSnippetStubFragment()
            }
//            Timber.e("item $item")

            fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {

                override fun onStart(owner: LifecycleOwner) {
                    super.onStart(owner)
//                    if (!isTopFragmentViewVideo(owner) && !fragment.isFragmentStarted) {
//                        fragment.onStartFragment()
//                    }
                }

                override fun onStop(owner: LifecycleOwner) {
//                    if (fragment.isFragmentStarted) {
//                        fragment.onStopFragment()
//                    }
                    super.onStop(owner)
                }
            })
            itemEntryMap[position] = ItemEntry(
                fragment = fragment,
                position = position
            )
            return fragment
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
//            (itemEntryMap[position]?.fragment as? MeeraUserInfoFragment)?.apply {
//                userSnippet = null
//                removeListener(this)
//            }
            itemEntryMap.remove(position)
            super.destroyItem(container, position, obj)
        }

        override fun getItemPosition(obj: Any): Int {
            return when (obj) {
                is MeeraUserInfoFragment -> {
//                    if (obj.userId != null && items.find { (it as? UserPreviewItem)?.uid == obj.userId } != null) {
//                        POSITION_UNCHANGED
//                    } else {
//                        POSITION_NONE
//                    }
                    POSITION_UNCHANGED
                }

                else -> POSITION_NONE
            }
        }

        override fun getPageWidth(position: Int): Float {
            return if (getItemModel(position) is LoaderItem) 0.6f else 1f
        }

        fun getItemModel(position: Int): UserSnippetItem? {
            return getItemModels().getOrNull(position)
        }

        fun getItemModels(): List<UserSnippetItem> {
            return items
        }

        fun setItemModels(items: List<UserSnippetItem>) {
            if (items == this.items) return
            post {
                this.items = items
                notifyDataSetChanged()
                items.forEachIndexed { index, userSnippetItem ->
                    ((userSnippetItem as? UserPreviewItem)?.payload as? UserSnippetModel)?.let { userSnippetModel ->
//                        (itemEntryMap[index]?.fragment as? MeeraUserInfoFragment)?.setUserSnippetModel(userSnippetModel)
                    }
                }
            }
        }

        fun findItemEntry(position: Int): ItemEntry? {
            return itemEntryMap[position]
        }
    }

    data class ItemEntry(
        val fragment: Fragment,
        val position: Int
    )

    interface Listener {
        fun onUserSelected(userSnippetModel: UserSnippetModel)
        fun onUserSnippetStateChanged(state: SnippetState)
        fun onUserSnippetSlide(offset: Float)
    }

    companion object {
        const val PAGE_SIZE = 10
        private const val PAGE_MARGIN_DP = 24

        private const val ONBOARDING_DRAG_OFFSET_DP = 60
        private const val ONBOARDING_START_DELAY_MS = 900L
        private const val ONBOARDING_DURATION_MS = 550L
        private const val ONBOARDING_END_DELAY = 500L
    }
}
