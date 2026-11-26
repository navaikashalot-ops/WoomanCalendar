package com.example.mycycle

import android.app.Application
import com.example.mycycle.data.AppDatabase
import com.example.mycycle.data.AppRepository
import com.example.mycycle.data.BackupManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class CycleApp : Application() {
    lateinit var repo: AppRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.build(this)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        repo = AppRepository(db.dao(), BackupManager(this, db.dao()), scope)
        repo.start()
    }
}
