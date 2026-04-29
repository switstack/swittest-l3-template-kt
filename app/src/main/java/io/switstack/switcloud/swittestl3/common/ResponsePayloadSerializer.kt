package io.switstack.switcloud.swittestl3.common

import io.switstack.switcloud.swittestl3.data.responses.ResponsePayload
import io.switstack.switcloud.swittestl3.data.responses.Status
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

object ResponsePayloadSerializer : KSerializer<ResponsePayload> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(ResponsePayload.Companion::class.java.simpleName) {
        element("status", Status.serializer().descriptor)
        element("others", JsonObject.serializer().descriptor)
    }

    override fun deserialize(decoder: Decoder): ResponsePayload {
        require(decoder is JsonDecoder)

        // 1. Read the whole object as a raw JsonElement
        val root = decoder.decodeJsonElement().jsonObject

        // 2. Extract and decode the 'status' field manually
        // We use the JSON configuration from the decoder to ensure settings are preserved
        val statusElement = root["status"]
            ?: throw SerializationException("Field 'status' is missing")
        val status = decoder.json.decodeFromJsonElement(Status.serializer(), statusElement)

        // 3. Filter everything else into 'others'
        val others = JsonObject(root.filterKeys { it != "status" })

        return ResponsePayload(status, others)
    }

    override fun serialize(encoder: Encoder, value: ResponsePayload) {
        require(encoder is JsonEncoder)

        // 1. Convert the status back to a JsonElement
        val statusElement = encoder.json.encodeToJsonElement(Status.serializer(), value.status)

        // 2. Merge 'status' + 'others' into one map
        val combinedMap = (value.others?.toMutableMap() ?: mutableMapOf())
        combinedMap["status"] = statusElement

        // 3. Write the merged object
        encoder.encodeJsonElement(JsonObject(combinedMap))
    }
}