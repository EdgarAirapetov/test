package com.numplates.nomera3.modules.notifications.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.databinding.ItemNotificationGroupHeaderBinding
import com.numplates.nomera3.modules.notifications.ui.itemdecorator.NotificationDividerItemDecorator
import com.numplates.nomera3.presentation.view.utils.NotSwipeDeletable

class ClearNotificationGroupAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var actionLabel: String? = null
    private var onActionClicked: (() -> Unit)? = null
    private var isVisible = false

    override fun getItemCount(): Int = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return LayoutInflater.from(parent.context)
            .let { inflater -> ItemNotificationGroupHeaderBinding.inflate(inflater, parent, false) }
            .let { viewBinding -> ClearNotificationGroupViewHolder(viewBinding) }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? ClearNotificationGroupViewHolder)?.bind(
            actionLabel = actionLabel,
            onActionClicked = onActionClicked,
            isVisible = isVisible
        )
    }

    fun onNewAction(actionLabel: String, onActionClicked: () -> Unit) {
        this.actionLabel = actionLabel
        this.onActionClicked = onActionClicked
        notifyItemChanged(0)
    }

    fun setVisibility(isVisible: Boolean) {
        this.isVisible = isVisible
        notifyItemChanged(0)
    }

    class ClearNotificationGroupViewHolder(
        private val viewBinding: ItemNotificationGroupHeaderBinding
    ): RecyclerView.ViewHolder(viewBinding.root), NotSwipeDeletable, NotificationDividerItemDecorator.NotDecoratable {

        fun bind(actionLabel: String?, onActionClicked: (() -> Unit)?, isVisible: Boolean) {
            viewBinding.tvAction.apply {
                this.isVisible = isVisible
                text = actionLabel
                setOnClickListener { onActionClicked?.invoke() }
            }
        }
    }
}
