package com.example.data

import kotlinx.coroutines.flow.Flow

/**
 * Room Repository managing [Task] records for local marketplace service requirements
 * and real-time communication with verified Taskers via Supabase.
 */
class TaskRepository(
    private val dao: TaskaDao,
    val supabaseService: TaskaSupabaseService = TaskaSupabaseService()
) {
    val allTasks: Flow<List<Task>> = dao.getAllTasks()

    fun getTasksByArea(area: String): Flow<List<Task>> = dao.getTasksByArea(area)

    suspend fun getTaskById(id: Int): Task? = dao.getTaskById(id)

    suspend fun insertTask(task: Task): Long = dao.insertTask(task)

    suspend fun updateTask(task: Task) = dao.updateTask(task)

    suspend fun deleteTaskById(id: Int) = dao.deleteTaskById(id)

    suspend fun createMarketplaceTaskRequirement(
        title: String,
        description: String,
        categoryId: String,
        area: String,
        budgetZmw: String,
        budgetNegotiable: Boolean,
        urgencyOrTiming: String = "Flexible",
        assignedTasker: TaskerEntity? = null,
        requesterUserId: String? = null
    ): Task {
        val numericBudget = budgetZmw.filter { it.isDigit() || it == '.' }.toDoubleOrNull()
        val formattedBudget = if (numericBudget != null && numericBudget > 0) {
            "ZMW ${numericBudget.toInt()}"
        } else if (budgetZmw.isNotBlank()) {
            budgetZmw.trim()
        } else {
            "Open to offers"
        }

        val cleanTitle = title.trim().ifBlank {
            description.trim().take(48).ifBlank { "Local Service Requirement" }
        }
        val cleanDetails = description.trim().ifBlank { cleanTitle }

        val supabaseReqId = supabaseService.submitServiceDemandToSupabase(
            requestText = cleanDetails,
            broadArea = area,
            requesterId = requesterUserId,
            shortTitle = cleanTitle,
            clientBudgetZmw = numericBudget,
            budgetNegotiable = budgetNegotiable
        ).orEmpty()

        val supabaseThreadId = if (supabaseReqId.isNotBlank() && supabaseReqId != "synced") {
            supabaseService.getOrCreateChatThread(
                requestId = supabaseReqId,
                requesterId = requesterUserId
            ).orEmpty()
        } else {
            ""
        }

        val generatedPin = (100000..999999).random().toString()
        val initialSystemMsg = buildString {
            append("Taska Shield::Private marketplace task created for $area ($formattedBudget")
            if (budgetNegotiable) append(" · Negotiable")
            append("). Phone numbers remain hidden.::Just now")
            if (assignedTasker != null) {
                append("||You::Hi ${assignedTasker.name}, I shared my task requirement: \"$cleanTitle\" ($cleanDetails).::Just now")
            }
        }

        if (supabaseThreadId.isNotBlank() && assignedTasker != null) {
            supabaseService.sendChatMessageToSupabase(
                threadId = supabaseThreadId,
                senderId = requesterUserId,
                body = "Hi ${assignedTasker.name}, I shared my task requirement: \"$cleanTitle\" ($cleanDetails)."
            )
        }

        val newTask = Task(
            title = cleanTitle,
            description = cleanDetails,
            categoryId = categoryId,
            area = area,
            budgetZmw = formattedBudget,
            budgetNegotiable = budgetNegotiable,
            urgencyOrTiming = urgencyOrTiming,
            assignedTaskerId = assignedTasker?.id,
            assignedTaskerName = assignedTasker?.name ?: "Verified Local Taskers",
            assignedTaskerInitials = assignedTasker?.initials ?: "TK",
            assignedTaskerVerified = assignedTasker?.isVerified ?: true,
            requesterUserId = requesterUserId.orEmpty(),
            status = if (assignedTasker != null) "Active in Private Chat" else "Open",
            completionPin = generatedPin,
            supabaseRequestId = supabaseReqId,
            supabaseThreadId = supabaseThreadId,
            chatHistorySerialized = initialSystemMsg
        )

        val insertedId = dao.insertTask(newTask).toInt()
        return newTask.copy(id = insertedId)
    }

    suspend fun sendRealTimeMessageForTask(
        task: Task,
        messageText: String,
        senderUserId: String? = null
    ): Task {
        val cleanText = messageText.trim()
        if (cleanText.isBlank()) return task

        var activeThreadId = task.supabaseThreadId
        if (activeThreadId.isBlank() && task.supabaseRequestId.contains("-")) {
            activeThreadId = supabaseService.getOrCreateChatThread(
                requestId = task.supabaseRequestId,
                requesterId = senderUserId ?: task.requesterUserId
            ).orEmpty()
        }

        if (activeThreadId.isNotBlank()) {
            supabaseService.sendChatMessageToSupabase(
                threadId = activeThreadId,
                senderId = senderUserId ?: task.requesterUserId,
                body = cleanText
            )
        }

        val appendedEntry = "You::$cleanText::Just now"
        val updatedHistory = if (task.chatHistorySerialized.isBlank()) {
            appendedEntry
        } else {
            "${task.chatHistorySerialized}||$appendedEntry"
        }

        val updatedTask = task.copy(
            status = if (task.status == "Completed") "Completed" else "Active in Private Chat",
            supabaseThreadId = activeThreadId,
            chatHistorySerialized = updatedHistory
        )
        dao.updateTask(updatedTask)
        return updatedTask
    }

    fun streamRealTimeChatForThread(
        threadId: String,
        currentUserId: String?,
        counterpartyName: String
    ): Flow<List<SupabaseChatMessage>> {
        return supabaseService.streamChatMessagesFlow(
            threadId = threadId,
            currentUserId = currentUserId,
            counterpartyName = counterpartyName
        )
    }
}
