package com.numplates.nomera3.modules.gifservice.ui

import android.graphics.Point
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.extensions.doDelayed
import com.meera.core.utils.tedbottompicker.compat.OnImagesReady
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardHowProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardWhereProperty
import com.numplates.nomera3.modules.chat.mediakeyboard.data.entity.MediaKeyboardTab
import com.numplates.nomera3.modules.chat.mediakeyboard.data.entity.isRegularTab
import com.numplates.nomera3.modules.chat.mediakeyboard.data.entity.isStickerPackTab
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.adapter.MeeraMediaKeyboardPagesAdapter
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.adapter.MeeraMediaKeyboardTabsAdapter
import com.numplates.nomera3.modules.gifservice.ui.entity.GifMenuCallbackEvents
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

const val POSITION_RECENTS = 0
const val POSITION_FAVORITES = 1
const val POSITION_PICKER = 2
const val POSITION_GIF = 3
const val POSITION_STICKERS = 4

private const val POSITION_HIDE = -1
const val POSITION_WIDGETS_FAVORITES = 0
const val POSITION_WIDGETS_PICKER = 1
const val POSITION_WIDGETS_STICKERS = 2

enum class MediaKeyboardPagesPosition(
    val recents: Int,
    val favourites: Int,
    val picker: Int,
    val gif: Int,
    val stickers: Int
) {
    DEFAULT(POSITION_RECENTS, POSITION_FAVORITES, POSITION_PICKER, POSITION_GIF, POSITION_STICKERS),
    WIDGETS(
        recents = POSITION_HIDE,
        favourites = POSITION_WIDGETS_FAVORITES,
        picker = POSITION_WIDGETS_PICKER,
        gif = POSITION_HIDE,
        stickers = POSITION_WIDGETS_STICKERS
    )
}

private val DRAWABLE_RECENTS = R.drawable.ic_outlined_time_m
private val DRAWABLE_FAVORITES = R.drawable.ic_outlined_star2_m
private val DRAWABLE_GIF = R.drawable.ic_outlined_gif_m
private val DRAWABLE_PICKER = R.drawable.ic_outlined_gallery_m

private const val STICKER_PACK_SCROLL_DELAY = 200L
private const val STICKER_PACK_TAB_DELAY = 10L
private const val SLIDE_OFFSET_MIN = 0F
private const val SLIDE_OFFSET_MAX = 1F

class MeeraMediaKeyboard(
    private val fragment: Fragment,
    private val bottomSheetBehavior: BottomSheetBehavior<View>,
    rootView: ViewGroup,
    private val openFrom: MediaControllerOpenPlace,
    private val interactionCallback: (GifMenuCallbackEvents) -> Unit = { },
    private val imagesCallback: OnImagesReady,
    private val amplitudeMediaKeyboardAnalytic: AmplitudeMediaKeyboardAnalytic
) {
    private val needShowWidgets = openFrom == MediaControllerOpenPlace.Moments
    private val tabItems = mutableListOf<MediaKeyboardTab>().also { list ->
        list.addAll(if (needShowWidgets) generateMomentTabsList() else generateDefaultList())
    }
    private val _mediaKeyboardPagesPosition = if (needShowWidgets) {
        MediaKeyboardPagesPosition.WIDGETS
    } else {
        MediaKeyboardPagesPosition.DEFAULT
    }
    val mediaKeyboardPagesPosition: MediaKeyboardPagesPosition
        get() = _mediaKeyboardPagesPosition

    private val tabsAdapter: MeeraMediaKeyboardTabsAdapter by lazy {
        MeeraMediaKeyboardTabsAdapter(
            callback = ::tabClicked,
            stickerPackViewedCallback = ::stickerPackViewed,
            useDarkMode = needShowWidgets
        )
    }

    private var pagesAdapter: MeeraMediaKeyboardPagesAdapter = createAdapterByState(
        if (needShowWidgets) GifMenuState.CREATE_MOMENT else GifMenuState.DEFAULT
    )
    private var isSelectPageBySwipe = false

    val favoritesTabPosition: Point?
        get() {
            val view = rvTabs?.getChildAt(POSITION_FAVORITES) ?: return null
            val position = IntArray(2)
            view.getLocationInWindow(position)
            return Point(position[0], position[1])
        }

    private var isMenuInitialized = false

    private var isPageChangeLoggedInAnalytic = false

    private var currentStickerPack: MediaKeyboardStickerPackUiModel? = null
    val stickerPacks = mutableListOf<MediaKeyboardStickerPackUiModel>()
    private val recentStickers = mutableListOf<MediakeyboardFavoriteRecentUiModel>()

    private val rvTabs = rootView.findViewById<RecyclerView>(R.id.rv_mediakeyboard_tabs)
    private val vpPages = rootView.findViewById<ViewPager2>(R.id.vp_mediakeyboard_pages)
    private val vgHeaderDialog = rootView.findViewById<FrameLayout>(R.id.vg_mediakeyboard_header)

    val isVisible: Boolean
        get() = bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN

    val isHidden: Boolean
        get() = bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN

    val isExpanded: Boolean
        get() = bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED

    init {
        dismissMenu()
    }

    fun enableViewPagerScrolling(enabled: Boolean) {
        vpPages.isUserInputEnabled = enabled
    }

    fun onBottomSheetStateChange(state: Int) {
        pagesAdapter.onBottomSheetStateChange(state)
    }

    fun onStartAnimationTransitionFragment() {
        pagesAdapter.onStartAnimationTransitionFragment()
    }

    fun onOpenTransitionFragment() {
        pagesAdapter.onOpenTransitionFragment()
    }

    fun setCurrentChosenStickerPack(stickerPack: MediaKeyboardStickerPackUiModel) {
        this.currentStickerPack = stickerPack
        setTabCheckedByStickerPack()
    }

    fun setRecentStickersChosen() {
        this.currentStickerPack = null
        setTabCheckedByDrawableId(DRAWABlE_RECENT_STICKERS)
    }

    fun setWidgetsChosen() {
        this.currentStickerPack = null
        setTabCheckedByDrawableId(DRAWABLE_WIDGETS)
    }

    fun setStickerPacks(
        stickerPacks: List<MediaKeyboardStickerPackUiModel>,
        recentStickers: List<MediakeyboardFavoriteRecentUiModel>
    ) {
        val areStickerPacksSame = stickerPacks == this.stickerPacks
        val areRecentStickersExist = recentStickers.size == this.recentStickers.size
        val areTabsTheSame = areStickerPacksSame && areRecentStickersExist
        this.stickerPacks.clear()
        this.stickerPacks.addAll(stickerPacks)
        this.recentStickers.clear()
        this.recentStickers.addAll(recentStickers)
        if (areTabsTheSame) return
        createTabsWithStickers()
    }

    fun dismissMenu() {
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun showMenuIfNotExpanded(startPosition: Int? = null) {
        if (!isExpanded) {
            bottomSheetBehavior.isHideable = false
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            onFirstStart(startPosition)
        }
    }

    fun onFirstStart(startPosition: Int? = null) {
        if (!isMenuInitialized) {
            initViews(startPosition)
        } else if (startPosition != null) {
            isPageChangeLoggedInAnalytic = true
            vpPages?.setCurrentItem(startPosition, false)
            changePage(startPosition)
        }
    }

    fun expandMenuFullScreen() {
        bottomSheetBehavior.isHideable = false
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun collapseMenuWhenHeaderClick() {
        interactionCallback.invoke(GifMenuCallbackEvents.OnHideKeyboard)
        collapseMenu()
    }

    fun showAddToFavoritesAnimation() {
        val newList = mutableListOf<MediaKeyboardTab>()
        tabItems.forEach {
            newList.add(
                MediaKeyboardTab(
                    drawableId = it.drawableId,
                    checked = it.checked,
                    stickerPack = it.stickerPack,
                    isDivider = it.isDivider
                )
            )
        }
        newList.firstOrNull { it.drawableId == DRAWABLE_FAVORITES }?.playAnimation = true
        tabsAdapter.submitList(newList)
    }

    fun collapseMenu() {
        bottomSheetBehavior.isHideable = false
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun switchState(state: GifMenuState) {
        refreshTabItems(state)
        pagesAdapter = createAdapterByState(state)
        isMenuInitialized = false
    }

    fun switchVisibilityDialogHeader(isEnabled: Boolean) {
        vgHeaderDialog?.isInvisible = !isEnabled
    }

    private fun createAdapterByState(state: GifMenuState): MeeraMediaKeyboardPagesAdapter {
        return MeeraMediaKeyboardPagesAdapter(
            state = state,
            fragment = fragment,
            openFrom = openFrom,
            pickerCallback = object : OnImagesReady {
                override fun onReady(image: Uri) = imagesCallback.onReady(image)

                override fun onReady(images: MutableList<Uri>?) = imagesCallback.onReady(images)

                override fun onReadyWithText(images: MutableList<out Uri>?, text: String?) {
                    imagesCallback.onReadyWithText(images, text)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }

                override fun onRequestChangeState(bottomSheetState: Int) {
                    bottomSheetBehavior.state = bottomSheetState
                }
            })
    }

    private fun refreshTabItems(state: GifMenuState) {
        tabItems.clear()
        tabItems.addAll(
            when (state) {
                GifMenuState.DEFAULT -> generateDefaultList()
                GifMenuState.MESSAGE_EDITING -> generateMessageEditingList()
                GifMenuState.CREATE_MOMENT -> generateMomentTabsList()
            }
        )
        when (state) {
            GifMenuState.DEFAULT, GifMenuState.CREATE_MOMENT -> createTabsWithStickers()
            GifMenuState.MESSAGE_EDITING -> createTabsMessageEditing()
        }
    }

    private fun generateDefaultList(): List<MediaKeyboardTab> {
        return listOf(
            MediaKeyboardTab(DRAWABLE_RECENTS, false),
            MediaKeyboardTab(DRAWABLE_FAVORITES, false),
            MediaKeyboardTab(DRAWABLE_PICKER, true),
            MediaKeyboardTab(DRAWABLE_GIF, false)
        )
    }

    private fun generateMomentTabsList(): List<MediaKeyboardTab> {
        return listOf(
            MediaKeyboardTab(DRAWABLE_FAVORITES, false),
            MediaKeyboardTab(DRAWABLE_PICKER, false),
        )
    }

    private fun generateMessageEditingList(): List<MediaKeyboardTab> {
        return listOf(MediaKeyboardTab(DRAWABLE_PICKER, true))
    }

    private fun createTabsMessageEditing() {
        tabsAdapter.submitList(tabItems)
    }

    private fun createTabsWithStickers() {
        val newList =
            tabItems.filterNot { it.drawableId == null || it.isRecentStickersTab || it.isWidgetsTab }.toMutableList()
        newList.add(MediaKeyboardTab(isDivider = true))

        if (needShowWidgets) newList.add(MediaKeyboardTab(drawableId = DRAWABLE_WIDGETS, checked = true))

        if (recentStickers.isNotEmpty()) {
            newList.add(MediaKeyboardTab(drawableId = DRAWABlE_RECENT_STICKERS))
        }

        val isCurrentPageStickers = vpPages?.currentItem == mediaKeyboardPagesPosition.stickers
        newList.addAll(
            stickerPacks.map {
                MediaKeyboardTab(
                    stickerPack = it,
                    checked = !needShowWidgets && currentStickerPack?.id == it.id && isCurrentPageStickers
                )
            }
        )
        if (isCurrentPageStickers && newList.none { it.checked }) {
            newList.firstOrNull { it.isStickerPackTab }?.checked = true
        }
        tabsAdapter.submitList(newList)
        tabItems.clear()
        tabItems += newList
        setTabCheckedByStickerPack()
    }

    private fun tabClicked(tab: MediaKeyboardTab) {
        isSelectPageBySwipe = false
        if (!tab.isStickerPackTab) currentStickerPack = null
        when {
            tab.isWidgetsTab -> {
                setTabCheckedByDrawableId(DRAWABLE_WIDGETS)
                proceedWidgetsClick()
            }

            tab.isRecentStickersTab -> {
                setTabCheckedByDrawableId(DRAWABlE_RECENT_STICKERS)
                proceedRecentStickersClick()
            }

            tab.isRegularTab -> {
                when (tab.drawableId) {
                    DRAWABLE_GIF -> {
                        amplitudeMediaKeyboardAnalytic.logMediaPanelGifOpen(
                            how = AmplitudeMediaKeyboardHowProperty.TAP_BUTTON
                        )
                        isPageChangeLoggedInAnalytic = true
                    }

                    DRAWABLE_PICKER -> {
                        amplitudeMediaKeyboardAnalytic.logMediaPanelMediaOpen(
                            how = AmplitudeMediaKeyboardHowProperty.TAP_BUTTON
                        )
                        isPageChangeLoggedInAnalytic = true
                    }
                }
                val item = tabItems.indexOf(tab)
                vpPages?.setCurrentItem(item, false)
            }

            tab.isStickerPackTab -> tab.stickerPack?.let(this::proceedStickerPackClick)
        }
    }

    private fun proceedWidgetsClick() {
        vpPages?.currentItem = mediaKeyboardPagesPosition.stickers
        fragment.doDelayed(STICKER_PACK_SCROLL_DELAY) {
            pagesAdapter.scrollStickersToWidgets()
        }
    }

    private fun proceedRecentStickersClick() {
        vpPages?.currentItem = POSITION_STICKERS
        fragment.doDelayed(STICKER_PACK_SCROLL_DELAY) {
            pagesAdapter.scrollStickersToRecent()
        }
    }

    private fun proceedStickerPackClick(stickerPack: MediaKeyboardStickerPackUiModel) {
        vpPages?.currentItem = mediaKeyboardPagesPosition.stickers
        fragment.lifecycleScope.launch {
            delay(STICKER_PACK_SCROLL_DELAY)
            pagesAdapter.scrollStickersToPack(stickerPack)
            delay(STICKER_PACK_TAB_DELAY)
            currentStickerPack = stickerPack
            setTabCheckedByStickerPack()
        }
    }

    private fun stickerPackViewed(stickerPack: MediaKeyboardStickerPackUiModel) {
        interactionCallback.invoke(GifMenuCallbackEvents.OnStickerPackViewed(stickerPack))
    }

    private fun initViews(startPosition: Int?) {
        initRecyclerView()
        initViewPager(startPosition)
        initBottomSheet()
    }

    private fun initRecyclerView() {
        (rvTabs?.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        rvTabs?.adapter = tabsAdapter
        if (stickerPacks.size == 0) tabsAdapter.submitList(tabItems)
    }

    private fun initViewPager(startPosition: Int?) {
        vpPages?.adapter = pagesAdapter
        val recyclerView = vpPages?.getRecyclerView()
        recyclerView?.isNestedScrollingEnabled = false
        recyclerView?.overScrollMode = View.OVER_SCROLL_NEVER
        isPageChangeLoggedInAnalytic = true
        if (startPosition != null) {
            vpPages?.post {
                isPageChangeLoggedInAnalytic = true
                vpPages.setCurrentItem(startPosition, false)
                changePage(position = startPosition)
                vpPages.registerOnPageChangeCallback(pageChangeCallback)
            }
        }
    }

    private fun changePage(position: Int) {
        when (position) {
            mediaKeyboardPagesPosition.recents -> {
                setTabCheckedByDrawableId(DRAWABLE_RECENTS)
                pagesAdapter.refreshRecents()
            }

            mediaKeyboardPagesPosition.favourites -> {
                setTabCheckedByDrawableId(DRAWABLE_FAVORITES)
            }

            mediaKeyboardPagesPosition.picker -> {
                setTabCheckedByDrawableId(DRAWABLE_PICKER)
                if (!isPageChangeLoggedInAnalytic) {
                    amplitudeMediaKeyboardAnalytic.logMediaPanelMediaOpen(
                        how = AmplitudeMediaKeyboardHowProperty.SWIPE
                    )
                }
                isPageChangeLoggedInAnalytic = false
            }

            mediaKeyboardPagesPosition.gif -> {
                setTabCheckedByDrawableId(DRAWABLE_GIF)
                if (!isPageChangeLoggedInAnalytic) {
                    amplitudeMediaKeyboardAnalytic.logMediaPanelGifOpen(
                        how = AmplitudeMediaKeyboardHowProperty.SWIPE
                    )
                }
                isPageChangeLoggedInAnalytic = false
            }

            mediaKeyboardPagesPosition.stickers -> when {
                !isSelectPageBySwipe -> setTabCheckedByStickerPack()
                needShowWidgets -> {
                    if (currentStickerPack != null) setTabCheckedByStickerPack()
                    else setTabCheckedByDrawableId(DRAWABLE_WIDGETS)
                }

                recentStickers.isNotEmpty() -> {
                    if (currentStickerPack != null) setTabCheckedByStickerPack()
                    else setTabCheckedByDrawableId(DRAWABlE_RECENT_STICKERS)
                }

                else -> setTabCheckedByStickerPack()
            }
        }
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            changePage(position)
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            isSelectPageBySwipe = true
        }
    }

    private fun initBottomSheet() {
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    val whereProperty = when (vpPages?.currentItem) {
                        POSITION_GIF -> AmplitudeMediaKeyboardWhereProperty.GIF
                        POSITION_PICKER -> AmplitudeMediaKeyboardWhereProperty.MEDIA_BLOCK
                        else -> return
                    }
                    amplitudeMediaKeyboardAnalytic.logPanelPull(
                        where = whereProperty
                    )
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset !in SLIDE_OFFSET_MIN..SLIDE_OFFSET_MAX) return
                pagesAdapter.onBottomSheetSlide(slideOffset)
            }
        })
    }

    private fun setTabCheckedByStickerPack() {
        if (this.currentStickerPack == null) return

        val newList = mutableListOf<MediaKeyboardTab>()
        tabItems.forEach {
            newList.add(
                MediaKeyboardTab(
                    drawableId = it.drawableId,
                    checked = it.checked,
                    stickerPack = it.stickerPack,
                    isDivider = it.isDivider
                )
            )
        }
        if (newList.firstOrNull { it.stickerPack?.id == currentStickerPack?.id }?.checked == true) return

        newList.firstOrNull { it.checked }?.checked = false
        newList.firstOrNull { it.stickerPack?.id == currentStickerPack?.id }?.checked = true

        tabsAdapter.submitList(newList)
        tabItems.clear()
        tabItems += newList
        isNotFirstStartMenu { scrollTabToCurrentItem() }
    }

    private fun setTabCheckedByDrawableId(drawableId: Int) {
        if (tabItems.none { it.drawableId == drawableId }) return
        val newList = mutableListOf<MediaKeyboardTab>()
        tabItems.forEach {
            newList.add(
                MediaKeyboardTab(
                    drawableId = it.drawableId,
                    checked = drawableId == it.drawableId,
                    stickerPack = it.stickerPack,
                    isDivider = it.isDivider
                )
            )
        }
        tabsAdapter.submitList(newList)
        tabItems.clear()
        tabItems += newList
        isNotFirstStartMenu { scrollTabToCurrentItem() }
    }

    private fun scrollTabToCurrentItem() {
        val layoutManager = rvTabs?.layoutManager as? LinearLayoutManager? ?: return
        val checkedItem = tabItems.indexOfFirst { it.checked }
        val currentFirstVisibleItem = layoutManager.findFirstCompletelyVisibleItemPosition()
        val currentLastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
        if (checkedItem !in currentFirstVisibleItem..currentLastVisibleItem) {
            rvTabs.scrollToPosition(checkedItem)
        }
    }

    private fun isNotFirstStartMenu(block: () -> Unit) {
        if (isMenuInitialized) block()
        isMenuInitialized = true
    }

    fun getLatestAdapterPosition(): Int = vpPages?.currentItem ?: 0

    private fun ViewPager2.getRecyclerView(): RecyclerView? {
        try {
            val field = ViewPager2::class.java.getDeclaredField("mRecyclerView")
            field.isAccessible = true
            return field.get(this) as RecyclerView
        } catch (e: NoSuchFieldException) {
            Timber.e(e)
        } catch (e: IllegalAccessException) {
            Timber.e(e)
        }
        return null
    }

}
