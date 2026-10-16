# BUILDING — Derleme, İmzalama ve APK Erişimi

> Bu belge **değerleri değil**, sırların nerede ve nasıl tutulduğunu anlatır.
> Hiçbir anahtar, parola veya token bu depoya yazılmaz.

- Son güncelleme: 2026-10-16
- Durum: Aşama 0. Derleme altyapısı henüz kurulmadı; bu belge planlanan düzeni tanımlar.

---

## 1. Genel akış

```
Kod değişikliği (yapay zekâ, GitHub)
        ↓
GitHub Actions: elle tetiklenen derleme (workflow_dispatch)
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
- minSdk 26; targetSdk ve sürüm kodu Aşama 1'de sabitlenir.
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
kuralı) — bu yüzden bu adım elle yapılır.

**Kullanıcının sorumluluğu:** Anahtar dosyası ve parolası, telefondan bağımsız **iki ayrı yerde**
saklanır (ör. şifre yöneticisi + Drive'da bir klasör). Anahtar kaybedilirse:

- Yeni APK'lar mevcut kurulumun üzerine kurulamaz.
- Tek çare kaldırıp yeniden kurmaktır → yerel veri silinir; bu yüzden güncel bir ZIP yedeği şarttır.

**Not:** Debug imzasına güvenilmez; CI'da üretilen geçici debug anahtarı kalıcı test güncellemeleri
için uygun değildir.

## 4. Actions kullanımı

- Derleme **push ile otomatik** çalışır (`on: push: branches: [main]`). Ölçülen neden: yapay zekânın
  token'ında elle tetikleme (`workflow_dispatch` çağrısı) izni yok (403) ama push ile otomatik
  tetikleme için özel izin gerekmiyor. Kullanıcı isterse GitHub arayüzünden "Run workflow" düğmesini
  de kullanabilir (kendi hesabıyla).
- Workflow'un **kendi token'ı** `permissions: contents: write` ile repoya yazabiliyor; bu yetki
  **Release oluşturmak için yeterlidir** (G1'de commit atarak kanıtlandı).
- **Log okuma deseni:** derleme logları dışarıdan indirilemiyor; bu yüzden her iş akışı ölçüm/özet
  çıktısını (sürümler, dosya listesi, APK SHA-256, hata metni) **repoya yazar**
  (ör. `docs/ORTAM_RAPORU.md`, ileride `docs/DERLEME_RAPORU.md`).
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

**Debug ve release APK'ları karıştırılmaz.** Günlük kullanımda her zaman sabit anahtarla imzalanmış
release APK kullanılır; Actions'ın ürettiği test/debug çıktıları yalnızca geliştirme amaçlıdır ve
günlük arşivin kurulu olduğu telefonun üzerine kurulmaz.

## 5. APK'ya telefondan erişim

> Not: İlk CI denemelerinde APK, Actions **artifact** olarak da üretilir (indirmek için ZIP'ten
> çıkarmak gerekir). Kalıcı yol her zaman **Releases**'tir.

1. GitHub'da deponun **Releases** bölümü açılır.
2. En son sürümdeki `.apk` dosyasına dokunulur (bu doğrudan APK'dır, ZIP değildir).
3. İndirme tamamlanınca dosya açılarak kurulum yapılır. Android ilk seferde "bilinmeyen kaynaklardan
   kurulum izni" isteyebilir; bu izin yalnızca kullanılan tarayıcı/dosya yöneticisi için verilir.
4. Mevcut uygulama **kaldırılmaz**; kurulum doğrudan üzerine yapılır ve veriler korunur.
5. Depo public olduğu sürece Release dosyalarını bağlantıya sahip herkes görebilir; depo private
   yapılırsa yalnızca yetkili hesaplar erişir.

**Artifact indirme (yalnızca gerekirse):** Actions çalıştırmasının altındaki artifact bir **ZIP**
olarak iner ve içinden APK çıkarılması gerekir. Telefonda bu ek adım zahmetli olduğu için birincil
yol değildir.

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
