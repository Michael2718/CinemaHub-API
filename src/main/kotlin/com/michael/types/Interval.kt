package com.michael.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.Table
import org.postgresql.util.PGInterval

object IntervalColumnType : IColumnType<PGInterval> {

    override var nullable: Boolean = true

    override fun sqlType(): String = "interval"

    override fun valueFromDB(value: Any): PGInterval {
        if (value is PGInterval) {
            return value
        } else {
            throw IllegalArgumentException("Unexpected value type for interval column: ${value.javaClass}")
        }
    }

    override fun valueToDB(value: PGInterval?): Any? {
        return value ?: if (nullable) {
            null
        } else {
            throw IllegalArgumentException("Non-nullable interval column cannot have null value")
        }
    }

    override fun valueToString(value: PGInterval?): String {
        return value?.toString() ?: super.valueToString(value)
    }
}


fun Table.interval(name: String): Column<PGInterval> = registerColumn(name, IntervalColumnType)

object PGIntervalSerializer : KSerializer<PGInterval> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PGInterval") {
        element<String>("interval")
    }

    override fun serialize(encoder: Encoder, value: PGInterval) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): PGInterval {
        val intervalString = decoder.decodeString()
        val parts = intervalString.split(" ").filterIndexed { index, _ ->  index % 2 == 0}
        if (parts.size != 6) throw SerializationException("Expected 6 parts in the interval string, got ${parts.size}")

        try {
            val years = parts[0].toInt()
            val months = parts[1].toInt()
            val days = parts[2].toInt()
            val hours = parts[3].toInt()
            val minutes = parts[4].toInt()
            val seconds = parts[5].toDouble()

            return PGInterval(years, months, days, hours, minutes, seconds)
        } catch (e: NumberFormatException) {
            throw SerializationException("Error parsing interval parts: ${e.message}")
        }
    }
}
