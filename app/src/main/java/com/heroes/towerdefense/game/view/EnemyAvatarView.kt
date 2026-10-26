package com.heroes.towerdefense.game.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.heroes.towerdefense.game.model.EnemyVisualType
import kotlin.math.min

/**
 * Animated custom View rendering an articulated monster or boss portrait avatar
 * in boss cinematic entrance modals, preparation screens, and UI dialogs
 * (Visual Upgrade Part 2).
 */
class EnemyAvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var visualType: EnemyVisualType = EnemyVisualType.GOBLIN
        private set

    private var animTimeSeconds: Float = 0f
    private var animator: ValueAnimator? = null

    fun setEnemyType(type: EnemyVisualType) {
        this.visualType = type
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

        val cx = w / 2f
        val cy = h / 2f
        val radius = (min(w, h) / 2f) - 3f

        EnemyCharacterRenderer.drawEnemyAvatar(
            canvas = canvas,
            visualType = visualType,
            cx = cx,
            cy = cy,
            radius = radius,
            animTime = animTimeSeconds
        )
    }
}
