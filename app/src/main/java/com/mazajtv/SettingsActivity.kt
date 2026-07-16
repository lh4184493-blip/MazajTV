package com.mazajtv

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var playlistUrlInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        playlistUrlInput = findViewById(R.id.playlistUrlInput)
        val saveButton = findViewById<android.widget.Button>(R.id.saveButton)
        val resetButton = findViewById<android.widget.Button>(R.id.resetButton)

        playlistUrlInput.setText(Utils.getPlaylistUrl(this) ?: "")

        saveButton.setOnClickListener {
            val url = playlistUrlInput.text.toString().trim()
            if (url.isNotEmpty()) {
                Utils.savePlaylistUrl(this, url)
                Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Utils.clearPlaylistUrl(this)
                Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        resetButton.setOnClickListener {
            Utils.clearPlaylistUrl(this)
            playlistUrlInput.setText("")
            Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
