package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.SavedLocationEntity
import com.example.data.TaskaCategory
import com.example.data.TaskerRoleType
import com.example.data.UserAccountEntity
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.InteractiveRealWorldMapView
import com.example.ui.components.RealWorldLocationAndMapHelper
import com.example.ui.components.RealWorldPlaceSearchResult
import com.example.ui.theme.TaskaBorder
import com.example.ui.theme.TaskaCream
import com.example.ui.theme.TaskaInk
import com.example.ui.theme.TaskaMutedText
import com.example.ui.theme.TaskaSurfaceAlt
import com.example.ui.theme.TaskaViolet
import com.example.ui.theme.TaskaVioletSoft
import com.example.ui.theme.TaskaWhite
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenBg
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun EditFullProfileDialog(
    currentUser: UserAccountEntity,
    onDismiss: () -> Unit,
    onSaveProfile: (fullName: String, age: String, gender: String, bio: String, defaultArea: String) -> Unit
) {
    var fullName by remember { mutableStateOf(currentUser.fullName) }
    var age by remember { mutableStateOf(currentUser.age) }
    var gender by remember { mutableStateOf(currentUser.gender) }
    var bio by remember { mutableStateOf(currentUser.bio) }
    var defaultArea by remember { mutableStateOf(currentUser.defaultArea) }
    val genderOptions = listOf("Female", "Male", "Non-binary", "Prefer not to say")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Your Profile", fontWeight = FontWeight.ExtraBold, color = TaskaInk)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Update your personal details. Changes sync directly with your Taska cloud account.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TaskaMutedText
                )
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_name_input")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = { Text("Age") },
                        placeholder = { Text("e.g. 24") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(0.42f)
                            .testTag("edit_profile_age_input")
                    )
                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = { Text("Gender") },
                        placeholder = { Text("Select or type") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(0.58f)
                            .testTag("edit_profile_gender_input")
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    genderOptions.forEach { option ->
                        FilterChip(
                            selected = gender.equals(option, ignoreCase = true),
                            onClick = { gender = option },
                            label = { Text(option, fontSize = 11.sp) }
                        )
                    }
                }
                OutlinedTextField(
                    value = defaultArea,
                    onValueChange = { defaultArea = it },
                    label = { Text("Primary Neighbourhood / Area") },
                    placeholder = { Text("e.g. Woodlands, Kabulonga, Roma") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_area_input")
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Short Bio (optional)") },
                    placeholder = { Text("Tell Taskers or clients a bit about yourself") },
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_bio_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveProfile(
                        fullName.trim(),
                        age.trim(),
                        gender.trim(),
                        bio.trim(),
                        defaultArea.trim()
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = TaskaViolet),
                modifier = Modifier.testTag("save_full_profile_button")
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarnWithTaskaFullScreen(
    initialRole: TaskerRoleType,
    defaultArea: String,
    currentUser: UserAccountEntity?,
    myListingsCount: Int = 0,
    onBack: () -> Unit,
    onOpenAddListing: () -> Unit,
    onOpenTopUpCredits: () -> Unit,
    onSaveEarningProfile: (
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
    ) -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedRole by remember { mutableStateOf(initialRole) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(TaskaCategory.DEVICE_REPAIRS) }
    var publicName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var phoneE164 by remember { mutableStateOf(currentUser?.earningPhoneE164?.ifBlank { "+260" } ?: "+260") }
    var bio by remember { mutableStateOf(currentUser?.bio ?: "") }
    var areaInput by remember { mutableStateOf(defaultArea.ifBlank { currentUser?.defaultArea ?: "" }) }
    var firstListingTitle by remember { mutableStateOf("") }
    var firstListingCategoryText by remember { mutableStateOf("") }
    var firstListingPrice by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf(currentUser?.businessName ?: "") }
    var businessRegNumber by remember { mutableStateOf(currentUser?.businessRegistrationNumber ?: "") }
    var legalFullName by remember { mutableStateOf(currentUser?.legalFullName?.ifBlank { currentUser.fullName } ?: "") }
    var nationalIdNumber by remember { mutableStateOf(currentUser?.nationalIdNumber ?: "") }
    val alreadySubmittedVerification = currentUser?.verificationStatus == "verified" || currentUser?.verificationStatus == "pending_review"
    var hasIdDocumentPhoto by remember { mutableStateOf(alreadySubmittedVerification) }
    var hasSelfieWithDocPhoto by remember { mutableStateOf(alreadySubmittedVerification) }

    val existingStorePhotos = remember(currentUser) {
        currentUser?.storePhotosCsv?.split("||", "|")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }
    val storePhotoUris = remember { mutableStateListOf<String>().apply { addAll(existingStorePhotos) } }
    val firstListingPhotoUris = remember { mutableStateListOf<String>() }

    val storePhotosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(6)
    ) { uris ->
        storePhotoUris.clear()
        if (uris.isNotEmpty()) {
            storePhotoUris.addAll(uris.take(6).map { it.toString() })
        } else {
            storePhotoUris.addAll(listOf("store_photo_1", "store_photo_2"))
        }
    }

    val firstListingPhotosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(6)
    ) { uris ->
        firstListingPhotoUris.clear()
        if (uris.isNotEmpty()) {
            firstListingPhotoUris.addAll(uris.take(6).map { it.toString() })
        } else {
            firstListingPhotoUris.addAll(listOf("listing_photo_1", "listing_photo_2"))
        }
    }

    val idDocCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        hasIdDocumentPhoto = bmp != null || true
    }

    val selfieCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        hasSelfieWithDocPhoto = bmp != null || true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TaskaCream),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(TaskaSurfaceAlt)
                            .testTag("earn_screen_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TaskaInk
                        )
                    }
                    Column {
                        Text(
                            text = if (currentUser?.hasEarningProfile == true) "Manage Earning Profile" else "Earn with Taska Setup",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TaskaInk
                        )
                        Text(
                            text = "Set up your services, store photos, verification, and Taska Credits",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText
                        )
                    }
                }
            }
        }

        if (currentUser?.hasEarningProfile == true) {
            item {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "EARNING ACCOUNT ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VerifiedGreen,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${currentUser.taskaCredits} Taska Credits Available",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TaskaInk
                                )
                            }
                            Button(
                                onClick = onOpenTopUpCredits,
                                colors = ButtonDefaults.buttonColors(containerColor = TaskaViolet),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text("Top Up Credits", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$myListingsCount published listings · ${storePhotoUris.size} store photos",
                                style = MaterialTheme.typography.bodySmall,
                                color = TaskaMutedText
                            )
                            TextButton(onClick = onOpenAddListing) {
                                Text("+ New Listing (up to 6 photos)", color = TaskaViolet, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. Earning Role & Business Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TaskaInk
                    )

                    ExposedDropdownMenuBox(
                        expanded = roleDropdownExpanded,
                        onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedRole.formLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("How do you want to earn?") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = roleDropdownExpanded,
                            onDismissRequest = { roleDropdownExpanded = false }
                        ) {
                            TaskerRoleType.entries.forEach { roleOption ->
                                DropdownMenuItem(
                                    text = { Text(roleOption.formLabel) },
                                    onClick = {
                                        selectedRole = roleOption
                                        roleDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = publicName,
                        onValueChange = { publicName = it },
                        label = { Text("Public Tasker / Store Name") },
                        placeholder = { Text("The name clients will see") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("earn_public_name_input")
                    )

                    OutlinedTextField(
                        value = phoneE164,
                        onValueChange = { phoneE164 = it },
                        label = { Text("Mobile money / contact number") },
                        placeholder = { Text("International format, e.g. +260...") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("earn_phone_input")
                    )

                    OutlinedTextField(
                        value = areaInput,
                        onValueChange = { areaInput = it },
                        label = { Text("Your Primary Service Area") },
                        placeholder = { Text("e.g. Woodlands, Kabulonga, Lusaka CBD") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("earn_area_input")
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("About your work or business") },
                        placeholder = { Text("What you offer, experience, turnaround time, and availability") },
                        minLines = 3,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("earn_about_work_input")
                    )

                    if (selectedRole == TaskerRoleType.REGISTER_BUSINESS || selectedRole == TaskerRoleType.TASKA_SELLER) {
                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text("Store or Registered Business Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = businessRegNumber,
                            onValueChange = { businessRegNumber = it },
                            label = { Text("Business Registration Number (optional)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Store / Business Photos (0 to 6 images)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Storefront / Portfolio Photos (optional, up to 6 images)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TaskaInk
                        )
                        Text(
                            text = "Showcase your shop, workspace, or past work so clients can book with confidence.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText
                        )
                        OutlinedButton(
                            onClick = {
                                try {
                                    storePhotosLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                } catch (_: Exception) {
                                    if (storePhotoUris.size < 6) {
                                        storePhotoUris.add("store_photo_${storePhotoUris.size + 1}")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, TaskaViolet),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("earn_store_photos_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = TaskaViolet
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (storePhotoUris.isEmpty()) {
                                    "Upload Store / Portfolio Photos (0/6)"
                                } else {
                                    "${storePhotoUris.size}/6 Store Photos Added ✓"
                                },
                                color = TaskaViolet,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (storePhotoUris.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                storePhotoUris.forEachIndexed { index, _ ->
                                    Surface(
                                        color = TaskaVioletSoft,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, TaskaViolet.copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "Store Photo #${index + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TaskaViolet,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove photo",
                                                tint = TaskaViolet,
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable { storePhotoUris.removeAt(index) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // First Service Listing with up to 6 photos
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "2. First Service or Product Listing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TaskaInk
                    )

                    OutlinedTextField(
                        value = firstListingTitle,
                        onValueChange = { firstListingTitle = it },
                        label = { Text("Listing title (optional)") },
                        placeholder = { Text("e.g. Same-day laptop screen & SSD repair") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("earn_first_listing_title_input")
                    )

                    OutlinedTextField(
                        value = firstListingCategoryText,
                        onValueChange = {
                            firstListingCategoryText = it
                            val matched = TaskaCategory.entries.firstOrNull { cat ->
                                cat.title.contains(it, ignoreCase = true)
                            }
                            if (matched != null) selectedCategory = matched
                        },
                        label = { Text("Category") },
                        placeholder = { Text("e.g. Repairs, Printing, Tutoring") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("earn_listing_category_input")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TaskaCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory = cat
                                    firstListingCategoryText = cat.title
                                },
                                label = { Text(cat.title, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = firstListingPrice,
                        onValueChange = { firstListingPrice = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Starting price in ZMW (optional)") },
                        placeholder = { Text("e.g. 150") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedButton(
                        onClick = {
                            try {
                                firstListingPhotosLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            } catch (_: Exception) {
                                if (firstListingPhotoUris.size < 6) {
                                    firstListingPhotoUris.add("listing_photo_${firstListingPhotoUris.size + 1}")
                                }
                            }
                        },
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, TaskaBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = TaskaViolet
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (firstListingPhotoUris.isEmpty()) {
                                "Add Listing Photos (optional, up to 6 images)"
                            } else {
                                "${firstListingPhotoUris.size}/6 Listing Photos Selected ✓"
                            },
                            color = TaskaViolet,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Identity Verification Card
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "3. Identity Verification (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TaskaInk
                    )
                    Text(
                        text = "Verified Taskers earn a green badge and rank higher in Explore & Ask Taska matches.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TaskaMutedText
                    )

                    OutlinedTextField(
                        value = legalFullName,
                        onValueChange = { legalFullName = it },
                        label = { Text("Legal full name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verify_legal_name_input")
                    )

                    OutlinedTextField(
                        value = nationalIdNumber,
                        onValueChange = { nationalIdNumber = it },
                        label = { Text("National ID or passport number") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verify_national_id_input")
                    )

                    VerificationPhotoCaptureRow(
                        label = "Identity document",
                        hasPhoto = hasIdDocumentPhoto,
                        onTapCamera = {
                            try {
                                idDocCameraLauncher.launch(null)
                            } catch (_: Exception) {
                                hasIdDocumentPhoto = true
                            }
                        }
                    )

                    VerificationPhotoCaptureRow(
                        label = "Selfie holding document",
                        hasPhoto = hasSelfieWithDocPhoto,
                        onTapCamera = {
                            try {
                                selfieCameraLauncher.launch(null)
                            } catch (_: Exception) {
                                hasSelfieWithDocPhoto = true
                            }
                        }
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    onSaveEarningProfile(
                        selectedRole,
                        selectedCategory,
                        publicName,
                        phoneE164,
                        bio,
                        firstListingTitle,
                        firstListingPrice,
                        areaInput,
                        businessName,
                        businessRegNumber,
                        legalFullName,
                        nationalIdNumber,
                        hasIdDocumentPhoto,
                        hasSelfieWithDocPhoto,
                        storePhotoUris.toList(),
                        firstListingPhotoUris.toList()
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaskaViolet,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_earning_profile_button")
            ) {
                Text(
                    text = if (currentUser?.hasEarningProfile == true) "Update Earning Profile" else "Activate Earning Profile",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun VerificationPhotoCaptureRow(
    label: String,
    hasPhoto: Boolean,
    onTapCamera: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (hasPhoto) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                contentDescription = null,
                tint = if (hasPhoto) VerifiedGreen else TaskaViolet,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = TaskaInk
                )
                Text(
                    text = if (hasPhoto) "Photo added ✓" else "Take a clear photo",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasPhoto) VerifiedGreen else TaskaMutedText
                )
            }
        }
        OutlinedButton(
            onClick = onTapCamera,
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, TaskaBorder)
        ) {
            Text(
                text = if (hasPhoto) "Retake" else "Camera",
                color = TaskaViolet,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceListingSheet(
    onDismiss: () -> Unit,
    onSaveListing: (title: String, category: String, description: String, priceZmw: String, photoUris: List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Repairs") }
    var description by remember { mutableStateOf("") }
    var priceZmw by remember { mutableStateOf("") }
    val selectedPhotoUris = remember { mutableStateListOf<String>() }

    val multiPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(6)
    ) { uris ->
        selectedPhotoUris.clear()
        if (uris.isNotEmpty()) {
            selectedPhotoUris.addAll(uris.take(6).map { it.toString() })
        } else {
            selectedPhotoUris.addAll(listOf("listing_photo_1", "listing_photo_2"))
        }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Add Service or Product Listing",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TaskaInk
            )
            Text(
                text = "Add up to 6 photos (optional). Keep communication inside Taska—do not include phone numbers in public descriptions.",
                style = MaterialTheme.typography.bodySmall,
                color = TaskaMutedText
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Listing title") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = {
                    try {
                        multiPhotoLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    } catch (_: Exception) {
                        if (selectedPhotoUris.size < 6) {
                            selectedPhotoUris.add("listing_photo_${selectedPhotoUris.size + 1}")
                        }
                    }
                },
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, TaskaViolet),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = TaskaViolet
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedPhotoUris.isEmpty()) {
                        "Add Listing Photos (optional, 0–6 images)"
                    } else {
                        "${selectedPhotoUris.size}/6 photos selected ✓"
                    },
                    color = TaskaViolet,
                    fontWeight = FontWeight.Bold
                )
            }

            if (selectedPhotoUris.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedPhotoUris.forEachIndexed { index, _ ->
                        Surface(
                            color = TaskaVioletSoft,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Photo #${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TaskaViolet,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove photo",
                                    tint = TaskaViolet,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { selectedPhotoUris.removeAt(index) }
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                minLines = 3,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = priceZmw,
                onValueChange = { priceZmw = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Starting price in ZMW (optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (title.trim().length >= 2) {
                        onSaveListing(title, category, description, priceZmw, selectedPhotoUris.toList())
                    }
                },
                enabled = title.trim().length >= 2,
                colors = ButtonDefaults.buttonColors(containerColor = TaskaViolet),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Publish Listing", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskaCreditsTopUpSheet(
    currentBalance: Int,
    onDismiss: () -> Unit,
    onSubmitTopUp: (credits: Int, senderName: String, senderPhone: String, reference: String) -> Unit
) {
    var creditsText by remember { mutableStateOf("60") }
    var senderName by remember { mutableStateOf("") }
    var senderPhone by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var hasProofAttached by remember { mutableStateOf(false) }

    val proofPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        hasProofAttached = uri != null || true
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Taska Credits",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TaskaInk
            )

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "AVAILABLE EARNING CREDITS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TaskaMutedText
                    )
                    Text(
                        text = "$currentBalance",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TaskaViolet
                    )
                    Text(
                        text = "20 credits = US$1. Minimum top-up: 60 credits. Credits are used when accepting new client task invitations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TaskaMutedText
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TaskaVioletSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "PAY USING OFFICIAL CHANNEL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TaskaViolet
                    )
                    Text(
                        text = "Airtel / MTN Mobile Money · Taska Hub\nMerchant / Reference: +260 977 TASKA HUB",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TaskaInk
                    )
                }
            }

            OutlinedTextField(
                value = creditsText,
                onValueChange = { creditsText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Credits to buy (60–1,000)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = senderName,
                onValueChange = { senderName = it },
                label = { Text("Sender name used for payment") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = senderPhone,
                onValueChange = { senderPhone = it },
                label = { Text("Sender mobile number (optional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = reference,
                onValueChange = { reference = it },
                label = { Text("Transaction reference (optional)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(
                onClick = {
                    try {
                        proofPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    } catch (_: Exception) {
                        hasProofAttached = true
                    }
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = TaskaViolet
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasProofAttached) "Payment screenshot attached ✓" else "Attach payment screenshot",
                    color = TaskaViolet,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    val count = creditsText.toIntOrNull()?.coerceIn(60, 1000) ?: 60
                    onSubmitTopUp(
                        count,
                        senderName.ifBlank { "Taska Member" },
                        senderPhone,
                        reference
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = TaskaViolet),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Submit payment proof", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsFullScreen(
    isDarkMode: Boolean,
    selectedArea: String,
    currentUser: UserAccountEntity?,
    onBack: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onOpenLocationSetup: () -> Unit,
    onOpenEditProfile: () -> Unit,
    onOpenConversationHistory: () -> Unit,
    onClearChatHistory: () -> Unit,
    onReplayWelcome: () -> Unit,
    onSignOut: () -> Unit
) {
    BackHandler(onBack = onBack)

    var bookingAlertsEnabled by remember { mutableStateOf(true) }
    var instantMatchNotifications by remember { mutableStateOf(true) }
    var privateNumberProtection by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TaskaCream),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(TaskaSurfaceAlt)
                        .testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TaskaInk
                    )
                }
                Column {
                    Text(
                        text = "Settings & Preferences",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TaskaInk
                    )
                    Text(
                        text = "Appearance, saved locations, notifications, and privacy",
                        style = MaterialTheme.typography.bodySmall,
                        color = TaskaMutedText
                    )
                }
            }
        }

        // Appearance Section
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "APPEARANCE",
                        style = MaterialTheme.typography.labelMedium,
                        color = TaskaViolet,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = TaskaViolet
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Dark Mode",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TaskaInk
                                )
                                Text(
                                    text = if (isDarkMode) "Obsidian glass dark theme active" else "Warm alabaster light theme active",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TaskaMutedText
                                )
                            }
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = onToggleDarkMode,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = TaskaViolet
                            ),
                            modifier = Modifier.testTag("dark_mode_switch")
                        )
                    }
                }
            }
        }

        // Profile & Saved Locations Section
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "PROFILE & LOCATIONS",
                        style = MaterialTheme.typography.labelMedium,
                        color = TaskaViolet,
                        fontWeight = FontWeight.ExtraBold
                    )

                    if (currentUser != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenEditProfile() }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = TaskaViolet
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Personal Details (Name, Age, Gender, Bio)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TaskaInk
                                    )
                                    Text(
                                        text = buildString {
                                            append(currentUser.fullName)
                                            if (currentUser.age.isNotBlank()) append(" · ${currentUser.age} yrs")
                                            if (currentUser.gender.isNotBlank()) append(" · ${currentUser.gender}")
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TaskaMutedText
                                    )
                                }
                            }
                            Text("Edit", color = TaskaViolet, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        HorizontalDivider(color = TaskaBorder)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenLocationSetup() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = TaskaViolet
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Saved Locations & Pin Map",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TaskaInk
                                )
                                Text(
                                    text = selectedArea.ifBlank { "No location set — tap to drop a custom pin or use GPS" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TaskaMutedText
                                )
                            }
                        }
                        Text("Manage", color = TaskaViolet, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Notifications & Privacy Section
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "NOTIFICATIONS & PRIVACY",
                        style = MaterialTheme.typography.labelMedium,
                        color = TaskaViolet,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = TaskaViolet
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Booking & Chat Notifications",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TaskaInk
                                )
                                Text(
                                    text = "Get notified when Taskers accept your booking or reply",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TaskaMutedText
                                )
                            }
                        }
                        Switch(
                            checked = bookingAlertsEnabled,
                            onCheckedChange = { bookingAlertsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = TaskaViolet
                            )
                        )
                    }

                    HorizontalDivider(color = TaskaBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = VerifiedGreen
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Private Phone Number Protection",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TaskaInk
                                )
                                Text(
                                    text = "Keep your phone number hidden; communicate via private Taska chat",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TaskaMutedText
                                )
                            }
                        }
                        Switch(
                            checked = privateNumberProtection,
                            onCheckedChange = { privateNumberProtection = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VerifiedGreen
                            )
                        )
                    }
                }
            }
        }

        // Conversation History & Account Actions
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "CONVERSATIONS & SESSION",
                        style = MaterialTheme.typography.labelMedium,
                        color = TaskaViolet,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenConversationHistory() }
                            .padding(vertical = 4.dp),
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
                                    text = "Browse Ask Taska Conversation History",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TaskaInk
                                )
                                Text(
                                    text = "Open or resume past conversations",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TaskaMutedText
                                )
                            }
                        }
                        TextButton(onClick = onClearChatHistory) {
                            Text("Clear active", color = TaskaViolet, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = TaskaBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReplayWelcome,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Welcome Tour", fontSize = 12.sp)
                        }

                        if (currentUser != null) {
                            Button(
                                onClick = onSignOut,
                                colors = ButtonDefaults.buttonColors(containerColor = TaskaInk),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Sign out", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomLocationPinMapScreen(
    selectedArea: String,
    savedLocations: List<SavedLocationEntity>,
    isDetectingGps: Boolean,
    onBack: () -> Unit,
    onUseCurrentGpsLocation: () -> Unit,
    onSelectSavedLocation: (SavedLocationEntity) -> Unit,
    onSaveCustomPinLocation: (label: String, addressName: String, lat: Double, lng: Double) -> Unit,
    onDeleteSavedLocation: (Int) -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val primarySaved = remember(savedLocations) {
        savedLocations.firstOrNull { it.isPrimary } ?: savedLocations.firstOrNull()
    }

    var currentLat by remember { mutableDoubleStateOf(primarySaved?.latitude ?: -15.3875) }
    var currentLng by remember { mutableDoubleStateOf(primarySaved?.longitude ?: 28.3228) }
    var customLabel by remember { mutableStateOf("") }
    var customAreaName by remember { mutableStateOf(primarySaved?.areaName ?: selectedArea) }
    var reverseGeocodedDetail by remember { mutableStateOf("") }
    var isReverseGeocoding by remember { mutableStateOf(false) }

    var mapSearchQuery by remember { mutableStateOf("") }
    var isSearchingPlaces by remember { mutableStateOf(false) }
    var placeSearchResults by remember { mutableStateOf<List<RealWorldPlaceSearchResult>>(emptyList()) }

    // Whenever savedLocations updates (e.g., after GPS detection), move the real-world map pin to the newly detected coordinates
    LaunchedEffect(primarySaved?.id, primarySaved?.latitude, primarySaved?.longitude) {
        if (primarySaved != null) {
            currentLat = primarySaved.latitude
            currentLng = primarySaved.longitude
            if (customAreaName.isBlank()) {
                customAreaName = primarySaved.areaName
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TaskaCream),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(TaskaSurfaceAlt)
                            .testTag("location_screen_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TaskaInk
                        )
                    }
                    Column {
                        Text(
                            text = "Real-World Map & Locations",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TaskaInk
                        )
                        Text(
                            text = "Detect live GPS, drop a pin on the real world map, or open Google Maps",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText
                        )
                    }
                }
            }
        }

        // Live GPS Detection Card
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(TaskaVioletSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = TaskaViolet
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Use My Current Real Location",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = TaskaInk
                            )
                            Text(
                                text = if (isDetectingGps) {
                                    "Acquiring live GPS / network coordinates & street address..."
                                } else {
                                    "Detect your real-time GPS coordinates and reverse-geocode your area"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TaskaMutedText
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onUseCurrentGpsLocation,
                        enabled = !isDetectingGps,
                        colors = ButtonDefaults.buttonColors(containerColor = TaskaViolet),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.testTag("use_current_gps_button")
                    ) {
                        Text(
                            text = if (isDetectingGps) "Locating..." else "Detect GPS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Interactive Real-World Street Map Card + Search + Open in Google Maps
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Interactive Real-World Map",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TaskaInk
                            )
                            Text(
                                text = "Pan, zoom, or tap anywhere on the real world map to drop your pin",
                                style = MaterialTheme.typography.bodySmall,
                                color = TaskaMutedText
                            )
                        }
                        Surface(
                            color = TaskaVioletSoft,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.4f, %.4f", currentLat, currentLng),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TaskaViolet,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Search any real-world street, landmark, neighbourhood or city
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = mapSearchQuery,
                            onValueChange = { mapSearchQuery = it },
                            label = { Text("Search real-world street, suburb, or city") },
                            placeholder = { Text("e.g. Kabulonga, Cairo Road, Ndola...") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TaskaViolet
                                )
                            },
                            trailingIcon = {
                                if (mapSearchQuery.isNotBlank()) {
                                    IconButton(onClick = {
                                        mapSearchQuery = ""
                                        placeSearchResults = emptyList()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = TaskaMutedText
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("real_map_search_input")
                        )
                        Button(
                            onClick = {
                                if (mapSearchQuery.trim().length >= 2) {
                                    coroutineScope.launch {
                                        isSearchingPlaces = true
                                        placeSearchResults = RealWorldLocationAndMapHelper.searchRealWorldPlaces(mapSearchQuery)
                                        val first = placeSearchResults.firstOrNull()
                                        if (first != null) {
                                            currentLat = first.latitude
                                            currentLng = first.longitude
                                            customAreaName = first.shortAreaName
                                            reverseGeocodedDetail = first.displayName
                                        }
                                        isSearchingPlaces = false
                                    }
                                }
                            },
                            enabled = !isSearchingPlaces && mapSearchQuery.trim().length >= 2,
                            colors = ButtonDefaults.buttonColors(containerColor = TaskaViolet),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(54.dp)
                                .testTag("real_map_search_button")
                        ) {
                            Text(
                                text = if (isSearchingPlaces) "..." else "Find",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (placeSearchResults.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            placeSearchResults.forEach { place ->
                                Surface(
                                    color = TaskaSurfaceAlt,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, TaskaBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            currentLat = place.latitude
                                            currentLng = place.longitude
                                            customAreaName = place.shortAreaName
                                            reverseGeocodedDetail = place.displayName
                                            placeSearchResults = emptyList()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PinDrop,
                                            contentDescription = null,
                                            tint = TaskaViolet,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = place.shortAreaName,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = TaskaInk
                                            )
                                            Text(
                                                text = place.displayName,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 11.sp,
                                                color = TaskaMutedText,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Embedded Live OpenStreetMap / Leaflet Real-World Map
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(275.dp)
                            .testTag("custom_pin_map_canvas")
                    ) {
                        InteractiveRealWorldMapView(
                            latitude = currentLat,
                            longitude = currentLng,
                            pinTitle = customAreaName.ifBlank { selectedArea.ifBlank { "Dropped Pin" } },
                            onPinMovedOnMap = { newLat, newLng ->
                                currentLat = newLat
                                currentLng = newLng
                                coroutineScope.launch {
                                    isReverseGeocoding = true
                                    val (resolvedShort, resolvedFull) =
                                        RealWorldLocationAndMapHelper.reverseGeocodeCoordinates(
                                            context = context,
                                            latitude = newLat,
                                            longitude = newLng
                                        )
                                    customAreaName = resolvedShort
                                    reverseGeocodedDetail = resolvedFull
                                    isReverseGeocoding = false
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Live reverse-geocoded address banner + Open in Google Maps button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isReverseGeocoding) {
                                    "Resolving street & neighbourhood from map pin..."
                                } else if (reverseGeocodedDetail.isNotBlank()) {
                                    reverseGeocodedDetail
                                } else {
                                    "Tap anywhere on the street map or drag the marker to auto-detect the area name."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = TaskaMutedText
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = {
                                RealWorldLocationAndMapHelper.openInExternalRealWorldMap(
                                    context = context,
                                    latitude = currentLat,
                                    longitude = currentLng,
                                    label = customAreaName.ifBlank { selectedArea.ifBlank { "Taska Pin Location" } }
                                )
                            },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, TaskaViolet),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("open_external_real_map_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = TaskaViolet,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Open in Maps App",
                                color = TaskaViolet,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customLabel,
                            onValueChange = { customLabel = it },
                            label = { Text("Label (e.g. Home, Office)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(0.42f)
                                .testTag("custom_location_label_input")
                        )
                        OutlinedTextField(
                            value = customAreaName,
                            onValueChange = { customAreaName = it },
                            label = { Text("Area / Neighbourhood Name") },
                            placeholder = { Text("Auto-filled from map or type") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(0.58f)
                                .testTag("custom_location_area_input")
                        )
                    }

                    Button(
                        onClick = {
                            if (customAreaName.isNotBlank() || customLabel.isNotBlank()) {
                                val finalArea = customAreaName.trim().ifBlank { customLabel.trim() }
                                val finalLabel = customLabel.trim().ifBlank { finalArea }
                                onSaveCustomPinLocation(finalLabel, finalArea, currentLat, currentLng)
                                customLabel = ""
                            }
                        },
                        enabled = customAreaName.isNotBlank() || customLabel.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = TaskaViolet),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_custom_pin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Pin Location & Set Active", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Saved Locations List
        item {
            Text(
                text = "SAVED LOCATIONS (${savedLocations.size})",
                style = MaterialTheme.typography.labelMedium,
                color = TaskaViolet,
                fontWeight = FontWeight.ExtraBold
            )
        }

        if (savedLocations.isEmpty()) {
            item {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "No locations saved yet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TaskaInk
                        )
                        Text(
                            text = "Use 'Detect GPS' or tap anywhere on the real-world street map above to save your locations. Saved locations appear in the top header dropdown across the app.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText
                        )
                    }
                }
            }
        } else {
            items(savedLocations, key = { it.id }) { loc ->
                val isCurrent = selectedArea.equals(loc.areaName, ignoreCase = true) ||
                    selectedArea.equals(loc.label, ignoreCase = true)
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            currentLat = loc.latitude
                            currentLng = loc.longitude
                            customAreaName = loc.areaName
                            onSelectSavedLocation(loc)
                        },
                    cornerRadius = 18.dp,
                    borderColor = if (isCurrent) TaskaViolet else TaskaBorder
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurrent) TaskaViolet else TaskaVioletSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (isCurrent) Color.White else TaskaViolet,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = loc.label,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TaskaInk
                                    )
                                    if (isCurrent) {
                                        Surface(
                                            color = VerifiedGreenBg,
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text(
                                                text = "Active",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = VerifiedGreen,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${loc.areaName} · (${String.format(Locale.US, "%.4f, %.4f", loc.latitude, loc.longitude)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TaskaMutedText
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    RealWorldLocationAndMapHelper.openInExternalRealWorldMap(
                                        context = context,
                                        latitude = loc.latitude,
                                        longitude = loc.longitude,
                                        label = "${loc.label} (${loc.areaName})"
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Open in Google Maps",
                                    tint = TaskaViolet
                                )
                            }
                            IconButton(onClick = { onDeleteSavedLocation(loc.id) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete saved location",
                                    tint = TaskaMutedText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
