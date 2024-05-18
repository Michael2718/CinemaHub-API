package com.michael.features.paymentMethod

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class PaymentMethod(
    val paymentMethodId: Int,
    val name: String,
    val description: String,
    val isActive: Boolean
)

object PaymentMethodTable : Table("payment_method") {
    val paymentMethodId = integer("payment_method_id")
    val name = varchar("name", 50)
    val description = varchar("description", 256)
    val isActive = bool("is_active")

    override val primaryKey = PrimaryKey(paymentMethodId)
}
