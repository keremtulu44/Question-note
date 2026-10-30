# HANDOFF — Yanlış Defteri

**Son güncelleme: 2026-10-30.** Önce bu belge, `docs/DERLEME_RAPORU.md`,
`docs/PREMIUM_GORSEL_GELISTIRME.md` ve [P4–P6 sözleşme/kanıt kaydı](docs/P4_P5_P6_UYGULAMA.md) okunur.

## 2026-10-30 — 1.7.0 önizlemesinin üzerine güncelleme adayı

Kullanıcı APK istedi ve telefonunda **1.7.0 önizlemesinin (43)** yüklü olduğunu belirtti.
Kaynakları ayrı iki daldan geldiği için, [preview-1.7.0](https://github.com/keremtulu44/Question-note/releases/tag/preview-1.7.0)
commit'i (`152ae9a`) mevcut `arena/02fc0055-question-note` dalına birleştirildi. Rehber,
1–10 dp/yumuşak çizgili tahta ve 1.7.0 deneme görünümü ile aşağıdaki öğrenci düzeltmeleri
birlikte korunur; ikinci çizim dinleyicisi ve güvenlik kontrolünü atlayan mükerrer deneme
çalışma düğmesi kaldırıldı. Farklı soru/puanlama dağılımları için özet ortalama/en iyi net
ve Bugün'deki trend üretilmez.

APK adayı **1.7.1 (versionCode 44)**, uygulama kimliği ve release sertifikası aynı; Room v5
ve ZIP biçimi değiştirilmedi. `android.yml`: PR testi salt-okunur; açık `[apk]` işaretli dal
push'u testler geçerse imzalı `preview-1.7.1` önizlemesi üretir, resmî `v*` sürümü oluşturmaz.
Elle tetikleme de yalnız önizleme/artefakt üretir. PR otomatik birleştirilmez. Test sonucu ve
imza doğrulaması için [PR kontrollerine](https://github.com/keremtulu44/Question-note/pull/16/checks)
ve APK çalışmasının özetine bak; fiziksel cihaz kabulü ayrıdır. Kullanıcıya kurulum öncesi
Ayarlar'dan ZIP yedeği alması önerilir; uygulamayı kaldırması istenmez.

## 2026-10-30 — öğrenci akışı genel kontrolü (taslak PR #16)

Çalışma `arena/02fc0055-question-note` dalında, [PR #16](https://github.com/keremtulu44/Question-note/pull/16)
üzerindedir; ana dala birleştirilmedi. Öğretmen modu askıda kalır; **öğrencinin**
öğretmene sorulacaklar akışı çalışır. Oturuma devam her zaman kullanıcının açık seçimiyle yapılır.

- Soru ve deneme formları değişiklik varsa geri/vazgeç öncesinde onay ister; değişiklik
  yoksa gereksiz uyarı çıkarmaz. Soru formunda görsel sırası ve geçici dosya değişiklikleri
  de karşılaştırılır; durum kurtarmada ilk değer ve taslak saklanır. Deneme kaydı sırasında
  girişler/geri eylemi kilitlenir; silme hatası sessiz başarı olarak gösterilmez.
- Çözdürdüm penceresindeki öğrenci çözüm notu, seçilen görseller ve bekleyen kamera kimliği
  kurtarılabilir; hedef soru yüklenene kadar beklenir, soru bulunamazsa bildirilir. Soruya özel
  durum anahtarı taslağın başka soruya geçmesini engeller. Değişiklikleri terk etmek veya
  seçilmiş fotoğrafları kaydetmeden ilerlemek onay gerektirir; geçici dosyaların temizliği
  kapatmadan önce beklenir. Bu akış gerçek kamera/galeri cihaz testi değildir.
- İlk kurulum, Ayarlar ders listesi ve kaynak/neden kataloğunda yükleme hatası açıkça gösterilir
  ve yeniden denenebilir. Son seçili ders kaldırılamadığında nedeni bildirilir. Sorulacaklar
  ders filtresi, konu merkezi araması ve dersin arşiv filtresi ileri/geri gezinmede korunur.
- Soru detayında boş çözüm için yanıltıcı düğme yoktur; açılmış gerçek çözüm yapılandırma
  kurtarmasından sonra da açılmış sayılır. Üst panelde seri henüz okunmadıysa "sıfır seri"
  uydurulmaz. `OdakOturumuDeposu` örnekleri süreç içi ortak kilit/nesil ile yedek geri
  yükleme sonrası eski bekleyen yazıları engeller (farklı Android süreçleri için kilit değildir).
- Önceki çalışma: ilk karede görünür lazy içerik; ders/filtre tercihlerinin korunması;
  kullanıcının seçimiyle yarım oturuma devam/silme/üzerine yazma; gerçek deneme, kaynak ve
  yanlış nedenleriyle analiz. Room kaynak taşıma ile deneme tercihleri aynı işlem değildir;
  kısmi hata bildirilir ve aynı adla yeniden denenebilir.

Doğrulama: PR'daki Android Debug/Release test sonucu için
[Actions kontrollerine](https://github.com/keremtulu44/Question-note/pull/16/checks) bak;
`docs/DERLEME_RAPORU.md` PR çalışmasında güncellenmeyen **main release** raporudur.
Yerelde SDK yoktur. Bu ilk genel kontrol aşamasında gerçek cihaz/emülatör, fiziksel
kamera/galeri, imzalı APK ve yayın doğrulanmamış/üretilmemişti. Yeni testler Robolectric + gerçek Room ve tercih
sınamalarıdır; fiziksel cihaz kabul testi yerine geçmez.

## 2026-10-29 (3) — uygulama içi rehber (dal: `arena/ce557218-question-note`)

Kullanıcı isteği: "anlamlı bir tutorial hazırla" — hem tanıtan, hem kayıt/deneme/çözmeyi gösteren,
hem yedeklemeyi öğreten, demo verilerle çalışan, premium hissi koruyan kapsamlı bir tur.

**Sonuç:** `v1.7.0` adayı (versionCode 43). Sürüm/Room/ZIP/tekrar motoru değişmedi; yeni izin ve
bağımlılık yok; öğretmen alanına dokunulmadı.

**Yeni dosyalar**
- `ui/rehber/RehberModeli.kt` — `RehberBolumu` (7 bölüm, `anahtar`), demo sorular/kuyruk,
  `tekrarOnizlemesi()` (gerçek `TekrarKurali`), `DemoDersSkoru` (+/-, `net(kural)`),
  `demoDenemeOzeti()` (gerçek `ozet()`), `yedekIcerigi()`. Compose bağımlılığı yok.
- `ui/rehber/RehberCizimleri.kt` — `RehberDefterCizimi`, `RehberAralikYolu`, `RehberYedekKutusu`
  (Canvas; bitmap/varlık yok, APK büyümez).
- `ui/rehber/RehberBolumleri.kt` — 7 bölümün içeriği; `RehberDurumu` (bölümler arası seçim),
  mini tahta `awaitEachGesture` + `historical` ile gerçek tahtanın küçük hâli.
- `ui/rehber/RehberEkrani.kt` — `HorizontalPager`, "3 / 7" ilerleme + `InceOlcek`, İleri/Geri/Atla,
  `BackHandler` ile bölüm bölüm geri, azaltılmış harekette animasyonsuz geçiş.
- `data/prefs/RehberDeposu.kt` — `gorulduMu/sonBolum/bolumuKaydet/tamamla/kapat/bastanBaslat/sifirla`.
  Ayrı SharedPreferences dosyası (`rehber_durumu`); **ZIP yedeğine girmez** (cihaz tercihi).

**Entegrasyon**
- `UygulamaEkrani.Rehber(baslangic, geri)` + saver `"rehber"`; `aktifSekme = AYARLAR`,
  `altMenuGorunur = false`, `geriHedefi = geri` (Ayarlar'dan açılınca Ayarlar, ilk kurulumda Bugün).
- `DersKurulumEkrani.onTamam` → `kurulumGerekli = false`; rehber hiç görülmediyse bir kez açılır.
- `AyarlarEkrani`'ye `onRehber: () -> Unit = {}` + **YARDIM** bölümü (varsayılanlı parametre,
  mevcut çağrılar değişmedi). `AppContainer.rehberDeposu` → `MainActivity` → host.
- Kapatma `RehberDeposu.kapat()`: otomatik açılış bir daha olmaz, kaldığı bölüm korunur.

**Testler:** `RehberModeliTest` (saf JVM: bölüm kimlikleri, motor eşitliği, aralık sırası,
kuyruk sınırları, D/Y sınırları ve simetri, özet, yedek listesi), `RehberDeposuTest` (Robolectric),
`RehberNavigasyonTest` (saver gidiş-dönüş + bozuk kayıt), `RehberEkraniTest` (testDebug,
Robolectric + Compose: ileri/geri, atla, son bölüm, canlı net, tekrar tarihi, mini tahta dokunuşu,
320 dp genişlik).

**CI:** `f37db2a` ilk denemede yalnız test beklentileri yanlıştı (ZORLANDIM önceki aralıkla sabit
kalır, boş soru sayısı 20'dir); `ba1d66f` yeşil. Compose testi eklenince `c81a5ed` derlenmedi:
`assertDoesNotExist` ve `fetchSemanticsNode` **üye** fonksiyonlardır, top-level import edilemez
(annotation'dan okundu) → importlar kaldırıldı, run `123674724445` **yeşil** (Debug + Release unit,
testDebug Compose dahil). Cihaz kabulü hâlâ açık: TalkBack ile tur, %200 yazı, koyu tema,
gerçek telefonda mini tahta çizimi.

**Son tur (aynı gün):** %200 yazı için uyarlanabilir yerleşim — seçenek/sayaç/kural çipleri
`FlowRow` ile alt alta, alt düğmeler tam genişlikte, kalınlık denetimi üç satır, `sizeIn` yerine
`heightIn(min = 44–48.dp)` hedefleri. `RehberEkraniTest` bunu `Density(fontScale = 2f)` ile
doğrular. Run `123679774478` yeşil (Debug + Release unit, 8 Compose testi dahil).

## 2026-10-29 (2) — çizim tahtası elden geçirildi (dal: `arena/ce557218-question-note`)

Kullanıcı geri bildirimi: "çizgide devamlılık yok, nokta koyulmuyor, kalınlık 1–10 ayarlanabilmeli".

**Kök nedenler ve düzeltmeler (`ui/calisma/CozumTahtasiDialog.kt`):**
1. `detectDragGestures` her `onDrag`'da vuruşun **tamamını** kopyalayıp `onVurusDegisti` ile dialog
   state'ine yazıyordu → her parmak hareketinde tüm listenin yeniden çizimi; hızlı çizimde kare
   düşmesi ("çizgi arada duruyor"). Artık `awaitEachGesture` + `awaitFirstDown` + `awaitPointerEvent`
   ile canlı noktalar **tuvalin içinde** (`canliNoktalar` + `canliSurum`) tutuluyor, yalnız çizim
   katmanı yenileniyor. `historical` ara konumları da işleniyor; hızlı harekette çizgi kopmuyor.
2. Serbest çizgi düz `lineTo` zinciri yerine **orta noktalardan geçen quadratic eğrilerle**
   (`noktalariYola`) çiziliyor → köşeli/kesik his kalktı.
3. Dokun-kaldır hiç iz bırakmıyordu (drag eşiği); artık tek dokunuş `drawCircle` ile nokta bırakır.
4. Görsel çerçevesi dışında başlayan dokunuş tamamen atılıyordu; başlangıç artık sınıra kırpılır.
5. Kalınlık 2/4/8 sabitleri yerine **1–10 dp, 1'er adımlı kaydırıcı** (`TahtaKalinlikSecimi`,
   dar çubukta `TahtaKompaktKalinlikDugmesi`). Uç önizleme + `dp` değeri görünür; `stateDescription`
   ile TalkBack "N dp" duyar. Renk şeridi dar ekranda yatay kaydırılır (42 dp hedef kırpılmaz).
6. Silgi `max(kalinlik, 4 dp) × 3.4` genişlikte siler; ince kalemde görünmez silgi hatası kapandı.
7. Kalınlık sözleşmesi Compose'suz `ui/calisma/TahtaKalinlik.kt` dosyasına alındı
   (`tahtaKalinligiSinirla`, NaN/Infinity güvenliği) → saf JVM testi `TahtaKalinligiTest`.
8. Sadeleştirme: kullanılmayan `appendPoint`, `etkinVurus` hoisting'i ve üç eski "İnce/Normal/Kalın"
   metni kaldırıldı.

**Yeni testler:** `TahtaKalinligiTest` (sınırlar, yuvarlama, NaN), `CozumTahtasiKullanimTest`
içinde `tekDokunusNoktaBirakirVeSuruklemeTekVurusUretir` ve
`kalinlikKaydirmalariBirIleOnArasindaAdimAdimAyarlanir`.

**CI:** run `40921231264` yeşil (Debug unit + Release unit). İlk denemede yalnız
`SemanticsActions` importu eksikti; hata, workflow'un zaten ürettiği **annotation** üzerinden
okundu (`check-runs/{id}/annotations`) — kısıtlı ağda log/artifact indirilemediğinde bu yol
kullanılmalı. Cihazda çizim akıcılığı kabulü (60 Hz, 200+ vuruş) hâlâ açık.

## 2026-10-29 — deneme tarafı: görünür eksikler + ürün değerlendirmesi (dal: `arena/ce557218-question-note`)

Kullanıcı isteği: depoyu tara, görüntüsel/eksik yerleri düzelt, deneme ve soru çözüm tarafı için
öneri üret, çevrimdışı OCR fikrini değerlendir. **Sürüm/Room/ZIP/tekrar motoru/XP değişmedi**,
öğretmen alanına dokunulmadı (`AGENTS.md`).

**Düzeltilen gerçek eksik:** `UygulamaEkrani.DenemeDetay` host'u `onDenemeyiCalis` veriyordu ama
`DenemeDetayEkrani` bu parametreyi hiç çizmiyordu (ölü uç). Artık denemede deftere yüklenmiş soru
varsa "Bu denemenin soruları" kartı görünür ve `SoruCalismaFiltresi(kaynak = deneme.ad)` turunu
açar (`deneme-study-button`).

**Görsel/erişilebilirlik:**
- Net grafiğine ilk/son tarih etiketleri ve `contentDescription` (`deneme_chart_description`).
- Grafiğin altına üç sayılık özet şerit: deneme sayısı, ortalama net, en iyi net. Kurallar
  karışıksa ortalama/en iyi **üretilmez**, yalnız "son net"; tek kayıtta da yalnız "son net"
  (uydurma karşılaştırma yok).
- Liste kartına "En düşük: <ders> · <net>" satırı (tek dersli denemede gösterilmez).
- Liste ekranındaki üst çubuk + liste başlığı tekrarı kaldırıldı; yerine tek satır bağlam.
- Bugün'deki deneme satırındaki sabit `oran = 0.72f` halka (sahte yüzde) yerine son 6 denemenin
  gerçek `MiniCizgi` sparkline'ı; iki kayıttan az veride boş halka.

**Yeni dosyalar:** `domain/model/DenemeOzeti.kt` (`ozet()`, `enZayifDers()`),
`test/.../DenemeOzetiTest.kt` (6 saf JVM testi), `docs/URUN_DEGERLENDIRME_VE_ONERILER.md`
(bulgular, deneme/çözüm yol haritası, OCR değerlendirmesi, kapsam sözleşmesi).

**CI:** iki ardışık çalıştırma yeşil — `40902868803` (5m25s) ve `40904376134`
(Debug + Release birim testleri başarılı; `release` işi PR'da beklendiği gibi atlandı).
Cihaz kabulü (deneme ekranları %200 yazı, koyu tema, TalkBack, 320 dp) **hâlâ açık**.

**Sıradaki somut adım:** (1) bu ekranların cihaz kontrolü, (2) `DenemeDeposu` bozuk-JSON
karantinası (bugün `getOrDefault(emptyList())` ile sessiz kayıp), (3) deneme sonrası toplu
fotoğraf kuyruğu, (4) grafiklerde kalan `clearAndSetSemantics` taraması, (5) ölü metin temizliği
(1.210 metnin 203'ü kullanılmıyor). Ayrıntı ve gerekçeler: `docs/URUN_DEGERLENDIRME_VE_ONERILER.md`.

## 2026-10-28 — üç iş: form kurtarma, CI bölme, belge senkronu (dal: `arena/35cb2bf0-question-note`)

Kullanıcı onayıyla üç iş uygulandı; **sürüm artırılmadı** (APK yayımları kullanıcı kapısında),
**APK küçültme/R8'e dokunulmadı**, öğretmen modu arkada gizli bırakıldı (kod/veri/test silinmedi).

1. **Soru formu süreç ölümüne dayanıklı:** `SoruFormuEkrani` kalıcı alanları, hazırlanmış
   görseller ve bekleyen kamera çekimi `rememberSaveable` + saver'larla yaşıyor; kök gezinme
   durumu `UygulamaEkraniSaver` (JSON) ile kurtarılıyor (odak oturumu ekranı bilerek Bugün'e
   döner, oturum devam noktası deposundan gelir). Düzenlemede kurtarılan değer DB ile
   ezilmiyor; kaydet/vazgeç taslağı sıfırlıyor. Yeni test: `SoruFormuDurumKurtarmaTest`
   (saver round-trip + `StateRestorationTester`). `SorulacaklarEkrani` içindeki
   Çözdürdüm dialogu o tarihte hedef (`Soru`) kalıcı olmadığı için kapsam dışındaydı;
   sonraki 2026-10-30 öğrenci akışı kontrolüne bak.
2. **CI bölündü:** `.github/workflows/android.yml` → `test` (PR + main/v*; salt-okunur,
   secret'sız; kanıt artifact + step summary) ve `release` (yalnız main/v*, `needs: test`;
   imza/Release/rapor commit'i burada). Elle dal listesi kaldırıldı; dalda CI için PR açmak
   yeterli. `preview-<sürüm>` prerelease güncelleme, imzalı APK artefaktı ve
   Today/P6/Progress önizleme kanıtları bölünmüş akışta aynen korunur.
   `docs/DERLEME_RAPORU.md` artık yalnız main release çalıştırmasında güncellenir —
   PR sonucu için Actions özeti/artifact'a bakılır.
3. **Belgeler 1.6.3'e senkronize:** README durum + indirme (1.6.3, asset SHA-256
   `787690fc…`), CHANGELOG'a 1.6.1/1.6.2/1.6.3 kayıtları (Release üst verisinden tamamlandı),
   BUILDING sürüm/CI satırları. Öğretmen modu ifadeleri güncel "gizli/askıda" kararına çekildi.

Doğrulama: yerel ortamda Java/SDK yok; `statik_kontrol.py` + `denge.py` + workflow YAML/bash
(`bash -n`) kontrolleri yeşil. PR #13 run `40703967953` (commit `0f2951c`): bölünmüş `test`
işi yeşil — Debug **394** (yeni 6 restorasyon testi dahil) + Release birim testleri 0 hata;
`release` işi PR'da beklendiği gibi atlandı. Üç iş, üst panel işini içeren güncel main
(`5410c15`, Debug 388) üzerine taşındı.
Room/ZIP sürümü, tekrar motoru, XP/rozet katsayıları değişmedi.

> Not (üst panel bölümündeki APK notu için): sürüm etiketi dolu olduğu için en güncel imzalı
> derleme `preview-1.6.3` prerelease'inden indirilir; yayımlanmış v1.6.3 asset'i üst panel
> öncesi derlemedir. README "Durum" bölümü yayımlanmış sürümü, bu belge ve
> `docs/DERLEME_RAPORU.md` en güncel derlemeyi anlatır.

## Güncel ürün kararı — 2026-10-27: öğretmen modu gizli ve askıda

**Önce kökteki `AGENTS.md` okunur.** Kullanıcı öğretmen tarafının gizlenmesini ve
sonraki oturumlarda geliştirilmemesini istedi. Önceki "öğretmen geçişi korunsun"
kararı artık geçerli değildir. `UrunKapsami.OGRETMEN_MODU_GORUNUR = false`:
kurulum/ayar girişleri gizli; eski kayıtlı öğretmen rolü de ekranda öğrenciye
 yönlenir. Rol tercihi, kod, tablolar, fotoğraflar ve yedek desteği korunur.
Öğrencinin öğretmene sorulacaklar akışı aktiftir. Kullanıcı açıkça yeniden
istemeden öğretmen alanına geliştirme/refaktör/görsel değişiklik yapılmamalı.
Bu gizleme değişikliği yayımlanmış 1.6.1 APK'da henüz yoktur.

## Premium üst panel + Bugün + ince ilerleme (2026-10-27, derleme bekliyor)

Kullanıcı isteğiyle üst boşluk, alt menünün kalıcı eşi olan bir **üst panelle** dolduruldu;
oyunlaştırma (XP/para/enerji) yok. Panel yalnız gerçek bugün durumunu taşır:

- **Üst panel** (`ui/panel/UstPanel.kt`): sol tarafta `Damga` + canlı tarih (`d MMMM EEEE`,
  tr-TR) + `SeriAlevi` ile seri; sağda `HedefHalkasi` (16 çentik + yay) ve `yapılan / hedef`.
  Hedef düğmesi 48dp, tek anlamlı geçişle İlerleme sekmesini açar; `onClickLabel` = "İlerlemeyi aç".
  Panel yalnız sekme köklerinde (`UygulamaEkrani.altMenuGorunur()`) görünür; detay/form
  ekranlarında gizlenir.
- **Insets sözleşmesi**: host `Scaffold` `contentWindowInsets = 0`; üst/alt boşlukları paneller
  taşır. Sekme kökleri (`Bugün`, `Dersler`, `Filtreli`, `İlerleme`, `Ayarlar`) `ekranPencereInsetsi()`
  ile yalnız **yatay** güvenli alan ekler; böylece alt menünün üstünde ikinci bir gezinme çubuğu
  boşluğu kalmaz. Panel göstermeyen detay/form ekranları (SoruDetay, SoruFormu, Sorulacaklar,
  Ders çalışma, Katalog, Konu yönetimi, Görsel bakım, Odak, Deneme, Karne, öğretmen) tam
  `safeDrawing` kullanmaya devam eder — onlarda üst boşluğu panel değil durum çubuğu taşır.
- **Bugün** (`ui/BugunEkrani.kt`): başlık ("Bugün" + tek satır bağlam + 48dp `+`), gradyan
  **odak kartı** (GÜNÜN ODAĞI, sayı + "soru" birimi, günlük hedefin ince ölçeği, tek bakır CTA),
  isteğe bağlı devam satırı, kuyruk kartı (3 önizleme, gecikme etiketi), sorulacaklar/deneme
  kısayolları. Boş havuzda sıfırlı kahraman kart yerine tek `BosDurumSoruYok`; hatada tek
  `DurumKarti`; yüklenirken CTA/ölçek çizilmez. FAB yok; `+` her durumda görünür.
- **Rütbe galerisi**: 15 noktalı yol + sıradaki rütbe kartı (görsel, "60 XP kaldı",
  8 bölmeli ince ölçek); kartlar `stateDescription` ile "Bu rütbeye ulaştın"/"Henüz ulaşılmadı"
  bildirir, ✓/halka metin glifi yerine çizilir.
- **GradyanKart ışığı**: düz yarı saydam dairelerin keskin kenarı koyu temada leke gibi
  görünüyordu; iki radyal parlamaya çevrildi.
- **İnce ilerleme detayları**: yeni `ui/bilesenler/InceCizimler.kt` (Damga, HedefHalkasi,
  InceOlcek, SeriAlevi, SoruIsareti, InceOk, KucukArti, KucukOnay, OynatSimgesi, YolNoktalari)
  Canvas ile çizilir; varlık dosyası eklenmedi. XP çubuğu 10 bölmeli `InceOlcek`; rütbe
  madalyonu 1dp dış halka + 2dp iz + bakır yay; rozet başlığında 54dp `HedefHalkasi(24)`;
  rozet kartlarında 4 bölmeli ölçek; `KutlamaHost` rütbe görselinin altında 9 noktalı yol.
  Rütbe galerisinde 15 noktalı yol + çizilmiş ✓/halka (metin glifi yerine).
- **Kaldırılanlar**: Filtreli çalışma ekranındaki gereksiz "Geri" düğmesi (sekme kökü; alt menü
  ve sistem geri tuşu yeterli), Bugün'deki "Hedef tamam / N soru daha" ikinci satırı ve FAB.
- **Test**: yeni `UstPanelArayuzTest` (hedef/seri/tarih, XP-benzeri kelime yokluğu, 48dp hedef,
  tıklama yönlendirmesi, 2× yazıda ölçü) ve `BugunUstPanelOnizlemeTest` (gerçek kabukta
  `docs/ui/today/bugun-light|dark.png`). Mevcut test sözleşmeleri korundu
  (`bugun_sorulacaklar_karti`, `today-deneme-card`, ilerleme etiketleri).

**CI kanıtı (2026-10-27, run `40541253872`, commit `e51ab43`):** Debug **388/0/0/0**,
Release ortak **303/0/0/0**, `assembleRelease` 0, imza + beklenen sertifika doğrulandı.
Önceki turlarda yalnız yeni test dosyaları kırıldı ve düzeltildi: `DpRect.height/width`
(Kotlin uzantısı importu), `SoruEntity` zorunlu `olusturma/guncelleme` alanları, tekrar
kaydı öncesi dış anahtarlı soru, %200 yazıda gizlenen "Hedef" etiketi beklentisi ve
`DenemeVeKurulumArayuzTest` içindeki tek `waitForIdle` yarışı (`waitUntil`'e çevrildi).
Görsel önizlemeler `docs/ui/today/bugun-light.png` ve `bugun-dark.png` olarak repoda.

**APK dağıtımı:** sürüm adı/numarası değiştirilmedi. CI artık iki yerden indirilebilir APK üretir:
(1) Actions artefaktı `yanlis-defteri-apk` (30 gün, GitHub girişi gerekir), (2) sürüm etiketi
doluysa `preview-<sürüm>` prerelease'i — girişsiz indirme, her koşuda `--clobber` ile güncellenir.
Bu tur: https://github.com/keremtulu44/Question-note/releases/tag/preview-1.6.3 ·
SHA-256 `0d270161494d04d7a2149b7d50f4eab4d8c578b9b92432b16d92095cda847bf1`
(run `40559677656`, commit `4a43ba8`, Debug 388/0, Release ortak 303/0, imza doğrulandı).
Tüm `app/src/**/*.kt` tree-sitter ile **0 sözdizimi hatası**, tüm `R.*` referansları
**0 eksik** (values + drawable*/font/raw/xml), `values*.xml` iyi biçimli. Gerçek kapı CI'dır;
sürüm/Room/ZIP değişmedi.

## 1.6.1 yayımlandı — güncel doğrulama

Son olgunlaştırma değişiklikleri 1.6.1 (40) APK'ya dahil edildi.
CI `40384089478`, derlenen commit `f4de076`: Debug 380/0, Release ortak 300/0;
imzalı APK Release v1.6.1'de mevcut. Ayrıntılar `docs/DERLEME_RAPORU.md`.
Aşağıdaki derleme bekliyor notları önceki yerel çalışma aşamasını anlatır;
son CI bunların derleme/test kapısını açmıştır. Fiziksel cihaz kabulü hâlâ ayrıdır.

## Kullanım olgunluğu çalışması (2026-10-26, derleme bekliyor)

`arena/263f59f4-question-note` üzerinde öğrenci Bugün kuyruğu en fazla üç önizlemeye
indirildi; tur/önizleme aynı saf `GunlukCalismaPlani` sırasını kullanır. Öğrenci
ayarlarında demo/motor kartları ve ilerlemede teknik ayrıntı menüsü kaldırıldı;
öğretmen moduna geçiş korundu. İlerleme çalışma önerisi, yedi günlük kayıt ve ders
pekişmesini öne alır. Öğretmen host safeDrawing, geniş ekran içerik sınırı, tema ve
canlı tarih düzeltmeleri; kuyrukta hedefli Room güncellemesiyle hızlı planlama eklendi.

İkinci geçiş: kart etiket/işlemleri sarılır, büyük yazı düğmeleri alt alta gelir;
gruba özgü iş özeti, kuyruk silme onayı + geri alma ve görünür listeyle sınırlı
toplu seçim eklendi. Yeni 320 dp / yüzde 200 yazı testleri ve kuyruk silme testi
kaynak olarak hazır; çalıştırılmadı. 30 değişen/yeni Kotlin dosyasında ayrıştırıcı
sözdizimi hatası yok (derleme kanıtı değildir).

Ayrıntılar, ihtiyaç temelli öneriler ve cihaz kabul listesi:
[`docs/OLGUNLASMA_INCELEMESI.md`](docs/OLGUNLASMA_INCELEMESI.md).
Statik kaynak kontrolü ve diff kontrolü geçti. **Java/SDK olmadığı için yeni Kotlin,
JUnit, APK ve görsel doğrulama yapılmadı.** Aşağıdaki eski CI sonuçları bu değişikliklerin
kanıtı değildir. Sürüm, Room şeması, yedek biçimi artırılmadı.

## T3 uygulandı ve CI'dan geçti (2026-10-26)

Kullanıcı kararı: **yalnız T3** ve **tamamı çevrimdışı** (bulut/hesap/bildirim yok, Play'e yüklenebilir
yerel uygulama). `versionName`/`versionCode` artırılmadı; Room/ZIP sürümü değişmedi.

**CI kanıtı:** run `40344883493` (commit `8d5c458`) — Debug **370/0/0/0**, Release **293/0/0/0**,
imza + beklenen sertifika parmak izi eşleşti. APK SHA-256 `f6b5c7188bdccf1cbfca9b77ab9700c2644938cfe3e9535ff20b743b2e3c4cf8`
(v1.6.0 etiketi zaten yayımlandığı için Release yeniden oluşturulmadı; sürüm artırılmadan yeni APK
yayımlanmaz).

- **Arama/filtre:** `OgretmenVerisi.filtrele(...)` + `OgretmenSorusu.aramaHavuzu(...)` (başlık, kim
  sordu, kaynak, ders/konu adı; `aramaAnahtari` = `tr-TR` küçük harf). `KuyrukGunFiltresi`
  (HEPSI/BUGUN/GECIKEN/PLANLANMAMIS) `kuyruk(...)` içinde; eski `yalnizGeciken` bayrağı kaldırıldı.
  UI: `OgretmenAramaAlani` + `FiltreCipi` satırları (gün, ders, soran), `rememberSaveable` durum.
- **Paylaşım:** `ui/ogretmen/OgretmenPaylasim.kt` — tek yerel `ACTION_SEND`/`ACTION_SEND_MULTIPLE`,
  `FileProvider` URI'leri (`res/xml/file_paths.xml` → `share_teacher` `ogretmen-img/`, `share_archive`
  `img/`), en fazla 9 fotoğraf, `FLAG_GRANT_READ_URI_PERMISSION`. **"kim sordu" ve cevap metne
  girmez.** Manifest değişmedi (provider zaten `exported=false`, izin yok).
- **Kapatma döngüsü:** `OgretmenDeposu.grupSil(grupId, dosyalariKoru = true)` + yeni
  `grupGeriAl(grup, sorular): Int?` (grup + sorular tek transaction, aynı kimlikler). Detay ekranı
  silmeden sonra `onGeri()` çağırıp geri almayı `Snackbar` (10 sn) ile sunar; bu yüzden host
  `OgretmenAnaEkrani` bir `kaliciKapsam` (host `rememberCoroutineScope`) geçirir — ekran kapanınca
  coroutine iptal olmasın. Arşiv sekmesinde `ogretmen_arsivden_geri_al` toplu geri alma.
- **Uyarılar:** `OgretmenAyarlarEkrani` 7+ gün/ hiç yedek yoksa `ogretmen_yedek_uyarisi`;
  `OgretmenSoruFormuEkrani` aynı `sha256` başka soruda ise `ogretmen_ayni_gorsel_dialog` onayı
  ("yine de kaydet").
- **Testler:** `OgretmenAramaVeFiltreTest` (saf JVM; arama/filtre/normalize/aynı görsel/1.000 soru
  < 200 ms), `OgretmenGrupGeriAlmaTest` (Room undo + tekrar çağrı + arşivden dönüş),
  `OgretmenPaylasimTest` (chooser/içerik URI'si/metin/9 sınırı + `file_paths.xml` ve sağlayıcı
  beyanı), `OgretmenT3ArayuzTest` (Robolectric Compose: arama, kart paylaşımı, snackbar geri alma).
  Paylaşımda URI üretimi `uriUret` ile enjekte edilir: `FileProvider` yol stratejisini süreç
  genelinde önbelleğe aldığı, Robolectric'in her test metoduna yeni `filesDir` verdiği için
  sağlayıcı aynı JVM'de iki kez çalıştırılamıyor (`Failed to find configured root`).
- **Kayıt dayanıklılığı:** `soruKaydet` ders/konu kimliğini katalogda doğrular; form açıkken
  arşivlenen/silinen bir ders yabancı anahtar hatasıyla tüm soruyu kaybettiriyordu, artık yalnız
  etiket atılır.
- **Bilinen açıklar:** "bugüne al / yarına at" hızlı plan aksiyonu, soru+çözüm paylaşım düğmesi
  (`cozumDahil` hazır), tüm-gruplar arşiv filtresi, içgörü kartı ve **cihaz kabulü** (200+ soru arama,
  5 soru paylaşma, grup geri alma, 9 günlük yedek uyarısı). Otomatik test yeşil olması cihaz kapısını
  açmaz.

## Güncel ve yayımlanmış: 1.6.0 Öğretmen modu — T0 + T1 + P2 başlangıcı

- **Dal:** `arena/5d75ca16-question-note`. **Sürüm:** `versionName = "1.6.0"` (`versionCode = 39`).
- **Veri katmanı (Room v5):** öğretmen çalışma alanı artık veritabanında:
  `ogretmen_gruplari` (FK `dersler` SET NULL), `ogretmen_sorulari` (FK grup CASCADE, ders/konu SET NULL),
  `ogretmen_soru_gorselleri` (FK soru CASCADE, `tur` = SORU/COZUM). `MIGRATION_4_5` yalnız ekleme yapar.
  `OgretmenDeposu` Room tabanlı; her mutasyon `baslat()` sonrası tek transaction + `state` yenileme.
- **Eski JSON:** `ogretmen_calisma_alani.json` açılışta bir kez toleranslı içe aktarılır, sonra
  `ogretmen_calisma_alani.ice-aktarildi.json` olarak saklanır; okunamayan dosya
  `ogretmen_calisma_alani.bozuk-<zaman>.json` karantinasına alınır ve `OgretmenAlanDurumu` ile bildirilir.
  İçe aktarmada `ders_id`/`konu_id` ortak katalogla doğrulanır (FK güvenliği) ve görselsiz sorular atlanır.
- **Arayüz:** `OgretmenAnaEkrani` üç hedefe ayrıldı (Gruplarım / Bugün / Ayarlar) ve 2.561 satırlık
  monolit `ui/ogretmen/` altında dosyalara bölündü: `OgretmenBilesenler`, `OgretmenGruplarEkrani`,
  `OgretmenKuyrukEkrani`, `OgretmenGrupDetayEkrani`, `OgretmenSoruFormuEkrani`, `OgretmenSunumEkrani`,
  `OgretmenAyarlarEkrani`. Kuyruk, toplu işlem, arşiv, silme geri alma, taşıma/kopyalama, sunum
  (ekran açık + gizli cevap + çözüm ayrı) ve depolama/bakım kartı bu pakette.
- **Kamera:** manifestte `CAMERA` izni yok; `SorulacaklarEkrani` ve öğretmen formundaki runtime izin
  kapısı kaldırıldı. `ACTION_IMAGE_CAPTURE` doğrudan `FileProvider` yoluna yazıyor.
- **Yedek:** `FORMAT_VERSION = 5`, `ROOM_SCHEMA_VERSION = 5`, okuma `{2,3,4,5}`; öğretmen görselleri
  `img/` veya `ogretmen-img/` olabilir, ZIP'te tek `images/` klasörü. `teacher_workspace_version` ve
  görsel türü/sayı doğrulamaları eklendi.
- **Testler:** `OgretmenMigrationTest` (gerçek v4 şemasından 4→5, indeksler, CASCADE/SET NULL),
  `OgretmenDeposuRoomTest` (JSON içe aktarma, toplu durum, taşıma, kopyalama, silme geri alma);
  mevcut üç test dosyası artık `OgretmenDeposu(context, gorselDeposu, database)` ile in-memory DB kullanıyor.
- **Doğrulanmış CI:** `40280978018` — **Debug 351/0/0/0 · Release 277/0/0/0**, `assembleRelease`,
  imza + beklenen sertifika parmak izi eşleşti; Release `v1.6.0` yayımlandı, APK
  `yanlis-defteri-1.6.0.apk` (13.653.001 bayt, SHA-256
  `db756b455b2d3942be7f2d009f00062696e100482938206b951c6b161f4c470f`). Sayılar iki varyantta
  tekrar eder, toplanmaz.
- **Arayüz kararları (K45/K46):** öğretmen onay/seçim/form pencereleri tek `OgretmenDiyalogu`
  kabuğunda (düz `Dialog`); Material3 `AlertDialog` Robolectric Compose testlerinde boşta kalmıyordu.
  Host ekranları animasyonsuz `when` ile çizilir; `AnimatedContent` dialog penceresini her karede
  yeniden kuruyordu.
- **CI altyapısı:** rapor adımı önce düz `git push` deniyor, yalnız reddedilirse derinleştirip
  rebase ediyor ve rapor özetini log'a basıyor; böylece test sonuçları dalda kaybolmuyor.
- **Açık kalanlar:** fiziksel cihaz kabulü (kamera izinsiz açılmalı, süreç öldürme, 200+ soru,
  1.6x yazı ölçeğinde sunum/klavye) ve T2 maddeleri (`ROADMAP.md`).
- **Sıradaki iş önerisi:** [öğretmen ihtiyaç araştırması](docs/OGRETMEN_IHTIYAC_ARASTIRMASI.md)
  (2026-10-26) — kanıtlı 14 boşluk, 22 fikir ve T3/T4 sıralaması. T3 tamamen mevcut v5 verisiyle
  yapılabilir (arama, "kim sordu" yüzeyi, paylaşım, plan grupları, arşiv geri alma, yedek tazeliği);
  T4 (fotoğrafsız sözlü soru, taslak kutusu) **Room/ZIP v6** gerektirir ve A12/A16 kararlarına bağlı.

## Önceki doğrulanmış teslim: 1.5.108 Uygulama Geneli Görsel, Geometrik & Tipografik Bütünlük Denetimi

- **Sürüm:** `versionName = "1.5.108"` (`versionCode = 38`), çalışma dalı `arena/9d4e7eac-question-note`.
- **Tam ekran görüntüleyici ve yakınlaştırma sınırları:** `SoruGorselBileseni` içinde `SoruGorselTamEkran` tam ekran görüntüleyicisinin `0px` yüksekliğe çökmesine neden olan `matchParentSize` / `fillMaxWidth` hatası düzeltildi; `SoruGorselTamEkran`, `SorulacakTamEkranSunumDialog` ve `OgretmenTamEkranSoruDialog` bileşenlerine `.clipToBounds()`, çift dokunuşla `2.4x` yakınlaştırma, `1x Sıfırla`, koyu stüdyo kenarlık kontrastı ve devre dışı buton soluklaştırması eklendi.
- **`LazyItemScope` çakışma ve yerleşim düzeltmeleri:** `OdakOturumuEkrani`, `GorselBakimEkrani` ve `DenemeEkranlari` içindeki çoklu kardeş öğeli `item {}` bloklarının `(0, 0)` noktasında üst üste binmesi giderildi; `DerslerEkrani` ve `KatalogYonetimiEkrani` üst kartları kaydırılabilir yapıya alındı; `DersKarti` esnek yükseklik (`min = 144.dp`) ve 2 satır başlıkla güncellendi; `KonuYonetimiEkrani` ve `SoruDetayEkrani` eylem satırları dar ekranlar için `FlowRow` yapısına geçirildi; tüm kalan ekranlara (`Bugun`, `OdakOturumu`, `SoruDetay`, `KonuYonetimi`, `Ayarlar`, `Karne`, `KatalogYonetimi`, `GorselBakim`, `Seviye`) tutarlı `safeDrawing` + `consumeWindowInsets` uygulandı.
- **Manrope glif güvenliği, Deneme soru konumu ve Öğretmen yedek önizlemesi:** Binary TTF `cmap` denetimiyle Manrope içinde yer almayan tüm sembollere (`◎`, `◈`, `★`, `✓`) `PremiumSembolAilesi` tanımlandı (`▾` → `↓`). Deneme kaynağından soru eklerken `SoruFormuEkrani` içinde `kaynakKonumu` alanı açık tutuldu; `MotorAyarlariKarti` radyo seçeneklerine görsel seçim halkası ve vurgu eklendi; `OgretmenAnaEkrani` paylaşılan `SnackbarHostState` ve `GeriYuklemeOnizlemesi` öğretmen grup/soru sayılarıyla bağlandı.
- **Geometrik & görsel regresyon testleri:** `GorselButunlukVeYerlesimArayuzTest` ile `PremiumYanlisDefteriTema` (Açık/Koyu) altında `SoruGorselTamEkran` sıfır olmayan piksel boyutları, `OdakOturumuEkrani` ve `GorselBakimEkrani` dikey çakışmasızlık (`boundsInRoot`), Deneme `gizliKaynak` konum alanı, Öğretmen `Snackbar` + ZIP4 önizleme sayıları, `1.6x` yazı ölçeği ve Manrope TTF `cmap` tablosu doğrulandı.

## Önceki doğrulanmış teslim: 1.5.107 Klavye/IME Düzeltmeleri & Öğretmen Modu Premium Yükseltmesi

- **Sürüm:** `versionName = "1.5.107"` (`versionCode = 37`), çalışma dalı `arena/9d4e7eac-question-note`.
- **Klavye, `WindowInsets` ve sabit panel düzeltmeleri:** `AndroidManifest.xml` içinde `.MainActivity` ve `.ui.premium.PremiumDemoActivity` için `android:windowSoftInputMode="adjustResize"` tanımlandı. `DenemeDetayEkrani` üzerinde `Scaffold(contentWindowInsets = WindowInsets.safeDrawing)` ile birlikte kullanılan mükerrer `.imePadding()` kaldırılarak klavye açıldığında listenin çökmesi ve alanların üst panelin arkasında kalması giderildi; tüm ekran kapsayıcılarına (`YanlisDefteriUygulamasi`, `OgretmenAnaEkrani`, `DenemeEkranlari`, `SoruFormuEkrani`, `DersCalismaEkrani`, `FiltreliCalismaEkrani`, `KonuMerkeziEkrani`, `SorulacaklarEkrani`) tutarlı `.consumeWindowInsets(...)` eklendi. Klavye açıkken `AltMenu`, `OgretmenAltMenu` ve soru formu sabit alt çubukları otomatik gizlenir; `DersCalismaEkrani` arama alanı ve üst kart kaydırılabilir `LazyColumn` içine taşındı.
- **Rol Seçimi & Öğretmen Modu Premium yükseltmesi (`RolSecimEkrani`, `OgretmenAnaEkrani`):** Manrope glif düşüşü yaratabilecek ham semboller kaldırılarak özel `Canvas` vektör ikonlu `OgretmenAltMenu` ve rol amblemleri eklendi; `HareketPolitikasi` uyumlu ekran geçişleri, canlı tablo rakamlı (`tnum`) `GradyanKart` özet paneli, renk/monogram kodlu grup kartları, grup ve soru içi arama, `Dialog` + `LazyColumn` tabanlı klavye-güvenli grup/kopyalama pencereleri, çift dokunuşla yakınlaştırma + `1x Sıfırla` destekli ve varsayılan gizli cevaplı tam ekran sunum görünümü sunuldu.

## Önceki doğrulanmış teslim: 1.5.106 Görsel Kırpma, Öğretmene Sorulacaklar & Öğretmen Modu

- **Sürüm:** `versionName = "1.5.106"` (`versionCode = 36`), çalışma dalı `arena/9d4e7eac-question-note`.
- **Görsel kırpma kararlılığı ve UX:** `SoruGorselBileseni` ve `GorselKirpmaDialog` içinde `asImageBitmap()` ile sarılan `Bitmap` örneklerinde erken `recycle()` kaldırılarak `RenderThread` çökmesi giderildi; `LocalSoruGorselDeposu.kirp` bellek-güvenli alt-örneklemeli çözme ve OOM korumasıyla güçlendirildi; `GorselSecimBolumu` bileşeninde `✂ Görseli kırp` düğmesi her görsel kartının altına ortalanmış birincil konumda sunuldu.
- **Öğrenci — Öğretmene Sorulacaklar (`SoruPlanDurumu.OGRETMENE_SORULACAK`):** Bu durumdaki sorular `sonrakiTekrarGun = null` ile günlük tekrardan ayrı tutulur, `SorulacaklarEkrani` içinde ders ders gruplanır, `Bugün` ekranında hatırlatıcı kartla gösterilir, tam ekran yakınlaştırılabilir sunumla açılır ve `SoruRepository.ogretmeneCozduruldu` ile (çözüm fotoğrafıyla veya fotoğrafsız) ertesi gün (`bugün + 1`) aktif tekrar planına geçirilir.
- **Rol Seçimi & Öğretmen Çalışma Alanı (`KullaniciRolu`, `OgretmenDeposu`):** İlk kurulumda `RolSecimEkrani` (`Öğrenciyim` / `Öğretmenim`) gösterilir; mevcut kurulumlu kullanıcılar geriye dönük uyumlu biçimde `OGRENCI` olarak devam eder ve `Ayarlar` ekranından modlar arasında geçiş yapabilir. Öğretmen modu (`OgretmenAnaEkrani`) XP/rütbe/tekrar motorundan bağımsızdır; grup bazlı `Çözmediklerim` / `Çözdüklerim`, zorluk filtresi, tam ekran yakınlaştırmada `Cevabı göster` gizliliği ve bağımsız çözüldü durumuyla `Başka Gruba Ekle` kopyalamasını destekler.
- **Korunan şema ve yedekleme sözleşmeleri:** Room şeması `4` ve ZIP yedek formatı `4` (`REQUIRED_CSV_FILES` değişmedi). Kalıcı öğretmen görselleri `filesDir/ogretmen-img/<uuid>.jpg` altında referans sayımlı tutulur (öğrenci yetim görsel taramasından izoledir) ve ZIP yedeklerde `data.json -> teacher_workspace` + `manifest.json -> counts.teacher_images` + kurtarma günlüğü (`geri-yukleme.json`) ile tam atomik yedeklenir/geri yüklenir.

## Önceki doğrulanmış teslim: 1.5.105 Deneme & İlk Kurulum Premium Yükseltmesi

- **Sürüm:** `versionName = "1.5.105"` (`versionCode = 35`), çalışma dalı `arena/9d4e7eac-question-note`.
- **Doğrulanan test kapsamı:** Debug `332/0/0/0`, Release ortak `269/0/0/0`; `assembleRelease`, `apksigner` doğrulaması ve beklenen sertifika parmak izi eşleşmesi başarılı.
- **Korunan şema sözleşmeleri:** Room şeması `4` ve ZIP yedek formatı `4` (`REQUIRED_CSV_FILES` değişmedi). `data.json` ve kurtarma günlüğü (`geri-yukleme.json`) içinde isteğe bağlı `"mock_exams"` alanı geriye dönük uyumlu biçimde desteklenir; geri yüklemede deneme ders kimlikleri ders adına göre uzlaştırılır.
- **Özel ders görünürlüğü (`SinavProfili.OZEL`):** `SinavProfiliDeposu` hem `dersler` kimlik kümesini hem `ozel_ders_adlari` ad kümesini saklar; farklı cihazdan yedek geri yüklendiğinde seçili dersler ad üzerinden yeniden eşleştirilir ve ders ekranı asla tamamen boş kalmaz.
- **Deneme–Kaynak senkronizasyonu:** Deneme oluşturma/yeniden adlandırma `SoruRepository.kaynakAdiDegistir` ile kaynak kataloğunu ve mevcut deneme sorularının `kaynak` alanını atomik günceller; `RoomSoruRepository.soruKaydet` genel katalog sayısını bozmaz.
- **Premium arayüz ve ton:** 24 derslik katalog için tam açık/koyu renk ve tipografik sembol eşlemesi (`dersKartStiliGetir`), cam/gradyan kartlı `DenemeListesiEkrani` ve `DenemeDetayEkrani`, negatif net destekli grafikler, gruplanmış `DersKurulumEkrani`, Bugün ekranında sakin Deneme özet kartı ve destekleyici Türkçe metin dili uygulandı.

## Önceki temel: 1.5.1 sade İlerleme

**v1.5.1 / code19 yayımlandı.** [Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.5.1) · [APK](https://github.com/keremtulu44/Question-note/releases/download/v1.5.1/yanlis-defteri-1.5.1.apk) · [CI `39158221022`](https://github.com/keremtulu44/Question-note/actions/runs/39158221022)
**Debug312/0/0/0 · Release258/0/0/0**; APK/imza/sertifika başarılı. Ortak testler iki varyantta tekrar eder, sayılar toplanmaz.
Kaynak/tag `c25b32bce6c20b0f2c39ff27af6990d13c300074`; asset **654278786**, **10.819.807 bayt**; yayımlanma `2026-10-18T19:22:49Z`.
SHA-256 `c8d1d45cfd7ad55ab6d3ebba6bf52464fd772d16d23929cbcb406fe8b2a9a053`; GitHub asset digest ve tag referansı CI ile bağımsız eşleştirildi.

- Kullanıcının fazla metin/karmaşıklık düzeltmesi uygulandı: rütbe, günlük hedef, tek eylem;
  Koleksiyon/Ayrıntılar ayrı sayfalarda. Yeni veri, puan, şema veya tekrar kuralı yok.
- %200 alt menü etiketleri düzeltilmiş; tüm sekmeler adlandırılmış ve 48dp üzerinde. Geri,
  saved-state, yenileme, gerçek sayılar, filtre→Odak→karne ve gizli rozet sözleşmeleri testli.
- Sekiz native önizleme incelendi. Son büyük-yazı fixture’ı üretimdeki inset düzeniyle aynı;
  eylemin alt menünün tamamen üstünde olması katı geometrik kontrolden geçer.
- [Değişiklik/kanıt](docs/ILERLEME_SADELESTIRME.md) · [Native görüntüler](docs/ui/progress/README.md).
- **Etkin v3 kapalı**, Gölge v2 tarihini uygular. Fiziksel S24 FE/TalkBack/SAF/süreç ölümü/frame-p95 açık.
- Yayın bayrağı açık. Sonraki kaynak değişikliğinde yeni sürüm/code veya kapalı ara yayın kullan;
  doğrulanmış 1.5.1 APK’sını yeniden yazma. Merge/PR yapılmadı.

## Önceki doğrulanmış teslim: 1.5.0

- Kullanıcı: **“P4 P5 ve P6 uygula dikkatli adım adım çalış.”** P0–P3 önceki teslimde tamamlandı.
- P4 CI `39139377311` / `35896fe`: **240/0/0/0**. P5 CI `39140443680` / `522b009`: **274/0/0/0**.
  Her ikisinde release derlemesi/imza/sertifika başarılı; ara yayın bilinçli kapalıydı.
- **P6 son yayın CI `39144684877` / `9c3c51f`: Debug298/0/0/0, Release258/0/0/0**; APK derleme,
  imza/sertifika ve Release başarılı. İlk birleşik yeşil koşu `39143916007` / `478b86c` idi; final aynı kontrolleri yeniden geçti.
- **Yayımlanan v1.5.0 / code18**, `2026-10-18T16:24:15Z`. [Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.5.0) · [APK](https://github.com/keremtulu44/Question-note/releases/download/v1.5.0/yanlis-defteri-1.5.0.apk).
  Kaynak/tag commit'i **`9c3c51f34aa8419c2cab948601823f6fc58351e3`**, asset **653916834**, **10.791.135 bayt**.
  SHA-256 **`3e7158345d0e0c88bf03698b8ba21db1fedef6a123758de5e7b13f8e5a5470db`**. CI raporu ile GitHub asset digest, boyut/isim ve tag referansı bağımsız API sorgusuyla eşleştirildi.
- **Etkin v3 kapalı**; Gölge varsayılanı v2 tarihini uygular. Yayın bayrağı bu ayrı motor kapılarını açmaz.
- Host ölçümü: Debug sıcak medyan575,56 / host-p95 617,73ms; Release521,81 /604,30ms,
  tam Release İlerleme672,57ms. Dört ince SELECT, 22.749.680 bayt karar JSON'u projeksiyon dışında.
  Bu **telefon frame-p95 veya cold-start kabulü değildir**; örnekler/planlar iki otomatik raporda.
- Beş P6 PNG incelendi: açık/koyu/%200/boş/analiz, footer kontrastı ve gerçek eşleşen gün adetleri.
  Görüntüler native/sentetik; cihaz ekran fotoğrafı değildir. Fiziksel kabul açık.

## Çalışma sınırları

- Yalnız **`arena/59c6570d-question-note`**. Merge/PR yok, main'e dokunulmaz. Adım adım onay istenmez.
- Bash timeout'lu; uzun `gh run watch` yok. Sınırlı durum sorguları; beklerken ~25 s aralık.
- Java/Android SDK yerelde yok; gerçek Kotlin/Android derleme/test CI'da. Parser/statik başarı derleme değildir.
- CI botu aynı dala rapor/şema/küçük PNG yazar. `git fetch` + koruyan `rebase --autostash`; merge/reset yok.
- `testDebug`: tüm ortak testler + debug-only Compose; `testRelease`: ortak testler. **Sayılar toplanmaz.**
- Gerçek cihaz/SAF/TalkBack/süreç ölümü/frame-p95 henüz yapılmadı. Native sentetik çizim/benchmark telefon kabulü değildir.

## Korunan P2/P3 temeli

- `AktifSureOlcer`: elapsedRealtime segment farkları, UI tick'i yalnız gösterim. Deneme/inceleme ayrı;
  çözüm ilk açılınca faz geri dönmez. Değerlendirme dondurur; loading/mola/arka plan/kaydetme/zoom hariç.
- YOK/TAM/KISMI/GECERSIZ; eski null ≠ 0; faz başına 24 saat sınırı. Tam ekran/eksik görsel/kurtarma kısmi.
  Ölçüm hatası geçerli sonuç/XP kaydını engellemez.
- Checkpoint2: kimlik/birikim/seçim/faz; monotonik başlangıç yazılmaz. 1 s gösterim/5 s aktif IO;
  revizyon ve sahiplik eski işin yeni oturumu diriltmesini önler. Çözüm görünmeden açma işareti kalıcıdır.
- Tekrar + ölçüm/bağlam/karar + plan tek transaction; deneme ID idempotensi ve gerçek same-day kontrolü.
  Önizleme gün/saat dilimi/politika/geçmiş/bağlam/profil değişince açık yenilenir.
- Commit–checkpoint kesintisi deneme/oturum kimliğiyle uzlaşır; ilk checkpoint gecikse/duvar saati geri gitse
  de tamamlanan tekrar tanınır. Eski kimliksiz kayıt zaman fallback'i sürer. Prefs SQLite transaction'ı değildir.
- Profil politikası1: 60 gün, mevcut deneme hariç, pozitif TAM. Yardımsız Rahat ile tüm-sonuç iş yükü ayrı.
  Aynı soru/gün tek, sonra soru başına medyan; medyan/çeyrek/MAD. Ders **20/10/5**, konu/kaynak **30/15/7**.
  Yalnız aynı ders fallback'i; az veride null. NFC/Türkçe harf/ASCII iç boşluk normalizasyonu korunur.
- Kayıt anı bağlamı değişmez; eski bilinmeyen bağlam uydurulmaz. Kalıcı profil/analitik cache yok.

## P4 — uygulanan korumalar

- Ayrı `UyarlanabilirTekrarKurali`; v2 temel, v3 aday. Gölge v2 uygular; Etkin için iki default-false kapı.
  Kapalı snapshot1/V2, Gölge/Etkin snapshot2. Prefs kapı aşamaz; test fixture'ı açık kapıyı ayrıca kurar.
- Yakın en fazla 5 ayrı gün/90 gün; günün son kaydı, iki son bağımsız başarı güçlüğü kaldırır.
- Düzeltme yalnız güncel yardımsız Rahat + TAM/pozitif + ≥2 yakın güçlük günü + yeterli finite referans:
  süre >1,5×medyan **ve** >medyan+max(3MAD,1,5IQR). Hız bonusu/yavaşlık-tek-başına ceza yok.
- Çözüm/fail 1 gün. Gecikme/Zorlandım düzeltmesiyle birikmez. `floor(base/10)` indirim; 1–180 gün.
- Açık geçmiş1/2/3 yönlendirmesi; v3 ease okunur. Rollback yalnız yeni kayıtları v2 yapar; geçmiş yeniden yazılmaz.

## P5 — uygulanan analitik / eylem / ödül

- Gerçek farklı soru/tekrar/payda/kapsama; geçersiz süre geçerli sonucu paydadan çıkarmaz. Tarihsel grup
  adları, eksik ve bilinmeyen bağlam ayrı; kitap/öğrenci kalite sıralaması yok.
- Gerçek 7 gün ritim, bugün eksik. Tamamlanmış −14…−8 ve −7…−1; aynı soru/bağlamın son kaydı eşleştirilir.
  ≥10 ortak soru ve son değerlendirmelerde her pencerede ≥3 gün; gerçek gün sayısı görünür. Öğrenme artışı hükmü yok.
- Seçili profil için gerçek uygun plan; gecikenler bugün. Tüm sonuçlardan TAM Q1–Q3 iş yükü;
  ≥%80 kapsamada yaklaşık toplam, altında yalnız kapsananların ara toplamı; sıfır kapsamada dakika yok.
- Vade → uygun yakın güçlük grubu → isteğe bağlı küçük tur. Görünür ders/vade/kaynak filtresi, fresh start/save
  kontrolü; askı/ders arşivi/profil/same-day sınırı aşılmaz. Hedef otomatik artmaz.
- Tüm arşiv XP/analitiği profille gizlenmez. Grup listesi altılı sayfa, ayrıntılar kapalı; sonlu/reduced motion.
- `RutbeDeposu` kalıcı üst eşik/tek birleşik bekleyen olay; startup/hedef/restore sessiz taban.
  Sahiplik korunur, rütbe XP vermez. Badge-priority `KutlamaHost`: iki fullscreen üst üste gelmez.

## P6 — kod / gözlenen kanıt / açık kapılar

- UI testleri `src/testDebug`; Release'e fixture/test Activity taşınmadı. Katı JUnit özetleri ayrı.
  CI test cache/up-to-date kapalı; benchmark/PNG her koşunun kendi çıktısı olmalı.
- `MotorTaniHizmeti`: toplam ile zaman damgasına göre son ≤100 örnek ayrı; strict doğrulama, farklı aday/
  Gölge/Etkin/V2/bozuk sayısı. Count + 1–2 sınırlı Room parent + tek batched relation; N+1 yok.
- Analiz4 ince SELECT; karar JSON'u/görsel/not yok. SQLite EXPLAIN mevcut ders/gün/PK/same-day indekslerini
  gösterir. Ana iş parçacığı dışında saf hesap, DB snapshot tutarlılığı korunur; fiziksel maliyet ölçülmedi.
- Native SDK35 host benchmark: 1.000 soru/20.000 tekrar, 5 sıcak örnek, profil/tam özet maliyeti.
  `docs/PERFORMANS_DEBUG.md` kaynak SHA ve gerçek ham süreleri içerir. `docs/PERFORMANS_RELEASE.md` de gerçek ölçüm içerir; her rapor kendi kaynak SHA’sını taşır.
- Birleşik yeşil Debug 298/0: öneri→Filtreli→Odak→Karne→İlerleme; %200/48dp/reduced-motion, tek host, politika gate;
  broadcast, gece yarısı ve Activity resume; bounded tanı; ZIP snapshot2/active3 rollback/tamper;
  restore callback kesintisinde marker/kozmetik kuyruklar. Bunlar fiziksel süreç öldürme değildir.
- `docs/ui/p6`: 5 gerçek Compose/sentetik görüntü. Koyu fixture açıklamasının kontrastı, yeni panel yüzeyleri
  ve karşılaştırmadaki gerçek gün adetleri görüntü incelemesi sonrası düzeltildi.
- İlk P6 hataları ve düzeltmeleri saklanır: eksik test context'i; tek-sefer restore hatasının anında iyileşmesi;
  Room parent cursor tekrarını saymayan beklenti; Release görevinin başlayamaması (düzeltildi); UI testinde aynı adlı başlık/tab ve kaydırma/hazır olma sınırları. Koruma/test silinmedi.

## Sürüm / yedek / ürün değişmezleri

- **Room4**, additive 3→4; 1/2/3→4 ve şemalar korunur. **ZIP4**, v2/v3/v4 açık okuyucu.
  Ölçüm1, profil1, karar1/2, geçmiş algoritma1/2/3 ayrı kavramlar; yalnız sayı artırılarak uyumluluk iddia edilmez.
- ZIP hash/FK/null/kalite/sürüm/taşma/snapshot denetimi; eski satırlar değişmez. Rozet/rütbe/politika/hedef/
  sınav/checkpoint prefs'i dahil değil. Restore tam değişim + safety copy; senkronizasyon/birleştirme değil.
- Restore yalnız commit doğrulanınca checkpoint ve kozmetik kuyrukları marker kaldırılmadan temizler.
  Sahiplik kalır; anlık recovery de başarısızsa marker korunur. Precommit hata eski oturumu korur.
- Offline tek kullanıcı, hesap/sunucu yok. Alt menü5; Ayarlar'da profil/konu yönetimi/yedek.
- Bağımsız = Rahat + açılmamış çözüm; Zorlandım yarım, Çözemedim çözemedi; açık çözüm destekli.
- XP soru10/tekrar5/bağımsız+5/hedef25/rozet50, seri3/7/30=30/70/300. Hedef5/10/15/20/30; 15 rütbe/30 rozet/5 sır aynı.
- Geçmiş karne arşivi, mola günü jetonu ve bildirimler bu pakette yok.

## Sonraki somut adım

Kod/otomatik teslim tamamlandı, v1.5.0 yayımlandı; merge/PR yapılmadı.

1. Fiziksel `P6.D1–D13` kabulünü gerçek S24 FE/TalkBack/SAF ve mümkünse daha düşük güçlü cihazda yürüt.
2. Gerçek süreç ölümü/reboot/düşük depolama ve cihaz frame-p95 sonuçlarını host ölçümüyle karıştırmadan kaydet.
3. Gerçek gölge fark/neden/eksik veri/yuvarlama/toparlanma örneklerini uzunlamasına incele.
4. Bu iki kabul olmadan etkin v3 kapılarını açma. Sorunda Ayarlar → Kapalı/v2; eski APK'ya DB downgrade yapma.
