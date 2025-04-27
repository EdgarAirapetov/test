package com.numplates.nomera3.presentation.view.fragments.aboutapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.UiKitCell
import com.numplates.nomera3.R

interface AboutFragmentHolderBinder {
    fun bind(item: AboutFragmentAdapterItem)
}

class AboutAppAdapter(
    val items: List<AboutFragmentAdapterItem>,
    val actionListener: (action: AboutFragmentAction) -> Unit
) : Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_CONTENT -> AboutFragmentContentVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.meera_about_fragment_content,
                    parent,
                    false
                )
            )

            else -> AboutFragmentButtonsVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.meera_about_fragment_button,
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemViewType(position: Int) = items[position].adapterType

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as AboutFragmentHolderBinder
        holder.bind(items[position])
    }

    inner class AboutFragmentContentVH(itemView: View) : RecyclerView.ViewHolder(itemView), AboutFragmentHolderBinder {

        override fun bind(item: AboutFragmentAdapterItem) {
            item as AboutFragmentAdapterItem.Content
            with(itemView) {
                findViewById<ImageView>(R.id.about_fragment_image).setImageResource(item.imageId)
                findViewById<TextView>(R.id.about_fragment_desc).text = item.text
            }
        }
    }

    inner class AboutFragmentButtonsVH(itemView: View) : RecyclerView.ViewHolder(itemView), AboutFragmentHolderBinder {

        override fun bind(item: AboutFragmentAdapterItem) {
            val button = itemView.findViewById<UiKitCell>(R.id.about_fragment_button)

            when (item.adapterType) {
                TYPE_AGREEMENT -> button.cellPosition = CellPosition.TOP
                TYPE_SUPPORT -> button.cellPosition = CellPosition.BOTTOM
                TYPE_WEB_SITE,
                TYPE_COLLABA -> button.cellPosition = CellPosition.MIDDLE
            }
            button.cellArrowRight = true
            button.setTitleValue(item.text)
            button.setThrottledClickListener { actionListener(getButtonAction(item.adapterType)) }
        }

        private fun getButtonAction(adapterType: Int) = when (adapterType) {
            TYPE_SUPPORT -> AboutFragmentAction.SupportAction
            TYPE_AGREEMENT -> AboutFragmentAction.AgreementAction
            TYPE_WEB_SITE -> AboutFragmentAction.WebSiteAction
            TYPE_COLLABA -> AboutFragmentAction.CollaborationAction
            else -> {
                error("Unsupported button action AboutAppAdapter")
            }
        }
    }
}
