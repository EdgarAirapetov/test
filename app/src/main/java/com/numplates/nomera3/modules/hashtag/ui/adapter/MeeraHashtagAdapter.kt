package com.numplates.nomera3.modules.hashtag.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.empty
import com.numplates.nomera3.databinding.MeeraItemHashtagInfoBinding
import com.numplates.nomera3.modules.hashtag.ui.viewholder.MeeraHashtagInfoViewHolder
import com.numplates.nomera3.presentation.view.utils.inflateBinding

class MeeraHashtagAdapter: RecyclerView.Adapter<MeeraHashtagInfoViewHolder>() {

    private var text: String = String.empty()

    fun setTotalPostsCountText(text: String) {
        this.text = text
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraHashtagInfoViewHolder {
        return MeeraHashtagInfoViewHolder(parent.inflateBinding(MeeraItemHashtagInfoBinding::inflate))
    }

    override fun onBindViewHolder(holder: MeeraHashtagInfoViewHolder, position: Int) {
        holder.bind(text)
    }

    override fun getItemCount() = 1

}
