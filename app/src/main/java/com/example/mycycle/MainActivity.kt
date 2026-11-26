package com.example.mycycle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import com.example.mycycle.ui.theme.PeonyTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mycycle.ui.MainScreen

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PeonyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(vm)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        vm.flushBackup()
    }
}
