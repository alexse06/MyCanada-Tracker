package com.example.ircctracker.data.local

/**
 * Static dictionary for official IRCC Processing Times (2024/2025 Estimates).
 * Replaces the AI "Crystal Ball" lookup to ensure 100% free and offline capability.
 *
 * Source: Official IRCC website & trend analysis (Jan 2026).
 */
object ProcessingTimeData {

    // Default fallback if no match found (Conservative estimate)
    private const val DEFAULT_MONTHS = 12

    private val staticTimes = mapOf(
        // --- PERMANENT RESIDENCE ---
        "Spouse" to 12, // Averaged Inland/Outland (10-13m)
        "Common-law" to 12,
        "Partner" to 12,
        "Sponsorship" to 12,
        "Express Entry" to 6,
        "Canadian Experience Class" to 5,
        "Federal Skilled Worker" to 6,
        "Provincial Nominee" to 8, // Non-Express Entry can be longer, but 8 is a safe median
        "Parents" to 24,
        "Grandparents" to 24,
        "Citizenship" to 7,
        "Card" to 2, // PR Card renewal/new (approx 60-70 days)

        // --- EXACT CODES (From API) ---
        "SW1-FED" to 6, // Federal Skilled Worker (Express Entry)
        "CEC" to 5,     // Canadian Experience Class
        "FST" to 6,     // Federal Skilled Trades
        "PNP" to 8,     // Provincial Nominee
        "FC1" to 12,    // Family Class (Spouse)
        "V-TR" to 5,    // Visitor

        // --- TEMPORARY RESIDENCE ---
        "Visitor" to 5, // ~154 days for online
        "Study" to 3,   // ~12 weeks
        "Work" to 4,    // ~16 weeks
        "Extension" to 4, // Visitor record/Work extension
        "Visa" to 1 // Simple TRV often faster depending on country, but safe placeholder
    )

    fun getEstimate(lob: String?): Int {
        if (lob.isNullOrBlank()) return DEFAULT_MONTHS

        // Normalize string: Trim & Uppercase to ensure matches
        val key = lob.trim().uppercase()

        // 1. Exact Match Check (using uppercase keys map if feasible, or iterating)
        // Since our map keys are mixed case, let's look for a match ignoring case
        staticTimes.entries.find { it.key.equals(key, ignoreCase = true) }?.let { return it.value }

        // 2. Keyword Search (Priority based)
        // Check specific types first
        if (key.contains("PARENTS") || key.contains("GRANDPARENTS")) return 24
        if (key.contains("SPOUSE") || key.contains("COMMON-LAW") || key.contains("SPONSORSHIP") || key.contains("FC1")) return 12
        if (key.contains("EXPRESS ENTRY") || key.contains("SKILLED WORKER") || key.contains("SW1") || key.contains("FST")) return 6
        if (key.contains("CANADIAN EXPERIENCE") || key.contains("CEC")) return 5
        if (key.contains("CITIZENSHIP")) return 7
        
        // Check temporary types
        if (key.contains("VISITOR") || key.contains("V-TR")) return 5
        if (key.contains("STUDY") || key.contains("STUDENT")) return 3
        if (key.contains("WORK")) return 4
        
        // Default
        return DEFAULT_MONTHS
    }
}
