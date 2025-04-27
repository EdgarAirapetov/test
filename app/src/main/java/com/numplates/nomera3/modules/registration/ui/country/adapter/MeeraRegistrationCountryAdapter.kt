package com.numplates.nomera3.modules.registration.ui.country.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.empty
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.cell.CellPosition
import com.numplates.nomera3.databinding.MeeraRegistrationCountryItemBinding
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel

private const val MARGIN_START_DIVIDER = 16

class MeeraRegistrationCountryAdapter(
    private val callback: (RegistrationCountryModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items: List<RegistrationCountryModel> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = MeeraRegistrationCountryItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RegistrationCountryViewHolder(binding, callback)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? RegistrationCountryViewHolder?)?.bind(items[position], position == items.lastIndex)
    }

    override fun getItemCount() = items.size

    class RegistrationCountryViewHolder(
        private val binding: MeeraRegistrationCountryItemBinding,
        private val callback: (RegistrationCountryModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(country: RegistrationCountryModel, lastCountry: Boolean) {
            with(binding) {
                root.setThrottledClickListener { callback.invoke(country) }
                ivCountryFlag.loadGlide(country.flag)
                countryItem.setTitleValue(country.name)
                countryItem.setMarginStartDivider(MARGIN_START_DIVIDER)
                countryItem.setRightTextboxValue(country.code ?: String.empty())
                if (lastCountry) countryItem.cellPosition = CellPosition.BOTTOM
            }
        }
    }
}
