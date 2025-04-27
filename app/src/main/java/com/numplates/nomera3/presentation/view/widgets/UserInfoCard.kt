package com.numplates.nomera3.presentation.view.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.meera.core.extensions.dp
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAppearAnimate
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues.ACCOUNT_TYPE_PREMIUM
import com.numplates.nomera3.data.network.core.INetworkValues.ACCOUNT_TYPE_VIP
import com.numplates.nomera3.data.network.core.INetworkValues.COLOR_BLUE
import com.numplates.nomera3.data.network.core.INetworkValues.COLOR_GREEN
import com.numplates.nomera3.data.network.core.INetworkValues.COLOR_PINK
import com.numplates.nomera3.data.network.core.INetworkValues.COLOR_PURPLE
import com.numplates.nomera3.data.network.core.INetworkValues.COLOR_RED
import com.numplates.nomera3.data.network.core.INetworkValues.VEHICLE_TYPE_BICYCLE
import com.numplates.nomera3.data.network.core.INetworkValues.VEHICLE_TYPE_BOARD
import com.numplates.nomera3.data.network.core.INetworkValues.VEHICLE_TYPE_CAR
import com.numplates.nomera3.data.network.core.INetworkValues.VEHICLE_TYPE_JET_SKI
import com.numplates.nomera3.data.network.core.INetworkValues.VEHICLE_TYPE_MOTO
import com.numplates.nomera3.data.network.core.INetworkValues.VEHICLE_TYPE_PLANE
import com.numplates.nomera3.data.network.core.INetworkValues.VEHICLE_TYPE_ROLLS
import com.numplates.nomera3.data.network.core.INetworkValues.VEHICLE_TYPE_SCOOTER
import com.numplates.nomera3.data.network.core.INetworkValues.VEHICLE_TYPE_SKATEBOARD
import com.numplates.nomera3.data.network.core.INetworkValues.VEHICLE_TYPE_SNOWMOBILE
import com.numplates.nomera3.presentation.view.widgets.numberplateview.NumberPlateEditView
import timber.log.Timber


class UserInfoCard: ConstraintLayout {

    private var color = COLOR_RED
    private var numplateNew: NumberNew? = null
    private var accountType = ACCOUNT_TYPE_PREMIUM

    private val view: View = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        .inflate(R.layout.layout_user_info_card, this)
    private val vUserAvatarGradient: View = view.findViewById(R.id.v_user_avatar_gradient)
    private val cvBgContainer: ConstraintLayout = view.findViewById(R.id.cv_bg_container)
    private val ivCrownCard: ImageView = view.findViewById(R.id.iv_crown_card)
    private val tvUserNameCard: TextView = view.findViewById(R.id.tv_user_name_card)
    private val tvAgeCity: TextView = view.findViewById(R.id.tv_age_city)
    private val vSeparatorUserCard: View = view.findViewById(R.id.v_separator_user_card)
    private val cvUserInfoCard: CardView = view.findViewById(R.id.cv_user_info_card)
    private val ivVehicleUserCard: ImageView = view.findViewById(R.id.iv_vehicle_user_card)
    private val ivUserCrown: ImageView = view.findViewById(R.id.iv_user_crown)
    private val ivUserAvatarCard: ImageView = view.findViewById(R.id.iv_user_avatar_card)
    private val cvVehicleLogo: CardView = view.findViewById(R.id.cv_vehicle_logo)
    private val ivVehicleLogo: ImageView = view.findViewById(R.id.iv_vehicle_logo)
    private val llNplateContainer: LinearLayout = view.findViewById(R.id.ll_nplate_container)

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init(){
        makeVip()
    }


    /**
     *
     * @param color set color using {@link com.numplates.nomera3.data.network.core.INetworkValues}
     *
     * */
    fun setGradient(color: Int, accountType: Int = ACCOUNT_TYPE_PREMIUM){
        this.color = color
        context?.let { context ->
            var colorr: Int = COLOR_RED
            when(color){
                COLOR_RED -> {
                    vUserAvatarGradient.visibleAppearAnimate()
                    vUserAvatarGradient.background = ContextCompat.getDrawable(context, R.drawable.avatar_gradient_bottom_red)
                    colorr = COLOR_RED
                }
                COLOR_GREEN -> {
                    vUserAvatarGradient.visibleAppearAnimate()
                    vUserAvatarGradient.background = ContextCompat.getDrawable(context, R.drawable.avatar_gradient_bottom_green)
                    colorr = COLOR_GREEN
                }
                COLOR_BLUE -> {
                    vUserAvatarGradient.visibleAppearAnimate()
                    vUserAvatarGradient.background = ContextCompat.getDrawable(context, R.drawable.avatar_gradient_bottom_blue)
                    colorr = COLOR_BLUE
                }
                COLOR_PURPLE -> {
                    vUserAvatarGradient.visibleAppearAnimate()
                    vUserAvatarGradient.background = ContextCompat.getDrawable(context, R.drawable.avatar_gradient_bottom_purple)
                    colorr = COLOR_PURPLE
                }
                COLOR_PINK -> {
                    vUserAvatarGradient.visibleAppearAnimate()
                    vUserAvatarGradient.background = ContextCompat.getDrawable(context, R.drawable.avatar_gradient_bottom_pink)
                    colorr = COLOR_PINK
                }
            }
            lastVehicle?.let { vehicle->
                plate?.setBackgroundPlate(vehicle.typeId, vehicle.countryId, accountType, colorr)
            }
            lastVehicleNoNumber?.setType(accountType, colorr)
        }

    }

    fun setBgColor(color: Int){
        cvBgContainer.setBackgroundColor(color)
    }

    fun makeVip(){
        accountType = ACCOUNT_TYPE_PREMIUM
        ivCrownCard.gone()
        numplateNew?.setType(ACCOUNT_TYPE_PREMIUM, null)
        context?.let {
            setBgColor(ContextCompat.getColor(it, R.color.ui_white))
            tvUserNameCard.setTextColor(ContextCompat.getColor(it, R.color.ui_black))
            tvAgeCity.setTextColor(ContextCompat.getColor(it, R.color.ui_black_60))
            vSeparatorUserCard.background = ContextCompat.getDrawable(it, R.color.ui_black_20)
            ivCrownCard.setImageResource(R.drawable.ic_crown_silver_small_nomera)
        }
        cvUserInfoCard.cardElevation = dpToPx(8).toFloat()
        ivVehicleUserCard.setColorFilter(
                ContextCompat.getColor(context, R.color.ui_black_10),
                android.graphics.PorterDuff.Mode.MULTIPLY)


    }

    fun makeGold(){
        accountType = ACCOUNT_TYPE_VIP
        ivCrownCard.visible()
        numplateNew?.setType(ACCOUNT_TYPE_VIP, null)
        context?.let {
            setBgColor(ContextCompat.getColor(it, R.color.ui_black))
            ivUserCrown.setImageResource(R.drawable.crown_golden)
            tvUserNameCard.setTextColor(ContextCompat.getColor(it, R.color.ui_yellow))
            tvAgeCity.setTextColor(ContextCompat.getColor(it, R.color.ui_white))
            ivVehicleUserCard.setColorFilter(
                    ContextCompat.getColor(context, R.color.ui_white_10),
                    android.graphics.PorterDuff.Mode.MULTIPLY)
            vSeparatorUserCard.background = ContextCompat.getDrawable(it, R.color.colorWhite20)
        }
        cvUserInfoCard.cardElevation = 0F
    }

    fun setName(name: String){
        tvUserNameCard.text = name
    }

    fun setAgeCity(ageCity: String){
        tvAgeCity.text = ageCity
    }

    fun setUserAvatarResource(imageResource: Int){
        ivUserAvatarCard.setImageResource(imageResource)
    }

    fun setUserAvatar(url: String?){
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.fill_8)
                .into(ivUserAvatarCard)

    }

    fun setUpNumplate(vehicles: List<UserInfoUIEntity>?) {
        vehicles?.firstOrNull { it.isMain == true }?.let { vehicle ->
            initNumber(vehicle)
        }
    }

    private var plate: NumberPlateEditView? = null
    private var lastVehicle: UserInfoUIEntity? = null
    private var lastVehicleNoNumber: NumberNew? = null
    private fun initNumber(vehicle: UserInfoUIEntity) {
        lastVehicle = vehicle
        ivVehicleUserCard.setImageDrawable(ContextCompat.getDrawable(context, setupType(vehicle.typeId, ivVehicleUserCard)))

        vehicle.brandLogo?.let {
            cvVehicleLogo.visible()
            ivVehicleLogo.loadGlide(it)
        }?: kotlin.run {
            cvVehicleLogo.gone()
        }

        if (vehicle.hasNumber == true){
            plate = NumberPlateEditView(context)
            plate?.readOnly = true

            llNplateContainer.removeAllViews()
            llNplateContainer.addView(plate)
            if (plate == null) return
            NumberPlateEditView.Builder(plate!!)
                .setVehicleNew(vehicle.number, vehicle.countryId, vehicle.typeId)
                    .build()

            val vehicleTypeId = vehicle.typeId ?: -1

            if (vehicleTypeId == 1) {
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)


                params.gravity = Gravity.CENTER
                params.weight = 1f

                params.setMargins(
                        dpToPx(-91),
                        dpToPx(-16),
                        dpToPx(-91),
                        dpToPx(-16)
                )
                plate?.layoutParams = params

                plate?.scaleX = 0.4f
                plate?.scaleY = 0.4f
                plate?.setBackgroundPlate(vehicle.typeId, vehicle.countryId, ACCOUNT_TYPE_PREMIUM, color)
            } else if (vehicleTypeId == 2) {
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(
                        dpToPx(-27),
                        dpToPx(-26),
                        dpToPx(-27),
                        dpToPx(-26)
                )
                plate?.layoutParams = params
                plate?.scaleX = 0.55f
                plate?.scaleY = 0.55f
                plate?.setBackgroundPlate(vehicle.typeId, vehicle.countryId, ACCOUNT_TYPE_PREMIUM, color)
            }
        } else {
            val plate = NumberNew(context)
            lastVehicleNoNumber = plate
            this.numplateNew = plate
            llNplateContainer.addView(plate)
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(60))
            plate.setType(ACCOUNT_TYPE_PREMIUM, color)
            plate.elevation = 0f
            plate.setName(vehicle.brandName ?: "")
            plate.setModel(vehicle.modelName ?: "")
            plate.layoutParams = params

        }
    }

    private fun setupType(type: Int?, ivVehicleUserCard: ImageView?): Int {
        Timber.d("vehicle type = $type")
        when (type) {
            VEHICLE_TYPE_CAR -> {
                ivVehicleUserCard?.layoutParams?.width = 160.dp
                ivVehicleUserCard?.layoutParams?.height = 57.dp
                return R.drawable.img_car
            }
            VEHICLE_TYPE_MOTO -> {
                ivVehicleUserCard?.layoutParams?.width = 146.dp
                ivVehicleUserCard?.layoutParams?.height = 77.dp
                return R.drawable.img_motorbike
            }
            VEHICLE_TYPE_BICYCLE -> {
                ivVehicleUserCard?.layoutParams?.width = 146.dp
                ivVehicleUserCard?.layoutParams?.height = 77.dp
                return R.drawable.img_bicycle
            }
            VEHICLE_TYPE_SCOOTER -> {
                ivVehicleUserCard?.layoutParams?.width = 146.dp
                ivVehicleUserCard?.layoutParams?.height = 100.dp
                return R.drawable.img_scooter
            }
            VEHICLE_TYPE_SKATEBOARD -> {
                return R.drawable.skateboard_facebar
            }
            VEHICLE_TYPE_ROLLS -> {
                return R.drawable.img_roller_skates
            }
            VEHICLE_TYPE_SNOWMOBILE -> {
                ivVehicleUserCard?.layoutParams?.width = 146.dp
                ivVehicleUserCard?.layoutParams?.height = 85.dp
                return R.drawable.img_snowmobile
            }
            VEHICLE_TYPE_JET_SKI -> {
                ivVehicleUserCard?.layoutParams?.width = 146.dp
                ivVehicleUserCard?.layoutParams?.height = 85.dp
                return R.drawable.img_jet_ski
            }
            VEHICLE_TYPE_PLANE -> {
                ivVehicleUserCard?.layoutParams?.width = 136.dp
                ivVehicleUserCard?.layoutParams?.height = 80.dp
                return R.drawable.img_plane
            }
            VEHICLE_TYPE_BOARD -> {
                ivVehicleUserCard?.layoutParams?.width = 140.dp
                ivVehicleUserCard?.layoutParams?.height = 72.dp
                return R.drawable.img_boat
            }
        }
        ivVehicleUserCard?.layoutParams?.width = 146.dp
        ivVehicleUserCard?.layoutParams?.height = 87.dp
        return R.drawable.img_pedestrian
    }

}

data class UserInfoUIEntity(
    val brandLogo: String?,
    val typeId: Int?,
    val hasNumber: Boolean?,
    val number: String?,
    val countryId: Long?,
    val brandName: String?,
    val modelName: String?,
    val isMain: Boolean?
)
