# Kullanım olgunluğu — 26 Ekim 2026

## Bu değişiklikte uygulananlar

### 1. Biriken tekrarlar: borç listesi yerine yönetilebilir tur

**Bulgu:** `BugunGunKarti` bütün bekleyen soruları bir `Column.forEach` içinde
oluşturuyordu. 100 soruda 100 satır aynı anda oluşturuluyor, başlatma düğmesi listenin
sonunda kalıyordu. Oturum zaten sınırlı olmasına rağmen ekran bu sınırı anlatmıyordu.

**Yeni davranış:**
- Toplam bekleyen soru sayısı ve geciken/bugüne planlanan ayrımı korunur.
- Ana ekranda en fazla üç önizleme oluşturulur.
- Önizleme ve oturum aynı akıllı öncelik sırasını kullanır: önce eski vadeler.
- Başlat düğmesi önizlemeden önce gelir.
- Tur boyutu kalan hedefe göre belirlenir; mevcut 5–30 soruluk sınır korunur.
  Hedef tamamlandıktan sonra isteğe bağlı beş soruluk kısa tur sunulur.
- Tur dışında kaç sorunun beklediği açıklanır. Örneğin 100 soru / 10 hedef /
  0 tamamlanan durumunda 10 soruluk tur, 3 önizleme ve sırada 90 soru vardır.
- Tarihler ileri taşınmaz, gecikme silinmez, tekrar yapılmış gibi kayıt yazılmaz.
  Açıklama ve bölüm görüntülemek ölçüm, XP veya soru planı üretmez.

`GunlukCalismaPlani` bu davranışı Compose'tan bağımsız, test edilebilir şekilde taşır.
Bu bir tüm-sorular tarayıcısı değildir; ayrıntılı soru erişimi Dersler üzerinden sürer.

### 2. Öğrenci ilerlemesi

**Bulgu:** Ayrıntılar altında emek, ritim, plan, dönem, gruplar, dersler ve süre
olmak üzere birbirini takip eden teknik menüler vardı. Öğrenci için karar vermek
veriyi bulmaktan daha zor hale geliyordu.

**Yeni sıra:**
1. Bugünkü gerçek hedef ve mevcut analizden üretilen tek çalışma önerisi.
2. Seçili ders kapsamındaki son yedi gün: aktif gün ve tekrar toplamı, gün gün kayıt.
3. Derslere göre tekrar/pekişme dağılımı.
4. Rütbe ve ayrı rozet koleksiyonu; bunlar artık çalışma kararının önüne geçmez.

“Ayrıntılı bilgi” menüsü kaldırıldı. Analiz ve süre hesaplama altyapısı silinmedi;
gelecekte gerçek ihtiyaç varsa kullanılabilir. Son yedi gün kartı, aynı sorunun farklı
günlerdeki tekrarlarını ayrı kayıt sayar. Sınav başarısı, net veya öğrenme garantisi
gibi sunulmaz; bugün tamamlanmadığı da açıklanır. Günlük hedef ile seçili analiz
kapsamı aynı ölçüm gibi birleştirilmez; kapsam etiketleri korunur.

### 3. Öğrenci ayarları

- Normal ayarlardan görsel demo girişi ve tekrar motoru/tanı kartı çıkarıldı.
- Öğretmen moduna geçiş **korundu**.
- Ders seçimi, günlük hedef, ses, yedekleme, geri yükleme ve katalog/konu/görsel
  bakım araçları korundu. Veri kurtarma işlevleri “gereksiz” sayılıp kaldırılmadı.
- Saklanmış motor tercihi değiştirilmedi. Demo ve tanı altyapısının kendisi silinmedi.

### 4. Yerleşim ve öğretmen kullanımı

- Öğretmen host'unun sıfır sistem inset'i yerine `safeDrawing` kullanılır. Üst
  durum çubuğu, çentik ve alt sistem alanı içerik alanından ayrılır; alt menünün
  tükettiği alan çocuklarda yeniden eklenmez.
- Öğrenci/öğretmen ana içerik alanları geniş ekranlarda en fazla 840 dp ile yatay
  ortalanır. Telefonda mevcut genişlik kullanılır. Kısa ekranları dikey ortalamak
  yerine kaydırılabilir içerikte üst/alt güvenli pay korunur.
- Birçok ana liste ve formda üst içerik payı 8–12 dp yerine 20 dp yapıldı.
- Öğretmen kartlarına sabit `koyuTema = false` yerine sistem tema tercihi verildi.
- Öğretmen tarihi artık yalnız ilk açılışta hesaplanmaz: gece yarısı, uygulamaya
  dönüş ve saat/tarih değişiminde yenilenir.
- Kuyrukta plan tarihi görünür; “Bugüne al / Yarına planla” form açmadan kullanılabilir.
  Sadece bekleyen kaydın plan tarihi ve güncelleme zamanı yazılır; çözülmüş veya
  silinmiş soru yeniden açılmaz. Not/görsel/cevap alanları yeniden kaydedilmez.
  İşlem sonucu snackbar ile bildirilir. Aktif gün filtresinden çıkan soru silinmez.

Room şeması, yedek biçimi ve uygulama sürümü değişmedi. Hesap, bulut, ağ servisi
ve bildirim izni eklenmedi.

## Sonraki adımlar — gerçek ihtiyaç sırasıyla

### Öğrenci

1. **Konuya geri dönüş:** Sadece yeterli tekrar kaydı olan ve son denemelerinde
   zorlanılan bir konu gösterilmeli. “Neden bu konu?” açıklaması ve doğrudan küçük
   tur olmalı. Kayıt yoksa zayıflık uydurulmamalı; mevcut öneri altyapısı kullanılmalı.
2. **Yardım bekleyen soru döngüsü:** Öğretmene sorulacak soru, çözüm öğrenildikten
   sonra tekrar planına dönebilmeli. Önce mevcut “çözdürdüm” akışının anlaşılabilirliği
   cihazda gözlenmeli; ayrı bir sosyal mesajlaşma sistemi gerekmiyor.
3. **Haftalık değerlendirme:** “Bu hafta daha iyisin” demeden önce yeterli ve
   karşılaştırılabilir soru/ölçüm olmalı. Yalnız tekrar sayısının artması başarı artışı
   değildir. Sonuç belirsizse sayılar ve neden karşılaştırma yapılmadığı söylenmeli.
4. **Birikmiş iş için seçim:** Gelecekte büyük kuyrukta ders bazlı küçük tur seçimi
   eklenebilir. Bunun için tekrar tarihlerini otomatik ertelemek veya birikmiş işi
   gizlemek yerine seçimin etkisi açıkça gösterilmeli.

### Öğretmen

1. **Soru → sınıfta çözüm → kapanış:** En önemli iş akışı budur. Hızlı planlama bu
   değişiklikte eklendi. Sıradaki ihtiyaç, çözüm sonrası “çözümü dahil paylaş” seçeneği
   ve hangi gruplarda işin tamamlandığını kolayca görebilmektir. Cevap ve “kim sordu”
   bilgisinin paylaşımda yanlışlıkla açılmaması şarttır.
2. **Bekleyen iş özeti:** Grup başına bekleyen, geciken, planlanmamış soru ve en eski
   bekleme tarihi yeterlidir. Öğrenciler sıralanmamalı; bu uygulamadaki “kim sordu”
   serbest metni bir öğrenci performans veritabanı değildir.
3. **Güvenli toplu işlemler:** Toplu silmenin onay/geri alma davranışı ile tekil silme
   davranışı aynı standarda taşınmalı. Özellikle filtre değişince görünmeyen seçimin
   işlem kapsamına yanlışlıkla girmediği cihazda sınanmalı.
4. **Yedek kabulü:** Yedek düğmesinin varlığı yetmez. Gerçek yedekten başka cihazda
   geri yükleme; grup, soru, çözüm görselleri ve plan tarihlerinin korunmasıyla
   doğrulanmalı. Bu, yeni grafiklerden daha yüksek önceliklidir.

## Doğrulama durumu

- `python3 tools/statik_kontrol.py`: **1192 kaynak, 0 hata**.
- `git diff --check`: geçti.
- Yeni saf JVM testleri: 100 geciken soru, 3 önizleme, kayıpsız kuyruk, tutarlı sıra,
  boş kuyruk, hedefin tamamlanması ve 30 soruluk üst sınır.
- Mevcut Room testine planın kalıcılığı, içerik koruması, planı kaldırma,
  bulunamayan/çözülmüş kaydın değiştirilememesi kontrolleri eklendi.
- Compose testleri yeni ilerleme hiyerarşisine uyarlandı; haftalık özet,
  koleksiyon durumunun geri yüklenmesi, geri tuşu, büyük yazı ve XP animasyon
  sözleşmeleri korunacak şekilde düzenlendi. Görsel test hedefleri de güncellendi.
- **Testler çalıştırılmış değildir:** `./gradlew --version` Java bulunmadığı için
  başlatılamadı (`JAVA_HOME` yok). Bu ortamda Android SDK da yapılandırılmamış.
  Dolayısıyla APK, JUnit sonucu veya yeni ekran görüntüsü üretildiği iddia edilmiyor.

### Cihaz kabul listesi (henüz yapılmadı)

- Çentikli cihaz, hareketli gezinme ve üç düğmeli gezinme.
- 360 dp telefon, yatay ekran/tablet, yüzde 200 yazı; klavye açılmış soru formu.
- 0 / 1 / 100 geciken soru; ilk üç önizleme ile turun ilk sorularının eşleşmesi.
- Tur bittikten sonra kalan sayının ve hedefin güncellenmesi.
- Öğretmen: Bugün/Geciken filtresindeyken yarına planla, gece yarısı ve yeniden açılış.
- Açık/koyu tema kart okunabilirliği; rozet ekranından geri dönünce kaydırma konumu.


## İkinci geçiş: görsel sağlamlaştırma ve güvenli işlemler

- Öğretmen soru kartının etiketleri ve alt işlemleri `FlowRow` ile satır kırar.
  Uzun ders/grup etiketleri diğer etiketleri ekran dışına itemez.
- Ana kart düğmeleri sabit 46 dp yüksekliğe kilitlenmez; en az 48 dp olur,
  yüzde 150 ve üzeri yazı boyutunda alt alta yerleşir. Aynı düzen öğrencinin
  öğretmene sorulacak soru kartlarında ve yedekleme düğmelerinde kullanılır.
- Öğretmen başlık sayaçları büyük yazıda dikey yerleşir. Grup başlığına iki satır
  ayrılır, durum rozetleri alt satıra geçebilir. Seçim filtreleri en az 48 dp'dir.
- Öğretmen diyalogları tablette en fazla 560 dp genişlikte ortalanır.
- Ayarlarda bakım/katalog metinleri düğmelerin alanını tüketmez.
- Bugün önizlemelerinde gecikme rozeti, başlıkla yan yana yarışmak yerine
  metnin altında yer alır.
- Yedi günlük ilerleme normal yazıda kısa sütunlarla, büyük yazıda tarih ve
  sayıları koruyan satırlarla sunulur. TalkBack için her sütunun tarihi ve gerçek
  tekrar sayısı vardır; dekoratif çubuklar sahte başarı yüzdesi taşımaz.
- Grup kartları bugün/geciken/plansız iş sayılarını gösterir. `OgretmenIsOzeti`
  yalnız BEKLIYOR kayıtlarını sayar; çözülmüş ve arşivlenmiş sorular sayılmaz.
- Kuyrukta tekil/toplu silme artık onay ister ve mevcut dosyaları koruyarak gerçek
  geri alma sunar. Bu, grup detayındaki güvenlik standardını kuyruğa taşır.
- Arama/sekme/gün filtresi değiştiğinde görünmeyen seçili sorular toplu işlemlere
  dahil edilmez. Hem kuyruk hem grup detayında etkin seçim görünür listeyle kesiştirilir.

Ek test kaynakları: `OgretmenIsOzetiTest`, `OgretmenGorselDuzenTest` (320 dp /
yüzde 200 yazı / düğme sınırları), `OgretmenT3ArayuzTest` içindeki kuyruk silme,
iptal ve geri alma senaryosu. Bu testler henüz çalıştırılmadı.

Ek kontrol: değişen/yeni **30 Kotlin dosyası tree-sitter Kotlin ayrıştırıcısından
sıfır sözdizimi hatasıyla geçti**. Bu yalnız sözdizimi kontrolüdür; Kotlin tip
kontrolü, Compose derlemesi, JUnit ve gerçek cihaz görsel kabulünün yerine geçmez.

Çözümü dahil paylaşma, karşılaştırmalı konu önerileri ve başka cihazda yedek kabulü
bu ikinci geçişte uygulanmış/tamamlanmış sayılmıyor; yukarıdaki sonraki adımlar olarak kalır.


## Yayın güncellemesi

Yerel ortam kısıtından sonra GitHub Actions üzerinde derleme tamamlandı:
[40384089478](https://github.com/keremtulu44/Question-note/actions/runs/40384089478).
Debug 380 ve Release ortak 300 testin tamamı geçti; 1.6.1 (40) imzalı APK yayımlandı.
Yukarıdaki “çalıştırılmadı” ifadeleri önceki yerel inceleme anını anlatır. Güncel
sonuç için `DERLEME_RAPORU.md` geçerlidir. Fiziksel cihaz kabulü tamamlanmış sayılmaz.
