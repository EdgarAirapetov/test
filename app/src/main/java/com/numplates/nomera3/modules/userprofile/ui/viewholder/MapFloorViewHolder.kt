package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.widget.ContentLoadingProgressBar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.ui.location.LocationContract
import com.numplates.nomera3.modules.maps.ui.pin.model.PinMomentsUiModel
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityMapFloor
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.view.widgets.facebar.AvatarView

//mapView in Lite Mode
class MapFloorViewHolder(
    val parent: ViewGroup,
    private val locationContract: LocationContract,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit
) : BaseUserViewHolder<UserEntityMapFloor>(parent, R.layout.item_map_floor), OnMapReadyCallback {

    private val showOnMapContainer = itemView.findViewById<ConstraintLayout>(R.id.showOnMapContainer)
    private val miniMapContainer = itemView.findViewById<CardView>(R.id.miniMapContainer)
    private val showOnMapLabel = itemView.findViewById<TextView>(R.id.showOnMapLabel)
    private val distanceToUser = itemView.findViewById<TextView>(R.id.distanceToUser)
    private val miniMapStub = itemView.findViewById<FrameLayout>(R.id.miniMapStub)
    private val miniMapClickArea = itemView.findViewById<FrameLayout>(R.id.miniMapClickArea)

    private val miniMapUserAvatar = itemView.findViewById<AvatarView>(R.id.miniMapUserAvatar)
    private val miniMapSettingsVisibleForAllOrFriendsContainer =
        itemView.findViewById<ConstraintLayout>(R.id.miniMapSettingsVisibleForAllOrFriendsContainer)
    private val miniMapSettingsVisibleForAllOrFriendsLabel =
        itemView.findViewById<TextView>(R.id.miniMapSettingsVisibleForAllOrFriendsLabel)
    private val miniMapSettingsVisibleForAllOrFriendsText =
        itemView.findViewById<TextView>(R.id.miniMapSettingsVisibleForAllOrFriendsText)
    private val miniMapSettingsArrowIcon = itemView.findViewById<ImageView>(R.id.miniMapSettingsArrowIcon)

    private val miniMapSettingsVisibleForNobody = itemView.findViewById<TextView>(R.id.miniMapSettingsVisibleForNobody)
    private val miniMapSettingsContainer = itemView.findViewById<FrameLayout>(R.id.miniMapSettingsContainer)

    private val miniMapLoader = itemView.findViewById<ContentLoadingProgressBar>(R.id.miniMapLoader)

    private val miniMap = itemView.findViewById<MapView>(R.id.miniMap)
    private val vSeparator = itemView.findViewById<View>(R.id.v_map_separator)

    private lateinit var map: GoogleMap
    private var latLng: LatLng? = null

    private var entity: UserEntityMapFloor? = null
    init {
        with(miniMap) {
            // Initialise the MapView
            onCreate(null)
            // Set the map ready callback to receive the GoogleMap object
            getMapAsync(this@MapFloorViewHolder)
        }
    }

    private fun setMapLocation() {
        val latitude = entity?.coordinates?.latitude
        val longitude = entity?.coordinates?.longitude
        if (latitude != null && longitude != null) {
            latLng = LatLng(latitude, longitude)
        }

        if (!::map.isInitialized) return
        latLng?.let { latLng ->
            with(map) {
                moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
                mapType = GoogleMap.MAP_TYPE_NORMAL
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        MapsInitializer.initialize(itemView.context.applicationContext)
        // If map is not initialised properly
        map = googleMap ?: return
        setMapLocation()
    }

    /** This function is called by the recycleListener, when we need to clear the map. */
    fun clearView() {
        with(map) {
            // Clear the map and free up resources by changing the map type to none
            clear()
            mapType = GoogleMap.MAP_TYPE_NONE
        }
    }

    override fun bind(data: UserEntityMapFloor) {
        this.entity = data
        miniMapUserAvatar?.show(
                accountType = data.accountTypeEnum.value,
                accountColor = data.accountColor ?: 0,
                smallUserPhotoUrl = data.userAvatarSmall,
                moments = PinMomentsUiModel(hasMoments = false, hasNewMoments = false)
        )

        when (data.accountTypeEnum) {
            AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
            AccountTypeEnum.ACCOUNT_TYPE_UNKNOWN -> {
                setupCommonTheme()
            }
            AccountTypeEnum.ACCOUNT_TYPE_VIP -> {
                setupVipTheme()
            }
            AccountTypeEnum.ACCOUNT_TYPE_PREMIUM -> {
                setupCommonTheme()
            }
        }
        if (data.isMe) {
            //если есть старые данные, сначала отрисовываем их, потом перезапрашиваем (вдруг что поменялось)
            if (data.value != null && data.countBlacklist != null && data.countWhitelist != null){
                setupMapForOwnProfile(data)
            } else {
                profileUIActionHandler.invoke(UserProfileUIAction.RequestMapPrivacySettings)
            }
        } else setupForOtherProfile(data)

        miniMapClickArea?.click {
            profileUIActionHandler.invoke(UserProfileUIAction.OnMapClicked(
                lat = entity?.coordinates?.latitude,
                lng = entity?.coordinates?.longitude
            ))
        }
        handleSeparator(data.isSeparable)
    }

    private fun handleSeparator(separable: Boolean) {
        if (separable) vSeparator?.visible()
        else vSeparator?.gone()
    }

    private fun setupMapForOwnProfile(data: UserEntityMapFloor) {
        showOnMapContainer?.visible()
        miniMapLoader?.gone()
        distanceToUser?.gone()
        val isPermissionGranted = locationContract.isPermissionGranted()
        val isLocationEnabled = locationContract.isLocationEnabled()
        if (isLocationEnabled && isPermissionGranted) {
            when (data.value) {
                SettingsUserTypeEnum.ALL.key -> {
                    loadMiniMapUserAvatar(data)
                    miniMapContainer?.visible()
                    miniMapUserAvatar?.visible()
                    miniMapStub?.gone()

                    miniMapSettingsVisibleForAllOrFriendsContainer?.visible()
                    miniMapSettingsVisibleForNobody?.gone()

                    val value = getCountExclusionsText(countBlacklist = data.countBlacklist)
                            .takeIf { it.isNotEmpty() }
                            ?.let { itemView.context.getString(R.string.mini_map_visibility_everyone) + " $it" }
                            ?: itemView.context.getString(R.string.mini_map_visibility_everyone)

                    miniMapSettingsVisibleForAllOrFriendsText?.text = value
                }
                SettingsUserTypeEnum.FRIENDS.key -> {
                    loadMiniMapUserAvatar(data)

                    miniMapContainer?.visible()
                    miniMapUserAvatar?.visible()
                    miniMapStub?.gone()

                    miniMapSettingsVisibleForAllOrFriendsContainer?.visible()
                    miniMapSettingsVisibleForNobody?.gone()

                    miniMapSettingsVisibleForAllOrFriendsText?.invisible()

                    val value = getCountExclusionsText(
                        countBlacklist = data.countBlacklist,
                        countWhitelist = data.countWhitelist
                    ).takeIf { it.isNotEmpty() }
                        ?.let { itemView.context.getString(R.string.mini_map_visibility_friends) + " $it" }
                        ?: itemView.context.getString(R.string.mini_map_visibility_friends)

                    miniMapSettingsVisibleForAllOrFriendsText?.text = value
                    miniMapSettingsVisibleForAllOrFriendsText?.visible()

                }
                SettingsUserTypeEnum.NOBODY.key -> {
                    loadMiniMapUserAvatar(data, true)

                    miniMapContainer?.visible()
                    miniMapUserAvatar?.visible()
                    miniMapStub?.gone()

                    miniMapSettingsVisibleForAllOrFriendsContainer?.visible()
                    miniMapSettingsVisibleForNobody?.gone()

                    val value = getCountExclusionsText(countWhitelist = data.countWhitelist)
                            .takeIf { it.isNotEmpty() }
                            ?.let { itemView.context.getString(R.string.mini_map_visibility_nobody) + " $it" }
                            ?: itemView.context.getString(R.string.mini_map_visibility_nobody)

                    miniMapSettingsVisibleForAllOrFriendsText?.text = value

                }
                else -> {
                    showOnMapContainer?.gone()
                }
            }

            miniMapSettingsContainer?.click {
                profileUIActionHandler.invoke(UserProfileUIAction.OnPrivacyClick)
            }
        } else {
            loadMiniMapUserAvatar(data, false)

            miniMapUserAvatar?.visible()
            miniMapStub?.visible()
            miniMapContainer?.gone()

            miniMapSettingsVisibleForAllOrFriendsContainer?.gone()
            miniMapSettingsVisibleForNobody?.visible()

            if (data.accountTypeEnum == AccountTypeEnum.ACCOUNT_TYPE_REGULAR
                    || data.accountTypeEnum == AccountTypeEnum.ACCOUNT_TYPE_PREMIUM
                    || data.accountTypeEnum == AccountTypeEnum.ACCOUNT_TYPE_UNKNOWN) {
                miniMapStub?.setBackgroundResource(R.drawable.mini_map_settings_empty_map_background_regular)
                miniMapSettingsVisibleForNobody?.text =
                    Html.fromHtml(itemView.context.getString(R.string.mini_map_text_nobody_visible_regular))
            } else {
                miniMapStub?.setBackgroundResource(R.drawable.mini_map_settings_empty_map_background_vip)
                miniMapSettingsVisibleForNobody?.text =
                    Html.fromHtml(itemView.context.getString(R.string.mini_map_text_nobody_visible_vip))
            }

            miniMapSettingsContainer?.setOnClickListener {
                if (!isPermissionGranted) locationContract.requestLocationPermissions()
                else if (!isLocationEnabled) locationContract.requestEnableLocation()
            }
        }
        setMapLocation()
    }

    private fun setupForOtherProfile(data: UserEntityMapFloor) {
        miniMapSettingsContainer?.invisible()
        // сделать миникарту по ширине экрана
        miniMapStub?.gone()
        val newConstraintSet = showOnMapContainer?.let { ConstraintSet().apply { this.clone(it) } }
        miniMapContainer?.id
                ?.also { newConstraintSet?.clear(it, ConstraintSet.END) }
                ?.also {
                    val miniMapSectionId = showOnMapContainer?.id!!
                    newConstraintSet?.connect(it, ConstraintSet.END, miniMapSectionId, ConstraintSet.END, 16.dp)
                }
        newConstraintSet?.applyTo(showOnMapContainer)
        // поставить аватар по центру
        val newConstraintSet2 = showOnMapContainer?.let { ConstraintSet().apply { this.clone(it) } }
        miniMapUserAvatar?.id
                ?.also { newConstraintSet2?.clear(it, ConstraintSet.END) }
                ?.also {
                    val miniMapSectionId = showOnMapContainer?.id!!
                    newConstraintSet2?.connect(it, ConstraintSet.END, miniMapSectionId, ConstraintSet.END, 16.dp)
                }
        newConstraintSet2?.applyTo(showOnMapContainer)
        loadMiniMapUserAvatar(data, false)

        //TODO добавить обновление камеры
        //user?.also { moveMiniMapCameraByNewLatLngZoom(it) }

        val distance = data.distance
        if (distance != null) {
            val km = distance / 1000
            val metres = distance % 1000
            val distanceFormatted = if (km > 0) "$km.${metres / 100} км" else "0.${metres} м"
            distanceToUser?.text = distanceFormatted
            distanceToUser?.visible()
        } else {
            distanceToUser?.gone()
        }
        miniMapContainer?.visible()
        setMapLocation()
    }

    private fun loadMiniMapUserAvatar(data: UserEntityMapFloor, showPrivateLabel: Boolean = false) {
        val accountType = data.accountTypeEnum.value
        val accountColor = data.accountColor ?: 0
        val moments = PinMomentsUiModel(
            hasMoments = false,
            hasNewMoments = false
        )
        miniMapUserAvatar?.show(accountType, accountColor, data.userAvatarSmall, moments)
//        miniMapUserAvatar?.showHatIfExistsWithCache(accountType)
        if (showPrivateLabel) miniMapUserAvatar?.setPrivateAvatar()
        else miniMapUserAvatar?.hidePrivateAvatar()
        miniMapUserAvatar?.visible()
    }

    private fun setupVipTheme() {
        val context = itemView.context
        showOnMapLabel?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
        miniMapSettingsArrowIcon?.setImageResource(R.drawable.ic_mini_map_setting_arrow_white)
        miniMapSettingsVisibleForAllOrFriendsLabel?.setTextColor(ContextCompat.getColor(context, R.color.ui_white))
        miniMapSettingsVisibleForAllOrFriendsText?.setTextColor(
            ContextCompat.getColor(context, R.color.ui_white_light)
        )
        distanceToUser?.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_user_profile_location_distance_vip),
                null, null, null
        )
        distanceToUser?.setTextColor(ContextCompat.getColor(context, R.color.vip_map_distance_color))
        miniMapSettingsVisibleForAllOrFriendsContainer?.setBackgroundResource(
            R.drawable.mini_map_settings_background_vip
        )
    }

    private fun setupCommonTheme() {
        val context = itemView.context
        showOnMapLabel?.setTextColor(ContextCompat.getColor(context, R.color.ui_black))
        distanceToUser?.setCompoundDrawablesWithIntrinsicBounds(
                ContextCompat.getDrawable(context, R.drawable.ic_user_profile_location_distance_regular),
                null, null, null
        )
        distanceToUser?.setTextColor(ContextCompat.getColor(context, R.color.common_map_distance_color))
        miniMapSettingsVisibleForAllOrFriendsContainer?.setBackgroundResource(
            R.drawable.mini_map_settings_background_regular
        )
        miniMapSettingsArrowIcon?.setImageResource(R.drawable.ic_mini_map_settings_arrow_regular)
        miniMapSettingsVisibleForAllOrFriendsLabel?.setTextColor(ContextCompat.getColor(context, R.color.ui_black))
        miniMapSettingsVisibleForAllOrFriendsText?.setTextColor(ContextCompat.getColor(context, R.color.ui_black_light))
    }

    // взято из PrivacySettingsAdapter строка 291
    private fun getCountExclusionsText(countBlacklist: Int? = 0, countWhitelist: Int? = 0): String {
        if (countBlacklist != null && countWhitelist != null) {
            if (countBlacklist > 0 && countWhitelist > 0) {
                return "(+$countWhitelist,-$countBlacklist)"
            }
            if (countBlacklist > 0 && countWhitelist == 0) {
                return "(-$countBlacklist)"
            }
            if (countWhitelist > 0 && countBlacklist == 0) {
                return "(+$countWhitelist)"
            }
        }
        return String.empty()
    }
}
