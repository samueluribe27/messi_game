package com.example.messi_game

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.caverock.androidsvg.SVGImageView
import com.google.android.material.appbar.MaterialToolbar
import kotlin.random.Random

class GameActivity : AppCompatActivity() {
    
    private lateinit var gameCanvas: FrameLayout
    private lateinit var gameScore: TextView
    private lateinit var messiFace: SVGImageView
    private lateinit var gameOverOverlay: FrameLayout
    private lateinit var gameOverScoreText: TextView
    private lateinit var gameRecordTextView: TextView
    private lateinit var particleView: ParticleView
    
    private var score = 0
    private var difficulty = "easy"
    private var isGameOver = false
    private var comboCount = 0
    private var lastDodgeTime = 0L
    
    private lateinit var playerName: String
    private var recordValue: Int = 76
    private lateinit var playerPrefs: PlayerPreferences
    
    private var ballHitSound: MediaPlayer? = null
    
    private val handler = Handler(Looper.getMainLooper())
    private val gameObjects = mutableListOf<View>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        
        // Inicializar preferencias del jugador
        playerPrefs = PlayerPreferences(this)
        
        // Obtener información del jugador y dificultad
        playerName = intent.getStringExtra(EXTRA_PLAYER_NAME)
            ?: getString(R.string.home_player_name_placeholder)
        recordValue = intent.getIntExtra(EXTRA_RECORD_VALUE, DEFAULT_RECORD)
        difficulty = intent.getStringExtra(EXTRA_DIFFICULTY) ?: "easy"
        
        // Configurar toolbar
        val toolbar: MaterialToolbar = findViewById(R.id.gameToolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Configurar interfaz
        findViewById<TextView>(R.id.gamePlayerName).text = playerName
        findViewById<TextView>(R.id.gameRecord).text = recordValue.toString()
        
        gameCanvas = findViewById(R.id.gameCanvas)
        gameScore = findViewById(R.id.gameScore)
        messiFace = findViewById(R.id.gameMessiFace)
        gameOverOverlay = findViewById(R.id.gameOverOverlay)
        gameOverScoreText = findViewById(R.id.gameOverScore)
        gameRecordTextView = findViewById(R.id.gameRecord)
        
        // Crear y agregar vista de partículas
        particleView = ParticleView(this)
        particleView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        gameCanvas.addView(particleView)
        
        gameScore.text = score.toString()
        updateRecordDisplay()
        
        // Inicializar sonido
        ballHitSound = MediaPlayer.create(this, R.raw.balon_cae)
        
        // Configurar control táctil para Messi
        gameCanvas.setOnTouchListener { _, event ->
            if (!isGameOver) {
                when (event.action) {
                    MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN -> {
                        moveMessiFaceToTouch(event.x)
                    }
                }
            }
            true
        }
        
        // Setup restart and exit buttons
        findViewById<View>(R.id.restartButton).setOnClickListener {
            restartGame()
        }
        
        findViewById<View>(R.id.exitButton).setOnClickListener {
            finish()
        }
        
        // Start game with countdown
        handler.postDelayed({
            showCountdown()
        }, 500)
    }
    
    private fun showCountdown() {
        val countdownNumbers = listOf("3", "2", "1", "¡GO!")
        var currentIndex = 0
        
        fun showNextNumber() {
            if (currentIndex >= countdownNumbers.size) {
                // Iniciar el juego después del countdown
                startGame()
                return
            }
            
            val number = countdownNumbers[currentIndex]
            val isGo = number == "¡GO!"
            
            val countdownText = TextView(this).apply {
                text = number
                textSize = if (isGo) 56f else 72f
                setTextColor(if (isGo) 0xFF4CAF50.toInt() else 0xFFFFFFFF.toInt())
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setShadowLayer(12f, 0f, 0f, 0xFF000000.toInt())
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.CENTER
                }
                alpha = 0f
                scaleX = 0.3f
                scaleY = 0.3f
            }
            
            gameCanvas.addView(countdownText)
            
            // Animación de aparición explosiva
            countdownText.animate()
                .alpha(1f)
                .scaleX(1.5f)
                .scaleY(1.5f)
                .setDuration(200)
                .withEndAction {
                    // Animación de salida
                    countdownText.animate()
                        .alpha(0f)
                        .scaleX(2f)
                        .scaleY(2f)
                        .setDuration(300)
                        .setStartDelay(if (isGo) 300 else 200)
                        .withEndAction {
                            gameCanvas.removeView(countdownText)
                            currentIndex++
                            handler.postDelayed({
                                showNextNumber()
                            }, 100)
                        }
                        .start()
                }
                .start()
        }
        
        showNextNumber()
    }
    
    private fun moveMessiFaceToTouch(touchX: Float) {
        val faceWidth = messiFace.width
        val canvasWidth = gameCanvas.width
        
        // Center Messi on touch position
        var newX = touchX - (faceWidth / 2f)
        
        // Keep within bounds
        if (newX < 0) newX = 0f
        if (newX > canvasWidth - faceWidth) newX = (canvasWidth - faceWidth).toFloat()
        
        messiFace.x = newX
    }
    
    private fun startGame() {
        // Spawn game objects periodically
        spawnGameObject()
    }
    
    private fun spawnGameObject() {
        if (isGameOver) {
            return
        }
        
        val canvasWidth = gameCanvas.width
        
        // Create ball (SVG)
        val ball = SVGImageView(this, null).apply {
            setImageResource(R.raw.balon_icon)
            layoutParams = FrameLayout.LayoutParams(70, 70)
            contentDescription = getString(R.string.game_ball_description)
            scaleX = 0.5f
            scaleY = 0.5f
            alpha = 0f
        }
        
        // Random X position
        val randomX = Random.nextInt(0, canvasWidth - 70)
        ball.x = randomX.toFloat()
        ball.y = -70f
        
        gameCanvas.addView(ball)
        gameObjects.add(ball)
        
        // Entry animation (scale and fade in)
        ball.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(200)
            .start()
        
        // Animate falling with rotation
        animateFalling(ball)
        
        // Spawn next object
        val delay = when (difficulty) {
            "easy" -> 1200L
            "medium" -> 800L
            "hard" -> 500L
            else -> 1200L
        }
        
        handler.postDelayed({
            spawnGameObject()
        }, delay)
    }
    
    private fun animateFalling(ball: View) {
        val canvasHeight = gameCanvas.height
        val fallDuration = when (difficulty) {
            "easy" -> 2500L
            "medium" -> 1800L
            "hard" -> 1200L
            else -> 2500L
        }
        
        var collisionHandled = false
        
        // Check collision periodically during fall
        val collisionCheckRunnable = object : Runnable {
            override fun run() {
                if (!isGameOver && gameObjects.contains(ball) && !collisionHandled) {
                    if (checkCollision(ball)) {
                        // Hit by ball - Game Over!
                        collisionHandled = true
                        ball.animate().cancel() // Stop animation immediately
                        playSound()
                        handler.postDelayed({
                            gameOver()
                        }, 100) // Small delay to let sound start
                    } else {
                        handler.postDelayed(this, 30) // Check every 30ms for better precision
                    }
                }
            }
        }
        handler.postDelayed(collisionCheckRunnable, 100)
        
        // Rotation animation
        ball.animate()
            .rotation(360f * 3) // 3 full rotations
            .setDuration(fallDuration)
            .setInterpolator(AccelerateInterpolator())
            .start()
        
        // Falling animation
        ball.animate()
            .y(canvasHeight.toFloat())
            .setDuration(fallDuration)
            .withEndAction {
                // Only increase score if ball reached bottom AND game is not over AND no collision
                if (!isGameOver && gameObjects.contains(ball) && !collisionHandled) {
                    playSound() // Play sound when ball hits ground
                    showScoreEffect(ball.x + 35, canvasHeight.toFloat() - 50)
                    
                    // ✨ Crear efecto de estrellas al esquivar
                    particleView.createStarBurst(ball.x + 35, canvasHeight.toFloat() - 50, 12)
                    
                    // 🔥 Detectar combo (esquivar rápido)
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastDodgeTime < 1000) { // Menos de 1 segundo
                        comboCount++
                        if (comboCount >= 3) {
                            // Trail de fuego en combo
                            particleView.createFireTrail(messiFace.x + 32, messiFace.y + 32, 15)
                            // Mostrar texto de combo
                            showComboText(comboCount)
                        }
                    } else {
                        comboCount = 0
                    }
                    lastDodgeTime = currentTime
                    
                    handler.postDelayed({
                        if (!isGameOver) {
                            score++
                            gameScore.text = score.toString()
                            
                            // Update record in real-time if current score is higher
                            updateRecordIfHigher()
                        }
                    }, 50)
                }
                
                // Remove from canvas
                handler.postDelayed({
                    gameCanvas.removeView(ball)
                    gameObjects.remove(ball)
                }, 100)
            }
            .start()
    }
    
    private fun showScoreEffect(x: Float, y: Float) {
        val effectText = TextView(this).apply {
            text = "+1"
            textSize = 24f
            setTextColor(0xFF4CAF50.toInt())
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            this.x = x
            this.y = y
            alpha = 0f
        }
        
        gameCanvas.addView(effectText)
        
        effectText.animate()
            .alpha(1f)
            .y(y - 100)
            .setDuration(800)
            .withEndAction {
                gameCanvas.removeView(effectText)
            }
            .start()
    }
    
    private fun playSound() {
        try {
            // Create a new MediaPlayer instance for each sound to avoid overlap issues
            val sound = MediaPlayer.create(this, R.raw.balon_cae)
            sound?.setOnCompletionListener { mp ->
                mp.release()
            }
            sound?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun updateRecordIfHigher() {
        val currentRecord = playerPrefs.getScoreForDifficulty(difficulty)
        if (score > currentRecord) {
            // Save new record immediately
            playerPrefs.updateScoreForDifficulty(difficulty, score)
            // Update display
            updateRecordDisplay()
            
            // 🎊 Confeti en múltiples puntos de la pantalla
            val canvasWidth = gameCanvas.width
            val canvasHeight = gameCanvas.height
            
            // Confeti desde arriba izquierda
            particleView.createConfetti(canvasWidth * 0.2f, canvasHeight * 0.2f, 25)
            // Confeti desde arriba derecha
            particleView.createConfetti(canvasWidth * 0.8f, canvasHeight * 0.2f, 25)
            // Confeti desde el centro
            particleView.createConfetti(canvasWidth * 0.5f, canvasHeight * 0.3f, 30)
            // Confeti desde abajo izquierda
            particleView.createConfetti(canvasWidth * 0.3f, canvasHeight * 0.7f, 20)
            // Confeti desde abajo derecha
            particleView.createConfetti(canvasWidth * 0.7f, canvasHeight * 0.7f, 20)
            
            // Mostrar texto de récord
            showRecordBreakText()
        }
    }
    
    private fun updateRecordDisplay() {
        val currentRecord = playerPrefs.getPlayerRecord()
        gameRecordTextView.text = currentRecord.toString()
    }
    
    private fun showRecordBreakText() {
        val messages = listOf(
            "🔥 ¡ESTÁS EN FUEGO!",
            "⚡ ¡IMPARABLE!",
            "🌟 ¡NUEVO RÉCORD!",
            "💪 ¡INCREÍBLE!",
            "🚀 ¡LEYENDA!"
        )
        
        val recordText = TextView(this).apply {
            text = messages.random()
            textSize = 32f
            setTextColor(0xFFFFD700.toInt()) // Gold
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(8f, 0f, 0f, 0xFF000000.toInt())
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
            alpha = 0f
            scaleX = 0.5f
            scaleY = 0.5f
        }
        
        gameCanvas.addView(recordText)
        
        // Animación de aparición
        recordText.animate()
            .alpha(1f)
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(300)
            .withEndAction {
                // Animación de salida
                recordText.animate()
                    .alpha(0f)
                    .scaleX(1.5f)
                    .scaleY(1.5f)
                    .translationY(-100f)
                    .setDuration(1000)
                    .setStartDelay(800)
                    .withEndAction {
                        gameCanvas.removeView(recordText)
                    }
                    .start()
            }
            .start()
    }
    
    private fun showComboText(combo: Int) {
        val comboText = TextView(this).apply {
            text = when {
                combo >= 10 -> "🔥 COMBO x$combo! ¡EN LLAMAS!"
                combo >= 7 -> "⚡ COMBO x$combo! ¡BRUTAL!"
                combo >= 5 -> "💥 COMBO x$combo! ¡GENIAL!"
                else -> "🔥 COMBO x$combo!"
            }
            textSize = 28f
            setTextColor(when {
                combo >= 10 -> 0xFFFF0000.toInt() // Red
                combo >= 7 -> 0xFFFF6600.toInt() // Orange
                combo >= 5 -> 0xFFFFAA00.toInt() // Yellow-Orange
                else -> 0xFFFFD700.toInt() // Gold
            })
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setShadowLayer(6f, 0f, 0f, 0xFF000000.toInt())
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL or android.view.Gravity.TOP
                topMargin = 120
            }
            alpha = 0f
            scaleX = 0.3f
            scaleY = 0.3f
        }
        
        gameCanvas.addView(comboText)
        
        // Animación de aparición explosiva
        comboText.animate()
            .alpha(1f)
            .scaleX(1.3f)
            .scaleY(1.3f)
            .setDuration(200)
            .withEndAction {
                comboText.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .withEndAction {
                        // Mantener visible y luego fade out
                        comboText.animate()
                            .alpha(0f)
                            .translationY(-50f)
                            .setDuration(600)
                            .setStartDelay(400)
                            .withEndAction {
                                gameCanvas.removeView(comboText)
                            }
                            .start()
                    }
                    .start()
            }
            .start()
    }
    
    private fun gameOver() {
        isGameOver = true
        
        // 💥 Crear explosión al perder
        val messiCenterX = messiFace.x + messiFace.width / 2
        val messiCenterY = messiFace.y + messiFace.height / 2
        particleView.createExplosion(messiCenterX, messiCenterY, 50)
        
        // Vibrate on game over
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        }
        
        // Save score for this difficulty if it's higher
        playerPrefs.updateScoreForDifficulty(difficulty, score)
        
        // Stop all animations and spawning
        handler.removeCallbacksAndMessages(null)
        
        // Stop all ball animations
        for (obj in gameObjects.toList()) {
            obj.animate().cancel()
            gameCanvas.removeView(obj)
        }
        gameObjects.clear()
        
        // Show game over overlay
        gameOverScoreText.text = "Puntaje: $score"
        gameOverOverlay.visibility = View.VISIBLE
    }
    
    private fun restartGame() {
        // Reset game state
        isGameOver = false
        score = 0
        comboCount = 0
        lastDodgeTime = 0L
        gameScore.text = score.toString()
        gameOverOverlay.visibility = View.GONE
        findViewById<TextView>(R.id.gameOverTitle).text = "¡Perdiste!"
        
        // Clear particles
        particleView.clear()
        
        // Clear any remaining objects
        for (obj in gameObjects.toList()) {
            obj.animate().cancel()
            gameCanvas.removeView(obj)
        }
        gameObjects.clear()
        
        // Reset Messi position
        messiFace.x = (gameCanvas.width - messiFace.width) / 2f
        
        // Start game again with countdown
        handler.postDelayed({
            showCountdown()
        }, 500)
    }
    
    private fun checkCollision(gameObject: View): Boolean {
        val faceLeft = messiFace.x
        val faceRight = messiFace.x + messiFace.width
        val faceTop = messiFace.y
        val faceBottom = messiFace.y + messiFace.height
        
        val objectLeft = gameObject.x
        val objectRight = gameObject.x + gameObject.width
        val objectTop = gameObject.y
        val objectBottom = gameObject.y + gameObject.height
        
        // Check if objects overlap
        return objectRight >= faceLeft && 
               objectLeft <= faceRight && 
               objectBottom >= faceTop && 
               objectTop <= faceBottom
    }
    
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        
        // Release sound resources
        ballHitSound?.release()
        ballHitSound = null
    }
    
    override fun onPause() {
        super.onPause()
        // Pause sound if playing
        ballHitSound?.pause()
    }
    
    companion object {
        const val EXTRA_PLAYER_NAME = "com.example.messi_game.extra.PLAYER_NAME"
        const val EXTRA_RECORD_VALUE = "com.example.messi_game.extra.RECORD_VALUE"
        const val EXTRA_DIFFICULTY = "com.example.messi_game.extra.DIFFICULTY"
        private const val DEFAULT_RECORD = 76
    }
}

