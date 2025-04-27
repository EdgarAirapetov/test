package com.numplates.nomera3.modules.registration.ui.country.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.ItemRegistrationCountryBinding
import com.numplates.nomera3.databinding.ItemRegistrationCountryShimmerBinding
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.modules.registration.ui.country.fragment.RegistrationCountryFromScreenType

class RegistrationCountryAdapter(
    private val fromScreenType: RegistrationCountryFromScreenType,
    private val callback: (RegistrationCountryModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items: List<RegistrationCountryModel> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_SHIMMER -> {
                val binding = ItemRegistrationCountryShimmerBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return RegistrationCountryShimmerViewHolder(binding)
            }
            TYPE_ITEM -> {
                val binding = ItemRegistrationCountryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return RegistrationCountryViewHolder(binding, callback)
            }
            else -> error("No such a view holder.")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items.isEmpty()) TYPE_SHIMMER else TYPE_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? RegistrationCountryViewHolder?)?.bind(items[position])
    }

    override fun getItemCount() = when {
        items.isEmpty() && fromScreenType == RegistrationCountryFromScreenType.Transport ->
            SHIMMER_ITEM_COUNT_TRANSPORT
        items.isEmpty() -> SHIMMER_ITEM_COUNT_DEFAULT
        else -> items.size
    }

    class RegistrationCountryViewHolder(
        private val binding: ItemRegistrationCountryBinding,
        private val callback: (RegistrationCountryModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(country: RegistrationCountryModel) {
            with(binding) {
                root.setThrottledClickListener { callback.invoke(country) }
                ivCountryFlag.loadGlide(country.flag)
                tvCountryName.text = country.name
                tvCountryCode.text = country.code
            }
        }

    }

    class RegistrationCountryShimmerViewHolder(
        private val binding: ItemRegistrationCountryShimmerBinding
    ) : RecyclerView.ViewHolder(binding.root)

    companion object {
        private const val TYPE_SHIMMER = 0
        private const val TYPE_ITEM = 1

        private const val SHIMMER_ITEM_COUNT_DEFAULT = 11
        private const val SHIMMER_ITEM_COUNT_TRANSPORT = 5
    }

}
