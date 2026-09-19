package com.example.data

import com.example.model.Bookmark
import com.example.model.HistoryItem
import kotlinx.coroutines.flow.Flow

class BrowserRepository(private val dao: BrowserDao) {
    val allBookmarks: Flow<List<Bookmark>> = dao.getAllBookmarks()
    val recentHistory: Flow<List<HistoryItem>> = dao.getRecentHistory()

    fun isBookmarked(url: String): Flow<Boolean> = dao.isBookmarked(url)

    suspend fun addBookmark(title: String, url: String) {
        dao.insertBookmark(Bookmark(title = title, url = url))
    }

    suspend fun removeBookmark(id: Long) {
        dao.deleteBookmark(id)
    }

    suspend fun removeBookmarkByUrl(url: String) {
        dao.deleteBookmarkByUrl(url)
    }

    suspend fun addHistory(title: String, url: String) {
        if (url.isNotBlank() && !url.startsWith("about:") && !url.startsWith("data:")) {
            dao.insertHistory(HistoryItem(title = title.ifBlank { url }, url = url))
        }
    }

    suspend fun deleteHistory(id: Long) {
        dao.deleteHistoryItem(id)
    }

    suspend fun clearHistory() {
        dao.clearHistory()
    }
}
