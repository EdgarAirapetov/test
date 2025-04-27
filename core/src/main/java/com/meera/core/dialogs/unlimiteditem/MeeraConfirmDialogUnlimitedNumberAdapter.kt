package com.meera.core.dialogs.unlimiteditem

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.R
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.meera.core.databinding.MeeraConfirmDialogUnlimitedItemBinding
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.CellWidthType

private const val ONE_LIST_USERS = 1

class MeeraConfirmDialogUnlimitedNumberAdapter(
    private val listener: (action: MeeraConfirmDialogUnlimitedNumberItemsAction) -> Unit,
    private val closeListener: () -> Unit,
    private val itemWithMargins: Boolean = true
) :
    ListAdapter<MeeraConfirmDialogUnlimitedNumberItemsData, MeeraConfirmDialogUnlimitedNumberAdapter.HolderItem>(
        diffCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderItem {
        return HolderItem(parent.toBinding())
    }

    override fun onBindViewHolder(holder: HolderItem, position: Int) {
        holder.bind(
            item = currentList[position],
            first = position == 0,
            last = currentList.lastIndex == position,
            oneElement = currentList.size == ONE_LIST_USERS
        )
    }

    inner class HolderItem(
        val binding: MeeraConfirmDialogUnlimitedItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MeeraConfirmDialogUnlimitedNumberItemsData, first: Boolean, last: Boolean, oneElement: Boolean) {
            binding.vDialogConfirmItem.apply {

                setTitleValue(binding.root.resources.getString(item.name))
                setLeftIcon(item.icon)
                item.contentColor?.let {
                    cellLeftIconAndTitleColor = it
                }
                when {
                    oneElement -> cellPosition = CellPosition.ALONE
                    first -> cellPosition = CellPosition.TOP
                    last -> cellPosition = CellPosition.BOTTOM
                    else -> cellPosition = CellPosition.MIDDLE
                }

                if (itemWithMargins) {
                    cellWidthType = CellWidthType.WITH_MARGINS
                    cellBackgroundColor = R.color.uiKitColorBackgroundSecondary
                } else {
                    cellWidthType = CellWidthType.WITHOUT_MARGINS
                    cellBackgroundColor = R.color.uiKitColorBackgroundPrimary
                }
                setThrottledClickListener {
                    listener.invoke(item.action)
                    closeListener.invoke()
                }
            }
        }
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<MeeraConfirmDialogUnlimitedNumberItemsData>() {
            override fun areItemsTheSame(
                oldItem: MeeraConfirmDialogUnlimitedNumberItemsData,
                newItem: MeeraConfirmDialogUnlimitedNumberItemsData
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: MeeraConfirmDialogUnlimitedNumberItemsData,
                newItem: MeeraConfirmDialogUnlimitedNumberItemsData
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
