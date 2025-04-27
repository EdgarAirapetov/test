package com.numplates.nomera3.modules.peoples.ui.content.holder

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.meera.core.extensions.setDrawable
import com.meera.core.extensions.setDrawableClickListener
import com.numplates.nomera3.databinding.ItemLabelBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.HeaderUiEntity
import timber.log.Timber

private const val INFO_ICON_DRAWABLE_PADDING_DP = 5

class LabelHolder constructor(
    private val binding: ItemLabelBinding,
    private val actionListener: (FriendsContentActions) -> Unit
) : BasePeoplesViewHolder<HeaderUiEntity, ItemLabelBinding>(binding) {

    init {
        initListeners()
    }

    override fun bind(item: HeaderUiEntity) {
        super.bind(item)
        setLabelText(item.text)
        setLabelTextSize(item.textSize)
        setLabelDrawable(item.textDrawable)
    }

    private fun setLabelDrawable(@DrawableRes textDrawable: Int?) {
        binding.tvContentLabel.setDrawable(
            end = getLabelDrawableEndInfoIcon(textDrawable),
            drawablePadding = INFO_ICON_DRAWABLE_PADDING_DP
        )
    }

    private fun setLabelText(text: String) {
        binding.tvContentLabel.text = text
    }

    private fun setLabelTextSize(textSize: Int) {
        binding.tvContentLabel.textSize = textSize.toFloat()
    }

    private fun getLabelDrawableEndInfoIcon(
        @DrawableRes icon: Int?
    ): Drawable? {
        val iconNotNull = icon ?: -1
        return try {
            ContextCompat.getDrawable(binding.root.context, iconNotNull)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    private fun initListeners() {
        binding.tvContentLabel.setDrawableClickListener { _, _ ->
            actionListener.invoke(FriendsContentActions.ShowOnboardingAction)
        }
    }
}
