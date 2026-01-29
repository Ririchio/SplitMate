package ru.fefu.splitmate.data.model

import java.util.UUID

data class Calculation(
    val id: String = UUID.randomUUID().toString(),
    val totalAmount: Double,
    val peopleCount: Int,
    val tipPercentage: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    val tipAmount: Double
        get() = totalAmount * (tipPercentage / 100.0)

    val totalWithTip: Double
        get() = totalAmount + tipAmount

    val totalPerPerson: Double
        get() = totalWithTip / peopleCount
}