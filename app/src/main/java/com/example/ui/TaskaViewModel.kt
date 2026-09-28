package com.example.ui

import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.AudioVoiceHelper
import com.example.ai.TaskaGeminiModels
import com.example.ai.TaskaGeminiService
import com.example.data.BookingEntity
import com.example.data.ChatMessageEntity
import com.example.data.ConversationSessionEntity
import com.example.data.InboxNotificationEntity
import com.example.data.MarketplaceRequestEntity
import com.example.data.SavedLocationEntity
import com.example.data.ServiceDemandEntity
import com.example.data.SupabaseChatMessage
import com.example.data.Task
import com.example.data.TaskaCategory
import com.example.data.TaskaRepository
import com.example.data.TaskerEntity
import com.example.data.TaskerReviewEntity
import com.example.data.TaskerRoleType
import com.example.data.UserAccountEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class TaskaTab {
    ASK_TASKA,
    BOOKINGS, // My Tasks & Private Chats
    EXPLORE,
    INBOX,
    TASKER_MODE // Profile, Earn with Taska, Credits & Settings
}

enum class AuthScreenState {
    NONE,
    CHOICE,
    SIGN_IN,
    SIGN_UP
}

data class LiveVoiceTurn(
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class TaskaViewModel(
    private val repository: TaskaRepository,
    private val geminiService: TaskaGeminiService,
    private val audioVoiceHelper: AudioVoiceHelper,
    private val prefs: SharedPreferences? = null
) : ViewModel() {

    companion object {
        private const val KEY_HAS_SEEN_WELCOME = "has_seen_welcome_v1"
        private const val KEY_LOGGED_IN_USER_ID = "logged_in_user_id_v1"
        private const val KEY_DARK_MODE = "taska_dark_mode"
        private const val KEY_AREA_LABEL = "taska_area_label"
        private const val KEY_SEARCH_RADIUS = "taska_search_radius"
        private const val KEY_GUEST_CLIENT_KEY = "taska_guest_client_key"
    }

    val isSupabaseConfigured: Boolean
        get() = repository.supabaseService.isConfigured

    val tasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val taskers: StateFlow<List<TaskerEntity>> = repository.allTaskers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookings: StateFlow<List<BookingEntity>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeConversationSessionId = MutableStateFlow("default")
    val activeConversationSessionId: StateFlow<String> = _activeConversationSessionId.asStateFlow()

    val chatMessages: StateFlow<List<ChatMessageEntity>> = combine(
        repository.allChatMessages,
        _activeConversationSessionId
    ) { allMsgs, sessionId ->
        val threadFiltered = allMsgs.filter { msg ->
            msg.threadId == sessionId || (sessionId == "default" && msg.threadId.isBlank())
        }
        // Ensure there is never a duplicate initial greeting message
        var seenGreeting = false
        threadFiltered.filter { msg ->
            if (!msg.isFromUser && msg.text.startsWith("Hi — I'm Taska")) {
                if (seenGreeting) false else {
                    seenGreeting = true
                    true
                }
            } else {
                true
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversationSessions: StateFlow<List<ConversationSessionEntity>> = repository.allConversationSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedLocations: StateFlow<List<SavedLocationEntity>> = repository.allSavedLocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val taskerReviews: StateFlow<List<TaskerReviewEntity>> = repository.allTaskerReviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val marketplaceRequests: StateFlow<List<MarketplaceRequestEntity>> = repository.allMarketplaceRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inboxNotifications: StateFlow<List<InboxNotificationEntity>> = repository.allInboxNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val serviceDemands: StateFlow<List<ServiceDemandEntity>> = repository.allServiceDemands
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dedicated full-screen sub-navigation states to avoid long scrolling
    private val _showConversationHistoryScreen = MutableStateFlow(false)
    val showConversationHistoryScreen: StateFlow<Boolean> = _showConversationHistoryScreen.asStateFlow()

    private val _showEarningProfileScreen = MutableStateFlow(false)
    val showEarningProfileScreen: StateFlow<Boolean> = _showEarningProfileScreen.asStateFlow()

    private val _showSettingsScreen = MutableStateFlow(false)
    val showSettingsScreen: StateFlow<Boolean> = _showSettingsScreen.asStateFlow()

    private val _showLocationManagerScreen = MutableStateFlow(false)
    val showLocationManagerScreen: StateFlow<Boolean> = _showLocationManagerScreen.asStateFlow()

    // First-time install Welcome Screen state
    private val _showWelcomeScreen = MutableStateFlow(
        !(prefs?.getBoolean(KEY_HAS_SEEN_WELCOME, false) ?: false)
    )
    val showWelcomeScreen: StateFlow<Boolean> = _showWelcomeScreen.asStateFlow()

    // Dark Mode state
    private val _isDarkMode = MutableStateFlow(
        prefs?.getBoolean(KEY_DARK_MODE, false) ?: false
    )
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Current authenticated user (null = Guest mode; can still explore & Ask Taska freely)
    private val _currentUser = MutableStateFlow<UserAccountEntity?>(null)
    val currentUser: StateFlow<UserAccountEntity?> = _currentUser.asStateFlow()

    // Sign-In / Sign-Up screen navigation state
    private val _authScreenState = MutableStateFlow(AuthScreenState.NONE)
    val authScreenState: StateFlow<AuthScreenState> = _authScreenState.asStateFlow()

    // Context message shown when a guest triggers an auth-gated feature
    private val _authPromptReason = MutableStateFlow<String?>(null)
    val authPromptReason: StateFlow<String?> = _authPromptReason.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _showAccountSheet = MutableStateFlow(false)
    val showAccountSheet: StateFlow<Boolean> = _showAccountSheet.asStateFlow()

    // Holds a pending Tasker booking if a guest clicked "Book privately" before signing in
    private var pendingBookingAfterAuth: Pair<TaskerEntity, String>? = null

    private val _currentTab = MutableStateFlow(TaskaTab.ASK_TASKA)
    val currentTab: StateFlow<TaskaTab> = _currentTab.asStateFlow()

    private val _selectedArea = MutableStateFlow(
        prefs?.getString(KEY_AREA_LABEL, "")
            ?.takeIf { !it.equals("UNZA Campus Hub", ignoreCase = true) }
            .orEmpty()
    )
    val selectedArea: StateFlow<String> = _selectedArea.asStateFlow()

    private val _searchRadiusKm = MutableStateFlow(
        prefs?.getString(KEY_SEARCH_RADIUS, "10") ?: "10"
    )
    val searchRadiusKm: StateFlow<String> = _searchRadiusKm.asStateFlow()

    private val _showLocationPickerSheet = MutableStateFlow(false)
    val showLocationPickerSheet: StateFlow<Boolean> = _showLocationPickerSheet.asStateFlow()

    private val _showDemandSheet = MutableStateFlow(false)
    val showDemandSheet: StateFlow<Boolean> = _showDemandSheet.asStateFlow()

    private val _showCreateTaskSheet = MutableStateFlow(false)
    val showCreateTaskSheet: StateFlow<Boolean> = _showCreateTaskSheet.asStateFlow()

    private val _createTaskTargetTasker = MutableStateFlow<TaskerEntity?>(null)
    val createTaskTargetTasker: StateFlow<TaskerEntity?> = _createTaskTargetTasker.asStateFlow()

    private val _selectedTaskerForProfile = MutableStateFlow<TaskerEntity?>(null)
    val selectedTaskerForProfile: StateFlow<TaskerEntity?> = _selectedTaskerForProfile.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _exploreSearchQuery = MutableStateFlow("")
    val exploreSearchQuery: StateFlow<String> = _exploreSearchQuery.asStateFlow()

    private val _exploreMinRating = MutableStateFlow(0.0)
    val exploreMinRating: StateFlow<Double> = _exploreMinRating.asStateFlow()

    private val _exploreVerifiedOnly = MutableStateFlow(false)
    val exploreVerifiedOnly: StateFlow<Boolean> = _exploreVerifiedOnly.asStateFlow()

    private val _selectedAiModel = MutableStateFlow(TaskaGeminiModels.FLASH)
    val selectedAiModel: StateFlow<String> = _selectedAiModel.asStateFlow()

    private val _useMapsGrounding = MutableStateFlow(true)
    val useMapsGrounding: StateFlow<Boolean> = _useMapsGrounding.asStateFlow()

    private val _useSearchGrounding = MutableStateFlow(true)
    val useSearchGrounding: StateFlow<Boolean> = _useSearchGrounding.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _isRecordingAudio = MutableStateFlow(false)
    val isRecordingAudio: StateFlow<Boolean> = _isRecordingAudio.asStateFlow()

    private val _isSpeakingTts = MutableStateFlow(false)
    val isSpeakingTts: StateFlow<Boolean> = _isSpeakingTts.asStateFlow()

    private val _fastEstimateText = MutableStateFlow<String?>(null)
    val fastEstimateText: StateFlow<String?> = _fastEstimateText.asStateFlow()

    private val _activeBookingChatId = MutableStateFlow<Int?>(null)
    val activeBookingChatId: StateFlow<Int?> = _activeBookingChatId.asStateFlow()

    private val _liveSupabaseChatMessages = MutableStateFlow<List<SupabaseChatMessage>>(emptyList())
    val liveSupabaseChatMessages: StateFlow<List<SupabaseChatMessage>> = _liveSupabaseChatMessages.asStateFlow()

    private val _showHowItWorksModal = MutableStateFlow(false)
    val showHowItWorksModal: StateFlow<Boolean> = _showHowItWorksModal.asStateFlow()

    private val _showLiveVoiceModal = MutableStateFlow(false)
    val showLiveVoiceModal: StateFlow<Boolean> = _showLiveVoiceModal.asStateFlow()

    private val _liveVoiceHistory = MutableStateFlow<List<LiveVoiceTurn>>(
        listOf(
            LiveVoiceTurn(
                isUser = false,
                text = "Hi! Taska Live Voice is active. Tell me what you need done in your area, and I'll match you with verified local Taskers in real time."
            )
        )
    )
    val liveVoiceHistory: StateFlow<List<LiveVoiceTurn>> = _liveVoiceHistory.asStateFlow()

    private val _liveMatchedTaskers = MutableStateFlow<List<TaskerEntity>>(emptyList())
    val liveMatchedTaskers: StateFlow<List<TaskerEntity>> = _liveMatchedTaskers.asStateFlow()

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
            repository.syncAnnouncementsFromSupabase()
            val savedUserId = prefs?.getInt(KEY_LOGGED_IN_USER_ID, -1) ?: -1
            if (savedUserId > 0) {
                val restoredUser = repository.getUserById(savedUserId)
                if (restoredUser != null) {
                    val cleanedUser = if (restoredUser.defaultArea.equals("UNZA Campus Hub", ignoreCase = true)) {
                        restoredUser.copy(defaultArea = "")
                    } else {
                        restoredUser
                    }
                    _currentUser.value = cleanedUser
                    if (cleanedUser.defaultArea.isNotBlank()) {
                        _selectedArea.value = cleanedUser.defaultArea
                    }
                }
            }
        }
        viewModelScope.launch {
            repository.allSavedLocations.collect { locations ->
                if (_selectedArea.value.isBlank() && locations.isNotEmpty()) {
                    val primary = locations.firstOrNull { it.isPrimary } ?: locations.first()
                    _selectedArea.value = primary.areaName
                    _searchRadiusKm.value = primary.radiusKm.toString()
                }
            }
        }
    }

    private fun getOrCreateGuestClientKey(): String {
        val existing = prefs?.getString(KEY_GUEST_CLIENT_KEY, null)
        if (!existing.isNullOrBlank()) return existing
        val generated = UUID.randomUUID().toString()
        prefs?.edit()?.putString(KEY_GUEST_CLIENT_KEY, generated)?.apply()
        return generated
    }

    // --- Welcome Screen & Auth Actions ---

    fun completeWelcomeAndExplore() {
        prefs?.edit()?.putBoolean(KEY_HAS_SEEN_WELCOME, true)?.apply()
        _showWelcomeScreen.value = false
        _authScreenState.value = AuthScreenState.NONE
    }

    fun completeWelcomeAndOpenAuth(step: AuthScreenState = AuthScreenState.CHOICE) {
        prefs?.edit()?.putBoolean(KEY_HAS_SEEN_WELCOME, true)?.apply()
        _showWelcomeScreen.value = false
        _authErrorMessage.value = null
        _authPromptReason.value = null
        _authScreenState.value = step
    }

    fun replayWelcomeScreen() {
        _showAccountSheet.value = false
        _showHowItWorksModal.value = false
        _showWelcomeScreen.value = true
    }

    fun toggleDarkMode(enabled: Boolean) {
        com.example.ui.theme.TaskaThemeState.isDark = enabled
        _isDarkMode.value = enabled
        prefs?.edit()?.putBoolean(KEY_DARK_MODE, enabled)?.apply()
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
        pendingBookingAfterAuth = null
        _authScreenState.value = AuthScreenState.NONE
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    fun setShowAccountSheet(show: Boolean) {
        _showAccountSheet.value = show
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authErrorMessage.value = "Please enter both your email and password."
            return
        }
        viewModelScope.launch {
            val result = repository.signInUser(email, password)
            result.onSuccess { account ->
                onAuthenticatedSuccess(account, isNewSignup = false)
            }.onFailure { err ->
                _authErrorMessage.value = err.message ?: "Unable to sign in. Please check your credentials."
            }
        }
    }

    fun signUp(
        fullName: String,
        email: String,
        password: String,
        defaultArea: String,
        memberRole: String,
        age: String = "",
        gender: String = ""
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
                onAuthenticatedSuccess(account, isNewSignup = true)
            }.onFailure { err ->
                _authErrorMessage.value = err.message ?: "Could not create account."
            }
        }
    }

    fun onUserAuthenticatedFromAuthViewModel(account: UserAccountEntity, isNewSignup: Boolean = false) {
        viewModelScope.launch {
            onAuthenticatedSuccess(account, isNewSignup)
        }
    }

    fun syncDatabaseWithSupabase() {
        viewModelScope.launch {
            repository.syncDatabaseWithSupabase(_currentUser.value)
            _statusBannerMessage.value = "Latest marketplace listings and notifications refreshed."
        }
    }

    fun showBannerMessage(message: String) {
        _statusBannerMessage.value = message
    }

    private suspend fun onAuthenticatedSuccess(account: UserAccountEntity, isNewSignup: Boolean) {
        val cleanDefaultArea = if (account.defaultArea.equals("UNZA Campus Hub", ignoreCase = true)) "" else account.defaultArea
        val cleanedAccount = account.copy(defaultArea = cleanDefaultArea)
        _currentUser.value = cleanedAccount
        if (cleanDefaultArea.isNotBlank()) {
            _selectedArea.value = cleanDefaultArea
        }
        prefs?.edit()
            ?.putBoolean(KEY_HAS_SEEN_WELCOME, true)
            ?.putInt(KEY_LOGGED_IN_USER_ID, cleanedAccount.id)
            ?.putString(KEY_AREA_LABEL, _selectedArea.value)
            ?.apply()

        _authErrorMessage.value = null
        _authPromptReason.value = null
        _authScreenState.value = AuthScreenState.NONE

        val pending = pendingBookingAfterAuth
        pendingBookingAfterAuth = null
        if (pending != null) {
            val (tasker, summary) = pending
            executePrivateBooking(tasker, summary)
        } else {
            _statusBannerMessage.value = if (isNewSignup) {
                "Welcome to Taska, ${cleanedAccount.fullName}! Set your location anytime to match with local Taskers."
            } else {
                "Signed in as ${cleanedAccount.fullName}."
            }
        }
    }

    fun signOut() {
        _currentUser.value = null
        _activeBookingChatId.value = null
        _showAccountSheet.value = false
        _showEarningProfileScreen.value = false
        _showSettingsScreen.value = false
        prefs?.edit()?.remove(KEY_LOGGED_IN_USER_ID)?.apply()
        _statusBannerMessage.value = "Signed out. You can still explore Taska and chat with Ask Taska as a guest!"
    }

    fun updateDisplayName(newName: String) {
        val user = _currentUser.value ?: return
        if (newName.trim().length < 2) return
        viewModelScope.launch {
            val updated = repository.updateUserDisplayName(user, newName)
            _currentUser.value = updated
            _statusBannerMessage.value = "Saved. Your display name is updated to ${updated.fullName}."
        }
    }

    fun updateUserFullProfile(
        fullName: String,
        age: String,
        gender: String,
        bio: String,
        defaultArea: String
    ) {
        val user = _currentUser.value ?: return
        if (fullName.trim().length < 2) {
            _statusBannerMessage.value = "Display name must be at least 2 characters."
            return
        }
        viewModelScope.launch {
            val updated = repository.updateUserFullProfile(
                user = user,
                fullName = fullName,
                age = age,
                gender = gender,
                bio = bio,
                defaultArea = defaultArea
            )
            _currentUser.value = updated
            if (defaultArea.isNotBlank()) {
                _selectedArea.value = defaultArea.trim()
                prefs?.edit()?.putString(KEY_AREA_LABEL, defaultArea.trim())?.apply()
            }
            _statusBannerMessage.value = "Profile updated & saved!"
        }
    }

    // --- Dedicated Sub-Screens ---

    fun setShowConversationHistoryScreen(show: Boolean) {
        _showConversationHistoryScreen.value = show
    }

    fun setShowEarningProfileScreen(show: Boolean) {
        _showEarningProfileScreen.value = show
    }

    fun setShowSettingsScreen(show: Boolean) {
        _showSettingsScreen.value = show
    }

    fun setShowLocationManagerScreen(show: Boolean) {
        _showLocationManagerScreen.value = show
    }

    // --- Saved Custom Locations & Pin Drops ---

    fun saveCustomPinLocation(
        label: String,
        areaName: String,
        latitude: Double = -15.3875,
        longitude: Double = 28.3228,
        radiusKm: Int = 10,
        setAsPrimary: Boolean = true
    ) {
        val cleanArea = areaName.trim()
        if (cleanArea.isEmpty()) {
            _statusBannerMessage.value = "Please enter or detect an area name to save."
            return
        }
        viewModelScope.launch {
            val saved = repository.saveCustomLocation(
                label = label.trim().ifBlank { cleanArea },
                areaName = cleanArea,
                latitude = latitude,
                longitude = longitude,
                radiusKm = radiusKm,
                userEmail = _currentUser.value?.email.orEmpty(),
                setAsPrimary = setAsPrimary
            )
            if (setAsPrimary || _selectedArea.value.isBlank()) {
                setSelectedArea(saved.areaName, saved.radiusKm.toString())
            } else {
                _statusBannerMessage.value = "Saved custom location '${saved.label}' (${saved.areaName})."
            }
            val user = _currentUser.value
            if (user != null && (setAsPrimary || user.defaultArea.isBlank())) {
                val updatedUser = repository.updateUserFullProfile(
                    user = user,
                    fullName = user.fullName,
                    age = user.age,
                    gender = user.gender,
                    bio = user.bio,
                    defaultArea = saved.areaName
                )
                _currentUser.value = updatedUser
            }
        }
    }

    fun selectSavedLocation(location: SavedLocationEntity) {
        setSelectedArea(location.areaName, location.radiusKm.toString())
    }

    fun deleteSavedLocation(id: Int) {
        viewModelScope.launch {
            repository.deleteSavedLocation(id)
            _statusBannerMessage.value = "Removed saved location."
        }
    }

    // --- Conversation Sessions (Ask Taska History) ---

    fun openConversationSession(sessionId: String) {
        _activeConversationSessionId.value = sessionId
        _showConversationHistoryScreen.value = false
        _currentTab.value = TaskaTab.ASK_TASKA
    }

    fun startNewConversationSession() {
        val newSessionId = "session_${System.currentTimeMillis()}"
        viewModelScope.launch {
            val areaText = _selectedArea.value.ifBlank { "your area" }
            val greeting = "Hi — I'm Taska. Tell me what you need, when you need it, and where in $areaText. I'll match you with verified local Taskers."
            repository.addChatMessage(
                ChatMessageEntity(
                    threadId = newSessionId,
                    isFromUser = false,
                    text = greeting,
                    modelTag = TaskaGeminiModels.FLASH
                )
            )
            repository.saveOrUpdateConversationSession(
                sessionId = newSessionId,
                title = "New Conversation",
                previewText = greeting,
                area = _selectedArea.value.ifBlank { "All Areas" }
            )
            _activeConversationSessionId.value = newSessionId
            _showConversationHistoryScreen.value = false
            _statusBannerMessage.value = "Started a new Ask Taska conversation."
        }
    }

    fun deleteConversationSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteConversationSession(sessionId)
            if (_activeConversationSessionId.value == sessionId) {
                _activeConversationSessionId.value = "default"
                repository.ensureSeedData()
            }
            _statusBannerMessage.value = "Conversation removed from history."
        }
    }

    // --- Navigation & Location Actions ---

    fun selectTab(tab: TaskaTab) {
        _currentTab.value = tab
    }

    fun setSelectedArea(area: String, radiusKm: String = _searchRadiusKm.value) {
        val cleanArea = area.trim()
        val cleanRadius = radiusKm.trim().ifBlank { "10" }
        _selectedArea.value = cleanArea
        _searchRadiusKm.value = cleanRadius
        prefs?.edit()
            ?.putString(KEY_AREA_LABEL, cleanArea)
            ?.putString(KEY_SEARCH_RADIUS, cleanRadius)
            ?.apply()
        _showLocationPickerSheet.value = false
        if (cleanArea.isNotBlank()) {
            _statusBannerMessage.value = "Location set to $cleanArea (${cleanRadius}km radius)."
        }
    }

    fun setShowLocationPickerSheet(show: Boolean) {
        _showLocationPickerSheet.value = show
    }

    fun setShowDemandSheet(show: Boolean) {
        _showDemandSheet.value = show
    }

    fun openCreateTaskSheet(targetTasker: TaskerEntity? = null) {
        if (_currentUser.value == null) {
            if (targetTasker != null) {
                pendingBookingAfterAuth = targetTasker to ""
            }
            openAuthFlow(
                step = AuthScreenState.CHOICE,
                reason = "Sign in or create a free account to create a Taska request and invite Taskers."
            )
            return
        }
        _createTaskTargetTasker.value = targetTasker
        _showCreateTaskSheet.value = true
    }

    fun closeCreateTaskSheet() {
        _showCreateTaskSheet.value = false
        _createTaskTargetTasker.value = null
    }

    fun openTaskerProfileDetail(tasker: TaskerEntity?) {
        _selectedTaskerForProfile.value = tasker
    }

    fun toggleFavouriteTasker(tasker: TaskerEntity) {
        if (_currentUser.value == null) {
            openAuthFlow(
                step = AuthScreenState.CHOICE,
                reason = "Sign in to save favourite Taskers to your account."
            )
            return
        }
        viewModelScope.launch {
            val updated = repository.toggleTaskerFavourite(tasker)
            if (_selectedTaskerForProfile.value?.id == updated.id) {
                _selectedTaskerForProfile.value = updated
            }
            _statusBannerMessage.value = if (updated.isFavourite) {
                "Saved ${updated.name} to your favourite Taskers."
            } else {
                "Removed ${updated.name} from favourites."
            }
        }
    }

    fun submitServiceDemand(requestText: String, broadArea: String) {
        if (requestText.trim().isEmpty()) return
        viewModelScope.launch {
            val demand = repository.submitServiceDemand(
                requestText = requestText,
                broadArea = broadArea.ifBlank { _selectedArea.value.ifBlank { "Local Area" } },
                requesterId = _currentUser.value?.supabaseUserId ?: _currentUser.value?.id?.toString()
            )
            _showDemandSheet.value = false
            _statusBannerMessage.value = "Request logged for ${demand.broadArea}! Local Taskers in this category will be notified."
        }
    }

    fun setCategoryFilter(categoryId: String?) {
        _selectedCategoryFilter.value = categoryId
    }

    fun setExploreSearchQuery(query: String) {
        _exploreSearchQuery.value = query
    }

    fun setExploreMinRating(minRating: Double) {
        _exploreMinRating.value = minRating
    }

    fun toggleExploreVerifiedOnly() {
        _exploreVerifiedOnly.value = !_exploreVerifiedOnly.value
    }

    fun submitReviewForTasker(
        tasker: TaskerEntity,
        ratingStars: Int,
        comment: String,
        serviceTitle: String = tasker.specialty.removePrefix("Verified · ")
    ) {
        if (_currentUser.value == null) {
            openAuthFlow(
                step = AuthScreenState.CHOICE,
                reason = "Sign in to leave a verified rating and review."
            )
            return
        }
        viewModelScope.launch {
            val reviewer = _currentUser.value?.fullName?.ifBlank { "Verified Taska Client" } ?: "Verified Taska Client"
            repository.submitTaskerReview(
                taskerId = tasker.id,
                taskerName = tasker.name,
                reviewerName = reviewer,
                ratingStars = ratingStars,
                comment = comment,
                serviceTitle = serviceTitle
            )
            val refreshed = repository.getAllTaskersOnce().firstOrNull { it.id == tasker.id }
            if (refreshed != null && _selectedTaskerForProfile.value?.id == refreshed.id) {
                _selectedTaskerForProfile.value = refreshed
            }
            _statusBannerMessage.value = "Thank you! Your ★ $ratingStars.0 review for ${tasker.name} is now live."
        }
    }

    fun setSelectedAiModel(model: String) {
        _selectedAiModel.value = model
    }

    fun toggleMapsGrounding() {
        _useMapsGrounding.value = !_useMapsGrounding.value
    }

    fun toggleSearchGrounding() {
        _useSearchGrounding.value = !_useSearchGrounding.value
    }

    fun setShowHowItWorks(show: Boolean) {
        _showHowItWorksModal.value = show
    }

    fun setShowLiveVoiceModal(show: Boolean) {
        _showLiveVoiceModal.value = show
        if (!show) {
            audioVoiceHelper.stopSpeaking()
        }
    }

    fun openBookingChat(bookingId: Int?) {
        if (bookingId != null && _currentUser.value == null) {
            openAuthFlow(
                step = AuthScreenState.CHOICE,
                reason = "Sign in or create a free account to open Private Task Chats and coordinate bookings."
            )
            return
        }
        _activeBookingChatId.value = bookingId
        if (bookingId != null) {
            refreshActiveBookingLiveChat()
        } else {
            _liveSupabaseChatMessages.value = emptyList()
        }
    }

    fun refreshActiveBookingLiveChat() {
        val activeId = _activeBookingChatId.value ?: return
        val booking = bookings.value.firstOrNull { it.id == activeId } ?: return
        viewModelScope.launch {
            val currentUid = _currentUser.value?.supabaseUserId
            var threadId = booking.supabaseThreadId
            if (threadId.isBlank() && booking.supabaseRequestId.contains("-")) {
                threadId = repository.supabaseService.getOrCreateChatThread(
                    requestId = booking.supabaseRequestId,
                    requesterId = currentUid
                ).orEmpty()
            }
            if (threadId.isNotBlank()) {
                val remoteMsgs = repository.supabaseService.fetchChatMessagesForThread(
                    threadId = threadId,
                    currentUserId = currentUid,
                    counterpartyName = booking.taskerName
                )
                _liveSupabaseChatMessages.value = remoteMsgs
            }
        }
    }

    fun deleteTaskRequirement(taskId: Int) {
        viewModelScope.launch {
            repository.deleteTaskRequirement(taskId)
            _statusBannerMessage.value = "Task requirement removed from your local marketplace."
        }
    }

    fun dismissBannerMessage() {
        _statusBannerMessage.value = null
    }

    fun sendUserMessage(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return

        viewModelScope.launch {
            _isAiLoading.value = true
            val sessionId = _activeConversationSessionId.value
            val priorHistory = chatMessages.value.map { it.isFromUser to it.text }
            val areaLabel = _selectedArea.value.ifBlank { "Local Area" }

            repository.addChatMessage(
                ChatMessageEntity(
                    threadId = sessionId,
                    isFromUser = true,
                    text = clean
                )
            )

            val allTaskersList = repository.getAllTaskersOnce()

            val supabaseIntent = repository.supabaseService.invokeParseTaskaIntent(
                text = clean,
                guestClientKey = getOrCreateGuestClientKey()
            )

            val reply = geminiService.sendConciergeChat(
                conversationTurns = priorHistory,
                userPrompt = clean,
                currentArea = areaLabel,
                availableTaskers = allTaskersList,
                selectedModel = _selectedAiModel.value,
                useMapsGrounding = _useMapsGrounding.value,
                useSearchGrounding = _useSearchGrounding.value
            )

            val finalText = if (supabaseIntent != null && supabaseIntent.reply.isNotBlank()) {
                supabaseIntent.reply
            } else {
                reply.text
            }
            val badgeList = buildList {
                if (supabaseIntent != null) add("Verified Match (${supabaseIntent.category})")
                addAll(reply.groundingBadges)
            }

            repository.addChatMessage(
                ChatMessageEntity(
                    threadId = sessionId,
                    isFromUser = false,
                    text = finalText,
                    matchedTaskerIdsCsv = reply.matchedTaskerIds.joinToString(","),
                    modelTag = reply.modelUsed,
                    groundingInfo = badgeList.joinToString(" • "),
                    canRequestMissingService = true
                )
            )
            repository.saveOrUpdateConversationSession(
                sessionId = sessionId,
                title = clean,
                previewText = finalText,
                area = areaLabel
            )
            _isAiLoading.value = false
        }
    }

    fun analyzePhotoAndSendToChat(bitmap: Bitmap, caption: String) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val sessionId = _activeConversationSessionId.value
            val areaLabel = _selectedArea.value.ifBlank { "Local Area" }
            val userNote = caption.ifBlank { "Uploaded a photo to analyze for a local task." }
            repository.addChatMessage(
                ChatMessageEntity(
                    threadId = sessionId,
                    isFromUser = true,
                    text = userNote,
                    attachedPhotoSummary = "Photo attached (${bitmap.width}×${bitmap.height}px)"
                )
            )

            val allTaskersList = repository.getAllTaskersOnce()
            val reply = geminiService.analyzeTaskPhoto(
                bitmap = bitmap,
                userCaption = userNote,
                currentArea = areaLabel,
                availableTaskers = allTaskersList
            )

            repository.addChatMessage(
                ChatMessageEntity(
                    threadId = sessionId,
                    isFromUser = false,
                    text = reply.text,
                    matchedTaskerIdsCsv = reply.matchedTaskerIds.joinToString(","),
                    modelTag = reply.modelUsed,
                    groundingInfo = reply.groundingBadges.joinToString(" • "),
                    canRequestMissingService = true
                )
            )
            repository.saveOrUpdateConversationSession(
                sessionId = sessionId,
                title = userNote,
                previewText = reply.text,
                area = areaLabel
            )
            _isAiLoading.value = false
        }
    }

    fun startMicrophoneRecording(): Boolean {
        val started = audioVoiceHelper.startMicRecording()
        _isRecordingAudio.value = started
        if (!started) {
            _statusBannerMessage.value = "Microphone is unavailable on this device."
        }
        return started
    }

    fun stopMicrophoneAndTranscribe(forLiveVoice: Boolean = false) {
        if (!_isRecordingAudio.value) return
        _isRecordingAudio.value = false

        viewModelScope.launch {
            _isAiLoading.value = true
            val base64Audio = audioVoiceHelper.stopMicRecordingAndGetBase64() ?: ""
            val transcribed = geminiService.transcribeAudioBase64(base64Audio)
            _isAiLoading.value = false

            if (transcribed.isBlank()) {
                _statusBannerMessage.value = "No clear speech detected. Please speak into the microphone and try again."
                return@launch
            }

            if (forLiveVoice) {
                sendLiveVoiceTurn(transcribed)
            } else {
                _statusBannerMessage.value = "Voice note transcribed"
                sendUserMessage(transcribed)
            }
        }
    }

    fun speakWithGeminiTts(text: String) {
        viewModelScope.launch {
            _isSpeakingTts.value = true
            val audioPair = geminiService.synthesizeSpeechBase64(text)
            audioVoiceHelper.playGeminiOrFallbackTts(text, audioPair)
            _isSpeakingTts.value = false
        }
    }

    fun sendLiveVoiceTurn(spokenText: String) {
        val clean = spokenText.trim()
        if (clean.isEmpty()) return

        viewModelScope.launch {
            _liveVoiceHistory.value = _liveVoiceHistory.value + LiveVoiceTurn(isUser = true, text = clean)
            _isAiLoading.value = true

            val allTaskersList = repository.getAllTaskersOnce()
            val reply = geminiService.sendLiveVoiceTurn(
                spokenUserText = clean,
                currentArea = _selectedArea.value.ifBlank { "Local Area" },
                availableTaskers = allTaskersList
            )

            _liveVoiceHistory.value = _liveVoiceHistory.value + LiveVoiceTurn(isUser = false, text = reply.text)
            _liveMatchedTaskers.value = allTaskersList.filter { it.id in reply.matchedTaskerIds }
            _isAiLoading.value = false

            val audioPair = geminiService.synthesizeSpeechBase64(reply.text)
            audioVoiceHelper.playGeminiOrFallbackTts(reply.text, audioPair)
        }
    }

    fun requestFastTaskEstimate(queryOrCategory: String) {
        viewModelScope.launch {
            _fastEstimateText.value = "Calculating instant local price estimate…"
            val estimate = geminiService.generateFastTaskEstimate(queryOrCategory, _selectedArea.value.ifBlank { "Local Area" })
            _fastEstimateText.value = estimate
        }
    }

    fun bookTaskerPrivately(tasker: TaskerEntity, taskSummary: String) {
        if (_currentUser.value == null) {
            pendingBookingAfterAuth = tasker to taskSummary
            openAuthFlow(
                step = AuthScreenState.CHOICE,
                reason = "Sign in or create a free account to book ${tasker.name} privately and unlock Private Task Chats."
            )
            return
        }

        viewModelScope.launch {
            executePrivateBooking(tasker, taskSummary)
        }
    }

    fun submitCreateTaskSheet(
        shortTitle: String,
        requestText: String,
        budgetZmw: String,
        negotiable: Boolean,
        targetTasker: TaskerEntity?,
        categoryId: String = TaskaCategory.DEVICE_REPAIRS.id,
        urgencyOrTiming: String = "Flexible",
        customArea: String = ""
    ) {
        if (_currentUser.value == null) {
            closeCreateTaskSheet()
            openAuthFlow(
                step = AuthScreenState.CHOICE,
                reason = "Sign in to create a Taska request."
            )
            return
        }
        viewModelScope.launch {
            val effectiveArea = customArea.trim().ifBlank { _selectedArea.value.ifBlank { "Local Area" } }
            val requesterId = _currentUser.value?.supabaseUserId ?: _currentUser.value?.id?.toString()
            if (targetTasker != null) {
                val bookingId = repository.createPrivateBooking(
                    tasker = targetTasker,
                    taskDescription = requestText.ifBlank { shortTitle },
                    area = effectiveArea,
                    shortTitle = shortTitle,
                    clientBudget = budgetZmw,
                    budgetNegotiable = negotiable,
                    requesterId = requesterId,
                    categoryId = categoryId,
                    urgencyOrTiming = urgencyOrTiming
                )
                closeCreateTaskSheet()
                _selectedTaskerForProfile.value = null
                _statusBannerMessage.value = "Booking confirmed! Invited ${targetTasker.name} in a private task chat."
                _currentTab.value = TaskaTab.BOOKINGS
                _activeBookingChatId.value = bookingId.toInt()
                refreshActiveBookingLiveChat()
            } else {
                val createdReq = repository.createMarketplaceTaskaRequest(
                    shortTitle = shortTitle,
                    requestText = requestText,
                    area = effectiveArea,
                    clientBudget = budgetZmw,
                    budgetNegotiable = negotiable,
                    requesterId = requesterId,
                    categoryId = categoryId,
                    urgencyOrTiming = urgencyOrTiming
                )
                closeCreateTaskSheet()
                _selectedTaskerForProfile.value = null
                _statusBannerMessage.value = "Taska '${createdReq.title}' published in ${createdReq.area}! Verified Taskers can now accept or message you."
                _currentTab.value = TaskaTab.BOOKINGS
            }
        }
    }

    private suspend fun executePrivateBooking(tasker: TaskerEntity, taskSummary: String) {
        val effectiveArea = _selectedArea.value.ifBlank { tasker.area.ifBlank { "Local Area" } }
        val summary = taskSummary.ifBlank {
            "Need ${tasker.specialty.removePrefix("Verified · ")} in $effectiveArea"
        }
        val bookingId = repository.createPrivateBooking(
            tasker = tasker,
            taskDescription = summary,
            area = effectiveArea,
            shortTitle = "${tasker.name} · ${tasker.specialty.removePrefix("Verified · ")}",
            requesterId = _currentUser.value?.supabaseUserId ?: _currentUser.value?.id?.toString()
        )
        _selectedTaskerForProfile.value = null
        _statusBannerMessage.value = "Booked ${tasker.name} privately! Your phone number stays private."
        _currentTab.value = TaskaTab.BOOKINGS
        _activeBookingChatId.value = bookingId.toInt()
        refreshActiveBookingLiveChat()
    }

    fun issueCompletionPin(booking: BookingEntity) {
        viewModelScope.launch {
            val pin = repository.issueOrRefreshCompletionPin(booking)
            _statusBannerMessage.value = "Completion PIN issued: $pin. Share with ${booking.taskerName} only once work is done."
        }
    }

    fun sendPrivateBookingMessage(booking: BookingEntity, messageText: String) {
        if (_currentUser.value == null) {
            openAuthFlow(
                step = AuthScreenState.CHOICE,
                reason = "Sign in or create a free account to send messages in Private Task Chats."
            )
            return
        }
        val clean = messageText.trim()
        if (clean.isEmpty()) return

        viewModelScope.launch {
            val senderId = _currentUser.value?.supabaseUserId
            repository.appendPrivateBookingMessage(
                booking = booking,
                sender = "You",
                message = clean,
                timeLabel = "Now",
                senderUserId = senderId
            )
            refreshActiveBookingLiveChat()
        }
    }

    fun completeAndRateBooking(booking: BookingEntity, rating: Int, writtenReview: String = "") {
        viewModelScope.launch {
            val reviewer = _currentUser.value?.fullName?.ifBlank { "Verified Taska Client" } ?: "Verified Taska Client"
            repository.markBookingCompleted(
                booking = booking,
                rating = rating,
                writtenReview = writtenReview,
                reviewerName = reviewer
            )
            _statusBannerMessage.value = "Task marked Done & rated ★ $rating.0! Verified review published."
        }
    }

    fun saveFullEarningProfile(
        kindRole: TaskerRoleType,
        category: TaskaCategory,
        publicName: String,
        phoneE164: String,
        bio: String,
        firstListingTitle: String,
        firstListingPrice: String,
        area: String,
        businessName: String,
        businessRegNumber: String,
        legalFullName: String,
        nationalIdNumber: String,
        hasIdPhoto: Boolean,
        hasSelfiePhoto: Boolean,
        storePhotoUris: List<String> = emptyList(),
        firstListingPhotoUris: List<String> = emptyList()
    ) {
        if (_currentUser.value == null) {
            openAuthFlow(
                step = AuthScreenState.CHOICE,
                reason = "Sign in first to create an earning profile on Taska."
            )
            return
        }
        if (publicName.trim().length < 2 || phoneE164.trim().isEmpty()) {
            _statusBannerMessage.value = "Add a public earning name and a mobile money / contact number to continue."
            return
        }

        viewModelScope.launch {
            val (_, updatedUser) = repository.saveFullEarningProfile(
                currentUser = _currentUser.value,
                kindRole = kindRole,
                category = category,
                publicName = publicName,
                phoneE164 = phoneE164,
                bio = bio,
                firstListingTitle = firstListingTitle,
                firstListingPrice = firstListingPrice,
                area = area.ifBlank { _selectedArea.value.ifBlank { "Local Area" } },
                businessName = businessName,
                businessRegNumber = businessRegNumber,
                legalFullName = legalFullName,
                nationalIdNumber = nationalIdNumber,
                hasIdPhoto = hasIdPhoto,
                hasSelfiePhoto = hasSelfiePhoto,
                storePhotoUris = storePhotoUris,
                firstListingPhotoUris = firstListingPhotoUris
            )
            if (updatedUser != null) {
                _currentUser.value = updatedUser
            }
            _statusBannerMessage.value = "Your earning profile is active! Taska Credits and listings are now unlocked."
        }
    }

    fun addServiceListing(
        title: String,
        categoryName: String,
        description: String,
        priceZmw: String,
        photoUris: List<String> = emptyList()
    ) {
        if (_currentUser.value == null) {
            openAuthFlow(
                step = AuthScreenState.CHOICE,
                reason = "Sign in to add a service or product listing."
            )
            return
        }
        viewModelScope.launch {
            repository.addServiceListingToUserTasker(
                title = title,
                categoryName = categoryName,
                description = description,
                priceZmw = priceZmw,
                photoUris = photoUris
            )
            _statusBannerMessage.value = "Listing '$title' published with ${photoUris.size} photo(s)!"
        }
    }

    fun updateStorePhotos(photoUris: List<String>) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = repository.updateStorePhotosForUserTasker(user, photoUris)
            if (updated != null) {
                _currentUser.value = updated
            }
            _statusBannerMessage.value = "Store & business gallery updated (${photoUris.size}/6 photos)."
        }
    }

    fun submitCreditTopUp(
        creditsRequested: Int,
        senderName: String,
        senderPhone: String,
        reference: String
    ) {
        val user = _currentUser.value
        if (user == null) {
            openAuthFlow(
                step = AuthScreenState.CHOICE,
                reason = "Sign in to top up your Taska Credits."
            )
            return
        }
        if (!user.hasEarningProfile && user.earningKind.isBlank()) {
            _showEarningProfileScreen.value = true
            _statusBannerMessage.value = "Complete your Earn with Taska profile first to unlock Taska Credits."
            return
        }
        viewModelScope.launch {
            val updated = repository.submitCreditTopUp(
                user = user,
                creditsRequested = creditsRequested,
                senderName = senderName,
                senderPhone = senderPhone,
                reference = reference
            )
            _currentUser.value = updated
            _statusBannerMessage.value = "Payment proof submitted! +$creditsRequested Taska Credits added to your balance."
        }
    }

    fun acceptMarketplaceRequestWithPrice(
        request: MarketplaceRequestEntity,
        proposedPriceZmw: String,
        roleTitle: String
    ) {
        if (_currentUser.value == null) {
            openAuthFlow(
                step = AuthScreenState.CHOICE,
                reason = "Sign in to accept work invitations and propose prices."
            )
            return
        }
        viewModelScope.launch {
            _isAiLoading.value = true
            val draft = geminiService.generateTaskerProposal(
                requestTitle = request.title,
                requestDetails = request.details,
                taskerType = roleTitle
            )
            val priceTag = if (proposedPriceZmw.isNotBlank()) "ZMW ${proposedPriceZmw.trim()}" else request.budget
            repository.updateMarketplaceRequest(
                request.copy(
                    status = "Accepted · $priceTag",
                    proposedPrice = priceTag,
                    aiProposalText = draft
                )
            )
            _isAiLoading.value = false
            _statusBannerMessage.value = "You accepted '${request.title}' ($priceTag). The client can now proceed in Private Chat!"
        }
    }

    fun declineMarketplaceRequest(request: MarketplaceRequestEntity) {
        viewModelScope.launch {
            repository.updateMarketplaceRequest(
                request.copy(status = "Declined")
            )
            _statusBannerMessage.value = "Invitation '${request.title}' declined."
        }
    }

    fun clearChatConversation() {
        viewModelScope.launch {
            val sessionId = _activeConversationSessionId.value
            if (sessionId == "default") {
                repository.clearChat()
                repository.ensureSeedData()
            } else {
                repository.deleteConversationSession(sessionId)
                _activeConversationSessionId.value = "default"
                repository.ensureSeedData()
            }
            _statusBannerMessage.value = "Cleared current Ask Taska chat."
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioVoiceHelper.release()
    }
}

class TaskaViewModelFactory(
    private val repository: TaskaRepository,
    private val geminiService: TaskaGeminiService,
    private val audioVoiceHelper: AudioVoiceHelper,
    private val prefs: SharedPreferences? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TaskaViewModel(repository, geminiService, audioVoiceHelper, prefs) as T
    }
}
