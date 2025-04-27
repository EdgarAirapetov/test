package com.numplates.nomera3.presentation.view.ui.edittextautocompletable

public interface SuggestionMenuView {

    fun dismiss()

    fun dismiss(type: TagType)

    fun getSuggestedTagList(tag: String, tagType: TagType)

    fun getSuggestedUniqueNameList(uniqueName: String)

    fun getSuggestedUniqueNameListInGroupChat(uniqueName: String, chatRoomId: Long)

    fun onBackPressed(): Boolean?

    fun setEditText(editText: EditTextAutoCompletable?)

    fun clearResources()
}
