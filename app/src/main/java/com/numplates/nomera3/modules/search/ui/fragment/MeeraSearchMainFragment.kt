package com.numplates.nomera3.modules.search.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.orFalse
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.uikit.widgets.dp
import com.meera.uikit.widgets.navigation.UiKitNavigationBarViewVisibilityState
import com.noomeera.nmrmediatools.utils.setThrottledClickListener
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.databinding.MeeraFragmentSearchMainBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriendsWhereProperty
import com.numplates.nomera3.modules.newroads.data.entities.FilterSettingsProvider
import com.numplates.nomera3.modules.peoples.ui.entity.PeopleUiEffect
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.search.filters.ui.fragment.MeeraCityFilterResultCallback
import com.numplates.nomera3.modules.search.filters.ui.fragment.MeeraFilterBottomSheetDialog
import com.numplates.nomera3.modules.search.filters.ui.fragment.MeeraFilterCallback
import com.numplates.nomera3.modules.search.filters.ui.fragment.MeeraFilterCitiesBottomSheet
import com.numplates.nomera3.modules.search.ui.adapter.MeeraSearchPagerAdapter
import com.numplates.nomera3.modules.search.ui.viewmodel.user.SearchUserViewModel
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.FilterResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_TIME_MS = 500L

private const val TAG_FILTER_CITIES = "MeeraFilterCitiesBottomSheet"
private const val TAG_FILTERS = "MeeraFilterBottomSheetDialog"
private const val MARGIN_SEARCH_DEFAULT = 16

const val ARG_KEY_DIALOG_MODE = "ArgKeyDialogMode"

class MeeraSearchMainFragment : MeeraBaseDialogFragment(R.layout.meera_fragment_search_main, ScreenBehaviourState.Full),
    MeeraCityFilterResultCallback,
    MeeraFilterCallback,
    IOnBackPressed {

    private var currentQuery: String? = null
    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentSearchMainBinding::bind)

    private var searchJob: Job? = null

    private val userSearchViewModel by viewModels<SearchUserViewModel>(
        factoryProducer = { App.component.getViewModelFactory() },
        ownerProducer = { this }
    )

    private var keyboardHeightProvider: KeyboardHeightProvider? = null
    private var wasKeyboardOpenedAtStart = false

    private val openedFromPeoples by lazy {
        arguments?.getBoolean(KEY_FROM_PEOPLES, false) ?: false
    }

    private val listFragments by lazy {
        listOf<Fragment>(
            MeeraSearchUserFragment.newInstance(openedFromPeoples),
            MeeraSearchGroupFragment(),
            MeeraSearchHashTagFragment()
        )
    }

    private var selectedTab: Int = 0

    private var isClearTextMode = false
    private var filterByNumberApplied = false
    private var pagerAdapter: MeeraSearchPagerAdapter? = null
    private var searchFilter: MeeraFilterBottomSheetDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logOnFindFriendsPressed()

        val fromMap = arguments?.getBoolean(IArgContainer.ARG_SEARCH_FROM_MAP, FROM_MAP).orFalse()

        notFromMap = fromMap.not()

        selectedTab = arguments?.getInt(IArgContainer.ARG_SEARCH_OPEN_PAGE, PAGE_SEARCH_PEOPLE)
            ?: PAGE_SEARCH_PEOPLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSearchInput()
        initViews()
        checkIfOpeningFromPeoples()
        initViewPager()
        observeEffect()
        initSearchInputObservers()
    }

    override fun onStart() {
        super.onStart()
        startKeyboardHeightObserving()
        binding.vpSearchMain.currentItem = selectedTab
        updateSelectedTab(getCurrentFragment())
    }

    override fun onStateChanged(newState: Int) {
        if (newState == BottomSheetBehavior.STATE_HIDDEN && !notFromMap) {
            NavigationManager.getManager().isMapMode = true
            NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().stateVisibility =
                UiKitNavigationBarViewVisibilityState.VISIBLE
            findNavController().popBackStack()
        }
        super.onStateChanged(newState)
    }

    override fun onPause() {
        super.onPause()
        this.currentQuery = binding.isSearchMain.searchInputText
    }

    override fun onStop() {
        super.onStop()
        keyboardHeightProvider?.release()
    }

    override fun onSelectCityClick() {
        MeeraFilterCitiesBottomSheet.newInstance(
            saveResult = false,
            filterType = FilterSettingsProvider.FilterType.Main
        ).show(childFragmentManager, TAG_FILTER_CITIES)
    }

    override fun onGetCitiesResult(cities: List<City>) {
        searchFilter?.onCitySearchComplete(cities)
    }

    override fun onFilterResult(result: FilterResult?) {
        userSearchViewModel.setFilterResult(result)
        binding.vFiltersSearchApplied.isVisible = result != null
        if (filterByNumberApplied) {
            filterByNumberApplied = false
            binding.vNumberSearchApplied.gone()
            userSearchViewModel.setNumberFilter(null)
            userSearchViewModel.resetPaging()
        }
        onFilterApplySearch()
    }

    private fun observeEffect() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    userSearchViewModel.peoplesContentEvent.collect(::handleUiEffect)
                }
            }
        }
    }

    private fun handleUiEffect(effect: PeopleUiEffect) {
        when (effect) {
            is PeopleUiEffect.ApplyNumberSearchParams -> {
                binding.vNumberSearchApplied.visible()
                binding.vFiltersSearchApplied.gone()
                filterByNumberApplied = true
                getCurrentFragment().showAndLoadSearchScreen(effect.numberSearchParams)
            }

            else -> Unit
        }
    }

    private fun logOnFindFriendsPressed() {
        val openedFromWhere = arguments?.getSerializable(IArgContainer.ARG_FIND_FRIENDS_OPENED_FROM_WHERE)
            as? AmplitudeFindFriendsWhereProperty
        if (openedFromWhere != null) {
            userSearchViewModel.logOnFindFriendsPressed(openedFromWhere)
        }
    }

    private fun startKeyboardHeightObserving() {
        keyboardHeightProvider?.start()
        keyboardHeightProvider?.observer = { height ->
            val isKeyboardOpened = height > 0
            val isSearchBlank = binding.isSearchMain.searchInputText.isBlank()
            val isSearchHasFocus = binding.isSearchMain.hasFocus()
            val isCurrentFragmentSearchUser = getCurrentFragment() is MeeraSearchUserFragment
            if (!isKeyboardOpened && isSearchBlank && !isSearchHasFocus && isCurrentFragmentSearchUser) {
                binding.btnFilters.visible()
                binding.btnSearchByNumber.visible()
            }
        }
    }

    private fun initSearchInput() {
        binding.isSearchMain.searchInputText = currentQuery ?: String.empty()
        binding.btnFilters.isVisible = binding.isSearchMain.searchInputText.isEmpty() && getCurrentFragment() is MeeraSearchUserFragment
        binding.btnSearchByNumber.isVisible = binding.isSearchMain.searchInputText.isEmpty() && getCurrentFragment() is MeeraSearchUserFragment
    }

    private fun initViews() {
        binding.root.let { root -> keyboardHeightProvider = KeyboardHeightProvider(root) }
        binding.nvSearchMain.backButtonClickListener = {
            getCurrentFragment().exitScreen()
            context?.hideKeyboard(requireView())
            doDelayed(100) {
                findNavController().popBackStack()
            }
        }

        binding.isSearchMain.setCloseButtonClickedListener {
            if (getCurrentFragment() is MeeraSearchUserFragment) {
                binding.btnFilters.visible()
                binding.btnSearchByNumber.visible()
            }
        }

        binding.isSearchMain.setClearButtonClickedListener {
            if (isClearTextMode) {
                binding.isSearchMain.clear()
            } else {
                insertUniqueSign()
            }
        }

        binding.isSearchMain.focusChangedListener = { hasFocus ->
            if (hasFocus || binding.isSearchMain.searchInputText.isNotBlank()) {
                binding.btnFilters.gone()
                binding.btnSearchByNumber.gone()
                binding.vFiltersSearchApplied.gone()
                binding.vNumberSearchApplied.gone()
            } else if (getCurrentFragment() is MeeraSearchUserFragment) {
                binding.btnFilters.visible()
                binding.btnSearchByNumber.visible()
                if (userSearchViewModel.getFilterResult() != null) {
                    binding.vFiltersSearchApplied.visible()
                }
            }
        }

        binding.btnSearchByNumber.setThrottledClickListener {
            userSearchViewModel.amplitudeHelper.logTapSearchByNumberButton()
            showNumberDialog()
        }

        binding.btnFilters.setThrottledClickListener {
            showFilterDialog()
        }

        if (!notFromMap) {
            binding.nvSearchMain.showBackArrow = false
            binding.isSearchMain.setMargins(start = MARGIN_SEARCH_DEFAULT.dp)
            binding.grabber.visible()
        }
    }

    private fun checkIfOpeningFromPeoples() {
        if (!openedFromPeoples || wasKeyboardOpenedAtStart) return
        wasKeyboardOpenedAtStart = true
        requestFocusAndOpenKeyboard()
    }

    private fun requestFocusAndOpenKeyboard() {
        binding.isSearchMain.etInput.run {
            requestFocus()

            val imm =
                context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager?
            imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun insertUniqueSign() {
        userSearchViewModel.amplitudeHelper.logTapSearchAtSign()
        binding.isSearchMain.apply {
            val currentText = searchInputText
            if (currentText.indexOf(AT_SIGN) != 0) {
                etInput.setText("$AT_SIGN$currentText")
                etInput.setSelection(1)
                requestFocusAndOpenKeyboard()
            } else {
                clear()
            }
        }
    }

    private fun initViewPager() {
        pagerAdapter = MeeraSearchPagerAdapter(childFragmentManager)
        pagerAdapter?.addTitles(
            mutableListOf(
                getString(R.string.search_tab_people),
                getString(R.string.search_tab_community),
                getString(R.string.search_tab_hashtags)
            )
        )

        pagerAdapter?.addFragments(listFragments)
        binding.vpSearchMain.apply {
            offscreenPageLimit = listFragments.size
            adapter = pagerAdapter
            binding.rtlSearchMain.setupWithViewPager(this)
        }

        binding.vpSearchMain.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) = Unit

            override fun onPageSelected(position: Int) {
                val previousFragment = listFragments[selectedTab] as SearchScreenContext

                selectedTab = position

                previousFragment.hideMessages()

                updateSelectedTab(getCurrentFragment())
            }

            override fun onPageScrollStateChanged(state: Int) = Unit
        })

        updateSelectedTab(getCurrentFragment())
    }

    private fun initSearchInputObservers() {
        binding.isSearchMain.doAfterSearchTextChanged { inputText ->
            val text = inputText.trim()
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(SEARCH_DEBOUNCE_TIME_MS)
                onTextInputSearch(text)
            }
        }
        if (binding.isSearchMain.searchInputText.isNotEmpty()) {
            return
        }
        doDelayed(500) {
            onTextInputSearch(String.empty())
        }
    }


    private fun updateSelectedTab(fragment: SearchScreenContext) {
        binding.apply {
            when (fragment) {
                is MeeraSearchUserFragment -> {
                    isSearchMain.setHint(getString(R.string.search_by_users))
                    val isSearchBlank = binding.isSearchMain.searchInputText.isBlank()
                    val isSearchHasFocus = binding.isSearchMain.hasFocus()
                    if (!isSearchHasFocus && isSearchBlank) {
                        btnSearchByNumber.visible()
                        btnFilters.visible()
                        if (userSearchViewModel.getFilterResult() != null) {
                            vFiltersSearchApplied.visible()
                        }
                    }
                    onTabChangedSearch()
                }

                is MeeraSearchGroupFragment -> {
                    isSearchMain.setHint(getString(R.string.search_by_groups))
                    btnSearchByNumber.gone()
                    btnFilters.gone()
                    vFiltersSearchApplied.gone()
                    vNumberSearchApplied.gone()
                    onTabChangedSearch()
                }

                is MeeraSearchHashTagFragment -> {
                    isSearchMain.setHint(getString(R.string.search_by_hashtags))
                    btnSearchByNumber.gone()
                    btnFilters.gone()
                    onTabChangedSearch()
                }
            }

        }
    }

    private fun onFilterApplySearch() {
        val currentInputHeaderQuery = binding.isSearchMain.searchInputText

        if (currentInputHeaderQuery.isEmpty() && userSearchViewModel.getFilterResult() == null) {
            getCurrentFragment().clearCurrentResult()

            getCurrentFragment().setScreenState(SearchScreenContext.ScreenState.Default)
            return
        }

        if (currentInputHeaderQuery.isEmpty() && userSearchViewModel.getFilterResult() != null) {
            getCurrentFragment().blankSearch()
        } else {
            getCurrentFragment().search(currentInputHeaderQuery)
        }
    }

    private fun onTextInputSearch(query: String) {
        if (filterByNumberApplied) {
            filterByNumberApplied = false
            binding.vNumberSearchApplied.gone()
            userSearchViewModel.setNumberFilter(null)
            userSearchViewModel.resetPaging()
        }
        layoutTopButtons()

        if (getCurrentFragment() is SearchUserFragmentNew
            && query.isEmpty()
            && userSearchViewModel.getFilterResult() != null
        ) {
            getCurrentFragment().blankSearch()
        } else {
            getCurrentFragment().search(query)
        }
    }

    private fun onTabChangedSearch() {
        layoutTopButtons()

        if (getCurrentFragment().getFragmentLifecycle().currentState.isAtLeast(Lifecycle.State.STARTED).not()) {
            return
        }

        val currentInputHeaderQuery = binding.isSearchMain.searchInputText

        if (getCurrentFragment() is SearchUserFragmentNew
            && currentInputHeaderQuery.isEmpty()
            && userSearchViewModel.getFilterResult() != null
        ) {
            return
        }

        getCurrentFragment().search(currentInputHeaderQuery)
    }

    private fun layoutTopButtons() {
        val currentInputHeaderQuery = binding.isSearchMain.searchInputText

        binding.apply {
            if (currentInputHeaderQuery.isNotEmpty()
                && currentInputHeaderQuery != SHARP_SIGN
            ) {
                isSearchMain.rightButtonImageRes = R.drawable.ic_outlined_close_s
                isSearchMain.rightButtonTint = R.color.uiKitColorForegroundPrimary
                isSearchMain.forceBtnClearVisibility(true)
                isClearTextMode = true
            } else {
                isSearchMain.rightButtonImageRes = R.drawable.ic_mention
                isSearchMain.rightButtonTint = R.color.uiKitColorForegroundSecondary
                isClearTextMode = false

                val btnClearVisibility = when (getCurrentFragment()) {
                    is MeeraSearchUserFragment -> true
                    else -> false
                }
                isSearchMain.forceBtnClearVisibility(btnClearVisibility)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val fromMap = arguments?.getBoolean(
            IArgContainer.ARG_SEARCH_FROM_MAP,
            FROM_MAP
        ) ?: IArgContainer.ARG_SEARCH_FROM_MAP
        if (fromMap == true) {
            NavigationManager.getManager().isMapMode = true
        }
    }

    private fun getCurrentFragment(): SearchScreenContext {
        return listFragments[selectedTab] as SearchScreenContext
    }

    private fun showNumberDialog() {
        getCurrentFragment().hideMessages()
        NavigationManager.getManager().initGraph(R.navigation.bottom_search_number_graph)
    }

    private fun showFilterDialog() {
        if (childFragmentManager.findFragmentByTag(TAG_FILTERS) != null) {
            return
        }

        getCurrentFragment().hideMessages()

        searchFilter = MeeraFilterBottomSheetDialog()
        userSearchViewModel.getFilterResult()?.let { searchFilter?.setFilterResult(it) }
        searchFilter?.show(childFragmentManager, TAG_FILTERS)
    }

    override fun onBackPressed(): Boolean {
        getCurrentFragment().exitScreen()
        return false
    }

    companion object {
        const val PAGE_SEARCH_PEOPLE = 0
        const val FROM_MAP = false
    }
}
