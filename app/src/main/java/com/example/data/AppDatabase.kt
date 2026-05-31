package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Shout::class,
        ShoutComment::class,
        UserProfile::class,
        ForumTopic::class,
        DirectMessage::class,
        ChatNotification::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoutDao(): ShoutDao
    abstract fun commentDao(): CommentDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun forumTopicDao(): ForumTopicDao
    abstract fun directMessageDao(): DirectMessageDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chatlanka_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
