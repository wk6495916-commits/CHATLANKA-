package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.GeminiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class RetroScreen {
    CHAT,      // ShoutBox
    INBOX,     // Personal messages
    FORUM,     // Discussion board
    FRIENDS,   // Member directories
    NOTIFICATIONS, // Site activities
    PROFILE    // Personal terminal
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = ChatRepository(db)

    // Exposed States
    val shouts = repository.shouts.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val topics = repository.topics.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val messages = repository.messages.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val notifications = repository.notifications.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val profile = repository.profile.stateIn(viewModelScope, SharingStarted.Lazily, null)

    // App state
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _activeScreen = MutableStateFlow(RetroScreen.CHAT)
    val activeScreen: StateFlow<RetroScreen> = _activeScreen

    private val _selectedShoutId = MutableStateFlow<Int?>(null)
    val selectedShoutId: StateFlow<Int?> = _selectedShoutId

    // Dynamic Comments State Flow based on selectedShoutId
    val shoutComments: StateFlow<List<ShoutComment>> = _selectedShoutId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getCommentsForShout(id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated online members
    val onlineFriends = listOf(
        Pair("Humble_Boy", "online"),
        Pair("sampee", "online"),
        Pair("kala-graham", "online"),
        Pair("TechBoY", "online"),
        Pair("HTML_Geek", "online"),
        Pair("Harmony_Queen", "offline")
    )

    init {
        // Run database initialization
        repository.initializeDatabaseIfEmpty(viewModelScope)
        
        // Auto increment profile views slightly over time to simulate active members visiting
        startProfileViewsSimulation()
    }

    private fun startProfileViewsSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(45000) // every 45 secs
                if (Random.nextFloat() > 0.4) {
                    repository.incrementViews()
                    // Add a random visit notification
                    val randomVisitor = onlineFriends.shuffled().first().first
                    repository.insertNotification(
                        title = "Profile view",
                        body = "$randomVisitor checked out your terminal!"
                    )
                }
            }
        }
    }

    fun setScreen(screen: RetroScreen) {
        _activeScreen.value = screen
    }

    fun selectShoutForComments(shoutId: Int?) {
        _selectedShoutId.value = shoutId
    }

    // Shoutbox actions
    fun postShout(text: String, greetingBanner: String, emojiStyle: String, useAiReply: Boolean = true) {
        viewModelScope.launch {
            val user = profile.value?.username ?: "ABDULWAJID"
            val shout = Shout(
                username = user,
                text = text,
                greetingBanner = greetingBanner,
                emojiStyle = emojiStyle,
                likesCount = 0,
                commentsCount = 0,
                timestamp = System.currentTimeMillis()
            )
            repository.insertShout(shout)

            if (useAiReply) {
                triggerSimulationOrGemini(text)
            }
        }
    }

    fun likeShout(shoutId: Int) {
        viewModelScope.launch {
            repository.likeShout(shoutId)
            // Random chance that the poster notices and is happy
            val shout = shouts.value.find { it.id == shoutId }
            if (shout != null && shout.username != "ABDULWAJID") {
                delay(1200)
                repository.insertNotification(
                    title = "Mutual respect +1",
                    body = "${shout.username} appreciated your thumbs up!"
                )
            }
        }
    }

    fun addComment(shoutId: Int, text: String) {
        viewModelScope.launch {
            val user = profile.value?.username ?: "ABDULWAJID"
            repository.addComment(
                ShoutComment(shoutId = shoutId, username = user, text = text)
            )
        }
    }

    // Direct Messaging actions
    fun sendDirectMessage(content: String, receiver: String) {
        viewModelScope.launch {
            // Inserts sent message
            repository.sendMessage(sender = "Me", content = content, isFromMe = true)
            
            // Generate auto AI reply
            _isGenerating.value = true
            val systemPrompt = """
                You are $receiver, a friendly retro ChatLanka user. 
                You are replying to a direct private message from ABDULWAJID in the platform inbox.
                Write a single warm sentence. Do not use markdown. Keep it retro and cozy.
            """
            val prompt = "ABDULWAJID sends you this DM: \"$content\". Write a quick 1-sentence reply as $receiver."
            
            delay(1000) // network latency simulation
            val reply = GeminiClient.generateResponse(prompt, systemInstruction = systemPrompt)
            repository.sendMessage(sender = receiver, content = reply, isFromMe = false)
            _isGenerating.value = false
        }
    }

    // Forum board actions
    fun createTopic(title: String, body: String) {
        viewModelScope.launch {
            val user = profile.value?.username ?: "ABDULWAJID"
            repository.insertTopic(
                ForumTopic(title = title, creator = user, content = body)
            )
            // Auto topic reply simulation by Gemini or simulated member after 4 seconds
            delay(4000)
            val randomReplier = onlineFriends.shuffled().first().first
            val replyBody = "Awesome discussion starting! Let us write more columns."
            repository.insertNotification(
                title = "New board reply",
                body = "$randomReplier commented on your topic: \"$title\"!"
            )
        }
    }

    // Profile settings
    fun updateTerminalProfile(name: String, status: String) {
        viewModelScope.launch {
            repository.updateProfile(name, status)
        }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            db.shoutDao().deleteAllShouts()
            db.commentDao().deleteAllComments()
            db.forumTopicDao().deleteAllTopics()
            db.directMessageDao().deleteAllMessages()
            db.notificationDao().deleteAllNotifications()
            repository.initializeDatabaseIfEmpty(viewModelScope)
        }
    }

    private fun triggerSimulationOrGemini(userText: String) {
        viewModelScope.launch {
            _isGenerating.value = true
            
            // Choose a bot member to reply
            val activeBots = listOf("Humble_Boy", "sampee", "kala-graham", "TechBoY")
            val botChosen = activeBots.shuffled().first()
            
            // Wait slightly matching realistic retro typing speed
            delay(2000)
            
            val response = GeminiClient.generateResponse(
                prompt = "A user named ABDULWAJID just shouted: \"$userText\" inside the ChatLanka ShoutBox. Write a response as $botChosen, addressing their message in 1 friendly sentence.",
                systemInstruction = "You are $botChosen. You are talking in a vintage ChatLanka.com ShoutBox feed."
            )
            
            // Insert comment on this newly made shout as the response!
            val recentShouts = shouts.value
            val targetShout = recentShouts.firstOrNull { it.username == (profile.value?.username ?: "ABDULWAJID") }
            if (targetShout != null) {
                repository.addComment(
                    ShoutComment(shoutId = targetShout.id, username = botChosen, text = response)
                )
                repository.insertNotification(
                    title = "New Reply In ShoutBox",
                    body = "$botChosen replied to your shout!"
                )
            }
            _isGenerating.value = false
        }
    }
}
