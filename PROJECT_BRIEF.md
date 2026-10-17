# PROJECT BRIEF — Yanlış Defteri

Bu belge projenin **karar kaydıdır**. Yeni bir geliştirme oturumu (insan veya yapay zekâ) önce bu
belgeyi, sonra [HANDOFF.md](HANDOFF.md) dosyasını okur.

- Son güncelleme: 2026-10-17
- Durum: **v1.2.0 (versionCode 14) yayımlandı**. CI `39067624050`: 130 test, Release/imza ve sertifika doğrulaması başarılı. 30 rozet/15 rütbe, gizli kutlamalar, sınav profilleri ve Odak Stüdyosu eklendi; Room/ZIP v3 korunur. Fiziksel cihaz kabulü ayrı ve hâlâ bekliyor; ayrıntı `HANDOFF.md` ve `docs/TEST_KONTROL_LISTESI.md`.

---

## 1. Amaç

Android telefonda kullanılacak, tek kişilik bir dijital yanlış defteri:

- Yanlış yapılan veya tekrar çözülmek istenen soruların fotoğrafı çekilir/galeriden seçilir.
- Soru bir **derse** ve isteğe bağlı bir **konuya** atanır.
- Sorular daha sonra **tekrar çözülür**; çözüm ayrı tutulur ve varsayılan olarak gizlidir.
- Uygulama, "bugün hangi soruları tekrar çözmeliyim" sorusunu kolayca cevaplar.

## 2. Kullanıcı ve cihaz

- **Tek kullanıcı**, kişisel kullanım. Çok kullanıcılı yapı, sosyal özellik, öğretmen paneli yok.
- Ana cihaz: **Samsung Galaxy S24 FE** (Android). Testler öncelikle bu cihazda yapılır.
- iOS ve web sürümü hedeflenmiyor.
- Play Store'da yayımlamak ilk sürüm için gerekli değil; APK doğrudan kurularak test edilir.
- Kullanıcı, geliştirme ortamıyla uğraşmak istemiyor. Kodlama, proje düzeni ve teknik değişiklikler
  ağırlıklı olarak yapay zekâ tarafından hazırlanır. Telefonda Android SDK kurulması veya zorunlu
  Termux kullanımı **beklenmez**.

## 3. Hedefler

| Hedef | Not |
|---|---|
| 2–3 yıl rahat kullanım | Garanti değil; sürdürülebilir tasarım, migration disiplini |
| Verilerin güvenli ve taşınabilir olması | Dışa aktarılabilir tam ZIP yedeği |
| Telefonda hızlı ve zahmetsiz kullanım | Kısa kayıt akışı, büyük dokunma alanları |
| Çevrimdışı temel işlev | Uçak modunda soru ekleme ve listeleme çalışır |
| Sunucu yönetimi olmaması | Supabase, Firebase, Render vb. kullanılmaz |
| Kullanıcıdan minimum teknik işlem | Derleme GitHub Actions'ta; kurulum APK ile |

---

## 4. Kesinleşen ürün kararları

| # | Karar |
|---|---|
| K1 | Başlangıçta 7 ders: Edebiyat, Matematik, Biyoloji, Kimya, Fizik, Coğrafya, Tarih. |
| K2 | TYT/AYT/LGS/KPSS **görünüm profilleri** vardır (v1.2.0). Temel ilişki hâlâ Ders → Konu → Soru; profil değişimi hiçbir kaydı silmez veya taşımaya zorlamaz. |
| K3 | Konu seçimi **zorunlu değil**. Sadece ders seçerek hızlı kayıt yapılabilir; konu sonradan eklenip değiştirilebilir. |
| K4 | Konusuz sorular kaybolmaz; "Konusuz" olarak açıkça listelenir ve erişilebilir. |
| K5 | v1'de yedi lise dersi sabittir; ders ekleme/yeniden adlandırma/arşivleme arayüzü olmayacak. Ders seçimi yalnızca soruları düzenlemek içindir. |
| K6 | Konu yönetimi derslerden ayrıdır: konular eklenip düzenlenebilir; kaldırma arşivleme ile yapılır ve bağlı sorular korunur. |
| K7 | **Çok görsel desteği** hem şemada hem arayüzde vardır. Bir soru ve bir çözüm için birden fazla sayfa eklenebilir ("bir sayfa daha ekle"). |
| K8 | Soru ve çözüm **ayrı alanlardır**. Çözüm varsayılan olarak gizlidir, "Çözümü göster" ile açılır. |
| K9 | Çözümün açıldığı bilgisi **kaydedilir**; "çözümü görünce anladım" ile "yardımsız çözdüm" aynı sayılmaz. |
| K10 | Tekrar değerlendirmesi 3 seçenek: **Çözemedim / Zorlanarak çözdüm / Rahat çözdüm** + çözümün açılıp açılmadığı işareti. |
| K11 | Yedek, kullanıcının seçtiği konuma yazılan tam bir ZIP dosyasıdır. Drive bir **hedef konumdur**, senkronizasyon sistemi değildir. |
| K12 | Arayüz, doğrulayamadığı bir bulut yüklemesini "tamamlandı" olarak göstermez. |
| K13 | Geri yükleme v1'de yalnızca **"tamamen değiştir"**; öncesinde mevcut arşivin otomatik güvenlik kopyası alınır. |
| K14 | Ürünün temel özellikleri için üyelik/uygulama hesabı gerekmez. |
| K15 | Otomatik şifreleme v1'de yok (anahtar kaybı riski ve CSV'nin okunabilirliği nedeniyle ayrı değerlendirilecek). |
| K16 | Soru ekleme akışı: fotoğraf çek/seç → (isteğe bağlı döndür) → ders seç → kaydet. Diğer tüm alanlar isteğe bağlıdır. |
| K17 | Sorunun **plan durumu** (`AKTIF` / `ASKIDA` / `TAMAMLANDI`) öğrenme düzeyinden **ayrı** bir alandır. Kullanıcı bir soruyu tekrar planından geçici olarak çıkarabilir veya kalıcı olarak bitirebilir; kayıt ve geçmiş silinmez, işlem geri alınabilir. |
| K18 | Tekrar aralığı kuralı **sürümlenmiş tek bir sınıfta** tutulur (`algoritma_surumu`); ileride algoritma değişirse geçmiş kayıtlar yeniden yazılmaz. |
| K19 | Çözüm açılarak yapılan tekrar **bağımsız deneme sayılmaz**. Aynı gün ikinci deneme yapılmaz (çözüm görüldüğü için bu gerçek hatırlama olmaz); soru **1 gün sonra** yeniden karşımıza çıkar ve bu kez bağımsız olarak çözülür. |
| K20 | Uzun bir aradan sonra **hiçbir soru otomatik olarak sıfırlanmaz**; gecikmiş sorular tek listede toplanır ve kullanıcı ne yapacağını kendisi seçer. |
| K21 | **Görsel kalite politikası:** fotoğraflar kaydedilirken otomatik optimize edilir (uzun kenar ~3000 piksel, JPEG kalitesi ~%90). Tek ölçüt **okunabilirliktir**: küçük formüller, kök içi ifadeler ve grafik etiketleri okunur kalmalı. Bu, Aşama 2'de gerçek fotoğraflarla doğrulanır. |
| K22 | Ana ekranın odağı **Bugün**. Gezinti beş alt sekmededir; yedekleme, bakım ve katalog Ayarlar içindedir. Üst sağda tekrar eden Ayarlar kısayolu yoktur. |
| K23 | **Sınav formatı seçimi Ayarlar'da:** LGS, TYT, AYT, KPSS Genel Yetenek/Genel Kültür ve Tümü / özel. Eksik dersler eklenir; mevcut ders ID'leri ve kayıtlar korunur. Tümü seçimi tüm aktif dersleri geri gösterir. |
| K24 | Kaynak kataloğu dersler arasında ortaktır; yeni kaynak `+` ile kaydedilir. Her soru için isteğe bağlı sayfa/test/soru konumu tutulur. Yanlış nedeni hazır listeden **tek ana seçim** veya kullanıcı tarafından eklenen özel seçenektir. Soru notu giriş/gösterim alanı kaldırılır; önceki `soru_notu` verisi migration, düzenleme ve ZIP yedeklerinde korunur. |
| K25 | **Sonuç → karne eşlemesi:** `Rahat` = bağımsız çözüm, `Zorlandım` = yarım (destekli), `Çözemedim` = çözemedi. Çözüm açıldıysa hangi sonuç seçilirse seçilsin soru bağımsız sayılmaz. Ayrıntı: `docs/SONUC_KARNESI_VE_ILERLEME.md`. |
| K26 | **Kutlama tonu:** karne ekranı animasyonlu ve rozetli olacak; tebrik cümlesi skora göre değişir, olumsuz/cezalandırıcı dil ve üzgün animasyon kullanılmaz. |
| K27 | **Günlük hedef 5/10/15/20/30 seçilebilir** (v1.1.0+), varsayılan 10. Hedef tercihi cihaz-yereldir; mola günü jetonu ve ilk-kurulum sihirbazı bu sürümde yoktur. |
| K28 | Karne karşılaştırması **önceki takvim haftasına** (Pzt–Paz) göre yapılır; geçen haftada yeterli kayıt yoksa satır gizlenir. |
| K29 | Kısa oturum eşiği **şimdilik 5**, tek sabitten (`KISA_OTURUM_ESIGI`) okunur; ileride değişken olacak. Karne oturum bitince **otomatik** açılır. |
| K30 | “Bu konudan çalış” düğmesi filtreyi yalnız seçilen konuya kurar, diğer filtreleri temizler ve Filtreli çalışma ekranını açar. |
| K32 | **Gezinme alt menüdedir** (v1.1.0): Bugün · Dersler · Filtreli · İlerleme · Ayarlar. Yan menü kaldırıldı; yedek/görsel bakımı/katalog Ayarlar altına taşındı. |
| K33 | **XP gerçek emekten türetilir:** soru +10, tekrar +5, bağımsız +5, günlük hedef +25, seri 3/7/30 +30/+70/+300, rozet +50. Yalnız RAHAT ve çözüm açılmamış kayıt bağımsızdır. Eşikler artar; **15 rütbe** sonrası da seviye büyür. Ayrı XP tablosu yoktur. |
| K34 | **Silme kuralları:** konu, soruda kullanılıyorsa silinmez (uyarı + arşivleme önerisi); kaynak/yanlış nedeni kullanımdaysa silinmez; hazır katalog seçenekleri hiçbir zaman silinemez. Sessiz veri kaybı yoktur. |
| K31 | **Karne uygulama notları (v1.0.10):** kova/oran hesabı `KarneHesabi`; seri ve hafta penceresi `SeriHesabi`/`HaftaPenceresi`; ilerleme rakamları `IlerlemeHizmeti` ile `tekrarlar` tablosundan anlık hesaplanır (yeni tablo yok). Rozetler `SharedPreferences` (`karne_rozetleri/kazanilanlar`) içinde tutulur ve ZIP yedeğine girmez; kazanım idempotenttir. İstikrar rozeti üst üste 5 **gün** (her gün ≥5 soru ve ≥%70 bağımsız) olarak uygulanır. Haftalık karşılaştırma eşiği `KISA_OTURUM_ESIGI` ile aynı sabitten okunur. |
| K35 | **30 rozet, 5 gizli:** açık koşul/ilerleme, gizli rozetlerde kazanılana kadar ad+çizim+koşul kapalı. Sahiplik ve tam ekran kutlama kuyruğu atomik kaydedilir; yeni rozet +50 yalnız bir kez verilir. |
| K36 | **Odak Stüdyosu** öz değerlendirmedir; otomatik doğrulama/AI cevap üretimi yapmaz. Mola, sona bırak, çözüm açmadan sonuç seçme, ayrı Kaydet ve ilerle eylemi, karne ve kalıcı devam noktası vardır. |
| K37 | **Adaptif v2** yeni tekrarları sürüm 2 yazar; eski kayıtları değiştirmez. Kolaylık/aralık/unutma mevcut geçmişten türetildiği için Room v3 / ZIP v3 korunur; eski v4 kolon önerisi bu uygulamada gerekli değildir. |

### Soru kaydındaki alanlar

Zorunlu: kalıcı benzersiz kimlik, ders, en az bir soru görseli, oluşturma/güncelleme tarihi.

İsteğe bağlı: konu, ek soru görselleri, çözüm görsel(ler)i, çözüm notu/açıklaması, ortak
katalogdan kaynak seçimi ve soruya özel sayfa/test/soru konumu, tek ana yanlış nedeni, favori/önemli
işareti. Soru notu alanı arayüzde yoktur; eski kayıtları silmeden saklamak/yedeklemek sürdürülür.

---

## 5. Teknik kararlar

| Konu | Karar | Gerekçe |
|---|---|---|
| Platform | Native Android | Kamera, dosya seçici, bildirim, zamanlama doğrudan; köprü katmanı ve ikinci ekosistem yok |
| Dil / Arayüz | Kotlin + Jetpack Compose | Android'in birinci sınıf, uzun ömürlü yolu |
| Yerel veritabanı | **Room / SQLite** | Sorgu, indeks, **şema migration** desteği uzun ömür için gerekli |
| Görsel yükleme | **Coil** | Standart, bakımı sürdürülen çözüm; kendi önbellek kodunu yazmamak için |
| Proje düzeni | **Tek Gradle modülü** | Bu ölçekte çok modül sadece yavaşlık ve bakım yükü |
| Bağımlılık enjeksiyonu | Çerçeve **yok** (basit `AppContainer`) | Hilt, derleme süresi ve sürüm uyum riski ekler |
| Görsel saklama | Uygulamanın kalıcı dosya alanı (`filesDir`) | Önbellekte tek kopya olmaz; galeri silinse de kayıt bozulmaz |
| Min. Android | **minSdk 26 (Android 8)** | Ek kod maliyeti yok, geniş uyum |
| Hedef SDK | Aşama 1'de güncel kararlı sürüm olarak sabitlenir | Güncel gerekliliklere uyum |
| Kaynak kodu | GitHub deposu | Sürüm kontrolü ve oturumlar arası devamlılık |
| Derleme | GitHub Actions (push ile otomatik) | Kullanıcının cihazında derleme ortamı gerektirmez; workflow ölçüm/özet raporunu repoya yazar |
| Dağıtım | GitHub **Releases** üzerinden APK | Telefondan doğrudan indirilir; artifact ZIP'i ve saklama süresi sorunu yok |
| İmzalama | **Sabit release anahtarı**, GitHub Secret'ta | Aynı imza olmadan güncelleme üzerine kurulamaz; kaldırmak veri kaybı demektir |

### Mimari ilkeler

- Yerel veri **tek doğruluk kaynağıdır**; ağ bağlantısı olmadan temel uygulama çalışır.
- Gereksiz katman, çok modüllü yapı ve ağır bağımlılık eklenmez.
- Buna rağmen veri, dosya yönetimi, yedekleme ve arayüz birbirine karışmaz. Tek modül içinde paket
  ayrımı: `data` (Room + DAO + repository), `domain` (tekrar/aralık kuralları), `images` (içe
  aktarma, dönüştürme, dosya yönetimi), `backup` (ZIP üretme/doğrulama/geri yükleme), `ui` (Compose
  ekranları). Bildirimler eklendiğinde `notifications` paketi açılır.
- **Veri yaşam döngüsü baştan doğru kurulur:** yedekleme, sonradan eklenen bir ayar düğmesi değil,
  veri katmanının sınırlarından biridir. Soru ekleme, güncelleme, migration ve geri yükleme aynı
  bütünlük kurallarına uyar.
- Yaygın ve bakımı sürdürülen kütüphaneler tercih edilir; sürümler `libs.versions.toml` içinde
  sabitlenir, Gradle wrapper depoda tutulur.

### Görsel işleme kuralları

- Kamera çekimi doğrudan uygulamanın kalıcı alanına yazılır (galeriye uğramaz, izin gerekmez).
- Galeriden seçimde sistem fotoğraf seçicisi (photo picker) kullanılır; kopya uygulamaya alınır.
- Yazma sırası: **geçici dosya → tamamlanınca kalıcı konuma taşı → veritabanı kaydı**. Yarım kalan
  işlem bozuk soru kaydı oluşturmaz; kalan öksüz dosyalar temizlenir.
- Gereksiz büyük dosyalar kontrollü küçültülür; **metin ve formüller okunmaz hâle gelmemelidir**.
- Liste ekranlarında küçük önizlemeler kullanılır; büyük görseller bellekte tam boyutla tutulmaz.
- Geniş depolama izni gibi gereksiz izinler istenmez.

---

## 6. Öğrenme durumu ve tekrar modeli

İki kavram **ayrı** tutulur:

1. **Tekrar kaydı (geçmiş):** her tekrarın tarihi, sonucu ve "çözüm açıldı mı" bilgisi. Ham veridir,
   üzerine yazılmaz.
2. **Öğrenme durumu (türetilmiş):** sorunun kendi üzerindeki durum + sonraki tekrar tarihi.

- Başlangıç aralıkları: **1 · 3 · 7 · 21 · 45 · 90 gün** (üst sınır ~180 gün). Kesin değil,
  kullanımda ayarlanabilir olacak şekilde tasarlanır.
- "Çözemedim" → aralık 1 güne döner.
- "Zorlanarak çözdüm" → daha yavaş ilerler.
- "Rahat çözdüm" → aralık büyür.
- **Çözüm açıldıysa** tekrar bağımsız sayılmaz; soru **1 gün sonra** yeniden karşımıza çıkar ve o
  zaman yardımsız çözülür. Aynı gün ikinci deneme kaydedilmez.
- Sorunun **plan durumu** (aktif/askıda/tamamlandı) ayrı tutulur; "Bugün" listesi yalnızca aktif
  soruları gösterir.
- Uzun aradan sonra hiçbir soru otomatik sıfırlanmaz; gecikmişler tek listede toplanır.
- Kullanıcı, sonucu seçtikten sonra sonraki tekrar tarihini elle değiştirebilmelidir.
- **Bugün yapılacaklar** ve **gecikmiş tekrarlar** ayrı ayrı görünür.
- Tekrar geçmişi saklanır ve görüntülenebilir.
- **Bildirim izni verilmese bile** uygulama içi "Bugün" listesi çalışır.

---

## 7. Kapsam dışı (aksi kararlaştırılmadıkça)

Sosyal özellikler · arkadaş ekleme · öğretmen paneli · çok kullanıcılı hesap sistemi · çoklu cihaz
canlı senkronizasyon · web yönetim paneli · iOS uygulaması · reklam · abonelik · uzak veritabanı ·
yapay zekâ ile otomatik soru çözümü · OCR · karmaşık oyunlaştırma · çok ayrıntılı istatistik panelleri.

Ayrıca v1'de yok: otomatik yedekleme, Drive API/OAuth bağlantısı, yedek şifreleme, kırpma ekranı,
"birleştir" tipi geri yükleme, PDF çalışma kâğıdı, bildirimler.

---

## 8. Açık sorular / sonra netleşecek noktalar

| # | Konu | Durum |
|---|---|---|
| ~~A1~~ | ~~Uygulama kimliği (applicationId)~~ | **Kapandı (2026-10-16): `com.keremtulu.questionnote`** — sabitlendi, değiştirilemez |
| A2 | Hedef SDK değeri | **API 37** seçildi ve G2 CI derlemesiyle doğrulandı (platform android-37.2); cihaz davranışı ayrıca test edilecek |
| A3 | `hasFragileUserData` (kaldırırken veriyi koru) | Denenecek; davranışı gerçek cihazda doğrulanmalı. Birincil güvenlik ağı yine kendi ZIP yedeğimiz |
| A4 | Kırpma (crop) ekranı | Aşama 5'e ertelendi; v1'de döndürme + tam ekran yakınlaştırma |
| A5 | Yedek şifreleme | Gerekirse konuşulacak; parola kaybı = veri kaybı riski |
| A6 | Doğrudan Drive bağlantısı | Aşama 6'da, önce SAF kalıcı izni + Drive klasörü yolu denenerek |
| A7 | Aralık değerlerinin kullanıcı tarafından ayarlanması | Aşama 4'te değerlendirilecek |
| A8 | GitHub Actions/Secret yazma yetkisi | Arena secret uçları **403** olmaya devam ediyor; kullanıcı iki secret'ı ekledi, G3 CI imzası ve sertifika parmak izi eşleşmesi değerlerin doğru olduğunu doğruladı |
| ~~A9~~ | ~~Görsel kalite varsayılanı~~ | **Kapandı (2026-10-16): K21 — otomatik optimize** (uzun kenar ~3000 px, JPEG ~%90; ölçüt okunabilirlik) |
| A10 | Depolama dolduğunda "eski görselleri yeniden sıkıştır" aracı | Aşama 5'te değerlendirilecek |
| A11 | Önizleme stratejisi (Coil önbelleği yeterli mi) | Aşama 5'te ölçümle karar verilecek |

---

## 9. Çalışma biçimi

- İletişim dili **Türkçe**; teknik kararlarda kısa gerekçe verilir.
- Kullanıcı, aynı anda çok sayıda soruyla bunaltılmaz; birbiriyle ilişkili birkaç önemli soru birlikte
  sorulur. Her aşamada kullanıcının yapacağı somut iş kısa ve açık yazılır.
- İstenmeyen özelliklerle kapsam büyütülmez.
- **Yapılmamış veya test edilmemiş hiçbir şey yapılmış gibi anlatılmaz.** Her geliştirme adımında
  şu ayrım açıkça yazılır: (a) gerçekten çalıştırılan testler, (b) yalnızca incelenen/kod okumasıyla
  değerlendirilen şeyler, (c) kullanıcının cihazında test etmesi gerekenler.
- Repo erişimi veya APK derleme imkânı kontrol edilmeden var sayılmaz.
- Güvenlik veya veri kaybı riski varsa açıkça belirtilir.
- Her önemli adımdan sonra ilgili belgeler (PROJECT_BRIEF, ROADMAP, HANDOFF, BUILDING, CHANGELOG)
  güncellenir.
- **Dışarıdan gelen öneriler gerekçeyle kabul veya reddedilir.** Bir öneri, "başka biri söyledi"
  diye değil, bu projenin kısıtlarına uyduğu ve gerçekten ihtiyaç doğurduğu için uygulanır.
  Reddedilen öneriler de gerekçesiyle HANDOFF'ta kayda geçer.

## 10. Bilinen ortam kısıtları

- Yapay zekâ çalışma ortamında **Java/Android SDK yoktur** ve Google/Maven/Gradle depolarına erişim
  kapalıdır. Bu nedenle APK **bu ortamda derlenemez**; derleme GitHub Actions'ta yapılır.
- Yapay zekâ, derleme loglarını okuyup hataları düzeltebilir, ancak uygulamanın **cihazdaki
  davranışını test etmiş sayılmaz**. Gerçek cihaz testleri kullanıcı tarafından yapılır.
- GitHub Actions kotası ve kullanım koşulları kurulum sırasında kontrol edilir; sınırsız/ücretsiz
  olduğu varsayılmaz.
