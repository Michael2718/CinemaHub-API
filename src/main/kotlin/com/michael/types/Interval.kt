package com.michael.types

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
