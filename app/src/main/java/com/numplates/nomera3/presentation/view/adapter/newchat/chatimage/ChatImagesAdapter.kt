package com.numplates.nomera3.presentation.view.adapter.newchat.chatimage

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.meera.core.extensions.GLIDE_THUMBNAIL_SIZE_MULTIPLIER
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.utils.blur.BlurHelper
import com.meera.db.models.message.MessageAttachment
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.IOnMessageClickedNew
import com.numplates.nomera3.modules.chat.helpers.isFileByLocalPathExists
import com.numplates.nomera3.modules.chat.views.RoundedFrameLayout
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy


class ChatImagesAdapter(
    private val act: Activity,
    private val message: MessageEntity?,
    private val blurHelper: BlurHelper?,
    private val callback: IOnMessageClickedNew,
    private val longClickListener: View.OnLongClickListener? = null,
    private val containerWidth: Int,
    private val isMyImages: Boolean,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val dataList = mutableListOf<PostImage>()
    private val imageCornerDecorator = ImageCornerDecorator(dpToPx(IMAGE_CORNER_SIZE_DP).toFloat())
    private var recyclerView: RecyclerView? = null

    val lookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return when (dataList.size) {
                1,
                2 -> MAX_ROWS
                3,
                4 -> if (position == 0) MAX_ROWS else MAX_ROWS / (dataList.size - 1)
                5 -> if (position in 0..1) MAX_ROWS / 2 else MAX_ROWS / 3
                else -> error("Please specify case for this scenario.")
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = null
        super.onDetachedFromRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_image, parent, false)
        val holder = ChatImageHolder(itemView = view)

        holder.itemView.setOnClickListener {
            removePlaceholdersImage()
            callback.onImageClicked(message, dataList, holder.adapterPosition)
        }

        longClickListener?.let {
            holder.itemView.setOnLongClickListener(it)
        }

        return holder
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ChatImageHolder) {
            holder.bind(dataList[position])
        }
    }

    fun setList(list: List<PostImage>) {
        dataList.clear()
        dataList.addAll(list)
        notifyDataSetChanged()
    }

    private fun removePlaceholdersImage() {
        dataList.removeAll { postImage -> postImage.url == MessageAttachment.EMPTY_URL }
    }

    inner class ChatImageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val placeholderBackground = itemView.findViewById<FrameLayout>(R.id.placeholder_background)
        private val ivPlaceholder = itemView.findViewById<ImageView>(R.id.iv_placeholder)
        private val ivPostImage = itemView.findViewById<ImageView>(R.id.iv_post_image)
        private val vgPostImage = itemView.findViewById<RoundedFrameLayout>(R.id.vg_post_image)
        private val ivGiphyWatermark = itemView.findViewById<ImageView>(R.id.iv_giphy_watermark)

        internal fun bind(item: PostImage) {

            isFileByLocalPathExists(item.url) {
                placeholderBackground.visible()
                ivPlaceholder.visible()
            }

            if (item.url == MessageAttachment.EMPTY_URL) {
                placeholderBackground.visible()
                itemView.isEnabled = false
            }

            itemView.layoutParams.width = calculateItemsWidth(containerWidth)
            itemView.requestLayout()

            imageCornerDecorator.bindImageCorners(
                rounded = vgPostImage,
                position = bindingAdapterPosition,
                imageCount = dataList.size,
                isMe = isMyImages,
            )

            val isBlurImage = message?.isShowImageBlurChatRequest ?: false
            if (isBlurImage) {
                blurHelper?.blurByUrl(item.url) { bitmap ->
                    bitmap?.let { ivPostImage.loadImage(it) }
                }
            } else {
                Glide.with(itemView)
                    .load(item.url)
                    .thumbnail(GLIDE_THUMBNAIL_SIZE_MULTIPLIER)
                    .addListener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean = false

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            val imageAspect =
                                resource.intrinsicWidth.toDouble() / resource.intrinsicHeight
                            val builder = Zoomy.Builder(act)
                                .target(ivPostImage)
                                .interpolator(OvershootInterpolator())
                                .tapListener {
                                    callback.onImageClicked(
                                        message,
                                        dataList,
                                        bindingAdapterPosition,
                                    )
                                }
                                .longPressListener {
                                    longClickListener?.onLongClick(itemView)
                                }
                            val duplicate = ImageView(itemView.context)
                            Glide.with(itemView)
                                .load(item.url)
                                .into(duplicate)
                            builder.setTargetDuplicate(duplicate).aspectRatio(imageAspect)
                            builder.register()
                            return false
                        }

                    })
                    .into(ivPostImage)
            }
            if (item.url == MessageAttachment.EMPTY_URL) itemView.isEnabled = false
            if (item.isShowGiphyWatermark) {
                ivGiphyWatermark.visible()
            } else {
                ivGiphyWatermark.gone()
            }
        }

        private fun calculateItemsWidth(containerWidth: Int): Int {
            return when (dataList.size) {
                1 -> ViewGroup.LayoutParams.MATCH_PARENT
                else -> {
                    val rv = recyclerView ?: return containerWidth / MAX_COLUMNS
                    val itemMargins = itemView.marginStart + itemView.marginEnd
                    val parentPaddings = rv.paddingStart + rv.paddingEnd
                    return containerWidth / MAX_COLUMNS - (parentPaddings / 2) - itemMargins
                }
            }
        }
    }

    private fun ImageView.loadImage(path: Any) {
        Glide.with(this)
            .load(path)
            .thumbnail(GLIDE_THUMBNAIL_SIZE_MULTIPLIER)
            .into(this)
    }

    companion object {
        const val MAX_ROWS = 24

        private const val MAX_COLUMNS = 2
        private const val IMAGE_CORNER_SIZE_DP = 10
    }
}
