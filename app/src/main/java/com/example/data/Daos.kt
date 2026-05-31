package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoutDao {
    @Query("SELECT * FROM shouts ORDER BY timestamp DESC")
    fun getAllShouts(): Flow<List<Shout>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShout(shout: Shout)

    @Update
    suspend fun updateShout(shout: Shout)

    @Query("UPDATE shouts SET likesCount = likesCount + 1 WHERE id = :id")
    suspend fun incrementLikes(id: Int)

    @Query("UPDATE shouts SET commentsCount = commentsCount + 1 WHERE id = :id")
    suspend fun incrementCommentsCount(id: Int)

    @Query("SELECT * FROM shouts WHERE id = :id")
    suspend fun getShoutById(id: Int): Shout?

    @Query("DELETE FROM shouts")
    suspend fun deleteAllShouts()
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE shoutId = :shoutId ORDER BY timestamp ASC")
    fun getCommentsForShout(shoutId: Int): Flow<List<ShoutComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: ShoutComment)

    @Query("DELETE FROM comments")
    suspend fun deleteAllComments()
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET profileViews = profileViews + 1 WHERE id = 1")
    suspend fun incrementProfileViews()
}

@Dao
interface ForumTopicDao {
    @Query("SELECT * FROM forum_topics ORDER BY timestamp DESC")
    fun getAllTopics(): Flow<List<ForumTopic>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: ForumTopic)

    @Query("UPDATE forum_topics SET repliesCount = repliesCount + 1 WHERE id = :id")
    suspend fun incrementRepliesCount(id: Int)

    @Query("DELETE FROM forum_topics")
    suspend fun deleteAllTopics()
}

@Dao
interface DirectMessageDao {
    @Query("SELECT * FROM direct_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<DirectMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: DirectMessage)

    @Query("DELETE FROM direct_messages")
    suspend fun deleteAllMessages()
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<ChatNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: ChatNotification)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()
}
