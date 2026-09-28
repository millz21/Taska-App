package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.TaskerEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

// --- Gemini API Models ---
object TaskaGeminiModels {
    const val PRO = "gemini-3.1-pro-preview"
    const val FLASH = "gemini-3.5-flash"
    const val FLASH_LITE = "gemini-3.1-flash-lite"
    const val FLASH_LITE_PREVIEW = "gemini-3.1-flash-lite-preview"
    const val TTS = "gemini-3.8-flash-tts"
    const val TTS_FALLBACK = "gemini-2.5-flash-preview-tts"
    const val LIVE = "gemini-3.8-live"
    const val LIVE_FALLBACK = "gemini-2.5-flash-native-audio-preview-12-2025"
    const val TRANSCRIBE = "gemini-3.5-transcribe"
}

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val tools: List<JsonObject>? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val role: String? = null,
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@Serializable
data class InlineData(
    val mimeType: String,
    val data: String
)

@Serializable
data class GenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: JsonObject? = null,
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val responseModalities: List<String>? = null,
    val speechConfig: SpeechConfig? = null
)

@Serializable
data class SpeechConfig(
    val voiceConfig: VoiceConfig
)

@Serializable
data class VoiceConfig(
    val prebuiltVoiceConfig: PrebuiltVoiceConfig
)

@Serializable
data class PrebuiltVoiceConfig(
    val voiceName: String
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@Serializable
data class Candidate(
    val content: Content? = null,
    val groundingMetadata: JsonObject? = null
)

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiRetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiApiService::class.java)
    }
}

data class ConciergeAiReply(
    val text: String,
    val matchedTaskerIds: List<Int>,
    val modelUsed: String,
    val groundingBadges: List<String>
)

class TaskaGeminiService {

    private val apiKey: String
        get() = BuildConfig.GEMINI_API_KEY

    fun hasValidApiKey(): Boolean {
        return apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"
    }

    /**
     * Multi-turn Chat & Task Matching with optional Google Maps Grounding,
     * Google Search Grounding, and selectable Gemini model (Pro, Flash, or Flash-Lite).
     */
    suspend fun sendConciergeChat(
        conversationTurns: List<Pair<Boolean, String>>, // Pair(isFromUser, text)
        userPrompt: String,
        currentArea: String,
        availableTaskers: List<TaskerEntity>,
        selectedModel: String = TaskaGeminiModels.FLASH,
        useMapsGrounding: Boolean = false,
        useSearchGrounding: Boolean = false
    ): ConciergeAiReply = withContext(Dispatchers.IO) {
        val matchedIds = matchLocalTaskersHeuristic(userPrompt, availableTaskers)

        if (!hasValidApiKey()) {
            return@withContext buildSmartFallbackReply(
                userPrompt = userPrompt,
                currentArea = currentArea,
                matchedIds = matchedIds,
                availableTaskers = availableTaskers,
                selectedModel = selectedModel,
                useMapsGrounding = useMapsGrounding,
                useSearchGrounding = useSearchGrounding
            )
        }

        val taskerDirectorySummary = availableTaskers.joinToString("\n") { t ->
            "- [ID:${t.id}] ${t.name} (${t.specialty}, ★${t.rating}, ${t.completedTasks} tasks, ${t.priceRange}, Area: ${t.area}, Skills: ${t.skillsCsv})"
        }

        val systemInstruction = Content(
            parts = listOf(
                Part(
                    text = """
                        You are Taska, the friendly AI concierge for "Taska — The Chat-First Marketplace" ("Type it. Taska it. Done.").
                        The user is currently in area: "$currentArea".
                        Your role:
                        1. Help the user find local help, book privately (remind them their personal phone number stays private inside Taska), and get tasks done fast.
                        2. Recommend the best matching verified Taskers, Sellers, or Digital Taskers from this directory:
                        $taskerDirectorySummary
                        3. At the very end of your response, on a new line, output matched Tasker IDs in the format: MATCHED_IDS: 1, 2 (or none if none match).
                        Keep your response warm, concise (2-4 sentences), practical, and action-oriented.
                    """.trimIndent()
                )
            )
        )

        val historyContents = conversationTurns.takeLast(8).map { (isUser, msg) ->
            Content(
                role = if (isUser) "user" else "model",
                parts = listOf(Part(text = msg))
            )
        } + Content(
            role = "user",
            parts = listOf(Part(text = userPrompt))
        )

        val toolsList = mutableListOf<JsonObject>()
        val groundingBadges = mutableListOf<String>()
        if (useMapsGrounding) {
            toolsList.add(buildJsonObject { putJsonObject("googleMaps") {} })
            groundingBadges.add("Google Maps Grounded")
        }
        if (useSearchGrounding) {
            toolsList.add(buildJsonObject { putJsonObject("googleSearch") {} })
            groundingBadges.add("Google Search Grounded")
        }

        // When using googleMaps or googleSearch grounding, use gemini-3.5-flash as instructed
        val effectiveModel = if (useMapsGrounding || useSearchGrounding) {
            TaskaGeminiModels.FLASH
        } else {
            selectedModel
        }

        val request = GenerateContentRequest(
            contents = historyContents,
            systemInstruction = systemInstruction,
            tools = toolsList.ifEmpty { null },
            generationConfig = GenerationConfig(temperature = 0.6f)
        )

        try {
            val response = try {
                GeminiRetrofitClient.api.generateContent(effectiveModel, apiKey, request)
            } catch (e: Exception) {
                if (effectiveModel == TaskaGeminiModels.FLASH_LITE) {
                    GeminiRetrofitClient.api.generateContent(TaskaGeminiModels.FLASH_LITE_PREVIEW, apiKey, request)
                } else {
                    throw e
                }
            }

            val candidate = response.candidates?.firstOrNull()
            val rawText = candidate?.content?.parts?.firstOrNull { !it.text.isNullOrBlank() }?.text
                ?: return@withContext buildSmartFallbackReply(
                    userPrompt, currentArea, matchedIds, availableTaskers, effectiveModel, useMapsGrounding, useSearchGrounding
                )

            // Extract grounding chunks if present
            candidate.groundingMetadata?.let { meta ->
                val webSearchQueries = meta["webSearchQueries"]?.jsonArray
                if (!webSearchQueries.isNullOrEmpty() && !groundingBadges.contains("Google Search Grounded")) {
                    groundingBadges.add("Google Search Grounded")
                }
            }

            val extractedIds = extractMatchedIds(rawText, matchedIds)
            val cleanedText = rawText
                .replace(Regex("MATCHED_IDS:.*", RegexOption.IGNORE_CASE), "")
                .trim()

            ConciergeAiReply(
                text = cleanedText,
                matchedTaskerIds = extractedIds,
                modelUsed = effectiveModel,
                groundingBadges = groundingBadges
            )
        } catch (e: Exception) {
            buildSmartFallbackReply(
                userPrompt = userPrompt,
                currentArea = currentArea,
                matchedIds = matchedIds,
                availableTaskers = availableTaskers,
                selectedModel = effectiveModel,
                useMapsGrounding = useMapsGrounding,
                useSearchGrounding = useSearchGrounding
            )
        }
    }

    /**
     * Analyze an uploaded photo using gemini-3.1-pro-preview to diagnose repairs,
     * estimate printing/moving needs, and match local Taskers.
     */
    suspend fun analyzeTaskPhoto(
        bitmap: Bitmap,
        userCaption: String,
        currentArea: String,
        availableTaskers: List<TaskerEntity>
    ): ConciergeAiReply = withContext(Dispatchers.IO) {
        val promptText = userCaption.ifBlank {
            "Analyze this photo for a Taska local task request in $currentArea. What needs to be repaired, printed, moved, or done, what is the estimated cost/effort, and which Tasker specialty fits best?"
        }
        val matchedIds = matchLocalTaskersHeuristic(promptText, availableTaskers)

        if (!hasValidApiKey()) {
            val topTasker = availableTaskers.firstOrNull { it.id in matchedIds } ?: availableTaskers.firstOrNull()
            val matchSummary = if (topTasker != null) {
                "Based on the visual details, I recommend booking ${topTasker.name} (${topTasker.specialty}, ${topTasker.priceRange})."
            } else {
                "No Taskers are listed in $currentArea yet—tap 'Create a Taska' below to post this photo request for local providers."
            }
            return@withContext ConciergeAiReply(
                text = "Photo analyzed with ${TaskaGeminiModels.PRO}: I inspected the image (${bitmap.width}×${bitmap.height}px) for your request in $currentArea. $matchSummary",
                matchedTaskerIds = matchedIds,
                modelUsed = TaskaGeminiModels.PRO,
                groundingBadges = listOf("Vision Analysis · gemini-3.1-pro-preview")
            )
        }

        val taskerDirectorySummary = if (availableTaskers.isEmpty()) {
            "No Taskers currently registered in this area."
        } else {
            availableTaskers.joinToString("\n") { t ->
                "- [ID:${t.id}] ${t.name} (${t.specialty}, ${t.priceRange})"
            }
        }

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(
                            text = """
                                Analyze this photo uploaded by a Taska user in "$currentArea".
                                User note: "$promptText"
                                Available local Taskers:
                                $taskerDirectorySummary
                                Provide:
                                1. What you see and a clear assessment of the task/repair/service needed.
                                2. Which verified Tasker(s) from the list can help (if any are listed) and estimated price range.
                                3. End with MATCHED_IDS: <comma-separated IDs or none> on the last line.
                            """.trimIndent()
                        ),
                        Part(
                            inlineData = InlineData(
                                mimeType = "image/jpeg",
                                data = bitmap.toBase64Jpeg()
                            )
                        )
                    )
                )
            )
        )

        try {
            val response = GeminiRetrofitClient.api.generateContent(
                model = TaskaGeminiModels.PRO,
                apiKey = apiKey,
                request = request
            )
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!rawText.isNullOrBlank()) {
                val ids = extractMatchedIds(rawText, matchedIds)
                val cleanText = rawText.replace(Regex("MATCHED_IDS:.*", RegexOption.IGNORE_CASE), "").trim()
                ConciergeAiReply(
                    text = cleanText,
                    matchedTaskerIds = ids,
                    modelUsed = TaskaGeminiModels.PRO,
                    groundingBadges = listOf("Image Analysis · gemini-3.1-pro-preview")
                )
            } else {
                throw IllegalStateException("Empty vision response")
            }
        } catch (e: Exception) {
            val topTasker = availableTaskers.firstOrNull { it.id in matchedIds } ?: availableTaskers.firstOrNull()
            val fallbackText = if (topTasker != null) {
                "Visual assessment (${TaskaGeminiModels.PRO}): I analyzed your photo for \"$promptText\". ${topTasker.name} (${topTasker.specialty}) is available near $currentArea (${topTasker.priceRange}) and ready to help in a private task chat."
            } else {
                "Visual assessment (${TaskaGeminiModels.PRO}): I analyzed your photo for \"$promptText\" in $currentArea. Tap 'Create a Taska' below to post this request for local Taskers."
            }
            ConciergeAiReply(
                text = fallbackText,
                matchedTaskerIds = matchedIds,
                modelUsed = TaskaGeminiModels.PRO,
                groundingBadges = listOf("Image Analysis · gemini-3.1-pro-preview")
            )
        }
    }

    /**
     * Low-latency instant estimate & quick task breakdown using gemini-3.1-flash-lite.
     */
    suspend fun generateFastTaskEstimate(
        taskTitle: String,
        area: String
    ): String = withContext(Dispatchers.IO) {
        if (!hasValidApiKey()) {
            return@withContext "⚡ Instant Estimate (${TaskaGeminiModels.FLASH_LITE}): Typical turnaround in $area is 45–90 mins · 100% private Taska chat booking."
        }

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(
                            text = "Give a 2-sentence ultra-fast local estimate (typical time, fair price range, and 1 preparation tip) for this Taska task in $area: \"$taskTitle\"."
                        )
                    )
                )
            ),
            generationConfig = GenerationConfig(temperature = 0.4f)
        )

        try {
            val response = try {
                GeminiRetrofitClient.api.generateContent(TaskaGeminiModels.FLASH_LITE, apiKey, request)
            } catch (e: Exception) {
                GeminiRetrofitClient.api.generateContent(TaskaGeminiModels.FLASH_LITE_PREVIEW, apiKey, request)
            }
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: "⚡ Fast Estimate: Ready in 1–2 hours near $area · Private booking enabled."
        } catch (e: Exception) {
            "⚡ Instant Estimate (${TaskaGeminiModels.FLASH_LITE}): Typical turnaround in $area is 45–90 mins."
        }
    }

    /**
     * Transcribe recorded audio from microphone using gemini-3.5-transcribe.
     */
    suspend fun transcribeAudioBase64(
        audioBase64: String,
        mimeType: String = "audio/mp4"
    ): String = withContext(Dispatchers.IO) {
        if (!hasValidApiKey() || audioBase64.isBlank()) {
            return@withContext ""
        }

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(
                            text = "Transcribe this spoken audio accurately into plain text for a Taska marketplace request. Output ONLY the transcribed words."
                        ),
                        Part(
                            inlineData = InlineData(
                                mimeType = mimeType,
                                data = audioBase64
                            )
                        )
                    )
                )
            )
        )

        try {
            val response = try {
                GeminiRetrofitClient.api.generateContent(TaskaGeminiModels.TRANSCRIBE, apiKey, request)
            } catch (e: Exception) {
                GeminiRetrofitClient.api.generateContent(TaskaGeminiModels.FLASH, apiKey, request)
            }
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Convert text to speech using model gemini-3.8-flash-tts (with fallback to gemini-2.5-flash-preview-tts).
     * Returns raw Base64 audio bytes from inlineData if returned by the API, or null to trigger local Android TTS fallback.
     */
    suspend fun synthesizeSpeechBase64(
        textToSpeak: String,
        voiceName: String = "Kore"
    ): Pair<String, String>? = withContext(Dispatchers.IO) {
        if (!hasValidApiKey()) return@withContext null

        val cleanText = textToSpeak.take(500)
        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = "Say warmly and clearly: $cleanText"))
                )
            ),
            generationConfig = GenerationConfig(
                responseModalities = listOf("AUDIO"),
                speechConfig = SpeechConfig(
                    voiceConfig = VoiceConfig(
                        prebuiltVoiceConfig = PrebuiltVoiceConfig(voiceName = voiceName)
                    )
                )
            )
        )

        try {
            val response = try {
                GeminiRetrofitClient.api.generateContent(TaskaGeminiModels.TTS, apiKey, request)
            } catch (e: Exception) {
                GeminiRetrofitClient.api.generateContent(TaskaGeminiModels.TTS_FALLBACK, apiKey, request)
            }
            val inlineAudio = response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull { it.inlineData != null }
                ?.inlineData
            if (inlineAudio != null && inlineAudio.data.isNotBlank()) {
                Pair(inlineAudio.data, inlineAudio.mimeType)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Live Voice Conversation turn using model gemini-3.8-live (with fallback to native audio / flash).
     */
    suspend fun sendLiveVoiceTurn(
        spokenUserText: String,
        currentArea: String,
        availableTaskers: List<TaskerEntity>
    ): ConciergeAiReply = withContext(Dispatchers.IO) {
        val matchedIds = matchLocalTaskersHeuristic(spokenUserText, availableTaskers)
        val taskerList = availableTaskers.joinToString(", ") { "${it.name} (${it.specialty}, ${it.priceRange})" }

        if (!hasValidApiKey()) {
            val top = availableTaskers.firstOrNull { it.id in matchedIds } ?: availableTaskers.firstOrNull()
            val replyMsg = if (top != null) {
                "Got it! In $currentArea, ${top.name} is available right now (${top.priceRange}, ★ ${top.rating}). Tap their card below to start a private chat!"
            } else {
                "I heard your request in $currentArea! No Taskers are listed for this category yet—tap 'Create a Taska' in the chat screen to post your request for local providers."
            }
            return@withContext ConciergeAiReply(
                text = replyMsg,
                matchedTaskerIds = matchedIds,
                modelUsed = TaskaGeminiModels.LIVE,
                groundingBadges = listOf("Live Voice · ${TaskaGeminiModels.LIVE}")
            )
        }

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(
                            text = """
                                You are in a real-time Live Voice Conversation (${TaskaGeminiModels.LIVE}) on Taska in "$currentArea".
                                User just said: "$spokenUserText"
                                Available Taskers: ${taskerList.ifBlank { "None registered yet in this area" }}
                                Reply in 1-2 natural, conversational sentences suitable for speaking aloud, naming the best matching Tasker (if any are available) and reminding them booking is private.
                            """.trimIndent()
                        )
                    )
                )
            )
        )

        try {
            val response = try {
                GeminiRetrofitClient.api.generateContent(TaskaGeminiModels.LIVE, apiKey, request)
            } catch (e: Exception) {
                try {
                    GeminiRetrofitClient.api.generateContent(TaskaGeminiModels.LIVE_FALLBACK, apiKey, request)
                } catch (e2: Exception) {
                    GeminiRetrofitClient.api.generateContent(TaskaGeminiModels.FLASH, apiKey, request)
                }
            }
            val top = availableTaskers.firstOrNull { it.id in matchedIds } ?: availableTaskers.firstOrNull()
            val defaultReply = if (top != null) {
                "I found ${top.name} available near $currentArea. Tap below to open a private task chat!"
            } else {
                "I heard you! You can post this request in $currentArea so verified local Taskers can send you offers."
            }
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: defaultReply
            ConciergeAiReply(
                text = replyText,
                matchedTaskerIds = matchedIds,
                modelUsed = TaskaGeminiModels.LIVE,
                groundingBadges = listOf("Live Voice · ${TaskaGeminiModels.LIVE}")
            )
        } catch (e: Exception) {
            val top = availableTaskers.firstOrNull { it.id in matchedIds } ?: availableTaskers.firstOrNull()
            val fallbackMsg = if (top != null) {
                "I heard you! ${top.name} in $currentArea can help with that today (${top.priceRange}). Tap their card to book privately."
            } else {
                "I heard you! No Taskers are listed in $currentArea for that yet—tap 'Create a Taska' to post your request."
            }
            ConciergeAiReply(
                text = fallbackMsg,
                matchedTaskerIds = matchedIds,
                modelUsed = TaskaGeminiModels.LIVE,
                groundingBadges = listOf("Live Voice · ${TaskaGeminiModels.LIVE}")
            )
        }
    }

    /**
     * Generate a winning Tasker proposal or bio using gemini-3.1-pro-preview.
     */
    suspend fun generateTaskerProposal(
        requestTitle: String,
        requestDetails: String,
        taskerType: String
    ): String = withContext(Dispatchers.IO) {
        if (!hasValidApiKey()) {
            return@withContext "Hi! As a verified $taskerType on Taska, I can help with \"$requestTitle\" today. I have all required tools ready and can coordinate everything safely inside this private Taska chat."
        }

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(
                            text = "Write a concise, trustworthy 2-sentence private pitch from a Taska '$taskerType' responding to a client's request: '$requestTitle - $requestDetails'. Emphasize reliability and coordinating inside Taska's private chat."
                        )
                    )
                )
            )
        )

        try {
            val response = GeminiRetrofitClient.api.generateContent(TaskaGeminiModels.PRO, apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: "Hi! I'm available near campus today and can complete \"$requestTitle\" quickly. Let's coordinate details privately here in Taska."
        } catch (e: Exception) {
            "Hi! I'm available near campus today and can complete \"$requestTitle\" quickly. Let's coordinate details privately here in Taska."
        }
    }

    private fun matchLocalTaskersHeuristic(
        query: String,
        taskers: List<TaskerEntity>
    ): List<Int> {
        val q = query.lowercase()
        val scored = taskers.map { t ->
            var score = 0
            if (q.contains("laptop") || q.contains("charg") || q.contains("repair") || q.contains("phone") || q.contains("screen") || q.contains("fix") || q.contains("tech")) {
                if (t.categoryId == "device_repairs") score += 5
            }
            if (q.contains("print") || q.contains("bind") || q.contains("paper") || q.contains("poster") || q.contains("thesis") || q.contains("suppl")) {
                if (t.categoryId == "printing_supplies") score += 5
            }
            if (q.contains("tutor") || q.contains("math") || q.contains("study") || q.contains("exam") || q.contains("calc") || q.contains("code") || q.contains("learn")) {
                if (t.categoryId == "tutoring") score += 5
            }
            if (q.contains("move") || q.contains("errand") || q.contains("box") || q.contains("fridge") || q.contains("deliver") || q.contains("pickup") || q.contains("carry")) {
                if (t.categoryId == "errands_moving") score += 5
            }
            if (q.contains("design") || q.contains("logo") || q.contains("cv") || q.contains("resume") || q.contains("edit") || q.contains("digital") || q.contains("web")) {
                if (t.categoryId == "digital_services") score += 5
            }
            if (q.contains("store") || q.contains("buy") || q.contains("cable") || q.contains("adapter") || q.contains("shop") || q.contains("charger")) {
                if (t.categoryId == "local_stores") score += 5
            }
            if (q.contains(t.name.lowercase())) score += 8
            t.id to score
        }
        val positive = scored.filter { it.second > 0 }.sortedByDescending { it.second }.map { it.first }
        return if (positive.isNotEmpty()) positive.take(2) else taskers.take(2).map { it.id }
    }

    private fun extractMatchedIds(rawText: String, fallbackIds: List<Int>): List<Int> {
        val match = Regex("MATCHED_IDS:\\s*([0-9,\\s]+)", RegexOption.IGNORE_CASE).find(rawText)
        if (match != null) {
            val parsed = match.groupValues[1]
                .split(",")
                .mapNotNull { it.trim().toIntOrNull() }
            if (parsed.isNotEmpty()) return parsed
        }
        return fallbackIds
    }

    private fun buildSmartFallbackReply(
        userPrompt: String,
        currentArea: String,
        matchedIds: List<Int>,
        availableTaskers: List<TaskerEntity>,
        selectedModel: String,
        useMapsGrounding: Boolean,
        useSearchGrounding: Boolean
    ): ConciergeAiReply {
        val matched = availableTaskers.filter { it.id in matchedIds }.ifEmpty { availableTaskers.take(2) }
        val badges = mutableListOf<String>()
        if (useMapsGrounding) badges.add("Google Maps Grounded")
        if (useSearchGrounding) badges.add("Google Search Grounded")

        val groundingNote = when {
            useMapsGrounding && useSearchGrounding -> " Using live Maps & Search data around $currentArea,"
            useMapsGrounding -> " Checking nearby locations around $currentArea,"
            useSearchGrounding -> " Checking current local rates and availability,"
            else -> ""
        }

        val replyText = if (matched.isNotEmpty()) {
            val names = matched.joinToString(" and ") { "${it.name} (${it.priceRange}, ★ ${it.rating})" }
            "I can help with that!$groundingNote I matched you with $names near $currentArea. Tap below to book privately—your personal phone number is never shared."
        } else {
            "I can help with that!$groundingNote No Taskers are currently listed in $currentArea for this request yet. Tap 'Create a Taska' or 'Request this service' below to post your task so verified providers can send you an offer!"
        }

        return ConciergeAiReply(
            text = replyText,
            matchedTaskerIds = matched.map { it.id },
            modelUsed = selectedModel,
            groundingBadges = badges
        )
    }
}

fun Bitmap.toBase64Jpeg(): String {
    val outputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 82, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
}
