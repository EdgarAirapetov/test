package com.numplates.nomera3.presentation.view.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.loadGlideWithCache
import com.meera.core.extensions.visible
import com.meera.core.utils.graphics.NGraphics
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.modules.baseCore.helper.HolidayInfoHelper
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentsIndicatorParams
import javax.inject.Inject


/**
 * created by c7j on 15.11.18
 */

class VipView : FrameLayout, INetworkValues {

    var currentAvatarLink: Any? = null

    private val DEFAULT_AVATAR_PADDING_DP = 0
    private val PREMIUM_AVATAR_PADDING_DP = 2
    private val MOMENTS_AVATAR_PADDING_DP = 4

    private var sizeAttrs: String? = null
    var size: Int? = null
    var type: Int = TYPE_DEFAULT
    private var accentResourceColor: Int = 0

    lateinit var view: View

    lateinit var ivAvatar: ImageView
    lateinit var ivCrown: ImageView
    lateinit var cv_container: CardView
    lateinit var ivHolidayHat: ImageView
    private var ivPrivateAvatar: ImageView? = null
    private var vAvatarMask: View? = null
    private var mcivMomentsIndicator: MomentsCircleIndicatorView? = null

    internal var onImageReady: (drawable: Drawable?) -> Unit = { drawable -> }

    @Inject
    lateinit var holidayInfoHelper: HolidayInfoHelper

    init {
        App.component.inject(this)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.VipView)
        sizeAttrs = typedArray.getString(R.styleable.VipView_size)
        if (sizeAttrs != null) {
            size = Integer.parseInt(sizeAttrs!!)
        }
        init(size)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.VipView, defStyleAttr, 0)
        sizeAttrs = typedArray.getString(R.styleable.VipView_size)
        if (sizeAttrs != null) {
            size = Integer.parseInt(sizeAttrs!!)
        }
        init(size)
    }

    fun init(size: Int?) {
        if (size == null)
            setSize(SIZE45)
        else
            setSize(size)
        ivAvatar = view.findViewById(R.id.iv_avatar)
        ivPrivateAvatar = view.findViewById(R.id.iv_private_avatar)
        ivCrown = view.findViewById(R.id.iv_avatar_crown)
        cv_container = view.findViewById(R.id.cv_container)
        ivHolidayHat = view.findViewById(R.id.iv_holiday_hat)
        vAvatarMask = view.findViewById(R.id.v_avatar_mask)
        mcivMomentsIndicator = view.findViewById(R.id.mciv_moments_indicator)
    }


    @JvmOverloads
    fun setUp(
        context: Context,
        avatarLink: Any?,
        accountType: Int?,
        frameColor: Int?,
        isNeedShowCrown: Boolean = false,
        hasShadow: Boolean = false,
        hasMoments: Boolean = false,
        hasNewMoments: Boolean = false,
        alwaysShowFrame: Boolean = false
    ) {
        this.currentAvatarLink = avatarLink
        val requestListener = object : RequestListener<Drawable> {

            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                onImageReady(AppCompatResources.getDrawable(context, R.drawable.fill_8_round))
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                onImageReady(resource)
                return false
            }
        }

        if (avatarLink == null) {
            Glide.with(context)
                .load(R.drawable.fill_8_round)
                .apply(RequestOptions.circleCropTransform())
                .listener(requestListener)
                .into(ivAvatar)
        } else {
            val options = RequestOptions()
                .centerCrop()
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.fill_8_round)
                .error(R.drawable.fill_8_round)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)

            Glide.with(context)
                .load(avatarLink)
                .apply(options)
                .listener(requestListener)
                .into(ivAvatar)
        }

        if (accountType != null && accountType == INetworkValues.ACCOUNT_TYPE_VIP && isNeedShowCrown) {
            ivCrown.visible()
        } else {
            ivCrown.invisible()
        }

        if (accountType != null && frameColor != null) {
            accentResourceColor = ContextCompat.getColor(context, NGraphics.getColorResourceId(accountType, frameColor))
        } else if (alwaysShowFrame) {
            accentResourceColor = ContextCompat.getColor(context, R.color.ui_white)
        }

        cv_container.setCardBackgroundColor(ContextCompat.getColor(context, R.color.transparent))

        if (hasShadow) {
            cv_container.cardElevation = 8f.dp
            ivHolidayHat.elevation = 8f.dp
        } else {
            if (size == SIZE41 && accountType == INetworkValues.ACCOUNT_TYPE_REGULAR) {
                ivAvatar.setPadding(0)
            } else if (size == SIZE41) { // Нужно для корректного отображения випов(обводки) у юзеров в дороге
                ivAvatar.setPadding(3.dp)
            }
            cv_container.cardElevation = 0f
            ivHolidayHat.elevation = 0f
        }

        ivAvatar.refreshDrawableState()
        ivCrown.refreshDrawableState()
        showHolidayHatIfExists(accountType)
        setupCircleIndicator(
            hasMoments = hasMoments,
            hasNewMoments = hasNewMoments,
            accountType = accountType,
            accountColor = accentResourceColor,
            alwaysShowCircleIndicator = alwaysShowFrame
        )
    }

    fun setUp(context: Context, avatarLink: Any?) {
        val requestListener = object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                onImageReady(AppCompatResources.getDrawable(context, R.drawable.fill_8_round))
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                onImageReady(resource)
                return false
            }
        }
        if (avatarLink == null) {
            Glide.with(context)
                .load(R.drawable.fill_8_round)
                .apply(RequestOptions.circleCropTransform())
                .listener(requestListener)
                .into(ivAvatar)
        } else {
            val options = RequestOptions()
                .centerCrop()
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.fill_8_round)
                .error(R.drawable.fill_8_round)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
            Glide.with(context)
                .load(avatarLink)
                .apply(options)
                .listener(requestListener)
                .into(ivAvatar)
        }
        cv_container.setCardBackgroundColor(Color.WHITE)
        if (size == SIZE41) ivAvatar.setPadding(0)
        cv_container.cardElevation = 0f
        ivHolidayHat.elevation = 0f
        ivAvatar.refreshDrawableState()
        ivCrown.refreshDrawableState()
    }

    private fun showHolidayHatIfExists(accountType: Int?) {
        if (accountType == null) return
        if (holidayInfoHelper.isHolidayExistAndMatches()) {
            val currentHoliday = holidayInfoHelper.currentHoliday()
            val hatLink = when (accountType) {
                INetworkValues.ACCOUNT_TYPE_PREMIUM -> currentHoliday.hatsLink.premium
                INetworkValues.ACCOUNT_TYPE_VIP -> currentHoliday.hatsLink.vip
                else -> currentHoliday.hatsLink.general
            }
            if (!hatLink.isNullOrBlank()) {
                showHolidayHat(hatLink)
            }
        }
    }

    fun hideHolidayHat() {
        ivHolidayHat.gone()
    }

    fun darkAvatar() {
        vAvatarMask?.visible()
    }

    fun showPrivateAvatar() = ivPrivateAvatar?.visible()

    fun hidePrivateAvatar() = ivPrivateAvatar?.gone()

    private fun showHolidayHat(hatLink: String?) {
        if (size == SIZE60 && type == TYPE_FEED) {
            ivHolidayHat.x = if (view.width == 0) (+3f).dp else 72.25F
            ivHolidayHat.y = if (view.width == 0) (-1f).dp else -2.75F
        }
        ivCrown.gone()
        ivHolidayHat.loadGlideWithCache(hatLink)
        ivHolidayHat.visible()
    }

    fun resetUserPhotoPadding() {
        ivAvatar.setPadding(0)
        ivAvatar.refreshDrawableState()
        ivCrown.refreshDrawableState()
        ivHolidayHat.refreshDrawableState()
    }

    private fun needSetPremiumAvatarPadding(accountType: Int?, alwaysShowCircleIndicator: Boolean): Boolean {
        return accountType == INetworkValues.ACCOUNT_TYPE_PREMIUM
            || accountType == INetworkValues.ACCOUNT_TYPE_VIP
            || alwaysShowCircleIndicator
    }

    private fun needShowCircleIndicator(
        hasMoments: Boolean,
        accountType: Int?,
        alwaysShowCircleIndicator: Boolean,
        isMomentsEnabled: Boolean): Boolean {
        if (alwaysShowCircleIndicator
            ||accountType == INetworkValues.ACCOUNT_TYPE_PREMIUM
            || accountType == INetworkValues.ACCOUNT_TYPE_VIP) {
            return true
        }

        if (!isMomentsEnabled) {
            return false
        }

        return hasMoments
    }

    private fun setupCircleIndicator(
        accountType: Int?,
        accountColor: Int?,
        hasMoments: Boolean,
        hasNewMoments: Boolean,
        alwaysShowCircleIndicator: Boolean
    ) {
        val isMomentsDisabled = (context.applicationContext as? FeatureTogglesContainer)?.momentsFeatureToggle?.isEnabled == false
        val avatarPadding = when {
            isMomentsDisabled -> DEFAULT_AVATAR_PADDING_DP.dp
            hasMoments -> MOMENTS_AVATAR_PADDING_DP.dp
            needSetPremiumAvatarPadding(accountType, alwaysShowCircleIndicator) -> PREMIUM_AVATAR_PADDING_DP.dp
            else -> DEFAULT_AVATAR_PADDING_DP.dp
        }

        ivAvatar.setPadding(avatarPadding)

        if(!isMomentsDisabled) {
            mcivMomentsIndicator?.bind(
                params = MomentsIndicatorParams(
                    hasMoments = hasMoments,
                    hasNewMoments = hasNewMoments,
                    isPremiumAccountType = accountType == INetworkValues.ACCOUNT_TYPE_PREMIUM
                        || accountType == INetworkValues.ACCOUNT_TYPE_VIP,
                    accountColor = accountColor,
                    alwaysShowIndicator = alwaysShowCircleIndicator
                )
            )
        }

        mcivMomentsIndicator?.isVisible = needShowCircleIndicator(
            hasMoments = hasMoments,
            accountType = accountType,
            alwaysShowCircleIndicator = alwaysShowCircleIndicator,
            isMomentsEnabled = !isMomentsDisabled
        )
    }

    private fun setSize(size: Int) {
        if (size == SIZE100) {
            val inflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.vipview100, this)
        }
        if (size == SIZE80) {
            val inflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.vipview80, this)
        }
        if (size == SIZE60) {
            val inflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.vipview60, this)
        }
        if (size == SIZE70) {
            val inflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.vipview70, this)
        }
        if (size == SIZE52) {
            val inflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.vipview52, this)
        }
        if (size == SIZE45) {
            val inflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.vipview45, this)
        }
        if (size == SIZE41) {
            val inflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.vipview41, this)
        }
        if (size == SIZE35) {
            val inflater = this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.vipview35, this)
        }
    }

    companion object {

        const val SIZE100 = 1
        const val SIZE80 = 2
        const val SIZE60 = 3
        const val SIZE70 = 7
        const val SIZE45 = 4
        const val SIZE41 = 5
        const val SIZE35 = 8
        const val SIZE52 = 6


        val WHITE = -1
        val PURPLE = -2
        val GRAY = -3
        val NONE_COLOR = -4

        const val ITEM_FEED_WIDTH = 152

        const val TYPE_DEFAULT = 0
        const val TYPE_FEED = 1
        const val TYPE_MAIN_ROAD_AVATAR = 2
        const val TYPE_NO_HAT = 2
    }
}
