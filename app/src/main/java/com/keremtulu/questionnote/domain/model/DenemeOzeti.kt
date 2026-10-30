package com.keremtulu.questionnote.domain.model

/**
 * Deneme listesinin üstünde gösterilen küçük özet.
 *
 * Dürüstlük kuralı: farklı puanlama kuralları (4 yanlış 1 doğru, 3 yanlış 1 doğru, götürmeyen)
 * aynı ölçekte toplanamaz. Liste karışıksa ortalama/en iyi net üretilmez; yalnız kayıt sayısı
 * ve son net döner, arayüz "neden karşılaştırma yok" açıklamasını gösterir. Uydurma bir sayı
 * yerine boşluk yeğlenir.
 */
data class DenemeOzeti(
    val kayitSayisi: Int,
    val ortalamaNet: Double? = null,
    val enIyiNet: Double? = null,
    val sonNet: Double? = null,
    val oncekiIleFark: Double? = null,
) {
    /** Aynı kuraldaki kayıtlar varsa ortalama/en iyi net anlamlıdır. */
    val karsilastirilabilir: Boolean get() = ortalamaNet != null
}

fun List<DenemeKaydi>.ozet(): DenemeOzeti {
    if (isEmpty()) return DenemeOzeti(kayitSayisi = 0)
    val sirali = kronolojik()
    val son = sirali.last()
    if (map { it.kural }.distinct().size > 1) {
        return DenemeOzeti(kayitSayisi = size, sonNet = son.toplamNet())
    }
    val netler = sirali.map { it.toplamNet() }
    val onceki = sirali.dropLast(1).lastOrNull()
    return DenemeOzeti(
        kayitSayisi = size,
        ortalamaNet = netler.average(),
        enIyiNet = netler.max(),
        sonNet = netler.last(),
        oncekiIleFark = onceki?.let { son.toplamNet() - it.toplamNet() },
    )
}

/**
 * Listenin en düşük netli dersi: ders adı + net. Tek dersli denemede "en düşük ders" bilgisi
 * yanıltıcı olacağı için null döner; arayüz bu satırı hiç çizmez.
 */
fun DenemeKaydi.enZayifDers(dersAdlari: Map<String, String>): Pair<String, Double>? {
    if (satirlar.size < 2) return null
    val zayif = satirlar.minByOrNull { kural.net(it.dogru, it.yanlis) } ?: return null
    val ad = dersAdlari[zayif.dersId] ?: zayif.dersAdi ?: return null
    return ad to kural.net(zayif.dogru, zayif.yanlis)
}
