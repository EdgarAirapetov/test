package com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityfilter

import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meera.core.extensions.clearText
import com.meera.core.extensions.displayHeight
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.newHeight
import com.meera.core.extensions.visible
import com.meera.core.utils.KeyboardHeightProvider
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.modules.newroads.data.entities.FilterSettingsProvider
import com.numplates.nomera3.modules.search.filters.ui.fragment.DELAY_KEYBOARD_VISIBILITY
import com.numplates.nomera3.presentation.utils.viewModelsFactory
import com.numplates.nomera3.presentation.view.utils.NToast

interface CityFilterResultCallback {
    fun onGetCitiesResult(cities: List<City>) {}
    fun onDismiss() {}
}

class CityFilterResultCallbackStub : CityFilterResultCallback

/**
 * TODO техдолг (!) во фрагментах лучше не использовать конструктор
 * (может привести к крэшу при изменении конфигурации)
 */
/**
 * @param isSaveResult - сохрагять ли результат выбора в sharedPref для постов
 * @param callback - возвращает результат выбора городов
 */
class CityFilterBottomSheet(
    private val isSaveResult: Boolean = true,
    private val callback: CityFilterResultCallback = CityFilterResultCallbackStub(),
    private val filterSettingsType: FilterSettingsProvider.FilterType
) : BottomSheetDialogFragment(), FoundCitiesAdapterCallback {

    companion object {
        const val APPLY_BUTTON_ANIMATION_DURATION = 150L
    }

    private lateinit var dialogRoot: View
    private lateinit var closeButton: View
    private lateinit var dialogTopBlock: View
    private lateinit var applySelectedCities: View
    private lateinit var clearCitySearchField: View
    private lateinit var noSearchResultPlaceholder: View
    private lateinit var citySearchField: AppCompatEditText
    private lateinit var foundCitiesRecyclerView: RecyclerView
    private lateinit var foundCitiesAdapter: FoundCitiesAdapter

    private var previousSoftInputMode: Int? = null
    private var dismissListener: DismissListener? = null
    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    private val viewModel: CityFilterViewModel by viewModelsFactory {
        CityFilterViewModel(filterSettingsType)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_city_filter_dialog_fragment_new, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        previousSoftInputMode = activity?.window?.attributes?.softInputMode
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        view.post {
            initFoundCityAdapter()
            initViews(view)
            initDataSources()

            keyboardHeightProvider = dialog?.window?.decorView?.let { KeyboardHeightProvider(it) }

            addAnimationToApplyButton()
            initializeFilter()
            openKeyboardForSearchView()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val onCreateDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return onCreateDialog.apply {
            behavior.peekHeight = resources.displayMetrics?.heightPixels!!
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        applyPreviousSoftInputMode()
        dismissListener?.onDismiss()
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    private fun openKeyboardForSearchView() {
        if (::citySearchField.isInitialized) {
            doDelayed(DELAY_KEYBOARD_VISIBILITY) {
                citySearchField.requestFocus()
                citySearchField.performClick()
                citySearchField.openKeyboard()
            }
        }
    }

    private fun initializeFilter() {
        viewModel.initializeFilter(isSaveResult)
    }

    private fun addAnimationToApplyButton() {
        fun moveUpAnimation(moveUpValue: Float) {
            ObjectAnimator
                .ofFloat(applySelectedCities, "translationY", moveUpValue)
                .apply {
                    duration = APPLY_BUTTON_ANIMATION_DURATION
                    start()
                }
        }

        keyboardHeightProvider?.observer = { keyboardHeight: Int ->
            val distance = if (keyboardHeight > 0
                && ::citySearchField.isInitialized
                && citySearchField.hasFocus()
            ) {
                -keyboardHeight.toFloat()
            } else {
                0.toFloat()
            }

            moveUpAnimation(distance)
        }
    }

    private fun initDataSources() {
        viewModel.foundCities.observe(viewLifecycleOwner, Observer {
            foundCitiesRecyclerView.post {
                foundCitiesAdapter.updateFoundCitiesList(it)
            }
        })

        viewModel.showResultList.observe(viewLifecycleOwner, Observer {
            foundCitiesRecyclerView.post {
                foundCitiesRecyclerView.isVisible = it
            }
        })

        viewModel.showApplyButton.observe(viewLifecycleOwner, Observer {
            applySelectedCities.isVisible = it
        })

        viewModel.showNoResultPlaceholder.observe(viewLifecycleOwner, Observer {
            noSearchResultPlaceholder.isVisible = it
        })

        viewModel.showLimitAlert.observe(viewLifecycleOwner, Observer {
            NToast.with(activity as Act)
                .text(getString(R.string.city_filter_selection_size_limit))
                .typeError()
                .inView(dialog?.window?.decorView)
                .show()
        })
    }

    private fun initFoundCityAdapter() {
        foundCitiesAdapter = FoundCitiesAdapter(
            foundCityList = mutableListOf(),
            callback = this
        )
    }

    private fun initViews(view: View) {
        closeButton = view.findViewById(R.id.close_city_filter)
        closeButton.setOnClickListener {
            closeKeyboard()
            dismiss()
        }

        clearCitySearchField = view.findViewById(R.id.clear_search_field)
        clearCitySearchField.setOnClickListener {
            citySearchField.text?.isNotEmpty()?.let {
                citySearchField.clearText()
                viewModel.resetFilter()
            }
        }

        citySearchField = view.findViewById(R.id.bottom_sheet_city_filter_search_input)
        citySearchField.doOnTextChanged { text, start, count, after ->
            if (text?.length ?: 0 > 0) {
                clearCitySearchField.visible()
            } else clearCitySearchField.gone()
            text?.toString()
                ?.takeIf { it.length >= 2 }
                ?.let { searchPhrase: String ->
                    viewModel.findCities(searchPhrase)
                }
        }

        foundCitiesRecyclerView = view.findViewById(R.id.bottom_sheet_city_filter_result_list)
        foundCitiesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = foundCitiesAdapter
            addItemDecoration(FirstItemMarginTop(24.dp))
            // hide keyboard when dragging
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == SCROLL_STATE_DRAGGING) {
                        citySearchField.hideKeyboard()
                    }

                    super.onScrollStateChanged(recyclerView, newState)
                }
            })
        }

        applySelectedCities = view.findViewById(R.id.apply_selected_cities)
        applySelectedCities.setOnClickListener {
            if (isSaveResult) {
                viewModel.saveSelectedCitiesStorage()
            }

            callback.onGetCitiesResult(viewModel.getSelectedCities())
            dismiss()
        }

        noSearchResultPlaceholder = view.findViewById(R.id.bottom_sheet_city_filter_no_result)

        dialogTopBlock = view.findViewById(R.id.bottom_sheet_city_filter_top_block)
        dialogRoot = view.findViewById(R.id.bottom_sheet_city_filter_root_container)
        dialogRoot.newHeight(getDistanceStatusNavigationBars())
    }

    private fun getDistanceStatusNavigationBars(): Int {
        return requireContext().displayHeight - context.getStatusBarHeight()
    }


    override fun onCityClicked(city: FoundCityModel, cityList: MutableList<FoundCityModel>) {
        viewModel.onCityClicked(city)
    }

    private fun AppCompatEditText.openKeyboard() {
        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE).let {
            it as InputMethodManager
            it.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun closeKeyboard() {
        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE).let {
            it as InputMethodManager
            it.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        }
    }

    fun setOnDismissListener(listener: DismissListener) {
        dismissListener = listener
    }

    interface DismissListener {
        fun onDismiss()
    }

    private fun applyPreviousSoftInputMode() {
        previousSoftInputMode?.let { nonNullPreviousSoftInputMode: Int ->
            activity?.window?.setSoftInputMode(nonNullPreviousSoftInputMode)
        }
    }
}
