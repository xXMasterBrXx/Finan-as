package com.example.util

import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern

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

    private val BANK_PACKAGE_MAP = mapOf(
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
        "br.com.neon" to "Neon"
    )

    private val FINANCE_KEYWORDS = listOf(
        "compra", "aprovada", "pix", "transferencia", "transferência", "pagamento",
        "debito", "débito", "credito", "crédito", "cartao", "cartão", "recebeu",
        "recebido", "enviado", "enviou", "saque", "fatura", "gasto", "cobranca",
        "cobrança", "cashback", "estorno", "reembolso", "deposito", "depósito",
        "r$", "brl", "usd", "$"
    )

    fun isBankNotification(packageName: String, title: String, text: String): Boolean {
        if (BANK_PACKAGE_MAP.containsKey(packageName)) return true
        val combined = "$title $text".lowercase(Locale.getDefault())
        return FINANCE_KEYWORDS.any { combined.contains(it) }
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
                // e.g. 1.250,50 -> remove dot, replace comma with dot
                if (s.lastIndexOf(",") > s.lastIndexOf(".")) {
                    s = s.replace(".", "").replace(",", ".")
                } else {
                    // e.g. 1,250.50 -> remove comma
                    s = s.replace(",", "")
                }
            } else if (s.contains(",")) {
                // e.g. 150,00 -> replace comma with dot
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
        val incomeKeywords = listOf(
            "recebeu", "recebido", "pix recebido", "transferencia recebida",
            "credito", "deposito", "cashback", "reembolso", "estorno",
            "received", "credited", "salario", "proventos", "dividendo"
        )
        val isIncome = incomeKeywords.any { normalized.contains(it) }
        return if (isIncome) "INCOME" else "EXPENSE"
    }

    private fun extractMerchant(title: String, text: String, type: String, bankName: String): String {
        val full = "$title $text"
        val normalized = removeAccents(full.lowercase(Locale.getDefault()))

        // Patterns to match establishment / merchant / recipient
        val patterns = listOf(
            Regex("""(?:compra\s+aprovada\s+(?:no|na|em)|compra\s+(?:no|na|em))\s+([A-Za-z0-9\.\-\_\s\*]{3,35})(?:\s+no\s+valor|\s+de\s+R\$|\s+R\$|\s*$|\.)""", RegexOption.IGNORE_CASE),
            Regex("""(?:em|no|na)\s+([A-Za-z0-9\.\-\_\s\*]{3,35})\s+(?:no\s+valor|de\s+R\$|R\$)""", RegexOption.IGNORE_CASE),
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

        // Fallback: Use clean title or first meaningful words
        val cleanTitle = title.replace(bankName, "", ignoreCase = true).trim()
        if (cleanTitle.length in 3..40 && !cleanTitle.contains("R$")) {
            return cleanMerchantName(cleanTitle)
        }

        return if (type == "INCOME") "Transferência / Pix" else "Gasto no Cartão"
    }

    private fun isValidMerchantCandidate(candidate: String, bankName: String): Boolean {
        val lower = candidate.lowercase(Locale.getDefault())
        if (candidate.length < 2) return false
        if (lower.contains("valor") || lower.contains("reais") || lower.contains("cartao") || lower.contains("fatura")) return false
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
            Regex("""(?:cart[aã]o|final)\s*(\d{4})""", RegexOption.IGNORE_CASE),
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

    private fun removeAccents(str: String): String {
        val normalized = Normalizer.normalize(str, Normalizer.Form.NFD)
        return normalized.replace(Regex("""\p{InCombiningDiacriticalMarks}+"""), "")
    }
}
