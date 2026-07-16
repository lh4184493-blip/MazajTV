package com.mazajtv

import android.content.Context
import android.content.SharedPreferences
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

object Utils {

    private const val PREFS_NAME = "mazaj_prefs"
    private const val KEY_PLAYLIST_URL = "playlist_url"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun savePlaylistUrl(context: Context, url: String) {
        prefs(context).edit().putString(KEY_PLAYLIST_URL, url).apply()
    }

    fun getPlaylistUrl(context: Context): String? =
        prefs(context).getString(KEY_PLAYLIST_URL, null)

    fun clearPlaylistUrl(context: Context) {
        prefs(context).edit().remove(KEY_PLAYLIST_URL).apply()
    }

    /**
     * Fetches raw text content from a remote URL (used for M3U playlists).
     * Runs on whatever thread it's called from — callers should invoke this
     * off the main thread (e.g. from a coroutine or background executor).
     */
    @Throws(IOException::class)
    fun fetchTextFromUrl(url: String): String {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response code: ${response.code}")
            }
            return response.body?.string() ?: ""
        }
    }

    /**
     * Parses a standard M3U / M3U8 playlist into a list of Channel objects.
     * Supports the common #EXTINF format:
     *   #EXTINF:-1 tvg-logo="..." group-title="...",Channel Name
     *   http://stream-url
     */
    fun parseM3U(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = content.lines()

        var pendingName: String? = null
        var pendingLogo: String? = null
        var pendingGroup: String? = null

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            when {
                line.startsWith("#EXTINF", ignoreCase = true) -> {
                    pendingLogo = Regex("tvg-logo=\"([^\"]*)\"")
                        .find(line)?.groupValues?.get(1)
                    pendingGroup = Regex("group-title=\"([^\"]*)\"")
                        .find(line)?.groupValues?.get(1)
                    pendingName = line.substringAfterLast(",").trim()
                        .ifEmpty { "Channel ${channels.size + 1}" }
                }
                line.startsWith("#") -> {
                    // Other M3U directives are ignored for now.
                }
                else -> {
                    // Treat as a stream URL line.
                    channels.add(
                        Channel(
                            name = pendingName ?: "Channel ${channels.size + 1}",
                            streamUrl = line,
                            logoUrl = pendingLogo,
                            groupTitle = pendingGroup
                        )
                    )
                    pendingName = null
                    pendingLogo = null
                    pendingGroup = null
                }
            }
        }
        return channels
    }

    /**
     * Small demo playlist for first-run / testing purposes only.
     * These are publicly available, freely licensed sample streams
     * commonly used to test video players (Apple's official HLS test
     * stream and Google's Shaka demo assets) — NOT copyrighted TV channels.
     * Replace with your own licensed M3U playlist from Settings.
     */
    fun demoChannels(): List<Channel> = listOf(
        Channel(
            name = "Apple HLS Test Stream",
            streamUrl = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8",
            groupTitle = "Demo"
        ),
        Channel(
            name = "Big Buck Bunny (Demo)",
            streamUrl = "https://storage.googleapis.com/shaka-demo-assets/bbb-dark-truths/hls.m3u8",
            groupTitle = "Demo"
        )
    )
}
