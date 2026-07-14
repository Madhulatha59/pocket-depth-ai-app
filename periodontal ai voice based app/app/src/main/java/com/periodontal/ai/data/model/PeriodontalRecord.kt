package com.periodontal.ai.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

data class ToothMeasurement(
    val toothNumber: Int,
    val distalFacial: Int = 3,
    val facial: Int = 3,
    val mesialFacial: Int = 3,
    val distalLingual: Int = 3,
    val lingual: Int = 3,
    val mesialLingual: Int = 3,
    val bleedingDF: Boolean = false,
    val bleedingF: Boolean = false,
    val bleedingMF: Boolean = false,
    val bleedingDL: Boolean = false,
    val bleedingL: Boolean = false,
    val bleedingML: Boolean = false
) {
    // Utility to serialize to simple string for Room storage
    fun toSerializedString(): String {
        return "$toothNumber,$distalFacial,$facial,$mesialFacial,$distalLingual,$lingual,$mesialLingual," +
                "${if (bleedingDF) 1 else 0},${if (bleedingF) 1 else 0},${if (bleedingMF) 1 else 0}," +
                "${if (bleedingDL) 1 else 0},${if (bleedingL) 1 else 0},${if (bleedingML) 1 else 0}"
    }

    companion object {
        fun fromSerializedString(str: String): ToothMeasurement {
            val parts = str.split(",")
            return ToothMeasurement(
                toothNumber = parts[0].toInt(),
                distalFacial = parts[1].toInt(),
                facial = parts[2].toInt(),
                mesialFacial = parts[3].toInt(),
                distalLingual = parts[4].toInt(),
                lingual = parts[5].toInt(),
                mesialLingual = parts[6].toInt(),
                bleedingDF = parts[7] == "1",
                bleedingF = parts[8] == "1",
                bleedingMF = parts[9] == "1",
                bleedingDL = parts[10] == "1",
                bleedingL = parts[11] == "1",
                bleedingML = parts[12] == "1"
            )
        }
    }
}

@Entity(tableName = "periodontal_records")
data class PeriodontalRecord(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val patientId: String,
    val serializedMeasurements: String, // toothMeasurements joined by ";"
    val aiClassification: String,       // Healthy, Gingivitis, Periodontitis
    val averagePocketDepth: Float,
    val bleedingOnProbingPercentage: Float,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    // Helper to get structured measurements list
    fun getMeasurements(): List<ToothMeasurement> {
        if (serializedMeasurements.isEmpty()) return emptyList()
        return try {
            serializedMeasurements.split(";").map { ToothMeasurement.fromSerializedString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        fun createSerialized(measurements: List<ToothMeasurement>): String {
            return measurements.joinToString(";") { it.toSerializedString() }
        }
    }
}
