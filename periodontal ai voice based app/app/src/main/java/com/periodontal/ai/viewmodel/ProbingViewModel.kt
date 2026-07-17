package com.periodontal.ai.viewmodel

import androidx.lifecycle.ViewModel
import com.periodontal.ai.data.ai.ClassificationResult
import com.periodontal.ai.data.ai.PeriodontalClassifier
import com.periodontal.ai.data.model.ToothMeasurement
import com.periodontal.ai.data.voice.ProbingSite
import com.periodontal.ai.data.voice.VoiceCommand
import com.periodontal.ai.data.voice.VoiceCommandParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProbingUiState(
    val selectedToothNumber: Int = 1,
    val selectedSite: ProbingSite = ProbingSite.DISTAL_FACIAL,
    val toothMeasurements: List<ToothMeasurement> = List(32) { index -> ToothMeasurement(toothNumber = index + 1) },
    val isVoiceListening: Boolean = false,
    val voiceTranscript: String = "",
    val voiceFeedbackMessage: String = "Press mic to speak measurements",
    val classification: ClassificationResult = PeriodontalClassifier.classify(emptyList())
)

class ProbingViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProbingUiState())
    val uiState: StateFlow<ProbingUiState> = _uiState.asStateFlow()

    init {
        recalculateClassification()
    }

    fun selectTooth(toothNum: Int) {
        if (toothNum in 1..32) {
            _uiState.update { it.copy(selectedToothNumber = toothNum) }
        }
    }

    fun selectSite(site: ProbingSite) {
        _uiState.update { it.copy(selectedSite = site) }
    }

    fun updateCurrentSiteDepth(depth: Int) {
        val currentTooth = _uiState.value.selectedToothNumber
        val currentSite = _uiState.value.selectedSite
        updateDepth(currentTooth, currentSite, depth)
    }

    fun toggleCurrentSiteBleeding() {
        val currentTooth = _uiState.value.selectedToothNumber
        val currentSite = _uiState.value.selectedSite
        _uiState.update { state ->
            val updatedList = state.toothMeasurements.map { measurement ->
                if (measurement.toothNumber == currentTooth) {
                    when (currentSite) {
                        ProbingSite.DISTAL_FACIAL -> measurement.copy(bleedingDF = !measurement.bleedingDF)
                        ProbingSite.FACIAL -> measurement.copy(bleedingF = !measurement.bleedingF)
                        ProbingSite.MESIAL_FACIAL -> measurement.copy(bleedingMF = !measurement.bleedingMF)
                        ProbingSite.DISTAL_LINGUAL -> measurement.copy(bleedingDL = !measurement.bleedingDL)
                        ProbingSite.LINGUAL -> measurement.copy(bleedingL = !measurement.bleedingL)
                        ProbingSite.MESIAL_LINGUAL -> measurement.copy(bleedingML = !measurement.bleedingML)
                    }
                } else {
                    measurement
                }
            }
            state.copy(toothMeasurements = updatedList)
        }
        recalculateClassification()
    }

    fun setVoiceListening(listening: Boolean) {
        _uiState.update { it.copy(isVoiceListening = listening) }
    }

    fun setVoiceFeedback(message: String) {
        _uiState.update { it.copy(voiceFeedbackMessage = message) }
    }

    fun setVoiceTranscript(transcript: String) {
        _uiState.update { it.copy(voiceTranscript = transcript) }
    }

    fun processVoiceInput(spokenText: String) {
        _uiState.update { it.copy(voiceTranscript = spokenText) }
        val command = VoiceCommandParser.parse(spokenText)
        executeVoiceCommand(command)
    }

    private fun executeVoiceCommand(command: VoiceCommand) {
        when (command) {
            is VoiceCommand.SelectTooth -> {
                selectTooth(command.toothNumber)
                setVoiceFeedback("Selected Tooth ${command.toothNumber}")
            }
            is VoiceCommand.InputSiteDepth -> {
                updateDepth(_uiState.value.selectedToothNumber, command.site, command.depth)
                selectSite(command.site)
                setVoiceFeedback("Set ${command.site.name} to ${command.depth}mm")
            }
            is VoiceCommand.InputDepths -> {
                applySequentialDepths(command.depths)
            }
            is VoiceCommand.ToggleBleeding -> {
                setBleeding(_uiState.value.selectedToothNumber, command.site, command.bleeding)
                setVoiceFeedback("${command.site.name} bleeding: ${command.bleeding}")
            }
            VoiceCommand.NextTooth -> {
                val next = _uiState.value.selectedToothNumber + 1
                if (next <= 32) {
                    selectTooth(next)
                    setVoiceFeedback("Moved to Tooth $next")
                } else {
                    setVoiceFeedback("Already at last tooth")
                }
            }
            VoiceCommand.PrevTooth -> {
                val prev = _uiState.value.selectedToothNumber - 1
                if (prev >= 1) {
                    selectTooth(prev)
                    setVoiceFeedback("Moved to Tooth $prev")
                } else {
                    setVoiceFeedback("Already at first tooth")
                }
            }
            VoiceCommand.ClearRecord -> {
                _uiState.update { state ->
                    state.copy(
                        toothMeasurements = List(32) { index -> ToothMeasurement(toothNumber = index + 1) }
                    )
                }
                recalculateClassification()
                setVoiceFeedback("Cleared all measurements")
            }
            is VoiceCommand.Unknown -> {
                setVoiceFeedback("Unknown Command: \"${command.rawText}\"")
            }
        }
    }

    private fun updateDepth(toothNum: Int, site: ProbingSite, depth: Int) {
        _uiState.update { state ->
            val updatedList = state.toothMeasurements.map { measurement ->
                if (measurement.toothNumber == toothNum) {
                    when (site) {
                        ProbingSite.DISTAL_FACIAL -> measurement.copy(distalFacial = depth)
                        ProbingSite.FACIAL -> measurement.copy(facial = depth)
                        ProbingSite.MESIAL_FACIAL -> measurement.copy(mesialFacial = depth)
                        ProbingSite.DISTAL_LINGUAL -> measurement.copy(distalLingual = depth)
                        ProbingSite.LINGUAL -> measurement.copy(lingual = depth)
                        ProbingSite.MESIAL_LINGUAL -> measurement.copy(mesialLingual = depth)
                    }
                } else {
                    measurement
                }
            }
            state.copy(toothMeasurements = updatedList)
        }
        recalculateClassification()
    }

    private fun setBleeding(toothNum: Int, site: ProbingSite, bleeding: Boolean) {
        _uiState.update { state ->
            val updatedList = state.toothMeasurements.map { measurement ->
                if (measurement.toothNumber == toothNum) {
                    when (site) {
                        ProbingSite.DISTAL_FACIAL -> measurement.copy(bleedingDF = bleeding)
                        ProbingSite.FACIAL -> measurement.copy(bleedingF = bleeding)
                        ProbingSite.MESIAL_FACIAL -> measurement.copy(bleedingMF = bleeding)
                        ProbingSite.DISTAL_LINGUAL -> measurement.copy(bleedingDL = bleeding)
                        ProbingSite.LINGUAL -> measurement.copy(bleedingL = bleeding)
                        ProbingSite.MESIAL_LINGUAL -> measurement.copy(bleedingML = bleeding)
                    }
                } else {
                    measurement
                }
            }
            state.copy(toothMeasurements = updatedList)
        }
        recalculateClassification()
    }

    private fun applySequentialDepths(depths: List<Int>) {
        val toothNum = _uiState.value.selectedToothNumber
        val startSite = _uiState.value.selectedSite

        // Map depths in order of current site selection
        val order = ProbingSite.values()
        var currentIdx = order.indexOf(startSite)

        _uiState.update { state ->
            var updatedList = state.toothMeasurements
            depths.forEach { depth ->
                val site = order[currentIdx]
                updatedList = updatedList.map { measurement ->
                    if (measurement.toothNumber == toothNum) {
                        when (site) {
                            ProbingSite.DISTAL_FACIAL -> measurement.copy(distalFacial = depth)
                            ProbingSite.FACIAL -> measurement.copy(facial = depth)
                            ProbingSite.MESIAL_FACIAL -> measurement.copy(mesialFacial = depth)
                            ProbingSite.DISTAL_LINGUAL -> measurement.copy(distalLingual = depth)
                            ProbingSite.LINGUAL -> measurement.copy(lingual = depth)
                            ProbingSite.MESIAL_LINGUAL -> measurement.copy(mesialLingual = depth)
                        }
                    } else {
                        measurement
                    }
                }
                // Cycle index or advance
                currentIdx = (currentIdx + 1) % order.size
            }
            state.copy(
                toothMeasurements = updatedList,
                selectedSite = order[currentIdx] // Auto advance active cursor
            )
        }
        recalculateClassification()
        setVoiceFeedback("Input depths: ${depths.joinToString(", ")}mm")
    }

    private fun recalculateClassification() {
        val result = PeriodontalClassifier.classify(_uiState.value.toothMeasurements)
        _uiState.update { it.copy(classification = result) }
    }
}
