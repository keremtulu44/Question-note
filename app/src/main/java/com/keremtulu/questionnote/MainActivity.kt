package com.keremtulu.questionnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.keremtulu.questionnote.data.ses.SesHizmeti
import com.keremtulu.questionnote.ui.LocalUygulamaSesleri
import com.keremtulu.questionnote.ui.YanlisDefteriUygulamasi
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema

class MainActivity : ComponentActivity() {
    private val appContainer by lazy(LazyThreadSafetyMode.NONE) {
        AppContainer.getInstance(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YanlisDefteriTema {
                CompositionLocalProvider(LocalUygulamaSesleri provides SesHizmeti.uygulama(this)) {
                YanlisDefteriUygulamasi(
                    dersRepository = appContainer.dersRepository,
                    konuRepository = appContainer.konuRepository,
                    soruRepository = appContainer.soruRepository,
                    gorselDeposu = appContainer.gorselDeposu,
                    gorselTutarliligi = appContainer.gorselTutarliligi,
                    disaAktarmaHizmeti = appContainer.disaAktarmaHizmeti,
                    ilerlemeHizmeti = appContainer.ilerlemeHizmeti,
                    seviyeHizmeti = appContainer.seviyeHizmeti,
                    rozetDeposu = appContainer.rozetDeposu,
                    rutbeDeposu = appContainer.rutbeDeposu,
                    rozetHizmeti = appContainer.rozetHizmeti,
                    profilDeposu = appContainer.profilDeposu,
                    tekrarPolitikasi = appContainer.tekrarPolitikasi,
                    motorTanisi = appContainer.motorTanisi,
                    oturumDeposu = appContainer.oturumDeposu,
                    gunlukHedefDeposu = appContainer.gunlukHedefDeposu,
                    tercihHedefDeposu = appContainer.tercihHedefDeposu,
                )
                }
            }
        }
    }
}
