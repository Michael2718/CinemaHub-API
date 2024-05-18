package com.michael.features.paymentMethod

interface PaymentMethodDao {
    suspend fun getAll(): List<PaymentMethod>

    suspend fun addPaymentMethod(
        paymentMethod: PaymentMethod
    ): PaymentMethod?
}