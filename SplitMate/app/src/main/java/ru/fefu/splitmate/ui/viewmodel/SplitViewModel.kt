package ru.fefu.splitmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.fefu.splitmate.data.model.Calculation
import ru.fefu.splitmate.data.model.TipOption
import ru.fefu.splitmate.data.model.defaultTipOptions
import java.util.*

class SplitViewModel : ViewModel() {

    private val _state = MutableStateFlow(SplitState())
    val state: StateFlow<SplitState> = _state.asStateFlow()

    private val _navigationEvents = Channel<NavigationEvent>()
    val navigationEvents = _navigationEvents.receiveAsFlow()

    init {
        _state.value = SplitState(
            uiState = SplitUiState(
                peopleCount = "1",
                peopleCountInt = 1,
                selectedTip = defaultTipOptions[2]
            )
        )
    }

    fun onEvent(event: SplitEvent) {
        val currentState = _state.value

        when (event) {
            is SplitEvent.UpdateTotal -> {
                val newTotal = event.value.toDoubleOrNull() ?: 0.0
                _state.value = currentState.copy(
                    uiState = currentState.uiState.copy(
                        totalAmount = event.value,
                        totalAmountDouble = newTotal,
                        isCalculateEnabled = isInputValid(newTotal, currentState.uiState.peopleCountInt)
                    )
                )
            }

            is SplitEvent.UpdatePeople -> {
                val newPeople = event.value.toIntOrNull() ?: 1
                val validPeople = newPeople.coerceAtLeast(1)
                _state.value = currentState.copy(
                    uiState = currentState.uiState.copy(
                        peopleCount = validPeople.toString(),
                        peopleCountInt = validPeople,
                        isCalculateEnabled = isInputValid(currentState.uiState.totalAmountDouble, validPeople)
                    )
                )
            }

            is SplitEvent.SelectTip -> {
                _state.value = currentState.copy(
                    uiState = currentState.uiState.copy(selectedTip = event.tipOption)
                )
            }

            SplitEvent.Calculate -> {
                val uiState = currentState.uiState
                if (!uiState.isCalculateEnabled) return

                val calculation = createCalculation(uiState)

                _state.value = currentState.copy(
                    calculations = (currentState.calculations + calculation).takeLast(5)
                )

                viewModelScope.launch {
                    _navigationEvents.send(NavigationEvent.NavigateToResult(calculation.id))
                }
            }

            SplitEvent.Reset -> {
                _state.value = SplitState(
                    uiState = SplitUiState(
                        peopleCount = "1",
                        peopleCountInt = 1,
                        selectedTip = defaultTipOptions[2]
                    )
                )
            }
        }
    }

    fun getCalculationById(id: String): Calculation? {
        return _state.value.calculations.find { it.id == id }
    }

    private fun createCalculation(uiState: SplitUiState): Calculation {
        return Calculation(
            id = UUID.randomUUID().toString(),
            totalAmount = uiState.totalAmountDouble,
            peopleCount = uiState.peopleCountInt,
            tipPercentage = uiState.selectedTip.percentage,
            timestamp = System.currentTimeMillis()
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
    val selectedTip: TipOption = defaultTipOptions[2],
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