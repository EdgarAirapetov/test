package com.numplates.nomera3.modules.newroads.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.newroads.ui.entity.QuickAnswerEntity
import com.meera.core.extensions.inflate

class QuickAnswerAdapter: RecyclerView.Adapter<QuickAnswerViewHolder>() {

    private val collection: MutableList<QuickAnswerEntity> = mutableListOf()

    internal var clickListener: (String, String) -> Unit = { _, _ -> }

    fun addItems(){
        val items = mutableListOf(
            QuickAnswerEntity("😍", "love"),
            QuickAnswerEntity("😎", "eyewear"),
            QuickAnswerEntity("🔥", "fire"),
            QuickAnswerEntity("😅", "laugh"),
            QuickAnswerEntity("😊", "smile"),
            QuickAnswerEntity("\uD83D\uDE14", "sad"),
            QuickAnswerEntity("👍", "like")
        )

        collection.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickAnswerViewHolder {
        return QuickAnswerViewHolder(parent.inflate(R.layout.quick_answer_item))
    }

    override fun getItemCount() = collection.size

    override fun onBindViewHolder(holder: QuickAnswerViewHolder, position: Int) {
        holder.bind(collection[position], clickListener)
    }

}
