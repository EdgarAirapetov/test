package com.meera.core.utils.contacts

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

private const val MAX_CONTACTS = 2000

interface UserContactsProvider {
    suspend fun provide(): List<String>
}

@Suppress("detekt:LoopWithTooManyJumpStatements")
class UserContactsProviderImpl @Inject constructor(
    private val context: Context
) : UserContactsProvider {

    //TODO https://nomera.atlassian.net/browse/BR-24108
    override suspend fun provide(): List<String> {
        val hashSet = hashSetOf<String>()
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                null
            ) ?: return emptyList()
            if (cursor.moveToFirst()) {
                do {
                    if (hashSet.size == MAX_CONTACTS) break
                    val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val number: String? = cursor.getString(numberIndex)
                    if (number.isNullOrEmpty()) continue
                    val formattedNumber = PhoneNumberUtils.formatNumber(number, Locale.getDefault().country)
                    if (formattedNumber.isNullOrEmpty()) continue
                    hashSet.add(formattedNumber)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            cursor?.close()
        }
        return ArrayList(hashSet)
    }
}
