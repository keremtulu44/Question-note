# KODLAMA PLANI (Aşama 1 ve sonrası)

> **Yeni geliştirme planı — 2026-10-18:** Premium arayüz, güvenilir süre ölçümü, kişisel profiller ve açıklanabilir tekrar kararları için [Gelişim ve Öğrenme Planı](GELISIM_VE_OGRENME_PLANI.md) esas alınır. Bu yeni işler henüz uygulanmadı. Aşağıdaki belge kurulum ve önceki aşamaların teknik/tarihsel kaydıdır.

> Bu belge **nasıl kodlayacağımızı** ve ölçülen sonuçları kaydeder: görev sırası, dosya yapısı,
> sürüm sabitleme, imzalama kurulumu, derleme bütçesi, test stratejisi. v1.0.8 filtreli çalışma akışı CI run `39044777998` ile doğrulandı ve yayımlandı; `+ Konu` koruması ile v1.0.5–v1.0.8 özelliklerinin fiziksel cihaz kabulü bekleniyor. v1.0.9 düzeltmeleri CI run `39048703584` ile doğrulandı; cihaz kabulü bekliyor.
>
> İlgili belgeler: [PROJECT_BRIEF.md](../PROJECT_BRIEF.md) (kararlar) · [ROADMAP.md](../ROADMAP.md)
> (aşamalar) · [BUILDING.md](../BUILDING.md) (derleme/imzalama) · [docs/VERI_MODELI.md](VERI_MODELI.md)
>
> Son güncelleme: 2026-10-17

---

## 0. Kodlama öncesi durum (ölçülmüş kısıtlar — G1, 2026-10-16)

| Bulgu | Durum | Sonuç |
|---|---|---|
| Sandbox'ta Java / Android SDK **yok** | Ölçüldü | APK yalnızca GitHub Actions'ta derlenir |
| `maven.google.com`, `repo1.maven.org`, `plugins.gradle.org`, `services.gradle.org` erişilemez | Ölçüldü (HTTP 000) | Bağımlılık çözümleme ve Gradle dağıtımı sandbox'ta yapılamaz |
| Sandbox'ta **openssl var**, `keytool` yok | Ölçüldü | Anahtar PKCS12 olarak burada üretilir (**üretildi**) |
| Arena token: depo içeriği / workflow dosyası yazma / PR | ✅ Var | Kod ve belge akışı doğrudan yürütülebilir |
| Arena token: **elle tetikleme (dispatch)** ve **secrets** | ❌ 403 | Dispatch gereksiz; kullanıcı secret'ları ekledi, G3/G4 CI ile doğrulandı |
| Workflow'un **kendi token'ı**: repoya commit / Release oluşturma | ✅ Var (G3/G4'te kanıtlandı) | İmzalı v1.0.0 ve v1.0.1 Release'leri yayımlandı |
| Derleme **loglarını indirme** | ❌ Engelli (log sunucusu erişilemez) | **Desen:** CI ölçüm/raporu repoya yazar, ben repodan okurum |
| CI ortamı | Ölçüldü: JDK 17 (8/11/21/25 de var), SDK build-tools ≤37.0.0, platforms ≤android-37.2, apksigner 0.9, Gradle 9.8.1 hazır | Sürüm seçimi bu aralığa göre yapılır |

**Sonuç:** Kod yazma hızımız CI turu sayısıyla sınırlı. Bu yüzden plan **küçük, doğrulanabilir
adımlara** bölünmüştür; tek seferde büyük kod yazıp log okuyarak hata ayıklamayacağız.

### Log okuma deseni (önemli)

`gh run view --log` çalışmıyor (log dosyası erişilemeyen bir sunucudan geliyor). Bu yüzden:

- Her iş akışı, ölçüm ve özet bilgisini (sürümler, dosya listesi, APK hash'i, hata metni) **repoya
  yazacak** (ör. `docs/ORTAM_RAPORU.md`, ileride `docs/DERLEME_RAPORU.md`).
- Derleme başarısız olursa, hata çıktısı da bu rapora yazılır → hata ayıklama repodan yapılır.
- Bu yaklaşım aynı zamanda "her derlemenin kanıtı repoda" ilkesini sağlar.

---

## 1. Kod yazma ilkeleri

1. **Tek modül, paket ayrımı.** Modül sayısı artırılmaz; sorumluluklar paketlerle ayrılır.
2. **Çerçeve yok.** DI için `AppContainer`, durum yönetimi için Compose + ViewModel; Hilt, çoklu
   modül, kod üreteci (annotation processor) sayısı minimumda.
3. **Sürümler sabit.** `gradle/libs.versions.toml` tek kaynak; Gradle wrapper repoda.
4. **Her şema değişikliği**: şema sürümü + migration + otomatik test. `fallbackToDestructiveMigration`
   asla.
5. **Her veri işlemi atomik olmaya çalışır**; dosya ve veritabanı ayrı olduğu için tutarlılık denetimi
   ayrıca yazılır (§5.3).
6. **Kullanıcıya görünen metinler Türkçe ve `strings.xml`'de.** Kodda gömülü metin yok.
7. **Kodda alan adları Türkçe, çatı terimleri İngilizce**: `Soru`, `Ders`, `Konu`, `Tekrar`
   (belgeler ve veritabanıyla tek sözlük), `Activity`, `Dao`, `Repository`, `Screen`/`Ekran`,
   Compose terimleri İngilizce. Böylece belge ↔ kod ↔ veritabanı aynı dili konuşur.
8. **Ölçmeden iddia yok.** Performans/boyut ifadeleri yalnızca ölçümden sonra yazılır.
9. **Her commit küçük ve tek amaçlı.** CI kırmızıysa yeni özellik eklenmez, önce düzeltilir.

---

## 2. Sürüm ve hedef belirleme (Aşama 1'in ilk teknik işi)

Sürümler **tahminle değil, derleme zamanında doğrulanarak** sabitlenir. İlk adaylar G2'de CI'da
ölçüldü; aşağıdaki tabloda başarılı kombinasyon gösterilir. Sandbox'tan Google Maven / Maven
Central'a erişilemese de GitHub CI derlemesi bu bağımlılıkları çözdü. (Ölçülen CI tabanı: JDK 17,
platforms android-34→37.2, build-tools 34.0.0→37.0.0, Gradle 9.8.1.)

| Bileşen | Sabitlenen sürüm | G2 CI sonucu |
|---|---|---|
| Android Gradle Plugin | **9.2.0** | `assembleRelease` başarılı |
| AGP built-in Kotlin + Compose compiler plugin | **2.4.0** (`org.jetbrains.kotlin.plugin.compose`) | Kotlin kaynağı derlendi; dış `org.jetbrains.kotlin.android` eklentisi kullanılmıyor |
| Compose BOM | **2026.09.00** | Bağımlılıklar çözüldü ve derleme başarılı |
| Activity-Compose | **1.13.0** | Derleme başarılı |
| Gradle wrapper | **9.8.1** | CI'da gerçek Gradle ile üretildi (Yol A), repoya yazıldı ve kullanıldı |
| JDK | **Temurin 17** | G2 CI derlemesi başarılı |
| compileSdk / targetSdk | **37 / 37** | G2 CI derlemesi başarılı; cihaz davranışı ayrıca test edilecek |
| minSdk | **26 (Android 8)** — kesin karar | Yapılandırıldı |
| Room | **2.8.4** | A2 şema v1 derlendi ve test edildi (CI) |
| KSP | **2.3.12** | Room kod üretimi CI'da başarılı |
| JUnit / Robolectric | **4.13.2 / 4.15.1** | SDK 35 hedefli 8 Room/repository testi geçti (run `38948106275`) |
| Coil, Lifecycle | Henüz eklenmedi | Görsel akışı/uygulama durumu aşamasında seçilip CI'da doğrulanacak |

**A2 CI ölçümü (2026-10-16):** Room `2.8.4` + KSP `2.3.12`, JUnit `4.13.2` / Robolectric `4.15.1` birleşimi run `38948106275`'te 8 testi geçti: schema/seed/FK, ders repository akışı/sayıları, dosya tabanlı DB açılışı ve konu ekleme/yeniden adlandırma/arşivleme/ilişki koruma. Release derlemesi ve imza doğrulaması başarılı. Gerçek cihaz testi yapılmadı; v1.0.1 Release mevcut olduğundan yeni Release yayımlanmadı.

**G2 ölçüm sonucu (2026-10-16):** İlk koşu `38924729686`, AGP 9.2.0'ın built-in Kotlin desteği
nedeniyle `org.jetbrains.kotlin.android` eklentisini reddetti. Yalnızca bu eklenti kaldırıldı;
AGP 9.2.0, Compose compiler plugin 2.4.0, Compose BOM 2026.09.00, Activity-Compose 1.13.0 ve
Gradle 9.8.1 korunarak ikinci koşu `38925064386` başarılı oldu. Detaylı kanıt:
[`docs/DERLEME_RAPORU.md`](DERLEME_RAPORU.md). Sürümler `gradle/libs.versions.toml` içindedir.

**Compose notu:** AGP 9'un built-in Kotlin desteği kullanılır; `org.jetbrains.kotlin.android`
eklentisi eklenmez. Compose için `org.jetbrains.kotlin.plugin.compose` sürüm kataloğunda sabitlenir.

### Gradle wrapper nasıl oluşturulacak?

Sandbox'ta Java olmadığı için `gradle wrapper` komutu buradan çalıştırılamaz. **Üç yol** var
(ilk ikisi G1'de ölçülen yeteneğe dayanır):

- **Yol A (tercih edilen):** CI'da tek seferlik bir iş, `gradle wrapper --gradle-version <sürüm>`
  çalıştırır ve üretilen `gradlew`, `gradlew.bat`, `gradle/wrapper/*` dosyalarını **repoya commit
  eder** (workflow token'ının yazma yetkisi olduğu G1'de kanıtlandı). Böylece wrapper, gerçek Gradle
  tarafından üretilmiş olur; elle hazırlanmış dosya riski kalmaz.
- **Yol B (yedek):** `gradle-wrapper.jar` + `gradlew` official `gradle/gradle` deposundan sabit bir
  etiketle alınır, `gradle-wrapper.properties` elle yazılır; jar'ın SHA-256'sı belgeye işlenir.
- **Yol C (son çare):** CI, `gradle/actions/setup-gradle@v4` ile Gradle'ı kurar ve wrapper'a hiç
  ihtiyaç duymadan derler. Repo kendi kendine yeterli olmaz; kabul edilmez ama derleme kurtarılır.

**Ölçülen seçim:** Yol A başarıyla uygulandı; Gradle 9.8.1 wrapper CI'da üretildi, repoya commit edildi ve sonraki derlemede kullanıldı. Yol B/C gerekmedi.

---

## 3. İmzalama kurulumu (Aşama 1'in en kritik adımı)

### 3.1 Anahtar üretimi ✅ (2026-10-16'da yapıldı)

`keytool` yerine PKCS12 üretildi (Java gerekmez). Ölçülen sonuçlar:

| Ölçüm | Değer |
|---|---|
| Algoritma / boyut | RSA **2048**, PKCS12, alias `yanlisdefteri` |
| Geçerlilik | 30 yıl (2056-10-08'e kadar) |
| Dosya boyutu | 2552 bayt → base64 **3404 karakter** |
| Karşılaştırma | RSA 4096 olsaydı: 4270 bayt → base64 5696 karakter (daha büyük yapıştırma) |
| Sertifika SHA-256 parmak izi | `FA:13:DE:D7:…:89:DE:54` (tamamı `docs/IMZALAMA.md`) |
| Yerel konum | `anahtar/` (git dışı): `yanlis-defteri.p12`, `PAROLA.txt`, `keystore.b64` |

**Neden RSA 2048:** imzalama için yeterli (Android'in kendi varsayılanı), yapıştırılacak metin %40
daha kısa. **Neden `PBE-SHA1-3DES` + `sha1` MAC:** evrensel JDK uyumluluğu (PKCS12 PBES2/AES
desteklenmeyen eski JDK'lar dâhil); ilk CI derlemesinde imzalama açıkça doğrulanır.

### 3.2 Depoda saklanacaklar / saklanmayacaklar

| Nerede | Ne |
|---|---|
| GitHub Secret | **`SIGNING_KEYSTORE_BASE64`** ve **`SIGNING_PASSWORD`** (yalnızca 2 alan) |
| Repo (`docs/IMZALAMA.md`) | Yalnızca **sertifika SHA-256 parmak izi** ve prosedür |
| Repo dışı, kullanıcıda | Anahtar dosyası + parola (iki bağımsız yerde: şifre yöneticisi + Drive) |
| `.gitignore` | `*.p12`, `*.jks`, `*.keystore`, `*.pem`, `anahtar/`, `secrets/` (hazır) |

**Neden 2 secret yeterli:** takma ad (`yanlisdefteri`) gizli bilgi değil → workflow'da sabit yazılır;
PKCS12'de store ve key parolası aynı → tek parola secret'ı yeterlidir.

### 3.3 Secret yetkisi sorunu (E1) — çözüm ve doğrulama

Arena token'ı depo için `viewerPermission=ADMIN` gösterse de `gh secret list` **403** verdi; bu uç
tekrar denenmeyecek. Kullanıcı `SIGNING_KEYSTORE_BASE64` ve `SIGNING_PASSWORD` secret'larını GitHub
web arayüzünden ekledi. Değerler rapora/komut çıktısına yazılmadı.

| Yol | Ölçülen durum |
|---|---|
| Arena token'ıyla secret API | 403; secret isim/değerlerini okuyamıyor |
| GitHub web arayüzü | Kullanıcı iki secret'ı ekledi; CI bunlarla keystore'u açıp imzaladı |
| İmzasız APK ile ilerlemek | Kalıcı test için kullanılmadı; update zinciri imzalı APK ile doğrulanıyor |

**G3 sonucu (2026-10-16):** CI keystore'u çözdü, APK'yı `apksigner` ile imzaladı ve sertifika SHA-256
parmak izini bu belgedeki değerle eşleştirdi; Release v1.0.0 oluşturuldu. İlk G3 koşusunda sertifika
satırını ayrıştıran betikte hata vardı; sonraki koşuda düzeltildi. G4 koşusu da aynı anahtarla geçti
ve v1.0.1 Release oluşturdu. Ayrıntılar `docs/DERLEME_RAPORU.md` ve HANDOFF'tadır. Secret'ları
workflow kendisi oluşturamaz ve hiçbir secret değeri repoya yazılmaz.

### 3.4 G2/G3 imza ayrımı

G2'de `assembleRelease` yalnızca imzasız derleme doğrulamasıdır. G3 workflow'u APK'yı `apksigner`
ile ayrı adımda imzalar; secret eksikse, açılamazsa veya parmak izi eşleşmezse Release yayımlanmaz.
Gradle tarafında debug imzasına sessiz düşüş yapılmaz.

---

## 4. Aşama 1 — görev listesi (uygulama sırası)

Her görev: amaç → dosyalar → bitti ölçütü. Sıra, **en ucuz doğrulamadan en pahalıya** doğrudur.

### G1. Yetki denemesi ✅ (2026-10-16)
- Push ile otomatik CI iki kez çalıştı; workflow token'ının `contents:write` ile raporu repoya
  commit edebildiği doğrulandı. `workflow_dispatch` ve secret uçları 403 verdi; elle tetikleme
  gerekmediği için bu engel G2/G3 akışını durdurmuyor.
- Kullanıcı iki imzalama secret'ını web'den ekledi; secret değerleri Arena token'ından okunamıyor.
  G3 CI imza ve parmak izi eşleşmesiyle doğrulandı.

### G2. Proje iskeleti (yalnızca derleme, arayüz yok) ✅ (2026-10-16)
- **Dosyalar:** `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`,
  `gradle/libs.versions.toml`, wrapper dosyaları, `app/build.gradle.kts`,
  `app/src/main/AndroidManifest.xml` ve G2'de arayüz içermeyen `MainActivity.kt`. Tema ve G4 arayüz dosyaları sonradan eklendi.
- **Neden arayüz yok:** İlk CI derlemesinde tek sorun kaynağı olsun; arayüz G4 aşamasında eklendi.
- **Bitti ölçütü:** CI yeşil; `assembleRelease` çıktısı üretiliyor; log temiz.
- **Ölçülen sonuç:** `assembleRelease` başarılı; imzasız APK ve SHA-256 raporu `docs/DERLEME_RAPORU.md` içinde. Wrapper CI'da gerçek Gradle 9.8.1 ile üretildi ve repoya commit edildi. İlk denemedeki built-in Kotlin çakışması yukarıda kayıtlıdır.

### G3. İmzalama + Release akışı ✅ (2026-10-16)
- **Yapı:** CI, base64 secret'tan keystore'u çözer → `assembleRelease` → `zipalign` + `apksigner`
  → `apksigner verify --print-certs` → parmak izi karşılaştırması → SHA-256 → GitHub Release.
- `versionName`/`versionCode`, `app/build.gradle.kts` içinden alınır. Her izlenen push derlenir;
  ilgili `v<versionName>` etiketi henüz yoksa Release oluşturulur (var olan tag yeniden yayımlanmaz).
- **Ölçülen sonuç:** v1.0.0 (run `38927072038`) ve v1.0.1 (run `38927750387`) imzalı Release'leri
  oluşturuldu; sertifika parmak izi eşleşti. Release notunda sürüm + commit + APK SHA-256 bulunuyor.

### G4. Ekran iskeleti ve 7 ders kartı ✅ CI (2026-10-16)
- **Dosyalar:** `MainActivity.kt`, `ui/DerslerEkrani.kt`, `ui/bilesenler/DersKarti.kt`,
  `res/values/strings.xml`, tema; tek `Scaffold`, adaptive grid.
- **Karar:** Gezinme kütüphanesi v1'de kullanılmıyor; bu aşamada tek ekran yeterli.
- **Ölçülen:** v1.0.1 `assembleRelease`, imza/parmak izi kontrolü ve Release CI'da başarılı.
  G5'te kullanıcı uygulamanın açıldığını ve 7 kartı gördüğünü bildirdi; ayrıntılı açık/koyu tema
  kontrast testi TEST 5.9'da yapılacak.

### G5. Güncelleme zinciri testi ✅ (kullanıcı bildirimi, 2026-10-16)
- **Yöntem:** v1.0.0 (`versionCode=1`) ardından v1.0.1 (`versionCode=2`) uygulama kaldırılmadan kuruldu.
- **Sonuç:** Kullanıcı güncellemenin başarılı olduğunu, uygulamanın açıldığını ve 7 ders kartının göründüğünü bildirdi. Aşama 2 başlayabilir.

### G6. Belge güncellemesi ✅ (G2–G5)
- **Dosyalar:** HANDOFF (ne yapıldı, hangi sürümler sabitlendi, son başarılı derleme), BUILDING
  (secret adları ve prosedür doğrulandı mı), CHANGELOG (sürüm notu), ROADMAP (Aşama 1 durumu).
- **Bitti ölçütü:** Yeni bir oturum yalnızca belgeleri okuyarak devam edebiliyor.

### Aşama 1 dosya ağacı (hedef)

```
.
├── .github/workflows/android.yml        # derleme + imzalama + release
├── .gitignore
├── README.md · PROJECT_BRIEF.md · ROADMAP.md · HANDOFF.md · BUILDING.md · CHANGELOG.md
├── docs/…                               # mevcut belgeler + IMZALAMA.md + KODLAMA_PLANI.md
├── settings.gradle.kts · build.gradle.kts · gradle.properties
├── gradlew · gradlew.bat
├── gradle/
│   ├── libs.versions.toml                # sabitlenmiş sürümler (tek kaynak)
│   └── wrapper/gradle-wrapper.{jar,properties}
└── app/
    ├── build.gradle.kts                  # applicationId, minSdk/targetSdk, sürüm bilgisi
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/keremtulu/questionnote/
        │   │   ├── MainActivity.kt
        │   │   └── ui/theme/{Renk.kt, Tema.kt, Tipografi.kt}
        │   └── res/values/{strings.xml, themes.xml}
        └── test/java/…                    # ilk birim testleri (G2 sonrası)
```

---

## 5. Aşama 2 — görev listesi (yerel arşiv)

### 5.1 Uygulanan paket yapısı

```
com.keremtulu.questionnote/
├── data/local/                 # Room v3, entity/DAO/provider ve MIGRATION_1_2 / MIGRATION_2_3
├── data/repository/            # ders, konu, soru repository'leri
├── data/images/                # JPEG optimizasyonu, kalıcı depo, bütünlük/temizlik hizmeti
├── data/export/                # YerelYedeklemeHizmeti + eski export API adı için typealias
├── domain/                     # modeller, repository sözleşmeleri, tekrar kuralı
├── ui/                         # Bugün, yan menü, soru ayrıntısı/tekrar geçmişi, yedek ve görsel bakım
└── AppContainer.kt / MainActivity.kt
```

### 5.2 Tamamlanan kod ve CI ölçütleri

- Room v3 ve v2→v3 veri koruyan migration: `kaynak_konumu`, global kaynak/neden katalog tablosu, önceki soru alanları korunur. v1→v2→v3 ve doğrudan v2→v3 testleri CI run `39037429421` içinde geçti; schema 3 JSON üretildi.
- Ders/konu yönetimi; soru CRUD, liste/filtre/ayrıntı; tekrar plan kuralları.
- Soru girişinde ortak kaynak seçimi, soru konumu, hazır/özel nedenlerden tek seçim; soru notu UI'den kaldırıldı, eski DB/backup verisi korunuyor.
- Kamera/galeri çoklu görselleri, EXIF yönü, JPEG uzun kenar ~3000 px/kalite 90, SHA-256, staging→kalıcı alan ve kontrollü bakım.
- ZIP v3/SAF yedekleme: katalog ve locator içeren manifest/JSON/6 CSV/görsel payload'ları; v2 ZIP içe aktarımı; doğrulama sınırları, güvenlik kopyası ve journal/Room marker kurtarması. CI run `39037429421` içindeki v3/v2 yedek testleri geçti.
- v1.0.5 CI run `39037429421`: 36 test, `assembleRelease`, imza/apksigner doğrulaması ve sertifika fingerprint eşleşmesi başarılı; Release yayımlandı. CI raporu `docs/DERLEME_RAPORU.md`; CI raporundaki APK SHA-256 `00f0b3b33f1efa81156043ef38a31640f57ce3ba34cf608505eb3164e0a8bedb`.
- v1.0.6 CI run `39039791751`: form yenilemesi dahil kod 36 testle derlendi; `assembleRelease`, imza ve fingerprint doğrulandı, v1.0.6 Release yayımlandı. APK SHA-256 `dde5f837a926bf906e164c202a77a85c6bf2ffadce1c16b98cf8d8c8bbc5332a`.
- v1.0.7 CI run `39041684003`: `+ Konu` eklemesi ve yedi dersin eski konu listelerini koruma testi dahil 37 test geçti; Release/imza/fingerprint doğrulandı. APK SHA-256 `3d10f48d7c6db6ff246d0942fa958df974ac3c87e182b2627855f192035f99c4`; gerçek cihaz kabulü bekliyor.
- v1.0.8 CI run `39044777998`: filtreli çalışma ekranı, filtre SQL'i ve sıralama testleri dahil 42 test geçti; Release/imza/fingerprint doğrulandı. APK SHA-256 `66563dc1b04ffe632ec5285baaefa92fbd9f5a068e8fab2726c4b85480b76b3e`; gerçek cihaz kabulü bekliyor.
- v1.0.9 CI run `39048703584`: düzeltmeler dahil 44 test geçti; Release/imza/fingerprint doğrulandı. APK SHA-256 `c6f1fd47b1bfae06e77758e91ed33ec69ad81262208e541080477269191b0f38`; cihaz kabulü bekliyor. Yerel Java olmadığı için derleme/test yalnızca CI'da yapılabildi.
- Kullanıcı v1.0.3'teki Bugün/yan menü/tekrar kaydı cihaz adımlarını geçti bildirdi. v1.0.4 tekrar geçmişi UI'si, v1.0.5 güncelleme/migration ve katalog akışı, v1.0.6 form yenilemesi, v1.0.7 `+ Konu`, v1.0.8 filtreli çalışma, SAF/Drive, temiz kurulum/başka cihaz ve fotoğraf okunabilirliği cihaz testi bekliyor.

### 5.3 Dosya ↔ veritabanı tutarlılığı (yazma sırası)

1. Görsel **geçici** dosyaya yazılır (uygulamanın kendi geçici klasörü).
2. Dosya açılıp doğrulanır (gerçekten görsel mi, okunabiliyor mu), gerekirse küçültülür, `sha256`
   hesaplanır.
3. Kalıcı klasöre **taşınır** (aynı dosya sistemi içinde atomik taşıma).
4. Veritabanı kaydı **tek transaction** içinde yazılır (soru + görseller).
5. Adım 4 başarısız olursa taşınan dosya silinir; adım 3 başarısız olursa geçici dosya silinir.
6. Uygulama öldürülürse: kalıntı geçici dosyalar bir sonraki açılışta temizlenir; kalıcı klasörde
   kaydı olmayan dosyalar **silinmez**, rapor edilir (§5.4).

### 5.4 Öksüz dosya politikası

Kaydı olmayan kalıcı dosya hemen silinmez (devam eden işleme ait olabilir). Ayarlar'daki "bakım"
işi: belirli yaştan eski dosyaları bulur, **kaç dosya / ne kadar yer** olduğunu gösterir, silme
yalnızca kullanıcı onayıyla ve raporla yapılır.

---

## 6. Aşama 3–6 — güncel durum

**Aşama 3 (tam ZIP yedek):** `data/export/YerelYedeklemeHizmeti.kt` içinde; v1.0.5 biçim v3, kaynak/neden kataloğu ve locator'ı yedekler; önceki v2 biçimini içe alır. CI run `39037429421` testleri geçti; SAF sağlayıcı/Drive ve başka cihazdaki kabul kullanıcı testini bekliyor.

**Aşama 4 (tekrar arayüzü):** Bugün/gecikmiş ekran, yan menü ve tekrar sonucu kaydı v1.0.3'te; planlanan/gerçekleşen tarih, sonuç ve çözüm açma gösteren tekrar geçmişi v1.0.4'te uygulandı. v1.0.4 history UI cihaz testi ve elle tarih arayüzü açık.

**Aşama 5 (sağlamlaştırma):** Büyük arşiv ölçümü, önizleme stratejisi, kırpma ekranı, erişilebilirlik ve depolama göstergesi açık.

**Aşama 6 (isteğe bağlı):** Doğrudan Drive entegrasyonu, otomatik yedekleme, bildirim, PDF, istatistik ve şifreleme; ayrı ürün kararı gerektirir.

---

## 7. Test stratejisi

| Katman | Nerede çalışır | Ne test eder | Ne zaman |
|---|---|---|---|
| Birim (JVM) | CI, her derlemede | aralık hesabı, tarih/gün dönüşümü, yedek doğrulama, ZIP güvenliği, manifest sayıları | Aşama 2'den itibaren |
| Şema/migration (Robolectric) | CI | `v1→vN` zinciri, DAO sorguları, ilişkiler | Aşama 2 |
| İmza doğrulama | CI | APK imzasının beklenen parmak iziyle eşleşmesi | Aşama 1'den itibaren |
| Gerçek cihaz (kabul) | Kullanıcı, S24 FE | listedeki tüm KULLANICI testleri | Her aşama sonunda |
| Emülatör | — | (v1'de kullanılmıyor; gerekirse sonradan) | — |

**Raporlama zorunluluğu:** Her turda "gerçekten çalıştırdım / yalnızca inceledim / sende test
edilecek" ayrımı yazılır. `docs/TEST_KONTROL_LISTESI.md` işaretlenir.

---

## 8. Derleme bütçesi (CI turu maliyeti)

- Sandbox'ta derleme yapılamadığı için **her doğrulama bir CI turudur.** Bu yüzden:
  - İlk commit **arayüzsüz** olur (G2) — bağımlılık/sürüm sorunlarını arayüz gürültüsünden ayırır.
  - `gh run view --log-failed` logları indirilemediği için hata, CI'ın repoya yazdığı `docs/DERLEME_RAPORU.md` içinden okunur.
  - Gradle bağımlılık önbelleği CI'da `setup-java` cache'i ile tutulur.
  - Aynı hatayı iki kez almamak için hata + çözüm HANDOFF'a yazılır.
- Actions kotası kurulumda kontrol edilir; sınırsız/ücretsiz varsayılmaz (repo public olduğu sürece
  standart runner'larda dakika sınırı yok, ama bu varsayım olarak değil kontrol olarak işlenir).

---

## 9. Riskler (kodlama aşamasına özgü)

| Risk | Etki | Önlem |
|---|---|---|
| Arena token'ının Actions/Secrets yetkisi yok (E1) | Secret değerleri okunamaz | Kullanıcı web arayüzünden ekledi; G3/G4 CI imza ve parmak izi doğrulaması başarılı |
| Aday sürümler uyumsuz çıkar | CI kırmızı, zaman kaybı | Sürümler tek tek düşürülür; en küçük iskeletle başlanır |
| PKCS12 (OpenSSL 3) imzalamayı JDK'da desteklemez | İmzalama adımı başarısız | G3'te açık doğrulama; gerekirse JKS'e çevrilir |
| Wrapper jar'ı oluşturulamaz | İlk derleme yapılamaz | Yol A/B (§2) |
| Migration hatası veri kaybına yol açar | En ciddi risk | `fallbackToDestructiveMigration` yasak + zorunlu migration testi |
| Fotoğraf kalitesi metni okunmaz kılar | Ürün işe yaramaz | Test 2.18: gerçek fotoğraflarla doğrulama; gerekirse kalite yükseltilir |
| Aynı gün çift tekrar kaydı | Yanlış aralık | Birim testiyle kayıt tekilliği (Test 4.7) |
| Kullanıcı arşiv biriktirdikten sonra imza değişir | Veri kaybı | Sabit anahtar + CI parmak izi kontrolü + G5 zincir testi |

---

## 10. Kimin ne yapacağı (özet)

| Aşama | Ben (yapay zekâ) | Kullanıcı |
|---|---|---|
| 1 | G1–G5 tamamlandı: CI, imzalı Release'ler, 7 kart ve kullanıcı cihazı güncellemesi | Anahtarı iki bağımsız yerde korumak; sonraki APK'larda Aşama 2 fotoğraf akışını test etmek |
| 2 | Veri katmanı, görsel hattı, ekranlar, testler | Ölçülü cihaz testleri (uçak modu, iptal, konusuz soru, silme…) |
| 3 | Yedek üretme/doğrulama/geri yükleme, testler | Drive'a kaydetme denemesi; temiz kurulumda geri yükleme; bozuk yedek denemesi |
| 4 | Tekrar ekranı ve akışı | Günlük kullanım: "Bugün" listesini gerçekten çözmek |
| 5 | Performans ölçümü, durum ekranları, kırpma | Büyük arşivde kullanım deneyimi geri bildirimi |
| 6 | Seçilen özellikler | Karar vermek |

---

## 11. Bir sonraki oturum nasıl başlar?

1. Bu belge + HANDOFF okunur; HANDOFF'taki "sıradaki somut adım" uygulanır.
2. v1.0.9 CI ve Release tamamlandı; rapor `docs/DERLEME_RAPORU.md`, run `39048703584` (44 test). Yerel ortamda Java olmadığı için yerel Gradle testi çalıştırılamadı.
3. Sıradaki iş kullanıcı cihaz kabulidir: v1.0.9 APK'yı kaldırmadan güncelle, eski veriyi doğrula; filtreli çalışmada konu/kaynak/neden seçimini, soru sayısını, sıralı ilerlemeyi ve bitiş ekranını dene. `+ Konu`, soru formu, katalog/locator ve ZIP v3 geri yüklemeyi de kontrol et. v1.0.4 history UI ve SAF/Drive testleri beklemede; v1.0.2 “3. tur” eşlemesi teyitsiz.
4. Sürüm numarası her yayında artırılmalı; CI var olan `v{versionName}` etiketini yeniden oluşturmaz.
5. Her görev sonunda HANDOFF güncellenir: yapılan, ölçülen, kalan, sürümler, son başarılı derleme.
