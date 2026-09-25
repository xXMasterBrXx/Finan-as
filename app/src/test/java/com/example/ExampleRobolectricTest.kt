package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("BUMoney", appName)
  }

  @Test
  fun `test bank notification parser for purchases and pix`() {
    val nubankNotif = com.example.util.BankNotificationParser.parse(
        "com.nu.production",
        "Nubank",
        "Compra de R$ 89,90 aprovada no iFood com o cartão final 1234."
    )
    org.junit.Assert.assertNotNull(nubankNotif)
    assertEquals(89.90, nubankNotif!!.amount, 0.001)
    assertEquals("iFood", nubankNotif.merchant)
    assertEquals("EXPENSE", nubankNotif.type)
    assertEquals("1234", nubankNotif.cardLastFourDigits)

    val itauPix = com.example.util.BankNotificationParser.parse(
        "com.itau",
        "Itaú",
        "Você recebeu um Pix de R$ 250,00 de Carlos Silva."
    )
    org.junit.Assert.assertNotNull(itauPix)
    assertEquals(250.00, itauPix!!.amount, 0.001)
    assertEquals("Carlos Silva", itauPix.merchant)
    assertEquals("INCOME", itauPix.type)
  }
}
