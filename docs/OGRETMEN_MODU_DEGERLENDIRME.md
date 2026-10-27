# Öğretmen Modu — Ürün ve Teknik Değerlendirme

- Tarih: **2026-10-25**
- Kapsam: `OgretmenAnaEkrani.kt`, `OgretmenDeposu.kt`, öğretmen modelleri, yedekleme entegrasyonu, manifest, testler, dokümanlar
- Durum: **analiz ve öneri** — bu belge için **hiçbir üretim kodu değiştirilmedi**

## 0. Yöntem ve sınırlar (dürüstlük notu)

Bu belgedeki tespitler üç ayrı güven seviyesindedir; karıştırılmamalıdır:

| Seviye | Ne demek | Bu belgede |
|---|---|---|
| **(R)** | Kod okumasıyla **kesin** — kod yolu tek anlamlı | S1, S2, S3, S4, S5, S12; S6'nın "yok" maddeleri |
| **(R?)** | Kod okumasıyla çok güçlü olasılık; cihazda 1–2 dakikada doğrulanır | S6'nın görsel sonucu, S15, S16 |
| **(D)** | Yalnızca gerçek cihazda / TalkBack / büyük yazıda / ölçümle belirlenir | S13, S14, performans (S7, S8 büyük veride) |

- Bu ortamda **Java/Android SDK yok**; derleme ve JUnit çalıştırılmadı. "Derleme geçti" gibi bir iddia yok.
- Gerçek cihaz davranışı test edilmedi. Belgede "çalışmaz" dediğim yerler kod yoluna dayanır; cihaz teyidi gerekir.
- Yedek/geri yükleme kodunu satır satır okudum; **paylaşılan görsel** (bir soru birden fazla grupta) senaryosunda yedek doğrulamasının `distinctBy` ile tutarlı olduğunu doğruladım; bu noktada hata **yok**.

---

## 1. Yönetici özeti

Öğretmen modu, 1.5.106–1.5.108 arasında **depolama + iki sekme** olarak iyi kurulmuş; ama öğretmenin asıl iş döngüsünü (topla → kuyruğa al → sınıfta çöz → sonucu öğrenciye kapat → arşivle) **kapatmıyor**. En kritik beş bulgu:

1. **Kamera düğmesi öğretmen formunda ölü** (S1). `AndroidManifest.xml` içinde `CAMERA` izni **yok**, ama öğretmen formu ve "Çözdürdüm" penceresi izin *isteyerek* kamera açıyor. Beyan edilmemiş izin isteği anında reddedilir → sessiz başarısızlık. Öğretmen modunun ana giriş yöntemi (fotoğraf çek) fiilen çalışmıyor; yalnızca galeri kullanılabiliyor.
2. **Öğretmen verisi sessizce sıfırlanabilir** (S2). `OgretmenDeposu.baslangicOku()` hatayı yutup boş veri döndürüyor; ardından ilk yazma işlemi dosyayı boş veriyle **üzerine yazıyor**. Tek bozuk kayıt / bilinmeyen enum / sürüm düşüşü → yıllık çalışma alanı sessizce buharlaşır, kullanıcıya hiçbir uyarı gitmez.
3. **Çözümü kaydetme ve öğrenciye geri gönderme yok** (S4). Öğretmen yalnızca *metin* cevap girebiliyor; çözüm fotoğrafı yok, paylaş/çıktı yok. Hâlbuki "öğrenciden gelen soruyu çözüp geri gönderme" öğretmenin döngüsünü kapatan adımdır.
4. **Birleşik kuyruk ve öncelik yok** (S5). Sınıf sınıf liste var, "bugün hangilerini çözeceğim" yok; tarih ekranda hiç gösterilmiyor, sıralama sabit (en yeni önce), yaş/gecikme kavramı yok.
5. **Sunum modu sınıf için eksik** (S6). Tam ekranda sonraki/önceki **soru** yok, ekranı açık tutma yok, çizim katmanı yok — oysa öğrenci tarafında hazır bir `CozumTahtasiDialog` var. Anlatırken ekran kilitlenebilir.

Bunlara ek: veri modeli (S3), bakım/depolama (S7), bulunabilirlik (S8), yıkıcı işlemler (S9), **doküman-kod çelişkisi** (S10), test kapsamı (S11) ve kod yapısı (S12) aşağıda.

---

## 2. Bugünkü durum: ne var / ne yok

### 2.1 Var olan (ve korunması gereken) iyi kararlar

- Öğretmen modu **XP/rütbe/tekrar motorundan tamamen ayrı** — doğru karar; öğretmene oyunlaştırma dayatılmıyor.
- Grup bazında **bağımsız `çözüldü` durumu** ve "Başka Gruba Ekle" kopyalama; kopyaların görsel dosyalarını **referans sayımı** ile koruyor (`OgretmenVerisi.kaliciDosyaAdlari()`).
- **Cevap varsayılan gizli**, sunumda "Cevabı göster".
- Yazma yolu **atomik**: geçici dosya → `fsync` → `ATOMIC_MOVE`; görsel kopyalama için geri alma var (`soruKaydet` `finally`).
- **ZIP yedeğine tam entegrasyon**: `teacher_workspace`, `counts.teacher_images`, `ogretmen-img/` → `images/`, kurtarma günlüğü, geri yükleme öncesi güvenlik yedeği, görsel doğrulama (SHA-256 + boyut + piksel).
- Hazır grup önerileri, monogram/renk kodu, arama, büyük yazı uyumu, klavye/inset disiplini.
- Hesapsız, sunucusuz, çevrimdışı — proje ilkeleriyle uyumlu.

### 2.2 Yok olan (ihtiyaç tarafı)

| İhtiyaç | Bugün | Sonuç |
|---|---|---|
| Ders / konu / kaynak etiketi | Yok, yalnızca serbest metin başlık | "Türevden gelen sorular" geri bulunamıyor |
| Çözüm fotoğrafı | Yok, yalnızca metin cevap | Öğrenciye geri gönderilecek somut çözüm üretilemiyor |
| Paylaşma / dışa aktarma / paylaş menüsünden içe alma | **Hiç yok** (repoda tek bir `ACTION_SEND` yok) | WhatsApp → uygulama yolu yok; her şey elle |
| Tarih / yaş / vade | Alan var (`olusturma`), **ekranda hiç gösterilmiyor** | "Kim ne kadar bekledi" cevaplanamıyor |
| Sınıflar arası birleşik kuyruk | Yok (yalnız 2 sekme: Gruplarım, Ayarlar) | 6 sınıfı olan öğretmen tüm birikmişi göremiyor |
| Sıralama / filtre | Yalnız zorluk; sıra sabit "en yeni önce" | En eski bekleyen soru listenin dibinde |
| Toplu işlem | Yok | Derste 20 soruyu tek tek işaretlemek |
| Gruplar arası **taşıma** | Yok (kopyala + sil) | Tarih ve çözüldü durumu kayboluyor |
| Arşiv / temizlik | Yok ("Çözdüklerim" sonsuza büyür) | Binlerce kart, yavaşlık, depolama |
| Bakım / depolama göstergesi | Yok | Öğretmen görselleri **hiç taranmıyor** |
| Sunumda sonraki soru / ekran açık / çizim | Yok | Sınıf içi kullanım yarım |
| Öğretmen için test/önizleme | 3 test, 0 Compose test/önizleme | 2.561 satırlık ekran güvencesiz |

---

## 3. Öğretmenin gerçek iş döngüsü ve ihtiyaç haritası

Öğretmen modu, öğrenci modunun türevi değil; **kendi döngüsü** olan bir iş aracı. Doğru tasarım şu beş adımı en az dokunuşla kapatır:

| # | Adım | Bugünkü karşılama | Hedef (öneri) |
|---|---|---|---|
| 1 | **Topla** — öğrenciden gelen soru fotoğrafı | Kamera **bozuk** (S1), paylaş menüsü yok, form 3 ekran | Paylaş menüsünden 1 dokunuş; "hızlı çekim" modunda çek→kaydet döngüsü |
| 2 | **Kuyruğa al** — sınıf + ders/konu + kim sordu | Yalnız grup; etiket yok | Grup + isteğe bağlı ders/konu/kaynak/"kim sordu" |
| 3 | **Çöz** — sınıfta/tahtada sun | Tam ekran + zoom var; sonraki soru, ekran açık, çizim yok | Sunum kuyruğu: sonraki/önceki, ekran açık, çizim katmanı, cevap gizli |
| 4 | **Kapat** — çözümü kaydet ve öğrenciye gönder | Yalnız "Çözdüm" + metin cevap | Çözüm fotoğrafı + not + paylaş/çıktı |
| 5 | **Arşivle** — dönem sonu, arama, yedek | "Çözdüklerim" sekmesi, otomatik yedek yok | Arşiv, toplu temizlik, depolama göstergesi, yedek hatırlatması |

Bu tablo, "öğretmen tarafı ciddi manada ihtiyaca uygun olsun" isteğinin ölçülebilir hâlidir: her adımın **dokunuş sayısı** ve **kaçış yolu** (işlem yarıda kalırsa ne olur) tanımlanmalı. Bugün 1. adım çalışmıyor, 4. ve 5. adım yok.

### 3.1 Öğretmen kim? — tasarımı belirleyen üç ayrım

Aynı "öğretmen modu" üç farklı kullanıcıya hizmet ediyor; önerilerin önceliği buna göre değişir:

1. **Sınıf öğretmeni (kalabalık, 5–8 şube):** Öncelik = *kuyruk, toplu işlem, yaş/gecikme, sunum*. Tek tek kart gezmek imkânsız.
2. **Özel ders / etüt öğretmeni (tek öğrenci = tek grup):** Öncelik = *öğrenci bazlı geçmiş, çözüm fotoğrafı, paylaşma*.
3. **Kendi çalışmasını da yapan öğretmen:** Bugün rol düğmesiyle mod değiştirmek zorunda; iki dünya arasında köprü yok. Bu bir **ürün kararı** olarak netleşmeli (bkz. §11).

---

## 4. Sorun tespitleri

Ağırlık: **K** = kritik, **Y** = yüksek, **O** = orta, **D** = düşük.

### S1 — K Öğretmen formunda kamera ölü (beyan edilmemiş izin)

**Kanıt**
- `app/src/main/AndroidManifest.xml:3` — yalnız `VIBRATE` beyan edilmiş; `CAMERA` **yok**, hiçbir bağımlılık da beyan etmiyor (`build.gradle.kts`'te kamera kütüphanesi yok).
- `ui/ogretmen/OgretmenAnaEkrani.kt:2084-2100` — `checkSelfPermission(CAMERA)` → değilse `kameraIzni.launch(...)`; izin verilmezse **hiçbir şey olmuyor** (hata mesajı da yok).
- `ui/SorulacaklarEkrani.kt:718-733` — "Çözdürdüm → çözüm fotoğrafı" aynı desende.
- Karşılaştırma: `ui/SoruFormuEkrani.kt:279-293` doğru deseni kullanıyor — izin sormadan doğrudan `TakePicture` başlatıyor (beyan edilmemiş izinle ACTION_IMAGE_CAPTURE izin gerektirmez).

**Etki:** Öğretmenin en sık eylemi (kamerayla soru ekleme) sessizce çalışmıyor. Yalnız galeri kalıyor; "öğrenci sorusunu hızlıca çek" akışı ölü. Aynı hata öğrenci tarafında "çözüm fotoğrafı" eklemeyi de vuruyor.

**Öneri (iki seçenek, biri seçilmeli):**
- **(a) Tercih edilen:** Öğretmen formu ve `CozdurdumDialog` içindeki izin kapısını **kaldır**, öğrenci formundaki gibi doğrudan `TakePicture` kullan. Manifest'e `CAMERA` **ekleme**.
- **(b)** `CAMERA` iznini beyan et + her iki akışta da runtime izin ve **reddedilince açıklayıcı mesaj + "galeriden seç" yönlendirmesi** ekle. (Daha fazla kod, daha çok durum.)

(a) seçilirse `Manifest`/`PackageManager` importları temizlenir. Bu, tek satırlık olmayan ama **en düşük riskli** düzeltmedir.

**Kabul:** Cihazda: öğretmen formu → kamera → çek → görsel eklenir; izin diyaloğu çıkmaz; galeri akışı bozulmaz. "Çözdürdüm" penceresinde de aynı.

---

### S2 — K Öğretmen verisi sessizce sıfırlanabilir

**Kanıt:** `data/prefs/OgretmenDeposu.kt:36` + `41-44`
```kotlin
private val state = MutableStateFlow(baslangicOku())
private fun baslangicOku(): OgretmenVerisi = runCatching {
    if (!storageFile.isFile) OgretmenVerisi()
    else jsonOku(JSONObject(storageFile.readText(Charsets.UTF_8)))
}.getOrDefault(OgretmenVerisi())
```
`jsonOku` (`:334`) **katı**: `images` dizisi yoksa, `width`/`height` eksikse veya `difficulty` bilinmeyen bir değerse `IOException` atar. Yani **tek bir kayıt** tüm çalışma alanını düşürür.

**Etki zinciri:** Açılışta boş `state` → UI "hiç grup yok" gösterir → kullanıcı yeni grup ekler → `atomikYaz` **bozuk dosyanın üzerine** boş+1 grup yazar → bozuk dosya da gider → yedek yoksa kurtarma imkânsız. Görsel dosyaları diskte kalır ama referansları kaybolur (bkz. S7).

Tetikleyiciler: sürüm düşürme (yeni enum değeri), elle/dışa aktarma hatası, kısmi dosya sistemi sorunu, ileride eklenecek alanların eski sürümce reddedilmesi.

**Öneri:**
1. `baslangicOku` **hata durumunu ayrı taşısın** (`Yukleniyor | Hazir | Bozuk`); bozuksa **yazma kilidi** ve kalıcı uyarı bandı ("Çalışma alanı okunamadı, yeni kayıt yapılmıyor; yedekten geri yükleyin").
2. Bozuk dosyayı silmeden `ogretmen_calisma_alani.bozuk-<zaman>.json` olarak **karantinaya** al.
3. `jsonOku` **kayıt bazında toleranslı** olsun: bozuk kaydı atla, sayısını raporla; dosyanın tamamını düşürme.
4. En az bir **"son iyi kopya"** (`.bak`, başarılı her yazımda önceki dosya) tut.
5. Test: bozuk JSON, bilinmeyen zorluk, eksik görsel dizisi, `images` null → veri korunur / yazma kilitlenir.

---

### S3 — Y Çalışma alanı şemasının sürümü yok

**Kanıt:** `OgretmenDeposu.jsonYaz` (`:284-330`) yalnız `groups` + `questions` yazıyor; **`schema_version` yok**. `jsonOku` bilinmeyen alanları sessizce atıyor. ZIP formatı `FORMAT_VERSION = 4` (`YerelYedeklemeHizmeti.kt:1700`) ve `teacher_workspace` bu sözleşmenin parçası, ama öğretmen yükünün kendi sürümü yok; `docs/VERI_MODELI.md` ve `docs/YEDEK_FORMATI.md` içinde **tek kelime** geçmiyor.

**Etki:** Ders/konu/çözüm fotoğrafı/arşiv gibi alanlar eklendiğinde: (a) eski sürüm yeni yedeği sessizce eksik okur, (b) yeni sürüm eski veriyi varsayımla yorumlar, (c) geri dönüş (downgrade) veri kaybı üretir, (d) yedek doğrulaması bunu **yakalayamaz**.

**Öneri:** `teacher_workspace.schema_version = 1` (mevcut durum) ile başla; v2'ye geçerken dosyayı `...v1.bak` olarak sakla; migration'ı öğrenci tarafındaki gibi **veri koruyan** ve test edilen bir fonksiyona bağla. ZIP manifestine `teacher_workspace_version` ekle; bilinmeyen yüksek sürümde geri yüklemeyi **reddet**, sessizce okuma.

---

### S4 — Y Çözümü kaydetme ve öğrenciye geri gönderme yok

**Kanıt:** `domain/model/OgretmenModelleri.kt` — `OgretmenSorusu` alanları: `baslik, zorluk, cevap (String), cozuldu, gorseller`. Görsellerin türü yok (`GorselTuru.SORU` sabit, `toGirdi()`), çözüm görseli ayrımı yok. Formda (`OgretmenAnaEkrani.kt:2251-2259`) yalnız serbest metin cevap var. Repoda **tek bir `ACTION_SEND`/paylaşım yolu yok**.

**Etki:** Öğretmen "çözdüm" diyebiliyor ama **çözümü teslim edemiyor**. Özel ders/WhatsApp akışı tamamen uygulama dışında kalıyor. Öğrenci tarafında bu yetenek var (`GorselTuru.COZUM`, `CozdurdumDialog`, `CozumTahtasiDialog`) — yani altyapı **mevcut**, öğretmen tarafına bağlanmamış.

**Öneri (T1'in çekirdeği):**
1. `OgretmenGorseli`'ye `tur: GorselTuru` (SORU/COZUM) ekle → şema v2.
2. Sunum ve kart ekranında **"Çözümü ekle"**: kamera/galeri + mevcut `CozumTahtasiDialog`'u yeniden kullan (çiz, kaydet).
3. Sunumda "Çözümü göster" ikinci bir gizli katman olsun (cevap metninden bağımsız).
4. **Paylaş**: soru + çözüm + cevabı tek `ACTION_SEND` (`image/*` + metin) ile dışa ver. Tek soru; PDF/çoklu seçim sonraya.
5. (Ayrı ve çok değerli) **Paylaş menüsünden içe alma**: `MainActivity`'ye `ACTION_SEND`/`ACTION_SEND_MULTIPLE` (`image/*`) intent-filter; hedef grup seçimi → doğrudan staging'e. Bugün öğretmen önce galeriye kaydetmek zorunda.

**Kabul:** Ders sırasında: tam ekran → çiz → çözümü kaydet → "Paylaş" → WhatsApp'ta görsel+metin; uygulamadan çıkmadan.

---

### S5 — Y Birleşik kuyruk, tarih ve öncelik yok

**Kanıt:** Alt menü iki sekme (`OgretmenAnaEkrani.kt:172` `OgretmenAltSekme { GRUPLAR, AYARLAR }`). Grup detayında sıralama sabit: `sortedByDescending { it.olusturma }` (`:1042`). `olusturma` **hiçbir yerde gösterilmiyor** (yalnız sıralamada ve kart sıra numarasında). Vade/yaş/gecikme kavramı modelde yok.

**Etki:**
- 6 şubesi olan öğretmen "bu hafta hangi soruları çözmem lazım" sorusunu tek ekranda cevaplayamıyor; 6 gruba tek tek girip zihinsel birleştirme yapıyor.
- En eski bekleyen soru, listenin **en dibinde** (yeni önce). Yani en çok bekleyen öğrenci en görünmez olan.
- Öğrenciye "hocam sordum, çözmediniz" dedirtme riski artıyor.

**Öneri:**
1. Üçüncü sekme **"Bugün / Kuyruk"**: tüm gruplardan çözülmemişler, gruplara göre filtre çipleri, varsayılan sıralama **en eski önce**.
2. Kart üzerinde **yaş etiketi**: "3 gün", "bugün geldi", "1 hafta+".
3. İsteğe bağlı **vade** ("yarın çözeceğim") ve "gecikenler" bölümü — bildirim yok, uygulama içi yeter (proje kapsamına uygun).
4. Sıralama seçenekleri: en eski / en yeni / zorluk / gruba göre.

**Kabul:** Öğretmen, uygulamayı açar açmaz tüm sınıfların bekleyenlerini tek listede ve yaşlarıyla görür; 2 dokunuşta en eski 10 soruya odaklanır.

---

### S6 — Y Sunum modu sınıf kullanımı için eksik

**Kanıt:** `OgretmenTamEkranSoruDialog` (`OgretmenAnaEkrani.kt:1610-1848`).

| Eksik | Neden önemli |
|---|---|
| Sonraki/önceki **soru** (yalnız görsel sayfası gezinmesi var) | Derste 10 soru = 10 kez kapat-aç |
| **Ekranı açık tutma** (repoda `KEEP_SCREEN_ON` yok) | Telefon 30 sn sonra kilitlenir; öğretmen dokunmadan anlatırken ekran söner |
| **Çizim / tahta katmanı** | `ui/calisma/CozumTahtasiDialog.kt` hazır; sunuma bağlanmamış |
| Sunum sırası / "kaçıncı soru" | Sınıfta takip zorlaşır |
| Tam ekran (immersive) / yatay uyum | Kenar çubukları ve dikey kart oranı projeksiyonda yer kaybı |
| Uzun cevapta **taşma** (bkz. S15) | Cevap kutusu butonları ekran dışına itebilir |

**Öneri:** Sunum durumunu bir "oturum" olarak ele al: `sorular: List<id>, indeks`. Ekran açık bayrağı, sonraki/önceki, "çözüldü + sonraki" tek düğme, çizim katmanı (mevcut tahta bileşeni), cevap/çözüm gizli varsayılan. Bu, ürünün "sınıfta anlat" vaadini gerçekten karşılayan tek maddedir.

---

### S7 — O Bakım, yetim görseller ve depolama

**Kanıt**
- `LocalSoruGorselDeposu.kt:375` `ogretmenKaliciDosyalariListele()` **tanımlı ama hiç çağrılmıyor** (repo geneli tarama).
- Yetim taraması `GorselTutarliligiHizmeti` **yalnız Room** görsellerini geziyor; `ogretmen-img/` "öğrenci yetim taramasından izole" (HANDOFF) → **öğretmen görselleri için hiçbir temizlik yolu yok**.
- Ayar ekranı (`OgretmenAnaEkrani.kt:2356-2510`) yalnız rol + yedek + ses içeriyor; depolama göstergesi / bakım yok.
- Silme akışları önce JSON'u yazıp **sonra** dosyaları siliyor; arada süreç ölürse yetim kalır (kurtarma yok).

**Etki (ölçek hesabı):** 5 sınıf × haftada 10 soru × 36 hafta ≈ **1.800 soru**; soru başına 1,5 fotoğraf (3000 px / JPEG 90 ≈ 0,8–1,5 MB) ≈ **2,5–4 GB**. Yetimler bunun üstüne biner; kullanıcı bunu **göremez**. JSON ise soru başına ~500–700 B → ~1,2 MB; her "çözüldü" dokunuşunda **tam dosya** yeniden yazılıyor.

**Öneri:**
1. Öğretmen görselleri için **referans kontrollü yetim taraması** (JSON'daki `kaliciDosyaAdlari()` ile disk karşılaştırması, 7 günden eski dosyalar, onaylı silme) — `ogretmenKaliciDosyalariListele()` zaten bunun için yazılmış.
2. Öğretmen Ayarlar'ına **depolama kartı**: toplam boyut, grup bazında boyut, "son yedek" yaşı, "son 7 gün eklenen".
3. **Arşiv**: çözülenleri tarih eşiğiyle arşive al (görünümden çıkar, veri silinmez).
4. Yazma sıklığını azalt: `çözüldü` gibi küçük değişiklikler için tek alan yazımı (veya Room'a geçiş, §5).

---

### S8 — O Yeniden bulma (arama/filtre) zayıf

**Kanıt**
- Grup araması yalnız **≥4 grup** varsa görünüyor (`:602`), soru araması yalnız **≥3 soru** varsa (`:1274`). Küçük veride arama yok — "yok" sanılıyor.
- Arama **yalnız aktif grup içinde**; sınıflar arası arama yok.
- Arama kapsamı `baslik` + `cevap`; cevap aramak gizli cevabı ima ediyor (içerik sızdırmaz ama sonuç listesinde "cevabı var" bilgisi görünür).
- Kart görseli sabit **196 dp** (`:1469`) → 100 soruluk grupta tarama çok yavaş; kompakt liste yok.
- Grup listesi sayıları her kart için `veri.sorular.filter{}` ile hesaplanıyor (`:657`) → O(grup × soru) her yeniden çizimde.

**Öneri:** global arama (grup adı + başlık + tarih), görünüm anahtarı (kart/kompakt), tarih başlıklarıyla gruplama ("Bu hafta", "Geçen hafta"), arama alanını eşik olmadan göster, sayımları tek geçişte hesapla (`groupingBy`).

---

### S9 — O Yıkıcı işlemler ve kopya yönetimi

- Grup silme onayı **soru sayısını göstermiyor** (`strings.xml:999` yalnız grup adı). 200 soruluk şubeyi silmek tek onay; geri alma yok.
- "Başka Gruba Ekle" **mükerrer kopyayı engellemiyor**; aynı soru bir gruba iki kez eklenebilir.
- **Taşıma yok**: yanlış gruba eklenen soru için kopyala+sil → `olusturma` ve `cozuldu` kaybı.
- Kopyanın **hangi gruplarda bulunduğu** gösterilmiyor → öğretmen aynı soruyu iki kez çözebilir.
- "Çözüldü" tek dokunuşla değişiyor, **geri alma** yok (yanlış dokunuş sessiz).

**Öneri:** Onay metnine sayı + "son yedek X gün önce"; silme ve toplu işlemlerde `Snackbar` "Geri al"; kopya sayısı rozeti; **taşı** eylemi; mükerrer uyarısı.

---

### S10 — O Doküman ve karar kaydı çelişkisi

Bu, kod hatası değil ama projenin kendi çalışma disiplinini bozuyor:

- `PROJECT_BRIEF.md:22` "Çok kullanıcılı yapı, sosyal özellik, **öğretmen paneli yok**"; `:166` kapsam dışı listesinde yine "öğretmen paneli". Oysa uygulamada öğretmen modu **var** ve veri modeli, yedek formatı, rol seçimi ekliyor.
- `ROADMAP.md` içinde öğretmen modu için **tek bir madde yok** (grep: 0 sonuç).
- `docs/VERI_MODELI.md` ve `docs/YEDEK_FORMATI.md` `teacher_workspace` / `ogretmen-img` konusunda **sessiz**; `docs/` altında yalnız `FIKIR_ALMA_METNI.md` ve derleme raporu değiniyor.
- `PROJECT_BRIEF.md:102` "Görsel yükleme: **Coil**" diyor; `gradle/libs.versions.toml` ve `app/build.gradle.kts` içinde **Coil yok** (elle `BitmapFactory` + `produceState`). `A11` açık sorusu da buna dayanıyor.

**Etki:** Bir sonraki geliştirme oturumu (insan veya yapay zekâ) "öğretmen paneli kapsam dışı" okuyup mevcut modu **yanlış** anlar; yeni alanlar eklendiğinde hangi sözleşmeye uyulacağı belirsiz kalır. Projenin "her önemli adımdan sonra belgeler güncellenir" kuralı burada uygulanmamış.

**Öneri (kod değil, belge):**
1. `PROJECT_BRIEF` §1/§7'yi düzelt: "öğretmen **paneli** (başka cihazdan öğrenci yönetimi) kapsam dışı; **aynı cihazdaki öğretmen çalışma alanı** kapsam içi (K38+)". Karar numarası ver.
2. Yeni **`docs/OGRETMEN_MODU.md`**: veri modeli, sınırlar, akışlar, kabul kriterleri, kapsam dışı.
3. `VERI_MODELI.md` + `YEDEK_FORMATI.md`'ye `teacher_workspace` bloğu ve sürüm kuralı.
4. `ROADMAP`'e öğretmen maddeleri (T0/T1) ve fiziksel kabul satırları.
5. Coil kararını ya uygula ya da `PROJECT_BRIEF`'te "elle çözme + ölçüm" olarak güncelle (A11'i kapat).

---

### S11 — O Test kapsamı yetersiz

**Kanıt:** Öğretmen için tek dosya `app/src/test/.../OgretmenVeSorulacaklarVeriTest.kt` ve **3 test**: sorulacak akışı, gruplar arası bağımsız durum + ortak görsel, ZIP round-trip. Buna karşılık 2.561 satırlık UI ve 392 satırlık depo var. Öğretmen ekranları için Compose testi/önizleme yok (P6 önizlemeleri yalnız `seviye`).

**Eksik test başlıkları:** bozuk JSON (S2), şema sürümü/ileri uyum (S3), kopya sonrası silme/yetim (S7), grup silme sırasında kısmi hata, mükerrer kopya, uzun cevap, arama/filtre, `cozuldu` değişiminin kalıcılığı, eşzamanlı iki işlem (mutex), yedek v3→v4 uyumu.

**Öneri:** Depo için en az 12–15 hedef test (Robolectric, mevcut altyapı yeterli) + 3–4 Compose testi (grup ekle, soru ekle, çözüldü değiştir, tam ekran aç/kapat) + 2 native önizleme (büyük yazı, koyu tema).

---

### S12 — D Kod yapısı ve test edilebilirlik

- `ui/ogretmen/OgretmenAnaEkrani.kt` **2.561 satır**, 15+ `private @Composable`; gruplar/detay/form/sunum/ayarlar tek dosyada. Değişiklik yapmak riskli, incelemesi zor.
- `data/prefs/OgretmenDeposu.kt` — "prefs" paketinde ama JSON dosyası + dosya sistemi + görsel deposu + iş kuralları. `Context` alan somut sınıf; **arayüz yok** → UI doğrudan concrete'e bağlı, sahte depo ile önizleme/test zor.
- Tüm veri tek `StateFlow<OgretmenVerisi>`; her mutasyon tüm listeyi kopyalıyor. Küçük veride sorun değil, ama sınır yazılı değil.
- `AppContainer.kt:30` depoyu **eager** oluşturuyor → JSON okuma+parse **açılışta ana thread'de** (öğrenci kullanıcısı için bile). Büyük veride açılış jank'i.
- UI dosyasında `com.keremtulu.questionnote.*` tam-sınıf yolları, `BuildConfig`, `Formatter` vb. karışık.

**Öneri:** `data/ogretmen/` paketi + `OgretmenRepository` arayüzü; ekranları 4–5 dosyaya böl (Gruplar / Detay / Form / Sunum / Ayarlar); başlangıç okumasını `Dispatchers.IO`'ya al ve **lazy** başlat; UiState tek nesne olarak toparlansın (hata, yükleniyor, veri).

---

### S13 — D Erişilebilirlik ve büyük yazı

- Alt menü ikonları `Canvas` ile çiziliyor; `contentDescription` yalnız **büyük yazıda ve seçili olmayan** sekmede atanıyor (`:395`). TalkBack'te iki sekme adı belirsiz kalabilir.
- Kart görseli sabit 196 dp; `1.6x` yazı ölçeğinde başlık/rozet satırları taşabilir — **cihazda doğrulanmalı**.
- Sunum penceresinde odak sırası ve geri tuşu `BackHandler` ile doğru; ama içerik betimlemeleri genel (`question_image_description`).
- Öğretmen ekranları için P6 benzeri büyük yazı / koyu tema kapısı **yok**.

---

### S14 — D Gizlilik ve veri sorumluluğu

- Öğretmen modunda öğrencilerin el yazısı, adı, okul bilgisi fotoğrafta olabilir. Uygulamada kilit yok; ZIP yedeği **şifresiz**; `allowBackup=false` (iyi) ama yedek dosyası kullanıcının seçtiği yerde duruyor.
- Yedek bildirimi/hatırlatıcısı yok → yıllık veri tek cihazda, tek kopya riski.
- **Öneri:** kısa bir KVKK/gizlilik notu (Ayarlar'da 2 satır), isteğe bağlı **uygulama kilidi** (biyometri) ve yedek şifreleme kararı (A5) öğretmen verisiyle birlikte yeniden değerlendirilsin. Şifreleme, parola kaybı riski nedeniyle **v1'de önerilmez**; ama karar kaydı güncellenmeli.

---

### S15 — O Uzun cevap sunum düzenini taşırıyor *(cihazda teyit)*

**Kanıt:** Sunumdaki dış `Column` (`:1643-1650`) `fillMaxSize` + `Arrangement.SpaceBetween`, **dikey kaydırma yok**; görsel `weight(1f)`, alt panel serbest yükseklik. Cevap alanı (`:2251`) `minLines = 2`, üst sınır yok; depo 50.000 karaktere izin veriyor (`MAX_METIN`).

**Etki:** Öğretmen uzun bir çözüm metni yapıştırırsa alt panel büyür, görsel küçülür ve en alttaki "Çözdüm / Başka Gruba Ekle" düğmeleri ekran dışına taşabilir. Kapatma üstte olduğu için tam kilitlenme olmaz ama ana eylem kaybolur.

**Öneri:** Alt paneli `heightIn(max = …)` + `verticalScroll` ile sınırla; cevap alanına karakter sayacı ve makul üst sınır (ör. 2.000) koy; sunumda "daha fazla" ile genişlet.

---

### S16 — D Geri dönüşte bağlam kaybı

**Kanıt:** `OgretmenGrupDetayEkrani` durumu `remember(grup.id)` ile tutuluyor; ekran `AnimatedContent` içinde hedef değişince kompozisyondan çıkıyor. Soru formuna gidip dönünce sekme, arama, filtre ve kaydırma konumu **sıfırlanıyor**.

**Etki:** "Çözdüklerim" sekmesinde 40. satırdaki soruyu düzenleyip dönen öğretmen başa döner. Öğrenci tarafında saved-state disiplini var; öğretmen tarafında yok.

**Öneri:** Bu durumları `rememberSaveable` + anahtar olarak yukarı taşı (veya `OgretmenAnaEkrani` içinde ekran başına `SaveableStateHolder` kullan).

---

## 5. Veri modeli ve mimari değerlendirmesi

### 5.1 Bugünkü yapı (özet)

```
filesDir/ogretmen_calisma_alani.json     ← tek dosya, tüm çalışma alanı
filesDir/ogretmen-img/<uuid>.jpg         ← kalıcı görseller (referans: JSON)
ZIP data.json → teacher_workspace        ← aynı JSON
ZIP manifest → counts.teacher_images, files[]
```

**Güçlü yanı:** atomik yazma, mutex ile serileştirme, görsel referans sayımı, yedek entegrasyonu, görsel bütünlük doğrulaması.
**Zayıf yanı:** şema/sürüm yok, tolerans yok, kısmi kurtarma yok, sorgu/index yok, tam dosya yazımı, ana thread'de açılış okuması, yetim temizliği yok.

### 5.2 Room'a geçiş — gerekli mi, ne zaman?

| Ölçüt | JSON (bugün) | Room |
|---|---|---|
| Şema/migration disiplini | Yok | Var (v1→v4 öğrenci tarafında kanıtlı) |
| Kısmi bozulma toleransı | Yok (S2) | Satır bazında |
| Sorgu/index (ders, konu, tarih, durum) | Bellekte filtre | SQL, ölçekli |
| Küçük güncelleme maliyeti | Tüm dosya | Tek satır |
| Değişiklik riski | — | **Yüksek**: migration + ZIP v5 + test |

**Öneri (aşamalı, risk kontrollü):**
- **Aşama A (T0, düşük risk):** JSON kalır; `schema_version`, toleranslı okuma, karantina, `.bak`, yazma kilidi, yetim temizliği, lazy/IO açılış. Bu adım tek başına S2/S3/S7'yi kapatır ve **veri kaybı riskini sıfıra yaklaştırır**.
- **Aşama B (T2, yapısal):** Ders/konu/kaynak/çözüm/arşiv/vade alanları gerçekten gerektiğinde — ki T1 bunu gerektiriyor — **Room v5 + ZIP v5** ve JSON'dan tek seferlik, doğrulanmış içe aktarma. Öğrenci tarafındaki migration ve kurtarma günlüğü deseni birebir kopyalanabilir.

> Not: Ders/konu etiketi eklemek **zorunlu olarak** Room gerektirmez; JSON v2 ile de yapılabilir. Ancak sınıflar arası arama, tarih/sıralama, toplu işlem, arşiv ve binlerce kayıt bir arada gelince Room maliyeti kendini amorti eder. Karar, T1 kapsamı kesinleşince verilmeli.

### 5.3 Önerilen şema v2 (taslak)

```jsonc
{
  "schema_version": 2,
  "groups": [
    { "id": "...", "name": "9B", "description": "Salı-Perşembe",
      "lesson_id": "matematik",        // opsiyonel, mevcut Ders kataloğuyla hizalı
      "order": 3, "created_at": 1730000000000, "archived_at": null }
  ],
  "questions": [
    { "id": "...", "group_id": "...",
      "title": "Türev - zincir kuralı", "topic_id": "...",
      "source": "Hız Yayınları 3. deneme", "asked_by": "Mert",
      "difficulty": "ORTA",
      "status": "BEKLIYOR",             // BEKLIYOR | COZULDU | ARSIV
      "solved_at": null,
      "due_date": "2026-10-27",         // opsiyonel
      "answer": "f'(x)=...", "solution_note": null,
      "images": [
        { "id": "...", "kind": "SORU",  "order": 0, "...": "mevcut alanlar" },
        { "id": "...", "kind": "COZUM", "order": 0, "...": "mevcut alanlar" }
      ],
      "created_at": 0, "updated_at": 0 }
  ]
}
```

**Geriye uyum kuralları**
- `schema_version` yoksa **1** kabul et.
- `solved: true` → `status = COZULDU`, `solved_at = updated_at`.
- Bilinmeyen alan **korunur** (yazarken geri yaz) veya en azından okuma sırasında düşürülürken uyarı üretilir.
- v2'ye ilk yazımdan önce `ogretmen_calisma_alani.v1.bak` bırak.
- ZIP: `teacher_workspace_version` manifeste yazılır; yüksek sürüm → **geri yükleme reddedilir**.

---

## 6. Önerilen yol haritası

Her madde: **ne / neden / kabul / risk**.

### T0 — "Güvenilir ve çalışır" (tek sürüm, düşük risk, yüksek kazanç)

1. **Kamera düzeltmesi (S1)** — 2 dosya. Kabul: öğretmen formu + "Çözdürdüm" kamerası çalışır; izin diyaloğu yok. Risk: düşük.
2. **Dayanıklı çalışma alanı (S2, S3)** — sürüm alanı, toleranslı okuma, karantina + `.bak`, bozukta yazma kilidi ve uyarı bandı. Kabul: bozuk JSON senaryosunda veri korunur ve kullanıcı görür. Risk: orta (okuma yolu değişiyor) → 8–10 test şart.
3. **Paylaş menüsünden içe alma** — `ACTION_SEND`/`ACTION_SEND_MULTIPLE` (`image/*`), hedef grup seçimi → staging. Kabul: WhatsApp'tan 2 dokunuşla soru. Risk: düşük-orta (Activity yönlendirme).
4. **Yıkıcı işlem güvenliği (S9)** — grup silmede soru sayısı + son yedek yaşı; silme/toplu işlemlerde geri al; mükerrer kopya uyarısı. Risk: düşük.
5. **Sunum temelleri (S6/S15)** — ekranı açık tut, sıradaki/önceki soru, "çözüldü + sonraki", cevap paneli kaydırma, cevap gizleme otomatik. Risk: düşük.
6. **Bakım (S7)** — öğretmen görsel yetim taraması + depolama kartı. Risk: düşük (silme onaylı ve yaş kontrollü olmalı).
7. **Test/önizleme tabanı (S11)** — 15+ depo testi, 4 Compose testi, 2 önizleme. Risk: yok.

### T1 — "Öğretmen akışına uygunluk" (asıl ihtiyaç)

1. **Etiketler:** ders/konu/kaynak/"kim sordu" (opsiyonel, hızlı formu yavaşlatmadan).
2. **Bugün / Kuyruk sekmesi:** tüm gruplar birleşik, en eski önce, yaş etiketi, grup filtresi, isteğe bağlı vade.
3. **Çözüm katmanı:** çözüm fotoğrafı (kamera/galeri/tahta), çözüm notu, gizli varsayılan, paylaş.
4. **Toplu işlem + taşı:** çoklu seç, çözüldü/taşı/sil/arşivle.
5. **Arşiv + kompakt liste + tarih gruplaması.**
6. **Hızlı çekim modu:** grup seçili → çek → kaydet → çek (formu atla). Sınıfta toplama için en büyük kazanç.

### T2 — "Yapısal"

1. **Room v5 + ZIP v5** (öğretmen tabloları), JSON'dan doğrulanmış içe aktarma, öğrenci tarafıyla aynı kurtarma günlüğü deseni.
2. Ders/konu kataloğunun öğrenci tarafıyla **ortak** kullanımı (mevcut Room tabloları).
3. Hafif öğretmen içgörüleri: bu hafta çözülen, ortalama bekleme süresi, en çok biriken konu. (Gamification yok.)
4. Tablet/yatay iki panel; isteğe bağlı PDF/çoklu paylaşım.
5. Uygulama kilidi (biyometri) ve yedek politikası kararı.

### Öncelik gerekçesi

S1 ve S2 **veri/güven** kategorisinde; projenin kendi önceliği "veri güvenliği → görsel işlemleri → günlük akış" (ROADMAP başı). S1 olmadan toplama, S4 olmadan kapatma, S5 olmadan öncelik yok. Bu yüzden T0 = S1+S2+S9+S6 temelleri; T1 = S4+S5+S8.

---

## 7. Kapsam dışı tutulmasını önerdiğim fikirler

Projenin "dışarıdan gelen öneriler gerekçeyle kabul/reddedilir" kuralı gereği:

| Fikir | Öneri | Gerekçe |
|---|---|---|
| Öğretmen için **bulut/çok cihaz senkron** | **Red** | Sunucusuz/hesapsız ilke; veri sahipliği ve maliyet |
| **Öğrenci hesabı / öğrenci cihazından soru gönderme** | **Red (v1)** | Çok kullanıcılı yapı; ayrı ürün. Paylaş menüsü aynı ihtiyacı karşılıyor |
| **OCR / AI ile çözüm üretme** | **Red** | K36 ve §7; yanlış cevap riski, maliyet, güven |
| Öğretmene **XP/rütbe/seri** | **Red** | Mevcut ayrım doğru; iş aracı oyunlaştırılmamalı |
| **PDF çalışma kâğıdı / yazdırma** | **Ertele** | T2; önce paylaş menüsü |
| WhatsApp'a doğrudan entegrasyon | **Red** | API yok/desteklenmez; `ACTION_SEND` yeterli |
| Otomatik yedekleme (bulut) | **Ertele** | Kapsam dışı; ama **yedek hatırlatması** ucuz ve yapılmalı |
| Öğretmen verisi için **şifreleme** | **Ertele, karar kaydı açık** | Anahtar kaybı = veri kaybı (A5) |
| PDF/rapor ile "sınıf karnesi" | **Ertele (T2)** | Önce kuyruk ve kapatma döngüsü |

---

## 8. Belgelerde düzeltilmesi gerekenler

| Belge | Sorun | Öneri |
|---|---|---|
| `PROJECT_BRIEF.md` §1, §7 | "öğretmen paneli yok" — mod mevcut | "panel (uzak/çok kullanıcı)" ile "aynı cihazda çalışma alanı" ayrımı; K38 kararı |
| `PROJECT_BRIEF.md` §5, A11 | Coil seçilmiş görünüyor, kullanılmıyor | Kararı gerçeğe göre güncelle veya Coil'i ekle |
| `ROADMAP.md` | Öğretmen maddesi yok | T0/T1/T2 maddeleri + fiziksel kabul satırları |
| `docs/VERI_MODELI.md` | `teacher_workspace` yok | JSON şeması + sürüm kuralı |
| `docs/YEDEK_FORMATI.md` | Öğretmen yükü tanımsız | `teacher_workspace`, `teacher_images`, sürüm alanı |
| `HANDOFF.md` | Öğretmen modu yalnız sürüm notlarında | Ayrı "Öğretmen modu durumu" bölümü ve açık cihaz testleri |
| `docs/TEST_KONTROL_LISTESI.md` | Öğretmen adımları yok | §9'daki liste |

---

## 9. Öğretmen modu test/kabul listesi (öneri)

**Otomatik (JVM/Robolectric)**
1. JSON round-trip: tüm alanlar, sıra, zaman damgaları.
2. `schema_version` yok → v1 kabul; v2 alanları yazılınca geri okunur.
3. Bozuk kayıt → diğer kayıtlar korunur, uyarı sayısı raporlanır.
4. Tamamen bozuk dosya → yazma kilidi, karantina dosyası, `.bak` korunur.
5. Bilinmeyen `difficulty` → kayıt atlanır, çalışma alanı yaşar.
6. Görsel referans sayımı: kopyala → sil → dosya **korunur**; son referans silinince dosya **gider**.
7. Yetim taraması: referanssız + eski dosya silinir; yeni dosya korunur.
8. Grup silme: soru sayısı, görsel temizliği, kısmi hata sonrası tutarlılık.
9. Mükerrer kopya engeli; taşıma sonrası `olusturma`/`cozuldu` korunur.
10. ZIP: paylaşılan görselli öğretmen alanı round-trip (mevcut test genişletilir).
11. ZIP: `teacher_workspace_version` yüksek → geri yükleme reddi.
12. Eşzamanlı `soruKaydet` + `cozulduDurumDegistir` (mutex).
13. 2.000 soru ile sıralama/filtre süresi (makul sınır).
14. Uzun cevap (2.000+ karakter) kaydı ve okunuşu.

**Compose (debug)**
15. Grup ekle → kart görünür, sayaçlar doğru.
16. Soru ekle (galeri) → `Çözmediklerim`'de.
17. `Çözdüm` → `Çözdüklerim`'e geçer, sayaç güncellenir.
18. Tam ekran aç/kapat, zoom sıfırla, cevap göster/gizle.
19. Büyük yazı (1.6x) + koyu tema: taşma yok, düğmeler erişilebilir.
20. Sunumda uzun cevap: butonlar hâlâ ekranda (S15).

**Cihaz (kullanıcı)**
21. Kamera: öğretmen formu + "Çözdürdüm".
22. Paylaş menüsünden içe alma (WhatsApp → uygulama).
23. Sunumda 30 sn dokunmadan ekran sönmüyor.
24. Süreç öldürme: form yarıda → staging temizleniyor, veri bozulmuyor.
25. Düşük depolama: kayıt reddi ve anlaşılır mesaj.
26. 3 sınıf × 100 soru ile kaydırma akıcılığı (frame-p95, gerçek cihaz).
27. Yedek al → temiz kurulum → geri yükle → öğretmen verisi + görseller tam.
28. TalkBack: sekmeler, kartlar, sunum düğmeleri.

---

## 10. Kısa vadede "yapılmaması gerekenler"

- **Öğretmen verisini Room'a taşımadan** ders/konu/çözüm/arşiv gibi alanları **tek seferde** eklemek: şema sürümü olmadığı için geri dönüş yolu kalmaz (S3). Önce sürüm + tolerans, sonra alanlar.
- **Sunum ekranını yeniden yazmak**: mevcut dialog çalışıyor; önce "sonraki soru + ekran açık + kaydırılabilir cevap" eklenmeli.
- **Öğretmen kartını daha da büyütmek**: 196 dp görsel zaten taramayı zorlaştırıyor; kompakt görünüm asıl ihtiyaç.
- **Yeni grafik/animasyon**: öğretmen modunda eksik olan süs değil, döngü.

---

## 11. Karar bekleyen açık sorular

1. **Odak kullanıcı kim?** Sınıf öğretmeni mi (kuyruk + toplu işlem + sunum), özel ders öğretmeni mi (öğrenci geçmişi + çözüm + paylaş), yoksa ikisi de mi? İlk sürümde baskın senaryo seçilmeli.
2. **Ders/konu etiketi** öğrenci tarafındaki 24 derslik katalogla mı, serbest metinle mi, yoksa yalnız "grup" ile mi kalacak?
3. **Çözüm teslimi**: tek soru paylaşımı yeterli mi, PDF/çoklu seçim gerekli mi? Paylaş menüsünden içe alma öncelikli mi?
4. **Room v5 / ZIP v5** şimdi mi, T1 sonunda mı? (Önerim: önce JSON v2 + tolerans, alanlar netleşince Room.)
5. **Öğretmen + öğrenci aynı cihazda** eşzamanlı kullanım senaryosu var mı? Rol düğmesi yeterli mi, "iki çalışma alanı" ayrımı mı istiyorsunuz?
6. **Vade/hatırlatıcı**: uygulama içi "gecikenler" yeterli mi, yoksa bildirim (Aşama 6) öğretmen için öne çekilsin mi?
7. **Gizlilik**: uygulama kilidi ve yedek şifreleme, öğretmen verisiyle birlikte yeniden değerlendirilsin mi? (A5/A3)

---

## 12. Özet cümle

Öğretmen modu iyi bir **depo**; ihtiyaç duyulan şey iyi bir **iş akışı**. Sıra şu olmalı: **kamerayı ve veriyi güvene al (S1, S2, S3) → kapatma döngüsünü kur (S4, S5) → sınıf içi sunumu tamamla (S6) → ölçek ve arşiv (S7, T2)**. Bu sırayla gidilirse, her adım tek başına kullanıcıya yarar sağlar ve hiçbiri geri alınamaz veri kaybı riski taşımaz.
