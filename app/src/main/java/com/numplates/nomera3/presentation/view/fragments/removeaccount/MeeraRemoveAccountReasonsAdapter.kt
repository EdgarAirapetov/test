package com.numplates.nomera3.presentation.view.fragments.removeaccount

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.cell.UiKitCell
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemRemoveAccountButtonContinueBinding
import com.numplates.nomera3.databinding.ItemRemoveAccountReasonMeeraBinding

private const val ITEM_COUNT = 2

class MeeraRemoveAccountReasonsAdapter(
    val recyclerView: RecyclerView,
    val actionListener: (action: ReasonFragmentAction) -> Unit
) : RecyclerView.Adapter<MeeraRemoveAccountReasonsAdapter.ReasonsHolder>() {

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
            createReasonViewHolder(parent)
        } else {
            createButtonViewHolder(parent)
        }

    override fun onBindViewHolder(holder: ReasonsHolder, position: Int) = holder.bind()

    override fun getItemCount(): Int = ITEM_COUNT

    override fun getItemViewType(position: Int): Int = position

    abstract class ReasonsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind()
    }

    inner class ReasonViewHolder(val binding: ItemRemoveAccountReasonMeeraBinding) :
        ReasonsHolder(binding.root) {
        private var selectedCell: UiKitCell? = null
        override fun bind() {

            binding.root.doOnPreDraw {
                reasonsHeight = binding.root.height
                notifyItemChanged(BUTTON_POSITION)
            }
            initReasonClick()
        }

        private fun initReasonClick() {
            reasonClick(binding.leavingWhile, ReasonFragmentAction.LeavingWhileAction)
            reasonClick(binding.difficultFigure, ReasonFragmentAction.DifficultFigureAction)
            reasonClick(binding.dontFeelSafeAction, ReasonFragmentAction.DontFeelSafeAction)
            reasonClick(binding.uglyAppAction, ReasonFragmentAction.UglyAppAction)
            reasonClick(binding.dontLikeModerationAction, ReasonFragmentAction.DontLikeModerationAction)
            reasonClick(binding.unpleasantCommunicationAction, ReasonFragmentAction.UnpleasantCommunicationAction)
            reasonClick(binding.uninterestingContentAction, ReasonFragmentAction.UninterestingContentAction)
            reasonClick(binding.spendTooMuchTimeAction, ReasonFragmentAction.SpendTooMuchTimeAction)
            reasonClick(binding.technicalSupport, ReasonFragmentAction.AnotherReasonAction)
        }

        private fun reasonClick(reason: UiKitCell, action: ReasonFragmentAction) {
            reason.setRightElementContainerClickable(false)
            reason.setThrottledClickListener {
                reason.toggleRightCheckbox()
                selectedCell?.toggleRightCheckbox()
                selectedCell = reason
                actionListener(action)
                enableButton = true
            }
        }
    }

    inner class ButtonViewHolder(val binding: ItemRemoveAccountButtonContinueBinding) :
        ReasonsHolder(binding.root) {

        override fun bind() {
            binding.btnContinue.isEnabled = false
            val topPadding =
                (recyclerView.height - buttonHeight - reasonsHeight).coerceAtLeast(0)
            binding.root.setPadding(0, topPadding, 0, 0)
            if (enableButton) {
                enableButtonState()
            }
        }

        private fun enableButtonState() {
            binding.btnContinue.isEnabled = true
            binding.btnContinue.setThrottledClickListener { actionListener(ReasonFragmentAction.ContinueButtonAction) }
        }
    }

    private fun createReasonViewHolder(parent: ViewGroup) = ReasonViewHolder(
        ItemRemoveAccountReasonMeeraBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    private fun createButtonViewHolder(parent: ViewGroup) = ButtonViewHolder(
        ItemRemoveAccountButtonContinueBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    companion object {
        private const val REASONS_POSITION = 0
        private const val BUTTON_POSITION = 1
    }
}

