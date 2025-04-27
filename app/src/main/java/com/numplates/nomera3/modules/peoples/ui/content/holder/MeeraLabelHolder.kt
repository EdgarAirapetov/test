package com.numplates.nomera3.modules.peoples.ui.content.holder

import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.meera.core.extensions.setDrawable
import com.meera.core.extensions.setDrawableClickListener
import com.numplates.nomera3.databinding.MeeraItemLabelBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.HeaderUiEntity


class MeeraLabelHolder(
    private val binding: MeeraItemLabelBinding,
    private val actionListener: (actions: FriendsContentActions) -> Unit,
) : BasePeoplesViewHolder<HeaderUiEntity, MeeraItemLabelBinding>(binding) {

    init {
        binding.tvContentLabel.setDrawableClickListener { _, _ ->
            actionListener.invoke(FriendsContentActions.ShowOnboardingAction)
        }
    }

    override fun bind(item: HeaderUiEntity) {
        super.bind(item)
        setLabelText(item.text)
        setLabelDrawable(item.textDrawable)
    }

    private fun setLabelText(text: String) {
        binding.tvContentLabel.text = text
    }

    private fun setLabelDrawable(@DrawableRes textDrawable: Int?) {
        textDrawable ?: return
        binding.tvContentLabel.setDrawable(end = ContextCompat.getDrawable(binding.root.context, textDrawable))
    }

}
