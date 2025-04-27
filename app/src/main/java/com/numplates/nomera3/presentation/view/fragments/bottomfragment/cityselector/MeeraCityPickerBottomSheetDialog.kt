package com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityselector

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.databinding.MeeraBottomSheetUserCitySelectorDialogBinding
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

const val INPUT_TIMEOUT = 300L

class MeeraCityPickerBottomSheetDialog() :
    UiKitBottomSheetDialog<MeeraBottomSheetUserCitySelectorDialogBinding>(), CityListAdapterOnClickListener {

    private var noSearchResultPlaceholder: View? = null
    private var cityListAdapter: MeeraCityListAdapter? = null
    private var cityListRecyclerView: RecyclerView? = null
    private var predefinedCityList = mutableListOf<City>()
    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraBottomSheetUserCitySelectorDialogBinding
        get() = MeeraBottomSheetUserCitySelectorDialogBinding::inflate
    private var selectedCity: City? = null
    private var searchDebounce: Disposable? = null
    private var onDismissListener: UserCitySelectorDismissListener? = null

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(true)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewVariables()
        initCityRecyclerView()
        initClickListeners()
        initSearchCityListener()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        searchDebounce?.dispose()
        onDismissListener?.onDismiss(selectedCity)
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onItemClicked(city: City) {
        selectedCity = city
        dismiss()
    }

    fun setOnDismissListener(listener: UserCitySelectorDismissListener) {
        onDismissListener = listener
    }

    fun setPredefinedCityList(city: List<City>) {
        predefinedCityList = city.toMutableList()
    }

    private fun initViewVariables() {
        cityListRecyclerView = contentBinding?.bottomSheetCityFilterResultList
        contentBinding?.bottomSheetCityFilterNoResult?.tvEmptySearch?.text = getString(R.string.general_search_no_results)
        noSearchResultPlaceholder = contentBinding?.bottomSheetCityFilterNoResult?.root
        rootBinding?.vgDialogToolbar?.gone()
    }

    private fun initCityRecyclerView() {
        contentBinding?.bottomSheetCityFilterResultList?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager =
                    contentBinding?.bottomSheetCityFilterResultList?.layoutManager as? LinearLayoutManager
                val firstVisibleItemPosition = layoutManager?.findFirstCompletelyVisibleItemPosition()
                contentBinding?.vDividerElevation?.isVisible = firstVisibleItemPosition != 0
            }
        })
        cityListAdapter = MeeraCityListAdapter(predefinedCityList.toMutableList(), this)
        cityListRecyclerView?.adapter = cityListAdapter

        // если начинается пролистывание списка, то скрыть клавиатуру
        cityListRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    contentBinding?.searchViewCitySelector.hideKeyboard()
                }

                super.onScrollStateChanged(recyclerView, newState)
            }
        })
        contentBinding?.bottomSheetCityFilterResultList?.post {
            val parentView = rootBinding?.root?.parent as View
            val parentLayoutParams = parentView.layoutParams as CoordinatorLayout.LayoutParams?
            parentLayoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    private fun initClickListeners() {
        rootBinding?.ivBottomSheetDialogClose?.setThrottledClickListener {
            closeKeyboard()
            dismiss()
        }
    }

    private fun initSearchCityListener() {
        contentBinding?.searchViewCitySelector?.doAfterSearchTextChanged { searchCountryName ->
            searchDebounce?.dispose()
            searchDebounce = Observable.just(searchCountryName)
                .debounce(INPUT_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { cityName ->
                    val listCountryResult = predefinedCityList.filter { city ->
                        city.title_?.lowercase()?.startsWith(cityName.lowercase()) ?: false
                    }

                    if (listCountryResult.isEmpty()) {
                        noSearchResultPlaceholder?.visible()
                        cityListRecyclerView?.gone()
                    } else {
                        noSearchResultPlaceholder?.gone()
                        cityListRecyclerView?.visible()
                        cityListAdapter?.updateCityList(listCountryResult)
                    }

                }
        }
    }

    private fun closeKeyboard() {
        context?.getSystemService(Context.INPUT_METHOD_SERVICE).let {
            it as InputMethodManager
            it.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        }
    }

    interface UserCitySelectorDismissListener {
        fun onDismiss(city: City?)
    }
}

class MeeraCityPickerBottomSheetDialogBuilder() {
    private var predefinedCityList = mutableListOf<City>()
    private var onDismissListener: MeeraCityPickerBottomSheetDialog.UserCitySelectorDismissListener? = null
    private var isCancelable = true

    fun setPredefinedCityList(city: List<City>): MeeraCityPickerBottomSheetDialogBuilder {
        predefinedCityList = city.toMutableList()
        return this
    }

    fun setDismissListener(
        dismissListener: MeeraCityPickerBottomSheetDialog.UserCitySelectorDismissListener
    ): MeeraCityPickerBottomSheetDialogBuilder {
        onDismissListener = dismissListener
        return this
    }

    fun show(fm: FragmentManager): MeeraCityPickerBottomSheetDialog {
        val dialog = MeeraCityPickerBottomSheetDialog()
        onDismissListener?.let { dialog.setOnDismissListener(it) }
        predefinedCityList.let { dialog.setPredefinedCityList(it) }
        dialog.isCancelable = this.isCancelable
        dialog.show(fm, MeeraCityPickerBottomSheetDialog::class.simpleName)
        return dialog
    }
}
