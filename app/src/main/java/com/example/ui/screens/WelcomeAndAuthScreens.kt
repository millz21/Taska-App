package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.UserAccountEntity
import com.example.ui.AuthScreenState
import com.example.ui.components.TaskaMixedHeadline
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.ui.theme.PlusJakartaSansFamily
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

private data class WelcomeSlideData(
    val badge: String,
    val boldTitle: String,
    val italicTitle: String,
    val description: String,
    val highlights: List<String>,
    val icon: ImageVector
)

@Composable
fun TaskaWelcomeScreen(
    onOpenAndExploreApp: () -> Unit,
    onGoToAuthChoice: () -> Unit,
    onGoToSignIn: () -> Unit,
    onGoToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slides = remember {
        listOf(
            WelcomeSlideData(
                badge = "WELCOME TO TASKA",
                boldTitle = "Type it. Taska it.\n",
                italicTitle = "Done.",
                description = "The chat-first marketplace for local services, campus repairs, printing, tutoring, and everyday tasks.",
                highlights = listOf(
                    "Ask Taska in plain language or talk with Live Voice",
                    "Snap a photo of any broken item or task for instant AI matching",
                    "See real local availability & price ranges in seconds"
                ),
                icon = Icons.AutoMirrored.Filled.Chat
            ),
            WelcomeSlideData(
                badge = "EXPLORE FIRST, ZERO PRESSURE",
                boldTitle = "See what's inside.\n",
                italicTitle = "No sign-in wall.",
                description = "You're never forced to sign in just to use Taska. Jump straight in, chat with Taska AI, talk to Live Voice, and browse verified Taskers freely.",
                highlights = listOf(
                    "Full access to Ask Taska AI & Live Voice as a guest",
                    "Browse all categories, ratings, and instant price estimates",
                    "Sign in only when you're ready to book or open private chats"
                ),
                icon = Icons.Default.Explore
            ),
            WelcomeSlideData(
                badge = "TRUST WITHOUT THE AWKWARDNESS",
                boldTitle = "Work with people.\n",
                italicTitle = "Not random numbers.",
                description = "When you sign up or sign in, you unlock Private Task Chats, 1-tap booking, and the ability to earn as a Tasker—without ever sharing your personal phone number.",
                highlights = listOf(
                    "100% private task chats—your phone number stays hidden",
                    "Verified provider badges & complete booking history",
                    "One account to both book help and earn as a Tasker"
                ),
                icon = Icons.Default.Lock
            )
        )
    }

    var currentSlideIndex by remember { mutableIntStateOf(0) }
    val currentSlide = slides[currentSlideIndex]

    Surface(
        color = TaskaCream,
        modifier = modifier
            .fillMaxSize()
            .testTag("welcome_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Brand Bar + Skip to App button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(TaskaViolet),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "T",
                            fontFamily = PlayfairDisplayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = TaskaWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Taska",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = TaskaInk
                        )
                        Text(
                            text = "Chat-First Marketplace",
                            style = MaterialTheme.typography.labelSmall,
                            color = TaskaMutedText
                        )
                    }
                }

                TextButton(
                    onClick = onOpenAndExploreApp,
                    modifier = Modifier.testTag("welcome_skip_button")
                ) {
                    Text(
                        text = "Skip to App →",
                        color = TaskaViolet,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Hero Banner Card
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = TaskaInk),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(195.dp)
                ) {
                    Image(
                        painter = painterResource(
                            id = if (currentSlideIndex == 2) {
                                R.drawable.img_tasker_banner_1790525810048
                            } else {
                                R.drawable.img_hero_campus_1790525798777
                            }
                        ),
                        contentDescription = "Taska Welcome Visual",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        TaskaInk.copy(alpha = 0.25f),
                                        TaskaInk.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    // Top badge inside hero
                    Surface(
                        color = TaskaLime,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = currentSlide.icon,
                                contentDescription = null,
                                tint = TaskaInk,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentSlide.badge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = TaskaInk
                            )
                        }
                    }

                    // Bottom floating preview pill inside hero
                    Surface(
                        color = TaskaWhite.copy(alpha = 0.94f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(TaskaVioletSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = TaskaViolet,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "\"Need my laptop screen fixed near Campus Hub\"",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TaskaInk
                                    )
                                    Text(
                                        text = "3 verified Taskers matched · Ready now",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = VerifiedGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Animated Onboarding Slide Content
            AnimatedContent(
                targetState = currentSlide,
                label = "welcome_slide_content"
            ) { slide ->
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = TaskaWhite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TaskaBorder, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        TaskaMixedHeadline(
                            boldPrefix = slide.boldTitle,
                            italicSuffix = slide.italicTitle,
                            fontSize = 30.sp,
                            lineHeight = 34.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = slide.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TaskaMutedText
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        slide.highlights.forEach { point ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = TaskaViolet,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = point,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = TaskaInk
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Slide Step Dots + Next Slide Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    slides.indices.forEach { idx ->
                        val selected = idx == currentSlideIndex
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (selected) 26.dp else 8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) TaskaViolet else TaskaBorder)
                                .clickable { currentSlideIndex = idx }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (currentSlideIndex > 0) {
                        TextButton(onClick = { currentSlideIndex-- }) {
                            Text("Back", color = TaskaMutedText)
                        }
                    }
                    if (currentSlideIndex < slides.lastIndex) {
                        TextButton(
                            onClick = { currentSlideIndex++ },
                            modifier = Modifier.testTag("welcome_next_slide_button")
                        ) {
                            Text("Next tip →", color = TaskaViolet, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary & Secondary CTAs
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onOpenAndExploreApp,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TaskaViolet,
                        contentColor = TaskaWhite
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("welcome_explore_button")
                ) {
                    Text(
                        text = "Open Taska & Explore Now",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                OutlinedButton(
                    onClick = onGoToAuthChoice,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.5.dp, TaskaInk),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TaskaInk),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("welcome_auth_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign In or Create Account",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account?",
                        style = MaterialTheme.typography.bodySmall,
                        color = TaskaMutedText
                    )
                    TextButton(
                        onClick = onGoToSignIn,
                        modifier = Modifier.testTag("welcome_direct_sign_in_button")
                    ) {
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TaskaViolet
                        )
                    }
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodySmall,
                        color = TaskaMutedText
                    )
                    TextButton(
                        onClick = onGoToSignUp,
                        modifier = Modifier.testTag("welcome_direct_sign_up_button")
                    ) {
                        Text(
                            text = "Sign Up",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = TaskaViolet
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskaAuthFlowScreen(
    authStep: AuthScreenState,
    promptReason: String?,
    errorMessage: String?,
    defaultArea: String,
    onSelectStep: (AuthScreenState) -> Unit,
    onSignIn: (email: String, password: String) -> Unit,
    onSignUp: (fullName: String, email: String, password: String, area: String, role: String, age: String, gender: String) -> Unit,
    onContinueExploringAsGuest: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        if (authStep == AuthScreenState.SIGN_IN || authStep == AuthScreenState.SIGN_UP) {
            onClearError()
            onSelectStep(AuthScreenState.CHOICE)
        } else {
            onContinueExploringAsGuest()
        }
    }

    Surface(
        color = TaskaCream,
        modifier = modifier
            .fillMaxSize()
            .testTag("auth_flow_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Top Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (authStep == AuthScreenState.SIGN_IN || authStep == AuthScreenState.SIGN_UP) {
                                onClearError()
                                onSelectStep(AuthScreenState.CHOICE)
                            } else {
                                onContinueExploringAsGuest()
                            }
                        },
                        modifier = Modifier.testTag("auth_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TaskaInk
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(TaskaViolet),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "T",
                            fontFamily = PlayfairDisplayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TaskaWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Taska Account",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        color = TaskaInk
                    )
                }

                TextButton(
                    onClick = onContinueExploringAsGuest,
                    modifier = Modifier.testTag("auth_top_continue_guest_button")
                ) {
                    Text(
                        text = "Explore as Guest",
                        color = TaskaViolet,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contextual Prompt Banner (when guest clicked Book Privately or Private Task Chat)
            if (!promptReason.isNullOrBlank()) {
                Surface(
                    color = TaskaVioletSoft,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, TaskaViolet.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .testTag("auth_prompt_reason_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(TaskaViolet),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = TaskaLime,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sign in required for this action",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = TaskaViolet
                            )
                            Text(
                                text = promptReason,
                                style = MaterialTheme.typography.bodySmall,
                                color = TaskaInk
                            )
                        }
                    }
                }
            }

            // Error Banner if any
            if (!errorMessage.isNullOrBlank()) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFE53935)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("auth_error_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB71C1C),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onClearError,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss error",
                                tint = Color(0xFFB71C1C),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            when (authStep) {
                AuthScreenState.CHOICE, AuthScreenState.NONE -> {
                    AuthChoiceContent(
                        onChooseSignIn = {
                            onClearError()
                            onSelectStep(AuthScreenState.SIGN_IN)
                        },
                        onChooseSignUp = {
                            onClearError()
                            onSelectStep(AuthScreenState.SIGN_UP)
                        },
                        onContinueAsGuest = onContinueExploringAsGuest
                    )
                }

                AuthScreenState.SIGN_IN -> {
                    SignInFormContent(
                        onSignInSubmit = onSignIn,
                        onSwitchToSignUp = {
                            onClearError()
                            onSelectStep(AuthScreenState.SIGN_UP)
                        },
                        onContinueAsGuest = onContinueExploringAsGuest
                    )
                }

                AuthScreenState.SIGN_UP -> {
                    SignUpFormContent(
                        defaultArea = defaultArea,
                        onSignUpSubmit = onSignUp,
                        onSwitchToSignIn = {
                            onClearError()
                            onSelectStep(AuthScreenState.SIGN_IN)
                        },
                        onContinueAsGuest = onContinueExploringAsGuest
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthChoiceContent(
    onChooseSignIn: () -> Unit,
    onChooseSignUp: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Brand Hero Card
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = TaskaViolet),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Surface(
                    color = TaskaLime,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "YOUR PRIVATE TASKA ACCOUNT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TaskaInk,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                TaskaMixedHeadline(
                    boldPrefix = "Sign in to book.\n",
                    italicSuffix = "Or explore freely first.",
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    textColor = TaskaWhite,
                    italicColor = TaskaLime
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Anyone can Ask Taska, use Live Voice, and browse local providers without an account. Sign in or sign up when you're ready to book a Tasker, open Private Task Chats, or list your skills.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TaskaWhite.copy(alpha = 0.9f)
                )
            }
        }

        // Option 1: Sign In Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = TaskaWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TaskaBorder, RoundedCornerShape(22.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(TaskaVioletSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TaskaViolet
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome back",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TaskaInk
                        )
                        Text(
                            text = "Sign in to access your private bookings, chat history, and Tasker profile.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onChooseSignIn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TaskaViolet,
                        contentColor = TaskaWhite
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_choice_sign_in_button")
                ) {
                    Text("Sign In to Taska", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Option 2: Sign Up Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = TaskaWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TaskaBorder, RoundedCornerShape(22.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(VerifiedGreenBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = VerifiedGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "New to Taska?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TaskaInk
                        )
                        Text(
                            text = "Create a free account in seconds to book verified Taskers privately or earn with your skills.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onChooseSignUp,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TaskaInk,
                        contentColor = TaskaWhite
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_choice_sign_up_button")
                ) {
                    Text("Create Free Account (Sign Up)", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Continue as Guest Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = TaskaWhite),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TaskaBorder, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(
                    onClick = onContinueAsGuest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_choice_continue_guest_button")
                ) {
                    Text(
                        text = "Not now — Continue exploring & chatting with Taska as Guest",
                        color = TaskaInk,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun SignInFormContent(
    onSignInSubmit: (email: String, password: String) -> Unit,
    onSwitchToSignUp: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = TaskaWhite),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TaskaBorder, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "SIGN IN TO TASKA",
                style = MaterialTheme.typography.labelMedium,
                color = TaskaViolet,
                fontWeight = FontWeight.Bold
            )
            TaskaMixedHeadline(
                boldPrefix = "Welcome back.\n",
                italicSuffix = "Your private chats await.",
                fontSize = 26.sp,
                lineHeight = 30.sp
            )
            Text(
                text = "Enter your email and password to unlock private bookings, task chats, and your Tasker profile.",
                style = MaterialTheme.typography.bodySmall,
                color = TaskaMutedText
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email address") },
                placeholder = { Text("you@campus.edu") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TaskaViolet,
                    unfocusedBorderColor = TaskaBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sign_in_email_input")
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                placeholder = { Text("Enter your password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TaskaViolet,
                    unfocusedBorderColor = TaskaBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sign_in_password_input")
            )

            Button(
                onClick = { onSignInSubmit(email, password) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaskaViolet,
                    contentColor = TaskaWhite
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("sign_in_submit_button")
            ) {
                Text("Sign In & Enter Taska", fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account yet?",
                    style = MaterialTheme.typography.bodySmall,
                    color = TaskaMutedText
                )
                TextButton(
                    onClick = onSwitchToSignUp,
                    modifier = Modifier.testTag("switch_to_sign_up_button")
                ) {
                    Text(
                        text = "Sign Up Free",
                        color = TaskaViolet,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = TaskaBorder)

            TextButton(
                onClick = onContinueAsGuest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Continue using Taska without signing in",
                    color = TaskaMutedText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SignUpFormContent(
    defaultArea: String,
    onSignUpSubmit: (fullName: String, email: String, password: String, area: String, role: String, age: String, gender: String) -> Unit,
    onSwitchToSignIn: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Client & Tasker") }

    val genderOptions = listOf("Female", "Male", "Non-binary", "Prefer not to say")
    val roles = listOf(
        "Book Help & Explore" to "Client",
        "Both Book Help & Earn as Tasker" to "Client & Tasker"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = TaskaWhite),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TaskaBorder, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "CREATE YOUR TASKA ACCOUNT",
                style = MaterialTheme.typography.labelMedium,
                color = TaskaViolet,
                fontWeight = FontWeight.Bold
            )
            TaskaMixedHeadline(
                boldPrefix = "Join Taska.\n",
                italicSuffix = "Private by design.",
                fontSize = 26.sp,
                lineHeight = 30.sp
            )
            Text(
                text = "Your personal phone number is never shared. After creating your account, you can set your custom location pin or use GPS.",
                style = MaterialTheme.typography.bodySmall,
                color = TaskaMutedText
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                placeholder = { Text("e.g., Jordan Taylor") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TaskaViolet,
                    unfocusedBorderColor = TaskaBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sign_up_name_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it.filter { ch -> ch.isDigit() }.take(3) },
                    label = { Text("Age (optional)") },
                    placeholder = { Text("e.g. 23") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TaskaViolet,
                        unfocusedBorderColor = TaskaBorder
                    ),
                    modifier = Modifier
                        .weight(0.42f)
                        .testTag("sign_up_age_input")
                )
                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("Gender (optional)") },
                    placeholder = { Text("Select below") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TaskaViolet,
                        unfocusedBorderColor = TaskaBorder
                    ),
                    modifier = Modifier
                        .weight(0.58f)
                        .testTag("sign_up_gender_input")
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                genderOptions.forEach { option ->
                    FilterChip(
                        selected = gender.equals(option, ignoreCase = true),
                        onClick = { gender = option },
                        label = { Text(option, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TaskaVioletSoft,
                            selectedLabelColor = TaskaViolet
                        )
                    )
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email address") },
                placeholder = { Text("jordan@example.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TaskaViolet,
                    unfocusedBorderColor = TaskaBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sign_up_email_input")
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Create Password (min 6 chars)") },
                placeholder = { Text("At least 6 characters") },
                singleLine = true,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TaskaViolet,
                    unfocusedBorderColor = TaskaBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sign_up_password_input")
            )

            // Member Role Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "How do you want to use Taska?",
                    style = MaterialTheme.typography.labelMedium,
                    color = TaskaInk,
                    fontWeight = FontWeight.Bold
                )
                roles.forEach { (label, roleVal) ->
                    val isSelected = selectedRole == roleVal
                    Surface(
                        color = if (isSelected) TaskaVioletSoft else TaskaCream,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) TaskaViolet else TaskaBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedRole = roleVal }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isSelected) TaskaViolet else TaskaMutedText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = TaskaInk
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    onSignUpSubmit(fullName, email, password, "", selectedRole, age, gender)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaskaInk,
                    contentColor = TaskaWhite
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("sign_up_submit_button")
            ) {
                Text("Create Account & Enter Taska", fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.bodySmall,
                    color = TaskaMutedText
                )
                TextButton(
                    onClick = onSwitchToSignIn,
                    modifier = Modifier.testTag("switch_to_sign_in_button")
                ) {
                    Text(
                        text = "Sign In",
                        color = TaskaViolet,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = TaskaBorder)

            TextButton(
                onClick = onContinueAsGuest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Continue using Taska without signing in",
                    color = TaskaMutedText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAccountBottomSheet(
    currentUser: UserAccountEntity?,
    onOpenSignIn: () -> Unit,
    onOpenSignUp: () -> Unit,
    onSignOut: () -> Unit,
    onReplayWelcomeScreen: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TaskaCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (currentUser != null) {
                // Signed-In User Sheet
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(TaskaViolet),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser.avatarInitials,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TaskaWhite
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentUser.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TaskaInk
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (currentUser.verificationStatus == "verified") VerifiedGreenBg else TaskaVioletSoft,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = when (currentUser.verificationStatus) {
                                            "verified" -> "✓ Verified"
                                            "pending_review" -> "Pending Review"
                                            else -> "Taska Member"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (currentUser.verificationStatus == "verified") VerifiedGreen else TaskaViolet,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${currentUser.email} · ${currentUser.defaultArea}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TaskaMutedText
                            )
                        }
                    }
                }

                Surface(
                    color = TaskaWhite,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, TaskaBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Unlocked with your Taska Account",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TaskaViolet
                        )
                        Text(
                            text = "• 1-Tap Private Bookings & Private Task Chats\n• Phone number hidden by default\n• Publish Tasker profiles & send AI proposals",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaInk
                        )
                    }
                }

                OutlinedButton(
                    onClick = onReplayWelcomeScreen,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_sheet_replay_welcome_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Welcome Screen Tour")
                }

                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TaskaInk,
                        contentColor = TaskaWhite
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_sheet_sign_out_button")
                ) {
                    Text("Sign Out (Switch to Guest Mode)")
                }
            } else {
                // Guest Mode Sheet
                Surface(
                    color = TaskaVioletSoft,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "BROWSING IN GUEST MODE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TaskaViolet,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                TaskaMixedHeadline(
                    boldPrefix = "Explore freely.\n",
                    italicSuffix = "Sign in to book & chat privately.",
                    fontSize = 24.sp,
                    lineHeight = 28.sp
                )

                Text(
                    text = "You can use Ask Taska, talk to Live Voice, and browse local providers without signing in. Sign in or sign up to book Taskers, open Private Task Chats, and create a Tasker profile.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TaskaMutedText
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenSignIn,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TaskaViolet,
                            contentColor = TaskaWhite
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("account_sheet_sign_in_button")
                    ) {
                        Text("Sign In", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onOpenSignUp,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TaskaInk,
                            contentColor = TaskaWhite
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("account_sheet_sign_up_button")
                    ) {
                        Text("Sign Up", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onReplayWelcomeScreen,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_sheet_replay_welcome_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Replay First-Time Welcome Screen")
                }
            }
        }
    }
}
