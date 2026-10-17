package com.keremtulu.questionnote.data.local

import com.keremtulu.questionnote.data.local.entity.SoruSecenegiEntity
import com.keremtulu.questionnote.domain.model.SoruSecenekTuru
import com.keremtulu.questionnote.domain.model.normalizeSoruSecenegiAdi

internal val VarsayilanSoruNedenleri = listOf(
    "Yanlış okuma",
    "Soruyu yanlış anlama",
    "Bilgi eksiği",
    "Kural/formül karışıklığı",
    "İşlem/hesap hatası",
    "Dikkat/acele",
    "Zaman baskısı",
    "Hatırlayamama",
)

internal fun varsayilanSoruSecenekleri(now: Long): List<SoruSecenegiEntity> =
    VarsayilanSoruNedenleri.mapIndexed { index, ad ->
        SoruSecenegiEntity(
            id = "builtin-reason-${index + 1}",
            tur = SoruSecenekTuru.YANLIS_NEDENI.name,
            ad = ad,
            normalizeAd = normalizeSoruSecenegiAdi(ad),
            varsayilan = true,
            sira = index,
            olusturma = now,
        )
    }
