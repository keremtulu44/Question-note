# ROADMAP — Yanlış Defteri

Durum işaretleri: `[ ]` yapılmadı · `[~]` kod/CI hazır, kullanıcı kabulü bekliyor · `[x]` tamamlandı · `[!]` engellendi

Öncelik: veri güvenliği → görsel işlemleri → günlük tekrar akışı → arama/filtreler → bildirimler → ek özellikler.

---

## Aşama 0 — Konuşma ve plan ✅

- [x] Gereksinimler, teknoloji ve ürün kararları netleştirildi.
- [x] Dış inceleme önerileri gerekçeleriyle değerlendirildi.
- [x] Aşama 1'in cihaz güncelleme kabulü kullanıcı tarafından bildirildi.

## Aşama 1 — İlk kurulabilir APK ✅

- [x] Push ile Android CI, `contents:write`, rapor commit'i ve Release akışı doğrulandı.
- [x] v1.0.0 (versionCode 1) ve v1.0.1 (versionCode 2) imzalı APK/Release üretildi; sertifika parmak izi doğrulandı.
- [x] Kullanıcı v1.0.0 → v1.0.1 uygulamasını kaldırmadan güncelledi, açılışı ve 7 ders kartını doğruladı.

## Aşama 2 — Yerel soru arşivi [~]

> Kod ve CI kapsamı hazır. Kamera/galeri, kalıcı veri, gerçek fotoğraf okunabilirliği ve cihaz kabulü kullanıcı testini bekliyor.

- [x] Room şema v2; `exportSchema`; yabancı anahtarlar ve v1→v2 veri koruyan migration + Robolectric testi. Yıkıcı migration fallback'i yok.
- [x] Yedi seed dersi; ders ekleme/ad-sıra düzenleme/arşivleme/geri getirme ve dashboard Flow'u.
- [x] Konu ekleme/düzenleme/arşivleme/geri getirme; arşiv ilişkiyi silmiyor.
- [x] Soru ekleme/düzenleme/silme, ders/konu/konusuz/plan filtreleri, ayrıntı ve görsellerin ilişkilendirilmesi.
- [x] Tekrar/plan kuralları ve birim testleri (soru arşivinden bağımsız `TekrarKurali`).
- [x] Kamera ve sistem fotoğraf seçicisi; soru/çözüm için çoklu görsel; staging→kalıcı alan, SHA-256 ve atomik dosya yazımı.
- [x] Görsel optimizasyonu: uzun kenar en çok ~3000 px, JPEG kalite 90; metadata ve tutarlılık kontrolü.
- [x] Bakım ekranında denetim, yaş ve veritabanı referansı yeniden kontrol edilerek onaylı yetim temizliği.
- [x] Dışa aktarma akışı tam ZIP yedekleme hizmetine yükseltildi; geri uyumlu eski API adı korunuyor.
- [x] CI run `39027221270`: toplam 31 birim/Robolectric testi geçti; Release derlemesi, imza ve sertifika parmak izi başarılı; v1.0.2 test Release'i yayımlandı.
- [ ] Kullanıcı cihazında uçak modunda kayıt, yeniden başlatma, izin/iptal, düzenleme/silme ve fotoğraf görüntüleme kabulü.
- [ ] 30–50 gerçek fotoğrafta küçük formül, kök ve grafik etiketlerinin okunabilirliği.

## Aşama 3 — Tam yedek ve geri yükleme [~]

> ZIP üretimi, doğrulaması, geri yüklemesi ve CI testleri hazır. Gerçek SAF/Drive, temiz kurulum ve başka cihaz testi yapılmadı.

- [x] Sürüm kontrollü ZIP: `manifest.json`, kanonik `data.json`, 5 CSV ve soru/çözüm JPEG'leri.
- [x] Dosya listesi, uzunluk, SHA-256, ilişki, görsel metadata, şema/format sürümü, yol kaçışı ve ZIP büyüme sınırları doğrulanıyor.
- [x] SAF `CreateDocument` / `OpenDocument` akışları; Drive/bulut yüklemesinin ayrıca doğrulanması gerektiği kullanıcıya bildiriliyor.
- [x] İçe aktarmadan önce mevcut/yedek soru-görsel sayısı önizlemesi ve açık tam-değiştirme onayı.
- [x] Geri yükleme öncesi doğrulanmış güvenlik ZIP'i; görseller yeni kalıcı adlarla kopyalanıyor; Room değişimi transaction içinde.
- [x] Room işareti + app-private günlükle kesinti sonrası commit/rollback kurtarması; v1→v2 migration ve test.
- [x] CI'da checksum bozulması, desteklenmeyen format, path traversal, tam geri yükleme, güvenlik kopyası ve commit/rollback kurtarma testleri geçti (run `39027221270`).
- [ ] Gerçek cihazda Downloads/Drive'a kaydetme, Drive yüklemesini ayrıca doğrulama.
- [ ] Temiz kurulumda/başka cihazda soru, görsel, tekrar geçmişi ve planın tam geri yüklenmesi.
- [ ] Cihazda iptal, bozuk yedek ve geri yükleme sırasında uygulama kapatma senaryoları.

## Aşama 4 — Tekrar deneyimi ve günlük akış

- [x] Tekrar aralığı kuralı ve repository seviyesinde plan/tekrar davranışları Aşama 2'de yazıldı ve test edildi.
- [ ] Bugün/gecikmiş tekrar ekranı ve tekrar geçmişi arayüzü.
- [ ] Elle tarih atama ve kullanıcıya görünür plan akışları.
- [ ] Saat dilimi ve gerçek kullanım kabul testleri.

## Aşama 5 — Kullanılabilirlik ve sağlamlaştırma

- [ ] Büyük arşiv performansını ölçme; ölçmeden performans iddiası yok.
- [ ] Depolama göstergesi, düşük depolama ve yarıda kesilen işlemler için cihaz testleri.
- [ ] Önizleme stratejisi, kırpma ekranı, erişilebilirlik ve tema iyileştirmeleri.

## Aşama 6 — İsteğe bağlı geliştirmeler

- [ ] Doğrudan Drive bağlantısı (SAF'ten ayrı; yalnızca gerekirse).
- [ ] Otomatik/kolay yedekleme, bildirimler, mini deneme, PDF, istatistik.
- [ ] Şifreleme ancak kullanıcı kararı ve olgun bir standartla.

## Kapsam dışı

Sosyal özellikler, çoklu cihaz canlı senkronizasyonu, iOS, web paneli, reklam, abonelik, uzak
veritabanı ve OCR/yapay zekâ ile otomatik çözüm. Ayrıntı: [PROJECT_BRIEF.md](PROJECT_BRIEF.md) §7.
