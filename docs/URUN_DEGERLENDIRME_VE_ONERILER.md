# Ürün değerlendirmesi ve öneriler — 29 Ekim 2026

**Kapsam:** Depo taraması (148 üretim Kotlin dosyası, 1.210 metin kaynağı, Room v5, ZIP v5),
öğrenci yüzeylerinin görsel/erişilebilirlik incelemesi, deneme ve soru çözüm akışları için
önceliklendirilmiş öneriler. Öğretmen alanı `AGENTS.md` gereği **kapsam dışı** (gizli/askıda,
bu belgede ne özellik ne refaktör önerilir).

**Yöntem:** Kod okuma + statik araçlar (`tools/statik_kontrol.py`, `tools/denge.py`), ölü
parametre/ölü metin taraması, test sözleşmelerinin (testTag/metin) okunması. **Cihazda
çalıştırma ve görsel kabul yapılmadı**; derleme/test kanıtı CI'a aittir (bkz. §10).

---

## 1. Kısa özet — en önemli bulgular

| # | Bulgu | Etki | Durum |
|---|---|---|---|
| 1 | `DenemeDetayEkrani`da `onDenemeyiCalis` parametresi host'ta bağlı ama ekranda **hiç çizilmiyor**; `deneme_study_action` metni ölü | Deneme → defter köprüsü yok: öğrenci denemeyi görüyor, sorularını çalışamıyor | **Bu turda düzeltildi** |
| 2 | Net grafiği tarih/eksen etiketi ve sayısal özet taşımıyor; TalkBack'e hiçbir bilgi vermiyor | "Gidişat" kartı süs gibi kalıyor | **Düzeltildi** (tarih etiketleri + özet şerit + içerik açıklaması) |
| 2b | Deneme listesinde ekran başlığı iki kez yazılıyordu (üst çubuk + liste başlığı) | Aynı bilgi tekrarda güveni değil gürültüyü artırır | **Düzeltildi** (liste başlığı yalnız tek satır bağlama indirildi) |
| 3 | Liste kartı yalnız D/Y/B ve yüklenen soru sayısını veriyor | Kart açılmadan "hangi derse bakmalıyım" cevaplanamıyor | **Düzeltildi** (en düşük netli ders satırı) |
| 4 | Bugün'deki deneme satırı sabit `oran = 0.72f` halka çiziyor | Ekranda **sahte ilerleme**; öğrenciye olmayan bir yüzde ima ediyor | **Düzeltildi** (son 6 denemenin gerçek mini çizgisi) |
| 5 | 1.210 metin kaynağının **203'ü kullanılmıyor** (kaldırılan ekranların artıkları) | Bakım maliyeti + çeviri/denetim gürültüsü | Öneri: temizlik turu (riski düşük, ayrı iş) |
| 6 | Grafiklerin çoğu Canvas; yalnız testTag var, `contentDescription` yok | TalkBack kullanıcısı grafiklerden hiçbir şey öğrenemiyor | Kısmen düzeltildi; kalanlar için liste §3.3 |
| 7 | Deneme verisi `SharedPreferences` + JSON; sorular Room'da | Yedek/göç iki ayrı biçim; deneme bozulursa `getOrDefault(emptyList())` ile **sessiz veri kaybı** riski | Öneri: bozuk JSON'u karantinaya al (öğretmen deposunda bu desen var) |
| 8 | Tek dosyalar çok büyük: `CozumTahtasiDialog` 1.873, `SoruFormuEkrani` 1.448, `YanlisDefteriUygulamasi` 1.133 satır | Değişiklik maliyeti ve regresyon riski | Öneri: davranış değiştirmeden bölme (yalnız ihtiyaç doğduğunda) |

---

## 2. Ürün soruları: öğrenci tek uygulamada neyi kontrol etmek ister?

Öğrencinin gerçek ihtiyacı "her şeyi takip etmek" değil; **karar yorgunluğunu azaltmak**.
Uygulamanın bugünkü omurgası (yanlış defteri + aralıklı tekrar + tek günlük hedef) doğru.
Eksik olan, öğrencinin şu altı soruya **tek dokunuşta** cevap alabilmesi:

| Öğrencinin sorusu | Bugünkü cevap | Eksik / fırsat |
|---|---|---|
| Bugün ne yapacağım? | Bugün ekranı + odak kartı (net, iyi) | Tur büyüklüğünü "5/10/20" diye seçme yok; hedef otomatik türetiliyor (Odak'ta var, Bugün'de yok) |
| Ne kadar kaldı? | Hedef halkası + üst panel | Doğru |
| Neden bu soru? | Kuyruk kartında gecikme etiketi | "Neden bu konu?" açıklaması yalnız İlerleme'de; soru bazında tek satır yok |
| Nerede zayıfım? | İlerleme'de ders dağılımı, denemede ders barları | **Deneme ↔ defter** bağı kopuk: denemede zayıf ders, filtreli çalışmaya tek dokunuşla taşınmıyor (kısmen düzeltildi) |
| Gelişiyor muyum? | Net çizgisi, delta rozetleri, haftalık kayıt | Net grafiği okunaksızdı (düzeltildi); "yeterli veri yok" dürüstlüğü korunuyor |
| Verim bende mi? | ZIP yedeği, geri yükleme, dışa aktarma | Doğru; yedek **hatırlatması** öğrenci tarafında yok |

**Tasarım ilkesi (korunmalı):** her yeni sayı **gerçek kayıttan** türetilmeli; veri yoksa
"yok" denmeli. Bu turdaki tüm değişiklikler bu ilkeye uyuyor (karışık puanlama kuralında
ortalama/en iyi net üretilmiyor, tek kayıtta trend çizilmiyor).

---

## 3. Bulgular ve çözüm önerileri

### 3.1 Ürün akışı

1. **Deneme → çalışma köprüsü (düzeltildi).** `YanlisDefteriUygulamasi` `onDenemeyiCalis`
   ile `SoruCalismaFiltresi(kaynak = deneme.ad)` kuruyor; ekran parametreyi çizmediği için akış
   ölüydü. Artık denemede yüklenmiş soru varsa "Bu denemenin soruları" kartı görünür.
2. **Denemede "kalan soru" takibi tek yerde.** Detay ekranı ders başına "N kaldı" diyor, liste
   kartı toplam "x / y defterde" diyor. İkisi aynı hesabı kullanıyor (`yuklenecek = yanlış + boş`);
   ek bir yerde tekrar edilmemeli.
3. **Boş soruların deftere aktarımı "soru başına fotoğraf" istiyor.** Denemede 40 boş+yanlış
   varsa bu 40 ayrı form açılışı demek. Fikir: **toplu ekleme kuyruğu** — tek oturumda art arda
   fotoğraf çek, her fotoğraf aynı kaynak/ders ile kaydedilsin (kamera zaten `FileProvider`
   üzerinden çalışıyor, yeni izin gerekmez).
4. **Sınav profili yalnız ders filtresi.** Profil (LGS/YKS/9-10) seçili ama **sınav tarihi**
   yok. Çevrimdışı ve bildirimsiz bir "kalan gün" göstergesi (yerel tarih farkı) motivasyon
   için yeterlidir; bildirim izni/WorkManager gerekmez.

### 3.2 Görsel olarak zayıf kalan yerler

| Yer | Sorun | Öneri (öncelik) |
|---|---|---|
| Deneme net grafiği | Etiketsiz, özet sayısız | **Yapıldı**: ilk/son tarih + deneme/ortalama/en iyi net |
| Deneme liste kartı | Karar bilgisi yok | **Yapıldı**: en düşük netli ders satırı |
| Bugün → deneme satırı | Sabit `0.72` halka = uydurma yüzde | **Yapıldı**: gerçek mini çizgi |
| Deneme detayı | Kaydettikten sonra "sırada ne var" boşluğu | **Yapıldı**: çalışma kartı |
| Ders grid kartları | Soru sayısı dışında sinyal yok; "bugün kaç tekrar var" görünmüyor | Orta: kartın altına 4 bölmeli mini ölçek (bugünkü tekrar / toplam) `InceOlcek` ile |
| Karne | Bölüm sayısı fazla (oran halkası, kova, konu, hafta, ilerleme, 7 gün, rozet) | Orta: ilk ekranda 2 bölüm, gerisi "ayrıntı" katlaması (P0'daki sadeleştirme ruhu) |
| Filtreli çalışma | Başlangıç ekranı iyi; sonuç ekranı Karne'ye devrediyor | Düşük: filtre özetini (ders/konu/kaynak) tur başında tek satırda sabitle |
| Boş durumlar | `PremiumBosDurum` tutarlı | Doğru; yeni boş durum eklerken aynı bileşen kullanılmalı |

**Görsel dil sözleşmesi (korunmalı):** gradyan yalnız kahraman kart ve tek CTA; sonsuz animasyon
yok; `HareketPolitikasi` "azalt" seçeneği tüm animasyonları kapatır; glif karmaşası
(`PremiumSembolAilesi`) kuralına uyulur. Bu turdaki eklemeler yalnız `Text`, `SadeKart` ve
mevcut Canvas dili kullanıyor — yeni varlık, yeni renk, yeni animasyon yok.

### 3.3 Erişilebilirlik

- **Yapıldı:** net çizgisi `contentDescription` veriyor (`deneme_chart_description`).
- **Kalan:** `DersNetBarlari`, `InceOlcek`, `HedefHalkasi`, `RutbeYolculugu` çizimleri dekoratif
  oldukları yerde `clearAndSetSemantics { }` ile susturulmalı; veri taşıdıkları yerde
  (ders barları) tek bir özet `contentDescription` almalı.
- **Kalan:** %200 yazıda üç sütunlu metrik satırı (yeni özet şerit) `labelSmall` iki satıra
  sarıyor; cihazda `%200` kontrolü kabul listesine eklenmeli.
- **Kalan:** `Canvas` tabanlı grafiklerde dokunma hedefi yok (grafik salt okunur) — sorun değil,
  ama TalkBack sırası `LazyColumn` içinde anlamlı olmalı.

### 3.4 Teknik borç (davranış değiştirmeden)

1. **203 kullanılmayan metin** (`analysis_*`, `lesson_*`, `level_*`, `home_*` kümeleri). Silme
   işi ayrı bir PR olmalı; `tools/statik_kontrol.py` yalnız "eksik" arıyor, "fazla"yı raporlamıyor.
   Öneri: aynı betiğe `--olu` raporu ekle (hata değil, uyarı).
2. **Kod içine gömülü metinler:** `Text("+")`, `Text("← ...")`, `PremiumDemoEkrani` içindeki
   tanıtım metinleri. Demo ekranı bilinçli istisna; üretim ekranlarında gömülü metin kalmamalı.
3. **`DenemeDeposu.oku()` bozuk JSON'da `getOrDefault(emptyList())`** → sessiz kayıp. Öğretmen
   deposundaki `bozuk-<zaman>.json` karantina deseni buraya da uygulanmalı (veri kaybı riski).
4. **Ölü parametre taraması** (bu turda 1 bulundu). Öneri: CI'da `-Werror` yerine ayrı bir
   "kullanılmayan parametre" raporu; davranışı bozmadan temizlik.
5. **Room v5 + `allowBackup=false`:** doğru. Yedek hatırlatması (7 gün) yalnız öğretmen tarafında
   var; öğrenciye de sessiz bir "en son yedek X gün önce" satırı Ayarlar'da gösterilebilir.

---

## 4. Deneme tarafı — yol haritası

### Ufuk A (yakın, düşük risk)

- **A1. Kayıt sırasında "boş"ları otomatik işaretle.** `DenemeDersSatiri.bos` zaten hesaplanıyor;
  ders kartında "8 boş" bilgisini giriş alanının yanında göster (şu an yalnız üst rozette).
- **A2. Aynı denemeyi düzenlerken kaynak adı değişirse soruların kaynağı güncellensin** —
  `soruRepository.kaynakAdiDegistir` var; kullanıcıya "N soru güncellendi" geri bildirimi ekle.
- **A3. Deneme listesinde türe göre filtre.** `DenemeKaydi.ad` serbest metin; kaynak kataloğu
  (`SoruSecenekTuru.KAYNAK`) zaten var → "yayın evi" seçilebilir hale getirilebilir.
- **A4. Delta rozetinin yanına "önceki ortalamaya göre" etiketi** (yanlış anlaşılmayı önler).

### Ufuk B (orta)

- **B1. Ders bazlı trend.** `DersNetBarlari` yalnız son denemeyi gösteriyor. Aynı kuraldaki son 6
  deneme için ders başına mini çizgi (`MiniCizgi` hazır) → "hangi ders düşüyor" sorusu.
- **B2. Deneme raporu (paylaşılabilir görsel).** Karne ekranındaki `RozetGorseli` deseniyle
  (Canvas → Bitmap) tek sayfalık PNG: ad, tarih, net, ders barları. Dosya seçiciye yazılır;
  paylaşımda soru/cevap metni yok (paylaşım kuralı korunur).
- **B3. Toplu fotoğraf kuyruğu** (§3.1/3).
- **B4. "Deneme sonrası 5 dakikalık kapanış".** Deneme kaydedilince tek ekran: en düşük 2 ders,
  "bu derslerden kaç soru yüklendi", tek CTA (kalan soruları yükle veya çalış).
- **B5. Sınav günü geri sayımı** (§3.1/4) — yalnız yerel tarih, bildirim yok.

### Ufuk C (uzun, tartışmalı)

- **C1. Deneme arşivi/silmeme.** Şu an silme geri alınamıyor; "arşivle" + geri al (öğretmen
  tarafında bu desen var, öğrenciye uyarlanabilir).
- **C2. Süre kaydı.** Denemenin toplam süresi (dakika) elle girilirse "net/dk" gibi bir oran
  üretilebilir — **önerilmez**: hızı başarı saymak bu ürünün "emek odaklı" duruşuna aykırı.
- **C3. Konu bazlı deneme çıkarımı (OCR/puan tanıma ile) — yapılmamalı.** Optik form okuma
  çevrimdışı ve güvenilir biçimde mümkün değil; yanlış okunan bir puan, öğrencinin kendi
  kaydına güvenini bitirir.

---

## 5. Soru çözüm tarafı — yol haritası

Bugünkü durum: `CozumTahtasiDialog` (çizim, renk, geri al, oturum belleği) + `OdakOturumuEkrani`
(süre ölçümü, 3 kademeli öz değerlendirme: `COZEMEDIM / ZORLANDIM / RAHAT`, çözümü açma kaydı).

1. **Çözümü "kademeli aç".** Bugün çözüm ya gizli ya açık. Öneri: (a) tek satır ipucu,
   (b) tam çözüm. İpucu açmak `cozumAcildi` sayılmamalı — **ayrı bayrak** (`ipucuAcildi`);
   bağımsız çözme tanımı bozulmaz, öğrenciye gerçek bir ara adım verilir. Veri şeması değişir
   (Room v6) → yedek sürümü ile birlikte planlanmalı.
2. **Yarım kalan çözüm.** Tahta oturum belleği (`TahtaOturumCizimBellegi`) var; süreç ölümünde
   kayboluyor. Öneri: çizimi geçici dosyaya yaz, soru açıldığında "devam et / yeni sayfa" sor.
3. **Çözüm fotoğrafı ile karşılaştırma.** Çözümü açtığında öğrencinin kendi tahtası küçük bir
   pencerede kalsın (kapatılabilir). Tek dokunuşluk, veri gerektirmeyen, çok işe yarayan bir
   karşılaştırma.
4. **"Çözdüm ama anlamadım" ara durumu.** `ZORLANDIM` bunu kısmen karşılıyor; soru planına
   "konu tekrarına geri dön" etiketi eklemek (İlerleme'deki konu önerisiyle aynı mantık) doğal
   sonraki adım.
5. **Sessiz süre.** `AktifSureOlcer` var; oturum ortalaması soru kartında **karşılaştırma**
   olarak değil, yalnız kendi geçmişine göre "bu soruda 4 dk, geçen sefer 7 dk" biçiminde
   gösterilmeli. Hız baskısı yaratacak sıralama/rozet yok.
6. **Çözümü hiç yazmama.** Ürün "çözüm ayrı ve varsayılan gizli" diyor; öğrenci çözümü hiç
   eklemeyebilir. Bu bir eksik değil, bilinçli sınır — arayüz bunu gizli bir kusur gibi
   göstermemeli (mevcut `focus_no_solution` metni doğru).

---

## 6. OCR: ne işe yarar, neye yaramaz?

### 6.1 Neye **yaramaz** (ve bu yüzden yapılmamalı)

- **Cevap doğrulama / otomatik puanlama.** Çevrimdışı, tek cihazda, el yazısı ve fotoğraf
  kalitesiyle güvenilir değil. Yanlış "doğru" diyen bir sistem, öğrencinin kendi
  değerlendirmesine olan güveni yok eder. Uygulamanın bugünkü dürüstlük çizgisi
  ("öz değerlendirme, otomatik doğrulama değil") korunmalı.
- **Soru metnini anlamlandırma / çözüm üretme.** Model yok, olmamalı (APK boyutu, çevrimdışı
  sınırı, güvenilirlik).

### 6.2 Neye **yarar** (gerçek kazanç sırasıyla)

1. **Çoklu soru kırpma (en yüksek kazanç).** Bir sayfa fotoğrafında 4-6 soru var. Metin
   bloklarını bulup **soru sınırlarını önermek** kırpma işini tek tek elle yapmaktan kurtarır.
   `GorselKirpmaDialog` zaten var; OCR yalnız "önerilen kutular" üretir, öğrenci onaylar.
   Yanlış öneri maliyeti düşük (kutu kaydırılır).
2. **Kaynak / deneme adı ve tarih yakalama.** Sayfanın üstündeki "3D Türkiye Geneli 4" gibi
   başlık satırı, soru formundaki `kaynak` alanına **öneri** olarak düşer; katalogdaki mevcut
   kaynaklarla eşleşirse tek dokunuşla seçilir. Deneme takibiyle doğrudan bağlanır.
3. **Yinelenen soru uyarısı.** Aynı görselin sha256'sı zaten kontrol ediliyor (öğretmen formunda);
   öğrenci formunda OCR metninin benzerliği ile "bu soruyu 3 hafta önce eklemiş olabilirsin"
   uyarısı verilebilir (yalnız uyarı, engel değil).
4. **Defterde metin arama.** Bugün arama `soruNotu` ile sınırlı; OCR metni saklanırsa
   "içinde 'türev' geçen sorular" bulunabilir. Bu, uzun vadede en çok istenecek özellik.
5. **Ders/konu önerisi.** Anahtar kelime → ders sözlüğü eşlemesi (kural tabanlı, model yok).
   Yanlış öneri kabul edilebilir çünkü alan yalnız önceden doldurulur.

### 6.3 Çevrimdışı mimari ve maliyet

- **Araç:** ML Kit Text Recognition v2 **bundled** (Latin) — indirme yapmaz, ilk kullanımda
  internet istemez. Cihaz içi; `INTERNET` izni gerekmez (manifestte zaten yok, bu korunmalı).
- **Boyut:** bundled Latin modeli kabaca 3-5 MB APK büyümesi demek; bugünkü APK ~13,7 MB.
  Play dağıtımı için kabul edilebilir, ancak **ölçülmeden eklenmemeli**.
- **Alternatif (boyut duyarlıysa):** hiç OCR yok; bunun yerine "elle kırpma + son kullanılan
  ders/kaynak" hafızası (zaten kısmen var) ve çoklu kırpma UX'i iyileştirilir. OCR, kırpma
  UX'i oturmadan eklenirse yalnız yeni hata kaynağı olur.
- **Veri:** `SoruEntity`'ye `ocrMetni` (nullable TEXT) + Room v6 + ZIP v6 gerekir. Yedek
  okuma geriye dönük (v2-v5) korunmalı. OCR metni **paylaşımda dışarı çıkmamalı**
  (paylaşım metin üretmiyor; bu sözleşme korunur).
- **Sıralama:** (1) kırpma UX'i, (2) toplu ekleme kuyruğu, (3) OCR ile kutu önerisi,
  (4) kaynak/tarih önerisi, (5) metin arama. OCR'ı 1. adıma koymak yanlış sıra.

### 6.4 Kabul kapıları (OCR eklenirse)

- Uçak modunda, 20 gerçek sayfa fotoğrafında kutu önerisi: en az %60 sayfada 1 dokunuş kazancı.
- OCR hiçbir zaman kaydı **engellemez**; metin okunamazsa form normal çalışır.
- APK büyümesi ölçülür ve CHANGELOG'a yazılır; `INTERNET` izni eklenmez.
- Yanlış okuma örneği 5 senaryoda (eğik, gölgeli, el yazısı, düşük ışık, İngilizce sayfa)
  belgelenir.

---

## 7. "Tek uygulama" dengesi — kapsam sözleşmesi

**İçeride kalmalı:** yanlış defteri, aralıklı tekrar, günlük hedef, deneme net takibi,
ders/konu kataloğu, çözüm tahtası, karne/rozet, ZIP yedeği.

**Dışarıda kalmalı (dürüstçe "yok" denmeli):** sosyal akış, sıralama/lig, yapay zekâ çözüm
üretimi, otomatik cevap doğrulama, bulut senkronizasyon, bildirim izni gerektiren hatırlatmalar,
optik form okuma. Bunların her biri çevrimdışı + tek cihaz sözüyle çelişir veya güvenilmez
bir "ölçüm" iddiası doğurur.

**Öğrenciyi "kontrol" hissinde tutan üç şey:** (1) verinin cihazda ve yedeklenebilir olması,
(2) her sayının kaynağının görünmesi ("neden bu konu", "hangi deneme"), (3) tek ekranda tek
karar (bugün ne çalışacağım).

---

## 8. Bu turda uygulanan değişiklikler

| Dosya | Değişiklik |
|---|---|
| `domain/model/DenemeOzeti.kt` (yeni) | `ozet()` ve `enZayifDers()`; karışık puanlama kuralında ortalama/en iyi net **üretilmez** |
| `ui/deneme/DenemeEkranlari.kt` | Net grafiğine tarih etiketleri + `contentDescription`; üç sayılık özet şerit (tek kayıtta yalnız "son net", karışık kuralda ortalama yok); liste kartında "en düşük netli ders"; deneme → çalışma kartı (`deneme-study-button`); üst çubukla aynı olan ikinci başlık kaldırıldı, yerine tek satır bağlam |
| `ui/bilesenler/InceCizimler.kt` | `MiniCizgi` (sparkline): yalnız gerçek noktalar, 2'den az veride çizim yok |
| `ui/BugunEkrani.kt` | Deneme satırındaki sabit `0.72` halka yerine son 6 denemenin gerçek çizgisi |
| `res/values/strings.xml` | 7 yeni metin (`deneme_summary_*`, `deneme_chart_description`, `deneme_card_weakest`, `deneme_study_*`) |
| `test/.../DenemeOzetiTest.kt` (yeni) | 6 saf JVM testi: boş liste, aynı/karışık kural, tarih sırası, tek ders, kayıtlı ders adı |

Sürüm, Room şeması, ZIP biçimi, tekrar motoru, XP/rozet katsayıları **değişmedi**. Yeni izin
ve bağımlılık **yok**.

---

## 9. Sıradaki iş (öncelik sırası)

1. **Cihaz kabulü** (`docs/TEST_KONTROL_LISTESI.md`): bu turdaki deneme ekranları %200 yazı,
   koyu tema, TalkBack ve 320 dp genişlikte görülmeli.
2. **`DenemeDeposu` bozuk JSON karantinası** (§3.4/3) — veri kaybı riski.
3. **Toplu fotoğraf kuyruğu** (§3.1/3) — deneme tarafındaki en büyük sürtünme.
4. **Grafiklerde `clearAndSetSemantics` taraması** (§3.3).
5. **Ölü metin temizliği + `statik_kontrol.py --olu`** (§3.4/1).
6. **OCR yalnız yukarıdakilerden sonra** (§6.3 sıralaması).

---

## 10. Doğrulama durumu

- `python3 tools/statik_kontrol.py` → 1.210 kaynak, 15 rütbe amblem × 5 yoğunluk, **0 hata**.
- `python3 tools/denge.py` → Kotlin dosyalarında parantez/süslü denge hatası yok.
- `git diff --check` → temiz.
- **Yerelde Java/Android SDK yok**: Kotlin derlemesi, JUnit ve görsel önizleme bu ortamda
  üretilmedi. Gerçek kapı CI'dır (`Android CI` → Debug + Release birim testleri);
  cihaz kabulü ayrı ve hâlâ açıktır.
