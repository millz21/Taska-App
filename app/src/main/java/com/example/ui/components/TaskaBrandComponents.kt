package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SavedLocationEntity
import com.example.data.TaskerEntity
import com.example.data.UserAccountEntity
import com.example.ui.TaskaTab
import com.example.ui.theme.CategoryButter
import com.example.ui.theme.CategoryButterIcon
import com.example.ui.theme.CategoryCoolGray
import com.example.ui.theme.CategoryCoolGrayIcon
import com.example.ui.theme.CategoryLavender
import com.example.ui.theme.CategoryLavenderIcon
import com.example.ui.theme.CategoryMint
import com.example.ui.theme.CategoryMintIcon
import com.example.ui.theme.CategoryPeach
import com.example.ui.theme.CategoryPeachIcon
import com.example.ui.theme.CategorySky
import com.example.ui.theme.CategorySkyIcon
import com.example.ui.theme.CoralDot
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.ui.theme.PlusJakartaSansFamily
import com.example.ui.theme.StarAmber
import com.example.ui.theme.TaskaBorder
import com.example.ui.theme.TaskaCream
import com.example.ui.theme.TaskaCreamSecondary
import com.example.ui.theme.TaskaDeepInk
import com.example.ui.theme.TaskaGlassBorder
import com.example.ui.theme.TaskaGlassCardBrush
import com.example.ui.theme.TaskaGlassHeaderBrush
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
import com.example.ui.theme.VerifiedGreenBg

@Composable
fun TaskaGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    borderColor: Color = TaskaGlassBorder,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = modifier
        .clip(shape)
        .background(brush = TaskaGlassCardBrush, shape = shape)
        .border(1.dp, borderColor, shape)
        .let { mod -> if (onClick != null) mod.clickable { onClick() } else mod }

    Column(
        modifier = baseModifier.padding(contentPadding),
        content = content
    )
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    borderColor: Color = TaskaGlassBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .clip(shape)
            .background(brush = TaskaGlassCardBrush, shape = shape)
            .border(1.dp, borderColor, shape),
        content = content
    )
}

@Composable
fun TaskaTopBar(
    selectedArea: String,
    savedLocations: List<SavedLocationEntity> = emptyList(),
    onAreaSelected: (String) -> Unit,
    onOpenLocationSetup: () -> Unit = {},
    onOpenLiveVoice: () -> Unit,
    onOpenHowItWorks: () -> Unit,
    currentUser: UserAccountEntity? = null,
    onOpenAccountOrAuth: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var areaMenuExpanded by remember { mutableStateOf(false) }
    val hasAreaSet = selectedArea.isNotBlank()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(brush = TaskaGlassHeaderBrush)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Taska Brand Mark
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("taska_brand_header")
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(TaskaVioletSolid, Color(0xFF3A1FD6))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "T",
                            fontFamily = PlayfairDisplayFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TaskaPureWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Taska",
                        fontFamily = PlusJakartaSansFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp,
                        color = TaskaInk,
                        letterSpacing = (-0.5).sp
                    )
                }

                // Custom Saved Location Selector & Quick Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box {
                        Surface(
                            color = if (hasAreaSet) TaskaGlassWhite else TaskaVioletSoft,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                1.dp,
                                if (hasAreaSet) TaskaBorder else TaskaViolet
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { areaMenuExpanded = true }
                                .testTag("area_selector_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (hasAreaSet) Icons.Default.LocationOn else Icons.Default.AddLocationAlt,
                                    contentDescription = null,
                                    tint = if (hasAreaSet) TaskaViolet else CoralDot,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (hasAreaSet) selectedArea else "Set Location",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasAreaSet) TaskaInk else TaskaViolet,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TaskaMutedText,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = areaMenuExpanded,
                            onDismissRequest = { areaMenuExpanded = false },
                            modifier = Modifier
                                .background(TaskaWhite)
                                .border(1.dp, TaskaBorder, RoundedCornerShape(14.dp))
                        ) {
                            if (savedLocations.isEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = "No saved locations yet",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = TaskaInk
                                            )
                                            Text(
                                                text = "Tap below to detect GPS or drop a custom pin",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 11.sp,
                                                color = TaskaMutedText
                                            )
                                        }
                                    },
                                    onClick = {
                                        areaMenuExpanded = false
                                        onOpenLocationSetup()
                                    }
                                )
                            } else {
                                savedLocations.forEach { loc ->
                                    val isSelected = selectedArea.equals(loc.areaName, ignoreCase = true)
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = loc.label,
                                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                    color = if (isSelected) TaskaViolet else TaskaInk,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "${loc.areaName} · ${loc.radiusKm}km radius",
                                                    fontSize = 11.sp,
                                                    color = TaskaMutedText
                                                )
                                            }
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.PushPin,
                                                contentDescription = null,
                                                tint = if (isSelected) TaskaViolet else TaskaMutedText,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        },
                                        onClick = {
                                            onAreaSelected(loc.areaName)
                                            areaMenuExpanded = false
                                        }
                                    )
                                }
                            }

                            HorizontalDivider(color = TaskaBorder)

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (savedLocations.isEmpty()) "+ Set Location / Use GPS" else "+ Drop Pin / Manage Locations",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TaskaViolet,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = null,
                                        tint = TaskaViolet,
                                        modifier = Modifier.size(17.dp)
                                    )
                                },
                                onClick = {
                                    areaMenuExpanded = false
                                    onOpenLocationSetup()
                                }
                            )
                        }
                    }

                    // Live Voice Conversation Button
                    Surface(
                        color = TaskaLime,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onOpenLiveVoice() }
                            .testTag("live_voice_top_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Start Live Voice",
                                tint = TaskaDeepInk,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Live",
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TaskaDeepInk
                            )
                        }
                    }

                    // Sign In / Account Pill
                    Surface(
                        color = if (currentUser != null) TaskaVioletSoft else TaskaVioletSolid,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onOpenAccountOrAuth() }
                            .testTag("top_bar_account_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (currentUser != null) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(TaskaVioletSolid),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentUser.avatarInitials.take(1),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TaskaPureWhite
                                    )
                                }
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = currentUser.fullName.split(" ").firstOrNull() ?: "Account",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontSize = 11.sp,
                                    color = TaskaViolet,
                                    maxLines = 1
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Sign In",
                                    tint = TaskaPureWhite,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sign In",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontSize = 11.sp,
                                    color = TaskaPureWhite
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onOpenHowItWorks,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("how_it_works_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "How Taska Works",
                            tint = TaskaInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            HorizontalDivider(color = TaskaBorder.copy(alpha = 0.6f), thickness = 0.8.dp)
        }
    }
}

@Composable
fun TaskaModernBottomBar(
    currentTab: TaskaTab,
    bookingsCount: Int,
    inboxCount: Int,
    isSignedIn: Boolean,
    onSelectTab: (TaskaTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        Triple(TaskaTab.ASK_TASKA, "Ask Taska", Icons.AutoMirrored.Filled.Chat to "nav_tab_ask_taska"),
        Triple(TaskaTab.BOOKINGS, "My Tasks", Icons.Default.Checklist to "nav_tab_bookings"),
        Triple(TaskaTab.EXPLORE, "Explore", Icons.Default.Explore to "nav_tab_explore"),
        Triple(TaskaTab.INBOX, "Inbox", Icons.Default.Notifications to "nav_tab_inbox"),
        Triple(TaskaTab.TASKER_MODE, "Profile", Icons.Default.Person to "nav_tab_tasker")
    )

    Surface(
        color = TaskaGlassWhite,
        tonalElevation = 10.dp,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = BorderStroke(1.dp, TaskaGlassBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { (tab, label, iconAndTag) ->
                val (icon, tag) = iconAndTag
                val isSelected = currentTab == tab
                val badgeCount = when (tab) {
                    TaskaTab.BOOKINGS -> if (isSignedIn) bookingsCount else 0
                    TaskaTab.INBOX -> inboxCount
                    else -> 0
                }

                ModernNavTabButton(
                    label = label,
                    icon = icon,
                    isSelected = isSelected,
                    badgeCount = badgeCount,
                    testTag = tag,
                    onClick = { onSelectTab(tab) }
                )
            }
        }
    }
}

@Composable
private fun ModernNavTabButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    badgeCount: Int,
    testTag: String,
    onClick: () -> Unit
) {
    val pillBg = if (isSelected) {
        Brush.horizontalGradient(
            colors = listOf(TaskaVioletSoft, TaskaVioletSoft.copy(alpha = 0.7f))
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color.Transparent)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(brush = pillBg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(
                        containerColor = TaskaVioletSolid,
                        contentColor = TaskaPureWhite
                    ) {
                        Text(
                            text = badgeCount.coerceAtMost(99).toString(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) TaskaViolet else TaskaMutedText,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) TaskaViolet else TaskaMutedText,
            maxLines = 1
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(TaskaViolet)
            )
        }
    }
}

@Composable
fun TaskaMixedHeadline(
    boldPrefix: String,
    italicSuffix: String,
    fontSize: TextUnit = 34.sp,
    lineHeight: TextUnit = 38.sp,
    textColor: Color = TaskaInk,
    italicColor: Color = textColor,
    modifier: Modifier = Modifier
) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = fontSize,
                    color = textColor,
                    letterSpacing = (-0.8).sp
                )
            ) {
                append(boldPrefix)
            }
            withStyle(
                SpanStyle(
                    fontFamily = PlayfairDisplayFamily,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic,
                    fontSize = (fontSize.value + 2).sp,
                    color = italicColor
                )
            ) {
                append(italicSuffix)
            }
        },
        lineHeight = lineHeight,
        modifier = modifier
    )
}

@Composable
fun TrustStripBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(TaskaDeepInk)
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val items = listOf(
                "NO PHONE NUMBERS SHARED",
                "CHAT-FIRST BOOKING",
                "VERIFIED TASKERS",
                "CUSTOM PIN LOCATIONS"
            )
            items.forEachIndexed { index, label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TaskaPureWhite,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.9.sp
                )
                if (index < items.lastIndex) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(TaskaLime)
                    )
                }
            }
        }
    }
}

@Composable
fun TaskaRatingBreakdownSummary(
    tasker: TaskerEntity,
    modifier: Modifier = Modifier
) {
    val totalRatings = tasker.ratingCount.coerceAtLeast(1)
    val starRows = listOf(
        5 to tasker.fiveStarCount,
        4 to tasker.fourStarCount,
        3 to tasker.threeStarCount,
        2 to tasker.twoStarCount,
        1 to tasker.oneStarCount
    )

    TaskaGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(86.dp)
            ) {
                Text(
                    text = String.format("%.1f", tasker.rating),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TaskaInk,
                    fontSize = 32.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { idx ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (idx < tasker.rating.toInt()) StarAmber else TaskaBorder,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${tasker.ratingCount} reviews",
                    style = MaterialTheme.typography.labelSmall,
                    color = TaskaMutedText
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                starRows.forEach { (star, count) ->
                    val fraction = if (tasker.ratingCount > 0) {
                        (count.toFloat() / totalRatings.toFloat()).coerceIn(0f, 1f)
                    } else if (star == 5) 1f else 0f
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$star★",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TaskaMutedText,
                            modifier = Modifier.width(24.dp)
                        )
                        LinearProgressIndicator(
                            progress = { fraction },
                            color = StarAmber,
                            trackColor = TaskaCreamSecondary,
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$count",
                            style = MaterialTheme.typography.labelSmall,
                            color = TaskaMutedText,
                            modifier = Modifier.width(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskerMatchCard(
    tasker: TaskerEntity,
    onBookPrivately: (TaskerEntity) -> Unit,
    onViewProfile: ((TaskerEntity) -> Unit)? = null,
    onSpeakBio: ((String) -> Unit)? = null,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (avatarBg, avatarText) = when (tasker.avatarColorType) {
        "lavender" -> CategoryLavender to CategoryLavenderIcon
        "mint" -> CategoryMint to CategoryMintIcon
        "butter" -> CategoryButter to CategoryButterIcon
        "sky" -> CategorySky to CategorySkyIcon
        "gray" -> CategoryCoolGray to CategoryCoolGrayIcon
        else -> CategoryPeach to CategoryPeachIcon
    }

    val storePhotosCount = remember(tasker.storePhotosCsv, tasker.servicesListedCsv) {
        val storeCount = tasker.storePhotosCsv.split("||").count { it.isNotBlank() }
        val listingPhotoCount = tasker.servicesListedCsv.split("||").sumOf { token ->
            val parts = token.split("::")
            if (parts.size >= 4) parts[3].split(";").count { it.isNotBlank() } else 0
        }
        storeCount + listingPhotoCount
    }

    val latestReviewSnippet = remember(tasker.reviewsCsv) {
        val firstReview = tasker.reviewsCsv.split("||").firstOrNull { it.isNotBlank() }
        val parts = firstReview?.split("::")
        if (parts != null && parts.size >= 3) {
            "“${parts[2]}” — ${parts[0]}"
        } else null
    }

    TaskaGlassCard(
        cornerRadius = 22.dp,
        contentPadding = PaddingValues(if (compact) 13.dp else 16.dp),
        onClick = if (onViewProfile != null) ({ onViewProfile(tasker) }) else null,
        modifier = modifier
            .fillMaxWidth()
            .testTag("tasker_card_${tasker.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Initials Avatar Circle
            Box(
                modifier = Modifier
                    .size(if (compact) 44.dp else 50.dp)
                    .clip(CircleShape)
                    .background(avatarBg)
                    .border(1.dp, avatarText.copy(alpha = 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tasker.initials,
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = if (compact) 14.sp else 16.sp,
                    color = avatarText
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tasker.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TaskaInk
                    )
                    if (tasker.isVerified) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Tasker",
                            tint = TaskaViolet,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = tasker.specialty,
                    style = MaterialTheme.typography.bodySmall,
                    color = TaskaMutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        color = StarAmber.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                tint = StarAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format("%.1f", tasker.rating),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = TaskaInk
                            )
                            Text(
                                text = " (${tasker.ratingCount})",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = TaskaMutedText
                            )
                        }
                    }

                    Text(
                        text = "· ${tasker.completedTasks} done · ${tasker.priceRange}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = TaskaMutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Available Badge
            Surface(
                color = VerifiedGreenBg,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = if (tasker.isAvailable) "Available" else "Busy",
                    color = VerifiedGreen,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        if (!compact) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = tasker.bio,
                style = MaterialTheme.typography.bodySmall,
                color = TaskaInk.copy(alpha = 0.88f)
            )

            if (latestReviewSnippet != null || storePhotosCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (storePhotosCount > 0) {
                        Surface(
                            color = TaskaVioletSoft,
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = TaskaViolet,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$storePhotosCount photo(s)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = TaskaViolet,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (latestReviewSnippet != null) {
                        Text(
                            text = latestReviewSnippet,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            color = TaskaMutedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = TaskaMutedText,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (tasker.area.isNotBlank()) "${tasker.area} · Private chat" else "Private by default",
                    style = MaterialTheme.typography.labelSmall,
                    color = TaskaMutedText
                )
                if (onSpeakBio != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            onSpeakBio("${tasker.name}. ${tasker.specialty}. Rated ${tasker.rating} stars across ${tasker.ratingCount} reviews. ${tasker.bio}")
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Read Tasker profile aloud",
                            tint = TaskaViolet,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Button(
                onClick = { onBookPrivately(tasker) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaskaVioletSolid,
                    contentColor = TaskaPureWhite
                ),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                modifier = Modifier.testTag("book_tasker_button_${tasker.id}")
            ) {
                Text(
                    text = "Book privately →",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 12.sp,
                    color = TaskaPureWhite
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowItWorksBottomSheet(
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = TaskaCream
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "SIMPLE BY DESIGN",
                style = MaterialTheme.typography.labelMedium,
                color = TaskaViolet
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "From “I need help” to done.",
                style = MaterialTheme.typography.headlineLarge,
                color = TaskaInk
            )
            Spacer(modifier = Modifier.height(16.dp))

            HowItWorksStepCard(
                number = "01",
                title = "Say what you need",
                description = "Message Taska naturally, upload a photo, or use voice. No long forms or confusing category menus.",
                highlightText = "“Need printing by 4 PM.”",
                highlightBg = TaskaVioletSoft,
                highlightTextColor = TaskaViolet
            )
            Spacer(modifier = Modifier.height(10.dp))
            HowItWorksStepCard(
                number = "02",
                title = "Choose your match",
                description = "See suitable Taskers, sellers, or stores with clear ratings, verified reviews, and photo galleries.",
                highlightText = "✓ Verified Taskers · ★ 4.9",
                highlightBg = CategoryPeach,
                highlightTextColor = TaskaInk
            )
            Spacer(modifier = Modifier.height(10.dp))
            HowItWorksStepCard(
                number = "03",
                title = "Keep it private",
                description = "Book and coordinate inside Taska. Your personal phone number stays yours.",
                highlightText = "Your number stays private.",
                highlightBg = TaskaLime,
                highlightTextColor = TaskaDeepInk
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HowItWorksStepCard(
    number: String,
    title: String,
    description: String,
    highlightText: String,
    highlightBg: Color,
    highlightTextColor: Color
) {
    TaskaGlassCard(
        cornerRadius = 18.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.labelMedium,
            color = TaskaViolet
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TaskaInk
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = TaskaMutedText
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(highlightBg)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = highlightText,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 13.sp,
                color = highlightTextColor
            )
        }
    }
}
