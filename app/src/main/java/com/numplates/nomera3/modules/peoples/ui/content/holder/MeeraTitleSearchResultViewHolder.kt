package com.numplates.nomera3.modules.peoples.ui.content.holder

import com.numplates.nomera3.databinding.MeeraItemSearchResultTitleBinding
import com.numplates.nomera3.modules.peoples.ui.content.entity.TitleSearchResultUiEntity

class MeeraTitleSearchResultViewHolder(
    private val binding: MeeraItemSearchResultTitleBinding
) : BasePeoplesViewHolder<TitleSearchResultUiEntity, MeeraItemSearchResultTitleBinding>(binding) {

    override fun bind(item: TitleSearchResultUiEntity) {
        super.bind(item)
        binding.tvSearchResultTitle.text = itemView.context.getString(item.titleResource)
    }

}
