package com.numplates.nomera3.modules.reactionStatistics.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.meera.core.extensions.getScreenHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.simpleName
import com.meera.core.extensions.textColor
import com.meera.core.extensions.visible
import com.meera.core.utils.showCommonError
import com.meera.uikit.bottomsheet.UiKitViewPagerBottomSheetBehavior
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraBottomSheetReactionsStatisticsBinding
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.ui.util.ReactionCounterFormatter
import com.numplates.nomera3.modules.reactionStatistics.ui.entity.ReactionTabUiEntity
import com.numplates.nomera3.modules.reactionStatistics.ui.entity.ReactionTabsUiEntity
import com.numplates.nomera3.modules.reactionStatistics.ui.entity.ReactionUserUiEntity
import com.numplates.nomera3.modules.reactionStatistics.ui.entity.ReactionsUiEvent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MeeraReactionsStatisticsBottomDialogFragment
    : UiKitBottomSheetDialog<MeeraBottomSheetReactionsStatisticsBinding>() {

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraBottomSheetReactionsStatisticsBinding
        get() = MeeraBottomSheetReactionsStatisticsBinding::inflate

    private val entityId: Long by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getLong(ARG_ENTITY_ID)
    }

    private val entityType: ReactionsEntityType by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().get(ARG_ENTITY_TYPE) as ReactionsEntityType
    }

    private val openFromReactions: Boolean by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().get(ARG_OPEN_FROM_REACTIONS) as Boolean
    }

    private var pagerAdapter: MeeraReactionsPagerAdapter? = null

    private var viewsCountListener: ViewsCountListener? = null

    fun setViewsCountChangeListener(listener: ViewsCountListener) {
        viewsCountListener = listener
    }

    private var _navigationListener: ((destination: DestinationTransition) -> Unit)? = null

    private var bottomSheetBehavior = UiKitViewPagerBottomSheetBehavior<View>()

    private val reactionCounterFormatter by lazy {
        ReactionCounterFormatter(
            requireContext().getString(R.string.thousand_lowercase_label),
            requireContext().getString(R.string.million_lowercase_label),
            oneAllow = true,
            thousandAllow = true
        )
    }

    private val viewModel by viewModels<ReactionsViewModel> {
        App.component.getViewModelFactory()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding?.let { existBinding ->
            existBinding.root.updateLayoutParams {
                height = getScreenHeight()
            }

            existBinding.ivBottomSheetReactionsClose.setThrottledClickListener {
                dismissAllowingStateLoss()
            }

            pagerAdapter = MeeraReactionsPagerAdapter(this, entityId, entityType) { userEntity ->
                _navigationListener?.invoke(DestinationTransition.UserProfileDestination(userEntity))
            }

            existBinding.vp2Reactions.adapter = pagerAdapter
            existBinding.vp2Reactions.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    viewModel.saveTabPosition(position)
                    bottomSheetBehavior.invalidateScrollingChild(true)
                }
            })

            TabLayoutMediator(existBinding.tabLayoutReactionsIndicator, existBinding.vp2Reactions) { tab, position ->
                val reaction = pagerAdapter?.getItem(position)
                if (reaction?.isViewersTab.isTrue()) {
                    tab.setCustomView(R.layout.item_views_tab)
                    return@TabLayoutMediator
                }

                tab.setCustomView(R.layout.meera_item_reaction_tab)
                tab.customView?.let {
                    val lav = listOf<LottieAnimationView>(
                        it.findViewById(R.id.lav_reaction_1),
                        it.findViewById(R.id.lav_reaction_2),
                        it.findViewById(R.id.lav_reaction_3)
                    )
                    val vBg = listOf<View>(
                        it.findViewById(R.id.v_reaction_1_bg), it.findViewById(R.id.v_reaction_2_bg)
                    )

                    reaction?.reactions?.map {
                        ReactionType.getByString(it) ?: error("Reaction type not found")
                    }?.forEachIndexed { index, react ->
                        lav[index].visible()

                        val lottieAnimation = if (react.resourceNoBorder == ReactionType.Fire.resourceNoBorder) {
                            vBg.getOrNull(index)?.gone()
                            R.raw.reaction_fire_lottie_bg_white
                        } else {
                            vBg.getOrNull(index)?.visible()
                            react.resourceNoBorder
                        }
                        lav[index].setAnimation(lottieAnimation)
                    }

                    val tvCount: TextView = it.findViewById(R.id.tv_count)
                    tvCount.text = reactionCounterFormatter.format(reaction?.count ?: 0)
                }
            }.attach()

            existBinding.tabLayoutReactionsIndicator.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (isViewerTab(tab)) {
                        tab?.customView?.setBackgroundResource(R.drawable.meera_background_reaction_bar_button)
                        return
                    }
                    tab?.customView?.let {
                        val lav = listOf<Triple<LottieAnimationView, ConstraintLayout, TextView>>(
                            Triple(
                                it.findViewById(R.id.lav_reaction_1),
                                it.findViewById(R.id.ll_border_reaction),
                                it.findViewById(R.id.tv_count)

                            ),
                            Triple(
                                it.findViewById(R.id.lav_reaction_2),
                                it.findViewById(R.id.ll_border_reaction),
                                it.findViewById(R.id.tv_count)
                            ),
                            Triple(
                                it.findViewById(R.id.lav_reaction_3), it.findViewById(R.id.ll_border_reaction),
                                it.findViewById(R.id.tv_count)
                            )
                        )

                        lav.forEach { reaction ->
                            reaction.first.playAnimation()
                            reaction.second.setBackgroundResource(R.drawable.meera_background_reaction_bar_button)
                            reaction.third.textColor(R.color.color_soft_black)
                        }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    if (isViewerTab(tab)) {
                        tab?.customView?.setBackgroundResource(R.color.transparent)
                        return
                    }
                    tab?.customView?.let {
                        val lav = listOf<Triple<LottieAnimationView, ConstraintLayout, TextView>>(
                            Triple(
                                it.findViewById(R.id.lav_reaction_1),
                                it.findViewById(R.id.ll_border_reaction),
                                it.findViewById(R.id.tv_count)

                            ),
                            Triple(
                                it.findViewById(R.id.lav_reaction_2),
                                it.findViewById(R.id.ll_border_reaction),
                                it.findViewById(R.id.tv_count)
                            ),
                            Triple(
                                it.findViewById(R.id.lav_reaction_3), it.findViewById(R.id.ll_border_reaction),
                                it.findViewById(R.id.tv_count)
                            )
                        )
                        lav.forEach { reaction ->
                            reaction.first.cancelAnimation()
                            reaction.second.setBackgroundResource(R.color.transparent)
                            reaction.third.textColor(R.color.colorGray9298A0)
                        }
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab?) = Unit
            })
        }
        viewModel.init(entityId, entityType)
        initObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resetTabSetting()
        contentBinding?.vp2Reactions?.adapter = null
    }

    private fun isViewerTab(tab: TabLayout.Tab?): Boolean {
        val position = tab?.position ?: 0
        val reaction = pagerAdapter?.getItem(position)
        return reaction?.isViewersTab.isTrue()
    }

    fun initObservers() {
        viewModel.liveReactionTabsState.observe(viewLifecycleOwner) { state ->
            contentBinding?.sflReactionTabs?.gone()
            contentBinding?.tabLayoutReactionsIndicator?.visible()
            contentBinding?.vp2Reactions?.visible()
            pagerAdapter?.submitItems(state.items)

            setupTabPosition(state)

            contentBinding?.vp2Reactions?.offscreenPageLimit = state.items.size
        }

        viewModel.liveViewsCountState.observe(viewLifecycleOwner) { viewsCount ->
            if (viewsCount == 0L) return@observe
            viewsCountListener?.onViewCountChange(viewsCount)
        }

        viewModel.reactionsEvent.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach(::handleUiEffect)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupTabPosition(state: ReactionTabsUiEntity) {
        when {
            state.selectedTab != null -> {
                contentBinding?.vp2Reactions?.currentItem = state.selectedTab
            }
            entityType == ReactionsEntityType.MOMENT_WITH_VIEWS && openFromReactions -> {
                selectFirstReactionTab(state.items)
            }
        }
    }

    private fun selectFirstReactionTab(tabItems: List<ReactionTabUiEntity>?) {
        tabItems ?: return
        for (i in tabItems.indices) {
            if (!tabItems[i].isViewersTab) {
                contentBinding?.vp2Reactions?.currentItem = i
                break
            }
        }
    }

    private fun handleUiEffect(event: ReactionsUiEvent) {
        when (event) {
            is ReactionsUiEvent.ShowErrorToast -> {
                showErrorToast(event.message)
            }

            is ReactionsUiEvent.ShowReactionsIsEmpty -> {
                contentBinding?.let {
                    it.sflReactionTabs.gone()
                    it.tabLayoutReactionsIndicator.gone()
                    it.vp2Reactions.gone()
                    it.llReactionsEmpty.visible()
                }
            }
        }
    }

    private fun showErrorToast(@StringRes errorMessageRes: Int) {
        dismissAllowingStateLoss()
        showCommonError(getText(errorMessageRes), requireView())
    }

    fun show(manager: FragmentManager?) {
        val fragment = manager?.findFragmentByTag(simpleName)
        if (fragment != null) return
        manager?.let {
            super.show(manager, simpleName)
        }
    }

    override fun createDialogState() = UiKitBottomSheetDialogParams(
        needShowToolbar = false,
        needShowCloseButton = false,
        dialogStyle = R.style.BottomSheetDialogTransparentTheme
    )

    override fun getBehaviorDelegate() =
        UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.COLLAPSED)
            .create(dialog)

    companion object {
        const val ARG_ENTITY_ID = "argEntityId"
        const val ARG_ENTITY_TYPE = "argEntityType"
        const val ARG_OPEN_FROM_REACTIONS = "argPageViewers"

        fun makeInstance(
            entityId: Long,
            entityType: ReactionsEntityType,
            viewCountListener: ViewsCountListener? = null,
            startWithReactionTab: Boolean = false,
            navigationListener: ((destination: DestinationTransition) -> Unit)? = null
        ) = MeeraReactionsStatisticsBottomDialogFragment().apply {
            arguments = bundleOf(
                ARG_ENTITY_ID to entityId,
                ARG_ENTITY_TYPE to entityType,
                ARG_OPEN_FROM_REACTIONS to startWithReactionTab
            )
            viewCountListener?.let { setViewsCountChangeListener(it) }
            _navigationListener = navigationListener
        }
    }

    interface ViewsCountListener {
        fun onViewCountChange(viewsCount: Long)
    }

    sealed class DestinationTransition {
        data class UserProfileDestination(val userEntity: ReactionUserUiEntity) : DestinationTransition()
    }
}
