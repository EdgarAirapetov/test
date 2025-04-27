package com.numplates.nomera3.modules.comments.ui.viewholder

import android.view.ViewGroup
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.meera.core.extensions.click
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.LOTTIE_LOADER_ANIMATION
import com.numplates.nomera3.LOTTIE_LOADER_SPEED
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.comments.ui.entity.CommentSeparatorEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentSeparatorType
import com.numplates.nomera3.modules.notifications.ui.viewholder.BaseViewHolder
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.string


class CommentSeparatorViewHolder(
        viewGroup: ViewGroup
) : BaseViewHolder(viewGroup, R.layout.item_comment_separator) {

    private val progress = itemView.findViewById<LottieAnimationView>(R.id.lav_progress_indicator)
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
        progress.visible()
        progress.setAnimation(LOTTIE_LOADER_ANIMATION)
        progress.speed = LOTTIE_LOADER_SPEED
        progress.repeatCount = LottieDrawable.INFINITE
        progress.playAnimation()
        textView.gone()
    }

    private fun hideProgress(){
        itemView.isClickable = true
        progress.gone()
        textView.visible()
    }

}
