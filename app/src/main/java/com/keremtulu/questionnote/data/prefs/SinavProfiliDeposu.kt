package com.keremtulu.questionnote.data.prefs

import android.content.Context
import com.keremtulu.questionnote.domain.model.KullaniciRolu
import com.keremtulu.questionnote.domain.model.SinavProfili
import com.keremtulu.questionnote.domain.model.SinavSecimi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Stored separately from the question backup. Selection is published only after seeding succeeds. */
class SinavProfiliDeposu(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("sinav_profili", Context.MODE_PRIVATE)
    private val secim = MutableStateFlow(
        SinavSecimi(
            profil = SinavProfili.anahtarla(prefs.getString("secili", null)),
            dersIdleri = prefs.getStringSet("dersler", emptySet()).orEmpty().toSet(),
            ozelDersAdlari = prefs.getStringSet("ozel_ders_adlari", emptySet()).orEmpty().toSet(),
        ),
    )
    private val rol = MutableStateFlow(KullaniciRolu.anahtarla(prefs.getString("kullanici_rolu", null)))
    private val rolSecildiDurumu = MutableStateFlow(
        prefs.contains("kullanici_rolu") || !kurulumGerekli(),
    )
    val secimIzle: StateFlow<SinavSecimi> = secim.asStateFlow()
    val rolIzle: StateFlow<KullaniciRolu> = rol.asStateFlow()
    val rolSecildiIzle: StateFlow<Boolean> = rolSecildiDurumu.asStateFlow()

    fun secili(): SinavProfili = secim.value.profil
    fun aktifRol(): KullaniciRolu = rol.value
    fun rolSecildi(): Boolean = rolSecildiDurumu.value

    fun kurulumGerekli(): Boolean =
        !prefs.getBoolean("kurulum", false) && prefs.getString("secili", null) == null

    fun rolKaydet(yeniRol: KullaniciRolu) {
        check(
            prefs.edit()
                .putString("kullanici_rolu", yeniRol.name)
                .commit(),
        )
        rol.value = yeniRol
        rolSecildiDurumu.value = true
    }

    fun kaydet(
        profil: SinavProfili,
        dersIdleri: Set<String>,
        ozelDersAdlari: Set<String> = emptySet(),
    ) {
        val kopyaIdler = dersIdleri.toSet()
        val kopyaAdlar = ozelDersAdlari.toSet()
        val kayitliRol = prefs.getString("kullanici_rolu", null) ?: KullaniciRolu.OGRENCI.name
        check(
            prefs.edit()
                .putString("secili", profil.name)
                .putStringSet("dersler", kopyaIdler)
                .putStringSet("ozel_ders_adlari", kopyaAdlar)
                .putBoolean("kurulum", true)
                .putString("kullanici_rolu", kayitliRol)
                .commit(),
        )
        secim.value = SinavSecimi(profil, kopyaIdler, ozelDersAdlari = kopyaAdlar)
        rol.value = KullaniciRolu.anahtarla(kayitliRol)
        rolSecildiDurumu.value = true
    }
}
