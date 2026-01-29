package ru.fefu.splitmate.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import ru.fefu.splitmate.data.model.Calculation
import ru.fefu.splitmate.data.model.TipOption
import ru.fefu.splitmate.data.model.defaultTipOptions

class SplitViewModel : ViewModel() {
    private val _state = mutableStateOf(SplitState())
    val state = _state

    private var _navigationCallback: ((NavigationEvent) -> Unit)? = null

    fun setNavigationCallback(callback: (NavigationEvent) -> Unit) {
        _navigationCallback = callback
    }

    init {
        _state.value = SplitState(
            uiState = SplitUiState(
                peopleCount = "1",
                peopleCountInt = 1,
                selectedTip = defaultTipOptions.getOrElse(2) { defaultTipOptions[0] }
            )
        )
    }

    fun onEvent(event: SplitEvent) {
        val currentState = _state.value

        when (event) {
            is SplitEvent.UpdateTotal -> {
                val newTotal = event.value.toDoubleOrNull() ?: 0.0
                val newUiState = currentState.uiState.copy(
                    totalAmount = event.value,
                    totalAmountDouble = newTotal,
                    isCalculateEnabled = isInputValid(newTotal, currentState.uiState.peopleCountInt)
                )
                _state.value = currentState.copy(uiState = newUiState)
            }

            is SplitEvent.UpdatePeople -> {
                val newPeople = event.value.toIntOrNull() ?: 1
                val validPeople = newPeople.coerceAtLeast(1)
                val newUiState = currentState.uiState.copy(
                    peopleCount = validPeople.toString(),
                    peopleCountInt = validPeople,
                    isCalculateEnabled = isInputValid(currentState.uiState.totalAmountDouble, validPeople)
                )
                _state.value = currentState.copy(uiState = newUiState)
            }

            is SplitEvent.SelectTip -> {
                val newUiState = currentState.uiState.copy(selectedTip = event.tipOption)
                _state.value = currentState.copy(uiState = newUiState)
            }

            SplitEvent.Calculate -> {
                val uiState = currentState.uiState
                if (!uiState.isCalculateEnabled) return

                val calculation = createCalculation(uiState)

                _state.value = currentState.copy(
                    calculations = currentState.calculations + calculation
                )

                _navigationCallback?.invoke(NavigationEvent.NavigateToResult(calculation.id))
            }

            SplitEvent.Reset -> {
                _state.value = SplitState(
                    uiState = SplitUiState(
                        peopleCount = "1",
                        peopleCountInt = 1,
                        selectedTip = defaultTipOptions.getOrElse(2) { defaultTipOptions[0] }
                    ),
                    calculations = emptyList()
                )
            }
        }
    }

    fun getCalculationById(id: String): Calculation? {
        return state.value.calculations.find { it.id == id }
    }

    private fun createCalculation(uiState: SplitUiState): Calculation {
        return Calculation(
            totalAmount = uiState.totalAmountDouble,
            peopleCount = uiState.peopleCountInt,
            tipPercentage = uiState.selectedTip.percentage
        )
    }

    private fun isInputValid(total: Double, people: Int): Boolean {
        return total > 0 && people > 0
    }
}

data class SplitState(
    val uiState: SplitUiState = SplitUiState(),
    val calculations: List<Calculation> = emptyList()
)

data class SplitUiState(
    val totalAmount: String = "",
    val totalAmountDouble: Double = 0.0,
    val peopleCount: String = "1",
    val peopleCountInt: Int = 1,
    val selectedTip: TipOption = defaultTipOptions.first(),
    val isCalculateEnabled: Boolean = false
)

sealed class SplitEvent {
    data class UpdateTotal(val value: String) : SplitEvent()
    data class UpdatePeople(val value: String) : SplitEvent()
    data class SelectTip(val tipOption: TipOption) : SplitEvent()
    object Calculate : SplitEvent()
    object Reset : SplitEvent()
}

sealed class NavigationEvent {
    data class NavigateToResult(val calculationId: String) : NavigationEvent()
    object NavigateBack : NavigationEvent()
}