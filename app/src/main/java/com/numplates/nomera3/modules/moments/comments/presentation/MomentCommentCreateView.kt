package com.numplates.nomera3.modules.moments.comments.presentation

import android.content.Context
import android.graphics.Rect
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.animateHeight
import com.meera.core.extensions.clearText
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.invisible
import com.meera.core.extensions.setListener
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAppearAnimate
import com.meera.core.utils.graphics.SpanningLinearLayoutManager
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MomentCommentsBottomSheetCreateCommentBlockBinding
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.newroads.ui.adapter.QuickAnswerAdapter
import com.numplates.nomera3.modules.tags.ui.base.SuggestedTagListMenu

private const val INPUT_TOUCH_EXTRA_RIGHT_SPACE = 16
private const val OPEN_RELY_HEIGHT_DP = 30
private const val OPEN_RELY_ANIMATION_DURATION = 100L
private const val CLOSE_RELY_ANIMATION_DURATION = 100L

class MomentCommentCreateView {

    private var suggestionsMenu: SuggestedTagListMenu? = null
    private var createCommentBinding: MomentCommentsBottomSheetCreateCommentBlockBinding? = null
    private var fragment: Fragment? = null

    private var selectedComment: CommentEntityResponse? = null

    private var isScrollButtonShown = false

    fun init(
        momentItem: MomentItemUiModel,
        fragment: MomentCommentsBottomSheetFragment,
        binding: MomentCommentsBottomSheetCreateCommentBlockBinding,
        bottomSheetDialog: BottomSheetDialog,
        callback: Callback
    ) {
        this.fragment = fragment
        this.createCommentBinding = binding

        val bottomContainer =
            bottomSheetDialog.findViewById<ViewGroup>(R.id.fl_bottom_container) ?: return
        val activity = (fragment.activity as Act)

        attachCreateCommentBlockToMainLayout(
            bottomContainer = bottomContainer,
            createCommentBottomBlock = binding.root
        )

        initTextChangeListener(
            createCommentBinding = binding
        )

        initQuickAnswerMenu(
            createCommentBinding = binding,
            context = fragment.requireContext()
        )

        initSendBtn(
            createCommentBinding = binding,
            activity = activity,
            callback = callback
        )

        extendInputTextTouchArea(
            createCommentBinding = binding
        )

        listenInputTextSelected(
            createCommentBinding = binding,
            callback = callback
        )

        attachOtherListeners(
            callback = callback
        )

        setCommentAvailability(
            isCommentable = momentItem.isMomentCommentable()
        )
    }

    fun handleCommentReply(comment: CommentEntityResponse) {
        selectedComment = comment

        addInfoInInputMessageWidget()
    }

    fun handleShowScrollButtonDown() {
        val downButton = createCommentBinding?.ivScrollDownButton ?: return

        if (isScrollButtonShown) {
            downButton.visible()
            return
        }

        isScrollButtonShown = true

        downButton.isEnabled = true
        downButton
            .animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(150)
            .start()

        downButton.visible()
    }

    fun handleHideScrollButtonDown() {
        val downButton = createCommentBinding?.ivScrollDownButton ?: return

        if (!isScrollButtonShown) {
            downButton.gone()
            return
        }

        isScrollButtonShown = false
        downButton.isEnabled = false
        downButton
            .animate()
            .scaleX(0f)
            .scaleY(0f)
            .setDuration(150)
            .setListener(
                onAnimationEnd = {
                    downButton.gone()
                }
            )
            ?.start()
    }

    fun handleEnableWriteComment() {
        createCommentBinding?.ivSendComment?.isEnabled = true
        createCommentBinding?.etWriteComment?.isEnabled = true
    }

    fun handleCommentNotAvailable() {
        setCommentAvailability(isCommentable = false)
    }

    fun openRelyExtraContainer() {
        val container = createCommentBinding?.vgRellayExtraInfoContainer ?: return

        container.visible()
        container.measure(ViewGroup.LayoutParams.MATCH_PARENT, OPEN_RELY_HEIGHT_DP.dp)
        container.measuredHeight.let { measuredHeight ->
            container.animateHeight(measuredHeight, OPEN_RELY_ANIMATION_DURATION)
        }
    }

    fun handleReplyComment() = addInfoInInputMessageWidget()

    fun handleDeleteComment() {
        val binding = createCommentBinding ?: return
        closeSendMessageExtraInfo(binding)
    }

    fun setSuggestionMenuPeekHeight(peekHeight: Int) {
        suggestionsMenu?.setExtraPeekHeight(
            newExtraPeekHeight = peekHeight,
            isAnimate = true
        )
    }

    private fun attachOtherListeners(callback: Callback) {
        createCommentBinding?.ivScrollDownButton?.setThrottledClickListener {
            callback.onScrollDownButtonPress()
            createCommentBinding?.ivScrollDownButton?.gone()
        }
    }

    private fun setCommentAvailability(isCommentable: Boolean) {
        if (isCommentable) {
            createCommentBinding?.vgCreateBlockMainContainer?.visible()
            createCommentBinding?.vgBlockedHolder?.gone()
        } else {
            createCommentBinding?.tvBlockMessage?.text =
                fragment?.getString(R.string.comments_disabled)
            createCommentBinding?.vgCreateBlockMainContainer?.invisible()
            createCommentBinding?.vgBlockedHolder?.visible()
        }
    }

    private fun addInfoInInputMessageWidget() {
        openRelyExtraContainer()
        if (selectedComment != null && selectedComment?.user?.name != null) {
            createCommentBinding?.tvCommentOwner?.text = selectedComment?.user?.name
        }

        openKeyboard()
    }

    private fun openKeyboard() {
        val context = fragment?.requireContext()
        val editText = createCommentBinding?.etWriteComment ?: return

        context?.showKeyboard(editText)
    }

    private fun attachCreateCommentBlockToMainLayout(
        bottomContainer: ViewGroup,
        createCommentBottomBlock: ViewGroup
    ) {
        bottomContainer.addView(createCommentBottomBlock)
    }

    private fun listenInputTextSelected(
        createCommentBinding: MomentCommentsBottomSheetCreateCommentBlockBinding,
        callback: Callback
    ) {
        createCommentBinding.etWriteComment.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                callback.onInputTextSelected()
            }
        }
        createCommentBinding.etWriteComment.setOnClickListener {
            callback.onInputTextSelected()
        }
    }

    private fun initTextChangeListener(createCommentBinding: MomentCommentsBottomSheetCreateCommentBlockBinding) {
        createCommentBinding.etWriteComment.doAfterTextChanged { text ->
            if (text.toString().trim().isEmpty())
                createCommentBinding.ivSendComment.gone()
            else if (createCommentBinding.ivSendComment.visibility != View.VISIBLE) {
                createCommentBinding.ivSendComment.visibleAppearAnimate()
            }
        }
    }

    private fun initQuickAnswerMenu(
        createCommentBinding: MomentCommentsBottomSheetCreateCommentBlockBinding,
        context: Context
    ) {
        val adapter = QuickAnswerAdapter()
        createCommentBinding.rvQuickAnswer.layoutManager = SpanningLinearLayoutManager(
            context,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        createCommentBinding.rvQuickAnswer.adapter = adapter
        adapter.addItems()
        adapter.clickListener = { emojiString, emojiName ->
            val cursorPosition = createCommentBinding.etWriteComment.selectionEnd
            val newText = cursorPosition.let {
                createCommentBinding.etWriteComment.text?.insert(it, emojiString)
            } ?: kotlin.run { "${createCommentBinding.etWriteComment.text}$emojiString" }
            createCommentBinding.etWriteComment.setText(newText)
            cursorPosition.let {
                createCommentBinding.etWriteComment.setSelection(it + emojiString.length)
            }

            openKeyboard()
        }
    }

    private fun extendInputTextTouchArea(createCommentBinding: MomentCommentsBottomSheetCreateCommentBlockBinding) {
        val textFieldToExtend = createCommentBinding.etWriteComment
        val parent = textFieldToExtend.parent as View
        val extraSpace = INPUT_TOUCH_EXTRA_RIGHT_SPACE.dp

        val touchableArea = Rect()
        textFieldToExtend.getHitRect(touchableArea)
        touchableArea.top -= extraSpace
        touchableArea.bottom += extraSpace
        touchableArea.left -= extraSpace
        touchableArea.right += INPUT_TOUCH_EXTRA_RIGHT_SPACE.dp
        parent.touchDelegate = TouchDelegate(touchableArea, textFieldToExtend)
    }

    private fun initSendBtn(
        createCommentBinding: MomentCommentsBottomSheetCreateCommentBlockBinding,
        activity: Act,
        callback: Callback
    ) {

        createCommentBinding.ivSendComment.setThrottledClickListener {
            var message = createCommentBinding.etWriteComment.text.toString()
            message = message.trim()

            if (message.isEmpty()) return@setThrottledClickListener

            createCommentBinding.ivSendComment.isEnabled = false
            createCommentBinding.etWriteComment.isEnabled = false

            activity.hideKeyboard(activity.getRootView()!!)

            createCommentBinding.etWriteComment.clearText()

            callback.onMessageCreate(
                message = message,
                parentCommentId = selectedComment.getId()
            )

            closeSendMessageExtraInfo(createCommentBinding)
        }

        createCommentBinding.ivCancelBtn.click {
            closeSendMessageExtraInfo(createCommentBinding)
        }
    }

    private fun closeSendMessageExtraInfo(createCommentBinding: MomentCommentsBottomSheetCreateCommentBlockBinding) {
        createCommentBinding.tvCommentOwner.text = ""
        selectedComment = null
        closeRelyExtraContainer(createCommentBinding)
    }

    private fun closeRelyExtraContainer(createCommentBinding: MomentCommentsBottomSheetCreateCommentBlockBinding) {
        createCommentBinding.vgRellayExtraInfoContainer.visible()
        createCommentBinding.vgRellayExtraInfoContainer.animateHeight(0, CLOSE_RELY_ANIMATION_DURATION) {
            createCommentBinding.vgRellayExtraInfoContainer.gone()
        }
    }

    private fun CommentEntityResponse?.getId(): Long {
        return this?.id ?: 0
    }

    interface Callback {
        fun onMessageCreate(
            message: String,
            parentCommentId: Long
        )

        fun onInputTextSelected()

        fun onScrollDownButtonPress()
    }
}
