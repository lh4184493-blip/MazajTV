package com.mazajtv

/**
 * Represents a single playable channel entry, typically parsed from an
 * M3U / M3U8 playlist (#EXTINF line + stream URL).
 */
data class Channel(
    val name: String,
    val streamUrl: String,
    val logoUrl: String? = null,
    val groupTitle: String? = null,
    val isLive: Boolean = true
)
