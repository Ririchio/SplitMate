package ru.fefu.splitmate.data.model

data class TipOption(
    val label: String,
    val percentage: Int
)

val defaultTipOptions = listOf(
    TipOption("0%", 0),
    TipOption("5%", 5),
    TipOption("10%", 10),
    TipOption("15%", 15),
    TipOption("20%", 20)
)