# Öğretmen ihtiyaç araştırması — ne eklenmeli?

**Tarih:** 2026-10-26 · **Durum:** öneri (uygulanmadı) · **Kapsam:** v1.6.0 sonrası
**İlgili belgeler:** [Öğretmen modu sözleşmesi](OGRETMEN_MODU.md) · [Değerlendirme ve T0/T1/T2 yol haritası](OGRETMEN_MODU_DEGERLENDIRME.md) · [PROJECT_BRIEF §7–§8](../PROJECT_BRIEF.md) · [ROADMAP](../ROADMAP.md)

Bu belge "fikir listesi" değil; **mevcut kodun gerçekten ne yaptığı** ile **öğretmenin gerçek
iş akışı** arasındaki farkı madde madde gösterir. Her boşluk için kanıt (dosya/davranış), etki,
öneri, efor ve risk yazılıdır. Uygulanmayacak fikirler de gerekçesiyle listelenir (PROJECT_BRIEF §9).

---

## 1. Yöntem

1. **Kod envanteri:** `ui/ogretmen/` altındaki 8 dosya (~4.256 satır), `OgretmenDeposu`,
   `OgretmenDao`, Room v5 şeması, yedek biçimi v5, `AndroidManifest.xml`, CI iş akışı.
2. **Davranış doğrulaması:** her iddia için `grep`/okuma; "yok" dediğim şeylerin gerçekten
   çağrılmadığı arandı (ör. `ACTION_SEND`, `WorkManager`, `NotificationCompat`, `kimSordu`).
3. **Dış kanıt:** öğretmen iş yükü ve biçimlendirici değerlendirme literatürü (kaynaklar §9).
4. **Sınır:** fiziksel cihaz/gerçek öğretmen görüşmesi yapılmadı. Bu belge bir **hipotez setidir**;
   T3'e giren her madde cihazda ve mümkünse 1–2 öğretmenle doğrulanmalı.

---

## 2. Dış kanıttan çıkan üç kısıt

| Bulgu | Bu ürüne etkisi |
|---|---|
| Öğretmenlerin **%74'ü** genel idari işlere, **%53'ü** veri kaydetme/analiz işine "çok fazla zaman" harcadığını söylüyor ([DfE çalışma yaşamı 2024](https://assets.publishing.service.gov.uk/media/685bcd654cd6b031687097cd/Working_lives_of_teachers_and_leaders_-_qualitative_insight_into_teacher_leader_workload.pdf)); derleme çalışmasında **%75 idari iş**, **%49 veri işi** ([Teacher workload review](https://www.tandfonline.com/doi/full/10.1080/00131881.2026.2629284)). | Öğretmene *yeni kayıt yükü* ekleyen hiçbir özellik kabul edilmemeli. Her ekran "kaç dokunuş" sorusuyla ölçülmeli. Rapor/karne üretmek cazip görünse de asıl darboğaz **kayıt anı**. |
| Biçimlendirici değerlendirmenin değeri, kanıtı **bir sonraki adıma çevirmekte**; çıkış bileti/exit ticket pratiğinde öğretmenin yanıtları okuyup "sınıf ilerlesin mi" kararı vermesi esas ([Evidence Based Education](https://evidencebased.education/resource/effective-and-efficient-exit-tickets/)). Etki büyüklükleri tutarsız, kanıt kalitesi düşük ila orta ([eleştirel özet](https://www.structural-learning.com/post/exit-tickets)). | "Kuyruk var" yeterli değil; **kapatma** (çözüldü → arşiv → öğrenciye dönüş) döngüsü tamamlanmalı. Bu üründe asıl eksik burası. |
| Retrieval practice kanıtı güçlü ama **abartılmamalı**: 50 sınıf deneyinin %57'sinde orta/büyük etki ([Agarwal vd. 2021](https://pdf.poojaagarwal.com/Agarwal_etal_2021_EDPR.pdf)); matematikte aralıklı tekrar etkisi küçük–orta (g = 0,28) ([Murray vd. 2025](https://link.springer.com/article/10.1007/s10648-025-10035-1)). | Öğretmene "bilimsel garanti" dilinde vaat verilmemeli. Öğretmen tarafı bir **iş aracı**; XP/rütbe yok (K38–K44 korunur). |

---

## 3. Öğretmenin günü ve bugünkü karşılığı

| An | Öğretmenin yaptığı iş | Uygulamanın bugünkü durumu | Boşluk |
|---|---|---|---|
| Dersten önce | "Bugün hangi soruları kapatmalıyım?" | "Bugün" sekmesi + geciken filtresi + grup çipleri | Ders/konu filtresi yok, arama yok, planlanan gün gruplaması yok |
| Derste (soru geliyor) | Telefonla fotoğraf, kim sordu, hangi konu | Form: kamera/galeri, ders, konu, kaynak, kim sordu, çözüm günü; **fotoğraf zorunlu** | Sözlü soru için fotoğraf yok; 10 soruluk ödev için 10 ayrı kayıt döngüsü |
| Derste (tahtada çözme) | Soruyu ekrana yansıtıp birlikte çözme | Tam ekran sunum: ekran açık, cevap gizli, çözüm ayrı | Sunumdan sonra "kime döneceğim" bağlantısı yok (kim sordu görünmüyor) |
| Ders sonrası | Çözüleni kapatma, arşiv | Kart üstünden çözüldü, toplu durum, onaylı arşiv, silme geri alma | Arşivden geri getirme yok; grup silme geri alınamıyor |
| Ders dışı (veli/öğrenci) | "Şu soruyu gönder" | **Yok** — uygulamada hiç `ACTION_SEND` yok | Paylaşım tamamen eksik |
| Dönem sonu | "Bu dönem ne birikti?" | Yalnız bekleyen/çözülen sayıları | Ders/konu dağılımı, bekleme süresi, kapanma oranı yok |

---

## 4. Tespit edilen boşluklar (kanıtlı)

Her madde: **kanıt → etki → öneri → efor → risk**. Efor: S ≤ 1 gün, M ≈ 2–4 gün, L ≥ 1 hafta
(tek geliştirici, testler dâhil).

### G1 — Öğretmen ekranlarında arama yok (regresyon)
- **Kanıt:** `ui/ogretmen/` içinde tek `OutlinedTextField` kullanımı grup formunda; kuyruk ve grup
  detayında arama alanı yok. v1.5.107 sürüm notunda "grup ve soru içi anlık arama" yazıyor; 2.561
  satırlık monolitten bölünürken düşmüş.
- **Etki:** 200+ soruluk grupta belirli bir soruyu bulmak imkânsız; öğretmen listede kaydırıyor.
- **Öneri:** kuyruk ve grup detayına tek satır arama (başlık + kaynak + kim sordu), `dersId`/
  `konuId` filtre çipleri (`OgretmenVerisi.kuyruk(dersId = …)` zaten destekliyor, UI kullanmıyor).
- **Efor:** S · **Risk:** düşük (şema değişmez) · **Test:** filtre birimi + `performTextInput` UI testi.

### G2 — "Kim sordu" toplanıyor, hiç kullanılmıyor
- **Kanıt:** `kimSordu` yalnız formda yazılıyor (`OgretmenSoruFormuEkrani`), kartta/kuyrukta/
  istatistikte okunmuyor; `OgretmenSoruKarti` parametrelerinde yok.
- **Etki:** Öğretmenin asıl kazanımı "Ali'ye dönüş" — alan boşa toplanıyor. Ayrıca serbest metin
  olduğu için ("ali", "Ali Y.") gruplanamıyor.
- **Öneri:** (a) kartta küçük "Ali sordu" rozeti, (b) grup detayında "kim sordu" filtresi,
  (c) son kullanılan 5 isimden hızlı seçim (öneri çipleri), (d) aynı normalize anahtarla toplam.
- **Efor:** S–M · **Risk:** düşük (metin alanı) · **Test:** filtre + rozet görünürlüğü.

### G3 — Fotoğraf zorunlu, ama sorular çoğu zaman sözlü
- **Kanıt:** `kaydet()` içinde `gorseller.isEmpty()` → `question_error_photo_required`
  ("En az bir soru görseli ekleyin."). Buna karşılık formda `kimSordu` alanı var: yani "Ali sordu"
  bilgisini kaydetmek isteyen öğretmen fotoğraf çekmek zorunda.
- **Etki:** Sınıf içinde en sık senaryo (sözlü soru, tahtadaki soru, "şu sayfadaki 7. soru") için
  akış tıkanıyor; öğretmen ya vazgeçiyor ya alakasız bir fotoğraf çekiyor (veri kirliliği).
- **Öneri:** "Fotoğrafsız not" yolu: `gorsel_bekliyor = true` alanı, kuyrukta "fotoğraf ekle"
  rozeti, yedek doğrulamasında "en az bir görsel" kuralının bu işaretle gevşetilmesi.
- **Efor:** M (Room **v6** + yedek **v6** + doğrulama) · **Risk:** orta — "fotoğrafsız kayıt"
  ürün kimliğini sulandırabilir; bu yüzden **varsayılan değil, açık tercih** olmalı.
- **Karar gerekli:** §8/S1.

### G4 — Paylaşım yok (veli/öğrenci iletişimi)
- **Kanıt:** kod tabanında `ACTION_SEND` / `createChooser` yok; dosya paylaşımı yalnız yedek
  ZIP'inde (`SAF`). `FileProvider` kamera için mevcut.
- **Etki:** Öğretmen bir soruyu/çözümü öğrenci veya veliye gönderemiyor. Değerlendirmede
  "WhatsApp'a doğrudan entegrasyon: Red — `ACTION_SEND` yeterli" denmiş; **`ACTION_SEND` hiç
  uygulanmamış.**
- **Öneri:** kart menüsünde "Paylaş" → (a) soru fotoğrafları, (b) soru + çözüm (tek PDF değil,
  görsel olarak), (c) seçili N soru. Sistem paylaşım sayfası; uygulama hesap/izin istemez.
- **Efor:** S · **Risk:** düşük; gizlilik notu gerekir (paylaşılan görselde öğrenci adı geçebilir,
  `kimSordu` paylaşımda varsayılan **dışarıda**).

### G5 — Toplu fotoğraf alımı yok
- **Kanıt:** galeri `PickMultipleVisualMedia(10)` ile 10 görsel **tek soruya** eklenebiliyor; kamera
  her seferinde tek çekim + tek kayıt. Kaydet-ve-yeni var ama her soru yine tam form döngüsü.
- **Etki:** 10 soruluk ödev/föy → 10 kez form doldurma. Kayıt yükü tam olarak literatürün uyardığı
  yer (bkz. §2).
- **Öneri:** "Hızlı toplama" modu: tek oturumda çek/ seç → her fotoğraf **taslak** olarak kuyruğa
  düşer, ders/konu/ kim sordu sonradan **tek ekranda toplu** atanır.
- **Efor:** L (taslak durumu = Room v6 + yeni ekran) · **Risk:** orta (taslak birikirse çöp olur;
  "taslakları temizle" ve yaş göstergesi şart).

### G6 — `planGun` var, plan yok
- **Kanıt:** `planGun` yalnız `kuyruk(yalnizGeciken = true)` filtresinde ve "geciken" rozetinde
  kullanılıyor; "bugün" için ayrı grup yok, hatırlatma yok.
- **Etki:** Öğretmen "bugün kapatmam gerekenler" ile "birikenler" ayrımını göremiyor.
- **Öneri:** kuyruk üstünde üç durum: **Bugün** (planGun = bugün), **Geciken**, **Planlanmamış**;
  kart menüsünde tek dokunuşla "bugüne al / yarına at / planı kaldır".
- **Efor:** S · **Risk:** düşük · **Test:** gün sınırı (yerel saat) birimi.

### G7 — Sınıf içgörüsü yok
- **Kanıt:** `OgretmenHeroKarti` yalnız bekleyen/çözülen/grup sayısı; ders/konu dağılımı,
  bekleme süresi, kapanma oranı hiç hesaplanmıyor.
- **Etki:** "Hangi konudan çok soru geliyor, hangi konuyu tekrar anlatmalıyım?" sorusu cevapsız.
  Değerlendirme T2'sinde bu madde zaten var (#3), uygulanmadı.
- **Öneri:** Gruplarım altında **tek kart**: son 30 günde konu dağılımı (ilk 5), ortalama bekleme
  (gün), bu hafta kapatılan sayısı, "en çok biriken konu". Oyunlaştırma yok, seri/rozet yok.
- **Efor:** M · **Risk:** düşük; **dil** riski var ("performans" iddiası kurmamalı, yalnız sayım).

### G8 — Kapatma döngüsünün sonu eksik: arşivden geri dönüş ve grup silme
- **Kanıt:** `cozulmusleriArsivle` ve toplu `ARSIV` var; arşiv sekmesi yalnız grup detayında.
  `grupSil` kalıcı: soruları ve dosyaları siler, geri alma yok. Soru silmede `silmeyiGeriAl` var →
  **asimetri**.
- **Etki:** Öğretmen yanlış arşivlediği soruyu tüm gruplarda arayamıyor; grup silme korkutucu
  olduğu için ya hiç silmiyor ya yanlış siliyor (son yedek tarihi gösterilerek azaltılmış).
- **Öneri:** (a) kuyrukta "Arşiv" filtresi (tüm gruplar), (b) toplu "arşivden çıkar",
  (c) grup silmeyi **10 saniyelik geri alınabilir** yap (dosyaları hemen silme, onay sonrası sil),
  (d) silme diyaloğunda "önce arşivle" ikincil eylemi.
- **Efor:** S–M · **Risk:** düşük · **Test:** geri alma penceresi + dosya koruma.

### G9 — Aynı fotoğrafın ikinci kez eklenmesi sessiz
- **Kanıt:** her görselde `sha256` ve `boyut` var; formda **hiçbir** tekrar kontrolü yok.
- **Etki:** Aynı soru iki gruba/iki kez eklenir, depolama şişer, kuyruk kirlenir.
- **Öneri:** kaydetmeden önce "bu görsel `7C · Kesirler` sorusunda zaten var" uyarısı (engelleme
  değil, bilgi) + "yine de ekle".
- **Efor:** S · **Risk:** düşük (yalnız sorgu) · **Test:** aynı dosya iki kez → uyarı.

### G10 — Yedek tazeliği görünmüyor
- **Kanıt:** Ayarlar "son yedek" bilgisini yazıyor; yaş/uyarı yok, hatırlatma yok.
- **Etki:** Değerlendirmede "yedek hatırlatması ucuz ve yapılmalı" denmişti; hâlâ yok. Telefon
  kaybı = tüm öğretmen arşivi.
- **Öneri:** son yedek **7+ gün** ise Gruplarım ve Ayarlar'da tek satır uyarı + tek dokunuş yedek;
  bildirim gerekmez.
- **Efor:** S · **Risk:** düşük.

### G11 — Tablet/yatay ve büyük yazı ölçeği
- **Kanıt:** tüm öğretmen ekranları tek `LazyColumn`; `WindowSizeClass` kullanımı yok. Sunum
  diyaloğu cihaz ölçeğinde test edilmedi (1.6x yalnız otomatik testte).
- **Etki:** Sınıfta tablet yaygın; yatayda liste + detay iki panel beklentisi var.
- **Öneri:** yatayda ≥ 840 dp'de iki panel (liste | detay), sunumda yatayda görsel solda/çözüm sağda.
- **Efor:** M · **Risk:** orta (test matrisi büyür) · **Karar:** önce cihaz kullanımı sorulmalı.

### G12 — Öğrenci "hocaya sor" köprüsü öğretmen tarafında görünmüyor
- **Kanıt:** öğrenci tarafı `SorulacaklarEkrani` "öğretmene sorulacak" işaretini ve "Çözdürdüm"
  akışını tutuyor; öğretmen modu bu tabloyu hiç okumuyor (ayrı ekran, ayrı liste).
- **Etki:** Aynı cihazı kullanan öğrenci-öğretmen (özel ders, veli-çocuk) için kopya iş yükü:
  öğrenci işaretliyor, öğretmen yine kendi tarafına giriyor.
- **Öneri:** öğretmen "Bugün" ekranında **Bana sorulanlar** şeridi (yalnız aynı cihazda, tek
  kullanıcı varsayımıyla), tek dokunuşla "sunuma al" veya "öğretmen sorusuna kopyala".
- **Efor:** M · **Risk:** orta — çok kullanıcılı yapı izlenimi vermemeli (K38 sınırı).
  **Karar gerekli.**

### G13 — Ders/konu filtresi modelde var, arayüzde yok
- **Kanıt:** `OgretmenVerisi.kuyruk(grupId, dersId, yalnizGeciken, bugun)`; kuyruk ekranı `dersId`
  geçmiyor. Ders rozetleri yalnız grup kartında.
- **Etki:** "Yarın matematik sınavı var, matematik soruları neler?" sorusu cevapsız.
- **Öneri:** G1 ile birlikte: ders/konu filtre çipleri (arşivli dersler dâhil, adıyla).
- **Efor:** S · **Risk:** düşük.

### G14 — Dönem sonu / arşiv yönetimi
- **Kanıt:** grup `arsivli` bayrağı var; dönem kavramı yok, toplu "dönemi kapat" yok.
- **Etki:** Yıl sonunda 8–12 grup birikir; liste uzar, eski gruplar kuyruğa karışır.
- **Öneri:** "Dönemi kapat": seçili grupları arşivle + çözülmüşleri arşivle + özet (kaç soru,
  kaç kapandı) tek ekranda; geri alma aynı oturumda.
- **Efor:** M · **Risk:** düşük.

---

## 5. Fikir havuzu ve değerlendirme

Değer: 1–5 (öğretmenin gerçek işini ne kadar kısaltıyor) · Efor: S/M/L · Risk: teknik/ürün riski.

| # | Fikir | Boşluk | Değer | Efor | Risk | Öneri |
|---|---|---|---|---|---|---|
| 1 | Kuyruk + grup detayında arama ve ders/konu filtresi | G1, G13 | 5 | S | Düşük | **T3 — ilk iş** |
| 2 | "Kim sordu" rozeti, filtresi ve hızlı isim seçimi | G2 | 4 | S | Düşük | **T3** |
| 3 | Paylaş (soru / soru+çözüm / seçili N soru) | G4 | 5 | S | Düşük | **T3** |
| 4 | "Bugün / Geciken / Planlanmamış" gruplaması + hızlı plan | G6 | 4 | S | Düşük | **T3** |
| 5 | Arşivden geri getirme + grup silmede geri alma penceresi | G8 | 4 | S–M | Düşük | **T3** |
| 6 | Yedek tazeliği uyarısı (7+ gün) | G10 | 3 | S | Düşük | **T3** |
| 7 | Aynı görsel uyarısı (sha256) | G9 | 3 | S | Düşük | **T3** |
| 8 | Hafif içgörü kartı (konu dağılımı, bekleme, haftalık kapanma) | G7 | 4 | M | Düşük | **T3/T4 sınırı** |
| 9 | Fotoğrafsız "sözlü soru" + fotoğraf ekle rozeti | G3 | 5 | M (v6) | Orta | **T4 — karar sonrası** |
| 10 | Hızlı toplama / taslak kutusu (yığın fotoğraf) | G5 | 5 | L (v6) | Orta | **T4** |
| 11 | Günlük özet bildirimi (varsayılan kapalı) | G6 | 3 | M | Orta (izin/pil) | **T4 — karar sonrası** |
| 12 | Yatay/tablet iki panel | G11 | 3 | M | Orta | **T4 — cihaz verisi sonrası** |
| 13 | Öğrenci "bana sorulanlar" şeridi (aynı cihaz) | G12 | 4 | M | Orta | **T4 — karar sonrası** |
| 14 | PDF çalışma kâğıdı / yazdırma | G4 | 3 | M–L | Orta | **T4 — paylaşım sonrası ölç** |
| 15 | Dönemi kapat (toplu arşiv + özet) | G14 | 3 | M | Düşük | **T4** |
| 16 | Uygulama kilidi (biyometri) | — | 2 | M | Yüksek | **Red** (kullanıcı kararı: "şifre vesaire yapma") |
| 17 | Bulut senkron / hesap / çok cihaz | — | 4 | XL | Çok yüksek | **Red** (sunucusuz ilke, K38) |
| 18 | OCR / AI ile soru-çözüm üretme | — | 3 | XL | Yüksek | **Red** (K36, §7) |
| 19 | Öğretmene XP/rütbe/seri, sınıf liderlik tablosu | — | 1 | M | Yüksek | **Red** (iş aracı oyunlaştırılmaz) |
| 20 | Doğrudan WhatsApp API / mesaj gönderme | — | 2 | L | Çok yüksek | **Red** (`ACTION_SEND` yeterli) |
| 21 | Notlandırma/karne, sınıf yönetimi, LMS entegrasyonu | — | 2 | XL | Yüksek | **Red** (başka ürün; veri sorumluluğu) |
| 22 | Öğrenci hesabı / öğrenci cihazından gönderim | — | 3 | XL | Yüksek | **Red v1** (çok kullanıcılı yapı) |

---

## 6. Önerilen sıralama

### T3 — "görünürlük ve iletişim" (1.6.x, şema değişmeden)
Amaç: mevcut veriyi kullanılabilir hâle getirmek. **Room/ZIP sürümü artmaz.**

1. **Arama + ders/konu filtresi** (G1, G13) — kabul: 1.000 soruda ilk sonuç < 200 ms; filtre
   durumu ekran dönüşünde korunur (`rememberSaveable`).
2. **"Kim sordu" yüzeyi** (G2) — kart rozeti, filtre, hızlı isim çipleri; normalize anahtar
   (`kucuk harf`, kırpma) ile gruplama.
3. **Paylaşım** (G4) — `ACTION_SEND` + `FileProvider`; varsayılan **kim sordu dışarıda**; çoklu
   seçimde "seçilenleri paylaş".
4. **Bugün/Geciken/Planlanmamış** (G6) — plan günü hızlı aksiyonları.
5. **Kapatma döngüsü** (G8) — arşiv filtresi, arşivden çıkarma, grup silmede geri alma penceresi.
6. **Yedek tazeliği** (G10) + **aynı görsel uyarısı** (G9).
7. **İçgörü kartı** (G7) — T3 sonunda, ölçüm için temel sayılar.

**T3 kabul kapısı:** Debug/Release testler yeşil + fiziksel cihazda (a) 200+ soruluk grupta arama,
(b) 5 soruyu paylaşma, (c) bir grubu silip 10 sn içinde geri alma, (d) 9 günlük yedekte uyarı.

### T4 — "kayıt yükünü azaltma" (1.7.0, Room v6 + ZIP v6)
1. **Fotoğrafsız sözlü soru** (G3) — `gorsel_bekliyor`; yedek doğrulaması ve göç testi zorunlu.
2. **Hızlı toplama / taslak kutusu** (G5) — taslak yaşı, "taslakları temizle", toplu alan atama.
3. **Bana sorulanlar şeridi** (G12) — aynı cihaz varsayımı yazılı olarak sınırlanır.
4. **Dönemi kapat** (G14) ve **PDF/çalışma kâğıdı** (14) — paylaşım kullanımı ölçüldükten sonra.
5. **Tablet/yatay** (G11) — yalnız gerçekten tablet kullanılıyorsa.
6. **Bildirim** (G11→11) — varsayılan kapalı, günlük tek özet, izin istenmeden önce açıklama.

**Sıra gerekçesi:** veri güvenliği → günlük akış → iletişim → yapısal. T3'ün tamamı mevcut v5
verisiyle çalışır; T4'ün ilk iki maddesi ancak şema göçüyle ve yedek biçimi v6 ile yapılabilir
(v5 yedekleri okunmaya devam eder).

---

## 7. Nasıl ölçeriz (başarı kriteri)

| Ölçüt | Bugün | Hedef |
|---|---|---|
| Tek soru kaydı (kamera → kaydet) | ölçülmedi | ≤ 20 sn, ≤ 6 dokunuş |
| 10 soruluk ödev toplama | 10 tam form döngüsü | T4 sonrası ≤ 3 dk |
| 200+ soruluk grupta arama ilk sonuç | yok | < 200 ms |
| Kuyruktaki en eski sorunun yaşı | yalnız gösterim | T3 sonrası haftalık düşüş |
| Son yedeğin yaşı | gösterilmiyor | 7 günden eskiyse uyarı |
| Kapanma oranı (çözülen / gelen) | hesaplanmıyor | içgörü kartında görünür |

Not: bunlar **yerel** ölçümlerdir; telemetri yok, veri cihazdan çıkmaz (K38).

---

## 8. Karar gerektiren sorular

| # | Soru | Seçenekler | Varsayılan öneri |
|---|---|---|---|
| S1 | Fotoğrafsız (sözlü) soru kaydı olsun mu? | (a) hayır, fotoğraf zorunlu kalsın · (b) evet, "fotoğraf bekliyor" rozetiyle | **(b)** — `kimSordu` alanı zaten sözlü soruyu ima ediyor |
| S2 | Paylaşım kime? | (a) öğrenci · (b) veli · (c) yalnız kendine/başka uygulamaya | **(c) + sistem sayfası**; uygulama kanal bilmez |
| S3 | Günlük bildirim istiyor musun? | (a) hayır · (b) evet, akşam tek özet | **(a)** ile başla, T3 ölçümünden sonra sor |
| S4 | Cihaz: telefon mu, tablet mi, ikisi de? | — | Yatay iki panel ancak tablet varsa |
| S5 | Aynı cihazı öğrenci de kullanıyor mu? | (a) hayır · (b) evet | (b) ise G12 (bana sorulanlar) öne alınır |
| S6 | PDF çalışma kâğıdı gerekli mi? | (a) paylaşım yeter · (b) PDF şart | **(a)** önce; kullanım görülürse (b) |

---

## 9. Kaynaklar

1. DfE — *Working lives of teachers and leaders* (2024): %74 genel idari iş, %53 veri kaydı/analiz
   "çok fazla zaman" — <https://assets.publishing.service.gov.uk/media/685bcd654cd6b031687097cd/Working_lives_of_teachers_and_leaders_-_qualitative_insight_into_teacher_leader_workload.pdf>
2. *The components and implications of teacher workload: a review* (2026): %75 idari, %49 veri işi —
   <https://www.tandfonline.com/doi/full/10.1080/00131881.2026.2629284>
3. Evidence Based Education — *Effective and efficient exit tickets*: biçimlendirici değerlendirmede
   asıl değer, kanıtı sonraki adıma çevirmek —
   <https://evidencebased.education/resource/effective-and-efficient-exit-tickets/>
4. Structural Learning — *Exit tickets*: kanıt kalitesi düşük–orta, etki değişken —
   <https://www.structural-learning.com/post/exit-tickets>
5. Agarwal, Nunes & Blunt (2021) — 50 sınıf deneyi, etkilerin %57'si orta/büyük —
   <https://pdf.poojaagarwal.com/Agarwal_etal_2021_EDPR.pdf>
6. Murray, Horner & Göbel (2025) — matematikte aralıklı tekrar g = 0,28 (küçük–orta) —
   <https://link.springer.com/article/10.1007/s10648-025-10035-1>

> Kaynaklar yalnız **yön** belirlemek için kullanıldı; bu uygulama hiçbir öğrenme iddiasını
> garanti olarak sunmaz (K36, §7).
