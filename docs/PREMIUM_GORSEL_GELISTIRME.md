# Premium görsel iyileştirme — arena/abf7be2d

**Kapsam:** Yalnızca `ui/` katmanı. Domain, data, tekrar kuralları (v2/Gölge), Room/ZIP,
prefs, gezinme durum makinesi ve ölçüm mantığına dokunulmadı; test metinleri/akışları korundu.

## Aşama 1 — Tutarlılık

- **Ham `Card` → `PremiumCard`:** 17 üretim kartı (Bugün kartları, Odak seçenek/zorluk/çözüm
  kartları, SoruDetay/SoruFormu/Konu satırları, rozet/rütbe kartları, motor kartı…); basınç
  ölçeği + ses/haptic profili artık her kartta ortak.
- **Ham `Button`/`TextButton`/`OutlinedButton` → premium sarmalayıcılar:** ~45 çağrı, 20 dosya;
  `return@Premium*` etiketleri güncellendi. Sarmalayıcılara `contentPadding` (ve `TextButton`'a
  `shape`) parametreleri eklendi; `FilledTonalButton` tonal renklerle `PremiumButton`'a taşındı.
- **Şekil ölçeği:** düzenlenen ekranlarda sabit `RoundedCornerShape` tekillikleri
  `MaterialTheme.shapes.*` (PremiumSekiller 6/10/14/20/28) ile birleştirildi.
- **Snackbar:** `inverseSurface` zemin, `shapes.medium`, gölge, yarı-kalın eylem — UNDO/geri
  alma akışı (mesaj, süre, `performAction`) birebir aynı.
- **Boş durum:** Bugün'in özel ham kartı kaldırıldı; üretim geneliyle aynı `BosDurumSoruYok`
  (aynı `today_empty_*` metinleri + mevcut "Soru ekle" eylemi).

## Aşama 2 — Tipografi ve hareket

- **Manrope (OFL) `res/font`:** 400/500/600/700 statik yüzler; Türkçe glif kapsaması
  fontTools ile doğrulandı (ğĞşŞıİöÖüÜçÇ). Lisans: `docs/MANROPE_OFL.txt`.
- **PremiumTipografi:** display/headline/title/label stilleri Manrope'a bağlandı; gövde metni
  sistem fontu olarak kaldı. Sayaç/süre/skor başlıklarında `fontFeatureSettings = "tnum"`
  (tabular figures) — `5/12`, süre ve XP sayıları hizalanır.
- **Bugün hero'su:** diğer ekranlarla aynı `GradyanKart + kicker + başlık` deseni
  (kicker BakirIsik, başlık Kagit — açık/koyu ortak, plan §10.1 kimliği).
- **Hedef halkası:** girişte tek sakin süpürme (`animateFloatPremium`,400ms); hedef değişiklikleri
  animasyonlu ilerler; sistem "hareketi azalt" açıksa anında geçiş.
- **Alt menü göstergesi:** aktif pill `drawBehind` ile ~160ms yumuşak geçiş (eski: anlık atlama).
- **Kutlama parçacıkları:** günlük hedef **yeni aşıldığında** kartta tek seferlik konfeti
  (yeniden girişte tekrar oynamaz; ses zaten `SesTuruGenis.HEDEF` ile mevcut).

## Aşama 3 — Karakter

- **Soru görseli çerçevesi:** `shapes.medium` köşe + `outlineVariant` hairline + 1dp gölge;
  yüklenirken nefes alan shimmer yer tutucusu (durum metni TalkBack/test için korundu).
  Zoom yapılan yüzeylerde (tam ekran, beyaz tahta arka planı) `cerceve = false`.
- **Seçici gradyan CTA** (`PremiumGradyanButon`, `BUTON_BIRINCI`): Bugün "Bugünü başlat",
  Filtreli çalışma başlangıcı, Odak "Değerlendir"/"Kaydet" — her yerde değil.
- **Odak girişi:** çözüm notu/görselleri ve değerlendirme bloğu ekranda seviyede hazırlanan
  giriş fade'i ile belirir; kaydırma yeniden girince tekrar oynamaz (hareket sözleşmesi §10.3),
  gizli içerik ve ölçüm eşikleri (`gorselDurumlari`, `olcum`) değişmedi.

## Uygulama simgesi (launcher icon)

- Yeni tasarım: **sıcak kağıt kapak + derin petrol zemin** — koyu üstü koyu sorunu kalkadı, renk
  ayrımı küçük boyutta bile net. Bakır sayfa kenarı, kırmızı ayraç ve kırmızı düzeltme çizgisi
  (büyük X motifi kullanıcı isteğiyle kaldırıldı).
- Kaynak: `assets/ic_launcher_source.png` (1254×1254). Tüm `mipmap-*` yoğunlukları (48→192)
  bu kaynaktan LANCZOS ile üretildi; `ic_launcher_round.png` 4× supersample daire maskeli.
- APK'ya yalnızca mipmap PNG'leri girer; repo-root `assets/` paketlenmez.

## Sembol/tipografi tutarlılığı + Bugün tek karta indirme (2. tur geri bildirim)

- **Kök neden:** Manrope'ta `⌄ ✓  ◇ ✂` yok; bu glifler sisteme düşüp yanında Manrope harflerle
  farklı görünüyor ("harf karmaşası"). Çözüm: `PremiumTema.PremiumSembolAilesi = FontFamily.Default`
  ortak anahtarı + kapsam dışı gliflerin Manrope'ta olanlarla değişimi.
- Filtreli: `⌄`→`↓`, `‹  ` çift boşluğu `‹ ` oldu. SoruFormu: iki `⌄`→`↓`, `✂` sembol ailesiyle.
- Ayarlar/Odak/Rutbe/Adım göstergesi: `✓` sembol ailesiyle; Rutbe'de `◇`→`◊` (Manrope'ta var).
- Rozet/Karne/Ders kartı: `✓ ` ön eki kaldırıldı veya tek aileye alındı; strings.xml'deki `◎ ± ★ ✗`
  tek gliflik kutularda tutarlı aileden çiziliyor.
- **Bugün:** 5 kart → **2 kart**. Hero (kicker+tarih birleşti) + tek "gün kartı": odak özeti,
  hedef halkası (animasyon+kutlama aynen), soru satırları ve gradyan başlatma CTA'sı tek kartta.
  Veri akışları, sıralama (`akilliOncelik`, take((hedef-gunluk).coerceIn(5,30))), tıklama sözleşmesi,
  tüm string kaynakları ve hata/boş durumları korundu. Sürüm 1.5.98 (code 28).

## Korunan sözleşmeler

- `HareketPolitikasi`: tüm yeni animasyonlar `animateFloatPremium`/`tweenPremium` üzerinden;
  sonsuz animasyon yok, blur yok, altın/ışıltı yalnız başarı anı.
- Metinler `strings.xml`'den; yeni izin/bağımlılık yok (fontlar asset, APK'ya ~390 KB).
- Cihaz/%200 yazı/TalkBack kontrolleri için `docs/TEST_KONTROL_LISTESI.md` akışı geçerli.

## Doğrulama

- Yerelde Java/Android SDK yok; derleme/testler CI'da
  (`.github/workflows/android.yml`, dal `arena/abf7be2d-question-note` — bu dal için
  `PUBLISH_RELEASE` kapalı: yalnız test+derleme+imza, halka açık release yok).
- Statik denetimler: süslü/parantez dengesi, import–kullanım eşleşmesi, kalan `return@`
  etiketleri, konumsal argüman tuzakları — hepsi temiz.

## 2026-10-27 — Üst panel, Bugün hiyerarşisi ve ince ilerleme (derleme bekliyor)

**Kapsam:** `ui/` + `res/values/strings.xml`. Domain, Room, ZIP, prefs, tekrar kuralları ve
öğretmen alanı değişmedi.

- **Üst panel (`ui/panel/UstPanel.kt`)**: alt menünün kalıcı eşi; yalnız sekme köklerinde.
  Sol: `Damga` + canlı tarih (`d MMMM EEEE`, tr-TR) + `SeriAlevi` + seri. Sağ: `HedefHalkasi`
  (16 çentik, 2.5dp yay) + `yapılan / hedef`. Hedef düğmesi 48dp, `onClickLabel` = "İlerlemeyi aç".
  XP/para/enerji yok; tek geçiş İlerleme sekmesi.
- **Insets**: `LocalUstPanelVar` ile sekme kökleri `ekranPencereInsetsi()` üzerinden yalnız yatay
  güvenli alan ekler; üst/alt boşluk panellerde. Alt menü üstünde ikinci boşluk şeridi kalktı.
- **Bugün**: başlık + `+` → gradyan odak kartı (GÜNÜN ODAĞI, sayı + "soru", ince hedef ölçeği,
  bakır CTA) → devam → kuyruk (3 önizleme) → kısayollar. Boşta `BosDurumSoruYok`, hatada tek
  `DurumKarti`, yüklenirken CTA yok. FAB kaldırıldı.
- **Mikro çizimler (`ui/bilesenler/InceCizimler.kt`)**: yeni `YolNoktalari` (2–15 nokta, iz çizgisi,
  dolu/boş nokta yarıçapları) ve mevcut Canvas ailesi. Varlık dosyası yok, APK büyümedi.
- **İlerleme**: XP çubuğu 10 bölmeli `InceOlcek`; `RutbeMadalyonu` 1dp dış halka + 2dp iz +
  bakır `drawArc` (seviye içi gerçek oran); rozet başlığı 54dp `HedefHalkasi(24)`; rozet
  kartlarında 4 bölmeli ölçek; rütbe kutlamasında 9 noktalı `YolNoktalari`; rütbe galerisinde
  15 noktalı yol ve `✓`/halka çizimleri (`stateDescription` ile erişilebilir).
- **Korunanlar**: `progress-xp-bar` `ProgressBarRangeInfo`, tek `analysis-action`, `progress-*`
  test etiketleri, `bugun_sorulacaklar_karti`, `today-deneme-card`, metin bütçesi ve %200
  yerleşim sözleşmeleri. Yeni test: `UstPanelArayuzTest`.

**Doğrulama:** JDK/Android SDK yok → derleme/JUnit yok. tree-sitter: 0 sözdizimi hatası;
`R.*`: 0 eksik; `values*.xml` iyi biçimli. Gerçek kapı CI'dır.
