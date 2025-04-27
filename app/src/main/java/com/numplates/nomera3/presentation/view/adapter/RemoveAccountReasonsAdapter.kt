package com.numplates.nomera3.presentation.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.click
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemRemoveAccountButtonBinding
import com.numplates.nomera3.databinding.ItemRemoveAccountReasonBinding

class RemoveAccountReasonsAdapter(
    val recyclerView: RecyclerView,
    val onReasonClicked: (reasonId: Int) -> Unit,
    val onButtonClicked: () -> Unit
) :
    RecyclerView.Adapter<RemoveAccountReasonsAdapter.ReasonsHolder>() {

    private val buttonHeight =
        recyclerView.context.resources.getDimensionPixelOffset(R.dimen.remove_account_button_height)
    private var reasonsHeight: Int = 0
    private var enableButton: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                notifyItemChanged(BUTTON_POSITION)
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReasonsHolder =
        if (viewType == REASONS_POSITION) {
            ReasonViewHolder(
                ItemRemoveAccountReasonBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            ButtonViewHolder(
                ItemRemoveAccountButtonBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

    override fun onBindViewHolder(holder: ReasonsHolder, position: Int) = holder.bind()

    override fun getItemCount(): Int = 2

    override fun getItemViewType(position: Int): Int = position

    abstract class ReasonsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind()
    }

    inner class ReasonViewHolder(val binding: ItemRemoveAccountReasonBinding) :
        ReasonsHolder(binding.root) {

        override fun bind() {
            binding.root.doOnPreDraw {
                reasonsHeight = binding.root.height
                notifyItemChanged(BUTTON_POSITION)
            }
            binding.radioGroupReasons.setOnCheckedChangeListener { _, checkedId ->
                enableButton = true
                when (checkedId) {
                    R.id.reason1 -> onReasonClicked(1)
                    R.id.reason2 -> onReasonClicked(2)
                    R.id.reason3 -> onReasonClicked(3)
                    R.id.reason4 -> onReasonClicked(4)
                    R.id.reason5 -> onReasonClicked(5)
                    R.id.reason6 -> onReasonClicked(6)
                    R.id.reason7 -> onReasonClicked(7)
                    R.id.reason8 -> onReasonClicked(8)
                    R.id.reason9 -> onReasonClicked(9)
                }
            }
        }
    }

    inner class ButtonViewHolder(val binding: ItemRemoveAccountButtonBinding) :
        ReasonsHolder(binding.root) {

        override fun bind() {
            val topPadding =
                (recyclerView.height - buttonHeight - reasonsHeight).coerceAtLeast(0)
            binding.root.setPadding(0, topPadding, 0, 0)
            if (enableButton) {
                binding.button.isEnabled = true
                binding.button.alpha = 1f
                binding.button.click { onButtonClicked() }
            }
        }
    }

    companion object {
        private const val REASONS_POSITION = 0
        private const val BUTTON_POSITION = 1
    }

}

