package com.numplates.nomera3.modules.tags.ui.base

import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.bottomsheets.SuggestionsMenuContract
import com.meera.core.extensions.dp
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType.ROAD
import com.numplates.nomera3.modules.tags.ui.TagViewEvent
import com.numplates.nomera3.modules.tags.ui.adapter.MeeraTagsListAdapter
import com.numplates.nomera3.modules.tags.ui.entity.UITagEntity
import com.numplates.nomera3.modules.tags.ui.viewmodel.TagMenuViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Выпадающее меню быстрого выбора уникального имени
 *
 * P.S. весь закомментированный код убран в сниппеты https://git.nomera.com/nomera/NUMAD/-/snippets
 * */
class SuggestionsMenu(
        private val fragment: Fragment,
        private val type: SuggestionsMenuType = ROAD,
        private val isDarkMode: Boolean = true
): SuggestionsMenuContract {

    override val isHidden: Boolean
        get() = bottomSheetBehavior?.state == BottomSheetBehavior.STATE_HIDDEN

    val menuBottomSheetBehavior: BottomSheetBehavior<View>?
        get() = bottomSheetBehavior

    /**
     * Коллбек срабатывающий при нажатии на плашку пользователя из выпадающего списка
     * */
    var onSuggestedUniqueNameClicked: ((UITagEntity) -> Unit)? = null

    override var suggestedUniqueNameClicked: ((SuggestionsMenuContract.UITagEntity) -> Unit)? = null

    /**
     * Job на запрос поиска рекомендаций по уникальному имени
     * */
    private var searchUsersByUniqueNameJob: Job? = null

    /**
     * В меню репостов клавиатура сдвигает вверх и вниз текстовое поле вместе с suggestion меню.
     * При этом если количество контента в suggestion меню меняется, мы должны подстраивать его
     * высоту.
     *
     * Чтобы всё это корректно работало, заведены две переменные:
     *
     * 1. currentContentPeekHeight - насколько надо поднять suggestion меню поверх инпута, чтобы
     *    отображение контента было по высоте контента
     *
     * 2. currentExtraPeekHeight - насколько надо дополнительно поднять suggestion меню, чтоб он
     *    оставался поверх инпута
     * */
    private var currentContentPeekHeight: Int = 160.dp
    private var currentExtraPeekHeight: Int = 0.dp

    private var recyclerView: RecyclerView? = null
    private var editText: EditText? = null
    private val disposables = CompositeDisposable()
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null
    private var meeraTagListAdapter: MeeraTagsListAdapter? = null
    private var draggingEnabled = false

    private val viewModel by fragment.viewModels<TagMenuViewModel>()

    override fun init(recyclerView: RecyclerView, editText: EditText?, bottomSheetBehavior: BottomSheetBehavior<View>) {
        this.recyclerView = recyclerView
        this.editText = editText
        this.bottomSheetBehavior = bottomSheetBehavior
        forceCloseMenu()

        if (fragment is LifecycleOwner) {
            //слушатель на жизненый цикл
            fragment.lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_START)
                fun connectListener() = Unit

                @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
                fun disconnectListener() {
                    disposables.clear()
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun closeMenu() {
                    forceCloseMenu()
                }
            })
        }


        initObservers()
        meeraTagListAdapter = MeeraTagsListAdapter(isDarkMode)
        meeraTagListAdapter?.onTagClick = { onTagClicked(it) }
        recyclerView.adapter = meeraTagListAdapter
        recyclerView.layoutManager = LinearLayoutManager(fragment.context)
    }

    override fun setDarkColored() {
//        fragment.context?.let { context ->
//            recyclerView?.setBackgroundColor(
//                ContextCompat.getColor(context, R.color.black_preview_toolbar)
//            )
//            meeraTagListAdapter?.isDarkColoredBackground = true
//        }
    }

    /**
     *
     * */
    override fun setExtraPeekHeight(newExtraPeekHeight: Int, isAnimate: Boolean) {
        currentExtraPeekHeight = newExtraPeekHeight
        bottomSheetBehavior?.setPeekHeight(
            currentContentPeekHeight + currentExtraPeekHeight,
            isAnimate
        )
    }

    private fun onTagClicked(tag: UITagEntity) {
        onSuggestedUniqueNameClicked?.invoke(tag)
        suggestedUniqueNameClicked?.invoke(SuggestionsMenuContract.UITagEntity(
            id = tag.id,
            image = tag.image,
            uniqueName = tag.uniqueName,
            userName = tag.userName,
            isMale = tag.isMale,
            isVerified = tag.isVerified
        ))
    }

    /*
    * инициализация обсерверов
    * */
    private fun initObservers() {
        fragment.viewLifecycleOwner.let { lifecycleOwner ->
            viewModel.liveTags.observe(lifecycleOwner, Observer {
                handleNewList(it)
            })

            viewModel.liveViewEventsTagMenu.observe(lifecycleOwner, Observer {
                handleViewEvent(it)
            })
        }
    }

    /**
     *
     * */
    private fun getNewPeekHeight(newListSize: Int): Int {
        return when (newListSize) {
            1 -> 60.dp
            2 -> 120.dp
            else -> 160.dp
        }
    }

    private fun handleNewList(tags: List<UITagEntity>) {
        if (editText?.text.isNullOrEmpty()) return

        currentContentPeekHeight = getNewPeekHeight(tags.size)

        bottomSheetBehavior?.setPeekHeight(currentContentPeekHeight + currentExtraPeekHeight, true)

        meeraTagListAdapter?.submitList(tags)
        forceOpenMenu()

        recyclerView?.scrollToPosition(0)
    }

    private fun handleViewEvent(event: TagViewEvent) {
        when (event) {
            is TagViewEvent.HideMenu -> {
                forceCloseMenu()
                meeraTagListAdapter?.clearAdapter()
            }
        }
    }


    /**
     * Найти рекомендации по уникальному имени
     * */
    override fun searchUsersByUniqueName(uniqueName: String?) {
        if (uniqueName != null && uniqueName.isNotBlank() && uniqueName.isNotEmpty()) {
            searchUsersByUniqueNameJob?.cancel()
            searchUsersByUniqueNameJob = fragment.lifecycleScope.launch {
                delay(100)
                viewModel.getTags(text = uniqueName, type = type)
            }
        } else {
            forceCloseMenu()
        }
    }

    /**
     * Найти рекомендации по уникальному имени в чат группе
     * */
    fun searchUsersByUniqueName(uniqueName: String?, chatRoomId: Long) {
        if (uniqueName != null && uniqueName.isNotBlank() && uniqueName.isNotEmpty()) {
            searchUsersByUniqueNameJob?.cancel()
            searchUsersByUniqueNameJob = fragment.lifecycleScope.launch {
                delay(200)
                viewModel.getUniqueNameSuggestionsInGroupChat(
                    text = uniqueName,
                    type = type,
                    chatRoomId = chatRoomId.toInt()
                )
            }
        } else {
            forceCloseMenu()
        }
    }

    /*
    * Дополнительная обработка кнопки нажатия назад
    * если меню открыто закрыть его
    * */
    fun onBackPressed(): Boolean? {
        return if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED
            || bottomSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED
        ) {
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            true
        } else {
            null
        }
    }

    override fun forceCloseMenu() {
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun forceOpenMenu() {
        draggingEnabled = true

        //if (tagListAdapter?.itemCount ?: 0 >= 3) {

        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED

        //} else {
        //    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        //}
    }
}
