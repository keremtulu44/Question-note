package com.keremtulu.questionnote.ui.premium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.keremtulu.questionnote.data.ses.HapticTipi
import com.keremtulu.questionnote.data.ses.HapticYoneticisi
import com.keremtulu.questionnote.data.ses.SesTuruGenis
import com.keremtulu.questionnote.data.ses.SesYoneticisi
import com.keremtulu.questionnote.ui.animasyon.animateFloatPremium
import com.keremtulu.questionnote.ui.animasyon.animateItemInsertion
import com.keremtulu.questionnote.ui.animasyon.rememberShimmer
import com.keremtulu.questionnote.ui.bilesenler.premium.AdimIlerlemeGostergesi
import com.keremtulu.questionnote.ui.bilesenler.premium.BosDurumTekrarYok
import com.keremtulu.questionnote.ui.bilesenler.premium.CamKarti
import com.keremtulu.questionnote.ui.bilesenler.premium.CamYukseklik
import com.keremtulu.questionnote.ui.bilesenler.premium.GradyanKart
import com.keremtulu.questionnote.ui.bilesenler.premium.IskeletKart
import com.keremtulu.questionnote.ui.bilesenler.premium.IskeletMetin
import com.keremtulu.questionnote.ui.bilesenler.premium.KutlamaParcaciklari
import com.keremtulu.questionnote.ui.bilesenler.premium.PremiumCizgiselIlerleme
import com.keremtulu.questionnote.ui.bilesenler.premium.PremiumDaireselIlerleme
import com.keremtulu.questionnote.ui.bilesenler.premium.PremiumBosDurum
import com.keremtulu.questionnote.ui.bilesenler.premium.PremiumListItem
import com.keremtulu.questionnote.ui.bilesenler.premium.VarsayilanIlustrasyon
import com.keremtulu.questionnote.ui.tema.LocalHareketPolitikasi
import com.keremtulu.questionnote.ui.tema.PremiumYanlisDefteriTema
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Internal showcase for testing the premium motion, sound, and surface toolkit. */
class PremiumDemoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PremiumYanlisDefteriTema {
                PremiumSistemleriBaslat()
                PremiumDemoEkrani(onGeri = ::finish)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SesYoneticisi.uygulama(applicationContext).devamEt()
    }

    override fun onPause() {
        SesYoneticisi.uygulama(applicationContext).hepsiniDurdur()
        super.onPause()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumDemoEkrani(onGeri: (() -> Unit)? = null) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium deneyim", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onGeri != null) androidx.compose.material3.TextButton(onClick = onGeri) { Text("Geri") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Daha akıcı hareket, yumuşak sesler ve küçük dokunsal ayrıntılar.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
            SesVeTitreSimBolumu()
            CamVeGradyanBolumu()
            IlerlemeBolumu()
            IskeletBolumu()
            HareketVeKutlamaBolumu()
            ListeVeBosDurumBolumu()
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DemoBolum(baslik: String, aciklama: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(baslik, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            if (aciklama != null) Text(aciklama, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
private fun SesVeTitreSimBolumu() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var ambiyans by remember { mutableStateOf(false) }
    DemoBolum("Ses ve dokunsal geri bildirim", "Kısa efektler ve haptik örüntüler; Ayarlar'daki ses tercihini izler.") {
        listOf(
            listOf("Hafif tık" to SesTuruGenis.TIKLAMA, "Sayfa geçişi" to SesTuruGenis.SAYFA_GECIS),
            listOf("Başarı" to SesTuruGenis.BASARI_ORTA, "Rozet" to SesTuruGenis.ROZET_KAZANDI),
            listOf("Odak başladı" to SesTuruGenis.ODAK_BASLADI, "Uyarı" to SesTuruGenis.UYARI),
        ).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (label, sound) ->
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { SesYoneticisi.uygulama(context).calPremium(sound) },
                    ) { Text(label, textAlign = TextAlign.Center) }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(modifier = Modifier.weight(1f), onClick = {
                HapticYoneticisi.uygulama(context).tetkikle(HapticTipi.TIKLAMA)
            }) { Text("Hafif titreşim") }
            Button(modifier = Modifier.weight(1f), onClick = {
                HapticYoneticisi.uygulama(context).tetkikle(HapticTipi.KUTLAMA)
            }) { Text("Kutlama titreşimi") }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("Odak ambiyansı", fontWeight = FontWeight.SemiBold)
                Text("Düşük sesli, kapatılabilir arka plan döngüsü", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = ambiyans,
                onCheckedChange = {
                    ambiyans = it
                    val sounds = SesYoneticisi.uygulama(context)
                    if (it) sounds.ambiyansBaslat(SesTuruGenis.AMBIYANS_ODAK) else sounds.ambiyansDurdur()
                },
            )
        }
    }
}

@Composable
private fun CamVeGradyanBolumu() {
    DemoBolum("Cam yüzeyler ve gradyanlar", "Yumuşak kenarlık, derinlik ve kontrollü vurgu.") {
        CamKarti(elevation = CamYukseklik.YUKSEK, onClick = {}) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Cam kart", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Tıklama sırasında hafifçe küçülür; içerik kontrastı korunur.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        GradyanKart(gradyan = PremiumGradyanlar.KAHRAMAN_GRADYAN) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Odak alanı", color = PremiumMarkaRenkler.Kagit, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Derin petrol gradyanı ve sıcak vurgu kenarı.", color = PremiumMarkaRenkler.Kagit.copy(alpha = 0.86f), style = MaterialTheme.typography.bodyMedium)
            }
        }
        CamKarti(elevation = CamYukseklik.DUSUK) {
            Text("Daha sakin bir yüzey varyantı", Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun IlerlemeBolumu() {
    var progress by remember { mutableStateOf(0.42f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(2_400)
            progress = if (progress >= 0.99f) 0.18f else (progress + 0.17f).coerceAtMost(1f)
        }
    }
    DemoBolum("İlerleme göstergeleri", "Animasyonlar sistem hareket azaltma ayarına uyar.") {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            PremiumDaireselIlerleme(progress = progress, modifier = Modifier.size(92.dp), strokeWidth = 7)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Bugünkü hedef", fontWeight = FontWeight.SemiBold)
                PremiumCizgiselIlerleme(progress = progress, height = 10, modifier = Modifier.fillMaxWidth())
                Text("%${(progress * 100).toInt()} tamamlandı", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        AdimIlerlemeGostergesi(
            steps = listOf("Başla", "Çalış", "Değerlendir", "Kaydet"),
            currentStep = (progress * 3).toInt().coerceIn(0, 3),
        )
    }
}

@Composable
private fun IskeletBolumu() {
    val shimmer = rememberShimmer()
    DemoBolum("Yükleme iskeletleri", "İçerik hazırlanırken düzen sabit kalır.") {
        IskeletMetin(lines = 2, widthFraction = 0.76f, animation = shimmer)
        repeat(2) { IskeletKart(height = 86, animation = shimmer) }
    }
}

@Composable
private fun HareketVeKutlamaBolumu() {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val hareket = LocalHareketPolitikasi.current
    var scaleTarget by remember { mutableStateOf(1f) }
    var particlesVisible by remember { mutableStateOf(false) }
    var celebration by remember { mutableStateOf(false) }
    val scale = animateFloatPremium(scaleTarget, label = "demo-microinteraction")
    KutlamaDizisiOynat(celebration, KutlamaTuru.ROZET)
    DemoBolum("Mikro hareketler ve kutlama", "Az ve amaçlı: ana içerikten rol çalmaz.") {
        Box(Modifier.fillMaxWidth().height(160.dp)) {
            Box(
                Modifier.align(Alignment.Center).size(78.dp)
                    .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
                    .background(PremiumMarkaRenkler.Murekkep, RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("Dokun", color = PremiumMarkaRenkler.Kagit, fontWeight = FontWeight.Bold)
            }
            KutlamaParcaciklari(visible = particlesVisible, modifier = Modifier.fillMaxSize())
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(modifier = Modifier.weight(1f), onClick = {
                scaleTarget = if (scaleTarget < 1.1f) 1.12f else 1f
                SesYoneticisi.uygulama(context).calPremium(SesTuruGenis.KART_SECILDI, HapticTipi.KART_SECILDI, scope)
            }) { Text("Basış animasyonu") }
            Button(modifier = Modifier.weight(1f), onClick = {
                particlesVisible = true
                scope.launch { delay(1_500); particlesVisible = false }
            }) { Text("Parçacık efekti") }
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = {
            celebration = true
            scope.launch { delay(60); celebration = false }
        }) { Text("Rozet kutlamasını dinle") }
        if (!hareket.hareketAcik) {
            Text("Cihazın hareket azaltma ayarı açık; dekoratif animasyonlar kapalı.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ListeVeBosDurumBolumu() {
    DemoBolum("Liste ve boş durum", "Küçük etkileşimler, net yönlendirmeyle birlikte.") {
        listOf(
            "Matematik" to "12 soru · 3 konu · Bugün çalışıldı",
            "Fizik" to "8 soru · 2 konu · Dün çalışıldı",
            "Kimya" to "5 soru · 1 konu · Yeni eklendi",
        ).forEachIndexed { index, (lesson, detail) ->
            val enter = animateItemInsertion(index)
            PremiumListItem(
                modifier = Modifier.graphicsLayer {
                    alpha = enter.value
                    translationX = (1f - enter.value) * -28.dp.toPx()
                },
                title = lesson,
                subtitle = detail,
                trailing = { Text("›", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                onClick = {},
            )
        }
        PremiumBosDurum(
            modifier = Modifier.fillMaxWidth().height(320.dp),
            illustration = { VarsayilanIlustrasyon() },
            baslik = "Yeni bir başlangıç",
            aciklama = "İlk sorunu eklediğinde tekrar planın burada görünecek.",
            butonMetni = "Örnek düğme",
            onButonTikla = {},
        )
    }
}
