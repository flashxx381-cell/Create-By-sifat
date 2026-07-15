package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.NoorShieldViewModel
import kotlinx.coroutines.delay

@Composable
fun NoorShieldApp(viewModel: NoorShieldViewModel) {
    var showSplash by remember { mutableStateOf(true) }

    // Splash screen timer
    LaunchedEffect(Unit) {
        delay(2500)
        showSplash = false
    }

    MyApplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            Crossfade(
                targetState = showSplash,
                animationSpec = tween(500),
                label = "SplashTransition"
            ) { isSplash ->
                if (isSplash) {
                    SplashScreen(onDismiss = { showSplash = false })
                } else {
                    MainAppContent(viewModel)
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onDismiss: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, Color(0xFF030712))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Splash image mosque silhouette & crescent moon
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .drawBehind {
                        drawCircle(
                            color = AccentBlue.copy(alpha = 0.1f * glowScale),
                            radius = size.minDimension / 1.6f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_mosque_splash),
                    contentDescription = "নূর শিল্ড মস্ক স্প্ল্যাশ",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .border(1.dp, AccentBlue.copy(alpha = 0.4f), CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "নূর শিল্ড",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    shadow = Shadow(
                        color = AccentBlue,
                        offset = Offset(0f, 0f),
                        blurRadius = 15f
                    )
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Noor Shield • আত্মিক সুরক্ষাকবচ",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AccentBlue,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = AccentBlue,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue.copy(alpha = 0.6f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.3f)),
                modifier = Modifier.testTag("enter_button")
            ) {
                Text(text = "অ্যাপে প্রবেশ করুন", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: NoorShieldViewModel) {
    val currentTabState by viewModel.currentTab.collectAsStateWithLifecycle()
    val isFocusMode by viewModel.isFocusModeActive.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            if (!isFocusMode) {
                NoorShieldBottomNavigation(
                    currentTab = currentTabState,
                    onTabSelected = { viewModel.currentTab.value = it }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
        ) {
            if (isFocusMode) {
                FocusModeScreen(viewModel)
            } else {
                when (currentTabState) {
                    "home" -> HomeScreen(viewModel)
                    "quran" -> QuranScreen(viewModel)
                    "prayer" -> PrayerScreen(viewModel)
                    "tasbih" -> TasbihScreen(viewModel)
                    "profile" -> ProfileScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun NoorShieldBottomNavigation(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .border(width = (0.5).dp, color = Color.White.copy(alpha = 0.05f))
            .padding(vertical = 8.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                NavigationTab("home", "হোম", Icons.Default.Home),
                NavigationTab("quran", "কুরআন", Icons.Default.Book),
                NavigationTab("prayer", "নামাজ", Icons.Default.Schedule),
                NavigationTab("tasbih", "তাসবিহ", Icons.Default.Casino), // Use Casino as beautiful counter representer
                NavigationTab("profile", "প্রোফাইল", Icons.Default.Person)
            )

            tabs.forEach { tab ->
                val isSelected = currentTab == tab.id
                val color = if (isSelected) AccentBlue else TextMuted
                val iconSize = if (isSelected) 26.dp else 22.dp

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onTabSelected(tab.id) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("nav_tab_${tab.id}")
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = color,
                        modifier = Modifier.size(iconSize)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.label,
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = color
                        )
                    )
                }
            }
        }
    }
}

data class NavigationTab(val id: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

// --- HOME SCREEN ---
@Composable
fun HomeScreen(viewModel: NoorShieldViewModel) {
    val nextPrayer by viewModel.nextPrayerName.collectAsStateWithLifecycle()
    val countdown by viewModel.countdownStr.collectAsStateWithLifecycle()
    val gregDate by viewModel.todayDateStr.collectAsStateWithLifecycle()
    val hijriDate by viewModel.hijriDateStr.collectAsStateWithLifecycle()
    val locationCity by viewModel.selectedDivision.collectAsStateWithLifecycle()

    // Trigger loading status
    LaunchedEffect(Unit) {
        viewModel.loadTodayPrayerStatus()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "আসসালামু আলাইকুম",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = gregDate,
                            style = TextStyle(fontSize = 12.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
                        )
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .background(TextMuted, CircleShape)
                        )
                        Text(
                            text = hijriDate,
                            style = TextStyle(fontSize = 11.sp, color = TextMuted)
                        )
                    }
                }

                // Profile Image / App Icon
                Image(
                    painter = painterResource(id = R.drawable.img_app_icon_fg),
                    contentDescription = "প্রোফাইল লোগো",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .border(1.dp, AccentBlue.copy(alpha = 0.3f), CircleShape)
                )
            }
        }

        item {
            // Next Prayer Countdown Glass Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryBlue.copy(alpha = 0.5f), CardBackground)
                        )
                    )
                    .border(1.dp, AccentBlue.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "পরবর্তী নামাজ",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentBlue,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = nextPrayer,
                                style = TextStyle(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    shadow = Shadow(color = AccentBlue.copy(alpha = 0.6f), blurRadius = 8f)
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "বাকি সময়",
                                style = TextStyle(fontSize = 12.sp, color = TextMuted)
                            )
                            Text(
                                text = countdown,
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.activateFocusMode() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = DarkBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activate_focus_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(8.dp)
                                    .background(Color.Red, CircleShape)
                            )
                            Text(text = "নামাজ মোড চালু করুন", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            // Quick Action Grid
            Text(
                text = "কুইক অ্যাক্সেস",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val actions = listOf(
                    QuickActionItem("📖", "কুরআন", "quran"),
                    QuickActionItem("📿", "তাসবিহ", "tasbih"),
                    QuickActionItem("🧭", "নামাজ", "prayer"), // redirects to Prayer Compass
                    QuickActionItem("🕌", "দুয়া", "quran") // redirected/used inside
                )

                actions.forEach { act ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .clickable { viewModel.currentTab.value = act.tabRedirect },
                        colors = CardDefaults.cardColors(containerColor = GlassWhite),
                        border = BorderStroke(1.dp, GlassWhiteBorder),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = act.icon, fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = act.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                        }
                    }
                }
            }
        }

        item {
            // Daily Inspiration - Quran Verse
            GlassmorphicInspirationCard(
                tag = "আজকের আয়াত",
                content = "\"${IslamicData.dailyVerse.textBengali}\"",
                reference = "[সুরা আল-ইনশিরাহ: ৬]"
            )
        }

        item {
            // Daily Hadith Card
            GlassmorphicInspirationCard(
                tag = "আজকের হাদিস",
                content = "\"${IslamicData.dailyHadith.textBengali}\"",
                reference = "- ${IslamicData.dailyHadith.narrator} • ${IslamicData.dailyHadith.collection}"
            )
        }

        item {
            // Daily Dua Card
            GlassmorphicInspirationCard(
                tag = "আজকের দুয়া",
                content = "${IslamicData.dailyDua.title}: \"${IslamicData.dailyDua.meaningBengali}\"",
                reference = "উচ্চারণ: ${IslamicData.dailyDua.pronunciation}"
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

data class QuickActionItem(val icon: String, val label: String, val tabRedirect: String)

@Composable
fun GlassmorphicInspirationCard(
    tag: String,
    content: String,
    reference: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassWhite)
            .border(1.dp, GlassWhiteBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 12.dp)
                        .background(AccentBlue, RoundedCornerShape(2.dp))
                )
                Text(
                    text = tag,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextWhite,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = reference,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = TextMuted,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// --- PRAYER FOCUS MODE ---
@Composable
fun FocusModeScreen(viewModel: NoorShieldViewModel) {
    val remaining by viewModel.focusRemainingSec.collectAsStateWithLifecycle()
    val totalMin by viewModel.focusTotalDurationMin.collectAsStateWithLifecycle()

    val progress = if (totalMin > 0) {
        remaining.toFloat() / (totalMin * 60f)
    } else 0f

    val min = remaining / 60
    val sec = remaining % 60

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        // Glowing background mosque silhoutte
        Image(
            painter = painterResource(id = R.drawable.img_mosque_splash),
            contentDescription = "মস্ক ব্যাকগ্রাউন্ড",
            modifier = Modifier
                .size(300.dp)
                .rotate(15f)
                .align(Alignment.Center)
                .drawBehind {
                    drawCircle(color = AccentBlue.copy(alpha = 0.05f * pulseAlpha))
                },
            alpha = 0.15f
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "নামাজ মনোযোগ মোড",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    shadow = Shadow(color = AccentBlue, blurRadius = 10f)
                )
            )

            Text(
                text = "নিস্তব্ধতা বজায় রাখুন এবং মহান আল্লাহর ধ্যানে নিমগ্ন হোন।",
                style = TextStyle(fontSize = 13.sp, color = TextMuted, textAlign = TextAlign.Center),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Beautiful Circular Countdown Progress
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    color = AccentBlue,
                    strokeWidth = 6.dp,
                    trackColor = GlassWhite,
                    modifier = Modifier.fillMaxSize()
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%02d:%02d", min, sec),
                        style = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    )
                    Text(
                        text = "অবশিষ্ট সময়",
                        style = TextStyle(fontSize = 11.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.deactivateFocusMode() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.2f),
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("deactivate_focus_button")
            ) {
                Text(text = "নামাজ মোড বন্ধ করুন", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- QURAN SCREEN ---
@Composable
fun QuranScreen(viewModel: NoorShieldViewModel) {
    val selectedSurahState by viewModel.selectedSurah.collectAsStateWithLifecycle()

    Crossfade(
        targetState = selectedSurahState,
        animationSpec = tween(400),
        label = "QuranNavigation"
    ) { surah ->
        if (surah == null) {
            SurahListScreen(viewModel)
        } else {
            SurahReadScreen(viewModel, surah)
        }
    }
}

@Composable
fun SurahListScreen(viewModel: NoorShieldViewModel) {
    val searchState by viewModel.quranSearchQuery.collectAsStateWithLifecycle()

    val filteredSurahs = remember(searchState) {
        IslamicData.surahList.filter {
            it.nameBengali.contains(searchState, ignoreCase = true) ||
                    it.nameEnglish.contains(searchState, ignoreCase = true) ||
                    it.number.toString().contains(searchState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "আল কুরআন",
            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White),
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        // Custom search bar
        OutlinedTextField(
            value = searchState,
            onValueChange = { viewModel.quranSearchQuery.value = it },
            placeholder = { Text("সুরা নম্বর বা নাম দিয়ে খুঁজুন...", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "খুঁজুন", tint = AccentBlue) },
            trailingIcon = {
                if (searchState.isNotEmpty()) {
                    IconButton(onClick = { viewModel.quranSearchQuery.value = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "মুছে ফেলুন", tint = TextMuted)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = GlassWhiteBorder,
                focusedContainerColor = GlassWhite,
                unfocusedContainerColor = GlassWhite
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("quran_search")
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp)
        ) {
            items(filteredSurahs) { surah ->
                Card(
                    onClick = { viewModel.selectedSurah.value = surah },
                    colors = CardDefaults.cardColors(containerColor = GlassWhite),
                    border = BorderStroke(1.dp, GlassWhiteBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("surah_${surah.number}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Surah Number inside glowing circle
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(PrimaryBlue.copy(alpha = 0.3f), CircleShape)
                                    .border(1.dp, AccentBlue.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = viewModel.getBengaliNumber(surah.number),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = surah.nameBengali,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${surah.nameEnglish} • ${surah.type} • ${viewModel.getBengaliNumber(surah.totalVerses)} আয়াত",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        // Arabic Name
                        Text(
                            text = surah.nameArabic,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentBlue,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SurahReadScreen(viewModel: NoorShieldViewModel, surah: Surah) {
    var isMuted by remember { mutableStateOf(false) }
    var isPlayingAudio by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Top app bar inside screen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.selectedSurah.value = null },
                modifier = Modifier.testTag("back_to_surahs")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "ফিরে যান", tint = Color.White)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = surah.nameBengali,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${surah.nameEnglish} • ${surah.type}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            // Audio Player Controls Simulation
            IconButton(
                onClick = { isPlayingAudio = !isPlayingAudio },
                modifier = Modifier.testTag("play_audio")
            ) {
                Icon(
                    imageVector = if (isPlayingAudio) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                    contentDescription = "অডিও প্লে করুন",
                    tint = AccentBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Surah Intro Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GlassWhite, RoundedCornerShape(16.dp))
                .border(1.dp, GlassWhiteBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "পরম করুণাময়, অতি দয়ালু আল্লাহর নামে শুরু করছি।",
                    fontSize = 11.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(surah.verses) { verse ->
                var isStarred by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    isStarred = viewModel.isBookmarked("quran_${surah.number}_${verse.number}")
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassWhite)
                        .border(1.dp, GlassWhiteBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(AccentBlue.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = viewModel.getBengaliNumber(verse.number),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = {
                                        viewModel.toggleBookmark(
                                            id = "quran_${surah.number}_${verse.number}",
                                            type = "quran",
                                            title = "${surah.nameBengali} - আয়াত ${verse.number}",
                                            reference = "সুরা ${surah.number}, আয়াত ${verse.number}",
                                            subtitle = verse.textBengali
                                        )
                                        isStarred = !isStarred
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isStarred) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "বুকমার্ক",
                                        tint = AccentBlue
                                    )
                                }
                            }
                        }

                        // Arabic verse body
                        Text(
                            text = verse.textArabic,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            lineHeight = 32.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Bengali translation
                        Text(
                            text = verse.textBengali,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextWhite,
                            lineHeight = 18.sp
                        )

                        // English translation
                        Text(
                            text = verse.textEnglish,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextMuted,
                            lineHeight = 16.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

// --- PRAYER SCREEN ---
@Composable
fun PrayerScreen(viewModel: NoorShieldViewModel) {
    val divisionState by viewModel.selectedDivision.collectAsStateWithLifecycle()
    val todayStatus by viewModel.todayPrayerStatus.collectAsStateWithLifecycle()

    var showCompass by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTodayPrayerStatus()
    }

    val times = viewModel.getPrayerTimesForDivision(divisionState)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "নামাজের সময়সূচি",
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            )

            // Compass Toggle
            IconButton(
                onClick = { showCompass = !showCompass },
                modifier = Modifier.testTag("toggle_compass")
            ) {
                Icon(
                    imageVector = Icons.Default.CompassCalibration,
                    contentDescription = "কিবলা কম্পাস",
                    tint = AccentBlue,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        if (showCompass) {
            CompassCard { showCompass = false }
        } else {
            // Location Picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassWhite, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "আপনার এলাকা:", fontSize = 12.sp, color = TextMuted)
                val divisionsList = listOf("Dhaka", "Chittagong", "Sylhet", "Rajshahi", "Khulna", "Barishal", "Rangpur", "Mymensingh")
                var expanded by remember { mutableStateOf(false) }

                Box {
                    Button(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = AccentBlue),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(text = divisionState, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "dropdown")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(CardBackground)
                    ) {
                        divisionsList.forEach { div ->
                            DropdownMenuItem(
                                text = { Text(text = div, color = TextWhite) },
                                onClick = {
                                    viewModel.selectedDivision.value = div
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prayer Tracker List
            Text(text = "আজকের ওয়াক্ত ট্র্যাকার", fontSize = 13.sp, color = TextWhite, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(times) { pt ->
                    val isChecked = when (pt.nameEn.lowercase()) {
                        "fajr" -> todayStatus?.fajr ?: false
                        "dhuhr" -> todayStatus?.dhuhr ?: false
                        "asr" -> todayStatus?.asr ?: false
                        "maghrib" -> todayStatus?.maghrib ?: false
                        "isha" -> todayStatus?.isha ?: false
                        else -> false
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = GlassWhite),
                        border = BorderStroke(1.dp, GlassWhiteBorder),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = pt.nameBn,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "ওয়াক্ত শুরু: ${pt.timeStr}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isChecked) "আদায় করা হয়েছে" else "আদায় করুন",
                                    fontSize = 11.sp,
                                    color = if (isChecked) AccentBlue else TextMuted,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { viewModel.togglePrayer(pt.nameEn.lowercase()) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = AccentBlue,
                                        uncheckedColor = GlassWhiteBorder
                                    ),
                                    modifier = Modifier.testTag("checkbox_${pt.nameEn.lowercase()}")
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    MosqueFinderCard()
                }
            }
        }
    }
}

@Composable
fun CompassCard(onClose: () -> Unit) {
    var compassAngle by remember { mutableStateOf(245f) } // Standard Qibla angle from Bangladesh is approx 245-255 degrees

    Card(
        colors = CardDefaults.cardColors(containerColor = GlassWhite),
        border = BorderStroke(1.dp, GlassWhiteBorder),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "কিবলা কিবলা নির্দেশক", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Beautiful rotating Compass Vector
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .background(PrimaryBlue.copy(alpha = 0.2f), CircleShape)
                    .border(2.dp, AccentBlue.copy(alpha = 0.5f), CircleShape)
            ) {
                // Compass Needle
                Icon(
                    imageVector = Icons.Default.CompassCalibration,
                    contentDescription = "সূঁচ",
                    tint = AccentBlue,
                    modifier = Modifier
                        .size(110.dp)
                        .rotate(compassAngle)
                )

                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.Red, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "বাংলাদেশ (ঢাকা) থেকে কিবলা কোণ: ২৫১° পশ্চিম-দক্ষিণ",
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { compassAngle = (compassAngle + 15f) % 360f },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("calibrate_compass")
            ) {
                Text(text = "কম্পাস ক্যালিব্রেট করুন", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkBackground)
            }
        }
    }
}

@Composable
fun MosqueFinderCard() {
    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(containerColor = GlassWhite),
        border = BorderStroke(1.dp, GlassWhiteBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "নিকটস্থ মসজিদসমূহ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            Spacer(modifier = Modifier.height(8.dp))

            val mockMosques = listOf(
                "বায়তুল মোকাররম জাতীয় মসজিদ • ০.৪ কিমি",
                "গুলশান সেন্ট্রাল মসজিদ • ১.৮ কিমি",
                "লালবাগ কেল্লা শাহী মসজিদ • ২.৫ কিমি"
            )

            mockMosques.forEach { mosque ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🕌", fontSize = 14.sp)
                    Text(text = mosque, fontSize = 12.sp, color = TextWhite)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val gmmIntentUri = Uri.parse("geo:0,0?q=Mosque")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    context.startActivity(mapIntent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("find_mosque_maps")
            ) {
                Text(text = "গুগল ম্যাপে মসজিদ খুঁজুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- TASBIH SCREEN ---
@Composable
fun TasbihScreen(viewModel: NoorShieldViewModel) {
    val count by viewModel.tasbihCount.collectAsStateWithLifecycle()
    val target by viewModel.tasbihTarget.collectAsStateWithLifecycle()
    val phrase by viewModel.tasbihName.collectAsStateWithLifecycle()
    val history by viewModel.allTasbihRecords.collectAsStateWithLifecycle()

    val phrasesList = listOf(
        "সুবহানাল্লাাহ (سبحان الله)",
        "আলহামদুলিল্লাহ (الحمد لله)",
        "আল্লাহু আকবার (الله أكبر)",
        "লা ইলাহা ইল্লাল্লাহ (لا إله إلا الله)",
        "আস্তাগফিরুল্লাহ (أستغفر الله)"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "ডিজিটাল তসবিহ",
            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White),
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        // Select Dhikr Phrase
        var expanded by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GlassWhite, RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = phrase, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Icon(Icons.Default.ArrowDropDown, contentDescription = "dropdown", tint = AccentBlue)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(CardBackground)
            ) {
                phrasesList.forEach { pr ->
                    DropdownMenuItem(
                        text = { Text(text = pr, color = TextWhite) },
                        onClick = {
                            viewModel.changeTasbihPhrases(pr)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large glowing click area
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(230.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(PrimaryBlue.copy(alpha = 0.4f), Color.Transparent)
                        )
                    )
                    .border(2.dp, AccentBlue.copy(alpha = 0.5f), CircleShape)
                    .clickable { viewModel.incrementTasbih() }
                    .testTag("tasbih_click_area")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = count.toString(),
                        style = TextStyle(
                            fontSize = 64.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            shadow = Shadow(color = AccentBlue, blurRadius = 15f)
                        )
                    )
                    Text(
                        text = "লক্ষ্য: ${if (target > 0) target.toString() else "সীমাহীন"}",
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Action Options Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { viewModel.resetTasbih() },
                colors = ButtonDefaults.buttonColors(containerColor = GlassWhite),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "রিসেট করুন", color = TextWhite, fontSize = 11.sp)
            }

            Button(
                onClick = { viewModel.setTasbihTarget(if (target == 33) 100 else 33) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "লক্ষ্য: ${if (target == 33) "১০০" else "৩৩"}", color = Color.White, fontSize = 11.sp)
            }
        }

        // Recent stats history list
        Card(
            colors = CardDefaults.cardColors(containerColor = GlassWhite),
            border = BorderStroke(1.dp, GlassWhiteBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "সাম্প্রতিক জিকির ইতিহাস", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text(
                        text = "মুছে ফেলুন",
                        fontSize = 10.sp,
                        color = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.clickable { viewModel.clearTasbihHistory() }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                if (history.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "কোনো জিকির রেকর্ড নেই।", fontSize = 11.sp, color = TextMuted)
                    }
                } else {
                    LazyColumn {
                        items(history) { rec ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = rec.name,
                                    fontSize = 11.sp,
                                    color = TextWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "+${rec.count}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- PROFILE & EXTRA LEARNING ---
@Composable
fun ProfileScreen(viewModel: NoorShieldViewModel) {
    var subTab by remember { mutableStateOf("profile_main") }

    Crossfade(
        targetState = subTab,
        animationSpec = tween(400),
        label = "SubProfileNavigation"
    ) { currentSubTab ->
        when (currentSubTab) {
            "profile_main" -> ProfileMainScreen(viewModel, onNavigate = { subTab = it })
            "quiz" -> QuizScreen(viewModel, onBack = { subTab = "profile_main" })
            "names_99" -> Names99Screen(onBack = { subTab = "profile_main" })
            "zakat" -> ZakatScreen(viewModel, onBack = { subTab = "profile_main" })
            "ai_chat" -> AiChatScreen(viewModel, onBack = { subTab = "profile_main" })
        }
    }
}

@Composable
fun ProfileMainScreen(viewModel: NoorShieldViewModel, onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(PrimaryBlue.copy(alpha = 0.5f), DarkBackground)
                    )
                )
                .border(1.dp, AccentBlue.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_icon_fg),
                    contentDescription = "প্রোফাইল ছবি",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(2.dp, AccentBlue, CircleShape)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "নূর শিল্ড ব্যবহারকারী",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "মুমিন ভাই • ঢাকা, বাংলাদেশ",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Features Grid / Options
        Text(text = "ইসলামিক শিক্ষা ও হিসাব", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))

        val menuItems = listOf(
            ProfileMenuItem("🤖", "এআই ইসলামিক সহকারী", "ai_chat"),
            ProfileMenuItem("📝", "ইসলামিক কুইজ খেলুন", "quiz"),
            ProfileMenuItem("🧮", "যাকাত ক্যালকুলেটর", "zakat"),
            ProfileMenuItem("📖", "আল্লাহর ৯৯ নাম", "names_99")
        )

        menuItems.forEach { item ->
            Card(
                onClick = { onNavigate(item.redirectKey) },
                colors = CardDefaults.cardColors(containerColor = GlassWhite),
                border = BorderStroke(1.dp, GlassWhiteBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("menu_${item.redirectKey}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = item.icon, fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = item.label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    }
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = "forward", tint = AccentBlue, modifier = Modifier.size(14.dp))
                }
            }
        }

        // Achievements / Badges Section
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "অর্জনসমূহ ও ব্যাজ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val badges = listOf(
                BadgeItem("🕌", "নামাজ কায়েমকারী", true),
                BadgeItem("📿", "তাসবিহ সাধক", true),
                BadgeItem("📖", "কুরআনপ্রেমী", false)
            )

            badges.forEach { bg ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(
                                if (bg.unlocked) PrimaryBlue.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f),
                                CircleShape
                            )
                            .border(1.dp, if (bg.unlocked) AccentBlue else Color.Transparent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = bg.icon, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = bg.name, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (bg.unlocked) TextWhite else TextMuted)
                }
            }
        }
    }
}

data class ProfileMenuItem(val icon: String, val label: String, val redirectKey: String)
data class BadgeItem(val icon: String, val name: String, val unlocked: Boolean)

// --- ALLAH'S 99 NAMES SCREEN ---
@Composable
fun Names99Screen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("names_99_back")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "back", tint = Color.White)
            }
            Text(text = "আল্লাহর ৯৯ নাম", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.size(48.dp))
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(IslamicData.allahNames) { name ->
                var expanded by remember { mutableStateOf(false) }

                Card(
                    onClick = { expanded = !expanded },
                    colors = CardDefaults.cardColors(containerColor = GlassWhite),
                    border = BorderStroke(1.dp, GlassWhiteBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(PrimaryBlue.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = name.number.toString(), fontSize = 11.sp, color = AccentBlue, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = name.pronunciation, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(text = name.meaningBengali, fontSize = 12.sp, color = TextMuted)
                                }
                            }
                            Text(text = name.nameArabic, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                        }

                        if (expanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = GlassWhiteBorder)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "আমল ও ফজিলত: ${name.benefits}",
                                fontSize = 12.sp,
                                color = TextWhite,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- ZAKAT CALCULATOR ---
@Composable
fun ZakatScreen(viewModel: NoorShieldViewModel, onBack: () -> Unit) {
    val gold by viewModel.zakatGoldWeight.collectAsStateWithLifecycle()
    val silver by viewModel.zakatSilverWeight.collectAsStateWithLifecycle()
    val cash by viewModel.zakatCash.collectAsStateWithLifecycle()
    val business by viewModel.zakatBusinessAssets.collectAsStateWithLifecycle()
    val debts by viewModel.zakatDebts.collectAsStateWithLifecycle()
    val result by viewModel.zakatResult.collectAsStateWithLifecycle()
    val reached by viewModel.zakatNisabReached.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("zakat_back")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "back", tint = Color.White)
            }
            Text(text = "যাকাত ক্যালকুলেটর", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.size(48.dp))
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = GlassWhite),
            border = BorderStroke(1.dp, GlassWhiteBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "হিসাব বিবরণী (টাকা/গ্রাম)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentBlue)

                ZakatInputField(value = gold, onValueChange = { viewModel.zakatGoldWeight.value = it }, label = "স্বর্ণের পরিমাণ (ভরি/তোলা)", placeholder = "০.০")
                ZakatInputField(value = silver, onValueChange = { viewModel.zakatSilverWeight.value = it }, label = "রুপার পরিমাণ (ভরি/তোলা)", placeholder = "০.০")
                ZakatInputField(value = cash, onValueChange = { viewModel.zakatCash.value = it }, label = "নগদ বা ব্যাংক ব্যালেন্স (টাকা)", placeholder = "০")
                ZakatInputField(value = business, onValueChange = { viewModel.zakatBusinessAssets.value = it }, label = "ব্যবসার পণ্য মূল্য (টাকা)", placeholder = "০")
                ZakatInputField(value = debts, onValueChange = { viewModel.zakatDebts.value = it }, label = "ঋণ বা দেনা পরিশোধযোগ্য (টাকা)", placeholder = "০")

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.calculateZakat() },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("zakat_calculate_btn")
                    ) {
                        Text(text = "হিসাব করুন", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.resetZakat() },
                        colors = ButtonDefaults.buttonColors(containerColor = GlassWhiteBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "মুছে ফেলুন", color = TextWhite)
                    }
                }
            }
        }

        if (result != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (reached) PrimaryBlue.copy(alpha = 0.3f) else Color.Red.copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.dp, if (reached) AccentBlue.copy(alpha = 0.4f) else Color.Red.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (reached) {
                        Text(text = "আপনার যাকাত ফরজ হয়েছে!", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "আপনার প্রদেয় যাকাতের পরিমাণ: ${viewModel.getBengaliNumber(result!!.toInt())} টাকা (২.৫%)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Text(text = "আপনার যাকাত ফরজ হয়নি।", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            text = "নিসাব পরিমাণ সম্পদ না থাকায় যাকাত ফরজ হয়নি। (রুপার সর্বনিম্ন নিসাব আনুমানিক ৯৫,০০০ টাকা)",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ZakatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String
) {
    Column {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, color = TextMuted) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = GlassWhiteBorder,
                focusedContainerColor = GlassWhite,
                unfocusedContainerColor = GlassWhite
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// --- ISLAMIC QUIZ SCREEN ---
@Composable
fun QuizScreen(viewModel: NoorShieldViewModel, onBack: () -> Unit) {
    val currentIdx by viewModel.currentQuizIndex.collectAsStateWithLifecycle()
    val selectedAns by viewModel.selectedQuizAnswer.collectAsStateWithLifecycle()
    val isAnswered by viewModel.isQuizAnswered.collectAsStateWithLifecycle()
    val score by viewModel.quizScore.collectAsStateWithLifecycle()
    val showResults by viewModel.showQuizResults.collectAsStateWithLifecycle()

    val totalQuestions = IslamicData.quizQuestions.size
    val currentQuestion = IslamicData.quizQuestions[currentIdx]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("quiz_back")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "back", tint = Color.White)
            }
            Text(text = "ইসলামিক কুইজ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.size(48.dp))
        }

        if (showResults) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GlassWhite),
                    border = BorderStroke(1.dp, GlassWhiteBorder),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "কুইজ সমাপ্ত!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "আপনার স্কোর: ${viewModel.getBengaliNumber(score)} / ${viewModel.getBengaliNumber(totalQuestions)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.resetQuiz() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "আবার শুরু করুন", color = DarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Question Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "প্রশ্ন: ${viewModel.getBengaliNumber(currentIdx + 1)} / ${viewModel.getBengaliNumber(totalQuestions)}",
                    fontSize = 12.sp,
                    color = AccentBlue,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "স্কোর: ${viewModel.getBengaliNumber(score)}",
                    fontSize = 12.sp,
                    color = TextWhite
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { (currentIdx + 1).toFloat() / totalQuestions.toFloat() },
                color = AccentBlue,
                trackColor = GlassWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Question Card
            Card(
                colors = CardDefaults.cardColors(containerColor = GlassWhite),
                border = BorderStroke(1.dp, GlassWhiteBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = currentQuestion.question,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(20.dp),
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Options List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                currentQuestion.options.forEachIndexed { optIdx, option ->
                    val isSelected = selectedAns == optIdx
                    val isCorrect = optIdx == currentQuestion.correctAnswerIndex

                    val color = when {
                        isAnswered && isCorrect -> Color(0xFF4CAF50) // Green for correct answer
                        isAnswered && isSelected && !isCorrect -> Color(0xFFF44336) // Red for wrong answer
                        isSelected -> AccentBlue
                        else -> GlassWhiteBorder
                    }

                    Card(
                        onClick = { viewModel.selectQuizAnswer(optIdx) },
                        colors = CardDefaults.cardColors(containerColor = GlassWhite),
                        border = BorderStroke(1.dp, color),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_opt_$optIdx")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = option, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                        }
                    }
                }
            }

            if (isAnswered) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "ব্যাখ্যা:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                        Text(text = currentQuestion.explanation, fontSize = 12.sp, color = TextWhite, modifier = Modifier.padding(top = 4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.nextQuizQuestion() },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_next_btn")
                ) {
                    Text(
                        text = if (currentIdx + 1 == totalQuestions) "কুইজ শেষ করুন" else "পরবর্তী প্রশ্ন",
                        color = DarkBackground,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- AI ISLAMIC ASSISTANT CHAT SCREEN ---
@Composable
fun AiChatScreen(viewModel: NoorShieldViewModel, onBack: () -> Unit) {
    val messages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val inputText by viewModel.aiInputText.collectAsStateWithLifecycle()
    val isLoading by viewModel.aiIsLoading.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "back", tint = Color.White)
            }
            Text(text = "এআই ইসলামিক সহকারী", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                text = "মুছুন",
                fontSize = 11.sp,
                color = Color.Red.copy(alpha = 0.8f),
                modifier = Modifier
                    .clickable { viewModel.clearAiChat() }
                    .padding(8.dp)
            )
        }

        // Messages list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val align = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                val cardColor = if (msg.isUser) PrimaryBlue.copy(alpha = 0.5f) else GlassWhite
                val borderColor = if (msg.isUser) AccentBlue.copy(alpha = 0.4f) else GlassWhiteBorder

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = align) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        border = BorderStroke(1.dp, borderColor),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (msg.isUser) 16.dp else 0.dp,
                            bottomEnd = if (msg.isUser) 0.dp else 16.dp
                        ),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = msg.text, fontSize = 13.sp, color = TextWhite, lineHeight = 18.sp)
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GlassWhite),
                            border = BorderStroke(1.dp, GlassWhiteBorder),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = AccentBlue, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Text(text = "নূর শিল্ড ভাবছে...", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }

        // Chat Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { viewModel.aiInputText.value = it },
                placeholder = { Text("যেকোনো প্রশ্ন লিখুন...", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = GlassWhiteBorder,
                    focusedContainerColor = GlassWhite,
                    unfocusedContainerColor = GlassWhite
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { viewModel.sendAiQuestion() }),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input")
            )

            IconButton(
                onClick = { viewModel.sendAiQuestion() },
                modifier = Modifier
                    .background(AccentBlue, CircleShape)
                    .size(44.dp)
                    .testTag("chat_send_btn")
            ) {
                Icon(Icons.Default.Send, contentDescription = "পাঠান", tint = DarkBackground, modifier = Modifier.size(20.dp))
            }
        }
    }
}
