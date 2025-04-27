package com.numplates.nomera3.modules.userprofile.ui.viewholder

import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearSnapHelper
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.databinding.MeeraItemGarageFloorBinding
import com.numplates.nomera3.modules.userprofile.ui.fragment.UserInfoRecyclerData
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.MeeraVehicleProfileListAdapter
import timber.log.Timber

class MeeraGarageFloorViewHolder(
    private val binding: MeeraItemGarageFloorBinding, private val profileUIActionHandler: (UserProfileUIAction) -> Unit
) : BaseVH<UserInfoRecyclerData, MeeraItemGarageFloorBinding>(binding) {

    private val adapterGarage: MeeraVehicleProfileListAdapter by lazy {
        MeeraVehicleProfileListAdapter(clickListener = ::itemClickListener)
    }

    private fun setupText(data: UserInfoRecyclerData.UserInfoGarageFloorRecyclerData) {
        binding.apply {
            tvVehiclesAmount.text = data.vehicleCount.toString()
            tvVehiclesAmount.isVisible = data.vehicleCount > 0
            btnEmptyGarage.setThrottledClickListener {
                profileUIActionHandler.invoke(UserProfileUIAction.OnAddVehicleClick)
            }
            ivEmptyGarage.setThrottledClickListener {
                profileUIActionHandler.invoke(UserProfileUIAction.OnAddVehicleClick)
            }
            if (data.isMe) buttonAddVehicle.visible()
            else buttonAddVehicle.gone()
        }
    }

    private fun setupGarage(data: UserInfoRecyclerData.UserInfoGarageFloorRecyclerData) {
        binding.apply {
            rvGarage.onFlingListener = null
            rvGarage.isNestedScrollingEnabled = false
            rvGarage.setHasFixedSize(true)
            rvGarage.adapter = adapterGarage
            val snapHelper = LinearSnapHelper()
            rvGarage.let { snapHelper.attachToRecyclerView(it) }

            adapterGarage.setUserTypeColor(data.accountTypeEnum.value, data.userColor)

            if (data.vehicleCount > 0) {
                rvGarage.visible()
                adapterGarage.collection = data.listVehicles.filterNot { it.hidden }
                groupEmptyGarage.gone()
            } else {
                rvGarage.gone()
                groupEmptyGarage.visible()
            }
        }
    }

    private fun itemClickListener(position: Int) {
        try {
            val vehicle = adapterGarage.collection[position]
            profileUIActionHandler.invoke(UserProfileUIAction.OnVehicleClick(vehicle))
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun bind(data: UserInfoRecyclerData) {
        val garageFloor = (data as UserInfoRecyclerData.UserInfoGarageFloorRecyclerData)
        setupGarage(garageFloor)
        setupText(garageFloor)
        binding.apply {
            buttonAddVehicle.isGone = garageFloor.listVehicles.isEmpty()
            buttonAddVehicle.setThrottledClickListener {
                profileUIActionHandler.invoke( UserProfileUIAction.OnAllVehiclesClick)
            }
            tvVehicles.setThrottledClickListener {
                profileUIActionHandler.invoke(UserProfileUIAction.OnAllVehiclesClick)
            }
        }
    }
}
