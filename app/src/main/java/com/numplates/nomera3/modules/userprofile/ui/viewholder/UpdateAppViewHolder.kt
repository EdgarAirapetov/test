package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.view.ViewGroup
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityUpdateBtn
import com.meera.core.extensions.click
import com.meera.core.extensions.clickAnimate
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction

class UpdateAppViewHolder(
    parent: ViewGroup,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit
) : BaseUserViewHolder<UserEntityUpdateBtn>(parent, R.layout.item_update_app) {

    override fun bind(data: UserEntityUpdateBtn) {
        itemView.click {
            itemView.clickAnimate()
            profileUIActionHandler.invoke(UserProfileUIAction.UpdateButtonClicked)

        }
    }

}
