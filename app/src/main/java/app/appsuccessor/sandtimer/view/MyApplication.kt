package app.appsuccessor.sandtimer.view

import android.app.Application
import app.appsuccessor.sandtimer.datasource.local.AppDatabase
import app.appsuccessor.sandtimer.datasource.local.ClockDatabase

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.getInstance(this) // Initialize the Room database
        ClockDatabase.getInstance(this) // Initialize the Room database
    }
}
