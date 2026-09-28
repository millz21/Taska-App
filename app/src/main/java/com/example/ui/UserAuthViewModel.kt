package com.example.ui

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.SupabaseSyncReport
import com.example.data.TaskaRepository
import com.example.data.UserAccountEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class UserSessionState {
    data object Guest : UserSessionState()
    data object Authenticating : UserSessionState()
    data class SignedIn(
        val account: UserAccountEntity,
        val isSupabaseAuthenticated: Boolean
    ) : UserSessionState()
    data class AwaitingEmailConfirmation(val email: String) : UserSessionState()
}

/**
 * Dedicated ViewModel that manages user session state (Signed-In vs. Guest),
 * Supabase authentication (Sign-Up, Login, Password Reset, Session Persistence),
 * and live synchronization with the Supabase backend database.
 */
class UserAuthViewModel(
    private val repository: TaskaRepository,
    private val prefs: SharedPreferences? = null
) : ViewModel() {

    companion object {
        const val KEY_LOGGED_IN_USER_ID = "logged_in_user_id_v1"
        const val KEY_HAS_SEEN_WELCOME = "has_seen_welcome_v1"
        const val KEY_AREA_LABEL = "taska_area_label"
    }

    private val _sessionState = MutableStateFlow<UserSessionState>(UserSessionState.Guest)
    val sessionState: StateFlow<UserSessionState> = _sessionState.asStateFlow()

    val currentUser: StateFlow<UserAccountEntity?> = _sessionState
        .map { state -> (state as? UserSessionState.SignedIn)?.account }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isGuest: StateFlow<Boolean> = _sessionState
        .map { state -> state !is UserSessionState.SignedIn }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val isAuthenticating: StateFlow<Boolean> = _sessionState
        .map { state -> state is UserSessionState.Authenticating }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _authScreenState = MutableStateFlow(AuthScreenState.NONE)
    val authScreenState: StateFlow<AuthScreenState> = _authScreenState.asStateFlow()

    private val _authPromptReason = MutableStateFlow<String?>(null)
    val authPromptReason: StateFlow<String?> = _authPromptReason.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _lastSyncReport = MutableStateFlow<SupabaseSyncReport?>(null)
    val lastSyncReport: StateFlow<SupabaseSyncReport?> = _lastSyncReport.asStateFlow()

    val isSupabaseConfigured: Boolean
        get() = repository.supabaseService.isConfigured

    init {
        restoreSession()
    }

    fun restoreSession() {
        viewModelScope.launch {
            repository.ensureSeedData()
            val savedUserId = prefs?.getInt(KEY_LOGGED_IN_USER_ID, -1) ?: -1
            if (savedUserId > 0) {
                val restoredUser = repository.getUserById(savedUserId)
                if (restoredUser != null) {
                    _sessionState.value = UserSessionState.SignedIn(
                        account = restoredUser,
                        isSupabaseAuthenticated = restoredUser.supabaseUserId.isNotBlank()
                    )
                    val report = repository.syncDatabaseWithSupabase(restoredUser)
                    _lastSyncReport.value = report
                    return@launch
                }
            }
            _sessionState.value = UserSessionState.Guest
            val report = repository.syncDatabaseWithSupabase(null)
            _lastSyncReport.value = report
        }
    }

    fun openAuthFlow(
        step: AuthScreenState = AuthScreenState.CHOICE,
        reason: String? = null
    ) {
        _authErrorMessage.value = null
        _authPromptReason.value = reason
        _authScreenState.value = step
    }

    fun closeAuthFlow() {
        _authErrorMessage.value = null
        _authPromptReason.value = null
        _authScreenState.value = AuthScreenState.NONE
    }

    fun clearError() {
        _authErrorMessage.value = null
    }

    fun continueAsGuest() {
        _sessionState.value = UserSessionState.Guest
        closeAuthFlow()
    }

    fun login(
        email: String,
        password: String,
        onAuthenticated: (UserAccountEntity) -> Unit = {}
    ) {
        if (email.isBlank() || password.isBlank()) {
            _authErrorMessage.value = "Please enter both your email and password."
            return
        }
        val previousState = _sessionState.value
        _sessionState.value = UserSessionState.Authenticating
        _authErrorMessage.value = null

        viewModelScope.launch {
            val result = repository.signInUser(email, password)
            result.onSuccess { account ->
                persistSignedInAccount(account)
                onAuthenticated(account)
                val report = repository.syncDatabaseWithSupabase(account)
                _lastSyncReport.value = report
            }.onFailure { err ->
                _sessionState.value = previousState
                _authErrorMessage.value = repository.supabaseService.friendlyError(err, "sign in")
                    .takeIf { repository.supabaseService.isConfigured }
                    ?: (err.message ?: "Unable to sign in. Please check your credentials.")
            }
        }
    }

    fun signUp(
        fullName: String,
        email: String,
        password: String,
        defaultArea: String = "",
        memberRole: String = "Hire & Earn",
        age: String = "",
        gender: String = "",
        onAuthenticated: (UserAccountEntity) -> Unit = {}
    ) {
        if (fullName.isBlank()) {
            _authErrorMessage.value = "Please enter your display name."
            return
        }
        if (email.isBlank() || !email.contains("@")) {
            _authErrorMessage.value = "Please enter a valid email address."
            return
        }
        if (password.length < 6) {
            _authErrorMessage.value = "Password must be at least 6 characters."
            return
        }

        val previousState = _sessionState.value
        _sessionState.value = UserSessionState.Authenticating
        _authErrorMessage.value = null

        viewModelScope.launch {
            val result = repository.registerUser(
                fullName = fullName,
                email = email,
                password = password,
                defaultArea = defaultArea,
                memberRole = memberRole,
                age = age,
                gender = gender
            )
            result.onSuccess { account ->
                persistSignedInAccount(account)
                onAuthenticated(account)
                val report = repository.syncDatabaseWithSupabase(account)
                _lastSyncReport.value = report
            }.onFailure { err ->
                _sessionState.value = previousState
                _authErrorMessage.value = repository.supabaseService.friendlyError(err, "create your account")
                    .takeIf { repository.supabaseService.isConfigured }
                    ?: (err.message ?: "Could not create account.")
            }
        }
    }

    private fun persistSignedInAccount(account: UserAccountEntity) {
        val cleanArea = if (account.defaultArea.equals("UNZA Campus Hub", ignoreCase = true)) "" else account.defaultArea
        val cleaned = account.copy(defaultArea = cleanArea)
        _sessionState.value = UserSessionState.SignedIn(
            account = cleaned,
            isSupabaseAuthenticated = cleaned.supabaseUserId.isNotBlank()
        )
        prefs?.edit()
            ?.putBoolean(KEY_HAS_SEEN_WELCOME, true)
            ?.putInt(KEY_LOGGED_IN_USER_ID, cleaned.id)
            ?.putString(KEY_AREA_LABEL, cleanArea)
            ?.apply()
        _authErrorMessage.value = null
        _authPromptReason.value = null
        _authScreenState.value = AuthScreenState.NONE
    }

    fun updateAccountInSession(account: UserAccountEntity) {
        _sessionState.value = UserSessionState.SignedIn(
            account = account,
            isSupabaseAuthenticated = account.supabaseUserId.isNotBlank()
        )
    }

    fun updateDisplayName(
        newName: String,
        onUpdated: (UserAccountEntity) -> Unit = {}
    ) {
        val current = currentUser.value ?: return
        if (newName.trim().length < 2) return
        viewModelScope.launch {
            val updated = repository.updateUserDisplayName(current, newName)
            updateAccountInSession(updated)
            onUpdated(updated)
        }
    }

    fun updateFullProfile(
        fullName: String,
        age: String,
        gender: String,
        bio: String,
        defaultArea: String,
        onUpdated: (UserAccountEntity) -> Unit = {}
    ) {
        val current = currentUser.value ?: return
        if (fullName.trim().length < 2) return
        viewModelScope.launch {
            val updated = repository.updateUserFullProfile(
                user = current,
                fullName = fullName,
                age = age,
                gender = gender,
                bio = bio,
                defaultArea = defaultArea
            )
            updateAccountInSession(updated)
            onUpdated(updated)
        }
    }

    fun sendPasswordReset(
        email: String,
        onResult: (String) -> Unit = {}
    ) {
        val clean = email.trim()
        if (clean.isEmpty() || !clean.contains("@")) {
            _authErrorMessage.value = "Enter your email address above first, then tap Reset Password."
            return
        }
        viewModelScope.launch {
            if (repository.supabaseService.isConfigured) {
                val res = repository.supabaseService.sendPasswordResetEmail(clean)
                res.onSuccess {
                    onResult("Password reset link sent to $clean.")
                }.onFailure { err ->
                    _authErrorMessage.value = repository.supabaseService.friendlyError(err, "send reset email")
                }
            } else {
                _authErrorMessage.value = "Password reset email service is currently unavailable."
            }
        }
    }

    fun syncWithSupabase(onReport: (SupabaseSyncReport) -> Unit = {}) {
        viewModelScope.launch {
            val report = repository.syncDatabaseWithSupabase(currentUser.value)
            _lastSyncReport.value = report
            onReport(report)
        }
    }

    fun signOut(onSignedOut: () -> Unit = {}) {
        repository.supabaseService.currentAccessToken = null
        _sessionState.value = UserSessionState.Guest
        prefs?.edit()?.remove(KEY_LOGGED_IN_USER_ID)?.apply()
        onSignedOut()
    }
}

class UserAuthViewModelFactory(
    private val repository: TaskaRepository,
    private val prefs: SharedPreferences? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UserAuthViewModel(repository, prefs) as T
    }
}
