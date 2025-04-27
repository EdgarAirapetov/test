package com.numplates.nomera3.presentation.view.fragments.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.Country
import com.meera.core.extensions.dp

/*
* Адаптер для CountryPickerDialogFragment
* */
class CountryPickerDialogAdapter(
        private val countries: List<Country>,
        private val callback: CountryPickerDialogCallback? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PersonalInfoCountryItem(
                LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.personal_info_country_selector_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as PersonalInfoCountryItem
        holder.bind(countries[position])
    }

    override fun getItemCount(): Int = countries.size

    inner class PersonalInfoCountryItem(private val view: View) : RecyclerView.ViewHolder(view) {
        private var countryModel: Country? = null
        private var countryFlag: ImageView = view.findViewById(R.id.country_flag)
        private var countryName: TextView = view.findViewById(R.id.country_name)

        fun bind(country: Country) {
            countryModel = country
            countryModel?.let { countryName.text = it.name }
            countryModel?.let { loadCountryFlag(it.flag) }

            view.setOnClickListener {
                callback?.onCountryClicked(countryModel)
            }
        }

        // todo добавить метод чтоб устанавливать флаги из ресурсов https://zpl.io/VOOB05P
        private fun loadCountryFlag(flagUrl: String?) {
            Glide.with(countryFlag)
                    .load(flagUrl ?: R.drawable.gray_circle_transparent_shape)
                    .override(28.dp, 28.dp)
                    .apply(RequestOptions
                            .circleCropTransform()
                            .placeholder(R.drawable.gray_circle_transparent_shape)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    )
                    .transition(DrawableTransitionOptions.withCrossFade(200))
                    .into(countryFlag)
        }
    }
}