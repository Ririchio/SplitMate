package ru.fefu.splitmate.data.model

data class Calculation(
    val id: String,
    val totalAmount: Double,
    val peopleCount: Int,
    val tipPercentage: Int,
    val timestamp: Long
) {
    val tipAmount: Double
        get() = totalAmount * (tipPercentage / 100.0)

    val totalWithTip: Double
        get() = totalAmount + tipAmount

    val totalPerPerson: Double
        get() = totalWithTip / peopleCount
}