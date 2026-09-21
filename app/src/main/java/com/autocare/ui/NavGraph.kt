package com.autocare.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.autocare.AutoCareApplication
import com.autocare.ui.dashboard.DashboardScreen
import com.autocare.ui.dashboard.DashboardViewModel
import com.autocare.ui.dashboard.DashboardViewModelFactory
import com.autocare.ui.expense.AddExpenseScreen
import com.autocare.ui.expense.ExpenseViewModel
import com.autocare.ui.expense.ExpenseViewModelFactory
import com.autocare.ui.fuel.AddFuelScreen
import com.autocare.ui.fuel.FuelViewModel
import com.autocare.ui.fuel.FuelViewModelFactory
import com.autocare.ui.history.HistoryScreen
import com.autocare.ui.history.HistoryViewModel
import com.autocare.ui.history.HistoryViewModelFactory
import com.autocare.ui.maintenance.AddMaintenanceScreen
import com.autocare.ui.maintenance.MaintenanceViewModel
import com.autocare.ui.maintenance.MaintenanceViewModelFactory
import com.autocare.ui.reminder.EditReminderScreen
import com.autocare.ui.reminder.ReminderViewModel
import com.autocare.ui.reminder.ReminderViewModelFactory
import com.autocare.ui.vehicle.AddVehicleScreen
import com.autocare.ui.vehicle.VehicleViewModel
import com.autocare.ui.vehicle.VehicleViewModelFactory

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object AddVehicle : Screen("add_vehicle")
    object EditVehicle : Screen("edit_vehicle/{vehicleId}") {
        fun createRoute(vehicleId: Int) = "edit_vehicle/$vehicleId"
    }
    object AddFuel : Screen("add_fuel/{vehicleId}") {
        fun createRoute(vehicleId: Int) = "add_fuel/$vehicleId"
    }
    object AddMaintenance : Screen("add_maintenance/{vehicleId}") {
        fun createRoute(vehicleId: Int) = "add_maintenance/$vehicleId"
    }
    object AddExpense : Screen("add_expense/{vehicleId}") {
        fun createRoute(vehicleId: Int) = "add_expense/$vehicleId"
    }
    object EditFuel : Screen("edit_fuel/{fuelId}") {
        fun createRoute(fuelId: Int) = "edit_fuel/$fuelId"
    }
    object EditMaintenance : Screen("edit_maintenance/{maintId}") {
        fun createRoute(maintId: Int) = "edit_maintenance/$maintId"
    }
    object EditExpense : Screen("edit_expense/{expenseId}") {
        fun createRoute(expenseId: Int) = "edit_expense/$expenseId"
    }
    object EditReminder : Screen("edit_reminder/{reminderId}") {
        fun createRoute(reminderId: Int) = "edit_reminder/$reminderId"
    }
    object History : Screen("history/{vehicleId}") {
        fun createRoute(vehicleId: Int) = "history/$vehicleId"
    }
    object Settings : Screen("settings")
}

@Composable
fun AutoCareNavGraph(
    navController: NavHostController = rememberNavController(),
    darkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    isBiometricSecurityEnabled: Boolean,
    onBiometricSecurityChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as AutoCareApplication

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            val dashboardViewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModelFactory(
                    app.vehicleRepository,
                    app.maintenanceRepository,
                    app.fuelLoadRepository,
                    app.expenseRepository,
                    app.reminderRepository
                )
            )
            DashboardScreen(
                viewModel = dashboardViewModel,
                onNavigateToAddVehicle = {
                    navController.navigate(Screen.AddVehicle.route)
                },
                onNavigateToEditVehicle = { vehicleId: Int ->
                    navController.navigate(Screen.EditVehicle.createRoute(vehicleId))
                },
                onNavigateToAddFuel = { vehicleId: Int ->
                    navController.navigate(Screen.AddFuel.createRoute(vehicleId))
                },
                onNavigateToAddMaintenance = { vehicleId: Int ->
                    navController.navigate(Screen.AddMaintenance.createRoute(vehicleId))
                },
                onNavigateToAddExpense = { vehicleId: Int ->
                    navController.navigate(Screen.AddExpense.createRoute(vehicleId))
                },
                onNavigateToHistory = { vehicleId: Int ->
                    navController.navigate(Screen.History.createRoute(vehicleId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToEditReminder = { reminderId ->
                    navController.navigate(Screen.EditReminder.createRoute(reminderId))
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                darkTheme = darkTheme,
                onThemeChange = onThemeChange,
                isBiometricSecurityEnabled = isBiometricSecurityEnabled,
                onBiometricSecurityChange = onBiometricSecurityChange,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AddVehicle.route) {
            val vehicleViewModel: VehicleViewModel = viewModel(
                factory = VehicleViewModelFactory(app.vehicleRepository)
            )
            AddVehicleScreen(
                viewModel = vehicleViewModel,
                onVehicleAdded = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.EditVehicle.route,
            arguments = listOf(androidx.navigation.navArgument("vehicleId") {
                type = androidx.navigation.NavType.IntType
            })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getInt("vehicleId") ?: -1
            val vehicleViewModel: VehicleViewModel = viewModel(
                factory = VehicleViewModelFactory(app.vehicleRepository)
            )
            AddVehicleScreen(
                viewModel = vehicleViewModel,
                vehicleId = vehicleId,
                onVehicleAdded = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.AddFuel.route,
            arguments = listOf(androidx.navigation.navArgument("vehicleId") {
                type = androidx.navigation.NavType.IntType
            })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getInt("vehicleId") ?: -1
            val fuelViewModel: FuelViewModel = viewModel(
                factory = FuelViewModelFactory(app.fuelLoadRepository, app.vehicleRepository)
            )
            AddFuelScreen(
                viewModel = fuelViewModel,
                vehicleId = vehicleId,
                onFuelAdded = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.AddMaintenance.route,
            arguments = listOf(androidx.navigation.navArgument("vehicleId") {
                type = androidx.navigation.NavType.IntType
            })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getInt("vehicleId") ?: -1
            val maintenanceViewModel: MaintenanceViewModel = viewModel(
                factory = MaintenanceViewModelFactory(app.maintenanceRepository, app.vehicleRepository, app.reminderRepository)
            )
            AddMaintenanceScreen(
                viewModel = maintenanceViewModel,
                vehicleId = vehicleId,
                onMaintenanceAdded = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.AddExpense.route,
            arguments = listOf(androidx.navigation.navArgument("vehicleId") {
                type = androidx.navigation.NavType.IntType
            })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getInt("vehicleId") ?: -1
            val expenseViewModel: ExpenseViewModel = viewModel(
                factory = ExpenseViewModelFactory(app.expenseRepository)
            )
            AddExpenseScreen(
                viewModel = expenseViewModel,
                vehicleId = vehicleId,
                onExpenseAdded = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Screen.EditFuel.route,
            arguments = listOf(androidx.navigation.navArgument("fuelId") {
                type = androidx.navigation.NavType.IntType
            })
        ) { backStackEntry ->
            val fuelId = backStackEntry.arguments?.getInt("fuelId") ?: -1
            val fuelViewModel: FuelViewModel = viewModel(
                factory = FuelViewModelFactory(app.fuelLoadRepository, app.vehicleRepository)
            )
            AddFuelScreen(
                viewModel = fuelViewModel,
                vehicleId = -1, // Not needed for edit if we have fuelId
                fuelId = fuelId,
                onFuelAdded = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.EditMaintenance.route,
            arguments = listOf(androidx.navigation.navArgument("maintId") {
                type = androidx.navigation.NavType.IntType
            })
        ) { backStackEntry ->
            val maintId = backStackEntry.arguments?.getInt("maintId") ?: -1
            val maintenanceViewModel: MaintenanceViewModel = viewModel(
                factory = MaintenanceViewModelFactory(app.maintenanceRepository, app.vehicleRepository, app.reminderRepository)
            )
            AddMaintenanceScreen(
                viewModel = maintenanceViewModel,
                vehicleId = -1,
                maintenanceId = maintId,
                onMaintenanceAdded = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.EditExpense.route,
            arguments = listOf(androidx.navigation.navArgument("expenseId") {
                type = androidx.navigation.NavType.IntType
            })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getInt("expenseId") ?: -1
            val expenseViewModel: ExpenseViewModel = viewModel(
                factory = ExpenseViewModelFactory(app.expenseRepository)
            )
            AddExpenseScreen(
                viewModel = expenseViewModel,
                vehicleId = -1,
                expenseId = expenseId,
                onExpenseAdded = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.History.route,
            arguments = listOf(androidx.navigation.navArgument("vehicleId") {
                type = androidx.navigation.NavType.IntType
            })
        ) { backStackEntry ->
            val vehicleId = backStackEntry.arguments?.getInt("vehicleId") ?: -1
            val historyViewModel: HistoryViewModel = viewModel(
                factory = HistoryViewModelFactory(
                    app.fuelLoadRepository,
                    app.maintenanceRepository,
                    app.expenseRepository,
                    app.reminderRepository
                )
            )
            HistoryScreen(
                vehicleId = vehicleId,
                viewModel = historyViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditFuel = { fuelId ->
                    navController.navigate(Screen.EditFuel.createRoute(fuelId))
                },
                onEditMaint = { maintId ->
                    navController.navigate(Screen.EditMaintenance.createRoute(maintId))
                },
                onEditExp = { expenseId ->
                    navController.navigate(Screen.EditExpense.createRoute(expenseId))
                },
                onAddFuel = { vId ->
                    navController.navigate(Screen.AddFuel.createRoute(vId))
                },
                onAddMaint = { vId ->
                    navController.navigate(Screen.AddMaintenance.createRoute(vId))
                },
                onAddExp = { vId ->
                    navController.navigate(Screen.AddExpense.createRoute(vId))
                },
                onEditReminder = { reminderId ->
                    navController.navigate(Screen.EditReminder.createRoute(reminderId))
                }
            )
        }
        composable(
            route = Screen.EditReminder.route,
            arguments = listOf(androidx.navigation.navArgument("reminderId") {
                type = androidx.navigation.NavType.IntType
            })
        ) { backStackEntry ->
            val reminderId = backStackEntry.arguments?.getInt("reminderId") ?: -1
            val reminderViewModel: ReminderViewModel = viewModel(
                factory = ReminderViewModelFactory(app.reminderRepository)
            )
            EditReminderScreen(
                reminderId = reminderId,
                viewModel = reminderViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
