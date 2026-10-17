# BUILDING — Derleme, İmzalama ve APK Erişimi

> Bu belge **değerleri değil**, sırların nerede ve nasıl tutulduğunu anlatır.
> Hiçbir anahtar, parola veya token bu depoya yazılmaz.

- Son güncelleme: 2026-10-17
- Son yayımlanan sürüm: **v1.0.7 (versionCode 8)**. CI run `39041684003`: 37 test geçti; Release derlemesi, imza/apksigner doğrulaması ve sertifika fingerprint kontrolü başarılı. [Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.7); CI raporundaki APK SHA-256 `3d10f48d7c6db6ff246d0942fa958df974ac3c87e182b2627855f192035f99c4`.
- v1.0.7 ders bazında `+ Konu` akışı; v1.0.6 form yenilemesi ve v1.0.5 katalog/migration fiziksel cihazda henüz kabul edilmedi. v1.0.4 tekrar geçmişi, SAF/Drive yedek/geri yükleme ve fotoğraf okunabilirliği de cihaz testi bekliyor.

---

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
- minSdk 26, compileSdk/targetSdk 37; yayımlanmış v1.0.7 `versionCode=8` (önceki v1.0.6: `versionCode=7`). v1.0.7 CI sonucu yukarıda kayıtlı.
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

- Derleme **push ile otomatik** çalışır (`main` ve bu oturumun geliştirme dalı; yalnızca workflow'da
  izlenen dosyalar değişince). Elle tetikleme (`workflow_dispatch`) izni 403 olsa da gerekmez.
  G2/G3/G4 derlemeleri push ile çalıştı.
- Workflow'un **kendi token'ı** `permissions: contents: write` ile rapor commit edebiliyor; aynı izinle
  v1.0.0, v1.0.1, v1.0.2, v1.0.3 ve v1.0.4 GitHub Release'leri oluşturuldu.
- **Log okuma deseni:** derleme logları dışarıdan indirilemiyor; her iş akışı ölçüm/özet çıktısını
  (sürümler, APK SHA-256, imza/parmak izi ve hata metni) **`docs/DERLEME_RAPORU.md` dosyasına yazar**.
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

Yayımlanan Release'ler:

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
