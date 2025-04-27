package com.numplates.nomera3.modules.registration.ui.location

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.widget.RxTextView
import com.meera.core.extensions.click
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.databinding.BottomSheetCitiesBinding
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class CitiesBottomSheetFragment: BaseBottomSheetDialogFragment<BottomSheetCitiesBinding>() {

    private val viewModel by viewModels<RegistrationLocationViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private val adapter = CitiesAdapter()
    private val disposable = CompositeDisposable()

    override fun getTheme(): Int = R.style.BottomSheetDialogTransparentResizeableTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.setCities(viewModel.getCitiesList())
        binding?.rvCities?.layoutManager = LinearLayoutManager(context)
        binding?.rvCities?.adapter = adapter
        binding?.tvCancel?.click { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        binding?.etSearchCity?.let { editText ->
            disposable.add(
                RxTextView.textChanges(editText)
                    .skip(1)
                    .debounce(SEARCH_DEBOUNCE_TIME_MS, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { filter(it?.toString()?.trim()) },
                        { Timber.e(it) }
                    )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        setDialogExpanded()
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

    private fun setDialogExpanded() {
        dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            ?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
    }

    private fun filter(query: String?) {
        if (query.isNullOrEmpty()) {
            adapter.setCities(viewModel.getCitiesList())
        } else {
            adapter.setCities(viewModel.getCitiesList()
                .filter { it.title_?.contains(query, ignoreCase = true) == true })
        }
        setDialogExpanded()
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> BottomSheetCitiesBinding
        get() = BottomSheetCitiesBinding::inflate

    private inner class CitiesAdapter: RecyclerView.Adapter<CitiesAdapter.ViewHolder>() {

        private val visibleCities = mutableListOf<City>()

        @SuppressLint("NotifyDataSetChanged")
        fun setCities(cities: List<City>) {
            visibleCities.clear()
            visibleCities.addAll(cities)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitiesAdapter.ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_city_bottom, parent, false))
        }

        override fun onBindViewHolder(holder: CitiesAdapter.ViewHolder, position: Int) = holder.bind(position)

        override fun getItemCount() = visibleCities.size

        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

            private val tvCityName = view.findViewById<AppCompatTextView?>(R.id.tvCityName)

            fun bind(position: Int) {
                val city = visibleCities[position]
                tvCityName?.text = city.title_
                itemView.click {
                    viewModel.setCity(city)
                    dismiss()
                }
            }
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_TIME_MS = 500L
    }
}
