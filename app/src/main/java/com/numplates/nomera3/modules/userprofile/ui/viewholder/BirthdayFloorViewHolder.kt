package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityBirthdayFloor
import com.meera.core.extensions.click
import com.meera.core.extensions.clickAnimate
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction

class BirthdayFloorViewHolder(
    val parent: ViewGroup,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit
) : BaseUserViewHolder<UserEntityBirthdayFloor>(parent, R.layout.item_birthday_floor) {

    private val root = itemView.findViewById<ConstraintLayout>(R.id.cl_banner_root)

    override fun bind(data: UserEntityBirthdayFloor) {
        root.click { clickedView ->
            clickedView.clickAnimate()
            profileUIActionHandler(UserProfileUIAction.OnCongratulationClick)
        }
    }

}
