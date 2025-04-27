package com.meera.core.bottomsheets

import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior

private const val DEFAULT_MALE = 1
private const val DEFAULT_VERIFIED = 0
interface SuggestionsMenuContract {

    val isHidden: Boolean

    fun init(recyclerView: RecyclerView, editText: EditText?, bottomSheetBehavior: BottomSheetBehavior<View>)

    fun setExtraPeekHeight(newExtraPeekHeight: Int, isAnimate: Boolean)

    fun searchUsersByUniqueName(uniqueName: String?)

    fun forceCloseMenu()

    fun setDarkColored()

    var suggestedUniqueNameClicked: ((UITagEntity) -> Unit)?

    data class UITagEntity(
        var id: Long?,
        var image: String?,
        var uniqueName: String?,
        var userName: String?,
        var isMale: Int? = DEFAULT_MALE,
        var isVerified: Int? = DEFAULT_VERIFIED
    )
}
