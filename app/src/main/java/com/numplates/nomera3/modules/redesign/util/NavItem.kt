package com.numplates.nomera3.modules.redesign.util

import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.ActionProvider
import android.view.ContextMenu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import com.numplates.nomera3.R

private const val ID_UNDEFINED = -1
private const val ORDER_FIRST = 1
private const val GROUP_ID_FIRST = 1

// Временная реализация элементов bottom view
enum class NavTabItem(val itemNav: ItemNav) {
    ROAD_TAB_ITEM(ItemNav(ids = R.id.mainRoadFragment, orders = ORDER_FIRST, groupIds = GROUP_ID_FIRST)),
    MAP_TAB_ITEM(ItemNav(ids = R.id.emptyMapFragment, orders = ORDER_FIRST, groupIds = GROUP_ID_FIRST)),
    SWITCH_TAB_ITEM(ItemNav(ids = ID_UNDEFINED, orders = ORDER_FIRST, groupIds = GROUP_ID_FIRST)),
    CHAT_TAB_ITEM(ItemNav(ids = R.id.mainChatFragment, orders = ORDER_FIRST, groupIds = GROUP_ID_FIRST)),
    SERVICE_TAB_ITEM(ItemNav(ids = R.id.servicesNavGraph, orders = ORDER_FIRST, groupIds = GROUP_ID_FIRST));

    companion object {
        fun checkIfIdUndefined(tabItem: NavTabItem): Boolean =
            tabItem.itemNav.ids == ID_UNDEFINED
    }
}


class ItemNav(
    val ids: Int,
    val orders: Int,
    val groupIds: Int
) : MenuItem {
    override fun getItemId(): Int = ids

    override fun getGroupId(): Int = groupIds

    override fun getOrder(): Int = orders

    override fun setTitle(title: CharSequence?): MenuItem {
        TODO("Not yet implemented")
    }

    override fun setTitle(title: Int): MenuItem {
        TODO("Not yet implemented")
    }

    override fun getTitle(): CharSequence? {
        TODO("Not yet implemented")
    }

    override fun setTitleCondensed(title: CharSequence?): MenuItem {
        TODO("Not yet implemented")
    }

    override fun getTitleCondensed(): CharSequence? {
        TODO("Not yet implemented")
    }

    override fun setIcon(icon: Drawable?): MenuItem {
        TODO("Not yet implemented")
    }

    override fun setIcon(iconRes: Int): MenuItem {
        TODO("Not yet implemented")
    }

    override fun getIcon(): Drawable? {
        TODO("Not yet implemented")
    }

    override fun setIntent(intent: Intent?): MenuItem {
        TODO("Not yet implemented")
    }

    override fun getIntent(): Intent? {
        TODO("Not yet implemented")
    }

    override fun setShortcut(numericChar: Char, alphaChar: Char): MenuItem {
        TODO("Not yet implemented")
    }

    override fun setNumericShortcut(numericChar: Char): MenuItem {
        TODO("Not yet implemented")
    }

    override fun getNumericShortcut(): Char {
        TODO("Not yet implemented")
    }

    override fun setAlphabeticShortcut(alphaChar: Char): MenuItem {
        TODO("Not yet implemented")
    }

    override fun getAlphabeticShortcut(): Char {
        TODO("Not yet implemented")
    }

    override fun setCheckable(checkable: Boolean): MenuItem {
        TODO("Not yet implemented")
    }

    override fun isCheckable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setChecked(checked: Boolean): MenuItem {
        TODO("Not yet implemented")
    }

    override fun isChecked(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setVisible(visible: Boolean): MenuItem {
        TODO("Not yet implemented")
    }

    override fun isVisible(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setEnabled(enabled: Boolean): MenuItem {
        TODO("Not yet implemented")
    }

    override fun isEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasSubMenu(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getSubMenu(): SubMenu? {
        TODO("Not yet implemented")
    }

    override fun setOnMenuItemClickListener(menuItemClickListener: MenuItem.OnMenuItemClickListener?): MenuItem {
        TODO("Not yet implemented")
    }

    override fun getMenuInfo(): ContextMenu.ContextMenuInfo? {
        TODO("Not yet implemented")
    }

    override fun setShowAsAction(actionEnum: Int) {
        TODO("Not yet implemented")
    }

    override fun setShowAsActionFlags(actionEnum: Int): MenuItem {
        TODO("Not yet implemented")
    }

    override fun setActionView(view: View?): MenuItem {
        TODO("Not yet implemented")
    }

    override fun setActionView(resId: Int): MenuItem {
        TODO("Not yet implemented")
    }

    override fun getActionView(): View? {
        TODO("Not yet implemented")
    }

    override fun setActionProvider(actionProvider: ActionProvider?): MenuItem {
        TODO("Not yet implemented")
    }

    override fun getActionProvider(): ActionProvider? {
        TODO("Not yet implemented")
    }

    override fun expandActionView(): Boolean {
        TODO("Not yet implemented")
    }

    override fun collapseActionView(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isActionViewExpanded(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setOnActionExpandListener(listener: MenuItem.OnActionExpandListener?): MenuItem {
        TODO("Not yet implemented")
    }
}
