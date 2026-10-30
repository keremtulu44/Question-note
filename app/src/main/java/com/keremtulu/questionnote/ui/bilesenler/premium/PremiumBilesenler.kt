package com.keremtulu.questionnote.ui.bilesenler.premium

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.ui.premium.PremiumSembolAilesi
import com.keremtulu.questionnote.ui.animasyon.Parcacik
import com.keremtulu.questionnote.ui.animasyon.ParcacikSekli
import com.keremtulu.questionnote.ui.animasyon.animateFloatPremium
import com.keremtulu.questionnote.ui.animasyon.createCelebrationParticles
import com.keremtulu.questionnote.ui.animasyon.isParticleAlive
import com.keremtulu.questionnote.ui.animasyon.rememberPulse
import com.keremtulu.questionnote.ui.animasyon.rememberShimmer
import com.keremtulu.questionnote.ui.animasyon.springPremium
import com.keremtulu.questionnote.ui.animasyon.updateParticle
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.tema.LocalHareketPolitikasi
import androidx.compose.runtime.withFrameNanos
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Rounded translucent surface with a fine highlight, subtle edge, and press feedback. */
@Composable
fun CamKarti(
    modifier: Modifier = Modifier,
    elevation: CamYukseklik = CamYukseklik.ORTA,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale = animateFloatPremium(
        target = if (pressed && onClick != null) 0.975f else 1f,
        animationSpec = springPremium(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label = "glass-card-press",
    )
    val clickModifier = if (onClick == null) Modifier else Modifier.clickable(
        indication = null,
        interactionSource = interactionSource,
        role = Role.Button,
        onClick = { onClick?.invoke() },
    )
    val shadow = when (elevation) {
        CamYukseklik.DUSUK -> 1.dp
        CamYukseklik.ORTA -> 7.dp
        CamYukseklik.YUKSEK -> 14.dp
        CamYukseklik.COK_YUKSEK -> 22.dp
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
            .then(clickModifier),
        shape = shape,
        color = colors.surface.copy(alpha = 0.96f),
        contentColor = contentColor,
        tonalElevation = 0.dp,
        shadowElevation = shadow,
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.16f)),
    ) {
        Box(Modifier.drawBehind {
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.22f), Color.Transparent, Color.Black.copy(alpha = 0.03f)),
                ),
            )
        }.padding(1.dp)) {
            content()
        }
    }
}

enum class CamYukseklik { DUSUK, ORTA, YUKSEK, COK_YUKSEK }

/** Glass list row with a clear title/subtitle hierarchy and optional leading/trailing content. */
@Composable
fun PremiumListItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    CamKarti(
        modifier = modifier,
        elevation = CamYukseklik.DUSUK,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            leading?.invoke()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (!subtitle.isNullOrBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            trailing?.invoke()
        }
    }
}

/** Premium gradient surface with a subtle animated gradient edge. */
@Composable
fun GradyanKart(
    modifier: Modifier = Modifier,
    gradyan: Brush = Brush.horizontalGradient(listOf(PremiumMarkaRenkler.Murekkep, PremiumMarkaRenkler.MurekkepKoyu)),
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    borderAnimation: Boolean = true,
    content: @Composable () -> Unit,
) {
    val pulse = if (borderAnimation) {
        rememberPulse(minAlpha = 0.55f, maxAlpha = 1f, durationMs = 2_200)
    } else {
        remember { mutableStateOf(0.75f) }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(gradyan)
            .drawBehind {
                // Yumuşak ışık: düz daire kenarı keskin bir "leke" gibi görünüyordu; artık
                // merkeze doğru sönümlenen iki radyal parlama var, kartın köşeleri temiz kalır.
                val ustSag = Offset(size.width * 0.92f, size.height * 0.08f)
                val ustSagYaricap = size.maxDimension * 0.42f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PremiumMarkaRenkler.BakirIsik.copy(alpha = 0.20f),
                            Color.Transparent,
                        ),
                        center = ustSag,
                        radius = ustSagYaricap,
                    ),
                    radius = ustSagYaricap,
                    center = ustSag,
                )
                val altSol = Offset(size.width * 0.08f, size.height * 0.95f)
                val altSolYaricap = size.maxDimension * 0.34f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PremiumMarkaRenkler.DenizAcik.copy(alpha = 0.12f),
                            Color.Transparent,
                        ),
                        center = altSol,
                        radius = altSolYaricap,
                    ),
                    radius = altSolYaricap,
                    center = altSol,
                )
            }
            .border(
                BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(
                        listOf(
                            PremiumMarkaRenkler.BakirIsik.copy(alpha = if (borderAnimation) pulse.value else 0.82f),
                            Color.White.copy(alpha = 0.38f),
                            PremiumMarkaRenkler.DenizAcik.copy(alpha = if (borderAnimation) pulse.value else 0.82f),
                        ),
                    ),
                ),
                shape,
            )
            .padding(22.dp),
    ) { content() }
}

// -----------------------------------------------------------------------------
// Shimmer placeholders
// -----------------------------------------------------------------------------

@Composable
fun IskeletMetin(
    modifier: Modifier = Modifier,
    lines: Int = 1,
    lineHeight: Int = 24,
    widthFraction: Float = 1f,
    animation: State<Float> = rememberShimmer(),
) {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val alpha = shimmerAlpha(animation.value)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(lines.coerceAtLeast(1)) { index ->
            val width = if (index == lines - 1) widthFraction.coerceIn(0.15f, 1f) else 1f
            Box(
                Modifier.fillMaxWidth(width)
                    .height(lineHeight.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(base.copy(alpha = alpha)),
            )
        }
    }
}

@Composable
fun IskeletKart(
    modifier: Modifier = Modifier,
    height: Int = 120,
    showImage: Boolean = true,
    showTitle: Boolean = true,
    showSubtitle: Boolean = true,
    animation: State<Float> = rememberShimmer(),
) {
    val surface = MaterialTheme.colorScheme.surface
    val variant = MaterialTheme.colorScheme.surfaceVariant
    val alpha = shimmerAlpha(animation.value)
    Card(
        modifier = modifier.fillMaxWidth().height(height.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (showImage) Box(
                Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(variant.copy(alpha = alpha)),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (showTitle) Box(
                    Modifier.fillMaxWidth(0.72f).height(18.dp).clip(RoundedCornerShape(5.dp))
                        .background(variant.copy(alpha = alpha)),
                )
                if (showSubtitle) Box(
                    Modifier.fillMaxWidth(0.52f).height(14.dp).clip(RoundedCornerShape(5.dp))
                        .background(variant.copy(alpha = alpha * 0.85f)),
                )
            }
        }
    }
}

@Composable
fun IskeletListeOgesi(modifier: Modifier = Modifier, animation: State<Float> = rememberShimmer()) {
    IskeletKart(modifier.padding(vertical = 5.dp), height = 82, animation = animation)
}

@Composable
fun IskeletSayfa(itemCount: Int = 5, animation: State<Float> = rememberShimmer()) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IskeletMetin(lines = 1, lineHeight = 28, widthFraction = 0.46f, animation = animation)
        Spacer(Modifier.height(8.dp))
        repeat(itemCount.coerceAtLeast(0)) { IskeletListeOgesi(animation = animation) }
    }
}

private fun shimmerAlpha(position: Float): Float =
    (0.46f + 0.34f * (0.5f + 0.5f * cos(position * 2f * PI.toFloat()))).coerceIn(0.36f, 0.90f)

// -----------------------------------------------------------------------------
// Premium progress indicators
// -----------------------------------------------------------------------------

@Composable
fun PremiumDaireselIlerleme(
    progress: Float,
    modifier: Modifier = Modifier.size(64.dp),
    strokeWidth: Int = 6,
    gradyan: Brush = Brush.sweepGradient(
        listOf(Color(0xFFEFC18A), Color(0xFF7BDDC3), Color(0xFFAAC8FF), Color(0xFFEFC18A)),
    ),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    showGlow: Boolean = true,
    animasyonAcik: Boolean = true,
) {
    val hareket = LocalHareketPolitikasi.current
    val progressState = if (animasyonAcik && hareket.hareketAcik) {
        animateFloatPremium(progress.coerceIn(0f, 1f), springPremium(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
    } else null
    val normalized = (progressState?.value ?: progress).coerceIn(0f, 1f)
    Canvas(modifier) {
        val stroke = strokeWidth.dp.toPx().coerceAtLeast(1f)
        val inset = stroke / 2f
        val arcSize = Size((size.width - stroke).coerceAtLeast(0f), (size.height - stroke).coerceAtLeast(0f))
        val topLeft = Offset(inset, inset)
        drawArc(
            color = backgroundColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        if (showGlow && normalized > 0f) {
            drawArc(
                brush = gradyan,
                startAngle = -90f,
                sweepAngle = 360f * normalized,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                alpha = 0.20f,
                style = Stroke(width = stroke * 2.2f, cap = StrokeCap.Round),
            )
        }
        drawArc(
            brush = gradyan,
            startAngle = -90f,
            sweepAngle = 360f * normalized,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
    }
}

@Composable
fun PremiumCizgiselIlerleme(
    progress: Float,
    modifier: Modifier = Modifier.fillMaxWidth().height(8.dp),
    height: Int = 8,
    gradyan: Brush = Brush.horizontalGradient(listOf(Color(0xFFEFC18A), Color(0xFF7BDDC3))),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    cornerRadius: Int = 4,
    animasyonAcik: Boolean = true,
) {
    val hareket = LocalHareketPolitikasi.current
    val animation = if (animasyonAcik && hareket.hareketAcik) animateFloatPremium(progress.coerceIn(0f, 1f)) else null
    val normalized = (animation?.value ?: progress).coerceIn(0f, 1f)
    BoxWithConstraints(
        modifier = modifier.height(height.dp).clip(RoundedCornerShape(cornerRadius.dp)).background(backgroundColor),
    ) {
        Box(
            Modifier.fillMaxWidth(normalized).fillMaxHeight()
                .background(gradyan, RoundedCornerShape(cornerRadius.dp)),
        )
    }
}

@Composable
fun AdimIlerlemeGostergesi(
    steps: List<String>,
    currentStep: Int,
    modifier: Modifier = Modifier.fillMaxWidth(),
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.outlineVariant,
    connectorColor: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    if (steps.isEmpty()) return
    val activeIndex = currentStep.coerceIn(0, steps.lastIndex)
    Column(modifier) {
        Box(Modifier.fillMaxWidth().height(34.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                if (steps.size > 1) {
                    val y = size.height / 2f
                    val segment = size.width / steps.size
                    for (index in 0 until steps.lastIndex) {
                        drawLine(
                            color = if (index < activeIndex) activeColor else connectorColor.copy(alpha = 0.7f),
                            start = Offset(segment * index + segment / 2f + 14.dp.toPx(), y),
                            end = Offset(segment * (index + 1) + segment / 2f - 14.dp.toPx(), y),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                }
            }
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                steps.forEachIndexed { index, _ ->
                    val done = index < activeIndex
                    val active = index <= activeIndex
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(28.dp),
                            shape = CircleShape,
                            color = if (active) activeColor else inactiveColor.copy(alpha = 0.24f),
                            border = if (active) null else BorderStroke(1.dp, inactiveColor.copy(alpha = 0.55f)),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (done) "✓" else "${index + 1}",
                                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = PremiumSembolAilesi,
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
            steps.forEachIndexed { index, label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                    color = if (index <= activeIndex) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (index == activeIndex) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Empty states and their small vector illustrations
// -----------------------------------------------------------------------------

@Composable
fun PremiumBosDurum(
    illustration: @Composable () -> Unit = { VarsayilanIlustrasyon() },
    baslik: String,
    aciklama: String,
    butonMetni: String? = null,
    onButonTikla: (() -> Unit)? = null,
    modifier: Modifier = Modifier.fillMaxSize(),
    ikincilButonMetni: String? = null,
    onIkincilButon: (() -> Unit)? = null,
) {
    val hareket = LocalHareketPolitikasi.current
    var shown by remember { mutableStateOf(!hareket.hareketAcik) }
    LaunchedEffect(Unit) { shown = true }
    val enter = animateFloatPremium(
        target = if (shown) 1f else 0f,
        animationSpec = springPremium(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "empty-state-enter",
    )
    Column(
        // Empty/error content must be readable immediately, including when reached by a fling.
        modifier = modifier.padding(24.dp).graphicsLayer {
            scaleX = 0.98f + enter.value * 0.02f
            scaleY = 0.98f + enter.value * 0.02f
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
    ) {
        Box(
            Modifier.size(112.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { illustration() }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(baslik, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(aciklama, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        if (butonMetni != null && onButonTikla != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onButonTikla, modifier = Modifier.fillMaxWidth().widthIn(max = 300.dp).heightIn(min = 50.dp), shape = RoundedCornerShape(16.dp)) {
                    Text(butonMetni, fontWeight = FontWeight.SemiBold)
                }
                if (ikincilButonMetni != null && onIkincilButon != null) {
                    OutlinedButton(onClick = onIkincilButon, modifier = Modifier.fillMaxWidth().widthIn(max = 300.dp).heightIn(min = 48.dp), shape = RoundedCornerShape(16.dp)) {
                        Text(ikincilButonMetni)
                    }
                }
            }
        }
    }
}

@Composable
fun VarsayilanIlustrasyon() {
    val color = MaterialTheme.colorScheme.primary
    Canvas(Modifier.size(76.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val book = Path().apply {
            moveTo(cx - 19.dp.toPx(), cy - 24.dp.toPx())
            lineTo(cx - 2.dp.toPx(), cy - 20.dp.toPx())
            lineTo(cx + 19.dp.toPx(), cy - 24.dp.toPx())
            lineTo(cx + 19.dp.toPx(), cy + 23.dp.toPx())
            quadraticTo(cx, cy + 18.dp.toPx(), cx, cy + 25.dp.toPx())
            quadraticTo(cx - 2.dp.toPx(), cy + 18.dp.toPx(), cx - 19.dp.toPx(), cy + 23.dp.toPx())
            close()
        }
        drawPath(book, color.copy(alpha = 0.14f))
        drawPath(book, color, style = Stroke(width = 2.dp.toPx()))
        drawLine(color.copy(alpha = 0.7f), Offset(cx, cy - 19.dp.toPx()), Offset(cx, cy + 23.dp.toPx()), 1.5.dp.toPx())
        repeat(3) { index ->
            val y = cy - 11.dp.toPx() + index * 8.dp.toPx()
            drawLine(color.copy(alpha = 0.5f), Offset(cx - 13.dp.toPx(), y), Offset(cx - 4.dp.toPx(), y), 1.2.dp.toPx(), StrokeCap.Round)
            drawLine(color.copy(alpha = 0.5f), Offset(cx + 4.dp.toPx(), y), Offset(cx + 13.dp.toPx(), y), 1.2.dp.toPx(), StrokeCap.Round)
        }
    }
}

@Composable
fun BosDurumSoruYok(onSoruEkle: () -> Unit) {
    PremiumBosDurum(
        baslik = stringResource(R.string.today_empty_title),
        aciklama = stringResource(R.string.today_empty_description),
        butonMetni = stringResource(R.string.progress_add_question),
        onButonTikla = onSoruEkle,
        illustration = { VarsayilanIlustrasyon() },
    )
}

@Composable
fun BosDurumTekrarYok(onGit: () -> Unit) {
    PremiumBosDurum(
        baslik = "Bugün tekrar yok",
        aciklama = "Bugün için planlı tekrarın bulunmuyor. İstersen bir soru ekleyip öğrenme yolculuğuna devam edebilirsin.",
        butonMetni = "Soru ekle",
        onButonTikla = onGit,
        illustration = { VarsayilanIlustrasyon() },
    )
}

// -----------------------------------------------------------------------------
// Particle burst overlay
// -----------------------------------------------------------------------------

@Composable
fun KutlamaParcaciklari(
    visible: Boolean,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(PremiumMarkaRenkler.BakirIsik, PremiumMarkaRenkler.DenizAcik, PremiumMarkaRenkler.Mercan),
    particleCount: Int = 42,
) {
    val hareket = LocalHareketPolitikasi.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var particles by remember { mutableStateOf(emptyList<Parcacik>()) }
    LaunchedEffect(visible, canvasSize, hareket.hareketAcik, particleCount, colors) {
        if (!visible || !hareket.hareketAcik || canvasSize == IntSize.Zero) {
            particles = emptyList()
            return@LaunchedEffect
        }
        particles = createCelebrationParticles(
            centerX = canvasSize.width / 2f,
            centerY = canvasSize.height * 0.42f,
            count = particleCount,
            colors = colors,
            spread = (canvasSize.width.coerceAtLeast(1) / 360f).coerceIn(0.65f, 1.8f),
        )
        var previousFrame = withFrameNanos { it }
        while (particles.any(::isParticleAlive)) {
            val frame = withFrameNanos { it }
            val delta = ((frame - previousFrame) / 1_000_000_000f).coerceIn(0f, 0.05f)
            previousFrame = frame
            particles = particles.map { updateParticle(it, delta) }.filter(::isParticleAlive)
        }
    }
    Canvas(modifier.onSizeChanged { canvasSize = it }) {
        particles.forEach { particle ->
            val radius = particle.size.dp.toPx()
            val point = Offset(particle.x, particle.y)
            val color = particle.color.copy(alpha = particle.life.coerceIn(0f, 1f))
            when (particle.shape) {
                ParcacikSekli.DAIRE -> drawCircle(color, radius, point)
                else -> rotate(particle.rotation, pivot = point) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(point.x - radius, point.y - radius * 0.55f),
                        size = Size(radius * 2f, radius * 1.1f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius * 0.25f),
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Tooltip
// -----------------------------------------------------------------------------

@Composable
fun PremiumTooltip(
    visible: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
) {
    if (!visible) return
    Popup(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier.padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.inverseSurface,
            shadowElevation = 8.dp,
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                color = MaterialTheme.colorScheme.inverseOnSurface,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
