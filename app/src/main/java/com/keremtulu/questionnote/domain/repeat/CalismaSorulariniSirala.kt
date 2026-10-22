package com.keremtulu.questionnote.domain.repeat

import com.keremtulu.questionnote.domain.model.Soru
import com.keremtulu.questionnote.domain.model.OgrenmeDurumu
import java.time.Instant
import java.time.ZoneId

/** Orders a filtered practice set from the question with the oldest study activity. */
object CalismaSorulariniSirala {
    /** Due/overdue work first, then weaker/new questions; deterministic ties prevent queue jumping. */
    fun akilliOncelik(sorular: List<Soru>, bugun: Int): List<Soru> = sorular.sortedWith(
        compareBy<Soru> { if ((it.sonrakiTekrarGun ?: Int.MAX_VALUE) <= bugun) 0 else 1 }
            .thenBy { it.sonrakiTekrarGun ?: Int.MAX_VALUE }
            .thenBy { when (it.ogrenmeDurumu) {
                OgrenmeDurumu.OGRENIYOR -> 0
                OgrenmeDurumu.YENI -> 1
                OgrenmeDurumu.PEKISIYOR -> 2
                OgrenmeDurumu.SAGLAM -> 3
            } }
            .thenBy { it.sonTekrarGun ?: Int.MIN_VALUE }
            .thenBy { it.olusturma }
            .thenBy { it.id },
    )

    fun enUzunSureCalisilmayanlar(
        sorular: List<Soru>,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): List<Soru> = sorular.sortedWith(
        compareBy<Soru> { soru ->
            soru.sonTekrarGun ?: Instant.ofEpochMilli(soru.olusturma)
                .atZone(zoneId)
                .toLocalDate()
                .toEpochDay()
                .toInt()
        }
            .thenBy { it.sonrakiTekrarGun ?: Int.MAX_VALUE }
            .thenBy { it.olusturma }
            .thenBy { it.id },
    )
}
