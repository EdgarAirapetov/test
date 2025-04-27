package com.numplates.nomera3.presentation.view.fragments.vehiclebrandmodelselect

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraItemVehicleBrandModelBinding

class VehicleBrandModelAdapter(private val onItemSelected: (VehicleBrandModelItem) -> Unit) :
    ListAdapter<VehicleBrandModelItem, VehicleBrandModelAdapter.BrandModelVH>(object :
        DiffUtil.ItemCallback<VehicleBrandModelItem>() {
        override fun areItemsTheSame(oldItem: VehicleBrandModelItem, newItem: VehicleBrandModelItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: VehicleBrandModelItem, newItem: VehicleBrandModelItem): Boolean {
            return oldItem == newItem
        }
    }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = BrandModelVH(
        MeeraItemVehicleBrandModelBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: BrandModelVH, position: Int) {
        holder.onBind(getItem(position))
    }


    inner class BrandModelVH(private val binding: MeeraItemVehicleBrandModelBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: VehicleBrandModelItem) {
            binding.apply {
                ivIcon.isGone = item.image == null
                ivIcon.loadGlide(item.image)

                tvTitle.text = item.name
                vDivider.isInvisible = item.isLast

                root.setThrottledClickListener {
                    onItemSelected.invoke(item)
                }
            }
        }
    }
}
