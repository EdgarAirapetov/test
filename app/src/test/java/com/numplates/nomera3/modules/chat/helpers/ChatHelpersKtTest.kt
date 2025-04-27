package com.numplates.nomera3.modules.chat.helpers

import org.junit.Assert.assertEquals
import org.junit.Test

internal class ChatHelpersKtTest {

    @Test
    fun isNetworkUriTest() {
        val isNetworkUri = emptyList<String>().isNetworkPath()
        assertEquals(isNetworkUri, false)
    }

}