package com.numplates.nomera3.presentation.view.widgets.numberplateview.maskformatter

import android.annotation.SuppressLint
import android.os.Build
import android.text.Editable
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import android.widget.TextView
import com.meera.core.extensions.empty
import kotlin.math.min

private const val FIRST_CHAR = 1
private const val SECOND_CHAR = 2

class MeeraMaskFormatter : TextWatcher {

    private var mask: String? = null
    private var maskedField: EditText? = null
    private var dynamicMask: TextView? = null
    private var maskCharacter = 0.toChar()

    private var editTextChange = false
    private var newIndex = 0
    private var textBefore: String? = null
    private var selectionBefore = 0
    private var passwordMask = 0


    private var charTransforms: CharTransforms? = null

    constructor(
        mask: String?,
        maskedField: EditText,
        dynamicMask: TextView,
        pattern: String?
    ) : this(mask, maskedField, ' ') {
        charTransforms = CharTransforms(pattern)
        this.dynamicMask = dynamicMask
    }

    constructor(mask: String?, maskedField: EditText, maskCharacter: Char) {
        this.mask = mask
        this.maskedField = maskedField
        this.maskCharacter = maskCharacter
        this.passwordMask = getPasswordMask(maskedField)
        setInputTypeBasedOnMask()
    }

    private fun getPasswordMask(maskedField: EditText): Int {
        val inputType = maskedField.inputType
        var maskedFieldPasswordMask = (inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD
            or inputType and InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            maskedFieldPasswordMask =
                maskedFieldPasswordMask or (inputType and InputType.TYPE_NUMBER_VARIATION_PASSWORD)
            maskedFieldPasswordMask =
                maskedFieldPasswordMask or (inputType and InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)
        }

        return maskedFieldPasswordMask
    }

    override fun beforeTextChanged(s: CharSequence, index: Int, toBeReplacedCount: Int, addedCount: Int) {
        textBefore = s.toString()
        maskedField?.selectionEnd?.let { selectionBefore = it }

    }

    private var currentNumber = String.empty()
    private var startInd = 0

    override fun onTextChanged(s: CharSequence, index: Int, replacedCount: Int, addedCount: Int) {
        if (editTextChange) {
            maskedField?.setSelection(newIndex)
            editTextChange = false
            return
        }
        try {
            if (index == 0 && replacedCount == FIRST_CHAR) {
                maskedField?.setText(String.empty())
                val newMask = updateMask(String.empty())
                maskedField?.currentHintTextColor?.let { dynamicMask?.setTextColor(it) }

                dynamicMask?.text = newMask.toString().uppercase()
                return
            }

            if (s == null || s.isEmpty()) return

            currentNumber = s.subSequence(0, index + addedCount).toString()
            val appliedMaskString = applyMask(currentNumber).uppercase()
            val newMask = updateMask(appliedMaskString)
            dynamicMask?.text = newMask.toString().uppercase()
            if (appliedMaskString != s.toString()) {

                editTextChange = true
                newIndex = countNewIndex(addedCount, appliedMaskString)

                maskedField?.setText(appliedMaskString)
            }
        } catch (_: InvalidTextException) {
            editTextChange = true
            newIndex = selectionBefore
            maskedField?.setText(textBefore)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun updateMask(appliedMaskString: String): SpannableStringBuilder {
        maskedField?.let { masked ->
            val formattedText = SpannableStringBuilder()
            formattedText.append(appliedMaskString, ForegroundColorSpan(masked.currentTextColor), 0)
            val maskStartInd = min(appliedMaskString.length, mask?.length ?: 0)
            startInd = maskStartInd
            formattedText.append(
                mask?.substring(maskStartInd), ForegroundColorSpan(masked.currentHintTextColor), 0
            )
            return formattedText
        } ?: return SpannableStringBuilder()
    }

    private fun countNewIndex(addedCount: Int, appliedMaskString: String): Int {
        if (appliedMaskString.isEmpty()) {
            return 0
        }

        if (addedCount < FIRST_CHAR) {
            return newIndexForRemovingCharacters(appliedMaskString)
        }

        return newIndexForAddingCharacters(appliedMaskString)
    }

    private fun newIndexForRemovingCharacters(appliedMaskString: String): Int {
        selectionBefore = if (selectionBefore > appliedMaskString.length) {
            appliedMaskString.length
        } else {
            selectionBefore - FIRST_CHAR
        }

        if (selectionBefore < 0) {
            return 0
        }

        if (selectionBefore - FIRST_CHAR >= 0
            && appliedMaskString[selectionBefore - FIRST_CHAR] == maskCharacter
        ) {
            return selectionBefore - FIRST_CHAR
        }

        return selectionBefore
    }

    private fun newIndexForAddingCharacters(appliedMaskString: String): Int {
        if (selectionBefore >= appliedMaskString.length) {
            return appliedMaskString.length
        }

        if (appliedMaskString[selectionBefore] == maskCharacter) {
            return selectionBefore + SECOND_CHAR
        }

        return selectionBefore + FIRST_CHAR
    }

    override fun afterTextChanged(s: Editable?) {
        if (editTextChange) {
            maskedField?.setSelection(newIndex)
        }
        setInputTypeBasedOnMask()
    }

    @Throws(InvalidTextException::class)
    private fun applyMask(newValue: String): String {
        val newValueWithoutSpaces = newValue.replace(maskCharacter.toString().toRegex(), String.empty())
        val sb = StringBuilder()
        var index = 0
        for (c in newValueWithoutSpaces.toCharArray()) {
            if (index >= mask?.length ?: 0) {
                throw InvalidTextException()
            }
            mask?.let {
                while (it[index] == maskCharacter) {
                    sb.append(maskCharacter)
                    index++
                }
            }

            sb.append(applyMaskToChar(c, index))
            index++
        }

        return sb.toString()
    }

    @Throws(InvalidTextException::class)
    private fun applyMaskToChar(c: Char, maskIndex: Int): Char {
        return mask?.let {
            charTransforms?.transformChar(c, it[maskIndex])
        } ?: return ' '
    }

    private fun setInputTypeBasedOnMask() {
        maskedField?.selectionEnd?.let {
            val selection = it
            if (selection >= (mask?.length ?: 0)) {
                return
            }
        }


        val maskChar = getFirstNotWhiteCharFromMask()
        if (maskChar == maskCharacter) {
            return
        }

        maskedField?.inputType = passwordMask or CharInputType.getKeyboardTypeForNextChar(maskChar)
    }

    private fun getFirstNotWhiteCharFromMask(): Char {
        mask?.let {
            var maskIndex = maskedField?.selectionEnd ?: 0
            while (maskIndex < it.length && it[maskIndex] == maskCharacter) {
                maskIndex++
            }
            return it[maskIndex]
        } ?: return ' '
    }
}
