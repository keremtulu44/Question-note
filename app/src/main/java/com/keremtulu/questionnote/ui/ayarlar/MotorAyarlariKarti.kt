package com.keremtulu.questionnote.ui.ayarlar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

@Composable
fun MotorAyarlariKarti(depo: TekrarPolitikasiDeposu) {
    val ayar by depo.izle.collectAsState()
    val scope = rememberCoroutineScope()
    var mesgul by remember { mutableStateOf(false) }
    var hata by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.motor_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.motor_shadow_note), style = MaterialTheme.typography.bodySmall)
            TekrarMotorModu.entries.forEach { mod ->
                val acik = mod != TekrarMotorModu.ETKIN || ayar.kapi.etkinAcik
                OutlinedButton(onClick = {
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
        }
    }
}
