package io.switstack.switcloud.swittestl3.common

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

object SerializationUtils {

    val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    inline fun <reified T> decodeMessage(payload: String): T = json.decodeFromString<T>(payload)

    inline fun <reified T> serializeToJsonString(data: T) = json.encodeToString(data)

    inline fun <reified T> T.toJsonObject(json: Json = Json): JsonObject = json.encodeToJsonElement(this).jsonObject
}