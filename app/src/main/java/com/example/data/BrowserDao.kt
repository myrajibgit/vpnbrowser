package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.Bookmark
import com.example.model.HistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowserDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: Long)

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteBookmarkByUrl(url: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url)")
    fun isBookmarked(url: String): Flow<Boolean>

    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT 150")
    fun getRecentHistory(): Flow<List<HistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: HistoryItem)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistoryItem(id: Long)

    @Query("DELETE FROM history")
    suspend fun clearHistory()
}
