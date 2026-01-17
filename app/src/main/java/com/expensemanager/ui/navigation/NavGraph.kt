package com.expensemanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.expensemanager.ui.screens.*

/**
 * Main navigation graph for the app
 */
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Dashboard.route
    ) {
        // Dashboard
        composable(route = NavRoutes.Dashboard.route) {
            DashboardScreen(
                onNavigateToTransactions = {
                    navController.navigate(NavRoutes.Transactions.route)
                },
                onNavigateToAddExpense = {
                    navController.navigate(NavRoutes.AddTransaction.createRoute("expense"))
                },
                onNavigateToAddIncome = {
                    navController.navigate(NavRoutes.AddTransaction.createRoute("income"))
                },
                onNavigateToSmsImport = {
                    navController.navigate(NavRoutes.SmsImport.route)
                },
                onNavigateToTags = {
                    navController.navigate(NavRoutes.Tags.route)
                },
                onNavigateToEditTransaction = { transactionId ->
                    navController.navigate(NavRoutes.EditTransaction.createRoute(transactionId))
                }
            )
        }
        
        // Transactions List
        composable(route = NavRoutes.Transactions.route) {
            TransactionsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddExpense = {
                    navController.navigate(NavRoutes.AddTransaction.createRoute("expense"))
                },
                onNavigateToAddIncome = {
                    navController.navigate(NavRoutes.AddTransaction.createRoute("income"))
                },
                onNavigateToEditTransaction = { transactionId ->
                    navController.navigate(NavRoutes.EditTransaction.createRoute(transactionId))
                }
            )
        }
        
        // Add Transaction
        composable(
            route = NavRoutes.AddTransaction.route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val transactionType = backStackEntry.arguments?.getString("type") ?: "expense"
            AddEditTransactionScreen(
                transactionType = transactionType,
                transactionId = null,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddTag = { tagType ->
                    navController.navigate(NavRoutes.EditTag.createRoute(tagType))
                }
            )
        }
        
        // Edit Transaction
        composable(
            route = NavRoutes.EditTransaction.route,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
            AddEditTransactionScreen(
                transactionType = "expense", // Will be determined from loaded transaction
                transactionId = transactionId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddTag = { tagType ->
                    navController.navigate(NavRoutes.EditTag.createRoute(tagType))
                }
            )
        }
        
        // Tags Management
        composable(route = NavRoutes.Tags.route) {
            TagsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditTag = { tagType, tagId ->
                    navController.navigate(NavRoutes.EditTag.createRoute(tagType, tagId))
                }
            )
        }
        
        // Edit Tag
        composable(
            route = NavRoutes.EditTag.route,
            arguments = listOf(
                navArgument("tagType") { type = NavType.StringType },
                navArgument("tagId") { 
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val tagType = backStackEntry.arguments?.getString("tagType") ?: "CATEGORY"
            val tagId = backStackEntry.arguments?.getLong("tagId") ?: -1L
            EditTagScreen(
                tagType = tagType,
                tagId = if (tagId == -1L) null else tagId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // SMS Import
        composable(route = NavRoutes.SmsImport.route) {
            SmsImportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
