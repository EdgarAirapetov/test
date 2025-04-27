package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearSnapHelper
import com.meera.core.extensions.addClickForRange
import com.meera.core.extensions.click
import com.meera.core.extensions.gone
import com.meera.core.extensions.setBackgroundShapeColor
import com.meera.core.extensions.setTint
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityGarageFloor
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.VehicleProfileListAdapterNew
import com.numplates.nomera3.presentation.view.ui.OrientationAwareRecyclerView
import timber.log.Timber

class GarageFloorViewHolder(
    private val parent: ViewGroup,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit
) : BaseUserViewHolder<UserEntityGarageFloor>(parent, R.layout.item_garage_floor) {

    private val tvVehicles = itemView.findViewById<TextView>(R.id.tvVehicles)
    private val tvVehiclesAmount = itemView.findViewById<TextView>(R.id.tvVehiclesAmount)
    private val tvAddVehicle = itemView.findViewById<TextView>(R.id.tvAddVehicle)

    private val rvGarage = itemView.findViewById<OrientationAwareRecyclerView>(R.id.rvGarage)

    private val layoutEmptyGarage = itemView.findViewById<ConstraintLayout>(R.id.layout_empty_garage)
    private val clEmptyGarageContainer = itemView.findViewById<ConstraintLayout>(R.id.cl_empty_garage_container)
    private val iconNoVehicleGarage = itemView.findViewById<ImageView>(R.id.icon_no_vehicle_garage)
    private val tvGaragePlaceholder = itemView.findViewById<TextView>(R.id.tv_garage_placeholder)
    private val separator = itemView.findViewById<View>(R.id.v_garage_separator)

    override fun bind(data: UserEntityGarageFloor) {
        when (data.accountTypeEnum) {
            AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
            AccountTypeEnum.ACCOUNT_TYPE_PREMIUM,
            AccountTypeEnum.ACCOUNT_TYPE_UNKNOWN -> setupCommonTheme()
            AccountTypeEnum.ACCOUNT_TYPE_VIP -> setupVipTheme()
        }
        setupGarage(data)
        setupText(data)
        tvVehicles?.click {
            profileUIActionHandler.invoke(UserProfileUIAction.OnAllVehiclesClick)
        }

        tvAddVehicle?.click {
            profileUIActionHandler.invoke(UserProfileUIAction.OnAddVehicleClick)
        }
        if (data.isSeparable) separator?.visible()
        else separator?.gone()
    }

    private fun setupText(data: UserEntityGarageFloor) {
        tvGaragePlaceholder?.movementMethod = LinkMovementMethod.getInstance()
        val colorSelectedText = if (data.accountTypeEnum == AccountTypeEnum.ACCOUNT_TYPE_VIP)
            R.color.ui_yellow else R.color.ui_purple
        tvGaragePlaceholder?.addClickForRange(IntRange(0, 8), colorSelectedText) {
            profileUIActionHandler.invoke(UserProfileUIAction.OnAddVehicleClick)
        }
        tvVehiclesAmount?.text = data.vehicleCount.toString()
        if (data.isMe) {
            tvAddVehicle?.visible()
        } else {
            tvAddVehicle?.gone()
        }
    }

    private fun setupGarage(data: UserEntityGarageFloor) {
        rvGarage.onFlingListener = null
        rvGarage?.isNestedScrollingEnabled = false
        rvGarage?.setHasFixedSize(true)
        val adapterGarage = VehicleProfileListAdapterNew(
                parentWidth = parent.width
        )
        adapterGarage.clickListener = { position ->
            try {
                val vehicle = adapterGarage.collection[position]
                profileUIActionHandler.invoke(UserProfileUIAction.OnVehicleClick(vehicle))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
        rvGarage?.adapter = adapterGarage
        val snapHelper = LinearSnapHelper()
        rvGarage?.let {
            snapHelper.attachToRecyclerView(it)
        }

        adapterGarage.setUserTypeColor(data.accountTypeEnum.value, data.userColor)

        if (data.vehicleCount > 0) {
            rvGarage?.visible()
            adapterGarage.collection = data.listVehicles
            layoutEmptyGarage.gone()
        } else {
            rvGarage?.gone()
            layoutEmptyGarage.visible()
        }

    }

    private fun setupVipTheme() {
        val context = itemView.context
        tvVehicles?.setTextColor(ContextCompat.getColor(context, R.color.white_1000))
        tvVehiclesAmount?.setTextColor(ContextCompat.getColor(context, R.color.ui_light_gray))
        tvAddVehicle?.setTextColor(ContextCompat.getColor(context, R.color.ui_yellow))
        clEmptyGarageContainer?.setBackgroundShapeColor(R.color.ui_yellow)
        iconNoVehicleGarage?.setTint(R.color.ui_yellow)
        tvGaragePlaceholder?.setTextColor(Color.WHITE)
    }

    private fun setupCommonTheme() {
        val context = itemView.context
        tvVehicles?.setTextColor(ContextCompat.getColor(context, R.color.ui_black))
        tvVehiclesAmount?.setTextColor(ContextCompat.getColor(context, R.color.ui_text_gray))
        tvAddVehicle?.setTextColor(ContextCompat.getColor(context, R.color.ui_purple))
        clEmptyGarageContainer?.setBackgroundShapeColor(R.color.ui_gray)
        iconNoVehicleGarage?.setTint(R.color.ui_gray)
        tvGaragePlaceholder?.setTextColor(ContextCompat.getColor(context, R.color.ui_text_gray))
    }
}
