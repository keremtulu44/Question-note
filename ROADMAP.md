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

**Çıktı:** Kararlar ve uygulama planı. Aşama 1'de G2/G3/G4 tamamlandı; G5 cihaz testi aşağıda bekliyor.

---

## Aşama 1 — İlk kurulabilir APK

- **Aşama 1'in ayrıntılı uygulama sırası:** [docs/KODLAMA_PLANI.md](docs/KODLAMA_PLANI.md)

**Amaç:** Derleme → indirme → kurma → güncelleme zincirini doğrulamak. Bu zincir kırıkken
uygulama geliştirmenin anlamı yok.

- [x] G1: push ile otomatik çalışan GitHub Actions ve `contents:write` yetkisi ölçüldü
- [x] G2: tek Android modülü, Kotlin + Compose, sabit sürüm kataloğu ve Gradle wrapper; arayüzsüz `assembleRelease` CI'da başarılı
- [x] `applicationId=com.keremtulu.questionnote`, `minSdk=26`, API 37 compile/target sabitlendi
- [x] Sabit release anahtarı üretildi; kullanıcı secret'ları ekledi; G3'te imzalama ve SHA-256 parmak izi eşleşti
- [x] Push ile otomatik Android derleme, imzalama ve Release workflow'u (elle tetikleme gerekmiyor)
- [x] G3: imzalı v1.0.0 APK + sertifika doğrulaması + Release
- [x] G4: açık/koyu tema ve 7 ders kartı; imzalı v1.0.1 APK + Release
- [ ] **KULLANICI / G5:** v1.0.0'ı kur; v1.0.1'i uygulamayı kaldırmadan yükle; 7 kartı gördüğünü bildir

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
- [ ] Görsel kalite politikası: otomatik optimize (uzun kenar ~3000 px, JPEG ~%90) ve 30–50 gerçek
      soru fotoğrafıyla doğrulama (küçük formüller, kök içi ifadeler, grafik etiketleri okunuyor mu)
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
- [ ] "Çözüm açıldı" işaretinin kaydı; destekli tekrarın bağımsız sayılmaması (sonraki tekrar 1 gün)
- [ ] Aynı gün ikinci deneme oluşturulmaması (kayıt tekilliği)
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
