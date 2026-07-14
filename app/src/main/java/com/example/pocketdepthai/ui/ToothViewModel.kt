package com.example.pocketdepthai.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocketdepthai.data.AppDatabase
import com.example.pocketdepthai.data.ToothData
import com.example.pocketdepthai.data.ToothRepository
import com.example.pocketdepthai.util.AIEngine
import com.example.pocketdepthai.util.ModelWeights
import com.example.pocketdepthai.util.TrainingSample
import com.example.pocketdepthai.util.Parser
import com.example.pocketdepthai.voice.VoiceRecognizerManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ToothViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ToothRepository
    val allToothData: StateFlow<List<ToothData>>
    
    private val voiceManager = VoiceRecognizerManager(application)
    
    val isListening = voiceManager.isListening
    val recognizedText = voiceManager.recognizedText
    val voiceError = voiceManager.error

    // Firebase Auth and Firestore instances
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private var currentWeights: ModelWeights? = null

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    private val _patients = MutableStateFlow<List<String>>(listOf("Sarah Johnson", "Michael Chen", "Emily Davis"))
    val patients: StateFlow<List<String>> = _patients

    private var currentPatientName: String = "Patient"

    private val _clinicalUserName = MutableStateFlow("Sarah Johnson")
    val clinicalUserName: StateFlow<String> = _clinicalUserName

    // --- Dynamic Clinical Data Analytics Model ---

    private val _activePatientName = MutableStateFlow("Sarah Johnson")
    val activePatientName: StateFlow<String> = _activePatientName

    // Manual Input State Flows
    private val _manualPpd = MutableStateFlow<Map<String, String>>(emptyMap())
    val manualPpd: StateFlow<Map<String, String>> = _manualPpd

    private val _manualCal = MutableStateFlow<Map<String, String>>(emptyMap())
    val manualCal: StateFlow<Map<String, String>> = _manualCal

    private val _manualBop = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val manualBop: StateFlow<Map<Int, Boolean>> = _manualBop

    // Computed Clinical Metrics
    private val _meanPpd = MutableStateFlow(4.2f)
    val meanPpd: StateFlow<Float> = _meanPpd

    private val _meanCal = MutableStateFlow(3.1f)
    val meanCal: StateFlow<Float> = _meanCal

    private val _bopPercentage = MutableStateFlow(45f)
    val bopPercentage: StateFlow<Float> = _bopPercentage

    private val _deepPocketsCount = MutableStateFlow(14)
    val deepPocketsCount: StateFlow<Int> = _deepPocketsCount

    private val _clinicalVerdict = MutableStateFlow("Moderate Periodontitis")
    val clinicalVerdict: StateFlow<String> = _clinicalVerdict

    private val _stageAndGrade = MutableStateFlow("Stage II, Grade B")
    val stageAndGrade: StateFlow<String> = _stageAndGrade

    // Diagnostic confidence values
    private val _probModerate = MutableStateFlow(87)
    val probModerate: StateFlow<Int> = _probModerate

    private val _probSevere = MutableStateFlow(8)
    val probSevere: StateFlow<Int> = _probSevere

    private val _probHealthy = MutableStateFlow(5)
    val probHealthy: StateFlow<Int> = _probHealthy

    // Backend AI Model Training & Status StateFlows
    private val _aiModelAccuracy = MutableStateFlow(95.0f)
    val aiModelAccuracy: StateFlow<Float> = _aiModelAccuracy

    private val _isTrainingModel = MutableStateFlow(false)
    val isTrainingModel: StateFlow<Boolean> = _isTrainingModel

    private val _aiTrainingStatus = MutableStateFlow<String?>(null)
    val aiTrainingStatus: StateFlow<String?> = _aiTrainingStatus

    private val _isCustomModelTrained = MutableStateFlow(false)
    val isCustomModelTrained: StateFlow<Boolean> = _isCustomModelTrained

    fun setActivePatient(name: String) {
        _activePatientName.value = name
        currentPatientName = name
        resetClinicalMetrics()
    }

    fun resetClinicalMetrics() {
        _manualPpd.value = emptyMap()
        _manualCal.value = emptyMap()
        _manualBop.value = emptyMap()

        // Reset to neutral healthy baseline — actual values are computed once real input arrives
        _meanPpd.value = 2.0f
        _meanCal.value = 0.5f
        _bopPercentage.value = 10f
        _deepPocketsCount.value = 0
        _clinicalVerdict.value = "Healthy Periodontium"
        _stageAndGrade.value = "Healthy, Grade A"
        _probModerate.value = 8
        _probSevere.value = 2
        _probHealthy.value = 90
    }

    fun updateManualInput(
        ppd: Map<String, String>,
        cal: Map<String, String>,
        bop: Map<Int, Boolean>
    ) {
        _manualPpd.value = ppd
        _manualCal.value = cal
        _manualBop.value = bop
        analyzeAndStoreMetrics()
    }

    fun analyzeAndStoreMetrics() {
        val ppdValues = mutableListOf<Int>()
        val calValues = mutableListOf<Int>()
        
        // Merge manual PPD inputs and voice DB inputs per tooth to prevent double-counting
        for (toothNum in 1..32) {
            val manualVals = (0..2).mapNotNull { index ->
                _manualPpd.value["tooth${toothNum}_ppd_$index"]?.toIntOrNull()
            }
            if (manualVals.isNotEmpty()) {
                ppdValues.addAll(manualVals)
            } else {
                val voiceEntry = allToothData.value
                    .filter { it.toothNumber == toothNum }
                    .maxByOrNull { it.timestamp }
                voiceEntry?.values?.let { ppdValues.addAll(it) }
            }
        }
        
        // Collect all non-empty manual CAL inputs
        for (toothNum in 1..32) {
            val manualVals = (0..2).mapNotNull { index ->
                _manualCal.value["tooth${toothNum}_cal_$index"]?.toIntOrNull()
            }
            if (manualVals.isNotEmpty()) {
                calValues.addAll(manualVals)
            }
        }

        val bopCount = _manualBop.value.values.count { it }
        
        // 1. Calculate Mean PPD
        val avgPpd = if (ppdValues.isNotEmpty()) ppdValues.average().toFloat() else 2.0f
        _meanPpd.value = Math.round(avgPpd * 10f) / 10f

        // 2. Calculate Mean CAL (estimate conservatively from PPD if empty)
        val avgCal = if (calValues.isNotEmpty()) {
            calValues.average().toFloat()
        } else {
            if (ppdValues.isNotEmpty()) {
                // Conservative estimate: CAL ≈ PPD - 1.5 (clamped to 0)
                (avgPpd - 1.5f).coerceAtLeast(0f)
            } else {
                0.0f
            }
        }
        _meanCal.value = Math.round(avgCal * 10f) / 10f

        // 3. BOP Percentage
        val measuredTeeth = _manualBop.value.size
        val bopPct = if (measuredTeeth > 0) {
            (bopCount.toFloat() / measuredTeeth.toFloat()) * 100f
        } else if (ppdValues.isNotEmpty()) {
            // Estimate BOP conservatively from proportion of deep pockets (≥4mm)
            val deepPocketTeeth = ppdValues.count { it >= 4 }
            (deepPocketTeeth.toFloat() / ppdValues.size.toFloat() * 60f).coerceIn(0f, 100f)
        } else {
            10f
        }
        _bopPercentage.value = Math.round(bopPct).toFloat()

        // 4. Deep pockets count (sites with PPD ≥ 4mm)
        val deepCount = ppdValues.count { it >= 4 } + calValues.count { it >= 4 }
        _deepPocketsCount.value = if (ppdValues.isNotEmpty() || calValues.isNotEmpty()) deepCount else 0

        // 5. Local fallback classification using AAP/EFP 2018 Guidelines
        //    Based on MEAN values (not max) to avoid single outlier teeth causing wrong class
        val localClass = when {
            avgPpd >= 5.0f || avgCal >= 4.0f || bopPct > 60f || deepCount > 15 -> 3 // Severe
            avgPpd >= 3.8f || avgCal >= 2.8f || bopPct > 30f || deepCount > 5  -> 2 // Moderate
            avgPpd >= 2.8f || avgCal >= 1.2f || bopPct > 20f || deepCount > 1  -> 1 // Mild
            else -> 0 // Healthy
        }

        when (localClass) {
            3 -> {
                _clinicalVerdict.value = "Severe Periodontitis"
                _stageAndGrade.value   = "Stage III, Grade C"
                _probSevere.value   = 82
                _probModerate.value = 14
                _probHealthy.value  = 4
            }
            2 -> {
                _clinicalVerdict.value = "Moderate Periodontitis"
                _stageAndGrade.value   = "Stage II, Grade B"
                _probModerate.value = 78
                _probSevere.value   = 14
                _probHealthy.value  = 8
            }
            1 -> {
                _clinicalVerdict.value = "Mild Periodontitis"
                _stageAndGrade.value   = "Stage I, Grade A"
                _probHealthy.value  = 58
                _probModerate.value = 30
                _probSevere.value   = 12
            }
            else -> {
                _clinicalVerdict.value = "Healthy Periodontium"
                _stageAndGrade.value   = "Healthy, Grade A"
                _probHealthy.value  = 90
                _probModerate.value = 8
                _probSevere.value   = 2
            }
        }

        // Trigger Firebase-backed AI prediction asynchronously
        predictDiseaseOnline(_meanPpd.value, _meanCal.value, _bopPercentage.value, _deepPocketsCount.value)
    }

    fun predictDiseaseOnline(meanPpd: Float, meanCal: Float, bopPercentage: Float, deepPocketsCount: Int) {
        viewModelScope.launch {
            try {
                val result = AIEngine.predict(meanPpd, meanCal, bopPercentage, deepPocketsCount, currentWeights)
                if (result.success) {
                    _clinicalVerdict.value = result.verdict
                    _stageAndGrade.value = result.stageAndGrade
                    _probHealthy.value = result.probHealthy
                    _probModerate.value = result.probModerate
                    _probSevere.value = result.probSevere
                    _aiTrainingStatus.value = "Synced with AI Engine (${result.modelVersion})"
                }
            } catch (e: Exception) {
                // Offline fallback - retains local guidelines calculations
                _aiTrainingStatus.value = "Offline AI (Guidelines Mode)"
            }
        }
    }

    private fun seedBaseTrainingData(onComplete: () -> Unit) {
        val batch = db.batch()
        AIEngine.baseTrainingSamples.forEachIndexed { index, sample ->
            val docRef = db.collection("ai_training_data").document("base_sample_$index")
            val data = hashMapOf(
                "meanPpd" to sample.meanPpd,
                "meanCal" to sample.meanCal,
                "bopPercentage" to sample.bopPercentage,
                "deepPocketsCount" to sample.deepPocketsCount,
                "diagnosisClass" to sample.diagnosisClass
            )
            batch.set(docRef, data)
        }
        batch.commit()
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { onComplete() }
    }

    fun trainAIModel(onSuccess: (Float) -> Unit, onError: (String) -> Unit) {
        _isTrainingModel.value = true
        _aiTrainingStatus.value = "Training AI on Firebase..."
        viewModelScope.launch {
            try {
                db.collection("ai_training_data").get()
                    .addOnSuccessListener { result ->
                        val samples = mutableListOf<TrainingSample>()
                        for (doc in result.documents) {
                            val meanPpd = doc.getDouble("meanPpd")?.toFloat()
                            val meanCal = doc.getDouble("meanCal")?.toFloat()
                            val bopPercentage = doc.getDouble("bopPercentage")?.toFloat()
                            val deepPocketsCount = doc.getLong("deepPocketsCount")?.toInt()
                            val diagnosisClass = doc.getLong("diagnosisClass")?.toInt()
                            if (meanPpd != null && meanCal != null && bopPercentage != null && deepPocketsCount != null && diagnosisClass != null) {
                                samples.add(TrainingSample(meanPpd, meanCal, bopPercentage, deepPocketsCount, diagnosisClass))
                            }
                        }

                        if (samples.isEmpty()) {
                            viewModelScope.launch {
                                seedBaseTrainingData {
                                    trainAIModel(onSuccess, onError)
                                }
                            }
                            return@addOnSuccessListener
                        }

                        val trainedWeights = AIEngine.train(samples)
                        val weightsData = hashMapOf(
                            "W" to trainedWeights.W,
                            "b" to trainedWeights.b,
                            "accuracy" to trainedWeights.accuracy,
                            "samplesCount" to trainedWeights.samplesCount,
                            "trainedAt" to trainedWeights.trainedAt
                        )
                        db.collection("model_config").document("weights").set(weightsData)
                            .addOnSuccessListener {
                                _aiModelAccuracy.value = trainedWeights.accuracy
                                _isCustomModelTrained.value = true
                                _aiTrainingStatus.value = "Model trained! Accuracy: ${trainedWeights.accuracy}%"
                                analyzeAndStoreMetrics()
                                onSuccess(trainedWeights.accuracy)
                            }
                            .addOnFailureListener { e ->
                                _aiTrainingStatus.value = "Sync error"
                                onError(e.message ?: "Failed to upload weights")
                             }
                    }
                    .addOnFailureListener { e ->
                        _aiTrainingStatus.value = "Fetch error"
                        onError(e.message ?: "Failed to fetch training data")
                    }
            } catch (e: Exception) {
                _aiTrainingStatus.value = "Training error"
                onError(e.message ?: "Training failed")
            } finally {
                _isTrainingModel.value = false
            }
        }
    }

    fun addTrainingSample(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val verdict = _clinicalVerdict.value
                val diagnosisClass = when {
                    verdict.contains("Severe", ignoreCase = true) -> 3
                    verdict.contains("Moderate", ignoreCase = true) -> 2
                    verdict.contains("Mild", ignoreCase = true) -> 1
                    else -> 0
                }

                val data = hashMapOf(
                    "meanPpd" to _meanPpd.value,
                    "meanCal" to _meanCal.value,
                    "bopPercentage" to _bopPercentage.value,
                    "deepPocketsCount" to _deepPocketsCount.value,
                    "diagnosisClass" to diagnosisClass,
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("ai_training_data").add(data)
                    .addOnSuccessListener {
                        _statusMessage.value = "Case archived in training vault!"
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Failed to save sample.")
                    }
            } catch (e: Exception) {
                onError(e.message ?: "Error saving sample")
            }
        }
    }

    // --- End Dynamic Clinical Data Analytics Model ---

    fun registerUser(name: String, email: String, phone: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        val uid = authResult.user?.uid ?: ""
                        val clinicianData = hashMapOf(
                            "uid" to uid,
                            "name" to name,
                            "email" to email,
                            "phone" to phone,
                            "createdAt" to System.currentTimeMillis()
                        )
                        db.collection("users").document(uid).set(clinicianData)
                            .addOnSuccessListener {
                                _statusMessage.value = "Clinician '$name' registered successfully"
                                _clinicalUserName.value = name
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                onError(e.message ?: "Failed to save clinician details")
                            }
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Registration failed")
                    }
            } catch (e: Exception) {
                onError(e.message ?: "Registration failed")
            }
        }
    }

    fun loginUser(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authResult ->
                        val uid = authResult.user?.uid ?: ""
                        db.collection("users").document(uid).get()
                            .addOnSuccessListener { doc ->
                                val name = doc.getString("name") ?: "Clinician"
                                _clinicalUserName.value = name
                                _statusMessage.value = "Welcome back, $name"
                                onSuccess()
                            }
                            .addOnFailureListener {
                                _clinicalUserName.value = "Clinician"
                                _statusMessage.value = "Welcome back!"
                                onSuccess()
                            }
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Login failed. Check credentials.")
                    }
            } catch (e: Exception) {
                onError(e.message ?: "Login failed")
            }
        }
    }

    fun registerPatient(name: String, email: String = "", phone: String = "", history: String = "") {
        if (name.isNotBlank()) {
            if (!_patients.value.contains(name)) {
                _patients.value = _patients.value + name
            }
            setActivePatient(name)
            viewModelScope.launch {
                try {
                    val patientData = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "phone" to phone,
                        "history" to history,
                        "createdAt" to System.currentTimeMillis()
                    )
                    db.collection("patients").document(name).set(patientData)
                        .addOnSuccessListener {
                            _statusMessage.value = "Synced Patient '$name' to Firebase"
                        }
                        .addOnFailureListener { e ->
                            _statusMessage.value = "Registered locally. Sync error: ${e.message}"
                        }
                } catch (e: Exception) {
                    _statusMessage.value = "Registered locally. Firebase offline."
                }
            }
        }
    }

    init {
        val toothDao = AppDatabase.getDatabase(application).toothDao()
        repository = ToothRepository(toothDao)
        allToothData = repository.allToothData.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
        fetchPatientsFromBackend()
        
        // Listen to model weight changes in real-time
        db.collection("model_config").document("weights")
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null && snapshot.exists()) {
                    try {
                        val wRaw = snapshot.get("W") as? List<List<Double>>
                        val bRaw = snapshot.get("b") as? List<Double>
                        val acc = snapshot.getDouble("accuracy")?.toFloat() ?: 95f
                        val cnt = snapshot.getLong("samplesCount")?.toInt() ?: 40
                        val dt = snapshot.getString("trainedAt") ?: ""
                        if (wRaw != null && bRaw != null) {
                            val W = wRaw.map { row -> row.map { it.toFloat() } }
                            val b = bRaw.map { it.toFloat() }
                            currentWeights = ModelWeights(W, b, acc, cnt, dt)
                            _aiModelAccuracy.value = acc
                            _isCustomModelTrained.value = true
                            _aiTrainingStatus.value = "Model loaded! Accuracy: $acc%"
                            analyzeAndStoreMetrics()
                        }
                    } catch (ex: Exception) {
                        // Parse error fallback
                    }
                }
            }

        viewModelScope.launch {
            allToothData.collect {
                analyzeAndStoreMetrics()
            }
        }
    }

    fun fetchPatientsFromBackend() {
        viewModelScope.launch {
            try {
                db.collection("patients").get()
                    .addOnSuccessListener { result ->
                        val backendList = result.documents.mapNotNull { it.getString("name") }
                        if (backendList.isNotEmpty()) {
                            _patients.value = (_patients.value + backendList).distinct()
                        }
                    }
            } catch (e: Exception) {
                // Ignore background loading failures
            }
        }
    }

    fun startRecording(patientName: String = "Patient") {
        currentPatientName = patientName
        _statusMessage.value = null
        voiceManager.startListening { text ->
            processResult(text)
        }
    }

    fun stopRecording() {
        voiceManager.stopListening()
    }

    private fun processResult(text: String) {
        val result = Parser.parseSpeech(text)
        if (result != null) {
            val (toothNumber, values) = result
            val toothData = ToothData(toothNumber = toothNumber, values = values)
            viewModelScope.launch {
                repository.insert(toothData)
                _statusMessage.value = "Saved Tooth $toothNumber"
                
                // Immediately trigger analytics refresh with the new voice value included
                analyzeAndStoreMetrics()

                // Sync measurements to Firebase
                try {
                    val toothMeasurement = hashMapOf(
                        "patientName" to currentPatientName,
                        "toothNumber" to toothNumber,
                        "values" to values,
                        "timestamp" to toothData.timestamp
                    )
                    db.collection("tooth_measurements")
                        .document("${currentPatientName}_tooth_${toothNumber}_${toothData.timestamp}")
                        .set(toothMeasurement)
                        .addOnSuccessListener {
                            _statusMessage.value = "Saved & Synced Tooth $toothNumber"
                        }
                } catch (e: Exception) {
                    // Log or handle backend exception, local database is already successfully saved
                }
            }
        } else {
            _statusMessage.value = "Could not parse: $text. Try 'Tooth 11 - 3 4 5'"
        }
    }

    fun clearData() {
        viewModelScope.launch {
            repository.deleteAll()
            resetClinicalMetrics()
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
