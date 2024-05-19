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
import org.postgresql.util.PGmoney

object MoneyColumnType : IColumnType<PGmoney> {
    override var nullable: Boolean = true

    override fun sqlType(): String = "money"

    override fun valueFromDB(value: Any): PGmoney {
        return when (value) {
            is PGmoney -> {
                value
            }

            is Double -> {
                PGmoney(value)
            }

            else -> {
                throw IllegalArgumentException("Unexpected value type for money column: ${value.javaClass}")
            }
        }
    }

    override fun valueToDB(value: PGmoney?): Any? {
        return value ?: if (nullable) {
            null
        } else {
            throw IllegalArgumentException("Non-nullable money column cannot have null value")
        }
    }

    override fun valueToString(value: PGmoney?): String {
        return value?.toString() ?: super.valueToString(value)
    }
}


fun Table.money(name: String): Column<PGmoney> = registerColumn(name, MoneyColumnType)

object PGMoneySerializer : KSerializer<PGmoney> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("PGmoney") {
        element<String>("money")
    }

    override fun serialize(encoder: Encoder, value: PGmoney) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): PGmoney {
        try {
            val moneyDouble = decoder.decodeDouble()
            return PGmoney(moneyDouble)
        } catch (e: NumberFormatException) {
            throw SerializationException("Error parsing money: ${e.message}")
        }
    }
}