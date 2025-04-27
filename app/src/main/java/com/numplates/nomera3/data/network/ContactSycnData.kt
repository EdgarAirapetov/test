package com.numplates.nomera3.data.network

data class  ContactSycnData(
        val contacts: List<SyncContact>
)

data class SyncContact (
    val contactId: Long,
    val phone: String,
    val name: String
)