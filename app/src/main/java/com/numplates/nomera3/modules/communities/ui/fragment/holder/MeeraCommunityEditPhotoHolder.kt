package com.numplates.nomera3.modules.communities.ui.fragment.holder

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setTint
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraEditGroupPhotoItemBinding
import com.numplates.nomera3.modules.communities.ui.fragment.MeeraCommunityEditAction

class MeeraCommunityEditPhotoHolder(
    val binding: MeeraEditGroupPhotoItemBinding,
    val listener: (action: MeeraCommunityEditAction) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(avatarBig: String?) {
        binding.ivEmptyImage.setTint(R.color.uiKitColorForegroundSecondary)
        if (avatarBig?.isNotEmpty() == true) {
            loadImageUrl(avatarBig)
        } else {
            binding.addPhotoBtn.setThrottledClickListener {
                listener.invoke(MeeraCommunityEditAction.AddPhoto {
                    setImage(it)
                })
            }
            binding.ivImageGroup.isClickable = false
        }
    }

    private fun loadImageUrl(imageUrl: String) {
        Glide.with(binding.root)
            .load(imageUrl)
            .into(binding.ivImageGroup)
        communityImageVisible(imageUrl)
    }

    private fun setImage(imageUrl: String) {
        if (imageUrl.isNotEmpty()) {
            binding.ivImageGroup.setImageURI(Uri.parse(imageUrl))
            communityImageVisible(imageUrl, true)
        }
    }

    private fun communityImageVisible(imageUrl: String, isLoadDevices: Boolean = false) {
        binding.vEditImageBtn.visible()
        binding.vDeleteImageBtn.visible()
        binding.ivImageGroup.visible()
        binding.vPhotoEmptyState.invisible()
        binding.ivEmptyImageState.gone()
        initEditPhotoListener(imageUrl, isLoadDevices)
        initDeletePhotoListener()
    }

    private fun communityImageInvisible() {
        binding.vEditImageBtn.invisible()
        binding.vDeleteImageBtn.invisible()
        binding.ivImageGroup.invisible()
        binding.vPhotoEmptyState.visible()
        binding.ivEmptyImageState.visible()
    }

    private fun initEditPhotoListener(imageUrl: String, isLoadDevices: Boolean) {
        binding.vEditImageBtn.setThrottledClickListener {
            listener.invoke(MeeraCommunityEditAction.EditPhoto(imageUrl, isLoadDevices) {
                setImage(it)
            })
        }

        binding.ivImageGroup.setThrottledClickListener {
            listener.invoke(MeeraCommunityEditAction.OpenPicker {
                setImage(it)
            })
        }
    }

    private fun initDeletePhotoListener() {
        binding.vDeleteImageBtn.setThrottledClickListener {
            listener.invoke(MeeraCommunityEditAction.DeletePhoto())
            communityImageInvisible()
        }
    }

}
