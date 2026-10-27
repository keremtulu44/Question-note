package com.keremtulu.questionnote.ui.ogretmen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.images.LocalSoruGorselDeposu
import com.keremtulu.questionnote.domain.model.OgretmenDurum
import com.keremtulu.questionnote.domain.model.OgretmenSorusu
import com.keremtulu.questionnote.ui.bilesenler.SoruGorselResmi
import com.keremtulu.questionnote.ui.premium.PremiumButton
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumTextButton

/**
 * In-class presentation surface: full screen, screen kept awake, answer hidden until tapped,
 * next/previous across the list the teacher came from and an optional solution overlay.
 *
 * Keeping it a Dialog (not a route) means Back and the close button never lose the list position.
 */
@Composable
internal fun OgretmenSunumDialog(
    sorular: List<OgretmenSorusu>,
    baslangicIndex: Int,
    grupAdlari: Map<String, String>,
    dersAdlari: Map<String, String> = emptyMap(),
    konuAdlari: Map<String, String> = emptyMap(),
    gorselDeposu: LocalSoruGorselDeposu,
    onKapat: () -> Unit,
    onCozulduDegistir: (OgretmenSorusu, Boolean) -> Unit,
    onBaskaGrubaEkle: (OgretmenSorusu) -> Unit,
) {
    if (sorular.isEmpty()) return
    var index by remember(sorular) {
        mutableIntStateOf(baslangicIndex.coerceIn(0, sorular.lastIndex))
    }
    val soru = sorular[index.coerceIn(0, sorular.lastIndex)]
    val view = LocalView.current

    // A presentation lasts minutes; the screen must not sleep in the middle of a solution.
    DisposableEffect(view) {
        val onceki = view.keepScreenOn
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = onceki }
    }
    BackHandler(onBack = onKapat)

    Dialog(
        onDismissRequest = onKapat,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        OgretmenSunumIcerigi(
            sorular = sorular,
            index = index,
            soru = soru,
            grupAdlari = grupAdlari,
            dersAdlari = dersAdlari,
            konuAdlari = konuAdlari,
            gorselDeposu = gorselDeposu,
            onKapat = onKapat,
            onOnceki = { if (index > 0) index-- },
            onSonraki = { if (index < sorular.lastIndex) index++ },
            onCozulduDegistir = onCozulduDegistir,
            onBaskaGrubaEkle = onBaskaGrubaEkle,
        )
    }
}

@Composable
private fun OgretmenSunumIcerigi(
    sorular: List<OgretmenSorusu>,
    index: Int,
    soru: OgretmenSorusu,
    grupAdlari: Map<String, String>,
    dersAdlari: Map<String, String>,
    konuAdlari: Map<String, String>,
    gorselDeposu: LocalSoruGorselDeposu,
    onKapat: () -> Unit,
    onOnceki: () -> Unit,
    onSonraki: () -> Unit,
    onCozulduDegistir: (OgretmenSorusu, Boolean) -> Unit,
    onBaskaGrubaEkle: (OgretmenSorusu) -> Unit,
) {
    val gorseller = remember(soru.gorseller) { soru.soruGorselleri() }
    val cozumler = remember(soru.gorseller) { soru.cozumGorselleri() }
    var sayfa by remember(soru.id) { mutableIntStateOf(0) }
    var cevapAcik by remember(soru.id) { mutableStateOf(false) }
    var cozumAcik by remember(soru.id) { mutableStateOf(false) }
    var cozumSayfa by remember(soru.id) { mutableIntStateOf(0) }
    val aktifGorsel = gorseller.getOrNull(sayfa.coerceIn(0, (gorseller.size - 1).coerceAtLeast(0)))
    val aktifCozum = cozumler.getOrNull(cozumSayfa.coerceIn(0, (cozumler.size - 1).coerceAtLeast(0)))
    var scale by remember(aktifGorsel?.id) { mutableFloatStateOf(1f) }
    var offsetX by remember(aktifGorsel?.id) { mutableFloatStateOf(0f) }
    var offsetY by remember(aktifGorsel?.id) { mutableFloatStateOf(0f) }
    val transformState = rememberTransformableState { zoom, pan, _ ->
        scale = (scale * zoom).coerceIn(1f, 5f)
        if (scale <= 1.02f) {
            offsetX = 0f
            offsetY = 0f
        } else {
            offsetX += pan.x
            offsetY += pan.y
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PremiumMarkaRenkler.MurekkepKoyu)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = listOfNotNull(grupAdlari[soru.grupId], soru.baslik)
                        .joinToString(" · ")
                        .ifBlank { stringResource(R.string.teacher_untitled_question) },
                    color = PremiumMarkaRenkler.Kagit,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val soranSatiri = listOfNotNull(
                    soru.kimSordu?.takeIf { it.isNotBlank() }
                        ?.let { stringResource(R.string.teacher_asked_by_chip, it) },
                    soru.dersId?.let { dersAdlari[it] }?.takeIf { it.isNotBlank() },
                    soru.konuId?.let { konuAdlari[it] }?.takeIf { it.isNotBlank() },
                ).joinToString(" · ")
                if (soranSatiri.isNotBlank()) {
                    Text(
                        text = soranSatiri,
                        color = PremiumMarkaRenkler.BakirIsik,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = stringResource(R.string.teacher_presentation_progress, index + 1, sorular.size) +
                        " · " + stringResource(R.string.teacher_presentation_keep_awake),
                    color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.66f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            if (scale > 1.05f) {
                PremiumTextButton(
                    onClick = {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    },
                ) {
                    Text(
                        stringResource(R.string.teacher_zoom_reset),
                        color = PremiumMarkaRenkler.BakirIsik,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            PremiumTextButton(onClick = onKapat) {
                Text(stringResource(R.string.common_close), color = PremiumMarkaRenkler.Kagit)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
                .pointerInput(aktifGorsel?.id) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (scale > 1.05f) {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            } else {
                                scale = 2.4f
                            }
                        },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            if (cozumAcik && aktifCozum != null) {
                SoruGorselResmi(
                    file = gorselDeposu.dosyaGoruntuIcin(aktifCozum.dosyaAdi),
                    contentDescription = stringResource(R.string.teacher_solution_section),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    maxDecodeEdge = 2_400,
                    cerceve = false,
                )
            } else {
                SoruGorselResmi(
                    file = aktifGorsel?.let { gorselDeposu.dosyaGoruntuIcin(it.dosyaAdi) },
                    contentDescription = stringResource(R.string.question_image_description),
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                        }
                        .transformable(transformState),
                    contentScale = ContentScale.Fit,
                    maxDecodeEdge = 2_400,
                    cerceve = false,
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = PremiumMarkaRenkler.Murekkep.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, PremiumMarkaRenkler.Kagit.copy(alpha = 0.14f)),
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (gorseller.size > 1 || cozumler.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (gorseller.size > 1) {
                            PremiumTextButton(onClick = { if (sayfa > 0) sayfa-- }, enabled = sayfa > 0) {
                                Text(
                                    stringResource(R.string.ask_teacher_prev_page),
                                    color = if (sayfa > 0) PremiumMarkaRenkler.Kagit
                                    else PremiumMarkaRenkler.Kagit.copy(alpha = 0.36f),
                                )
                            }
                            Text(
                                text = stringResource(
                                    R.string.ask_teacher_page_indicator,
                                    sayfa + 1,
                                    gorseller.size,
                                ),
                                color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelMedium,
                            )
                            PremiumTextButton(
                                onClick = { if (sayfa < gorseller.lastIndex) sayfa++ },
                                enabled = sayfa < gorseller.lastIndex,
                            ) {
                                Text(
                                    stringResource(R.string.ask_teacher_next_page),
                                    color = if (sayfa < gorseller.lastIndex) PremiumMarkaRenkler.Kagit
                                    else PremiumMarkaRenkler.Kagit.copy(alpha = 0.36f),
                                )
                            }
                        }
                        Box(Modifier.weight(1f))
                        if (cozumler.isNotEmpty()) {
                            PremiumTextButton(
                                onClick = { cozumAcik = !cozumAcik },
                                modifier = Modifier.testTag("ogretmen_sunum_cozum"),
                            ) {
                                Text(
                                    text = stringResource(
                                        if (cozumAcik) R.string.teacher_solution_hide
                                        else R.string.teacher_solution_show,
                                    ) + " (${cozumler.size})",
                                    color = PremiumMarkaRenkler.YesilAcik,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            if (cozumAcik && cozumler.size > 1) {
                                PremiumTextButton(onClick = { if (cozumSayfa > 0) cozumSayfa-- }) {
                                    Text("‹", color = PremiumMarkaRenkler.Kagit)
                                }
                                PremiumTextButton(onClick = { if (cozumSayfa < cozumler.lastIndex) cozumSayfa++ }) {
                                    Text("›", color = PremiumMarkaRenkler.Kagit)
                                }
                            }
                        }
                    }
                }
                if (!soru.cevap.isNullOrBlank()) {
                    if (cevapAcik) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().testTag("ogretmen_cevap_kutusu"),
                            shape = RoundedCornerShape(14.dp),
                            color = PremiumMarkaRenkler.Bakir.copy(alpha = 0.22f),
                            border = BorderStroke(1.dp, PremiumMarkaRenkler.BakirIsik.copy(alpha = 0.45f)),
                        ) {
                            Column(
                                // A long answer scrolls instead of pushing the actions off-screen.
                                modifier = Modifier
                                    .heightIn(max = 200.dp)
                                    .verticalScroll(rememberScrollState())
                                    .padding(14.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.teacher_answer_prefix, soru.cevap),
                                    color = PremiumMarkaRenkler.Kagit,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                    PremiumOutlinedButton(
                        onClick = { cevapAcik = !cevapAcik },
                        modifier = Modifier.fillMaxWidth().testTag("ogretmen_cevabi_goster"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PremiumMarkaRenkler.Kagit),
                        border = BorderStroke(1.dp, PremiumMarkaRenkler.Kagit.copy(alpha = 0.32f)),
                    ) {
                        Text(
                            stringResource(
                                if (cevapAcik) R.string.teacher_hide_answer else R.string.teacher_show_answer,
                            ),
                            color = PremiumMarkaRenkler.Kagit,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PremiumTextButton(
                        onClick = onOnceki,
                        enabled = index > 0,
                        modifier = Modifier.testTag("ogretmen_sunum_onceki"),
                    ) {
                        Text(
                            stringResource(R.string.teacher_presentation_prev),
                            color = if (index > 0) PremiumMarkaRenkler.Kagit
                            else PremiumMarkaRenkler.Kagit.copy(alpha = 0.36f),
                        )
                    }
                    PremiumTextButton(
                        onClick = onSonraki,
                        enabled = index < sorular.lastIndex,
                        modifier = Modifier.testTag("ogretmen_sunum_sonraki"),
                    ) {
                        Text(
                            stringResource(R.string.teacher_presentation_next),
                            color = if (index < sorular.lastIndex) PremiumMarkaRenkler.Kagit
                            else PremiumMarkaRenkler.Kagit.copy(alpha = 0.36f),
                        )
                    }
                    Box(Modifier.weight(1f))
                    PremiumOutlinedButton(
                        onClick = { onBaskaGrubaEkle(soru) },
                        modifier = Modifier.heightIn(min = 46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PremiumMarkaRenkler.Kagit),
                        border = BorderStroke(1.dp, PremiumMarkaRenkler.Kagit.copy(alpha = 0.32f)),
                    ) {
                        Text(stringResource(R.string.teacher_copy_to_group), color = PremiumMarkaRenkler.Kagit)
                    }
                }
                // One tap marks it solved and moves on: the teacher is in front of the student.
                PremiumButton(
                    onClick = {
                        onCozulduDegistir(soru, soru.durum == OgretmenDurum.BEKLIYOR)
                        if (soru.durum == OgretmenDurum.BEKLIYOR) onSonraki()
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp).testTag("ogretmen_sunum_coz"),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = stringResource(
                            if (soru.durum == OgretmenDurum.BEKLIYOR) R.string.teacher_presentation_solve_next
                            else R.string.teacher_mark_unsolved,
                        ),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
