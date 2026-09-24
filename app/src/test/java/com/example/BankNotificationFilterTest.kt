package com.example

import com.example.util.BankNotificationParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BankNotificationFilterTest {

    @Test
    fun testRejectsEcommerceAndRetailNotifications() {
        // Shopee promo
        assertFalse(
            BankNotificationParser.isBankNotification(
                packageName = "com.shopee.br",
                title = "Shopee: Cupom imperdível!",
                text = "Use o cupom e ganhe R$ 20 de desconto em sua compra."
            )
        )

        // Mercado Livre promo
        assertFalse(
            BankNotificationParser.isBankNotification(
                packageName = "com.mercadolibre",
                title = "Ofertas no Mercado Livre",
                text = "Compre agora seu tênis por apenas R$ 99,90 com frete grátis."
            )
        )

        // Magazine Luiza promo
        assertFalse(
            BankNotificationParser.isBankNotification(
                packageName = "com.luizalabs.magazineluiza",
                title = "Magalu: Não perca!",
                text = "Itens no carrinho te esperando a partir de R$ 19,90."
            )
        )

        // AliExpress promo
        assertFalse(
            BankNotificationParser.isBankNotification(
                packageName = "com.alibaba.aliexpresshd",
                title = "AliExpress Super Ofertas",
                text = "Descontos de até R$ 50 na sua primeira compra."
            )
        )
    }

    @Test
    fun testRejectsPromotionalKeywordsEvenFromUnknownApps() {
        assertFalse(
            BankNotificationParser.isBankNotification(
                packageName = "com.random.app",
                title = "Grande Promoção",
                text = "Aproveite já descontos incríveis de R$ 30,00 só hoje!"
            )
        )

        assertFalse(
            BankNotificationParser.isBankNotification(
                packageName = "com.nu.production",
                title = "Oferta especial de empréstimo",
                text = "Você tem um limite pré-aprovado de R$ 5.000 para contratar."
            )
        )
    }

    @Test
    fun testAcceptsLegitimateBankNotifications() {
        // Nubank purchase
        assertTrue(
            BankNotificationParser.isBankNotification(
                packageName = "com.nu.production",
                title = "Compra aprovada",
                text = "Compra de R$ 45,90 aprovada em Padaria São Bento no crédito"
            )
        )

        val parsedNu = BankNotificationParser.parse(
            packageName = "com.nu.production",
            title = "Compra aprovada",
            text = "Compra de R$ 45,90 aprovada em Padaria São Bento no crédito"
        )
        assertNotNull(parsedNu)
        assertEquals(45.90, parsedNu!!.amount, 0.001)
        assertEquals("EXPENSE", parsedNu.type)
        assertEquals("Alimentação", parsedNu.category)

        // Itaú purchase with card digits
        assertTrue(
            BankNotificationParser.isBankNotification(
                packageName = "com.itau",
                title = "Itaú Cartões",
                text = "Compra aprovada no cartão final 1234 em Posto Shell no valor de R$ 120,00"
            )
        )

        val parsedItau = BankNotificationParser.parse(
            packageName = "com.itau",
            title = "Itaú Cartões",
            text = "Compra aprovada no cartão final 1234 em Posto Shell no valor de R$ 120,00"
        )
        assertNotNull(parsedItau)
        assertEquals(120.00, parsedItau!!.amount, 0.001)
        assertEquals("1234", parsedItau.cardLastFourDigits)
        assertEquals("Transporte", parsedItau.category)
    }

    @Test
    fun testAcceptsAndParsesGoogleWalletNotifications() {
        // Google Wallet NFC payment
        assertTrue(
            BankNotificationParser.isBankNotification(
                packageName = "com.google.android.apps.walletnfcrel",
                title = "Google Pay",
                text = "Você pagou R$ 32,50 para Restaurante Solar"
            )
        )

        val parsedWallet = BankNotificationParser.parse(
            packageName = "com.google.android.apps.walletnfcrel",
            title = "Google Pay",
            text = "Você pagou R$ 32,50 para Restaurante Solar"
        )
        assertNotNull(parsedWallet)
        assertEquals(32.50, parsedWallet!!.amount, 0.001)
        assertEquals("Restaurante Solar", parsedWallet.merchant)
        assertEquals("EXPENSE", parsedWallet.type)
        assertEquals("Google Carteira", parsedWallet.bankName)
    }

    @Test
    fun testGooglePlayServicesGeneralNotificationsAreIgnored() {
        assertFalse(
            BankNotificationParser.isBankNotification(
                packageName = "com.google.android.gms",
                title = "Serviços do Google Play",
                text = "Atualização concluída com sucesso."
            )
        )
    }
}
