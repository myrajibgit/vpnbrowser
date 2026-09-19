package com.example.model

import java.util.UUID

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "about:blank",
    val title: String = "New Tab",
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val isSecure: Boolean = false,
    val isIncognito: Boolean = false
)
