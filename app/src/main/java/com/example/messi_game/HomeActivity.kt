package com.example.messi_game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.messi_game.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarBotones()
    }

    private fun configurarBotones() {
        // Botón JUGAR - Navega a DificultadActivity
        binding.btnJugar.setOnClickListener {
            val intent = Intent(this@HomeActivity, DifficultyActivity::class.java)
            startActivity(intent)
        }

        // Botón PUNTAJES - Muestra los récords guardados
        binding.btnPuntajes.setOnClickListener {
            mostrarPuntajes()
        }

        // Botón CRÉDITOS - Muestra información del juego
        binding.btnCreditos.setOnClickListener {
            mostrarCreditos()
        }
    }

    private fun mostrarPuntajes() {
        val prefs = getSharedPreferences("MessiGamePrefs", Context.MODE_PRIVATE)
        val recordFacil = prefs.getInt("record_Fácil", 0)
        val recordDificil = prefs.getInt("record_Difícil", 0)
        val recordExperto = prefs.getInt("record_Experto", 0)

        val mensaje = """
            🏆 MEJORES PUNTAJES 🏆
            
            🟢 Fácil: $recordFacil segundos
            🟡 Difícil: $recordDificil segundos
            🔴 Experto: $recordExperto segundos
            
            ¡Sigue mejorando tus récords!
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Puntajes")
            .setMessage(mensaje)
            .setPositiveButton("Aceptar", null)
            .setNeutralButton("Borrar Récords") { _, _ ->
                confirmarBorrarRecords()
            }
            .show()
    }

    private fun confirmarBorrarRecords() {
        AlertDialog.Builder(this)
            .setTitle("¿Borrar Récords?")
            .setMessage("Se eliminarán todos los puntajes guardados. ¿Deseas continuar?")
            .setPositiveButton("Sí") { _, _ ->
                val prefs = getSharedPreferences("MessiGamePrefs", Context.MODE_PRIVATE)
                prefs.edit().clear().apply()

                AlertDialog.Builder(this)
                    .setTitle("Récords Eliminados")
                    .setMessage("Todos los puntajes han sido borrados exitosamente")
                    .setPositiveButton("Aceptar", null)
                    .show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun mostrarCreditos() {
        val mensaje = """
            🎮 MESSI GAME 🎮
            
            Versión: 1.0
            
            📖 CÓMO JUGAR:
            Mueve a Messi con la barra inferior para esquivar los balones que caen. Sobrevive el mayor tiempo posible.
            
            ⚡ PUNTAJE:
            Cada segundo que sobrevivas suma 1 punto.
            
            🏆 DIFICULTADES:
            • Fácil: 20 segundos objetivo
            • Difícil: 40 segundos objetivo
            • Experto: 60 segundos objetivo
            
            Desarrollado con ❤️
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Créditos")
            .setMessage(mensaje)
            .setPositiveButton("Cerrar", null)
            .show()
    }
}