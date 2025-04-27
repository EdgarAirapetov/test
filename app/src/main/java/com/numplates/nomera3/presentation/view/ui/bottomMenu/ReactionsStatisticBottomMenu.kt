package com.numplates.nomera3.presentation.view.ui.bottomMenu

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import com.meera.core.extensions.simpleName
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.BottomMenuBinding
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.custom.ReactionBottomMenuVerticalItem
import com.numplates.nomera3.modules.reaction.ui.util.ReactionCounterFormatter
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment

open class ReactionsStatisticBottomMenu(
    private val activityContext: Context?
) : BaseBottomSheetDialogFragment<BottomMenuBinding>() {

    var viewList: MutableList<View> = mutableListOf()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> BottomMenuBinding
        get() = BottomMenuBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, null)
        addCancelItem()
        viewList.forEach {
            binding?.llBottomMenuContainer?.addView(it)
        }
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onDismiss(dialog: DialogInterface) {
        dialogListener?.onDismissDialog()
        super.onDismiss(dialog)
    }

    fun show(manager: FragmentManager?) {
        if (manager == null) return
        if (manager.findFragmentByTag(simpleName) == null) {
            super.show(manager, simpleName)
        }
    }

    fun addTitle(@StringRes title: Int, number: Int) {
        if (activityContext == null) {
            return
        }
        val count = formatReactionCount(activityContext, number)
        addTitle(activityContext.getString(title), count)
    }

    fun addTitle(@StringRes title: Int, label: String? = null) {
        if (activityContext == null) {
            return
        }
        addTitle(activityContext.getString(title), label)
    }

    fun addTitle(title: String, label: String? = null) {
        if (activityContext == null) {
            return
        }
        val titleView = BottomItemTitleView(activityContext)
        titleView.layoutParams = ViewGroup.LayoutParams(
            MATCH_PARENT,
            WRAP_CONTENT
        )
        titleView.setTitle(title)
        titleView.setReactionsNumber(label)
        viewList.add(0, titleView)
    }

    fun addReaction(reaction: ReactionEntity, hasDivider: Boolean = false) {
        if (activityContext == null) {
            return
        }
        val reactionItem = ReactionBottomMenuVerticalItem(activityContext)
        reactionItem.setReaction(reaction)
        if (hasDivider) {
            reactionItem.addDivider()
        }
        viewList.add(reactionItem)
    }

    private fun formatReactionCount(context: Context, count: Int): String {
        val reactionCounterFormatter = ReactionCounterFormatter(
            context.getString(R.string.thousand_lowercase_label),
            context.getString(R.string.million_lowercase_label),
            oneAllow = true,
            thousandAllow = false
        )
        return reactionCounterFormatter.format(
            value = count
        )
    }

    private fun addCancelItem() {
        if (activityContext == null) {
            return
        }
        val itemView = BottomItemView(activityContext)
        itemView.showTopSeparator()
        itemView.layoutParams = ViewGroup.LayoutParams(
            MATCH_PARENT,
            WRAP_CONTENT
        )
        itemView.setTitle(getString(R.string.general_cancel))
        itemView.setIcon(R.drawable.ic_close_crose_black)
        itemView.setIconColor(R.color.ui_purple)
        viewList.add(itemView)
        itemView.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }
}
