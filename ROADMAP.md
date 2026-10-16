# ROADMAP — Yanlış Defteri

Durum işaretleri: `[ ]` yapılmadı · `[~]` devam ediyor · `[x]` tamam · `[!]` engellendi

Öncelik sırası: **veri güvenliği → görsel işlemleri → günlük tekrar akışı → arama/filtreler →
bildirimler → ek özellikler.**

---

## Aşama 0 — Konuşma ve plan ✅

- [x] Gereksinimlerin değerlendirilmesi
- [x] Teknoloji seçiminin gözden geçirilmesi (native Android / Kotlin / Compose / Room)
- [x] Açık soruların kullanıcıyla netleştirilmesi (görseller, ders/konu yönetimi, tekrar modeli,
      geri yükleme politikası, imzalama)
- [x] İlk sürüm kapsamının belirlenmesi
- [x] Dış inceleme (ikinci görüş) alınması ve önerilerin gerekçeyle değerlendirilmesi
- [x] Bekleme: kullanıcının Aşama 1 için açık onayı

**Çıktı:** Bu depodaki belgeler. Kod yazılmadı.

---

## Aşama 1 — İlk kurulabilir APK

**Amaç:** Derleme → indirme → kurma → güncelleme zincirini doğrulamak. Bu zincir kırıkken
uygulama geliştirmenin anlamı yok.

- [ ] GitHub Actions'ta yazma yetkisinin ve Secret ekleme imkânının doğrulanması
- [ ] Temel Android proje iskeleti (tek Gradle modülü, Kotlin + Compose, sürüm kataloğu)
- [ ] `applicationId`, veritabanı adı ve imza anahtarının ilk günden sabitlenmesi
- [ ] Sabit release imzalama anahtarının oluşturulması ve GitHub Secret olarak tanımlanması
- [ ] Elle tetiklenen derleme workflow'u
- [ ] APK'nın GitHub Release olarak yayımlanması; release notlarında sürüm, commit kimliği, APK SHA-256
- [ ] Temel tema (açık/koyu), ekran iskeleti, 7 ders kartı
- [ ] **Kullanıcı testi:** APK'yı indir, kur, aç, 7 ders kartını gör
- [ ] **Kullanıcı testi:** ikinci APK'yı (v2) **kaldırmadan** üzerine kur — veri/imza zinciri çalışıyor mu

**Çıkış kriteri:** Kullanıcı, telefonda kaldırma yapmadan yeni APK ile güncelleme yapabiliyor.

---

## Aşama 2 — Yerel soru arşivi

> Veri yaşam döngüsü baştan doğru kurulur: yedekleme sonradan "eklenen bir düğme" değil, veri
> katmanının sınırlarından biri olarak tasarlanır.

- [ ] Paket ayrımı ilk günden: `data` (Room/DAO/repository) · `domain` (tekrar kuralı) ·
      `images` (içe aktarma/dosya yönetimi) · `backup` (ZIP/doğrulama/geri yükleme) · `ui`
- [ ] Room veritabanı, şema v1, migration altyapısı ve `exportSchema`
- [ ] Yabancı anahtarlar ve silme davranışlarının açıkça tanımlanması
- [ ] 7 ders + konu yönetimi (ekle / yeniden adlandır / arşivle / yeniden etkinleştir)
- [ ] Kamera ile çekim ve sistem fotoğraf seçicisi ile galeriden seçim
- [ ] Görsellerin kalıcı dosya alanına alınması, atomik yazma, tutarlılık denetimi, onaylı temizlik
- [ ] Soru ekleme akışı: fotoğraf → (isteğe bağlı döndür) → ders → kaydet
- [ ] Çok görsel: "bir sayfa daha ekle" (soru ve çözüm için); çözüm sonradan da eklenebilir
- [ ] Görsel kalite politikası: 30–50 gerçek soru fotoğrafıyla sıkıştırma denemesi
      (küçük formüller, kök içi ifadeler, grafik etiketleri okunuyor mu)
- [ ] Listeleme (derse/konuya/plana göre filtre, "konusuz" listesi)
- [ ] Soru görüntüleme: tam ekran, yakınlaştırma, çözüm gizli + "Çözümü göster"
- [ ] Düzenleme, silme (onay diyaloğu ile) ve plan durumu (aktif / askıda / tamamlandı)
- [ ] JVM birim testleri: aralık hesabı, yedek doğrulama, ZIP yol güvenliği
- [ ] Robolectric ile şema ve migration testleri
- [ ] **Aşama sonu ek güvenlik ağı:** minimum "dışa aktar" (yedek özelliği tamamlanmasa bile veriyi
      dışarı alabilme)

---

## Aşama 3 — Veri güvenliği (yedeğin doğrulanması)

> Bu aşama doğrulanmadan uygulama, ciddi soru arşivi biriktirmek için güvenilir kabul edilmez.

- [ ] Tam ZIP yedeği üretimi (manifest + JSON + CSV + görseller)
- [ ] Dosya seçicisi ile dışa kaydetme; cihazda Drive'a kaydetme akışının denenmesi
- [ ] Yedek doğrulama: manifest, dosya başına sha256, sayılar, ilişki tutarlılığı, sürüm kontrolü
- [ ] ZIP güvenlik sınırları: yol kaçışı, dosya sayısı/boyut üst sınırları, zip bomb koruması
- [ ] Geri yükleme: "tamamen değiştir" + öncesinde otomatik güvenlik kopyası
- [ ] **Kesintiye dayanıklılık:** işlem sürerken uygulama kapanırsa sonraki açılışta tamamlama/geri alma
- [ ] Geri yükleme öncesi özet (yedekteki soru/görsel sayısı ↔ mevcut arşiv) ve onay
- [ ] Bozuk/eksik/anlamsız yedek mevcut arşivi bozmuyor testi
- [ ] Temiz kurulumda ve **başka cihazda** gerçek yedekten geri yükleme testi
- [ ] Güncellemede veri koruma testi (Aşama 1 zincirinin gerçek veriyle tekrarı)
- [ ] Son yedek bilgisinin (tarih, boyut, dosya adı) gösterilmesi
- [ ] Başarısız yedek işleminin başarı gibi gösterilmediğinin doğrulanması

---

## Aşama 4 — Tekrar sistemi

- [ ] Tekrar değerlendirmesi: Çözemedim / Zorlanarak çözdüm / Rahat çözdüm
- [ ] "Çözüm açıldı" işaretinin kaydı; destekli tekrarın bağımsız sayılmaması (aralık en fazla 1–3 gün)
- [ ] "Çözümü kapat ve tekrar dene": aynı gün ikinci, bağımsız deneme kaydı
- [ ] Aralık kuralının **sürümlü tek sınıfta** yazılması (`algoritma_surumu`)
- [ ] Sonraki tekrar tarihi hesabı (1·3·7·21·45·90, üst sınır ~180)
- [ ] Elle tarih atama
- [ ] "Bugün" ekranı: bugün yapılacaklar + gecikmiş tekrarlar + boş durum mesajı
- [ ] Uzun aradan sonra otomatik sıfırlama yapılmaması
- [ ] Soru plan durumu: aktif / askıda / tamamlandı
- [ ] Tekrar geçmişi görüntüleme (planlanan gün ↔ gerçekleşen gün)
- [ ] Temel filtreler
- [ ] Yerel takvim günü semantiği ve saat dilimi testleri

---

## Aşama 5 — Kullanılabilirlik ve sağlamlaştırma

- [ ] Büyük arşiv performansı (listeleme, görsel açma) — ölçüm yapılır
- [ ] Önizleme stratejisinin gözden geçirilmesi (Coil önbelleği yeterli mi)
- [ ] Depolama kullanımı göstergesi ve (depolama dolarsa) isteğe bağlı "eski görselleri yeniden
      sıkıştır" aracı
- [ ] Kırpma (crop) ekranı
- [ ] Hata ve boş durum ekranları, yükleme göstergeleri
- [ ] Görsel kalite / dosya boyutu dengesi
- [ ] Erişilebilirlik (okunaklı yazı, dokunma alanları, içerik açıklamaları)
- [ ] İzin reddi durumları
- [ ] Düşük depolama alanı ve yarıda kesilen işlem senaryoları
- [ ] Tema ve tasarım iyileştirmeleri

---

## Aşama 6 — İsteğe bağlı geliştirmeler

- [ ] Doğrudan Drive bağlantısı (önce SAF kalıcı izni + Drive klasörü yolu)
- [ ] Otomatik/kolay yedekleme ("Şimdi yedekle" düğmesi dâhil)
- [ ] Bildirimler (hatırlatma) — kesin alarm kullanılmaz; arka plan kısıtları nedeniyle tam saat
      garantisi verilmez
- [ ] Mini deneme oluşturma
- [ ] PDF çalışma kâğıdı
- [ ] Basit istatistikler
- [ ] Yedek şifreleme (kullanıcı kararı ile)

---

## Kapsam dışı (kalıcı olarak değerlendirilmedi)

Sosyal özellikler, çoklu cihaz canlı senkronizasyon, iOS, web paneli, reklam, abonelik, uzak
veritabanı, OCR/yapay zekâ ile otomatik çözüm. Ayrıntı: [PROJECT_BRIEF.md](PROJECT_BRIEF.md) §7.
