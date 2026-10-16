# KODLAMA PLANI (Aşama 1 ve sonrası)

> Bu belge **nasıl kodlayacağımızı** anlatır: görev sırası, dosya yapısı, sürüm sabitleme, imzalama
> kurulumu, derleme bütçesi, test stratejisi. Kod içermez; kod Aşama 1 onayıyla başlar.
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
| Arena token: **elle tetikleme (dispatch)** ve **secrets** | ❌ 403 | Elle tetikleme **gerekmiyor** (push ile otomatik); secrets'ı kullanıcı ekler |
| Workflow'un **kendi token'ı**: repoya commit atma | ✅ Var (kanıtlandı) | Release oluşturma da aynı yetkiyle yapılabilir |
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

Sürümler **tahminle değil, derleme zamanında doğrulanarak** sabitlenir. Aşağıdakiler bu oturumdaki
araştırmadan gelen **aday** değerlerdir; Google Maven / Maven Central'a bu sandbox'tan erişemediğim
için **doğrulanmış sayılmazlar**. (CI ortamının ölçülen tabanı: JDK 17 varsayılan, platforms
android-34→37.2, build-tools 34.0.0→37.0.0, Gradle 9.8.1 hazır.)

| Bileşen | Aday | Doğrulama |
|---|---|---|
| Android Gradle Plugin | 9.2.x (Nisan 2026) | AGP sürüm notları + Google Maven |
| Kotlin | 2.4.x (kararlı) | plugins.gradle.org |
| Compose BOM | 2026.09.00 | Google Maven |
| Gradle | AGP'nin istediği sürüm | AGP sürüm notları |
| JDK | 17 veya 21 (CI'da Temurin) | AGP gereksinimi |
| minSdk | **26 (Android 8)** — kesin karar | — |
| targetSdk | O günkü en güncel kararlı API | Aşama 1'de sabitlenir (A2) |
| Room, Coil, Activity-Compose, Lifecycle | Aday: Room 2.8+, Coil 3.x, activity-compose 1.13, lifecycle 2.11 | Google Maven / Maven Central |

**Kural:** İlk `build.gradle.kts` dosyası bu tablodaki adaylarla yazılır; CI ilk derlemede hangi
sürümü çözdüğünü loglar. Uyumsuzluk çıkarsa **tek tek** düşürülür ve hangi kombinasyonun çalıştığı
`libs.versions.toml` + HANDOFF'a yazılır.

**Şema/gereksinim notu:** Kotlin 2.x'te Compose için ayrı derleyici eklentisi gerekir
(`org.jetbrains.kotlin.plugin.compose`); bu, sürüm kataloğuna baştan eklenir.

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

A başarısız olursa B, o da olmazsa C; hangisinin kullanıldığı HANDOFF'a yazılır.

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

### 3.3 Secret yetkisi sorunu (E1) — karar gerekli

Ölçülen durum: token'da depo admin'i var ama **Actions Secrets erişimi yok** (`gh secret list` ve
`actions/secrets/public-key` → **403**; Actions okuma ise çalışıyor). Üç yol var:

| Yol | Ne olur | Kimin işi |
|---|---|---|
| **A. Arena GitHub bağlantı iznini genişletmek** ✅ *(seçildi)* | Bağlantıya Actions/Secrets yazma izni eklenir; ben 4 secret'ı komutla eklerim | Kullanıcı (Arena'da izin) |
| **B. Kullanıcı secret'ları web arayüzünden ekler** *(A olmazsa)* | Ben anahtarı ve değerleri üretirim; kullanıcı GitHub web'den 4 alanı doldurur | Kullanıcı (telefondan ~5 dk) |
| **C. Şimdilik imzasız/debug ile ilerle** | İlk APK kurulur ama üzerine güncelleme **çalışmaz**; gerçek arşiv başlamadan bir kez kaldır-kur gerekir | Ben + kullanıcı |

**Güncel durum (G1 ölçümü):** Arena uygulamasının izin tanımı değişmedi; dispatch ve secrets
uçları hâlâ **403**. Bu nedenle **yol B uygulanıyor**: kullanıcı 2 secret'ı GitHub web arayüzünden
ekleyecek (adımlar: `anahtar/KURULUM_ADIMLARI.md`, git dışı).

**Önemli:** Secret'ları **workflow da oluşturamaz** (güvenlik gereği). Yani "GitHub'da yeni bir action
açmak" CI'ı çalıştırır ama secret sorununu çözmez. CI'ın tek ihtiyacı olan şey, bir kez elle eklenmiş
2 secret'tır.

### 3.4 Yerel derleme anahtarı

CI secret'ları yokken yerel derleme yapılabilmesi için `app/build.gradle.kts`, imzalama bilgilerini
**ortam değişkenlerinden** okur, yoksa `debug` imzasına düşer. Release APK üretmeye çalışırken
anahtar yoksa **derleme hata vererek durur** (sessizce debug'lamaz).

---

## 4. Aşama 1 — görev listesi (uygulama sırası)

Her görev: amaç → dosyalar → bitti ölçütü. Sıra, **en ucuz doğrulamadan en pahalıya** doğrudur.

### G1. Yetki denemesi (kod yazmadan)
- **Amaç:** Workflow dosyası yazma ve (varsa) secret ekleme yetkisini ölçmek.
- **Yöntem:** En küçük workflow dosyası (`hello` job'u) eklenir, `gh workflow run` ile tetiklenir,
  log okunur. Başarısızsa dosya geri alınır ve yol §3.3-A/B'ye göre seçilir.
- **Bitti ölçütü:** "Workflow yazabiliyorum: evet/hayır" ve "Secret ekleyebiliyorum: evet/hayır"
  sorusunun **ölçülmüş** cevabı.

### G2. Proje iskeleti (yalnızca derleme, arayüz yok)
- **Dosyalar:** `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`,
  `gradle/libs.versions.toml`, wrapper dosyaları, `app/build.gradle.kts`,
  `app/src/main/AndroidManifest.xml`, boş `MainActivity.kt` + `ui/theme/` (renk/tema/tipografi).
- **Neden arayüz yok:** İlk CI derlemesinde tek sorun kaynağı olsun. Arayüz G4'te eklenir.
- **Bitti ölçütü:** CI yeşil; `assembleRelease` çıktısı üretiliyor; log temiz.

### G3. İmzalama + Release akışı
- **Yapı:** CI, base64 secret'tan keystore'u çözer → `assembleRelease` → `apksigner verify
  --print-certs` → parmak izi `docs/IMZALAMA.md` ile karşılaştırılır → SHA-256 hesaplanır →
  artifact yüklenir → (release tetiklenmişse) Release oluşturulur.
- **Workflow girdileri:** `version_name` (isteğe bağlı), `make_release` (true/false).
- **Bitti ölçütü:** İmzalı APK indirilebilir; parmak izi doğrulanmış; release notunda sürüm + commit
  + SHA-256 var.

### G4. Ekran iskeleti ve 7 ders kartı
- **Dosyalar:** `MainActivity.kt`, `ui/DerslerEkrani.kt`, `ui/bilesenler/DersKarti.kt`,
  `res/values/strings.xml`, tema; gezinme için tek `Scaffold` + alt/üst yapı kararı.
- **Karar:** Gezinme kütüphanesi v1'de kullanılmayacak — 5 sekme için Compose içi basit durum
  yönetimi yeterli. İhtiyaç doğarsa sonra eklenir (gereksiz bağımlılık eklememe ilkesi).
- **Bitti ölçütü:** Kullanıcı APK'yı kurup 7 ders kartını görüyor; açık/koyu tema çalışıyor.

### G5. Güncelleme zinciri testi
- **Yöntem:** `versionCode` artırılmış ikinci APK üretilir; kullanıcı **kaldırmadan** kurar.
- **Bitti ölçütü:** Kurulum reddedilmiyor ve uygulama açılıyor. Zincir doğrulanmadan Aşama 2 başlamaz.

### G6. Belge güncellemesi
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
    ├── build.gradle.kts                  # applicationId, minSdk 26, targetSdk, imzalama
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
  - Hata durumunda `gh run view --log-failed` ile yalnızca başarısız adım okunur.
  - Gradle önbelleği CI'da açık tutulur; ilk turdan sonra süreler kısalır.
  - Aynı hatayı iki kez almamak için hata + çözüm HANDOFF'a yazılır.
- Actions kotası kurulumda kontrol edilir; sınırsız/ücretsiz varsayılmaz (repo public olduğu sürece
  standart runner'larda dakika sınırı yok, ama bu varsayım olarak değil kontrol olarak işlenir).

---

## 9. Riskler (kodlama aşamasına özgü)

| Risk | Etki | Önlem |
|---|---|---|
| Token'ın workflow/secrets yetkisi yok (E1) | Aşama 1 durur | G1'de ölçülür; §3.3 A/B/C yolu |
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
| 1 | Yetki ölçümü, iskelet, imzalama, CI workflow, ekran iskeleti, belgeler | Anahtarı iki yerde saklamak; (gerekirse) 4 secret'ı web'den eklemek; APK kurmak; 2. APK ile üzerine kurulum testi |
| 2 | Veri katmanı, görsel hattı, ekranlar, testler | Ölçülü cihaz testleri (uçak modu, iptal, konusuz soru, silme…) |
| 3 | Yedek üretme/doğrulama/geri yükleme, testler | Drive'a kaydetme denemesi; temiz kurulumda geri yükleme; bozuk yedek denemesi |
| 4 | Tekrar ekranı ve akışı | Günlük kullanım: "Bugün" listesini gerçekten çözmek |
| 5 | Performans ölçümü, durum ekranları, kırpma | Büyük arşivde kullanım deneyimi geri bildirimi |
| 6 | Seçilen özellikler | Karar vermek |

---

## 11. Bir sonraki oturum nasıl başlar?

1. Bu belge + HANDOFF okunur; HANDOFF'taki "sıradaki somut adım" uygulanır.
2. Kullanıcıdan Aşama 1 onayı zaten varsa **G1 (yetki denemesi)** ile başlanır.
3. Her görev sonunda HANDOFF güncellenir: yapılan, ölçülen, kalan, sürümler, son başarılı derleme.
