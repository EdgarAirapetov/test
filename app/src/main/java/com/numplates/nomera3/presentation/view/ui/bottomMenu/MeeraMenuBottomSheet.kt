package com.numplates.nomera3.presentation.view.ui.bottomMenu

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.forEachIndexed
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.drawable
import com.meera.core.extensions.gone
import com.meera.core.extensions.simpleName
import com.meera.core.extensions.string
import com.meera.core.views.BottomItemProgressView
import com.meera.core.views.BottomItemView
import com.meera.uikit.widgets.cell.CellLeftElement
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.UiKitCell
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.BottomMenuBinding
import com.numplates.nomera3.databinding.BottomMenuTitleWithBackBinding
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.custom.ReactionBottomMenuItem
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.numplates.nomera3.presentation.view.ui.CloseTypes
import timber.log.Timber

const val COMMENT_MENU_TAG = "COMMENT_MENU_TAG"
const val POST_MENU_TAG = "POST_MENU_TAG"
const val PROGRESS_MENU_TAG = "PROGRESS_MENU_TAG"

open class MeeraMenuBottomSheet(private val activityContext: Context?) :
    BaseBottomSheetDialogFragment<BottomMenuBinding>() {

    interface Listener {
        fun onDismiss() = Unit
        fun onCancelByUser(menuTag: String?) = Unit
    }

    //TODO ROAD_RIX
    companion object {
//        private val TITLE_LEFT_PADDING = 20.dp
//        private val TITLE_TOP_PADDING = 16.dp
//        private val TITLE_RIGHT_PADDING = 16.dp
//        private val TITLE_BOTTOM_PADDING = 4.dp
//        private const val TITLE_TEXT_SIZE = 20f
    }

    var viewList: MutableList<View> = mutableListOf()
    var isClickedBack = false

    val isEmpty: Boolean
        get() = viewList.isEmpty()

    private var listener: Listener? = null

    private var progressItem: BottomItemProgressView? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> BottomMenuBinding
        get() = BottomMenuBinding::inflate

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? Listener ?: parentFragment as? Listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, null)
        addTitle(R.string.actions)
        initCancel()
        initViewList()

        dialog?.let {
            val sheet = it as BottomSheetDialog
            sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            sheet.behavior.skipCollapsed = true
        }
    }

    private fun initViewList() {
        viewList.forEachIndexed { index, view ->
            val lastItemIndex = viewList.size - 1
            if (view is ReactionBottomMenuItem) {
                binding?.nsvMenu?.setTouchDisabled(true)
            }
            if (view is UiKitCell) {
                if (viewList.size == 1) {
                    view.cellPosition = CellPosition.ALONE
                } else {
                    when (index) {
                        0 -> view.cellPosition = CellPosition.TOP
                        lastItemIndex -> view.cellPosition = CellPosition.BOTTOM
                        else -> view.cellPosition = CellPosition.MIDDLE
                    }
                }

            }
            binding?.llBottomMenuContainer?.addView(view)
        }
    }

    inline fun <reified ItemType> getMenuItem(): ItemType? {
        return this.viewList.find { it is ItemType } as? ItemType
    }

    fun show(manager: FragmentManager?) = showWithTag(manager, simpleName)

    fun showWithTag(manager: FragmentManager?, tag: String?) {
        val fragment = manager?.findFragmentByTag(tag)
        if (fragment != null) return
        manager?.let {
            super.show(manager, tag)
        }
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    fun addReactions(reactions: List<ReactionEntity>, selectListener: (ReactionType) -> Unit) {
        activityContext?.let { context ->
            isDraggableBehavior = false
            val reactionsItem = ReactionBottomMenuItem(context) {reactionType ->
                binding?.nsvMenu?.setTouchDisabled(false)
                selectListener.invoke(reactionType)
            }
            reactionsItem.setReaction(reactions)

            viewList.add(reactionsItem)
        }
    }

    fun addTitle(@StringRes title: Int) {
        activityContext?.let {
            binding?.tvBottomSheetDialogLabel?.text = activityContext.getString(title)
        }
    }

    fun addTitleWithBack(@StringRes title: Int, click: () -> Unit) {
        activityContext?.let {
            with(BottomMenuTitleWithBackBinding.inflate(LayoutInflater.from(it), null, false)) {
                tvTitle.text = it.getString(title)
                ivBack.setOnClickListener {
                    dismiss()
                    click.invoke()
                }
                viewList.add(0, root)
            }
        }
    }

    fun showLoadingProgress() {
        activityContext?.let { ctx ->
            val count = binding?.llBottomMenuContainer?.childCount?.minus(1) ?: 0
            binding?.llBottomMenuContainer?.forEachIndexed { index, view ->
                if (index != count) view.gone()
            }
            val view = BottomItemProgressView(ctx)
            view.tag = PROGRESS_MENU_TAG
            binding?.llBottomMenuContainer?.addView(view, 0)
            progressItem = view
            viewList.add(view)
        }
    }

    fun setLoadingProgress(progress: Int) {
        progressItem?.setProgress(progress)
    }

    fun addDivider() {
        activityContext?.let {
            val divider = View(activityContext)
            divider.layoutParams = ViewGroup.LayoutParams(
                MATCH_PARENT,
                1.dp
            )
            divider.background = ContextCompat.getDrawable(
                activityContext,
                R.drawable.drawable_divider_decoration_gray
            )
            viewList.add(divider)
        }
    }

    fun addItem(title: String?, click: () -> Unit) {
        activityContext?.let {
            addItem(title = title, icon = null, click = click)
        }
    }

    fun addItem(@StringRes title: Int, click: () -> Unit) {
        activityContext?.let {
            val mTitle = activityContext.getString(title)
            addItem(title = mTitle, icon = null, click = click)
        }
    }

    fun addItemIsArrow(
        @StringRes title: Int,
        @DrawableRes icon: Int,
        click: () -> Unit
    ) {
        activityContext?.let {
            val mTitle = activityContext.string(title)
            val mIcon = activityContext.drawable(icon) as Any
            val itemView = BottomItemView(activityContext)
            itemView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            itemView.setTitle(mTitle)
            itemView.setIcon(mIcon)
            viewList.add(itemView)
            itemView.click {
                this.dismiss()
                click.invoke()
            }
        }
    }


    fun addItem(
        @StringRes title: Int,
        @DrawableRes icon: Int,
        bottomSeparatorVisible: Boolean = false,
        isDismissMenu: Boolean = true,
        click: () -> Unit
    ) {
        activityContext?.let {
            val mTitle = activityContext.getString(title)
            addItem(
                title = mTitle,
                icon = icon,
                bottomSeparatorVisible = bottomSeparatorVisible,
                isDismissMenu = isDismissMenu,
                click = click
            )
        }
    }

    fun addItem(@StringRes title: Int,
                @DrawableRes icon: Int,
                @ColorRes iconAndTitleColor: Int,
                vararg formatArgs: Any?,
                click: () -> Unit) {
        activityContext?.let {
            val mTitle = activityContext.getString(title, *formatArgs)
            addItem(title = mTitle, icon = icon, click = click, iconAndTitleColor = iconAndTitleColor)
        }
    }

    fun addItem(@StringRes title: Int, @DrawableRes icon: Int, click: () -> Unit,
                @ColorRes color: Int? = null, isNewItem: Boolean) {
        activityContext?.let {
            val mTitle = activityContext.string(title)
            val mIcon = activityContext.drawable(icon) as Any
            val itemView = BottomItemView(activityContext)

            itemView.layoutParams = ViewGroup.LayoutParams(
                MATCH_PARENT,
                WRAP_CONTENT)

            itemView.setTitle(mTitle)
            itemView.setIcon(mIcon)

            color?.let {
                    color-> itemView.setIconColor(color)
            }

            if (isNewItem) {
                itemView.createNewItemLabel()
            }

            viewList.add(itemView)

            itemView.click {
                this.dismiss()
                click()
            }
        }
    }

    fun addItemWithColor(
        @StringRes title: Int,
        @DrawableRes icon: Int,
        @ColorRes color: Int,
        click: () -> Unit
    ) {
        activityContext?.let {
            val mTitle = activityContext.getString(title)
            addItem(title = mTitle, icon = icon, click = click, color = color)
        }
    }

    private fun initCancel() {
        binding?.ivBottomSheetDialogClose?.setOnClickListener {
            dismissAllowingStateLoss()
            listener?.onCancelByUser(tag)
        }
    }

    fun addItem(
        title: String?,
        @DrawableRes icon: Int?,
        @ColorRes color: Int? = null,
        @ColorRes iconAndTitleColor: Int = R.color.uiKitColorForegroundPrimary,
        topSeparatorVisible: Boolean = false,
        bottomSeparatorVisible: Boolean = false,
        isDismissMenu: Boolean = true,
        click: () -> Unit,
    ) {
        Timber.e("$topSeparatorVisible $bottomSeparatorVisible")
        activityContext?.let {

            val itemView = UiKitCell(it).apply {
                title?.let {
                    setTitleValue(title.toString())
                }

                icon?.let {
                    cellLeftElement = CellLeftElement.ICON
                    setLeftIcon(icon)
                }
                cellBackgroundColor = R.color.uiKitColorBackgroundSecondary
                cellPosition = CellPosition.ALONE
                cellLeftIconAndTitleColor = iconAndTitleColor
                click {
                    if (isDismissMenu)
                        dismiss()
                    click.invoke()
                }
            }

            viewList.add(itemView)
            itemView.setOnClickListener {
                if (isDismissMenu) this.dismiss()
                click.invoke()
            }
        }
    }

    fun addBackItem(
        @StringRes header: Int,
        click: (Boolean) -> Unit
    ) {
        activityContext?.let {
            val mHeader = activityContext.string(header)
            val itemView = BottomItemView(activityContext)
            itemView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            itemView.setTitle(mHeader)
            viewList.add(itemView)
            itemView.click {
                isClickedBack = true
                this.dismiss()
                click.invoke(true)
            }
        }
    }

    fun addRadioItem(
        @StringRes title: Int,
        isSelected: Boolean,
        dismissAfterClick: Boolean,
        tag: String? = null,
        click: (Boolean) -> Unit
    ) {
        activityContext?.let {
            val mTitle = activityContext.string(title)
            val itemView = BottomItemView(activityContext)
            itemView.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            itemView.setTitle(mTitle)
            itemView.showRadioButton(isSelected = isSelected, tag = tag)
            viewList.add(itemView)
            itemView.click {
                if (dismissAfterClick) this.dismiss()
                click.invoke(true)
            }
        }
    }

    fun setRadioButtonChecked(tag: String) {
        viewList.forEach {
            if (it is BottomItemView) {
                it.setRadioButtonChecked(tag)
            }
        }
    }

    fun addSwitchItem(title: String?, isChecked: Boolean, click: (Boolean) -> Unit) {
        activityContext?.let {
            addSwitchItem(title, null, isChecked, click)
        }
    }

    fun addSwitchItem(@StringRes title: Int, isChecked: Boolean, click: (Boolean) -> Unit) {
        activityContext?.let {
            addSwitchItem(activityContext.getString(title), null, isChecked, click)
        }
    }

    fun addSwitchItem(
        @StringRes title: Int,
        icon: Any?,
        isChecked: Boolean,
        click: (Boolean) -> Unit
    ) {
        activityContext?.let {
            addSwitchItem(activityContext.getString(title), icon, isChecked, click)
        }
    }

    fun addSwitchItem(title: String?, icon: Any?, isChecked: Boolean, click: (Boolean) -> Unit) {
        activityContext?.let {
            val itemView = BottomItemView(activityContext)
            itemView.layoutParams = ViewGroup.LayoutParams(
                MATCH_PARENT,
                WRAP_CONTENT
            )

            icon?.let { icon ->
                itemView.setIcon(icon)
            }

            var switchButton: SwitchCompat? = null
            title?.let {
                switchButton = itemView.createSwitchButton(title)
            }
            switchButton?.isChecked = isChecked

            viewList.add(itemView)

            switchButton?.let { switchButton ->
                itemView.setOnClickListener {
                    if (switchButton.isChecked) {
                        switchButton.isChecked = false
                        click.invoke(false)
                    } else {
                        switchButton.isChecked = true
                        click.invoke(true)
                    }
                }
            }
        }
    }

    fun addDescriptionItem(
        title: String?,
        icon: Any?,
        description: String?,
        click: () -> Unit
    ) {

        activityContext?.let {
            val itemView = BottomItemWithDescription(activityContext)
            itemView.layoutParams = ViewGroup.LayoutParams(
                MATCH_PARENT,
                WRAP_CONTENT
            )

            icon?.let { icon ->
                itemView.setIcon(icon)
            }
            title?.let {
                itemView.setTitle(it)
            }

            description?.let {
                itemView.setDescription(it)
            }
            itemView.setOnClickListener {
                click()
            }

            viewList.add(itemView)
        }
    }

    fun addDescriptionItem(
        @StringRes title: Int,
        icon: Any?,
        @StringRes description: Int,
        click: () -> Unit
    ) {

        activityContext?.let {
            val itemView = BottomItemWithDescription(activityContext)
            itemView.layoutParams = ViewGroup.LayoutParams(
                MATCH_PARENT,
                WRAP_CONTENT
            )

            icon?.let { icon ->
                itemView.setIcon(icon)
            }
            title.let { res ->
                itemView.setTitle(it.getString(res))
            }

            description.let { res ->
                itemView.setDescription(it.getString(res))
            }
            itemView.setOnClickListener {
                this.dismiss()
                click()
            }

            viewList.add(itemView)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener?.onDismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        dialogListener?.onDismissDialog(CloseTypes(isClickedBack = isClickedBack))
        super.onDismiss(dialog)
        listener?.onDismiss()
    }
}
