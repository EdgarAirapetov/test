package com.numplates.nomera3.presentation.view.ui.customView

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.ViewCompat
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlideCircleWithPlaceHolder
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setTextStyle
import com.meera.core.extensions.visible
import com.meera.core.utils.IS_APP_REDESIGNED
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewMutualUsersBinding
import com.numplates.nomera3.presentation.model.MutualUserUiModel
import com.numplates.nomera3.presentation.model.MutualUsersUiModel

class MutualUsersView @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    private val binding: ViewMutualUsersBinding = LayoutInflater.from(context)
        .inflate(R.layout.view_mutual_users, this, false)
        .apply(::addView)
        .let(ViewMutualUsersBinding::bind)

    init {
        initStateByAttrs()
    }

    fun setMutualUsers(mutualUsersUiModel: MutualUsersUiModel) {
        setMutualUsersText(mutualUsersUiModel.mutualUsersText)
        setMutualUsersTextColor(mutualUsersUiModel.mutualUsersTextColorRes)
        setIconsVisibilityAndLoadAvatar(mutualUsersUiModel.mutualUsers)
    }

    private fun setIconsVisibilityAndLoadAvatar(mutualUsers: List<MutualUserUiModel>) {
        val allIcons = arrayOf(binding.ivMutualFirst, binding.ivMutualSecond, binding.ivMutualThird)
        allIcons.forEachIndexed { index, avatar ->
            mutualUsers.getOrNull(index)?.let { model ->
                avatar.visible()
                avatar.loadGlideCircleWithPlaceHolder(
                    path = model.avatar,
                    placeholderResId = R.drawable.fill_8_round
                )
            } ?: run {
                avatar.gone()
            }
        }
    }

    private fun setMutualUsersText(mutualUsersText: String) {
        binding.tvMutualUsers.text = mutualUsersText
    }

    private fun initStateByAttrs() {
        context.withStyledAttributes(attrs, R.styleable.MutualUsersView) {
            val iconWidth = getDimension(
                R.styleable.MutualUsersView_mutualIconWidth,
                DEFAULT_ICON_WIDTH_DP.dp
            )
            val iconHeight = getDimension(
                R.styleable.MutualUsersView_mutualIconHeight,
                DEFAULT_ICON_HEIGHT_DP.dp
            )
            val mutualTextSize = getDimension(
                R.styleable.MutualUsersView_mutualUsersTextSize,
                DEFAULT_TEXT_SIZE_DP.dp
            )
            val textColor = getColor(
                R.styleable.MutualUsersView_mutualUsersTextColor,
                getDefaultMutualTextColor()
            )
            setTextColorByArgument(textColor)
            initIconsLayoutParamsByArgument(
                iconWidth = iconWidth.toInt(),
                iconHeight = iconHeight.toInt()
            )
            setTextSize(mutualTextSize)
            setupMutualUsersTextStyle()
        }
    }

    private fun setupMutualUsersTextStyle() {
        val textStyle = if (IS_APP_REDESIGNED) R.style.UiKit_Body_Description else R.style.common_text
        binding.tvMutualUsers.setTextStyle(textStyle)
    }

    private fun setTextColorByArgument(@ColorInt textColor: Int) {
        binding.tvMutualUsers.setTextColor(textColor)
    }

    private fun initIconsLayoutParamsByArgument(
        iconWidth: Int,
        iconHeight: Int
    ) {
        val mutualUsers = arrayOf(binding.ivMutualFirst, binding.ivMutualSecond, binding.ivMutualThird)
        mutualUsers.forEach { mutualUser ->
            mutualUser.layoutParams = LayoutParams(
                iconWidth,
                iconHeight
            )
            val iconRoundSize = (iconWidth / 2).toFloat()
            val shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setAllCorners(CornerFamily.ROUNDED, iconRoundSize)
                .build()
            val shapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
            shapeDrawable.fillColor = ColorStateList.valueOf(getDefaultRoundColor())
            ViewCompat.setBackground(mutualUser, shapeDrawable)
            if (mutualUser.id != binding.ivMutualFirst.id) {
                mutualUser.setMargins(
                    start = -iconWidth / 2
                )
            }
        }
    }

    private fun setTextSize(textSize: Float) {
        binding.tvMutualUsers.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }

    private fun setMutualUsersTextColor(@ColorRes colorRes: Int) {
        val textColorResult = ContextCompat.getColor(context, colorRes)
        binding.tvMutualUsers.setTextColor(textColorResult)
    }

    @ColorInt
    private fun getDefaultRoundColor() = ContextCompat.getColor(context, R.color.white)

    @ColorInt
    private fun getDefaultMutualTextColor() = ContextCompat.getColor(context, R.color.black)

    companion object {
        private const val DEFAULT_ICON_WIDTH_DP = 33f
        private const val DEFAULT_ICON_HEIGHT_DP = 33f
        private const val DEFAULT_TEXT_SIZE_DP = 14f
    }
}
