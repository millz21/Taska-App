package com.example.ui.screens

import android.Manifest
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import com.example.R
import com.example.ai.TaskaGeminiModels
import com.example.data.ChatMessageEntity
import com.example.data.ConversationSessionEntity
import com.example.data.TaskerEntity
import com.example.ui.components.TaskaGlassCard
import com.example.ui.components.TaskaMixedHeadline
import com.example.ui.components.TaskerMatchCard
import com.example.ui.components.TrustStripBanner
import com.example.ui.theme.CoralDot
import com.example.ui.theme.TaskaBorder
import com.example.ui.theme.TaskaCream
import com.example.ui.theme.TaskaDeepInk
import com.example.ui.theme.TaskaGlassWhite
import com.example.ui.theme.TaskaInk
import com.example.ui.theme.TaskaLime
import com.example.ui.theme.TaskaMutedText
import com.example.ui.theme.TaskaPureWhite
import com.example.ui.theme.TaskaViolet
import com.example.ui.theme.TaskaVioletSoft
import com.example.ui.theme.TaskaVioletSolid
import com.example.ui.theme.TaskaWhite
import com.example.ui.theme.VerifiedGreen

@Composable
fun AskTaskaChatScreen(
    selectedArea: String,
    chatMessages: List<ChatMessageEntity>,
    taskers: List<TaskerEntity>,
    selectedAiModel: String,
    useMapsGrounding: Boolean,
    useSearchGrounding: Boolean,
    isAiLoading: Boolean,
    isRecordingAudio: Boolean,
    onSendMessage: (String) -> Unit,
    onSelectModel: (String) -> Unit,
    onToggleMapsGrounding: () -> Unit,
    onToggleSearchGrounding: () -> Unit,
    onAnalyzePhoto: (android.graphics.Bitmap, String) -> Unit,
    onStartAudioRecording: () -> Unit,
    onStopAndTranscribeAudio: () -> Unit,
    onSpeakText: (String) -> Unit,
    onBookTasker: (TaskerEntity, String) -> Unit,
    onOpenHowItWorks: () -> Unit,
    onOpenLiveVoice: () -> Unit,
    onResetChat: () -> Unit,
    onOpenCreateTaskSheet: () -> Unit = {},
    onOpenDemandSheet: () -> Unit = {},
    conversationCount: Int = 1,
    onOpenConversationHistoryScreen: () -> Unit = {},
    onOpenLocationSetup: () -> Unit = {},
    onOpenTaskerProfile: (TaskerEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        onAnalyzePhoto(bitmap, inputText)
                        inputText = ""
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        onStartAudioRecording()
    }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TaskaCream)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // 0. Location Setup Prompt if user has not set a location yet
            if (selectedArea.isBlank()) {
                item {
                    TaskaGlassCard(
                        cornerRadius = 18.dp,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        onClick = onOpenLocationSetup,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("set_location_prompt_banner")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(TaskaVioletSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = TaskaViolet,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Set your location for local matches",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TaskaInk
                                    )
                                    Text(
                                        text = "Use your current GPS location or drop custom pins for your areas.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = TaskaMutedText
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = TaskaVioletSolid,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "Set Location →",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TaskaPureWhite,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 1. Hero Section ("What do you need done?")
            item {
                HeroIntroCard(
                    selectedArea = selectedArea.ifBlank { "your area" },
                    onQuickPromptClick = { prompt ->
                        onSendMessage(prompt)
                    },
                    onOpenHowItWorks = onOpenHowItWorks,
                    onOpenLiveVoice = onOpenLiveVoice
                )
            }

            // 2. Trust Strip Banner
            item {
                TrustStripBanner()
            }

            // 3. Conversation History Bar (Tap bar to open past conversations screen, tap Clear to clear chat)
            item {
                TaskaGlassCard(
                    cornerRadius = 18.dp,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    onClick = onOpenConversationHistoryScreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("conversation_history_bar")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(TaskaVioletSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = TaskaViolet,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "CONVERSATION HISTORY",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TaskaViolet,
                                        letterSpacing = 0.8.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = TaskaVioletSoft,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = "${conversationCount.coerceAtLeast(1)} saved",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = TaskaViolet,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Tap to view & reopen past conversations with Taska",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = TaskaMutedText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            color = CoralDot.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onResetChat() }
                                .testTag("clear_chat_button")
                        ) {
                            Text(
                                text = "Clear",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = CoralDot,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 4. Smart Concierge Mode Controls
            item {
                AiIntelligenceToolbar(
                    selectedAiModel = selectedAiModel,
                    useMapsGrounding = useMapsGrounding,
                    useSearchGrounding = useSearchGrounding,
                    onSelectModel = onSelectModel,
                    onToggleMapsGrounding = onToggleMapsGrounding,
                    onToggleSearchGrounding = onToggleSearchGrounding,
                    onResetChat = onResetChat
                )
            }

            // 5. Multi-Turn Chat Thread Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = TaskaGlassWhite,
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TaskaBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = VerifiedGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (selectedArea.isNotBlank()) "Verified Taskers in $selectedArea" else "Verified Local Taskers",
                                style = MaterialTheme.typography.labelSmall,
                                color = TaskaInk,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = TaskaGlassWhite,
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TaskaBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = TaskaViolet,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Private by default",
                                style = MaterialTheme.typography.labelSmall,
                                color = TaskaInk,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 6. Chat Messages & Embedded Tasker Match Cards
            items(chatMessages, key = { it.id }) { msg ->
                ChatBubbleItem(
                    message = msg,
                    allTaskers = taskers,
                    onSpeakText = onSpeakText,
                    onViewTaskerProfile = onOpenTaskerProfile,
                    onBookTasker = { tasker ->
                        onBookTasker(tasker, msg.text)
                    }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenCreateTaskSheet,
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TaskaViolet),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_create_taska_button")
                    ) {
                        Text("Create a Taska", color = TaskaViolet, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onOpenDemandSheet,
                        shape = RoundedCornerShape(50),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TaskaBorder),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_request_service_button")
                    ) {
                        Text("Request this service", color = TaskaInk, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }

            if (isAiLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = TaskaViolet
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Taska is matching verified local Taskers…",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText
                        )
                    }
                }
            }
        }

        // 7. Bottom Multimodal Chat Composer ("Ask Taska anything...")
        Surface(
            color = TaskaGlassWhite,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (isRecordingAudio) {
                    Surface(
                        color = CoralDot.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(CoralDot)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Listening to your voice note…",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontSize = 12.sp,
                                    color = CoralDot
                                )
                            }
                            Button(
                                onClick = onStopAndTranscribeAudio,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CoralDot,
                                    contentColor = TaskaPureWhite
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Done", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("upload_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Attach task photo",
                            tint = TaskaViolet
                        )
                    }

                    IconButton(
                        onClick = {
                            if (isRecordingAudio) {
                                onStopAndTranscribeAudio()
                            } else {
                                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("transcribe_audio_button")
                    ) {
                        Icon(
                            imageVector = if (isRecordingAudio) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Record voice prompt",
                            tint = if (isRecordingAudio) CoralDot else TaskaInk
                        )
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Ask Taska anything…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TaskaMutedText
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TaskaViolet,
                            unfocusedBorderColor = TaskaBorder,
                            focusedContainerColor = TaskaCream,
                            unfocusedContainerColor = TaskaCream
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ask_taska_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(TaskaVioletSolid)
                            .clickable {
                                if (inputText.isNotBlank()) {
                                    onSendMessage(inputText)
                                    inputText = ""
                                }
                            }
                            .testTag("send_chat_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send to Taska",
                            tint = TaskaPureWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroIntroCard(
    selectedArea: String,
    onQuickPromptClick: (String) -> Unit,
    onOpenHowItWorks: () -> Unit,
    onOpenLiveVoice: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = TaskaWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TaskaBorder, RoundedCornerShape(26.dp))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Hero Visual Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_campus_1790525798777),
                        contentDescription = "Taska local campus marketplace",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        TaskaInk.copy(alpha = 0.65f)
                                    )
                                )
                            )
                    )
                    Surface(
                        color = TaskaLime,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(TaskaInk)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Built for real life. Designed to go anywhere.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TaskaInk,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "THE CHAT-FIRST MARKETPLACE",
                        style = MaterialTheme.typography.labelMedium,
                        color = TaskaViolet
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TaskaMixedHeadline(
                        boldPrefix = "What do you\nneed ",
                        italicSuffix = "done?",
                        fontSize = 34.sp,
                        lineHeight = 38.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tell Taska what you need like you would tell a friend. Find local help in $selectedArea, book privately, and get it done.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TaskaMutedText
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                onQuickPromptClick("I need someone to fix my laptop charging port near campus today.")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TaskaViolet,
                                contentColor = TaskaWhite
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("hero_cta_button")
                        ) {
                            Text("Match me now")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenHowItWorks,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("How it works", color = TaskaInk)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Natural Language Task Starters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val quickIdeas = listOf(
                            "🔧 Fix laptop charging port",
                            "🖨️ Need printing by 4 PM",
                            "🎓 Calculus & CS tutor",
                            "📦 Dorm moving helper",
                            "💻 Logo & CV design"
                        )
                        quickIdeas.forEach { idea ->
                            Surface(
                                color = TaskaVioletSoft,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onQuickPromptClick(idea) }
                            ) {
                                Text(
                                    text = idea,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontSize = 12.sp,
                                    color = TaskaViolet,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiIntelligenceToolbar(
    selectedAiModel: String,
    useMapsGrounding: Boolean,
    useSearchGrounding: Boolean,
    onSelectModel: (String) -> Unit,
    onToggleMapsGrounding: () -> Unit,
    onToggleSearchGrounding: () -> Unit,
    onResetChat: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedAiModel == TaskaGeminiModels.FLASH_LITE,
                onClick = { onSelectModel(TaskaGeminiModels.FLASH_LITE) },
                label = { Text("Instant Match", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TaskaLime,
                    selectedLabelColor = TaskaDeepInk
                )
            )

            FilterChip(
                selected = selectedAiModel == TaskaGeminiModels.FLASH,
                onClick = { onSelectModel(TaskaGeminiModels.FLASH) },
                label = { Text("Smart Concierge", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TaskaVioletSoft,
                    selectedLabelColor = TaskaViolet
                )
            )

            FilterChip(
                selected = selectedAiModel == TaskaGeminiModels.PRO,
                onClick = { onSelectModel(TaskaGeminiModels.PRO) },
                label = { Text("Deep Photo & Task Analysis", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TaskaVioletSolid,
                    selectedLabelColor = TaskaPureWhite,
                    selectedLeadingIconColor = TaskaPureWhite
                )
            )

            FilterChip(
                selected = useMapsGrounding,
                onClick = onToggleMapsGrounding,
                label = { Text("Nearby Places", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                }
            )

            FilterChip(
                selected = useSearchGrounding,
                onClick = onToggleSearchGrounding,
                label = { Text("Live Prices", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessageEntity,
    allTaskers: List<TaskerEntity>,
    onSpeakText: (String) -> Unit,
    onViewTaskerProfile: (TaskerEntity) -> Unit = {},
    onBookTasker: (TaskerEntity) -> Unit
) {
    val matchedTaskers = remember(message.matchedTaskerIdsCsv, allTaskers) {
        if (message.matchedTaskerIdsCsv.isBlank()) {
            emptyList()
        } else {
            val ids = message.matchedTaskerIdsCsv.split(",").mapNotNull { it.trim().toIntOrNull() }
            allTaskers.filter { it.id in ids }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (message.isFromUser) TaskaVioletSolid else TaskaGlassWhite,
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (message.isFromUser) 20.dp else 6.dp,
                bottomEnd = if (message.isFromUser) 6.dp else 20.dp
            ),
            border = if (message.isFromUser) null else androidx.compose.foundation.BorderStroke(1.dp, TaskaBorder),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (message.attachedPhotoSummary.isNotBlank()) {
                    Surface(
                        color = TaskaPureWhite.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "📷 ${message.attachedPhotoSummary}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (message.isFromUser) TaskaLime else TaskaViolet,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isFromUser) TaskaPureWhite else TaskaInk
                )

                if (!message.isFromUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = TaskaVioletSoft,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "Taska Concierge",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TaskaViolet,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (message.groundingInfo.isNotBlank() && !message.groundingInfo.contains("Supabase", ignoreCase = true)) {
                                Surface(
                                    color = TaskaLime.copy(alpha = 0.75f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = message.groundingInfo,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TaskaDeepInk,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Surface(
                            color = TaskaCream,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onSpeakText(message.text) }
                                .testTag("tts_speak_button_${message.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Read aloud",
                                    tint = TaskaViolet,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Listen",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TaskaViolet,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (matchedTaskers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier.fillMaxWidth(0.94f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                matchedTaskers.forEach { tasker ->
                    TaskerMatchCard(
                        tasker = tasker,
                        onBookPrivately = onBookTasker,
                        onViewProfile = onViewTaskerProfile,
                        onSpeakBio = onSpeakText,
                        compact = true
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationHistoryScreen(
    sessions: List<ConversationSessionEntity>,
    activeSessionId: String,
    onSelectSession: (String) -> Unit,
    onStartNewConversation: () -> Unit,
    onDeleteSession: (String) -> Unit,
    onClearAllCurrentChat: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TaskaCream)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = TaskaGlassWhite,
                    shape = RoundedCornerShape(50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TaskaBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onBack() }
                        .testTag("history_back_button")
                ) {
                    Text(
                        text = "← Back to Ask Taska",
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TaskaInk,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            Button(
                onClick = onStartNewConversation,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaskaVioletSolid,
                    contentColor = TaskaPureWhite
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier.testTag("start_new_conversation_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "ASK TASKA ARCHIVE",
            style = MaterialTheme.typography.labelMedium,
            color = TaskaViolet
        )
        Spacer(modifier = Modifier.height(4.dp))
        TaskaMixedHeadline(
            boldPrefix = "Conversation ",
            italicSuffix = "history.",
            fontSize = 30.sp,
            lineHeight = 34.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Tap any past conversation to reopen it and continue where you left off.",
            style = MaterialTheme.typography.bodySmall,
            color = TaskaMutedText
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(sessions, key = { it.sessionId }) { session ->
                val isCurrent = session.sessionId == activeSessionId
                TaskaGlassCard(
                    cornerRadius = 20.dp,
                    borderColor = if (isCurrent) TaskaViolet else TaskaBorder,
                    onClick = { onSelectSession(session.sessionId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("conversation_session_card_${session.sessionId}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = if (isCurrent) TaskaVioletSolid else TaskaVioletSoft,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = if (isCurrent) "Active Now" else "${session.messageCount} messages",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isCurrent) TaskaPureWhite else TaskaViolet,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                            if (session.area.isNotBlank()) {
                                Surface(
                                    color = TaskaCream,
                                    shape = RoundedCornerShape(50),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, TaskaBorder)
                                ) {
                                    Text(
                                        text = session.area,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TaskaMutedText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        if (sessions.size > 1) {
                            Surface(
                                color = CoralDot.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { onDeleteSession(session.sessionId) }
                            ) {
                                Text(
                                    text = "Delete",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CoralDot,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TaskaInk
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = session.previewText,
                        style = MaterialTheme.typography.bodySmall,
                        color = TaskaMutedText,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tap to open conversation →",
                            style = MaterialTheme.typography.labelSmall,
                            color = TaskaViolet,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
