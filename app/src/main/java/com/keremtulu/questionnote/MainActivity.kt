package com.keremtulu.questionnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.keremtulu.questionnote.ui.YanlisDefteriUygulamasi
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema

class MainActivity : ComponentActivity() {
    private val appContainer by lazy(LazyThreadSafetyMode.NONE) {
        AppContainer(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YanlisDefteriTema {
                YanlisDefteriUygulamasi(
                    dersRepository = appContainer.dersRepository,
                    konuRepository = appContainer.konuRepository,
                )
            }
        }
    }
}
