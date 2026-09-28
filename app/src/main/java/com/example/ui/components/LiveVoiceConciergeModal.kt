package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.TaskaGeminiModels
import com.example.data.TaskerEntity
import com.example.ui.LiveVoiceTurn
import com.example.ui.theme.CoralDot
import com.example.ui.theme.TaskaInk
import com.example.ui.theme.TaskaLime
import com.example.ui.theme.TaskaMutedText
import com.example.ui.theme.TaskaViolet
import com.example.ui.theme.TaskaVioletSoft
import com.example.ui.theme.TaskaWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveVoiceConciergeModal(
    selectedArea: String,
    liveVoiceHistory: List<LiveVoiceTurn>,
    liveMatchedTaskers: List<TaskerEntity>,
    isRecordingAudio: Boolean,
    isAiLoading: Boolean,
    onStartRecording: () -> Unit,
    onStopAndSendRecording: () -> Unit,
    onSendSpokenPrompt: (String) -> Unit,
    onSpeakText: (String) -> Unit,
    onBookTasker: (TaskerEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        onStartRecording()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isRecordingAudio || isAiLoading) 1.18f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(750),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF14141F)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = TaskaLime,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color(0xFF14141F),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Taska Live Voice",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF14141F),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Live Voice",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (selectedArea.isBlank()) "Talk to Taska" else "Talk to Taska in $selectedArea",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Text(
                text = "Describe what you need aloud and get matched with verified local Taskers.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Voice Orb & Mic Control
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            if (isRecordingAudio) CoralDot.copy(alpha = 0.35f)
                            else TaskaViolet.copy(alpha = 0.35f)
                        )
                )
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(CircleShape)
                        .background(if (isRecordingAudio) CoralDot else TaskaViolet)
                        .clickable {
                            if (isRecordingAudio) {
                                onStopAndSendRecording()
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                        .testTag("live_voice_mic_orb"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecordingAudio) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecordingAudio) "Stop & Send Voice" else "Tap to Speak",
                        tint = TaskaWhite,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Text(
                text = when {
                    isRecordingAudio -> "Listening… Tap orb to finish speaking"
                    isAiLoading -> "Taska Live is responding…"
                    else -> "Tap microphone orb to talk or try a voice prompt below"
                },
                style = MaterialTheme.typography.labelLarge,
                fontSize = 12.sp,
                color = TaskaLime
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Voice Prompts for Instant Testing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val quickVoicePrompts = listOf(
                    "Fix my laptop port near campus",
                    "Need color printing by 4 PM",
                    "Find a math tutor tonight",
                    "Help moving 3 boxes"
                )
                quickVoicePrompts.forEach { prompt ->
                    Surface(
                        color = TaskaWhite.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onSendSpokenPrompt(prompt) }
                    ) {
                        Text(
                            text = "🎙️ “$prompt”",
                            style = MaterialTheme.typography.labelSmall,
                            color = TaskaWhite,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Conversation Transcript
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(liveVoiceHistory) { turn ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (turn.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            color = if (turn.isUser) TaskaViolet else TaskaVioletSoft,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(0.88f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = turn.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (turn.isUser) TaskaWhite else TaskaInk,
                                    modifier = Modifier.weight(1f)
                                )
                                if (!turn.isUser) {
                                    IconButton(
                                        onClick = { onSpeakText(turn.text) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Replay voice response",
                                            tint = TaskaViolet,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (liveMatchedTaskers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Matched Local Taskers:",
                    style = MaterialTheme.typography.labelLarge,
                    color = TaskaLime,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                val topMatch = liveMatchedTaskers.first()
                TaskerMatchCard(
                    tasker = topMatch,
                    onBookPrivately = {
                        onBookTasker(it)
                        onDismiss()
                    },
                    compact = true
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
