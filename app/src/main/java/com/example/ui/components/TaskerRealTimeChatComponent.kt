package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.data.SupabaseChatMessage
import com.example.data.TaskaCategory
import com.example.ui.theme.CategoryPeach
import com.example.ui.theme.CategoryPeachIcon
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

/**
 * Real-time Chat Interface UI Component where clients and verified Taskers communicate,
 * backed by Room persistence and Supabase `chat_threads` + `chat_messages` real-time sync.
 */
@Composable
fun TaskerRealTimeChatComponent(
    booking: BookingEntity,
    liveSupabaseMessages: List<SupabaseChatMessage> = emptyList(),
    isSupabaseConnected: Boolean = true,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onRefreshLiveChat: () -> Unit = {},
    onCompleteWithRating: (Int) -> Unit,
    onIssueCompletionPin: () -> Unit,
    onSpeakMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var msgInput by remember { mutableStateOf("") }
    var showPinBox by remember { mutableStateOf(false) }

    val localMessages = remember(booking.privateChatHistory) {
        booking.privateChatHistory.split("||").mapNotNull { entry ->
            val parts = entry.split("::")
            if (parts.size >= 2) {
                Triple(parts[0], parts[1], parts.getOrElse(2) { "Now" })
            } else null
        }
    }

    // Merge local serialized messages with any new real-time messages from Supabase `chat_messages`
    val mergedMessages = remember(localMessages, liveSupabaseMessages) {
        val combined = localMessages.toMutableList()
        val existingBodies = localMessages.map { it.second.trim() }.toSet()
        liveSupabaseMessages.forEach { remoteMsg ->
            if (remoteMsg.body.trim() !in existingBodies) {
                combined.add(
                    Triple(
                        remoteMsg.senderLabel,
                        remoteMsg.body,
                        remoteMsg.createdAt
                    )
                )
            }
        }
        combined
    }

    val categoryBadge = remember(booking.categoryId) {
        TaskaCategory.entries.firstOrNull { it.id == booking.categoryId }?.title ?: "Marketplace"
    }

    val quickCoordinationPrompts = remember(booking.taskerName, booking.area) {
        listOf(
            "Hi ${booking.taskerName}, are you available in ${booking.area} today?",
            "Can we confirm the final ZMW price before starting?",
            "I'm ready at the agreed pickup/meeting spot.",
            "Great work! I'm ready to share the Completion PIN."
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TaskaCream)
            .testTag("realtime_tasker_chat_component")
    ) {
        // 1. Top Bar: Verified Tasker Identity + Supabase Real-Time Status
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
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to My Tasks",
                            tint = TaskaInk
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CategoryPeach),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = booking.taskerInitials,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = CategoryPeachIcon
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = booking.taskerName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TaskaInk
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified Tasker",
                                tint = TaskaViolet,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = if (isSupabaseConnected) VerifiedGreenBg else TaskaVioletSoft,
                                shape = RoundedCornerShape(50)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = if (isSupabaseConnected) VerifiedGreen else TaskaViolet,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Verified Private Chat",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSupabaseConnected) VerifiedGreen else TaskaViolet
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "· Phone hidden",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = TaskaMutedText
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRefreshLiveChat,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("sync_live_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Refresh private chat messages",
                            tint = TaskaViolet,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (booking.status != "Completed") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(
                            onClick = { onCompleteWithRating(5) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerifiedGreen,
                                contentColor = TaskaWhite
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("mark_task_done_button")
                        ) {
                            Text("Mark Done ★5", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Service Requirement Summary & Completion PIN Banner
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = TaskaVioletSoft,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = categoryBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = TaskaViolet,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = booking.taskTitle.ifBlank { booking.taskRequestText },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TaskaInk,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = booking.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (booking.status == "Completed") VerifiedGreen else TaskaViolet
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Requirement: ${booking.taskRequestText} · Area: ${booking.area}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = TaskaInk
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Keep communication in Taska for privacy. Personal phone numbers remain hidden.",
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
                            text = "${booking.priceEstimate}${if (booking.budgetNegotiable) " · Negotiable" else " · Fixed"}",
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
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = TaskaViolet,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Share this 6-digit Completion PIN only once the task is finished: ${booking.completionPin.ifBlank { "Issued on demand" }}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TaskaViolet
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Message Stream (Room + Real-Time Supabase `chat_messages`)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(mergedMessages) { (sender, body, timestampLabel) ->
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
                            shape = RoundedCornerShape(
                                topStart = 18.dp,
                                topEnd = 18.dp,
                                bottomStart = if (isUser) 18.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 18.dp
                            ),
                            border = if (isUser) null else BorderStroke(1.dp, TaskaBorder),
                            modifier = Modifier.fillMaxWidth(0.84f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = sender,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isUser) TaskaLime else TaskaViolet,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = timestampLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = if (isUser) TaskaWhite.copy(alpha = 0.7f) else TaskaMutedText
                                        )
                                    }
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

        // 4. Quick Coordination Prompt Chips + Real-Time Composer
        if (booking.status != "Completed") {
            Surface(
                color = TaskaWhite,
                shadowElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickCoordinationPrompts.forEachIndexed { index, promptText ->
                            Surface(
                                color = TaskaVioletSoft,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .clickable {
                                        onSendMessage(promptText)
                                    }
                                    .testTag("quick_chat_chip_$index")
                            ) {
                                Text(
                                    text = promptText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TaskaViolet,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = msgInput,
                            onValueChange = { msgInput = it },
                            placeholder = {
                                Text(
                                    text = "Message ${booking.taskerName} privately in Taska…",
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
            }
        } else {
            Surface(
                color = TaskaWhite,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "This task is complete. Its chat is safely stored in your Taska history.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TaskaMutedText,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
