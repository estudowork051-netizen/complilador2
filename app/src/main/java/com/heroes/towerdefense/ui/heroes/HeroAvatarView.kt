package com.heroes.towerdefense.ui.heroes

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.heroes.towerdefense.game.view.HeroCharacterRenderer
import com.heroes.towerdefense.model.hero.Hero
import kotlin.math.min

/**
 * Animated custom View rendering an articulated hero portrait avatar
 * in hero collection cards, preparation screens, and UI dialogs.
 * Displays breathing motions, rotating steampunk cogs, orbiting celestial orbs,
 * dynamic capes, and ornate rarity borders.
 */
class HeroAvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var hero: Hero? = null
    private var isUnlocked: Boolean = true
    private var animTimeSeconds: Float = 0f

    private var animator: ValueAnimator? = null

    init {
        // Default hero fallback
        hero = Hero.getPlannedHeroes().firstOrNull()
    }

    fun setHero(hero: Hero, isUnlocked: Boolean = true) {
        this.hero = hero
        this.isUnlocked = isUnlocked
        invalidate()
    }

    fun setHeroId(heroId: String, isUnlocked: Boolean = true) {
        val found = Hero.getPlannedHeroes().find { it.id == heroId }
        this.hero = found ?: Hero.getPlannedHeroes().firstOrNull()
        this.isUnlocked = isUnlocked
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimationLoop()
    }

    override fun onDetachedFromWindow() {
        stopAnimationLoop()
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) {
            startAnimationLoop()
        } else {
            stopAnimationLoop()
        }
    }

    private fun startAnimationLoop() {
        if (animator != null && animator!!.isRunning) return

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                animTimeSeconds += 0.033f // ~30 fps update for UI avatars
                invalidate()
            }
            start()
        }
    }

    private fun stopAnimationLoop() {
        animator?.cancel()
        animator = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val activeHero = hero ?: return
        val cx = w / 2f
        val cy = h / 2f
        val radius = (min(w, h) / 2f) - 3f

        HeroCharacterRenderer.drawHeroAvatar(
            canvas = canvas,
            hero = activeHero,
            cx = cx,
            cy = cy,
            radius = radius,
            animTime = animTimeSeconds,
            isUnlocked = isUnlocked
        )
    }
}
