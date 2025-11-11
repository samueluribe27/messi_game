package com.example.messi_game

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.appbar.MaterialToolbar

class DifficultyActivity : AppCompatActivity() {
    
    private lateinit var playerPrefs: PlayerPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_difficulty)

        playerPrefs = PlayerPreferences(this)

        val toolbar: MaterialToolbar = findViewById(R.id.difficultyToolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val playerName = intent.getStringExtra(EXTRA_PLAYER_NAME)
            ?: getString(R.string.home_player_name_placeholder)
        val recordValue = intent.getIntExtra(EXTRA_RECORD_VALUE, DEFAULT_RECORD)

        findViewById<TextView>(R.id.difficultyPlayerNameValue).text = playerName
        findViewById<TextView>(R.id.difficultyRecordValue).text = recordValue.toString()

        // Setup difficulty card click listeners
        setupDifficultyCards()

        // Aplicar animaciones
        animateViews()
    }
    
    override fun onResume() {
        super.onResume()
        // Update player info in case it changed
        val currentName = playerPrefs.getPlayerName()
        val currentRecord = playerPrefs.getPlayerRecord()
        findViewById<TextView>(R.id.difficultyPlayerNameValue).text = currentName
        findViewById<TextView>(R.id.difficultyRecordValue).text = currentRecord.toString()
    }

    private fun setupDifficultyCards() {
        findViewById<CardView>(R.id.difficultyEasyCard).setOnClickListener {
            launchGame("easy")
        }

        findViewById<CardView>(R.id.difficultyMediumCard).setOnClickListener {
            launchGame("medium")
        }

        findViewById<CardView>(R.id.difficultyHardCard).setOnClickListener {
            launchGame("hard")
        }
    }
    
    private fun launchGame(difficulty: String) {
        // Get current player data from SharedPreferences
        val playerName = playerPrefs.getPlayerName()
        val recordValue = playerPrefs.getPlayerRecord()
        
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra(GameActivity.EXTRA_PLAYER_NAME, playerName)
            putExtra(GameActivity.EXTRA_RECORD_VALUE, recordValue)
            putExtra(GameActivity.EXTRA_DIFFICULTY, difficulty)
        }
        startActivity(intent)
    }

    private fun animateViews() {
        val toolbar = findViewById<View>(R.id.difficultyToolbar)
        val headerCard = findViewById<View>(R.id.difficultyHeaderCard)
        val contentCard = findViewById<View>(R.id.difficultyContentCard)

        // Animación de fade in para el toolbar
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        toolbar.startAnimation(fadeIn)

        // Animación de slide up para la tarjeta de header
        Handler(Looper.getMainLooper()).postDelayed({
            val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
            headerCard.startAnimation(slideUp)
        }, 100)

        // Animación de scale in para la tarjeta de contenido
        Handler(Looper.getMainLooper()).postDelayed({
            val scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in)
            contentCard.startAnimation(scaleIn)
        }, 250)
    }

    companion object {
        const val EXTRA_PLAYER_NAME = "com.example.messi_game.extra.PLAYER_NAME"
        const val EXTRA_RECORD_VALUE = "com.example.messi_game.extra.RECORD_VALUE"
        private const val DEFAULT_RECORD = 76
    }
}

