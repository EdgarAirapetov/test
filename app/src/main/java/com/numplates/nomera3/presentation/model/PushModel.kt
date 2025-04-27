package com.numplates.nomera3.presentation.model

import java.io.Serializable


/**
 * created by c7j on 28.10.18
 */
data class PushModel (
    var ava: String?,
    var title: String?,
    var subTitile: String?,
    var content: String?,
    var link: String?,
    var time: Long,
    var type: String?,
    var accountType: Int,
    var innerLink: String?,
    var innerLink2: String?)

: Serializable {

    constructor() : this("", "", "", "", "", 0, "", 0, "", "")

}