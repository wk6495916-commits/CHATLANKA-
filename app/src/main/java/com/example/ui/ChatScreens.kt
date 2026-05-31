package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainChatLankaApp(viewModel: ChatViewModel) {
    val activeScreen by viewModel.activeScreen.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val userProfile by viewModel.profile.collectAsStateWithLifecycle()
    val shouts by viewModel.shouts.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    var shoutTextToPost by remember { mutableStateOf("") }
    var selectedGreeting by remember { mutableStateOf("None") }
    var selectedEmoji by remember { mutableStateOf("None") }

    val selectedShoutId by viewModel.selectedShoutId.collectAsStateWithLifecycle()
    val comments by viewModel.shoutComments.collectAsStateWithLifecycle()

    // Screen layout
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        containerColor = RetroBgGray,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Classical Blue Sky/Ocean Gradient Web Header Background
            TopWebHeader(
                activeScreen = activeScreen,
                onScreenSelected = { viewModel.setScreen(it) },
                shoutsCount = shouts.size,
                unreadNotifications = notifications.filter { !it.isRead }.size
            )

            // 2. Neon lime retro top message bar "Good Evening USERNAME!"
            userProfile?.let {
                GreetingBar(profile = it) {
                    viewModel.setScreen(RetroScreen.PROFILE)
                }
            }

            // 3. Highlight Daily Box
            DailyHighlightsCard(viewModel = viewModel)

            // 4. Yellow Scrolling Marquee System Warning
            RetroMarqueeTextRow()

            // 5. Active Screen Display Frame
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(2.dp, Color.Gray, RoundedCornerShape(2.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(2.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    when (activeScreen) {
                        RetroScreen.CHAT -> {
                            ShoutBoxScreen(
                                viewModel = viewModel,
                                shouts = shouts,
                                textInput = shoutTextToPost,
                                onTextChange = { shoutTextToPost = it },
                                greetingBanner = selectedGreeting,
                                onGreetingSelected = { selectedGreeting = it },
                                emojiStyle = selectedEmoji,
                                onEmojiSelected = { selectedEmoji = it },
                                isGenerating = isGenerating
                            )
                        }
                        RetroScreen.INBOX -> {
                            InboxScreen(viewModel = viewModel, isGenerating = isGenerating)
                        }
                        RetroScreen.FORUM -> {
                            ForumScreen(viewModel = viewModel)
                        }
                        RetroScreen.FRIENDS -> {
                            FriendsScreen(viewModel = viewModel)
                        }
                        RetroScreen.NOTIFICATIONS -> {
                            NotificationsScreen(viewModel = viewModel, notifications = notifications)
                        }
                        RetroScreen.PROFILE -> {
                            ProfileScreen(viewModel = viewModel, profile = userProfile)
                        }
                    }
                }
            }
        }
    }

    // Comments Sheet overlay when comment is tapped
    if (selectedShoutId != null) {
        val shout = shouts.find { it.id == selectedShoutId }
        shout?.let {
            CommentsDialog(
                shout = it,
                comments = comments,
                onDismiss = { viewModel.selectShoutForComments(null) },
                onAddComment = { content ->
                    viewModel.addComment(it.id, content)
                }
            )
        }
    }
}

// 1. Top Web Header with gradient and text links modeled directly on ChatLanka
@Composable
fun TopWebHeader(
    activeScreen: RetroScreen,
    onScreenSelected: (RetroScreen) -> Unit,
    shoutsCount: Int,
    unreadNotifications: Int
) {
    var currentTimeString by remember { mutableStateOf("") }

    // Live dynamic calendar clock matching format: "Sun 31 May 26 - 22:04"
    LaunchedEffect(Unit) {
        while (true) {
            val formatter = SimpleDateFormat("EEE dd MMM yy - HH:mm", Locale.US)
            currentTimeString = formatter.format(Date())
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(RetroWebBlue, RetroWebOcean)
                )
            )
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                drawLine(
                    color = Color.Black,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
            }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ChatLanka Brand Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Logo",
                tint = RetroYellowHighlight,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "ChatLanka.com",
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = Color.White,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 3f
                    )
                ),
                modifier = Modifier.testTag("app_title")
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dynamic Live Clock
        Text(
            text = currentTimeString,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Row of web text navigation links
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val menuItems = listOf(
                Pair(RetroScreen.INBOX, "Inbox"),
                Pair(RetroScreen.FRIENDS, "Friends"),
                Pair(RetroScreen.FORUM, "Forum"),
                Pair(RetroScreen.CHAT, "Chat($shoutsCount)"),
                Pair(RetroScreen.NOTIFICATIONS, "Alerts($unreadNotifications)"),
                Pair(RetroScreen.PROFILE, "Profile")
            )

            menuItems.forEachIndexed { index, item ->
                val selected = activeScreen == item.first
                Text(
                    text = item.second,
                    color = if (selected) RetroYellowHighlight else Color.White,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = FontFamily.Serif,
                    fontSize = 13.sp,
                    textDecoration = if (selected) TextDecoration.Underline else TextDecoration.None,
                    modifier = Modifier
                        .clickable { onScreenSelected(item.first) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .testTag("nav_link_${item.second.lowercase().take(4)}")
                )
                if (index < menuItems.size - 1) {
                    Text(text = "|", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

// 2. Greeting bar: Good Evening User!
@Composable
fun GreetingBar(profile: UserProfile, onProfileClick: () -> Unit) {
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RetroGreenBar)
            .border(1.dp, Color.Black)
            .clickable { onProfileClick() }
            .padding(vertical = 5.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$greeting ",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            fontSize = 15.sp
        )
        Text(
            text = "${profile.username.uppercase()}!",
            color = RetroYellowHighlight,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            fontSize = 15.sp,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(1f, 1f),
                    blurRadius = 1f
                )
            ),
            modifier = Modifier.testTag("greeting_username")
        )
    }
}

// 3. Highlight Daily Box (Member, Topic, Club) with custom MP3 visualizer
@Composable
fun DailyHighlightsCard(viewModel: ChatViewModel) {
    var isPlayingMusic by remember { mutableStateOf(false) }
    val animatedProgress = rememberInfiniteTransition(label = "music")
    val visualizerHeight by animatedProgress.animateFloat(
        initialValue = 4f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vis"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, RetroBorderMint, RoundedCornerShape(2.dp)),
        colors = CardDefaults.cardColors(containerColor = RetroLightMint),
        shape = RoundedCornerShape(2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Member line
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text(
                    text = "Member of the day : ",
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "TechBoY",
                    color = Color(0xFFBF360C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.clickable {
                        viewModel.setScreen(RetroScreen.FRIENDS)
                    }
                )
            }

            // Topic line
            Row(
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .clickable { isPlayingMusic = !isPlayingMusic }
            ) {
                Text(
                    text = "Topic of the day : ",
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "Sunday vibez.mp3.*DIFFERENT PATTERN*ftQuavo246[champion]",
                    color = Color(0xFF0D47A1),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Serif,
                    textDecoration = TextDecoration.Underline,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                if (isPlayingMusic) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Row(
                        modifier = Modifier.height(18.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        repeat(4) { i ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 1.dp)
                                    .width(3.dp)
                                    .height(
                                        if (i % 2 == 0) (visualizerHeight).dp else (visualizerHeight * 0.6f).dp
                                    )
                                    .background(Color(0xFFE65100))
                            )
                        }
                    }
                }
            }

            // Club line
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text(
                    text = "Club of the Week : ",
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "HARMONY CIRCLE",
                    color = Color(0xFF4A148C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Serif
                )
            }

            if (isPlayingMusic) {
                Text(
                    text = " Now streaming high-fidelity lo-fi MP3 beats (24kbps Dial-UP optimized)...",
                    color = Color.DarkGray,
                    fontStyle = FontStyle.Italic,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// 4. Yellow Scrolling Marquee System Warning
@Composable
fun RetroMarqueeTextRow() {
    val scrollState = rememberScrollState()
    var offset by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            scrollState.animateScrollTo(
                value = scrollState.value + 4,
                animationSpec = tween(50, easing = LinearEasing)
            )
            if (scrollState.value >= scrollState.maxValue) {
                scrollState.scrollTo(0)
            }
            delay(50)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEEEEEE))
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = Color.Gray,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
            }
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState, enabled = false)
                .fillMaxWidth()
        ) {
            Text(
                text = "We will remove all fake accounts soon!    •    Enjoy genuine Discussion Chats here on ChatLanka!!!    •    Respect community standards    •     Sunday vibes are live. Join club channels now!",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = FontFamily.Serif,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
            // Duplicate text to enable infinite feeling scroll
            Text(
                text = "We will remove all fake accounts soon!    •    Enjoy genuine Discussion Chats here on ChatLanka!!!    •    Respect community standards    •     Sunday vibes are live. Join club channels now!",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = FontFamily.Serif,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
        }
    }
}

// 5a. CHAT SCREEN: ShoutBox feed list with entry drawer
@Composable
fun ShoutBoxScreen(
    viewModel: ChatViewModel,
    shouts: List<Shout>,
    textInput: String,
    onTextChange: (String) -> Unit,
    greetingBanner: String,
    onGreetingSelected: (String) -> Unit,
    emojiStyle: String,
    onEmojiSelected: (String) -> Unit,
    isGenerating: Boolean
) {
    val focusManager = LocalFocusManager.current

    Column {
        // Shoutbox header (Yellow with border)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(RetroShoutBoxYellow)
                .border(1.dp, Color.Black)
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ShoutBox",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Shouts List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 450.dp)
        ) {
            if (isGenerating) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = RetroWebBlue,
                    trackColor = Color.LightGray
                )
                Text(
                    text = "Generating response as other members typing...",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            if (shouts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No shouts here yet. Be the first to shout!", color = Color.Gray, fontSize = 13.sp)
                }
            } else {
                shouts.forEach { shout ->
                    ShoutCard(
                        shout = shout,
                        onLike = { viewModel.likeShout(shout.id) },
                        onTag = { onTextChange("@${shout.username} ") },
                        onComments = { viewModel.selectShoutForComments(shout.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray, thickness = 1.dp)

        // Shoutbox inputs form
        Text(
            text = "Compose a Shout:",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            fontFamily = FontFamily.Serif,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Greeting decoration chooser
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Title:", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(55.dp))
            listOf("None", "Good Evening", "Hi").forEach { title ->
                val active = greetingBanner == title
                Card(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable { onGreetingSelected(title) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (active) RetroWebBlue else Color.LightGray
                    ),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        color = if (active) Color.White else Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Emoji accessory chooser
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sticker:", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(55.dp))
            listOf("None", "Smiley", "Eyes").forEach { emoji ->
                val active = emojiStyle == emoji
                Card(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable { onEmojiSelected(emoji) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (active) RetroWebBlue else Color.LightGray
                    ),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(
                        text = emoji,
                        fontSize = 11.sp,
                        color = if (active) Color.White else Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Message input text field styled in retro slate box
        OutlinedTextField(
            value = textInput,
            onValueChange = onTextChange,
            placeholder = { Text("Write a shout here...", fontSize = 13.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("shout_input_field"),
            textStyle = TextStyle(fontSize = 13.sp),
            maxLines = 3,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (textInput.isNotBlank()) {
                    viewModel.postShout(textInput, greetingBanner, emojiStyle)
                    onTextChange("")
                    focusManager.clearFocus()
                }
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RetroWebBlue,
                unfocusedBorderColor = Color.Gray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFF9F9F9)
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.postShout(textInput, greetingBanner, emojiStyle)
                        onTextChange("")
                        focusManager.clearFocus()
                    }
                },
                modifier = Modifier.testTag("post_shout_btn"),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RetroWebBlue)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Shout!", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

// Custom Retro WordArt styling components for the Shouts Box
@Composable
fun ShoutCard(
    shout: Shout,
    onLike: () -> Unit,
    onTag: () -> Unit,
    onComments: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(2.dp))
            .shadow(1.dp, RoundedCornerShape(2.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F6F6)),
        shape = RoundedCornerShape(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            // Optional Center-Aligned Fancy Title WordArt Block
            if (shout.greetingBanner != "None") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (shout.greetingBanner) {
                        "Good Evening" -> {
                            // Gold style WordArt replica
                            Text(
                                text = "Good Evening",
                                style = TextStyle(
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontStyle = FontStyle.Italic,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color(0xFFFFEA00), Color(0xFFE65100))
                                    ),
                                    shadow = Shadow(
                                        color = Color.DarkGray,
                                        offset = Offset(2f, 2f),
                                        blurRadius = 1f
                                    )
                                )
                            )
                        }
                        "Hi" -> {
                            // Pink glow bubbly text
                            Text(
                                text = "Hi",
                                style = TextStyle(
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFF007F),
                                    shadow = Shadow(
                                        color = Color(0xFFFF80DF),
                                        offset = Offset(0f, 0f),
                                        blurRadius = 8f
                                    )
                                )
                            )
                        }
                    }
                }
            }

            // Optional accessory visual sticker drawn on Canvas (No generic AI image slop!)
            if (shout.emojiStyle != "None") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(bottom = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(60.dp)) {
                        when (shout.emojiStyle) {
                            "Smiley" -> {
                                // Draw retro yellow smiley face
                                drawCircle(
                                    color = Color(0xFFFFEB3B),
                                    radius = size.width / 2f
                                )
                                drawCircle(
                                    color = Color.Black,
                                    radius = size.width / 2f,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                // Left eye
                                drawCircle(
                                    color = Color.Black,
                                    radius = 4.dp.toPx(),
                                    center = Offset(size.width * 0.33f, size.height * 0.4f)
                                )
                                // Right eye
                                drawCircle(
                                    color = Color.Black,
                                    radius = 4.dp.toPx(),
                                    center = Offset(size.width * 0.67f, size.height * 0.4f)
                                )
                                // Smiley mouth path
                                val mouthPath = Path().apply {
                                    arcTo(
                                        rect = androidx.compose.ui.geometry.Rect(
                                            left = size.width * 0.25f,
                                            top = size.height * 0.4f,
                                            right = size.width * 0.75f,
                                            bottom = size.height * 0.8f
                                        ),
                                        startAngleDegrees = 0f,
                                        sweepAngleDegrees = 180f,
                                        forceMoveTo = true
                                    )
                                }
                                drawPath(
                                    path = mouthPath,
                                    color = Color.Black,
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }
                            "Eyes" -> {
                                // Draw retro blue visual eye silhouette
                                val eyePathLeft = Path().apply {
                                    moveTo(10.dp.toPx(), 30.dp.toPx())
                                    quadraticTo(25.dp.toPx(), 15.dp.toPx(), 40.dp.toPx(), 30.dp.toPx())
                                    quadraticTo(25.dp.toPx(), 45.dp.toPx(), 10.dp.toPx(), 30.dp.toPx())
                                }
                                val eyePathRight = Path().apply {
                                    moveTo(50.dp.toPx(), 30.dp.toPx())
                                    quadraticTo(65.dp.toPx(), 15.dp.toPx(), 80.dp.toPx(), 30.dp.toPx())
                                    quadraticTo(65.dp.toPx(), 45.dp.toPx(), 50.dp.toPx(), 30.dp.toPx())
                                }
                                drawPath(
                                    path = eyePathLeft,
                                    color = Color(0xFF3F51B5),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                drawPath(
                                    path = eyePathRight,
                                    color = Color(0xFF3F51B5),
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                // Pupils
                                drawCircle(
                                    color = Color(0xFF3F51B5),
                                    radius = 5.dp.toPx(),
                                    center = Offset(25.dp.toPx(), 30.dp.toPx())
                                )
                                drawCircle(
                                    color = Color(0xFF3F51B5),
                                    radius = 5.dp.toPx(),
                                    center = Offset(65.dp.toPx(), 30.dp.toPx())
                                )
                                // Eyebrows
                                drawLine(
                                    color = Color(0xFF3F51B5),
                                    start = Offset(8.dp.toPx(), 16.dp.toPx()),
                                    end = Offset(38.dp.toPx(), 22.dp.toPx()),
                                    strokeWidth = 3.dp.toPx()
                                )
                                drawLine(
                                    color = Color(0xFF3F51B5),
                                    start = Offset(48.dp.toPx(), 22.dp.toPx()),
                                    end = Offset(78.dp.toPx(), 16.dp.toPx()),
                                    strokeWidth = 3.dp.toPx()
                                )
                            }
                        }
                    }
                }
            }

            // User handle and text line
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Colored dot representation of profile avatar
                val userColor = when (shout.username) {
                    "Humble_Boy" -> RetroUserGreen
                    "sampee" -> RetroUserBlue
                    "kala-graham" -> RetroUserIndigo
                    "TechBoY" -> Color(0xFFE65100)
                    else -> Color.Black
                }

                Box(
                    modifier = Modifier
                        .padding(top = 4.dp, end = 6.dp)
                        .size(8.dp)
                        .background(userColor, CircleShape)
                )

                // Render name and text content inline with specific formatting
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = shout.username,
                            color = userColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            fontSize = 14.sp
                        )
                        Text(
                            text = ":",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                    Text(
                        text = shout.text,
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Actions Strip: Like | Tag | Comments
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.LightGray)
                    .background(Color(0xFFEEEEEE))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbs up Like Action
                Row(
                    modifier = Modifier
                        .clickable { onLike() }
                        .padding(vertical = 2.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Like",
                        tint = Color(0xFF3F51B5),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Like [${shout.likesCount}]",
                        fontSize = 11.sp,
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Tag Action
                Row(
                    modifier = Modifier
                        .clickable { onTag() }
                        .padding(vertical = 2.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Tag",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Tag",
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Comments Bubble Action
                Row(
                    modifier = Modifier
                        .clickable { onComments() }
                        .padding(vertical = 2.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Comments",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Comments [${shout.commentsCount}]",
                        fontSize = 11.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// 5b. COMMENTS PANEL: View & submit replies to a shout
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsDialog(
    shout: Shout,
    comments: List<ShoutComment>,
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Shout Comments: ${shout.username}",
                fontSize = 16.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)) {
                // Header original message info card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                    shape = RoundedCornerShape(2.dp),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "${shout.username}: ${shout.text}",
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color.Black
                        )
                    }
                }

                // Replies feed Scroll list
                Box(modifier = Modifier.weight(1f)) {
                    if (comments.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No replies yet. Type one below!", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(comments) { comment ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F1F1)),
                                    shape = RoundedCornerShape(2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(6.dp)) {
                                        Row {
                                            Text(
                                                text = comment.username,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1565C0),
                                                fontSize = 12.sp
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = "reply",
                                                fontStyle = FontStyle.Italic,
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        Text(
                                            text = comment.text,
                                            fontSize = 12.sp,
                                            color = Color.Black,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Write comment...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f).testTag("comment_field"),
                        textStyle = TextStyle(fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RetroWebBlue
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                onAddComment(commentText)
                                commentText = ""
                                focusManager.clearFocus()
                            }
                        },
                        modifier = Modifier.testTag("submit_comment_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Add comment")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = RetroWebBlue, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(4.dp)
    )
}

// 5c. INBOX SCREEN: Direct Messaging
@Composable
fun InboxScreen(viewModel: ChatViewModel, isGenerating: Boolean) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    var selectedFriend by remember { mutableStateOf("Humble_Boy") }
    var dmText by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val chatPartners = viewModel.onlineFriends.map { it.first }

    Column {
        Text(
            text = "Personal Terminal Inbox",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Select partner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 10.dp)
        ) {
            chatPartners.forEach { partner ->
                val active = selectedFriend == partner
                Card(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable { selectedFriend = partner },
                    colors = CardDefaults.cardColors(
                        containerColor = if (active) RetroWebBlue else Color.LightGray
                    ),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text(
                        text = partner,
                        color = if (active) Color.White else Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Divider(color = Color.LightGray)

        // Messages list
        val filteredMessages = messages.filter {
            (it.sender == selectedFriend && !it.isFromMe) || (it.sender == "Me")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Color(0xFFECEFF1))
                .border(1.dp, Color.Gray)
                .padding(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (filteredMessages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No messages with $selectedFriend yet.", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredMessages) { msg ->
                            val isMe = msg.isFromMe
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isMe) RetroWebBlue else Color.White
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, Color.Gray),
                                    modifier = Modifier.widthIn(max = 220.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = if (isMe) "Me:" else "${selectedFriend}:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isMe) RetroYellowHighlight else Color.DarkGray
                                        )
                                        Text(
                                            text = msg.content,
                                            fontSize = 13.sp,
                                            color = if (isMe) Color.White else Color.Black,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isGenerating) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp))
                Text("typing reply...", fontStyle = FontStyle.Italic, fontSize = 10.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Message input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = dmText,
                onValueChange = { dmText = it },
                placeholder = { Text("Type private message to $selectedFriend...", fontSize = 13.sp) },
                modifier = Modifier.weight(1f).testTag("dm_field"),
                textStyle = TextStyle(fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RetroWebBlue)
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(
                onClick = {
                    if (dmText.isNotBlank()) {
                        viewModel.sendDirectMessage(dmText, selectedFriend)
                        dmText = ""
                        focusManager.clearFocus()
                    }
                },
                modifier = Modifier.testTag("send_dm_btn")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send Direct Message")
            }
        }
    }
}

// 5d. FORUM SCREEN: Threaded Board
@Composable
fun ForumScreen(viewModel: ChatViewModel) {
    val topics by viewModel.topics.collectAsStateWithLifecycle()
    var displayNewTopicForm by remember { mutableStateOf(false) }

    var topicTitle by remember { mutableStateOf("") }
    var topicBody by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Discussion Boards",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = { displayNewTopicForm = !displayNewTopicForm },
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RetroGreenBar)
            ) {
                Text(if (displayNewTopicForm) "View board" else "Write columns", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (displayNewTopicForm) {
            // New column form
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = topicTitle,
                onValueChange = { topicTitle = it },
                label = { Text("Topic Title (e.g. Quavo hits)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = topicBody,
                onValueChange = { topicBody = it },
                label = { Text("Discuss detail body content...") },
                modifier = Modifier.fillMaxWidth().height(100.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (topicTitle.isNotBlank() && topicBody.isNotBlank()) {
                        viewModel.createTopic(topicTitle, topicBody)
                        topicTitle = ""
                        topicBody = ""
                        displayNewTopicForm = false
                        focusManager.clearFocus()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(2.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RetroWebBlue)
            ) {
                Text("Publish to board")
            }
        } else {
            // Topic lists
            topics.forEach { topic ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, Color.LightGray),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = topic.title,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D47A1),
                            fontFamily = FontFamily.Serif,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Created by : ${topic.creator} • 2 replies",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = topic.content,
                            fontSize = 12.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

// 5e. FRIENDS SCREEN: Members Directory
@Composable
fun FriendsScreen(viewModel: ChatViewModel) {
    Column {
        Text(
            text = "Active Buddies",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        viewModel.onlineFriends.forEach { friend ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                if (friend.second == "online") RetroLimeGreen else Color.Gray,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = friend.first,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = {
                            viewModel.setScreen(RetroScreen.INBOX)
                        },
                        shape = RoundedCornerShape(2.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RetroWebBlue),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Text("DM", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// 5f. NOTIFICATIONS SCREEN: Site alerts log
@Composable
fun NotificationsScreen(viewModel: ChatViewModel, notifications: List<ChatNotification>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Activity Terminal Log",
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
        }

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No activities recorded on terminal yet.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            notifications.forEach { node ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                    shape = RoundedCornerShape(2.dp),
                    border = BorderStroke(1.dp, Color.Yellow)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = node.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                        Text(
                            text = node.body,
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// 5g. PROFILE SCREEN: Configuration & Profile terminal
@Composable
fun ProfileScreen(viewModel: ChatViewModel, profile: UserProfile?) {
    var usernameField by remember { mutableStateOf(profile?.username ?: "ABDULWAJID") }
    var statusField by remember { mutableStateOf(profile?.status ?: "Member of the day") }
    val focusManager = LocalFocusManager.current

    Column {
        Text(
            text = "Personal Terminal Profile",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFECEFF1)),
            border = BorderStroke(1.dp, Color.Gray)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = "Terminal Stats",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Profile views count : ${profile?.profileViews ?: 2} times", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                Text(text = "Joined community logs : ${profile?.joinedDate ?: "Sun 31 May 26"}", fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        }

        OutlinedTextField(
            value = usernameField,
            onValueChange = { usernameField = it },
            label = { Text("Custom Handle Name") },
            modifier = Modifier.fillMaxWidth().testTag("username_edit_field")
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = statusField,
            onValueChange = { statusField = it },
            label = { Text("Profile Status Quote") },
            modifier = Modifier.fillMaxWidth().testTag("status_edit_field")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (usernameField.isNotBlank()) {
                    viewModel.updateTerminalProfile(usernameField, statusField)
                    focusManager.clearFocus()
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("save_profile_btn"),
            shape = RoundedCornerShape(2.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RetroWebBlue)
        ) {
            Text("Update Terminal Settings")
        }

        Spacer(modifier = Modifier.height(15.dp))

        Divider()

        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = {
                viewModel.resetDatabase()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(2.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text("Reset & Restore Default Screenshot State", color = Color.White)
        }
    }
}
