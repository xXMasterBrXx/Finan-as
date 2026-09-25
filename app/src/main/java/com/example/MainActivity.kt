package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.AppThemeColor
import com.example.ui.FinanceApp
import com.example.ui.components.BiometricLockScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.BiometricHelper

class MainActivity : FragmentActivity() {
  private var financeViewModel: FinanceViewModel? = null
  private var backgroundTimestamp = 0L

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: FinanceViewModel = viewModel()
      financeViewModel = viewModel
      val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
      val themeColor by viewModel.themeColor.collectAsStateWithLifecycle()
      val customColorHex by viewModel.customThemeColorHex.collectAsStateWithLifecycle()

      val isBiometricEnabled by viewModel.isBiometricAuthEnabled.collectAsStateWithLifecycle()
      val isAppUnlocked by viewModel.isAppUnlocked.collectAsStateWithLifecycle()
      val biometricErrorMessage by viewModel.biometricErrorMessage.collectAsStateWithLifecycle()

      // When biometric is enabled and locked, prevent screenshots and task switcher previews
      if (isBiometricEnabled && !isAppUnlocked) {
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
      } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
      }

      MyApplicationTheme(
          themeMode = themeMode,
          themeColor = themeColor,
          customColorHex = customColorHex
      ) {
        if (isBiometricEnabled && !isAppUnlocked) {
          BiometricLockScreen(
              errorMessage = biometricErrorMessage,
              onUnlockClicked = { promptBiometricUnlock() }
          )
        } else {
          FinanceApp(
              viewModel = viewModel,
              onPromptBiometric = { onSuccess ->
                promptBiometricConfirmation(onSuccess)
              },
              onCheckBiometricAvailability = {
                BiometricHelper.checkAvailability(this)
              }
          )
        }
      }
    }
  }

  private fun promptBiometricUnlock() {
    BiometricHelper.showBiometricPrompt(
        activity = this,
        title = "BUMoney - Desbloqueio",
        subtitle = "Confirme sua identidade para acessar o painel financeiro",
        description = "Use sua impressão digital, reconhecimento facial ou senha do dispositivo",
        onSuccess = {
          financeViewModel?.unlockApp()
        },
        onError = { errorCode, errString ->
          if (errorCode != 10 && errorCode != 13) {
            financeViewModel?.setBiometricErrorMessage(errString)
          } else {
            financeViewModel?.setBiometricErrorMessage("Toque no botão para desbloquear")
          }
        },
        onFailed = {
          financeViewModel?.setBiometricErrorMessage("Biometria não reconhecida. Tente novamente.")
        }
    )
  }

  private fun promptBiometricConfirmation(onSuccess: () -> Unit) {
    BiometricHelper.showBiometricPrompt(
        activity = this,
        title = "Confirmação de Segurança",
        subtitle = "Confirme sua biometria para alterar as configurações de segurança",
        description = "Confirmação com digital, face ou senha do dispositivo",
        onSuccess = {
          onSuccess()
        },
        onError = { _, _ -> },
        onFailed = {}
    )
  }

  override fun onResume() {
    super.onResume()
    financeViewModel?.refreshNotificationListenerStatus()
  }

  override fun onStop() {
    super.onStop()
    backgroundTimestamp = System.currentTimeMillis()
  }

  override fun onStart() {
    super.onStart()
    if (backgroundTimestamp > 0L) {
      val elapsed = System.currentTimeMillis() - backgroundTimestamp
      if (elapsed > 2000L && financeViewModel?.isBiometricAuthEnabled?.value == true) {
        financeViewModel?.lockApp()
      }
    }
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
