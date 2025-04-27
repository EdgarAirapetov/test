package com.numplates.nomera3.modules.chat.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.meera.core.extensions.setImageDrawable
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraChatToolbarDialogMenuBinding

class MeeraChatToolbarDialogMenu @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = MeeraChatToolbarDialogMenuBinding
        .inflate(LayoutInflater.from(context), this)

    fun setLeftItemClick(onClick: () -> Unit) {
        binding.leftItemTouchArea.setThrottledClickListener { onClick() }
    }

    fun setCenterItemClick(onClick: () -> Unit) {
        binding.centerItemTouchArea.setThrottledClickListener { onClick() }
    }

    fun setRightItemClick(onClick: () -> Unit) {
        binding.rightItemTouchArea.setThrottledClickListener { onClick() }
    }

    fun setLeftImageIcon(@DrawableRes iconRes: Int) {
        binding.ivLeftImageIcon.setImageDrawable(iconRes)
    }

    fun setCenterImageIcon(@DrawableRes iconRes: Int) {
        binding.ivCenterImageIcon.setImageDrawable(iconRes)
    }

    fun setRightImageIcon(@DrawableRes iconRes: Int) {
        binding.ivRightImageIcon.setImageDrawable(iconRes)
    }

    fun setLeftItemTitle(@StringRes titleRes: Int) {
        binding.tvLeftIconDescription.setText(titleRes)
    }

    fun setCenterItemTitle(@StringRes titleRes: Int) {
        binding.tvCenterIconDescription.setText(titleRes)
    }

    fun setRightItemTitle(@StringRes titleRes: Int) {
        binding.tvRightIconDescription.setText(titleRes)
    }

    fun isEnabledLeftItem(isEnabled: Boolean) {
        binding.leftItemTouchArea.isEnabled = isEnabled
    }

    fun isEnabledCenterItem(isEnabled: Boolean) {
        binding.centerItemTouchArea.isEnabled = isEnabled
    }

    fun isEnabledRightItem(isEnabled: Boolean) {
        binding.rightItemTouchArea.isEnabled = isEnabled
    }

    fun isVisibleLeftItem(isVisible: Boolean) {
        binding.leftItemTouchArea.isVisible = isVisible
    }

    fun isVisibleCenterItem(isVisible: Boolean) {
        binding.centerItemTouchArea.isVisible = isVisible
    }

    fun isVisibleRightItem(isVisible: Boolean) {
        binding.rightItemTouchArea.isVisible = isVisible
    }

}
