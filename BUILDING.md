# BUILDING — Derleme, İmzalama ve APK Erişimi

> Bu belge **değerleri değil**, sırların nerede ve nasıl tutulduğunu anlatır.
> Hiçbir anahtar, parola veya token bu depoya yazılmaz.

- Son güncelleme: 2026-10-30
- Son resmî sürüm: **v1.6.3 (versionCode 42)**. 1.7.0 önizlemesi (43) yayımlandı;
  1.7.1 (44) birleştirilmiş önizleme adayıdır. **Debug 384/0/0/0**, **Release 303/0/0/0**; Release derlemesi, imza ve sertifika doğrulandı.
- [Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.6.3) · [APK indir](https://github.com/keremtulu44/Question-note/releases/download/v1.6.3/yanlis-defteri-1.6.3.apk).
  APK **13.706.249 bayt**; SHA-256 `787690fca609207a057b258c22dfb16f0e205cdec5bef237656c35ed5cad7218`.
  Derlenen commit `9e9df5b27801dbd3bb90b9b4bbac76f184426e5f`; sertifika SHA-256 `FA:13:DE:D7:3C:B4:6E:DE:9E:36:37:47:EE:38:95:F2:B6:37:14:9C:16:57:90:A2:E4:67:78:F1:30:89:DE:54`.
- Uygulama kimliği aynı; Room v5 / ZIP v5, geçmiş/snapshot uyumluluğu korunur.
- P0–P5 ve P6 otomatik kalite kapıları yayımlandı.
  Varsayılan **Gölge v2 tarihi uygular; etkin v3 kapalıdır**. XP/30 rozet/15 rütbe katsayıları aynı.
- **Fiziksel cihaz kabulü ayrıdır**; otomatik testin yeşil olması gerçek cihaz testi yapıldığı anlamına gelmez.
  Ayrıntı `docs/TEST_KONTROL_LISTESI.md`; host benchmark telefon/frame/cold-start ölçümü değildir.

---

## Varyant ve kanıt kapısı

- CI iki iştir: **`test`** (PR + `main`/`v*` push + `[apk]` istekli dal; salt-okunur token,
  imza secret'ı yok) ve **`release`** (test geçmeden başlamaz). `main`/`v*` push resmî yayın,
  `[apk]` istekli dal yalnız önizleme; elle tetikleme de yalnız önizleme/artefakt üretir. Güncel
  sonuç ve kaynak: [DERLEME_RAPORU.md](docs/DERLEME_RAPORU.md) (yalnız `main` release
  çalıştırmalarında güncellenir; PR kanıtı Actions artifact + step summary'dedir).
- Debug başarısız olsa da Release testi çalışır; APK için **ikisi de** geçmelidir. Sıra: `test` işinde `:app:testDebugUnitTest` → `:app:testReleaseUnitTest`; `release` işinde `:app:assembleRelease` →
  imza/sertifika → yayın. Ortak testler iki varyantta; UI/Preview `src/testDebug`'da, sayılar toplanmaz.
- `androidComponents` host UnitTest seçimi açık. CI test sonuçları cache/up-to-date ile atlanmaz;
  derleyici/dependency cache korunur. Her benchmark/önizleme o koşuda üretilir.
- `tools/junit_ozeti.py` eksik/sıfır rapor, başarısız/hatalı/atlanan test ve zorunlu test sınıfının
  keşfedilmemesini reddeder. Derleme hatası, “0 test başarılı” diye geçilmez.
- Ara `PUBLISH_RELEASE=false`, başarılı imzalı ara APK'yı yayımlamaz. İki varyant `39143916007` ile
  birlikte doğrulandı; nihai paket bayrağı açıldı, `39144684877` aynı kapıları yeniden geçerek yayımladı. **Bu yayın bayrağı, ayrı olan etkin-v3 kapılarını açmaz.**
- Küçük native PNG'ler `docs/ui/progress` (sekiz güncel ekran) ve `docs/ui/p6`, sentetik host ölçümleri `docs/PERFORMANS_DEBUG.md` ve
  `docs/PERFORMANS_RELEASE.md`. Büyük veri/ZIP/APK Git'e girmez.

## 1. Genel akış

```
Kod değişikliği (yapay zekâ, GitHub)
        ↓
GitHub Actions: push ile otomatik derleme
        ↓
Sabit release anahtarı ile imzalanmış APK
        ↓
GitHub Release (kalıcı indirme bağlantısı)
        ↓
Telefondan indir → üzerine kur (veriler korunur)
```

Kullanıcının telefonunda Android SDK, Gradle veya Termux kurması **gerekmez**.

## 2. Derleme ortamı

- Tek Gradle modülü, Kotlin + Jetpack Compose.
- Sürümler `gradle/libs.versions.toml` içinde sabitlenir; Gradle wrapper depoda tutulur.
- minSdk 26, compileSdk/targetSdk 37; yayımlanmış v1.6.3 `versionCode=42` (v1.6.2: 41; v1.6.1: 40; v1.6.0: 39).
- Yapay zekâ ortamında Java/Android SDK ve Maven erişimi yoktur → derleme **yalnızca** Actions'ta olur.

## 3. İmzalama düzeni (kritik)

**Neden:** GitHub Actions her çalıştırmada sıfırdan başlar. Sabit bir anahtar verilmezse her derleme
farklı anahtarla imzalanır; Android yeni APK'yı kurulu uygulamanın **devamı olarak kabul etmez** ve
kurulumu reddeder. Tek çare eski uygulamayı kaldırmaktır ki bu **telefondaki tüm veriyi siler**.

**Kural:** Sabit bir release anahtarı oluşturulur ve GitHub'da **şifreli Secret** olarak saklanır.
Anahtarın değeri repoda, workflow dosyasında veya belgelerde asla bulunmaz.

Secret'lar (değerler yalnızca GitHub'da tutulur; **yalnızca 2 alan** gerekir):

| Secret adı | İçerik |
|---|---|
| `SIGNING_KEYSTORE_BASE64` | Keystore dosyasının base64 kodlanmış hâli (3404 karakter, tek satır) |
| `SIGNING_PASSWORD` | Keystore **ve** anahtar parolası (PKCS12'de aynı) |

Takma ad (`yanlisdefteri`) gizli bilgi olmadığı için workflow içinde sabit yazılır.

Secret'lar **yalnızca kullanıcı** tarafından eklenebilir: Settings → Secrets and variables → Actions →
New repository secret. Adımlar `anahtar/KURULUM_ADIMLARI.md` içinde (git dışı). Yapay zekânın token'ı
secret uçlarına erişemiyor (403) ve **bir workflow kendi secret'ını oluşturamaz** (GitHub güvenlik
kuralı) — bu yüzden bu adım elle yapılır. Kullanıcı iki secret'ı ekledi; G3 CI keystore'u çözdü,
APK'yı imzaladı ve beklenen sertifika SHA-256 parmak izinin aynısını doğruladı.

**Kullanıcının sorumluluğu:** Anahtar dosyası ve parolası, telefondan bağımsız **iki ayrı yerde**
saklanır (ör. şifre yöneticisi + Drive'da bir klasör). Anahtar kaybedilirse:

- Yeni APK'lar mevcut kurulumun üzerine kurulamaz.
- Tek çare kaldırıp yeniden kurmaktır → yerel veri silinir; bu yüzden güncel bir ZIP yedeği şarttır.

**Not:** Debug imzasına güvenilmez; CI'da üretilen geçici debug anahtarı kalıcı test güncellemeleri
için uygun değildir.

## 4. Actions kullanımı

- Derleme/test **otomatik** çalışır: `main` push ve `v*` etiketlerinde iki iş de, pull request'lerde
  yalnız `test` işi (yalnızca workflow'da izlenen dosyalar değişince). Dal commit mesajında `[apk]`
  açıkça istenirse iki testten sonra yalnız imzalı önizleme üretilir; resmî etiket oluşturulmaz.
  Elle `workflow_dispatch` de yalnız önizleme veya artefakt sunar.
- Token en az yetkiyle bölündü: üst düzey `permissions: contents: read`; yalnız `release` işi
  `contents: write` alır (Release oluşturma + rapor commit'i). İmza secret'ları da yalnız
  `release` işinin imza adımındadır; testler secret'sız ortamda çalışır.
- **Log okuma deseni:** `main` release çalıştırması özet çıktısını (sürümler, APK SHA-256,
  imza/parmak izi ve hata metni) **`docs/DERLEME_RAPORU.md` dosyasına yazar**; PR çalıştırmalarında
  aynı kanıt Actions **artifact**'ında (`test-kaniti`) ve **step summary**'de yer alır.
- Kotlin/Compose derlemesi hatasız bitmeden "APK hazır" denmez; başarısız derleme için Release
  oluşturulmaz.
- GitHub Actions kotası ve kullanım koşulları kurulum sırasında kontrol edilir; sınırsız/ücretsiz
  olduğu varsayılmaz.
- Depo public olduğu sürece standart runner'larda dakika sınırı uygulanmaz; depo private'a alınırsa
  ücretsiz planda aylık 2.000 Linux dakikası ve 500 MB artifact alanı bulunur. Bu proje için ikisi de
  yeterlidir.
- Artifact **saklama süresi kalıcı değildir** (varsayılan 90 gün, public repoda azami 90 gün). Bu
  nedenle kalıcı dağıtım **Releases** üzerinden yapılır; artifact yalnızca ara çıktıdır.

### Release çıktısının izlenebilirliği

Her Release'de şunlar bulunur:

- APK dosyası (`yanlis-defteri-<sürüm>.apk`)
- `versionName` ve `versionCode`
- Derlenen Git commit kimliği
- APK'nın **SHA-256** özeti (indirilen dosyanın gerçekten üretilen sürüm olduğu doğrulanabilir)
- Sertifikanın **SHA-256** parmak izi (imzalama kimliği doğrulanabilir)

**Debug ve release APK'ları karıştırılmaz.** Günlük kullanımda her zaman sabit anahtarla imzalanmış
release APK kullanılır; Actions'ın ürettiği test/debug çıktıları yalnızca geliştirme amaçlıdır ve
günlük arşivin kurulu olduğu telefonun üzerine kurulmaz.

## 5. APK'ya telefondan erişim

**Güncel:** [v1.5.92 APK'yı indir](https://github.com/keremtulu44/Question-note/releases/download/v1.5.92/yanlis-defteri-1.5.92.apk) · [Release ayrıntıları](https://github.com/keremtulu44/Question-note/releases/tag/v1.5.92).
Aynı uygulama kimliği ve imza ile güncelleme olarak kur; güvenlik için güncelleme öncesi Ayarlar'dan ZIP yedeği almak yine önerilir.

İlk sürümlerin tarihsel Release kayıtları:

- **v1.0.0 (versionCode 1):** [Release v1.0.0](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.0) — G2'nin arayüzsüz ilk imzalı sürümü.
- **v1.0.1 (versionCode 2):** [Release v1.0.1](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.1) — açık/koyu temalı 7 ders kartı.
- **v1.0.2 (versionCode 3):** [Release v1.0.2](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.2) — Aşama 2/3 kodu. APK SHA-256: `264a15082587d3ed4883c783d3d94fea964d0472cf857e802fac1276f77729ac`.
- **v1.0.3 (versionCode 4):** [Release v1.0.3](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.3) — Bugün ana ekranı, yan menü ve tekrar sonucu kaydı; kullanıcı cihaz adımlarını geçti bildirdi. APK SHA-256: `4641aff60fb38aeb114c765a9721d50849156e9f094b9b99662807cc41724108`.
- **v1.0.4 (versionCode 5):** [Release v1.0.4](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.4) · [APK indir](https://github.com/keremtulu44/Question-note/releases/download/v1.0.4/yanlis-defteri-1.0.4.apk) — Soru ayrıntısında tekrar geçmişi; yeni history UI cihaz kabulü bekliyor. CI APK SHA-256: `bf6978a506a472834d7c7a952ac59818d69b0d9c205772d4c02ce2767cc27468`.

### G5 — telefonda güncelleme zinciri (**GEÇTİ**, kullanıcı bildirimi 2026-10-16)

Kullanıcı v1.0.0'dan v1.0.1'e uygulamayı kaldırmadan güncelledi; uygulama açıldı ve dersler göründü.
Bu, kurulum ve güncelleme zincirinin kullanıcı cihazında doğrulandığını gösterir. Release bağlantıları
ve sürüm ayrıntıları aşağıda korunmuştur.

Android ilk kurulumda "bilinmeyen kaynaklardan kurulum izni" isteyebilir; izni yalnızca kullandığın
indiriciye ver. Release APK doğrudan `.apk` dosyasıdır, ZIP değildir. Depo public olduğu sürece
Release dosyalarını bağlantıya sahip herkes görebilir.

**Artifact indirme (yalnızca gerekirse):** Actions artifact'i ZIP olarak iner; içinden APK çıkarmak
gerekir. Telefonda ek adım gerektirdiği için birincil yol Releases'tir.

## 6. Sürümleme kuralları

- Uygulama kimliği (applicationId) **sabit kalır**: `com.keremtulu.questionnote` (2026-10-16'da
  sabitlendi). Sonradan değiştirilirse Android bunu **yeni bir uygulama** sayar; mevcut veri taşınamaz.
- Her APK'da `versionCode` artırılır, `versionName` kullanıcıya gösterilir.
- Veritabanı değişiklikleri **veri koruyan migration** ile yapılır; hatalı geçişte veritabanını
  otomatik silen yaklaşımlar normal güncelleme çözümü olarak kullanılmaz.
- Yedek formatı sürümlenir; eski yedekleri okuma politikası [docs/YEDEK_FORMATI.md](docs/YEDEK_FORMATI.md)
  içinde belgelenir.
- Kullanıcıya yansıyan değişiklikler [CHANGELOG.md](CHANGELOG.md) dosyasına işlenir.

## 7. Sırlar ve güvenlik

- Anahtar, token ve parola **repoya yazılmaz**; `.gitignore` keystore ve yerel yapılandırma
  dosyalarını dışlar.
- İmzalama anahtarı kaynak koddan ayrı tutulur ve ayrıca yedeklenir.
- Telefonun sistem yedeği tek güvenlik mekanizması olarak görülmez.
