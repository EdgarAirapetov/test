package com.numplates.nomera3.modules.search.filters.ui.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.showKeyboard
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.PaddingState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.dp
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.noomeera.nmrmediatools.extensions.hideKeyboard
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.databinding.MeeraDialogCityFilterBinding
import com.numplates.nomera3.modules.newroads.data.entities.FilterSettingsProvider
import com.numplates.nomera3.modules.search.filters.ui.adapter.MeeraFilterCitiesDecoration
import com.numplates.nomera3.modules.search.filters.ui.adapter.MeeraFilterCityAdapter
import com.numplates.nomera3.presentation.utils.viewModelsFactory
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityfilter.CityFilterViewModel

private const val KEY_SAVE_RESULT = "KEY_SAVE_RESULT"
private const val KEY_FILTER_TYPE = "KEY_FILTER_TYPE"

private const val MARGIN_BUTTON = 16
private const val ELEVATION_SNACKBAR = 50F
private const val PADDING_SNACKBAR = 76

const val DELAY_KEYBOARD_VISIBILITY = 300L

interface MeeraCityFilterResultCallback {
    fun onGetCitiesResult(cities: List<City>) {}
}

class MeeraFilterCitiesBottomSheet : UiKitBottomSheetDialog<MeeraDialogCityFilterBinding>() {

    companion object {
        fun newInstance(
            saveResult: Boolean,
            filterType: FilterSettingsProvider.FilterType
        ): MeeraFilterCitiesBottomSheet {
            return MeeraFilterCitiesBottomSheet().apply {
                arguments = bundleOf(
                    KEY_SAVE_RESULT to saveResult,
                    KEY_FILTER_TYPE to filterType.ordinal
                )
            }
        }
    }

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraDialogCityFilterBinding
        get() = MeeraDialogCityFilterBinding::inflate

    override fun getBehaviorDelegate() = UiKitBottomSheetDialogBehDelegate.Builder()
        .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
        .setSkipCollapsed(true)
        .create(dialog)

    private val saveResult: Boolean by lazy { arguments?.getBoolean(KEY_SAVE_RESULT, false) ?: false }
    private val filterType: FilterSettingsProvider.FilterType by lazy {
        val ordinal = arguments?.getInt(KEY_FILTER_TYPE, FilterSettingsProvider.FilterType.Main.ordinal)
            ?: FilterSettingsProvider.FilterType.Main.ordinal
        FilterSettingsProvider.FilterType.entries[ordinal]
    }

    private val foundCitiesAdapter: MeeraFilterCityAdapter by lazy {
        MeeraFilterCityAdapter { city ->
            viewModel.onCityClicked(city)
        }
    }

    private var dismissListener: DismissListener? = null

    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    private val viewModel: CityFilterViewModel by viewModelsFactory {
        CityFilterViewModel(filterType)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams {
        return UiKitBottomSheetDialogParams(
            needShowGrabberView = true,
            needShowToolbar = false,
            needShowCloseButton = false,
            needShowSettingsButton = false,
            dialogStyle = R.style.BottomSheetDialogTheme
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initDataSources()

        keyboardHeightProvider = parentFragment?.view?.let { KeyboardHeightProvider(it) }

        initializeFilter()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onDismiss()
    }

    override fun onStart() {
        super.onStart()
        startKeyboardHeightObserving()
    }

    override fun onStop() {
        super.onStop()
        keyboardHeightProvider?.release()
    }

    private fun startKeyboardHeightObserving() {
        keyboardHeightProvider?.start()
        keyboardHeightProvider?.observer = { height ->
            contentBinding?.btnApply?.setMargins(bottom = height + MARGIN_BUTTON.dp, end = MARGIN_BUTTON.dp)
        }
    }

    private fun initializeFilter() {
        viewModel.initializeFilter(saveResult)
    }

    private fun initDataSources() {
        viewModel.foundCities.observe(viewLifecycleOwner) {
            foundCitiesAdapter.submitList(it)
        }
        viewModel.showResultList.observe(viewLifecycleOwner) {
            contentBinding?.rvFilterCities?.isVisible = it
        }
        viewModel.showApplyButton.observe(viewLifecycleOwner) {
            contentBinding?.btnApply?.isVisible = it
            contentBinding?.btnApply?.post { contentBinding?.btnApply?.requestLayout() }
        }
        viewModel.showNoResultPlaceholder.observe(viewLifecycleOwner) {
            contentBinding?.vgNoResult?.isVisible = it
        }
        viewModel.showLimitAlert.observe(viewLifecycleOwner) {
            UiKitSnackBar.make(
                requireView(),
                SnackBarParams(
                    paddingState = PaddingState(bottom = PADDING_SNACKBAR.dp),
                    snackBarViewState = SnackBarContainerUiState(
                        avatarUiState = AvatarUiState.CustomResAvatarState(R.drawable.ic_outlined_attention_m_yellow),
                        messageText = getText(R.string.city_filter_selection_size_limit)
                    ),
                    dismissOnClick = true
                )
            ).apply {
                ViewCompat.setElevation(this.view, ELEVATION_SNACKBAR)
            }.show()
        }
    }

    private fun initViews() {
        contentBinding?.apply {
            nvFilterCities.backButtonClickListener = {
                hideKeyboard()
                dismiss()
            }
            isFilterCities.setClearButtonClickedListener {
                isFilterCities.clear()
                viewModel.resetFilter()
            }
            isFilterCities.doAfterSearchTextChanged { viewModel.findCities(it) }
            isFilterCities.setCloseButtonClickedListener {
                isFilterCities.hideKeyboard()
                viewModel.resetFilter()
            }
            rvFilterCities.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = foundCitiesAdapter
                addItemDecoration(MeeraFilterCitiesDecoration())
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                            isFilterCities.etInput.clearFocus()
                            hideKeyboard()
                        }
                    }
                })
            }
            btnApply.setThrottledClickListener {
                (parentFragment as? MeeraCityFilterResultCallback?)?.onGetCitiesResult(viewModel.getSelectedCities())
                viewModel.saveSelectedCitiesStorage()
                dismiss()
            }
            lifecycle.doDelayed(DELAY_KEYBOARD_VISIBILITY) {
                isFilterCities.etInput.showKeyboard()
            }
        }
    }

    fun setOnDismissListener(listener: DismissListener) {
        dismissListener = listener
    }

    interface DismissListener {
        fun onDismiss()
    }

}
