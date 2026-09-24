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
        "com.nu.production" to "Nubank",
        "com.nu.pay" to "Nubank",
        "com.itau" to "Itaú",
        "com.itau.personnalite" to "Itaú Personnalité",
        "com.itau.card" to "Itaúcard",
        "br.com.intermedium" to "Banco Inter",
        "com.bradesco" to "Bradesco",
        "com.bradesco.cartoes" to "Bradesco Cartões",
        "br.com.bb.android" to "Banco do Brasil",
        "com.santander.app" to "Santander",
        "com.santander.app.way" to "Santander Way",
        "com.c6bank.app" to "C6 Bank",
        "br.gov.caixa.tem" to "Caixa Tem",
        "br.com.grupocaixa.cartoes" to "Caixa Cartões",
        "br.com.caixa" to "Caixa Econômica",
        "com.picpay" to "PicPay",
        "com.mercadopago.wallet" to "Mercado Pago",
        "br.com.uol.ps.myaccount" to "PagBank",
        "co.stone.app" to "Stone",
        "br.com.bradesco.next" to "Next",
        "com.btg.pactual.banking" to "BTG Pactual",
        "br.com.sicoob.mobile" to "Sicoob",
        "br.com.sicredi.mobile" to "Sicredi",
        "br.com.xp.wallet" to "XP Investimentos",
        "com.nomad.app" to "Nomad",
        "com.transferwise.android" to "Wise",
        "com.willbank.app" to "Will Bank",
        "br.com.neon" to "Neon",
        "com.google.android.apps.walletnfcrel" to "Google Carteira",
        "com.google.android.gms" to "Google Pay",
        "com.samsung.android.spay" to "Samsung Wallet"
    )

    // Known non-financial packages (retail, e-commerce, delivery, social) that should NEVER be parsed as banks
    val BLOCKED_PACKAGES = setOf(
        // E-commerce & Marketplaces
        "com.shopee.br",
        "com.mercadolibre",
        "com.mercadolibre.android",
        "com.mercadolivre",
        "com.mercadolivre.android",
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

        // Food & Delivery Promo Notifications
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

    // Keywords that indicate a promotional/marketing notification rather than a confirmed financial transaction
    private val PROMO_KEYWORDS = listOf(
        "cupom", "cupons", "oferta", "ofertas", "desconto", "descontos",
        "promocao", "promoção", "promocoes", "promoções", "aproveite",
        "compre agora", "compre e ganhe", "economize", "frete gratis",
        "frete grátis", "sem frete", "imperdivel", "imperdível",
        "só hoje", "so hoje", "tempo limitado", "ultimas horas",
        "últimas horas", "a partir de", "parcelas a partir", "sem juros",
        "novidade", "confira", "veja as ofertas", "clique aqui",
        "toque para ver", "não perca", "nao perca", "sorteio",
        "concorra", "prêmio", "premio", "limite pré-aprovado",
        "limite pre-aprovado", "empréstimo pré-aprovado", "emprestimo pre-aprovado",
        "peça já", "peca ja", "solicite já", "solicite ja", "leia mais",
        "faça seu pedido", "faca seu pedido", "carrinho", "esqueceu algo",
        "itens no carrinho", "avalie sua compra", "como foi sua entrega",
        "o que achou", "ganhe até", "ganhe ate", "resgate seu", "apenas r$",
        "por apenas", "use o código", "use o codigo"
    )

    // Patterns indicating a legitimate confirmed money movement (purchase, payment, pix, transfer)
    private val TRANSACTION_ACTION_PATTERNS = listOf(
        // Purchases & Cards
        Regex("""(?:compra\s+aprovada|compra\s+confirmada|compra\s+realizada)""", RegexOption.IGNORE_CASE),
        Regex("""(?:compra\s+no\s+(?:cr[eé]dito|d[eé]bito|cart[aã]o))""", RegexOption.IGNORE_CASE),
        Regex("""(?:compra\s+(?:no|na|em)\s+.*?\s+(?:no\s+valor|de\s+R\$|R\$))""", RegexOption.IGNORE_CASE),

        // Payments & Wallets
        Regex("""(?:voc[eê]\s+pagou|pagou\s+R\$|pagamento\s+(?:aprovado|realizado|efetuado|confirmado))""", RegexOption.IGNORE_CASE),
        Regex("""(?:pagamento\s+com\s+(?:google\s+pay|carteira|aproxima[cç][aã]o))""", RegexOption.IGNORE_CASE),
        Regex("""(?:pagamento\s+de\s+(?:boleto|conta|fatura))""", RegexOption.IGNORE_CASE),
        Regex("""(?:d[eé]bito\s+(?:aprovado|realizado|autorizado))""", RegexOption.IGNORE_CASE),

        // Pix & Transfers
        Regex("""(?:pix\s+(?:enviado|recebido|agendado|realizado|pago))""", RegexOption.IGNORE_CASE),
        Regex("""(?:voc[eê]\s+recebeu\s+um\s+pix|recebeu\s+um\s+pix|transfer[eê]ncia\s+(?:recebida|enviada|realizada))""", RegexOption.IGNORE_CASE),
        Regex("""(?:ted\s+(?:recebida|enviada)|doc\s+(?:recebido|enviado))""", RegexOption.IGNORE_CASE),

        // Income / Receipts
        Regex("""(?:dep[oó]sito\s+recebido|sal[aá]rio\s+creditado|valor\s+creditado|cashback\s+creditado|reembolso\s+recebido|estorno\s+(?:realizado|aprovado))""", RegexOption.IGNORE_CASE),

        // Google Wallet / Google Pay specific triggers
        Regex("""(?:Google\s+Pay|Carteira\s+do\s+Google|Google\s+Wallet)""", RegexOption.IGNORE_CASE),
        Regex("""(?:cart[aã]o\s+final\s*\d{4}\s*•)""", RegexOption.IGNORE_CASE),
        Regex("""(?:•{3,4}\s*\d{4})""", RegexOption.IGNORE_CASE)
    )

    fun isWalletPackage(packageName: String): Boolean {
        val lower = packageName.lowercase(Locale.getDefault())
        return lower == "com.google.android.apps.walletnfcrel" ||
                lower == "com.google.android.gms" ||
                lower == "com.samsung.android.spay"
    }

    fun isBankNotification(packageName: String, title: String, text: String): Boolean {
        val pkg = packageName.lowercase(Locale.getDefault())

        // 1. Block known non-banking / retail / shopping / delivery apps
        if (BLOCKED_PACKAGES.contains(pkg)) {
            return false
        }
        if (pkg.contains("shopee") || pkg.contains("mercadolibre") || pkg.contains("mercadolivre") ||
            pkg.contains("aliexpress") || pkg.contains("shein") || pkg.contains("magazineluiza") ||
            pkg.contains("americanas") || pkg.contains("casasbahia") || pkg.contains("ifood") ||
            pkg.contains("rappi") || pkg.contains("enjoei") || pkg.contains("olx") ||
            pkg.contains("kabum") || pkg.contains("dafitigroup") || pkg.contains("netshoes")
        ) {
            return false
        }

        val fullContent = "$title $text".lowercase(Locale.getDefault())
        val normalized = removeAccents(fullContent)

        // 2. Reject promotional / marketing keywords
        if (PROMO_KEYWORDS.any { normalized.contains(removeAccents(it)) }) {
            return false
        }

        // 3. For Google Play Services, ONLY accept if it's explicitly Google Pay / Wallet
        if (pkg == "com.google.android.gms") {
            val isGooglePay = fullContent.contains("google pay") ||
                    fullContent.contains("carteira") ||
                    fullContent.contains("você pagou") ||
                    fullContent.contains("voce pagou") ||
                    fullContent.contains("aproximação") ||
                    fullContent.contains("aproximacao")
            if (!isGooglePay) return false
        }

        // 4. Must match a legitimate financial transaction action pattern
        val matchesAction = TRANSACTION_ACTION_PATTERNS.any { it.containsMatchIn(fullContent) }
        if (!matchesAction) {
            return false
        }

        // 5. Must contain a valid money amount
        val amount = extractAmount(fullContent)
        if (amount == null || amount <= 0.0) {
            return false
        }

        // 6. Must be in known financial package map or have strong banking signal
        if (BANK_PACKAGE_MAP.containsKey(pkg)) {
            return true
        }

        return hasStrongBankingConfirmation(fullContent)
    }

    private fun hasStrongBankingConfirmation(text: String): Boolean {
        val normalized = removeAccents(text.lowercase(Locale.getDefault()))
        val strongSignals = listOf(
            "compra aprovada", "compra realizada", "compra confirmada",
            "pix enviado", "pix recebido", "recebeu um pix",
            "pagamento realizado", "pagamento aprovado", "pagamento efetuado",
            "debito aprovado", "debito realizado", "transferencia recebida",
            "transferencia enviada", "ted recebida", "saque realizado"
        )
        return strongSignals.any { normalized.contains(it) }
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

    private fun detectBankFromText(title: String, text: String): String {
        val combined = "$title $text".lowercase(Locale.getDefault())
        return when {
            combined.contains("nubank") -> "Nubank"
            combined.contains("itau") || combined.contains("itaú") -> "Itaú"
            combined.contains("banco inter") || combined.contains("inter") -> "Banco Inter"
            combined.contains("bradesco") -> "Bradesco"
            combined.contains("banco do brasil") || combined.contains("bb") -> "Banco do Brasil"
            combined.contains("santander") -> "Santander"
            combined.contains("c6") || combined.contains("c6bank") -> "C6 Bank"
            combined.contains("caixa") -> "Caixa"
            combined.contains("picpay") -> "PicPay"
            combined.contains("mercado pago") -> "Mercado Pago"
            combined.contains("pagbank") || combined.contains("pagseguro") -> "PagBank"
            combined.contains("stone") -> "Stone"
            combined.contains("next") -> "Next"
            combined.contains("btg") -> "BTG Pactual"
            combined.contains("sicoob") -> "Sicoob"
            combined.contains("sicredi") -> "Sicredi"
            combined.contains("xp") -> "XP Investimentos"
            combined.contains("nomad") -> "Nomad"
            combined.contains("wise") -> "Wise"
            combined.contains("will") || combined.contains("will bank") -> "Will Bank"
            combined.contains("neon") -> "Neon"
            combined.contains("carteira") || combined.contains("google pay") -> "Google Carteira"
            combined.contains("samsung pay") || combined.contains("samsung wallet") -> "Samsung Wallet"
            else -> "Notificação Bancária"
        }
    }

    private fun extractAmount(text: String): Double? {
        // Matches R$ 150,00 | R$150.00 | BRL 150,00 | USD 10,00 | 150,00 BRL | R$ 1.250,50
        val regexes = listOf(
            Regex("""(?:R\$\s*|USD\s*|EUR\s*|\$\s*|\bBRL\s*)([\d\.\,]+)""", RegexOption.IGNORE_CASE),
            Regex("""([\d\.\,]+)\s*(?:R\$|BRL|USD|EUR)""", RegexOption.IGNORE_CASE),
            Regex("""(?:de|valor\s+de|no\s+valor\s+de)\s*R?\$\s*([\d\.\,]+)""", RegexOption.IGNORE_CASE)
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

    private fun parseMoneyString(raw: String): Double? {
        try {
            var s = raw.trim()
            if (s.contains(",") && s.contains(".")) {
                if (s.lastIndexOf(",") > s.lastIndexOf(".")) {
                    s = s.replace(".", "").replace(",", ".")
                } else {
                    s = s.replace(",", "")
                }
            } else if (s.contains(",")) {
                s = s.replace(",", ".")
            }
            val parsed = s.toDoubleOrNull()
            return if (parsed != null && parsed < 1_000_000.0) Math.round(parsed * 100.0) / 100.0 else null
        } catch (e: Exception) {
            return null
        }
    }

    private fun extractType(text: String): String {
        val normalized = removeAccents(text.lowercase(Locale.getDefault()))

        // Explicit Expense keywords always take precedence (e.g. "compra no crédito", "pagamento com cartão de crédito")
        val expenseKeywords = listOf(
            "compra", "pagou", "pagamento", "gasto", "debito", "fatura", "saque", "tarifa"
        )
        if (expenseKeywords.any { normalized.contains(it) }) {
            return "EXPENSE"
        }

        val incomeKeywords = listOf(
            "recebeu", "recebido", "pix recebido", "transferencia recebida",
            "creditado", "credito em conta", "crédito em conta", "deposito", "cashback creditado", "reembolso", "estorno",
            "received", "credited", "salario", "proventos", "dividendo"
        )
        val isIncome = incomeKeywords.any { normalized.contains(it) }
        return if (isIncome) "INCOME" else "EXPENSE"
    }

    private fun extractMerchant(title: String, text: String, type: String, bankName: String): String {
        val full = "$title $text"

        // Patterns to match establishment / merchant / recipient
        val patterns = listOf(
            // Google Wallet: "Você pagou R$ 35,00 para Restaurante Solar"
            Regex("""(?:voc[eê]\s+pagou\s+R?\$?\s*[\d\.\,]+\s+(?:para|em))\s+([A-Za-z0-9\.\-\_\s\*]{2,35})""", RegexOption.IGNORE_CASE),
            // Google Wallet: "Você pagou R$ 35,00 com Nubank em Restaurante Solar"
            Regex("""(?:voc[eê]\s+pagou\s+.*?\s+(?:em|no|na|para))\s+([A-Za-z0-9\.\-\_\s\*]{2,35})""", RegexOption.IGNORE_CASE),
            // Google Wallet: "Restaurante Solar • R$ 35,00"
            Regex("""([A-Za-z0-9\.\-\_\s\*]{2,35})\s*•\s*R?\$?\s*[\d\.\,]+""", RegexOption.IGNORE_CASE),
            // Standard Purchases: "Compra aprovada no/na/em Restaurante Solar..."
            Regex("""(?:compra\s+aprovada\s+(?:no|na|em)|compra\s+(?:no|na|em))\s+([A-Za-z0-9\.\-\_\s\*]{3,35})(?:\s+no\s+valor|\s+de\s+R\$|\s+R\$|\s*$|\.)""", RegexOption.IGNORE_CASE),
            Regex("""(?:em|no|na)\s+([A-Za-z0-9\.\-\_\s\*]{3,35})\s+(?:no\s+valor|de\s+R\$|R\$)""", RegexOption.IGNORE_CASE),
            // Pix & Transfers
            Regex("""(?:pix\s+enviado\s+para|pix\s+para|transferencia\s+para|pagamento\s+para)\s+([A-Za-z0-9\.\-\_\s\*]{3,35})(?:\s+no\s+valor|\s+de\s+R\$|\s*$|\.)""", RegexOption.IGNORE_CASE),
            Regex("""(?:pix\s+recebido\s+de|pix\s+de|recebeu\s+um\s+pix\s+de|transferencia\s+de)\s+([A-Za-z0-9\.\-\_\s\*]{3,35})(?:\s+no\s+valor|\s+de\s+R\$|\s*$|\.)""", RegexOption.IGNORE_CASE),
            Regex("""(?:no|na|em)\s+([A-Za-z0-9\.\-\_\s\*]{3,30})""", RegexOption.IGNORE_CASE)
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
        val lower = candidate.lowercase(Locale.getDefault())
        if (candidate.length < 2) return false
        if (lower.contains("valor") || lower.contains("reais") || lower.contains("cartao") || lower.contains("fatura")) return false
        if (lower.contains("google pay") || lower.contains("carteira") || lower.contains("wallet") || lower.contains("google play")) return false
        if (lower.contains(bankName.lowercase(Locale.getDefault()))) return false
        return true
    }

    private fun cleanMerchantName(name: String): String {
        var clean = name.replace(Regex("""\b(R\$|\$\d+|BRL|USD|final\s*\d+)\b""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""\s+"""), " ")
            .trim()
        clean = clean.split("-", "*", "(").first().trim()
        return clean.take(35).ifBlank { "Estabelecimento" }
            .split(" ")
            .joinToString(" ") { word ->
                word.lowercase(Locale.getDefault()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
    }

    private fun extractCardLastDigits(text: String): String? {
        val regexes = listOf(
            Regex("""(?:cart[aã]o|final)\s*[:\s]*(\d{4})""", RegexOption.IGNORE_CASE),
            Regex("""(?:[•\*]{3,4}\s*|final\s*[:\s]*)(\d{4})""", RegexOption.IGNORE_CASE),
            Regex("""final\s*:\s*(\d{4})""", RegexOption.IGNORE_CASE)
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

        val foodKeywords = listOf("ifood", "uber eats", "rappi", "ze delivery", "restaurante", "padaria", "lanchonete", "acougue", "mercado", "supermercado", "carrefour", "pao de acucar", "extra", "assai", "atacadao", "mcdonald", "burger king", "outback", "subway", "coco bambu", "hortifruti", "conveniencia", "bar", "pizzaria", "cafeteria", "starbucks", "alimento", "refeicao")
        val transportKeywords = listOf("uber", "99", "99taxis", "posto", "shell", "ipiranga", "petrobras", "br", "sem parar", "veloe", "conectcar", "estacionamento", "garage", "combustivel", "gasolina", "etanol", "metro", "bus", "passagem", "taxi")
        val leisureKeywords = listOf("netflix", "spotify", "cinema", "kinoplex", "cinemark", "playstation", "psn", "xbox", "steam", "nintendo", "disney", "hbo", "prime video", "amazon prime", "choperia", "pub", "show", "ingresso", "sympla", "eventim", "jogos")
        val shoppingKeywords = listOf("amazon", "mercado livre", "mercadolivre", "shopee", "magalu", "magazine luiza", "casas bahia", "shein", "zara", "renner", "c&a", "riachuelo", "aliexpress", "loja", "varejo", "shopping")
        val healthKeywords = listOf("farmacia", "drogaria", "drogasil", "raia", "pague menos", "panvel", "hospital", "clinica", "consultorio", "odonto", "laboratorio", "fleury", "exame", "medico")
        val billsKeywords = listOf("luz", "enel", "cpfl", "cemig", "light", "agua", "sabesp", "sanepar", "copasa", "internet", "vivo", "claro", "tim", "oi", "gas", "condominio", "iptu", "ipva", "conta")
        val educationKeywords = listOf("curso", "udemy", "alura", "faculdade", "escola", "colegio", "universidade", "pearson", "idiomas")

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
