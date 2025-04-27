package com.numplates.nomera3.modules.complains.ui.change

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.click
import com.meera.core.extensions.inflate
import com.numplates.nomera3.R.layout
import com.numplates.nomera3.databinding.ItemUserComplainChangeBinding
import com.numplates.nomera3.modules.complains.ui.change.ChangeReasonAdapter.UserComplainViewHolder

class ChangeReasonAdapter constructor(
    private val selectItemListener: (ChangeReasonUiModel) -> Unit = { _ -> }
) : ListAdapter<ChangeReasonUiModel, UserComplainViewHolder>(ChangeReasonDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserComplainViewHolder {
        return UserComplainViewHolder(parent.inflate(layout.item_user_complain_change), ::updateSelected)
    }

    override fun onBindViewHolder(holder: UserComplainViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private fun updateSelected(bindingAdapterPosition: Int) {
        selectItemListener.invoke(getItem(bindingAdapterPosition))
    }

    class UserComplainViewHolder(itemView: View, listener: (Int) -> Unit) : RecyclerView.ViewHolder(itemView) {

        private val binding = ItemUserComplainChangeBinding.bind(itemView)

        init {
            itemView.click { listener.invoke(bindingAdapterPosition) }
        }

        fun bind(item: ChangeReasonUiModel) = with(binding) {
            rbItemTitle.setText(item.complainUiModel.titleRes)
            rbItemTitle.isChecked = item.isChecked
        }
    }

    class ChangeReasonDiffUtilCallback : DiffUtil.ItemCallback<ChangeReasonUiModel>() {
        override fun areItemsTheSame(
            oldItem: ChangeReasonUiModel,
            newItem: ChangeReasonUiModel
        ): Boolean {
            return oldItem.complainUiModel.reasonId == newItem.complainUiModel.reasonId
        }

        override fun areContentsTheSame(
            oldItem: ChangeReasonUiModel,
            newItem: ChangeReasonUiModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}
