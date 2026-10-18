# HANDOFF — Yanlış Defteri

Son güncelleme: **2026-10-18**. Önce bu belge, `PROJECT_BRIEF.md`, `docs/DERLEME_RAPORU.md` okunur.

## Güncel durum

- **P0 ve P1 uygulandı; v1.3.0 (versionCode 15) yayımlandı.**
- [CI `39131225744`](https://github.com/keremtulu44/Question-note/actions/runs/39131225744):
  **157 test, 0 başarısız/hata/atlanan**. Release derlemesi, apksigner ve beklenen sertifika eşleşmesi başarılı.
- Derlenen commit: `1df9c7c4754490b7d8686235e64c9e7c65aebd2d`; CI rapor botu: `b12bec7`.
- [Release v1.3.0](https://github.com/keremtulu44/Question-note/releases/tag/v1.3.0) ·
  [APK indir](https://github.com/keremtulu44/Question-note/releases/download/v1.3.0/yanlis-defteri-1.3.0.apk).
  APK **10.574.047 bayt**; SHA-256 `a232e7759e8f98398449295f32e524565d9d917bb5e3644881908a8f85a96b8f`.
  GitHub asset `653550121` digest'i CI ile ayrıca karşılaştırıldı ve aynı.
- **Dal:** `arena/59c6570d-question-note`. Merge/PR yapılmaz; main'e dokunulmaz. Her adım için
  onay istenmez. Bash çağrılarına timeout konur; uzun `gh run watch` yerine aralıklı,
  sınırlı `gh run view --json status,conclusion` çağrıları kullanılır.
- Yerelde Java/Android SDK yok. Gerçek derleme/test CI'da; statik kontrol onun yerine geçmez.
  CI botu aynı dala rapor/önizleme commit eder; push öncesi fetch/rebase gerekir.

## P0: güvenilirlik

1. Karne rozet toplamı, eski bir defalık snapshot yerine `RozetDeposu.sahipIzle` üzerinden canlıdır.
2. `dayanikliIzle`: soğuk kaynak fabrikası, 1/2/3 saniye beklemeli en fazla üç yeniden deneme;
   tükenirse snackbar yenileme eylemi veya Bugün/İlerleme'ye yeniden giriş yeni gözlem kurar.
   CancellationException yayılır; downstream hataları depo hatası diye yeniden denenmez.
3. Geri yükleme başlamadan devam noktası **silinmez**. Doğrulanmış DB commit'i sonrasında,
   journal/Room işareti kaldırılmadan kalıcı olarak temizlenir. Aynı soru ID'lerinin tekrar
   kullanılması eski oturumu geçerli yapmaz. Temizlik hatasında kurtarma işaretleri korunur.
4. Startup recovery de aynı temizlemeyi uygular. Başarısız precommit/rollback eski oturumu korur;
   UI belleğindeki devam noktası işlem sonunda kalıcı oturum kimliğiyle uzlaştırılır.
5. İlerleme özeti tek Room transaction'ında; galeri ve XP için aynı sahiplik/hedef/gün snapshot'ı.
   Günlük hedef değişikliği başka tekrar gerekmeden XP/rozet gözlemlerini tetikler.

## P1: görsel sistem

- Petrol/lacivert-bakır rütbe odağı; rütbe → seviye → XP → sonraki rütbe. Mevcut 15 amblem ve
  30 rozet çizimi korunur. Normal ölçekte 2×2, büyük yazıda tek sütun sayılar/rozetler.
- Açılır XP dökümü, rozet ve rütbe koleksiyonu. Mevcut ders pekişmesi ve 7 günlük planlı soru
  adedi gerçek kayıtlardan gelir; gecikmiş soruların grafiğe dahil olmadığı açıklanır.
- `SeviyeDurumuUi` / stateless `SeviyeSayfasi`: yükleniyor, hata/yenileme, tekrar verisi yetersiz,
  hazır. Boş arşivden soru ekleme; diğer durumda Bugün'e dönüş gerçek route'ları kullanır.
- Ekran açılışı sahte XP kazancı değildir. Açık ekrandaki gerçek değişiklik 500 ms'lik sonlu
  animasyon kullanır. Hareket kapalıyken animasyon State'i yerine doğrudan gerçek değer çizilir.
- Ortak `HareketPolitikasi`, sistem animator ölçeğini ContentObserver ile canlı izler; Seviye,
  Karne, kutlama ve Odak otomatik kaydırmasında kullanılır. Yeni hareket tercihi dosyası yoktur.
- Yalnız `src/debug` içinde altı Studio Preview ve örnek veri vardır. Beş native Robolectric
  çizimi [önizleme belgesinde](docs/ui/p1/README.md). Bunlar fiziksel cihaz ekran görüntüsü
  veya piksel-golden karşılaştırması değildir; gerçek Compose bileşenlerinin sentetik çıktısıdır.

## Korunan temel davranış

- Offline, tek kullanıcı; hesap/sunucu yok. Alt menü Bugün/Dersler/Filtreli/İlerleme/Ayarlar;
  yan menü ve sağ üst Ayarlar kısayolları yoktur.
- LGS/TYT/AYT/KPSS/Tümü ve bütün dersleri kapsayan konu yönetimi/silme Ayarlar'dadır.
  Kullanılan katalog satırları silinmez. Profil eski soru/konu/tekrarları silmez.
- Bağımsız yalnız **RAHAT + çözüm açılmamış**. Zorlandım yarım, Çözemedim çözemedi;
  çözüm açıldıysa destekli. Aynı soru/günde ikinci tekrar engellenir.
- XP katsayıları, seviye/rütbe eşikleri ve 30 rozet kuralı aynı. Kazanım idempotent; kutlama
  kapatılması sahipliği silmez; kazanılmamış gizli ad/çizim/erişilebilirlik metni maskelenir.
- Odak: tek/sıralı soru, akıllı/en eski sıralama, 5/10/20, mola, sona bırak, ayrı değerlendirme/
  kaydetme, kapanma sonrası devam ve karne. **Sayaç hâlâ delay tabanlı; kalıcı süre verisi yok.**
- **Room v3 / ZIP v3 / tekrar algoritması v2 aynı.** v2 ZIP import ve migration zinciri korunur.
  Rozet/hedef/profil/devam noktası cihaz-yerel prefs'tir, ZIP'e dahil değildir.

## Test kanıtı ve açık kapılar

- v1.2.0'ın 130-test tabanı korundu; 27 yeni test: 5 akış, 3 özet/hedef tutarlılığı,
  3 restore, 11 sunum/hareket/karne ve 5 native önizleme. Mevcut recovery testi ayrıca güçlendirildi.
- İlk CI `39130524882` 155/1, ikinci `39130869814` 156/1 idi; aynı yeni manuel-saatli XP testi
  yeniden çizimden önce eski semantics okuyordu. Hareket kapalı sunum doğrudan değerle yapıldı;
  test Compose redraw ile eşitlendi, sanal zaman sınırı korundu. Normal ilk gösterim ve gerçek
  XP değişiminin sonlu hareketi için ek testler eklendi. Son CI **157/0**.
- `tools/statik_kontrol.py`: 592 string/resource, 15 amblem × 5 yoğunluk, 0 hata. Kotlin
  sözdizimi/TOML/YAML ve diff kontrolü geçti; bunlar derleme yerine sunulmaz.
- **Gerçek cihaz kabulü bekler:** S24 FE üzerinde kaldırmadan güncelleme, 3-düğmeli/jest nav,
  yatay ekran, %200 yazı/TalkBack, kutlama, arka plan/süreç ölümü, restore kesintisi ve performans.
  Native çizimler veya sanal animasyon zamanı gerçek telefon performans ölçümü değildir.
- **P2–P6 uygulanmadı:** monotonik/fazlı süre ölçümü, Room/ZIP v4, kişisel profiller, gölge v3,
  kaynak/konu analizleri ve yeni öğrenme önerileri. Tek devam planı:
  [GELISIM_VE_OGRENME_PLANI.md](docs/GELISIM_VE_OGRENME_PLANI.md).
- Geçmiş karneler arşivi, seri koruyan mola günü jetonu ve bildirimler hâlâ yoktur.

## Başlıca dosyalar

- `data/ilerleme/DayanikliIzleme.kt`, `RozetHizmeti.kt`, `data/seviye/SeviyeHizmeti.kt`.
- `data/export/YerelYedeklemeHizmeti.kt`, `AppContainer.kt`, `ui/YanlisDefteriUygulamasi.kt`.
- `ui/tema/{HareketPolitikasi,Renk,Tipografi,Tema}.kt`.
- `ui/seviye/{SeviyeEkrani,IlerlemeIcerigi,RozetKoleksiyonu,RutbeYolculugu}.kt`.
- `ui/karne/{KarneEkrani,RozetKutlamasi}.kt`, debug `SeviyeOnizlemeleri.kt`.
- `docs/SEVIYE_SISTEMI.md`, `ODAK_STUDYOSU.md`, `YEDEK_FORMATI.md`, `TEST_KONTROL_LISTESI.md`.
