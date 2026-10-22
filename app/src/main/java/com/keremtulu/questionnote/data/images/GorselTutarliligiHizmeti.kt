package com.keremtulu.questionnote.data.images

import androidx.room.withTransaction
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase

data class GorselBakimRaporu(
    val eksikDosyalar: List<String>,
    val bozukDosyalar: List<String>,
    val onayBekleyenYetimler: List<KaliciGorselDosyasi>,
)

class GorselTutarliligiHizmeti(
    private val database: YanlisDefteriDatabase,
    private val depo: LocalSoruGorselDeposu,
) {
    suspend fun denetle(simdi: Long = System.currentTimeMillis()): GorselBakimRaporu {
        val kayitlar = database.gorselDao().tumunuGetir()
        val disk = depo.kaliciDosyalariListele().associateBy { it.dosyaAdi }
        val namesInDatabase = kayitlar.mapTo(mutableSetOf()) { it.dosyaAdi }
        val eksik = mutableListOf<String>()
        val bozuk = mutableListOf<String>()
        kayitlar.forEach { row ->
            val file = disk[row.dosyaAdi]
            when {
                file == null -> eksik += row.dosyaAdi
                file.boyut != row.boyut || file.sha256 != row.sha256 ||
                    file.genislik != row.genislik || file.yukseklik != row.yukseklik -> bozuk += row.dosyaAdi
            }
        }
        val yetimler = disk.values.filter {
            it.dosyaAdi !in namesInDatabase && simdi - it.sonDegisme >= YETIM_MIN_YASI_MS
        }.sortedBy { it.dosyaAdi }
        return GorselBakimRaporu(eksik, bozuk, yetimler)
    }

    /** Only exact user-selected names are considered; references and file age are rechecked before deletion. */
    suspend fun onaylananYetimleriSil(dosyaAdlari: Set<String>, simdi: Long = System.currentTimeMillis()): Int {
        if (dosyaAdlari.isEmpty()) return 0
        val candidates = depo.kaliciDosyalariListele().filter {
            it.dosyaAdi in dosyaAdlari && simdi - it.sonDegisme >= YETIM_MIN_YASI_MS
        }
        val initiallyUnreferenced = database.withTransaction {
            val references = database.gorselDao().tumunuGetir().mapTo(mutableSetOf()) { it.dosyaAdi }
            candidates.filter { it.dosyaAdi !in references }
        }
        var deleted = 0
        for (candidate in initiallyUnreferenced) {
            val stillUnreferenced = database.withTransaction {
                database.gorselDao().tumunuGetir().none { it.dosyaAdi == candidate.dosyaAdi }
            }
            if (
                stillUnreferenced &&
                simdi - candidate.sonDegisme >= YETIM_MIN_YASI_MS &&
                depo.kaliciDosyayiSil(candidate.dosyaAdi)
            ) {
                deleted++
            }
        }
        return deleted
    }

    private companion object {
        const val YETIM_MIN_YASI_MS = 24L * 60L * 60L * 1_000L
    }
}
