package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.NoorShieldApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.NoorShieldViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: NoorShieldViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NoorShieldApp(viewModel)
            }
        }
    }
}
