# KODLAMA PLANI (Aşama 1 ve sonrası)

> Bu belge **nasıl kodlayacağımızı** ve ölçülen sonuçları kaydeder: görev sırası, dosya yapısı,
> sürüm sabitleme, imzalama kurulumu, derleme bütçesi, test stratejisi. Aşama 1 kodlaması başladı.
>
> İlgili belgeler: [PROJECT_BRIEF.md](../PROJECT_BRIEF.md) (kararlar) · [ROADMAP.md](../ROADMAP.md)
> (aşamalar) · [BUILDING.md](../BUILDING.md) (derleme/imzalama) · [docs/VERI_MODELI.md](VERI_MODELI.md)
>
> Son güncelleme: 2026-10-16

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
| Room, Coil, Lifecycle | Henüz eklenmedi | İlgili aşamada sürümlenip CI ile doğrulanacak |

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

### 5.1 Paket yapısı (hedef)

```
com.keremtulu.questionnote/       # applicationId (sabitlendi 2026-10-16)
├── AppContainer.kt            # elle bağımlılık kurulumu (DB, repository'ler, dosya yöneticisi)
├── MainActivity.kt
├── data/
│   ├── YanlisDefteriVeritabani.kt   # Room DB, migration listesi, exportSchema
│   ├── entity/{Ders.kt, Konu.kt, Soru.kt, Gorsel.kt, Tekrar.kt}
│   ├── dao/{DersDao.kt, KonuDao.kt, SoruDao.kt, GorselDao.kt, TekrarDao.kt}
│   └── repo/{DersRepository.kt, SoruRepository.kt, TekrarRepository.kt}
├── domain/
│   ├── Tarih.kt                # yerel gün (epochDay) dönüşümleri, tek noktada
│   ├── TekrarKurali.kt         # sürümlü aralık hesabı (algoritma_surumu = 1)
│   └── OgrenmeDurumu.kt        # türetilmiş durum + plan durumu geçişleri
├── images/
│   ├── GorselIceAktarma.kt     # seç → geçici → doğrula → küçült → kalıcı → sha256
│   ├── GorselDonusturucu.kt    # ölçekleme/kalite (uzun kenar ~3000 px, JPEG ~%90)
│   └── GorselDosyaYonetimi.kt  # kalıcı klasör, tutarlılık denetimi, onaylı temizlik
├── ui/
│   ├── ekranlar/{BugunEkrani.kt, DerslerEkrani.kt, SoruEkleEkrani.kt, SoruDetayEkrani.kt,
│   │             KonuYonetimiEkrani.kt, AyarlarEkrani.kt}
│   ├── bilesenler/{SoruKarti.kt, GorselGosterici.kt, CozumBolumu.kt, TarihSecici.kt}
│   └── tema/…
└── backup/                     # Aşama 3'te doldurulur (paket Aşama 2'de açılır)
```

### 5.2 Görev sırası

| # | Görev | Bitti ölçütü |
|---|---|---|
| 2.1 | Room şeması v1 + `exportSchema` + yabancı anahtar kuralları | Şema JSON'u repoda; migration altyapısı hazır |
| 2.2 | 7 ders başlangıç verisi kişileri (ilk açılışta yalnızca bir kez) | Uygulama ilk açılışta dersleri oluşturur, ikinci açılışta tekrar eklemez |
| 2.3 | Ders/konu yönetimi: ekle, yeniden adlandır, arşivle, geri getir | Test 2.13 geçer |
| 2.4 | `Tarih.kt` + `TekrarKurali.kt` + birim testleri | Tüm sonuç × çözüm açıldı kombinasyonları testten geçer |
| 2.5 | Görsel içe aktarma boru hattı (kamera + fotoğraf seçici) | Geçici→kalıcı akışı, iptal durumu, sha256, küçültme; Test 2.4/2.5/2.18 |
| 2.6 | Tutarlılık denetimi ve onaylı temizlik | Test 2.16/2.17 |
| 2.7 | Soru ekleme akışı (fotoğraf → ders → kaydet; diğer alanlar isteğe bağlı) | 3 dokunuşla kayıt yapılabiliyor |
| 2.8 | Soru listesi + filtreler (ders/konu/plan durumu) + "Konusuz" | Konusuz sorular erişilebilir |
| 2.9 | Soru görüntüleme: tam ekran, yakınlaştırma, çözüm gizli + "Çözümü göster" | Test 2.7/2.10 |
| 2.10 | Düzenleme, silme (onay), plan durumu (aktif/askıda/tamamlandı) | Test 2.11/2.12/4.12 |
| 2.11 | Migration testleri (Robolectric) + DAO testleri | Zincir testi `v1→vN` yeşil |
| 2.12 | Minimum "dışa aktar" (veriyi dışarı alma güvenlik ağı) | Dosya seçiciye yazılabiliyor |

> **2.12 özellikle erken:** Tam yedekleme Aşama 3'te tamamlanır, ama gerçek arşiv birikmeye
> başlamadan önce veriyi dışarı alabilme yeteneği olmalı.

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

## 6. Aşama 3–6 — kısa plan

**Aşama 3 (yedek):** `backup/` paketi → `YedekOlusturucu`, `YedekDogrulayici`, `GeriYukleyici`,
`YedekManifest`. Testler: manifest/sha256, yol kaçışı, zip bomb, kesintiye dayanıklılık (journal),
temiz kurulumda geri yükleme, bozuk yedek. Ayrıntı: [docs/YEDEK_FORMATI.md](YEDEK_FORMATI.md).

**Aşama 4 (tekrar):** `BugunEkrani` gerçek verilerle, tekrar değerlendirme akışı, geçmiş görünümü,
elle tarih atama, plan durumu filtreleri. Aralık kuralı zaten 2.4'te yazıldığı için burada yalnızca
arayüz ve akış eklenir.

**Aşama 5 (sağlamlaştırma):** Büyük arşiv ölçümü, önizleme stratejisi kararı (A10), kırpma ekranı,
boş/hata durumları, erişilebilirlik, depolama göstergesi.

**Aşama 6 (isteğe bağlı):** SAF kalıcı izni ile Drive klasörü, "şimdi yedekle", bildirimler (kesin
alarm yok), PDF çalışma kâğıdı, basit istatistikler, yedek şifreleme.

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
2. **G5 geçti; sıradaki iş Aşama 2**: Room şeması, veri ilişkileri ve migration testleriyle yerel arşiv temelini kur; sonra soru/görsel akışına ilerle.
3. Her görev sonunda HANDOFF güncellenir: yapılan, ölçülen, kalan, sürümler, son başarılı derleme.
