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

    /**
     * Sunum için serpiştirilmiş akıllı sıra: önce aciliyet kuşağı (geciken → vadesi bugün →
     * gelecek) korunur; kuşak içinde dersler ve mümkün olduğunda konular serpiştirilir.
     * Aynı konu kuyruğundaki göreli akıllı öncelik bozulmaz. Deterministiktir; tekrar
     * tarihlerini ve öğrenme durumunu değiştirmez.
     */
    fun karmaAkilliOncelik(sorular: List<Soru>, bugun: Int): List<Soru> {
        val temel = akilliOncelik(sorular, bugun)
        if (temel.size < 3) return temel
        val geciken = temel.filter {
            val sonraki = it.sonrakiTekrarGun
            sonraki != null && sonraki < bugun
        }
        val vadesiBugun = temel.filter { it.sonrakiTekrarGun == bugun }
        val gelecek = temel.filter {
            val sonraki = it.sonrakiTekrarGun
            sonraki == null || sonraki > bugun
        }
        val gecikenSirali = dersleriSerpistir(geciken)
        val vadesiBugunSirali = dersleriSerpistir(vadesiBugun, gecikenSirali.lastOrNull())
        val gelecekSirali = dersleriSerpistir(
            gelecek,
            vadesiBugunSirali.lastOrNull() ?: gecikenSirali.lastOrNull(),
        )
        return gecikenSirali + vadesiBugunSirali + gelecekSirali
    }

    private fun dersleriSerpistir(
        temel: List<Soru>,
        oncekiIlk: Soru? = null,
    ): List<Soru> {
        if (temel.size < 3) return temel
        val sira = temel.withIndex().associate { (index, soru) -> soru.id to index }
        val kuyruklar = linkedMapOf<String, MutableList<Soru>>()
        temel.forEach { kuyruklar.getOrPut(it.dersId) { mutableListOf() } += it }
        val sonuc = mutableListOf<Soru>()
        while (sonuc.size < temel.size) {
            val aktif = kuyruklar.values.filter { it.isNotEmpty() }
            if (aktif.size == 1) {
                sonuc += konulariSerpistir(
                    aktif.single(),
                    sonuc.lastOrNull() ?: oncekiIlk,
                    sira,
                )
                break
            }
            val onceki = sonuc.lastOrNull() ?: oncekiIlk
            var havuz = aktif.filter { it.first().dersId != onceki?.dersId }
            if (havuz.isEmpty()) havuz = aktif
            val konuHavuzu = havuz.filter { it.first().konuId != onceki?.konuId }
            if (konuHavuzu.isNotEmpty()) havuz = konuHavuzu
            val secilen = havuz.minBy { sira.getValue(it.first().id) }
            sonuc += secilen.removeAt(0)
        }
        return sonuc
    }

    private fun konulariSerpistir(
        kalan: List<Soru>,
        oncekiIlk: Soru?,
        sira: Map<String, Int>,
    ): List<Soru> {
        if (kalan.size < 3) return kalan
        val kuyruklar = linkedMapOf<String?, MutableList<Soru>>()
        kalan.forEach { kuyruklar.getOrPut(it.konuId) { mutableListOf() } += it }
        if (kuyruklar.size < 2) return kalan
        val sonuc = mutableListOf<Soru>()
        var onceki = oncekiIlk
        while (sonuc.size < kalan.size) {
            val aktif = kuyruklar.values.filter { it.isNotEmpty() }
            var havuz = aktif.filter { it.first().konuId != onceki?.konuId }
            if (havuz.isEmpty()) havuz = aktif
            val secilen = havuz.minBy { sira.getValue(it.first().id) }
            val soru = secilen.removeAt(0)
            sonuc += soru
            onceki = soru
        }
        return sonuc
    }

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
