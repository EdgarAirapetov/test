package com.numplates.nomera3.modules.communities.ui.fragment.holder

import android.annotation.SuppressLint
import android.text.InputFilter
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setPaddingBottom
import com.meera.core.extensions.setPaddingTop
import com.meera.core.extensions.setTint
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraEditGroupTextItemBinding
import com.numplates.nomera3.modules.communities.ui.fragment.MeeraCommunityEditAction
import com.numplates.nomera3.modules.communities.ui.fragment.adapter.CommunityEditItemType
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

private const val MAX_NAME_LENGTH = 45
private const val MAX_DESCRIPTION_LENGTH = 500
private const val SINGLE_LINE_EDIT_TEXT = 1
private const val TIMEOUT_EDIT_TEXT_SEND = 300L
private const val NAME_VERTICAL_PADDING = 12
private const val DESCRIPTION_LINE_SIZE = 10

class MeeraCommunityEditTextHolder(
    val binding: MeeraEditGroupTextItemBinding,
    val inputType: CommunityEditItemType,
    val listener: (action: MeeraCommunityEditAction) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private var delayedNameValidation: Disposable? = null
    private val communityNameChangeListener = InputFilter { source, _, _, dest, _, _ ->
        delayedNameValidation?.dispose()
        delayedNameValidation = Observable.just(dest)
            .debounce(TIMEOUT_EDIT_TEXT_SEND, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { communityName ->
                if(communityName.isNotEmpty()) {
                    actionListenerEditText(CommunityEditItemType.NAME_TEXT_ITEM, communityName.toString())
                } else {
                    binding.vErrorMessageContainer.gone()
                }
            }
        source
    }
    private val communityDescriptionChangeListener = InputFilter { source, _, _, dest, _, _ ->
        delayedNameValidation?.dispose()
        delayedNameValidation = Observable.just(dest)
            .debounce(TIMEOUT_EDIT_TEXT_SEND, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { description ->
                if (description.isNotEmpty()){
                    actionListenerEditText(CommunityEditItemType.DESCRIPTION_TEXT_ITEM, description.toString())
                }
            }
        source
    }

    @SuppressLint("ResourceAsColor")
    fun bind(name: String? = null, description: String? = null) {
        binding.ivErrorIcon.setTint(R.color.uiKitColorAccentWrong)
        when (inputType) {
            CommunityEditItemType.NAME_TEXT_ITEM -> {
                binding.tvHeaderGroupEditText.text = binding.root.resources.getString(R.string.group_name_label_text)
                binding.etEditGroupEditText.minLines = SINGLE_LINE_EDIT_TEXT
                binding.etEditGroupEditText.maxLines = SINGLE_LINE_EDIT_TEXT
                binding.etEditGroupEditText.setPaddingTop(NAME_VERTICAL_PADDING.dp)
                binding.etEditGroupEditText.setPaddingBottom(NAME_VERTICAL_PADDING.dp)
                binding.etEditGroupEditText.filters =
                    arrayOf(communityNameChangeListener, InputFilter.LengthFilter(MAX_NAME_LENGTH))
                initScrollListener()
                name?.let {
                    binding.etEditGroupEditText.setText(name)
                    actionListenerEditText(CommunityEditItemType.NAME_TEXT_ITEM, it)
                }
            }

            CommunityEditItemType.DESCRIPTION_TEXT_ITEM -> {
                binding.tvHeaderGroupEditText.text = binding.root.resources.getString(R.string.description_txt)
                binding.etEditGroupEditText.setHint(binding.root.resources.getString(R.string.set_description))
                binding.etEditGroupEditText.maxLines = DESCRIPTION_LINE_SIZE
                binding.etEditGroupEditText.minLines = DESCRIPTION_LINE_SIZE
                binding.etEditGroupEditText.setMargins(bottom = -NAME_VERTICAL_PADDING.dp)
                binding.etEditGroupEditText.filters =
                    arrayOf(communityDescriptionChangeListener, InputFilter.LengthFilter(MAX_DESCRIPTION_LENGTH))

                initScrollListener()

                description?.let {
                    binding.etEditGroupEditText.setText(description)
                    actionListenerEditText(CommunityEditItemType.DESCRIPTION_TEXT_ITEM, it)
                }
            }

            else -> Unit
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initScrollListener() {
        binding.etEditGroupEditText.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }

    private fun actionListenerEditText(type: CommunityEditItemType, text: String) {
        when (type) {
            CommunityEditItemType.NAME_TEXT_ITEM -> {
                listener.invoke(MeeraCommunityEditAction.EditNameCommunity(text) { errorText ->
                    if (errorText.isNotEmpty()) {
                        binding.tvErrorMessageText.text = errorText
                        binding.vErrorMessageContainer.visible()
                    } else {
                        binding.vErrorMessageContainer.gone()
                    }
                })
            }

            CommunityEditItemType.DESCRIPTION_TEXT_ITEM -> {
                listener.invoke(MeeraCommunityEditAction.EditDescriptionCommunity(text) { errorText ->
                    if (errorText.isNotEmpty()) {
                        binding.tvErrorMessageText.text = errorText
                        binding.vErrorMessageContainer.visible()
                    } else {
                        binding.vErrorMessageContainer.gone()
                    }
                })
            }

            else -> Unit
        }
    }
}
