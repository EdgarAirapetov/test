package com.numplates.nomera3.modules.userprofile.ui.viewholder

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.setBackgroundTint
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setOnActionMoveListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.userprofile.ui.action.NestedRecyclerAction
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityGalleryFloor
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.PhotoProfileListAdapterNew

class GalleryFloorViewHolder(
    parent: ViewGroup,
    private val action: NestedRecyclerAction? = null,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit,
) : BaseUserViewHolder<UserEntityGalleryFloor>(parent, R.layout.item_gallery_floor) {

    private val profileTitlePhotos = itemView.findViewById<TextView>(R.id.profile_title_photos)
    private val tvPhotosAmount = itemView.findViewById<TextView>(R.id.tvPhotosAmount)
    private val tvAddPhoto = itemView.findViewById<TextView>(R.id.tvAddPhoto)

    private val rvPhotos = itemView.findViewById<RecyclerView>(R.id.rvPhotos)

    private val tvShowAllPhotos = itemView.findViewById<TextView>(R.id.tv_show_all_photos)
    private val ivShowAllPhotos = itemView.findViewById<ImageView>(R.id.iv_show_all_photos)
    private val containerShowAllPhotos = itemView.findViewById<LinearLayout>(R.id.container_show_all_photos)

    private val layoutEmptyPhotos = itemView.findViewById<LinearLayout>(R.id.layout_empty_photos)
    private val ivPhotosPlaceholder = itemView.findViewById<ImageView>(R.id.iv_photos_placeholder)
    private val tvPhotosPlaceholder = itemView.findViewById<TextView>(R.id.tv_photos_placeholder)
    private val progressUploadPhoto = itemView.findViewById<ProgressBar>(R.id.progress_upload_photo)
    private val separator = itemView.findViewById<View>(R.id.v_gallery_separator)

    private var mData: UserEntityGalleryFloor? = null

    override fun bind(data: UserEntityGalleryFloor) {
        mData = data
        when (data.accountTypeEnum) {
            AccountTypeEnum.ACCOUNT_TYPE_VIP -> {
                setupVipTheme()
            }
            AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
            AccountTypeEnum.ACCOUNT_TYPE_PREMIUM,
            AccountTypeEnum.ACCOUNT_TYPE_UNKNOWN -> {
                setupCommonTheme()
            }
        }

        setupText(data)

        containerShowAllPhotos?.click {
            profileUIActionHandler.invoke(UserProfileUIAction.OnGridGalleryClicked)
        }

        if (data.isMineGallery) {
            tvAddPhoto.visible()
            tvAddPhoto?.click {
                profileUIActionHandler.invoke(UserProfileUIAction.AddPhoto)
            }
            if (data.listPhotoEntity.isEmpty()) showEmptyPlaceholder()
            else hideEmptyPlaceholder()
        } else {
            tvAddPhoto.gone()
            if (data.listPhotoEntity.isEmpty()) itemView.gone()
        }

        showRvGallery(data)

        if (data.isLoading) showProgress()
        else hideProgress()
        handleSeparator(data.isSeparable)
    }

    private fun handleSeparator(separable: Boolean) {
        if (separable) separator?.visible()
        else separator?.gone()
    }


    private fun showRvGallery(data: UserEntityGalleryFloor) {
        val photoManager = LinearLayoutManager(itemView.context,
                RecyclerView.HORIZONTAL, false)

        val adapter = PhotoProfileListAdapterNew()
        rvPhotos.setOnActionMoveListener {
            action?.onScroll(it)
        }
        rvPhotos?.adapter = adapter
        rvPhotos?.isNestedScrollingEnabled = false
        rvPhotos?.setHasFixedSize(true)
        rvPhotos?.layoutManager = photoManager

        adapter.clickListener = { position, imageView ->
            profileUIActionHandler.invoke(UserProfileUIAction.OnShowImage(data.listPhotoEntity, position))
        }

        adapter.collection = data.listPhotoEntity
    }

    private fun hideEmptyPlaceholder() {
        rvPhotos?.visible()
        containerShowAllPhotos?.visible()
        layoutEmptyPhotos?.gone()
        separator?.setMargins(top = 9.dp)
    }

    private fun showEmptyPlaceholder() {
        rvPhotos?.gone()
        containerShowAllPhotos?.gone()
        layoutEmptyPhotos?.visible()
        layoutEmptyPhotos?.click {
            profileUIActionHandler.invoke(UserProfileUIAction.AddPhoto)
        }
        separator?.setMargins(top = 16.dp)
    }

    private fun setupText(data: UserEntityGalleryFloor) {
        tvPhotosAmount.text = data.photoCount.toString()
    }

    fun showProgress() = progressUploadPhoto.visible()

    fun hideProgress() = progressUploadPhoto.gone()

    private fun setupCommonTheme() {
        val context = itemView.context
        profileTitlePhotos?.setTextColor(ContextCompat.getColor(context, R.color.ui_black))
        tvPhotosAmount?.setTextColor(ContextCompat.getColor(context, R.color.ui_text_gray))
        tvAddPhoto?.setTextColor(ContextCompat.getColor(context, R.color.ui_purple))
        tvShowAllPhotos?.setTextColor(ContextCompat.getColor(context, R.color.ui_purple))

        ivShowAllPhotos?.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.ic_show_all_photos)
        )

        layoutEmptyPhotos?.setBackgroundTint(R.color.ui_gray)

        ivPhotosPlaceholder?.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.ic_show_all_photos_grey)
        )

        tvPhotosPlaceholder?.setTextColor(ContextCompat.getColor(context, R.color.ui_gray))
    }

    private fun setupVipTheme() {
        val context = itemView.context
        profileTitlePhotos?.setTextColor(ContextCompat.getColor(context, R.color.white_1000))
        tvPhotosAmount?.setTextColor(ContextCompat.getColor(context, R.color.ui_light_gray))
        tvAddPhoto?.setTextColor(ContextCompat.getColor(context, R.color.ui_yellow))
        tvShowAllPhotos?.setTextColor(ContextCompat.getColor(context, R.color.ui_yellow))

        ivShowAllPhotos?.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.ic_show_gallery_profile_vip)
        )

        layoutEmptyPhotos?.setBackgroundTint(R.color.ui_yellow)

        ivPhotosPlaceholder?.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.ic_show_gallery_profile_vip)
        )

        tvPhotosPlaceholder?.setTextColor(ContextCompat.getColor(context, R.color.ui_yellow))
    }

}
