package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatRepository(private val db: AppDatabase) {
    val shouts: Flow<List<Shout>> = db.shoutDao().getAllShouts()
    val topics: Flow<List<ForumTopic>> = db.forumTopicDao().getAllTopics()
    val messages: Flow<List<DirectMessage>> = db.directMessageDao().getAllMessages()
    val notifications: Flow<List<ChatNotification>> = db.notificationDao().getAllNotifications()
    val profile: Flow<UserProfile?> = db.userProfileDao().getProfileFlow()

    fun getCommentsForShout(shoutId: Int): Flow<List<ShoutComment>> {
        return db.commentDao().getCommentsForShout(shoutId)
    }

    suspend fun insertShout(shout: Shout) {
        db.shoutDao().insertShout(shout)
    }

    suspend fun likeShout(shoutId: Int) {
        db.shoutDao().incrementLikes(shoutId)
    }

    suspend fun addComment(comment: ShoutComment) {
        db.commentDao().insertComment(comment)
        db.shoutDao().incrementCommentsCount(comment.shoutId)
    }

    suspend fun insertTopic(topic: ForumTopic) {
        db.forumTopicDao().insertTopic(topic)
    }

    suspend fun incrementReplies(topicId: Int) {
        db.forumTopicDao().incrementRepliesCount(topicId)
    }

    suspend fun sendMessage(sender: String, content: String, isFromMe: Boolean) {
        db.directMessageDao().insertMessage(
            DirectMessage(sender = sender, content = content, isFromMe = isFromMe)
        )
    }

    suspend fun insertNotification(title: String, body: String) {
        db.notificationDao().insertNotification(
            ChatNotification(title = title, body = body)
        )
    }

    suspend fun updateProfile(username: String, status: String) {
        val existing = db.userProfileDao().getProfile() ?: UserProfile()
        db.userProfileDao().insertOrUpdateProfile(
            existing.copy(username = username, status = status)
        )
    }

    suspend fun incrementViews() {
        db.userProfileDao().incrementProfileViews()
    }

    // Prepopulate if DB is empty
    fun initializeDatabaseIfEmpty(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val existingShouts = db.shoutDao().getAllShouts().first()
            if (existingShouts.isEmpty()) {
                // 1. Initial User Profile
                db.userProfileDao().insertOrUpdateProfile(UserProfile())

                // 2. Initial Shouts matching screenshot
                val shout1 = Shout(
                    id = 1,
                    username = "Humble_Boy",
                    text = "everyone, come and we discuss this day, how was it to everyone.",
                    greetingBanner = "Good Evening",
                    likesCount = 1,
                    commentsCount = 2,
                    timestamp = System.currentTimeMillis() - 600000 // 10 mins ago
                )
                val shout2 = Shout(
                    id = 2,
                    username = "sampee",
                    text = "every body am a new here hope you guys are fine",
                    greetingBanner = "Hi",
                    emojiStyle = "Smiley",
                    likesCount = 4,
                    commentsCount = 4,
                    timestamp = System.currentTimeMillis() - 300000 // 5 mins ago
                )
                val shout3 = Shout(
                    id = 3,
                    username = "kala-graham",
                    text = "ABDULWAJID The thing about life is that when you get something, they will come",
                    greetingBanner = "None",
                    emojiStyle = "Eyes",
                    likesCount = 0,
                    commentsCount = 1,
                    timestamp = System.currentTimeMillis() - 100000 // 1.6 mins ago
                )

                db.shoutDao().insertShout(shout1)
                db.shoutDao().insertShout(shout2)
                db.shoutDao().insertShout(shout3)

                // 3. Comments for sampee's shout
                db.commentDao().insertComment(ShoutComment(shoutId = 2, username = "Humble_Boy", text = "Welcome sampee! Glad to have you here.", timestamp = System.currentTimeMillis() - 250000))
                db.commentDao().insertComment(ShoutComment(shoutId = 2, username = "TechBoY", text = "Welcome mate! Enjoy the sunday vibes.", timestamp = System.currentTimeMillis() - 200000))
                db.commentDao().insertComment(ShoutComment(shoutId = 2, username = "kala-graham", text = "Be safe in the chat boxes!", timestamp = System.currentTimeMillis() - 150000))
                db.commentDao().insertComment(ShoutComment(shoutId = 2, username = "sampee", text = "Thanks guys! Let's chat more under the topics.", timestamp = System.currentTimeMillis() - 100000))

                // Comments for Humble_Boy's shout
                db.commentDao().insertComment(ShoutComment(shoutId = 1, username = "kala-graham", text = "A bit quiet today, but we discuss soon.", timestamp = System.currentTimeMillis() - 500000))
                db.commentDao().insertComment(ShoutComment(shoutId = 1, username = "TechBoY", text = "My Sunday is going epic, tuning into mp3 jamz!", timestamp = System.currentTimeMillis() - 400000))

                // Comments for kala-graham's shout
                db.commentDao().insertComment(ShoutComment(shoutId = 3, username = "ABDULWAJID", text = "Wise words graham. True statement.", timestamp = System.currentTimeMillis() - 50000))

                // 4. Forum Topics
                db.forumTopicDao().insertTopic(
                    ForumTopic(
                        id = 1,
                        title = "Sunday vibez.mp3.*DIFFERENT PATTERN*ftQuavo246[champion]",
                        creator = "TechBoY",
                        content = "Who has heard the new Sunday Vibes joint featuring Quavo246? It's blowing up columns! Post your reviews here.",
                        repliesCount = 3,
                        timestamp = System.currentTimeMillis() - 3600000 * 2
                    )
                )
                db.forumTopicDao().insertTopic(
                    ForumTopic(
                        id = 2,
                        title = "Retro HTML layouts represent absolute peak web nostalgia",
                        creator = "HTML_Geek",
                        content = "Who else misses the days of marquee tags, scrolling text, green border widgets, neon tables and bright yellow text? This app captures it perfectly!",
                        repliesCount = 12,
                        timestamp = System.currentTimeMillis() - 3600000 * 5
                    )
                )

                // 5. Direct Messages
                db.directMessageDao().insertMessage(
                    DirectMessage(id = 1, sender = "Humble_Boy", content = "Hey ABDULWAJID, welcome back! Awesome seeing you online today.", isFromMe = false, timestamp = System.currentTimeMillis() - 600000)
                )
                db.directMessageDao().insertMessage(
                    DirectMessage(id = 2, sender = "sampee", content = "Hi, can you help me with the club profile setup?", isFromMe = false, timestamp = System.currentTimeMillis() - 200000)
                )

                // 6. Notifications
                db.notificationDao().insertNotification(
                    ChatNotification(id = 1, title = "New Shout Like", body = "sampee liked your shout", timestamp = System.currentTimeMillis() - 120000, isRead = false)
                )
                db.notificationDao().insertNotification(
                    ChatNotification(id = 2, title = "Profile Visited", body = "Humble_Boy visited your profile", timestamp = System.currentTimeMillis() - 400000, isRead = false)
                )
            }
        }
    }
}
