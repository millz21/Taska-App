package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TaskerEntity::class,
        TaskerReviewEntity::class,
        SavedLocationEntity::class,
        ConversationSessionEntity::class,
        Task::class,
        BookingEntity::class,
        ChatMessageEntity::class,
        MarketplaceRequestEntity::class,
        InboxNotificationEntity::class,
        ServiceDemandEntity::class,
        UserAccountEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class TaskaDatabase : RoomDatabase() {
    abstract fun taskaDao(): TaskaDao

    companion object {
        @Volatile
        private var INSTANCE: TaskaDatabase? = null

        fun getInstance(context: Context): TaskaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskaDatabase::class.java,
                    "taska_marketplace.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
