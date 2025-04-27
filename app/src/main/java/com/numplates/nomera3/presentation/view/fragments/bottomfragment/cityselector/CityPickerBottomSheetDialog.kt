package com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityselector

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.AppCompatEditText
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meera.core.extensions.clearText
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.invisible
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.databinding.BottomSheetUserCitySelectorDialogBinding
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityfilter.FirstItemMarginTop
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

// пикер города на экранах регистрации / личные данные
class CityPickerBottomSheetDialog(
        private val predefinedCityList: List<City>,
        private val countryId: Long
) : BottomSheetDialogFragment(), CityListAdapterOnClickListener {

    private lateinit var closeButton: View
    private lateinit var dialogTopBlock: View
    private lateinit var noSearchResultPlaceholder: View
    private lateinit var clearCitySearchFieldButton: View

    private lateinit var citySearchField: AppCompatEditText
    private lateinit var cityListAdapter: CityListAdapter
    private lateinit var cityListRecyclerView: RecyclerView

    private val viewModel: CityPickerBottomSheetDialogViewModel by viewModels()
    private var binding: BottomSheetUserCitySelectorDialogBinding? = null
    private var selectedCity: City? = null
    private var searchDebounce: Disposable? = null
    private var onDismissListener: UserCitySelectorDismissListener? = null

    fun setOnDismissListener(listener: UserCitySelectorDismissListener) {
        onDismissListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return dialog.apply {
            behavior.peekHeight = resources.displayMetrics?.heightPixels!!
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    // https://stackoverflow.com/questions/41150995/appcompatactivity-oncreate-can-only-be-called-from-within-the-same-library-group
    // можно использовать RestrictedApi т.к. это баг в самих тулзах,
    // ничего страшного в этом нет
    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        binding = BottomSheetUserCitySelectorDialogBinding.inflate(LayoutInflater.from(context))

        binding?.root?.let { dialog.setContentView(it) }


        val parentView = binding?.root?.parent as View
        val parentLayoutParams = parentView.layoutParams as CoordinatorLayout.LayoutParams?
        parentLayoutParams?.height = -1

        val behavior = parentLayoutParams?.behavior
        if (behavior != null && behavior is BottomSheetBehavior) {
            behavior.peekHeight = -1
        }

        binding?.let { binding ->
            binding.root.post {
                initViewVariables(binding)
                initCityRecyclerView()
                initClickListeners()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        searchDebounce?.dispose()
        onDismissListener?.onDismiss(selectedCity)
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    // срабатывает по нажатию на город из списка
    override fun onItemClicked(city: City) {
        selectedCity = city

        dismiss()
    }

    private fun initViewVariables(binding: BottomSheetUserCitySelectorDialogBinding) {
        closeButton = binding.closeCityFilter
        clearCitySearchFieldButton = binding.clearSearchField
        dialogTopBlock = binding.bottomSheetCityFilterTopBlock
        citySearchField = binding.bottomSheetCityFilterSearchInput
        cityListRecyclerView = binding.bottomSheetCityFilterResultList
        noSearchResultPlaceholder = binding.bottomSheetCityFilterNoResult.root
    }

    private fun initCityRecyclerView() {
        cityListAdapter = CityListAdapter(predefinedCityList.toMutableList(), this)

        cityListRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        cityListRecyclerView.adapter = cityListAdapter
        cityListRecyclerView.addItemDecoration(FirstItemMarginTop(24.dp))

        // если начинается пролистывание списка, то скрыть клавиатуру
        cityListRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    citySearchField.hideKeyboard()
                }

                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    private fun initClickListeners() {
        closeButton.setOnClickListener {
            closeKeyboard()
            dismiss()
        }

        clearCitySearchFieldButton.setOnClickListener {
            citySearchField.text?.isNotEmpty()?.let {
                citySearchField.clearText()

                cityListRecyclerView.visible()
                noSearchResultPlaceholder.invisible()

                cityListAdapter.updateCityList(predefinedCityList)
            }
        }

        citySearchField.doOnTextChanged { text, start, count, after ->
            if (!text.isNullOrEmpty()) {
                clearCitySearchFieldButton.visible()

                if (text.length >= 2) {
                    searchDebounce?.dispose()
                    searchDebounce = Observable
                            .just(text)
                            .subscribeOn(Schedulers.computation())
                            .debounce(200, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { searchPhrase: CharSequence? ->
                                viewModel.queryFindCity(countryId, searchPhrase.toString()) {
                                    if (it != null && it.isNotEmpty()) {
                                        cityListRecyclerView.visible()
                                        noSearchResultPlaceholder.invisible()

                                        cityListAdapter.updateCityList(it)
                                    } else {
                                        cityListRecyclerView.invisible()
                                        noSearchResultPlaceholder.visible()
                                    }
                                }
                            }
                }
            } else {
                /**
                 * если пользователь удалил текст в строке поиска города, через backspace на
                 * клавиатуре или выделил курсором, а потом нажал на вырезать или удалить, то
                 * сбросить результаты поиска и показать список городов по умолчанию
                 * https://nomera.atlassian.net/browse/BR-5383
                 * */
                cityListRecyclerView.visible()
                noSearchResultPlaceholder.invisible()
                cityListAdapter.updateCityList(predefinedCityList)

                clearCitySearchFieldButton.gone()
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
