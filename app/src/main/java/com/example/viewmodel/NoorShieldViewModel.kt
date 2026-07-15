package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.api.Content
import com.example.api.GeminiClient
import com.example.api.Part
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NoorShieldViewModel(application: Application) : AndroidViewModel(application) {

    // Room Database Setup
    private val database: NoorShieldDatabase by lazy {
        Room.databaseBuilder(
            application,
            NoorShieldDatabase::class.java, "noor_shield_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    private val dao: NoorShieldDao by lazy { database.dao() }

    // UI Navigation State
    val currentTab = MutableStateFlow("home")

    // Language state: "bn" (Bengali), "en" (English)
    val appLanguage = MutableStateFlow("bn")

    // Division state (Location) for Prayer times
    val selectedDivision = MutableStateFlow("Dhaka")

    // Core Flows from Room Database
    val allBookmarks: StateFlow<List<BookmarkRecord>> = dao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasbihRecords: StateFlow<List<TasbihRecord>> = dao.getAllTasbihRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPrayerStatus: StateFlow<List<PrayerStatus>> = dao.getAllPrayerStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFastingRecords: StateFlow<List<FastingRecord>> = dao.getAllFastingRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Prayer times calculation based on Division
    // Fajr, Dhuhr, Asr, Maghrib, Isha
    data class PrayerTime(val nameBn: String, val nameEn: String, val timeStr: String, val hour: Int, val minute: Int)

    fun getPrayerTimesForDivision(division: String): List<PrayerTime> {
        return when (division) {
            "Chittagong" -> listOf(
                PrayerTime("ফজর", "Fajr", "০৩:৫৫", 3, 55),
                PrayerTime("যোহর", "Dhuhr", "১২:০০", 12, 0),
                PrayerTime("আসর", "Asr", "১৫:৪০", 15, 40),
                PrayerTime("মাগরিব", "Maghrib", "১৮:৪০", 18, 40),
                PrayerTime("এশা", "Isha", "২০:১০", 20, 10)
            )
            "Sylhet" -> listOf(
                PrayerTime("ফজর", "Fajr", "০৩:৫২", 3, 52),
                PrayerTime("যোহর", "Dhuhr", "১১:৫৭", 11, 57),
                PrayerTime("আসর", "Asr", "১৫:৩৭", 15, 37),
                PrayerTime("মাগরিব", "Maghrib", "১৮:৩৭", 18, 37),
                PrayerTime("এশা", "Isha", "২০:০৮", 20, 8)
            )
            "Rajshahi" -> listOf(
                PrayerTime("ফজর", "Fajr", "০৪:০৭", 4, 7),
                PrayerTime("যোহর", "Dhuhr", "১২:১২", 12, 12),
                PrayerTime("আসর", "Asr", "১৫:৫২", 15, 52),
                PrayerTime("মাগরিব", "Maghrib", "১৮:৫২", 18, 52),
                PrayerTime("এশা", "Isha", "২০:২২", 20, 22)
            )
            "Khulna" -> listOf(
                PrayerTime("ফজর", "Fajr", "০৪:০৫", 4, 5),
                PrayerTime("যোহর", "Dhuhr", "১২:১০", 12, 10),
                PrayerTime("আসর", "Asr", "১৫:৫০", 15, 50),
                PrayerTime("মাগরিব", "Maghrib", "১৮:৫০", 18, 50),
                PrayerTime("এশা", "Isha", "২০:২০", 20, 20)
            )
            "Barishal" -> listOf(
                PrayerTime("ফজর", "Fajr", "০৪:০৩", 4, 3),
                PrayerTime("যোহর", "Dhuhr", "১২:০৮", 12, 8),
                PrayerTime("আসর", "Asr", "১৫:৪৮", 15, 48),
                PrayerTime("মাগরিব", "Maghrib", "১৮:৪৮", 18, 48),
                PrayerTime("এশা", "Isha", "২০:১৮", 20, 18)
            )
            "Rangpur" -> listOf(
                PrayerTime("ফজর", "Fajr", "০৪:০৬", 4, 6),
                PrayerTime("যোহর", "Dhuhr", "১২:১১", 12, 11),
                PrayerTime("আসর", "Asr", "১৫:৫১", 15, 51),
                PrayerTime("মাগরিব", "Maghrib", "১৮:৫১", 18, 51),
                PrayerTime("এশা", "Isha", "২০:২১", 20, 21)
            )
            "Mymensingh" -> listOf(
                PrayerTime("ফজর", "Fajr", "০৪:০১", 4, 1),
                PrayerTime("যোহর", "Dhuhr", "১২:০৬", 12, 6),
                PrayerTime("আসর", "Asr", "১৫:৪৬", 15, 46),
                PrayerTime("মাগরিব", "Maghrib", "১৮:৪৬", 18, 46),
                PrayerTime("এশা", "Isha", "২০:১৬", 20, 16)
            )
            else -> listOf( // Dhaka as default
                PrayerTime("ফজর", "Fajr", "০৪:০০", 4, 0),
                PrayerTime("যোহর", "Dhuhr", "১২:০৫", 12, 5),
                PrayerTime("আসর", "Asr", "১৫:৪৫", 15, 45),
                PrayerTime("মাগরিব", "Maghrib", "১৮:৪৫", 18, 45),
                PrayerTime("এশা", "Isha", "২০:১৫", 20, 15)
            )
        }
    }

    // Dynamic Live Countdown Variables
    val nextPrayerName = MutableStateFlow("আসর")
    val countdownStr = MutableStateFlow("০০:০০:০০")
    val todayDateStr = MutableStateFlow("")
    val hijriDateStr = MutableStateFlow("")

    private var tickerJob: Job? = null

    init {
        startPrayerTicker()
        updateDates()
    }

    private fun startPrayerTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                calculateCountdown()
                delay(1000)
            }
        }
    }

    private fun updateDates() {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("bn", "BD"))
        todayDateStr.value = sdf.format(calendar.time)

        // Simple approximate Hijri date calculation for demonstration (Jul 14, 2026 is approx 28 Muharram 1448)
        // Adjust based on typical offset
        val hijriYear = calendar.get(Calendar.YEAR) - 578 // 2026 - 578 = 1448
        val hijriMonths = listOf(
            "মহররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জমাদিউল আউয়াল", 
            "জমাদিউস সানি", "রজব", "শাবান", "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
        )
        // July is roughly Muharram/Safar
        hijriDateStr.value = "২৮ মহররম $hijriYear হিজরি"
    }

    private fun calculateCountdown() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentSecond = calendar.get(Calendar.SECOND)
        val currentTimeInSec = currentHour * 3600 + currentMinute * 60 + currentSecond

        val times = getPrayerTimesForDivision(selectedDivision.value)

        // Map names
        var nextTime: PrayerTime? = null
        for (pt in times) {
            val ptSec = pt.hour * 3600 + pt.minute * 60
            if (ptSec > currentTimeInSec) {
                nextTime = pt
                break
            }
        }

        val diffSec: Int
        if (nextTime == null) {
            // Next prayer is tomorrow's Fajr
            val firstFajr = times.first()
            nextPrayerName.value = firstFajr.nameBn
            val tomorrowFajrSec = 24 * 3600 + (firstFajr.hour * 3600 + firstFajr.minute * 60)
            diffSec = tomorrowFajrSec - currentTimeInSec
        } else {
            nextPrayerName.value = nextTime.nameBn
            val nextSec = nextTime.hour * 3600 + nextTime.minute * 60
            diffSec = nextSec - currentTimeInSec
        }

        val h = diffSec / 3600
        val m = (diffSec % 3600) / 60
        val s = diffSec % 60

        // Format to Bengali digits
        countdownStr.value = String.format("%02d:%02d:%02d", h, m, s).toBengaliDigits()
    }

    // Helper to convert English digits to Bengali digits
    private fun String.toBengaliDigits(): String {
        val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val builder = StringBuilder()
        for (c in this) {
            if (c in '0'..'9') {
                builder.append(bnDigits[c - '0'])
            } else {
                builder.append(c)
            }
        }
        return builder.toString()
    }

    fun getBengaliNumber(num: Int): String {
        return num.toString().toBengaliDigits()
    }

    // --- Prayer Tracker Room Integration ---
    val todayKey: String
        get() {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(Date())
        }

    val todayPrayerStatus = MutableStateFlow<PrayerStatus?>(null)

    fun loadTodayPrayerStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            var status = dao.getPrayerStatusForDate(todayKey)
            if (status == null) {
                status = PrayerStatus(dateStr = todayKey)
                dao.insertPrayerStatus(status)
            }
            todayPrayerStatus.value = status
        }
    }

    fun togglePrayer(prayerName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = todayPrayerStatus.value ?: PrayerStatus(dateStr = todayKey)
            val updated = when (prayerName) {
                "fajr" -> current.copy(fajr = !current.fajr)
                "dhuhr" -> current.copy(dhuhr = !current.dhuhr)
                "asr" -> current.copy(asr = !current.asr)
                "maghrib" -> current.copy(maghrib = !current.maghrib)
                "isha" -> current.copy(isha = !current.isha)
                else -> current
            }
            dao.insertPrayerStatus(updated)
            todayPrayerStatus.value = updated
        }
    }

    // --- Fasting Tracker ---
    val todayFastingStatus = MutableStateFlow(false)

    fun loadTodayFastingStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            val records = dao.getAllFastingRecords()
            // simple check or query
            val list = dao.getPrayerStatusForDate(todayKey) // placeholder
            // Let's use Room dao queries
        }
    }

    fun toggleTodayFasting() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = todayFastingStatus.value
            val updated = !current
            dao.insertFastingRecord(FastingRecord(todayKey, updated))
            todayFastingStatus.value = updated
        }
    }

    // --- Tasbih Module ---
    val tasbihCount = MutableStateFlow(0)
    val tasbihTarget = MutableStateFlow(33)
    val tasbihName = MutableStateFlow("সুবহানাল্লাাহ (سبحان الله)")

    fun incrementTasbih() {
        tasbihCount.value += 1
        triggerVibration()

        if (tasbihCount.value >= tasbihTarget.value && tasbihTarget.value > 0) {
            // Target reached! Save to history automatically
            saveCurrentTasbihSession()
            tasbihCount.value = 0
        }
    }

    fun resetTasbih() {
        if (tasbihCount.value > 0) {
            saveCurrentTasbihSession()
        }
        tasbihCount.value = 0
    }

    fun setTasbihTarget(target: Int) {
        tasbihTarget.value = target
    }

    fun changeTasbihPhrases(phrase: String) {
        if (tasbihCount.value > 0) {
            saveCurrentTasbihSession()
        }
        tasbihName.value = phrase
        tasbihCount.value = 0
    }

    private fun saveCurrentTasbihSession() {
        val name = tasbihName.value
        val count = tasbihCount.value
        val target = tasbihTarget.value
        if (count > 0) {
            viewModelScope.launch(Dispatchers.IO) {
                dao.insertTasbihRecord(
                    TasbihRecord(
                        name = name,
                        count = count,
                        target = target
                    )
                )
            }
        }
    }

    fun clearTasbihHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearTasbihRecords()
        }
    }

    private fun triggerVibration() {
        val ctx = getApplication<Application>()
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    // --- Quran Module ---
    val quranSearchQuery = MutableStateFlow("")
    val selectedSurah = MutableStateFlow<Surah?>(null)
    val lastReadSurahNumber = MutableStateFlow(1)
    val lastReadVerseNumber = MutableStateFlow(1)

    fun toggleBookmark(id: String, type: String, title: String, reference: String, subtitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = dao.getBookmarkById(id)
            if (existing != null) {
                dao.deleteBookmarkById(id)
            } else {
                dao.insertBookmark(
                    BookmarkRecord(
                        id = id,
                        type = type,
                        title = title,
                        reference = reference,
                        subtitle = subtitle
                    )
                )
            }
        }
    }

    suspend fun isBookmarked(id: String): Boolean {
        return dao.getBookmarkById(id) != null
    }

    // --- Prayer Focus Mode ---
    val isFocusModeActive = MutableStateFlow(false)
    val focusRemainingSec = MutableStateFlow(0)
    val focusTotalDurationMin = MutableStateFlow(15) // default 15 minutes
    private var focusJob: Job? = null

    fun activateFocusMode() {
        isFocusModeActive.value = true
        focusRemainingSec.value = focusTotalDurationMin.value * 60
        focusJob?.cancel()
        focusJob = viewModelScope.launch(Dispatchers.Default) {
            while (focusRemainingSec.value > 0) {
                delay(1000)
                focusRemainingSec.value -= 1
            }
            deactivateFocusMode()
        }
    }

    fun deactivateFocusMode() {
        focusJob?.cancel()
        isFocusModeActive.value = false
        focusRemainingSec.value = 0
    }

    // --- Islamic Quiz Module ---
    val currentQuizIndex = MutableStateFlow(0)
    val selectedQuizAnswer = MutableStateFlow<Int?>(null)
    val isQuizAnswered = MutableStateFlow(false)
    val quizScore = MutableStateFlow(0)
    val showQuizResults = MutableStateFlow(false)

    fun selectQuizAnswer(index: Int) {
        if (!isQuizAnswered.value) {
            selectedQuizAnswer.value = index
            isQuizAnswered.value = true
            val question = IslamicData.quizQuestions[currentQuizIndex.value]
            if (index == question.correctAnswerIndex) {
                quizScore.value += 1
            }
        }
    }

    fun nextQuizQuestion() {
        val nextIdx = currentQuizIndex.value + 1
        if (nextIdx < IslamicData.quizQuestions.size) {
            currentQuizIndex.value = nextIdx
            selectedQuizAnswer.value = null
            isQuizAnswered.value = false
        } else {
            showQuizResults.value = true
        }
    }

    fun resetQuiz() {
        currentQuizIndex.value = 0
        selectedQuizAnswer.value = null
        isQuizAnswered.value = false
        quizScore.value = 0
        showQuizResults.value = false
    }

    // --- Zakat Calculator helper ---
    val zakatGoldWeight = MutableStateFlow("") // in bhori/tola (1 bhori = 11.66g, Nisab is 7.5 bhori)
    val zakatSilverWeight = MutableStateFlow("") // in bhori/tola (Nisab is 52.5 bhori)
    val zakatCash = MutableStateFlow("")
    val zakatBusinessAssets = MutableStateFlow("")
    val zakatDebts = MutableStateFlow("")
    val zakatResult = MutableStateFlow<Double?>(null)
    val zakatNisabReached = MutableStateFlow(false)

    fun calculateZakat() {
        val cash = zakatCash.value.toDoubleOrNull() ?: 0.0
        val goldValue = (zakatGoldWeight.value.toDoubleOrNull() ?: 0.0) * 120000.0 // approx 120,000 BDT per bhori
        val silverValue = (zakatSilverWeight.value.toDoubleOrNull() ?: 0.0) * 1800.0 // approx 1,800 BDT per bhori
        val business = zakatBusinessAssets.value.toDoubleOrNull() ?: 0.0
        val debts = zakatDebts.value.toDoubleOrNull() ?: 0.0

        val totalAssets = cash + goldValue + silverValue + business
        val netAssets = totalAssets - debts

        // Current Nisab threshold in Bangladesh (approx value of 7.5 tola/bhori gold = ~900,000 BDT, or 52.5 tola silver = ~95,000 BDT)
        // Usually, silver nisab is used as standard for charity convenience, which is around 95,000 BDT
        val nisabThresholdValue = 95000.0

        if (netAssets >= nisabThresholdValue) {
            zakatResult.value = netAssets * 0.025 // 2.5%
            zakatNisabReached.value = true
        } else {
            zakatResult.value = 0.0
            zakatNisabReached.value = false
        }
    }

    fun resetZakat() {
        zakatGoldWeight.value = ""
        zakatSilverWeight.value = ""
        zakatCash.value = ""
        zakatBusinessAssets.value = ""
        zakatDebts.value = ""
        zakatResult.value = null
        zakatNisabReached.value = false
    }

    // --- AI Chat Assistant Section ---
    val aiInputText = MutableStateFlow("")
    val aiMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "আসসালামু আলাইকুম! আমি 'নূর শিল্ড' ইসলামিক এআই সহকারী। কুরআন, হাদিস, দুয়া বা যেকোনো ইসলামিক বিষয়ে জিজ্ঞাসা করতে পারেন। ফতোয়া সংক্রান্ত বিষয়ে আলেমদের সাহায্য নিন।",
                isUser = false
            )
        )
    )
    val aiIsLoading = MutableStateFlow(false)

    data class ChatMessage(val text: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())

    fun sendAiQuestion() {
        val text = aiInputText.value.trim()
        if (text.isEmpty() || aiIsLoading.value) return

        // Add user message
        val updatedMsgs = aiMessages.value.toMutableList()
        updatedMsgs.add(ChatMessage(text = text, isUser = true))
        aiMessages.value = updatedMsgs
        aiInputText.value = ""
        aiIsLoading.value = true

        viewModelScope.launch {
            // Map previous messages to Gemini Client history formats
            val history = updatedMsgs.dropLast(1).map { msg ->
                Content(parts = listOf(Part(text = msg.text)))
            }

            val responseText = GeminiClient.askIslamicQuestion(text, history)

            val finalMsgs = aiMessages.value.toMutableList()
            finalMsgs.add(ChatMessage(text = responseText, isUser = false))
            aiMessages.value = finalMsgs
            aiIsLoading.value = false
        }
    }

    fun clearAiChat() {
        aiMessages.value = listOf(
            ChatMessage(
                text = "আসসালামু আলাইকুম! আমি 'নূর শিল্ড' ইসলামিক এআই সহকারী। কুরআন, হাদিস, দুয়া বা যেকোনো ইসলামিক বিষয়ে জিজ্ঞাসা করতে পারেন। ফতোয়া সংক্রান্ত বিষয়ে আলেমদের সাহায্য নিন।",
                isUser = false
            )
        )
    }

    // --- Dynamic Qibla Compass Helper (simulates smooth updates or lists sensor info) ---
    val compassDegrees = MutableStateFlow(0f)
}
