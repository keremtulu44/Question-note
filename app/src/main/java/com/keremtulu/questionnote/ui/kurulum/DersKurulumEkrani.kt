package com.keremtulu.questionnote.ui.kurulum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.keremtulu.questionnote.R
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.premium.PremiumButton
import com.keremtulu.questionnote.ui.premium.PremiumMarkaRenkler
import kotlinx.coroutines.launch

@Composable
fun DersKurulumEkrani(
    dersRepository: DersRepository,
    onTamam: () -> Unit,
) {
    var dersler by remember { mutableStateOf<List<Ders>>(emptyList()) }
    var secili by remember { mutableStateOf(setOf<String>()) }
    var hata by remember { mutableStateOf(false) }
    var kaydediliyor by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        dersler = runCatching { dersRepository.kataloguHazirla() }.getOrDefault(emptyList())
        if (secili.isEmpty()) secili = dersler.map { it.id }.toSet()
    }
    Scaffold { insets ->
        Column(Modifier.fillMaxSize().padding(insets).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GradyanKart(modifier = Modifier.fillMaxWidth(), borderAnimation = false) {
                Column {
                    Text(
                        stringResource(R.string.onboarding_kicker),
                        color = PremiumMarkaRenkler.BakirIsik,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        stringResource(R.string.onboarding_title),
                        modifier = Modifier.padding(top = 4.dp),
                        color = PremiumMarkaRenkler.Kagit,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        stringResource(R.string.onboarding_subtitle),
                        modifier = Modifier.padding(top = 4.dp),
                        color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(dersler, key = { it.id }) { ders ->
                    FilterChip(
                        selected = ders.id in secili,
                        onClick = {
                            secili = if (ders.id in secili) secili - ders.id else secili + ders.id
                        },
                        label = { Text(ders.ad) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (hata) {
                Text(stringResource(R.string.onboarding_error), color = MaterialTheme.colorScheme.error)
            }
            PremiumButton(
                enabled = !kaydediliyor && secili.isNotEmpty(),
                onClick = {
                    kaydediliyor = true
                    hata = false
                    scope.launch {
                        try {
                            dersRepository.dersSeciminiKaydet(secili)
                            onTamam()
                        } catch (_: Exception) {
                            hata = true
                            kaydediliyor = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(stringResource(R.string.onboarding_continue))
            }
        }
    }
}
