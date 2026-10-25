package com.keremtulu.questionnote.ui.ayarlar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.data.prefs.TekrarPolitikasiDeposu
import com.keremtulu.questionnote.domain.repeat.TekrarMotorModu
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import com.keremtulu.questionnote.ui.premium.PremiumOutlinedButton
import com.keremtulu.questionnote.ui.premium.PremiumCard

@Composable
fun MotorAyarlariKarti(depo: TekrarPolitikasiDeposu, taniHizmeti: com.keremtulu.questionnote.data.gelisim.MotorTaniHizmeti? = null) {
    val ayar by depo.izle.collectAsState()
    val scope = rememberCoroutineScope()
    var mesgul by remember { mutableStateOf(false) }
    var hata by remember { mutableStateOf(false) }
    var taniYenile by remember { mutableIntStateOf(0) }
    var taniHatasi by remember { mutableStateOf(false) }
    val tani by produceState<com.keremtulu.questionnote.data.gelisim.MotorTanisi?>(null, taniHizmeti, taniYenile, ayar) {
        value = null; taniHatasi = false
        if (taniHizmeti != null) {
            try {
                value = taniHizmeti.oku()
            } catch (iptal: CancellationException) {
                throw iptal
            } catch (_: Exception) {
                taniHatasi = true
            }
        }
    }
    PremiumCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.motor_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.motor_shadow_note), style = MaterialTheme.typography.bodySmall)
            TekrarMotorModu.entries.forEach { mod ->
                val acik = mod != TekrarMotorModu.ETKIN || ayar.kapi.etkinAcik
                val secili = ayar.uygulanan == mod
                val renkler = MaterialTheme.colorScheme
                PremiumOutlinedButton(
                    onClick = {
                        if (!mesgul) {
                            mesgul = true
                            scope.launch {
                                try {
                                    hata = !depo.ayarla(mod)
                                } catch (iptal: CancellationException) {
                                    throw iptal
                                } catch (_: Exception) {
                                    hata = true
                                } finally {
                                    mesgul = false
                                }
                            }
                        }
                    },
                    enabled = acik && !mesgul,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (secili) renkler.primaryContainer.copy(alpha = 0.55f) else Color.Transparent,
                        contentColor = if (secili) renkler.primary else renkler.onSurface,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = renkler.onSurfaceVariant.copy(alpha = 0.45f),
                    ),
                    border = BorderStroke(
                        width = if (secili) 1.6.dp else 1.dp,
                        color = when {
                            secili -> renkler.primary
                            !acik -> renkler.outlineVariant.copy(alpha = 0.42f)
                            else -> renkler.outlineVariant.copy(alpha = 0.85f)
                        },
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .semantics {
                            selected = secili
                            role = Role.RadioButton
                        },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(
                                when (mod) {
                                    TekrarMotorModu.KAPALI -> R.string.motor_off
                                    TekrarMotorModu.GOLGE -> R.string.motor_shadow
                                    TekrarMotorModu.ETKIN -> R.string.motor_active
                                },
                            ),
                            fontWeight = if (secili) FontWeight.Bold else FontWeight.Medium,
                        )
                        val halkaRengi = when {
                            secili -> renkler.primary
                            !acik -> renkler.outlineVariant.copy(alpha = 0.42f)
                            else -> renkler.outline
                        }
                        Canvas(Modifier.size(18.dp).clearAndSetSemantics {}) {
                            drawCircle(
                                color = halkaRengi,
                                radius = size.minDimension * 0.44f,
                                style = Stroke(width = 1.8.dp.toPx()),
                            )
                            if (secili) {
                                drawCircle(
                                    color = halkaRengi,
                                    radius = size.minDimension * 0.24f,
                                )
                            }
                        }
                    }
                }
            }
            if (!ayar.kapi.etkinAcik) Text(stringResource(R.string.motor_gate_note), style = MaterialTheme.typography.bodySmall)
            if (hata) Text(stringResource(R.string.motor_save_error), color = MaterialTheme.colorScheme.error)
            tani?.let { t ->
                Text(stringResource(R.string.motor_diagnostics, t.incelenen, t.toplamKarar, t.v2, t.golge, t.farkliAday, t.etkin, t.gecersiz),
                    style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.motor_diagnostics_note), style = MaterialTheme.typography.bodySmall)
            }
            if (taniHatasi) Text(stringResource(R.string.motor_diagnostics_error), color = MaterialTheme.colorScheme.error)
            if (taniHizmeti != null) PremiumOutlinedButton(onClick = { taniYenile++ }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(stringResource(R.string.motor_refresh_diagnostics))
            }
        }
    }
}
