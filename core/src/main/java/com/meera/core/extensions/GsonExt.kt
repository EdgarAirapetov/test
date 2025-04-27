package com.meera.core.extensions

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.Serializable

private val gson = GsonBuilder().create()

fun Serializable.toJson(): String {
    return gson.toJson(this)
}

fun <Type : Serializable> String.fromJson(explicitClass: Class<out Serializable>): Type {
    return gson.fromJson(this, explicitClass) as Type
}

inline fun <reified T> Gson.fromJson(json: String) =
    this.fromJson<T>(json, object : TypeToken<T>() {}.type)

inline fun <reified T> Gson.fromJson(map: Map<String, Any?>) =
    this.fromJson<T>(this.toJson(map), object : TypeToken<T>() {}.type)

inline fun <reified T> Gson.fromJson(reader: JsonReader) =
    this.fromJson<T>(reader, object : TypeToken<T>() {}.type)
