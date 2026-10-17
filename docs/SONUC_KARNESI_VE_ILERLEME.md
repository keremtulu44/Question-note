# Sonuç Karnesi, Kutlama ve İlerleme Sistemi — Tasarım Şartnamesi

> Durum: **tasarım onaylandı, kod henüz yazılmadı.** Bu belge v1.0.10 ve sonrasında yapılacak
> çalışmanın tek kaynağıdır. Kararlar kullanıcı tarafından 2026-10-17'de onaylandı; ilgili karar
> kayıtları `PROJECT_BRIEF.md` içindeki K25–K27'dir.
>
> Bu belgede ölçüm iddiası yoktur. Performans/animasyon maliyeti ancak cihazda ölçüldükten sonra
> yazılır (`docs/KODLAMA_PLANI.md` kural 8).
>
> Revizyon: 2026-10-17 — açık sorular kapandı (S7–S11); önceki hafta karşılaştırması, 5 soru eşiği,
> otomatik karne ve “Bu konudan çalış” davranışı eklendi. §11 karar kaydıdır.

---

## 1. Amaç

İki hedef var ve ayrı tutulur:

1. **Soru çözdürme mantığını geliştirmek:** bugün `ZORLANDIM` sonucu aralığı hiç büyütmüyor,
   unutma sayısı tutulmuyor ve gecikme hesaba katılmıyor. Bu üç açık kapatılacak (§7).
2. **Başarı ve ilerleme hissi kurmak:** oturum bitince “17 soru tamamlandı” yerine ne yaptığını
   anlatan bir **sonuç karnesi**, skora göre değişen **tebrik**, **animasyon** ve **rozet** (§3–§6).

Kapsam dışı: sosyal paylaşım, çok kullanıcı, sunucu, bildirim (ayrı değerlendirilecek).

---

## 2. Onaylanan kararlar (özet)

| # | Karar |
|---|---|
| S1 | **Sonuç → karne eşlemesi:** `Rahat` = bağımsız çözüm, `Zorlandım` = yarım (destekli), `Çözemedim` = çözemedi. |
| S2 | **Çözüm açıldıysa** hangi düğmeye basılırsa basılsın soru **bağımsız sayılmaz**; ayrı sayılır. |
| S3 | Karne, “20 sorudan 17'sini bağımsız çözdün” biçiminde **sayı + oran** gösterir. |
| S4 | **En çok zorlanılan yanlış nedeni** ve **en çok zorlanılan ders/konu** karnede listelenir. |
| S5 | Kutlama **animasyonlu ve rozetli**, skora göre değişen, zevkli ama abartısız olacak. |
| S6 | **Günlük hedef şimdilik sabit 10.** İlk kurulumda seçim ve Ayarlar'dan değiştirme **ileri sürüme** bırakıldı; şimdi yalnızca sabit değer ve okuma arayüzü yazılacak. |
| S7 | **Rozet adları onaylandı** (§5.2'deki ton kullanılacak). |
| S8 | **Karşılaştırma penceresi: önceki takvim haftası** (Pazartesi–Pazar). Oturum sonucu, içinde bulunulan haftanın oranıyla birlikte önceki haftaya kıyaslanır. Ayrıntı §4.5. |
| S9 | **“Bu konudan çalış”** düğmesi filtreyi otomatik kurup Filtreli çalışma ekranına götürür. Ayrıntı §4.6. |
| S10 | **Kısa oturum eşiği şimdilik 5**; ileride değişken (ayar/tercih) olacak. Kodda tek sabit (`KISA_OTURUM_ESIGI`) ve tek okuma noktası; sihirli sayı kullanılmaz. |
| S11 | **Karne otomatik açılır:** oturumun son sorusu kaydedilince doğrudan karne gösterilir; “Karnemi gör” ara adımı yok. |

---

## 3. Bağımsız çözüm tanımı (tek doğruluk kaynağı)

Karne, “doğru/yanlış” değil **bağımsızlık** ölçer. Soru bazında tek kural:

```
çözümAçıldı = true                → "çözümle"        (bağımsız değil)
çözümAçıldı = false:
    sonuç = RAHAT                 → "bağımsız"
    sonuç = ZORLANDIM             → "yarım"
    sonuç = COZEMEDIM             → "çözemedi"
```

Kurallar:

- Toplam = bağımsız + yarım + çözemedi + çözümle. Dört kova **birbirini dışlar** (öncelik:
  `cozum_acildi` her şeyden önce gelir).
- **Bağımsız oran** = bağımsız / toplam. Karnenin ana sayısı budur.
- “Yarım” kovada gösterilir ama bağımsız orana **dahil edilmez** (K9 ve K19'un devamı).
- Bu tanım `algoritma_surumu`'ndan bağımsızdır; v1 ve v2 kayıtlarında aynı şekilde okunur.

---

## 4. Sonuç karnesi ekranı

### 4.1 Yerleşim (yukarıdan aşağı)

```
┌──────────────────────────────────────────┐
│  ← Filtreli çalışma          (kapat)     │
│                                          │
│        🎯  20 sorudan 17'sini            │
│            bağımsız çözdün               │
│                                          │
│         ◜◝  %85                          │
│        (  ◯  )  hedefin %80              │
│         ◟◞                              │
│                                          │
│   ✅ 17 bağımsız   ⚡ 2 zorlanarak        │
│   ❌ 1 çözemedin    🔓 3 çözümü açtın     │
│                                          │
│   En çok zorlandığın neden               │
│   ███████  Dikkat / acele        4       │
│   ████     Bilgi eksiği          2       │
│                                          │
│   En çok zorlandığın konu                │
│   Fizik · Kuvvet                 3       │
│   [Bu konudan çalış]                     │
│                                          │
│   📈 Bu hafta %85 · geçen hafta %76      │
│      (+9 puan)                           │
│   🔥 6 günlük seri  ·  5/10 hedef        │
│   📅 Yarın 8 soru geliyor                │
│   [Yarınkileri gör]                      │
│                                          │
│   🏅 Yeni rozet: Haftalık seri           │
│                                          │
│   [Filtrelere dön]      [Bugün]          │
└──────────────────────────────────────────┘
```

### 4.2 Bölüm kuralları

| Bölüm | Kural | Veri kaynağı |
|---|---|---|
| Başlık | `toplam` ve `bağımsız` her zaman yazıyla; sayılar 0'dan yukarı sayarak gelir | oturum listesi (bellek) |
| Oran halkası | Bağımsız oran; hedef varsa ince bir hedef çizgisi | hesap |
| Dört kova | Toplam < `KISA_OTURUM_ESIGI` (şimdilik 5) ise yalnız `bağımsız / çözemedi` gösterilir; sadeleşir | hesap |
| Neden listesi | En çok 3 satır, **en az 2 kez** geçen nedenler; hiçbiri yoksa bölüm gizlenir | `sorular.yanlis_nedeni` |
| Konu listesi | En çok 3 satır, **en az 2 kez**; konusuz sorular “Konusuz” olarak gruplanır | `sorular.ders_id/konu_id` |
| Karşılaştırma | **Önceki takvim haftası** ≥ `KISA_OTURUM_ESIGI` tekrar içeriyorsa gösterilir; yoksa gizlenir (§4.5) | `tekrarlar` (haftalık) |
| Seri / hedef | Her zaman görünür | `tekrarlar.gun` + hedef |
| Tahmin | `sonraki_tekrar_gun = yarın` sayısı; 0 ise “Yarın planlı soru yok” | `sorular` |
| Rozet | Yalnız **bu oturumda yeni kazanılan** rozet; yoksa bölüm gizlenir | rozet deposu |
| Düğmeler | “Filtrelere dön” (filtre ekranı), “Bugün”; konu satırında “Bu konudan çalış” (§4.6) | — |

### 4.3 Tebrik cümlesi (bağımsız orana göre)

| Oran | Cümle |
|---|---|
| %90–100 | “Neredeyse tamamı senden. Bu konular kapanıyor.” |
| %70–89 | “Sağlam bir tur. Kalan {n} soru yarın tekrar gelecek.” |
| %50–69 | “Yarısı oturdu. {konu} için kısa bir tur iyi gelir.” |
| %0–49 | “Bugün zor bir gündü. Yarın 1'er soruyla başlayalım.” |
| toplam < 5 (`KISA_OTURUM_ESIGI`) | “Kısa bir ısınma turu.” (karşılaştırma ve tebrik atlanır; kova/kova sayıları korunur) |

Kurallar: **olumsuz/cezalandırıcı dil yok**, “başarısız” kelimesi kullanılmaz, ünlem işareti en çok
bir kez, kullanıcıya emir kipi yok (“çalışmalısın” değil “iyi gelir”).

### 4.4 Oturum verisi nereden gelir

Karne **oturum bitişinde, bellekteki listeden** hesaplanır:

- Oturum başında soru kimlikleri alınır; her sonuç kaydedildiğinde `sonuç + çözümAçıldı` belleğe
  yazılır (`SoruDetayEkrani` → `onTekrarTamamlandi` zaten bu anda çağrılıyor).
- Yeniden hesaplama **gerekmez**: aynı gün ikinci kayıt `AyniGunTekrarVar` ile engellendiği için
  `tekrarlar` tablosundan da aynı sonuç çıkar.
- Uygulama oturum ortasında kapanırsa **karne gösterilmez**; kaydedilen tekrarlar kaybolmaz.
  Bu bilinçli kabuldür (geçmiş karneler ileri sürümde `tekrarlar` üzerinden üretilebilir).

---


### 4.5 Haftalık karşılaştırma (S8)

**Pencere tanımı:** hafta **Pazartesi 00:00 – Pazar 23:59** (cihazın yerel takvimi).
`tekrarlar.gun` zaten yerel takvim günü (epoch gün) olduğu için ek dönüşüm gerekmez.

| Kavram | Hesap |
|---|---|
| Bu hafta | Bu haftanın Pazartesi'sinden **bugüne kadar** (bugün dâhil) tüm tekrarlar |
| Geçen hafta | Bir önceki Pazartesi–Pazar aralığındaki tüm tekrarlar |
| Oran | Aynı bağımsızlık tanımı (§3): `sonuc != COZEMEDIM AND cozum_acildi = 0` |

**Gösterim:** `Bu hafta %85 · geçen hafta %76 (+9 puan)`. Fark 0 ise “(aynı)”, negatifse “(−4 puan)”
biçiminde yazılır; eksi işareti kırmızıya boyanmaz (cezalandırıcı görünüm yok, K26).

**Gizleme kuralı:** geçen haftada `KISA_OTURUM_ESIGI`'den az tekrar varsa satır gizlenir; yerine
“Karşılaştırma için geçen hafta yeterli kayıt yok.” tek satırı gösterilir (ilk kurulumda yanıltıcı
“+0 puan” görünmesin).

**Sınır durumlar:**

- Hafta sınırı cihaz saatine göredir; saat dilimi değişirse geçmiş `gun` değerleri değişmez
  (VERI_MODELI §1 kuralı).
- Yılbaşı/hafta numarası hesabı yapılmaz; yalnız tarih aritmetiği (`LocalDate.minusWeeks(1)`) kullanılır.
- Bu hafta hiç tekrar yoksa oran yerine “—” gösterilir (0'a bölme yok).

### 4.6 “Bu konudan çalış” davranışı (S9)

Konu satırındaki düğme şunları yapar:

1. `SoruCalismaFiltresi(konuId = <seçilen konu>)` kurulur; **diğer filtreler temizlenir**
   (kaynak/neden/konusuz seçimleri sıfırlanır).
2. Soru sayısı alanı varsayılana döner (boş = tümü).
3. `UygulamaEkrani.FiltreliCalisma` açılır; karne ekranı geri yığından çıkar (kullanıcı “geri”
   dediğinde karneye değil filtre ekranına döner).
4. Konu **arşivliyse** veya ders arşivliyse bu yol gösterilmez; düğme yerine kısa bilgi yazılır.
   (Karne yalnız etkin derslerden soru çalıştığı için pratikte oluşmaz; savunma amaçlı kontrol.)
5. Konusuz sorular için düğme gizlenir (filtrelenecek konu yok).

Not: Bu davranış yeni bir ekran veya sorgu gerektirmez; mevcut `onOturumBaslat`/filtre durumu
kullanılır.

## 5. Kutlama: animasyon ve rozet

### 5.1 Animasyon kuralları

| An | Animasyon | Süre |
|---|---|---|
| Ekran açılışı | Başlık sayıları 0→n sayar; halka %0→oran dolar | 600–700 ms |
| Bağımsız oran ≥ %70 | **Konfeti patlaması** (Compose Canvas parçacıkları) | 1.2 s, tek sefer |
| Bağımsız oran < %70 | Yumuşak parıltı; **asla** üzgün/kırılgan animasyon yok | 600 ms |
| Kova sayıları | Sırayla belirme (stagger 80 ms) | 3×200 ms |
| Yeni rozet | Rozet 0.6 → 1.1 → 1.0 ölçek + ışık halkası; hafif titreşim (`LocalHapticFeedback`) | 400 ms |
| Sayfa geçişi | Halka ve başlık aşağıdan yukarı 12 dp | 300 ms |

Kısıtlar:

- **Yeni bağımlılık yok.** Konfeti ve parıltı Compose `Canvas` ile yazılır (Lottie/görsel dosya yok).
- **Hareket azaltma:** `Settings.Global.ANIMATOR_DURATION_SCALE == 0` ise tüm animasyonlar atlanır,
  içerik doğrudan son hâliyle görünür. Erişilebilirlik gereği zorunlu.
- Konfeti oturum başına **en çok bir kez**; tekrar gösterime girmez (ekran yeniden çizilse bile).
- Animasyon hiçbir veriyi değiştirmez; iptal edilirse sonuç aynı kalır.

### 5.2 Rozetler

Rozetler **cihaz-yerel**dir: `SharedPreferences` içinde `stringSet` olarak tutulur, ZIP yedeğine
girmez (düşük değer, yeniden kazanılabilir; yedek biçimini büyütmemek için). Kilit açma **idempotent**:
zaten varsa animasyon gösterilmez.

| Kod | Ad | Koşul | Faz |
|---|---|---|---|
| `ILK_KARNE` | İlk adım | İlk tamamlanan oturum | A |
| `KUSURSUZ_TUR` | Kusursuz tur | Bir oturumda %100 bağımsız ve ≥5 soru | A |
| `ON_BAGIMSIZ` | Onluk | Tek oturumda ≥10 bağımsız | A |
| `YIRMI_SORU` | Yirmilik | Tek oturumda ≥20 soru | A |
| `COZUMSUZ` | Kendi başına | ≥5 soruluk oturumda hiç çözüm açmamak | A |
| `ISTIKRAR` | İstikrar | Üst üste 5 oturumda ≥%70 bağımsız | A |
| `SERI_3` | Üç gün | Üst üste 3 gün en az 1 tekrar | A |
| `SERI_7` | Haftalık seri | Üst üste 7 gün | A |
| `SERI_30` | Otuz gün | Üst üste 30 gün | A |
| `YUZ_BAGIMSIZ` | Yüz | Toplam 100 bağımsız çözüm | A |
| `BES_YUZ` | Beş yüz | Toplam 500 bağımsız çözüm | A |
| `TOPARLANMA` | Toparlanma | `COZEMEDIM` sonrası soru `SAGLAM`a ulaştı | C |
| `ZOR_SORU_COZULDU` | İnatçıyı yendin | Zor soru (leech) etiketli soru bağımsız çözüldü | C |

`SERI_*`, `YUZ_BAGIMSIZ` ve `BES_YUZ` toplam sayaç gerektirir; karne ekranı bunları
`tekrarlar` tablosundan **anlık** hesaplar (ayrı tablo/kolon yok, migration yok). Sayaçlar her karne
açılışında yeniden hesaplandığı için tutarsızlık riski yoktur.

### 5.3 Rozet gösterimi

- Karne ekranında **yalnız yeni kazanılan** rozet(ler) gösterilir (en çok 2).
- Kalıcı “Rozetler” ekranı ileri sürümde; şimdilik karne altında “Kazandığın rozetler: 5/11” satırı.

---

## 6. Günlük hedef, seri ve haftalık özet

### 6.1 Günlük hedef — **şimdilik sabit 10**

| Konu | Karar |
|---|---|
| Şimdiki değer | `GUNLUK_HEDEF = 10`, kodda sabit |
| Arayüzden değiştirme | **Yok** (v1.0.10'da). Ayarlar ekranı ileri sürümde |
| İlk kurulumda seçim | **Yok** (v1.0.10'da). Onay ekranı ileri sürümde: 5 / 10 / 20 / 30 |
| Okuma yolu | `GunlukHedefDeposu` arayüzü + `SabitGunlukHedefDeposu(10)`; ileride `TercihGunlukHedefDeposu` takılır, çağrı yerleri değişmez |

Hedef sayacı **o gün en az bir kez çözülen soru** sayısıdır (herhangi bir sonuç). Bağımsız oran ayrı
gösterilir; ikisi karıştırılmaz. Sayaç `tekrarlar` tablosundaki `gun = bugün` kayıtlarından gelir.

Gösterim: Bugün ekranının üstünde ince halka `5/10`, altında seri. Hedef tamamlanınca halka dolar ve
tek seferlik küçük bir parıltı oynar (karne animasyonundan bağımsız, daha sakin).

### 6.2 Seri (streak)

- **Tanım:** üst üste, her gün en az 1 tekrar kaydı olan gün sayısı. Bugün henüz çalışılmadıysa seri
  **bozulmaz**; dün biten seri gösterilir (“dün 6 gün”).
- Kaynak: `tekrarlar.gun` üzerinden `SELECT DISTINCT gun ORDER BY gun DESC` — yeni kolon yok.
- **Mola günü hakkı** (1 gün atlama tolere eden jeton) ileri sürümde; `SharedPreferences` gerektirir,
  şimdilik yok. Karar kaydı K27'de not edildi.

### 6.3 Haftalık özet

Karne ekranının altında veya ayrı “İlerleme” sekmesinde son 7 gün:

| Gösterge | Hesap |
|---|---|
| Günlük çubuklar | `gun` başına kayıt sayısı (0 olan gün boş çubuk) |
| Bağımsız oranı | `sonuc != COZEMEDIM AND cozum_acildi = 0` |
| En çok zorlanılan neden | `yanlis_nedeni` + `sonuc != RAHAT` grubu |
| Toplam süre | **yok** (ölçüm eklenmedi; eklenirse §9'daki süre notu) |

Grafik: basit `Canvas` çubuklar, kütüphane yok. Veri yoksa (“henüz yeterli kayıt yok”) boş durum.

### 6.4 Pekişme halkaları (faz D)

Ders kartında `ogrenme_durumu` dağılımı: `SAGLAM / PEKISIYOR / OGRENIYOR / YENI` yüzdeleri.
Zaten hesaplanan alandan okunur, migration gerekmez. Kart üzerinde ince halka + dokununca açıklama.

### 6.5 7 günlük tahmin (faz D)

`sonraki_tekrar_gun` bugünden +1…+7 aralığında gruplanır: “Yarın 8 · Cuma 12 · …”. Amaç, yükün
patladığı günü önceden görmek. Günlük kota (faz D) bu tahminle birlikte anlam kazanır.

---

## 7. Soru çözdürme mantığı v2

### 7.1 Bugünkü açıklar

| # | Açık | Sonuç |
|---|---|---|
| 1 | `ZORLANDIM` aralığı hiç büyütmüyor | 1 gündeki soru sonsuza kadar 1 günde kalır, ilerleme hissi yok |
| 2 | Unutma sayısı tutulmuyor | 90→1 düşen soru ile ilk kez yanlış yapılan soru aynı muameleyi görür |
| 3 | `planlanan_gun` var ama gecikme cezalandırılmıyor | 40 gün gecikmiş soruya “rahat” demek, zamanında çözmekle aynı ödülü alır |

### 7.2 Yeni durum (soru başına)

| Alan | Tip | Aralık | Varsayılan |
|---|---|---|---|
| `aralik_gun` | INTEGER | 1–180 | 1 |
| `kolaylik_yuz` | INTEGER (×100) | 130–280 | 250 |
| `unutma_sayisi` | INTEGER | ≥0 | 0 |

`kolaylik` ondalık yerine **100 ile çarpılmış tam sayı** olarak tutulur: yedek/CSV/JSON turunda
kayan nokta kayması olmaz, testler birebir karşılaştırılabilir.

### 7.3 Formül

```
önceki = aralik_gun  (soru ilk kez çözülüyorsa 1)

eğer cozum_acildi veya sonuç == COZEMEDIM:
    yeni = 1
aksi halde eğer sonuç == ZORLANDIM:
    yeni = max(önceki + 1, round(önceki * 1,2))
aksi halde:                                   # RAHAT
    yeni = round(önceki * kolaylik_yuz / 100)

gecikme = bugun - planlanan_gun               # < 0 ise 0
eğer gecikme > 2 * önceki:
    yeni = round(yeni * 0,8)                  # şans eseri hatırlama

yeni = clamp(yeni, 1, 180)
```

Not: gecikme cezası sonrası aralık, önceki aralıktan **küçük** çıkabilir (ör. 21 → 20). Bu bilinçlidir:
uzun aradan sonra hatırlama zayıf sayılır. Alt sınır her zaman 1'dir.

Kolaylık güncellemesi (aynı işlemde):

| Sonuç | Δ kolaylık |
|---|---|
| RAHAT | +5 (`kolaylik_yuz += 5`) |
| ZORLANDIM | −15 |
| COZEMEDIM | −20, `unutma_sayisi += 1` |
| Çözüm açıldı | 0 (yargıya varmıyoruz) |

Sınırlar `130…280` arasında kırpılır.

### 7.4 Örnekler

| Önceki | Sonuç | Kolaylık | Yeni | Not |
|---|---|---|---|---|
| 1 | RAHAT | 250 | 3 | v1 merdiveniyle aynı |
| 1 | ZORLANDIM | 250 | **2** | **eski davranışta 1'de kalıyordu** |
| 7 | RAHAT | 250 | 18 | v1'de 21 idi; kademeler yakın seyreder |
| 21 | ZORLANDIM | 235 | 25 | artık ilerliyor |
| 45 | COZEMEDIM | 230 | 1 | öğrenme moduna dönüş + unutma +1 |
| 21 | RAHAT, 50 gün gecikme | 255 | 43 | 21×2.55=54; gecikme 50 > 2×21=42 → 54×0.8≈43 |

Bu tablo T-7 testinin birebir beklentisidir; formülde değişiklik yapılırsa tablo ve test birlikte
güncellenir.

### 7.5 Zor soru (leech)

- `unutma_sayisi >= 4` ise soru **zor soru** etiketlidir (türetilmiş, ayrı kolon gerekmez).
- Görünüm: soru detayında kısa uyarı + Bugün listesinde “Zorlanılanlar” grubu (faz C).
- Eylem önerisi: konuya git, soruyu `ASKIDA` yap veya küçük parçalara ayır. Otomatik işlem **yok**.

### 7.6 Sürümleme ve geriye uyumluluk

- `TekrarKurali.ALGORITMA_SURUMU = 2`; eski kayıtlar `algoritma_surumu = 1` olarak kalır ve
  yorumlanırken v1 kuralı kullanılır (geçmiş yeniden yazılmaz — K18).
- Yeni alanlar **yalnız** yeni kayıtlarda güncellenir; v1 kaydı olan sorular `aralik_gun` ve
  `unutma_sayisi` backfill ile doldurulur, `kolaylik_yuz` 250 kalır.

### 7.7 Room v3 → v4 migration (veri koruyan, fallback yok)

```sql
ALTER TABLE sorular ADD COLUMN aralik_gun INTEGER NOT NULL DEFAULT 1;
ALTER TABLE sorular ADD COLUMN kolaylik_yuz INTEGER NOT NULL DEFAULT 250;
ALTER TABLE sorular ADD COLUMN unutma_sayisi INTEGER NOT NULL DEFAULT 0;

UPDATE sorular SET aralik_gun = COALESCE((
    SELECT t.sonraki_aralik FROM tekrarlar t
    WHERE t.soru_id = sorular.id AND t.sonraki_aralik IS NOT NULL
    ORDER BY t.gun DESC, t.zaman DESC LIMIT 1), 1);

UPDATE sorular SET unutma_sayisi = (
    SELECT COUNT(*) FROM tekrarlar t
    WHERE t.soru_id = sorular.id AND t.sonuc = 'COZEMEDIM');
```

Kurallar: şema JSON'u `app/schemas/.../4.json` olarak CI'da üretilir; v3→v4 testi hem şemayı hem
**veri korunduğunu** doğrular; `migrate` parametre adı uyarısı da bu sırada düzeltilir.

### 7.8 Yedek biçimi v4

| Değişiklik | Kural |
|---|---|
| `format_version` | 4; `schema_version` 4 |
| Kabul edilen | **v4, v3, v2** (v1 reddedilir — mevcut davranış) |
| `data.json` soru alanları | `interval_days`, `ease_hundredths`, `lapse_count` eklenir |
| `csv/questions.csv` | Aynı üç kolon **sona** eklenir (v3 sütun sırası bozulmaz) |
| v3 yedeği geri yüklenirse | Yeni alanlar varsayılanla doldurulur (1 / 250 / 0) + §7.7 backfill |
| Doğrulama | `1 ≤ interval_days ≤ 180`, `130 ≤ ease_hundredths ≤ 280`, `lapse_count ≥ 0` |

`docs/YEDEK_FORMATI.md` bu bölümle birlikte güncellenecek; **kod ile belge aynı sürümde** yazılır.

---

## 8. Faz planı

| Sürüm | İçerik | Migration | Kabul ölçütü |
|---|---|---|---|
| **v1.0.10** | Karne ekranı otomatik açılır (§4, S11), animasyonlar (§5.1), rozetler (§5.2, A fazı 11 rozet), günlük hedef sabit 10 + seri (§6.1–6.2), haftalık özet ve önceki hafta karşılaştırması (§4.5, §6.3) | **Yok** | §9'daki T-1…T-6, T-12, T-13 + cihazda oturum sonu karne görüntüsü |
| **v1.0.11** | Algoritma v2 (§7), zor soru etiketi | **Var (v4)** + yedek v4 | Migration/backfill testi + 50 gün gecikme örneği + v3 yedek içe alma |
| **v1.0.12** | Pekişme halkaları, 7 günlük tahmin, günlük kota (§6.4–6.5) | Yok | Cihazda ders kartı ve tahmin ekranı |
| **Sonra** | Onboarding'de hedef seçimi + Ayarlar, mola günü, geçmiş karneler | Prefs | Kullanıcı onayı |

Her fazda: `docs/TEST_KONTROL_LISTESI.md`'ye satır eklenir, cihaz adımları **boş** bırakılır;
CI yeşil olmadan sürüm numarası artırılmaz.

---

## 9. Test planı (v1.0.10 ve v1.0.11)

Saf fonksiyonlar ayrı sınıflarda tutulur ki JVM testi hızlı olsun (Compose'a bağımlı olmasın).

| # | Test | Faz |
|---|---|---|
| T-1 | Dört kova birbirini dışlar; toplam = bağımsız+yarım+çözemedi+çözümle | A |
| T-2 | `cozum_acildi = true` iken `RAHAT` seçilse bile **bağımsız sayılmaz** | A |
| T-3 | Toplam 0 iken oran hesabı çökmez (oturum boş olamaz, savunma amaçlı) | A |
| T-4 | Tebrik eşikleri sınırlarda doğru cümleyi seçer (%49/50, %69/70, %89/90) | A |
| T-5 | Rozet kilidi idempotent: aynı koşul ikinci kez animasyon üretmez | A |
| T-6 | Seri: boşluk, tek gün, bugün henüz çözülmemiş, 30 gün kesintisiz | A |
| T-7 | v2 formülü: §7.4 tablosunun **her satırı** (gecikme satırı dâhil) | C |
| T-8 | Kolaylık 130/280 sınırlarında kırpılır | C |
| T-9 | v3→v4 migration şemayı ve satırları korur; backfill doğru | C |
| T-10 | v3 yedeği v4'e geri yüklenir; yeni alanlar varsayılanla dolar | C |
| T-11 | v4 yedeği gidiş-dönüş (export → import) alanları birebir korur | C |
| T-12 | Hafta penceresi Pazartesi'den başlar; sınır günlerinde (Pazar/Pazartesi) geçen hafta doğru hesaplanır; geçen hafta < 5 tekrarda karşılaştırma gizlenir | A |
| T-13 | `KISA_OTURUM_ESIGI` tek kaynaktan okunur; 4 soruluk oturumda sadeleşmiş karne, 5'te tam karne üretir | A |

Cihaz (KULLANICI) satırları: karne görünümü, animasyon akıcılığı, rozet bildirimi, seri sayacı.

---

## 10. Riskler ve bilinçli sınırlar

| Risk | Karar |
|---|---|
| Karne yalnız oturum belleğinden gelir; uygulama kapanırsa gösterilemez | Kabul. Tekrar kayıtları kaybolmaz; geçmiş karne ileri sürüm |
| Rozetler yedeğe girmez | Kabul. Cihaz değişince yeniden kazanılır; yedek biçimi büyümez |
| Konfeti düşük donanımda takılabilir | Parçacık sayısı sabit ve küçük; `ANIMATOR_DURATION_SCALE = 0` ile tamamen kapanır |
| “Yarım” kovası kullanıcıya karışık gelebilir | Karne altında tek satır açıklama: “Zorlanarak = yardımla/uzun sürede çözdün” |
| v2'de aralıklar v1 merdiveninden sapar | Beklenen davranış; sapma ±%15 içinde kalacak şekilde testle sabitlenir |
| Kolaylık backfill edilmez | Kabul: eski sorular 2.50 ile başlar; unutma sayısı geçmişten hesaplandığı için zor soru doğru çalışır |

### Ölçülmemiş varsayımlar

- Animasyonların cihazda akıcı olduğu **henüz ölçülmedi**.
- Karne hesaplamasının oturum sonunda gözle görülür gecikme yaratmadığı **henüz ölçülmedi**.

Bu iki madde cihaz kabulünde not edilmeden “hızlı/akıcı” ifadesi kullanılmayacak.

---

## 11. Karar kaydı

### 11.1 Kapatılan sorular (2026-10-17)

| Soru | Karar | Nerede |
|---|---|---|
| Rozet tonu | Onaylandı; §5.2'deki adlar kullanılacak | S7 |
| Karşılaştırma penceresi | **Önceki takvim haftası** (Pazartesi–Pazar) | S8, §4.5 |
| “Bu konudan çalış” | Evet; filtreyi kurup Filtreli çalışmaya götürür | S9, §4.6 |
| Kısa oturum eşiği | **Şimdilik 5**, ileride değişken; tek sabit `KISA_OTURUM_ESIGI` | S10 |
| Karne ne zaman açılır | Oturum bitince **otomatik** | S11 |

### 11.2 Hâlâ açık (v1.0.10'u engellemez)

1. **Mola günü jetonu** (seriyi 1 gün koruma): ileri sürüm; `SharedPreferences` gerektirir.
2. **Geçmiş karneler**: şimdilik saklanmıyor; ileride `tekrarlar` üzerinden üretilebilir.
3. **Günlük hedefin değiştirilebilir olması**: onboarding + Ayarlar ile birlikte yapılacak.
4. Rozetlerin ayrı “Rozetler” ekranında gösterimi.
