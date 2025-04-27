package com.numplates.nomera3.modules.moments.show.presentation.view.carousel

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.meera.core.extensions.dp
import com.meera.core.extensions.loadGlideCenterCropNoFade
import com.meera.core.extensions.longClick
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.setMargins
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemMomentUserBinding
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentsIndicatorParams

class MomentsItemUserView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val USER_CONTENT_PADDING = 4.dp
    private val USER_CONTENT_CORNER_RADIUS = 10.dp
    private val DEFAULT_CONTENT_CORNER_RADIUS = 16.dp

    private val binding = ItemMomentUserBinding.inflate(LayoutInflater.from(context), this, true)

    fun onLongClickMoment(onLongClick: () -> Unit) {
        binding.sivMomentItemBackground.longClick { onLongClick.invoke() }
    }

    fun onClickMoment(onClick: (view: View?) -> Unit) {
        binding.sivMomentItemBackground.setThrottledClickListener { onClick.invoke(binding.sivMomentItemBackground) }
    }

    fun onClickAddMoment(onClick: () -> Unit) {
        binding.ivAddMomentItemButton.setThrottledClickListener { onClick.invoke() }
    }

    fun onClickProfile(onClick: () -> Unit) {
        binding.vvMomentItemAvatar.setThrottledClickListener { onClick.invoke() }
    }

    fun clearListeners() {
        binding.sivMomentItemBackground.setOnClickListener(null)
        binding.sivMomentItemBackground.setOnLongClickListener(null)
        binding.ivAddMomentItemButton.setOnClickListener(null)
        binding.vvMomentItemAvatar.setOnClickListener(null)
    }

    fun toggleType(userMomentType: UserMomentType?) {
        binding.vvMomentItemAvatar.isVisible = userMomentType == UserMomentType.USER
        binding.ivAddMomentItemButton.isVisible = userMomentType == UserMomentType.ADD_MOMENT
    }

    fun setAvatar(
        avatarLink: Any?,
        accountType: Int?
    ) {
        binding.vvMomentItemAvatar.setUp(
            context = context,
            avatarLink = avatarLink,
            accountType = accountType ?: AccountTypeEnum.ACCOUNT_TYPE_REGULAR.value,
            frameColor = R.color.ui_white,
            alwaysShowFrame = true
        )
    }

    fun setText(name: String?) {
        binding.tvMomentItemUsername.text = name
    }

    fun setText(@StringRes nameRes: Int) {
        binding.tvMomentItemUsername.setText(nameRes)
    }

    fun setBackground(url: Any) {
        binding.sivMomentItemBackground.loadGlideCenterCropNoFade(url)
    }

    fun setWatchedStatus(isWatched: Boolean) {
        binding.mrivMomentItemIndicator.bind(MomentsIndicatorParams(
            hasMoments = true,
            hasNewMoments = !isWatched
        ))
    }

    fun setupViewsByMomentType(userMomentType: UserMomentType?, hasMoments: Boolean = true) {
        val showIndicator = userMomentType == UserMomentType.USER
                || (userMomentType == UserMomentType.ADD_MOMENT && hasMoments)
        binding.mrivMomentItemIndicator.isVisible = showIndicator
        binding.cvMomentItemContent.setMargins(if (showIndicator) USER_CONTENT_PADDING else 0)
        binding.cvMomentItemContent.radius = if (showIndicator) {
            USER_CONTENT_CORNER_RADIUS.toFloat()
        } else {
            DEFAULT_CONTENT_CORNER_RADIUS.toFloat()
        }
    }

    fun getContentView() = binding.cvMomentItemContent

    enum class UserMomentType {
        USER,
        ADD_MOMENT
    }
}
