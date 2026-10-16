# Proje Fikri — Yanlış Defteri (Android)

> Bu metin, **başka bir yapay zekâdan veya kişiden görüş almak** için tek başına okunacak şekilde
> hazırlandı. Tamamını kopyalayıp gönderebilirsin. Metin kasten taraflı yazılmadı: ürün kararları
> kesinleşmiş tercihler olarak, **teknik yaklaşım ise öneri olarak** sunuldu; itiraza açıktır.

Son güncelleme: 2026-10-16

---

## 1. Tek cümleyle fikir

Android telefonda çalışan, tek kişilik, çevrimdışı bir **dijital yanlış defteri**: yanlış yapılan
veya tekrar çözülmek istenen soruların fotoğrafı eklenir, soru bir derse (isteğe bağlı olarak bir
konuya) atanır, çözüm ayrı tutulur ve uygulama "bugün hangi soruları tekrar çözmeliyim?" listesini
kendisi çıkarır.

## 2. Kullanıcı ve cihaz

- Tek kullanıcı: lise düzeyinde ders çalışan bir öğrenci (üniversiteye hazırlık / TYT-AYT çalışması).
- Ana cihaz: **Samsung Galaxy S24 FE** (Android). Testler bu cihazda yapılır.
- Uygulama kişisel kullanım için; mağazada yayımlanması gerekmiyor, APK kurularak kullanılacak.
- Kullanıcı **teknik işlem yapmak istemiyor**: telefonda geliştirme ortamı kurmayacak. Kod yazma,
  yapılandırma ve derleme işleri yapay zekâ + bulut CI (GitHub Actions) üzerinden yürüyecek.

## 3. Hangi sorunu çözüyor?

Yanlış yapılan sorular tipik olarak telefonun galerisinde dağınık fotoğraflar hâlinde birikiyor.
Bu yetersiz kalıyor çünkü:

- Hangi soruyu ne zaman tekrar çözeceği belli olmuyor; galeri "hatırlatma" yapmıyor.
- Çözüm aynı fotoğrafın içinde olunca, soruyu tekrar çözmek yerine çözüme bakmak kolaylaşıyor.
- Derse/konuya göre filtreleme, "konusuz" soruları ayrı görme gibi bir düzen yok.
- Kaynak kitap/sayfa gibi bilgiler kayboluyor.

Uygulamanın amacı **fotoğraf arşivi olmak değil**; tekrar çalışmayı kolaylaştırmak ve çözümü,
öğrenme sürecinden ayrı tutmak.

## 4. Temel kullanım akışları

**Soru ekleme (mümkün olduğunca kısa olmalı):**
kamera ile çek veya galeriden seç → (isteğe bağlı) döndür → **ders seç** → kaydet.
Konu ve diğer tüm alanlar isteğe bağlıdır; sonradan eklenip değiştirilebilir.

**İsteğe bağlı alanlar:** konu, kısa soru notu, çözüm fotoğrafı/görselleri, çözüm açıklaması, kaynak
(kitap/sayfa/soru no), yanlış yapma nedeni, favori işareti.

**Soru açma / tekrar çözme:**
önce yalnızca soru görünür → gerekirse büyütülür → **"Çözümü göster"** düğmesine basılmadıkça çözüm
görünmez → çözüm açıldığında bu, kayıt altına alınır → ardından değerlendirme yapılır:
**Çözemedim / Zorlanarak çözdüm / Rahat çözdüm**.

**"Bugün" ekranı:** tekrar zamanı gelen sorular + gecikmiş tekrarlar + boş durumda açıklayıcı mesaj.
Bildirim izni verilmese bile bu liste çalışmalı.

**Yedekleme:** uygulama tam bir ZIP yedeği üretir; kullanıcı bunu Android'in dosya seçicisiyle
cihazdaki bir klasöre veya Drive'a kaydeder. Geri yükleme, kullanıcının seçtiği yedek dosyasından
yapılır.

## 5. Kesin kısıtlar (bunlar tartışmaya kapalı)

1. **Çevrimdışı öncelikli**; temel işlevler internet olmadan çalışmalı.
2. **Yerel veri tek doğruluk kaynağı.** Sürekli bir uzak sunucu/veritabanı (Supabase, Firebase,
   Render vb.) kullanılmayacak. Üyelik/hesap sistemi yok.
3. **Drive bir yedek hedefi**, senkronizasyon sistemi değil. Çoklu cihaz eşzamanlı düzenleme ve
   çakışma çözümü kapsam dışı.
4. Veriler kullanıcının kendi dosyasına aktarılabilir olmalı (taşınabilirlik).
5. Ana cihaz Android; iOS ve web hedefi yok.
6. Kullanıcı minimum teknik işlem yapacak; derleme bulutta olacak, kurulum APK ile yapılacak.
7. Uygulama **2–3 yıl** sorunsuz kullanılmalı; güncellemeler mevcut veriyi koruyarak üzerine
   kurulabilmeli (yani imzalama ve veritabanı göçleri baştan doğru kurgulanmalı).

## 6. Ürün kararları (kesinleşmiş tercihler)

- Başlangıçta 7 ders: Edebiyat, Matematik, Biyoloji, Kimya, Fizik, Coğrafya, Tarih.
- Yapı: **Ders → Konu → Soru**. Konu zorunlu değil; konusuz sorular ayrıca listelenir.
- TYT–AYT ayrımı yok.
- Ders ve konular düzenlenebilir (yeni ekleme, yeniden adlandırma). Silme yerine **arşivleme**
  yapılır ki mevcut soruların ilişkileri bozulmasın.
- Bir soruya **birden fazla görsel** eklenebilir (çok sayfalı sorular); soru görselleri ve çözüm
  görselleri ayrı alanlardır.
- Çözüm varsayılan olarak gizlidir. "Çözümü açtım" bilgisi kaydedilir; böylece
  **"çözümü görünce anladım"** ile **"yardımsız çözdüm"** aynı sayılmaz.
- Öğrenme durumu ile tek bir tekrarın sonucu **ayrı kavramlar** olarak tutulur (ham tekrar geçmişi
  saklanır, üzerine yazılmaz).
- Yedek: ZIP (manifest + JSON + CSV + görseller). CSV yalnızca başka programda incelemek için.
- Geri yükleme ilk sürümde yalnızca **"tamamen değiştir"**; öncesinde mevcut arşivin otomatik
  güvenlik kopyası alınır. Sessiz veri kaybı olmamalı.
- İlk sürümde yedek şifrelemesi yok.

## 7. Önerilen teknik yaklaşım (BURASI TARTIŞMAYA AÇIK)

Aşağıdaki seçimler bir öneri olarak yapıldı, kesin karar değil. **Özellikle bu bölümü eleştirmeni
istiyoruz.**

- Native Android uygulaması, **Kotlin** + **Jetpack Compose**.
- Yerel veritabanı: **Room / SQLite** (göç/migration desteği nedeniyle).
- Görseller: uygulamanın kalıcı dosya alanı (`filesDir`), gerektiğinde kontrollü küçültme.
  Görsel yükleme için Coil gibi yaygın bir kütüphane.
- Tek Gradle modülü; bağımlılık enjeksiyonu için Hilt gibi bir çerçeve **kullanılmaması** (basit bir
  elle kurulum yeterli görülüyor).
- Kaynak kodu GitHub'da; **GitHub Actions** ile elle tetiklenen derleme; APK'nın GitHub **Releases**
  üzerinden telefondan indirilmesi.
- Sabit bir **release imzalama anahtarı** (GitHub Secret olarak) — çünkü imza değişirse güncelleme
  kurulamaz ve uygulamayı kaldırmak gerekir ki bu yerel veriyi siler.
- Basit aralıklı tekrar yaklaşımı: 1 · 3 · 7 · 21 · 45 · 90 gün (üst sınır ~180 gün);
  "çözemedim" → 1 güne döner; çözüm açıldıysa aralık kısalır.

### Tasarlanan veri modeli (özet)

- `dersler(id, ad, sira, arsivli)`
- `konular(id, ders_id, ad, sira, arsivli)`
- `sorular(id, ders_id, konu_id?, soru_notu?, cozum_notu?, kaynak?, yanlis_nedeni?, favori, durum,
  son_tekrar_gun?, sonraki_tekrar_gun?, olusturma, guncelleme)`
- `gorseller(id, soru_id, tur[SORU|COZUM], sira, dosya_adi, genislik, yukseklik, boyut)`
- `tekrarlar(id, soru_id, zaman, gun, sonuc[COZEMEDIM|ZORLANDIM|RAHAT], cozum_acildi, onceki_aralik,
  sonraki_aralik, elle_atandi)`

Notlar: kimlikler UUID metin (ileride içe aktarma/birleştirme kolaylığı için); veritabanında dosya
yolu değil yalnızca dosya adı tutulur; kayıt anları epoch ms, tekrar tarihleri gün numarası.

### Beklenen ölçek

- 2–3 yılda birkaç bin soru (ör. 3.000–8.000) ve soru başına ortalama 1–3 görsel öngörülüyor.
- Bu, depolama ve liste performansı açısından ciddi bir mühendislik gerektirir mi, yoksa sıradan bir
  yerel veritabanı + örnekleme (thumbnail) yeterli mi — **ölçüm yapılmadan iddia edilmiyor.**

## 8. Kapsam dışı (ilk sürümlerde olmayacak)

Sosyal özellikler, arkadaş ekleme, öğretmen paneli, çok kullanıcılı hesap, çoklu cihaz canlı
senkronizasyon, web paneli, iOS, reklam, abonelik, uzak veritabanı, OCR/yapay zekâ ile otomatik soru
çözme, karmaşık oyunlaştırma, ayrıntılı istatistik panelleri, PDF çalışma kâğıdı (ileride olabilir).

## 9. Kabul kriterleri (ürün "kullanılabilir" sayılmadan önce)

- Uçak modunda soru eklenebiliyor.
- Uygulama kapanıp açıldığında ve telefon yeniden başlatıldığında kayıtlar duruyor.
- Galerideki kaynak görsel silinse bile soru açılıyor (uygulama kendi kopyasını tutuyor).
- Kamera/galeri işlemi iptal edilince uygulama bozulmuyor; yarım kalan dosya işlemi bozuk kayıt
  oluşturmuyor.
- Konusuz soru eklenebiliyor ve bulunabiliyor.
- Soru ve çözüm ayrı gösteriliyor; görseller büyütülebiliyor.
- Uygulama güncellemesi mevcut veriyi koruyor.
- Yedek görselleri ve tekrar geçmişini kapsıyor; temiz kurulumda geri yükleme çalışıyor.
- Bozuk yedek mevcut arşivi bozmuyor; başarısız yedek işlemi "başarılı" gibi gösterilmiyor.
- Silme işlemleri kullanıcıya sürpriz veri kaybı yaşatmıyor.

## 10. Riskler / zayıf noktalar (farkındayız)

- Bir Android uygulaması yıllarca yaşamalı: AGP/Kotlin/Compose sürüm değişimleri, göç (migration)
  disiplini ve yedek formatı sürümlemesi sürekli bakım ister.
- İmzalama anahtarı kaybedilirse güncelleme zinciri kopar.
- Android'in sistem yedeği (Auto Backup) uygulama başına ~25 MB ile sınırlı; fotoğraf arşivi bu
  sınırı aşar, dolayısıyla sistem yedeği güvenilir bir güvenlik ağı değil.
- Dosya seçicisiyle Drive'a yazmak, "buluta yükleme tamamlandı" demek değil; arayüz bunu
  doğrulanmış gibi gösteremez.
- Uygulama kaldırılırsa yerel veri kaybolur.

## 11. Somut olarak görüşünü istediğimiz sorular

1. **Teknoloji seçimi:** Kotlin + Compose + Room, bu ihtiyaçlar ve "tek kişi geliştiriyor, 3 yıl
   yaşamalı" kısıtı için doğru tercih mi? Daha basit ve daha uzun ömürlü bir alternatif var mı
   (ör. farklı bir yerel depolama yaklaşımı, farklı bir çatı, hatta farklı bir platform tercihi)?
   Gerekçeleriyle söyle.
2. **Aşırı mühendislik:** Bu planda gereksiz karmaşık bulduğun, ilk sürümden çıkarılması gereken
   şeyler var mı? Buna karşılık, hafife aldığımız kritik bir eksik var mı?
3. **Veri modeli:** Yukarıdaki şemada eksik, yanlış veya ileride acı verecek bir tasarım var mı?
   Özellikle "ders/konu arşivleme", "çok görsel" ve "öğrenme durumu ≠ tekrar sonucu" ayrımları
   hakkında ne dersin?
4. **Tekrar algoritması:** 1-3-7-21-45-90 gibi sabit aralıklar mı, yoksa SM-2 benzeri basit bir
   algoritma mı daha uygun? Bu tür bir kullanıcı için "yeterince iyi ama anlaşılır" dengeyi nasıl
   kurardın?
5. **Görseller ve ölçek:** Birkaç bin soru ve görselde saklama, sıkıştırma (metin/formül okunabilirliği
   korunarak), önizleme üretimi ve liste performansı için ne önerirsin? Hangi çözünürlük/kalite
   dengesi makul?
6. **Yedekleme:** ZIP + dosya seçici yaklaşımı yeterli mi? Artımlı/kısmi yedek gerekli mi? Drive
   gerçeği (buluta yüklemenin doğrulanamaması) için arayüz nasıl davranmalı? Bozuk yedeği reddetme
   ve "tamamen değiştir" geri yükleme dışında önerdiğin bir güvenlik deseni var mı?
7. **Uzun ömür:** 3 yıl boyunca bu uygulamanın bakımını kolaylaştırmak için baştan yapılması gereken
   ama kolayca atlanan şeyler neler? (sürüm sabitleme, göç testleri, yeniden üretilebilir derleme,
   dokümantasyon vb.)
8. **Kaçırdığımız senaryolar:** Telefon değiştirme, depolama dolması, saat dilimi değişimi, uzun süre
   kullanmama, düşük pil/temizleyici uygulamalar, Samsung'a özgü arka plan kısıtları gibi durumlarda
   neyi baştan düşünmeliyiz?

**Ek olarak:** Yukarıdaki planda "böyle yapmak yerine şöyle yapmalıydın" dediğin en önemli tek şey
ne olurdu?
