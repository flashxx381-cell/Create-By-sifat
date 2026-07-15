package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// --- Entities ---

@Entity(tableName = "prayer_logs")
data class PrayerLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // format "YYYY-MM-DD"
    val prayerName: String, // Fajr, Zuhr, Asr, Maghrib, Isha
    val isCompleted: Boolean
)

@Entity(tableName = "kaza_counts")
data class KazaCount(
    @PrimaryKey val prayerName: String,
    val count: Int
)

@Entity(tableName = "quran_bookmarks")
data class QuranBookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val textArabic: String,
    val textBengali: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasbih_stats")
data class TasbihStats(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val count: Int
)

// --- DAOs ---

@Dao
interface NoorDao {
    // Prayer Logs
    @Query("SELECT * FROM prayer_logs WHERE date = :date")
    fun getPrayerLogsForDate(date: String): Flow<List<PrayerLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerLog(log: PrayerLog)

    @Query("DELETE FROM prayer_logs WHERE date = :date AND prayerName = :prayerName")
    suspend fun deletePrayerLog(date: String, prayerName: String)

    // Kaza Counts
    @Query("SELECT * FROM kaza_counts")
    fun getAllKazaCounts(): Flow<List<KazaCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKazaCount(kaza: KazaCount)

    @Query("UPDATE kaza_counts SET count = :count WHERE prayerName = :prayerName")
    suspend fun updateKazaCount(prayerName: String, count: Int)

    // Quran Bookmarks
    @Query("SELECT * FROM quran_bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<QuranBookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: QuranBookmark)

    @Query("DELETE FROM quran_bookmarks WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber")
    suspend fun deleteBookmark(surahNumber: Int, ayahNumber: Int)

    // Tasbih Stats
    @Query("SELECT * FROM tasbih_stats ORDER BY date DESC")
    fun getTasbihHistory(): Flow<List<TasbihStats>>

    @Query("SELECT * FROM tasbih_stats WHERE date = :date")
    suspend fun getTasbihForDate(date: String): TasbihStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasbihStats(stats: TasbihStats)
}

// --- Database ---

@Database(
    entities = [PrayerLog::class, KazaCount::class, QuranBookmark::class, TasbihStats::class],
    version = 1,
    exportSchema = false
)
abstract class NoorDatabase : RoomDatabase() {
    abstract fun noorDao(): NoorDao

    companion object {
        @Volatile
        private var INSTANCE: NoorDatabase? = null

        fun getDatabase(context: Context): NoorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoorDatabase::class.java,
                    "noor_shield_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
