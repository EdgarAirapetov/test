package com.numplates.nomera3.modules.maps.ui.pin

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.maps.ui.pin.model.PinMomentsUiModel
import com.numplates.nomera3.modules.maps.ui.pin.model.UserPinUiModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentsIndicatorParams
import com.numplates.nomera3.presentation.view.widgets.MomentsCircleIndicatorView
import com.numplates.nomera3.presentation.view.widgets.facebar.AvatarView

class UserPinView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    init {
        View.inflate(context, R.layout.view_user_pin, this)
    }

    val avatarView: AvatarView = findViewById(R.id.av_avatar)
    private val nameTv: TextView = findViewById(R.id.tv_avatar_name)
    private val nameBg: ViewGroup = findViewById(R.id.vg_avatar_name_bg)
    private val mcivMoments: MomentsCircleIndicatorView = findViewById(R.id.mciv_moments)

    fun show(user: UserPinUiModel) {
        nameTv.apply {
            if (!user.name.isNullOrBlank() && user.isFriend) {
                text = user.name
                val decorationColor = if (user.moments.hasMoments.not()) {
                    avatarView.getDecorationColor(
                        accountTypeEnum = user.accountType,
                        accountColor = user.accountColor
                    )
                } else {
                    Color.TRANSPARENT
                }
                setBackgroundDecorationColor(decorationColor)
                val textColorResId = if (
                    user.accountType == AccountTypeEnum.ACCOUNT_TYPE_PREMIUM
                    && decorationColor != Color.TRANSPARENT
                ) {
                    R.color.white
                } else {
                    R.color.black
                }
                setTextColor(context.getColor(textColorResId))
                nameBg.visible()
            } else {
                nameBg.gone()
            }
        }
        avatarView.show(user)
        showMoments(user.moments)
    }

    fun showMoments(moments: PinMomentsUiModel) {
        val params = MomentsIndicatorParams(
            hasMoments = moments.hasMoments,
            hasNewMoments = moments.hasNewMoments
        )
        mcivMoments.bind(params)
    }

    fun setNameVisible(visible: Boolean) {
        nameBg.isVisible = visible
    }

    private fun setBackgroundDecorationColor(@ColorInt color: Int) {
        ResourcesCompat.getDrawable(resources, R.drawable.avatar_view_name_decoration_background, context.theme)
            ?.mutate()
            ?.apply { setTint(color) }
            ?.let(findViewById<TextView>(R.id.tv_avatar_name)::setBackground)
    }
}
