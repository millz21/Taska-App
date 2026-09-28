package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.InboxNotificationEntity
import com.example.data.SavedLocationEntity
import com.example.data.TaskaCategory
import com.example.data.TaskerEntity
import com.example.data.TaskerReviewEntity
import com.example.data.UserAccountEntity
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.RealWorldLocationAndMapHelper
import com.example.ui.components.TaskaMixedHeadline
import com.example.ui.components.TaskaRatingBreakdownSummary
import com.example.ui.theme.StarAmber
import com.example.ui.theme.TaskaBorder
import com.example.ui.theme.TaskaCream
import com.example.ui.theme.TaskaDeepInk
import com.example.ui.theme.TaskaInk
import com.example.ui.theme.TaskaLime
import com.example.ui.theme.TaskaMutedText
import com.example.ui.theme.TaskaSurfaceAlt
import com.example.ui.theme.TaskaViolet
import com.example.ui.theme.TaskaVioletSoft
import com.example.ui.theme.TaskaWhite
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenBg

@Composable
fun TaskaInboxScreen(
    notifications: List<InboxNotificationEntity>,
    currentUser: UserAccountEntity?,
    onSignInClick: () -> Unit,
    onOpenRequestDemand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleNotifications = remember(notifications, currentUser) {
        if (currentUser != null) {
            notifications
        } else {
            notifications.filter { !it.isPrivateForSignedInUser }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TaskaCream),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Inbox",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TaskaInk
                    )
                    Surface(
                        color = VerifiedGreenBg,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = VerifiedGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Private & Verified",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = VerifiedGreen
                            )
                        }
                    }
                }
                Text(
                    text = if (currentUser == null) {
                        "Sign in for private booking updates. Platform announcements are visible below."
                    } else {
                        "Booking updates, Tasker offer alerts, and Taska Hub announcements."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TaskaMutedText
                )
            }
        }

        if (currentUser == null) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = TaskaViolet),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "PRIVATE UPDATES & OFFERS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TaskaLime,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        TaskaMixedHeadline(
                            boldPrefix = "Stay updated.\n",
                            italicSuffix = "Privately in Taska.",
                            fontSize = 24.sp,
                            lineHeight = 28.sp,
                            textColor = Color.White,
                            italicColor = TaskaLime
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sign in to receive private Tasker offer alerts, completion PIN notifications, and Taska Credit updates.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onSignInClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TaskaLime,
                                contentColor = TaskaDeepInk
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.testTag("inbox_sign_in_button")
                        ) {
                            Text("Sign in for private updates", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (visibleNotifications.isEmpty()) {
            item {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = TaskaViolet,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "No notifications or announcements yet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TaskaInk
                        )
                        Text(
                            text = "Official platform announcements from Taska Hub, Tasker offers, and private booking updates will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText
                        )
                    }
                }
            }
        }

        items(visibleNotifications, key = { it.id }) { item ->
            val icon = when (item.category) {
                "booking_update" -> Icons.Default.Lock
                "credits" -> Icons.Default.Toll
                "offer" -> Icons.Default.NotificationsActive
                else -> Icons.Default.Campaign
            }
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (item.isPrivateForSignedInUser) TaskaVioletSoft else VerifiedGreenBg
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (item.isPrivateForSignedInUser) TaskaViolet else VerifiedGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TaskaInk,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.timeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = TaskaMutedText
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.body,
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText
                        )
                    }
                }
            }
        }

        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Missing a service in your area?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TaskaInk
                        )
                        Text(
                            text = "Tell Taska Hub what you need so we can invite verified local providers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedButton(
                        onClick = onOpenRequestDemand,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, TaskaViolet)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            tint = TaskaViolet,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Request", color = TaskaViolet, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationAndRadiusPickerSheet(
    currentArea: String,
    currentRadiusKm: String,
    savedLocations: List<SavedLocationEntity> = emptyList(),
    onUseCurrentGpsLocation: () -> Unit = {},
    onOpenCustomPinMap: () -> Unit = {},
    onSaveAreaAndRadius: (area: String, radiusKm: String) -> Unit,
    onDismiss: () -> Unit
) {
    var areaInput by remember(currentArea) { mutableStateOf(currentArea) }
    var radiusInput by remember(currentRadiusKm) { mutableStateOf(currentRadiusKm) }
    val context = LocalContext.current
    val primaryLocation = remember(savedLocations, areaInput) {
        savedLocations.firstOrNull {
            it.areaName.equals(areaInput, ignoreCase = true) ||
                it.label.equals(areaInput, ignoreCase = true)
        } ?: savedLocations.firstOrNull { it.isPrimary } ?: savedLocations.firstOrNull()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TaskaCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Your search area & real-world map",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TaskaInk
            )
            Text(
                text = "Detect your live GPS coordinates, drop a pin on the interactive real-world street map, or open in Google Maps.",
                style = MaterialTheme.typography.bodySmall,
                color = TaskaMutedText
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onUseCurrentGpsLocation()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, TaskaViolet),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("use_current_location_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = TaskaViolet,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Use current GPS",
                        color = TaskaViolet,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenCustomPinMap()
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, TaskaViolet),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_custom_pin_map_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = TaskaViolet,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Interactive Map",
                        color = TaskaViolet,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    RealWorldLocationAndMapHelper.openInExternalRealWorldMap(
                        context = context,
                        latitude = primaryLocation?.latitude ?: -15.3875,
                        longitude = primaryLocation?.longitude ?: 28.3228,
                        label = areaInput.ifBlank { currentArea.ifBlank { "Taska Search Area" } }
                    )
                },
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, TaskaBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sheet_open_google_maps_button")
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = TaskaViolet,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Open \"${areaInput.ifBlank { currentArea }}\" in Real-World Maps App",
                    color = TaskaInk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }

            if (savedLocations.isNotEmpty()) {
                Text(
                    text = "Your Saved Locations",
                    style = MaterialTheme.typography.labelMedium,
                    color = TaskaViolet,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    savedLocations.forEach { loc ->
                        FilterChip(
                            selected = areaInput.equals(loc.areaName, ignoreCase = true) ||
                                areaInput.equals(loc.label, ignoreCase = true),
                            onClick = { areaInput = loc.areaName },
                            label = { Text("${loc.label} (${loc.areaName})", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TaskaVioletSoft,
                                selectedLabelColor = TaskaViolet
                            )
                        )
                    }
                }
            }

            OutlinedTextField(
                value = areaInput,
                onValueChange = { areaInput = it },
                label = { Text("Area, neighbourhood or city") },
                placeholder = { Text("e.g. Woodlands, Kabulonga, Roma") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("area_input_field")
            )

            OutlinedTextField(
                value = radiusInput,
                onValueChange = { radiusInput = it.filter { ch -> ch.isDigit() } },
                label = { Text("Search radius in km") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("radius_input_field")
            )

            Button(
                onClick = { onSaveAreaAndRadius(areaInput, radiusInput) },
                enabled = areaInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaskaViolet,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_search_area_button")
            ) {
                Text("Save search area", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestMissingServiceDemandSheet(
    defaultArea: String,
    onSubmitDemand: (requestText: String, area: String) -> Unit,
    onDismiss: () -> Unit
) {
    var requestText by remember { mutableStateOf("") }
    var areaText by remember { mutableStateOf(defaultArea) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TaskaCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Request a service",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TaskaInk
            )
            Text(
                text = "Tell Taska what is missing. It helps owners find or invite the right providers in your hub.",
                style = MaterialTheme.typography.bodySmall,
                color = TaskaMutedText
            )

            OutlinedTextField(
                value = requestText,
                onValueChange = { requestText = it },
                label = { Text("What are you looking for?") },
                placeholder = { Text("e.g., Need a sewing / tailoring Tasker near Student Union") },
                minLines = 3,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("demand_request_input")
            )

            OutlinedTextField(
                value = areaText,
                onValueChange = { areaText = it },
                label = { Text("Your area (optional)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { onSubmitDemand(requestText, areaText) },
                enabled = requestText.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaskaViolet,
                    contentColor = TaskaWhite
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_demand_button")
            ) {
                Text("Send request", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskRequestSheet(
    targetTasker: TaskerEntity?,
    initialRequestText: String = "",
    onCreateTask: (
        shortTitle: String,
        requestText: String,
        budgetZmw: String,
        negotiable: Boolean,
        categoryId: String,
        urgencyOrTiming: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember {
        mutableStateOf(
            if (targetTasker != null) "Request for ${targetTasker.name}" else ""
        )
    }
    var selectedCategoryId by remember {
        mutableStateOf(targetTasker?.categoryId ?: TaskaCategory.DEVICE_REPAIRS.id)
    }
    var selectedUrgency by remember { mutableStateOf("Urgent · Today") }
    var budget by remember { mutableStateOf("") }
    var negotiable by remember { mutableStateOf(true) }
    var requestText by remember {
        mutableStateOf(
            initialRequestText.ifBlank {
                if (targetTasker != null) {
                    "I would like help from ${targetTasker.name} (${targetTasker.specialty.removePrefix("Verified · ")})."
                } else {
                    ""
                }
            }
        )
    }

    val urgencyOptions = remember {
        listOf("Urgent · Today", "Within 24 Hours", "This Weekend", "Flexible")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TaskaCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Create a Taska",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TaskaInk
            )
            Text(
                text = if (targetTasker == null) {
                    "Define your service requirements for your local marketplace. Verified Taskers can accept or propose a rate."
                } else {
                    "This creates a private request for ${targetTasker.name}. They can accept, then you decide whether to proceed."
                },
                style = MaterialTheme.typography.bodySmall,
                color = TaskaMutedText
            )

            // Marketplace Category Selector
            Text(
                text = "Service Category",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = TaskaInk
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskaCategory.entries.forEach { category ->
                    val isSelected = selectedCategoryId == category.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryId = category.id },
                        label = {
                            Text(
                                text = category.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TaskaViolet,
                            selectedLabelColor = TaskaWhite,
                            containerColor = TaskaWhite,
                            labelColor = TaskaInk
                        ),
                        modifier = Modifier.testTag("task_category_chip_${category.id}")
                    )
                }
            }

            // Timing / Urgency Selector
            Text(
                text = "When do you need this done?",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = TaskaInk
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                urgencyOptions.forEach { timing ->
                    val isSelected = selectedUrgency == timing
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedUrgency = timing },
                        label = {
                            Text(
                                text = timing,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TaskaInk,
                            selectedLabelColor = TaskaLime,
                            containerColor = TaskaWhite,
                            labelColor = TaskaInk
                        )
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Short title") },
                placeholder = { Text("e.g., Laptop USB-C port repair") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_taska_title_input")
            )

            OutlinedTextField(
                value = budget,
                onValueChange = { budget = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Your budget in ZMW (optional)") },
                placeholder = { Text("A Tasker can propose a price before you confirm") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_taska_budget_input")
            )

            Surface(
                color = TaskaWhite,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, TaskaBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Open to negotiating",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TaskaInk
                        )
                        Text(
                            text = "The Tasker can suggest a different price; you stay in control of the final booking.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = negotiable,
                        onCheckedChange = { negotiable = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = TaskaWhite, checkedTrackColor = TaskaViolet)
                    )
                }
            }

            OutlinedTextField(
                value = requestText,
                onValueChange = { requestText = it },
                label = { Text("Service requirements & details") },
                minLines = 3,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_taska_request_input")
            )

            Button(
                onClick = {
                    onCreateTask(
                        title,
                        requestText,
                        budget,
                        negotiable,
                        selectedCategoryId,
                        selectedUrgency
                    )
                },
                enabled = requestText.trim().length >= 2 || title.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaskaViolet,
                    contentColor = TaskaWhite
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_create_taska_button")
            ) {
                Text("Publish Task Requirement", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskerProfileDetailSheet(
    tasker: TaskerEntity,
    taskerReviews: List<TaskerReviewEntity> = emptyList(),
    onToggleFavourite: () -> Unit,
    onRequestThisTasker: () -> Unit,
    onBookPrivatelyNow: () -> Unit,
    onSubmitReview: (stars: Int, comment: String) -> Unit = { _, _ -> },
    onDismiss: () -> Unit
) {
    var reviewStars by remember { mutableIntStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }

    val services = remember(tasker.servicesListedCsv) {
        tasker.servicesListedCsv.split("||").mapNotNull { raw ->
            val parts = raw.split("::")
            if (parts.size >= 3) Triple(parts[0], parts[1], parts[2]) else null
        }
    }
    val csvReviews = remember(tasker.reviewsCsv) {
        tasker.reviewsCsv.split("||").mapNotNull { raw ->
            val parts = raw.split("::")
            if (parts.size >= 2) parts[0] to parts[1] else null
        }
    }
    val storePhotos = remember(tasker.storePhotosCsv) {
        tasker.storePhotosCsv.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    }
    val specificReviews = remember(taskerReviews, tasker.id) {
        taskerReviews.filter { it.taskerId == tasker.id }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TaskaCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(TaskaVioletSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tasker.initials,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = TaskaViolet
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tasker.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TaskaInk
                            )
                            Text(
                                text = "${tasker.roleType} · ${tasker.area}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TaskaMutedText
                            )
                            if (tasker.isVerified) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "✓ Taska Verified Provider",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = VerifiedGreen
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = tasker.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TaskaInk
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "★ ${tasker.rating}  ·  ${tasker.ratingCount} ratings  ·  ${tasker.completedTasks} completed Taskas",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TaskaInk
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onToggleFavourite,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (tasker.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = TaskaViolet,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (tasker.isFavourite) "Saved" else "Save Tasker",
                                color = TaskaViolet,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onRequestThisTasker,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TaskaViolet,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("request_this_tasker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Request Tasker", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onBookPrivatelyNow,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TaskaDeepInk,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = TaskaLime,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Instant Private Chat & Book (${tasker.priceRange})")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Taska keeps direct contact details private until you choose to work together.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = TaskaMutedText
                    )
                }
            }

            // Store / Portfolio Photos Gallery
            if (storePhotos.isNotEmpty()) {
                Text(
                    text = "STORE & PORTFOLIO PHOTOS (${storePhotos.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TaskaMutedText
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    storePhotos.forEachIndexed { idx, label ->
                        GlassmorphicCard(
                            modifier = Modifier
                                .width(150.dp)
                                .height(104.dp),
                            cornerRadius = 16.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(TaskaVioletSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        tint = TaskaViolet,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = if (label.startsWith("content://") || label.startsWith("http")) {
                                        "Portfolio Photo #${idx + 1}"
                                    } else {
                                        label.replace("_", " ").replaceFirstChar { it.uppercase() }
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TaskaInk,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }

            if (services.isNotEmpty()) {
                Text(
                    text = "SERVICES & LISTINGS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TaskaMutedText
                )
                services.forEach { (srvTitle, srvCat, srvPrice) ->
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = srvTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TaskaInk
                                )
                                Text(
                                    text = srvCat,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TaskaMutedText
                                )
                            }
                            Surface(
                                color = TaskaVioletSoft,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = srvPrice,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TaskaViolet,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Full 5-star to 1-star Rating Breakdown Card
            TaskaRatingBreakdownSummary(tasker = tasker)

            // Interactive Write a Review Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Leave a Rating & Review",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TaskaInk
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star stars",
                                tint = if (star <= reviewStars) StarAmber else TaskaBorder,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { reviewStars = star }
                                    .testTag("profile_review_star_$star")
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$reviewStars / 5 stars",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TaskaInk
                        )
                    }
                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        label = { Text("Share your experience with ${tasker.name}") },
                        placeholder = { Text("How was the quality, speed, and communication?") },
                        minLines = 2,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_review_comment_input")
                    )
                    Button(
                        onClick = {
                            if (reviewComment.isNotBlank()) {
                                onSubmitReview(reviewStars, reviewComment.trim())
                                reviewComment = ""
                            }
                        },
                        enabled = reviewComment.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = TaskaViolet),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_profile_review_button")
                    ) {
                        Text("Submit Verified Review", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (specificReviews.isNotEmpty() || csvReviews.isNotEmpty()) {
                Text(
                    text = "CLIENT REVIEWS (${specificReviews.size + csvReviews.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TaskaMutedText
                )
                specificReviews.forEach { rev ->
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = StarAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${rev.ratingStars} / 5 · ${rev.reviewerName} · ${rev.dateLabel}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TaskaInk
                                )
                                Text(
                                    text = rev.comment,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TaskaMutedText
                                )
                            }
                        }
                    }
                }
                csvReviews.forEach { (stars, comment) ->
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = StarAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "$stars / 5 · Verified Client",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TaskaInk
                                )
                                Text(
                                    text = comment,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TaskaMutedText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
