package com.example.messi_game

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class CreditsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credits)

        val toolbar: MaterialToolbar = findViewById(R.id.creditsToolbar)
        toolbar.setNavigationOnClickListener { 
            finish()
        }

        val playerName = intent.getStringExtra(EXTRA_PLAYER_NAME)
            ?: getString(R.string.home_player_name_placeholder)
        val recordValue = intent.getIntExtra(EXTRA_RECORD_VALUE, DEFAULT_RECORD)

        findViewById<TextView>(R.id.creditsPlayerNameValue).text = playerName
        findViewById<TextView>(R.id.creditsRecordValue).text = recordValue.toString()

        // Aplicar animaciones
        animateViews()
    }

    private fun animateViews() {
        try {
            val toolbar = findViewById<View>(R.id.creditsToolbar)
            val headerCard = findViewById<View>(R.id.creditsHeaderCard)
            val contentCard = findViewById<View>(R.id.creditsContentCard)

            // Animación de fade in para el toolbar
            val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            toolbar.startAnimation(fadeIn)

            // Animación de slide up para la tarjeta de header
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
                    headerCard.startAnimation(slideUp)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 100)

            // Animación de scale in para la tarjeta de contenido
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in)
                    contentCard.startAnimation(scaleIn)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 250)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val EXTRA_PLAYER_NAME = "com.example.messi_game.extra.PLAYER_NAME"
        const val EXTRA_RECORD_VALUE = "com.example.messi_game.extra.RECORD_VALUE"
        private const val DEFAULT_RECORD = 76
    }
}
