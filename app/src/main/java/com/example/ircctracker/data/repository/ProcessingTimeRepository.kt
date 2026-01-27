package com.example.ircctracker.data.repository

class ProcessingTimeRepository(private val geminiRepository: GeminiRepository? = null) {

    // Mock data based on typical processing times (in months)
    private val processingTimes = mapOf(
        "CIT" to 14, // Citizenship
        "PR" to 12,  // Permanent Residence (Generic)
        "CEC" to 6,  // Canadian Experience Class
        "FSW" to 6,  // Federal Skilled Worker
        "SP" to 20,  // Sponsorship (Parents/Grandparents)
        "SPO" to 12, // Sponsorship (Spouse)
        "TR" to 3,   // Temporary Residence (Visitor)
        "WP" to 4,   // Work Permit
        "SP" to 2    // Study Permit
    )

    suspend fun getEstimatedMonths(lob: String?): Int {
        if (lob == null) return 12
        
        // Try AI fetch first if repo is available
        if (geminiRepository != null) {
            val aiTime = geminiRepository.getProcessingTime(getLobDescription(lob))
            if (aiTime != null && aiTime > 0) {
                return aiTime
            }
        }
        
        // Fallback to static map
        return processingTimes[lob] 
            ?: processingTimes.entries.find { lob.contains(it.key, ignoreCase = true) }?.value
            ?: 12
    }
    
    fun getLobDescription(lob: String?): String {
       return when(lob) {
           "CIT" -> "Citizenship"
           "PR" -> "Permanent Residence"
           "CEC" -> "Canadian Experience Class"
           else -> lob ?: "Application"
       }
    }
}
