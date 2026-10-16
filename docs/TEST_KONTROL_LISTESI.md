# Test Kontrol Listesi

> Her testte **kim** yaptığı açıkça yazılır. Yapay zekâ, çalıştırmadığı bir testi "geçti" olarak
> işaretleyemez.
>
> - **YZ** → yapay zekâ tarafından gerçekten çalıştırılan/otomatik test veya kod incelemesi
> - **KULLANICI** → gerçek cihazda (Samsung Galaxy S24 FE) kullanıcı tarafından yapılacak test
> - Durum: `[ ]` bekliyor · `[x]` geçti · `[!]` başarısız · `[-]` uygulanamaz

Son güncelleme: 2026-10-16

---

## Aşama 1 — Derleme ve güncelleme zinciri

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| 1.1 | Actions workflow'u yazılabiliyor ve çalışıyor | YZ | [ ] |
| 1.2 | Derleme başarıyla tamamlanıyor (loglar hatasız) | YZ | [ ] |
| 1.3 | İmzalı APK Release'e ekleniyor | YZ | [ ] |
| 1.4 | APK telefondan indirilip kuruluyor | KULLANICI | [ ] |
| 1.5 | Uygulama açılıyor, 7 ders kartı görünüyor | KULLANICI | [ ] |
| 1.6 | İkinci APK **kaldırmadan** üzerine kuruluyor | KULLANICI | [ ] |

## Aşama 2 — Yerel arşiv

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| 2.1 | Uçak modunda soru eklenebiliyor | KULLANICI | [ ] |
| 2.2 | Uygulama kapanıp açıldığında kayıtlar duruyor | KULLANICI | [ ] |
| 2.3 | Telefon yeniden başlatıldığında veriler duruyor | KULLANICI | [ ] |
| 2.4 | Galerideki kaynak görsel silinse de soru açılıyor | KULLANICI | [ ] |
| 2.5 | Kamera/fotoğraf seçme iptal edilince uygulama bozulmuyor | KULLANICI | [ ] |
| 2.6 | Konusuz soru eklenebiliyor ve "Konusuz" listesinde görünüyor | KULLANICI | [ ] |
| 2.7 | Soru ve çözüm ayrı gösteriliyor (çözüm varsayılan gizli) | KULLANICI | [ ] |
| 2.8 | Çözüm sonradan eklenebiliyor | KULLANICI | [ ] |
| 2.9 | Çok görsel: "bir sayfa daha ekle" çalışıyor | KULLANICI | [ ] |
| 2.10 | Görsel büyütme/yakınlaştırma çalışıyor | KULLANICI | [ ] |
| 2.11 | Soru düzenleme veri ilişkilerini bozmuyor | KULLANICI | [ ] |
| 2.12 | Silme davranışı açık ve güvenli (onay diyaloğu, sonuç bildirimi) | KULLANICI | [ ] |
| 2.13 | Ders/konu arşivleme mevcut soruları bozmuyor | KULLANICI | [ ] |
| 2.14 | Görseller kalıcı alanda, önbellekte tek kopya yok (dosya sistemi incelenir) | YZ + KULLANICI | [ ] |
| 2.15 | Yarım kalan işlem bozuk kayıt oluşturmuyor (kod incelemesi + senaryo denemesi) | YZ | [ ] |

## Aşama 3 — Yedek ve geri yükleme

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| 3.1 | Yedek ZIP'i oluşturuluyor ve doğrulanıyor (manifest, sha256, sayılar) | YZ | [ ] |
| 3.2 | Yedek cihazdaki bir klasöre kaydediliyor | KULLANICI | [ ] |
| 3.3 | Yedek Drive'a kaydediliyor (dosya seçici akışı) | KULLANICI | [ ] |
| 3.4 | Temiz kurulumda yedekten geri yükleme çalışıyor | KULLANICI | [ ] |
| 3.5 | Yedek görselleri kapsıyor (geri yükleme sonrası görseller açılıyor) | KULLANICI | [ ] |
| 3.6 | Yedek tekrar geçmişini kapsıyor (durum ve tarihler korunuyor) | KULLANICI | [ ] |
| 3.7 | Bozuk yedek mevcut arşivi bozmuyor (kasıtlı bozulmuş ZIP ile) | YZ + KULLANICI | [ ] |
| 3.8 | Desteklenmeyen/eksik yedek anlaşılır hatayla reddediliyor | YZ | [ ] |
| 3.9 | Başarısız yedek işlemi başarı gibi gösterilmiyor | YZ | [ ] |
| 3.10 | Geri yükleme öncesi güvenlik kopyası oluşuyor | YZ + KULLANICI | [ ] |
| 3.11 | Uygulama güncellemesi mevcut verileri koruyor (gerçek veriyle) | KULLANICI | [ ] |
| 3.12 | ZIP içi yol güvenliği: `..` veya mutlak yol içeren ZIP reddediliyor | YZ | [ ] |

## Aşama 4 — Tekrar sistemi

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| 4.1 | "Bugün" listesi doğru soruları gösteriyor | KULLANICI | [ ] |
| 4.2 | Gecikmiş tekrarlar ayrı görünüyor | KULLANICI | [ ] |
| 4.3 | "Çözemedim" sonrası tarih 1 güne dönüyor | YZ + KULLANICI | [ ] |
| 4.4 | "Rahat çözdüm" sonrası aralık büyüyor | YZ + KULLANICI | [ ] |
| 4.5 | Çözüm açıldığında bu bilgi kaydediliyor ve aralığı etkiliyor | YZ + KULLANICI | [ ] |
| 4.6 | Tekrar geçmişi görüntülenebiliyor | KULLANICI | [ ] |
| 4.7 | Bildirim izni verilmese de "Bugün" listesi çalışıyor | KULLANICI | [ ] |
| 4.8 | Elle tarih atama çalışıyor | KULLANICI | [ ] |
| 4.9 | Saat dilimi değişiminde gün hesabı bozulmuyor | YZ + KULLANICI | [ ] |

## Aşama 5 — Dayanıklılık

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| 5.1 | Binlerce kayıtta listeleme kabul edilebilir hızda (ölçüm yapılır, tahmin yazılmaz) | YZ + KULLANICI | [ ] |
| 5.2 | Büyük görseller bellek sorununa yol açmıyor | YZ + KULLANICI | [ ] |
| 5.3 | Düşük depolama alanında anlaşılır hata mesajı | KULLANICI | [ ] |
| 5.4 | İzin reddi durumlarında uygulama çökmüyor | KULLANICI | [ ] |
| 5.5 | Geri tuşu beklenen şekilde çalışıyor | KULLANICI | [ ] |
| 5.6 | Sistem çubukları ve ekran boşlukları düzgün (çentik, gezinme çubuğu) | KULLANICI | [ ] |
| 5.7 | Açık/koyu tema okunaklı | KULLANICI | [ ] |

## Genel / sürekli

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| G.1 | Hiçbir gizli anahtar, parola veya token repoda değil | YZ | [ ] |
| G.2 | Yedek dosyası repoya işlenmiyor (`.gitignore`) | YZ | [ ] |
| G.3 | İmzalama anahtarının iki ayrı yedek kopyası kullanıcıda mevcut | KULLANICI | [ ] |
| G.4 | Uygulama kaldırılırsa verilerin kaybolacağı kullanıcı tarafından biliniyor | KULLANICI | [ ] |

---

## Raporlama kuralı

Her geliştirme adımının sonunda şu ayrım yazılır:

1. **Gerçekten çalıştırılan testler** (komut/çıktı ile),
2. **Yalnızca gözden geçirilenler** (kod okuması, statik değerlendirme),
3. **Kullanıcının cihazında test etmesi gerekenler**.
