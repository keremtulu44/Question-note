# HANDOFF — Yanlış Defteri

Son güncelleme: **2026-10-17**. Önce bu belge, `PROJECT_BRIEF.md`, `docs/DERLEME_RAPORU.md` okunur.

## Güncel durum

- **v1.2.0 (versionCode 14) hazırlanıyor.** Özellik kodunun CI run `39067149944` doğrulaması
  başarılı: **127 test**, Release derlemesi, imza ve beklenen sertifika eşleşti. Bu doğrulama
  sürüm artırılmadan yapıldığı için eski v1.1.1 Release dosyası değiştirilmedi.
- Son adım: 3 Compose/Robolectric UI testi (inset/büyük yazı, gizli rozet detayları, kutlama kuyruğu)
  ve v1.2.0 sürüm artışı ile son CI/Release. Henüz yayımlandı kabul edilmez.
- Son yayımlanan sürüm: [v1.1.1](https://github.com/keremtulu44/Question-note/releases/tag/v1.1.1),
  APK SHA-256 `6ea54b9c378936be5d184e6fc2b3f93037050a81306cc6a5dadd5f968e0cb015`.
- **Dal:** `arena/59c6570d-question-note`. Kullanıcı açıkça merge/PR istemiyor; main'e dokunulmaz.
  Her adım için onay istenmez. Bash çağrılarına kısa timeout konur; uzun `gh run watch` yerine
  aralıklı `gh run view --json status,conclusion` kullanılır.
- Yerelde Java/Android SDK yok; gerçek derleme/test CI'da. Yerel kaynak/XML kontrolleri derlemenin
  yerine geçmez. CI botu derleme raporunu aynı dala commit eder; push öncesi fetch/rebase gerekir.

## v1.2.0 teslim kapsamı

1. **30 rozet / 5 gizli**; koşullar, birikimli ilerleme, filtreler ve detay. Eski 11 rozet anahtarı
   korunur. Gizli rozet kilitliyken ad, koşul ve özel çizim hem kart hem detayda gizlidir.
2. **15 rütbe**; 6 yeni üretilmiş saydam WebP amblem (5 yoğunluk, 96 dp), rütbe eşiği galerisi.
   Ürün seviyesi 200 sınırı kaldırıldı; sayısal taşma koruması var.
3. **Tam ekran kutlama**; kalıcı sahiplik + bekleyen kuyruğu, idempotent kazanım, sonlu animasyon,
   sistem hareket ayarına uyum. Form/odak ekranını kesmez; güvenli ekranda gösterilir.
4. **Alt menü inset düzeltmesi** ve üst sağ Ayarlar kısayollarının kaldırılması. Android nav/cutout
   boşluğu menü tarafından uygulanır, dış Scaffold iç ekranlar için tüketir.
5. **LGS / TYT / AYT / KPSS / Tümü**: eksik dersleri ekleyen, mevcut ID/veriyi koruyan profil.
   Bugün/Dersler/Filtreli filtrelenir; tüm konu yönetimi profil dışında kalan derslere de ulaşır.
6. **Ayarlar → Konu yönetimi ve silme**. Kullanımdaki konu silinmez; arşivleme alternatifi vardır.
7. **Odak Stüdyosu**: tek/sıralı sorular, akıllı/en eski sıralama, 5/10/20 hazır sayılar, mola,
   çözüm açmadan değerlendirme, önizleme, ayrı Kaydet, sona bırak ve otomatik karne.
8. **Adaptif tekrar v2**: kolaylık/gecikme/unutma mevcut geçmişten türetilir. Önizleme ve yazma
   aynı fonksiyondur. Eski v1 kayıtları değişmez; yeniler `algoritma_surumu=2`.
9. **Oturum devam noktası**: ID/konum/sonuç/çözüm işaretleri cihazda; Room commit sonrası kesinti
   uzlaştırılır. Tekrar veya XP ikinci kez yazılmaz. Tam karne arşivi değildir.
10. **Bağımsız sayım düzeltmesi**: yalnız RAHAT + çözüm açılmamış; ZORLANDIM hâlâ +5 emek kazanır.

## Veri ve uyumluluk

- **Room v3 / ZIP v3**; v2 ZIP içe alma ve v1→v2→v3 migration zinciri korunur.
- Önceki v4 kolon önerisi yerine durum geçmişten türetildi; v4 migration yapılmadı/gerekmiyor.
- XP tablosu yok. Rozet/hedef/profil/son oturum prefs'tedir, ZIP'e dahil değildir.
- `AkilliTekrarKurali` yeni kaydı planlar; `TekrarKurali` v1 referansı ve testleri olarak kalır.
- Sınav profili hiçbir soruyu silmez/arşivlemez. Kullanıcının arşivli dersleri otomatik açılmaz.
- Geri yükleme eski devam noktasını temizler; seçili profilin eksik dersleri yeniden hazırlanır.

## Başlıca dosyalar

- `domain/karne/Rozet.kt`, `data/ilerleme/RozetHizmeti.kt`, `data/prefs/RozetDeposu.kt` — kurallar,
  geriye dönük toplamlar, idempotent sahiplik ve kutlama kuyruğu.
- `ui/karne/RozetKutlamasi.kt`, `ui/seviye/{RozetKoleksiyonu,RutbeYolculugu}.kt` — gizli/açık galeri,
  tam ekran kutlama, rütbe keşfi.
- `domain/model/SinavProfili.kt`, `data/prefs/SinavProfiliDeposu.kt`, `RoomDersRepository.kt` —
  format ve veri koruyan ders hazırlama.
- `ui/ayarlar/KonuMerkeziEkrani.kt` — profil dışı/arşivli dersleri de içeren konu yönetimi girişi.
- `domain/repeat/AkilliTekrarKurali.kt`, `ui/calisma/OdakOturumuEkrani.kt` — adaptif plan ve odak.
- `domain/calisma/OdakOturumu.kt`, `data/prefs/OdakOturumuDeposu.kt` — devam/kurtarma.
- `ui/AltMenu.kt`, `ui/YanlisDefteriUygulamasi.kt` — güvenli insets, gezinme, oturum ve kutlama host'u.
- `docs/SEVIYE_SISTEMI.md`, `docs/ODAK_STUDYOSU.md` — güncel uygulama kuralları.

## Test durumu ve sıradaki işler

- 127 JVM/Room/Robolectric testi CI'da geçti (run `39067149944`). Testler yeni profil/katalog
  silme, rozet eşiği/sahiplik, algoritma, ZIP'te v2 kayıt, devam noktası ve bağımsız sayımı kapsar.
- İlk genişletilmiş koşuda bir yeni test hazır **kaynak** bekliyordu; hazırlar yalnız **yanlış
  nedenleri** olduğu için test fixture'ı düzeltildi. Ürün davranışı gevşetilmedi.
- Son CI'da 3 UI testi ve imzalı **v1.2.0** Release tamamlanacak; ardından bu başlık ve SHA güncellenecek.
- Cihaz kabulü **ayrı ve bekliyor**: S24 FE üzerinde 3-düğmeli/jest nav, yatay ekran, büyük yazı,
  gerçek fotoğraflar, gizli rozet popup, arka plan/süreç kaybı ve eski sürüm üzerine güncelleme.
- Geçmiş karneler, mola günü jetonu ve bildirimler yapılmış sayılmaz. Şu an aktif molalı oturum
  vardır; bu seri koruyan "mola günü jetonu" değildir.
- Tarihsel sürüm/CI kanıtları `CHANGELOG.md`, kabul kayıtları `docs/TEST_KONTROL_LISTESI.md` içinde.
