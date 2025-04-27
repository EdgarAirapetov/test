package com.numplates.nomera3.modules.tags.ui.base

import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.meera.core.extensions.dp
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType.ROAD
import com.numplates.nomera3.modules.tags.ui.adapter.SuggestedTagListAdapterNew
import com.numplates.nomera3.modules.tags.ui.entity.SuggestedTagListUIModel
import com.numplates.nomera3.modules.tags.ui.entity.SuggestedTagListUIModel.HashtagUIModel
import com.numplates.nomera3.modules.tags.ui.entity.SuggestedTagListUIModel.UniqueNameUIModel
import com.numplates.nomera3.modules.tags.ui.viewmodel.TagMenuViewModelNew
import com.numplates.nomera3.modules.tags.ui.viewmodel.TagMenuViewModelNew.TagMenuEvent.OnErrorLoad
import com.numplates.nomera3.modules.tags.ui.viewmodel.TagMenuViewModelNew.TagMenuEvent.OnSuggestedHashtagListLoaded
import com.numplates.nomera3.modules.tags.ui.viewmodel.TagMenuViewModelNew.TagMenuEvent.OnSuggestedUniqueNameListLoaded
import com.numplates.nomera3.presentation.view.fragments.HorizontalLineDivider
import com.numplates.nomera3.presentation.view.ui.edittextautocompletable.EditTextAutoCompletable
import com.numplates.nomera3.presentation.view.ui.edittextautocompletable.SuggestionMenuView
import com.numplates.nomera3.presentation.view.ui.edittextautocompletable.TagType
import com.numplates.nomera3.presentation.view.ui.edittextautocompletable.TagType.HASHTAG
import com.numplates.nomera3.presentation.view.ui.edittextautocompletable.TagType.UNDEFINED
import com.numplates.nomera3.presentation.view.ui.edittextautocompletable.TagType.UNIQUE_NAME
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SuggestedTagListMenu(
    private val fragment: Fragment,
    private var editText: EditTextAutoCompletable?,
    private val recyclerView: RecyclerView,
    private val bottomSheetBehavior: BottomSheetBehavior<View>,
    private val chatRoomId: Long? = null,
    private var fullscreenTagsList: Boolean = false
) : SuggestionMenuView {

    val isHidden: Boolean
        get() = bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN

    val menuBottomSheetBehavior: BottomSheetBehavior<View>
        get() = bottomSheetBehavior

    var suggestedTagListMenuType: SuggestionsMenuType
        get() = _suggestedTagListMenuType
        set(value) {
            viewModel?.suggestedTagListMenuType = value // ? нужен
            _suggestedTagListMenuType = value
        }

    private val viewModel by fragment.viewModels<TagMenuViewModelNew>()

    private var searchTagJob: Job? = null
    private var currentTagType: TagType = UNDEFINED
    private var suggestedTagListAdapter: SuggestedTagListAdapterNew? = null
    private var _suggestedTagListMenuType: SuggestionsMenuType = ROAD
    private var dismissListener: (()-> Unit)? = null
    private var adapterItemClickListener: ((SuggestedTagListUIModel)-> Unit)? = null
    private var lifecycleObserver : LifecycleObserver? = null

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

    init {
        dismissMenu()
        initLifecycleObserver()
        initRecyclerView()
        initViewModelObservers()
    }

    override fun setEditText(editText: EditTextAutoCompletable?) {
        this.editText = editText
    }

    override fun clearResources() {
        dismissListener = null
        editText?.clearResources()
        editText = null
        searchTagJob?.cancel()
        searchTagJob = null
        recyclerView.adapter = null
        recyclerView.layoutManager = null
        recyclerView.clearOnScrollListeners()
        recyclerView.invalidateItemDecorations()
        lifecycleObserver?.let { observer ->
            fragment.lifecycle.removeObserver(observer)
            lifecycleObserver = null
        }
        adapterItemClickListener = null
        suggestedTagListAdapter = null
    }

    fun setOnDismissListener(listener: ()-> Unit) {
        dismissListener = listener
    }

    fun dismissMenu() {
        dismissListener?.invoke()
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        suggestedTagListAdapter?.clearAdapter()
    }

    private fun showMenu() {
        val showState = if (fullscreenTagsList) STATE_EXPANDED else STATE_COLLAPSED
        bottomSheetBehavior.state = showState
    }

    fun setExtraPeekHeight(newExtraPeekHeight: Int, isAnimate: Boolean) {
        currentExtraPeekHeight = newExtraPeekHeight
        bottomSheetBehavior.setPeekHeight(currentContentPeekHeight + currentExtraPeekHeight, isAnimate)
    }

    private fun initRecyclerView() {
        suggestedTagListAdapter = SuggestedTagListAdapterNew()
        adapterItemClickListener = { model: SuggestedTagListUIModel ->
            when (model) {
                is HashtagUIModel -> {
                    editText?.replaceHashtagBySuggestion(model.name)
                }
                is UniqueNameUIModel -> editText?.replaceUniqueNameBySuggestion(model.uniqueName!!)
            }

            dismissMenu()
        }
        suggestedTagListAdapter?.setOnItemClickListener(adapterItemClickListener)

        recyclerView.adapter = suggestedTagListAdapter
        recyclerView.layoutManager = LinearLayoutManager(fragment.context)

        // добавить разделитель
        fragment.context
            ?.let { ContextCompat.getDrawable(it, R.drawable.tag_list_item_divider_shape) }
            ?.let { shape -> HorizontalLineDivider(shape, 16.dp, 0) }
            ?.also { dividerExceptLastItem -> recyclerView.addItemDecoration(dividerExceptLastItem) }
    }

    private fun initLifecycleObserver() {
        lifecycleObserver = object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun closeMenu() {
                dismissMenu()
                fragment.lifecycle.removeObserver(this)
            }
        }.also { observer ->
            fragment.lifecycle.addObserver(observer)
        }
    }

    private fun initViewModelObservers() {
        viewModel.events.observe(fragment.viewLifecycleOwner, Observer { event ->
            when (event) {
                is OnErrorLoad -> {

                }
                is OnSuggestedHashtagListLoaded -> {
                    currentTagType = HASHTAG
                    showTagListView(event.suggestedHashtagList)
                }
                is OnSuggestedUniqueNameListLoaded -> {
                    currentTagType = UNIQUE_NAME
                    showTagListView(event.suggestedUniqueNameList)
                }
            }
        })
    }

    private fun getNewPeekHeight(newListSize: Int): Int {
        return when (newListSize) {
            1 -> 60.dp
            2 -> 120.dp
            else -> 160.dp
        }
    }

    private fun showTagListView(tags: List<SuggestedTagListUIModel>) {
        if (editText?.text.isNullOrEmpty()) {
            return
        }

        if (tags.isEmpty()) {
            dismissMenu()
            return
        }

        currentContentPeekHeight = getNewPeekHeight(tags.size)
        bottomSheetBehavior.setPeekHeight(currentContentPeekHeight + currentExtraPeekHeight, true)
        showMenu()
        recyclerView.post {
            suggestedTagListAdapter?.setTagList(tags)
            recyclerView.scrollToPosition(0)
        }
        if (tags.all { it is HashtagUIModel }) bottomSheetBehavior.isHideable = false
    }

    override fun onBackPressed(): Boolean? {
        if (bottomSheetBehavior.state == STATE_EXPANDED || bottomSheetBehavior.state == STATE_COLLAPSED) {
            dismissMenu()
            return true
        } else {
            return null
        }
    }

    override fun getSuggestedUniqueNameList(uniqueName: String) {
        searchTagJob?.cancel()
        searchTagJob = fragment.lifecycleScope.launch {
            delay(100)
            viewModel.getSuggestedUniqueNameList(text = uniqueName)
        }
    }

    override fun getSuggestedUniqueNameListInGroupChat(uniqueName: String, chatRoomId: Long) {
        searchTagJob?.cancel()
        searchTagJob = fragment.lifecycleScope.launch {
            delay(200)
            viewModel.getSuggestedUniqueNameListInGroupChat(text = uniqueName, chatRoomId = chatRoomId.toInt())
        }
    }

    override fun dismiss() {
        if (!isHidden) {
            dismissMenu()
        }
    }

    override fun getSuggestedTagList(tag: String, tagType: TagType) {
        when (tagType) {
            UNIQUE_NAME -> {
                if (chatRoomId != null) {
                    getSuggestedUniqueNameListInGroupChat(tag, chatRoomId)
                } else {
                    getSuggestedUniqueNameList(tag)
                }
            }
            HASHTAG -> {
                getSuggestedHashtagList(tag)
            }
            else -> {

            }
        }
    }

    private fun getSuggestedHashtagList(tag: String) {
        searchTagJob?.cancel()
        searchTagJob = fragment.lifecycleScope.launch {
            delay(200)
            viewModel.getSuggestedHashtagList(tag)
        }
    }

    override fun dismiss(type: TagType) {
        if (!isHidden && currentTagType == type) {
            currentTagType = UNDEFINED
            dismissMenu()
        }
    }
}
