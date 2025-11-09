package com.example.messi_game

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.messi_game.databinding.ActivityJuegoBinding
import kotlin.random.Random

class JuegoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJuegoBinding
    private val handler = Handler(Looper.getMainLooper())

    // Variables del juego
    private var puntaje = 0
    private var record = 0
    private var juegoActivo = false
    private var nombreJugador = ""
    private var dificultad = ""
    private var puntajeObjetivo = 20

    // Lista para mantener referencia de los balones
    private val balones = mutableListOf<ImageView>()

    // Velocidades según dificultad
    private var velocidadCaida = 15 // píxeles por frame
    private var intervaloBalones = 2000L // milisegundos entre balones

    // Runnables
    private lateinit var gameLoopRunnable: Runnable
    private lateinit var spawnBalonRunnable: Runnable
    private lateinit var puntajeRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJuegoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener datos del intent
        nombreJugador = intent.getStringExtra("nombreJugador") ?: "Jugador"
        dificultad = intent.getStringExtra("dificultad") ?: "Fácil"
        puntajeObjetivo = intent.getIntExtra("puntajeObjetivo", 20)

        // Configurar dificultad
        configurarDificultad()

        // Cargar record
        cargarRecord()

        // Configurar UI
        binding.tvNombreJugador.text = nombreJugador
        binding.tvRecord.text = record.toString()
        binding.tvPuntaje.text = "0"

        // Configurar SeekBar para mover a Messi
        binding.seekBarMover.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                moverMessi(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Botones
        binding.tvMenu.setOnClickListener {
            detenerJuego()
            finish()
        }

        binding.btnReintentar.setOnClickListener {
            reiniciarJuego()
        }

        binding.btnMenuPrincipal.setOnClickListener {
            detenerJuego()
            finish()
        }

        // Iniciar juego
        iniciarJuego()
    }

    private fun configurarDificultad() {
        when (dificultad) {
            "Fácil" -> {
                velocidadCaida = 12
                intervaloBalones = 2500L
            }
            "Difícil" -> {
                velocidadCaida = 18
                intervaloBalones = 1500L
            }
            "Experto" -> {
                velocidadCaida = 25
                intervaloBalones = 1000L
            }
        }
    }

    private fun cargarRecord() {
        val prefs = getSharedPreferences("MessiGamePrefs", Context.MODE_PRIVATE)
        record = prefs.getInt("record_$dificultad", 0)
    }

    private fun guardarRecord() {
        if (puntaje > record) {
            val prefs = getSharedPreferences("MessiGamePrefs", Context.MODE_PRIVATE)
            prefs.edit().putInt("record_$dificultad", puntaje).apply()
            record = puntaje
            binding.tvMensajeGameOver.text = "¡Nuevo récord!"
            binding.tvMensajeGameOver.visibility = android.view.View.VISIBLE
        } else {
            binding.tvMensajeGameOver.visibility = android.view.View.GONE
        }
    }

    private fun moverMessi(progress: Int) {
        val containerWidth = binding.gameContainer.width
        val messiWidth = binding.ivMessi.width
        val maxX = containerWidth - messiWidth
        val newX = (maxX * progress / 100f)
        binding.ivMessi.x = newX
    }

    private fun iniciarJuego() {
        juegoActivo = true
        puntaje = 0
        binding.tvPuntaje.text = "0"
        binding.layoutGameOver.visibility = android.view.View.GONE

        // Loop principal del juego
        gameLoopRunnable = object : Runnable {
            override fun run() {
                if (juegoActivo) {
                    actualizarBalones()
                    verificarColisiones()
                    handler.postDelayed(this, 16) // ~60 FPS
                }
            }
        }
        handler.post(gameLoopRunnable)

        // Spawn de balones
        spawnBalonRunnable = object : Runnable {
            override fun run() {
                if (juegoActivo) {
                    crearBalon()
                    handler.postDelayed(this, intervaloBalones)
                }
            }
        }
        handler.post(spawnBalonRunnable)

        // Contador de puntaje (1 punto por segundo)
        puntajeRunnable = object : Runnable {
            override fun run() {
                if (juegoActivo) {
                    puntaje++
                    binding.tvPuntaje.text = puntaje.toString()
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.postDelayed(puntajeRunnable, 1000)
    }

    private fun crearBalon() {
        val balon = ImageView(this)
        balon.setImageResource(R.drawable.balon) // Asegúrate de tener esta imagen

        val size = 60 // tamaño del balón en dp
        val params = ViewGroup.LayoutParams(
            (size * resources.displayMetrics.density).toInt(),
            (size * resources.displayMetrics.density).toInt()
        )
        balon.layoutParams = params

        // Posición X aleatoria
        val containerWidth = binding.gameContainer.width
        val maxX = containerWidth - params.width
        val randomX = Random.nextInt(0, maxX.coerceAtLeast(1))

        balon.x = randomX.toFloat()
        balon.y = -params.height.toFloat()

        binding.gameContainer.addView(balon)
        balones.add(balon)
    }

    private fun actualizarBalones() {
        val balonesToRemove = mutableListOf<ImageView>()

        for (balon in balones) {
            balon.y += velocidadCaida

            // Remover balones que salieron de pantalla
            if (balon.y > binding.gameContainer.height) {
                balonesToRemove.add(balon)
            }
        }

        // Remover balones
        for (balon in balonesToRemove) {
            binding.gameContainer.removeView(balon)
            balones.remove(balon)
        }
    }

    private fun verificarColisiones() {
        val messiRect = android.graphics.Rect()
        binding.ivMessi.getHitRect(messiRect)

        for (balon in balones) {
            val balonRect = android.graphics.Rect()
            balon.getHitRect(balonRect)

            if (android.graphics.Rect.intersects(messiRect, balonRect)) {
                // Colisión detectada
                gameOver()
                return
            }
        }
    }

    private fun gameOver() {
        juegoActivo = false
        guardarRecord()

        binding.tvPuntajeFinal.text = "Puntaje: $puntaje"
        binding.layoutGameOver.visibility = android.view.View.VISIBLE
    }

    private fun reiniciarJuego() {
        // Limpiar balones
        for (balon in balones) {
            binding.gameContainer.removeView(balon)
        }
        balones.clear()

        // Reiniciar posición de Messi
        binding.seekBarMover.progress = 50

        iniciarJuego()
    }

    private fun detenerJuego() {
        juegoActivo = false
        handler.removeCallbacks(gameLoopRunnable)
        handler.removeCallbacks(spawnBalonRunnable)
        handler.removeCallbacks(puntajeRunnable)
    }

    override fun onPause() {
        super.onPause()
        if (juegoActivo) {
            gameOver()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detenerJuego()
    }
}