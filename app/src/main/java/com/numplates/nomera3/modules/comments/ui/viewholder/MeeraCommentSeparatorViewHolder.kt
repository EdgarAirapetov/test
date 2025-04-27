package com.numplates.nomera3.modules.comments.ui.viewholder

import android.view.ViewGroup
import android.widget.TextView
import com.meera.core.extensions.click
import com.meera.core.extensions.gone
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.string
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.comments.ui.entity.CommentSeparatorEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentSeparatorType
import com.numplates.nomera3.modules.notifications.ui.viewholder.BaseViewHolder
import com.numplates.nomera3.presentation.view.ui.MeeraLoaderView


class MeeraCommentSeparatorViewHolder(
        viewGroup: ViewGroup
) : BaseViewHolder(viewGroup, R.layout.meera_item_comment_separator) {

    private val progress = itemView.findViewById<MeeraLoaderView>(R.id.lv_cs_progress_indicator)
    private val textView = itemView.findViewById<TextView>(R.id.title)

    fun bind(data: CommentSeparatorEntity, listener: (CommentSeparatorEntity) -> Unit) {
        hideProgress()
        itemView.click {
            if (data.separatorType == CommentSeparatorType.SHOW_MORE) showProgress()
            listener(data)
        }

        val count = data.count

        val text =
                when (data.separatorType) {
                    CommentSeparatorType.SHOW_MORE ->
                        getTextForComment(count)

                    CommentSeparatorType.HIDE_ALL ->
                        itemView.context.string(R.string.hide_all_answers)
                }

        textView.text = text
    }

    private fun getTextForComment(count: Int?): String = count?.let {
        itemView.context.pluralString(R.plurals.more_comment_plurals, count)
    }?: kotlin.run {
        ""
    }

    private fun showProgress(){
        itemView.isClickable = false
        progress.show()
        textView.gone()
    }

    private fun hideProgress(){
        itemView.isClickable = true
        progress.hide()
        textView.visible()
    }

}
