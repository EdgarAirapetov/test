package com.numplates.nomera3.modules.tags.ui.viewholder

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.meera.uikit.widgets.cell.UiKitCell
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.tags.ui.entity.SuggestedTagListUIModel
import com.numplates.nomera3.modules.tags.ui.entity.SuggestedTagListUIModel.UniqueNameUIModel

class UniqueNameViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val vUserTag: UiKitCell = view.findViewById(R.id.v_user_tag)
    private val tagItemContainer: ConstraintLayout = view.findViewById(R.id.cl_tag_item_container)

    fun bind(itemData: UniqueNameUIModel?, onItemClick: ((SuggestedTagListUIModel) -> Unit)?) {
        itemData?.also { model: UniqueNameUIModel ->

            vUserTag.apply {
                cellCityText = false
                setTitleValue(model.userName)
                setSubtitleValue("@"+ model.uniqueName)
                cellTitleVerified = model.isUserVerified
                setLeftUserPicConfig(
                    UserpicUiModel(
                        userAvatarUrl = model.imageURL
                    )
                )
            }

            tagItemContainer.setOnClickListener { onItemClick?.invoke(model) }
        }
    }

    fun unbind() {
        tagItemContainer.setOnClickListener(null)
    }

//    private fun getRequestOptions(): RequestOptions {
//        return RequestOptions()
//                .centerCrop()
//                .apply(RequestOptions.circleCropTransform())
//                .placeholder(R.drawable.fill_8_round)
//                .error(R.drawable.fill_8_round)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .priority(Priority.HIGH)
//    }
}
