package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskaDao {

    // --- Taskers ---
    @Query("SELECT * FROM taskers ORDER BY isUserCreated DESC, rating DESC")
    fun getAllTaskers(): Flow<List<TaskerEntity>>

    @Query("SELECT * FROM taskers ORDER BY isUserCreated DESC, rating DESC")
    suspend fun getAllTaskersOnce(): List<TaskerEntity>

    @Query("SELECT COUNT(*) FROM taskers")
    suspend fun getTaskerCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskers(taskers: List<TaskerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasker(tasker: TaskerEntity): Long

    @Update
    suspend fun updateTasker(tasker: TaskerEntity)

    @Query("DELETE FROM taskers WHERE isUserCreated = 0")
    suspend fun clearRemoteTaskers()

    @Query("SELECT * FROM taskers WHERE id = :id LIMIT 1")
    suspend fun getTaskerById(id: Int): TaskerEntity?

    @Query("DELETE FROM taskers")
    suspend fun clearAllTaskers()

    // --- Tasker Reviews ---
    @Query("SELECT * FROM tasker_reviews ORDER BY createdAt DESC")
    fun getAllTaskerReviews(): Flow<List<TaskerReviewEntity>>

    @Query("SELECT * FROM tasker_reviews WHERE taskerId = :taskerId ORDER BY createdAt DESC")
    suspend fun getReviewsForTaskerOnce(taskerId: Int): List<TaskerReviewEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskerReview(review: TaskerReviewEntity): Long

    // --- Saved Custom Locations ---
    @Query("SELECT * FROM saved_locations ORDER BY isPrimary DESC, createdAt DESC")
    fun getAllSavedLocations(): Flow<List<SavedLocationEntity>>

    @Query("SELECT * FROM saved_locations ORDER BY isPrimary DESC, createdAt DESC")
    suspend fun getAllSavedLocationsOnce(): List<SavedLocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedLocation(location: SavedLocationEntity): Long

    @Query("DELETE FROM saved_locations WHERE id = :id")
    suspend fun deleteSavedLocationById(id: Int)

    @Query("UPDATE saved_locations SET isPrimary = 0")
    suspend fun clearPrimarySavedLocations()

    // --- Conversation Sessions (Ask Taska History) ---
    @Query("SELECT * FROM conversation_sessions ORDER BY updatedAt DESC")
    fun getAllConversationSessions(): Flow<List<ConversationSessionEntity>>

    @Query("SELECT * FROM conversation_sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getConversationSessionById(sessionId: String): ConversationSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversationSession(session: ConversationSessionEntity)

    @Query("DELETE FROM conversation_sessions WHERE sessionId = :sessionId")
    suspend fun deleteConversationSessionById(sessionId: String)

    // --- Tasks (Service Requirements for Local Marketplace) ---
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE LOWER(area) = LOWER(:area) ORDER BY createdAt DESC")
    fun getTasksByArea(area: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Int): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    // --- Bookings / My Tasks ---
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT COUNT(*) FROM bookings")
    suspend fun getBookingCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    suspend fun getChatMessagesForThreadOnce(threadId: String): List<ChatMessageEntity>

    @Query("SELECT COUNT(*) FROM chat_messages WHERE threadId = :threadId")
    suspend fun getChatMessageCountForThread(threadId: String): Int

    @Query("SELECT COUNT(*) FROM chat_messages")
    suspend fun getChatMessageCount(): Int

    @Query("DELETE FROM chat_messages WHERE isFromUser = 0 AND id NOT IN (SELECT MIN(id) FROM chat_messages WHERE isFromUser = 0 GROUP BY threadId, text)")
    suspend fun deduplicateAssistantGreetings()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE threadId = :threadId")
    suspend fun clearChatMessagesForThread(threadId: String)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()

    // --- Marketplace Requests ---
    @Query("SELECT * FROM marketplace_requests ORDER BY createdAt DESC")
    fun getAllMarketplaceRequests(): Flow<List<MarketplaceRequestEntity>>

    @Query("SELECT COUNT(*) FROM marketplace_requests")
    suspend fun getMarketplaceRequestCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketplaceRequests(requests: List<MarketplaceRequestEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketplaceRequest(request: MarketplaceRequestEntity): Long

    @Update
    suspend fun updateMarketplaceRequest(request: MarketplaceRequestEntity)

    @Query("DELETE FROM marketplace_requests")
    suspend fun clearMarketplaceRequests()

    // --- Inbox Notifications & Platform Announcements ---
    @Query("SELECT * FROM inbox_notifications ORDER BY createdAt DESC")
    fun getAllInboxNotifications(): Flow<List<InboxNotificationEntity>>

    @Query("SELECT COUNT(*) FROM inbox_notifications")
    suspend fun getInboxNotificationCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInboxNotifications(items: List<InboxNotificationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInboxNotification(item: InboxNotificationEntity): Long

    @Query("DELETE FROM inbox_notifications WHERE category = 'announcement' AND isPrivateForSignedInUser = 0")
    suspend fun clearPublicAnnouncements()

    // --- Service Demand Requests ---
    @Query("SELECT * FROM service_demands ORDER BY createdAt DESC")
    fun getAllServiceDemands(): Flow<List<ServiceDemandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceDemand(demand: ServiceDemandEntity): Long

    // --- User Accounts ---
    @Query("DELETE FROM user_accounts WHERE LOWER(email) IN ('maya@campus.edu', 'demo@taska.app', 'chanda@taska.app')")
    suspend fun deleteLegacyMockAccounts()
    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getUserAccountCount(): Int

    @Query("SELECT * FROM user_accounts WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(account: UserAccountEntity): Long

    @Update
    suspend fun updateUserAccount(account: UserAccountEntity)
}
