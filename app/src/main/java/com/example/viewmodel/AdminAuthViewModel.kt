package com.example.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class UserRole(
    val displayName: String,
    val title: String,
    val email: String,
    val isAdmin: Boolean
) {
    COREY(
        displayName = "Corey",
        title = "Co-Founder & Administrator",
        email = "corey@coreysarah.com",
        isAdmin = true
    ),
    SARAH(
        displayName = "Sarah",
        title = "Co-Founder & Executive Director",
        email = "sarah@coreysarah.com",
        isAdmin = true
    ),
    LEARNER(
        displayName = "Student / Learner",
        title = "Standard Brain Explorer",
        email = "learner@example.com",
        isAdmin = false
    )
}

class AdminAuthViewModel : ViewModel() {

    private val _currentUser = MutableStateFlow(UserRole.COREY) // Default to Admin Corey so Corey & Sarah can immediately test and manage
    val currentUser: StateFlow<UserRole> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _isAdminAuthenticated = MutableStateFlow(true)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    fun login(role: UserRole, pin: String): Boolean {
        // Standard pin 2580 or empty for easy development access
        if (role.isAdmin) {
            if (pin.isEmpty() || pin == "2580" || pin == "1234") {
                _currentUser.value = role
                _isAdminAuthenticated.value = true
                _loginError.value = null
                return true
            } else {
                _loginError.value = "Incorrect security PIN. Please try again."
                return false
            }
        } else {
            _currentUser.value = UserRole.LEARNER
            _isAdminAuthenticated.value = false
            _loginError.value = null
            return true
        }
    }

    fun quickSwitch(role: UserRole) {
        _currentUser.value = role
        _isAdminAuthenticated.value = role.isAdmin
        _loginError.value = null
    }

    fun logoutToLearner() {
        _currentUser.value = UserRole.LEARNER
        _isAdminAuthenticated.value = false
        _loginError.value = null
    }

    fun clearError() {
        _loginError.value = null
    }
}
