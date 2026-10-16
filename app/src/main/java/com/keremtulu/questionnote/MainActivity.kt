package com.keremtulu.questionnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.keremtulu.questionnote.ui.DerslerEkrani
import com.keremtulu.questionnote.ui.tema.YanlisDefteriTema

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YanlisDefteriTema {
                DerslerEkrani()
            }
        }
    }
}
