package com.heroes.towerdefense.game.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.heroes.towerdefense.game.map.GraphicQuality
import com.heroes.towerdefense.game.model.CombatParticle
import com.heroes.towerdefense.game.model.CombatProjectile
import com.heroes.towerdefense.game.model.ProjectileVisualType
import com.heroes.towerdefense.game.model.VisualEffect
import com.heroes.towerdefense.game.model.VisualEffectType
import com.heroes.towerdefense.game.model.VisualPhysicsDebris
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Dedicated high-performance, zero-allocation Canvas renderer and animator
 * for stylized 2.5D / 2D combat visual effects, ballistic projectiles,
 * impactful explosions, shockwaves, and signature hero abilities in Heroes Tower Defense.
 */
object CombatVfxRenderer {

    // =========================================================================
    // Reusable Paints (Allocated once, zero GC allocations during frame rendering)
    // =========================================================================

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val shockwavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val pathHelper = Path()
    private val tempRect = RectF()

    // =========================================================================
    // 1. Stylized Combat Projectiles Rendering
    // =========================================================================

    /**
     * Renders a stylized combat projectile with orientation, ballistic parabolic elevation,
     * and dynamic motion trails.
     */
    fun drawStylizedProjectile(
        canvas: Canvas,
        projectile: CombatProjectile,
        w: Float,
        h: Float,
        animTime: Float
    ) {
        if (projectile.isExpired) return

        val px = projectile.currentX * w
        // Apply parabolic arc vertical elevation offset
        val py = (projectile.currentY - projectile.arcHeightOffset) * h

        val dx = projectile.targetLastX - projectile.currentX
        val dy = projectile.targetLastY - projectile.currentY
        val angleRad = atan2(dy, dx)
        val angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat()

        when (projectile.visualType) {
            ProjectileVisualType.ARCHER_ARROW -> {
                drawArcherArrow(canvas, px, py, angleDeg, w)
            }
            ProjectileVisualType.MAGE_ENERGY_MISSILE -> {
                drawMageEnergyMissile(canvas, px, py, angleDeg, w, animTime)
            }
            ProjectileVisualType.CANNON_BALL -> {
                drawCannonball(canvas, px, py, angleDeg, w, projectile.progress)
            }
            ProjectileVisualType.FROST_SHARD -> {
                drawFrostShard(canvas, px, py, angleDeg, w)
            }
            ProjectileVisualType.FIRE_BOLT -> {
                drawFireBolt(canvas, px, py, angleDeg, w, animTime)
            }
            ProjectileVisualType.ASTRAL_ARROW -> {
                drawAstralArrow(canvas, px, py, angleDeg, w, animTime)
            }
            ProjectileVisualType.SHADOW_DAGGER -> {
                drawShadowDagger(canvas, px, py, angleDeg, w, animTime)
            }
            ProjectileVisualType.GYRO_ROCKET -> {
                drawGyroRocket(canvas, px, py, angleDeg, w, animTime)
            }
            ProjectileVisualType.COSMIC_ORB -> {
                drawCosmicOrb(canvas, px, py, w, animTime)
            }
        }
    }

    private fun drawArcherArrow(canvas: Canvas, x: Float, y: Float, angleDeg: Float, w: Float) {
        val arrowLen = w * 0.038f
        val shaftW = w * 0.0035f

        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(angleDeg)

        // 1. Translucent Wind Streamer Trail (Motion Stream)
        strokePaint.color = Color.parseColor("#BAE6FD") // Soft cyan
        strokePaint.strokeWidth = 1.5f
        strokePaint.alpha = 110
        canvas.drawLine(-arrowLen * 1.6f, -shaftW * 1.5f, -arrowLen * 0.5f, -shaftW * 1.5f, strokePaint)
        canvas.drawLine(-arrowLen * 1.4f, shaftW * 1.5f, -arrowLen * 0.4f, shaftW * 1.5f, strokePaint)

        // 2. Polished Cedar Wood Shaft
        fillPaint.color = Color.parseColor("#78350F") // Dark amber-brown
        tempRect.set(-arrowLen, -shaftW * 0.5f, 0f, shaftW * 0.5f)
        canvas.drawRoundRect(tempRect, 2f, 2f, fillPaint)

        // 3. Forged Steel Arrowhead (Triangular with razor edge and barbs)
        val headLen = arrowLen * 0.32f
        val headWidth = arrowLen * 0.22f

        pathHelper.reset()
        pathHelper.moveTo(headLen, 0f) // Pointed tip
        pathHelper.lineTo(-arrowLen * 0.1f, -headWidth * 0.5f)
        pathHelper.lineTo(0f, 0f) // Barbed notch
        pathHelper.lineTo(-arrowLen * 0.1f, headWidth * 0.5f)
        pathHelper.close()

        fillPaint.color = Color.parseColor("#E2E8F0") // Polished steel
        canvas.drawPath(pathHelper, fillPaint)

        // Specular highlight on upper arrowhead blade
        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 1.5f
        strokePaint.alpha = 240
        canvas.drawLine(headLen, 0f, 0f, -headWidth * 0.35f, strokePaint)

        // 4. Emerald Feather Fletching (Dual angled vanes)
        val fletchLen = arrowLen * 0.28f
        val fletchSpan = arrowLen * 0.20f

        fillPaint.color = Color.parseColor("#10B981") // Vivid emerald vane
        pathHelper.reset()
        pathHelper.moveTo(-arrowLen, 0f)
        pathHelper.lineTo(-arrowLen - fletchLen * 0.4f, -fletchSpan)
        pathHelper.lineTo(-arrowLen + fletchLen * 0.5f, -fletchSpan * 0.8f)
        pathHelper.lineTo(-arrowLen + fletchLen, 0f)
        pathHelper.close()
        canvas.drawPath(pathHelper, fillPaint)

        pathHelper.reset()
        pathHelper.moveTo(-arrowLen, 0f)
        pathHelper.lineTo(-arrowLen - fletchLen * 0.4f, fletchSpan)
        pathHelper.lineTo(-arrowLen + fletchLen * 0.5f, fletchSpan * 0.8f)
        pathHelper.lineTo(-arrowLen + fletchLen, 0f)
        pathHelper.close()
        canvas.drawPath(pathHelper, fillPaint)

        // Tip spark
        fillPaint.color = Color.parseColor("#FEF08A")
        canvas.drawCircle(headLen, 0f, 2f, fillPaint)

        canvas.restore()
    }

    private fun drawMageEnergyMissile(
        canvas: Canvas,
        x: Float,
        y: Float,
        angleDeg: Float,
        w: Float,
        animTime: Float
    ) {
        val radius = w * 0.022f

        canvas.save()
        canvas.translate(x, y)

        // 1. Stardust Sparkle Trail
        strokePaint.color = Color.parseColor("#C084FC")
        strokePaint.strokeWidth = 2f
        val trailCount = 4
        for (i in 1..trailCount) {
            val dist = radius * (1.2f + i * 0.8f)
            val rad = Math.toRadians((angleDeg + 180f).toDouble())
            val tx = (cos(rad) * dist).toFloat() + sin(animTime * 15f + i) * (radius * 0.4f)
            val ty = (sin(rad) * dist).toFloat() + cos(animTime * 15f + i) * (radius * 0.4f)
            val sparkAlpha = (220 / (i + 1))
            fillPaint.color = Color.parseColor("#E0E7FF")
            fillPaint.alpha = sparkAlpha
            canvas.drawCircle(tx, ty, radius * (0.35f / i), fillPaint)
        }

        // 2. Soft Outer Glowing Plasma Halo
        glowPaint.color = Color.parseColor("#38BDF8") // Cyan glow
        glowPaint.alpha = 80
        canvas.drawCircle(0f, 0f, radius * 1.8f, glowPaint)

        glowPaint.color = Color.parseColor("#A855F7") // Arcane violet core aura
        glowPaint.alpha = 130
        canvas.drawCircle(0f, 0f, radius * 1.25f, glowPaint)

        // 3. Rotating Diamond Mana Crystal Core
        canvas.rotate((animTime * 180f) % 360f)
        pathHelper.reset()
        pathHelper.moveTo(0f, -radius)
        pathHelper.lineTo(radius * 0.8f, 0f)
        pathHelper.lineTo(0f, radius)
        pathHelper.lineTo(-radius * 0.8f, 0f)
        pathHelper.close()

        fillPaint.color = Color.parseColor("#BAE6FD")
        canvas.drawPath(pathHelper, fillPaint)

        // 4. White-Hot Incandescent Center Nucleus
        fillPaint.color = Color.WHITE
        canvas.drawCircle(0f, 0f, radius * 0.45f, fillPaint)

        // 5. Tilted Orbital Rune Ring
        strokePaint.color = Color.parseColor("#FDE047") // Gold spark ring
        strokePaint.strokeWidth = 2f
        tempRect.set(-radius * 1.35f, -radius * 0.45f, radius * 1.35f, radius * 0.45f)
        canvas.drawOval(tempRect, strokePaint)

        canvas.restore()
    }

    private fun drawCannonball(
        canvas: Canvas,
        x: Float,
        y: Float,
        angleDeg: Float,
        w: Float,
        progress: Float
    ) {
        val radius = w * 0.026f

        canvas.save()
        canvas.translate(x, y)

        // 1. Volumetric Smoke Trail Puffs behind the shell with flight dissipation
        fillPaint.color = Color.parseColor("#64748B")
        val rad = Math.toRadians((angleDeg + 180f).toDouble())
        val trailPuffScale = 0.8f + 0.35f * (progress % 0.33f) * 3f
        for (i in 1..3) {
            val dist = radius * (1.2f + i * 1.1f)
            val sx = (cos(rad) * dist).toFloat()
            val sy = (sin(rad) * dist).toFloat()
            fillPaint.alpha = ((140 - i * 40) * trailPuffScale).toInt().coerceIn(20, 220)
            canvas.drawCircle(sx, sy, radius * (0.6f + i * 0.35f) * trailPuffScale, fillPaint)
        }

        // 2. Heavy Cast-Iron 3D Spherical Cannonball Body
        fillPaint.color = Color.parseColor("#0F172A") // Deep cast iron shadow
        canvas.drawCircle(0f, 0f, radius, fillPaint)

        fillPaint.color = Color.parseColor("#334155") // Mid-tone iron body
        canvas.drawCircle(-radius * 0.15f, -radius * 0.15f, radius * 0.85f, fillPaint)

        // 3. Golden Riveted Reinforced Circumference Band
        strokePaint.color = Color.parseColor("#F59E0B")
        strokePaint.strokeWidth = 2.5f
        tempRect.set(-radius * 0.90f, -radius * 0.30f, radius * 0.90f, radius * 0.30f)
        canvas.drawOval(tempRect, strokePaint)

        // 4. Burning Rear Fuse with Embers
        val fuseX = (cos(rad) * radius * 0.9f).toFloat()
        val fuseY = (sin(rad) * radius * 0.9f).toFloat()
        fillPaint.color = Color.parseColor("#EF4444")
        canvas.drawCircle(fuseX, fuseY, radius * 0.30f, fillPaint)
        fillPaint.color = Color.parseColor("#FDE047")
        canvas.drawCircle(fuseX, fuseY, radius * 0.18f, fillPaint)

        // 5. Specular Highlight Sheen (Metallic reflection top-left)
        fillPaint.color = Color.parseColor("#E2E8F0")
        canvas.drawCircle(-radius * 0.35f, -radius * 0.35f, radius * 0.22f, fillPaint)

        canvas.restore()
    }

    private fun drawFrostShard(canvas: Canvas, x: Float, y: Float, angleDeg: Float, w: Float) {
        val len = w * 0.035f
        val thick = w * 0.012f

        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(angleDeg)

        // Cold mist aura
        glowPaint.color = Color.parseColor("#38BDF8")
        glowPaint.alpha = 90
        canvas.drawCircle(0f, 0f, len * 0.7f, glowPaint)

        // Faceted Ice Spike
        pathHelper.reset()
        pathHelper.moveTo(len, 0f)
        pathHelper.lineTo(-len * 0.5f, -thick)
        pathHelper.lineTo(-len * 0.8f, 0f)
        pathHelper.lineTo(-len * 0.5f, thick)
        pathHelper.close()

        fillPaint.color = Color.parseColor("#BAE6FD")
        canvas.drawPath(pathHelper, fillPaint)

        // Glacial center ridge
        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 2f
        canvas.drawLine(-len * 0.8f, 0f, len, 0f, strokePaint)

        canvas.restore()
    }

    private fun drawFireBolt(canvas: Canvas, x: Float, y: Float, angleDeg: Float, w: Float, animTime: Float) {
        val r = w * 0.024f
        canvas.save()
        canvas.translate(x, y)

        // Outer Flame Halo
        glowPaint.color = Color.parseColor("#EF4444")
        glowPaint.alpha = 110
        canvas.drawCircle(0f, 0f, r * 1.6f, glowPaint)

        // Mid Fiery Core
        fillPaint.color = Color.parseColor("#F97316")
        canvas.drawCircle(0f, 0f, r * 1.1f, fillPaint)

        // White-hot plasma nucleus
        fillPaint.color = Color.parseColor("#FEF08A")
        canvas.drawCircle(0f, 0f, r * 0.65f, fillPaint)

        // Trailing burning embers
        val rad = Math.toRadians((angleDeg + 180f).toDouble())
        for (i in 1..3) {
            val ex = (cos(rad) * r * (1.1f + i * 0.7f)).toFloat() + sin(animTime * 20f + i) * (r * 0.35f)
            val ey = (sin(rad) * r * (1.1f + i * 0.7f)).toFloat() + cos(animTime * 20f + i) * (r * 0.35f)
            fillPaint.color = Color.parseColor("#FDE047")
            canvas.drawCircle(ex, ey, r * (0.35f / i), fillPaint)
        }

        canvas.restore()
    }

    private fun drawAstralArrow(canvas: Canvas, x: Float, y: Float, angleDeg: Float, w: Float, animTime: Float) {
        val len = w * 0.042f
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(angleDeg)

        // Shimmering Celestial Light Beam
        val pulse = 1f + 0.18f * sin(animTime * 14f).toFloat()
        strokePaint.color = Color.parseColor("#38BDF8")
        strokePaint.strokeWidth = 6f * pulse
        strokePaint.alpha = (140 * pulse).toInt().coerceIn(60, 240)
        canvas.drawLine(-len * 1.2f, 0f, len, 0f, strokePaint)

        strokePaint.color = Color.WHITE
        strokePaint.strokeWidth = 2.5f
        strokePaint.alpha = 255
        canvas.drawLine(-len * 0.8f, 0f, len, 0f, strokePaint)

        // Cross-Star Arrowhead
        fillPaint.color = Color.parseColor("#FDE047")
        canvas.drawCircle(len, 0f, 4f * pulse, fillPaint)

        canvas.restore()
    }

    private fun drawShadowDagger(canvas: Canvas, x: Float, y: Float, angleDeg: Float, w: Float, animTime: Float) {
        val len = w * 0.030f
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(angleDeg + (animTime * 360f) % 360f) // Spinning shadow daggers

        // Void aura
        glowPaint.color = Color.parseColor("#7E22CE")
        glowPaint.alpha = 120
        canvas.drawCircle(0f, 0f, len * 0.8f, glowPaint)

        // Dagger blade
        pathHelper.reset()
        pathHelper.moveTo(0f, -len)
        pathHelper.lineTo(len * 0.3f, 0f)
        pathHelper.lineTo(0f, len * 0.4f)
        pathHelper.lineTo(-len * 0.3f, 0f)
        pathHelper.close()

        fillPaint.color = Color.parseColor("#C084FC")
        canvas.drawPath(pathHelper, fillPaint)

        canvas.restore()
    }

    private fun drawGyroRocket(canvas: Canvas, x: Float, y: Float, angleDeg: Float, w: Float, animTime: Float) {
        val len = w * 0.032f
        val thick = w * 0.014f

        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(angleDeg)

        // Exhaust smoke & flame with animated thrust flicker
        val flameFlicker = 0.8f + 0.35f * sin(animTime * 24f).toFloat()
        fillPaint.color = Color.parseColor("#F97316")
        canvas.drawCircle(-len * 0.7f, 0f, thick * 0.8f * flameFlicker, fillPaint)
        fillPaint.color = Color.parseColor("#FEF08A")
        canvas.drawCircle(-len * 0.5f, 0f, thick * 0.4f * flameFlicker, fillPaint)

        // Brass Rocket Body
        fillPaint.color = Color.parseColor("#D97706") // Brass
        tempRect.set(-len * 0.5f, -thick * 0.5f, len * 0.5f, thick * 0.5f)
        canvas.drawRoundRect(tempRect, 3f, 3f, fillPaint)

        // Conical Warhead
        pathHelper.reset()
        pathHelper.moveTo(len * 0.5f, -thick * 0.5f)
        pathHelper.lineTo(len, 0f)
        pathHelper.lineTo(len * 0.5f, thick * 0.5f)
        pathHelper.close()
        fillPaint.color = Color.parseColor("#DC2626") // Red nosecone
        canvas.drawPath(pathHelper, fillPaint)

        canvas.restore()
    }

    private fun drawCosmicOrb(canvas: Canvas, x: Float, y: Float, w: Float, animTime: Float) {
        val r = w * 0.024f
        canvas.save()
        canvas.translate(x, y)

        // Cosmic Nebula Swirl
        glowPaint.color = Color.parseColor("#3B82F6")
        glowPaint.alpha = 100
        canvas.drawCircle(0f, 0f, r * 1.5f, glowPaint)

        fillPaint.color = Color.parseColor("#818CF8")
        canvas.drawCircle(0f, 0f, r * 1.0f, fillPaint)

        fillPaint.color = Color.parseColor("#F472B6") // Magenta core
        canvas.drawCircle(0f, 0f, r * 0.6f, fillPaint)

        fillPaint.color = Color.WHITE
        canvas.drawCircle(0f, 0f, r * 0.25f, fillPaint)

        // Orbiting astral satellite spark
        val orbitAngle = animTime * 6f
        val orbitDist = r * 1.35f
        val ox = (cos(orbitAngle) * orbitDist).toFloat()
        val oy = (sin(orbitAngle) * orbitDist).toFloat()
        fillPaint.color = Color.parseColor("#38BDF8")
        canvas.drawCircle(ox, oy, r * 0.22f, fillPaint)

        canvas.restore()
    }

    // =========================================================================
    // 2. Stylized Visual Effects & Hero Skills Rendering
    // =========================================================================

    /**
     * Renders stylized multi-tier visual impacts, area explosions, and signature hero skills.
     */
    fun drawStylizedVisualEffect(
        canvas: Canvas,
        fx: VisualEffect,
        w: Float,
        h: Float,
        animTime: Float
    ) {
        if (fx.isExpired) return

        val fxX = fx.x * w
        val fxY = fx.y * h
        val currentR = fx.currentRadius * w
        val alpha = ((1f - fx.progress) * 255).toInt().coerceIn(0, 255)

        when (fx.effectType) {
            VisualEffectType.IMPACT_SPARK, VisualEffectType.HERO_BASIC_ATTACK -> {
                drawImpactSpark(canvas, fxX, fxY, currentR, alpha, fx.color)
            }
            VisualEffectType.AREA_EXPLOSION -> {
                drawAreaExplosion(canvas, fxX, fxY, currentR, alpha, fx.color, fx.progress)
            }
            VisualEffectType.KAEL_FLAME_STRIKE -> {
                drawKaelFlameStrike(canvas, fxX, fxY, currentR, alpha, fx.progress)
            }
            VisualEffectType.KAEL_FLAME_WAVE -> {
                drawKaelFlameWave(canvas, fxX, fxY, currentR, alpha)
            }
            VisualEffectType.LYRA_PRECISION_SHOT -> {
                drawLyraPrecisionShot(canvas, fxX, fxY, currentR, alpha)
            }
            VisualEffectType.LYRA_ARROW_VOLLEY -> {
                drawLyraArrowVolley(canvas, fxX, fxY, currentR, alpha, animTime)
            }
            VisualEffectType.BRAKK_GROUND_SLAM -> {
                drawBrakkGroundSlam(canvas, fxX, fxY, currentR, alpha, fx.progress)
            }
            VisualEffectType.BRAKK_SHIELD_WALL -> {
                drawBrakkShieldWall(canvas, fxX, fxY, currentR, alpha, animTime)
            }
            VisualEffectType.NYRA_SHADOW_STRIKE -> {
                drawNyraShadowStrike(canvas, fxX, fxY, currentR, alpha)
            }
            VisualEffectType.NYRA_BLADE_FLURRY -> {
                drawNyraBladeFlurry(canvas, fxX, fxY, currentR, alpha, animTime)
            }
            VisualEffectType.TIKO_FRAG_MINE -> {
                drawTikoFragMine(canvas, fxX, fxY, currentR, alpha, fx.progress)
            }
            VisualEffectType.TIKO_EXPLOSIVE_DEVICE -> {
                drawTikoExplosiveDevice(canvas, fxX, fxY, currentR, alpha, fx.progress)
            }
            VisualEffectType.VESPER_ASTRAL_COMET -> {
                drawVesperAstralComet(canvas, fxX, fxY, currentR, alpha)
            }
            VisualEffectType.VESPER_COSMIC_SINGULARITY -> {
                drawVesperCosmicSingularity(canvas, fxX, fxY, currentR, alpha, animTime)
            }
            VisualEffectType.BOSS_SLAM -> {
                drawAreaExplosion(canvas, fxX, fxY, currentR, alpha, Color.parseColor("#DC2626"), fx.progress)
            }
            VisualEffectType.BOSS_DEFEAT_BURST -> {
                drawBossDefeatBurst(canvas, fxX, fxY, currentR, alpha, fx.color, animTime)
            }
        }
    }

    private fun drawImpactSpark(canvas: Canvas, x: Float, y: Float, r: Float, alpha: Int, color: Int) {
        // 1. Central White-Hot Impact Flash
        fillPaint.color = Color.WHITE
        fillPaint.alpha = alpha
        canvas.drawCircle(x, y, r * 0.45f, fillPaint)

        // 2. Outward Expanding Color Shockwave Ring
        shockwavePaint.color = color
        shockwavePaint.strokeWidth = 3f
        shockwavePaint.alpha = alpha
        canvas.drawCircle(x, y, r, shockwavePaint)

        // 3. Directional Sparks radiating outward
        strokePaint.color = Color.parseColor("#FEF08A") // Bright yellow spark
        strokePaint.strokeWidth = 2f
        strokePaint.alpha = alpha
        val sparkCount = 6
        for (i in 0 until sparkCount) {
            val angle = (i * (2 * PI / sparkCount)).toFloat()
            val sx1 = x + cos(angle) * (r * 0.4f)
            val sy1 = y + sin(angle) * (r * 0.4f)
            val sx2 = x + cos(angle) * (r * 1.35f)
            val sy2 = y + sin(angle) * (r * 1.35f)
            canvas.drawLine(sx1, sy1, sx2, sy2, strokePaint)
        }
    }

    private fun drawAreaExplosion(
        canvas: Canvas,
        x: Float,
        y: Float,
        r: Float,
        alpha: Int,
        color: Int,
        progress: Float
    ) {
        // 1. Initial White Flash (during first 20% of explosion)
        if (progress < 0.25f) {
            val flashAlpha = ((1f - progress / 0.25f) * 255).toInt()
            fillPaint.color = Color.WHITE
            fillPaint.alpha = flashAlpha
            canvas.drawCircle(x, y, r * 0.6f, fillPaint)
        }

        // 2. Rolling Fiery Explosive Core (Multi-lobed fireball)
        fillPaint.color = color
        fillPaint.alpha = (alpha * 0.65f).toInt()
        canvas.drawCircle(x, y, r * 0.70f, fillPaint)

        fillPaint.color = Color.parseColor("#F59E0B") // Amber inner fire
        fillPaint.alpha = (alpha * 0.85f).toInt()
        canvas.drawCircle(x - r * 0.15f, y - r * 0.15f, r * 0.45f, fillPaint)

        fillPaint.color = Color.parseColor("#FEF08A") // Yellow-white nucleus
        fillPaint.alpha = alpha
        canvas.drawCircle(x, y, r * 0.25f, fillPaint)

        // 3. Primary & Secondary Expanding Shockwaves
        shockwavePaint.color = color
        shockwavePaint.strokeWidth = 4.5f
        shockwavePaint.alpha = alpha
        canvas.drawCircle(x, y, r, shockwavePaint)

        shockwavePaint.color = Color.WHITE
        shockwavePaint.strokeWidth = 2f
        shockwavePaint.alpha = (alpha * 0.7f).toInt()
        canvas.drawCircle(x, y, r * 0.85f, shockwavePaint)

        // 4. Ground Crater Fracture Cracks (4 radial fissures)
        strokePaint.color = Color.parseColor("#78350F")
        strokePaint.strokeWidth = 2.5f
        strokePaint.alpha = alpha
        for (i in 0..3) {
            val rad = (i * PI / 2.0).toFloat()
            val fx2 = x + cos(rad) * (r * 1.15f)
            val fy2 = y + sin(rad) * (r * 1.15f)
            canvas.drawLine(x, y, fx2, fy2, strokePaint)
        }
    }

    private fun drawKaelFlameStrike(canvas: Canvas, x: Float, y: Float, r: Float, alpha: Int, progress: Float) {
        // Vertical Incandescent Flame Column (rising and tapering with progress)
        fillPaint.color = Color.parseColor("#F97316")
        fillPaint.alpha = alpha
        val columnH = r * 2.2f * (1.1f - progress * 0.3f)
        tempRect.set(x - r * 0.4f, y - columnH, x + r * 0.4f, y + r * 0.2f)
        canvas.drawRoundRect(tempRect, r * 0.2f, r * 0.2f, fillPaint)

        // White-hot plasma core
        fillPaint.color = Color.parseColor("#FEF08A")
        tempRect.set(x - r * 0.2f, y - columnH * 0.85f, x + r * 0.2f, y)
        canvas.drawRoundRect(tempRect, r * 0.1f, r * 0.1f, fillPaint)

        // Expanding ground scorch ring
        val scorchRadius = r * (0.5f + 0.5f * progress)
        shockwavePaint.color = Color.parseColor("#EF4444")
        shockwavePaint.strokeWidth = 3f
        shockwavePaint.alpha = alpha
        canvas.drawCircle(x, y, scorchRadius, shockwavePaint)
    }

    private fun drawKaelFlameWave(canvas: Canvas, x: Float, y: Float, r: Float, alpha: Int) {
        // Concentric Serrated Fire Wave Rings
        shockwavePaint.color = Color.parseColor("#EF4444")
        shockwavePaint.strokeWidth = 5f
        shockwavePaint.alpha = alpha
        canvas.drawCircle(x, y, r, shockwavePaint)

        shockwavePaint.color = Color.parseColor("#F97316")
        shockwavePaint.strokeWidth = 3.5f
        canvas.drawCircle(x, y, r * 0.85f, shockwavePaint)

        shockwavePaint.color = Color.parseColor("#FDE047")
        shockwavePaint.strokeWidth = 2f
        canvas.drawCircle(x, y, r * 0.70f, shockwavePaint)

        // Flame crest spikes
        fillPaint.color = Color.parseColor("#F97316")
        fillPaint.alpha = alpha
        val tongues = 8
        for (i in 0 until tongues) {
            val angle = (i * (2 * PI / tongues)).toFloat()
            val tx = x + cos(angle) * r
            val ty = y + sin(angle) * r
            canvas.drawCircle(tx, ty, r * 0.15f, fillPaint)
        }
    }

    private fun drawLyraPrecisionShot(canvas: Canvas, x: Float, y: Float, r: Float, alpha: Int) {
        // Piercing Celestial Cross Star Burst
        strokePaint.color = Color.parseColor("#F59E0B")
        strokePaint.strokeWidth = 4f
        strokePaint.alpha = alpha
        canvas.drawLine(x - r * 1.5f, y, x + r * 1.5f, y, strokePaint)
        canvas.drawLine(x, y - r * 1.5f, x, y + r * 1.5f, strokePaint)

        // White-hot center diamond
        fillPaint.color = Color.WHITE
        fillPaint.alpha = alpha
        canvas.drawCircle(x, y, r * 0.4f, fillPaint)

        // Expanding light ring
        shockwavePaint.color = Color.parseColor("#FEF08A")
        shockwavePaint.strokeWidth = 2f
        shockwavePaint.alpha = alpha
        canvas.drawCircle(x, y, r, shockwavePaint)
    }

    private fun drawLyraArrowVolley(canvas: Canvas, x: Float, y: Float, r: Float, alpha: Int, animTime: Float) {
        // Mystic Green-Gold Arrow Rain Impact Zone
        fillPaint.color = Color.parseColor("#10B981")
        fillPaint.alpha = (alpha * 0.25f).toInt()
        canvas.drawCircle(x, y, r, fillPaint)

        shockwavePaint.color = Color.parseColor("#10B981")
        shockwavePaint.strokeWidth = 3f
        shockwavePaint.alpha = alpha
        canvas.drawCircle(x, y, r, shockwavePaint)

        // Light shafts representing descending arrows
        strokePaint.color = Color.parseColor("#FDE047")
        strokePaint.strokeWidth = 2f
        strokePaint.alpha = alpha
        for (i in -2..2) {
            val ox = x + i * (r * 0.35f)
            val oy = y + sin(animTime * 10f + i) * (r * 0.25f)
            canvas.drawLine(ox - 8f, oy - 25f, ox, oy, strokePaint)
            fillPaint.color = Color.WHITE
            canvas.drawCircle(ox, oy, 2.5f, fillPaint)
        }
    }

    private fun drawBrakkGroundSlam(canvas: Canvas, x: Float, y: Float, r: Float, alpha: Int, progress: Float) {
        // Heavy Seismic Ground Fissures & Rising Rock Shards
        strokePaint.color = Color.parseColor("#44403C") // Dark stone crack
        strokePaint.strokeWidth = 4f
        strokePaint.alpha = alpha
        val fissures = 6
        val fissureR = r * (0.6f + 0.4f * progress)
        for (i in 0 until fissures) {
            val angle = (i * (2 * PI / fissures)).toFloat()
            val midX = x + cos(angle) * (fissureR * 0.5f) + sin(angle * 2f) * 6f
            val midY = y + sin(angle) * (fissureR * 0.5f)
            val endX = x + cos(angle) * fissureR
            val endY = y + sin(angle) * fissureR
            canvas.drawLine(x, y, midX, midY, strokePaint)
            canvas.drawLine(midX, midY, endX, endY, strokePaint)
        }

        // Heavy Earth Dust Shockwave Ring expanding outward with progress
        val shockwaveR = r * (0.4f + 0.6f * progress)
        shockwavePaint.color = Color.parseColor("#A8A29E")
        shockwavePaint.strokeWidth = 5f
        shockwavePaint.alpha = alpha
        canvas.drawCircle(x, y, shockwaveR, shockwavePaint)

        // Inner glowing earthquake core
        fillPaint.color = Color.parseColor("#F59E0B")
        fillPaint.alpha = (alpha * 0.7f).toInt()
        canvas.drawCircle(x, y, r * 0.35f, fillPaint)
    }

    private fun drawBrakkShieldWall(canvas: Canvas, x: Float, y: Float, r: Float, alpha: Int, animTime: Float) {
        // Golden Runic Fortress Barrier Dome
        fillPaint.color = Color.parseColor("#EAB308")
        fillPaint.alpha = (alpha * 0.22f).toInt()
        canvas.drawCircle(x, y, r, fillPaint)

        shockwavePaint.color = Color.parseColor("#FDE047")
        shockwavePaint.strokeWidth = 4f
        shockwavePaint.alpha = alpha
        canvas.drawCircle(x, y, r, shockwavePaint)

        // 8 Rotating Ancient Defensive Runes around barrier rim
        val runes = 8
        fillPaint.color = Color.parseColor("#FEF08A")
        fillPaint.alpha = alpha
        for (i in 0 until runes) {
            val angle = (i * (2 * PI / runes) + animTime * 2f).toFloat()
            val rx = x + cos(angle) * r
            val ry = y + sin(angle) * r
            canvas.drawRect(rx - 4f, ry - 4f, rx + 4f, ry + 4f, fillPaint)
        }
    }

    private fun drawNyraShadowStrike(canvas: Canvas, x: Float, y: Float, r: Float, alpha: Int) {
        // Scissor Cross Void Slash Blades
        strokePaint.color = Color.parseColor("#7E22CE")
        strokePaint.strokeWidth = 5f
        strokePaint.alpha = alpha
        canvas.drawLine(x - r, y - r, x + r, y + r, strokePaint)
        canvas.drawLine(x + r, y - r, x - r, y + r, strokePaint)

        strokePaint.color = Color.parseColor("#E879F9") // Neon magenta core
        strokePaint.strokeWidth = 2f
        canvas.drawLine(x - r * 0.8f, y - r * 0.8f, x + r * 0.8f, y + r * 0.8f, strokePaint)
        canvas.drawLine(x + r * 0.8f, y - r * 0.8f, x - r * 0.8f, y + r * 0.8f, strokePaint)

        // Violet void smoke ring
        shockwavePaint.color = Color.parseColor("#A855F7")
        shockwavePaint.strokeWidth = 2f
        shockwavePaint.alpha = alpha
        canvas.drawCircle(x, y, r * 0.85f, shockwavePaint)
    }

    private fun drawNyraBladeFlurry(canvas: Canvas, x: Float, y: Float, r: Float, alpha: Int, animTime: Float) {
        // Swirling Crescent Blade Storm
        shockwavePaint.color = Color.parseColor("#A855F7")
        shockwavePaint.strokeWidth = 3f
        shockwavePaint.alpha = alpha
        canvas.drawCircle(x, y, r, shockwavePaint)

        strokePaint.color = Color.parseColor("#F472B6")
        strokePaint.strokeWidth = 3f
        strokePaint.alpha = alpha
        val blades = 5
        for (i in 0 until blades) {
            val angle = (i * (2 * PI / blades) + animTime * 12f).toFloat()
            val bx = x + cos(angle) * (r * 0.7f)
            val by = y + sin(angle) * (r * 0.7f)
            tempRect.set(bx - r * 0.25f, by - r * 0.15f, bx + r * 0.25f, by + r * 0.15f)
            canvas.drawArc(tempRect, Math.toDegrees(angle.toDouble()).toFloat(), 90f, false, strokePaint)
        }
    }

    private fun drawTikoFragMine(canvas: Canvas, x: Float, y: Float, r: Float, alpha: Int, progress: Float) {
        // Clockwork Gear Shrapnel Explosion
        drawAreaExplosion(canvas, x, y, r, alpha, Color.parseColor("#EA580C"), progress)

        // Flying Brass Cog Shrapnel Pieces
        fillPaint.color = Color.parseColor("#FDE047")
        fillPaint.alpha = alpha
        val cogs = 6
        for (i in 0 until cogs) {
            val angle = (i * (2 * PI / cogs)).toFloat()
            val cx = x + cos(angle) * (r * 0.9f)
            val cy = y + sin(angle) * (r * 0.9f)
            canvas.drawRect(cx - 3.5f, cy - 3.5f, cx + 3.5f, cy + 3.5f, fillPaint)
        }
    }

    private fun drawTikoExplosiveDevice(canvas: Canvas, x: Float, y: Float, r: Float, alpha: Int, progress: Float) {
        // High-Yield Incendiary Detonation with Multi-Tier Blast Wave
        drawAreaExplosion(canvas, x, y, r, alpha, Color.parseColor("#DC2626"), progress)

        shockwavePaint.color = Color.parseColor("#F97316")
        shockwavePaint.strokeWidth = 6f
        shockwavePaint.alpha = alpha
        canvas.drawCircle(x, y, r * 1.15f, shockwavePaint)
    }

    private fun drawVesperAstralComet(canvas: Canvas, x: Float, y: Float, r: Float, alpha: Int) {
        // Radiant Cyan Celestial Crater Impact
        fillPaint.color = Color.parseColor("#06B6D4")
        fillPaint.alpha = (alpha * 0.45f).toInt()
        canvas.drawCircle(x, y, r * 0.6f, fillPaint)

        fillPaint.color = Color.WHITE
        fillPaint.alpha = alpha
        canvas.drawCircle(x, y, r * 0.25f, fillPaint)

        shockwavePaint.color = Color.parseColor("#67E8F9")
        shockwavePaint.strokeWidth = 4f
        shockwavePaint.alpha = alpha
        canvas.drawCircle(x, y, r, shockwavePaint)

        // Radial Starlight Rays
        strokePaint.color = Color.parseColor("#A5F3FC")
        strokePaint.strokeWidth = 2f
        strokePaint.alpha = alpha
        val rays = 8
        for (i in 0 until rays) {
            val rad = (i * (2 * PI / rays)).toFloat()
            canvas.drawLine(x, y, x + cos(rad) * (r * 1.35f), y + sin(rad) * (r * 1.35f), strokePaint)
        }
    }

    private fun drawVesperCosmicSingularity(canvas: Canvas, x: Float, y: Float, r: Float, alpha: Int, animTime: Float) {
        // Gravitational Cosmic Nebula Vortex
        fillPaint.color = Color.parseColor("#1E1B4B") // Deep space black-blue
        fillPaint.alpha = (alpha * 0.55f).toInt()
        canvas.drawCircle(x, y, r * 0.75f, fillPaint)

        // Event horizon ring
        shockwavePaint.color = Color.parseColor("#3B82F6")
        shockwavePaint.strokeWidth = 4f
        shockwavePaint.alpha = alpha
        canvas.drawCircle(x, y, r, shockwavePaint)

        shockwavePaint.color = Color.parseColor("#C084FC")
        shockwavePaint.strokeWidth = 2.5f
        canvas.drawCircle(x, y, r * 0.50f, shockwavePaint)

        // Spiral galaxy arms
        strokePaint.color = Color.parseColor("#60A5FA")
        strokePaint.strokeWidth = 3f
        strokePaint.alpha = alpha
        val arms = 3
        for (i in 0 until arms) {
            val startAngle = (i * (2 * PI / arms) + animTime * 6f).toFloat()
            val armR = r * 0.85f
            tempRect.set(x - armR, y - armR, x + armR, y + armR)
            canvas.drawArc(tempRect, Math.toDegrees(startAngle.toDouble()).toFloat(), 110f, false, strokePaint)
        }

        // Center singularity star
        fillPaint.color = Color.WHITE
        fillPaint.alpha = alpha
        canvas.drawCircle(x, y, r * 0.15f, fillPaint)
    }

    private fun drawBossDefeatBurst(
        canvas: Canvas,
        x: Float,
        y: Float,
        r: Float,
        alpha: Int,
        color: Int,
        animTime: Float
    ) {
        // Cataclysmic Defeat Shockwave Burst
        drawAreaExplosion(canvas, x, y, r, alpha, color, 0.4f)

        shockwavePaint.color = Color.WHITE
        shockwavePaint.strokeWidth = 5f
        shockwavePaint.alpha = alpha
        canvas.drawCircle(x, y, r * 1.25f, shockwavePaint)

        fillPaint.color = Color.parseColor("#FDE047")
        fillPaint.alpha = alpha
        val sparks = 12
        for (i in 0 until sparks) {
            val angle = (i * (2 * PI / sparks) + animTime * 4f).toFloat()
            val sx = x + cos(angle) * (r * 1.4f)
            val sy = y + sin(angle) * (r * 1.4f)
            canvas.drawCircle(sx, sy, 4.5f, fillPaint)
        }
    }

    // =========================================================================
    // 3. Tactile Feedback Integration (Camera Screen Shake & Physical Debris)
    // =========================================================================

    /**
     * Triggers tactile screen shake, physical debris, and combat sparks upon visual effect creation.
     */
    fun triggerImpactFeedback(
        fx: VisualEffect,
        w: Float,
        h: Float,
        screenShake: CameraScreenShake,
        debrisList: MutableList<VisualPhysicsDebris>,
        particles: MutableList<CombatParticle>,
        quality: GraphicQuality
    ) {
        val fxX = fx.x * w
        val fxY = fx.y * h

        // 1. Camera Screen Shake
        if (quality.enableScreenShake && fx.effectType.triggersScreenShake) {
            screenShake.trigger(fx.effectType.shakeIntensity, 0.28f)
        }

        // 2. Physical Debris (Rocks, Embers, Metal Shrapnel)
        if (fx.effectType.generatesDebris && debrisList.size < quality.maxDebrisCount) {
            val debrisCount = when (quality) {
                GraphicQuality.LOW -> 0
                GraphicQuality.MEDIUM -> 3
                GraphicQuality.HIGH -> 6
            }
            val debrisColor = when (fx.effectType) {
                VisualEffectType.KAEL_FLAME_STRIKE -> Color.parseColor("#F97316")
                VisualEffectType.BRAKK_GROUND_SLAM -> Color.parseColor("#78716C")
                VisualEffectType.TIKO_FRAG_MINE, VisualEffectType.TIKO_EXPLOSIVE_DEVICE -> Color.parseColor("#D97706")
                else -> Color.parseColor("#475569")
            }
            for (i in 0 until debrisCount) {
                val angle = (i * (2 * PI / debrisCount.coerceAtLeast(1))).toFloat()
                val speed = 80f + (i * 25f)
                debrisList.add(
                    VisualPhysicsDebris(
                        x = fxX,
                        y = fxY,
                        z = 15f + i * 5f,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed * 0.6f, // Isometric 2.5D foreshortening
                        vz = 140f + i * 30f,
                        color = debrisColor,
                        size = 3.5f + (i % 3) * 1.5f,
                        maxLifetime = 0.85f
                    )
                )
            }
        }

        // 3. Dynamic Combat Spark Particles
        if (particles.size < quality.maxParticles) {
            val sparkCount = when (quality) {
                GraphicQuality.LOW -> 2
                GraphicQuality.MEDIUM -> 5
                GraphicQuality.HIGH -> 8
            }
            for (i in 0 until sparkCount) {
                val angle = (i * (2 * PI / sparkCount.coerceAtLeast(1))).toFloat()
                val speed = 90f + (i * 20f)
                particles.add(
                    CombatParticle(
                        x = fxX,
                        y = fxY,
                        vx = cos(angle) * speed,
                        vy = sin(angle) * speed,
                        color = fx.color,
                        radius = 3.0f,
                        maxLifetime = 0.45f
                    )
                )
            }
        }
    }
}
