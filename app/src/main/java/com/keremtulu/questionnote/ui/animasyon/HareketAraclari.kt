package com.keremtulu.questionnote.ui.animasyon

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.snap
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.ui.tema.LocalHareketPolitikasi
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Consistent motion timing and transitions. Decorative motion follows Android's reduced-motion setting. */
object SureToken {
    const val INSTANT = 50
    const val FAST = 100
    const val NORMAL = 160
    const val MEDIUM = 240
    const val SLOW = 320
    const val VERY_SLOW = 400
    const val CELEBRATION = 800
    const val MAJOR = 1200
}

val EasingPremium: Map<String, Easing> = mapOf(
    "swiftOut" to CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f),
    "swiftIn" to CubicBezierEasing(0.4f, 0f, 1f, 1f),
    "smoothOut" to CubicBezierEasing(0f, 0f, 0.2f, 1f),
    "bounceOut" to CubicBezierEasing(0.34f, 1.2f, 0.64f, 1f),
    "standard" to CubicBezierEasing(0.4f, 0f, 0.2f, 1f),
    "emphasized" to CubicBezierEasing(0.2f, 0f, 0f, 1f),
)

fun tweenPremium(
    durationMs: Int = SureToken.NORMAL,
    easing: Easing = EasingPremium.getValue("standard"),
    delayMs: Int = 0,
): AnimationSpec<Float> = tween(durationMillis = durationMs, delayMillis = delayMs, easing = easing)

fun springPremium(
    dampingRatio: Float = Spring.DampingRatioMediumBouncy,
    stiffness: Float = Spring.StiffnessMedium,
): AnimationSpec<Float> = spring(dampingRatio = dampingRatio, stiffness = stiffness)

fun staggerDelay(index: Int, baseDelay: Int = 45, maxDelay: Int = 280): Int =
    (index.coerceAtLeast(0) * baseDelay).coerceAtMost(maxDelay)

fun staggeredTween(
    index: Int,
    durationMs: Int = SureToken.MEDIUM,
    baseDelay: Int = 45,
    easing: Easing = EasingPremium.getValue("smoothOut"),
): AnimationSpec<Float> = tweenPremium(durationMs, easing, staggerDelay(index, baseDelay))

object GecisOzelikleri {
    val sayfaGiris: AnimationSpec<Float> = tweenPremium(SureToken.MEDIUM, EasingPremium.getValue("emphasized"))
    val sayfaCikis: AnimationSpec<Float> = tweenPremium(SureToken.FAST, EasingPremium.getValue("swiftIn"))
    val kartGenisleme: AnimationSpec<Float> = tweenPremium(SureToken.MEDIUM, EasingPremium.getValue("smoothOut"))
    val modalGiris: AnimationSpec<Float> = springPremium(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)
}

/** Animated float that resolves immediately when the user has requested reduced motion. */
@Composable
fun animateFloatPremium(
    target: Float,
    animationSpec: AnimationSpec<Float> = springPremium(),
    label: String = "premium-float",
): State<Float> {
    val hareket = LocalHareketPolitikasi.current
    return animateFloatAsState(
        targetValue = target,
        animationSpec = if (hareket.hareketAcik) animationSpec else snap(),
        label = label,
    )
}

/** Low-cost shimmer value shared by placeholders in a loading surface. */
@Composable
fun rememberShimmer(durationMs: Int = 1_400): State<Float> {
    val hareket = LocalHareketPolitikasi.current
    if (!hareket.hareketAcik) return remember { mutableFloatStateOf(0.5f) }
    val transition = rememberInfiniteTransition(label = "premium-shimmer")
    return transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs.coerceAtLeast(1), easing = LinearEasing),
        ),
        label = "shimmer-position",
    )
}

@Composable
fun rememberPulse(
    minAlpha: Float = 0.4f,
    maxAlpha: Float = 1f,
    durationMs: Int = 1_000,
): State<Float> {
    val hareket = LocalHareketPolitikasi.current
    if (!hareket.hareketAcik) return remember(maxAlpha) { mutableFloatStateOf(maxAlpha) }
    val transition = rememberInfiniteTransition(label = "premium-pulse")
    return transition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs.coerceAtLeast(1), easing = EasingPremium.getValue("smoothOut")),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
        ),
        label = "pulse-alpha",
    )
}

@Composable
fun rememberButtonPress(): Pair<MutableState<Boolean>, State<Float>> {
    val pressed = remember { mutableStateOf(false) }
    val scale = animateFloatPremium(
        target = if (pressed.value) 0.97f else 1f,
        animationSpec = springPremium(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label = "button-press-scale",
    )
    return pressed to scale
}

/** Staggered enter state for lists whose items are composed together. */
@Composable
fun animateItemInsertion(index: Int): State<Float> {
    val hareket = LocalHareketPolitikasi.current
    val shown = remember(index) { mutableStateOf(!hareket.hareketAcik) }
    LaunchedEffect(index, hareket.hareketAcik) {
        shown.value = true
    }
    return animateFloatPremium(
        target = if (shown.value) 1f else 0f,
        animationSpec = staggeredTween(index),
        label = "list-item-enter",
    )
}

/** Explicit, opt-in horizontal swipe navigation; apply only to a surface without a horizontal pager. */
fun Modifier.horizontalSwipeNavigation(
    enabled: Boolean = true,
    thresholdDp: Float = 72f,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
): Modifier = pointerInput(enabled, thresholdDp, onSwipeLeft, onSwipeRight) {
    if (!enabled) return@pointerInput
    var dragAmount = 0f
    detectHorizontalDragGestures(
        onHorizontalDrag = { change, amount ->
            dragAmount += amount
            if (abs(dragAmount) > thresholdDp.dp.toPx() * 1.4f) change.consume()
        },
        onDragEnd = {
            val thresholdPx = thresholdDp.dp.toPx()
            when {
                dragAmount <= -thresholdPx -> onSwipeLeft()
                dragAmount >= thresholdPx -> onSwipeRight()
            }
            dragAmount = 0f
        },
        onDragCancel = { dragAmount = 0f },
    )
}

/** A small particle model used by celebration overlays and previews. */
data class Parcacik(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color,
    val rotation: Float = 0f,
    val rotationSpeed: Float = 0f,
    val life: Float = 1f,
    val shape: ParcacikSekli = ParcacikSekli.DAIRE,
)

enum class ParcacikSekli { DAIRE, YILDIZ, UCGEN, KARE }

fun createCelebrationParticles(
    centerX: Float,
    centerY: Float,
    count: Int = 48,
    colors: List<Color>,
    spread: Float = 1f,
): List<Parcacik> {
    if (count <= 0 || colors.isEmpty()) return emptyList()
    val random = Random.Default
    return List(count) { index ->
        val angle = (2.0 * PI * index / count) + random.nextDouble(-0.22, 0.22)
        val speed = random.nextFloat() * 260f + 150f
        Parcacik(
            x = centerX,
            y = centerY,
            vx = (cos(angle) * speed * spread).toFloat(),
            vy = (sin(angle) * speed * spread - 170f).toFloat(),
            size = random.nextFloat() * 6f + 3f,
            color = colors[random.nextInt(colors.size)],
            rotation = random.nextFloat() * 360f,
            rotationSpeed = random.nextFloat() * 500f - 250f,
            life = random.nextFloat() * 0.7f + 0.8f,
            shape = ParcacikSekli.entries.random(random),
        )
    }
}

fun updateParticle(particle: Parcacik, deltaSeconds: Float, gravity: Float = 420f): Parcacik {
    val dt = deltaSeconds.coerceIn(0f, 0.05f)
    return particle.copy(
        x = particle.x + particle.vx * dt,
        y = particle.y + particle.vy * dt,
        vy = particle.vy + gravity * dt,
        rotation = particle.rotation + particle.rotationSpeed * dt,
        life = particle.life - dt,
    )
}

fun isParticleAlive(particle: Parcacik): Boolean = particle.life > 0f

/** Smooth scale modifier for press/selection affordances. */
@Composable
fun Modifier.premiumScale(scale: Float, label: String = "premium-scale"): Modifier {
    val animated = animateFloatPremium(scale, label = label)
    return graphicsLayer { scaleX = animated.value; scaleY = animated.value }
}

/** Small reusable particles state driver; motion reduction leaves a stable empty state. */
@Composable
fun rememberCelebrationParticles(
    trigger: Boolean,
    colors: List<Color>,
    particleCount: Int = 36,
): State<List<Parcacik>> {
    val hareket = LocalHareketPolitikasi.current
    val particles = remember { mutableStateOf(emptyList<Parcacik>()) }
    LaunchedEffect(trigger, hareket.hareketAcik, colors, particleCount) {
        if (!trigger || !hareket.hareketAcik) {
            particles.value = emptyList()
        } else {
            particles.value = createCelebrationParticles(0f, 0f, particleCount, colors)
        }
    }
    return particles
}
