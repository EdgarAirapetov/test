package com.numplates.nomera3.modules.newroads.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.toBoolean
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraPostDotsMenuDialogBinding
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity

class MeeraPostDotsMenuBottomDialog(
    val post: PostUIEntity,
    val isEditAvailable: Boolean,
    val isPostAuthor: Boolean,
    val menuItemClick: (action: MeeraPostDotsMenuAction) -> Unit
): UiKitBottomSheetDialog<MeeraPostDotsMenuDialogBinding>() {
    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraPostDotsMenuDialogBinding
        get() = MeeraPostDotsMenuDialogBinding::inflate

    private var menuAdapter: MeeraPostDotsMenuAdapter? = null

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(labelText = context?.getString(R.string.general_actions))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler(
            post = post,
            menuItemClick = menuItemClick
        )
    }

    private fun initRecycler(
        post: PostUIEntity,
        menuItemClick: (action: MeeraPostDotsMenuAction) -> Unit
    ){
        menuAdapter = MeeraPostDotsMenuAdapter(
            postDotsMenuItemList = filterMeeraPostDotsMenuItemType(),
            post = post,
            menuItemClick = menuItemClick,
            dismissItemClick = ::dismiss
        )

        contentBinding?.rvPostDotsMenu?.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )
        contentBinding?.rvPostDotsMenu?.adapter = menuAdapter
    }

    private fun filterMeeraPostDotsMenuItemType(): List<MeeraPostDotsMenuItemType>{
        val resultList = MeeraPostDotsMenuItemType.entries.toMutableList()

        if (!isPostAuthor) resultList.remove(MeeraPostDotsMenuItemType.DELETE_POST)
        if (!isEditAvailable) resultList.remove(MeeraPostDotsMenuItemType.EDIT_POST)
        if (isPostAuthor && post.user?.isSystemAdministrator != true && post.user?.subscriptionOn.toBoolean().not()){
            resultList.remove(MeeraPostDotsMenuItemType.HIDE_ALL_PROFILE_POST)
        }
        if (isPostAuthor) resultList.remove(MeeraPostDotsMenuItemType.COMPLAIN_POST)
        post.user?.subscriptionOn?.let { isSubscribed ->
            if (isSubscribed.isFalse() && isPostAuthor) {
                resultList.remove(MeeraPostDotsMenuItemType.SUBSCRIBE_TO_PROFILE)
            }
        }
        return resultList
    }
}

enum class MeeraPostDotsMenuItemType(val position: Int){
    EDIT_POST(0),
    SAVE_TO_DEVICE(1),
    SUBSCRIBE_TO_POST(2),
    SUBSCRIBE_TO_PROFILE(3),
    SHARE(4),
    COPY_LINK(5),
    HIDE_ALL_PROFILE_POST(6),
    COMPLAIN_POST(7),
    DELETE_POST(8)
}
