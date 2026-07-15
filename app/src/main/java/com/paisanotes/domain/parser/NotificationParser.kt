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
        "com.google.android.apps.nbu.paisa.user", // GPay
        "com.phonepe.app", // PhonePe
        "net.one97.paytm" // Paytm
    )

    fun parse(packageName: String, title: String, text: String): ParsedTransaction? {
        if (packageName !in validPackages) return null

        val fullText = "$title $text".lowercase()

        // Very basic Regex for demonstration.
        // Real-world Regex: /(?i)(?:paid|sent|rs\.?|inr)\s*[\s]*([0-9,]+(?:\.\d{1,2})?)\s*(?:to)\s*([a-z0-9\s]+)/

        try {
            // EXPENSE Matching: "Paid ₹500 to Starbucks"
            if (fullText.contains("paid") || fullText.contains("sent") || fullText.contains("debited")) {
                val amount = extractAmount(fullText)
                if (amount != null) {
                    return ParsedTransaction(amount, extractMerchant(text), "EXPENSE")
                }
            }

            // INCOME Matching: "Received ₹1000 from Rahul"
            if (fullText.contains("received") || fullText.contains("credited")) {
                val amount = extractAmount(fullText)
                if (amount != null) {
                    return ParsedTransaction(amount, extractMerchant(text), "INCOME")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null // Parsing failed or confidence too low
    }

    private fun extractAmount(text: String): Double? {
        // Find numbers following currency symbols or words
        val regex = Regex("(?:rs\\.?|inr|₹)\\s*([0-9,]+(?:\\.\\d{1,2})?)")
        val match = regex.find(text)
        return match?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
    }

    private fun extractMerchant(text: String): String {
        // Simplified merchant extraction (Just taking the raw text for now)
        // In a real app, you'd use capturing groups from regex.
        return if (text.length > 30) text.substring(0, 30) + "..." else text
    }
}