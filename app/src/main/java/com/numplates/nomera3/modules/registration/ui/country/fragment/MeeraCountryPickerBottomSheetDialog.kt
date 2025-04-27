package com.numplates.nomera3.modules.registration.ui.country.fragment

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
import com.numplates.nomera3.databinding.MeeraRegistrationCountryListFragmentBinding
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.modules.registration.ui.country.adapter.MeeraRegistrationCountryAdapter
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityselector.INPUT_TIMEOUT
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class MeeraCountryPickerBottomSheetDialog : UiKitBottomSheetDialog<MeeraRegistrationCountryListFragmentBinding>() {

    private var noSearchResultPlaceholder: View? = null
    private var countryListAdapter: MeeraRegistrationCountryAdapter? = null
    private var countryListRecyclerView: RecyclerView? = null

    private var predefinedCountryList: List<RegistrationCountryModel>? = null
    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraRegistrationCountryListFragmentBinding
        get() = MeeraRegistrationCountryListFragmentBinding::inflate

    private var searchDebounce: Disposable? = null
    private var onDismissListener: UserCountrySelectorDismissListener? = null

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
        initCountryRecyclerView()
        initClickListeners()
        initSearchCountryListener()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        searchDebounce?.dispose()
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    fun setOnDismissListener(listener: UserCountrySelectorDismissListener) {
        onDismissListener = listener
    }

    fun setPredefinedCountryList(list: List<RegistrationCountryModel>) {
        predefinedCountryList = list
    }

    private fun initViewVariables() {
        contentBinding?.rvAvailableCountryList?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = contentBinding?.rvAvailableCountryList?.layoutManager as? LinearLayoutManager
                val firstVisibleItemPosition = layoutManager?.findFirstCompletelyVisibleItemPosition()
                contentBinding?.vDividerElevation?.isVisible = firstVisibleItemPosition != 0
            }
        })
        countryListRecyclerView = contentBinding?.rvAvailableCountryList
        noSearchResultPlaceholder = contentBinding?.rvEmptySearchResult?.root
        rootBinding?.tvBottomSheetDialogLabel?.text = getString(R.string.country)
    }

    private fun initCountryRecyclerView() {
        countryListAdapter = MeeraRegistrationCountryAdapter() {
            onDismissListener?.onDismiss(it)
            dismiss()
        }
        countryListRecyclerView?.adapter = countryListAdapter
        predefinedCountryList?.let { countryListAdapter?.items = it }

        countryListRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    contentBinding?.searchCountryView.hideKeyboard()
                }

                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        contentBinding?.rvAvailableCountryList?.post {
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

    private fun initSearchCountryListener() {
        var delayedCountry: Disposable? = null
        contentBinding?.searchCountryView?.doAfterSearchTextChanged { searchCountryName ->
            delayedCountry?.dispose()
            delayedCountry = Observable.just(searchCountryName)
                .debounce(INPUT_TIMEOUT, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { countryName ->

                    val countryList = predefinedCountryList?.filter { country ->
                        country.name.lowercase().startsWith(countryName.lowercase())
                    }
                    if (countryList?.isEmpty() == true) {
                        noSearchResultPlaceholder?.visible()
                        countryListRecyclerView?.gone()
                    } else {
                        noSearchResultPlaceholder?.gone()
                        countryListRecyclerView?.visible()
                        countryList?.let { countryListAdapter?.items = it }
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

    interface UserCountrySelectorDismissListener {
        fun onDismiss(country: RegistrationCountryModel?)
    }
}

class MeeraCountryPickerBottomSheetDialogBuilder() {
    private var predefinedCountryList: List<RegistrationCountryModel>? = null
    private var onDismissListener: MeeraCountryPickerBottomSheetDialog.UserCountrySelectorDismissListener? = null
    private var isCancelable = true

    fun setPredefinedCountryList(list: List<RegistrationCountryModel>): MeeraCountryPickerBottomSheetDialogBuilder {
        predefinedCountryList = list
        return this
    }

    fun setOnDismissListener(
        listener: MeeraCountryPickerBottomSheetDialog.UserCountrySelectorDismissListener
    ): MeeraCountryPickerBottomSheetDialogBuilder {
        onDismissListener = listener
        return this
    }

    fun show(fm: FragmentManager): MeeraCountryPickerBottomSheetDialog {
        val dialog = MeeraCountryPickerBottomSheetDialog()
        onDismissListener?.let { dialog.setOnDismissListener(it) }
        predefinedCountryList?.let { dialog.setPredefinedCountryList(it) }
        dialog.isCancelable = this.isCancelable
        dialog.show(fm, MeeraCountryPickerBottomSheetDialog::class.simpleName)
        return dialog
    }
}
