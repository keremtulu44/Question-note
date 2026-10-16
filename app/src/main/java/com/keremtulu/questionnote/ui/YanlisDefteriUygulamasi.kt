package com.keremtulu.questionnote.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.keremtulu.questionnote.domain.model.Ders
import com.keremtulu.questionnote.domain.repository.DersRepository
import com.keremtulu.questionnote.domain.repository.KonuRepository

@Composable
fun YanlisDefteriUygulamasi(
    dersRepository: DersRepository,
    konuRepository: KonuRepository,
) {
    var seciliDers by remember { mutableStateOf<Ders?>(null) }

    BackHandler(enabled = seciliDers != null) {
        seciliDers = null
    }

    val ders = seciliDers
    if (ders == null) {
        DerslerEkrani(
            repository = dersRepository,
            onDersSecildi = { seciliDers = it },
        )
    } else {
        KonuYonetimiEkrani(
            ders = ders,
            repository = konuRepository,
            onGeri = { seciliDers = null },
        )
    }
}
