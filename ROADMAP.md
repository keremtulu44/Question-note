# ROADMAP — Yanlış Defteri

Durum işaretleri: `[ ]` yapılmadı · `[~]` devam ediyor · `[x]` tamam · `[!]` engellendi

---

## Aşama 0 — Konuşma ve plan ✅

- [x] Gereksinimlerin değerlendirilmesi
- [x] Teknoloji seçiminin gözden geçirilmesi (native Android / Kotlin / Compose / Room)
- [x] Açık soruların kullanıcıyla netleştirilmesi (görseller, ders/konu yönetimi, tekrar modeli,
      geri yükleme politikası, imzalama)
- [x] İlk sürüm kapsamının belirlenmesi
- [x] Bekleme: kullanıcının Aşama 1 için açık onayı

**Çıktı:** Bu depodaki belgeler. Kod yazılmadı.

---

## Aşama 1 — İlk kurulabilir APK

**Amaç:** Derleme → indirme → kurma → güncelleme zincirini doğrulamak. Bu zincir kırıkken
uygulama geliştirmenin anlamı yok.

- [ ] GitHub Actions'ta yazma yetkisinin ve Secret ekleme imkânının doğrulanması
- [ ] Temel Android proje iskeleti (tek Gradle modülü, Kotlin + Compose, sürüm kataloğu)
- [ ] Sabit release imzalama anahtarının oluşturulması ve GitHub Secret olarak tanımlanması
- [ ] Elle tetiklenen derleme workflow'u
- [ ] APK'nın GitHub Release olarak yayımlanması
- [ ] Temel tema (açık/koyu), ekran iskeleti, 7 ders kartı
- [ ] **Kullanıcı testi:** APK'yı indir, kur, aç, 7 ders kartını gör
- [ ] **Kullanıcı testi:** ikinci APK'yı (v2) **kaldırmadan** üzerine kur — veri/imza zinciri çalışıyor mu

**Çıkış kriteri:** Kullanıcı, telefonda kaldırma yapmadan yeni APK ile güncelleme yapabiliyor.

---

## Aşama 2 — Yerel soru arşivi

- [ ] Room veritabanı, şema v1, migration altyapısı
- [ ] 7 ders + konu yönetimi (ekle / yeniden adlandır / arşivle)
- [ ] Kamera ile çekim ve sistem fotoğraf seçicisi ile galeriden seçim
- [ ] Görsellerin kalıcı dosya alanına alınması, kontrollü küçültme, atomik yazma, öksüz dosya temizliği
- [ ] Soru ekleme akışı: fotoğraf → (isteğe bağlı döndür) → ders → kaydet
- [ ] Çok görsel: "bir sayfa daha ekle" (soru ve çözüm için)
- [ ] Listeleme (derse/konuya/duruma göre filtre, "konusuz" listesi)
- [ ] Soru görüntüleme: tam ekran, yakınlaştırma, çözüm gizli + "Çözümü göster"
- [ ] Düzenleme ve silme (onay diyaloğu ile, ilişkileri bozmadan)
- [ ] **Aşama sonu ek güvenlik ağı:** minimum "dışa aktar" (yedek özelliği tamamlanmasa bile veriyi dışarı alabilme)

---

## Aşama 3 — Veri güvenliği (yedeğin doğrulanması)

> Bu aşama doğrulanmadan uygulama, ciddi soru arşivi biriktirmek için güvenilir kabul edilmez.

- [ ] Tam ZIP yedeği üretimi (manifest + JSON + CSV + görseller)
- [ ] Dosya seçicisi ile dışa kaydetme; cihazda Drive'a kaydetme akışının denenmesi
- [ ] Yedek doğrulama (bütünlük kontrolü, ZIP yol güvenliği, sürüm kontrolü)
- [ ] Geri yükleme: "tamamen değiştir" + öncesinde otomatik güvenlik kopyası
- [ ] Bozuk/eksik yedek mevcut arşivi bozmuyor testi
- [ ] Temiz kurulumda gerçek yedekten geri yükleme testi
- [ ] Güncellemede veri koruma testi (Aşama 1 zincirinin gerçek veriyle tekrarı)
- [ ] Başarısız yedek işleminin başarı gibi gösterilmediğinin doğrulanması

---

## Aşama 4 — Tekrar sistemi

- [ ] Tekrar değerlendirmesi: Çözemedim / Zorlanarak çözdüm / Rahat çözdüm
- [ ] "Çözüm açıldı" işaretinin kaydı ve aralık hesabına etkisi
- [ ] Sonraki tekrar tarihi hesabı (1·3·7·21·45·90, üst sınır ~180)
- [ ] Elle tarih atama
- [ ] "Bugün" ekranı: bugün yapılacaklar + gecikmiş tekrarlar + boş durum mesajı
- [ ] Tekrar geçmişi görüntüleme
- [ ] Temel filtreler

---

## Aşama 5 — Kullanılabilirlik ve sağlamlaştırma

- [ ] Büyük arşiv performansı (listeleme, görsel açma, önizlemeler)
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
- [ ] Bildirimler (hatırlatma) — arka plan kısıtları nedeniyle tam saat garantisi verilmez
- [ ] Mini deneme oluşturma
- [ ] PDF çalışma kâğıdı
- [ ] Basit istatistikler
- [ ] Yedek şifreleme (kullanıcı kararı ile)

---

## Kapsam dışı (kalıcı olarak değerlendirilmedi)

Sosyal özellikler, çoklu cihaz canlı senkronizasyon, iOS, web paneli, reklam, abonelik, uzak
veritabanı, OCR/yapay zekâ ile otomatik çözüm. Ayrıntı: [PROJECT_BRIEF.md](PROJECT_BRIEF.md) §7.
