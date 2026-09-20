package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.UserPreferences
import com.example.ui.viewmodel.FinanceViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FinanceSettingsTest {

    @Test
    fun `test user preferences persistence`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val prefs = UserPreferences.getInstance(app)

        // Test theme mode
        prefs.setThemeMode(AppThemeMode.DARK)
        assertEquals(AppThemeMode.DARK, prefs.themeMode.value)

        prefs.setThemeMode(AppThemeMode.LIGHT)
        assertEquals(AppThemeMode.LIGHT, prefs.themeMode.value)

        prefs.setThemeMode(AppThemeMode.LIGHT_WARM)
        assertEquals(AppThemeMode.LIGHT_WARM, prefs.themeMode.value)

        prefs.setThemeMode(AppThemeMode.DARK_OLED)
        assertEquals(AppThemeMode.DARK_OLED, prefs.themeMode.value)

        // Test hide balances
        prefs.setHideBalances(true)
        assertTrue(prefs.hideBalances.value)

        prefs.setHideBalances(false)
        assertFalse(prefs.hideBalances.value)

        // Test monthly budget limit
        prefs.setMonthlyBudgetLimit(3500.0)
        assertEquals(3500.0, prefs.monthlyBudgetLimit.value, 0.001)
    }

    @Test
    fun `test viewModel settings integration`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = FinanceViewModel(app)

        viewModel.setThemeMode(AppThemeMode.DARK)
        assertEquals(AppThemeMode.DARK, viewModel.themeMode.value)

        viewModel.setHideBalances(true)
        assertTrue(viewModel.hideBalances.value)

        viewModel.setMonthlyBudgetLimit(4000.0)
        assertEquals(4000.0, viewModel.monthlyBudgetLimit.value, 0.001)
    }
}
