package com.numplates.nomera3.modules.baseCore

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.meera.core.extensions.pluralString


interface ResourceManager {
    fun getString(@StringRes res: Int): String

    fun getString(@StringRes res: Int, name: String) : String

    fun getPlurals(@PluralsRes idRes: Int, quantity: Int, vararg vars: Any?) : String
}

class ResourceManagerImp (private val appContext: Context): ResourceManager {
    override fun getString(@StringRes res: Int) =
        appContext.getString(res)

    override fun getPlurals(@PluralsRes idRes: Int, quantity: Int, vararg vars: Any?): String {
        return appContext.pluralString(idRes, quantity, *vars)
    }

    override fun getString(res: Int, name: String): String {
        return appContext.getString(res, name)
    }

}
