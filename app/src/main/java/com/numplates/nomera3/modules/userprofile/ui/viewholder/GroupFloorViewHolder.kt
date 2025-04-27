package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.addClickWithData
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.updatePadding
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityGroupFloor
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.GroupsProfileListAdapterNew
import com.meera.core.extensions.getColorCompat
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction


class GroupFloorViewHolder(
    parent: ViewGroup,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit
) : BaseUserViewHolder<UserEntityGroupFloor>(parent, R.layout.item_group_floor) {

    // в тексте определяем слова 34..46 - "создайте свое!"
    private val RANGE_CREATE = 34..46
    // в тексте определяем слова 0..5-"Найдите"
    private val RANGE_FIND = 0..5

    private val profileTitleGroups = itemView.findViewById<TextView>(R.id.profile_title_groups)
    private val tvGroupsAmount = itemView.findViewById<TextView>(R.id.tvGroupsAmount)
    private val tvSearchGroups = itemView.findViewById<TextView>(R.id.tvSearchGroups)

    private val rvGroups = itemView.findViewById<RecyclerView>(R.id.rvGroups)

    //placeholder
    private val clEmptyGroupContainer = itemView.findViewById<ConstraintLayout>(R.id.cl_empty_group_container)
    private val ivEmptyGroup = itemView.findViewById<ImageView>(R.id.iv_empty_group)
    private val tvEmptyGroupMessage = itemView.findViewById<TextView>(R.id.tv_empty_group_message)
    private val clEmptyGroupRoot = itemView.findViewById<ConstraintLayout>(R.id.layout_empty_group)

    override fun bind(data: UserEntityGroupFloor) {
        when (data.userTypeEnum) {
            AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
            AccountTypeEnum.ACCOUNT_TYPE_PREMIUM -> {
                setupCommonTheme()
            }
            AccountTypeEnum.ACCOUNT_TYPE_VIP -> {
                setupVipTheme()
            }
            else -> {}
        }
        initRecyclerView(data)
        setupText(data)
    }

    /**
     * Отображение плейсхолдера об отсутствии групп (отображается только у себя)
     * в тексте определяем слова 0..7-"Найдите" 45..53 - "создайте"
     * отмечаются цветом в зависимости от статуса юзера и устанавливаются слушатели на нажатие
     * */
    private fun setupText(data: UserEntityGroupFloor) {
        val context = itemView.context
        tvGroupsAmount.text = data.groupCount.toString()
        val spanText = SpannableStringBuilder(context.getString(R.string.empty_group_profile_common))
        val txt = if (data.userTypeEnum == AccountTypeEnum.ACCOUNT_TYPE_VIP) {
            spanText.addClickWithData("", RANGE_FIND, ContextCompat.getColor(context, R.color.ui_yellow)) {
                profileUIActionHandler.invoke(UserProfileUIAction.OnFindGroup)
            }
            spanText.addClickWithData("", RANGE_CREATE, ContextCompat.getColor(context, R.color.ui_yellow)) {
                profileUIActionHandler.invoke(UserProfileUIAction.OnCreateGroupClick)
            }
            spanText

        } else {
            spanText.addClickWithData("", RANGE_CREATE, ContextCompat.getColor(context, R.color.ui_purple)) {
                profileUIActionHandler.invoke(UserProfileUIAction.OnCreateGroupClick)
            }
            spanText.addClickWithData("", RANGE_FIND, ContextCompat.getColor(context, R.color.ui_purple)) {
                profileUIActionHandler.invoke(UserProfileUIAction.OnFindGroup)
            }
            spanText
        }
        tvEmptyGroupMessage?.text = txt
        tvEmptyGroupMessage?.movementMethod = LinkMovementMethod.getInstance()
        tvSearchGroups.click {
            profileUIActionHandler.invoke(UserProfileUIAction.OnAllGroupClick)
        }
    }

    private fun initRecyclerView(data: UserEntityGroupFloor) {
        if (data.groups.isNotEmpty()) {
            rvGroups.updatePadding(paddingStart =  if (data.groups.size == 1) 0.dp else 16.dp)

            rvGroups?.visible()
            tvSearchGroups?.visible()
            clEmptyGroupRoot?.gone()
            val groupsLayoutManager = LinearLayoutManager(itemView.context, RecyclerView.HORIZONTAL, false)
            val groupsProfileListAdapter = GroupsProfileListAdapterNew(data.userTypeEnum.value)
            rvGroups?.setHasFixedSize(true)
            rvGroups?.layoutManager = groupsLayoutManager
            rvGroups?.isNestedScrollingEnabled = false
            rvGroups?.adapter = groupsProfileListAdapter
            groupsProfileListAdapter.clickListener = { group ->
                profileUIActionHandler.invoke(UserProfileUIAction.OnGroupClicked(group))
            }
            groupsProfileListAdapter.collection = data.groups
        } else {
            clEmptyGroupRoot?.visible()
            rvGroups?.gone()
            tvSearchGroups?.gone()
        }
    }

    private fun setupVipTheme() {
        val context = itemView.context
        clEmptyGroupContainer?.background = ContextCompat.getDrawable(context, R.drawable.dashed_circle_gold)
        ivEmptyGroup?.setColorFilter(context.getColorCompat(R.color.ui_yellow))
        tvEmptyGroupMessage?.setTextColor(Color.WHITE)
        profileTitleGroups?.setTextColor(ContextCompat.getColor(context, R.color.white_1000))
        tvGroupsAmount?.setTextColor(ContextCompat.getColor(context, R.color.ui_light_gray))
        tvSearchGroups?.setTextColor(ContextCompat.getColor(context, R.color.ui_yellow))
    }

    private fun setupCommonTheme() {
        val context = itemView.context
        clEmptyGroupContainer?.background = ContextCompat.getDrawable(context, R.drawable.dashed_circle_grey)
        ivEmptyGroup?.setColorFilter(context.getColorCompat(R.color.ui_gray))
        tvEmptyGroupMessage?.setTextColor(Color.BLACK)
        profileTitleGroups?.setTextColor(ContextCompat.getColor(context, R.color.ui_black))
        tvGroupsAmount?.setTextColor(ContextCompat.getColor(context, R.color.ui_text_gray))
        tvSearchGroups?.setTextColor(ContextCompat.getColor(context, R.color.ui_purple))
    }
}
