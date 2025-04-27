package com.numplates.nomera3.modules.registration.ui.location

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.click
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.dp
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.databinding.MeeraBottomSheetCitiesBinding
import com.numplates.nomera3.databinding.MeeraItemCityBinding

private const val BOTTOM_SHEET_MARGIN_TOP = 60

class MeeraCitiesBottomSheetFragment : BottomSheetDialogFragment(R.layout.meera_bottom_sheet_cities) {

    private val binding by viewBinding(MeeraBottomSheetCitiesBinding::bind)

    private val viewModel by viewModels<RegistrationLocationViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private val adapter = CitiesAdapter()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let {
                val behaviour = BottomSheetBehavior.from(it)
                setupFullHeight(it)
                behaviour.skipCollapsed = true
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.height = Resources.getSystem().displayMetrics.heightPixels - BOTTOM_SHEET_MARGIN_TOP.dp
        bottomSheet.layoutParams = layoutParams
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.setCities(viewModel.getCitiesList())
        binding.rvCities.layoutManager = LinearLayoutManager(context)
        binding.rvCities.adapter = adapter
        binding.rvCities.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = binding.rvCities.layoutManager as? LinearLayoutManager
                val firstVisibleItemPosition = layoutManager?.findFirstCompletelyVisibleItemPosition()
                binding.vDivider.isVisible = firstVisibleItemPosition != 0
            }
        })
        binding.isCities.doAfterSearchTextChanged { text ->
            filter(text)
        }
    }

    override fun onResume() {
        super.onResume()
        setDialogExpanded()
    }

    private fun setDialogExpanded() {
        dialog?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            ?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
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

    private inner class CitiesAdapter: RecyclerView.Adapter<CitiesAdapter.ViewHolder>() {

        private val visibleCities = mutableListOf<City>()

        @SuppressLint("NotifyDataSetChanged")
        fun setCities(cities: List<City>) {
            visibleCities.clear()
            visibleCities.addAll(cities)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitiesAdapter.ViewHolder {
            val binding = MeeraItemCityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: CitiesAdapter.ViewHolder, position: Int) = holder.bind(position)

        override fun getItemCount() = visibleCities.size

        inner class ViewHolder(private val binding: MeeraItemCityBinding): RecyclerView.ViewHolder(binding.root) {

            fun bind(position: Int) {
                val city = visibleCities[position]
                if (position == itemCount - 1) binding.divider.gone()
                else binding.divider.visible()
                binding.ukcFilterCity.cellCityText = true
                binding.ukcFilterCity.setCityValue(city.countryName ?: String.empty())
                binding.ukcFilterCity.setTitleValue(city.title_ ?: String.empty())
                itemView.click {
                    viewModel.setCity(city)
                    dismiss()
                }
            }
        }
    }
}
