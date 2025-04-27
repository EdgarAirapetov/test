package com.numplates.nomera3.modules.auth.data.utils

import android.util.Base64
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

private val CHAR_POOL: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun getAuthCodeChallenge(randomString: String): String {
    return sha256(randomString)
}

@Throws(NoSuchAlgorithmException::class)
private fun sha256(text: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(text.toByteArray())
    val digest = md.digest()
    // NO_WRAP \n in the end of string
    return Base64.encodeToString(digest, Base64.NO_WRAP)
}

fun randomString(length: Int) =
    (1..length)
        .map { i -> kotlin.random.Random.nextInt(0, CHAR_POOL.size) }
        .map(CHAR_POOL::get)
        .joinToString("")