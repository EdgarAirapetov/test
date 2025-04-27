package com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.databinding.ItemPickerAlbumBinding
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.data.entity.Album
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setThrottledClickListener

class PickerAlbumsAdapter(
    private val listener: (Album) -> Unit
) : RecyclerView.Adapter<PickerAlbumsAdapter.PickerAlbumViewHolder>() {

    var items: List<Album> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickerAlbumViewHolder {
        val binding = ItemPickerAlbumBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PickerAlbumViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: PickerAlbumViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class PickerAlbumViewHolder(
        private val binding: ItemPickerAlbumBinding,
        private val listener: (Album) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Album) {
            binding.apply {
                root.setThrottledClickListener { listener.invoke(item) }
                tvName.text = item.name
                tvImageCount.text = item.imagesCount.toString()
                ivImage.loadGlide(item.lastImageUri)
                cbChecked.isChecked = item.chosen
            }
        }

    }

}
