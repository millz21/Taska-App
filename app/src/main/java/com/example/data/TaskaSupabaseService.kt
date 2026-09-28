package com.example.data

import com.example.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

data class SupabaseAuthSession(
    val userId: String,
    val email: String,
    val displayName: String,
    val accessToken: String,
    val refreshToken: String = "",
    val age: String = "",
    val gender: String = "",
    val bio: String = "",
    val defaultArea: String = "",
    val hasEarningProfile: Boolean = false,
    val storePhotosCsv: String = ""
)

data class ParsedTaskaIntent(
    val category: String,
    val summary: String,
    val reply: String
)

data class SupabaseSyncReport(
    val isConnected: Boolean,
    val syncedTaskersCount: Int,
    val syncedAnnouncementsCount: Int,
    val syncedCredits: Int?,
    val statusSummary: String,
    val timestampMs: Long = System.currentTimeMillis()
)

class TaskaSupabaseService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    val supabaseUrl: String
        get() = BuildConfig.SUPABASE_URL.trim().removeSuffix("/")

    val supabaseKey: String
        get() {
            val pubKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY.trim()
            val anonKey = BuildConfig.SUPABASE_ANON_KEY.trim()
            return when {
                pubKey.isNotEmpty() && pubKey != "MY_SUPABASE_PUBLISHABLE_KEY" -> pubKey
                anonKey.isNotEmpty() && anonKey != "MY_SUPABASE_ANON_KEY" -> anonKey
                else -> ""
            }
        }

    val supabasePublishableKey: String
        get() = supabaseKey

    val isConfigured: Boolean
        get() = supabaseUrl.isNotEmpty() &&
            supabaseKey.isNotEmpty() &&
            supabaseUrl != "MY_SUPABASE_URL" &&
            supabaseUrl.startsWith("http")

    // Official Supabase Kotlin SDK client instance (lazy-initialized when configured)
    val sdkClient: SupabaseClient? by lazy {
        if (!isConfigured) {
            null
        } else {
            runCatching {
                createSupabaseClient(
                    supabaseUrl = supabaseUrl,
                    supabaseKey = supabaseKey
                ) {
                    install(Auth) {
                        alwaysAutoRefresh = false
                        autoLoadFromStorage = false
                    }
                    install(Postgrest)
                    install(Functions)
                    install(Storage)
                }
            }.getOrNull()
        }
    }

    // Holds the active user's JWT access token for authenticated PostgREST / RPC requests
    @Volatile
    var currentAccessToken: String? = null

    private fun bearerToken(overrideToken: String? = null): String {
        val token = overrideToken ?: currentAccessToken
        return if (!token.isNullOrBlank()) token else supabaseKey
    }

    fun friendlyError(error: Throwable, action: String = "complete that request"): String {
        val raw = (error.message ?: error.toString()).lowercase()
        if (error is UnknownHostException ||
            error is IOException ||
            raw.contains("failed host lookup") ||
            raw.contains("socketexception") ||
            raw.contains("network")
        ) {
            return "You appear to be offline. Check your internet connection and try again."
        }
        if (raw.contains("invalid login credentials")) {
            return "That email or password is not correct. Try again or create an account."
        }
        if (raw.contains("email not confirmed")) {
            return "Confirm your email first, then return to sign in. Check inbox and spam for the Supabase email."
        }
        if (raw.contains("user already registered") || raw.contains("already exists")) {
            return "An account with this email is already registered on Supabase. Try signing in instead."
        }
        if (raw.contains("rate limit") || raw.contains("too many")) {
            return "Please wait a moment before trying again."
        }
        return "We could not $action right now. Please try again, or contact Taska Support if it continues."
    }

    // --- 1. Supabase Auth (GoTrue + Profiles Table) ---

    suspend fun signInWithPassword(
        email: String,
        password: String
    ): Result<SupabaseAuthSession> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalStateException("Supabase not configured"))
        }
        try {
            val payload = buildJsonObject {
                put("email", email.trim())
                put("password", password)
            }.toString()

            val req = Request.Builder()
                .url("$supabaseUrl/auth/v1/token?grant_type=password")
                .addHeader("apikey", supabaseKey)
                .addHeader("Content-Type", "application/json")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(req).execute().use { resp ->
                val bodyStr = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    return@withContext Result.failure(
                        IllegalStateException(bodyStr.ifBlank { "invalid login credentials" })
                    )
                }
                val root = json.parseToJsonElement(bodyStr).jsonObject
                val accessToken = root["access_token"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val refreshToken = root["refresh_token"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val userObj = root["user"]?.jsonObject
                val userId = userObj?.get("id")?.jsonPrimitive?.contentOrNull.orEmpty()
                val userEmail = userObj?.get("email")?.jsonPrimitive?.contentOrNull ?: email.trim()
                val metaObj = userObj?.get("user_metadata")?.jsonObject
                val metaName = metaObj?.get("display_name")?.jsonPrimitive?.contentOrNull
                    ?: userEmail.substringBefore("@")
                val metaAge = metaObj?.get("age")?.jsonPrimitive?.contentOrNull.orEmpty()
                val metaGender = metaObj?.get("gender")?.jsonPrimitive?.contentOrNull.orEmpty()
                val metaBio = metaObj?.get("bio")?.jsonPrimitive?.contentOrNull.orEmpty()
                val metaArea = metaObj?.get("default_area")?.jsonPrimitive?.contentOrNull.orEmpty()
                val metaEarning = metaObj?.get("has_earning_profile")?.jsonPrimitive?.booleanOrNull ?: false
                val metaStorePhotos = metaObj?.get("store_photos_csv")?.jsonPrimitive?.contentOrNull.orEmpty()

                currentAccessToken = accessToken

                // Sync with `profiles` table
                val profileName = fetchOrUpsertProfileDisplayName(
                    userId = userId,
                    fallbackName = metaName,
                    accessToken = accessToken
                )

                Result.success(
                    SupabaseAuthSession(
                        userId = userId,
                        email = userEmail,
                        displayName = profileName,
                        accessToken = accessToken,
                        refreshToken = refreshToken,
                        age = metaAge,
                        gender = metaGender,
                        bio = metaBio,
                        defaultArea = if (metaArea.equals("UNZA Campus Hub", ignoreCase = true)) "" else metaArea,
                        hasEarningProfile = metaEarning,
                        storePhotosCsv = metaStorePhotos
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
        age: String = "",
        gender: String = "",
        defaultArea: String = ""
    ): Result<SupabaseAuthSession?> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalStateException("Supabase not configured"))
        }
        try {
            val payload = buildJsonObject {
                put("email", email.trim())
                put("password", password)
                put(
                    "data",
                    buildJsonObject {
                        put("display_name", displayName.trim())
                        if (age.isNotBlank()) put("age", age.trim())
                        if (gender.isNotBlank()) put("gender", gender.trim())
                        if (defaultArea.isNotBlank()) put("default_area", defaultArea.trim())
                    }
                )
            }.toString()

            val req = Request.Builder()
                .url("$supabaseUrl/auth/v1/signup")
                .addHeader("apikey", supabaseKey)
                .addHeader("Content-Type", "application/json")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(req).execute().use { resp ->
                val bodyStr = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) {
                    return@withContext Result.failure(IllegalStateException(bodyStr))
                }
                val root = json.parseToJsonElement(bodyStr).jsonObject
                val accessToken = root["access_token"]?.jsonPrimitive?.contentOrNull
                val refreshToken = root["refresh_token"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val userObj = root["user"]?.jsonObject ?: root
                val userId = userObj["id"]?.jsonPrimitive?.contentOrNull.orEmpty()

                if (accessToken.isNullOrBlank()) {
                    // Email confirmation is enabled on the Supabase project
                    Result.success(null)
                } else {
                    currentAccessToken = accessToken
                    upsertProfileRow(
                        userId = userId,
                        displayName = displayName.trim(),
                        accessToken = accessToken,
                        age = age.trim(),
                        gender = gender.trim(),
                        defaultArea = defaultArea.trim()
                    )
                    Result.success(
                        SupabaseAuthSession(
                            userId = userId,
                            email = email.trim(),
                            displayName = displayName.trim(),
                            accessToken = accessToken,
                            refreshToken = refreshToken,
                            age = age.trim(),
                            gender = gender.trim(),
                            defaultArea = defaultArea.trim()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isConfigured) {
            return@withContext Result.failure(IllegalStateException("Supabase not configured"))
        }
        try {
            val payload = buildJsonObject {
                put("email", email.trim())
            }.toString()

            val req = Request.Builder()
                .url("$supabaseUrl/auth/v1/recover")
                .addHeader("apikey", supabaseKey)
                .addHeader("Content-Type", "application/json")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    val bodyStr = resp.body?.string().orEmpty()
                    return@withContext Result.failure(IllegalStateException(bodyStr))
                }
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsertProfileRow(
        userId: String,
        displayName: String,
        accessToken: String? = null,
        age: String = "",
        gender: String = "",
        bio: String = "",
        defaultArea: String = "",
        hasEarningProfile: Boolean = false,
        storePhotosCsv: String = ""
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured || userId.isBlank()) return@withContext false
        try {
            // 1. Update auth.users user_metadata via PUT /auth/v1/user so all extended fields (age, gender, bio, area, store photos) persist even if profiles table only has base columns
            val tokenToUse = accessToken ?: currentAccessToken
            if (!tokenToUse.isNullOrBlank()) {
                val userMetaPayload = buildJsonObject {
                    put(
                        "data",
                        buildJsonObject {
                            put("display_name", displayName.trim())
                            put("age", age.trim())
                            put("gender", gender.trim())
                            put("bio", bio.trim())
                            put("default_area", defaultArea.trim())
                            put("has_earning_profile", hasEarningProfile)
                            put("store_photos_csv", storePhotosCsv)
                        }
                    )
                }.toString()

                val authUpdateReq = Request.Builder()
                    .url("$supabaseUrl/auth/v1/user")
                    .addHeader("apikey", supabaseKey)
                    .addHeader("Authorization", "Bearer $tokenToUse")
                    .addHeader("Content-Type", "application/json")
                    .put(userMetaPayload.toRequestBody(jsonMediaType))
                    .build()
                runCatching { httpClient.newCall(authUpdateReq).execute().close() }
            }

            // 2. Upsert into public.profiles
            val fullPayload = buildJsonObject {
                put("id", userId)
                put("display_name", displayName.trim())
                if (defaultArea.isNotBlank()) put("default_area", defaultArea.trim())
                if (age.isNotBlank()) put("age", age.trim())
                if (gender.isNotBlank()) put("gender", gender.trim())
                if (bio.isNotBlank()) put("bio", bio.trim())
            }.toString()

            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/profiles")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer ${bearerToken(accessToken)}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
                .post(fullPayload.toRequestBody(jsonMediaType))
                .build()

            val fullSuccess = httpClient.newCall(req).execute().use { resp ->
                resp.isSuccessful
            }
            if (fullSuccess) return@withContext true

            // Fallback if public.profiles hasn't migrated age/gender columns yet
            val basePayload = buildJsonObject {
                put("id", userId)
                put("display_name", displayName.trim())
            }.toString()

            val baseReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/profiles")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer ${bearerToken(accessToken)}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
                .post(basePayload.toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(baseReq).execute().use { resp ->
                resp.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun fetchOrUpsertProfileDisplayName(
        userId: String,
        fallbackName: String,
        accessToken: String
    ): String = withContext(Dispatchers.IO) {
        if (!isConfigured || userId.isBlank()) return@withContext fallbackName
        try {
            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/profiles?select=display_name&id=eq.$userId&limit=1")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer ${bearerToken(accessToken)}")
                .get()
                .build()

            httpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val arr = json.parseToJsonElement(resp.body?.string().orEmpty()).jsonArray
                    val existingName = arr.firstOrNull()?.jsonObject
                        ?.get("display_name")?.jsonPrimitive?.contentOrNull
                    if (!existingName.isNullOrBlank()) {
                        return@withContext existingName
                    }
                }
            }
            upsertProfileRow(userId, fallbackName, accessToken)
            fallbackName
        } catch (_: Exception) {
            fallbackName
        }
    }

    // --- 2. Edge Function: `parse-taska-intent` ---

    suspend fun invokeParseTaskaIntent(
        text: String,
        guestClientKey: String
    ): ParsedTaskaIntent? = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext null
        try {
            val payload = buildJsonObject {
                put("text", text.trim())
            }.toString()

            val req = Request.Builder()
                .url("$supabaseUrl/functions/v1/parse-taska-intent")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer ${bearerToken()}")
                .addHeader("x-taska-client-key", guestClientKey)
                .addHeader("Content-Type", "application/json")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val bodyStr = resp.body?.string().orEmpty()
                val root = json.parseToJsonElement(bodyStr).jsonObject
                val intent = root["intent"]?.jsonObject ?: return@withContext null
                ParsedTaskaIntent(
                    category = intent["category"]?.jsonPrimitive?.contentOrNull ?: "local help",
                    summary = intent["summary"]?.jsonPrimitive?.contentOrNull ?: text,
                    reply = intent["reply"]?.jsonPrimitive?.contentOrNull.orEmpty()
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    // --- 3. Provider Profiles & Service Listings (`provider_profiles` & `service_listings`) ---

    suspend fun fetchProvidersFromSupabase(): List<TaskerEntity> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext emptyList()
        try {
            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/provider_profiles?select=id,kind,display_name,bio,is_online_service,business_name,verification_status,rating_avg,rating_count,completed_tasks_count,broad_area_label&limit=50")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer ${bearerToken()}")
                .get()
                .build()

            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext emptyList()
                val arr = json.parseToJsonElement(resp.body?.string().orEmpty()).jsonArray
                arr.mapIndexedNotNull { idx, el ->
                    val obj = el.jsonObject
                    val displayName = obj["display_name"]?.jsonPrimitive?.contentOrNull ?: return@mapIndexedNotNull null
                    val kind = obj["kind"]?.jsonPrimitive?.contentOrNull ?: "service_tasker"
                    val bio = obj["bio"]?.jsonPrimitive?.contentOrNull ?: "Verified local provider."
                    val rawArea = obj["broad_area_label"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    val area = if (rawArea.equals("UNZA Campus Hub", ignoreCase = true) || rawArea.isBlank()) "Local Area" else rawArea
                    val ratingAvg = obj["rating_avg"]?.jsonPrimitive?.doubleOrNull ?: 4.9
                    val ratingCount = obj["rating_count"]?.jsonPrimitive?.intOrNull ?: 12
                    val completedCount = obj["completed_tasks_count"]?.jsonPrimitive?.intOrNull ?: 15
                    val verStatus = obj["verification_status"]?.jsonPrimitive?.contentOrNull ?: "verified"

                    val words = displayName.trim().split(" ").filter { it.isNotBlank() }
                    val initials = if (words.size >= 2) {
                        "${words[0].first().uppercaseChar()}${words[1].first().uppercaseChar()}"
                    } else {
                        displayName.take(2).uppercase()
                    }

                    val catId = when (kind) {
                        "taska_seller" -> TaskaCategory.LOCAL_STORES.id
                        "digital_tasker" -> TaskaCategory.DIGITAL_SERVICES.id
                        "business" -> TaskaCategory.FOOD_NEARBY.id
                        else -> TaskaCategory.DEVICE_REPAIRS.id
                    }

                    TaskerEntity(
                        id = 500 + idx,
                        name = displayName,
                        initials = initials,
                        roleType = kind,
                        categoryId = catId,
                        specialty = "Verified · ${bio.take(36)}",
                        rating = ratingAvg,
                        ratingCount = ratingCount,
                        fiveStarCount = (ratingCount * 0.85).toInt().coerceAtLeast(1),
                        fourStarCount = (ratingCount * 0.15).toInt(),
                        completedTasks = completedCount,
                        priceRange = "ZMW 120 – 450",
                        area = area,
                        isVerified = verStatus == "verified" || verStatus == "approved",
                        isAvailable = true,
                        avatarColorType = "lavender",
                        bio = bio,
                        skillsCsv = "Verified Tasker,${kind.replace('_', ' ')}",
                        servicesListedCsv = "$displayName Service::General::ZMW 180::Professional service::1",
                        reviewsCsv = "5::Fast, reliable, and professional service!::Verified Client::Recently"
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun saveEarningProfileOnSupabase(
        accessToken: String?,
        userId: String?,
        kind: String,
        displayName: String,
        bio: String,
        phoneE164: String,
        businessName: String?,
        businessRegNumber: String?,
        broadArea: String? = null,
        firstListingTitle: String? = null,
        firstListingCategorySlug: String? = null,
        firstListingPriceZmw: String? = null
    ): String? = withContext(Dispatchers.IO) {
        if (!isConfigured || userId.isNullOrBlank()) return@withContext null
        try {
            val payload = buildJsonObject {
                put("owner_id", userId)
                put("kind", kind)
                put("display_name", displayName.trim())
                if (bio.isNotBlank()) put("bio", bio.trim())
                put("is_online_service", kind == "digital_tasker")
                if (!businessName.isNullOrBlank()) put("business_name", businessName.trim())
                if (!businessRegNumber.isNullOrBlank()) put("business_registration_number", businessRegNumber.trim())
                put("phone_note", phoneE164.trim())
                if (!broadArea.isNullOrBlank()) put("broad_area_label", broadArea.trim())
            }.toString()

            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/provider_profiles")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer ${bearerToken(accessToken)}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            var providerId: String? = null
            httpClient.newCall(req).execute().use { resp ->
                if (resp.isSuccessful) {
                    val bodyStr = resp.body?.string().orEmpty()
                    val arr = runCatching { json.parseToJsonElement(bodyStr).jsonArray }.getOrNull()
                    providerId = arr?.firstOrNull()?.jsonObject?.get("id")?.jsonPrimitive?.contentOrNull
                }
            }

            if (!providerId.isNullOrBlank() && !firstListingTitle.isNullOrBlank()) {
                insertServiceListingOnSupabase(
                    providerId = providerId!!,
                    categorySlug = firstListingCategorySlug ?: "repairs",
                    title = firstListingTitle,
                    description = bio,
                    priceZmw = firstListingPriceZmw?.toDoubleOrNull(),
                    accessToken = accessToken
                )
            }
            providerId
        } catch (_: Exception) {
            null
        }
    }

    suspend fun insertServiceListingOnSupabase(
        providerId: String,
        categorySlug: String,
        title: String,
        description: String,
        priceZmw: Double?,
        accessToken: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured || providerId.isBlank()) return@withContext false
        try {
            val payload = buildJsonObject {
                put("provider_id", providerId)
                put("category_slug", categorySlug)
                put("title", title.trim())
                if (description.isNotBlank()) put("description", description.trim())
                if (priceZmw != null) put("price_zmw", priceZmw)
                put("is_active", true)
            }.toString()

            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/service_listings")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer ${bearerToken(accessToken)}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(req).execute().use { resp ->
                resp.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }

    // --- 4. Identity Verification (`provider_verification_submissions`) ---

    suspend fun submitVerificationToSupabase(
        userId: String,
        providerId: String?,
        legalFullName: String,
        nationalIdNumber: String,
        hasIdPhoto: Boolean,
        hasSelfiePhoto: Boolean,
        accessToken: String? = null
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured || userId.isBlank() || legalFullName.isBlank()) return@withContext false
        try {
            val payload = buildJsonObject {
                put("user_id", userId)
                if (!providerId.isNullOrBlank()) put("provider_id", providerId)
                put("legal_full_name", legalFullName.trim())
                put("national_id_number", nationalIdNumber.trim())
                put("id_document_path", if (hasIdPhoto) "$userId/id_document.jpg" else "")
                put("selfie_with_id_path", if (hasSelfiePhoto) "$userId/selfie_with_id.jpg" else "")
                put("status", "pending_review")
            }.toString()

            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/provider_verification_submissions")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer ${bearerToken(accessToken)}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(req).execute().use { resp ->
                resp.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }

    // --- 5. Service Requests, Task Offers, Completion PIN RPCs & Reviews ---

    suspend fun fetchOpenServiceRequestsFromSupabase(): List<MarketplaceRequestEntity> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext emptyList()
        try {
            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/service_requests?select=id,short_title,request_text,broad_area,category_hint,client_budget_zmw,budget_negotiable,status,created_at&order=created_at.desc&limit=40")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer ${bearerToken()}")
                .get()
                .build()

            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext emptyList()
                val arr = json.parseToJsonElement(resp.body?.string().orEmpty()).jsonArray
                arr.mapIndexedNotNull { idx, el ->
                    val obj: JsonObject = el.jsonObject
                    val requestText = obj["request_text"]?.jsonPrimitive?.contentOrNull ?: return@mapIndexedNotNull null
                    val shortTitle = obj["short_title"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
                        ?: requestText.take(48)
                    val rawArea = obj["broad_area"]?.jsonPrimitive?.contentOrNull.orEmpty()
                    val area = if (rawArea.equals("UNZA Campus Hub", ignoreCase = true) || rawArea.isBlank()) "Local Area" else rawArea
                    val categoryHint = obj["category_hint"]?.jsonPrimitive?.contentOrNull ?: TaskaCategory.DEVICE_REPAIRS.id
                    val budgetZmw = obj["client_budget_zmw"]?.jsonPrimitive?.doubleOrNull
                    val budgetNegotiable = obj["budget_negotiable"]?.jsonPrimitive?.booleanOrNull ?: true
                    val status = obj["status"]?.jsonPrimitive?.contentOrNull ?: "Open"
                    val createdAtStr = obj["created_at"]?.jsonPrimitive?.contentOrNull ?: ""

                    MarketplaceRequestEntity(
                        id = 1000 + idx,
                        title = shortTitle,
                        categoryId = categoryHint,
                        area = area,
                        timing = if (createdAtStr.isNotBlank()) "Posted ${createdAtStr.take(10)}" else "Flexible",
                        budget = if (budgetZmw != null && budgetZmw > 0) "ZMW ${budgetZmw.toInt()}" else "Open to offers",
                        budgetNegotiable = budgetNegotiable,
                        details = requestText,
                        clientAlias = "Verified Taska Client",
                        status = status.replaceFirstChar { it.uppercase() }
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun submitServiceDemandToSupabase(
        requestText: String,
        broadArea: String?,
        requesterId: String? = null,
        shortTitle: String? = null,
        clientBudgetZmw: Double? = null,
        budgetNegotiable: Boolean = true
    ): String? = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext null
        try {
            val payload = buildJsonObject {
                put("request_text", requestText.trim())
                if (!shortTitle.isNullOrBlank()) put("short_title", shortTitle.trim())
                if (!broadArea.isNullOrBlank()) put("broad_area", broadArea.trim())
                if (!requesterId.isNullOrBlank() && requesterId.contains("-")) {
                    put("requester_id", requesterId)
                }
                if (clientBudgetZmw != null) put("client_budget_zmw", clientBudgetZmw)
                put("budget_negotiable", budgetNegotiable)
            }.toString()

            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/service_requests")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer ${bearerToken()}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val bodyStr = resp.body?.string().orEmpty()
                val arr = runCatching { json.parseToJsonElement(bodyStr).jsonArray }.getOrNull()
                arr?.firstOrNull()?.jsonObject?.get("id")?.jsonPrimitive?.contentOrNull ?: "synced"
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun invokeIssueCompletionPinRpc(requestId: String): String? = withContext(Dispatchers.IO) {
        if (!isConfigured || requestId.isBlank()) return@withContext null
        try {
            val payload = buildJsonObject {
                put("p_request_id", requestId)
            }.toString()

            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/rpc/issue_task_completion_pin")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer ${bearerToken()}")
                .addHeader("Content-Type", "application/json")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val bodyStr = resp.body?.string().orEmpty()
                val obj = runCatching { json.parseToJsonElement(bodyStr).jsonObject }.getOrNull()
                obj?.get("pin")?.jsonPrimitive?.contentOrNull
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun submitTaskReviewToSupabase(
        requestId: String?,
        providerName: String,
        stars: Int,
        comment: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext false
        try {
            val payload = buildJsonObject {
                if (!requestId.isNullOrBlank() && requestId.contains("-")) {
                    put("request_id", requestId)
                }
                put("stars", stars)
                put("comment", comment.ifBlank { "Rated $stars stars for $providerName" })
            }.toString()

            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/task_reviews")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer ${bearerToken()}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(req).execute().use { resp ->
                resp.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }

    // --- 5B. Real-Time Chat Threads & Messages (`chat_threads` & `chat_messages`) ---

    suspend fun getOrCreateChatThread(
        requestId: String?,
        requesterId: String?,
        providerId: String? = null
    ): String? = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext null
        try {
            if (!requestId.isNullOrBlank() && requestId.contains("-")) {
                val findReq = Request.Builder()
                    .url("$supabaseUrl/rest/v1/chat_threads?select=id&request_id=eq.$requestId&limit=1")
                    .addHeader("apikey", supabaseKey)
                    .addHeader("Authorization", "Bearer ${bearerToken()}")
                    .get()
                    .build()

                val existingId = httpClient.newCall(findReq).execute().use { resp ->
                    if (!resp.isSuccessful) null
                    else {
                        val arr = runCatching { json.parseToJsonElement(resp.body?.string().orEmpty()).jsonArray }.getOrNull()
                        arr?.firstOrNull()?.jsonObject?.get("id")?.jsonPrimitive?.contentOrNull
                    }
                }
                if (!existingId.isNullOrBlank()) return@withContext existingId
            }

            val payload = buildJsonObject {
                if (!requestId.isNullOrBlank() && requestId.contains("-")) {
                    put("request_id", requestId)
                }
                if (!requesterId.isNullOrBlank() && requesterId.contains("-")) {
                    put("requester_id", requesterId)
                }
                if (!providerId.isNullOrBlank() && providerId.contains("-")) {
                    put("provider_id", providerId)
                }
                put("status", "open")
            }.toString()

            val createReq = Request.Builder()
                .url("$supabaseUrl/rest/v1/chat_threads")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer ${bearerToken()}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(createReq).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val arr = runCatching { json.parseToJsonElement(resp.body?.string().orEmpty()).jsonArray }.getOrNull()
                arr?.firstOrNull()?.jsonObject?.get("id")?.jsonPrimitive?.contentOrNull
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun fetchChatMessagesForThread(
        threadId: String,
        currentUserId: String? = null,
        counterpartyName: String = "Verified Tasker"
    ): List<SupabaseChatMessage> = withContext(Dispatchers.IO) {
        if (!isConfigured || threadId.isBlank() || !threadId.contains("-")) return@withContext emptyList()
        try {
            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/chat_messages?select=id,thread_id,sender_id,body,created_at&thread_id=eq.$threadId&order=created_at.asc&limit=100")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer ${bearerToken()}")
                .get()
                .build()

            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext emptyList()
                val arr = json.parseToJsonElement(resp.body?.string().orEmpty()).jsonArray
                arr.mapNotNull { el ->
                    val obj = el.jsonObject
                    val id = obj["id"]?.jsonPrimitive?.contentOrNull ?: ""
                    val senderId = obj["sender_id"]?.jsonPrimitive?.contentOrNull ?: ""
                    val body = obj["body"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                    val createdAtRaw = obj["created_at"]?.jsonPrimitive?.contentOrNull ?: ""
                    val timeLabel = if (createdAtRaw.length >= 16) createdAtRaw.substring(11, 16) else "Live"
                    val isMe = currentUserId.isNullOrBlank() || senderId.isBlank() || senderId == currentUserId
                    SupabaseChatMessage(
                        id = id,
                        threadId = threadId,
                        senderId = senderId,
                        senderLabel = if (isMe) "You" else counterpartyName,
                        body = body,
                        createdAt = timeLabel,
                        isFromCurrentUser = isMe
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun sendChatMessageToSupabase(
        threadId: String,
        senderId: String?,
        body: String
    ): SupabaseChatMessage? = withContext(Dispatchers.IO) {
        if (!isConfigured || threadId.isBlank() || !threadId.contains("-") || body.isBlank()) return@withContext null
        try {
            val payload = buildJsonObject {
                put("thread_id", threadId)
                if (!senderId.isNullOrBlank() && senderId.contains("-")) {
                    put("sender_id", senderId)
                }
                put("body", body.trim())
            }.toString()

            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/chat_messages")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer ${bearerToken()}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val arr = runCatching { json.parseToJsonElement(resp.body?.string().orEmpty()).jsonArray }.getOrNull()
                val obj = arr?.firstOrNull()?.jsonObject ?: return@withContext null
                val id = obj["id"]?.jsonPrimitive?.contentOrNull ?: ""
                val createdAtRaw = obj["created_at"]?.jsonPrimitive?.contentOrNull ?: ""
                val timeLabel = if (createdAtRaw.length >= 16) createdAtRaw.substring(11, 16) else "Just now"
                SupabaseChatMessage(
                    id = id,
                    threadId = threadId,
                    senderId = senderId.orEmpty(),
                    senderLabel = "You",
                    body = body.trim(),
                    createdAt = timeLabel,
                    isFromCurrentUser = true
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    fun streamChatMessagesFlow(
        threadId: String,
        currentUserId: String? = null,
        counterpartyName: String = "Verified Tasker",
        pollIntervalMs: Long = 3500L
    ): Flow<List<SupabaseChatMessage>> = flow {
        if (!isConfigured || threadId.isBlank() || !threadId.contains("-")) {
            emit(emptyList())
            return@flow
        }
        while (true) {
            val latest = fetchChatMessagesForThread(
                threadId = threadId,
                currentUserId = currentUserId,
                counterpartyName = counterpartyName
            )
            emit(latest)
            delay(pollIntervalMs)
        }
    }

    // --- 6. Taska Credits (`taska_credit_balances` & `credit_topup_requests`) ---

    suspend fun fetchUserCreditBalance(userId: String): Int? = withContext(Dispatchers.IO) {
        if (!isConfigured || userId.isBlank() || !userId.contains("-")) return@withContext null
        try {
            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/taska_credit_balances?select=balance_credits&user_id=eq.$userId&limit=1")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer ${bearerToken()}")
                .get()
                .build()

            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val arr = json.parseToJsonElement(resp.body?.string().orEmpty()).jsonArray
                arr.firstOrNull()?.jsonObject?.get("balance_credits")?.jsonPrimitive?.intOrNull
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun submitCreditTopUpToSupabase(
        userId: String?,
        creditsRequested: Int,
        senderName: String,
        senderPhone: String,
        reference: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (!isConfigured || userId.isNullOrBlank() || !userId.contains("-")) return@withContext false
        try {
            val amountUsd = creditsRequested / 20.0
            val payload = buildJsonObject {
                put("user_id", userId)
                put("credits_requested", creditsRequested)
                put("amount_usd", amountUsd)
                put("sender_name", senderName.trim())
                put("sender_phone", senderPhone.trim())
                put("payment_reference", reference.trim().ifBlank { "Mobile Money" })
                put("status", "pending")
            }.toString()

            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/credit_topup_requests")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer ${bearerToken()}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .post(payload.toRequestBody(jsonMediaType))
                .build()

            httpClient.newCall(req).execute().use { resp ->
                resp.isSuccessful
            }
        } catch (_: Exception) {
            false
        }
    }

    // --- 7. Platform Announcements (`platform_announcements`) ---

    suspend fun fetchPlatformAnnouncements(): List<Triple<String, String, String>> = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext emptyList()
        try {
            val req = Request.Builder()
                .url("$supabaseUrl/rest/v1/platform_announcements?select=title,body,created_at&is_active=eq.true&order=created_at.desc&limit=20")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer ${bearerToken()}")
                .get()
                .build()

            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext emptyList()
                val arr = json.parseToJsonElement(resp.body?.string().orEmpty()).jsonArray
                arr.mapNotNull { el ->
                    val obj: JsonObject = el.jsonObject
                    val title = obj["title"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                    val body = obj["body"]?.jsonPrimitive?.contentOrNull ?: ""
                    val createdAt = obj["created_at"]?.jsonPrimitive?.contentOrNull ?: "Recently"
                    Triple(title, body, createdAt)
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        val COMPLETE_SUPABASE_SCHEMA_SQL = """
-- Taska Complete Supabase Schema & RLS Migration (Idempotent)
create extension if not exists "pgcrypto";

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  display_name text not null default '',
  default_area text not null default 'UNZA Campus Hub',
  updated_at timestamptz not null default now()
);

create table if not exists public.provider_profiles (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null references auth.users(id) on delete cascade,
  kind text not null default 'service_tasker',
  display_name text not null,
  bio text default '',
  is_online_service boolean not null default false,
  business_name text,
  business_registration_number text,
  phone_note text,
  broad_area_label text default 'UNZA Campus Hub',
  verification_status text not null default 'unsubmitted',
  rating_avg numeric(3,2) not null default 5.0,
  rating_count integer not null default 1,
  completed_tasks_count integer not null default 0,
  created_at timestamptz not null default now()
);

create table if not exists public.service_listings (
  id uuid primary key default gen_random_uuid(),
  provider_id uuid not null references public.provider_profiles(id) on delete cascade,
  category_slug text not null default 'repairs',
  title text not null,
  description text default '',
  price_zmw numeric(12,2),
  currency text not null default 'ZMW',
  photo_urls text[] not null default '{}',
  is_active boolean not null default true,
  created_at timestamptz not null default now()
);

create table if not exists public.service_requests (
  id uuid primary key default gen_random_uuid(),
  requester_id uuid references auth.users(id) on delete set null,
  short_title text,
  request_text text not null,
  broad_area text,
  category_hint text,
  client_budget_zmw numeric(12,2),
  budget_negotiable boolean not null default true,
  completion_pin text,
  status text not null default 'open',
  created_at timestamptz not null default now()
);

create table if not exists public.task_offers (
  id uuid primary key default gen_random_uuid(),
  request_id uuid not null references public.service_requests(id) on delete cascade,
  provider_id uuid not null references public.provider_profiles(id) on delete cascade,
  proposed_price_zmw numeric(12,2),
  status text not null default 'pending',
  created_at timestamptz not null default now()
);

create table if not exists public.chat_threads (
  id uuid primary key default gen_random_uuid(),
  request_id uuid references public.service_requests(id) on delete cascade,
  requester_id uuid references auth.users(id) on delete cascade,
  provider_id uuid references public.provider_profiles(id) on delete cascade,
  status text not null default 'open',
  created_at timestamptz not null default now()
);

create table if not exists public.chat_messages (
  id uuid primary key default gen_random_uuid(),
  thread_id uuid not null references public.chat_threads(id) on delete cascade,
  sender_id uuid references auth.users(id) on delete set null,
  body text not null,
  created_at timestamptz not null default now()
);

create table if not exists public.task_reviews (
  id uuid primary key default gen_random_uuid(),
  request_id uuid references public.service_requests(id) on delete set null,
  reviewer_id uuid references auth.users(id) on delete set null,
  provider_id uuid references public.provider_profiles(id) on delete cascade,
  stars integer not null check (stars between 1 and 5),
  comment text default '',
  created_at timestamptz not null default now()
);

create table if not exists public.provider_verification_submissions (
  id uuid primary key default gen_random_uuid(),
  provider_id uuid references public.provider_profiles(id) on delete cascade,
  user_id uuid not null references auth.users(id) on delete cascade,
  legal_full_name text not null,
  national_id_number text not null,
  id_document_path text not null,
  selfie_with_id_path text not null,
  status text not null default 'pending_review',
  created_at timestamptz not null default now()
);

create table if not exists public.taska_credit_balances (
  user_id uuid primary key references auth.users(id) on delete cascade,
  balance_credits integer not null default 120,
  updated_at timestamptz not null default now()
);

create table if not exists public.credit_topup_requests (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  credits_requested integer not null check (credits_requested >= 60),
  amount_usd numeric(10,2) not null,
  sender_name text,
  sender_phone text,
  payment_reference text,
  proof_storage_path text,
  status text not null default 'pending',
  created_at timestamptz not null default now()
);

create table if not exists public.platform_announcements (
  id uuid primary key default gen_random_uuid(),
  title text not null,
  body text not null,
  is_active boolean not null default true,
  created_at timestamptz not null default now()
);

create table if not exists public.saved_providers (
  user_id uuid not null references auth.users(id) on delete cascade,
  provider_id uuid not null references public.provider_profiles(id) on delete cascade,
  created_at timestamptz not null default now(),
  primary key (user_id, provider_id)
);
""".trimIndent()
    }
}
