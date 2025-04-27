package com.numplates.nomera3.modules.userprofile.ui.viewholder

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.adapters.baserecycleradapter.BaseVH
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.databinding.MeeraGalleryFloorItemBinding
import com.numplates.nomera3.modules.userprofile.ui.fragment.MeeraGalleryShimmerAdapter
import com.numplates.nomera3.modules.userprofile.ui.fragment.UserInfoRecyclerData
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.PhotoProfileListAdapterNew

private const val COUNT_SHIMMER_ITEM = 4

class MeeraGalleryFloorViewHolder(
    private val binding: MeeraGalleryFloorItemBinding,
    private val profileUIActionHandler: (UserProfileUIAction) -> Unit,
) : BaseVH<UserInfoRecyclerData, MeeraGalleryFloorItemBinding>(binding) {

    private var mData: UserInfoRecyclerData.UserEntityGalleryFloor? = null

    override fun bind(data: UserInfoRecyclerData) {
        mData = data as UserInfoRecyclerData.UserEntityGalleryFloor

        setupText(data)

        binding.profileTitlePhotos.setThrottledClickListener {
            profileUIActionHandler.invoke(UserProfileUIAction.OnGridGalleryClicked)
        }

        binding.tvShowAllPhotos.setThrottledClickListener {
            profileUIActionHandler.invoke(UserProfileUIAction.OnGridGalleryClicked)
        }

        if (data.isMineGallery) {
            binding.vAddPhotoBtn.visible()
            binding.vAddPhotoBtn.setThrottledClickListener {
                profileUIActionHandler.invoke(UserProfileUIAction.AddPhoto)
            }
            if (data.listPhotoEntity.isEmpty()) showEmptyPlaceholder()
            else hideEmptyPlaceholder()
        } else {
            binding.vAddPhotoBtn.gone()
            if (data.listPhotoEntity.isEmpty()) itemView.gone()
        }
        showRvGallery(data)
    }

    private fun showRvGallery(data: UserInfoRecyclerData.UserEntityGalleryFloor) {
        val photoManager = LinearLayoutManager(
            itemView.context,
            RecyclerView.HORIZONTAL, false
        )

        val adapter = PhotoProfileListAdapterNew()

        val shimmerAdapter = MeeraGalleryShimmerAdapter()
        val listShimmer = List(COUNT_SHIMMER_ITEM) { String.empty() }

        binding.rvPhotosShimmer.adapter = shimmerAdapter
        shimmerAdapter.submitList(listShimmer)
        showShimmer(data.listPhotoEntity.isNotEmpty())

        binding.rvPhotos.adapter = adapter
        binding.rvPhotos.isNestedScrollingEnabled = false
        binding.rvPhotos.setHasFixedSize(true)
        binding.rvPhotos.layoutManager = photoManager

        adapter.clickListener = { position, imageView ->
            profileUIActionHandler.invoke(UserProfileUIAction.OnShowImage(data.listPhotoEntity, position))
        }
        adapter.loadSuccess = {
            showShimmer(false)
        }
        adapter.collection = data.listPhotoEntity
    }

    private fun showShimmer(visible: Boolean){
        if (visible) {
            binding.rvPhotosShimmer.visible()
            binding.rvPhotos.invisible()
        } else {
            binding.rvPhotosShimmer.gone()
            binding.rvPhotos.visible()
        }
    }

    private fun hideEmptyPlaceholder() {
        binding.apply {
            rvPhotos.visible()
            tvShowAllPhotos.visible()
            tvPhotosAmount.visible()
            llEmptyState.gone()
        }
    }

    private fun showEmptyPlaceholder() {
        binding.apply {
            rvPhotos.gone()
            tvShowAllPhotos.invisible()
            tvPhotosAmount.invisible()
            llEmptyState.visible()
        }
    }

    private fun setupText(data: UserInfoRecyclerData.UserEntityGalleryFloor) {
        binding.tvPhotosAmount.text = data.photoCount.toString()
    }
}
