package com.numplates.nomera3.presentation.view.fragments

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.meera.core.extensions.invisible
import com.meera.core.extensions.preloadAndSet
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemAvatarBinding
import com.numplates.nomera3.modules.baseCore.domain.model.Gender
import com.numplates.nomera3.modules.userprofile.ui.model.PhotoModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AvatarsAdapter(val gender: Gender?, private val onAvatarClick: (Int) -> Unit) :
    ListAdapter<PhotoModel, AvatarsAdapter.AvatarVH>(object : DiffUtil.ItemCallback<PhotoModel>() {
        override fun areItemsTheSame(oldItem: PhotoModel, newItem: PhotoModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PhotoModel, newItem: PhotoModel): Boolean {
            return oldItem == newItem
        }
    }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarVH {
        return AvatarVH(
            binding = ItemAvatarBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onAvatarClick = onAvatarClick
        )
    }

    override fun onBindViewHolder(holder: AvatarVH, position: Int) = holder.onBind(getItem(position), gender)

    class AvatarVH(val binding: ItemAvatarBinding, val onAvatarClick: (Int) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: PhotoModel, gender: Gender?) {
            binding.ivAvatar.let { ivAvatar ->

                if (item.imageUrl.isNotEmpty()) {
                    Glide.with(ivAvatar).preloadAndSet(item.imageUrl, ivAvatar)
                } else {
                    if (gender != null) {
                        val ivG = if (gender == Gender.MALE) R.drawable.fill_8 else R.drawable.profile_picture1_female
                        setImageDrawable(ivG, ivAvatar)
                    } else {
                        setImageDrawable(R.drawable.fill_8, ivAvatar)
                    }
                }
            }
            if (item.animation.isNullOrEmpty()) {
                binding.vAvatarView.invisible()
                binding.ivAvatar.visible()
            } else {
                binding.vAvatarView.visible()
                binding.vAvatarView.avatarIsReadyCallback = {
                    binding.vAvatarView.startParallaxEffect()
                    binding.ivAvatar.invisible()
                }
                binding.vAvatarView.setStateAsync(item.animation, CoroutineScope(Dispatchers.Main))
            }

            binding.ivAvatar.setThrottledClickListener {
                onAvatarClick.invoke(absoluteAdapterPosition)
            }

            binding.vAvatarView.setThrottledClickListener {
                onAvatarClick.invoke(absoluteAdapterPosition)
            }
        }

        private fun setImageDrawable(@DrawableRes drawable: Int, imageView: ImageView) {
            Glide.with(imageView.context).load(drawable).transition(DrawableTransitionOptions.withCrossFade(200))
                .into(imageView)
        }
    }
}
