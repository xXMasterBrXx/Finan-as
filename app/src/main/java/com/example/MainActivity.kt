package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.AppThemeColor
import com.example.ui.FinanceApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {
  private var financeViewModel: FinanceViewModel? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: FinanceViewModel = viewModel()
      financeViewModel = viewModel
      val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
      val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
      val customColorHex by viewModel.customThemeColorHex.collectAsStateWithLifecycle()

      MyApplicationTheme(
          themeMode = themeMode,
          themeColor = themeColor,
          customColorHex = customColorHex
      ) {
        FinanceApp(viewModel = viewModel)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    financeViewModel?.refreshNotificationListenerStatus()
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}
