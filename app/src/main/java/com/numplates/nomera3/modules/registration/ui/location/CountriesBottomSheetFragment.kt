package com.numplates.nomera3.modules.registration.ui.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.extensions.click
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.Country
import com.numplates.nomera3.databinding.BottomSheetCountriesBinding
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment

class CountriesBottomSheetFragment: BaseBottomSheetDialogFragment<BottomSheetCountriesBinding>() {

    private val viewModel by viewModels<RegistrationLocationViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    override fun getTheme(): Int = R.style.BottomSheetDialogTransparentTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.rvCountries?.layoutManager = LinearLayoutManager(context)
        binding?.rvCountries?.adapter = CountriesAdapter(viewModel.getCountriesList())
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
            }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> BottomSheetCountriesBinding
        get() = BottomSheetCountriesBinding::inflate


    private inner class CountriesAdapter(
        private val countries: List<Country>
    ): RecyclerView.Adapter<CountriesAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_country, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

        override fun getItemCount() = countries.size

        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {

            private val TRANSITION_DURATION = 200

            private val flagIcon = view.findViewById<AppCompatImageView?>(R.id.ivCountryFlag)
            private val countryName = view.findViewById<AppCompatTextView?>(R.id.tvCountryName)
            private val divider = view.findViewById<View?>(R.id.divider)

            fun bind(position: Int) {
                val country = countries[position]
                setCountryFlag(country)
                countryName?.text = country.name

                if (position == countries.size) divider?.gone()
                else divider?.visible()
                itemView.click {
                    viewModel.setCountry(country)
                    dismiss()
                }
            }

            private fun setCountryFlag(country: Country) {
                when (country.countryId) {
                    Country.ID_ARMENIA -> flagIcon.setImageResource(R.drawable.country_am)
                    Country.ID_UKRAINE -> flagIcon.setImageResource(R.drawable.country_ua)
                    Country.ID_GEORGIA -> flagIcon.setImageResource(R.drawable.country_ge)
                    Country.ID_BELARUS -> flagIcon.setImageResource(R.drawable.country_by)
                    Country.ID_RUSSIA -> flagIcon.setImageResource(R.drawable.country_ru_darker)
                    Country.ID_KAZAKHSTAN -> flagIcon.setImageResource(R.drawable.country_kz)
                    else -> {
                        Glide.with(flagIcon)
                            .load(country.flag ?: R.drawable.gray_circle_transparent_shape)
                            .apply(
                                RequestOptions
                                .circleCropTransform()
                                .placeholder(R.drawable.gray_circle_transparent_shape)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            )
                            .transition(DrawableTransitionOptions.withCrossFade(TRANSITION_DURATION))
                            .into(flagIcon)
                    }
                }
            }
        }
    }
}
