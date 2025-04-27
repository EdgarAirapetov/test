package com.numplates.nomera3.modules.peoples.ui.content.holder

import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.ItemPeopleSyncContactsBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeopleSyncContactsUiModel

class PeopleSyncContactsHolder constructor(
    private val binding: ItemPeopleSyncContactsBinding,
    private val actionListener: (FriendsContentActions) -> Unit
) : BasePeoplesViewHolder<PeopleSyncContactsUiModel, ItemPeopleSyncContactsBinding>(binding) {

    init {
        initListeners()
    }

    override fun bind(item: PeopleSyncContactsUiModel) = Unit

    private fun initListeners() {
        binding.root.setThrottledClickListener {
            actionListener.invoke(FriendsContentActions.OnSyncContactsUiAction)
        }
    }
}
