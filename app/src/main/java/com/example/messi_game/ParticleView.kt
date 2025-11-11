package com.example.messi_game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import kotlin.random.Random

class ParticleView(context: Context) : View(context) {
    
    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var life: Float,
        var maxLife: Float,
        var size: Float,
        var color: Int,
        var rotation: Float = 0f,
        var rotationSpeed: Float = 0f,
        var shape: ParticleShape = ParticleShape.CIRCLE
    )
    
    enum class ParticleShape {
        CIRCLE, SQUARE, STAR, TRIANGLE, HEART
    }
    
    private val particles = mutableListOf<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    init {
        setWillNotDraw(false)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            
            // Update particle physics
            particle.x += particle.vx
            particle.y += particle.vy
            particle.vy += 0.5f // Gravity
            particle.life -= 1f
            particle.rotation += particle.rotationSpeed
            
            // Remove dead particles
            if (particle.life <= 0) {
                iterator.remove()
                continue
            }
            
            // Calculate alpha based on life
            val alpha = (particle.life / particle.maxLife * 255).toInt().coerceIn(0, 255)
            paint.color = (particle.color and 0x00FFFFFF) or (alpha shl 24)
            paint.style = Paint.Style.FILL
            
            // Draw particle based on shape
            canvas.save()
            canvas.translate(particle.x, particle.y)
            canvas.rotate(particle.rotation)
            
            when (particle.shape) {
                ParticleShape.CIRCLE -> {
                    canvas.drawCircle(0f, 0f, particle.size, paint)
                }
                ParticleShape.SQUARE -> {
                    canvas.drawRect(
                        -particle.size, -particle.size,
                        particle.size, particle.size,
                        paint
                    )
                }
                ParticleShape.STAR -> {
                    drawStar(canvas, particle.size, paint)
                }
                ParticleShape.TRIANGLE -> {
                    drawTriangle(canvas, particle.size, paint)
                }
                ParticleShape.HEART -> {
                    drawHeart(canvas, particle.size, paint)
                }
            }
            
            canvas.restore()
        }
        
        // Continue animation if there are particles
        if (particles.isNotEmpty()) {
            invalidate()
        }
    }
    
    private fun drawStar(canvas: Canvas, size: Float, paint: Paint) {
        val path = android.graphics.Path()
        val points = 5
        val outerRadius = size
        val innerRadius = size * 0.4f
        
        for (i in 0 until points * 2) {
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val angle = Math.PI / points * i - Math.PI / 2
            val x = (radius * Math.cos(angle)).toFloat()
            val y = (radius * Math.sin(angle)).toFloat()
            
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        canvas.drawPath(path, paint)
    }
    
    private fun drawTriangle(canvas: Canvas, size: Float, paint: Paint) {
        val path = android.graphics.Path()
        path.moveTo(0f, -size)
        path.lineTo(size, size)
        path.lineTo(-size, size)
        path.close()
        canvas.drawPath(path, paint)
    }
    
    private fun drawHeart(canvas: Canvas, size: Float, paint: Paint) {
        val path = android.graphics.Path()
        val scale = size / 10f
        
        path.moveTo(0f, 3f * scale)
        
        // Left curve
        path.cubicTo(
            -5f * scale, 0f,
            -8f * scale, -3f * scale,
            -5f * scale, -6f * scale
        )
        path.cubicTo(
            -3f * scale, -8f * scale,
            0f, -6f * scale,
            0f, -3f * scale
        )
        
        // Right curve
        path.cubicTo(
            0f, -6f * scale,
            3f * scale, -8f * scale,
            5f * scale, -6f * scale
        )
        path.cubicTo(
            8f * scale, -3f * scale,
            5f * scale, 0f,
            0f, 3f * scale
        )
        
        canvas.drawPath(path, paint)
    }
    
    // ✨ Estrellas al esquivar
    fun createStarBurst(x: Float, y: Float, count: Int = 15) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * Math.PI * 2
            val speed = Random.nextFloat() * 8 + 4
            val vx = (Math.cos(angle) * speed).toFloat()
            val vy = (Math.sin(angle) * speed).toFloat()
            
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    life = Random.nextFloat() * 30 + 30,
                    maxLife = 60f,
                    size = Random.nextFloat() * 6 + 4,
                    color = when (Random.nextInt(3)) {
                        0 -> 0xFFFFD700.toInt() // Gold
                        1 -> 0xFFFFFFFF.toInt() // White
                        else -> 0xFFFFA500.toInt() // Orange
                    },
                    rotation = Random.nextFloat() * 360,
                    rotationSpeed = Random.nextFloat() * 10 - 5,
                    shape = ParticleShape.STAR
                )
            )
        }
        invalidate()
    }
    
    // 💥 Explosión al perder
    fun createExplosion(x: Float, y: Float, count: Int = 40) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * Math.PI * 2
            val speed = Random.nextFloat() * 15 + 5
            val vx = (Math.cos(angle) * speed).toFloat()
            val vy = (Math.sin(angle) * speed).toFloat() - 5
            
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    life = Random.nextFloat() * 40 + 40,
                    maxLife = 80f,
                    size = Random.nextFloat() * 10 + 5,
                    color = when (Random.nextInt(4)) {
                        0 -> 0xFFFF4444.toInt() // Red
                        1 -> 0xFFFF8800.toInt() // Orange
                        2 -> 0xFFFFAA00.toInt() // Yellow-Orange
                        else -> 0xFFFFFF00.toInt() // Yellow
                    },
                    rotation = Random.nextFloat() * 360,
                    rotationSpeed = Random.nextFloat() * 20 - 10,
                    shape = when (Random.nextInt(3)) {
                        0 -> ParticleShape.CIRCLE
                        1 -> ParticleShape.SQUARE
                        else -> ParticleShape.TRIANGLE
                    }
                )
            )
        }
        invalidate()
    }
    
    // 🎊 Confeti al ganar
    fun createConfetti(x: Float, y: Float, count: Int = 50) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * Math.PI * 0.8 + Math.PI * 0.6 // Upward spread
            val speed = Random.nextFloat() * 20 + 10
            val vx = (Math.cos(angle) * speed).toFloat()
            val vy = (Math.sin(angle) * speed).toFloat()
            
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    life = Random.nextFloat() * 80 + 80,
                    maxLife = 160f,
                    size = Random.nextFloat() * 8 + 4,
                    color = when (Random.nextInt(6)) {
                        0 -> 0xFFFF1744.toInt() // Red
                        1 -> 0xFF00E676.toInt() // Green
                        2 -> 0xFF2979FF.toInt() // Blue
                        3 -> 0xFFFFD600.toInt() // Yellow
                        4 -> 0xFFE040FB.toInt() // Purple
                        else -> 0xFFFF6D00.toInt() // Orange
                    },
                    rotation = Random.nextFloat() * 360,
                    rotationSpeed = Random.nextFloat() * 15 - 7.5f,
                    shape = when (Random.nextInt(4)) {
                        0 -> ParticleShape.CIRCLE
                        1 -> ParticleShape.SQUARE
                        2 -> ParticleShape.TRIANGLE
                        else -> ParticleShape.HEART
                    }
                )
            )
        }
        invalidate()
    }
    
    // 🔥 Trail de fuego en combos
    fun createFireTrail(x: Float, y: Float, count: Int = 8) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * Math.PI * 0.5 + Math.PI * 0.25 // Upward
            val speed = Random.nextFloat() * 5 + 2
            val vx = (Math.cos(angle) * speed).toFloat()
            val vy = (Math.sin(angle) * speed).toFloat()
            
            particles.add(
                Particle(
                    x = x + Random.nextFloat() * 20 - 10,
                    y = y + Random.nextFloat() * 20 - 10,
                    vx = vx,
                    vy = vy,
                    life = Random.nextFloat() * 20 + 20,
                    maxLife = 40f,
                    size = Random.nextFloat() * 8 + 6,
                    color = when (Random.nextInt(3)) {
                        0 -> 0xFFFF4444.toInt() // Red
                        1 -> 0xFFFF8800.toInt() // Orange
                        else -> 0xFFFFAA00.toInt() // Yellow
                    },
                    rotation = Random.nextFloat() * 360,
                    rotationSpeed = Random.nextFloat() * 10 - 5,
                    shape = ParticleShape.CIRCLE
                )
            )
        }
        invalidate()
    }
    
    fun clear() {
        particles.clear()
        invalidate()
    }
}

