package com.numplates.nomera3.modules.complains.ui.reason

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.State
import com.meera.core.extensions.click
import com.meera.core.extensions.dpToPx
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.NOOMEERA_USER_AGREEMENT_URL
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentComplainReasonBinding
import com.numplates.nomera3.modules.complains.ui.ComplaintFlowInteraction
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_MOMENT_ID
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_ROOM_ID
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_SEND_RESULT
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_USER_ID
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_WHERE_VALUE
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAIN_TYPE
import com.numplates.nomera3.modules.complains.ui.KEY_EXTRA_USER_COMPLAIN
import com.numplates.nomera3.modules.complains.ui.details.UserComplainDetailsFragment
import com.numplates.nomera3.modules.user.ui.OnBottomSheetFragmentsListener
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew

class UserComplainReasonFragment : BaseFragmentNew<FragmentComplainReasonBinding>() {

    private val viewModel by viewModels<UserComplainReasonViewModel> { App.component.getViewModelFactory() }
    private var interactionCallback: OnBottomSheetFragmentsListener? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentComplainReasonBinding
        get() = FragmentComplainReasonBinding::inflate

    override fun onAttach(context: Context) {
        super.onAttach(context)
        interactionCallback = parentFragment as? OnBottomSheetFragmentsListener
            ?: error("The parent fragment must implement OnBottomSheetFragmentsListener")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initClickListeners()
        initScreenObservers()
        if (savedInstanceState == null) {
            viewModel.initializeMenuItems(complaintType = arguments?.getInt(KEY_COMPLAIN_TYPE))
            viewModel.logMenuOpened()
        }
    }

    fun initScreenObservers() {
        viewModel.screenLiveData.observe(viewLifecycleOwner) { items ->
            (binding?.rvComplainReasons?.adapter as UserComplainReasonAdapter).updateItems(items)
        }
    }

    private fun getComplaintFlowInteraction(): ComplaintFlowInteraction? = act

    private fun initRecycler() {
        val adapter = UserComplainReasonAdapter(
            itemClickListener = { complaint ->
                getComplaintFlowInteraction()?.setIsFinishing(isFinishing = false)
                add(
                    UserComplainDetailsFragment(),
                    Act.LIGHT_STATUSBAR,
                    Arg(KEY_COMPLAIN_TYPE, arguments?.getInt(KEY_COMPLAIN_TYPE)),
                    Arg(KEY_EXTRA_USER_COMPLAIN, complaint),
                    Arg(KEY_COMPLAINT_USER_ID, arguments?.getLong(KEY_COMPLAINT_USER_ID)),
                    Arg(KEY_COMPLAINT_WHERE_VALUE, arguments?.getSerializable(KEY_COMPLAINT_WHERE_VALUE)),
                    Arg(KEY_COMPLAINT_MOMENT_ID, arguments?.getLong(KEY_COMPLAINT_MOMENT_ID)),
                    Arg(KEY_COMPLAINT_ROOM_ID, arguments?.getLong(KEY_COMPLAINT_ROOM_ID)),
                    Arg(KEY_COMPLAINT_SEND_RESULT, arguments?.getBoolean(KEY_COMPLAINT_SEND_RESULT))
                )
            }
        )
        binding?.rvComplainReasons?.apply {
            this.setHasFixedSize(false)
            this.layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
            this.addItemDecoration(object : ItemDecoration() {

                private val leftMargin = dpToPx(16)
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
                    return position != RecyclerView.NO_POSITION && position != adapter.itemCount - 1 && position != 0
                }
            })
        }
    }

    private fun initClickListeners() {
        binding?.itemMenuCancel?.click {
            interactionCallback?.onCloseMenu()
        }

        binding?.itemReadPolicy?.click {
            showUserAgreement()
        }
    }

    private fun showUserAgreement() {
        val uriUrl = Uri.parse(NOOMEERA_USER_AGREEMENT_URL)
        val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
        startActivity(launchBrowser)
        viewModel.logOpenRules()
    }
}
