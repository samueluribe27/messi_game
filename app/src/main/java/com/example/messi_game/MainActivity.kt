package com.example.messi_game

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    
    private var menuMusic: MediaPlayer? = null
    private lateinit var playerPrefs: PlayerPreferences
    private lateinit var playerNameTextView: TextView
    private lateinit var recordTextView: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerPrefs = PlayerPreferences(this)
        
        playerNameTextView = findViewById(R.id.playerNameValue)
        recordTextView = findViewById(R.id.recordValue)
        
        // Cargar datos del jugador desde SharedPreferences
        val playerName = playerPrefs.getPlayerName()
        val recordValue = playerPrefs.getPlayerRecord()

        playerNameTextView.text = playerName
        recordTextView.text = recordValue.toString()
        
        // Configurar listener para editar nombre (tanto en texto como en ícono)
        playerNameTextView.setOnClickListener {
            showEditNameDialog()
        }
        
        findViewById<View>(R.id.editPlayerNameIcon).setOnClickListener {
            showEditNameDialog()
        }

        // Configurar listeners de botones
        findViewById<MaterialButton>(R.id.playButton).setOnClickListener {
            launchDifficulty()
        }

        findViewById<MaterialButton>(R.id.scoresButton).setOnClickListener {
            launchScores()
        }

        findViewById<MaterialButton>(R.id.creditsButton).setOnClickListener {
            launchCredits()
        }

        // Aplicar animaciones
        animateViews()
        
        // Iniciar música de fondo
        startMenuMusic()
    }
    
    private fun startMenuMusic() {
        try {
            menuMusic = MediaPlayer.create(this, R.raw.musica_menu)
            menuMusic?.isLooping = true
            menuMusic?.setVolume(0.5f, 0.5f) // Volumen al 50%
            menuMusic?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Reanudar música al volver al menú
        try {
            if (menuMusic == null) {
                startMenuMusic()
            } else {
                menuMusic?.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Intentar recrear si hay un error
            try {
                menuMusic?.release()
                menuMusic = null
                startMenuMusic()
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
        
        // Actualizar información del jugador por si cambió
        val currentName = playerPrefs.getPlayerName()
        val currentRecord = playerPrefs.getPlayerRecord()
        playerNameTextView.text = currentName
        recordTextView.text = currentRecord.toString()
    }
    
    override fun onPause() {
        super.onPause()
        // Pausar música al salir del menú
        try {
            menuMusic?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Liberar recursos de música
        try {
            menuMusic?.stop()
            menuMusic?.release()
            menuMusic = null
        } catch (e: Exception) {
            e.printStackTrace()
            menuMusic = null
        }
    }

    private fun animateViews() {
        val headerCard = findViewById<View>(R.id.headerCard)
        val canvas = findViewById<View>(R.id.facesCanvas)
        val playButton = findViewById<View>(R.id.playButton)
        val scoresButton = findViewById<View>(R.id.scoresButton)
        val creditsButton = findViewById<View>(R.id.creditsButton)

        // Animación de fade in para la tarjeta superior
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        headerCard.startAnimation(fadeIn)

        // Animación de escala para el lienzo
        Handler(Looper.getMainLooper()).postDelayed({
            val scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in)
            canvas.startAnimation(scaleIn)
        }, 150)

        // Animaciones escalonadas para los botones
        Handler(Looper.getMainLooper()).postDelayed({
            val slideUp1 = AnimationUtils.loadAnimation(this, R.anim.slide_up)
            playButton.startAnimation(slideUp1)
        }, 300)

        Handler(Looper.getMainLooper()).postDelayed({
            val slideUp2 = AnimationUtils.loadAnimation(this, R.anim.slide_up)
            scoresButton.startAnimation(slideUp2)
        }, 400)

        Handler(Looper.getMainLooper()).postDelayed({
            val slideUp3 = AnimationUtils.loadAnimation(this, R.anim.slide_up)
            creditsButton.startAnimation(slideUp3)
        }, 500)
    }

    private fun showEditNameDialog() {
        val input = EditText(this).apply {
            setText(playerPrefs.getPlayerName())
            hint = "Ej: Juan"
            setPadding(40, 40, 40, 40)
            textSize = 18f
            setTextColor(0xFF1A1A1A.toInt())
            setHintTextColor(0xFF757575.toInt())
            setSingleLine(true)
            maxLines = 1
        }
        
        val dialog = AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert)
            .setTitle("Editar Nombre")
            .setMessage("Ingresa tu nombre de jugador:")
            .setView(input)
            .setPositiveButton("GUARDAR") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty() && newName.length <= 20) {
                    playerPrefs.setPlayerName(newName)
                    updatePlayerName(newName)
                } else if (newName.isEmpty()) {
                    // Mostrar error
                    android.widget.Toast.makeText(
                        this,
                        "El nombre no puede estar vacío",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    android.widget.Toast.makeText(
                        this,
                        "El nombre es demasiado largo (máx. 20 caracteres)",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("CANCELAR", null)
            .setCancelable(true)
            .create()
        
        dialog.show()
        
        // Enfocar y mostrar teclado
        input.requestFocus()
        input.postDelayed({
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(input, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }
    
    private fun updatePlayerName(newName: String) {
        playerNameTextView.text = newName
        recordTextView.text = playerPrefs.getPlayerRecord().toString()
    }
    
    private fun launchDifficulty() {
        val intent = Intent(this, DifficultyActivity::class.java).apply {
            putExtra(DifficultyActivity.EXTRA_PLAYER_NAME, playerPrefs.getPlayerName())
            putExtra(DifficultyActivity.EXTRA_RECORD_VALUE, playerPrefs.getPlayerRecord())
        }
        startActivity(intent)
    }

    private fun launchScores() {
        val intent = Intent(this, ScoresActivity::class.java).apply {
            putExtra(ScoresActivity.EXTRA_PLAYER_NAME, playerPrefs.getPlayerName())
            putExtra(ScoresActivity.EXTRA_RECORD_VALUE, playerPrefs.getPlayerRecord())
        }
        startActivity(intent)
    }

    private fun launchCredits() {
        val intent = Intent(this, CreditsActivity::class.java).apply {
            putExtra(CreditsActivity.EXTRA_PLAYER_NAME, playerPrefs.getPlayerName())
            putExtra(CreditsActivity.EXTRA_RECORD_VALUE, playerPrefs.getPlayerRecord())
        }
        startActivity(intent)
    }

    companion object {
        const val EXTRA_PLAYER_NAME = "com.example.messi_game.extra.PLAYER_NAME"
        const val EXTRA_RECORD_VALUE = "com.example.messi_game.extra.RECORD_VALUE"
        private const val DEFAULT_RECORD = 76
    }
}
