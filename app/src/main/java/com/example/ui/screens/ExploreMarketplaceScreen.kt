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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskaCategory
import com.example.data.TaskerEntity
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.TaskerMatchCard
import com.example.ui.theme.StarGold
import com.example.ui.theme.TaskaBorder
import com.example.ui.theme.TaskaCream
import com.example.ui.theme.TaskaDeepInk
import com.example.ui.theme.TaskaGlassBorder
import com.example.ui.theme.TaskaInk
import com.example.ui.theme.TaskaLime
import com.example.ui.theme.TaskaMutedText
import com.example.ui.theme.TaskaSurfaceAlt
import com.example.ui.theme.TaskaViolet
import com.example.ui.theme.TaskaVioletSoft
import com.example.ui.theme.TaskaWhite
import com.example.ui.theme.VerifiedGreen
import com.example.ui.theme.VerifiedGreenBg
import java.util.Locale

@Composable
fun ExploreMarketplaceScreen(
    selectedArea: String,
    selectedCategoryFilter: String?,
    searchQuery: String = "",
    selectedMinRating: Double = 0.0,
    onlyVerifiedFilter: Boolean = false,
    taskers: List<TaskerEntity>,
    fastEstimateText: String?,
    onSelectCategory: (String?) -> Unit,
    onSearchQueryChange: (String) -> Unit = {},
    onSelectMinRating: (Double) -> Unit = {},
    onToggleVerifiedFilter: () -> Unit = {},
    onRequestFastEstimate: (String) -> Unit,
    onBookTasker: (TaskerEntity) -> Unit,
    onOpenTaskerProfile: (TaskerEntity) -> Unit = {},
    onOpenRequestDemand: () -> Unit = {},
    onOpenLocationSetup: () -> Unit = {},
    onSpeakBio: (String) -> Unit,
    onAskInChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cleanAreaLabel = selectedArea.ifBlank { "your area" }
    val queryLower = searchQuery.trim().lowercase()

    val filteredTaskers = taskers.filter { tasker ->
        val matchesCategory = selectedCategoryFilter == null || tasker.categoryId == selectedCategoryFilter
        val matchesRating = tasker.rating >= selectedMinRating
        val matchesVerified = !onlyVerifiedFilter || tasker.isVerified
        val matchesSearch = if (queryLower.isBlank()) {
            true
        } else {
            val catTitle = TaskaCategory.entries.firstOrNull { it.id == tasker.categoryId }?.title?.lowercase().orEmpty()
            tasker.name.lowercase().contains(queryLower) ||
                tasker.specialty.lowercase().contains(queryLower) ||
                tasker.skillsCsv.lowercase().contains(queryLower) ||
                tasker.bio.lowercase().contains(queryLower) ||
                tasker.area.lowercase().contains(queryLower) ||
                tasker.roleType.lowercase().contains(queryLower) ||
                catTitle.contains(queryLower)
        }
        matchesCategory && matchesRating && matchesVerified && matchesSearch
    }

    val avgMarketplaceRating = if (filteredTaskers.isNotEmpty()) {
        filteredTaskers.map { it.rating }.average()
    } else {
        4.9
    }
    val totalMarketplaceReviews = filteredTaskers.sumOf { it.ratingCount }

    val gridCategories = listOf(
        TaskaCategory.PRINTING_SUPPLIES,
        TaskaCategory.DEVICE_REPAIRS,
        TaskaCategory.TUTORING,
        TaskaCategory.MOVING,
        TaskaCategory.ERRANDS_MOVING,
        TaskaCategory.LOCAL_STORES,
        TaskaCategory.CARS_AUTO,
        TaskaCategory.HOMES_ROOMS,
        TaskaCategory.PHONES_GADGETS,
        TaskaCategory.FOOD_NEARBY,
        TaskaCategory.PHARMACIES,
        TaskaCategory.HOME_SERVICES
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TaskaCream),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header + Location chip
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explore",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TaskaInk
                    )
                    Surface(
                        color = if (selectedArea.isBlank()) TaskaVioletSoft else TaskaSurfaceAlt,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, if (selectedArea.isBlank()) TaskaViolet.copy(alpha = 0.4f) else TaskaBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onOpenLocationSetup() }
                            .testTag("explore_location_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = TaskaViolet,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (selectedArea.isBlank()) "Set location" else selectedArea,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedArea.isBlank()) TaskaViolet else TaskaInk
                            )
                        }
                    }
                }
                Text(
                    text = "Discover verified Taskers, stores, and ratings near $cleanAreaLabel.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TaskaMutedText
                )
            }
        }

        // Search Bar + Rating & Review Filter Strip
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "Search tasks, Taskers, skills, or categories...",
                                color = TaskaMutedText,
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Taskers",
                                tint = TaskaViolet
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = TaskaMutedText
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TaskaViolet,
                            unfocusedBorderColor = TaskaBorder,
                            focusedContainerColor = TaskaSurfaceAlt.copy(alpha = 0.6f),
                            unfocusedContainerColor = TaskaSurfaceAlt.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("explore_search_input")
                    )

                    // Rating & Verification Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = selectedMinRating == 0.0 && !onlyVerifiedFilter,
                            onClick = {
                                onSelectMinRating(0.0)
                                if (onlyVerifiedFilter) onToggleVerifiedFilter()
                            },
                            label = { Text("All Ratings", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TaskaViolet,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_rating_all")
                        )
                        FilterChip(
                            selected = selectedMinRating == 4.5,
                            onClick = {
                                onSelectMinRating(if (selectedMinRating == 4.5) 0.0 else 4.5)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (selectedMinRating == 4.5) Color.White else StarGold,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            label = { Text("4.5+ ★", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TaskaViolet,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_rating_45")
                        )
                        FilterChip(
                            selected = selectedMinRating == 4.8,
                            onClick = {
                                onSelectMinRating(if (selectedMinRating == 4.8) 0.0 else 4.8)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (selectedMinRating == 4.8) Color.White else StarGold,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            label = { Text("Top Rated 4.8+ ★", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TaskaViolet,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_rating_48")
                        )
                        FilterChip(
                            selected = onlyVerifiedFilter,
                            onClick = onToggleVerifiedFilter,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = if (onlyVerifiedFilter) Color.White else VerifiedGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            label = { Text("Verified ID Only", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VerifiedGreen,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_verified_only")
                        )
                    }
                }
            }
        }

        // 3x4 Category Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                gridCategories.chunked(3).forEach { rowCats ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowCats.forEach { category ->
                            ExploreCategoryTile(
                                category = category,
                                isSelected = selectedCategoryFilter == category.id,
                                onClick = {
                                    val next = if (selectedCategoryFilter == category.id) null else category.id
                                    onSelectCategory(next)
                                    onRequestFastEstimate(category.title)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Instant Rate & Availability Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = TaskaDeepInk),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TaskaGlassBorder, RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(TaskaLime),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = TaskaDeepInk,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instant Rate & Turnaround Check",
                                style = MaterialTheme.typography.labelLarge,
                                color = TaskaLime,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                val topic = TaskaCategory.entries.firstOrNull { it.id == selectedCategoryFilter }?.title
                                    ?: "Device repairs & printing in $cleanAreaLabel"
                                onRequestFastEstimate(topic)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TaskaViolet,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("fast_estimate_button")
                        ) {
                            Text("Check rates", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = fastEstimateText
                            ?: "Tap any category card above or 'Check rates' for an instant ZMW / USD price & turnaround estimate in $cleanAreaLabel.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Marketplace Rating & Review Metrics Summary Strip
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 18.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(StarGold.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = StarGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", avgMarketplaceRating)} ★ Average Rating",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TaskaInk
                            )
                            Text(
                                text = "$totalMarketplaceReviews verified client reviews across ${filteredTaskers.size} providers",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = TaskaMutedText
                            )
                        }
                    }
                    Surface(
                        color = VerifiedGreenBg,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Verified Reviews",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = VerifiedGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // Available Taskers Header + Filter Reset
        item {
            val selectedCategoryObj = TaskaCategory.entries.firstOrNull { it.id == selectedCategoryFilter }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedCategoryObj != null) {
                        "${selectedCategoryObj.title} (${filteredTaskers.size})"
                    } else if (searchQuery.isNotBlank()) {
                        "Search Results (${filteredTaskers.size})"
                    } else {
                        "Verified Taskers (${filteredTaskers.size})"
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = TaskaInk
                )
                if (selectedCategoryFilter != null || searchQuery.isNotBlank() || selectedMinRating > 0.0 || onlyVerifiedFilter) {
                    Text(
                        text = "Reset filters",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TaskaViolet,
                        modifier = Modifier
                            .clickable {
                                onSelectCategory(null)
                                onSearchQueryChange("")
                                onSelectMinRating(0.0)
                                if (onlyVerifiedFilter) onToggleVerifiedFilter()
                            }
                            .padding(6.dp)
                    )
                }
            }
        }

        if (filteredTaskers.isEmpty()) {
            item {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 22.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "No Taskers or listings matched \"$searchQuery\"."
                            } else {
                                "No listings are live in this category for $cleanAreaLabel yet."
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TaskaInk,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Try adjusting your search or request this service so nearby verified providers can respond.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText,
                            textAlign = TextAlign.Center
                        )
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
                            Text("Request this service", color = TaskaViolet, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredTaskers, key = { it.id }) { tasker ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskerMatchCard(
                        tasker = tasker,
                        onBookPrivately = onBookTasker,
                        onSpeakBio = onSpeakBio,
                        onViewProfile = onOpenTaskerProfile
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", tasker.rating)} ★ (${tasker.ratingCount} reviews) · ${tasker.completedTasks} jobs completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = TaskaMutedText
                        )
                        Text(
                            text = "View profile, photos & reviews →",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TaskaViolet,
                            modifier = Modifier
                                .clickable { onOpenTaskerProfile(tasker) }
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Request Missing Service Footer Card
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
                            text = "Can't find what you need?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TaskaInk
                        )
                        Text(
                            text = "Post a custom service request in $cleanAreaLabel and verified Taskers will send quotes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskaMutedText
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onOpenRequestDemand,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, TaskaViolet),
                        modifier = Modifier.testTag("explore_request_service_button")
                    ) {
                        Text("Request", color = TaskaViolet, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreCategoryTile(
    category: TaskaCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (category) {
        TaskaCategory.PRINTING_SUPPLIES -> Icons.Default.Print
        TaskaCategory.DEVICE_REPAIRS -> Icons.Default.LaptopMac
        TaskaCategory.TUTORING -> Icons.Default.School
        TaskaCategory.MOVING -> Icons.Default.LocalShipping
        TaskaCategory.ERRANDS_MOVING -> Icons.Default.ShoppingBag
        TaskaCategory.LOCAL_STORES -> Icons.Default.Storefront
        TaskaCategory.CARS_AUTO -> Icons.Default.DirectionsCar
        TaskaCategory.HOMES_ROOMS -> Icons.Default.HomeWork
        TaskaCategory.PHONES_GADGETS -> Icons.Default.PhoneAndroid
        TaskaCategory.FOOD_NEARBY -> Icons.Default.Fastfood
        TaskaCategory.PHARMACIES -> Icons.Default.MedicalServices
        TaskaCategory.HOME_SERVICES -> Icons.Default.Handyman
        TaskaCategory.DIGITAL_SERVICES -> Icons.Default.Code
    }

    val containerBg = if (isSelected) TaskaViolet else TaskaWhite
    val contentFg = if (isSelected) Color.White else TaskaInk
    val iconTint = if (isSelected) TaskaLime else TaskaViolet

    Surface(
        color = containerBg,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) TaskaViolet else TaskaGlassBorder
        ),
        modifier = modifier
            .height(94.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("category_card_${category.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = category.title,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.title,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = contentFg,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 14.sp
            )
        }
    }
}
