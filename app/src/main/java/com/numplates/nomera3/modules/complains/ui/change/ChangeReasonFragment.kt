package com.numplates.nomera3.modules.complains.ui.change

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.State
import com.meera.core.extensions.click
import com.meera.core.extensions.dpToPx
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentComplainChangeReasonBinding
import com.numplates.nomera3.modules.complains.ui.ComplainsNavigator
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAIN_TYPE
import com.numplates.nomera3.modules.complains.ui.KEY_EXTRA_USER_COMPLAIN
import com.numplates.nomera3.modules.complains.ui.model.UserComplainUiModel
import com.numplates.nomera3.modules.user.ui.OnBottomSheetFragmentsListener
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChangeReasonFragment : BaseFragmentNew<FragmentComplainChangeReasonBinding>() {

    private var interactionCallback: OnBottomSheetFragmentsListener? = null
    private val viewModel by viewModels<ChangeReasonViewModel> { App.component.getViewModelFactory() }
    private val complainsNavigator by lazy(LazyThreadSafetyMode.NONE) { ComplainsNavigator(requireActivity()) }
    private val itemsAdapter by lazy(LazyThreadSafetyMode.NONE) { ChangeReasonAdapter(::changeReasonItemSelected) }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentComplainChangeReasonBinding
        get() = FragmentComplainChangeReasonBinding::inflate

    override fun onAttach(context: Context) {
        super.onAttach(context)
        interactionCallback = parentFragment as? OnBottomSheetFragmentsListener
            ?: error("The parent fragment must implement OnBottomSheetFragmentsListener")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListeners()
        initRecycler()
        initScreenObservers()
        if (savedInstanceState == null) {
            val complain = requireArguments().getSerializable(KEY_EXTRA_USER_COMPLAIN) as UserComplainUiModel
            viewModel.initializeMenuItems(
                complain = complain,
                complaintType = arguments?.getInt(KEY_COMPLAIN_TYPE)
            )
        }
    }

    fun initScreenObservers() = with(viewModel) {
        screenLiveData.observe(viewLifecycleOwner) { items -> itemsAdapter.submitList(items) }
        screenEvents.onEach { event ->
            when (event) {
                is ChangeReasonEvent.FinishFlow -> interactionCallback?.onCloseMenu()
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun changeReasonItemSelected(reason: ChangeReasonUiModel) {
        viewModel.selectComplain(reason)
        complainsNavigator.sendChangeReasonResult(reason.complainUiModel)
    }

    private fun initRecycler() {
        binding?.rvComplainReasons?.apply {
            this.setHasFixedSize(false)
            this.layoutManager = LinearLayoutManager(requireContext())
            this.adapter = itemsAdapter
            this.addItemDecoration(object : ItemDecoration() {

                private val leftMargin = dpToPx(48)
                private val thickness = dpToPx(1)
                private val paint = Paint().apply {
                    color = ContextCompat.getColor(requireContext(), R.color.gray_separator)
                }

                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
                    outRect.bottom = if (view.isDecoratedItem(parent)) 0 else thickness
                }

                override fun onDraw(c: Canvas, parent: RecyclerView, state: State) {
                    parent.children.forEach { view ->
                        if (view.isDecoratedItem(parent)) {
                            val left = view.left + leftMargin
                            val top = view.bottom
                            val right = view.right
                            val bottom = view.bottom + thickness
                            c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
                        }
                    }
                }

                private fun View.isDecoratedItem(parent: RecyclerView): Boolean {
                    val position = parent.getChildAdapterPosition(this)
                    return position != RecyclerView.NO_POSITION && position != itemsAdapter.itemCount - 1
                }
            })
        }
    }

    private fun initClickListeners() {
        binding?.itemMenuCancel?.click {
            interactionCallback?.onCloseMenu()
        }
    }
}
