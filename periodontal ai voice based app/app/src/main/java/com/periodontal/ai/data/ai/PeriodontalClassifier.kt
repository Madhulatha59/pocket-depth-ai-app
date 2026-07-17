package com.periodontal.ai.data.ai

import com.periodontal.ai.data.model.ToothMeasurement

data class ClassificationResult(
    val category: String,       // Healthy Gums, Gingivitis, Periodontitis (Mild/Moderate/Severe)
    val averageDepth: Float,
    val bleedingPercentage: Float,
    val severeSitesCount: Int,
    val description: String,
    val recommendations: List<String>
)

object PeriodontalClassifier {

    fun classify(measurements: List<ToothMeasurement>): ClassificationResult {
        if (measurements.isEmpty()) {
            return ClassificationResult(
                category = "No Data",
                averageDepth = 0f,
                bleedingPercentage = 0f,
                severeSitesCount = 0,
                description = "No probing data recorded yet.",
                recommendations = listOf("Begin manual or voice probing to analyze gum health.")
            )
        }

        var totalSites = 0
        var totalDepthSum = 0
        var bleedingSites = 0
        var pocketsFourOrMore = 0
        var pocketsFiveOrMore = 0
        var pocketsSixOrMore = 0

        measurements.forEach { tooth ->
            val sites = listOf(
                Pair(tooth.distalFacial, tooth.bleedingDF),
                Pair(tooth.facial, tooth.bleedingF),
                Pair(tooth.mesialFacial, tooth.bleedingMF),
                Pair(tooth.distalLingual, tooth.bleedingDL),
                Pair(tooth.lingual, tooth.bleedingL),
                Pair(tooth.mesialLingual, tooth.bleedingML)
            )

            sites.forEach { (depth, bled) ->
                totalSites++
                totalDepthSum += depth
                if (bled) bleedingSites++
                if (depth >= 6) pocketsSixOrMore++
                else if (depth >= 5) pocketsFiveOrMore++
                else if (depth >= 4) pocketsFourOrMore++
            }
        }

        val averageDepth = if (totalSites > 0) totalDepthSum.toFloat() / totalSites else 0f
        val bleedingPercentage = if (totalSites > 0) (bleedingSites.toFloat() / totalSites) * 100f else 0f
        val severeSites = pocketsSixOrMore + pocketsFiveOrMore + pocketsFourOrMore

        val category: String
        val description: String
        val recommendations = mutableListOf<String>()

        when {
            // Severe Periodontitis: Multiple sites with depth >= 6mm
            pocketsSixOrMore >= 2 || (pocketsSixOrMore + pocketsFiveOrMore) >= 5 -> {
                category = "Severe Periodontitis"
                description = "Multiple deep pocket depths detected (>= 6mm) with significant tissue/bone attachment loss risks."
                recommendations.addAll(listOf(
                    "URGENT: Schedule professional deep scaling and root planing (SRP).",
                    "Laser gum therapy or periodontal surgical evaluation may be required.",
                    "Antimicrobial rinses and specialized site-specific antibiotics.",
                    "Re-evaluation within 3 to 4 weeks by a Periodontist."
                ))
            }
            // Moderate Periodontitis: Pockets of 5mm
            (pocketsFiveOrMore + pocketsFourOrMore) >= 3 -> {
                category = "Moderate Periodontitis"
                description = "Moderate pocket depths (5mm) and attachment loss indicated. Action required to prevent progressive bone loss."
                recommendations.addAll(listOf(
                    "Schedule professional scaling and root planing (SRP).",
                    "Implement prescription antimicrobial mouthwashes (Chlorhexidine).",
                    "Utilize interdental brushes and water flossing daily.",
                    "Establish a 3-month periodontal maintenance recall schedule."
                ))
            }
            // Mild Periodontitis: Small pockets of 4mm
            pocketsFourOrMore >= 3 || (pocketsFourOrMore + pocketsFiveOrMore) >= 1 -> {
                category = "Mild Periodontitis"
                description = "Early-stage periodontitis detected. Pockets measuring 4mm indicate initial attachment loss."
                recommendations.addAll(listOf(
                    "Professional dental cleaning and localized scaling.",
                    "Reinforce strict twice-daily brushing and flossing technique.",
                    "Incorporate antiseptic mouthwash.",
                    "Schedule follow-up probing check in 3 months."
                ))
            }
            // Gingivitis: Normal pockets but high bleeding index (BOP >= 10%)
            bleedingPercentage >= 10f -> {
                category = "Gingivitis"
                description = "Active gum inflammation detected. Normal pocket depths (<= 3mm) but excessive bleeding on probing."
                recommendations.addAll(listOf(
                    "Schedule professional dental prophylaxis (cleaning).",
                    "Thorough daily flossing to clear interproximal plaque.",
                    "Use soft-bristled electric toothbrush twice daily.",
                    "Reversible with improved home care over 10-14 days."
                ))
            }
            // Healthy Gums: depths <= 3mm and bleeding < 10%
            else -> {
                category = "Healthy Gums"
                description = "Excellent periodontal health. Pocket depths are within normal limits (<= 3mm) and minimal bleeding."
                recommendations.addAll(listOf(
                    "Maintain current excellent brushing and flossing habits.",
                    "Continue routine 6-month dental checkups and cleanings.",
                    "Stay hydrated and maintain a balanced diet rich in vitamins C and D."
                ))
            }
        }

        return ClassificationResult(
            category = category,
            averageDepth = averageDepth,
            bleedingPercentage = bleedingPercentage,
            severeSitesCount = severeSites,
            description = description,
            recommendations = recommendations
        )
    }
}
