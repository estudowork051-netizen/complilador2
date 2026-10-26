package com.heroes.towerdefense.game.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.heroes.towerdefense.core.GameConstants
import com.heroes.towerdefense.game.boss.ActiveBoss
import com.heroes.towerdefense.game.engine.GameEngine
import com.heroes.towerdefense.game.map.EnvironmentTheme
import com.heroes.towerdefense.game.map.GraphicQuality
import com.heroes.towerdefense.game.map.StageOneMap
import com.heroes.towerdefense.game.model.AmbientParticle
import com.heroes.towerdefense.game.model.CombatParticle
import com.heroes.towerdefense.game.model.TowerSpot
import com.heroes.towerdefense.game.model.VisualPhysicsDebris
import com.heroes.towerdefense.model.tower.TowerType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Custom 2D Canvas View for Heroes Tower Defense.
 *
 * Implements a high-fidelity 9-layer isometric battlefield architecture (Clash Royale style reference):
 * 1. 3D Isometric multi-tone ground terrain with elevation contours and stylized grid tiles
 * 2. Cobblestone stone path with paver stones, beveled stone curbs, and wooden bridges
 * 3. Flowing liquid hazard stream (river, mystic stream, void pool, subterranean lake, or molten lava)
 * 4. Living environmental props (swaying trees with wind physics, mossy boulders, crystal monoliths)
 * 5. Animated torches & iron braziers with dancing flames and radial point-light glow
 * 6. 3D Isometric Spawning Portal with spinning dimensional vortex
 * 7. 3D Medieval Fortress Base with corner bastions, portcullis gate, and waving flags
 * 8. Y-Sorted Entities (Towers, Enemies, Bosses, Deployed Hero) for realistic isometric depth
 * 9. Stylized 3D Isometric Defensive Towers (Archer, Mage, Cannon, Frost) with recoil and spawn animations
 * 10. Combat projectiles, impact shockwaves, visual physics debris, and atmospheric ambient particles
 *
 * Zero-allocation onDraw loop with static reusable Paint, Path, and RectF instances.
 */
class StageOneCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var gameEngine: GameEngine? = null
    var selectedSpotId: String? = null
    var previewTowerType: TowerType? = null

    var onSpotClickedListener: ((TowerSpot) -> Unit)? = null
    var onEmptyGroundClickedListener: (() -> Unit)? = null

    // Upgrade Visual Part 3: Environment Themes & Graphics Quality
    var environmentTheme: EnvironmentTheme = EnvironmentTheme.HIGHLAND_FOREST
        private set
    var graphicQuality: GraphicQuality = GraphicQuality.HIGH
    var stageNumber: Int = 1
        private set

    // Camera & Impact Physics
    val screenShake = CameraScreenShake()

    // Dynamic Combat & Ambient Particles
    val particles: MutableList<CombatParticle> = mutableListOf()
    val ambientParticles: MutableList<AmbientParticle> = mutableListOf()
    val physicsDebris: MutableList<VisualPhysicsDebris> = mutableListOf()
    private val handledEffectIds: MutableSet<String> = mutableSetOf()

    private var lastUpdateTimeNanos: Long = 0L
    private var totalAnimTime: Float = 0f

    // =========================================================================
    // Static Reusable Paints (Zero-Allocation onDraw)
    // =========================================================================

    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val debrisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val ambientParticlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val spotFoundationPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E293B")
        style = Paint.Style.FILL
    }

    private val spotBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#475569")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val spotPlusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#94A3B8")
        style = Paint.Style.STROKE
        strokeWidth = 3f
        strokeCap = Paint.Cap.ROUND
    }

    private val selectionRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F59E0B")
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val rangeFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#22F59E0B")
        style = Paint.Style.FILL
    }

    private val rangeStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#88F59E0B")
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(10f, 8f), 0f)
    }

    // Projectile Paints
    private val arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F59E0B")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
    }

    private val magicBoltCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E7FF")
        style = Paint.Style.FILL
    }

    private val magicBoltGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#818CF8")
        style = Paint.Style.FILL
    }

    private val cannonballPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#18181B")
        style = Paint.Style.FILL
    }

    private val cannonSparkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F97316")
        style = Paint.Style.FILL
    }

    private val effectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val shockwavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    // Reusable Data Structures for Y-Sorting (Pre-allocated pool to avoid allocations)
    private data class SortableEntity(
        val type: Int, // 0 = Spot/Tower, 1 = Enemy/Boss, 2 = Hero
        val y: Float,
        val obj: Any
    )

    private val entitySortPool = ArrayList<SortableEntity>(64)

    init {
        initAmbientParticles(width.toFloat().coerceAtLeast(800f), height.toFloat().coerceAtLeast(1200f))
    }

    /**
     * Updates stage number and configures the environment theme.
     */
    fun setStage(stageNum: Int) {
        stageNumber = stageNum
        environmentTheme = EnvironmentTheme.fromStageNumber(stageNum)
        ambientParticles.clear()
        val w = width.toFloat().coerceAtLeast(800f)
        val h = height.toFloat().coerceAtLeast(1200f)
        initAmbientParticles(w, h)
        invalidate()
    }

    private fun initAmbientParticles(w: Float, h: Float) {
        if (graphicQuality.maxAmbientParticles <= 0) return
        val rnd = java.util.Random()
        val count = graphicQuality.maxAmbientParticles
        for (i in 0 until count) {
            val speed = 10f + rnd.nextFloat() * 25f
            val angle = when (environmentTheme.ambientParticleType) {
                com.heroes.towerdefense.game.model.AmbientParticleType.VOLCANIC_EMBERS -> -Math.PI.toFloat() * 0.5f + (rnd.nextFloat() - 0.5f) * 0.6f // Upward
                else -> Math.PI.toFloat() * 0.25f + (rnd.nextFloat() - 0.5f) * 0.8f // Drifting diagonal
            }
            ambientParticles.add(
                AmbientParticle(
                    x = rnd.nextFloat() * w,
                    y = rnd.nextFloat() * h,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = if (i % 2 == 0) environmentTheme.ambientParticleColor else environmentTheme.ambientParticleSecondaryColor,
                    size = 2.5f + rnd.nextFloat() * 3.5f,
                    phase = rnd.nextFloat() * 6.28f,
                    oscillationSpeed = 1f + rnd.nextFloat() * 2f,
                    baseAlpha = 0.4f + rnd.nextFloat() * 0.4f
                )
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                val w = width.toFloat()
                val h = height.toFloat()
                if (w <= 0f || h <= 0f) return false

                val nx = event.x / w
                val ny = event.y / h

                val engine = gameEngine ?: return false
                val spots = engine.towerSpots

                // Find if a spot was tapped within touch radius
                val touchedSpot = spots.find { spot ->
                    val dx = spot.x - nx
                    val dy = spot.y - ny
                    sqrt(dx * dx + dy * dy) <= GameConstants.TOUCH_SPOT_SELECTION_RADIUS
                }

                if (touchedSpot != null) {
                    selectedSpotId = touchedSpot.id
                    invalidate()
                    onSpotClickedListener?.invoke(touchedSpot)
                } else {
                    if (selectedSpotId != null) {
                        selectedSpotId = null
                        invalidate()
                        onEmptyGroundClickedListener?.invoke()
                    }
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        // Time delta calculation
        val now = System.nanoTime()
        val dt = if (lastUpdateTimeNanos > 0L) {
            ((now - lastUpdateTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
        } else {
            0.016f
        }
        lastUpdateTimeNanos = now
        totalAnimTime += dt

        // Update Camera Screen Shake
        if (graphicQuality.enableScreenShake) {
            screenShake.update(dt)
        }

        // Apply Camera Screen Shake Translation
        canvas.save()
        if (graphicQuality.enableScreenShake && screenShake.isShaking) {
            canvas.translate(screenShake.offsetX, screenShake.offsetY)
        }

        // 1. Terrain Base Layer & Isometric Grid
        EnvironmentMapRenderer.drawTerrainBase(canvas, w, h, environmentTheme)

        // 2. Liquid Hazard Stream (River / Lava / Void Stream)
        EnvironmentMapRenderer.drawLiquidHazard(canvas, w, h, environmentTheme, totalAnimTime)

        // 3. 3D Cobblestone Path with Elevated Stone Curbs
        EnvironmentMapRenderer.drawCobblestonePath(canvas, w, h, environmentTheme)

        // 4. Environmental Props (Swaying Trees, Mossy Rocks, Animated Torches)
        drawEnvironmentalProps(canvas, w, h)

        // 5. 3D Spawning Portal & Medieval Fortress Base
        EnvironmentMapRenderer.drawSpawningPortal(canvas, w, h, environmentTheme, totalAnimTime)
        EnvironmentMapRenderer.drawFortressBase(canvas, w, h, environmentTheme, totalAnimTime)

        // 6. Y-Sorted Entity Layer (Towers, Enemies, Bosses, Deployed Hero)
        drawDepthSortedEntities(canvas, w, h)

        // 7. Active Projectiles
        drawProjectiles(canvas, w, h)

        // 8. Visual Impacts and Explosions
        drawVisualEffects(canvas, w, h)

        // 9. Visual Physics Debris (Chunks, Splinters, Bouncing Rocks)
        drawPhysicsDebris(canvas, w, h, dt)

        // 10. Dynamic Combat Particles (Part 8)
        drawCombatParticles(canvas, w, h, dt)

        // 11. Atmospheric Ambient Particles (Pollen, Fireflies, Embers, Stardust)
        drawAmbientParticles(canvas, w, h, dt)

        // 12. Atmospheric Vignette & Contrast Shading
        EnvironmentMapRenderer.drawAtmosphericVignette(canvas, w, h, environmentTheme)

        // Restore Camera Screen Shake
        canvas.restore()
    }

    // =========================================================================
    // Environmental Props Layer
    // =========================================================================

    private fun drawEnvironmentalProps(canvas: Canvas, w: Float, h: Float) {
        val animT = if (graphicQuality.enableFoliageSway) totalAnimTime else 0f

        // North-West Grove Trees
        EnvironmentMapRenderer.drawStylizedTree(canvas, w * 0.48f, h * 0.08f, w * 0.075f, environmentTheme, animT, 0f)
        EnvironmentMapRenderer.drawStylizedTree(canvas, w * 0.88f, h * 0.10f, w * 0.085f, environmentTheme, animT, 1.2f)

        // Mid-West Rocky Outcropping
        EnvironmentMapRenderer.drawStylizedRock(canvas, w * 0.08f, h * 0.22f, w * 0.065f, environmentTheme)
        EnvironmentMapRenderer.drawStylizedRock(canvas, w * 0.88f, h * 0.38f, w * 0.070f, environmentTheme)

        // Center Meadow Trees & Bushes
        EnvironmentMapRenderer.drawStylizedTree(canvas, w * 0.50f, h * 0.32f, w * 0.085f, environmentTheme, animT, 2.5f)
        EnvironmentMapRenderer.drawStylizedRock(canvas, w * 0.08f, h * 0.60f, w * 0.060f, environmentTheme)
        EnvironmentMapRenderer.drawStylizedTree(canvas, w * 0.90f, h * 0.62f, w * 0.080f, environmentTheme, animT, 3.8f)

        // South-West Approach Trees
        EnvironmentMapRenderer.drawStylizedTree(canvas, w * 0.12f, h * 0.88f, w * 0.085f, environmentTheme, animT, 5.0f)
        EnvironmentMapRenderer.drawStylizedRock(canvas, w * 0.88f, h * 0.85f, w * 0.075f, environmentTheme)

        // Animated Torches positioned along the tactical road bends
        val torchR = w * 0.038f
        val enableLights = graphicQuality.enableDynamicLighting
        EnvironmentMapRenderer.drawAnimatedTorch(canvas, w * 0.28f, h * 0.20f, torchR, environmentTheme, totalAnimTime, enableLights)
        EnvironmentMapRenderer.drawAnimatedTorch(canvas, w * 0.72f, h * 0.24f, torchR, environmentTheme, totalAnimTime, enableLights)
        EnvironmentMapRenderer.drawAnimatedTorch(canvas, w * 0.72f, h * 0.50f, torchR, environmentTheme, totalAnimTime, enableLights)
        EnvironmentMapRenderer.drawAnimatedTorch(canvas, w * 0.30f, h * 0.76f, torchR, environmentTheme, totalAnimTime, enableLights)
    }

    // =========================================================================
    // Y-Sorted Depth Entity Layer
    // =========================================================================

    private fun drawDepthSortedEntities(canvas: Canvas, w: Float, h: Float) {
        val engine = gameEngine ?: return
        entitySortPool.clear()

        // 1. Collect Tower Spots / Built Towers
        val spots = engine.towerSpots
        for (spot in spots) {
            entitySortPool.add(SortableEntity(0, spot.y * h, spot))
        }

        // 2. Collect Active Enemies & Bosses
        val enemies = engine.activeEnemies
        for (enemy in enemies) {
            if (enemy.isAlive || enemy.isDying) {
                entitySortPool.add(SortableEntity(1, enemy.y * h, enemy))
            }
        }

        // 3. Collect Deployed Hero
        val hero = engine.deployedHero
        if (hero != null) {
            entitySortPool.add(SortableEntity(2, hero.y * h, hero))
        }

        // Sort ascending by Y position for true 3D isometric occlusion depth
        entitySortPool.sortBy { it.y }

        // Render in sorted depth order
        for (entity in entitySortPool) {
            when (entity.type) {
                0 -> drawSingleTowerSpot(canvas, entity.obj as TowerSpot, w, h)
                1 -> {
                    val enemy = entity.obj as com.heroes.towerdefense.game.model.ActiveEnemy
                    val ex = enemy.x * w
                    val ey = enemy.y * h
                    if (enemy is ActiveBoss) {
                        EnemyCharacterRenderer.drawBoss(canvas, enemy, ex, ey, w, h)
                    } else {
                        EnemyCharacterRenderer.drawEnemy(canvas, enemy, ex, ey, w, h)
                    }
                }
                2 -> {
                    val deployedHero = entity.obj as com.heroes.towerdefense.game.model.DeployedHero
                    HeroCharacterRenderer.drawDeployedHero(canvas, deployedHero, w, h)
                }
            }
        }
    }

    private fun drawSingleTowerSpot(canvas: Canvas, spot: TowerSpot, w: Float, h: Float) {
        val sx = spot.x * w
        val sy = spot.y * h
        val spotRadius = w * 0.055f
        val isSelected = spot.id == selectedSpotId

        // If selected, draw attack range preview circle
        if (isSelected) {
            val rangeRadius = when {
                spot.placedTower != null -> spot.placedTower!!.range * w
                previewTowerType != null -> when (previewTowerType!!) {
                    TowerType.ARCHER_TOWER -> GameConstants.ARCHER_RANGE * w
                    TowerType.MAGE_TOWER -> GameConstants.MAGE_RANGE * w
                    TowerType.CANNON_TOWER -> GameConstants.CANNON_RANGE * w
                    TowerType.FROST_TOWER -> GameConstants.MAGE_RANGE * w
                }
                else -> GameConstants.ARCHER_RANGE * w
            }
            canvas.drawCircle(sx, sy, rangeRadius, rangeFillPaint)
            canvas.drawCircle(sx, sy, rangeRadius, rangeStrokePaint)
        }

        // 1. Isometric Stone Foundation Pedestal
        IsometricTowerRenderer.drawTowerShadow(canvas, sx, sy, spotRadius)
        canvas.drawCircle(sx, sy, spotRadius, spotFoundationPaint)
        canvas.drawCircle(sx, sy, spotRadius, spotBorderPaint)

        val tower = spot.placedTower
        if (tower == null) {
            // Empty build spot: subtle plus cross
            val crossSize = spotRadius * 0.45f
            canvas.drawLine(sx - crossSize, sy, sx + crossSize, sy, spotPlusPaint)
            canvas.drawLine(sx, sy - crossSize, sx, sy + crossSize, spotPlusPaint)

            // If selected with preview, show 3D holographic projection
            if (isSelected && previewTowerType != null) {
                IsometricTowerRenderer.drawPlacementPreview(
                    canvas = canvas,
                    cx = sx,
                    cy = sy,
                    type = previewTowerType!!,
                    rangeRadius = when (previewTowerType!!) {
                        TowerType.ARCHER_TOWER -> GameConstants.ARCHER_RANGE * w
                        TowerType.MAGE_TOWER -> GameConstants.MAGE_RANGE * w
                        TowerType.CANNON_TOWER -> GameConstants.CANNON_RANGE * w
                        TowerType.FROST_TOWER -> GameConstants.MAGE_RANGE * w
                    },
                    isValid = true,
                    animTime = totalAnimTime
                )
            }
        } else {
            // Built Tower: Calculate spawn scale bounce & attack recoil kickback
            val spawnScale = if (tower.spawnAnimTimer > 0f) {
                val p = 1f - (tower.spawnAnimTimer / 0.5f)
                1f + 0.25f * sin(p * Math.PI.toFloat()) * (1f - p)
            } else 1f

            val recoilOffset = if (tower.recoilAnimTimer > 0f) {
                (tower.recoilAnimTimer / 0.20f)
            } else 0f

            when (tower.type) {
                TowerType.ARCHER_TOWER -> IsometricTowerRenderer.drawArcherTower(
                    canvas, sx, sy, spotRadius, recoilOffset, spawnScale, totalAnimTime
                )
                TowerType.MAGE_TOWER -> IsometricTowerRenderer.drawMageTower(
                    canvas, sx, sy, spotRadius, recoilOffset, spawnScale, totalAnimTime
                )
                TowerType.CANNON_TOWER -> IsometricTowerRenderer.drawCannonTower(
                    canvas, sx, sy, spotRadius, recoilOffset, spawnScale, totalAnimTime
                )
                TowerType.FROST_TOWER -> IsometricTowerRenderer.drawFrostTower(
                    canvas, sx, sy, spotRadius, spawnScale, totalAnimTime
                )
            }
        }

        // Draw glowing gold selection ring if spot is actively selected
        if (isSelected) {
            canvas.drawCircle(sx, sy, spotRadius * 1.25f, selectionRingPaint)
        }
    }

    // =========================================================================
    // Projectiles & Combat Visuals
    // =========================================================================

    private fun drawProjectiles(canvas: Canvas, w: Float, h: Float) {
        val engine = gameEngine ?: return
        val projectiles = engine.activeProjectiles.toList()

        for (p in projectiles) {
            CombatVfxRenderer.drawStylizedProjectile(canvas, p, w, h, totalAnimTime)
        }
    }

    private fun drawVisualEffects(canvas: Canvas, w: Float, h: Float) {
        val engine = gameEngine ?: return
        val effects = engine.visualEffects.toList()

        for (fx in effects) {
            if (fx.isExpired) continue

            // Trigger tactile impact feedback (screen shake, physical debris, sparks) once on spawn
            if (handledEffectIds.add(fx.id)) {
                CombatVfxRenderer.triggerImpactFeedback(
                    fx = fx,
                    w = w,
                    h = h,
                    screenShake = screenShake,
                    debrisList = physicsDebris,
                    particles = particles,
                    quality = graphicQuality
                )
            }

            CombatVfxRenderer.drawStylizedVisualEffect(canvas, fx, w, h, totalAnimTime)
        }

        // Periodic cleanup of handled effect IDs cache
        if (handledEffectIds.size > 200) {
            val liveIds = effects.map { it.id }.toSet()
            handledEffectIds.retainAll(liveIds)
        }
    }

    // =========================================================================
    // Physical Debris & Combat Particles
    // =========================================================================

    private fun drawPhysicsDebris(canvas: Canvas, w: Float, h: Float, dt: Float) {
        if (physicsDebris.isEmpty()) return
        val it = physicsDebris.iterator()
        while (it.hasNext()) {
            val d = it.next()
            d.update(dt)
            if (d.isExpired || d.x < -40f || d.x > w + 40f || d.y < -40f || d.y > h + 40f) {
                it.remove()
            } else {
                debrisPaint.color = d.color
                debrisPaint.alpha = (d.alpha * 255).toInt().coerceIn(0, 255)

                canvas.save()
                canvas.translate(d.x, d.screenY)
                canvas.rotate(d.rotation)
                canvas.drawRect(-d.size * 0.5f, -d.size * 0.5f, d.size * 0.5f, d.size * 0.5f, debrisPaint)
                canvas.restore()
            }
        }
    }

    private fun drawCombatParticles(canvas: Canvas, w: Float, h: Float, dt: Float) {
        if (particles.isEmpty()) return
        val it = particles.iterator()
        while (it.hasNext()) {
            val p = it.next()
            p.update(dt)
            if (p.isExpired || p.x < -20f || p.x > w + 20f || p.y < -20f || p.y > h + 20f) {
                it.remove()
            } else {
                particlePaint.color = p.color
                particlePaint.alpha = (p.alpha * 255).toInt().coerceIn(0, 255)
                canvas.drawCircle(p.x, p.y, p.radius, particlePaint)
            }
        }
    }

    private fun drawAmbientParticles(canvas: Canvas, w: Float, h: Float, dt: Float) {
        if (ambientParticles.isEmpty()) return
        for (ap in ambientParticles) {
            ap.update(dt, w, h)
            ambientParticlePaint.color = ap.color
            ambientParticlePaint.alpha = (ap.currentAlpha * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(ap.x, ap.y, ap.size, ambientParticlePaint)
        }
    }

    // =========================================================================
    // Public Visual FX Triggers
    // =========================================================================

    fun triggerScreenShake(intensity: Float = 8f, duration: Float = 0.25f) {
        if (graphicQuality.enableScreenShake) {
            screenShake.trigger(intensity, duration)
        }
    }

    fun spawnBuildingDust(px: Float, py: Float) {
        spawnSparks(px, py, Color.parseColor("#CBD5E1"), count = 14)
        if (graphicQuality.enablePhysicsDebris) {
            spawnDebris(px, py, Color.parseColor("#475569"), count = 8)
        }
    }

    fun spawnDebris(px: Float, py: Float, color: Int, count: Int = 6) {
        if (!graphicQuality.enablePhysicsDebris) return
        val rnd = java.util.Random()
        for (i in 0 until count) {
            val angle = rnd.nextFloat() * 6.28f
            val hSpeed = 40f + rnd.nextFloat() * 120f
            physicsDebris.add(
                VisualPhysicsDebris(
                    x = px,
                    y = py,
                    z = 5f + rnd.nextFloat() * 20f,
                    vx = cos(angle) * hSpeed,
                    vy = sin(angle) * hSpeed * 0.6f, // Isometric vertical compression
                    vz = 140f + rnd.nextFloat() * 200f,
                    color = color,
                    size = 3.5f + rnd.nextFloat() * 4f,
                    rotation = rnd.nextFloat() * 360f,
                    rotationSpeed = (rnd.nextFloat() - 0.5f) * 720f,
                    maxLifetime = 0.65f + rnd.nextFloat() * 0.35f
                )
            )
        }
    }

    fun spawnSparks(px: Float, py: Float, color: Int, count: Int = 8) {
        val rnd = java.util.Random()
        for (i in 0 until count) {
            val angle = rnd.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 60f + rnd.nextFloat() * 160f
            val vx = cos(angle) * speed
            val vy = sin(angle) * speed
            particles.add(
                CombatParticle(
                    x = px,
                    y = py,
                    vx = vx,
                    vy = vy,
                    color = color,
                    radius = 3.5f + rnd.nextFloat() * 4f,
                    maxLifetime = 0.35f + rnd.nextFloat() * 0.35f
                )
            )
        }
    }

    fun spawnEnemyDefeatSparks(
        px: Float,
        py: Float,
        visualType: com.heroes.towerdefense.game.model.EnemyVisualType = com.heroes.towerdefense.game.model.EnemyVisualType.GOBLIN
    ) {
        val (c1, c2) = when (visualType) {
            com.heroes.towerdefense.game.model.EnemyVisualType.GOBLIN -> Color.parseColor("#34D399") to Color.parseColor("#FEF08A")
            com.heroes.towerdefense.game.model.EnemyVisualType.SKELETON -> Color.parseColor("#E2E8F0") to Color.parseColor("#06B6D4")
            com.heroes.towerdefense.game.model.EnemyVisualType.ORC -> Color.parseColor("#DC2626") to Color.parseColor("#94A3B8")
            com.heroes.towerdefense.game.model.EnemyVisualType.DARK_MAGE -> Color.parseColor("#A855F7") to Color.parseColor("#E9D5FF")
            com.heroes.towerdefense.game.model.EnemyVisualType.OBSIDIAN_GOLEM -> Color.parseColor("#EA580C") to Color.parseColor("#FDE047")
            com.heroes.towerdefense.game.model.EnemyVisualType.SHADOW_QUEEN -> Color.parseColor("#8B5CF6") to Color.parseColor("#C084FC")
            com.heroes.towerdefense.game.model.EnemyVisualType.ASTRAL_DRAGON -> Color.parseColor("#38BDF8") to Color.parseColor("#F43F5E")
        }
        spawnSparks(px, py, c1, count = 12)
        spawnSparks(px, py, c2, count = 8)
        if (visualType == com.heroes.towerdefense.game.model.EnemyVisualType.OBSIDIAN_GOLEM ||
            visualType == com.heroes.towerdefense.game.model.EnemyVisualType.ORC
        ) {
            spawnDebris(px, py, c1, count = 6)
        }
    }

    fun spawnHeroSkillBurst(px: Float, py: Float, color: Int) {
        spawnSparks(px, py, color, count = 16)
        spawnSparks(px, py, Color.WHITE, count = 8)
        triggerScreenShake(intensity = 6f, duration = 0.20f)
    }

    fun spawnVictoryFireworks(w: Float, h: Float) {
        val rnd = java.util.Random()
        val colors = intArrayOf(
            Color.parseColor("#F59E0B"), // Gold
            Color.parseColor("#10B981"), // Emerald
            Color.parseColor("#38BDF8"), // Cyan
            Color.parseColor("#F43F5E"), // Rose
            Color.parseColor("#A855F7")  // Purple
        )
        for (burst in 0 until 5) {
            val cx = w * (0.15f + rnd.nextFloat() * 0.7f)
            val cy = h * (0.15f + rnd.nextFloat() * 0.45f)
            val col = colors[burst % colors.size]
            spawnSparks(cx, cy, col, count = 16)
        }
        triggerScreenShake(intensity = 5f, duration = 0.40f)
    }
}
