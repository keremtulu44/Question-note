# HANDOFF — Yanlış Defteri

**Son güncelleme: 2026-10-18.** Önce bu belge, `docs/DERLEME_RAPORU.md` ve
[P4–P6 sözleşme/kanıt kaydı](docs/P4_P5_P6_UYGULAMA.md) okunur.

## Güncel çalışma / teslim

- Kullanıcı: **“P4 P5 ve P6 uygula dikkatli adım adım çalış.”** P0–P3 önceki teslimde tamamlandı.
- P4 CI `39139377311` / `35896fe`: **240/0/0/0**. P5 CI `39140443680` / `522b009`: **274/0/0/0**.
  Her ikisinde release derlemesi/imza/sertifika başarılı; ara yayın bilinçli kapalıydı.
- **P6 birleşik CI `39143916007` / `478b86c`: Debug 298/0/0/0, Release 258/0/0/0**, release APK
  derlemesi/imza/sertifika başarılı. İki varyant benchmark ve beş native PNG gerçekten üretildi; test cache/up-to-date kapalı.
- **1.5.0 / code18** son yayın koşusu açılıyor. **Etkin v3 kapalı**; Gölge varsayılanı v2 tarihini uygular.
  Ara APK yayımlanmadı; iki varyantın birleşik yeşil sonucundan sonra `PUBLISH_RELEASE=true` açılıyor.
- Son bağımsız doğrulanmış yayımlanan sürüm **v1.4.1 / code17**, CI `39136991000`, **221/0/0/0**;
  kaynak `2fbe6255646029d040cfe4e2f2d600340e0206a7`.
  [APK](https://github.com/keremtulu44/Question-note/releases/download/v1.4.1/yanlis-defteri-1.4.1.apk),
  **10.651.871 bayt**, asset `653705565`, SHA-256 `9634c5bb59ea574f27c101a8332ed2689719ddf95e31a5a102b6165e48180057`.
  CI ile GitHub asset digest ayrıca eşleştirildi.

## Çalışma sınırları

- Yalnız **`arena/59c6570d-question-note`**. Merge/PR yok, main'e dokunulmaz. Adım adım onay istenmez.
- Bash timeout'lu; uzun `gh run watch` yok. Sınırlı durum sorguları; beklerken ~25 s aralık.
- Java/Android SDK yerelde yok; gerçek Kotlin/Android derleme/test CI'da. Parser/statik başarı derleme değildir.
- CI botu aynı dala rapor/şema/küçük PNG yazar. `git fetch` + koruyan `rebase --autostash`; merge/reset yok.
- `testDebug`: tüm ortak testler + debug-only Compose; `testRelease`: ortak testler. **Sayılar toplanmaz.**
- Gerçek cihaz/SAF/TalkBack/süreç ölümü/frame-p95 henüz yapılmadı. Native sentetik çizim/benchmark telefon kabulü değildir.

## Korunan P2/P3 temeli

- `AktifSureOlcer`: elapsedRealtime segment farkları, UI tick'i yalnız gösterim. Deneme/inceleme ayrı;
  çözüm ilk açılınca faz geri dönmez. Değerlendirme dondurur; loading/mola/arka plan/kaydetme/zoom hariç.
- YOK/TAM/KISMI/GECERSIZ; eski null ≠ 0; faz başına 24 saat sınırı. Tam ekran/eksik görsel/kurtarma kısmi.
  Ölçüm hatası geçerli sonuç/XP kaydını engellemez.
- Checkpoint2: kimlik/birikim/seçim/faz; monotonik başlangıç yazılmaz. 1 s gösterim/5 s aktif IO;
  revizyon ve sahiplik eski işin yeni oturumu diriltmesini önler. Çözüm görünmeden açma işareti kalıcıdır.
- Tekrar + ölçüm/bağlam/karar + plan tek transaction; deneme ID idempotensi ve gerçek same-day kontrolü.
  Önizleme gün/saat dilimi/politika/geçmiş/bağlam/profil değişince açık yenilenir.
- Commit–checkpoint kesintisi deneme/oturum kimliğiyle uzlaşır; ilk checkpoint gecikse/duvar saati geri gitse
  de tamamlanan tekrar tanınır. Eski kimliksiz kayıt zaman fallback'i sürer. Prefs SQLite transaction'ı değildir.
- Profil politikası1: 60 gün, mevcut deneme hariç, pozitif TAM. Yardımsız Rahat ile tüm-sonuç iş yükü ayrı.
  Aynı soru/gün tek, sonra soru başına medyan; medyan/çeyrek/MAD. Ders **20/10/5**, konu/kaynak **30/15/7**.
  Yalnız aynı ders fallback'i; az veride null. NFC/Türkçe harf/ASCII iç boşluk normalizasyonu korunur.
- Kayıt anı bağlamı değişmez; eski bilinmeyen bağlam uydurulmaz. Kalıcı profil/analitik cache yok.

## P4 — uygulanan korumalar

- Ayrı `UyarlanabilirTekrarKurali`; v2 temel, v3 aday. Gölge v2 uygular; Etkin için iki default-false kapı.
  Kapalı snapshot1/V2, Gölge/Etkin snapshot2. Prefs kapı aşamaz; test fixture'ı açık kapıyı ayrıca kurar.
- Yakın en fazla 5 ayrı gün/90 gün; günün son kaydı, iki son bağımsız başarı güçlüğü kaldırır.
- Düzeltme yalnız güncel yardımsız Rahat + TAM/pozitif + ≥2 yakın güçlük günü + yeterli finite referans:
  süre >1,5×medyan **ve** >medyan+max(3MAD,1,5IQR). Hız bonusu/yavaşlık-tek-başına ceza yok.
- Çözüm/fail 1 gün. Gecikme/Zorlandım düzeltmesiyle birikmez. `floor(base/10)` indirim; 1–180 gün.
- Açık geçmiş1/2/3 yönlendirmesi; v3 ease okunur. Rollback yalnız yeni kayıtları v2 yapar; geçmiş yeniden yazılmaz.

## P5 — uygulanan analitik / eylem / ödül

- Gerçek farklı soru/tekrar/payda/kapsama; geçersiz süre geçerli sonucu paydadan çıkarmaz. Tarihsel grup
  adları, eksik ve bilinmeyen bağlam ayrı; kitap/öğrenci kalite sıralaması yok.
- Gerçek 7 gün ritim, bugün eksik. Tamamlanmış −14…−8 ve −7…−1; aynı soru/bağlamın son kaydı eşleştirilir.
  ≥10 ortak soru ve son değerlendirmelerde her pencerede ≥3 gün; gerçek gün sayısı görünür. Öğrenme artışı hükmü yok.
- Seçili profil için gerçek uygun plan; gecikenler bugün. Tüm sonuçlardan TAM Q1–Q3 iş yükü;
  ≥%80 kapsamada yaklaşık toplam, altında yalnız kapsananların ara toplamı; sıfır kapsamada dakika yok.
- Vade → uygun yakın güçlük grubu → isteğe bağlı küçük tur. Görünür ders/vade/kaynak filtresi, fresh start/save
  kontrolü; askı/ders arşivi/profil/same-day sınırı aşılmaz. Hedef otomatik artmaz.
- Tüm arşiv XP/analitiği profille gizlenmez. Grup listesi altılı sayfa, ayrıntılar kapalı; sonlu/reduced motion.
- `RutbeDeposu` kalıcı üst eşik/tek birleşik bekleyen olay; startup/hedef/restore sessiz taban.
  Sahiplik korunur, rütbe XP vermez. Badge-priority `KutlamaHost`: iki fullscreen üst üste gelmez.

## P6 — kod / gözlenen kanıt / açık kapılar

- UI testleri `src/testDebug`; Release'e fixture/test Activity taşınmadı. Katı JUnit özetleri ayrı.
  CI test cache/up-to-date kapalı; benchmark/PNG her koşunun kendi çıktısı olmalı.
- `MotorTaniHizmeti`: toplam ile zaman damgasına göre son ≤100 örnek ayrı; strict doğrulama, farklı aday/
  Gölge/Etkin/V2/bozuk sayısı. Count + 1–2 sınırlı Room parent + tek batched relation; N+1 yok.
- Analiz4 ince SELECT; karar JSON'u/görsel/not yok. SQLite EXPLAIN mevcut ders/gün/PK/same-day indekslerini
  gösterir. Ana iş parçacığı dışında saf hesap, DB snapshot tutarlılığı korunur; fiziksel maliyet ölçülmedi.
- Native SDK35 host benchmark: 1.000 soru/20.000 tekrar, 5 sıcak örnek, profil/tam özet maliyeti.
  `docs/PERFORMANS_DEBUG.md` kaynak SHA ve gerçek ham süreleri içerir. `docs/PERFORMANS_RELEASE.md` de gerçek ölçüm içerir; her rapor kendi kaynak SHA’sını taşır.
- Birleşik yeşil Debug 298/0: öneri→Filtreli→Odak→Karne→İlerleme; %200/48dp/reduced-motion, tek host, politika gate;
  broadcast, gece yarısı ve Activity resume; bounded tanı; ZIP snapshot2/active3 rollback/tamper;
  restore callback kesintisinde marker/kozmetik kuyruklar. Bunlar fiziksel süreç öldürme değildir.
- `docs/ui/p6`: 5 gerçek Compose/sentetik görüntü. Koyu fixture açıklamasının kontrastı, yeni panel yüzeyleri
  ve karşılaştırmadaki gerçek gün adetleri görüntü incelemesi sonrası düzeltildi.
- İlk P6 hataları ve düzeltmeleri saklanır: eksik test context'i; tek-sefer restore hatasının anında iyileşmesi;
  Room parent cursor tekrarını saymayan beklenti; Release görevinin başlayamaması (düzeltildi); UI testinde aynı adlı başlık/tab ve kaydırma/hazır olma sınırları. Koruma/test silinmedi.

## Sürüm / yedek / ürün değişmezleri

- **Room4**, additive 3→4; 1/2/3→4 ve şemalar korunur. **ZIP4**, v2/v3/v4 açık okuyucu.
  Ölçüm1, profil1, karar1/2, geçmiş algoritma1/2/3 ayrı kavramlar; yalnız sayı artırılarak uyumluluk iddia edilmez.
- ZIP hash/FK/null/kalite/sürüm/taşma/snapshot denetimi; eski satırlar değişmez. Rozet/rütbe/politika/hedef/
  sınav/checkpoint prefs'i dahil değil. Restore tam değişim + safety copy; senkronizasyon/birleştirme değil.
- Restore yalnız commit doğrulanınca checkpoint ve kozmetik kuyrukları marker kaldırılmadan temizler.
  Sahiplik kalır; anlık recovery de başarısızsa marker korunur. Precommit hata eski oturumu korur.
- Offline tek kullanıcı, hesap/sunucu yok. Alt menü5; Ayarlar'da profil/konu yönetimi/yedek.
- Bağımsız = Rahat + açılmamış çözüm; Zorlandım yarım, Çözemedim çözemedi; açık çözüm destekli.
- XP soru10/tekrar5/bağımsız+5/hedef25/rozet50, seri3/7/30=30/70/300. Hedef5/10/15/20/30; 15 rütbe/30 rozet/5 sır aynı.
- Geçmiş karne arşivi, mola günü jetonu ve bildirimler bu pakette yok.

## Sonraki somut adım

1. Son yayın koşusunun sonucunu takip et; Debug298/0 + Release258/0 + imza/sertifika yeniden geçmeden yayın oluşamaz.
2. Rapor botunu fetch/rebase ile al; Release asset boyut/digest/target SHA ilişkisini API'den bağımsız doğrula.
3. README/HANDOFF/BUILDING/ROADMAP/CHANGELOG ve test kaydındaki aday/yayın durumunu gerçek yayın kanıtıyla güncelle.
4. Fiziksel kabul bekleyenlerini açık bırak; etkin v3'ü cihaz/gölge kanıtı olmadan açma.
