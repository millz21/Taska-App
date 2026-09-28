package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BookingEntity
import com.example.data.SavedLocationEntity
import com.example.data.SupabaseChatMessage
import com.example.data.Task
import com.example.data.TaskaCategory
import com.example.data.UserAccountEntity
import com.example.ui.components.RealWorldLocationAndMapHelper
import com.example.ui.components.TaskLocationPin
import com.example.ui.components.TaskLocationsGoogleMapCard
import com.example.ui.components.TaskaMixedHeadline
import com.example.ui.components.TaskerRealTimeChatComponent
import com.example.ui.theme.CategoryPeach
import com.example.ui.theme.CategoryPeachIcon
import com.example.ui.theme.StarAmber
import com.example.ui.theme.TaskaBorder
import com.example.ui.theme.TaskaCream
import com.example.ui.theme.TaskaInk
import com.example.ui.theme.TaskaLime
import com.example.ui.theme.TaskaMutedText
import com.example.ui.theme.TaskaViolet
import com.example.ui.theme.TaskaVioletSoft
import com.example.ui.theme.TaskaWhite
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenBg

@Composable
fun BookingsAndTrustScreen(
    bookings: List<BookingEntity>,
    tasks: List<Task> = emptyList(),
    selectedArea: String = "Lusaka",
    savedLocations: List<SavedLocationEntity> = emptyList(),
    liveSupabaseChatMessages: List<SupabaseChatMessage> = emptyList(),
    isSupabaseConnected: Boolean = true,
    activeBookingChatId: Int?,
    currentUser: UserAccountEntity? = null,
    onSelectBookingChat: (Int?) -> Unit,
    onSendPrivateMessage: (BookingEntity, String) -> Unit,
    onRefreshLiveChat: () -> Unit = {},
    onCompleteAndRateBooking: (BookingEntity, Int) -> Unit,
    onIssueCompletionPin: (BookingEntity) -> Unit = {},
    onDeleteTaskRequirement: (Int) -> Unit = {},
    onOpenCreateTaskSheet: () -> Unit = {},
    onOpenLocationMap: () -> Unit = {},
    onSpeakMessage: (String) -> Unit,
    onGoToExplore: () -> Unit,
    onRequestSignInOrSignUp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activeBooking = bookings.firstOrNull { it.id == activeBookingChatId }
    val taskLocationPins = remember(tasks, bookings, savedLocations, selectedArea) {
        val fromTasks = tasks.mapIndexed { idx, t ->
            val (lat, lng) = RealWorldLocationAndMapHelper.resolveTaskAreaCoordinates(
                areaName = t.area.ifBlank { selectedArea },
                savedLocations = savedLocations,
                indexOffset = idx
            )
            TaskLocationPin(
                id = "task_${t.id}",
                title = t.title,
                subtitle = t.description,
                areaName = t.area.ifBlank { selectedArea.ifBlank { "Lusaka" } },
                badgeText = t.budgetZmw,
                latitude = lat,
                longitude = lng,
                isCompleted = t.status.equals("Completed", ignoreCase = true)
            )
        }
        val fromBookings = bookings.mapIndexed { idx, b ->
            val (lat, lng) = RealWorldLocationAndMapHelper.resolveTaskAreaCoordinates(
                areaName = b.area.ifBlank { selectedArea },
                savedLocations = savedLocations,
                indexOffset = tasks.size + idx + 1
            )
            TaskLocationPin(
                id = "booking_${b.id}",
                title = b.taskTitle.ifBlank { b.taskerName },
                subtitle = b.taskRequestText,
                areaName = b.area.ifBlank { selectedArea.ifBlank { "Lusaka" } },
                badgeText = b.status,
                latitude = lat,
                longitude = lng,
                isCompleted = b.status.equals("Completed", ignoreCase = true)
            )
        }
        (fromTasks + fromBookings).distinctBy { "${it.title}_${it.areaName}" }
    }

    if (activeBooking != null && currentUser != null) {
        BackHandler {
            onSelectBookingChat(null)
        }
        TaskerRealTimeChatComponent(
            booking = activeBooking,
            liveSupabaseMessages = liveSupabaseChatMessages,
            isSupabaseConnected = isSupabaseConnected,
            onBack = { onSelectBookingChat(null) },
            onSendMessage = { text -> onSendPrivateMessage(activeBooking, text) },
            onRefreshLiveChat = onRefreshLiveChat,
            onCompleteWithRating = { stars -> onCompleteAndRateBooking(activeBooking, stars) },
            onIssueCompletionPin = { onIssueCompletionPin(activeBooking) },
            onSpeakMessage = onSpeakMessage,
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TaskaCream),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Electric Violet Trust & Privacy Hero Section ("Work with people. Not random numbers.")
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = TaskaViolet),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "TRUST WITHOUT THE AWKWARDNESS",
                        style = MaterialTheme.typography.labelMedium,
                        color = TaskaLime
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TaskaMixedHeadline(
                        boldPrefix = "Work with people.\n",
                        italicSuffix = "Not random numbers.",
                        fontSize = 30.sp,
                        lineHeight = 34.sp,
                        textColor = TaskaWhite,
                        italicColor = TaskaWhite
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Taska keeps task conversations, booking history, Completion PINs, and ratings in one private place. Verified providers are easier to spot, while every booking leaves a record.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TaskaWhite.copy(alpha = 0.88f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // "Taska protects your details" Shield Card
                    Surface(
                        color = TaskaWhite.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, TaskaWhite.copy(alpha = 0.22f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(TaskaLime),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = TaskaInk,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Taska protects your details",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TaskaWhite
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            PrivacyRowItem("Private task chat", "On", TaskaWhite)
                            HorizontalDivider(color = TaskaWhite.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))
                            PrivacyRowItem("Personal phone number", "Not shared", TaskaWhite.copy(alpha = 0.85f))
                            HorizontalDivider(color = TaskaWhite.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))
                            PrivacyRowItem("Verified provider badge", "✓ Confirmed", TaskaLime)
                            HorizontalDivider(color = TaskaWhite.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 8.dp))
                            PrivacyRowItem("Completion PIN & history", "Available", TaskaWhite)
                        }
                    }
                }
            }
        }

        // 2. Guest vs Signed-In My Tasks & Private Chats Section
        if (currentUser == null) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = TaskaWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TaskaBorder, RoundedCornerShape(22.dp))
                        .testTag("bookings_guest_gate_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(TaskaVioletSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = TaskaViolet,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Save your Taskas in one place",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TaskaInk
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sign in to keep bookings, private chats, Completion PINs, future schedules, and task history.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onRequestSignInOrSignUp,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TaskaViolet,
                                contentColor = TaskaWhite
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("bookings_guest_sign_in_button")
                        ) {
                            Text("Sign In or Sign Up to Unlock Chats", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "My Tasks (${bookings.size})",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TaskaInk
                        )
                        Text(
                            text = "Active, upcoming, and completed bookings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText
                        )
                    }
                    Button(
                        onClick = onOpenCreateTaskSheet,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TaskaInk,
                            contentColor = TaskaWhite
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("my_tasks_create_taska_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create a Taska", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                TaskLocationsGoogleMapCard(
                    sectionTitle = "Interactive Task Locations Map",
                    sectionSubtitle = "View your active tasks and bookings on Google Maps (${taskLocationPins.size} locations)",
                    selectedArea = selectedArea,
                    taskPins = taskLocationPins,
                    savedLocations = savedLocations,
                    onSelectTaskPin = { pin ->
                        if (pin.id.startsWith("booking_")) {
                            val bookingId = pin.id.removePrefix("booking_").toIntOrNull()
                            if (bookingId != null) onSelectBookingChat(bookingId)
                        } else if (pin.id.startsWith("task_")) {
                            val matching = bookings.firstOrNull { it.taskTitle == pin.title }
                            if (matching != null) onSelectBookingChat(matching.id) else onOpenLocationMap()
                        }
                    },
                    onOpenFullLocationMap = onOpenLocationMap
                )
            }

            if (tasks.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Marketplace Service Requirements (${tasks.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TaskaInk
                        )
                        Text(
                            text = "Service requirements you defined for your local area.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText
                        )
                    }
                }
                items(tasks, key = { "task_${it.id}" }) { taskItem ->
                    val catTitle = TaskaCategory.entries.firstOrNull { it.id == taskItem.categoryId }?.title ?: "Service"
                    val matchingBooking = bookings.firstOrNull {
                        it.taskTitle == taskItem.title || (it.supabaseRequestId.isNotBlank() && it.supabaseRequestId == taskItem.supabaseRequestId)
                    }
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = TaskaWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, TaskaBorder, RoundedCornerShape(20.dp))
                            .testTag("task_requirement_card_${taskItem.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = TaskaVioletSoft,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = catTitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TaskaViolet,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = TaskaLime.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = taskItem.urgencyOrTiming,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TaskaInk,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "${taskItem.budgetZmw}${if (taskItem.budgetNegotiable) " · Negotiable" else ""}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TaskaViolet
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = taskItem.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TaskaInk
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${taskItem.description} · ${taskItem.area}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TaskaMutedText
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { onDeleteTaskRequirement(taskItem.id) },
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, TaskaBorder),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Remove", fontSize = 11.sp, color = TaskaMutedText)
                                }
                                if (matchingBooking != null) {
                                    Button(
                                        onClick = { onSelectBookingChat(matchingBooking.id) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = TaskaViolet,
                                            contentColor = TaskaWhite
                                        ),
                                        shape = RoundedCornerShape(50),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Open Real-Time Chat →", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (bookings.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = TaskaWhite),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, TaskaBorder, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = TaskaViolet,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Taskas yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TaskaInk
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Start with Ask Taska or Explore to create your first private request.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TaskaMutedText
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onGoToExplore,
                                colors = ButtonDefaults.buttonColors(containerColor = TaskaViolet)
                            ) {
                                Text("Explore Verified Taskers")
                            }
                        }
                    }
                }
            } else {
                items(bookings, key = { it.id }) { booking ->
                    BookingSummaryCard(
                        booking = booking,
                        onClick = { onSelectBookingChat(booking.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyRowItem(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TaskaWhite.copy(alpha = 0.9f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontSize = 12.sp,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BookingSummaryCard(
    booking: BookingEntity,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = TaskaWhite),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TaskaBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("booking_card_${booking.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CategoryPeach),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = booking.taskerInitials,
                        fontWeight = FontWeight.ExtraBold,
                        color = CategoryPeachIcon
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = booking.taskTitle.ifBlank { booking.taskerName },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TaskaInk
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = TaskaViolet,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "${booking.taskerName} · ${booking.area} · ${booking.priceEstimate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TaskaMutedText
                    )
                }
                Surface(
                    color = if (booking.status == "Completed") VerifiedGreenBg else TaskaVioletSoft,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = booking.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (booking.status == "Completed") VerifiedGreen else TaskaViolet,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = booking.taskRequestText,
                style = MaterialTheme.typography.bodyMedium,
                color = TaskaInk
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = VerifiedGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Phone number hidden · Tap for private chat & PIN",
                        style = MaterialTheme.typography.labelSmall,
                        color = TaskaMutedText
                    )
                }
                if (booking.userRating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = StarAmber,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "${booking.userRating}/5",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TaskaInk
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivateBookingChatDetailView(
    booking: BookingEntity,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onCompleteWithRating: (Int) -> Unit,
    onIssueCompletionPin: () -> Unit,
    onSpeakMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var msgInput by remember { mutableStateOf("") }
    var showPinBox by remember { mutableStateOf(false) }

    val messages = remember(booking.privateChatHistory) {
        booking.privateChatHistory.split("||").mapNotNull { entry ->
            val parts = entry.split("::")
            if (parts.size >= 2) {
                Triple(parts[0], parts[1], parts.getOrElse(2) { "Now" })
            } else null
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TaskaCream)
    ) {
        // Private Task Chat Top Bar
        Surface(
            color = TaskaWhite,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to My Tasks",
                            tint = TaskaInk
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = booking.taskerName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TaskaInk
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = TaskaLime,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "Private Chat",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TaskaInk,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Personal phone numbers are NOT shared",
                            style = MaterialTheme.typography.labelSmall,
                            color = VerifiedGreen
                        )
                    }
                }

                if (booking.status != "Completed") {
                    Button(
                        onClick = { onCompleteWithRating(5) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerifiedGreen,
                            contentColor = TaskaWhite
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Mark Done ★5", fontSize = 11.sp)
                    }
                }
            }
        }

        // Task Details + Completion PIN Card (matching _TaskDetail from Flutter version)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = TaskaWhite),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .border(1.dp, TaskaBorder, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = booking.taskTitle.ifBlank { booking.taskRequestText },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TaskaInk,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = booking.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TaskaViolet
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Keep communication in Taska for privacy. Sharing phone numbers or moving to another app is your own choice and risk.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = TaskaMutedText
                )

                if (booking.status != "Completed") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                onIssueCompletionPin()
                                showPinBox = !showPinBox
                            },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, TaskaViolet),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("show_completion_pin_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Password,
                                contentDescription = null,
                                tint = TaskaViolet,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (showPinBox) "Hide completion PIN" else "Show completion PIN",
                                fontSize = 11.sp,
                                color = TaskaViolet,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = booking.priceEstimate,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TaskaInk
                        )
                    }

                    AnimatedVisibility(visible = showPinBox) {
                        Surface(
                            color = TaskaVioletSoft,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = "Give this PIN to your Tasker only once work is complete: ${booking.completionPin.ifBlank { "482910" }}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TaskaViolet,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { (sender, body, _) ->
                val isUser = sender == "You"
                val isSystem = sender == "System" || sender == "Taska Shield"

                if (isSystem) {
                    Surface(
                        color = TaskaLime.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = TaskaInk,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = body,
                                style = MaterialTheme.typography.labelSmall,
                                color = TaskaInk
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            color = if (isUser) TaskaViolet else TaskaWhite,
                            shape = RoundedCornerShape(18.dp),
                            border = if (isUser) null else BorderStroke(1.dp, TaskaBorder),
                            modifier = Modifier.fillMaxWidth(0.84f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sender,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isUser) TaskaLime else TaskaViolet,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { onSpeakMessage(body) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Read message aloud",
                                            tint = if (isUser) TaskaWhite else TaskaViolet,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = body,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isUser) TaskaWhite else TaskaInk
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Private Message Composer
        if (booking.status != "Completed") {
            Surface(
                color = TaskaWhite,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = msgInput,
                        onValueChange = { msgInput = it },
                        placeholder = {
                            Text(
                                text = "Message privately in Taska…",
                                style = MaterialTheme.typography.bodySmall,
                                color = TaskaMutedText
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TaskaViolet,
                            unfocusedBorderColor = TaskaBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("private_chat_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (msgInput.isNotBlank()) {
                                onSendMessage(msgInput)
                                msgInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(TaskaViolet)
                            .testTag("private_chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send private message",
                            tint = TaskaWhite
                        )
                    }
                }
            }
        } else {
            Surface(
                color = TaskaWhite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "This task is complete. Its chat is safely in your history.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TaskaMutedText,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
