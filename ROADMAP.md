# ROADMAP — Yanlış Defteri

Durum işaretleri: `[ ]` yapılmadı · `[~]` kod mevcut; bu sürümün CI/Release ve/veya cihaz kabulü bekliyor (madde metnini izle) · `[x]` tamamlandı · `[!]` engellendi

Öncelik: veri güvenliği → görsel işlemleri → günlük tekrar akışı → arama/filtreler → bildirimler → ek özellikler.

---

## Gelişim planı — P0–P5 uygulandı; P6 kapıları (2026-10-18)

Ayrıntılı görev/dosya/bağımlılık/test planı: [Gelişim ve Öğrenme Planı](docs/GELISIM_VE_OGRENME_PLANI.md).
P0–P5 kod/otomatik kapıları geçti; P6 son yayın CI `39144684877`: Debug298/0, Release258/0 + APK/imza/sertifika geçti. Fiziksel kabul açık. [Güncel kanıt](docs/P4_P5_P6_UYGULAMA.md).

- [~] **P0–P1 uygulandı:** canlı rozet/snapshot, dayanıklı gözlem ve commit sonrası devam temizliği; gerçek verili yeni İlerleme, ortak hareket politikası ve debug Preview. CI `39131225744`: **157/0**; cihaz kabulü bekliyor.
- [~] **P2 uygulandı:** monotonik/fazlı süre, kalite, atomik/idempotent kayıt, kimlikli kurtarma, çözüm açma bariyeri ve Room/ZIP v4.
- [~] **P3 uygulandı:** 60 gün; ayrı yardımsız/iş yükü referansı, soru başına medyan/çeyrek/MAD, yeterlilik/fallback, kayıt anı bağlamı; gerçek ders referansı UI’sı.
- [~] **P4 uygulandı:** ayrı açıklanabilir aday3; varsayılan gölge v2 tarihini korur. CI 240/0; etkin v3 fiziksel/gölge kapıları kapalı.
- [~] **P5 uygulandı:** gerçek kaynak/konu/ritim/dönem/iş yükü, uygun filtre/Odak eylemi ve kalıcı tek kutlama host’u. CI 274/0.
- [~] **P6:** birleşik Debug298/0 + Release258/0; iki host benchmark ve beş önizleme, APK/imza/sertifika doğrulandı. Fiziksel cihaz/TalkBack/SAF/frame-p95 bekliyor.

## Son yayımlanan — v1.5.0 / code18

- [x] P4/P5 + P6 otomatik kapıları: CI `39144684877` Debug298/0, Release258/0; release/imza/sertifika ve yayımlanan asset digest doğrulandı.
- [x] Kaynak/tag `9c3c51f`, APK 10.791.135 bayt. Etkin v3 kapalı, Gölge v2 tarihini uygular.
- [ ] P6.D1–D13 fiziksel/gölge kabulü; yalnız otomatik testle tamamlandı sayılmaz.

### Önceki v1.4.1 / P2–P3 teslimi

- [x] **v1.4.1/code 17:** CI `39136991000`, **221 test / 0 başarısız**, Release/imza/sertifika ve yayımlanmış APK digest eşleşmesi başarılı. Önceki 157-test tabanı + 64 yeni test.
- [x] P2/P3 ana paket v1.4.0 CI `39135928734` ile 217/0; son paket ek kurtarma/çözüm bariyeri güvencelerini içerir.
- [ ] P2/P3 gerçek cihaz: güncelleme, süreç öldürme/reboot, kilit/arka plan, yakınlaştırma, düşük depolama ve büyük veri performansı.
- [ ] Etkin v3’ün gerçek cihaz ve gerçek gölge kullanımı kabulü; kodlanmış olması bu kapıları açmaz.

## Korunan önceki kapsam — v1.3.0

- [x] **v1.3.0/code 15 yayımlandı:** CI `39131225744`, 157 test, Release/imza/sertifika başarılı.
- [x] GitHub APK digest'i CI ile aynı; sürüm notları ve küçük native Compose önizlemeleri kaydedildi.
- [ ] P0/P1 fiziksel cihaz / TalkBack / gerçek performans kabulü. Örnek verili görüntüler bunu tamamlamaz.

## Korunan önceki kapsam — v1.2.0

Aşağıdaki `[~]` özelliklerin kodu/otomatik testleri yayımlandı; fiziksel cihaz kabulü bekliyor.

- [~] 30 rozet/5 sır, koşul ve ilerleme açıklamaları, tam ekran kalıcı kutlama kuyruğu.
- [~] 15 rütbe, 6 yeni saydam amblem, rütbe galerisi ve 200-seviye sınırının kaldırılması.
- [~] Sistem gezinme çubuğu inset düzeltmesi; üst sağ Ayarlar kısayollarının kaldırılması.
- [~] LGS/TYT/AYT/KPSS/Tümü profilleri ve Ayarlar'da bütün derslere erişen konu silme.
- [~] Odak Stüdyosu, akıllı sıralama, adaptif tekrar v2, sona bırak/mola, kalıcı oturum kurtarma.
- [x] **v1.2.0 yayımlandı**: CI `39067624050`, 130 JVM/Room/Robolectric/Compose testi, Release ve imza doğrulaması başarılı.
- [ ] Üç düğmeli/jest gezinme, büyük yazı, yatay ekran, gizli rozet kutlaması ve odak akışı cihaz kabulü.
- [ ] Geçmiş karneler arşivi, mola günü jetonu, bildirimler. Bu sürümde tamamlandı diye işaretlenmez.

**Tarihsel v1.2 kararı:** v2 algoritma durumu mevcut geçmişten türetildiği için o sürümde v4 gerekmiyordu.
P2 süreleri geçmişten türetilemediğinden v1.4.x ile Room/ZIP v4 uygulandı. Detay `docs/ODAK_STUDYOSU.md`.

Aşağıdaki Aşama 0–4 kayıtları önceki sürümlerin tarihsel durumudur.

## Aşama 0 — Konuşma ve plan ✅

- [x] Gereksinimler, teknoloji ve ürün kararları netleştirildi.
- [x] Dış inceleme önerileri gerekçeleriyle değerlendirildi.
- [x] Aşama 1'in cihaz güncelleme kabulü kullanıcı tarafından bildirildi.

## Aşama 1 — İlk kurulabilir APK ✅

- [x] Push ile Android CI, `contents:write`, rapor commit'i ve Release akışı doğrulandı.
- [x] v1.0.0 (versionCode 1) ve v1.0.1 (versionCode 2) imzalı APK/Release üretildi; sertifika parmak izi doğrulandı.
- [x] Kullanıcı v1.0.0 → v1.0.1 uygulamasını kaldırmadan güncelledi, açılışı ve 7 ders kartını doğruladı.

## Aşama 2 — Yerel soru arşivi [~]

> v1.0.8 CI run `39044777998` (42 test) ve v1.0.9 CI run `39048703584` (44 test) geçti; Release yayımlandı. Filtreli çalışma akışı repository testlerinden geçti; `+ Konu`, v1.0.6 formu, kamera/galeri ve katalog/migration akışlarının fiziksel cihaz kabulü ile fotoğraf okunabilirliği hâlâ kullanıcı testini bekliyor. v1.0.9 düzeltmeleri CI'da doğrulandı, cihaz kabulü bekliyor.

- [x] Room şema v3 JSON üretildi; v1→v2→v3 ve doğrudan v2→v3 veri koruyan migration testleri CI run `39037429421` içinde geçti. Yıkıcı migration fallback'i yok.
- [x] Yedi lise dersi; kullanıcı arayüzünde ders listesi sabit, ders yönetimi/arsiv akışı ana ekrandan çıkarıldı.
- [x] Konu ekleme/düzenleme/arşivleme/geri getirme; arşiv ilişkiyi silmiyor.
- [x] Soru ekleme/düzenleme/silme, ders/konu/konusuz/plan filtreleri, ayrıntı ve görsellerin ilişkilendirilmesi.
- [~] v1.0.6 soru formu hiyerarşi/kartları, seçim kontrolleri ve sabit kaydetme alanıyla yenilendi; fiziksel kullanılabilirlik kabulü bekliyor.
- [~] v1.0.7 formdaki `+ Konu` yeni konuyu seçili derse ekler ve seçer; yedi ders için repository testi eski konu listesinin korunduğunu doğruladı (CI run `39041684003`); cihaz kabulü bekliyor.
- [~] Dersler arası ortak kaynak kataloğu, soruya özel sayfa/test/soru konumu ve hazır/özel nedenlerden tek ana yanlış nedeni; soru notu UI alanı kaldırılırken eski değerler korunuyor. v1.0.5 CI geçti; fiziksel cihaz kabulü bekliyor.
- [x] Tekrar/plan kuralları ve birim testleri (soru arşivinden bağımsız `TekrarKurali`).
- [x] Kamera ve sistem fotoğraf seçicisi; soru/çözüm için çoklu görsel; staging→kalıcı alan, SHA-256 ve atomik dosya yazımı.
- [x] Görsel optimizasyonu: uzun kenar en çok ~3000 px, JPEG kalite 90; metadata ve tutarlılık kontrolü.
- [x] Bakım ekranında denetim, yaş ve veritabanı referansı yeniden kontrol edilerek onaylı yetim temizliği.
- [x] Dışa aktarma akışı tam ZIP yedekleme hizmetine yükseltildi; geri uyumlu eski API adı korunuyor.
- [x] CI run `39030667492`: toplam 32 birim/Robolectric testi geçti; v1.0.3 Release derlemesi, imza ve sertifika parmak izi başarılı.
- [~] Kullanıcı v1.0.2 için “3. turu denemedim, onun dışında hepsi geçti” bildirdi; bunun checklist'teki 1/2/4 geçti, 3 (yedek/geri yükleme) denenmedi anlamına geldiği yorumu henüz teyit edilmedi. Bkz. `docs/TEST_KONTROL_LISTESI.md`.
- [x] Kullanıcı v1.0.3'teki güncelleme/veri koruma, Bugün/gecikenler, soru ekleme, yan menü ve tekrar sonucu cihaz adımlarının tümünü geçti bildirdi (2026-10-17); checklist'teki adım kapsamını okuyun.
- [ ] Uçak modu, izin/iptal, düzenleme/silme ve fotoğraf görüntüleme gibi ayrı Aşama 2 cihaz kontrolleri.
- [ ] 30–50 gerçek fotoğrafta küçük formül, kök ve grafik etiketlerinin okunabilirliğini doğrula.

## Aşama 3 — Tam yedek ve geri yükleme [~]

> ZIP üretimi, doğrulaması, geri yüklemesi ve CI testleri hazır. Gerçek SAF/Drive, temiz kurulum ve başka cihaz testi yapılmadı.

- [x] Sürüm kontrollü ZIP v3: `manifest.json`, kanonik `data.json`, 6 CSV (katalog seçenekleri dâhil) ve soru/çözüm JPEG'leri; eski v2 ZIP'leri içe alınır. v1.0.5 CI run `39037429421` testleri geçti.
- [x] Dosya listesi, uzunluk, SHA-256, ilişki, görsel metadata, şema/format sürümü, yol kaçışı ve ZIP büyüme sınırları doğrulanıyor.
- [x] SAF `CreateDocument` / `OpenDocument` akışları; Drive/bulut yüklemesinin ayrıca doğrulanması gerektiği kullanıcıya bildiriliyor.
- [x] İçe aktarmadan önce mevcut/yedek soru-görsel sayısı önizlemesi ve açık tam-değiştirme onayı.
- [x] Geri yükleme öncesi doğrulanmış güvenlik ZIP'i; görseller yeni kalıcı adlarla kopyalanıyor; Room değişimi transaction içinde.
- [x] Room işareti + app-private günlükle kesinti sonrası commit/rollback kurtarması; v1→v2→v3 migration zinciri.
- [x] CI run `39037429421` (36 test) katalog/kaynak konumu tam geri yüklemesini ve v2 ZIP içe aktarmasını doğruladı. Önceki run `39033698686` checksum/path traversal/tam geri yükleme/kurtarma testlerini de geçmişti.
- [ ] Gerçek cihazda Downloads/Drive'a kaydetme, Drive yüklemesini ayrıca doğrulama.
- [ ] Temiz kurulumda/başka cihazda soru, görsel, tekrar geçmişi ve planın tam geri yüklenmesi.
- [ ] Cihazda iptal, bozuk yedek ve geri yükleme sırasında uygulama kapatma senaryoları.

## Aşama 4 — Tekrar deneyimi ve günlük akış [~]

> Bugün/menü/sonuç akışı v1.0.3'te yayımlandı ve kullanıcı tarafından cihazda geçti bildirildi. Tekrar geçmişi v1.0.4, katalog/ZIP özellikleri v1.0.5, form yenilemesi v1.0.6, per-course `+ Konu` v1.0.7, filtreli çalışma v1.0.8 ve düzeltmeler v1.0.9 CI'da doğrulandı; v1.0.4 history UI ve v1.0.5–v1.0.9 cihaz kabulü bekliyor.

- [x] Tekrar aralığı kuralı ve repository seviyesinde plan/tekrar davranışları CI'da test edildi.
- [x] Bugün/gecikmiş soruları dersler arası listeleyen ana ekran, belirgin “Soru ekle” eylemi ve ikincil alanlar için yan menü.
- [x] Çözümün açıldığını ve tekrar sonucunu kaydetme akışı; CI'da derleme ve birim testleri geçti.
- [x] v1.0.3'te mevcut uygulamayı koruyarak güncelleme, Bugün/menü ve tekrar kaydı adımlarının cihazda geçtiği kullanıcı tarafından bildirildi.
- [x] Tekrar geçmişi: soru ayrıntısında planlanan/gerçekleşen gün, sonuç, çözüm açma ve aralık; CI run `39033698686` 33 testle geçti.
- [~] v1.0.8 filtreli çalışma: konu/kaynak/ana neden filtreleri, en uzun süredir çalışılmayandan sıralama ve sıralı oturum; CI run `39044777998` 42 testle geçti, cihaz kabulü bekliyor.
- [~] v1.0.9 düzeltmeleri: döndürmede durum koruma, oturumdan çıkış onayı, sayı alanı, görsel belleği, arşivli konu çakışması, yedek yazımı ve CSV formül nötrleştirmesi; CI run `39048703584` 44 testle geçti, cihaz kabulü bekliyor.
- [x] v1.0.10: sonuç karnesi, animasyon/rozet, günlük hedef (sabit 10), seri ve haftalık özet — yayımlandı; CI run `39051284547`, 72 test. Cihaz kabulü bekliyor.
- [x] v1.1.0/v1.1.1: alt menü (yan menü kaldırıldı), XP/seviye/rütbe, rozet galerisi, Ayarlar ekranı (hedef seçimi + yedek + bakım + katalog), konu/kaynak/neden silme — yayımlandı; CI run `39053319900`, 86 test. Cihaz kabulü bekliyor.
- [ ] v1.0.11: tekrar algoritması v2 (zorlandım ilerlemesi, kolaylık, gecikme cezası, zor soru) + Room v4 migration + yedek biçimi v4.
- [ ] v1.0.12: pekişme halkaları, 7 günlük tahmin, günlük kota.
- [ ] v1.0.4'te tekrar geçmişinin gerçek cihazda okunaklı/güncel gösterildiğinin kabulü.
- [ ] Elle tarih atama ve kullanıcıya görünür plan akışları.
- [ ] Saat dilimi ve uzun dönemli gerçek kullanım kabul testleri.

## Aşama 5 — Kullanılabilirlik ve sağlamlaştırma

- [~] P6: 1.000 soru/20.000 tekrar Debug/Release native host ölçümü ve EXPLAIN tamam. Gerçek cihaz frame-p95/ilk-sıcak açılış kabulü açık; host sonucu telefon performansı değildir.
- [ ] Depolama göstergesi, düşük depolama ve yarıda kesilen işlemler için cihaz testleri.
- [ ] Önizleme stratejisi, kırpma ekranı, erişilebilirlik ve tema iyileştirmeleri.

## Aşama 6 — İsteğe bağlı geliştirmeler

- [ ] Doğrudan Drive bağlantısı (SAF'ten ayrı; yalnızca gerekirse).
- [ ] Otomatik/kolay yedekleme, bildirimler, mini deneme, PDF, istatistik.
- [ ] Şifreleme ancak kullanıcı kararı ve olgun bir standartla.

## Kapsam dışı

Sosyal özellikler, çoklu cihaz canlı senkronizasyonu, iOS, web paneli, reklam, abonelik, uzak
veritabanı ve OCR/yapay zekâ ile otomatik çözüm. Ayrıntı: [PROJECT_BRIEF.md](PROJECT_BRIEF.md) §7.
