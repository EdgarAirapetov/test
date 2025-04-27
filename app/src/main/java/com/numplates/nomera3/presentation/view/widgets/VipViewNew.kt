package com.numplates.nomera3.presentation.view.widgets

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.meera.core.extensions.dp
import com.meera.core.extensions.getColorCompat
import com.meera.core.extensions.glideClear
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.loadGlideWithCache
import com.meera.core.extensions.visible
import com.meera.core.utils.graphics.NGraphics
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.modules.baseCore.helper.HolidayInfoHelper
import javax.inject.Inject

/**
 * Новая VipView на замену VipView
 * Размер выставляется в верстке как у обычной view, в VipView размер задается атрибутом size
 * При добавлении в верстку, нужно учитывать отступ справа для отображения короны - 10% от ширины ImageView
 */
class VipViewNew @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle), INetworkValues {

    var type: Int = TYPE_DEFAULT
    private var ivAvatar: ImageView? = null
    private var ivCrown: ImageView? = null
    private var ivHolidayHat: ImageView? = null

    private var accentResourceColor: Int = 0
    internal var onImageReady: (drawable: Drawable?) -> Unit = { drawable -> }
    @Inject
    lateinit var holidayInfoHelper: HolidayInfoHelper

    init {
        App.component.inject(this)
        ViewGroup.inflate(context, R.layout.vipview, this)

        ivAvatar = findViewById(R.id.iv_avatar)
        ivCrown = findViewById(R.id.iv_avatar_crown)
        ivHolidayHat = findViewById(R.id.iv_holiday_hat)
    }

    @JvmOverloads
    fun setUp(
            context: Context,
            avatarLink: Any?,
            accountType: Int?,
            frameColor: Int?,
            isNeedShowCrown: Boolean = true
    ) {
        val requestListener = object : RequestListener<Drawable> {

            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
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

        ivAvatar?.let {
            if (avatarLink == null) {
                Glide.with(context)
                        .load(R.drawable.fill_8_round)
                        .apply(RequestOptions.circleCropTransform())
                        .listener(requestListener)
                        .into(it)
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
                        .into(it)
            }
        }

        if (accountType != null && accountType == INetworkValues.ACCOUNT_TYPE_VIP && isNeedShowCrown) {
            ivCrown?.visible()
        } else {
            ivCrown?.invisible()
        }

        if (accountType != null && frameColor != null) {
            accentResourceColor = context.getColorCompat(
                    NGraphics.getColorResourceId(accountType, frameColor))
        }

        ivAvatar?.let {
            ViewCompat.setBackgroundTintList(it, ColorStateList.valueOf(accentResourceColor))
        }

        if (!isNeedShowCrown && accountType == INetworkValues.ACCOUNT_TYPE_REGULAR) {
            ivAvatar?.setPadding(0, 0, 0, 0)
        } else if (isNeedShowCrown) { // Нужно для корректного отображения випов(обводки) у юзеров в дороге
            ivAvatar?.setPadding(3.dp, 3.dp, 3.dp, 3.dp)
        }
        showHolidayHatIfExists(accountType)
    }

    private fun showHolidayHatIfExists(accountType: Int?) {
        if (holidayInfoHelper.isHolidayExistAndMatches()) {
            val currentHoliday = holidayInfoHelper.currentHoliday()
            val hatLink = when (accountType) {
                INetworkValues.ACCOUNT_TYPE_PREMIUM -> {
                    currentHoliday.hatsLink.premium
                }
                INetworkValues.ACCOUNT_TYPE_VIP -> {
                    currentHoliday.hatsLink.vip
                }
                else -> {
                    currentHoliday.hatsLink.general
                }
            }
            if (type != TYPE_NO_HOLIDAY_HAT && !hatLink.isNullOrBlank()) {
                ivCrown?.gone()
                ivHolidayHat?.loadGlideWithCache(hatLink)
                ivHolidayHat?.visible()
            }
        }
    }

    fun hideHat(){
        ivHolidayHat?.glideClear()
        ivHolidayHat?.gone()
    }

    companion object {
        const val TYPE_DEFAULT = 0
        const val TYPE_NO_HOLIDAY_HAT = 1
    }
}
