package com.mazajtv

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * Thin wrapper around Media3's ExoPlayer to keep playback setup and
 * teardown logic in one place, and out of PlayerActivity.
 */
class PlayerManager(private val context: Context) {

    var exoPlayer: ExoPlayer? = null
        private set

    fun initialize(
        playerView: PlayerView,
        streamUrl: String,
        onReady: () -> Unit,
        onError: (String) -> Unit
    ) {
        release()

        val player = ExoPlayer.Builder(context).build()
        exoPlayer = player
        playerView.player = player

        val mediaItem = MediaItem.fromUri(streamUrl)
        player.setMediaItem(mediaItem)

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    onReady()
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                onError(error.message ?: "Playback error")
            }
        })

        player.prepare()
        player.playWhenReady = true
    }

    fun pause() {
        exoPlayer?.playWhenReady = false
    }

    fun resume() {
        exoPlayer?.playWhenReady = true
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }
}
