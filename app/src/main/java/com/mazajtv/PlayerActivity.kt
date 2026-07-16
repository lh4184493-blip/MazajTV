package com.mazajtv

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.ui.PlayerView

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CHANNEL_NAME = "extra_channel_name"
        const val EXTRA_STREAM_URL = "extra_stream_url"
    }

    private lateinit var playerView: PlayerView
    private lateinit var loadingBar: View
    private lateinit var errorText: View
    private lateinit var retryButton: View
    private lateinit var backButton: View

    private lateinit var playerManager: PlayerManager
    private var streamUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)
        loadingBar = findViewById(R.id.playerLoading)
        errorText = findViewById(R.id.playerErrorText)
        retryButton = findViewById(R.id.retryButton)
        backButton = findViewById(R.id.backButton)

        playerManager = PlayerManager(this)
        streamUrl = intent.getStringExtra(EXTRA_STREAM_URL) ?: ""

        backButton.setOnClickListener { finish() }
        retryButton.setOnClickListener { startPlayback() }

        startPlayback()
    }

    private fun startPlayback() {
        if (streamUrl.isBlank()) {
            showError("Invalid stream URL")
            return
        }

        loadingBar.visibility = View.VISIBLE
        errorText.visibility = View.GONE
        retryButton.visibility = View.GONE

        playerManager.initialize(
            playerView = playerView,
            streamUrl = streamUrl,
            onReady = {
                loadingBar.visibility = View.GONE
            },
            onError = { message ->
                showError(message)
            }
        )
    }

    private fun showError(message: String) {
        loadingBar.visibility = View.GONE
        errorText.visibility = View.VISIBLE
        retryButton.visibility = View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        playerManager.pause()
    }

    override fun onResume() {
        super.onResume()
        playerManager.resume()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerManager.release()
    }
}
