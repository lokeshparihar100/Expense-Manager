package com.expensemanager

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Expense Manager
 * Initializes Hilt dependency injection
 */
@HiltAndroidApp
class ExpenseManagerApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Application initialization code here
    }
}
