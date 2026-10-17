package com.keremtulu.questionnote.domain.repeat

import com.keremtulu.questionnote.domain.model.Soru
import java.time.Instant
import java.time.ZoneId

/** Orders a filtered practice set from the question with the oldest study activity. */
object CalismaSorulariniSirala {
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
