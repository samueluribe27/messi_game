package com.example.messi_game
import android.content.Intent




import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.messi_game.databinding.ActivityDificultadBinding

class DificultadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDificultadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDificultadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvMenu.setOnClickListener {
            finish() // vuelve al menú anterior
        }

        // Listeners para los botones
        binding.btnFacil.setOnClickListener {
            iniciarJuego("Fácil", 20)
        }

        binding.btnDificil.setOnClickListener {
            iniciarJuego("Difícil", 40)
        }

        binding.btnExperto.setOnClickListener {
            iniciarJuego("Experto", 60)
        }
    }

    private fun iniciarJuego(dificultad: String, puntajeObjetivo: Int) {
        val nombreJugador = binding.etNombreJugador.text.toString()
        if (nombreJugador.isNotBlank()) {
            val intent = Intent(this, JuegoActivity::class.java)
            intent.putExtra("nombreJugador", nombreJugador)
            intent.putExtra("dificultad", dificultad)
            intent.putExtra("puntajeObjetivo", puntajeObjetivo)
            startActivity(intent)
        } else {
            binding.etNombreJugador.error = "Por favor ingresa tu nombre"
        }
    }
}
