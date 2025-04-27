package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.meera.core.extensions.click
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityHolidayFloor
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setHtmlText
import com.meera.core.extensions.textColor
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction


class HolidayFloorViewHolder(
    val parent: ViewGroup,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit
): BaseUserViewHolder<UserEntityHolidayFloor>(parent, R.layout.item_holiday_floor) {

    val clContainer: ConstraintLayout = itemView.findViewById(R.id.clContainer)
    val tvDesc: AppCompatTextView = itemView.findViewById(R.id.tv_desc)
    val tvTitle: AppCompatTextView = itemView.findViewById(R.id.tv_title)
    val ivImg: AppCompatImageView = itemView.findViewById(R.id.iv_img)
    val vSeparator: View = itemView.findViewById(R.id.separator)

    override fun bind(data: UserEntityHolidayFloor) {
        ivImg.loadGlide(data.giftItem.image)
        tvTitle.text = data.giftItem.customTitle
        data.giftItem.customDesc?.let { tvDesc.setHtmlText(it) }
        clContainer.click {
            profileUIActionHandler(UserProfileUIAction.OnSendGiftClicked(data.giftItem))
        }
        if (data.isVip) {
            tvDesc.textColor(R.color.white_1000)
            tvTitle.textColor(R.color.white_1000)
        }
        if (data.isSeparable) vSeparator.visible()
        else vSeparator.gone()
    }

}
