package com.paisanotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.paisanotes.presentation.transactions.TransactionsScreen
import dagger.hilt.android.AndroidEntryPoint

// 🚨 CRITICAL: Tells Hilt to inject dependencies into this Activity
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Apply standard Material 3 Theme (You can customize colors in ui/theme later)
            MaterialTheme {
                Surface {
                    // Load our brand new screen!
                    TransactionsScreen(
                        onNavigateToAddTransaction = {
                            // TODO: We will implement Type-Safe Navigation next!
                        }
                    )
                }
            }
        }
    }
}