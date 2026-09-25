package com.example.util

import java.text.Normalizer
import java.util.Locale

data class ParsedBankNotification(
    val packageName: String,
    val bankName: String,
    val rawTitle: String,
    val rawText: String,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val merchant: String,
    val category: String,
    val cardLastFourDigits: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

object BankNotificationParser {

    val BANK_PACKAGE_MAP = mapOf(
        // Nubank
        "com.nu.production" to "Nubank",
        "com.nu.pay" to "Nubank",
        "com.nubank" to "Nubank",

        // Itaú
        "com.itau" to "Itaú",
        "com.itau.personnalite" to "Itaú Personnalité",
        "com.itau.card" to "Itaúcard",
        "com.itau.empresas" to "Itaú Empresas",
        "com.iti.mobile" to "Iti Itaú",

        // Banco Inter
        "br.com.intermedium" to "Banco Inter",
        "br.com.intermedium.invest" to "Banco Inter",
        "br.com.inter" to "Banco Inter",

        // Bradesco & Next
        "com.bradesco" to "Bradesco",
        "com.bradesco.cartoes" to "Bradesco Cartões",
        "br.com.bradesco.next" to "Next",

        // Banco do Brasil
        "br.com.bb.android" to "Banco do Brasil",
        "br.com.bb" to "Banco do Brasil",

        // Santander
        "com.santander.app" to "Santander",
        "com.santander.app.way" to "Santander Way",

        // C6 Bank
        "com.c6bank.app" to "C6 Bank",
        "br.com.c6bank" to "C6 Bank",

        // Caixa
        "br.gov.caixa.tem" to "Caixa Tem",
        "br.com.grupocaixa.cartoes" to "Caixa Cartões",
        "br.com.caixa" to "Caixa Econômica",

        // PicPay
        "com.picpay" to "PicPay",

        // Mercado Pago
        "com.mercadopago.wallet" to "Mercado Pago",
        "com.mercadopago" to "Mercado Pago",

        // PagBank / PagSeguro
        "br.com.uol.ps.myaccount" to "PagBank",
        "com.pagseguro.pagbank" to "PagBank",

        // Stone / Ton
        "co.stone.app" to "Stone",
        "br.com.stone.ton" to "Ton",

        // BTG Pactual
        "com.btg.pactual.banking" to "BTG Pactual",

        // Cooperativas (Sicoob, Sicredi)
        "br.com.sicoob.mobile" to "Sicoob",
        "br.com.sicredi.mobile" to "Sicredi",

        // XP & Investimentos
        "br.com.xp.wallet" to "XP Investimentos",
        "br.com.xp" to "XP Investimentos",
        "com.rico.investimentos" to "Rico",
        "com.clarita" to "Clear",

        // Digitais & Internacionais
        "com.nomad.app" to "Nomad",
        "com.transferwise.android" to "Wise",
        "com.willbank.app" to "Will Bank",
        "br.com.neon" to "Neon",
        "br.com.neon.android" to "Neon",
        "com.sofisa.direto" to "Sofisa Direto",
        "br.com.pan.mobile" to "Banco Pan",
        "com.bancobmg.bmgcard" to "Banco BMG",
        "com.daycoval.mobile" to "Banco Daycoval",
        "com.infinitepay" to "InfinitePay",
        "com.recargapay" to "RecargaPay",
        "com.claropay" to "Claro Pay",
        "com.agibank" to "Agibank",
        "com.safra.mobile" to "Banco Safra",
        "br.com.banrisul" to "Banrisul",

        // Wallets
        "com.google.android.apps.walletnfcrel" to "Google Carteira",
        "com.google.android.gms" to "Google Pay",
        "com.samsung.android.spay" to "Samsung Wallet"
    )

    // Packages to explicitly ignore (Social, Messaging, Browsers, Media, Retail/Food Delivery promo notifications)
    val BLOCKED_PACKAGES = setOf(
        // E-commerce & Marketplaces
        "com.shopee.br",
        "com.mercadolibre",
        "com.mercadolibre.android",
        "com.alibaba.aliexpresshd",
        "com.aliexpress",
        "com.amazon.mShop.android.shopping",
        "br.com.magazineluiza",
        "com.luizalabs.magazineluiza",
        "com.shein.fashion",
        "com.b2w.americanas",
        "br.com.casasbahia",
        "br.com.pontofrio",
        "br.com.extra",
        "com.enjoei.app",
        "com.olx.southamerica",
        "com.kabum",
        "com.dafitigroup",
        "br.com.netshoes.app",
        "com.centauro",
        "com.contextlogic.wish",

        // Delivery Apps (Food Promos)
        "com.ifood",
        "delivery.ifood.com",
        "com.ifood.customer",
        "com.ubercab.eats",
        "com.rappi",
        "com.zedelivery.pedidos",
        "com.aiqfome",

        // Social, Messaging, Media & Browsers
        "com.whatsapp",
        "com.whatsapp.w4b",
        "org.telegram.messenger",
        "com.instagram.android",
        "com.facebook.katana",
        "com.facebook.orca",
        "com.zhiliaoapp.musically",
        "com.twitter.android",
        "com.discord",
        "com.netflix.mediaclient",
        "com.spotify.music",
        "com.google.android.youtube",
        "com.android.chrome"
    )

    // Keywords that indicate pure marketing/promo notifications, not actual money movements
    private val PROMO_KEYWORDS = listOf(
        "cupom", "cupons", "oferta", "ofertas", "desconto", "descontos",
        "promocao", "promoção", "promocoes", "promoções", "aproveite",
        "compre e ganhe", "economize", "frete gratis", "frete grátis",
        "sem frete", "imperdivel", "imperdível", "so hoje", "só hoje",
        "ultimas horas", "últimas horas", "a partir de", "sem juros",
        "veja as ofertas", "clique aqui", "toque para ver", "sorteio",
        "concorra", "limite pre-aprovado", "limite pré-aprovado",
        "emprestimo pre-aprovado", "empréstimo pré-aprovado", "faca seu emprestimo",
        "solicite ja seu cartao", "peca ja seu cartao", "convide amigos e ganhe",
        "carrinho", "esqueceu algo", "itens no carrinho", "como foi sua entrega",
        "o que achou", "use o codigo", "use o código"
    )

    // Financial action terms that indicate money activity
    private val FINANCIAL_ACTION_KEYWORDS = listOf(
        "compra", "comprou", "compras", "comprado",
        "aprovada", "aprovado", "confirmada", "confirmado",
        "realizada", "realizado", "efetuada", "efetuado",
        "autorizada", "autorizado", "processada", "processado",
        "pix", "transferencia", "transferência", "transferiu", "transferido", "ted", "doc",
        "pagou", "pagamento", "pago", "pagar",
        "recebeu", "recebido", "recebimento", "creditado", "credito em conta", "crédito em conta",
        "debito", "débito", "debitado",
        "cartao", "cartão",
        "fatura", "boleto",
        "estorno", "reembolso", "cashback",
        "saque",
        "gasto", "gastou",
        "carteira", "google pay", "samsung pay", "apple pay", "wallet"
    )

    fun isWalletPackage(packageName: String): Boolean {
        val lower = packageName.lowercase(Locale.getDefault())
        return lower == "com.google.android.apps.walletnfcrel" ||
                lower == "com.google.android.gms" ||
                lower == "com.samsung.android.spay"
    }

    fun isBankNotification(packageName: String, title: String, text: String): Boolean {
        val pkg = packageName.lowercase(Locale.getDefault())

        // 1. Never parse blocked apps (messaging, social, pure retail promo)
        if (BLOCKED_PACKAGES.contains(pkg)) {
            return false
        }

        val fullContent = "$title $text".lowercase(Locale.getDefault())
        val normalized = removeAccents(fullContent)

        // 2. Reject pure marketing/promotional notifications
        if (PROMO_KEYWORDS.any { normalized.contains(removeAccents(it)) }) {
            return false
        }

        // 3. For Google Play Services, ONLY accept if it's Google Pay / Wallet related
        if (pkg == "com.google.android.gms") {
            val isGooglePay = normalized.contains("google pay") ||
                    normalized.contains("carteira") ||
                    normalized.contains("voce pagou") ||
                    normalized.contains("aproximacao")
            if (!isGooglePay) return false
        }

        // 4. Must contain a valid monetary amount
        val amount = extractAmount(fullContent)
        if (amount == null || amount <= 0.0) {
            return false
        }

        // 5. Must contain at least one financial action trigger
        val hasFinancialAction = FINANCIAL_ACTION_KEYWORDS.any { normalized.contains(removeAccents(it)) }
        if (!hasFinancialAction) {
            return false
        }

        // 6. Check if known bank package or banking related
        if (BANK_PACKAGE_MAP.containsKey(pkg)) {
            return true
        }

        val isLikelyBanking = pkg.contains("bank") ||
                pkg.contains("banco") ||
                pkg.contains("cartao") ||
                pkg.contains("wallet") ||
                pkg.contains("pagamento") ||
                pkg.contains("finance") ||
                detectBankFromText(title, text) != "Notificação Bancária"

        return isLikelyBanking
    }

    fun parse(packageName: String, title: String, text: String, subtext: String? = null): ParsedBankNotification? {
        val bankName = BANK_PACKAGE_MAP[packageName] ?: detectBankFromText(title, text)
        val fullContent = listOfNotNull(title, text, subtext).joinToString(" ")
        if (fullContent.isBlank()) return null

        val amount = extractAmount(fullContent) ?: return null
        val type = extractType(fullContent)
        val merchant = extractMerchant(title, text, type, bankName)
        val category = suggestCategory(merchant, fullContent, type)
        val cardLastDigits = extractCardLastDigits(fullContent)

        return ParsedBankNotification(
            packageName = packageName,
            bankName = bankName,
            rawTitle = title,
            rawText = text,
            amount = amount,
            type = type,
            merchant = merchant,
            category = category,
            cardLastFourDigits = cardLastDigits
        )
    }

    fun detectBankFromText(title: String, text: String): String {
        val combined = "$title $text".lowercase(Locale.getDefault())
        return when {
            combined.contains("nubank") || combined.contains("nuconta") -> "Nubank"
            combined.contains("itau") || combined.contains("itaú") -> "Itaú"
            combined.contains("banco inter") || combined.contains("inter") -> "Banco Inter"
            combined.contains("bradesco") -> "Bradesco"
            combined.contains("banco do brasil") || combined.contains("ourocard") || combined.contains("bb") -> "Banco do Brasil"
            combined.contains("santander") -> "Santander"
            combined.contains("c6") || combined.contains("c6bank") -> "C6 Bank"
            combined.contains("caixa") -> "Caixa Econômica"
            combined.contains("picpay") -> "PicPay"
            combined.contains("mercado pago") -> "Mercado Pago"
            combined.contains("pagbank") || combined.contains("pagseguro") -> "PagBank"
            combined.contains("stone") || combined.contains("ton") -> "Stone"
            combined.contains("next") -> "Next"
            combined.contains("btg") -> "BTG Pactual"
            combined.contains("sicoob") -> "Sicoob"
            combined.contains("sicredi") -> "Sicredi"
            combined.contains("xp") || combined.contains("xp investimentos") -> "XP Investimentos"
            combined.contains("nomad") -> "Nomad"
            combined.contains("wise") -> "Wise"
            combined.contains("will") || combined.contains("will bank") -> "Will Bank"
            combined.contains("neon") -> "Neon"
            combined.contains("carteira") || combined.contains("google pay") -> "Google Carteira"
            combined.contains("samsung pay") || combined.contains("samsung wallet") -> "Samsung Wallet"
            else -> "Notificação Bancária"
        }
    }

    fun extractAmount(text: String): Double? {
        val regexes = listOf(
            // Matches R$ 150,00 | R$150.00 | BRL 150,00 | USD 10,00 | $ 150.00
            Regex("""(?:R\$\s*|USD\s*|EUR\s*|\$\s*|\bBRL\s*)([\d\.\,]+)""", RegexOption.IGNORE_CASE),
            // Matches 150,00 R$ | 150,00 reais | 150 reais
            Regex("""([\d\.\,]+)\s*(?:R\$|BRL|reais\b|USD|EUR)""", RegexOption.IGNORE_CASE),
            // Matches valor: R$ 150,00 | valor de 150,00 | no valor de 150,00 | total de 150,00 | de R$ 89,90
            Regex("""(?:valor\s*[:de\s]+|no\s+valor\s+de\s+|total\s*[:de\s]+|de\s+R\$\s*|de\s+)([\d\.\,]+)""", RegexOption.IGNORE_CASE),
            // Matches • R$ 35,00 or • 35,00
            Regex("""•\s*(?:R\$\s*)?([\d\.\,]+)""")
        )

        for (regex in regexes) {
            val match = regex.find(text)
            if (match != null) {
                val rawVal = match.groupValues[1]
                val cleaned = parseMoneyString(rawVal)
                if (cleaned != null && cleaned > 0.0) {
                    return cleaned
                }
            }
        }
        return null
    }

    fun parseMoneyString(raw: String): Double? {
        try {
            var s = raw.trim().trim {
                it == '.' || it == ',' || it == ';' || it == ':' || it == '!' || it == '?' || it == ')' || it == ']' || it == '(' || it == '['
            }
            if (s.isBlank()) return null

            // If it contains both comma and dot: e.g. "1.250,50" (BR) or "1,250.50" (US)
            if (s.contains(",") && s.contains(".")) {
                val lastComma = s.lastIndexOf(",")
                val lastDot = s.lastIndexOf(".")
                if (lastComma > lastDot) {
                    s = s.replace(".", "").replace(",", ".")
                } else {
                    s = s.replace(",", "")
                }
            } else if (s.contains(",")) {
                s = s.replace(",", ".")
            }

            val parsed = s.toDoubleOrNull() ?: return null
            if (parsed in 0.01..100_000_000.0) {
                return Math.round(parsed * 100.0) / 100.0
            }
            return null
        } catch (e: Exception) {
            return null
        }
    }

    fun extractType(text: String): String {
        val normalized = removeAccents(text.lowercase(Locale.getDefault()))

        // Explicit Expense keywords always take precedence (e.g. "compra no crédito", "pagamento com cartão")
        val expenseKeywords = listOf(
            "compra", "comprou", "pagou", "pagamento", "gasto", "gastou",
            "debito", "fatura", "saque", "tarifa", "transferiu", "pix enviado", "enviado"
        )
        if (expenseKeywords.any { normalized.contains(it) }) {
            return "EXPENSE"
        }

        val incomeKeywords = listOf(
            "recebeu", "recebido", "pix recebido", "transferencia recebida",
            "creditado", "credito em conta", "deposito", "cashback", "reembolso", "estorno",
            "salario", "proventos", "dividendo", "rendimento"
        )
        val isIncome = incomeKeywords.any { normalized.contains(it) }
        return if (isIncome) "INCOME" else "EXPENSE"
    }

    fun extractMerchant(title: String, text: String, type: String, bankName: String): String {
        val full = "$title $text"

        val patterns = listOf(
            // Google Wallet: "Restaurante Solar • R$ 35,00"
            Regex("""([A-Za-z0-9\.\-\_\s\*]{2,35})\s*•\s*R?\$?""", RegexOption.IGNORE_CASE),

            // Itaú style: "Compra aprovada no seu Itaucard final 1234 - LOJA XYZ R$ 89,90"
            Regex("""-\s*([A-Za-z0-9\.\-\_\s\*]{2,35})\s+R?\$?[\d\.\,]+""", RegexOption.IGNORE_CASE),

            // "Você pagou R$ 35,00 para/em Restaurante Solar"
            Regex("""(?:voc[eê]\s+pagou|pagou)\s+(?:R?\$?\s*[\d\.\,]+\s+)?(?:para|em|no|na)\s+([A-Za-z0-9\.\-\_\s\*]{2,35})""", RegexOption.IGNORE_CASE),

            // "Você recebeu um Pix de R$ 250,00 de Carlos Silva" / "Pix recebido de Carlos Silva" / "Pix de Carlos Silva"
            Regex("""(?:recebeu\s+um\s+pix|pix\s+recebido|transfer[eê]ncia\s+recebida|pix)\s+(?:de\s+R?\$?\s*[\d\.\,]+\s+)?de\s+([A-Za-z0-9\.\-\_\s\*]{2,40})""", RegexOption.IGNORE_CASE),

            // "Pix enviado para Carlos Silva" / "Você fez um Pix de R$ 50,00 para Carlos Silva" / "Pix para Carlos Silva"
            Regex("""(?:pix\s+enviado|transfer[eê]ncia\s+enviada|pix|transferiu|pagou)\s+(?:de\s+R?\$?\s*[\d\.\,]+\s+)?(?:para|a)\s+([A-Za-z0-9\.\-\_\s\*]{2,40})""", RegexOption.IGNORE_CASE),

            // "Compra de R$ 89,90 aprovada no/na/em iFood com o cartão final 1234."
            Regex("""aprovad[ao]\s+(?:no|na|em)\s+([A-Za-z0-9\.\-\_\s\*]+?)(?:\s+com\s+o\s+cart|\s+no\s+cart|\s+no\s+cr[eé]d|\s+no\s+d[eé]b|\s+via\s+pix|\s+no\s+valor|\s+de\s+R\$|\s+R\$|\.|$|,|;)""", RegexOption.IGNORE_CASE),

            // "Compra de R$ 89,90 no/na/em iFood"
            Regex("""compra\s+(?:de\s+R?\$?\s*[\d\.\,]+\s+)?(?:no|na|em)\s+([A-Za-z0-9\.\-\_\s\*]+?)(?:\s+aprovad|\s+com\s+o\s+cart|\s+no\s+cart|\s+no\s+cr[eé]d|\s+no\s+d[eé]b|\s+via\s+pix|\s+no\s+valor|\s+de\s+R\$|\s+R\$|\.|$|,|;)""", RegexOption.IGNORE_CASE),

            // "Compra aprovada em/no/na LOJA"
            Regex("""compra\s+aprovada\s+(?:no|na|em)\s+([A-Za-z0-9\.\-\_\s\*]+?)(?:\s+com\s+o\s+cart|\s+no\s+cart|\s+no\s+cr[eé]d|\s+no\s+d[eé]b|\s+via\s+pix|\s+no\s+valor|\s+de\s+R\$|\s+R\$|\.|$|,|;)""", RegexOption.IGNORE_CASE),

            // General "em/no/na [Estabelecimento]" followed by value or card or end
            Regex("""(?:no|na|em)\s+([A-Za-z0-9\.\-\_\s\*]{2,35})(?:\s+no\s+valor|\s+de\s+R\$|\s+R\$|\s+com\s+o\s+cart|\s+no\s+cart|\s*$|\.)""", RegexOption.IGNORE_CASE),

            // "para [Nome]"
            Regex("""(?:para|ao?)\s+([A-Za-z0-9\.\-\_\s\*]{3,35})(?:\s+no\s+valor|\s+de\s+R\$|\s+R\$|\s*$|\.)""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(full)
            if (match != null) {
                val candidate = match.groupValues[1].trim()
                if (isValidMerchantCandidate(candidate, bankName)) {
                    return cleanMerchantName(candidate)
                }
            }
        }

        // Fallback: Use clean title if it represents the establishment
        val cleanTitle = title.replace(bankName, "", ignoreCase = true)
            .replace("Google Pay", "", ignoreCase = true)
            .replace("Google Wallet", "", ignoreCase = true)
            .replace("Carteira", "", ignoreCase = true)
            .trim()
        if (cleanTitle.length in 3..40 && !cleanTitle.contains("R$") && isValidMerchantCandidate(cleanTitle, bankName)) {
            return cleanMerchantName(cleanTitle)
        }

        return if (type == "INCOME") "Transferência / Pix" else "Gasto no Cartão"
    }

    private fun isValidMerchantCandidate(candidate: String, bankName: String): Boolean {
        val lower = removeAccents(candidate.lowercase(Locale.getDefault())).trim()
        if (lower.length < 2) return false
        if (lower.startsWith("r$") || lower.matches(Regex("""^[\d\.,\s]+$"""))) return false
        if (lower.contains("valor") || lower.contains("reais") || lower.contains("fatura")) return false
        if (lower == "cartao" || lower == "credito" || lower == "debito" || lower == "conta corrente") return false
        if (lower.contains("google pay") || lower.contains("carteira") || lower.contains("google play")) return false
        val bankLower = removeAccents(bankName.lowercase(Locale.getDefault())).trim()
        if (bankLower.isNotEmpty() && lower == bankLower) return false
        return true
    }

    private fun cleanMerchantName(name: String): String {
        var clean = name
            .replace(Regex("""\s+(?:com\s+o\s+cart[a-zA-Z0-9áéíóúÁÉÍÓÚ]*|com\s+cart[a-zA-Z0-9áéíóúÁÉÍÓÚ]*).*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s+no\s+cart[a-zA-Z0-9áéíóúÁÉÍÓÚ]*.*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s+no\s+cr[eé]dito.*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s+no\s+d[eé]bito.*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s+via\s+pix.*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s+com\s+sucesso.*""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\b(R\$|\$\d+|BRL|USD|final\s*\d+)\b""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s+"""), " ")
            .trim()

        clean = clean.split("-", "*", "(", "•").first().trim()
        clean = clean.trim { it == '.' || it == ',' || it == '-' || it == ':' || it == '/' }
        val candidate = clean.take(35).ifBlank { "Estabelecimento" }

        val isAllUpper = candidate.all { !it.isLetter() || it.isUpperCase() }
        val isAllLower = candidate.all { !it.isLetter() || it.isLowerCase() }

        return if (isAllUpper || isAllLower) {
            candidate.split(" ")
                .filter { it.isNotBlank() }
                .joinToString(" ") { word ->
                    if (word.equals("ifood", ignoreCase = true)) "iFood"
                    else word.lowercase(Locale.getDefault()).replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    }
                }
        } else {
            candidate
        }
    }

    fun extractCardLastDigits(text: String): String? {
        val regexes = listOf(
            Regex("""final\s*[:\s]*(\d{4})""", RegexOption.IGNORE_CASE),
            Regex("""cart[aã]o\s+(?:com\s+)?final\s*[:\s]*(\d{4})""", RegexOption.IGNORE_CASE),
            Regex("""cart[aã]o\s*[:\s]*(\d{4})""", RegexOption.IGNORE_CASE),
            Regex("""(?:•{3,4}|\*{3,4})\s*(\d{4})""", RegexOption.IGNORE_CASE),
            Regex("""terminad[ao]\s+em\s*(\d{4})""", RegexOption.IGNORE_CASE),
            Regex("""ending\s+in\s*(\d{4})""", RegexOption.IGNORE_CASE)
        )
        for (regex in regexes) {
            val match = regex.find(text)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }

    fun suggestCategory(merchant: String, fullText: String, type: String): String {
        if (type == "INCOME") {
            val combined = "$merchant $fullText".lowercase(Locale.getDefault())
            return when {
                combined.contains("salario") || combined.contains("salário") || combined.contains("pagamento de salario") -> "Salário"
                combined.contains("freelance") || combined.contains("extra") -> "Freelance / Extra"
                combined.contains("investimento") || combined.contains("dividendo") || combined.contains("rendimento") -> "Investimentos"
                combined.contains("presente") || combined.contains("bonus") || combined.contains("bônus") -> "Presente / Bônus"
                else -> "Outras Entradas"
            }
        }

        val text = removeAccents("$merchant $fullText".lowercase(Locale.getDefault()))

        val foodKeywords = listOf(
            "ifood", "uber eats", "rappi", "ze delivery", "restaurante", "padaria", "lanchonete",
            "acougue", "mercado", "supermercado", "carrefour", "pao de acucar", "extra", "assai",
            "atacadao", "mcdonald", "burger king", "outback", "subway", "coco bambu", "hortifruti",
            "conveniencia", "bar", "pizzaria", "cafeteria", "starbucks", "alimento", "refeicao"
        )
        val transportKeywords = listOf(
            "uber", "99", "99taxis", "posto", "shell", "ipiranga", "petrobras", "br", "sem parar",
            "veloe", "conectcar", "estacionamento", "garage", "combustivel", "gasolina", "etanol",
            "metro", "bus", "passagem", "taxi"
        )
        val leisureKeywords = listOf(
            "netflix", "spotify", "cinema", "kinoplex", "cinemark", "playstation", "psn", "xbox",
            "steam", "nintendo", "disney", "hbo", "prime video", "amazon prime", "choperia", "pub",
            "show", "ingresso", "sympla", "eventim", "jogos"
        )
        val shoppingKeywords = listOf(
            "amazon", "mercado livre", "mercadolivre", "shopee", "magalu", "magazine luiza",
            "casas bahia", "shein", "zara", "renner", "c&a", "riachuelo", "aliexpress", "loja",
            "varejo", "shopping"
        )
        val healthKeywords = listOf(
            "farmacia", "drogaria", "drogasil", "raia", "pague menos", "panvel", "hospital",
            "clinica", "consultorio", "odonto", "laboratorio", "fleury", "exame", "medico"
        )
        val billsKeywords = listOf(
            "luz", "enel", "cpfl", "cemig", "light", "agua", "sabesp", "sanepar", "copasa",
            "internet", "vivo", "claro", "tim", "oi", "gas", "condominio", "iptu", "ipva", "conta"
        )
        val educationKeywords = listOf(
            "curso", "udemy", "alura", "faculdade", "escola", "colegio", "universidade", "pearson", "idiomas"
        )

        return when {
            foodKeywords.any { text.contains(it) } -> "Alimentação"
            transportKeywords.any { text.contains(it) } -> "Transporte"
            leisureKeywords.any { text.contains(it) } -> "Lazer"
            shoppingKeywords.any { text.contains(it) } -> "Compras"
            healthKeywords.any { text.contains(it) } -> "Saúde"
            billsKeywords.any { text.contains(it) } -> "Contas & Fixas"
            educationKeywords.any { text.contains(it) } -> "Educação"
            else -> "Outras Saídas"
        }
    }

    fun removeAccents(str: String): String {
        val normalized = Normalizer.normalize(str, Normalizer.Form.NFD)
        return normalized.replace(Regex("""\p{InCombiningDiacriticalMarks}+"""), "")
    }
}
