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
