package com.periodontal.ai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.periodontal.ai.data.local.AppDatabase
import com.periodontal.ai.data.model.Patient
import com.periodontal.ai.data.model.PeriodontalRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PatientViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val patientDao = db.patientDao()
    private val recordDao = db.recordDao()

    val patientsList: StateFlow<List<Patient>> = patientDao.getAllPatients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPatient = MutableStateFlow<Patient?>(null)
    val selectedPatient: StateFlow<Patient?> = _selectedPatient.asStateFlow()

    private val _selectedPatientRecords = MutableStateFlow<List<PeriodontalRecord>>(emptyList())
    val selectedPatientRecords: StateFlow<List<PeriodontalRecord>> = _selectedPatientRecords.asStateFlow()

    fun selectPatient(patient: Patient) {
        _selectedPatient.value = patient
        viewModelScope.launch {
            recordDao.getRecordsForPatient(patient.id).collect { records ->
                _selectedPatientRecords.value = records
            }
        }
    }

    fun addPatient(name: String, age: Int, gender: String, phoneNumber: String, onSuccess: (Patient) -> Unit = {}) {
        viewModelScope.launch {
            val patient = Patient(name = name, age = age, gender = gender, phoneNumber = phoneNumber)
            patientDao.insertPatient(patient)
            onSuccess(patient)
        }
    }

    fun addPeriodontalRecord(record: PeriodontalRecord) {
        viewModelScope.launch {
            recordDao.insertRecord(record)
        }
    }
}
