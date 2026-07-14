package com.periodontal.ai.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AuthUiState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val email: String = "",
    val error: String? = null
)

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        if (email.contains("@") && password.length >= 6) {
            _uiState.update { it.copy(isLoggedIn = true, isLoading = false, email = email) }
        } else {
            _uiState.update { it.copy(isLoading = false, error = "Invalid email or password (min 6 chars)") }
        }
    }

    fun logout() {
        _uiState.update { AuthUiState() }
    }
}
