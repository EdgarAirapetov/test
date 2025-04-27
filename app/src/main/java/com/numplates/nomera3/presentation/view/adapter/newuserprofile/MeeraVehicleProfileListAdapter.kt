package com.numplates.nomera3.presentation.view.adapter.newuserprofile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.click
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.loadGlideWithOptions
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.VehiclePlateTypeSize
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraItemGarageBinding
import com.numplates.nomera3.databinding.MeeraItemGarageSingleBinding
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel.Companion.TYPE_AUTO
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel.Companion.TYPE_MOTO
import com.numplates.nomera3.presentation.view.utils.NGraphics
import kotlin.properties.Delegates

class MeeraVehicleProfileListAdapter(
    private val clickListener: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var userType: Int? = null
    private var userColor: Int? = null

    fun setUserTypeColor(userType: Int?, userColor: Int?) {
        this.userType = userType
        this.userColor = userColor
        notifyDataSetChanged()
    }

    internal var collection: List<VehicleUIModel> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (collection.size > 1) ITEM_TYPE_COMMON else ITEM_TYPE_SINGLE
    }

    override fun getItemCount(): Int = collection.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_TYPE_COMMON -> {
                val binding = MeeraItemGarageBinding.inflate(inflater, parent, false)
                MeeraPhotoProfileViewHolder(binding, clickListener)
            }

            else -> {
                val binding = MeeraItemGarageSingleBinding.inflate(inflater, parent, false)
                MeeraPhotoProfileSingleViewHolder(binding, clickListener)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_TYPE_COMMON -> (holder as MeeraPhotoProfileViewHolder).bind(collection[position])
            ITEM_TYPE_SINGLE -> (holder as MeeraPhotoProfileSingleViewHolder).bind(collection[position])
        }
    }

    private inner class MeeraPhotoProfileViewHolder(
        private val binding: MeeraItemGarageBinding,
        private val clickListener: (Int) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(vehicle: VehicleUIModel) {

            if (!vehicle.brandLogo.isNullOrEmpty()) {
                binding.ivCarMaker.visible()
                binding.ivCarMaker.loadGlide(vehicle.brandLogo)
            } else {
                binding.ivCarMaker.gone()
            }

            val options = mutableListOf(RequestOptions.circleCropTransform())
            if (vehicle.typeId != null) {
                val placeholderResId = NGraphics.getVehicleTypeMap()[vehicle.typeId.toString()]?.placeHolderId
                options.add(RequestOptions.placeholderOf(placeholderResId ?: R.drawable.vehicle_car))
            } else {
                options.add(RequestOptions.placeholderOf(R.drawable.vehicle_car))
            }
            val photoThumb = when (vehicle.typeId) {
                TYPE_AUTO -> R.drawable.meera_car_choice
                TYPE_MOTO -> R.drawable.meera_moto_choice
                else -> R.drawable.meera_car_choice
            }
            if (vehicle.avatarSmall.isEmpty()) {
                binding.ivPicture.loadGlideWithOptions(photoThumb, options)
            } else {
                binding.ivPicture.loadGlideWithOptions(vehicle.avatarSmall, options)
            }

            if (vehicle.number.isNullOrEmpty().not()) {
                binding.vehiclePlateView.visible()
                val plateType = if (vehicle.typeId == TYPE_AUTO) VehiclePlateTypeSize.SMALL_AUTO else VehiclePlateTypeSize.SMALL_MOTO
                binding.vehiclePlateView.setTypeSize(plateType)
                binding.vehiclePlateView.text = vehicle.number ?: ""
            } else {
                binding.vehiclePlateView.invisible()
            }
            binding.tvName.text = vehicle.brandName
            itemView.click { clickListener(layoutPosition) }
        }

    }

    private inner class MeeraPhotoProfileSingleViewHolder(
        private val binding: MeeraItemGarageSingleBinding,
        private val clickListener: (Int) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(vehicle: VehicleUIModel) {
            if (!vehicle.brandLogo.isNullOrEmpty()) {
                binding.ivCarMaker.visible()
                binding.ivCarMaker.loadGlide(vehicle.brandLogo)
            } else {
                binding.ivCarMaker.gone()
            }

            val options = mutableListOf(RequestOptions.circleCropTransform())
            if (vehicle.typeId != null) {
                val placeholderResId = NGraphics.getVehicleTypeMap()[vehicle.typeId.toString()]?.placeHolderId
                options.add(RequestOptions.placeholderOf(placeholderResId ?: R.drawable.vehicle_car))
            } else {
                options.add(RequestOptions.placeholderOf(R.drawable.vehicle_car))
            }
            val photoThumb = when (vehicle.typeId) {
                TYPE_AUTO -> R.drawable.meera_car_choice
                TYPE_MOTO -> R.drawable.meera_moto_choice
                else -> R.drawable.meera_car_choice
            }
            if (vehicle.avatarSmall.isEmpty()) {
                binding.ivPicture.loadGlideWithOptions(photoThumb, options)
            } else {
                binding.ivPicture.loadGlideWithOptions(vehicle.avatarSmall, options)
            }

            if (vehicle.number.isNullOrEmpty().not()) {
                binding.vehiclePlateView.visible()
                val plateType = if (vehicle.typeId == TYPE_AUTO) VehiclePlateTypeSize.SMALL_AUTO else VehiclePlateTypeSize.SMALL_MOTO
                binding.vehiclePlateView.setTypeSize(plateType)
                binding.vehiclePlateView.text = vehicle.number ?: ""
            } else {
                binding.vehiclePlateView.invisible()
            }

            binding.tvName.text = vehicle.brandName
            binding.tvSubname.text = vehicle.modelName

            itemView.click { clickListener(layoutPosition) }
        }
    }

    companion object {
        const val ITEM_TYPE_SINGLE = 0
        const val ITEM_TYPE_COMMON = 1
    }
}
