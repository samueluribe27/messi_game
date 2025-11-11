package com.example.messi_game

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class ScoresActivity : AppCompatActivity() {
    
    private lateinit var playerPrefs: PlayerPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scores)

        playerPrefs = PlayerPreferences(this)

        val toolbar: MaterialToolbar = findViewById(R.id.scoresToolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val playerName = intent.getStringExtra(EXTRA_PLAYER_NAME)
            ?: getString(R.string.home_player_name_placeholder)
        val recordValue = intent.getIntExtra(EXTRA_RECORD_VALUE, DEFAULT_RECORD)

        findViewById<TextView>(R.id.scoresPlayerNameValue).text = playerName
        findViewById<TextView>(R.id.scoresRecordValue).text = recordValue.toString()

        // Load real scores from SharedPreferences
        findViewById<TextView>(R.id.scoresEasyValue).text = playerPrefs.getScoreForDifficulty("easy").toString()
        findViewById<TextView>(R.id.scoresMediumValue).text = playerPrefs.getScoreForDifficulty("medium").toString()
        findViewById<TextView>(R.id.scoresHardValue).text = playerPrefs.getScoreForDifficulty("hard").toString()

        // Aplicar animaciones
        animateViews()
    }

    private fun animateViews() {
        val toolbar = findViewById<View>(R.id.scoresToolbar)
        val headerCard = findViewById<View>(R.id.scoresHeaderCard)
        val contentCard = findViewById<View>(R.id.scoresContentCard)

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

