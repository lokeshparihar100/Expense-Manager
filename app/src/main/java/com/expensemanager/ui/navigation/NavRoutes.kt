package com.expensemanager.ui.navigation

/**
 * Navigation routes for the app
 */
sealed class NavRoutes(val route: String) {
    object Dashboard : NavRoutes("dashboard")
    object Transactions : NavRoutes("transactions")
    object AddTransaction : NavRoutes("add_transaction/{type}") {
        fun createRoute(type: String) = "add_transaction/$type"
    }
    object EditTransaction : NavRoutes("edit_transaction/{transactionId}") {
        fun createRoute(transactionId: Long) = "edit_transaction/$transactionId"
    }
    object Tags : NavRoutes("tags")
    object EditTag : NavRoutes("edit_tag/{tagType}/{tagId}") {
        fun createRoute(tagType: String, tagId: Long = -1) = "edit_tag/$tagType/$tagId"
    }
    object Settings : NavRoutes("settings")
    object SmsImport : NavRoutes("sms_import")
}
