package com.example.ircctracker.ui.util

object TranslationHelper {
    private val englishMappings = mapOf(
        "INITIAL" to "Application Received",
        "AOR" to "Acknowledgement of Receipt",
        "BIOMETRICS" to "Biometrics Collection",
        "MED_REPORT" to "Medical Exam Request",
        "MED_RESULT" to "Medical Exam Results",
        "MED_PROOF" to "Medical Exam Proof Received",
        "SPR_UNDERTAKING" to "Sponsorship Application (IMM 1344)",
        "GEN_APPL_FORM" to "Generic Application Form (IMM 0008)",
        "FAMILY_INFO" to "Additional Family Information (IMM 5406)",
        "USE_OF_REP" to "Use of Representative (IMM 5476)",
        "Medical" to "Medical Exam Update",
        "Biometrics" to "Biometrics Update",
        "COPR_ISSUED" to "Confirmation of Permanent Residence (COPR) Issued",
        "DECISION" to "Decision Made",
        "DEFAULT" to "Update"
    )

    private val frenchMappings = mapOf(
        "INITIAL" to "Demande Reçue",
        "AOR" to "Accusé de Réception",
        "BIOMETRICS" to "Collecte de Biométrie",
        "MED_REPORT" to "Demande Exemen Médical",
        "MED_RESULT" to "Résultats Médicaux",
        "MED_PROOF" to "Preuve Médicale Reçue",
        "SPR_UNDERTAKING" to "Demande de Parrainage (IMM 1344)",
        "GEN_APPL_FORM" to "Formulaire Générique (IMM 0008)",
        "FAMILY_INFO" to "Info Famille (IMM 5406)",
        "USE_OF_REP" to "Représentant (IMM 5476)",
        "Medical" to "Mise à jour Médicale",
        "Biometrics" to "Mise à jour Biométrique",
        "COPR_ISSUED" to "Confirmation Résidence Permanente (CRP)",
        "DECISION" to "Décision Prise",
        "DEFAULT" to "Mise à jour"
    )

    fun translate(key: String): String {
        val isFrench = java.util.Locale.getDefault().language == "fr"
        val map = if (isFrench) frenchMappings else englishMappings

        // Direct Mapping
        map[key]?.let { return it }

        // Smart Formatter for IMM codes
        if (key.startsWith("IMM") || key.matches(Regex("\\d{4,5}"))) {
            // If it's just numbers, assume it's a form or ID
            val number = key.replace("IMM", "").trim()
            return if (isFrench) "Formulaire IMM $number" else "Form IMM $number"
        }

        // Fallback: Humanize generic keys (e.g. "background_check" -> "Background Check")
        return key.replace("_", " ")
            .lowercase()
            .replaceFirstChar { it.uppercase() }
    }

    fun getDescription(key: String): String? {
        val isFrench = java.util.Locale.getDefault().language == "fr"
        return if (isFrench) {
            when(key) {
                "INITIAL" -> "Nous avons reçu votre demande et vérifions sa conformité."
                "AOR" -> "Nous avons commencé le traitement de votre demande."
                "BIOMETRICS" -> "Nous avons besoin de vos empreintes et photo."
                "MED_REPORT" -> "Vous devez passer un examen médical."
                else -> null
            }
        } else {
            when(key) {
                "INITIAL" -> "We received your application and are checking completeness."
                "AOR" -> "We have started processing your application."
                "BIOMETRICS" -> "We need your fingerprints and photo."
                "MED_REPORT" -> "You need to complete a medical exam."
                else -> null
            }
        }
    }
}
