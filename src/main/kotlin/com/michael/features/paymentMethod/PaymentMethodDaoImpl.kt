package com.michael.features.paymentMethod

import com.michael.plugins.DatabaseSingleton.dbQuery
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll

class PaymentMethodDaoImpl : PaymentMethodDao {
    override suspend fun getAll(): List<PaymentMethod> = dbQuery {
        PaymentMethodTable.selectAll().map { it.toPaymentMethod() }
    }

    override suspend fun addPaymentMethod(paymentMethod: PaymentMethod): PaymentMethod? {
        TODO("Not yet implemented")
    }

    private fun ResultRow.toPaymentMethod(): PaymentMethod = PaymentMethod(
        this[PaymentMethodTable.paymentMethodId],
        this[PaymentMethodTable.name],
        this[PaymentMethodTable.description],
        this[PaymentMethodTable.isActive],
    )
}