package com.newscorrelator.app.utils

import java.security.MessageDigest

fun hashString(input: String): String {
    val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

fun String.truncate(maxLength: Int): String {
    return if (this.length > maxLength) {
        this.substring(0, maxLength) + "..."
    } else {
        this
    }
}

fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> "${diff / 604800_000}w ago"
    }
}
