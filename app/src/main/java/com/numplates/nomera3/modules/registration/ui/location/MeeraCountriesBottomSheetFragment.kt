package com.numplates.nomera3.modules.registration.ui.location

import android.app.Dialog
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.click
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.dp
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.Country
import com.numplates.nomera3.databinding.MeeraBottomSheetCountriesBinding
import com.numplates.nomera3.databinding.MeeraItemCountryBinding

private const val BOTTOM_SHEET_MARGIN_TOP = 60

class MeeraCountriesBottomSheetFragment : BottomSheetDialogFragment(R.layout.meera_bottom_sheet_countries) {

    private val binding by viewBinding(MeeraBottomSheetCountriesBinding::bind)

    private val adapter by lazy { CountriesAdapter() }
    private val viewModel by viewModels<RegistrationLocationViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

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

    private fun initViews() {
        binding.btnCountriesClose.setThrottledClickListener { dismiss() }
        binding.rvAvailableCountryList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = binding.rvAvailableCountryList.layoutManager as? LinearLayoutManager
                val firstVisibleItemPosition = layoutManager?.findFirstCompletelyVisibleItemPosition()
                binding.vDivider.isVisible = firstVisibleItemPosition != 0
            }
        })
        binding.rvAvailableCountryList.layoutManager = LinearLayoutManager(context)
        binding.rvAvailableCountryList.adapter = adapter
        adapter.setItems(viewModel.getCountriesList())
        binding.isRegistrationCountry.doAfterSearchTextChanged { text ->
            val countries = viewModel.getCountriesList()
            val filteredCountries = if (text.isNotBlank()) {
                countries.filter { it.name?.contains(text, true) ?: false }
            } else countries
            adapter.setItems(filteredCountries)
        }
    }

    private inner class CountriesAdapter(
    ) : RecyclerView.Adapter<CountriesAdapter.ViewHolder>() {

        private var countries: List<Country> = emptyList()

        fun setItems(newCountries: List<Country>) {
            countries = newCountries
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = MeeraItemCountryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

        override fun getItemCount() = countries.size

        inner class ViewHolder(private val binding: MeeraItemCountryBinding) : RecyclerView.ViewHolder(binding.root) {

            private val TRANSITION_DURATION = 200

            fun bind(position: Int) {
                val country = countries[position]
                setCountryFlag(country)
                binding.tvCountryName.text = country.name

                if (position == countries.size) binding.divider.gone()
                else binding.divider.visible()
                itemView.click {
                    viewModel.setCountry(country)
                    dismiss()
                }
            }

            private fun setCountryFlag(country: Country) {
                Glide.with(binding.pfCountryFlag)
                    .load(country.flag ?: R.drawable.gray_circle_transparent_shape)
                    .apply(
                        RequestOptions
                            .circleCropTransform()
                            .placeholder(R.drawable.gray_circle_transparent_shape)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    )
                    .transition(DrawableTransitionOptions.withCrossFade(TRANSITION_DURATION))
                    .into(binding.pfCountryFlag)
            }
        }
    }
}
