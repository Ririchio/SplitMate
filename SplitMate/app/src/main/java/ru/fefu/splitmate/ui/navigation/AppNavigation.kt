package ru.fefu.splitmate.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.fefu.splitmate.ui.screens.HomeScreen
import ru.fefu.splitmate.ui.screens.InputScreen
import ru.fefu.splitmate.ui.screens.ResultScreen
import ru.fefu.splitmate.ui.viewmodel.SplitViewModel
import ru.fefu.splitmate.ui.viewmodel.NavigationEvent

@Composable
fun AppNavigation(viewModel: SplitViewModel) {
    val navController = rememberNavController()

    val state by remember { viewModel.state }

    LaunchedEffect(key1 = viewModel) {
        viewModel.setNavigationCallback { event ->
            when (event) {
                is NavigationEvent.NavigateToResult -> {
                    navController.navigate(Routes.createResultRoute(event.calculationId))
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
            val currentState = state.uiState

            InputScreen(
                state = currentState,
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
                LaunchedEffect(key1 = calcId) {
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
                    viewModel.onEvent(ru.fefu.splitmate.ui.viewmodel.SplitEvent.Reset)
                    navController.navigate(Routes.INPUT) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                }
            )
        }
    }
}

object Routes {
    const val HOME = "home"
    const val INPUT = "input"
    const val RESULT = "result/{calcId}"

    fun createResultRoute(calcId: String): String {
        return "result/$calcId"
    }
}