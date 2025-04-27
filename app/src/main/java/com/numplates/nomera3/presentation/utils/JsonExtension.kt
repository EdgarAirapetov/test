package com.numplates.nomera3.presentation.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.core.extensions.empty
import org.phoenixframework.Payload

inline fun <reified T> Payload.makeEntity(gson: Gson) =
    this.makeJson(gson).makeEntity<T>(gson)

inline fun <reified T> String.makeEntity(gson: Gson) =
    gson.fromJson<T>(this, object : TypeToken<T>() {}.type)

fun Payload.makeJson(gson: Gson): String = gson.toJson(this) ?: kotlin.String.empty()