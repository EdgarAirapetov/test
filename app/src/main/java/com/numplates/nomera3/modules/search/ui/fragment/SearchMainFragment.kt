package com.numplates.nomera3.modules.search.ui.fragment

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.viewpager.widget.ViewPager
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.extensions.clearText
import com.meera.core.extensions.click
import com.meera.core.extensions.color
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.databinding.FragmentUserSearchNewBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriendsWhereProperty
import com.numplates.nomera3.modules.newroads.data.entities.FilterSettingsProvider
import com.numplates.nomera3.modules.search.ui.adapter.SearchPagerAdapter
import com.numplates.nomera3.modules.search.ui.viewmodel.user.SearchUserViewModel
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityfilter.CityFilterBottomSheet
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityfilter.CityFilterResultCallback
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.FilterBottomSheet
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.FilterCallback
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.FilterResult
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchCallback
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchParameters
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val SEARCH_DEBOUNCE_TIME_MS = 500L
const val KEY_FROM_PEOPLES = "KEY_FROM_PEOPLES"

/**
 * Главный фоагмент для всех типов поиска
 */
class SearchMainFragment : BaseFragmentNew<FragmentUserSearchNewBinding>(), IOnBackPressed {

    private val disposables = CompositeDisposable()

    private val userSearchViewModel by viewModels<SearchUserViewModel>(
        factoryProducer = { App.component.getViewModelFactory() },
        ownerProducer = { this }
    )

    private val openedFromPeoples by lazy {
        arguments?.getBoolean(KEY_FROM_PEOPLES, false) ?: false
    }

    private val listFragments by lazy { listOf<Fragment>(
        SearchUserFragmentNew.newInstance(openedFromPeoples),
        SearchGroupFragment(),
        SearchHashTagFragment()
    ) }

    private var selectedTab: Int = 0

    private var isClearTextMode = false
    private var filterByNumberApplied = false
    private var searchFilter: FilterBottomSheet? = null
    private var pagerAdapter: SearchPagerAdapter? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUserSearchNewBinding
        get() = FragmentUserSearchNewBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logOnFindFriendsPressed()

        selectedTab = requireArguments().getInt(
            IArgContainer.ARG_SEARCH_OPEN_PAGE,
            PAGE_SEARCH_PEOPLE
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initViewPager()
    }

    override fun onStart() {
        super.onStart()
        binding?.statusBarSpace?.updateLayoutParams {
            height = context.getStatusBarHeight()
        }
        initSearchInputObservers()
    }

    override fun onStartFragment() {
        super.onStartFragment()
        binding?.searchViewPager?.currentItem = selectedTab
        updateSelectedTab(getCurrentFragment())
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }

    override fun onOpenTransitionFragment() {
        super.onOpenTransitionFragment()

        if (!openedFromPeoples) return
        binding?.etSearchUser?.run {
            requestFocus()

            val imm =
                context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager?
            imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun logOnFindFriendsPressed() {
        val openedFromWhere = arguments?.getSerializable(IArgContainer.ARG_FIND_FRIENDS_OPENED_FROM_WHERE)
            as? AmplitudeFindFriendsWhereProperty
        if (openedFromWhere != null) {
            userSearchViewModel.logOnFindFriendsPressed(openedFromWhere)
        }
    }

    private fun initViews() {
        context?.let {
            binding?.tabs?.setTabTextColors(it.color(R.color.ui_black), it.color(R.color.ui_purple))
        }

        binding?.btnBack?.click {
            getCurrentFragment().exitScreen()
            context?.hideKeyboard(requireView())
            doDelayed(100) {
                act.onBackPressed()
            }
        }

        val layoutTransition = LayoutTransition()
        layoutTransition.disableTransitionType(LayoutTransition.DISAPPEARING)
        binding?.appBarContainer?.layoutTransition = layoutTransition

        binding?.btnClearInput?.click {
            if (isClearTextMode) {
                binding?.etSearchUser?.clearText()
            } else {
                insertUniqueSign()
            }
        }

        binding?.btnSearchByNumber?.click {
            userSearchViewModel.amplitudeHelper.logTapSearchByNumberButton()
            showNumberDialog()
        }

        binding?.btnFilter?.click {
            showFilterDialog()
        }
    }

    private fun insertUniqueSign() {
        userSearchViewModel.amplitudeHelper.logTapSearchAtSign()
        binding?.etSearchUser?.apply {
            val current = text ?: String.empty()
            if (current.indexOf(AT_SIGN) != 0) {
                setText("$AT_SIGN$current")
                setSelection(1)
            } else {
                clearText()
            }
        }
    }

    private fun initViewPager() {
        pagerAdapter = SearchPagerAdapter(childFragmentManager)
        pagerAdapter?.addTitles(
            mutableListOf(
                getString(R.string.search_tab_people),
                getString(R.string.search_tab_community),
                getString(R.string.search_tab_hashtags)
            )
        )

        pagerAdapter?.addFragments(listFragments)
        binding?.searchViewPager?.run {
            offscreenPageLimit = listFragments.size
            adapter = pagerAdapter
            binding?.tabs?.setupWithViewPager(this)
        }

        binding?.searchViewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
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
        binding?.etSearchUser?.let { editText ->
            disposables.add(
                RxTextView.textChanges(editText)
                    .map { text -> text.toString().trim() }
                    .debounce(SEARCH_DEBOUNCE_TIME_MS, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ query ->
                        onTextInputSearch(query)
                    })
                    { Timber.e(it) }
            )
        }
    }


    private fun updateSelectedTab(fragment: SearchScreenContext) {
        binding?.apply {
            when (fragment) {
                is SearchUserFragmentNew -> {
                    etSearchUser.hint = getString(R.string.search_by_users)
                    etSearchUser.setMargins(end = 8.dp)
                    btnSearchByNumber.visible()
                    btnFilter.visible()
                    onTabChangedSearch()
                }
                is SearchGroupFragment -> {
                    etSearchUser.hint = getString(R.string.search_by_groups)
                    // Фильтр по сообществам отключен (не делаем в этой итерации 07.06.2021)
                    etSearchUser.setMargins(end = 16.dp)
                    btnSearchByNumber.gone()
                    btnFilter.gone()

                    // Когда будут сделаны фильтр по сообществам, раскоментировать код ниже, и удалить код сверху
                    /*
                    etSearchUser.setMargins(end = 8.dp)
                    btnSearchByNumber.gone()
                    btnFilter.visible()
                    */

                    onTabChangedSearch()
                }
                is SearchHashTagFragment -> {
                    etSearchUser.hint = getString(R.string.search_by_hashtags)
                    etSearchUser.setMargins(end = 16.dp)
                    btnSearchByNumber.gone()
                    btnFilter.gone()
                    onTabChangedSearch()
                }
            }

        }
    }

    private fun onFilterApplySearch() {
        val currentInputHeaderQuery = binding?.etSearchUser?.text.toString()

        if (currentInputHeaderQuery.isEmpty() && userSearchViewModel.getFilterResult() == null) {
            // вызываем clearCurrentResult, чтобы очистить список в текущем экране
            // и убрать мерцания при анимации при возвращении на этот экран
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
            binding?.btnSearchByNumber?.setImageResource(R.drawable.ic_search_by_number)
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

        val currentInputHeaderQuery = binding?.etSearchUser?.text.toString()

        if (getCurrentFragment() is SearchUserFragmentNew
            && currentInputHeaderQuery.isEmpty()
            && userSearchViewModel.getFilterResult() != null
        ) {
            return
        }

        getCurrentFragment().search(currentInputHeaderQuery)
    }

    private fun layoutTopButtons() {
        val currentInputHeaderQuery = binding?.etSearchUser?.text.toString()

        binding?.apply {
            if (currentInputHeaderQuery.isNotEmpty()              // Ввод текста
                && currentInputHeaderQuery != SHARP_SIGN
            ) {
                btnClearInput.setDrawable(R.drawable.ic_clear_circle)
                isClearTextMode = true
                btnClearInput.visible()
            } else {
                // Если поле пустое и это не хэштеги показать значек @
                btnClearInput.setDrawable(R.drawable.ic_search_unique_at)
                isClearTextMode = false

                when (getCurrentFragment()) {
                    is SearchUserFragmentNew -> btnClearInput.visible()
                    is SearchGroupFragment -> btnClearInput.gone()
                    is SearchHashTagFragment -> btnClearInput.gone()
                }
            }
        }
    }

    private fun getCurrentFragment(): SearchScreenContext {
        return listFragments[selectedTab] as SearchScreenContext
    }

    private fun ImageView.setDrawable(@DrawableRes drawableRes: Int) {
        this.setImageDrawable(ContextCompat.getDrawable(requireContext(), drawableRes))
    }

    private fun showNumberDialog() {
        if (childFragmentManager.findFragmentByTag(NumberSearchBottomSheetFragment::class.simpleName) != null) {
            return
        }

        getCurrentFragment().hideMessages()

        val bottomSheet = NumberSearchBottomSheetFragment(object : NumberSearchCallback {
            override fun searchParameters(parameters: NumberSearchParameters) {
                binding?.btnFilter?.setImageResource(R.drawable.ic_search_filter)
                userSearchViewModel.setFilterResult(null)
                binding?.btnSearchByNumber?.setImageResource(R.drawable.ic_search_by_number_applied)
                filterByNumberApplied = true
                userSearchViewModel.setNumberFilter(parameters)
                userSearchViewModel.resetPaging()
                getCurrentFragment().showAndLoadSearchScreen(parameters)
            }
        })
        bottomSheet.show(childFragmentManager, NumberSearchBottomSheetFragment::class.simpleName)
    }

    private fun showFilterDialog() {
        if (childFragmentManager.findFragmentByTag(FilterBottomSheet::class.simpleName) != null) {
            return
        }

        getCurrentFragment().hideMessages()

        searchFilter = FilterBottomSheet(
            filterType = FilterBottomSheet.Companion.FilterType.PEOPLE_SEARCH,
            object : FilterCallback {

                override fun onSelectCityClick() {
                    CityFilterBottomSheet(
                        isSaveResult = false,
                        callback = object : CityFilterResultCallback {

                            override fun onGetCitiesResult(cities: List<City>) {
                                searchFilter?.onCitySearchComplete(cities)
                            }

                        },
                        filterSettingsType = FilterSettingsProvider.FilterType.Main
                    ).show(childFragmentManager, "CityFilterBottomSheet")
                }

                override fun onFilterResult(result: FilterResult?) {
                    Timber.e("GET Filter result:$result")
                    userSearchViewModel.setFilterResult(result)
                    val filterIcon =
                        if (result != null) R.drawable.ic_search_filter_applied
                        else R.drawable.ic_search_filter
                    binding?.btnFilter?.setImageResource(filterIcon)
                    if (filterByNumberApplied) {
                        filterByNumberApplied = false
                        binding?.btnSearchByNumber?.setImageResource(R.drawable.ic_search_by_number)
                        userSearchViewModel.setNumberFilter(null)
                        userSearchViewModel.resetPaging()
                    }
                    onFilterApplySearch()
                }

            })
        userSearchViewModel.getFilterResult()?.let { searchFilter?.setFilterResult(it) }
        searchFilter?.show(childFragmentManager, FilterBottomSheet::class.simpleName)
    }

    override fun onBackPressed(): Boolean {
        getCurrentFragment().exitScreen()
        return false
    }

    companion object {
        const val PAGE_SEARCH_PEOPLE = 0
        const val PAGE_SEARCH_COMMUNITY = 1
    }
}
