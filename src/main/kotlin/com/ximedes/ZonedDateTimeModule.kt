package com.ximedes

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ZonedDateTimeModule : SimpleModule() {
    init {
        this.addSerializer(
                ZonedDateTime::class.java,
                IsoZonedDateTimeSerializer()
        )
        this.addDeserializer(
                ZonedDateTime::class.java,
                IsoZonedDateTimeDeserializer()
        )
    }
}

class IsoZonedDateTimeSerializer() : JsonSerializer<ZonedDateTime>() {
    override fun serialize(zonedDateTime: ZonedDateTime?, generator: JsonGenerator, provider: SerializerProvider) {
        if (zonedDateTime == null) {
            generator.writeNull()
        } else {
            val text = DateTimeFormatter.ISO_ZONED_DATE_TIME.format(zonedDateTime)
            generator.writeString(text)
        }
    }
}

class IsoZonedDateTimeDeserializer : JsonDeserializer<ZonedDateTime>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): ZonedDateTime? {
        val value: String = parser.codec.readValue(parser, String::class.java) ?: return null
        val parsed = DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(value)
        return ZonedDateTime.from(parsed)
    }
}