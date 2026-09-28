package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.AudioVoiceHelper
import com.example.ai.TaskaGeminiService
import com.example.data.TaskaDatabase
import com.example.data.TaskaRepository
import com.example.ui.AuthScreenState
import com.example.ui.TaskaTab
import com.example.ui.TaskaViewModel
import com.example.ui.TaskaViewModelFactory
import com.example.ui.UserAuthViewModel
import com.example.ui.UserAuthViewModelFactory
import com.example.ui.components.HowItWorksBottomSheet
import com.example.ui.components.LiveVoiceConciergeModal
import com.example.ui.components.RealWorldLocationAndMapHelper
import com.example.ui.components.TaskaModernBottomBar
import com.example.ui.components.TaskaTopBar
import com.example.ui.screens.AskTaskaChatScreen
import com.example.ui.screens.BookingsAndTrustScreen
import com.example.ui.screens.ConversationHistoryScreen
import com.example.ui.screens.CreateTaskRequestSheet
import com.example.ui.screens.CustomLocationPinMapScreen
import com.example.ui.screens.ExploreMarketplaceScreen
import com.example.ui.screens.LocationAndRadiusPickerSheet
import com.example.ui.screens.RequestMissingServiceDemandSheet
import com.example.ui.screens.TaskaAuthFlowScreen
import com.example.ui.screens.TaskaInboxScreen
import com.example.ui.screens.TaskaWelcomeScreen
import com.example.ui.screens.TaskerModeScreen
import com.example.ui.screens.TaskerProfileDetailSheet
import com.example.ui.screens.UserAccountBottomSheet
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TaskaCream
import com.example.ui.theme.TaskaDeepInk
import com.example.ui.theme.TaskaLime
import com.example.ui.theme.TaskaPureWhite
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = TaskaDatabase.getInstance(applicationContext)
        val repository = TaskaRepository(database.taskaDao())
        val geminiService = TaskaGeminiService()
        val audioVoiceHelper = AudioVoiceHelper(applicationContext)
        val prefs = getSharedPreferences("taska_app_prefs", MODE_PRIVATE)
        val factory = TaskaViewModelFactory(repository, geminiService, audioVoiceHelper, prefs)
        val authFactory = UserAuthViewModelFactory(repository, prefs)

        setContent {
            val viewModel: TaskaViewModel = viewModel(factory = factory)
            val userAuthViewModel: UserAuthViewModel = viewModel(factory = authFactory)
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkMode) {
                TaskaMainApp(
                    viewModel = viewModel,
                    userAuthViewModel = userAuthViewModel
                )
            }
        }
    }
}

@Composable
fun TaskaMainApp(
    viewModel: TaskaViewModel,
    userAuthViewModel: UserAuthViewModel? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isDetectingGps by remember { mutableStateOf(false) }

    val showWelcomeScreen by viewModel.showWelcomeScreen.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authScreenState by viewModel.authScreenState.collectAsStateWithLifecycle()
    val authPromptReason by viewModel.authPromptReason.collectAsStateWithLifecycle()
    val authErrorMessage by viewModel.authErrorMessage.collectAsStateWithLifecycle()
    val userAuthError = userAuthViewModel?.authErrorMessage?.collectAsStateWithLifecycle()?.value
    val showAccountSheet by viewModel.showAccountSheet.collectAsStateWithLifecycle()

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedArea by viewModel.selectedArea.collectAsStateWithLifecycle()
    val searchRadiusKm by viewModel.searchRadiusKm.collectAsStateWithLifecycle()
    val showLocationPickerSheet by viewModel.showLocationPickerSheet.collectAsStateWithLifecycle()
    val showDemandSheet by viewModel.showDemandSheet.collectAsStateWithLifecycle()
    val showCreateTaskSheet by viewModel.showCreateTaskSheet.collectAsStateWithLifecycle()
    val createTaskTargetTasker by viewModel.createTaskTargetTasker.collectAsStateWithLifecycle()
    val selectedTaskerForProfile by viewModel.selectedTaskerForProfile.collectAsStateWithLifecycle()

    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val exploreSearchQuery by viewModel.exploreSearchQuery.collectAsStateWithLifecycle()
    val exploreMinRating by viewModel.exploreMinRating.collectAsStateWithLifecycle()
    val exploreVerifiedOnly by viewModel.exploreVerifiedOnly.collectAsStateWithLifecycle()

    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val taskers by viewModel.taskers.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val liveSupabaseChatMessages by viewModel.liveSupabaseChatMessages.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val conversationSessions by viewModel.conversationSessions.collectAsStateWithLifecycle()
    val activeConversationSessionId by viewModel.activeConversationSessionId.collectAsStateWithLifecycle()
    val savedLocations by viewModel.savedLocations.collectAsStateWithLifecycle()
    val taskerReviews by viewModel.taskerReviews.collectAsStateWithLifecycle()
    val marketplaceRequests by viewModel.marketplaceRequests.collectAsStateWithLifecycle()
    val inboxNotifications by viewModel.inboxNotifications.collectAsStateWithLifecycle()

    val showConversationHistoryScreen by viewModel.showConversationHistoryScreen.collectAsStateWithLifecycle()
    val showEarningProfileScreen by viewModel.showEarningProfileScreen.collectAsStateWithLifecycle()
    val showSettingsScreen by viewModel.showSettingsScreen.collectAsStateWithLifecycle()
    val showLocationManagerScreen by viewModel.showLocationManagerScreen.collectAsStateWithLifecycle()

    val selectedAiModel by viewModel.selectedAiModel.collectAsStateWithLifecycle()
    val useMapsGrounding by viewModel.useMapsGrounding.collectAsStateWithLifecycle()
    val useSearchGrounding by viewModel.useSearchGrounding.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val isRecordingAudio by viewModel.isRecordingAudio.collectAsStateWithLifecycle()
    val fastEstimateText by viewModel.fastEstimateText.collectAsStateWithLifecycle()
    val activeBookingChatId by viewModel.activeBookingChatId.collectAsStateWithLifecycle()
    val showHowItWorks by viewModel.showHowItWorksModal.collectAsStateWithLifecycle()
    val showLiveVoiceModal by viewModel.showLiveVoiceModal.collectAsStateWithLifecycle()
    val liveVoiceHistory by viewModel.liveVoiceHistory.collectAsStateWithLifecycle()
    val liveMatchedTaskers by viewModel.liveMatchedTaskers.collectAsStateWithLifecycle()
    val statusBannerMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

    val gpsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        coroutineScope.launch {
            isDetectingGps = true
            val detected = RealWorldLocationAndMapHelper.detectRealWorldLocation(
                context = context,
                hasLocationPermission = granted
            )
            viewModel.saveCustomPinLocation(
                label = "My Current Location (${detected.source})",
                areaName = detected.areaName,
                latitude = detected.latitude,
                longitude = detected.longitude,
                radiusKm = searchRadiusKm.toIntOrNull() ?: 10,
                setAsPrimary = true
            )
            isDetectingGps = false
        }
    }

    val triggerUseCurrentGpsLocation: () -> Unit = {
        try {
            gpsPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } catch (_: Exception) {
            coroutineScope.launch {
                isDetectingGps = true
                val detected = RealWorldLocationAndMapHelper.detectRealWorldLocation(
                    context = context,
                    hasLocationPermission = false
                )
                viewModel.saveCustomPinLocation(
                    label = "My Current Location (${detected.source})",
                    areaName = detected.areaName,
                    latitude = detected.latitude,
                    longitude = detected.longitude,
                    radiusKm = searchRadiusKm.toIntOrNull() ?: 10,
                    setAsPrimary = true
                )
                isDetectingGps = false
            }
        }
    }

    // 1. First-Time Install Welcome Screen
    if (showWelcomeScreen) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = TaskaCream
        ) { welcomePadding ->
            TaskaWelcomeScreen(
                onOpenAndExploreApp = { viewModel.completeWelcomeAndExplore() },
                onGoToAuthChoice = { viewModel.completeWelcomeAndOpenAuth(AuthScreenState.CHOICE) },
                onGoToSignIn = { viewModel.completeWelcomeAndOpenAuth(AuthScreenState.SIGN_IN) },
                onGoToSignUp = { viewModel.completeWelcomeAndOpenAuth(AuthScreenState.SIGN_UP) },
                modifier = Modifier.padding(welcomePadding)
            )
        }
        return
    }

    // 2. Sign-In / Sign-Up Gateway & Forms
    if (authScreenState != AuthScreenState.NONE) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = TaskaCream
        ) { authPadding ->
            TaskaAuthFlowScreen(
                authStep = authScreenState,
                promptReason = authPromptReason,
                errorMessage = userAuthError ?: authErrorMessage,
                defaultArea = selectedArea,
                onSelectStep = { step ->
                    userAuthViewModel?.clearError()
                    viewModel.openAuthFlow(step, authPromptReason)
                },
                onSignIn = { email, password ->
                    if (userAuthViewModel != null) {
                        userAuthViewModel.login(email, password) { account ->
                            viewModel.onUserAuthenticatedFromAuthViewModel(account, isNewSignup = false)
                        }
                    } else {
                        viewModel.signIn(email, password)
                    }
                },
                onSignUp = { fullName, email, password, area, role, age, gender ->
                    if (userAuthViewModel != null) {
                        userAuthViewModel.signUp(
                            fullName = fullName,
                            email = email,
                            password = password,
                            defaultArea = area,
                            memberRole = role,
                            age = age,
                            gender = gender
                        ) { account ->
                            viewModel.onUserAuthenticatedFromAuthViewModel(account, isNewSignup = true)
                        }
                    } else {
                        viewModel.signUp(
                            fullName = fullName,
                            email = email,
                            password = password,
                            defaultArea = area,
                            memberRole = role,
                            age = age,
                            gender = gender
                        )
                    }
                },
                onContinueExploringAsGuest = {
                    userAuthViewModel?.continueAsGuest()
                    viewModel.closeAuthFlow()
                },
                onClearError = {
                    userAuthViewModel?.clearError()
                    viewModel.clearAuthError()
                },
                modifier = Modifier.padding(authPadding)
            )
        }
        return
    }

    if (currentTab != TaskaTab.ASK_TASKA && !showConversationHistoryScreen) {
        BackHandler {
            viewModel.selectTab(TaskaTab.ASK_TASKA)
        }
    }

    if (showHowItWorks) {
        HowItWorksBottomSheet(
            onDismiss = { viewModel.setShowHowItWorks(false) }
        )
    }

    if (showLocationPickerSheet) {
        LocationAndRadiusPickerSheet(
            currentArea = selectedArea,
            currentRadiusKm = searchRadiusKm,
            savedLocations = savedLocations,
            onUseCurrentGpsLocation = triggerUseCurrentGpsLocation,
            onOpenCustomPinMap = {
                viewModel.setShowLocationPickerSheet(false)
                viewModel.setShowLocationManagerScreen(true)
            },
            onSaveAreaAndRadius = { area, radius ->
                viewModel.saveCustomPinLocation(
                    label = area,
                    areaName = area,
                    radiusKm = radius.toIntOrNull() ?: 10,
                    setAsPrimary = true
                )
            },
            onDismiss = { viewModel.setShowLocationPickerSheet(false) }
        )
    }

    if (showDemandSheet) {
        RequestMissingServiceDemandSheet(
            defaultArea = selectedArea,
            onSubmitDemand = { reqText, area -> viewModel.submitServiceDemand(reqText, area) },
            onDismiss = { viewModel.setShowDemandSheet(false) }
        )
    }

    if (showCreateTaskSheet) {
        CreateTaskRequestSheet(
            targetTasker = createTaskTargetTasker,
            onCreateTask = { title, reqText, budget, negotiable, categoryId, urgencyOrTiming ->
                viewModel.submitCreateTaskSheet(
                    shortTitle = title,
                    requestText = reqText,
                    budgetZmw = budget,
                    negotiable = negotiable,
                    targetTasker = createTaskTargetTasker,
                    categoryId = categoryId,
                    urgencyOrTiming = urgencyOrTiming
                )
            },
            onDismiss = { viewModel.closeCreateTaskSheet() }
        )
    }

    selectedTaskerForProfile?.let { profileTasker ->
        TaskerProfileDetailSheet(
            tasker = profileTasker,
            taskerReviews = taskerReviews,
            onToggleFavourite = { viewModel.toggleFavouriteTasker(profileTasker) },
            onRequestThisTasker = {
                viewModel.openTaskerProfileDetail(null)
                viewModel.openCreateTaskSheet(profileTasker)
            },
            onBookPrivatelyNow = {
                viewModel.openTaskerProfileDetail(null)
                viewModel.bookTaskerPrivately(profileTasker, "")
            },
            onSubmitReview = { stars, comment ->
                viewModel.submitReviewForTasker(
                    tasker = profileTasker,
                    ratingStars = stars,
                    comment = comment
                )
            },
            onDismiss = { viewModel.openTaskerProfileDetail(null) }
        )
    }

    if (showAccountSheet) {
        UserAccountBottomSheet(
            currentUser = currentUser,
            onOpenSignIn = {
                viewModel.setShowAccountSheet(false)
                viewModel.openAuthFlow(AuthScreenState.SIGN_IN)
            },
            onOpenSignUp = {
                viewModel.setShowAccountSheet(false)
                viewModel.openAuthFlow(AuthScreenState.SIGN_UP)
            },
            onSignOut = {
                userAuthViewModel?.signOut()
                viewModel.signOut()
            },
            onReplayWelcomeScreen = { viewModel.replayWelcomeScreen() },
            onDismiss = { viewModel.setShowAccountSheet(false) }
        )
    }

    if (showLiveVoiceModal) {
        LiveVoiceConciergeModal(
            selectedArea = selectedArea,
            liveVoiceHistory = liveVoiceHistory,
            liveMatchedTaskers = liveMatchedTaskers,
            isRecordingAudio = isRecordingAudio,
            isAiLoading = isAiLoading,
            onStartRecording = { viewModel.startMicrophoneRecording() },
            onStopAndSendRecording = { viewModel.stopMicrophoneAndTranscribe(forLiveVoice = true) },
            onSendSpokenPrompt = { viewModel.sendLiveVoiceTurn(it) },
            onSpeakText = { viewModel.speakWithGeminiTts(it) },
            onBookTasker = { tasker ->
                viewModel.setShowLiveVoiceModal(false)
                viewModel.bookTaskerPrivately(tasker, "Booked via Taska Live Voice")
            },
            onDismiss = { viewModel.setShowLiveVoiceModal(false) }
        )
    }

    val userCreatedTasker = remember(taskers, currentUser) {
        taskers.firstOrNull { it.isUserCreated }
    }
    val myListingsCount = remember(userCreatedTasker) {
        userCreatedTasker?.servicesListedCsv
            ?.split("||")
            ?.count { it.isNotBlank() } ?: 0
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = TaskaCream,
        topBar = {
            Column {
                TaskaTopBar(
                    selectedArea = selectedArea,
                    savedLocations = savedLocations,
                    onAreaSelected = { viewModel.setSelectedArea(it) },
                    onOpenLocationSetup = {
                        viewModel.setShowLocationPickerSheet(true)
                    },
                    onOpenLiveVoice = { viewModel.setShowLiveVoiceModal(true) },
                    onOpenHowItWorks = { viewModel.setShowHowItWorks(true) },
                    currentUser = currentUser,
                    onOpenAccountOrAuth = {
                        if (currentUser != null) {
                            viewModel.setShowAccountSheet(true)
                        } else {
                            viewModel.openAuthFlow(AuthScreenState.CHOICE)
                        }
                    }
                )
                AnimatedVisibility(visible = statusBannerMessage != null) {
                    statusBannerMessage?.let { msg ->
                        Surface(
                            color = TaskaDeepInk,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable { viewModel.dismissBannerMessage() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TaskaLime,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss notification",
                                    tint = TaskaPureWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            TaskaModernBottomBar(
                currentTab = currentTab,
                bookingsCount = bookings.size,
                inboxCount = inboxNotifications.size,
                isSignedIn = currentUser != null,
                onSelectTab = { tab ->
                    viewModel.setShowConversationHistoryScreen(false)
                    viewModel.setShowLocationManagerScreen(false)
                    viewModel.selectTab(tab)
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showConversationHistoryScreen) {
                ConversationHistoryScreen(
                    sessions = conversationSessions,
                    activeSessionId = activeConversationSessionId,
                    onSelectSession = { sessionId ->
                        viewModel.openConversationSession(sessionId)
                    },
                    onStartNewConversation = {
                        viewModel.startNewConversationSession()
                    },
                    onDeleteSession = { sessionId ->
                        viewModel.deleteConversationSession(sessionId)
                    },
                    onClearAllCurrentChat = {
                        viewModel.clearChatConversation()
                    },
                    onBack = {
                        viewModel.setShowConversationHistoryScreen(false)
                    }
                )
            } else if (showLocationManagerScreen) {
                CustomLocationPinMapScreen(
                    selectedArea = selectedArea,
                    savedLocations = savedLocations,
                    isDetectingGps = isDetectingGps,
                    onBack = { viewModel.setShowLocationManagerScreen(false) },
                    onUseCurrentGpsLocation = triggerUseCurrentGpsLocation,
                    onSelectSavedLocation = { loc ->
                        viewModel.selectSavedLocation(loc)
                        viewModel.setShowLocationManagerScreen(false)
                    },
                    onSaveCustomPinLocation = { label, areaName, lat, lng ->
                        viewModel.saveCustomPinLocation(
                            label = label,
                            areaName = areaName,
                            latitude = lat,
                            longitude = lng,
                            radiusKm = searchRadiusKm.toIntOrNull() ?: 10,
                            setAsPrimary = true
                        )
                    },
                    onDeleteSavedLocation = { id -> viewModel.deleteSavedLocation(id) }
                )
            } else {
                when (currentTab) {
                    TaskaTab.ASK_TASKA -> AskTaskaChatScreen(
                        selectedArea = selectedArea,
                        chatMessages = chatMessages,
                        taskers = taskers,
                        selectedAiModel = selectedAiModel,
                        useMapsGrounding = useMapsGrounding,
                        useSearchGrounding = useSearchGrounding,
                        isAiLoading = isAiLoading,
                        isRecordingAudio = isRecordingAudio,
                        onSendMessage = { viewModel.sendUserMessage(it) },
                        onSelectModel = { viewModel.setSelectedAiModel(it) },
                        onToggleMapsGrounding = { viewModel.toggleMapsGrounding() },
                        onToggleSearchGrounding = { viewModel.toggleSearchGrounding() },
                        onAnalyzePhoto = { bmp, cap -> viewModel.analyzePhotoAndSendToChat(bmp, cap) },
                        onStartAudioRecording = { viewModel.startMicrophoneRecording() },
                        onStopAndTranscribeAudio = { viewModel.stopMicrophoneAndTranscribe(forLiveVoice = false) },
                        onSpeakText = { viewModel.speakWithGeminiTts(it) },
                        onBookTasker = { tasker, summary -> viewModel.bookTaskerPrivately(tasker, summary) },
                        onOpenHowItWorks = { viewModel.setShowHowItWorks(true) },
                        onOpenLiveVoice = { viewModel.setShowLiveVoiceModal(true) },
                        onResetChat = { viewModel.clearChatConversation() },
                        onOpenCreateTaskSheet = { viewModel.openCreateTaskSheet(null) },
                        onOpenDemandSheet = { viewModel.setShowDemandSheet(true) },
                        conversationCount = conversationSessions.size.coerceAtLeast(1),
                        onOpenConversationHistoryScreen = {
                            viewModel.setShowConversationHistoryScreen(true)
                        },
                        onOpenLocationSetup = {
                            viewModel.setShowLocationPickerSheet(true)
                        },
                        onOpenTaskerProfile = { tasker ->
                            viewModel.openTaskerProfileDetail(tasker)
                        }
                    )

                    TaskaTab.BOOKINGS -> BookingsAndTrustScreen(
                        bookings = bookings,
                        tasks = tasks,
                        selectedArea = selectedArea,
                        savedLocations = savedLocations,
                        liveSupabaseChatMessages = liveSupabaseChatMessages,
                        isSupabaseConnected = viewModel.isSupabaseConfigured,
                        activeBookingChatId = activeBookingChatId,
                        currentUser = currentUser,
                        onSelectBookingChat = { viewModel.openBookingChat(it) },
                        onSendPrivateMessage = { booking, text -> viewModel.sendPrivateBookingMessage(booking, text) },
                        onRefreshLiveChat = { viewModel.refreshActiveBookingLiveChat() },
                        onCompleteAndRateBooking = { booking, stars -> viewModel.completeAndRateBooking(booking, stars) },
                        onIssueCompletionPin = { booking -> viewModel.issueCompletionPin(booking) },
                        onDeleteTaskRequirement = { taskId -> viewModel.deleteTaskRequirement(taskId) },
                        onOpenCreateTaskSheet = { viewModel.openCreateTaskSheet(null) },
                        onOpenLocationMap = { viewModel.setShowLocationManagerScreen(true) },
                        onSpeakMessage = { viewModel.speakWithGeminiTts(it) },
                        onGoToExplore = { viewModel.selectTab(TaskaTab.EXPLORE) },
                        onRequestSignInOrSignUp = {
                            viewModel.openAuthFlow(
                                step = AuthScreenState.CHOICE,
                                reason = "Sign in or create a free account to open Private Task Chats, Completion PINs, and bookings."
                            )
                        }
                    )

                    TaskaTab.EXPLORE -> ExploreMarketplaceScreen(
                        selectedArea = selectedArea,
                        selectedCategoryFilter = selectedCategoryFilter,
                        searchQuery = exploreSearchQuery,
                        selectedMinRating = exploreMinRating,
                        onlyVerifiedFilter = exploreVerifiedOnly,
                        taskers = taskers,
                        fastEstimateText = fastEstimateText,
                        onSelectCategory = { viewModel.setCategoryFilter(it) },
                        onSearchQueryChange = { viewModel.setExploreSearchQuery(it) },
                        onSelectMinRating = { viewModel.setExploreMinRating(it) },
                        onToggleVerifiedFilter = { viewModel.toggleExploreVerifiedOnly() },
                        onRequestFastEstimate = { viewModel.requestFastTaskEstimate(it) },
                        onBookTasker = { tasker -> viewModel.bookTaskerPrivately(tasker, "") },
                        onOpenTaskerProfile = { tasker -> viewModel.openTaskerProfileDetail(tasker) },
                        onOpenRequestDemand = { viewModel.setShowDemandSheet(true) },
                        onOpenLocationSetup = { viewModel.setShowLocationPickerSheet(true) },
                        onSpeakBio = { viewModel.speakWithGeminiTts(it) },
                        onAskInChat = { query ->
                            viewModel.selectTab(TaskaTab.ASK_TASKA)
                            viewModel.sendUserMessage(query)
                        }
                    )

                    TaskaTab.INBOX -> TaskaInboxScreen(
                        notifications = inboxNotifications,
                        currentUser = currentUser,
                        onSignInClick = {
                            viewModel.openAuthFlow(
                                step = AuthScreenState.CHOICE,
                                reason = "Sign in to receive private Taska booking updates and Tasker offer notifications."
                            )
                        },
                        onOpenRequestDemand = { viewModel.setShowDemandSheet(true) }
                    )

                    TaskaTab.TASKER_MODE -> TaskerModeScreen(
                        selectedArea = selectedArea,
                        marketplaceRequests = marketplaceRequests,
                        isAiLoading = isAiLoading,
                        currentUser = currentUser,
                        isDarkMode = isDarkMode,
                        savedLocations = savedLocations,
                        myListingsCount = myListingsCount,
                        showEarningProfileScreen = showEarningProfileScreen,
                        showSettingsScreen = showSettingsScreen,
                        showCustomPinMapScreen = showLocationManagerScreen,
                        isDetectingGps = isDetectingGps,
                        onSetShowEarningProfileScreen = { viewModel.setShowEarningProfileScreen(it) },
                        onSetShowSettingsScreen = { viewModel.setShowSettingsScreen(it) },
                        onSetShowCustomPinMapScreen = { viewModel.setShowLocationManagerScreen(it) },
                        onOpenConversationHistoryScreen = {
                            viewModel.setShowConversationHistoryScreen(true)
                        },
                        onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                        onOpenSignInOrSignUp = {
                            viewModel.openAuthFlow(
                                step = AuthScreenState.CHOICE,
                                reason = "Sign in or create a free account to earn with Taska, manage credits, or save listings."
                            )
                        },
                        onSignOut = {
                            userAuthViewModel?.signOut()
                            viewModel.signOut()
                        },
                        onUpdateFullProfile = { fullName, age, gender, bio, area ->
                            userAuthViewModel?.updateFullProfile(
                                fullName = fullName,
                                age = age,
                                gender = gender,
                                bio = bio,
                                defaultArea = area
                            )
                            viewModel.updateUserFullProfile(
                                fullName = fullName,
                                age = age,
                                gender = gender,
                                bio = bio,
                                defaultArea = area
                            )
                        },
                        onUseCurrentGpsLocation = triggerUseCurrentGpsLocation,
                        onSelectSavedLocation = { loc -> viewModel.selectSavedLocation(loc) },
                        onSaveCustomPinLocation = { label, areaName, lat, lng ->
                            viewModel.saveCustomPinLocation(
                                label = label,
                                areaName = areaName,
                                latitude = lat,
                                longitude = lng,
                                radiusKm = searchRadiusKm.toIntOrNull() ?: 10,
                                setAsPrimary = true
                            )
                        },
                        onDeleteSavedLocation = { id -> viewModel.deleteSavedLocation(id) },
                        onReplayWelcome = { viewModel.replayWelcomeScreen() },
                        onClearChatHistory = { viewModel.clearChatConversation() },
                        onSaveFullEarningProfile = { role, cat, pubName, phone, bio, srvTitle, srvPrice, area, bizName, bizReg, legalName, nrc, hasDoc, hasSelfie, storePhotos, listingPhotos ->
                            viewModel.saveFullEarningProfile(
                                kindRole = role,
                                category = cat,
                                publicName = pubName,
                                phoneE164 = phone,
                                bio = bio,
                                firstListingTitle = srvTitle,
                                firstListingPrice = srvPrice,
                                area = area,
                                businessName = bizName,
                                businessRegNumber = bizReg,
                                legalFullName = legalName,
                                nationalIdNumber = nrc,
                                hasIdPhoto = hasDoc,
                                hasSelfiePhoto = hasSelfie,
                                storePhotoUris = storePhotos,
                                firstListingPhotoUris = listingPhotos
                            )
                            viewModel.setShowEarningProfileScreen(false)
                        },
                        onAddServiceListing = { title, cat, desc, price, photos ->
                            viewModel.addServiceListing(title, cat, desc, price, photos)
                        },
                        onSubmitCreditTopUp = { credits, sender, phone, ref ->
                            viewModel.submitCreditTopUp(credits, sender, phone, ref)
                        },
                        onAcceptRequestWithPrice = { req, price, roleTitle ->
                            viewModel.acceptMarketplaceRequestWithPrice(req, price, roleTitle)
                        },
                        onDeclineRequest = { req ->
                            viewModel.declineMarketplaceRequest(req)
                        }
                    )
                }
            }
        }
    }
}
