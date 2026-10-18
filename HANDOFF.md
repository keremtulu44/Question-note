# HANDOFF — Yanlış Defteri

Son güncelleme: **2026-10-18**. Önce bu belge, `docs/DERLEME_RAPORU.md` ve
[Ölçüm/profil sözleşmesi](docs/OLCUM_VE_PROFILLER.md) okunur.

## Güncel teslim

- **P0–P3 uygulandı; son teslim v1.4.1 / versionCode 17.**
- [CI `39136991000`](https://github.com/keremtulu44/Question-note/actions/runs/39136991000):
  **221 test; 0 başarısız / 0 hata / 0 atlanan**. Release, apksigner ve beklenen sertifika başarılı.
- Derlenen commit `2fbe6255646029d040cfe4e2f2d600340e0206a7`; rapor botu `5e94273`.
- [Release v1.4.1](https://github.com/keremtulu44/Question-note/releases/tag/v1.4.1) ·
  [APK](https://github.com/keremtulu44/Question-note/releases/download/v1.4.1/yanlis-defteri-1.4.1.apk).
  **10.651.871 bayt**, SHA-256 `9634c5bb59ea574f27c101a8332ed2689719ddf95e31a5a102b6165e48180057`.
  GitHub asset `653705565` digest'i CI ile ayrıca karşılaştırıldı; aynı.
- P2/P3 ana paket v1.4.0/code16, CI `39135928734`, **217/0**, commit `bb823ba` idi.
  v1.4.1 dört ek kesinti/sahiplik/çözüm bariyeri regresyonunu ve son sertleştirmeyi içerir.
- **Dal:** `arena/59c6570d-question-note`. Merge/PR yok, main'e dokunulmaz. Adım adım onay
  istenmez. Bash timeout'lu; uzun `gh run watch` yerine ~25 s aralıkla sınırlı `gh run view`.
- Yerelde Java/Android SDK yok; gerçek derleme/test CI'da. Statik kontrol derleme yerine geçmez.
  CI botu aynı dala rapor/schema/önizleme yazar; push öncesi fetch/rebase gerekir.

## P2 — gerçek ölçüm ve veri güvenliği

- Saf `AktifSureOlcer`, enjekte edilen monotonik segment farklarını toplar. Android adaptörü
  elapsedRealtime kullanır. UI tick'i süre kaynağı değil, gösterimdir.
- Deneme/inceleme ayrı. İlk çözüm açılması kalıcı faz değişimidir; gizleyince bağımsızlığa dönmez.
  Değerlendirmede süre dondurulur; loading, kaydetme, mola, arka plan, çıkış onayı sayılmaz.
  Tam ekran/eksik görsel kısmi; referans dışı. Sona bırakmada soru birikimi karışmaz.
- YOK/TAM/KISMI/GECERSIZ + nedenler, ölçüm sürümü 1; nullable tarihi süre. Faz başına 24 saat
  sınırı; geçerli uç süreler silinmez, taşma/tutarsız saat tam diye kırpılmaz.
- Checkpoint v2: küçük ID/ölçüm/seçim snapshot'ları. Monotonik başlangıç yazılmaz. Yeni süreçte
  birikim kısmi olarak kurtarılır; kapalı zaman eklenmez. V1 checkpoint hâlâ okunur.
- Geçişlerde ve yalnız aktif ön planda yaklaşık 5 saniyede bir IO kayıt; revizyon/oturum kontrolü
  eski işin yeni konumu geri almasını veya restore ile silinen oturumu diriltmesini önler.
- Çözüm görünmeden önce işaret en güncel sahiplik snapshot'ıyla kalıcılaştırılır. Eski/superseded
  queued yazı onay sayılmaz. Başarısızsa çözüm açma yeniden denenir. Uygulama içi döndürme
  `AppContainer.getInstance` üzerinden aynı checkpoint sahibini kullanır.
- `TekrarDenemesi` / `TekrarOnizlemesi` ortak kontratı. Preview ve kayıt aynı v2 girdilerini
  kullanır; gün/saat dilimi/geçmiş/bağlam/profil değişince açık yenileme gerekir.
- Tekrar + ölçüm/bağlam + yeni soru planı tek transaction. Deneme ID'si idempotent; aynı gün
  ikinci kayıt yok. Hata sonrası dondurulan deneme korunur. Ölçüm bozuk/yoksa sonuç/XP engellenmez.
- Commit–checkpoint arası kesinti deneme/oturum kimliğiyle uzlaştırılır; ilk soru checkpoint'i
  yetişmese veya duvar saati geri alınsa da tamamlanan kayıt tanınır. Eski kimliksiz satırlarda
  zaman damgalı fallback sürer. Ölçümsüz eski ayrıntı ekranı yeni kontratı `YOK` ile kullanır.

## P3 — kişisel referanslar

- Sürümlü `ProfilPolitikasi(1)`: 60 gün, mevcut deneme hariç; ayrı yardımsız Rahat ve tüm
  sonuçlardan aktif iş yükü dağılımı. Yalnız pozitif TAM ölçümler adaydır.
- Aynı soru/gün tek örnek; soru başına medyan, sonra bu özetlerden medyan/çeyrek/MAD.
  Tek kolay sorunun çok tekrarı profili ele geçiremez. MAD/IQR=0 bölme üretmez.
- Ders **20 ölçüm / 10 soru / 5 gün**; konu/kaynak **30 / 15 / 7**. Üçü birlikte aranır.
  Yetersiz alt gruptan yalnız aynı dersin yeterli referansına dönülür; yoksa referans null.
- NFC/Türkçe harf/boşluk normalizasyonu; benzer kitap isimleri fuzzy birleştirilmez.
  Ders/konu/kaynak bağlamı kayıt anında saklanır; eski bilinmeyen bağlam uydurulmaz.
- İlerleme'de gerçek kapsam, ders referansı, eşikler ve tarih aralığı; az veride “ölçüm birikiyor”.
  Genel sınav hızı/zekâ/başarı veya bilimsel güven yüzdesi iddiası yoktur.
- Profil cache'i yok; silme/restore/tarih değişiminde DB'den yeniden türetilir. Pencere sorguları
  toplu ve indeksli; Compose gövdesinde sorgu/medyan yok. Sayaç analitiği tetiklemez.

## Veri sözleşmesi / gelecek değişiklikler için kritik

- **Room v4**, additive `MIGRATION_3_4`; `tekrar_olcumleri` 0..1 child, FK cascade ve unique
  deneme ID. Eski soru/tekrar/tarih değişmez. Şemalar 1/2/3/4 kaynak kontrolünde.
- **ZIP v4** ölçüm + bağlam + karar JSON/CSV/manifest taşır. **V2/v3 import korunur.**
  Counts/hash/FK/kalite/sürüm/taşma/snapshot doğrulaması katı; JSON kanonik, CSV insan kopyasıdır.
- Ölçüm sürümü **1**, karar snapshot sürümü **1**, politika sürümü **1**, karar modu **V2**.
  Kod yeni moda/sürüme körlemesine geçmez. P4 için codec/readers/backward compatibility açıkça genişletilmeli.
- `AkilliTekrarKurali.hafiza` hâlâ `algoritmaSurumu >= 2` kullanır; P4/v3 eklenirken desteklenen
  sürümler açıklaştırılmalı. **P2/P3 v3 tarih yazmaz, gölge karar üretmez.**
- Rozet/hedef/sınav profili/yarım oturum prefs'i ZIP'e dahil değil. Tamamlanmış ölçüm/bağlam dahildir.
- Geri yükleme tam değişimdir. Checkpoint yalnız doğrulanmış commit sonrası, recovery işaretleri
  kaldırılmadan temizlenir. Precommit başarısızlık eski oturumu korur. Yeni profiller yeni DB'den gelir.

## Korunan özellikler ve kurallar

- Offline/tek kullanıcı; hesap/sunucu yok. Bugün/Dersler/Filtreli/İlerleme/Ayarlar; yan menü ve
  sağ üst Ayarlar kısayolları yok. LGS/TYT/AYT/KPSS/Tümü ve konu yönetimi Ayarlar'da.
- **Bağımsız = RAHAT + çözüm açılmamış.** Zorlandım yarım, Çözemedim çözemedi, çözüm açıldıysa
  destekli. Süre bu sınıflandırmayı veya XP'yi değiştirmez. Tekrar algoritması **v2**.
- 15 rütbe / 30 rozet / 5 sır; katsayılar/eşikler aynı. İdempotent sahiplik/kutlama; gizli ad/art/
  erişilebilirlik maskelemesi korunur. Kutlamayı kapatmak sahipliği silmez.
- P0/P1: canlı karne toplamı, üç sınırlı observer retry + yenileme, tek XP snapshot'ı,
  premium İlerleme, ortak canlı sistem-hareket politikası, gerçek başlangıç XP'si.

## Doğrulama ve açık işler

- Önceki **157-test** tabanı korunarak **64 test** eklendi: zaman/faz, kişisel profil, atomik
  repository, checkpoint/reveal/oturum sahipliği, 1/2/3→4 migration, ZIP v2/v3/v4 ve Compose.
- İlk kontrol test importunda durdu; ikinci 217/1 eski schema fixture'ında `indices` alanı olmayan
  tabloya takıldı. Ürün korumaları gevşetilmedi. Ana koşu 217/0, son koşu 221/0.
- Statik: **619 kaynak**, 15 amblem × 5 yoğunluk, sıfır hata. Gerçek derleme, Release ve
  apksigner CI'da; yayımlanan asset digest'i ayrıca doğrulandı.
- **Fiziksel kabul açık:** kaldırmadan güncelleme, S24 FE gezinme/yatay/%200/TalkBack,
  mola/arka plan/ekran kilidi/gerçek süreç ölümü/reboot, yakınlaştırma, gerçek düşük depolama,
  SAF/bulut ve 1.000 soru/20.000 tekrar ölçeğinde performans. Sentetik test gerçek cihaz değildir.
- **P4–P6 uygulanmadı:** shadow-first v3, kaynak/konu karşılaştırma ve öneri/çalışma eylemleri,
  ölçülmüş performans/cihaz kapıları. Tek devam planı:
  [GELISIM_VE_OGRENME_PLANI.md](docs/GELISIM_VE_OGRENME_PLANI.md).
- Geçmiş karne arşivi, seri koruyan mola günü jetonu ve bildirimler yoktur.

## Başlıca dosyalar

- `domain/calisma/{AktifSureOlcer,SureOlcumu,YerelTakvimSaati,OdakOturumu}.kt`.
- `domain/gelisim/KisiselProfil.kt`, `data/gelisim/{ProfilHizmeti,OlcumSerilestirme}.kt`.
- `data/local/{entity/TekrarOlcumEntity,dao/TekrarOlcumDao,YanlisDefteriMigrations}.kt`.
- `data/repository/RoomSoruRepository.kt`, `data/prefs/OdakOturumuDeposu.kt`.
- `data/export/YerelYedeklemeHizmeti.kt`, `ui/calisma/{OdakOturumuEkrani,OdakOlcumDurumu}.kt`.
- `ui/seviye/SureProfiliKarti.kt`, `docs/OLCUM_VE_PROFILLER.md`, `VERI_MODELI.md`, `YEDEK_FORMATI.md`.
