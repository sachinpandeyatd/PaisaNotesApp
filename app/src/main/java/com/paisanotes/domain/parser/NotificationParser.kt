package com.paisanotes.domain.parser

import javax.inject.Inject

data class ParsedTransaction(
    val amount: Double,
    val merchant: String?,
    val type: String // INCOME or EXPENSE
)

class NotificationParser @Inject constructor() {

    // Target specific apps
    private val validPackages = listOf(
        // UPI Apps
        "com.google.android.apps.nbu.paisa.user", // GPay
        "com.phonepe.app",                        // PhonePe
        "net.one97.paytm",                        // Paytm
        "in.amazon.mShop.android.shopping",       // Amazon Pay
        "com.csam.icici.bank.imobile",            // iMobile (ICICI)
        "com.sbi.SBIFreedomPlus",                 // YONO SBI

        // SMS Apps (Crucial for Credit Cards & Banks)
        "com.google.android.apps.messaging",      // Google Messages
        "com.samsung.android.messaging",          // Samsung Messages
        "com.truecaller",                         // Truecaller (Very popular for SMS in India)
        "com.motorola.messaging",                 // Moto Messages

        // Email Apps
        "com.google.android.gm",                  // Gmail
        "com.microsoft.office.outlook"            // Outlook
    )

    fun parse(packageName: String, title: String, text: String): ParsedTransaction? {
        if (packageName !in validPackages) return null

        val fullText = "$title $text".lowercase()
        val isFinancial = listOf("rs", "inr", "₹", "debited", "credited", "spent", "paid", "received", "a/c", "acct").any { fullText.contains(it) }

        if (!isFinancial) return null

        try {
            // --- EXPENSE MATCHING ---
            // Keywords: debited, spent, paid, deducted, sent
            val expenseKeywords = listOf("debited", "spent", "paid", "deducted", "sent", "withdrawal")
            if (expenseKeywords.any { fullText.contains(it) }) {
                val amount = extractAmount(fullText)
                if (amount != null) {
                    return ParsedTransaction(
                        amount = amount,
                        merchant = extractMerchant(text, "EXPENSE"),
                        type = "EXPENSE"
                    )
                }
            }

            // --- INCOME MATCHING ---
            // Keywords: credited, received, deposited, refunded
            val incomeKeywords = listOf("credited", "received", "deposited", "refunded")
            if (incomeKeywords.any { fullText.contains(it) }) {
                val amount = extractAmount(fullText)
                if (amount != null) {
                    return ParsedTransaction(
                        amount = amount,
                        merchant = extractMerchant(text, "INCOME"),
                        type = "INCOME"
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null // Parsing failed or confidence too low
    }

    private fun extractAmount(text: String): Double? {
        // Find numbers following currency symbols or words
        val regex = Regex("(?i)(?:rs\\.?|inr|₹)\\s*([0-9,]+(?:\\.\\d{1,2})?)")
        val match = regex.find(text)
        return match?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
    }

    private fun extractMerchant(text: String, type: String): String {
        val lowerText = text.lowercase()

        // Attempt to find who it was paid to (e.g., "paid to Starbucks", "spent at Amazon", "transfer to Rahul")
        if (type == "EXPENSE") {
            val toMatch = Regex("(?i)(?:to|at|info\\*)\\s+([a-zA-Z0-9\\s]+)").find(lowerText)
            if (toMatch != null && toMatch.groupValues.size > 1) {
                return toMatch.groupValues[1].trim().take(30).uppercase()
            }
        } else if (type == "INCOME") {
            val fromMatch = Regex("(?i)(?:from|by)\\s+([a-zA-Z0-9\\s]+)").find(lowerText)
            if (fromMatch != null && fromMatch.groupValues.size > 1) {
                return fromMatch.groupValues[1].trim().take(30).uppercase()
            }
        }

        // Fallback: If we can't cleanly extract the merchant, just return a snippet of the text
        return if (text.length > 30) text.substring(0, 30) + "..." else text
    }
}