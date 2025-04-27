package com.numplates.nomera3.presentation.view.widgets.facebar

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.meera.core.extensions.loadGlideCircleWithPlaceHolder
import com.meera.core.extensions.release
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.baseCore.helper.HolidayInfoHelper
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayInfo
import com.numplates.nomera3.modules.maps.ui.pin.model.PinMomentsUiModel
import com.numplates.nomera3.modules.maps.ui.pin.model.UserPinUiModel
import javax.inject.Inject

class AvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RelativeLayout(context, attrs, defStyle) {

    private var rlContainer: ViewGroup
    var ivAvatar: ImageView
    private var ivCrown: ImageView
    private var ivRing: ImageView
    private var iv_private_avatar: ImageView
    private var iv_holiday_hat: ImageView

    @Inject
    lateinit var holidayInfoHelper: HolidayInfoHelper
    private var holidayInfo: HolidayInfo? = null

    init {
        App.component.inject(this)
        holidayInfo = holidayInfoHelper.currentHoliday()
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)
            as LayoutInflater).inflate(R.layout.avatar_layout, this)
        iv_holiday_hat = findViewById(R.id.iv_holiday_hat_avatar)
        rlContainer = findViewById(R.id.vg_container)
        ivAvatar = findViewById(R.id.iv_avatar)
        ivCrown = findViewById(R.id.iv_crown)
        ivRing = findViewById(R.id.iv_ring)
        iv_private_avatar = findViewById(R.id.iv_private_avatar)
    }

    fun show(user: UserPinUiModel) {
        setUserDecoration(
            accountType = user.accountType.value,
            accountColor = user.accountColor,
            moments = user.moments
        )
        if (user.avatarBitmap != null) {
            ivAvatar.setImageBitmap(user.avatarBitmap)
        } else {
            ivAvatar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.fill_8_round))
        }
    }

    fun setPrivateAvatar() {
        iv_private_avatar.visibility = VISIBLE
    }

    fun hidePrivateAvatar() {
        iv_private_avatar.visibility = INVISIBLE
    }

    fun show(
        accountType: Int,
        accountColor: Int,
        userPic: Drawable?,
        moments: PinMomentsUiModel
    ) {
        setUserDecoration(
            accountType = accountType,
            accountColor = accountColor,
            moments = moments
        )
        ivAvatar.setImageDrawable(userPic)
    }

    fun clear() {
        ivAvatar.release()
        ivCrown.release()
        iv_private_avatar.release()
    }

    fun show(accountType: Int, accountColor: Int, smallUserPhotoUrl: String?, moments: PinMomentsUiModel) {
        setUserDecoration(
            accountType = accountType,
            accountColor = accountColor,
            moments = moments
        )
        ivAvatar.loadGlideCircleWithPlaceHolder(smallUserPhotoUrl, R.drawable.fill_8_round)
    }

    /**
     * Випы удалены/ Нужен рефакторинт
     * */
    @ColorInt
    fun getDecorationColor(accountTypeEnum: AccountTypeEnum, accountColor: Int?): Int {
        return when(accountTypeEnum) {
            AccountTypeEnum.ACCOUNT_TYPE_PREMIUM -> {
                when (accountColor) {
                    INetworkValues.COLOR_RED -> R.color.colorVipRed
                    INetworkValues.COLOR_GREEN -> R.color.colorVipGreen
                    INetworkValues.COLOR_BLUE -> R.color.colorVipBlue
                    INetworkValues.COLOR_PINK -> R.color.colorVipPink
                    INetworkValues.COLOR_PURPLE -> R.color.colorVipPurple
                    else -> android.R.color.transparent
                }
            }
            AccountTypeEnum.ACCOUNT_TYPE_VIP -> R.color.ui_yellow
            else -> android.R.color.transparent
        }.let(context::getColor)
    }

    private fun setUserDecoration(accountType: Int, accountColor: Int?, moments: PinMomentsUiModel) {
        val accountTypeEnum = createAccountTypeEnum(accountType)
        val decorationColor = if (moments.hasMoments) {
            Color.TRANSPARENT
        } else {
            getDecorationColor(accountTypeEnum, accountColor)
        }
        setAvatarRingDecorationColor(decorationColor)
        ivCrown.isVisible = accountTypeEnum == AccountTypeEnum.ACCOUNT_TYPE_VIP
    }

    private fun setAvatarRingDecorationColor(@ColorInt color: Int) {
        ResourcesCompat.getDrawable(resources, R.drawable.avatar_view_decoration_ring, context.theme)
            ?.mutate()
            ?.apply { setTint(color) }
            ?.let(ivRing::setImageDrawable)
    }
}
