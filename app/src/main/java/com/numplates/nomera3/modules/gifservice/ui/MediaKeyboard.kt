package com.numplates.nomera3.modules.gifservice.ui

import android.graphics.Point
import android.net.Uri
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.adapter.MediaKeyboardPagesAdapter
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.adapter.MediaKeyboardTabsAdapter
import com.numplates.nomera3.modules.gifservice.ui.entity.GifMenuCallbackEvents
import com.numplates.nomera3.modules.maps.ui.snippet.view.ViewPagerBottomSheetBehavior
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

private const val DRAWABLE_RECENTS = R.drawable.ic_mediakeyboard_recents
private const val DRAWABLE_FAVORITES = R.drawable.ic_add_to_favorite
private const val DRAWABLE_GIF = R.drawable.ic_mediakeyboard_gif
private const val DRAWABLE_PICKER = R.drawable.ic_mediakeyboard_picker
const val DRAWABLE_WIDGETS = R.drawable.ic_widgets_tab
const val DRAWABlE_RECENT_STICKERS = R.drawable.happy

private const val STICKER_PACK_SCROLL_DELAY = 100L
private const val STICKER_PACK_TAB_DELAY = 10L
private const val SLIDE_OFFSET_MIN = 0F
private const val SLIDE_OFFSET_MAX = 1F

class MediaKeyboard(
    private val fragment: Fragment,
    private val bottomSheetBehavior: ViewPagerBottomSheetBehavior<out View>,
    private val rootView: ViewGroup,
    private val openFrom: MediaControllerOpenPlace,
    private val interactionCallback: (GifMenuCallbackEvents) -> Unit = { },
    private val imagesCallback: OnImagesReady,
    private val amplitudeMediaKeyboardAnalytic: AmplitudeMediaKeyboardAnalytic
) {

    private val needShowWidgets = openFrom == MediaControllerOpenPlace.Moments
    private val tabItems = if (needShowWidgets) generateMomentTabsList() else generateDefaultList()
    private val mediaKeyboardPagesPosition = if (needShowWidgets) {
        MediaKeyboardPagesPosition.WIDGETS
    } else {
        MediaKeyboardPagesPosition.DEFAULT
    }

    private val tabsAdapter: MediaKeyboardTabsAdapter by lazy {
        MediaKeyboardTabsAdapter(
            callback = ::tabClicked,
            stickerPackViewedCallback = ::stickerPackViewed,
            useDarkMode = needShowWidgets
        )
    }

    private var pagesAdapter: MediaKeyboardPagesAdapter = createAdapterByState(
        if (needShowWidgets) GifMenuState.CREATE_MOMENT else GifMenuState.DEFAULT
    )
    private var isSelectPageBySwipe = false

    private val rvTabs = rootView.findViewById<RecyclerView>(R.id.rv_tabs)
    private val vpPages = rootView.findViewById<ViewPager2>(R.id.vp_pages)

    val favoritesTabPosition: Point?
        get() {
            val view = rvTabs?.getChildAt(mediaKeyboardPagesPosition.favourites) ?: return null
            val position = IntArray(2)
            view.getLocationInWindow(position)
            return Point(position[0], position[1])
        }

    private var isMenuInitialized = false

    private var isPageChangeLoggedInAnalytic = false

    private var keyboardHeight = 0
    private var currentStickerPack: MediaKeyboardStickerPackUiModel? = null
    val stickerPacks = mutableListOf<MediaKeyboardStickerPackUiModel>()
    private val recentStickers = mutableListOf<MediakeyboardFavoriteRecentUiModel>()

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

    fun onStartAnimationTransitionFragment() {
        pagesAdapter.onStartAnimationTransitionFragment()
    }

    fun onOpenTransitionFragment() {
        pagesAdapter.onOpenTransitionFragment()
    }

    fun setKeyboardHeight(height: Int) {
        this.keyboardHeight = height
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
        }
    }

    fun expandMenuFullScreen() {
        bottomSheetBehavior.isHideable = false
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun collapseMenuWhenHeaderClick() {
        interactionCallback.invoke(GifMenuCallbackEvents.OnHideKeyboard)
        fragment.lifecycle.doDelayed(GIF_MENU_UI_REACTION_DELAY) {
            collapseMenu()
        }
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

    private fun createAdapterByState(state: GifMenuState): MediaKeyboardPagesAdapter {
        return MediaKeyboardPagesAdapter(
            state = state,
            fragment = fragment,
            openFrom = openFrom,
            pickerCallback = object : OnImagesReady {
                override fun onReady(image: Uri) = imagesCallback.onReady(image)

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
            GifMenuState.DEFAULT,
            GifMenuState.CREATE_MOMENT -> createTabsWithStickers()
            GifMenuState.MESSAGE_EDITING -> createTabsMessageEditing()
        }
    }

    private fun generateDefaultList() = mutableListOf(
        MediaKeyboardTab(DRAWABLE_RECENTS, false),
        MediaKeyboardTab(DRAWABLE_FAVORITES, false),
        MediaKeyboardTab(DRAWABLE_PICKER, true),
        MediaKeyboardTab(DRAWABLE_GIF, false)
    )

    private fun generateMomentTabsList() = mutableListOf(
        MediaKeyboardTab(DRAWABLE_FAVORITES, false),
        MediaKeyboardTab(DRAWABLE_PICKER, false),
    )

    private fun generateMessageEditingList(): List<MediaKeyboardTab> {
        return listOf(MediaKeyboardTab(DRAWABLE_PICKER, true))
    }

    private fun createTabsMessageEditing() {
        tabsAdapter.submitList(tabItems)
    }

    private fun createTabsWithStickers() {
        tabItems.removeAll { it.drawableId == null || it.isRecentStickersTab || it.isWidgetsTab }
        tabItems.add(MediaKeyboardTab(isDivider = true))

        if (needShowWidgets) tabItems.add(MediaKeyboardTab(drawableId = DRAWABLE_WIDGETS, checked = true))

        if (recentStickers.isNotEmpty()) {
            tabItems.add(MediaKeyboardTab(drawableId = DRAWABlE_RECENT_STICKERS))
        }
        val isCurrentPageStickers = vpPages?.currentItem == mediaKeyboardPagesPosition.stickers
        tabItems.addAll(
            stickerPacks.map {
                MediaKeyboardTab(
                    stickerPack = it,
                    checked = !needShowWidgets && currentStickerPack?.id == it.id && isCurrentPageStickers
                )
            }
        )
        if (isCurrentPageStickers && tabItems.none { it.checked }) {
            tabItems.firstOrNull { it.isStickerPackTab }?.checked = true
        }
        tabsAdapter.submitList(tabItems)
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
        vpPages?.currentItem = mediaKeyboardPagesPosition.stickers
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
        rvTabs?.adapter = tabsAdapter
        if (stickerPacks.size == 0) tabsAdapter.submitList(tabItems)
    }

    private fun initViewPager(startPosition: Int?) {
        vpPages?.adapter = pagesAdapter
        val recyclerView = vpPages?.getRecyclerView()
        recyclerView?.isNestedScrollingEnabled = false
        recyclerView?.overScrollMode = View.OVER_SCROLL_NEVER
        if (startPosition != null) {
            vpPages?.post {
                vpPages.registerOnPageChangeCallback(pageChangeCallback)
                isPageChangeLoggedInAnalytic = true
                vpPages.setCurrentItem(startPosition, false)

            }
        }
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
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
                mediaKeyboardPagesPosition.stickers -> {
                    when {
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
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            isSelectPageBySwipe = true
        }
    }

    private fun initBottomSheet() {
        bottomSheetBehavior.addBottomSheetCallback(object : ViewPagerBottomSheetBehavior.BottomSheetCallback() {
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

        vpPages?.getRecyclerView()?.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                rv.parent.requestDisallowInterceptTouchEvent(true)
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) = Unit

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) = Unit
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
