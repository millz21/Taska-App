package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class TaskaCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val shortBadge: String
) {
    PRINTING_SUPPLIES(
        id = "printing_supplies",
        title = "Printing",
        subtitle = "Notes, binding & deadline rush.",
        shortBadge = "Printing"
    ),
    DEVICE_REPAIRS(
        id = "device_repairs",
        title = "Repairs",
        subtitle = "Laptops, screens & charging ports.",
        shortBadge = "Repairs"
    ),
    TUTORING(
        id = "tutoring",
        title = "Tutoring",
        subtitle = "Learn from verified local tutors.",
        shortBadge = "Tutoring"
    ),
    MOVING(
        id = "moving",
        title = "Moving",
        subtitle = "Bakkies, boxes & room moves.",
        shortBadge = "Moving"
    ),
    ERRANDS_MOVING(
        id = "errands_moving",
        title = "Errands",
        subtitle = "Deliveries, pickups & shopping.",
        shortBadge = "Errands"
    ),
    LOCAL_STORES(
        id = "local_stores",
        title = "Shop local",
        subtitle = "Products from local stores.",
        shortBadge = "Shop local"
    ),
    CARS_AUTO(
        id = "cars_auto",
        title = "Cars & auto",
        subtitle = "Diagnostics, mechanics & wash.",
        shortBadge = "Cars & auto"
    ),
    HOMES_ROOMS(
        id = "homes_rooms",
        title = "Homes & rooms",
        subtitle = "Boarding houses, rooms & flats.",
        shortBadge = "Homes & rooms"
    ),
    PHONES_GADGETS(
        id = "phones_gadgets",
        title = "Phones & gadgets",
        subtitle = "Accessories, cables & tech.",
        shortBadge = "Phones & gadgets"
    ),
    FOOD_NEARBY(
        id = "food_nearby",
        title = "Food nearby",
        subtitle = "Fresh local meals & baking.",
        shortBadge = "Food nearby"
    ),
    PHARMACIES(
        id = "pharmacies",
        title = "Pharmacies",
        subtitle = "Health essentials & wellness.",
        shortBadge = "Pharmacies"
    ),
    HOME_SERVICES(
        id = "home_services",
        title = "Home services",
        subtitle = "Plumbing, electrical & cleaning.",
        shortBadge = "Home services"
    ),
    DIGITAL_SERVICES(
        id = "digital_services",
        title = "Digital services",
        subtitle = "Design, CVs, editing & code.",
        shortBadge = "Digital"
    )
}

@Serializable
enum class TaskerRoleType(
    val id: String,
    val title: String,
    val formLabel: String,
    val description: String
) {
    SERVICE_TASKER(
        id = "service_tasker",
        title = "Service Tasker",
        formLabel = "Offer a service",
        description = "Offer services like device repairs, tutoring, printing, errands and more."
    ),
    TASKA_SELLER(
        id = "taska_seller",
        title = "Taska Seller",
        formLabel = "Sell products",
        description = "Sell products from your business or store."
    ),
    DIGITAL_TASKER(
        id = "digital_tasker",
        title = "Digital Tasker",
        formLabel = "Offer digital services",
        description = "Offer digital services like design, editing, writing, and more."
    ),
    REGISTER_BUSINESS(
        id = "business",
        title = "Registered Business",
        formLabel = "Register a business",
        description = "Represent a local store, pharmacy, eatery, or company on Taska."
    )
}

@Serializable
@Entity(tableName = "taskers")
data class TaskerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val initials: String,
    val roleType: String, // TaskerRoleType.id
    val categoryId: String, // TaskaCategory.id
    val specialty: String,
    val rating: Double,
    val ratingCount: Int = 1,
    val fiveStarCount: Int = 1,
    val fourStarCount: Int = 0,
    val threeStarCount: Int = 0,
    val twoStarCount: Int = 0,
    val oneStarCount: Int = 0,
    val responseRatePercent: Int = 98,
    val averageResponseMinutes: Int = 10,
    val completedTasks: Int,
    val priceRange: String,
    val area: String,
    val isVerified: Boolean = true,
    val isAvailable: Boolean = true,
    val isFavourite: Boolean = false,
    val avatarColorType: String, // "peach", "lavender", "mint", "butter", "sky", "gray"
    val bio: String,
    val skillsCsv: String,
    val servicesListedCsv: String = "", // Pipe-delimited "Title::Category::Price::Description::PhotoCount"
    val storePhotosCsv: String = "", // Pipe-delimited store/business/portfolio image URIs or labels (max 6)
    val reviewsCsv: String = "", // Pipe-delimited "Stars::Comment::ReviewerName::DateLabel"
    val isUserCreated: Boolean = false
)

@Serializable
@Entity(tableName = "tasker_reviews")
data class TaskerReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskerId: Int,
    val taskerName: String,
    val reviewerName: String,
    val ratingStars: Int,
    val comment: String,
    val serviceTitle: String = "",
    val isVerifiedBooking: Boolean = true,
    val dateLabel: String = "Just now",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String = "",
    val label: String,
    val areaName: String,
    val latitude: Double = -15.3875,
    val longitude: Double = 28.3228,
    val radiusKm: Int = 10,
    val isPrimary: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "conversation_sessions")
data class ConversationSessionEntity(
    @PrimaryKey val sessionId: String,
    val title: String,
    val previewText: String,
    val area: String = "",
    val messageCount: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val categoryId: String = TaskaCategory.DEVICE_REPAIRS.id,
    val area: String = "",
    val budgetZmw: String = "Open to offers",
    val budgetNegotiable: Boolean = true,
    val urgencyOrTiming: String = "Flexible",
    val assignedTaskerId: Int? = null,
    val assignedTaskerName: String = "",
    val assignedTaskerInitials: String = "TK",
    val assignedTaskerVerified: Boolean = true,
    val requesterUserId: String = "",
    val status: String = "Open", // "Open", "Offer Ready", "Active in Private Chat", "Completed"
    val completionPin: String = "",
    val supabaseRequestId: String = "",
    val supabaseThreadId: String = "",
    val chatHistorySerialized: String = "", // Pipe-delimited "Sender::Message::Time"
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class SupabaseChatMessage(
    val id: String = "",
    val threadId: String = "",
    val senderId: String = "",
    val senderLabel: String = "You",
    val body: String,
    val createdAt: String = "Just now",
    val isFromCurrentUser: Boolean = true
)

@Serializable
@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskerId: Int,
    val taskerName: String,
    val taskerInitials: String,
    val taskerSpecialty: String,
    val categoryId: String,
    val taskTitle: String = "",
    val taskRequestText: String,
    val area: String,
    val status: String, // "Open · Offer Ready", "Active in Private Chat", "In Progress", "Completed"
    val priceEstimate: String,
    val clientBudget: String = "",
    val budgetNegotiable: Boolean = true,
    val completionPin: String = "",
    val supabaseRequestId: String = "",
    val supabaseThreadId: String = "",
    val privateChatHistory: String, // Pipe-delimited "Sender::Message::Time"
    val userRating: Int? = null,
    val writtenReview: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val threadId: String = "default_thread",
    val isFromUser: Boolean,
    val text: String,
    val matchedTaskerIdsCsv: String = "",
    val modelTag: String = "",
    val groundingInfo: String = "",
    val attachedPhotoSummary: String = "",
    val canRequestMissingService: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "marketplace_requests")
data class MarketplaceRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val categoryId: String,
    val area: String,
    val timing: String,
    val budget: String,
    val budgetNegotiable: Boolean = true,
    val details: String,
    val clientAlias: String, // Kept private (no phone numbers shared)
    val status: String = "Open", // "Open", "Accepted", "Proposal Sent", "Declined"
    val proposedPrice: String = "",
    val aiProposalText: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "inbox_notifications")
data class InboxNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val body: String,
    val category: String = "announcement", // "announcement", "booking_update", "offer", "credits"
    val isPrivateForSignedInUser: Boolean = false,
    val timeLabel: String = "Just now",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "service_demands")
data class ServiceDemandEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val requestText: String,
    val broadArea: String,
    val syncedWithSupabase: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val email: String,
    val password: String,
    val defaultArea: String = "",
    val age: String = "",
    val gender: String = "",
    val bio: String = "",
    val memberRole: String = "Client & Tasker", // "Book Help", "Earn as Tasker", "Client & Tasker"
    val hasEarningProfile: Boolean = false,
    val taskaCredits: Int = 0,
    val earningKind: String = "service_tasker",
    val earningPhoneE164: String = "",
    val storePhotosCsv: String = "", // Up to 6 store/business photo URIs or labels
    val verificationStatus: String = "unsubmitted", // "unsubmitted", "pending_review", "verified"
    val legalFullName: String = "",
    val nationalIdNumber: String = "",
    val businessName: String = "",
    val businessRegistrationNumber: String = "",
    val supabaseUserId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val avatarInitials: String
        get() {
            val parts = fullName.trim().split(" ").filter { it.isNotBlank() }
            return when {
                parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "TK"
            }
        }
}
