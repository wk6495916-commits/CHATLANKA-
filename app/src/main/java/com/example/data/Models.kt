package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shouts")
data class Shout(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val text: String,
    val greetingBanner: String = "None", // e.g. "Good Evening", "Hi", "None"
    val emojiStyle: String = "None",      // e.g. "Smiley", "Eyes", "None"
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class ShoutComment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shoutId: Int,
    val username: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val username: String = "ABDULWAJID",
    val status: String = "Member of the day",
    val profileViews: Int = 2,
    val joinedDate: String = "Sun 31 May 26"
)

@Entity(tableName = "forum_topics")
data class ForumTopic(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val creator: String,
    val content: String,
    val repliesCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "direct_messages")
data class DirectMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val content: String,
    val isFromMe: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class ChatNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
