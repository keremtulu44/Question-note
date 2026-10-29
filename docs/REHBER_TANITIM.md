# Uygulama içi rehber (tutorial) — plan ve sözleşme

**Tarih:** 2026-10-29 · **Dal:** `arena/ce557218-question-note`
**Amaç:** Uygulamayı ilk kez açan (veya unutan) öğrenciye 2–3 dakikada
"bu uygulama ne yapar, bir soru nasıl kaydedilir, bugün ne çalışırım, deneme nasıl takip edilir,
çözüm nasıl kullanılır, verim nasıl yedeklenir" sorularını **deneyerek** öğretmek.

---

## 1. Tarama: rehber nereye bağlanacak?

| Yer | Bugünkü durum | Rehber için karar |
|---|---|---|
| `YanlisDefteriUygulamasi` | Ekranlar `UygulamaEkrani` sealed arayüzü + JSON saver | Yeni `UygulamaEkrani.Rehber`, saver'a `"rehber"`, `geriHedefi = Ayarlar`, alt menü/üst panel gizli |
| İlk kurulum (`DersKurulumEkrani`) | `onTamam = { kurulumGerekli = false }` | Kurulum bitince rehber **bir kez** otomatik açılır (`RehberDeposu.gorulduMu()` false ise) |
| Ayarlar | Dersler / hedef / ses / yedek / katalog / hakkında | "Yardım" bölümü + **"Uygulamayı tanı: rehber"** satırı (her zaman açılabilir) |
| `AyarlarEkrani` imzası | Callback listesi | `onRehber: () -> Unit = {}` eklenir (varsayılanlı, mevcut çağrılar bozulmaz) |
| `AppContainer` | Tüm depolar burada | `rehberDeposu` eklenir; `MainActivity` üzerinden host'a geçer |
| Metinler | 1.208 kaynak | Yeni metinler `strings.xml`'e, kod içine gömülü metin yok |
| Testler | `app/src/test` (saf JVM) + `app/src/testDebug` (Robolectric Compose) | Saf model testi + Compose akış testi + depo testi |

## 2. Tasarım ilkeleri

1. **Dürüstlük:** her sayı gerçek motordan gelir (`NetKurali`, `TekrarKurali`, `DenemeOzeti`),
   demo verisi **DEMO** etiketlidir ve "defterine yazılmaz" denir.
2. **Veri kirletmez:** rehber hiçbir şeyi Room'a yazmaz; demo içerik bellekte tutulur.
   Kullanıcı isterse bölüm sonundaki gerçek eylemlere ("İlk sorumu ekle", "Yedek al") geçer.
3. **Premium his:** `GradyanKart` kahraman, Manrope tipografi, `InceCizimler` çizimleri,
   tek bakır CTA; sonsuz animasyon yok, `HareketPolitikasi` "hareketi azalt" tüm geçişleri kapatır.
4. **Erişilebilirlik:** her bölüm tek başlık (`heading`), ilerleme göstergesi metinle
   ("3 / 7"), kaydırıcı/eğim gibi jestler klavye-TalkBack ile de kullanılabilir
   (deneme artır/azalt düğmeleri, kalınlık kaydırıcısı).
5. **Atlanabilir:** "Atla" her bölümde görünür; rehber yarıda bırakılırsa kaldığı bölüm
   saklanır ve bir sonraki açılışta oradan devam eder.
6. **Uzun ama sıkıcı değil:** 7 bölüm; her bölümde en fazla 3 kısa paragraf + 1 etkileşim.

## 3. Bölümler

| # | Bölüm | Anlatılan | Etkileşim (gerçek motor) |
|---|---|---|---|
| 1 | **Tanıtım** | Yanlış defteri mantığı, çözümün ayrı ve gizli olması, çevrimdışı | Kahraman kart + 3 ilke; "Başla" |
| 2 | **Soru kaydet** | Ders → fotoğraf (kamera/galeri + kırpma) → çözüm ayrı → ilk tekrar yarın | "Bugün nasıldı?" 3 seçenek → `TekrarKurali.hesapla` ile **gerçek** sonraki tarih |
| 3 | **Bugün** | Tek karar: bugün ne çalışacağım; tur boyutu, gecikme, kuyruk | DEMO kuyruk (3 satır) + hedef halkası + "sırada N" |
| 4 | **Deneme** | Net takibi, ders skorları, "deftere bağla" | D/Y/B artır-azalt + kural seçimi → **canlı net**; 2 örnek denemeyle mini çizgi + ortalama/en iyi |
| 5 | **Çözüm** | Çözüm sorudan ayrı; tahta geçici; bağımsız çözme tanımı | Mini beyaz tahta: parmakla çiz, 1–10 kalınlık, temizle (`TahtaKalinlik` sözleşmesi) |
| 6 | **Yedek** | ZIP nedir, neyi taşır/taşımaz, 3 adım, geri yükleme önizlemesi | "Ayarlar'da yedek al" (gerçek akışa geçiş); yedek içeriği listesi |
| 7 | **Kapanış / Kontrol sende** | Veri cihazda, izin yok (kamera FileProvider), kapsam dışı (bulut/sosyal) | "İlk sorumu ekle" + "Bugüne dön" |

## 4. Teknik yapı

```
ui/rehber/
  RehberModeli.kt        → bölüm listesi, demo veri, saf hesap sarmalayıcıları (Compose yok)
  RehberCizimleri.kt     → Canvas görseller (defter, takvim yolu, ZIP kutusu, net çizgisi)
  RehberBolumleri.kt     → 7 bölümün içeriği + etkileşimler (mini tahta dahil)
  RehberEkrani.kt        → HorizontalPager + ilerleme + Alt/Yukarı kontrolleri + bitirme
data/prefs/RehberDeposu.kt → goruldu / sonBolum (SharedPreferences)
```

- Bölüm kimliği tek kaynaktan (`RehberBolumu` enum) üretilir; başlık metinleri `strings.xml`.
- Pager `rememberPagerState(pageCount)`; `LaunchedEffect(sonBolum)` ile kaldığı yere döner.
- Host'a geçen geri çağrılar: `onBitir`, `onSoruEkle`, `onDenemeler`, `onYedek` (Ayarlar),
  `onKapat`. Rehber kendi başına veri yazmaz.

## 5. Test planı

| Test | Tür | Kapsam |
|---|---|---|
| `RehberModeliTest` | saf JVM | bölüm sayısı/sırası, demo veri tutarlılığı, tekrar önizlemesi (Rahat→ileri aralık, Çözemedim→1 gün), net hesabı, "hangi içerik yedeğe girer" listesi |
| `RehberDeposuTest` | Robolectric | goruldu/sonBolum kalıcılığı, tamamla→baştan, sıfırlama |
| `RehberEkraniTest` | Robolectric + Compose | ilk bölüm, "İleri/Geri", "Atla"nın bitirmesi, deneme netinin artır-azalt ile değişmesi, tekrar seçeneklerinin tarih metnini değiştirmesi, mini tahtada dokunuşun iz bırakması |
| `AyarlarEkrani` mevcut testleri | mevcut | yeni satır var olanları bozmamalı (imza varsayılanlı) |

## 6. Kapsam dışı (bilinçli)

- Rehber içinden gerçek soru kaydı (kamera akışı) — bölüm sonunda gerçek ekrana yönlendirme var.
- Rehber içinde veri silme/yedek oluşturma — yedek gerçek `Ayarlar` akışında yapılır (SAF izinleri).
- Öğretmen alanı: `AGENTS.md` gereği dokunulmaz.
- Rehber ilerlemesi ZIP yedeğine **girmez** (cihaz tercihi, veri değil).

## 7. Uygulama sonucu (2026-10-29)

Plan uygulandı; aşağıdaki farklar bilinçlidir.

| Konu | Plandan fark / netleşen karar |
|---|---|
| Bölüm içeriği | 7 bölüm aynı sırayla. Her bölüm en fazla 3 kısa metin bloğu + tek etkileşim. |
| Demo veri | `DemoSorular` yerine düz `List<DemoSoru>`; deneme özeti `demoDenemeOzeti()` içinde gerçek `ozet()` ile üretilir. |
| Kuyruk | `demoKuyrugu()` tur boyunu 1–30 ve önizlemeyi 3 satırla sınırlar; geciken sorular üstte. |
| Deneme etkileşimi | D/Y için +/− düğmeleri (TalkBack ve büyük yazıda kaydırıcıdan daha güvenilir); doğru+yanlış soru sayısını aşamaz, eksiltme 0'da durur. Kural seçimi (`NetKurali`) neti canlı değiştirir. |
| Mini tahta | Gerçek tahtanın küçük hâli: `awaitEachGesture` + `historical`, tek dokunuşta nokta, 1–10 dp kaydırıcı (`TahtaKalinlik` sözleşmesi). Rehber çizimi hiçbir yere kaydedilmez. |
| Kapatma | `RehberDeposu` üç ayrı eylem: `tamamla()` (bitir/atla: görüldü + başa dön), `kapat()` (görüldü, kaldığı bölüm korunur), `bastanBaslat()` (Ayarlar'dan yeniden izle). |
| Dönüş hedefi | `UygulamaEkrani.Rehber(baslangic, geri)`: Ayarlar'dan açılınca Ayarlar'a, ilk kurulumdan sonra Bugün'e döner. Saver `"rehber"`; süreç ölümünde korunur. |
| İlk açılış | Yalnızca kurulum **bu oturumda** tamamlandığında ve `gorulduMu()` false ise; mevcut kullanıcı güncellemede rahatsız edilmez. |
| Testler | `RehberModeliTest` (saf JVM), `RehberDeposuTest`, `RehberNavigasyonTest` (Robolectric) ve `RehberEkraniTest` (`app/src/testDebug`, Robolectric + Compose — depodaki mevcut Compose test düzeni). |
| Metinler | 1.305 kaynak, tekrar yok; rehber metinleri yalnız `strings.xml`. |

**Açık kalan cihaz doğrulamaları:** TalkBack ile turun baştan sona okunması, %200 yazı ölçeği,
koyu tema, 320 dp genişlik (Compose testinde kırpılmadığı doğrulandı) ve gerçek telefonda mini
tahta çizimi. Bunlar otomatik testle kapatılamaz; kabul listesinde duruyor.
