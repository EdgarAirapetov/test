package com.numplates.nomera3.presentation.view.widgets

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.flexbox.FlexboxLayout
import com.meera.core.extensions.color
import com.meera.core.extensions.empty
import com.meera.core.extensions.loadGlideCircleWithPlaceHolder
import com.meera.core.extensions.setMargins
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.presentation.model.MutualFriendsUiEntity
import com.numplates.nomera3.presentation.model.MutualUserUiEntity
import com.numplates.nomera3.presentation.view.utils.MutualFriendsTextUtils
import timber.log.Timber

@Deprecated(
    message = "1.В данной вьюшке используется entity из data слоя." +
        "2.Неэффективная логика при добавлении юзеров (общих подписок) Используйте MutualUsersView"
)
class MutualFriendsView @JvmOverloads constructor(
    context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var tvMutualFriends: TextView? = null
    private var vgIconsContainer: FlexboxLayout? = null

    private var iconWidth = DEFAULT_ICON_WIDTH
    private var iconHeight = DEFAULT_ICON_HEIGHT
    private var mutualTextSize = DEFAULT_TEXT_SIZE

    private var mutualFriendsUiEntity: MutualFriendsUiEntity? = null
    private var mutualFriendsTextUtils: MutualFriendsTextUtils? = null

    init {
        initRootView()
        getAttrs()
    }

    fun setMutualFriends(
        friends: MutualFriendsUiEntity,
        isShowDefaultMutualText: Boolean = true
    ) {
        this.mutualFriendsUiEntity = friends
        initMutualUsers(
            uiEntity = friends,
            isShowDefaultMutualText = isShowDefaultMutualText
        )
    }

    fun setText(mutualFriendsText: String?) {
        tvMutualFriends?.text = mutualFriendsText
    }

    fun setText(@StringRes textRes: Int) {
        try {
            setText(context?.getString(textRes))
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun setTextColor(@ColorRes colorRes: Int) {
        try {
            val color = context.color(colorRes)
            tvMutualFriends?.setTextColor(color)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun setTextSize(textSize: Float) {
        tvMutualFriends?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }

    private fun initRootView() {
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
            as LayoutInflater).inflate(R.layout.view_mutual_friend, this)
        vgIconsContainer = findViewById(R.id.vg_icons_container)
        tvMutualFriends = findViewById(R.id.tv_mutual_friends)
    }

    private fun getAttrs() {
        var typedArray: TypedArray? = null
        try {
            typedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.MutualFriendsView,
                defStyleAttr,
                0
            )
            setArguments(typedArray)
        } finally {
            typedArray?.recycle()
        }
    }

    private fun setArguments(typedArray: TypedArray?) {
        typedArray?.let {
            iconWidth = typedArray.getDimension(
                R.styleable.MutualFriendsView_iconWidth,
                DEFAULT_ICON_WIDTH
            )
            iconHeight = typedArray.getDimension(
                R.styleable.MutualFriendsView_iconHeight,
                DEFAULT_ICON_HEIGHT
            )
            mutualTextSize = typedArray.getDimension(
                R.styleable.MutualFriendsView_mutualTextSize,
                DEFAULT_TEXT_SIZE
            )
            val textColor = typedArray.getColor(
                R.styleable.MutualFriendsView_mutualTextColor,
                R.color.colorBlack
            )
            tvMutualFriends?.setTextColor(textColor)
            if (mutualTextSize != DEFAULT_TEXT_SIZE) {
                setTextSize(mutualTextSize)
            }
        }
    }

    private fun initMutualUsers(
        uiEntity: MutualFriendsUiEntity,
        isShowDefaultMutualText: Boolean = true
    ) {
        if (uiEntity.mutualFriends.size > MAX_MUTUAL_FRIENDS) return
        clearCurrentData()
        uiEntity.mutualFriends.forEachIndexed { index, user ->
            setMutualTextData(user.name)
            val itemView = getMutualUserView()
            val mutualUser = MutualUser(itemView)
            mutualUser.bindMutualUser(
                MutualUserUiEntity(
                    userData = user,
                    iconWidth = iconWidth,
                    iconHeight = iconHeight,
                    accountTypeEnum = uiEntity.accountTypeEnum
                )
            )
            if (index > 0) {
                mutualUser.setIconContainerMargin(iconWidth.toInt())
            }
            vgIconsContainer?.addView(itemView)
        }
        if (isShowDefaultMutualText) handleTextResult()
    }

    private fun setMutualTextData(userName: String?) {
        mutualFriendsTextUtils?.addUserName(userName ?: String.empty())
        mutualFriendsTextUtils?.moreCount = mutualFriendsUiEntity?.moreCount ?: 0
    }

    private fun handleTextResult() {
        tvMutualFriends?.text = mutualFriendsTextUtils?.getTextResult()
    }

    private fun clearIconsContainer() =
        vgIconsContainer?.removeAllViews()

    private fun getMutualUserView() = LayoutInflater.from(context)
        .inflate(R.layout.view_mutuals_users_icon, this, false)

    private fun clearCurrentData() {
        clearIconsContainer()
        mutualFriendsTextUtils = MutualFriendsTextUtils(context)
    }

    /**
     * Данный класс будет что-то вроде holder для каждого юзера, который
     * может содержать:
     * 1.Background в зависимости от типа аккаунта
     * 2.Аватарка юзера
     */
    class MutualUser constructor(
        private val view: View
    ) {
        private val vgIconContainer: CardView = view.findViewById(R.id.vg_friend_image_container)
        private val ivMutualUserIcon: ImageView = view.findViewById(R.id.iv_user_icon)

        fun bindMutualUser(
            mutualUserEntity: MutualUserUiEntity
        ) {
            loadGlide(mutualUserEntity.userData?.avatarSmall)
            setUserThemeByAccountType(mutualUserEntity.accountTypeEnum)
            setIconSize(mutualUserEntity)
        }

        fun setIconContainerMargin(iconWidth: Int) {
            vgIconContainer.setMargins(
                end = -iconWidth / 2
            )
        }

        private fun setIconSize(entity: MutualUserUiEntity) {
            vgIconContainer.layoutParams = FrameLayout.LayoutParams(
                entity.iconWidth.toInt(),
                entity.iconHeight.toInt()
            )
            vgIconContainer.radius = entity.iconWidth / 2
        }

        private fun setUserThemeByAccountType(accountType: AccountTypeEnum) {
            vgIconContainer.setCardBackgroundColor(
                view.context.color(getColor(accountType))
            )
        }

        private fun getColor(accountTypeEnum: AccountTypeEnum): Int {
            return when (accountTypeEnum) {
                AccountTypeEnum.ACCOUNT_TYPE_VIP -> R.color.colorBlack
                else -> R.color.ui_white
            }
        }

        private fun loadGlide(imageUrl: String?) {
            ivMutualUserIcon.loadGlideCircleWithPlaceHolder(
                path = imageUrl,
                placeholderResId = R.drawable.fill_8_round
            )
        }
    }

    companion object {
        private const val DEFAULT_ICON_WIDTH = 33F
        private const val DEFAULT_ICON_HEIGHT = 33F
        private const val MAX_MUTUAL_FRIENDS = 3
        private const val DEFAULT_TEXT_SIZE = 14f
    }
}
