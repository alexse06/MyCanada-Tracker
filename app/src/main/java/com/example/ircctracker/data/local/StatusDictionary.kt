package com.example.ircctracker.data.local

/**
 * A static dictionary of common IRCC status codes and their meanings.
 * This acts as a first line of defense to explain statuses without requiring an AI API Key.
 *
 * Sources: Official IRCC Glossary, Tracker, and Community Crowdsourcing (Reddit/Forums).
 */
object StatusDictionary {

    private val dictionary: Map<String, String> = mapOf(
        // --- MEDICAL ---
        "Medical Passed" to "Great news! IRCC has received and approved your medical exam results. You meet the health requirements.",
        "Passed" to "The assessment for this section has been successfully completed and approved.",
        "Medical Completed" to "Your medical exam results have been processed.",
        "Medical In Progress" to "IRCC is currently reviewing your medical exam results.",
        "Biometrics Completed" to "Your fingerprints and photo have been received and added to your file.",
        "Biometrics Exempt" to "You do not need to provide biometrics for this application.",
        
        // --- BACKGROUND / SECURITY ---
        "Background Verification Not Started" to "Security checks haven't begun yet. This is normal; they often happen last.",
        "Background Verification In Progress" to "IRCC is verifying your history (criminality, security). This is usually the longest step.",
        "Background Check Completed" to "Security and background screening is finished. You are very close to a decision!",
        "Security Screening" to "A detailed check by partners (CBSA/CSIS) to ensure admissibility.",
        
        // --- ELIGIBILITY ---
        "Eligibility Review Required" to "An officer needs to manually review your file to confirm you meet the programs requirements.",
        "Eligibility Passed" to "You met the core requirements for the program (e.g., points, work experience).",
        "Eligibility In Progress" to "An officer is actively calculating your eligibility (points, requirements).",
        
        // --- APPLICATION STATUS ---
        "Application Received" to "IRCC has your application and is checking if it is complete (R10 check).",
        "AOR" to "Acknowledgement of Receipt: IRCC has officially opened your file.",
        "Ghost Update" to "A background update happened (often a system sync) with no visible change. Usually means 'someone touched your file'.",
        
        // --- DECISION ---
        "Decision Made" to "A final decision has been reached. Watch your email/account for the official letter.",
        "PPR" to "Passport Request: The 'Golden Email'! IRCC is asking for your passport to issue the visa/COPR.",
        "COPR" to "Confirmation of Permanent Residence: Your document proving you are approved as a permanent resident.",
        "Refused" to "Unfortunately, the application was denied. You will receive a letter explaining why.",
        "Withdrawn" to "The application was cancelled by you or by IRCC.",
        
        // --- NUMERIC CODES (Tracker Specific) ---
        "54" to "Your biometrics information has been successfully received.",
        "47" to "Your medical exam results have been received.",
        "42" to "The background check for your application has started.",
        "86" to "A security screening for your application is in progress.",
        "9" to "Your application has moved to the final decision stage.",
        "5" to "Your application is currently being processed.",
        "57" to "Your biometrics information has been successfully received (System Update).",
        "873" to "Your eligibility assessment is currently being reviewed.",
        
        // --- IMM FORMS & DOCUMENTS ---
        "Formulaire IMM 5756" to "Biometrics Instruction Letter. You need to give fingerprints.",
        "Formulaire IMM 5794" to "Acknowledgement of Receipt (AOR). Official start of processing.",
        "IMM 5756" to "Biometrics Instruction Letter. You need to give fingerprints.",
        "IMM 5794" to "Acknowledgement of Receipt (AOR). Official start of processing.",
        "IMM 5406" to "Additional Family Information Request.",
        "IMM 5669" to "Schedule A: Background / Declaration Request.",
        "IMM 5257" to "Application for Visitor Visa / Temporary Resident Visa.",
        "IMM 5476" to "Use of Representative Form update.",
        "IMM 1017" to "Medical Examination Report Instructions.",
        "IMM 5801" to "Passport Request (PPR) or Visa Issuance.",

        // --- FRENCH / BILINGUAL SPECIFIC ---
        "Mise à jour Médicale" to "Statut médical mis à jour / Medical status updated.",
        "Demande Reçue" to "Application officielle reçue dans le système.",
        "Reçue" to "Application received. IRCC is checking for completeness.",
        "En cours" to "In progress. IRCC is reviewing your application.",
        "Fermée" to "Closed. A final decision has been made (Approved/Refused/Withdrawn).",
        "Non commencé" to "Not started. Review for this section hasn't begun.",
        "En attente" to "Waiting on you. IRCC needs more info/docs.",
        "Terminé" to "Completed. Review for this section is finished.",
        "Exempt" to "Exempted. You don't need to complete this.",
        "Refusée" to "Refused. Application denied.",
        "Retirée" to "Withdrawn. Application cancelled by you or IRCC.",
        "Abandonnée" to "Abandoned. Application closed due to lack of response.",
        "Vérification des antécédents" to "Background Check. Security/Criminality check in progress.",
        "Admissibilité" to "Eligibility. Checking if you meet program requirements.",
        "Examen médical" to "Medical Exam. Health review.",
        "Biométrie" to "Biometrics. Fingerprints and photo.",
        "Statut de la demande" to "Application Status.",

        // --- RAW KEYS (Before Translation) ---
        "Medical" to "Great news! IRCC has received and approved your medical exam results.",
        "Biometrics" to "Your fingerprints and photo have been received.",
        "AOR" to "Acknowledgement of Receipt. Official start of processing.",
        "INITIAL" to "Application received. IRCC is checking for completeness.",
        "DECISION" to "A final decision has been reached. Watch for email.",
        "MED_RESULT" to "Your medical exam results have been processed.",
        "MED_PROOF" to "Proof of medical exam received.",
        "SPR_UNDERTAKING" to "Sponsorship Application (IMM 1344) status.",
        "GEN_APPL_FORM" to "Generic Application Form (IMM 0008) status.",
        "FAMILY_INFO" to "Additional Family Information (IMM 5406) request.",
        "USE_OF_REP" to "Representative (IMM 5476) update.",
        "COPR_ISSUED" to "Congratulations! Confirmation of Permanent Residence issued.",

        // --- IMM NUMBERS (Raw) ---
        "5756" to "Biometrics Instruction Letter. You need to give fingerprints.",
        "5794" to "Acknowledgement of Receipt (AOR). Official start of processing.",
        "5406" to "Additional Family Information Request.",
        "5669" to "Schedule A: Background / Declaration Request.",
        "5257" to "Application for Visitor Visa / Temporary Resident Visa.",
        "5476" to "Use of Representative Form update.",
        "1017" to "Medical Examination Report Instructions.",
        "5801" to "Passport Request (PPR) or Visa Issuance.",
        "1344" to "Application to Sponsor, Sponsorship Agreement and Undertaking.",
        "0008" to "Generic Application Form for Canada.",
        // New from Screenshot 2
        "5791" to "Temporary Resident Visa (TRV) application check.",
        "10003" to "Pre-arrival services info / Employment strategy letter.",
        "10002" to "Pre-arrival services info / Settlement services letter.",
        "5787" to "Express Entry Profile: Confirmation of eligibility.",
        "5786" to "Express Entry Profile: Creation confirmation.",
        
        // --- IMM NUMBERS (Translated Keys for Safety) ---
        "IMM 5791" to "Temporary Resident Visa (TRV) application check.",
        "IMM 10003" to "Pre-arrival services info / Employment strategy letter.",
        "IMM 10002" to "Pre-arrival services info / Settlement services letter.",
        "IMM 5787" to "Express Entry Profile: Confirmation of eligibility.",
        "IMM 5786" to "Express Entry Profile: Creation confirmation.",

        // --- COMMON ENGLISH (Expanded) ---
        "Waived" to "Exempted. This requirement does not apply to you.",
        "Cancelled" to "IRCC had to recreate your application due to an error.",
        "Delayed" to "More time is needed to review your application.",
        "Waiting on you" to "Action required from you (e.g., submit docs).",
        "In progress" to "Currently being reviewed by an officer.",
        "Completed" to "This step is finished.",
        "Exempted" to "Requirement waived for your case.",

        // --- MISC ---
        "Met" to "Requirement fulfilled.",
        "Not Met" to "Requirement not fulfilled.",
        "Review Required" to "Complex case flag. An officer must manually assess a specific document or claim.",
        "Invitation to Apply" to "You have been selected from the pool and can now submit your full application."
    )

    /**
     * Attempts to find a definition for the given status code.
     * Performs a normalized lookup (ignoring case and some punctuation).
     */
    fun getExplanation(statusCode: String): String? {
        // 1. Direct Match
        dictionary[statusCode]?.let { return it }

        // 2. Case-insensitive Match
        val normalizedCode = statusCode.trim()
        val match = dictionary.entries.find { it.key.equals(normalizedCode, ignoreCase = true) }
        match?.let { return it.value }

        // 3. Partial Match (use with caution, maybe for "Medical" or "Background")
        // Prefer explicit keys to avoid bad guesses, so skipping broad partial matching for now.
        
        return null
    }

    /**
     * Returns the full dictionary for debugging or UI lists.
     */
    fun getAllDefinitions(): Map<String, String> = dictionary
}
