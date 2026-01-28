package ru.fefu.splitmate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.collectLatest
import ru.fefu.splitmate.ui.screens.HomeScreen
import ru.fefu.splitmate.ui.screens.InputScreen
import ru.fefu.splitmate.ui.screens.ResultScreen
import ru.fefu.splitmate.ui.viewmodel.SplitViewModel
import ru.fefu.splitmate.ui.viewmodel.NavigationEvent
import ru.fefu.splitmate.ui.viewmodel.SplitEvent

object Routes {
    const val HOME = "home"
    const val INPUT = "input"
    const val RESULT = "result/{calcId}"

    fun createResultRoute(calcId: String): String {
        return "result/$calcId"
    }
}

@Composable
fun AppNavigation(viewModel: SplitViewModel) {
    val navController = rememberNavController()

    val state by viewModel.state.collectAsState()

    val uiState = state.uiState


    LaunchedEffect(viewModel) {
        viewModel.navigationEvents.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateToResult -> {
                    val calculation = viewModel.getCalculationById(event.calculationId)
                    if (calculation != null) {
                        navController.navigate(Routes.createResultRoute(event.calculationId))
                    }
                }
                NavigationEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onStartClick = { navController.navigate(Routes.INPUT) }
            )
        }

        composable(Routes.INPUT) {
            InputScreen(
                state = uiState,
                onEvent = viewModel::onEvent,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.RESULT,
            arguments = listOf(navArgument("calcId") { type = NavType.StringType })
        ) { backStackEntry ->
            val calcId = backStackEntry.arguments?.getString("calcId") ?: ""
            val calculation = viewModel.getCalculationById(calcId)

            if (calculation == null) {
                LaunchedEffect(calcId) {
                    navController.popBackStack(Routes.INPUT, inclusive = false)
                }
                return@composable
            }

            ResultScreen(
                calculation = calculation,
                onEdit = {
                    navController.popBackStack(Routes.INPUT, inclusive = false)
                },
                onNewCalculation = {
                    viewModel.onEvent(SplitEvent.Reset)
                    navController.navigate(Routes.INPUT) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                }
            )
        }
    }
}