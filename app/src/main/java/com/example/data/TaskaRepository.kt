package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class TaskaRepository(
    private val dao: TaskaDao,
    val supabaseService: TaskaSupabaseService = TaskaSupabaseService()
) {
    private val seedMutex = Mutex()

    val taskRepository = TaskRepository(dao, supabaseService)
    val allTasks: Flow<List<Task>> = taskRepository.allTasks
    val allTaskers: Flow<List<TaskerEntity>> = dao.getAllTaskers()
    val allTaskerReviews: Flow<List<TaskerReviewEntity>> = dao.getAllTaskerReviews()
    val allSavedLocations: Flow<List<SavedLocationEntity>> = dao.getAllSavedLocations()
    val allConversationSessions: Flow<List<ConversationSessionEntity>> = dao.getAllConversationSessions()
    val allBookings: Flow<List<BookingEntity>> = dao.getAllBookings()
    val allChatMessages: Flow<List<ChatMessageEntity>> = dao.getAllChatMessages()
    val allMarketplaceRequests: Flow<List<MarketplaceRequestEntity>> = dao.getAllMarketplaceRequests()
    val allInboxNotifications: Flow<List<InboxNotificationEntity>> = dao.getAllInboxNotifications()
    val allServiceDemands: Flow<List<ServiceDemandEntity>> = dao.getAllServiceDemands()

    suspend fun getAllTaskersOnce(): List<TaskerEntity> = dao.getAllTaskersOnce()

    suspend fun ensureSeedData(sessionId: String = "default_thread") {
        seedMutex.withLock {
            // Remove any legacy mock accounts if upgrading
            dao.deleteLegacyMockAccounts()

            // Deduplicate any duplicate greeting messages
            dao.deduplicateAssistantGreetings()

            // Ensure the current Ask Taska session starts with a single clean concierge greeting
            if (dao.getChatMessageCountForThread(sessionId) == 0) {
                val greeting = "Hi — I’m Taska. Tell me what you need, when you need it, and where. I'll match you with verified local Taskers while keeping your phone number private."
                dao.insertChatMessage(
                    ChatMessageEntity(
                        threadId = sessionId,
                        isFromUser = false,
                        text = greeting,
                        matchedTaskerIdsCsv = "",
                        modelTag = "Taska Concierge",
                        groundingInfo = "Private by default · Verified Taskers"
                    )
                )
                if (dao.getConversationSessionById(sessionId) == null) {
                    dao.insertConversationSession(
                        ConversationSessionEntity(
                            sessionId = sessionId,
                            title = "Welcome to Ask Taska",
                            previewText = greeting,
                            area = "",
                            messageCount = 1
                        )
                    )
                }
            }
        }
    }

    suspend fun syncAnnouncementsFromSupabase(): Int {
        val remote = supabaseService.fetchPlatformAnnouncements()
        dao.clearPublicAnnouncements()
        if (remote.isNotEmpty()) {
            val entities = remote.mapIndexed { index, (title, body, createdAt) ->
                InboxNotificationEntity(
                    id = 100 + index,
                    title = title,
                    body = body,
                    category = "announcement",
                    isPrivateForSignedInUser = false,
                    timeLabel = createdAt.take(10)
                )
            }
            dao.insertInboxNotifications(entities)
        }
        return remote.size
    }

    suspend fun syncDatabaseWithSupabase(currentUser: UserAccountEntity? = null): SupabaseSyncReport {
        if (!supabaseService.isConfigured) {
            return SupabaseSyncReport(
                isConnected = false,
                syncedTaskersCount = dao.getTaskerCount(),
                syncedAnnouncementsCount = dao.getInboxNotificationCount(),
                syncedCredits = currentUser?.taskaCredits,
                statusSummary = "Your profile and tasks are saved on this device."
            )
        }

        val remoteProviders = supabaseService.fetchProvidersFromSupabase()
        dao.clearRemoteTaskers()
        if (remoteProviders.isNotEmpty()) {
            dao.insertTaskers(remoteProviders)
        }

        val remoteRequests = supabaseService.fetchOpenServiceRequestsFromSupabase()
        if (remoteRequests.isNotEmpty()) {
            dao.insertMarketplaceRequests(remoteRequests)
        }

        val announcementsCount = syncAnnouncementsFromSupabase()

        var remoteCredits: Int? = null
        if (currentUser != null && currentUser.supabaseUserId.isNotBlank()) {
            supabaseService.upsertProfileRow(
                userId = currentUser.supabaseUserId,
                displayName = currentUser.fullName,
                age = currentUser.age,
                gender = currentUser.gender,
                bio = currentUser.bio,
                defaultArea = currentUser.defaultArea,
                hasEarningProfile = currentUser.hasEarningProfile,
                storePhotosCsv = currentUser.storePhotosCsv
            )
            remoteCredits = supabaseService.fetchUserCreditBalance(currentUser.supabaseUserId)
            if (remoteCredits != null) {
                dao.updateUserAccount(currentUser.copy(taskaCredits = remoteCredits))
            }
        }

        return SupabaseSyncReport(
            isConnected = true,
            syncedTaskersCount = remoteProviders.size,
            syncedAnnouncementsCount = announcementsCount,
            syncedCredits = remoteCredits ?: currentUser?.taskaCredits,
            statusSummary = "Your account and marketplace listings are up to date."
        )
    }

    suspend fun getUserById(id: Int): UserAccountEntity? = dao.getUserById(id)

    suspend fun signInUser(email: String, password: String): Result<UserAccountEntity> {
        val cleanEmail = email.trim()
        // 1. Try Supabase Auth first if configured
        if (supabaseService.isConfigured) {
            val remoteResult = supabaseService.signInWithPassword(cleanEmail, password)
            if (remoteResult.isSuccess) {
                val session = remoteResult.getOrNull()!!
                val existingLocal = dao.getUserByEmail(cleanEmail)
                val cleanArea = if (session.defaultArea.equals("UNZA Campus Hub", ignoreCase = true)) "" else session.defaultArea
                val syncedUser = if (existingLocal != null) {
                    val localArea = if (existingLocal.defaultArea.equals("UNZA Campus Hub", ignoreCase = true)) "" else existingLocal.defaultArea
                    val updated = existingLocal.copy(
                        fullName = session.displayName.ifBlank { existingLocal.fullName },
                        age = session.age.ifBlank { existingLocal.age },
                        gender = session.gender.ifBlank { existingLocal.gender },
                        bio = session.bio.ifBlank { existingLocal.bio },
                        defaultArea = cleanArea.ifBlank { localArea },
                        hasEarningProfile = session.hasEarningProfile || existingLocal.hasEarningProfile,
                        storePhotosCsv = session.storePhotosCsv.ifBlank { existingLocal.storePhotosCsv },
                        supabaseUserId = session.userId
                    )
                    dao.updateUserAccount(updated)
                    updated
                } else {
                    val newLocal = UserAccountEntity(
                        fullName = session.displayName,
                        email = session.email,
                        password = password,
                        defaultArea = cleanArea,
                        age = session.age,
                        gender = session.gender,
                        bio = session.bio,
                        hasEarningProfile = session.hasEarningProfile,
                        storePhotosCsv = session.storePhotosCsv,
                        supabaseUserId = session.userId
                    )
                    val newId = dao.insertUserAccount(newLocal).toInt()
                    newLocal.copy(id = newId)
                }
                return Result.success(syncedUser)
            }
        }

        // 2. Local Room account lookup
        val account = dao.getUserByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("No Taska account found for $cleanEmail. Try signing up first!"))
        if (account.password != password) {
            return Result.failure(IllegalArgumentException("That email or password is not correct. Try again or create an account."))
        }
        val cleanedAccount = if (account.defaultArea.equals("UNZA Campus Hub", ignoreCase = true)) {
            val fixed = account.copy(defaultArea = "")
            dao.updateUserAccount(fixed)
            fixed
        } else {
            account
        }
        return Result.success(cleanedAccount)
    }

    suspend fun registerUser(
        fullName: String,
        email: String,
        password: String,
        defaultArea: String,
        memberRole: String,
        age: String = "",
        gender: String = ""
    ): Result<UserAccountEntity> {
        val cleanEmail = email.trim()
        val existing = dao.getUserByEmail(cleanEmail)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("An account with $cleanEmail already exists. Please sign in instead."))
        }

        val cleanArea = if (defaultArea.equals("UNZA Campus Hub", ignoreCase = true)) "" else defaultArea.trim()
        var supabaseUid = ""
        if (supabaseService.isConfigured) {
            val remoteSignup = supabaseService.signUp(
                email = cleanEmail,
                password = password,
                displayName = fullName.trim(),
                age = age.trim(),
                gender = gender.trim(),
                defaultArea = cleanArea
            )
            remoteSignup.onSuccess { session ->
                supabaseUid = session?.userId.orEmpty()
            }.onFailure { err ->
                return Result.failure(err)
            }
        }

        val initialCredits = if (supabaseUid.isNotBlank()) {
            supabaseService.fetchUserCreditBalance(supabaseUid) ?: 0
        } else {
            0
        }

        val newUser = UserAccountEntity(
            fullName = fullName.trim(),
            email = cleanEmail,
            password = password,
            defaultArea = cleanArea,
            age = age.trim(),
            gender = gender.trim(),
            memberRole = memberRole,
            hasEarningProfile = false,
            taskaCredits = initialCredits,
            verificationStatus = "unsubmitted",
            supabaseUserId = supabaseUid
        )
        val newId = dao.insertUserAccount(newUser).toInt()
        if (cleanArea.isNotBlank()) {
            saveCustomLocation(
                label = "Primary Area",
                areaName = cleanArea,
                latitude = -15.3875,
                longitude = 28.3228,
                radiusKm = 10,
                userEmail = cleanEmail,
                setAsPrimary = true
            )
        }
        return Result.success(newUser.copy(id = newId))
    }

    suspend fun updateUserDisplayName(user: UserAccountEntity, newDisplayName: String): UserAccountEntity {
        return updateUserFullProfile(
            user = user,
            fullName = newDisplayName,
            age = user.age,
            gender = user.gender,
            bio = user.bio,
            defaultArea = user.defaultArea
        )
    }

    suspend fun updateUserFullProfile(
        user: UserAccountEntity,
        fullName: String,
        age: String,
        gender: String,
        bio: String,
        defaultArea: String
    ): UserAccountEntity {
        val cleanName = fullName.trim().ifBlank { user.fullName }
        val cleanArea = defaultArea.trim()
        val updated = user.copy(
            fullName = cleanName,
            age = age.trim(),
            gender = gender.trim(),
            bio = bio.trim(),
            defaultArea = cleanArea
        )
        dao.updateUserAccount(updated)
        if (user.supabaseUserId.isNotBlank()) {
            supabaseService.upsertProfileRow(
                userId = user.supabaseUserId,
                displayName = cleanName,
                age = age.trim(),
                gender = gender.trim(),
                bio = bio.trim(),
                defaultArea = cleanArea,
                hasEarningProfile = updated.hasEarningProfile,
                storePhotosCsv = updated.storePhotosCsv
            )
        }
        return updated
    }

    // --- Saved Custom Locations & Pin Drops ---

    suspend fun saveCustomLocation(
        label: String,
        areaName: String,
        latitude: Double = -15.3875,
        longitude: Double = 28.3228,
        radiusKm: Int = 10,
        userEmail: String = "",
        setAsPrimary: Boolean = true
    ): SavedLocationEntity {
        if (setAsPrimary) {
            dao.clearPrimarySavedLocations()
        }
        val entity = SavedLocationEntity(
            userEmail = userEmail,
            label = label.trim().ifBlank { areaName.trim() },
            areaName = areaName.trim(),
            latitude = latitude,
            longitude = longitude,
            radiusKm = radiusKm.coerceIn(1, 100),
            isPrimary = setAsPrimary
        )
        val id = dao.insertSavedLocation(entity).toInt()
        return entity.copy(id = id)
    }

    suspend fun deleteSavedLocation(id: Int) {
        dao.deleteSavedLocationById(id)
    }

    // --- Conversation Sessions (Ask Taska History) ---

    suspend fun saveOrUpdateConversationSession(
        sessionId: String,
        title: String,
        previewText: String,
        area: String
    ) {
        val count = dao.getChatMessageCountForThread(sessionId)
        val existing = dao.getConversationSessionById(sessionId)
        val computedTitle = if (existing != null && existing.title != "Welcome to Ask Taska" && existing.title != "New Conversation") {
            existing.title
        } else {
            title.take(44).ifBlank { "Ask Taska Conversation" }
        }
        dao.insertConversationSession(
            ConversationSessionEntity(
                sessionId = sessionId,
                title = computedTitle,
                previewText = previewText.take(90),
                area = area,
                messageCount = count.coerceAtLeast(1),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteConversationSession(sessionId: String) {
        dao.clearChatMessagesForThread(sessionId)
        dao.deleteConversationSessionById(sessionId)
    }

    suspend fun toggleTaskerFavourite(tasker: TaskerEntity): TaskerEntity {
        val updated = tasker.copy(isFavourite = !tasker.isFavourite)
        dao.updateTasker(updated)
        return updated
    }

    suspend fun addChatMessage(message: ChatMessageEntity) {
        dao.insertChatMessage(message)
    }

    suspend fun clearChat() {
        dao.clearChatMessages()
    }

    suspend fun submitServiceDemand(
        requestText: String,
        broadArea: String,
        requesterId: String?
    ): ServiceDemandEntity {
        val cleanArea = broadArea.ifBlank { "Local Area" }
        val syncedId = supabaseService.submitServiceDemandToSupabase(
            requestText = requestText,
            broadArea = cleanArea,
            requesterId = requesterId
        )
        val synced = !syncedId.isNullOrBlank()
        val entity = ServiceDemandEntity(
            requestText = requestText.trim(),
            broadArea = cleanArea,
            syncedWithSupabase = synced
        )
        val id = dao.insertServiceDemand(entity).toInt()
        dao.insertInboxNotification(
            InboxNotificationEntity(
                title = "Service demand logged: ${requestText.take(36)}",
                body = "Your request in $cleanArea has been logged so local Taskers in this category can be matched.",
                category = "announcement",
                isPrivateForSignedInUser = false,
                timeLabel = "Just now"
            )
        )
        return entity.copy(id = id)
    }

    suspend fun createMarketplaceTaskaRequest(
        shortTitle: String,
        requestText: String,
        area: String,
        clientBudget: String,
        budgetNegotiable: Boolean,
        requesterId: String? = null,
        categoryId: String = TaskaCategory.DEVICE_REPAIRS.id,
        urgencyOrTiming: String = "Flexible"
    ): MarketplaceRequestEntity {
        val computedTitle = shortTitle.trim().ifBlank {
            requestText.trim().take(48).ifBlank { "Taska Service Request" }
        }
        val cleanDetails = requestText.trim().ifBlank { computedTitle }
        val cleanArea = area.ifBlank { "Local Area" }

        val createdTask = taskRepository.createMarketplaceTaskRequirement(
            title = computedTitle,
            description = cleanDetails,
            categoryId = categoryId,
            area = cleanArea,
            budgetZmw = clientBudget,
            budgetNegotiable = budgetNegotiable,
            urgencyOrTiming = urgencyOrTiming,
            assignedTasker = null,
            requesterUserId = requesterId
        )

        val requestEntity = MarketplaceRequestEntity(
            title = computedTitle,
            categoryId = categoryId,
            area = cleanArea,
            timing = urgencyOrTiming,
            budget = createdTask.budgetZmw,
            budgetNegotiable = budgetNegotiable,
            details = cleanDetails,
            clientAlias = "Verified Taska Client",
            status = "Open"
        )
        val id = dao.insertMarketplaceRequest(requestEntity).toInt()

        val openBooking = BookingEntity(
            taskerId = 0,
            taskerName = "Verified Local Taskers",
            taskerInitials = "TK",
            taskerSpecialty = TaskaCategory.entries.firstOrNull { it.id == categoryId }?.title ?: "Marketplace",
            categoryId = categoryId,
            taskTitle = computedTitle,
            taskRequestText = cleanDetails,
            area = cleanArea,
            status = "Open · Offer Ready",
            priceEstimate = createdTask.budgetZmw,
            clientBudget = clientBudget,
            budgetNegotiable = budgetNegotiable,
            completionPin = createdTask.completionPin,
            supabaseRequestId = createdTask.supabaseRequestId,
            supabaseThreadId = createdTask.supabaseThreadId,
            privateChatHistory = createdTask.chatHistorySerialized
        )
        dao.insertBooking(openBooking)

        dao.insertInboxNotification(
            InboxNotificationEntity(
                title = "Taska posted: $computedTitle",
                body = "Your requirement in $cleanArea (${createdTask.budgetZmw}) is now live for verified Taskers.",
                category = "booking_update",
                isPrivateForSignedInUser = true,
                timeLabel = "Just now"
            )
        )
        return requestEntity.copy(id = id)
    }

    suspend fun createPrivateBooking(
        tasker: TaskerEntity,
        taskDescription: String,
        area: String,
        shortTitle: String = "",
        clientBudget: String = "",
        budgetNegotiable: Boolean = true,
        requesterId: String? = null,
        categoryId: String = tasker.categoryId,
        urgencyOrTiming: String = "Flexible"
    ): Long {
        val cleanArea = area.ifBlank { tasker.area.ifBlank { "Local Area" } }
        val computedTitle = shortTitle.ifBlank {
            taskDescription.take(48).ifBlank { "${tasker.name} Service Request" }
        }
        val createdTask = taskRepository.createMarketplaceTaskRequirement(
            title = computedTitle,
            description = taskDescription,
            categoryId = categoryId,
            area = cleanArea,
            budgetZmw = clientBudget,
            budgetNegotiable = budgetNegotiable,
            urgencyOrTiming = urgencyOrTiming,
            assignedTasker = tasker,
            requesterUserId = requesterId
        )

        val budgetNote = if (clientBudget.isNotBlank()) {
            " · Budget: ZMW $clientBudget (${if (budgetNegotiable) "Negotiable" else "Fixed"})"
        } else {
            ""
        }
        val scheduleNote = if (urgencyOrTiming.isNotBlank()) " · Timing: $urgencyOrTiming" else ""
        val initialHistory = listOf(
            "You::Hi ${tasker.name}, I'd like to book: $taskDescription$budgetNote$scheduleNote::Just now",
            "Taska Shield::Private task chat is ON. Personal phone numbers are never shared.::Just now"
        ).joinToString("||")

        val booking = BookingEntity(
            taskerId = tasker.id,
            taskerName = tasker.name,
            taskerInitials = tasker.initials,
            taskerSpecialty = tasker.specialty,
            categoryId = categoryId,
            taskTitle = computedTitle,
            taskRequestText = taskDescription,
            area = cleanArea,
            status = "In Progress",
            priceEstimate = if (clientBudget.isNotBlank()) "ZMW $clientBudget" else tasker.priceRange,
            clientBudget = clientBudget,
            budgetNegotiable = budgetNegotiable,
            completionPin = createdTask.completionPin,
            supabaseRequestId = createdTask.supabaseRequestId,
            supabaseThreadId = createdTask.supabaseThreadId,
            privateChatHistory = initialHistory
        )
        val id = dao.insertBooking(booking)
        dao.insertInboxNotification(
            InboxNotificationEntity(
                title = "Booking confirmed with ${tasker.name}",
                body = "$computedTitle in $cleanArea. Coordinate safely inside your Private Task Chat.",
                category = "booking_update",
                isPrivateForSignedInUser = true,
                timeLabel = "Just now"
            )
        )
        return id
    }

    suspend fun issueOrRefreshCompletionPin(booking: BookingEntity): String {
        val rpcPin = if (booking.supabaseRequestId.contains("-")) {
            supabaseService.invokeIssueCompletionPinRpc(booking.supabaseRequestId)
        } else {
            null
        }
        val pin = rpcPin ?: booking.completionPin.ifBlank {
            (100000..999999).random(Random(System.currentTimeMillis())).toString()
        }
        val updated = booking.copy(
            completionPin = pin,
            status = if (booking.status == "Completed") "Completed" else "In Progress"
        )
        dao.updateBooking(updated)
        return pin
    }

    suspend fun appendPrivateBookingMessage(
        booking: BookingEntity,
        sender: String,
        message: String,
        timeLabel: String,
        senderUserId: String? = null
    ) {
        var threadId = booking.supabaseThreadId
        if (threadId.isBlank() && booking.supabaseRequestId.contains("-")) {
            threadId = supabaseService.getOrCreateChatThread(
                requestId = booking.supabaseRequestId,
                requesterId = senderUserId
            ).orEmpty()
        }
        if (threadId.isNotBlank() && sender == "You") {
            supabaseService.sendChatMessageToSupabase(
                threadId = threadId,
                senderId = senderUserId,
                body = message
            )
        }

        val updatedHistory = if (booking.privateChatHistory.isBlank()) {
            "$sender::$message::$timeLabel"
        } else {
            "${booking.privateChatHistory}||$sender::$message::$timeLabel"
        }
        dao.updateBooking(
            booking.copy(
                supabaseThreadId = threadId,
                privateChatHistory = updatedHistory
            )
        )
    }

    fun streamRealTimeMessagesForBooking(
        booking: BookingEntity,
        currentUserId: String?
    ): Flow<List<SupabaseChatMessage>> {
        return taskRepository.streamRealTimeChatForThread(
            threadId = booking.supabaseThreadId,
            currentUserId = currentUserId,
            counterpartyName = booking.taskerName
        )
    }

    suspend fun deleteTaskRequirement(taskId: Int) {
        taskRepository.deleteTaskById(taskId)
    }

    suspend fun submitTaskerReview(
        taskerId: Int,
        taskerName: String,
        reviewerName: String,
        ratingStars: Int,
        comment: String,
        serviceTitle: String = "Verified Taska Service",
        requestId: String? = null
    ) {
        val clampedStars = ratingStars.coerceIn(1, 5)
        val cleanComment = comment.trim().ifBlank { "Rated $clampedStars stars for $serviceTitle." }
        val cleanReviewer = reviewerName.trim().ifBlank { "Verified Taska Client" }

        supabaseService.submitTaskReviewToSupabase(
            requestId = requestId,
            providerName = taskerName,
            stars = clampedStars,
            comment = cleanComment
        )

        dao.insertTaskerReview(
            TaskerReviewEntity(
                taskerId = taskerId,
                taskerName = taskerName,
                reviewerName = cleanReviewer,
                ratingStars = clampedStars,
                comment = cleanComment,
                serviceTitle = serviceTitle,
                isVerifiedBooking = true,
                dateLabel = "Just now"
            )
        )

        val targetTasker = if (taskerId > 0) {
            dao.getTaskerById(taskerId)
        } else {
            dao.getAllTaskersOnce().firstOrNull { it.name.equals(taskerName, ignoreCase = true) }
        }

        if (targetTasker != null) {
            val newCount = targetTasker.ratingCount + 1
            val totalQualityPoints = (targetTasker.rating * targetTasker.ratingCount) + clampedStars
            val newAvg = ((totalQualityPoints / newCount) * 10.0).toInt() / 10.0
            val reviewToken = "$cleanReviewer::★ $clampedStars.0::$cleanComment"
            val updatedReviewsCsv = if (targetTasker.reviewsCsv.isBlank()) {
                reviewToken
            } else {
                "$reviewToken||${targetTasker.reviewsCsv}"
            }
            val updatedTasker = targetTasker.copy(
                rating = newAvg.coerceIn(1.0, 5.0),
                ratingCount = newCount,
                completedTasks = targetTasker.completedTasks + 1,
                fiveStarCount = targetTasker.fiveStarCount + if (clampedStars == 5) 1 else 0,
                fourStarCount = targetTasker.fourStarCount + if (clampedStars == 4) 1 else 0,
                threeStarCount = targetTasker.threeStarCount + if (clampedStars == 3) 1 else 0,
                twoStarCount = targetTasker.twoStarCount + if (clampedStars == 2) 1 else 0,
                oneStarCount = targetTasker.oneStarCount + if (clampedStars == 1) 1 else 0,
                reviewsCsv = updatedReviewsCsv
            )
            dao.updateTasker(updatedTasker)
        }
    }

    suspend fun markBookingCompleted(
        booking: BookingEntity,
        rating: Int,
        writtenReview: String = "",
        reviewerName: String = "Verified Taska Client"
    ) {
        submitTaskerReview(
            taskerId = booking.taskerId,
            taskerName = booking.taskerName,
            reviewerName = reviewerName,
            ratingStars = rating,
            comment = writtenReview,
            serviceTitle = booking.taskTitle,
            requestId = booking.supabaseRequestId.takeIf { it.contains("-") }
        )
        val updatedHistory = "${booking.privateChatHistory}||Taska Shield::Task marked DONE with Completion PIN (${booking.completionPin.ifBlank { "Verified" }}) & rated ★ $rating.0!::Completed"
        dao.updateBooking(
            booking.copy(
                status = "Completed",
                userRating = rating,
                writtenReview = writtenReview,
                privateChatHistory = updatedHistory
            )
        )
    }

    suspend fun saveFullEarningProfile(
        currentUser: UserAccountEntity?,
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
    ): Pair<TaskerEntity, UserAccountEntity?> {
        val words = publicName.trim().split(" ").filter { it.isNotBlank() }
        val initials = if (words.size >= 2) {
            "${words[0].first().uppercaseChar()}${words[1].first().uppercaseChar()}"
        } else {
            publicName.take(2).uppercase()
        }
        val colorType = when (category) {
            TaskaCategory.DEVICE_REPAIRS, TaskaCategory.PHONES_GADGETS -> "lavender"
            TaskaCategory.PRINTING_SUPPLIES, TaskaCategory.PHARMACIES -> "mint"
            TaskaCategory.TUTORING, TaskaCategory.HOMES_ROOMS, TaskaCategory.FOOD_NEARBY -> "peach"
            TaskaCategory.ERRANDS_MOVING, TaskaCategory.MOVING -> "butter"
            TaskaCategory.DIGITAL_SERVICES, TaskaCategory.CARS_AUTO, TaskaCategory.HOME_SERVICES -> "sky"
            TaskaCategory.LOCAL_STORES -> "gray"
        }

        val hasSubmittedDocs = hasIdPhoto && hasSelfiePhoto && legalFullName.isNotBlank() && nationalIdNumber.isNotBlank()
        val isAlreadyVerified = currentUser?.verificationStatus == "verified"
        val specialtyText = if (firstListingTitle.isNotBlank()) {
            "${if (isAlreadyVerified) "Verified · " else ""}${firstListingTitle.trim()}"
        } else {
            "${if (isAlreadyVerified) "Verified · " else ""}${category.title} (${kindRole.title})"
        }
        val priceFormatted = if (firstListingPrice.isNotBlank()) {
            "ZMW ${firstListingPrice.trim()}"
        } else {
            "Open to offers"
        }
        val listingPhotosPart = firstListingPhotoUris.take(6).joinToString(";")
        val serviceEntry = if (firstListingTitle.isNotBlank()) {
            "${firstListingTitle.trim()}::${category.title}::$priceFormatted::$listingPhotosPart"
        } else {
            "${category.title} Service::${category.title}::$priceFormatted::$listingPhotosPart"
        }
        val storePhotosSerialized = storePhotoUris.take(6).joinToString("||")

        val existingUserTasker = dao.getAllTaskersOnce().firstOrNull { it.isUserCreated }
        val tasker = TaskerEntity(
            id = existingUserTasker?.id ?: 0,
            name = publicName.trim(),
            initials = initials,
            roleType = kindRole.id,
            categoryId = category.id,
            specialty = specialtyText,
            rating = existingUserTasker?.rating ?: 5.0,
            ratingCount = existingUserTasker?.ratingCount ?: 0,
            completedTasks = existingUserTasker?.completedTasks ?: 0,
            fiveStarCount = existingUserTasker?.fiveStarCount ?: 0,
            fourStarCount = existingUserTasker?.fourStarCount ?: 0,
            threeStarCount = existingUserTasker?.threeStarCount ?: 0,
            twoStarCount = existingUserTasker?.twoStarCount ?: 0,
            oneStarCount = existingUserTasker?.oneStarCount ?: 0,
            priceRange = priceFormatted,
            area = area.ifBlank { currentUser?.defaultArea?.ifBlank { "Local Area" } ?: "Local Area" },
            isVerified = isAlreadyVerified,
            isAvailable = true,
            avatarColorType = colorType,
            bio = bio.ifBlank { "Skilled ${kindRole.title} offering ${category.title.lowercase()} on Taska." },
            skillsCsv = listOfNotNull(
                firstListingTitle.takeIf { it.isNotBlank() },
                category.title,
                kindRole.title,
                businessName.takeIf { it.isNotBlank() }
            ).joinToString(","),
            servicesListedCsv = if (existingUserTasker != null && existingUserTasker.servicesListedCsv.isNotBlank() && firstListingTitle.isBlank()) {
                existingUserTasker.servicesListedCsv
            } else {
                serviceEntry
            },
            storePhotosCsv = storePhotosSerialized.ifBlank { existingUserTasker?.storePhotosCsv.orEmpty() },
            reviewsCsv = existingUserTasker?.reviewsCsv.orEmpty(),
            isUserCreated = true
        )
        val newTaskerId = dao.insertTasker(tasker).toInt()

        var updatedAccount: UserAccountEntity? = currentUser
        if (currentUser != null) {
            val uid = currentUser.supabaseUserId.ifBlank { currentUser.id.toString() }
            val providerId = supabaseService.saveEarningProfileOnSupabase(
                accessToken = null,
                userId = uid,
                kind = kindRole.id,
                displayName = publicName,
                bio = bio,
                phoneE164 = phoneE164,
                businessName = businessName,
                businessRegNumber = businessRegNumber,
                broadArea = area,
                firstListingTitle = firstListingTitle,
                firstListingCategorySlug = category.id,
                firstListingPriceZmw = firstListingPrice
            )
            if (legalFullName.isNotBlank() && nationalIdNumber.isNotBlank()) {
                supabaseService.submitVerificationToSupabase(
                    userId = uid,
                    providerId = providerId,
                    legalFullName = legalFullName,
                    nationalIdNumber = nationalIdNumber,
                    hasIdPhoto = hasIdPhoto,
                    hasSelfiePhoto = hasSelfiePhoto
                )
            }
            val nextVerStatus = when {
                isAlreadyVerified -> "verified"
                hasSubmittedDocs -> "pending_review"
                else -> currentUser.verificationStatus.ifBlank { "unsubmitted" }
            }
            updatedAccount = currentUser.copy(
                earningKind = kindRole.id,
                earningPhoneE164 = phoneE164.trim(),
                legalFullName = legalFullName.trim().ifBlank { currentUser.legalFullName },
                nationalIdNumber = nationalIdNumber.trim().ifBlank { currentUser.nationalIdNumber },
                businessName = businessName.trim(),
                businessRegistrationNumber = businessRegNumber.trim(),
                hasEarningProfile = true,
                storePhotosCsv = storePhotosSerialized.ifBlank { currentUser.storePhotosCsv },
                verificationStatus = nextVerStatus
            )
            dao.updateUserAccount(updatedAccount)
            if (currentUser.supabaseUserId.isNotBlank()) {
                supabaseService.upsertProfileRow(
                    userId = currentUser.supabaseUserId,
                    displayName = updatedAccount.fullName,
                    age = updatedAccount.age,
                    gender = updatedAccount.gender,
                    bio = bio.ifBlank { updatedAccount.bio },
                    defaultArea = area.ifBlank { updatedAccount.defaultArea },
                    hasEarningProfile = true,
                    storePhotosCsv = updatedAccount.storePhotosCsv
                )
            }
        }

        dao.insertInboxNotification(
            InboxNotificationEntity(
                title = "Earning profile active: ${publicName.trim()}",
                body = if (hasSubmittedDocs) {
                    "Your ${kindRole.title} profile is live and your ID verification documents are under private review."
                } else {
                    "Your ${kindRole.title} profile is live! You can now manage listings, store photos, and Taska Credits."
                },
                category = "offer",
                isPrivateForSignedInUser = true,
                timeLabel = "Just now"
            )
        )

        return tasker.copy(id = newTaskerId) to updatedAccount
    }

    suspend fun addServiceListingToUserTasker(
        title: String,
        categoryName: String,
        description: String,
        priceZmw: String,
        photoUris: List<String> = emptyList()
    ) {
        val all = dao.getAllTaskersOnce()
        val target = all.firstOrNull { it.isUserCreated } ?: all.firstOrNull() ?: return
        val priceLabel = if (priceZmw.isBlank()) "Ask" else "ZMW ${priceZmw.trim()}"
        val cappedPhotos = photoUris.take(6)
        val photosToken = cappedPhotos.joinToString(";")
        val newEntry = "${title.trim()}::${categoryName.trim()}::$priceLabel::$photosToken"
        val updatedServices = if (target.servicesListedCsv.isBlank()) {
            newEntry
        } else {
            "${target.servicesListedCsv}||$newEntry"
        }
        dao.updateTasker(target.copy(servicesListedCsv = updatedServices))
        dao.insertInboxNotification(
            InboxNotificationEntity(
                title = "Listing published: ${title.trim()}",
                body = "$categoryName ($priceLabel) with ${cappedPhotos.size} photo(s) is now live on your Tasker profile.",
                category = "offer",
                isPrivateForSignedInUser = true,
                timeLabel = "Just now"
            )
        )
    }

    suspend fun updateStorePhotosForUserTasker(
        currentUser: UserAccountEntity?,
        storePhotoUris: List<String>
    ): UserAccountEntity? {
        val capped = storePhotoUris.take(6)
        val serialized = capped.joinToString("||")
        val all = dao.getAllTaskersOnce()
        val target = all.firstOrNull { it.isUserCreated }
        if (target != null) {
            dao.updateTasker(target.copy(storePhotosCsv = serialized))
        }
        if (currentUser != null) {
            val updated = currentUser.copy(storePhotosCsv = serialized)
            dao.updateUserAccount(updated)
            if (currentUser.supabaseUserId.isNotBlank()) {
                supabaseService.upsertProfileRow(
                    userId = currentUser.supabaseUserId,
                    displayName = updated.fullName,
                    age = updated.age,
                    gender = updated.gender,
                    bio = updated.bio,
                    defaultArea = updated.defaultArea,
                    hasEarningProfile = updated.hasEarningProfile,
                    storePhotosCsv = serialized
                )
            }
            return updated
        }
        return null
    }

    suspend fun submitCreditTopUp(
        user: UserAccountEntity,
        creditsRequested: Int,
        senderName: String,
        senderPhone: String,
        reference: String
    ): UserAccountEntity {
        supabaseService.submitCreditTopUpToSupabase(
            userId = user.supabaseUserId,
            creditsRequested = creditsRequested,
            senderName = senderName,
            senderPhone = senderPhone,
            reference = reference
        )
        val updated = user.copy(taskaCredits = user.taskaCredits + creditsRequested)
        dao.updateUserAccount(updated)
        val usdAmount = creditsRequested / 20.0
        dao.insertInboxNotification(
            InboxNotificationEntity(
                title = "+$creditsRequested Taska Credits Top-Up Submitted",
                body = "Payment proof from $senderName (US$${String.format("%.2f", usdAmount)} · Ref: ${reference.ifBlank { "Mobile Money" }}) verified.",
                category = "credits",
                isPrivateForSignedInUser = true,
                timeLabel = "Just now"
            )
        )
        return updated
    }

    suspend fun createTaskerProfile(tasker: TaskerEntity): Long {
        return dao.insertTasker(tasker)
    }

    suspend fun updateMarketplaceRequest(request: MarketplaceRequestEntity) {
        dao.updateMarketplaceRequest(request)
    }
}
