package com.example.ircctracker.util

import com.example.ircctracker.data.remote.Activities
import com.example.ircctracker.data.remote.HistoryEvent

enum class MilestoneStatus {
    PENDING,
    PASSED,
    UNKNOWN
}

data class ApplicationMilestones(
    val medical: MilestoneStatus = MilestoneStatus.UNKNOWN,
    val biometrics: MilestoneStatus = MilestoneStatus.UNKNOWN,
    val background: MilestoneStatus = MilestoneStatus.UNKNOWN,
    val eligibility: MilestoneStatus = MilestoneStatus.UNKNOWN,
    val lastUpdateDate: String? = null
)

class StatusParser {
    companion object {
        fun parse(activities: Activities?, events: List<HistoryEvent>?): ApplicationMilestones {
            var medical = MilestoneStatus.UNKNOWN
            var biometrics = MilestoneStatus.UNKNOWN
            var background = MilestoneStatus.UNKNOWN
            var eligibility = MilestoneStatus.UNKNOWN
            
            // 1. Prefer Direct Activities Data
            if (activities != null) {
                medical = mapStatus(activities.medical)
                biometrics = mapStatus(activities.biometrics)
                background = mapStatus(activities.background)
                eligibility = mapStatus(activities.eligibility)
            } else if (events != null) {
                 // 2. Fallback to parsing history if activities missing
                 events.forEach { event ->
                    val key = event.key.uppercase()
                    // Simple heuristic since we don't have status string in event
                    if (key.contains("MEDICAL") && key.contains("PASSED")) medical = MilestoneStatus.PASSED
                    if (key.contains("BIOMETRIC") && (key.contains("COMPLETED") || key.contains("RECEIVED"))) biometrics = MilestoneStatus.PASSED
                    if (key.contains("BACKGROUND") && key.contains("PASSED")) background = MilestoneStatus.PASSED
                    if (key.contains("ELIGIBILITY") && key.contains("PASSED")) eligibility = MilestoneStatus.PASSED
                }
            }

            var lastDate: String? = null
            if (!events.isNullOrEmpty()) {
                val sorted = events.sortedByDescending { it.dateCreated }
                lastDate = sorted.firstOrNull()?.dateCreated
            }

            return ApplicationMilestones(medical, biometrics, background, eligibility, lastDate)
        }

        private fun mapStatus(status: String?): MilestoneStatus {
            if (status == null) return MilestoneStatus.UNKNOWN
            val s = status.uppercase()
            return when {
                s.contains("PASSED") || s.contains("COMPLETED") || s.contains("MET") -> MilestoneStatus.PASSED
                s.contains("NOT STARTED") -> MilestoneStatus.UNKNOWN
                else -> MilestoneStatus.PENDING // Progress, Review, etc.
            }
        }
    }
}
