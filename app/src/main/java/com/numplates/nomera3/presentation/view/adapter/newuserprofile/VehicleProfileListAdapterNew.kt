package com.numplates.nomera3.presentation.view.adapter.newuserprofile

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.gone
import com.meera.core.extensions.inflate
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.loadGlideWithOptions
import com.meera.core.extensions.setBackgroundShapeColor
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel
import com.numplates.nomera3.presentation.view.utils.NGraphics
import com.numplates.nomera3.presentation.view.widgets.NumberNew
import com.numplates.nomera3.presentation.view.widgets.numberplateview.NumberPlateEditView
import kotlin.properties.Delegates

class VehicleProfileListAdapterNew(
    private val parentWidth: Int? = null
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

    internal var clickListener: (Int) -> Unit = { _ -> }

    override fun getItemViewType(position: Int): Int =
            if (collection.isNotEmpty()) ITEM_TYPE_COMMON else ITEM_TYPE_ZERO

    override fun getItemCount(): Int = collection.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            when (viewType) {
                ITEM_TYPE_COMMON -> {
                    val itemView = parent.inflate(R.layout.item_garage)
                    PhotoProfileViewHolder(itemView, (parentWidth ?: parent.width) - 32.dp)
                }
                ITEM_TYPE_ZERO -> ZeroItemViewHolder(parent.inflate(R.layout.item_zero))
                else -> ZeroItemViewHolder(parent.inflate(R.layout.item_zero))
            }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_TYPE_COMMON -> {
                (holder as PhotoProfileViewHolder).bind(collection[position], clickListener, collection.size > 1)
            }
        }
    }

    private inner class PhotoProfileViewHolder(itemView: View, private val parentWidth: Int) : RecyclerView.ViewHolder(itemView) {

        val ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)
        val ivCarMaker: ImageView = itemView.findViewById(R.id.ivCarMaker)
        val nvNumber: NumberPlateEditView = itemView.findViewById(R.id.nvNumber)
        val nvnNumber: NumberNew = itemView.findViewById(R.id.nvnNumber)

        val name: TextView = itemView.findViewById(R.id.tvName)
        val subName: TextView = itemView.findViewById(R.id.tvSubName)

        fun bind(vehicle: VehicleUIModel, clickListener: (Int) -> Unit, isMoreThanOne: Boolean = true) {
            itemView.layoutParams.width = parentWidth

            // Brand logo
            if(!vehicle.brandLogo.isNullOrEmpty()){
                ivCarMaker.visible()
                ivCarMaker.loadGlide(vehicle.brandLogo)
            } else {
                ivCarMaker.gone()
            }

            // Vehicle avatar
            val options = mutableListOf(RequestOptions.circleCropTransform())
            if (vehicle.typeId != null) {
                val placeholderResId = NGraphics
                    .getVehicleTypeMap()[vehicle.typeId.toString()]?.placeHolderId
                options.add(RequestOptions.placeholderOf(placeholderResId ?: R.drawable.vehicle_car))
            } else {
                options.add(RequestOptions.placeholderOf(R.drawable.vehicle_car))
            }
            ivPicture.loadGlideWithOptions(vehicle.avatarSmall, options)
            // Vip plate
            val vehicleHasNumber = vehicle.hasNumber ?: false
            if (vehicle.typeId != null && vehicleHasNumber) {
                nvnNumber.gone()
                nvNumber.visible()

                setupNumberPlateEditView(userColor, userType, vehicle)

                if (userType != null) {
                    val color = if (userColor != null && userType != null && userType == 2)
                        R.color.ui_yellow else userColor!!
                    nvNumber.setBackgroundPlateForGarage(vehicle.typeId, vehicle.countryId, userType!!, color)
                }
            } else {
                nvNumber.gone()
                nvnNumber.visible()
                nvnNumber.setName(vehicle.brandName ?: "")
                nvnNumber.setModel(vehicle.modelName ?: "")

                userType?.let { type ->
                    // 0 - обычный 1- премиум 2 - золотой
                    userColor?.let { colorInt ->
                        nvnNumber.setType(type, colorInt)
                    }
                } ?: kotlin.run {
                    nvnNumber.setType(0, 0)
                }

            }

            name.text = vehicle.brandName
            subName.text = vehicle.modelName

            if (userColor != null && userType != null && userType == 2) {
                itemView.setBackgroundShapeColor(R.color.ui_dark_gray_background)
                name.setTextColor(ContextCompat.getColor(name.context, R.color.ui_white))
                subName.setTextColor(ContextCompat.getColor(name.context, R.color.ui_white_light))
            } else {
                itemView.setBackgroundShapeColor(R.color.tale_purple)
                name.setTextColor(ContextCompat.getColor(name.context, R.color.ui_black_light))
                subName.setTextColor(ContextCompat.getColor(name.context, R.color.ui_black_light))
            }

            if (isMoreThanOne) {
                itemView.post { itemView.layoutParams.width = itemView.width - 5 }
            }

            itemView.click { clickListener(layoutPosition) }
        }

        fun setupNumberPlateEditView(accountColor: Int?, accountType: Int?, vehicle: VehicleUIModel) {
            val vehicleNumber = vehicle.number ?: ""
            if (vehicle.number != null && vehicleNumber.isNotEmpty()) {
                // Setup number plate Edit view
                NumberPlateEditView.Builder(nvNumber)
                        .setVehicleNew(vehicle.number, vehicle.countryId, vehicle.typeId)
                        .build()

                nvNumber.setBackgroundPlate(vehicle.typeId, vehicle.countryId, accountType, accountColor)

                if (vehicle.typeId == 1) {
                    val params = FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(
                            dpToPx(-91),
                            dpToPx(-16),
                            dpToPx(-91),
                            dpToPx(-16)
                    )
                    nvNumber.layoutParams = params
                } else if (vehicle.typeId == 2) {
                    val params = FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(
                            dpToPx(-47),
                            dpToPx(-26),
                            dpToPx(-47),
                            dpToPx(-26)
                    )
                    nvNumber.layoutParams = params
                }

                nvNumber.scaleX = 0.35f
                nvNumber.scaleY = 0.35f

                nvNumber.visible()


            } else {
                nvNumber.gone()
            }
        }
    }

    class ZeroItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    companion object {
        const val ITEM_TYPE_COMMON = 1
        const val ITEM_TYPE_ZERO = 0
    }

}
