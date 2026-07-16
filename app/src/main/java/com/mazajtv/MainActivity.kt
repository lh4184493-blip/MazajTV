package com.mazajtv

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var emptyStateText: View
    private lateinit var loadingProgress: View
    private lateinit var settingsButton: View

    private lateinit var adapter: ChannelAdapter
    private var channels: List<Channel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.channelsRecyclerView)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        emptyStateText = findViewById(R.id.emptyStateText)
        loadingProgress = findViewById(R.id.loadingProgress)
        settingsButton = findViewById(R.id.settingsButton)

        adapter = ChannelAdapter(channels) { channel -> openPlayer(channel) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        swipeRefresh.setOnRefreshListener { loadChannels() }
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        loadChannels()
    }

    override fun onResume() {
        super.onResume()
        // Reload in case the playlist URL changed in Settings.
        if (channels.isEmpty()) {
            loadChannels()
        }
    }

    private fun loadChannels() {
        val playlistUrl = Utils.getPlaylistUrl(this)

        loadingProgress.visibility = View.VISIBLE
        emptyStateText.visibility = View.GONE

        lifecycleScope.launch {
            val result: List<Channel> = try {
                withContext(Dispatchers.IO) {
                    if (playlistUrl.isNullOrBlank()) {
                        Utils.demoChannels()
                    } else {
                        val text = Utils.fetchTextFromUrl(playlistUrl)
                        val parsed = Utils.parseM3U(text)
                        parsed.ifEmpty { Utils.demoChannels() }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.error_loading),
                    Toast.LENGTH_SHORT
                ).show()
                Utils.demoChannels()
            }

            channels = result
            adapter.updateChannels(result)
            emptyStateText.visibility = if (result.isEmpty()) View.VISIBLE else View.GONE
            loadingProgress.visibility = View.GONE
            swipeRefresh.isRefreshing = false
        }
    }

    private fun openPlayer(channel: Channel) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_CHANNEL_NAME, channel.name)
            putExtra(PlayerActivity.EXTRA_STREAM_URL, channel.streamUrl)
        }
        startActivity(intent)
    }
}
