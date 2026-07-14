package com.example.pocketdepthai.util

import kotlin.math.exp
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ModelWeights(
    val W: List<List<Float>> = List(4) { List(4) { 0f } },
    val b: List<Float> = List(4) { 0f },
    val accuracy: Float = 95f,
    val samplesCount: Int = 40,
    val trainedAt: String = ""
) {
    // Check if the weights are empty/zeroed out
    fun isZero(): Boolean {
        return W.all { row -> row.all { it == 0f } } && b.all { it == 0f }
    }
}

data class TrainingSample(
    val meanPpd: Float,
    val meanCal: Float,
    val bopPercentage: Float,
    val deepPocketsCount: Int,
    val diagnosisClass: Int
)

data class PredictionResult(
    val success: Boolean,
    val verdict: String,
    val stageAndGrade: String,
    val probHealthy: Int,
    val probModerate: Int,
    val probSevere: Int,
    val modelVersion: String
)

object AIEngine {

    // Pre-seeded base training samples from train_model.php
    val baseTrainingSamples = listOf(
        // Class 0: Healthy Periodontium
        TrainingSample(1.5f, 0.2f, 5.0f, 0, 0),
        TrainingSample(1.8f, 0.5f, 8.0f, 0, 0),
        TrainingSample(2.0f, 0.8f, 10.0f, 0, 0),
        TrainingSample(2.2f, 1.0f, 12.0f, 1, 0),
        TrainingSample(1.6f, 0.3f, 4.0f, 0, 0),
        TrainingSample(2.3f, 0.9f, 14.0f, 1, 0),
        TrainingSample(1.9f, 0.6f, 7.0f, 0, 0),
        TrainingSample(2.1f, 0.7f, 9.0f, 0, 0),
        TrainingSample(1.7f, 0.4f, 6.0f, 0, 0),
        TrainingSample(2.0f, 0.5f, 11.0f, 1, 0),

        // Class 1: Mild Periodontitis
        TrainingSample(2.8f, 1.5f, 20.0f, 1, 1),
        TrainingSample(3.0f, 1.8f, 25.0f, 2, 1),
        TrainingSample(2.6f, 1.2f, 18.0f, 1, 1),
        TrainingSample(3.1f, 2.0f, 28.0f, 3, 1),
        TrainingSample(2.9f, 1.6f, 22.0f, 2, 1),
        TrainingSample(3.2f, 1.9f, 26.0f, 2, 1),
        TrainingSample(2.7f, 1.3f, 19.0f, 1, 1),
        TrainingSample(3.0f, 1.7f, 24.0f, 2, 1),
        TrainingSample(2.5f, 1.1f, 17.0f, 1, 1),
        TrainingSample(3.3f, 2.1f, 29.0f, 3, 1),

        // Class 2: Moderate Periodontitis
        TrainingSample(3.8f, 2.8f, 35.0f, 6, 2),
        TrainingSample(4.0f, 3.0f, 40.0f, 8, 2),
        TrainingSample(4.2f, 3.2f, 45.0f, 10, 2),
        TrainingSample(4.5f, 3.5f, 50.0f, 12, 2),
        TrainingSample(4.1f, 3.1f, 42.0f, 11, 2),
        TrainingSample(3.9f, 2.9f, 38.0f, 7, 2),
        TrainingSample(4.3f, 3.3f, 48.0f, 13, 2),
        TrainingSample(4.4f, 3.4f, 52.0f, 14, 2),
        TrainingSample(3.7f, 2.7f, 33.0f, 5, 2),
        TrainingSample(4.0f, 3.0f, 55.0f, 15, 2),

        // Class 3: Severe Periodontitis
        TrainingSample(5.2f, 4.8f, 68.0f, 18, 3),
        TrainingSample(5.5f, 5.0f, 72.0f, 20, 3),
        TrainingSample(5.8f, 5.3f, 78.0f, 23, 3),
        TrainingSample(6.0f, 5.5f, 82.0f, 26, 3),
        TrainingSample(6.2f, 5.8f, 85.0f, 28, 3),
        TrainingSample(6.5f, 6.0f, 88.0f, 30, 3),
        TrainingSample(5.0f, 4.5f, 65.0f, 17, 3),
        TrainingSample(5.3f, 4.9f, 70.0f, 19, 3),
        TrainingSample(6.0f, 5.6f, 90.0f, 31, 3),
        TrainingSample(5.7f, 5.2f, 75.0f, 24, 3)
    )

    fun predict(
        meanPpd: Float,
        meanCal: Float,
        bopPercentage: Float,
        deepPocketsCount: Int,
        weights: ModelWeights?
    ): PredictionResult {
        val numClasses = 4
        val numFeatures = 4

        var probHealthy = 0
        var probModerate = 0
        var probSevere = 0
        var predClass = 0
        val modelVersion: String

        if (weights != null && !weights.isZero()) {
            modelVersion = "Custom Trained"
            
            // Normalize input features
            val x = listOf(
                meanPpd / 8.0f,
                meanCal / 8.0f,
                bopPercentage / 100.0f,
                deepPocketsCount / 32.0f
            )

            // Compute logits
            val logits = FloatArray(numClasses) { 0.0f }
            for (i in 0 until numClasses) {
                var sum = 0.0f
                for (j in 0 until numFeatures) {
                    sum += weights.W[i][j] * x[j]
                }
                logits[i] = sum + weights.b[i]
            }

            // Softmax with overflow protection
            val maxLogit = logits.maxOrNull() ?: 0.0f
            val expVals = FloatArray(numClasses)
            var sumExp = 0.0f
            for (i in 0 until numClasses) {
                val value = exp(logits[i] - maxLogit)
                expVals[i] = value
                sumExp += value
            }

            val probs = FloatArray(numClasses)
            for (i in 0 until numClasses) {
                probs[i] = expVals[i] / if (sumExp != 0f) sumExp else 1.0f
            }

            // Find class with highest probability
            var maxIndex = 0
            var maxVal = logits[0]
            for (i in 1 until numClasses) {
                if (logits[i] > maxVal) {
                    maxVal = logits[i]
                    maxIndex = i
                }
            }
            predClass = maxIndex

            // Map mild (class 1) probabilities into healthy/moderate for the 3-bucket UI
            probHealthy = ((probs[0] + probs[1] * 0.6f) * 100f).roundToInt()
            probModerate = ((probs[2] + probs[1] * 0.4f) * 100f).roundToInt()
            probSevere = (probs[3] * 100f).roundToInt()

        } else {
            modelVersion = "Base Clinical Model"
            
            // Guideline-Based Rule Engine (AAP/EFP 2018)
            predClass = if (meanPpd <= 3.0f && meanCal <= 1.0f && bopPercentage <= 20.0f && deepPocketsCount <= 1) {
                0 // Healthy
            } else if (meanPpd <= 3.5f && meanCal <= 2.0f && bopPercentage <= 30.0f && deepPocketsCount <= 4) {
                1 // Mild
            } else if (meanPpd <= 5.0f && meanCal <= 4.0f && bopPercentage <= 60.0f && deepPocketsCount <= 15) {
                2 // Moderate
            } else {
                3 // Severe
            }

            // Build smooth probability curves using distance-to-threshold
            when (predClass) {
                0 -> { // Healthy
                    probHealthy = ((3.5f - meanPpd) / 3.5f * 90f + 60f).roundToInt().coerceIn(60, 92)
                    probModerate = (meanPpd / 8.0f * 30f).roundToInt().coerceIn(0, 15)
                    probSevere = (100 - probHealthy - probModerate).coerceAtLeast(0)
                }
                1 -> { // Mild
                    probHealthy = ((3.5f - meanPpd) / 3.5f * 40f + 10f).roundToInt().coerceIn(5, 35)
                    probModerate = (meanPpd / 5.0f * 40f + 10f).roundToInt().coerceIn(15, 50)
                    probSevere = (100 - probHealthy - probModerate).coerceAtLeast(0)
                }
                2 -> { // Moderate
                    val depthRatio = (meanPpd - 3.5f) / 1.5f // 0 to 1 across 3.5-5mm
                    probSevere = (depthRatio * 25f + 5f).roundToInt().coerceIn(5, 25)
                    probHealthy = ((1f - depthRatio) * 10f + 2f).roundToInt().coerceIn(2, 15)
                    probModerate = (100 - probHealthy - probSevere).coerceAtLeast(0)
                }
                else -> { // Severe
                    val depthRatio = ((meanPpd - 5.0f) / 3.0f).coerceAtMost(1.0f)
                    probSevere = (depthRatio * 30f + 62f).roundToInt().coerceIn(62, 95)
                    probHealthy = (5 - (depthRatio * 4f).roundToInt()).coerceAtLeast(1)
                    probModerate = (100 - probHealthy - probSevere).coerceAtLeast(0)
                }
            }
        }

        // Normalize to sum up to 100%
        val total = probHealthy + probModerate + probSevere
        if (total > 0 && total != 100) {
            val diff = 100 - total
            if (probHealthy >= probModerate && probHealthy >= probSevere) {
                probHealthy += diff
            } else if (probModerate >= probHealthy && probModerate >= probSevere) {
                probModerate += diff
            } else {
                probSevere += diff
            }
        }

        probHealthy = probHealthy.coerceAtLeast(0)
        probModerate = probModerate.coerceAtLeast(0)
        probSevere = probSevere.coerceAtLeast(0)

        // Map predicted class to staging & grading
        val verdict: String
        val stageAndGrade: String
        when (predClass) {
            0 -> {
                verdict = "Healthy Periodontium"
                stageAndGrade = "Healthy, Grade A"
            }
            1 -> {
                verdict = "Mild Periodontitis"
                stageAndGrade = "Stage I, Grade A"
            }
            2 -> {
                verdict = "Moderate Periodontitis"
                stageAndGrade = "Stage II, Grade B"
            }
            else -> {
                verdict = "Severe Periodontitis"
                stageAndGrade = "Stage III, Grade C"
            }
        }

        return PredictionResult(
            success = true,
            verdict = verdict,
            stageAndGrade = stageAndGrade,
            probHealthy = probHealthy,
            probModerate = probModerate,
            probSevere = probSevere,
            modelVersion = modelVersion
        )
    }

    fun train(samples: List<TrainingSample>): ModelWeights {
        val numFeatures = 4
        val numClasses = 4
        val epochs = 1000
        val lr = 0.10f

        // Initialize weights and biases to 0
        val W = MutableList(numClasses) { MutableList(numFeatures) { 0.0f } }
        val b = MutableList(numClasses) { 0.0f }

        val dataset = samples.toMutableList()

        for (epoch in 0 until epochs) {
            dataset.shuffle()
            for (sample in dataset) {
                val x = listOf(
                    sample.meanPpd / 8.0f,
                    sample.meanCal / 8.0f,
                    sample.bopPercentage / 100.0f,
                    sample.deepPocketsCount / 32.0f
                )
                val yClass = sample.diagnosisClass

                // Forward pass - compute logits
                val logits = FloatArray(numClasses) { 0.0f }
                for (i in 0 until numClasses) {
                    var sum = 0.0f
                    for (j in 0 until numFeatures) {
                        sum += W[i][j] * x[j]
                    }
                    logits[i] = sum + b[i]
                }

                // Softmax
                val maxLogit = logits.maxOrNull() ?: 0.0f
                val expVals = FloatArray(numClasses)
                var sumExp = 0.0f
                for (i in 0 until numClasses) {
                    val value = exp(logits[i] - maxLogit)
                    expVals[i] = value
                    sumExp += value
                }

                val probs = FloatArray(numClasses)
                for (i in 0 until numClasses) {
                    probs[i] = expVals[i] / if (sumExp != 0f) sumExp else 1.0f
                }

                // Stochastic Gradient Descent weight update
                for (i in 0 until numClasses) {
                    val yTarget = if (i == yClass) 1.0f else 0.0f
                    val error = probs[i] - yTarget
                    for (j in 0 until numFeatures) {
                        W[i][j] -= lr * error * x[j]
                    }
                    b[i] -= lr * error
                }
            }
        }

        // Calculate training accuracy
        var correct = 0
        for (sample in dataset) {
            val x = listOf(
                sample.meanPpd / 8.0f,
                sample.meanCal / 8.0f,
                sample.bopPercentage / 100.0f,
                sample.deepPocketsCount / 32.0f
            )
            val yClass = sample.diagnosisClass

            val logits = FloatArray(numClasses) { 0.0f }
            for (i in 0 until numClasses) {
                var sum = 0.0f
                for (j in 0 until numFeatures) {
                    sum += W[i][j] * x[j]
                }
                logits[i] = sum + b[i]
            }

            var maxIndex = 0
            var maxVal = logits[0]
            for (i in 1 until numClasses) {
                if (logits[i] > maxVal) {
                    maxVal = logits[i]
                    maxIndex = i
                }
            }

            if (maxIndex == yClass) {
                correct++
            }
        }

        val accuracy = if (dataset.isNotEmpty()) (correct.toFloat() / dataset.size.toFloat()) * 100f else 100f
        val trainedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        return ModelWeights(
            W = W,
            b = b,
            accuracy = accuracy,
            samplesCount = dataset.size,
            trainedAt = trainedAt
        )
    }
}
