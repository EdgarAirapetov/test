package com.numplates.nomera3.modules.reactionStatistics.ui

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.gone
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setVisible
import com.meera.core.extensions.simpleName
import com.meera.core.extensions.visible
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.BottomSheetReactionsStatisticsBinding
import com.numplates.nomera3.modules.maps.ui.snippet.view.ViewPagerBottomSheetBehavior
import com.numplates.nomera3.modules.maps.ui.snippet.view.ViewPagerBottomSheetBehavior.STATE_HIDDEN
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.ui.util.ReactionCounterFormatter
import com.numplates.nomera3.modules.reactionStatistics.ui.entity.ReactionTabUiEntity
import com.numplates.nomera3.modules.reactionStatistics.ui.entity.ReactionsUiEvent
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.numplates.nomera3.presentation.view.ui.BottomSheetDialogEventsListener
import com.numplates.nomera3.presentation.view.utils.NToast
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ReactionsStatisticsBottomSheetFragment : BaseBottomSheetDialogFragment<BottomSheetReactionsStatisticsBinding>() {
    private val BOTTOM_SHEET_MARGIN_TOP = 44
    private val BOTTOM_SHEET_EXPANDED_RATIO = 0.7f

    private val entityId: Long by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getLong(ARG_ENTITY_ID)
    }

    private val entityType: ReactionsEntityType by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().get(ARG_ENTITY_TYPE) as ReactionsEntityType
    }

    private val openFromReactions: Boolean by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().get(ARG_OPEN_FROM_REACTIONS) as Boolean
    }

    private val pagerAdapter by lazy { ReactionsPagerAdapter(this, entityId, entityType) }

    private var viewsCountListener: ViewsCountListener? = null
    fun setViewsCountChangeListener(listener: ViewsCountListener) {
        viewsCountListener = listener
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> BottomSheetReactionsStatisticsBinding
        get() = BottomSheetReactionsStatisticsBinding::inflate

    private var bottomSheetBehavior = ViewPagerBottomSheetBehavior<View>()

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialogListener?.onCreateDialog()
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?

            bottomSheet?.let {

                (it.layoutParams as? CoordinatorLayout.LayoutParams)?.behavior = bottomSheetBehavior
                val height = Resources.getSystem().displayMetrics.heightPixels

                bottomSheetBehavior.peekHeight = (height * BOTTOM_SHEET_EXPANDED_RATIO).toInt()
                bottomSheetBehavior.isHideable = true
                bottomSheetBehavior.addBottomSheetCallback(object : ViewPagerBottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == STATE_HIDDEN) dismissAllowingStateLoss()
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
                })

                setupFullHeight(it)
            }
        }
        return dialog
    }

    private fun setupFullHeight(bottomSheet: View) {
        val height = Resources.getSystem().displayMetrics.heightPixels
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = height - dpToPx(BOTTOM_SHEET_MARGIN_TOP)
        bottomSheet.layoutParams = layoutParams

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let { existBinding ->

            existBinding.ivCloseIcon.setThrottledClickListener { dismissAllowingStateLoss() }
            existBinding.vp2Reactions.adapter = pagerAdapter
            existBinding.vp2Reactions.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    bottomSheetBehavior.invalidateScrollingChild(true)
                }
            })

            TabLayoutMediator(existBinding.tabLayoutReactionsIndicator, existBinding.vp2Reactions) { tab, position ->

                val reaction = pagerAdapter.getItem(position)
                if (reaction.isViewersTab) {
                    tab.setCustomView(R.layout.item_views_tab)
                    return@TabLayoutMediator
                }

                tab.setCustomView(R.layout.item_reaction_tab)
                tab.customView?.let {
                    val lav = listOf<LottieAnimationView>(
                        it.findViewById(R.id.lav_reaction_1),
                        it.findViewById(R.id.lav_reaction_2),
                        it.findViewById(R.id.lav_reaction_3)
                    )
                    val vBg = listOf<View>(
                        it.findViewById(R.id.v_reaction_1_bg), it.findViewById(R.id.v_reaction_2_bg)
                    )

                    reaction.reactions.map {
                        ReactionType.getByString(it)
                            ?: throw java.lang.IllegalArgumentException("Reaction type not found")
                    }.forEachIndexed { index, react ->
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
                    tvCount.text = reactionCounterFormatter.format(reaction.count)
                }
            }.attach()

            existBinding.tabLayoutReactionsIndicator.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (isViewerTab(tab)) return
                    tab?.customView?.let {
                        val lav = listOf<LottieAnimationView>(
                            it.findViewById(R.id.lav_reaction_1),
                            it.findViewById(R.id.lav_reaction_2),
                            it.findViewById(R.id.lav_reaction_3)
                        )

                        lav.forEach { it.playAnimation() }
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    if (isViewerTab(tab)) return
                    tab?.customView?.let {
                        val lav = listOf<LottieAnimationView>(
                            it.findViewById(R.id.lav_reaction_1),
                            it.findViewById(R.id.lav_reaction_2),
                            it.findViewById(R.id.lav_reaction_3)
                        )
                        lav.forEach { it.cancelAnimation() }
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab?) = Unit
            })

        }
        initTitle(entityType)
        viewModel.init(entityId, entityType)
        initObservers()
    }


    private fun isViewerTab(tab: TabLayout.Tab?): Boolean {
        val position = tab?.position ?: 0
        val reaction = pagerAdapter.getItem(position)
        return reaction.isViewersTab
    }

    private fun initTitle(entityType: ReactionsEntityType) {
        binding?.tvReactionTitle?.setVisible(entityType != ReactionsEntityType.MOMENT_WITH_VIEWS)
    }

    fun initObservers() {
        viewModel.liveReactionTabsState.observe(viewLifecycleOwner) { tabItems->
            binding?.sflReactionTabs?.gone()
            binding?.tabLayoutReactionsIndicator?.visible()
            binding?.vp2Reactions?.visible()
            binding?.vDivider?.visible()
            pagerAdapter.submitItems(tabItems.items)

            if (entityType == ReactionsEntityType.MOMENT_WITH_VIEWS && openFromReactions) {
                selectFirstReactionTab(tabItems.items)
            }

            binding?.vp2Reactions?.offscreenPageLimit = tabItems.items.size
        }

        viewModel.liveViewsCountState.observe(viewLifecycleOwner) { viewsCount ->
            binding?.tvReactionTitle?.visible()
            if (viewsCount == 0L) return@observe
            val viewsCountString = requireContext().pluralString(R.plurals.moment_views_count, viewsCount.toInt())
            viewsCountListener?.onViewCountChange(viewsCount)
            binding?.tvReactionTitle?.text = viewsCountString
        }

        viewModel.reactionsEvent.flowWithLifecycle(viewLifecycleOwner.lifecycle).onEach(::handleUiEffect)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun selectFirstReactionTab(tabItems: List<ReactionTabUiEntity>?) {
        tabItems ?: return
        for (i in tabItems.indices) {
            if (!tabItems[i].isViewersTab) {
                binding?.vp2Reactions?.currentItem = i
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
                binding?.let {
                    it.sflReactionTabs.gone()
                    it.tabLayoutReactionsIndicator.gone()
                    it.vDivider.gone()
                    it.vp2Reactions.gone()
                    it.tvReactionsEmpty.visible()
                }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dialogListener?.onDismissDialog()
    }

    private fun showErrorToast(@StringRes errorMessageRes: Int) {
        dismissAllowingStateLoss()
        NToast.with(act).typeError().text(getString(errorMessageRes)).show()
    }

    fun show(manager: FragmentManager?) {
        val fragment = manager?.findFragmentByTag(simpleName)
        if (fragment != null) return
        manager?.let {
            super.show(manager, simpleName)
        }
    }

    companion object {
        const val ARG_ENTITY_ID = "argEntityId"
        const val ARG_ENTITY_TYPE = "argEntityType"
        const val ARG_PAGE_REACTION = "argPageReaction"
        const val ARG_PAGE_VIEWERS = "argPageViewers"
        const val ARG_OPEN_FROM_REACTIONS = "argPageViewers"
        fun getInstance(
            entityId: Long,
            entityType: ReactionsEntityType,
            viewCountListener: ViewsCountListener? = null,
            startWithReactionTab: Boolean = false,
            dialogEventsListener: BottomSheetDialogEventsListener? = null
        ) =
            ReactionsStatisticsBottomSheetFragment().apply {
                arguments = bundleOf(
                    ARG_ENTITY_ID to entityId,
                    ARG_ENTITY_TYPE to entityType,
                    ARG_OPEN_FROM_REACTIONS to startWithReactionTab
                )
                viewCountListener?.let { setViewsCountChangeListener(it) }
                dialogEventsListener?.let { setListener(it) }
            }
    }

    interface ViewsCountListener {
        fun onViewCountChange(viewsCount: Long)
    }
}
