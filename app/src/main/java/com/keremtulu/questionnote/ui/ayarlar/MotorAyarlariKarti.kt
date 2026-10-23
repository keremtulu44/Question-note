package com.keremtulu.questionnote.ui.ayarlar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
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
                PremiumOutlinedButton(onClick = {
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
                }, enabled = acik && !mesgul, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .semantics { selected = ayar.uygulanan == mod; role = Role.RadioButton }) {
                    Text(stringResource(when (mod) {
                        TekrarMotorModu.KAPALI -> R.string.motor_off
                        TekrarMotorModu.GOLGE -> R.string.motor_shadow
                        TekrarMotorModu.ETKIN -> R.string.motor_active
                    }))
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
