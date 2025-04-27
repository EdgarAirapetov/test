package com.numplates.nomera3.modules.peoples.ui.content.holder

import com.numplates.nomera3.databinding.SearchResultTitleItemBinding
import com.numplates.nomera3.modules.peoples.ui.content.entity.TitleSearchResultUiEntity

class TitleSearchResultViewHolder(
    private val binding: SearchResultTitleItemBinding
) : BasePeoplesViewHolder<TitleSearchResultUiEntity, SearchResultTitleItemBinding>(binding) {

    override fun bind(item: TitleSearchResultUiEntity) {
        super.bind(item)
        binding.nameText.text = itemView.context.getString(item.titleResource)
    }

}
