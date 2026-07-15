package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "prayer_status")
data class PrayerStatus(
    @PrimaryKey val dateStr: String, // "YYYY-MM-DD"
    val fajr: Boolean = false,
    val dhuhr: Boolean = false,
    val asr: Boolean = false,
    val maghrib: Boolean = false,
    val isha: Boolean = false
)

@Entity(tableName = "tasbih_record")
data class TasbihRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val count: Int,
    val target: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "fasting_record")
data class FastingRecord(
    @PrimaryKey val dateStr: String, // "YYYY-MM-DD"
    val isFasting: Boolean = false
)

@Entity(tableName = "bookmark_record")
data class BookmarkRecord(
    @PrimaryKey val id: String, // e.g. "quran_1_1" or "hadith_bukhari_1"
    val type: String, // "quran", "hadith", "dua"
    val title: String,
    val reference: String,
    val subtitle: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface NoorShieldDao {
    @Query("SELECT * FROM prayer_status")
    fun getAllPrayerStatus(): Flow<List<PrayerStatus>>

    @Query("SELECT * FROM prayer_status WHERE dateStr = :dateStr")
    suspend fun getPrayerStatusForDate(dateStr: String): PrayerStatus?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerStatus(status: PrayerStatus)

    @Query("SELECT * FROM tasbih_record ORDER BY timestamp DESC")
    fun getAllTasbihRecords(): Flow<List<TasbihRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasbihRecord(record: TasbihRecord)

    @Query("DELETE FROM tasbih_record")
    suspend fun clearTasbihRecords()

    @Query("SELECT * FROM fasting_record")
    fun getAllFastingRecords(): Flow<List<FastingRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFastingRecord(record: FastingRecord)

    @Query("SELECT * FROM bookmark_record ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkRecord>>

    @Query("SELECT * FROM bookmark_record WHERE id = :id")
    suspend fun getBookmarkById(id: String): BookmarkRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkRecord)

    @Query("DELETE FROM bookmark_record WHERE id = :id")
    suspend fun deleteBookmarkById(id: String)
}

@Database(
    entities = [PrayerStatus::class, TasbihRecord::class, FastingRecord::class, BookmarkRecord::class],
    version = 1,
    exportSchema = false
)
abstract class NoorShieldDatabase : RoomDatabase() {
    abstract fun dao(): NoorShieldDao
}
