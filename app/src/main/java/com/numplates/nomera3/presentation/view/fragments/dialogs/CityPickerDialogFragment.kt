package com.numplates.nomera3.presentation.view.fragments.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.widget.RxSearchView
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.databinding.CityPickerDialogFragmentBinding
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit

/*
* Диалог выбора города на экранах Регистрация / Ваш профиль
* */
class CityPickerDialogFragment(
        private val cityList: List<City>,
        private val countryId: Long
) : DialogFragment(), CityPickerDialogCallback {

    private lateinit var recyclerView: RecyclerView
    private lateinit var dialog: AlertDialog

    private val viewModel: CityPickerDialogViewModel by viewModels()
    private var binding: CityPickerDialogFragmentBinding? = null
    private var searchCityDisposable: Disposable? = null
    private var onDismissListener: (City?) -> Unit = {}
    private var isSizeConfigured: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = CityPickerDialogFragmentBinding.inflate(LayoutInflater.from(context))
        initViews()

        dialog = AlertDialog.Builder(requireContext())
                    .setView(binding?.root)
                    .setCancelable(false)
                    .create()

        viewModel.foundCitiesLiveData.observe(this, Observer {
            val adapter = recyclerView.adapter
            adapter as CityPickerDialogAdapter
            adapter.updateList(it)
        })

        return dialog
    }

    override fun onResume() {
        super.onResume()

        if (!isSizeConfigured) {
            isSizeConfigured = true
            initDialogSize()
        }
    }

    override fun onStop() {
        super.onStop()

        searchCityDisposable?.dispose()
    }

    private fun initDialogSize() {
        val displayWidth = DisplayMetrics().let { displayMetrics: DisplayMetrics ->
            val systemService = requireContext().getSystemService(Context.WINDOW_SERVICE)
            systemService as WindowManager
            systemService.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }

        val layoutParams = WindowManager.LayoutParams()
        dialog.window?.attributes?.let { layoutParams.copyFrom(it) }

        val dialogWindowWidth = (displayWidth * 0.85f).toInt()
        layoutParams.width = dialogWindowWidth
        dialog.window?.attributes = layoutParams
    }

    override fun onCityClicked(city: City?) {
        onDismissListener(city)
        dismiss()
    }

    fun setOnDismissListener(listener: (City?) -> Unit) {
        onDismissListener = listener
    }

    private fun initViews() {
        binding?.let{binding->
            recyclerView = binding.cities
            recyclerView.layoutManager = requireContext().createVerticalLinearLayoutManager()
            recyclerView.adapter = CityPickerDialogAdapter(cityList.toMutableList(), this)

            searchCityDisposable?.dispose()
            searchCityDisposable = RxSearchView
                    .queryTextChanges(binding.searchView)
                    .debounce(200, TimeUnit.MILLISECONDS)
                    .subscribe(
                            {
                                if (it.isNotEmpty()) {
                                    viewModel.queryFindCity(countryId, it.toString())
                                } else {
                                    viewModel.queryFindCity(countryId)
                                }
                            },
                            {
                                Timber.e(it)
                            }
                    )
        }

    }

    private fun Context.createVerticalLinearLayoutManager(): LinearLayoutManager {
        return LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }
}
