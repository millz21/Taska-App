package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MarketplaceRequestEntity
import com.example.data.SavedLocationEntity
import com.example.data.TaskaCategory
import com.example.data.TaskerRoleType
import com.example.data.UserAccountEntity
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.RealWorldLocationAndMapHelper
import com.example.ui.components.TaskLocationPin
import com.example.ui.components.TaskLocationsGoogleMapCard
import com.example.ui.components.TaskaMixedHeadline
import com.example.ui.theme.CategoryLavender
import com.example.ui.theme.CategoryLavenderIcon
import com.example.ui.theme.CategoryMint
import com.example.ui.theme.CategoryMintIcon
import com.example.ui.theme.CategoryPeach
import com.example.ui.theme.CategoryPeachIcon
import com.example.ui.theme.CategorySky
import com.example.ui.theme.CategorySkyIcon
import com.example.ui.theme.TaskaBorder
import com.example.ui.theme.TaskaCream
import com.example.ui.theme.TaskaDeepInk
import com.example.ui.theme.TaskaInk
import com.example.ui.theme.TaskaMutedText
import com.example.ui.theme.TaskaSurfaceAlt
import com.example.ui.theme.TaskaViolet
import com.example.ui.theme.TaskaVioletSoft
import com.example.ui.theme.TaskaWhite
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenBg

@Composable
fun TaskerModeScreen(
    selectedArea: String,
    marketplaceRequests: List<MarketplaceRequestEntity>,
    isAiLoading: Boolean,
    currentUser: UserAccountEntity? = null,
    isDarkMode: Boolean = false,
    savedLocations: List<SavedLocationEntity> = emptyList(),
    myListingsCount: Int = 0,
    showEarningProfileScreen: Boolean = false,
    showSettingsScreen: Boolean = false,
    showCustomPinMapScreen: Boolean = false,
    isDetectingGps: Boolean = false,
    onSetShowEarningProfileScreen: (Boolean) -> Unit = {},
    onSetShowSettingsScreen: (Boolean) -> Unit = {},
    onSetShowCustomPinMapScreen: (Boolean) -> Unit = {},
    onOpenConversationHistoryScreen: () -> Unit = {},
    onToggleDarkMode: (Boolean) -> Unit = {},
    onOpenSignInOrSignUp: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onUpdateFullProfile: (fullName: String, age: String, gender: String, bio: String, defaultArea: String) -> Unit = { _, _, _, _, _ -> },
    onUseCurrentGpsLocation: () -> Unit = {},
    onSelectSavedLocation: (SavedLocationEntity) -> Unit = {},
    onSaveCustomPinLocation: (label: String, addressName: String, lat: Double, lng: Double) -> Unit = { _, _, _, _ -> },
    onDeleteSavedLocation: (Int) -> Unit = {},
    onReplayWelcome: () -> Unit = {},
    onClearChatHistory: () -> Unit = {},
    onSaveFullEarningProfile: (
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
        storePhotoUris: List<String>,
        firstListingPhotoUris: List<String>
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onAddServiceListing: (
        title: String,
        categoryName: String,
        description: String,
        priceZmw: String,
        photoUris: List<String>
    ) -> Unit = { _, _, _, _, _ -> },
    onSubmitCreditTopUp: (
        creditsRequested: Int,
        senderName: String,
        senderPhone: String,
        reference: String
    ) -> Unit = { _, _, _, _ -> },
    onAcceptRequestWithPrice: (MarketplaceRequestEntity, String, String) -> Unit = { _, _, _ -> },
    onDeclineRequest: (MarketplaceRequestEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedRole by remember { mutableStateOf(TaskerRoleType.SERVICE_TASKER) }
    var showAddListingSheet by remember { mutableStateOf(false) }
    var showCreditsTopUpSheet by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var requestForAcceptDialog by remember { mutableStateOf<MarketplaceRequestEntity?>(null) }

    if (showEditProfileDialog && currentUser != null) {
        EditFullProfileDialog(
            currentUser = currentUser,
            onDismiss = { showEditProfileDialog = false },
            onSaveProfile = { fullName, age, gender, bio, area ->
                onUpdateFullProfile(fullName, age, gender, bio, area)
            }
        )
    }

    if (showAddListingSheet) {
        AddServiceListingSheet(
            onDismiss = { showAddListingSheet = false },
            onSaveListing = { title, cat, desc, price, photos ->
                onAddServiceListing(title, cat, desc, price, photos)
                showAddListingSheet = false
            }
        )
    }

    if (showCreditsTopUpSheet) {
        TaskaCreditsTopUpSheet(
            currentBalance = currentUser?.taskaCredits ?: 0,
            onDismiss = { showCreditsTopUpSheet = false },
            onSubmitTopUp = { credits, sender, phone, ref ->
                onSubmitCreditTopUp(credits, sender, phone, ref)
                showCreditsTopUpSheet = false
            }
        )
    }

    // Dedicated full-screen routes inside Profile & Earn tab
    if (showCustomPinMapScreen) {
        CustomLocationPinMapScreen(
            selectedArea = selectedArea,
            savedLocations = savedLocations,
            isDetectingGps = isDetectingGps,
            onBack = { onSetShowCustomPinMapScreen(false) },
            onUseCurrentGpsLocation = onUseCurrentGpsLocation,
            onSelectSavedLocation = { loc ->
                onSelectSavedLocation(loc)
                onSetShowCustomPinMapScreen(false)
            },
            onSaveCustomPinLocation = onSaveCustomPinLocation,
            onDeleteSavedLocation = onDeleteSavedLocation
        )
        return
    }

    if (showSettingsScreen) {
        SettingsFullScreen(
            isDarkMode = isDarkMode,
            selectedArea = selectedArea,
            currentUser = currentUser,
            onBack = { onSetShowSettingsScreen(false) },
            onToggleDarkMode = onToggleDarkMode,
            onOpenLocationSetup = {
                onSetShowSettingsScreen(false)
                onSetShowCustomPinMapScreen(true)
            },
            onOpenEditProfile = { showEditProfileDialog = true },
            onOpenConversationHistory = {
                onSetShowSettingsScreen(false)
                onOpenConversationHistoryScreen()
            },
            onClearChatHistory = onClearChatHistory,
            onReplayWelcome = onReplayWelcome,
            onSignOut = {
                onSetShowSettingsScreen(false)
                onSignOut()
            }
        )
        return
    }

    if (showEarningProfileScreen) {
        EarnWithTaskaFullScreen(
            initialRole = selectedRole,
            defaultArea = selectedArea,
            currentUser = currentUser,
            myListingsCount = myListingsCount,
            onBack = { onSetShowEarningProfileScreen(false) },
            onOpenAddListing = { showAddListingSheet = true },
            onOpenTopUpCredits = { showCreditsTopUpSheet = true },
            onSaveEarningProfile = { role, cat, pubName, phone, bio, srvTitle, srvPrice, area, bizName, bizReg, legalName, nrc, hasDoc, hasSelfie, storePhotos, listingPhotos ->
                onSaveFullEarningProfile(
                    role,
                    cat,
                    pubName,
                    phone,
                    bio,
                    srvTitle,
                    srvPrice,
                    area,
                    bizName,
                    bizReg,
                    legalName,
                    nrc,
                    hasDoc,
                    hasSelfie,
                    storePhotos,
                    listingPhotos
                )
            }
        )
        return
    }

    requestForAcceptDialog?.let { req ->
        var proposedPrice by remember { mutableStateOf(req.budget.filter { it.isDigit() }) }
        AlertDialog(
            onDismissRequest = { requestForAcceptDialog = null },
            title = { Text("Accept client invitation", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Client budget: ${req.budget}${if (req.budgetNegotiable) " · Negotiable" else ""}",
                        style = MaterialTheme.typography.labelLarge,
                        color = TaskaViolet,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = proposedPrice,
                        onValueChange = { proposedPrice = it },
                        label = { Text("Your proposed price in ZMW (optional)") },
                        placeholder = { Text("Leave blank to discuss in private chat") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAcceptRequestWithPrice(req, proposedPrice, selectedRole.title)
                        requestForAcceptDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TaskaViolet)
                ) {
                    Text("Accept & Send Pitch")
                }
            },
            dismissButton = {
                TextButton(onClick = { requestForAcceptDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TaskaCream),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header with Dedicated Settings Button & Location Pill
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profile & Earn",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TaskaInk
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = TaskaSurfaceAlt,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, TaskaBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { onSetShowCustomPinMapScreen(true) }
                                .testTag("profile_location_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Manage locations",
                                    tint = TaskaViolet,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = selectedArea.ifBlank { "Set Location" },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TaskaInk
                                )
                            }
                        }

                        IconButton(
                            onClick = { onSetShowSettingsScreen(true) },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(TaskaSurfaceAlt)
                                .border(1.dp, TaskaBorder, CircleShape)
                                .testTag("open_settings_screen_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = TaskaInk
                            )
                        }
                    }
                }

                // Account & Profile Card
                if (currentUser == null) {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 22.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
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
                                Column {
                                    Text(
                                        text = "Use Taska your way",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TaskaInk
                                    )
                                    Text(
                                        text = "Sign in to book, save custom locations, chat privately, and earn.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TaskaMutedText
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onOpenSignInOrSignUp,
                                colors = ButtonDefaults.buttonColors(containerColor = TaskaViolet),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.testTag("profile_sign_in_button")
                            ) {
                                Text("Sign in", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEditProfileDialog = true },
                        cornerRadius = 22.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(TaskaVioletSoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = currentUser.avatarInitials,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TaskaViolet
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = currentUser.fullName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
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
                                                        else -> "Member"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (currentUser.verificationStatus == "verified") VerifiedGreen else TaskaViolet,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = buildString {
                                                append(currentUser.email)
                                                if (currentUser.age.isNotBlank()) append(" · ${currentUser.age} yrs")
                                                if (currentUser.gender.isNotBlank()) append(" · ${currentUser.gender}")
                                                if (currentUser.defaultArea.isNotBlank()) append(" · ${currentUser.defaultArea}")
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TaskaMutedText
                                        )
                                        if (currentUser.bio.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = currentUser.bio,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TaskaInk
                                            )
                                        }
                                    }
                                }
                                IconButton(
                                    onClick = { showEditProfileDialog = true },
                                    modifier = Modifier.testTag("edit_full_profile_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit profile",
                                        tint = TaskaViolet,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Quick Hub Card: Taska Credits (gated by Earning Profile), Saved Locations & Conversation History
        item {
            val hasEarningSetup = currentUser?.hasEarningProfile == true
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Taska Credits Row — requires an earning profile setup first!
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                when {
                                    currentUser == null -> onOpenSignInOrSignUp()
                                    !hasEarningSetup -> onSetShowEarningProfileScreen(true)
                                    else -> showCreditsTopUpSheet = true
                                }
                            }
                            .padding(vertical = 8.dp)
                            .testTag("taska_credits_row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Toll,
                                contentDescription = null,
                                tint = TaskaViolet
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (hasEarningSetup) {
                                        "Taska Credits (${currentUser?.taskaCredits ?: 0} available)"
                                    } else {
                                        "Taska Credits (Earning Profile Required)"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TaskaInk
                                )
                                Text(
                                    text = if (hasEarningSetup) {
                                        "Top up credits to unlock new client job invitations (20 credits = US$1)."
                                    } else {
                                        "Set up your 'Earn with Taska' profile first to unlock Taska Credits and accept jobs."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TaskaMutedText
                                )
                            }
                        }
                        Surface(
                            color = if (hasEarningSetup) TaskaVioletSoft else TaskaSurfaceAlt,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = if (hasEarningSetup) "Top up →" else "Set up to unlock →",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TaskaViolet,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = TaskaBorder, modifier = Modifier.padding(vertical = 6.dp))

                    // Conversation History Row — clicking the row opens the full ConversationHistoryScreen, clicking Clear clears active chat
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenConversationHistoryScreen() }
                            .padding(vertical = 8.dp)
                            .testTag("profile_conversation_history_row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = TaskaViolet
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Conversation history",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TaskaInk
                                )
                                Text(
                                    text = "Tap to open and resume your past Ask Taska conversations",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TaskaMutedText
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = onClearChatHistory) {
                                Text("Clear", color = TaskaViolet, fontWeight = FontWeight.Bold)
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open conversation history",
                                tint = TaskaViolet,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Hero Card: "Earn with Taska" -> Opens Dedicated Full Screen
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = TaskaWhite),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TaskaBorder, RoundedCornerShape(26.dp))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(148.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_tasker_banner_1790525810048),
                            contentDescription = "Become a Tasker on Taska",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, TaskaDeepInk.copy(alpha = 0.76f))
                                    )
                                )
                        )
                        Surface(
                            color = TaskaViolet,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "EARN WITH TASKA",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(18.dp)) {
                        TaskaMixedHeadline(
                            boldPrefix = "Turn your skills\n",
                            italicSuffix = "into opportunities.",
                            fontSize = 26.sp,
                            lineHeight = 30.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Set up your earning profile, showcase up to 6 photos per listing or store, and receive private client bookings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        ChecklistBenefitRow("Offer services, sell products, digital work, or register a store")
                        ChecklistBenefitRow("Upload up to 6 photos per listing & storefront gallery")
                        ChecklistBenefitRow("Unlock Taska Credits and verified trust badges")

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (currentUser == null) {
                                        onOpenSignInOrSignUp()
                                    } else {
                                        onSetShowEarningProfileScreen(true)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TaskaViolet,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("get_started_tasker_button")
                            ) {
                                Text(
                                    text = if (currentUser?.hasEarningProfile == true) "Manage Earning Profile" else "Earn with Taska",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    when {
                                        currentUser == null -> onOpenSignInOrSignUp()
                                        !currentUser.hasEarningProfile -> onSetShowEarningProfileScreen(true)
                                        else -> showAddListingSheet = true
                                    }
                                },
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, TaskaInk),
                                modifier = Modifier
                                    .weight(0.9f)
                                    .testTag("add_service_listing_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Listing", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 4. "Ways to earn on Taska" Role Selector Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "YOUR SKILLS HAVE VALUE",
                    style = MaterialTheme.typography.labelMedium,
                    color = TaskaViolet
                )
                Text(
                    text = "Four ways to earn on Taska",
                    style = MaterialTheme.typography.headlineLarge,
                    color = TaskaInk
                )

                TaskerRoleType.entries.forEach { role ->
                    val isSelected = selectedRole == role
                    val (icon, bg, fg) = when (role) {
                        TaskerRoleType.SERVICE_TASKER -> Triple(Icons.Default.Build, CategoryLavender, CategoryLavenderIcon)
                        TaskerRoleType.TASKA_SELLER -> Triple(Icons.Default.Storefront, CategoryMint, CategoryMintIcon)
                        TaskerRoleType.DIGITAL_TASKER -> Triple(Icons.Default.Code, CategorySky, CategorySkyIcon)
                        TaskerRoleType.REGISTER_BUSINESS -> Triple(Icons.Default.Business, CategoryPeach, CategoryPeachIcon)
                    }

                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedRole = role
                                if (currentUser == null) {
                                    onOpenSignInOrSignUp()
                                } else {
                                    onSetShowEarningProfileScreen(true)
                                }
                            }
                            .testTag("role_card_${role.id}"),
                        cornerRadius = 20.dp,
                        borderColor = if (isSelected) TaskaViolet else TaskaBorder
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(bg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = role.title,
                                    tint = fg,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${role.title} (${role.formLabel})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TaskaInk
                                )
                                Text(
                                    text = role.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TaskaMutedText
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = TaskaViolet,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Work Invitations & Available Requests
        item {
            Column {
                Text(
                    text = "WORK INVITATIONS & OPEN REQUESTS",
                    style = MaterialTheme.typography.labelMedium,
                    color = TaskaViolet
                )
                Text(
                    text = "Accept invitations or propose your price",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TaskaInk
                )
            }
        }

        item {
            val requestPins = remember(marketplaceRequests, savedLocations, selectedArea) {
                marketplaceRequests.mapIndexed { index, req ->
                    val (lat, lng) = RealWorldLocationAndMapHelper.resolveTaskAreaCoordinates(
                        areaName = req.area.ifBlank { selectedArea },
                        savedLocations = savedLocations,
                        indexOffset = index
                    )
                    TaskLocationPin(
                        id = "req_${req.id}",
                        title = req.title,
                        subtitle = req.details,
                        areaName = req.area.ifBlank { selectedArea.ifBlank { "Lusaka" } },
                        badgeText = req.budget,
                        latitude = lat,
                        longitude = lng,
                        isCompleted = req.status.startsWith("Accepted", ignoreCase = true)
                    )
                }
            }
            TaskLocationsGoogleMapCard(
                sectionTitle = "Open Task Locations on Google Maps",
                sectionSubtitle = "Explore client work invitations pinned around ${selectedArea.ifBlank { "Lusaka" }}",
                selectedArea = selectedArea,
                taskPins = requestPins,
                savedLocations = savedLocations,
                onSelectTaskPin = { pin ->
                    val reqId = pin.id.removePrefix("req_").toIntOrNull()
                    val found = marketplaceRequests.firstOrNull { it.id == reqId }
                    if (found != null && found.status == "Open") {
                        requestForAcceptDialog = found
                    } else {
                        onSetShowCustomPinMapScreen(true)
                    }
                },
                onOpenFullLocationMap = { onSetShowCustomPinMapScreen(true) }
            )
        }

        if (marketplaceRequests.isEmpty()) {
            item {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No open work invitations in ${selectedArea.ifBlank { "your area" }} yet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TaskaInk
                        )
                        Text(
                            text = "New client service requests in your area appear here automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText
                        )
                    }
                }
            }
        } else {
            items(marketplaceRequests, key = { it.id }) { req ->
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = req.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TaskaInk,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                color = if (req.status.startsWith("Accepted")) VerifiedGreenBg else TaskaVioletSoft,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = req.status.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (req.status.startsWith("Accepted")) VerifiedGreen else TaskaViolet,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = req.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Client budget: ${req.budget}${if (req.budgetNegotiable) " · Negotiable" else ""} · ${req.area}",
                            style = MaterialTheme.typography.labelLarge,
                            color = TaskaViolet,
                            fontWeight = FontWeight.Bold
                        )

                        if (req.aiProposalText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = TaskaVioletSoft,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Proposal Sent: \"${req.aiProposalText}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TaskaInk,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        if (req.status == "Open") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { onDeclineRequest(req) }) {
                                    Text("Decline", color = TaskaMutedText)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { requestForAcceptDialog = req },
                                    enabled = !isAiLoading,
                                    colors = ButtonDefaults.buttonColors(containerColor = TaskaViolet),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.testTag("accept_request_${req.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Accept & Propose Price")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistBenefitRow(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(VerifiedGreenBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = VerifiedGreen,
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = TaskaInk,
            fontWeight = FontWeight.Medium
        )
    }
}
