package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * URL Address Bar component featuring a text input field, security indicator,
 * clear action, and an explicit 'Go' button to navigate the WebView to specific URLs.
 */
@Composable
fun UrlAddressBar(
    currentUrl: String,
    isSecure: Boolean,
    onNavigate: (String) -> Unit,
    onOpenSecuritySheet: () -> Unit,
    modifier: Modifier = Modifier,
    onUnblockNavigate: ((String) -> Unit)? = null
) {
    var isEditing by remember { mutableStateOf(false) }
    var inputUrlText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Synchronize currentUrl when updated from webview
    LaunchedEffect(currentUrl) {
        if (!isEditing) {
            inputUrlText = if (currentUrl == "about:home") "" else currentUrl
        }
    }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            try {
                focusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    fun submitNavigation() {
        val destination = inputUrlText.trim()
        if (destination.isNotEmpty()) {
            isEditing = false
            focusManager.clearFocus()
            onNavigate(destination)
        }
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = modifier
            .height(44.dp)
            .testTag("url_address_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Security Lock / Warning / Shield Icon
            IconButton(
                onClick = onOpenSecuritySheet,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("security_lock_button")
            ) {
                when {
                    isSecure -> {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Secure HTTPS",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    currentUrl == "about:home" -> {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security Shield",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Not Secure HTTP",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (isEditing) {
                // Interactive Text Input Field
                TextField(
                    value = inputUrlText,
                    onValueChange = { inputUrlText = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .testTag("url_text_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = { submitNavigation() },
                        onSearch = { submitNavigation() },
                        onDone = { submitNavigation() }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    placeholder = {
                        Text(
                            text = "Search or type URL",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                )

                // Clear input action
                if (inputUrlText.isNotEmpty()) {
                    IconButton(
                        onClick = { inputUrlText = "" },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("url_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear URL",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // 1-Tap Unblocker button
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0284C7),
                        onClick = {
                            val target = inputUrlText.trim()
                            isEditing = false
                            focusManager.clearFocus()
                            if (onUnblockNavigate != null) {
                                onUnblockNavigate(target)
                            } else {
                                onNavigate(if (target.isNotEmpty()) "https://proxyium.com/?url=${java.net.URLEncoder.encode(target, "UTF-8")}" else "https://proxyium.com")
                            }
                        },
                        modifier = Modifier
                            .height(30.dp)
                            .padding(end = 4.dp)
                            .testTag("url_unblock_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "⚡ Unblock",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Explicit 'Go' Button
                Button(
                    onClick = { submitNavigation() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    modifier = Modifier
                        .height(34.dp)
                        .padding(start = 2.dp, end = 2.dp)
                        .testTag("url_go_button")
                ) {
                    Text(
                        text = "Go",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Navigate to URL",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else {
                // Display Mode: clicking switches to editing mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            isEditing = true
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = if (currentUrl == "about:home") {
                            "Search or enter address"
                        } else {
                            currentUrl.removePrefix("https://").removePrefix("http://")
                        },
                        fontSize = 13.sp,
                        color = if (currentUrl == "about:home") {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Always-available quick 'Go' action button
                IconButton(
                    onClick = {
                        isEditing = true
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("url_enter_edit_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Edit or Go",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
