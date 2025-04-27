package com.numplates.nomera3.presentation.view.fragments.userprofileinfo.holders

import android.annotation.SuppressLint
import android.content.res.Resources
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraUserPersonalInfoInputItemBinding
import com.numplates.nomera3.presentation.view.fragments.userprofileinfo.UserPersonalInfoAction
import com.numplates.nomera3.presentation.view.fragments.userprofileinfo.UserPersonalInfoItemType
import com.numplates.nomera3.presentation.viewmodel.UserPersonalInfoContainer
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val FULL_NAME_MAX_LENGTH = 30
private const val UNIQUE_NAME_MAX_LENGTH = 25

class MeeraUserPersonalInfoInputItemHolder(
    val binding: MeeraUserPersonalInfoInputItemBinding,
    val type: UserPersonalInfoItemType,
    val resources: Resources,
    private val actionListener: (UserPersonalInfoAction) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    private var delayedNameValidation: Disposable? = null

    @SuppressLint("SetTextI18n")
    fun bind(userProfileContainer: UserPersonalInfoContainer) {
        when (type) {
            UserPersonalInfoItemType.FULL_NAME -> {
                binding.tvPersonalInfoInputItemHeader.text = resources.getString(R.string.first_end_last_name)
                binding.profileParamInput.setHint(binding.root.resources.getString(R.string.meera_hint_first_end_last_name))
                binding.profileParamInput.etInput.setText(userProfileContainer.nickname)
                binding.profileParamInput.etInput.filters = arrayOf(InputFilter.LengthFilter(FULL_NAME_MAX_LENGTH))
                initDoAfterTextChanged { fullName ->
                    actionListener.invoke(
                        UserPersonalInfoAction.InputFullName(fullName = fullName) { errorText ->
                            errorText?.let {
                                binding.vgErrorContainer.visible()
                                binding.tvErrorMessage.text = errorText
                            } ?: binding.vgErrorContainer.gone()
                        })
                }
            }

            UserPersonalInfoItemType.UNIQUE_NAME -> {
                binding.profileParamInput.showUsernameAtSymbol = true
                binding.profileParamInput.setHint(
                    binding.root.resources.getString(R.string.meera_enter_unique_name)
                )
                binding.tvPersonalInfoInputItemHeader.text = resources.getString(R.string.uniquename_label)
                binding.profileParamInput.etInput.setText(userProfileContainer.username)
                binding.profileParamInput.etInput.filters = arrayOf(InputFilter.LengthFilter(UNIQUE_NAME_MAX_LENGTH))
                initDoAfterTextChanged { uniqueName ->
                    actionListener.invoke(
                        UserPersonalInfoAction.InputUniqueName(
                            uniqueName = uniqueName
                        ) { textError ->
                            textError?.let {
                                binding.vgErrorContainer.visible()
                                if (binding.profileParamInput.etInput.text.isNullOrEmpty()) {
                                    binding.tvErrorMessage.setText(R.string.required_field)
                                } else {
                                    binding.tvErrorMessage.text = textError
                                }
                            } ?: binding.vgErrorContainer.gone()
                        })
                }
                initLowerCaseChanger()
            }

            else -> {
                Timber.i("Unknown field type")
            }
        }
    }

    private fun initDoAfterTextChanged(action: (text: String) -> Unit) {
        binding.profileParamInput.etInput.doAfterTextChanged { typed: Editable? ->
            typed?.toString()?.let { name: String ->
                delayedNameValidation?.dispose()
                delayedNameValidation = Observable.just(name)
                    .debounce(300, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { text ->
                        action.invoke(deletePrefixUniqueName(text))
                    }
            }
        }
    }

    private fun deletePrefixUniqueName(uniqueName: String): String {
        if (uniqueName.startsWith(resources.getString(R.string.uniquename_prefix))) {
            return uniqueName.substring(1)
        }
        return uniqueName
    }

    private fun initLowerCaseChanger() {
        val lowerCaseChanger = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                val sourceText = s?.toString() ?: ""
                val sourceLowerCased = sourceText.lowercase(Locale.getDefault())
                if (sourceText == sourceLowerCased) return
                binding.profileParamInput.etInput.removeTextChangedListener(this)
                val typedUniqueName = s?.toString() ?: ""
                binding.profileParamInput.etInput.setText(typedUniqueName.lowercase(Locale.getDefault()))
                binding.profileParamInput.etInput.setSelection(typedUniqueName.length)
                binding.profileParamInput.etInput.addTextChangedListener(this)
            }
        }
        binding.profileParamInput.etInput.addTextChangedListener(lowerCaseChanger)
    }
}
