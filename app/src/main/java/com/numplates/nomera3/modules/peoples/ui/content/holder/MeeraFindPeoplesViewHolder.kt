package com.numplates.nomera3.modules.peoples.ui.content.holder

import com.meera.core.extensions.dp
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraItemFindPeoplesActionBinding
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.action.SeparatorHandler
import com.numplates.nomera3.modules.peoples.ui.content.entity.FindPeoplesUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.FriendFindContentType
import com.numplates.nomera3.modules.peoples.ui.utils.OffsetProvider

private const val FIRST_TOP_OFFSET = 12
private const val DEFAULT_OFFSET = 8

class MeeraFindPeoplesViewHolder(
    private val binding: MeeraItemFindPeoplesActionBinding,
    private val actionListener: (actions: FriendsContentActions) -> Unit
) : BasePeoplesViewHolder<FindPeoplesUiEntity, MeeraItemFindPeoplesActionBinding>(binding), SeparatorHandler,
    OffsetProvider {

    init {
        initListeners()
    }

    override fun bind(item: FindPeoplesUiEntity) {
        super.bind(item)
        setItemData(item)
    }

    override fun needDrawDivider(): Boolean {
        return item?.isNeedToDrawSeparator ?: true
    }

    override fun provide(): Int = when (item?.contentType) {
        FriendFindContentType.FIND_FRIENDS -> FIRST_TOP_OFFSET.dp
        else -> DEFAULT_OFFSET.dp
    }

    private fun initListeners() {
        binding.root.setThrottledClickListener {
            when (item?.contentType) {
                FriendFindContentType.FIND_FRIENDS ->
                    actionListener.invoke(FriendsContentActions.FindFriendsContentUiActions)
                FriendFindContentType.INVITE_FRIENDS ->
                    actionListener.invoke(FriendsContentActions.OnReferralClicked)
                FriendFindContentType.BUMP ->
                    actionListener.invoke(FriendsContentActions.OnShowBumpClicked)
                else -> Unit
            }
        }
    }

    private fun setItemData(item: FindPeoplesUiEntity) {
        binding.ukcFindPeoples.apply {
            setTitleValue(item.label)
            setDescriptionValue(item.description)
            setLeftIcon(item.icon)
        }
    }
}
