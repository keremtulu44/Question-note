# Test Kontrol Listesi

> Her testte **kim** yaptığı açıkça yazılır. Yapay zekâ, çalıştırmadığı bir testi "geçti" olarak
> işaretleyemez.
>
> - **YZ** → yapay zekâ tarafından gerçekten çalıştırılan/otomatik test veya kod incelemesi
> - **KULLANICI** → gerçek cihazda (Samsung Galaxy S24 FE) kullanıcı tarafından yapılacak test
> - Durum: `[ ]` bekliyor · `[x]` geçti · `[!]` başarısız · `[-]` uygulanamaz
>
> Test yaklaşımı: hızlı geri bildirim için **JVM birim testleri** (aralık hesabı, yedek doğrulama,
> ZIP yol güvenliği) ve **Robolectric** (şema/migration testleri). Emülatör yalnızca gerçekten
> gerekirse kullanılır; asıl kabul testleri gerçek cihazda yapılır.

Son güncelleme: 2026-10-16 (A2 Room v1 şeması ve 3 Robolectric testi CI'da geçti; run `38944293610`)

---

## Aşama 1 — Derleme ve güncelleme zinciri

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| 1.1 | Actions workflow'u yazılabiliyor ve push ile otomatik çalışıyor | YZ | [x] |
| 1.2 | G2 arayüzsüz `assembleRelease` derlemesi başarılı | YZ | [x] |
| 1.3 | v1.0.0/v1.0.1 imzalı APK Release'e eklendi; sertifika parmak izi eşleşti | YZ | [x] |
| 1.4 | İki Release notunda sürüm, commit kimliği ve APK SHA-256 bulunuyor | YZ | [x] |
| 1.5 | v1.0.0 APK telefona indirilip kuruluyor ve açılıyor | KULLANICI | [x] |
| 1.6 | v1.0.1 güncellemesinden sonra uygulama açılıyor ve 7 ders kartı görünüyor | KULLANICI | [x] |
| 1.7 | v1.0.1, v1.0.0 üzerine **uygulamayı kaldırmadan** kuruluyor | KULLANICI | [x] |
| 1.8 | CI derleme özeti, hata çıktısı ve APK SHA-256 repoda okunabiliyor | YZ | [x] |
| 1.9 | VersionCode 1 → 2 artışı ve v1.0.1 Release CI'da doğrulandı | YZ | [x] |

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
| 2.13 | Ders/konu arşivleme mevcut soruları bozmuyor, yeni kayıtta seçenek olarak çıkmıyor | KULLANICI | [ ] |
| 2.14 | Görseller kalıcı alanda, önbellekte tek kopya yok (dosya sistemi incelenir) | YZ + KULLANICI | [ ] |
| 2.15 | Yarım kalan işlem bozuk kayıt oluşturmuyor (kod incelemesi + senaryo denemesi) | YZ | [ ] |
| 2.16 | Kullanılmayan dosya temizliği onaysız/yıkıcı biçimde çalışmıyor, ne sildiğini bildiriyor | YZ + KULLANICI | [ ] |
| 2.17 | Eksik görsel (dosya yok) durumunda kayıt kırılmıyor, anlaşılır mesaj gösteriliyor | YZ + KULLANICI | [ ] |
| 2.18 | Sıkıştırma testi: 30–50 gerçek soru fotoğrafında küçük formüller/kök içi ifadeler okunuyor | YZ + KULLANICI | [ ] |
| 2.19 | Room v1 oluşturuluyor; beş tablo ve sürüm 1 doğrulanıyor | YZ (Robolectric) | [x] |
| 2.20 | Yeni veritabanında yedi ders, sabit sırada ve arşivsiz oluşturuluyor | YZ (Robolectric) | [x] |
| 2.21 | Soru/ders/konu yabancı anahtarları ve görsel/geçmiş CASCADE kuralları açık | YZ (Robolectric) | [x] |

## Aşama 3 — Yedek ve geri yükleme

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| 3.1 | Yedek ZIP'i oluşturuluyor ve doğrulanıyor (manifest, sha256, sayılar) | YZ | [ ] |
| 3.2 | Yedek cihazdaki bir klasöre kaydediliyor | KULLANICI | [ ] |
| 3.3 | Yedek Drive'a kaydediliyor (dosya seçici akışı) | KULLANICI | [ ] |
| 3.4 | Temiz kurulumda yedekten geri yükleme çalışıyor | KULLANICI | [ ] |
| 3.5 | Yedek görselleri kapsıyor (geri yükleme sonrası görseller açılıyor) | KULLANICI | [ ] |
| 3.6 | Yedek tekrar geçmişini ve plan durumunu kapsıyor | KULLANICI | [ ] |
| 3.7 | Bozuk yedek mevcut arşivi bozmuyor (kasıtlı bozulmuş ZIP) | YZ + KULLANICI | [ ] |
| 3.8 | Desteklenmeyen/eksik yedek anlaşılır hatayla reddediliyor | YZ | [ ] |
| 3.9 | Başarısız yedek işlemi başarı gibi gösterilmiyor | YZ | [ ] |
| 3.10 | Geri yükleme öncesi güvenlik kopyası oluşuyor | YZ + KULLANICI | [ ] |
| 3.11 | Uygulama güncellemesi mevcut verileri koruyor (gerçek veriyle) | KULLANICI | [ ] |
| 3.12 | ZIP içi yol güvenliği: `..` veya mutlak yol içeren ZIP reddediliyor | YZ | [ ] |
| 3.13 | Geri yükleme sırasında uygulama kapatılırsa arşiv bozulmuyor; sonraki açılışta tamamlanıyor ya da geri alınıyor | YZ + KULLANICI | [ ] |
| 3.14 | ZIP bomb / aşırı büyüme / çok fazla dosya içeren arşiv açılmadan reddediliyor | YZ | [ ] |
| 3.15 | Yedek **başka bir cihazda** (cihaz bağlılığı olmadan) eksiksiz geri yükleniyor | KULLANICI | [ ] |
| 3.16 | Anlamsız/boş yedek seçilince kullanıcı onayı olmadan hiçbir şey değişmiyor | YZ + KULLANICI | [ ] |
| 3.17 | Son yedek bilgisi (tarih, boyut, dosya adı) doğru gösteriliyor | KULLANICI | [ ] |

## Aşama 4 — Tekrar sistemi

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| 4.1 | "Bugün" listesi doğru soruları gösteriyor | KULLANICI | [ ] |
| 4.2 | Gecikmiş tekrarlar ayrı görünüyor | KULLANICI | [ ] |
| 4.3 | "Çözemedim" sonrası tarih 1 güne dönüyor | YZ + KULLANICI | [ ] |
| 4.4 | "Rahat çözdüm" sonrası aralık büyüyor | YZ + KULLANICI | [ ] |
| 4.5 | Çözüm açıldığında bu bilgi kaydediliyor | YZ + KULLANICI | [ ] |
| 4.6 | **Çözüm açılarak yapılan tekrar bağımsız sayılmıyor**; sonraki tekrar 1 gün sonraya planlanıyor | YZ + KULLANICI | [ ] |
| 4.7 | Çözüm açıldıktan sonra aynı gün ikinci deneme kaydı oluşmuyor | YZ | [ ] |
| 4.8 | Tekrar geçmişi görüntülenebiliyor (planlanan gün ile gerçekleşen gün ayrı) | KULLANICI | [ ] |
| 4.9 | Bildirim izni verilmese de "Bugün" listesi çalışıyor | KULLANICI | [ ] |
| 4.10 | Elle tarih atama çalışıyor | KULLANICI | [ ] |
| 4.11 | Saat dilimi değişiminde gün hesabı bozulmuyor (kayıtlı gün değişmiyor) | YZ + KULLANICI | [ ] |
| 4.12 | `ASKIDA` / `TAMAMLANDI` sorular "Bugün" listesinden çıkıyor, kayıt silinmiyor, geri alınabiliyor | KULLANICI | [ ] |
| 4.13 | Uzun aradan sonra hiçbir soru otomatik sıfırlanmıyor; gecikmişler tek listede toplanıyor | YZ + KULLANICI | [ ] |
| 4.14 | Aralık hesabı birim testleri (tüm sonuç × çözüm açıldı/açılmadı kombinasyonları) | YZ | [ ] |

## Aşama 5 — Dayanıklılık

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| 5.1 | Binlerce kayıtta listeleme kabul edilebilir hızda (ölçüm yapılır, tahmin yazılmaz) | YZ + KULLANICI | [ ] |
| 5.2 | Önizleme stratejisi ölçümle doğrulanıyor (Coil önbelleği yeterli mi) | YZ | [ ] |
| 5.3 | Büyük görseller bellek sorununa yol açmıyor | YZ + KULLANICI | [ ] |
| 5.4 | Depolama kullanımı görüntülenebiliyor (kaç soru/görsel, ne kadar yer) | KULLANICI | [ ] |
| 5.5 | Düşük depolama alanında anlaşılır hata mesajı | KULLANICI | [ ] |
| 5.6 | İzin reddi durumlarında uygulama çökmüyor | KULLANICI | [ ] |
| 5.7 | Geri tuşu beklenen şekilde çalışıyor | KULLANICI | [ ] |
| 5.8 | Sistem çubukları ve ekran boşlukları düzgün (çentik, gezinme çubuğu) | KULLANICI | [ ] |
| 5.9 | Açık/koyu tema okunaklı | KULLANICI | [ ] |

## Genel / sürekli

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| G.1 | Hiçbir gizli anahtar, parola veya token repoda değil | YZ | [x] |
| G.2 | Yedek dosyası repoya işlenmiyor (`.gitignore`) | YZ | [ ] |
| G.3 | İmzalama anahtarının iki ayrı yedek kopyası kullanıcıda mevcut | KULLANICI | [ ] |
| G.4 | Uygulama kaldırılırsa verilerin kaybolacağı kullanıcı tarafından biliniyor | KULLANICI | [ ] |
| G.5 | Her migration için otomatik test var; `v1 → vN` zinciri doğrulanıyor | YZ | [ ] |
| G.6 | Debug ve release APK'ları karıştırılmıyor; günlük kullanımda hep release APK | KULLANICI | [ ] |
| G.7 | Derleme, sabitlenmiş sürümlerle tekrar edilebiliyor (bir yıl sonra da) | YZ | [ ] |

---

## Raporlama kuralı

Her geliştirme adımının sonunda şu ayrım yazılır:

1. **Gerçekten çalıştırılan testler** (komut/çıktı ile),
2. **Yalnızca gözden geçirilenler** (kod okuması, statik değerlendirme),
3. **Kullanıcının cihazında test etmesi gerekenler**.
