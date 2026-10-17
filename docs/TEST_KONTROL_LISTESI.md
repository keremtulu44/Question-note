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

Son güncelleme: 2026-10-17. v1.0.4 CI run `39033698686` 33 testi, Release derlemesini ve imzayı geçti. Kullanıcı, v1.0.3'te mevcut uygulamayı koruyan güncelleme, Bugün/gecikenler, menü ve tekrar sonucu cihaz adımlarının tümünü geçti bildirdi (2026-10-17). v1.0.4 tekrar geçmişi UI'si yeni olduğu için cihazda henüz test edilmedi. Yedek menüsünü açma ve geri yüklemeyi iptal etme, tam yedek/geri yükleme turu değildir; Aşama 3 cihaz kabulü hâlâ bekliyor. v1.0.2 için “3. turu denemedim” ifadesinin önceki checklist'te 1/2/4 geçti ve 3. bölüm denenmedi şeklinde eşlenmesi ise hâlâ teyitsizdir.

---

## Aşama 1 — Derleme ve güncelleme zinciri

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| 1.1 | Actions workflow'u yazılabiliyor ve push ile otomatik çalışıyor | YZ | [x] |
| 1.2 | G2 arayüzsüz `assembleRelease` derlemesi başarılı | YZ | [x] |
| 1.3 | v1.0.0/v1.0.1/v1.0.2/v1.0.3 imzalı APK Release'e eklendi; her sürümde sertifika parmak izi eşleşti | YZ | [x] |
| 1.4 | Yayımlanan her Release notunda sürüm, commit kimliği ve APK SHA-256 bulunuyor | YZ | [x] |
| 1.5 | v1.0.0 APK telefona indirilip kuruluyor ve açılıyor | KULLANICI | [x] |
| 1.6 | v1.0.1 güncellemesinden sonra uygulama açılıyor ve 7 ders kartı görünüyor | KULLANICI | [x] |
| 1.7 | v1.0.1, v1.0.0 üzerine **uygulamayı kaldırmadan** kuruluyor | KULLANICI | [x] |
| 1.8 | CI derleme özeti, hata çıktısı ve APK SHA-256 repoda okunabiliyor | YZ | [x] |
| 1.9 | VersionCode 1 → 2 artışı ve v1.0.1 Release CI'da doğrulandı | YZ | [x] |
| 1.10 | VersionCode 2 → 3; v1.0.2 Release, APK imzası ve fingerprint CI'da doğrulandı | YZ (CI run `39027221270`) | [x] |
| 1.11 | v1.0.2 mevcut kurulumun üzerine yüklendi; kullanıcı uygulamanın açıldığını bildirdi | KULLANICI (2026-10-17) | [x] |
| 1.12 | v1.0.3 mevcut uygulamanın üzerine kuruldu; kullanıcı açılışı, önceki verilerin korunmasını, Bugün ekranı ve menü adımlarını geçti bildirdi | KULLANICI (2026-10-17) | [x] |
| 1.13 | VersionCode 3 → 4; v1.0.3 Release, APK imzası ve fingerprint CI'da doğrulandı | YZ (CI run `39030667492`) | [x] |
| 1.14 | v1.0.4 mevcut uygulamanın üzerine yükleniyor; veri ve tekrar geçmişi korunuyor/görünüyor | KULLANICI | [ ] |
| 1.15 | VersionCode 4 → 5; v1.0.4 Release, APK imzası ve fingerprint CI'da doğrulandı | YZ (CI run `39033698686`) | [x] |

## Aşama 2 — Yerel arşiv

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| 2.1 | Uçak modunda soru eklenebiliyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.2 | Uygulama kapanıp açıldığında kayıtlar duruyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.3 | Telefon yeniden başlatıldığında veriler duruyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.4 | Galerideki kaynak görsel silinse de soru açılıyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.5 | Kamera/fotoğraf seçme iptal edilince uygulama bozulmuyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.6 | Konusuz soru eklenebiliyor ve "Konusuz" listesinde görünüyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.7 | Soru ve çözüm ayrı gösteriliyor (çözüm varsayılan gizli) | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.8 | Çözüm sonradan eklenebiliyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.9 | Çok görsel: "bir sayfa daha ekle" çalışıyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.10 | Görsel büyütme/yakınlaştırma çalışıyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.11 | Soru düzenleme veri ilişkilerini bozmuyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.12 | Silme davranışı açık ve güvenli (onay diyaloğu, sonuç bildirimi) | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.13 | Ders/konu arşivleme mevcut soruları bozmuyor, yeni kayıtta seçenek olarak çıkmıyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.14 | Görseller kalıcı alanda, önbellekte tek kopya yok (dosya sistemi incelenir) | YZ + KULLANICI | [ ] |
| 2.15 | Yarım kalan işlem bozuk kayıt oluşturmuyor (kod incelemesi + senaryo denemesi) | YZ | [ ] |
| 2.16 | Kullanılmayan dosya temizliği onaysız/yıkıcı biçimde çalışmıyor, ne sildiğini bildiriyor | YZ + KULLANICI | [ ] |
| 2.17 | Eksik görsel (dosya yok) durumunda kayıt kırılmıyor, anlaşılır mesaj gösteriliyor | YZ + KULLANICI | [ ] |
| 2.18 | Sıkıştırma testi: 30–50 gerçek soru fotoğrafında küçük formüller/kök içi ifadeler okunuyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.19 | Room v2 şeması, v1 fixture'ı ve v1→v2 veri koruyan migration zinciri doğrulanıyor | YZ (Robolectric) | [x] |
| 2.20 | Yeni veritabanında yedi ders, sabit sırada ve arşivsiz oluşturuluyor | YZ (Robolectric) | [x] |
| 2.21 | Soru/ders/konu yabancı anahtarları ve görsel/geçmiş CASCADE kuralları açık | YZ (Robolectric) | [x] |
| 2.22 | Repository yalnızca etkin dersleri, sabit sırada ve doğru soru sayılarıyla yayımlıyor | YZ (Robolectric) | [x] |
| 2.23 | Dosya tabanlı veritabanı yeniden açılınca yedi seed dersi koruyor, çoğaltmıyor | YZ (Robolectric) | [x] |
| 2.24 | Konu ekleme adı trimler; yinelenen Türkçe büyük/küçük harf varyantını reddeder | YZ (Robolectric) | [x] |
| 2.25 | Konu yeniden adlandırma boş/tekrarlı adı reddeder, geçerli adı ve sırayı korur | YZ (Robolectric) | [x] |
| 2.26 | Konu arşivleme/geri getirme soruyu veya `konu_id` ilişkisini silmez | YZ (Robolectric) | [x] |
| 2.27 | Konu ekle/düzenle/arşivle/geri getir akışı gerçek cihazda anlaşılır ve kalıcı çalışır | KULLANICI | [ ] |
| 2.28 | Ders ekle/yeniden adlandır/sıra/arşivleme ve geri getirme repository testleri | YZ (Robolectric) | [x] |
| 2.29 | Soru oluştur/düzenle/filtrele/arşivle/sil, ders-konu ilişkisi ve tekrar planı testleri | YZ (Robolectric) | [x] |
| 2.30 | JPEG optimizasyonu, EXIF yönü, SHA-256, bozuk/non-image girdi ve geçici dosya yaşı testleri | YZ (Robolectric) | [x] |
| 2.31 | Onaylı yetim temizliği, dosya yaşı ve DB referansını silme öncesi yeniden denetleme testleri | YZ (Robolectric) | [x] |
| 2.32 | Repository Bugün akışı dersler arası gecikmiş/vadesi gelen aktif soruları içeriyor; gelecek tarihli, askıdaki ve arşiv ders sorularını dışlıyor | YZ (CI run `39030667492`) | [x] |
| 2.33 | Yeni Bugün/gecikenler ekranı, ders seçerek soru ekleme ve yan menü adımları gerçek cihazda geçti | KULLANICI (2026-10-17, v1.0.3) | [x] |
| 2.34 | Tekrar sonucu kaydediliyor; uygulama Bugün'e dönüyor ve tamamlanan soru bugünkü listeden çıkıyor | KULLANICI (2026-10-17, v1.0.3) | [x] |
| 2.35 | Tekrar geçmişi boş durumu, newest-first sırası ve planlanan/gerçekleşen gün/sonuç/çözüm alanları repository'den doğru geliyor | YZ (CI run `39033698686`) | [x] |
| 2.36 | v1.0.4 soru ayrıntısında tekrar geçmişi cihazda okunaklı ve güncel gösteriliyor | KULLANICI (v1.0.4) | [ ] |

## Aşama 3 — Yedek ve geri yükleme

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| 3.1 | Tam ZIP oluşturma, manifest/CSV/JSON ve tüm envanter SHA-256/uzunluk doğrulaması | YZ (CI run `39030667492`) | [x] |
| 3.2 | Yedek cihazdaki bir klasöre kaydediliyor | KULLANICI | [ ] |
| 3.3 | Yedek Drive'a kaydediliyor (dosya seçici akışı) | KULLANICI | [ ] |
| 3.4 | Temiz kurulumda yedekten geri yükleme çalışıyor | KULLANICI | [ ] |
| 3.5 | Yedek görselleri kapsıyor (geri yükleme sonrası görseller açılıyor) | KULLANICI | [ ] |
| 3.6 | Yedek tekrar geçmişini ve plan durumunu kapsıyor | KULLANICI | [ ] |
| 3.7 | Checksum'u bozulmuş ZIP önizleme aşamasında reddediliyor; DB/görsel kayıtları değişmiyor | YZ (CI run `39030667492`) | [x] |
| 3.8 | Desteklenmeyen format sürümü reddediliyor; DB değişmiyor | YZ (CI run `39030667492`) | [x] |
| 3.9 | Başarısız yedek işlemi başarı gibi gösterilmiyor | YZ | [ ] |
| 3.10 | Tam geri yükleme öncesi doğrulanmış güvenlik ZIP'i oluşuyor, eski not/görsel verisi korunuyor | YZ (CI run `39030667492`) | [x] |
| 3.11 | v1.0.3 güncellemesi mevcut verileri koruyor (gerçek cihaz verisiyle) | KULLANICI (2026-10-17) | [x] |
| 3.12 | ZIP içi yol güvenliği: `..` içeren ZIP hiçbir dosya çıkarmadan reddediliyor | YZ (CI run `39030667492`) | [x] |
| 3.13 | Günlük/Room işareti simülasyonunda transaction öncesi rollback ve commit sonrası tamamlatma doğru çalışıyor | YZ (CI run `39030667492`) | [x] |
| 3.14 | ZIP bomb / aşırı büyüme / çok fazla dosya içeren arşiv açılmadan reddediliyor | YZ | [ ] |
| 3.15 | Yedek **başka bir cihazda** (cihaz bağlılığı olmadan) eksiksiz geri yükleniyor | KULLANICI | [ ] |
| 3.16 | Anlamsız/boş yedek seçilince kullanıcı onayı olmadan hiçbir şey değişmiyor | YZ + KULLANICI | [ ] |
| 3.17 | Son yedek bilgisi (tarih, boyut, dosya adı) doğru gösteriliyor | KULLANICI | [ ] |
| 3.18 | Gerçek cihazda geri yükleme sırasında uygulama kapatılır; sonraki açılışın kurtarması doğrulanır | KULLANICI | [ ] |

## Aşama 4 — Tekrar sistemi

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| 4.1 | "Bugün" listesi vadesi gelen soruları gösteriyor | KULLANICI (2026-10-17, v1.0.3) | [x] |
| 4.2 | Gecikmiş sorular gün sayısı/etiketiyle doğru gösteriliyor | KULLANICI (2026-10-17, v1.0.3) | [x] |
| 4.3 | Birim testinde "Çözemedim" sonrası aralık 1 güne dönüyor | YZ (CI run `39030667492`) | [x] |
| 4.4 | Birim testinde "Rahat çözdüm" sonrası aralık büyüyor ve 180 günde sınırlanıyor | YZ (CI run `39030667492`) | [x] |
| 4.5 | Çözüm açıldığında bu bilgi gerçek cihazdaki tekrar akışında kaydediliyor | KULLANICI (v1.0.3) | [ ] |
| 4.6 | Birim testinde çözüm açılmış tekrarın sonraki tarihi 1 gün sonrası oluyor | YZ (CI run `39030667492`) | [x] |
| 4.7 | Çözüm açıldıktan sonra aynı gün ikinci deneme kaydı oluşmuyor | YZ | [ ] |
| 4.8 | v1.0.4 soru ayrıntısında tekrar geçmişi gösteriliyor (planlanan/gerçekleşen gün ve sonuç) | KULLANICI | [ ] |
| 4.9 | Bildirim izni verilmese de "Bugün" listesi çalışıyor | KULLANICI | [ ] |
| 4.10 | Elle tarih atama çalışıyor | KULLANICI | [ ] |
| 4.11 | Saat dilimi değişiminde gün hesabı bozulmuyor (kayıtlı gün değişmiyor) | YZ + KULLANICI | [ ] |
| 4.12 | `ASKIDA` / `TAMAMLANDI` sorular "Bugün" listesinden çıkıyor, kayıt silinmiyor, geri alınabiliyor | KULLANICI | [ ] |
| 4.13 | Uzun aradan sonra hiçbir soru otomatik sıfırlanmıyor; gecikmişler tek listede toplanıyor | YZ + KULLANICI | [ ] |
| 4.14 | Aralık hesabı unit testleri (kolayda artış/üst sınır, zorlanma/başarısızlık ve çözüm açmada yarına planlama) | YZ (CI run `39030667492`) | [x] |

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
| G.2 | ZIP/APK kullanıcı yedekleri repoya işlenmiyor (`.gitignore` + `git check-ignore`) | YZ | [x] |
| G.3 | İmzalama anahtarının iki ayrı yedek kopyası kullanıcıda mevcut | KULLANICI | [ ] |
| G.4 | Uygulama kaldırılırsa verilerin kaybolacağı kullanıcı tarafından biliniyor | KULLANICI | [ ] |
| G.5 | Room v1→v2 migration ve veri koruması otomatik test edildi; gelecek migration'larda zincir korunacak | YZ (son CI run `39030667492`) | [x] |
| G.6 | Debug ve release APK'ları karıştırılmıyor; günlük kullanımda hep release APK | KULLANICI | [ ] |
| G.7 | Derleme, sabitlenmiş sürümlerle tekrar edilebiliyor (bir yıl sonra da) | YZ | [ ] |

---

## Raporlama kuralı

Her geliştirme adımının sonunda şu ayrım yazılır:

1. **Gerçekten çalıştırılan testler** (komut/çıktı ile),
2. **Yalnızca gözden geçirilenler** (kod okuması, statik değerlendirme),
3. **Kullanıcının cihazında test etmesi gerekenler**.
