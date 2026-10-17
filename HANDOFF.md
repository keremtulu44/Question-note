# HANDOFF — Yanlış Defteri

> Yeni bir geliştirme oturumu önce [PROJECT_BRIEF.md](PROJECT_BRIEF.md) ve bu belgeyi okur.

- Son güncelleme: 2026-10-17
- Aktif iş: **v1.0.9 (versionCode 10) CI'da.** Ekran döndürmede durum koruma, filtreli oturum çıkış onayı, “Kaç soru?” alanı, görsel çözme belleği, arşivli konu adı çakışması, yarım yedek dosyası, CSV formül nötrleştirmesi ve bağlama uygun geri etiketi düzeltmeleri bu sürümde. Push sonrası CI sonucu bekleniyor; son yayımlanmış sürüm v1.0.8.
- Son başarılı CI: run `39044777998`, kaynak commit `bef4bd4191b5b265a4ff8e61052e0ef969a175f6` — 42 test geçti; Release derlemesi, imza/apksigner ve beklenen sertifika fingerprint eşleşti. İlk koşu `39044614950` yanlış `layout.weight` import'uyla derlenemedi; düzeltme sonrası run başarılı.
- APK: [v1.0.8 Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.8), [doğrudan indir](https://github.com/keremtulu44/Question-note/releases/download/v1.0.8/yanlis-defteri-1.0.8.apk), CI raporundaki SHA-256 `66563dc1b04ffe632ec5285baaefa92fbd9f5a068e8fab2726c4b85480b76b3e`.
- Somut kullanıcı adımı: v1.0.8'i Samsung Galaxy S24 FE'ye mevcut uygulamayı **kaldırmadan** kur; eski verinin korunduğunu doğrula. Filtreli çalışmada konu/kaynak/neden seçimini, “Kaç soru?” alanını, sıralı ilerlemeyi ve bitiş ekranını dene. `+ Konu`, soru formu ve ZIP v3 yedek/geri yükleme de bekliyor. Henüz cihaz kabulü bildirilmedi.
- v1.0.3 için kullanıcı cihaz adımlarının tümünü geçti bildirdi (2026-10-17). Önceki v1.0.2 “3. tur” ifadesinin checklist bölüm 3 (yedek/geri yükleme) anlamına geldiği teyit edilmedi; ayrıntı `docs/TEST_KONTROL_LISTESI.md`.
- Tam ZIP biçimi: `docs/YEDEK_FORMATI.md`; test ayrımı ve bekleyenler: `docs/TEST_KONTROL_LISTESI.md`; CI kanıtı: `docs/DERLEME_RAPORU.md`.

---

## Güncel teslim — v1.0.8 (Aşamalar 2/3/4)

- v1.0.8 CI run `39044777998`: 42 test, `assembleRelease`, imza/apksigner doğrulaması ve beklenen sertifika fingerprint'i geçti; Release yayımlandı. Kanıt `docs/DERLEME_RAPORU.md` içinde.
- Filtreli çalışma eklendi: konu, kaynak ve ana yanlış nedeni AND ile birleşir; arşivli ders, `AKTIF` olmayan plan ve bugün çözülmüş sorular dışlanır; sıra en uzun süredir çalışılmayandan başlar. Filtreler uygulama kabuğunda tutulur, soru açıp dönünce korunur. Oturum ilerlemesi ve bitiş ekranı vardır.
- v1.0.7 `+ Konu`, v1.0.6 form hiyerarşisi/kartları/sabit kaydetme alanı ve v1.0.5 global kaynak/neden kataloğu, locator, eski not verisi koruma, Room v3 ve ZIP v3/v2 uyumluluğu korunur.
- Dal üzerinde yayımlanmamış düzeltmeler var: döndürmede durum koruma, filtreli oturum çıkış onayı, “Kaç soru?” alanı, görsel çözme belleği, arşivli konu adı çakışması, yarım yedek dosyası, CSV formül nötrleştirmesi, bağlama uygun geri etiketi. Bunlar için CI/Release çalışmadı, APK yok.
- Fiziksel cihaz kabulü henüz bildirilmedi: v1.0.8 üzerine güncelleme ve filtreli çalışma; v1.0.7 `+ Konu`; v1.0.6 formu; v1.0.5 katalog/locator ve ZIP v3 yedek/geri yükleme; v1.0.4 tekrar geçmişi; SAF/Drive ve fotoğraf okunabilirliği bekliyor.

## Önceki aşama geçmişi (tarihsel)

- Proje brifi kullanıcıyla birlikte değerlendirildi; teknik riskler ve gereksiz karmaşıklıklar konuşuldu.
- Kesinleşen kararlar alındı (bkz. PROJECT_BRIEF §4, §5).
- Aşama 0 belgeleri oluşturuldu: README, PROJECT_BRIEF, ROADMAP, HANDOFF, BUILDING, CHANGELOG,
  `docs/` altında veri modeli, yedek formatı, test listesi, kodlama planı, imzalama prosedürü.
- **G1 tamamlandı:** push ile otomatik CI iki kez çalıştı; ortam ölçümü `docs/ORTAM_RAPORU.md` içinde.
- **G2 tamamlandı:** tek modüllü Kotlin + Compose iskeleti ve Gradle 9.8.1 wrapper; arayüzsüz `assembleRelease` CI'da başarılı.
- **G3 tamamlandı:** secret'lardan açılan sabit anahtarla v1.0.0 imzalandı, parmak izi eşleşti ve Release yayımlandı.
- G3 ilk koşusu `38926666381` imza/verify aşamasında rapor ayrıştırma hatasıyla kırmızı oldu; `apksigner` sertifika çıktısı doğruydu. Parser düzeltildikten sonra run `38927072038` başarılı oldu; secret'lar doğru.
- **G4 tamamlandı (CI):** v1.0.1 / versionCode 2, açık/koyu tema ve 7 ders kartı; run `38927750387` imza ve Release ile başarılı.
- **G5 geçti (kullanıcı bildirimi, 2026-10-16):** v1.0.0'dan v1.0.1'e uygulama kaldırılmadan güncellendi; uygulama açıldı ve 7 ders kartı göründü.
- **A2 Room temeli eklendi:** `data/local/entity` altında beş tablo (ders, konu, soru, görsel, tekrar), Room veritabanı/provider, ders DAO'su, ilk açılış callback'i ve yedi sabit ders. Şema `app/schemas/com.keremtulu.questionnote.data.local.YanlisDefteriDatabase/1.json` olarak repoda.
- Şema/başlangıç/ilişki, ders repository'si, soru sayısı, kalıcı DB'yi yeniden açma ve konu repository testleri CI'da geçti. Run `38948106275`: 8 test, 0 başarısız, 0 hata, 0 atlanan; release derlemesi ve imza/parmak izi doğrulaması başarılı.
- Sabit ve CI'da doğrulanmış kombinasyon: AGP `9.2.0`, Compose/Kotlin plugin `2.4.0`, Compose BOM `2026.09.00`, Activity Compose `1.13.0`, Room `2.8.4`, KSP `2.3.12`, JUnit `4.13.2`, Robolectric `4.15.1`; Gradle `9.8.1`, Temurin JDK `17`, compile/target SDK `37/37`, minSdk `26`. Tam liste `gradle/libs.versions.toml` içinde.
- İlk Room CI run `38943027931` derleme hatasında kaldı; rapor tanıyı göstermediği için workflow hata satırlarını koruyacak şekilde güncellendi. Run `38943350088`, Kotlin 2.4'ün karışık SQL argümanlarında tür kesişimi çıkardığını gösterdi; `arrayOf<Any?>` düzeltmesi `cbab9db` ile yapıldı. Run `38943580341` başarılı oldu.
- Ders ekranını bağlayan ilk UI CI run `38946121569`, Column scope `weight` uzantısına doğrudan import verildiği için başarısız oldu; erişilemeyen import kaldırıldı (`ba29656`). Run `38946302916` 4 testle, diskten yeniden açma testi eklendikten sonraki run `38946633709` 5 testle başarılı oldu.
- `MainActivity` → `AppContainer` → Room repository akışında ders kartına dokununca konu yönetimi açılıyor. `KonuDao`/`RoomKonuRepository` ekleme, trim/boş-ad ve Türkçe büyük-küçük harf duyarsız ada göre yineleneni engelleme, yeniden adlandırma, arşivleme ve geri getirme sağlıyor; işlemler transaction içinde. Arşivleme satırı silmiyor, soru-konu FK ilişkisi korunuyor; schema v1 değişmedi.
- Konu ekranında etkin/arşiv filtreleri, ekle/düzenle dialog'ları, arşivle/etkinleştir ve snackbar hata/sonuçları var. Üç Robolectric test konu işlemlerini ve arşivleme sırasında sorunun `konu_id` ilişkisinin korunduğunu doğruladı. Gerçek cihaz testi yapılmadı.
- İlk UI CI run `38946121569`'daki Column scope `weight` import hatası `ba29656` ile giderilmiş, sonraki run `38946302916` geçmişti. Konu yönetimi içeren run `38948106275` toplam 8 testle geçti. İmzalı APK üretildi ancak v1.0.1 Release zaten var; yeni Release yayımlanmadı.

## Kullanıcıyla netleşen ek kararlar (Aşama 0 sırasında)

1. Çok görsel desteği hem şemada hem arayüzde olacak.
2. Ders ve konular veritabanında, düzenlenebilir; silme yerine arşivleme.
3. Tekrar değerlendirmesi 3 buton + "çözümü açtım" ayrı işaret.
4. Geri yükleme yalnızca "tamamen değiştir" + otomatik güvenlik kopyası.
5. Sabit imzalama anahtarı Aşama 1'de kurulacak (kullanıcı onayladı).
6. **Aynı gün ikinci deneme yok.** Kullanıcının gerekçesi: amaç unutulanları hatırlamak; çözüm
   görüldükten sonra aynı oturumda yapılan çözüm gerçek hatırlama değildir. Bu yüzden çözüm
   açılmışsa sonraki tekrar **1 gün** sonraya planlanır (K19). Model bu sayede sadeleşti.
7. **Görsel kalite: otomatik optimize** (uzun kenar ~3000 px, JPEG ~%90) — K21, A9 kapandı.
8. **Askıya alma / tamamlandı işaretleme 1. sürümde olacak** (K17).

## Aşama 1 / G1 — Yetki ve ortam ölçümü (TAMAMLANDI, 2026-10-16)

### Ölçülen yetki tablosu (Arena token'ı)

| İşlem | Sonuç | Kanıt |
|---|---|---|
| Repo içeriği yazma (push) | ✅ Var | Belge commit'leri itildi |
| Workflow dosyası yazma | ✅ Var | `.github/workflows/ortam-testi.yml` itildi |
| PR açma / birleştirme | ✅ Var | PR #1, #2, #3 birleştirildi |
| Workflow **elle tetikleme** (`dispatches`) | ❌ **403** | `Resource not accessible by integration` |
| **Secret** okuma/yazma | ❌ **403** | `gh secret list`, `actions/secrets/public-key` |
| Actions ayarlarını okuma | ❌ 403 | `actions/permissions` |
| Actions **okuma** (workflow/run listesi) | ✅ Var | `actions/workflows`, `actions/runs` |
| Derleme **logu indirme** | ❌ Engelli | Log başka bir sunucudan geliyor (sandbox'ta erişilemiyor) |

**Bu oturumdaki tekrar kontrolü (2026-10-16):** `gh auth status` başarılı; `gh repo view` depo için
`viewerPermission=ADMIN` gösterdi. `gh api .../permissions` 404, `gh secret list` 403 döndü. Secret
uçları tekrar çağrılmayacak; G3 imzalama iş akışı secret doğrulamasını tamamladı.

**Kritik sonuç:** Elle tetikleme izni gerekmiyor. Workflow **push ile otomatik** çalıştırıldığında
hiçbir özel izin gerekmiyor → **CI çalışıyor.** Denendi ve kanıtlandı: iki başarılı koşu
(`38919720842`, `38920099577`).

**İkinci kritik sonuç:** Workflow'un kendi token'ı `permissions: contents: write` ile raporu repoya
commit edebiliyor. G3/G4'te aynı token'la v1.0.0 ve v1.0.1 GitHub Release'leri gerçekten oluşturuldu.
Arena token'ına Release erişimi gerekmedi.

**Log okuma çözümü:** Derleme logları indirilemese de G1 raporu `docs/ORTAM_RAPORU.md`, Android
sonuçları `docs/DERLEME_RAPORU.md` olarak repoya yazılıyor. Teşhis döngüsü: CI ölçer → rapor commit
edilir → okunur.

### Ölçülen CI ortamı

| Bileşen | Ölçülen değer |
|---|---|
| Runner | `ubuntu-latest` (ubuntu24 görüntüsü) |
| Java (varsayılan) | Temurin **17.0.20.1** — ayrıca 8, 11, 21, 25 kurulu |
| Android SDK | `/usr/local/lib/android/sdk` (lisanslar mevcut) |
| build-tools | 34.0.0, 35.0.0, 35.0.1, 36.0.0, 36.1.0, **37.0.0** |
| platforms | android-34 … **android-37.2** (+ 37.2 beta'ları) |
| apksigner | 0.9 (build-tools 37.0.0) |
| Gradle | **9.8.1** önceden kurulu |
| openssl / keytool | 3.0.13 / var |

→ G2'de `compileSdk=37` ve `targetSdk=37` ile imzasız release derlemesi başarıyla doğrulandı.

### İmzalama anahtarı (üretildi ve G3/G4 CI'da doğrulandı)

- RSA 2048, PKCS12, alias `yanlisdefteri`, 30 yıl (2056'ya kadar)
- SHA-256 parmak izi: `FA:13:DE:D7:3C:B4:6E:DE:9E:36:37:47:EE:38:95:F2:B6:37:14:9C:16:57:90:A2:E4:67:78:F1:30:89:DE:54`
- Dosyalar **git dışı** `anahtar/` klasöründe: `yanlis-defteri.p12`, `PAROLA.txt`, `keystore.b64`
  (puantaj: `git status` temiz, klasör `.gitignore`'da)
- Ölçüm: base64 uzunluğu **3404 karakter** (RSA 2048). RSA 4096 olsaydı 5696 olurdu → 2048 tercih edildi.
- Kullanıcıya adım adım talimat: `anahtar/KURULUM_ADIMLARI.md` (git dışı)

**E1 güncel durum:** `gh secret list` 403 verdi; secret uçlarını tekrar çağırmayacağız. Kullanıcı iki
secret'ı web'den ekledi. G3 CI keystore'u çözdü, APK'yı imzaladı ve beklenen sertifika parmak izini
eşleştirdi (run `38927072038`); secret'lar doğrulandı, değerler hiçbir rapora yazılmadı.

## Dış inceleme — ikinci görüş (2026-10-16)

Proje, bağımsız bir yapay zekâya teknik inceleme için gönderildi (`docs/FIKIR_ALMA_METNI.md`).
Değerlendirme sonucu: teknoloji seçimi onaylandı; önerilerin bir kısmı kabul, bir kısmı gerekçeyle
reddedildi.

**Kabul edilenler (belgelere işlendi):**

| Öneri | Nereye işlendi |
|---|---|
| Sorunun **plan durumu** (aktif/askıda/tamamlandı) öğrenme düzeyinden ayrılmalı | PROJECT_BRIEF K17, VERI_MODELI §2 |
| Aralık kuralı **sürümlü tek sınıf** olmalı (`algoritma_surumu`) | K18, VERI_MODELI §2/§5 |
| Çözüm açılarak yapılan tekrar bağımsız sayılmamalı (tavan 1–3 gün) + ikinci bağımsız deneme | K19, VERI_MODELI §5 — *sonradan değişti, bkz. Reddedilenler #5: tavan yerine 1 gün* |
| Uzun aradan sonra otomatik sıfırlama yapılmamalı | K20, VERI_MODELI §5 |
| Geri yüklemede **kesintiye dayanıklılık** (işlem sürerken kapanma) | YEDEK_FORMATI §3.1 |
| ZIP bomb / yol kaçışı / boyut üst sınırı korumaları | YEDEK_FORMATI §3.2 |
| Manifest'te dosya başına sha256; format sürümü uygulama sürümünden bağımsız | YEDEK_FORMATI §2/§6 |
| Görsel kaydına `mime_type` ve `sha256` eklenmesi | VERI_MODELI §2 |
| **Migration testleri otomatik olmalı** (zincir testi dâhil) | VERI_MODELI §6, ROADMAP Aşama 2 |
| Depolama boyutunun asıl kısıt olduğu; 30–50 gerçek fotoğrafla sıkıştırma testi | ROADMAP Aşama 2, TEST 2.18 |
| Yedek/geri yükleme mimarisinin baştan veri katmanının parçası olması | PROJECT_BRIEF §5, ROADMAP Aşama 2 |
| Release çıktısında sürüm + commit + APK SHA-256 | BUILDING §4, TEST 1.4 |
| Boş/anlamsız yedeğin onaysız uygulanmaması, son yedek bilgisi | YEDEK_FORMATI §3/§4 |

**Reddedilenler (gerekçesiyle):**

1. **Her görsel için ayrı önizleme dosyası üretmek** → v1'de yapılmayacak. Türetilmiş veri kalıcı
   klasöre yazılırsa yedek boyutu artar ve silme/geri getirme mantığı gerekir. Liste görselleri
   Coil'in önbelleğinden üretilecek; ölçümde yetersiz kalırsa (Aşama 5) karar yeniden değerlendirilir.
2. **Emülatörde otomatik test** → öncelik JVM birim testleri ve Robolectric (şema/migration).
   Emülatör yavaş ve kırılgan; gerçek cihaz testi zaten kullanıcıda.
3. **Anahtar ve parolayı farklı yerlerde tutma kuralı (birincil kural olarak)** → bu projede tehdit
   modeli "sızma" değil "kaybetme". Öncelik **yedeklilik**: anahtar + parola iki bağımsız yerde
   bulunur; parolanın keystore dosyasının yanında düz metin durmaması koşuluyla.
4. **Tek `data.json`** → veriler ayrı JSON dosyalarında kalacak (incelenebilirlik/karşılaştırma);
   fark küçük, gerekçe YEDEK_FORMATI §1'de.
5. **"Çözümü kapat ve tekrar dene" düğmesi** → önce kabul edilmişti, **kullanıcı reddetti**: çözüm
   görüldükten sonra aynı gün yapılan çözüm gerçek hatırlama sayılmaz. Sonraki tekrar 1 gün sonraya
   planlanır (K19). Bu, hem bir düğmeyi hem de ek kayıt tipini ortadan kaldırdı.

## Ne test edildi?

**Gerçekten çalıştırılan (ölçüm/kanıt ile):**

- CI iş akışı **iki kez başarıyla çalıştı** (koşular `38919720842`, `38920099577`;
  "completed / success"). Push ile otomatik tetiklendi — elle tetikleme izni gerekmedi.
- G2 ilk build koşusu `38924729686` başarısız oldu: AGP 9.2.0'ın built-in Kotlin desteği dış
  `org.jetbrains.kotlin.android` eklentisini reddetti. Eklenti kaldırılınca `38925064386` başarılı oldu;
  imzasız APK/SHA-256 `docs/DERLEME_RAPORU.md` içinde.
- G3 run `38926666381` APK'yı imzaladı ve `apksigner verify` geçti, ancak fingerprint ayrıştırıcıdaki
  biçim varsayımı nedeniyle pipeline kırmızı oldu. Düzeltmeden sonraki run `38927072038` başarılı:
  secret'tan çözülen APK'nın parmak izi beklenenle eşleşti; v1.0.0 Release oluştu.
- G4 run `38927750387` başarılı: v1.0.1 (versionCode 2) arayüzü derlendi, aynı imzayla doğrulandı
  ve Release'e kondu. Her iki sürümün özeti ve SHA-256'sı `docs/DERLEME_RAPORU.md` içinde.
- CI token'ı `contents:write` ile rapor/wrapper commit edebildi; G3/G4 `gh release view` ile varlıkları
  ayrıca doğrulandı.
- Runner ortamı ölçüldü: Temurin JDK 17 (8/11/21/25 de kurulu), Android SDK (`build-tools` 34→37.0.0,
  `platforms` android-34→37.2), apksigner 0.9, Gradle 9.8.1 hazır, openssl 3.0.13.
- Sandbox yetenekleri: `openssl` var, `java`/`keytool` **yok**; Maven/Gradle depoları **kapalı**.
- Arena token'ı: içerik yazma ✅, workflow dosyası yazma ✅, PR açma/birleştirme ✅;
  workflow dispatch ❌ 403, secrets ❌ 403, Actions ayarları ❌ 403.
- İmzalama anahtarı üretildi ve parola ile açılabildiği doğrulandı (parmak izi okundu).

**Durum ve henüz test edilmemiş olanlar:**

- G2/G3/G4 derleme, wrapper, imzalama, parmak izi, APK SHA-256 ve Release CI'da doğrulandı.
  Sabit sürüm kombinasyonu `gradle/libs.versions.toml` ve `docs/KODLAMA_PLANI.md` içinde.
- **G5 kullanıcı bildirimi:** v1.0.0 → v1.0.1 uygulama kaldırılmadan güncellendi; uygulama açıldı ve
  7 ders kartı göründü. Bu cihaz testi kullanıcı raporudur; ben cihazda bağımsız çalıştırmadım.
- Room v1, repository/ana ekran ve konu CRUD/arşiv akışı CI/Robolectric'te doğrulandı (8 test, run `38948106275`). **Henüz uygulanmadı/test edilmedi:** ders yönetimi, soru CRUD/listeleme, görsel seçimi/kamera ve kalıcı dosya saklama, minimum dışa aktarma, gerçek fotoğraf okunabilirliği ve gerçek cihaz kabul testi. Bunlar Aşama 2–3 işleridir.
- Açık/koyu tema kontrastının detaylı kabul testi daha sonra TEST 5.9'da yapılacak.

## Bilinen hatalar / engeller

- **E1 (iş akışını engellemiyor):** Arena token'ının Actions/Secrets erişimi 403; workflow_dispatch da
  gerekmediği için çağrılmıyor. Kullanıcı secret'ları web'den ekledi; G3 imzalama/parmak izi kontrolü
  başarılı. Arena secret uçlarını tekrar çağırma.
- **E2 (kısıt, çözümü yok):** Yapay zekâ ortamında Android derlemesi/testi yapılamaz; ayrıca **CI
  derleme logları indirilemiyor**. Çözüm: workflow ölçüm/özet/hata çıktısını repoya yazar; G1 için
  `docs/ORTAM_RAPORU.md`, Android derlemeleri için `docs/DERLEME_RAPORU.md`.
- **E3 (izlenecek):** `hasFragileUserData` davranışı gerçek cihazda doğrulanmadı.
- **E4 (izlenecek):** Android Auto Backup, uygulama başına **25 MB** ile sınırlıdır; fotoğraf arşivi bu
  kotayı aşabilir. Manifest'te `allowBackup=false` ayarlandı; gerçek cihazdaki kaldırma/geri yükleme
  davranışı uygulama testlerinde ayrıca ele alınacak.
- **E5 (açık, kod incelemesi 2026-10-17):** Aşağıdakiler bilinçli olarak düzeltilmedi:
  (a) görsel bakımı eksik/bozuk dosyayı yalnızca raporlar, yeniden bağlama/onarma yok;
  (b) `dersSirasiniDegistir` yalnızca `sira` takas eder, eşit değerlerde görünen sıra değişmeyebilir
  (şu an arayüzden çağrılmıyor, ölü kod);
  (c) filtreli oturum süreç ölümünden sonra kaldığı yerden devam etmez (döndürme artık korunuyor);
  (d) göç eden eski `kaynak`/`yanlis_nedeni` metinleri katalogla birebir eşleşmezse filtrede
  “Belirtilmemiş” dışında seçilemez (silent eşleşmeme, veri kaybı değil);
  (e) `YanlisDefteriMigrations.migrate(database:)` parametre adı uyarısı (zararsız).

## Sıradaki somut adım

1. `docs/DERLEME_RAPORU.md` v1.0.8 CI/Release çıktısı doğrulandı (run `39044777998`); 42 test geçti. Yerel Gradle testi Java bulunmadığı için çalışmadı.
2. Sıradaki somut adım kullanıcı cihaz kabulü: [v1.0.8 Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.8)'i mevcut uygulamanın üzerine **kaldırmadan** kur; eski veriyi doğrula, filtreli çalışmada filtreleri/adet alanını/sıralı ilerlemeyi/bitiş ekranını dene; `+ Konu` ile ilgili dersteki listelerin korunduğunu kontrol et.
3. Kullanıcı sonucu gelince checklist'i güncelle; v1.0.8 filtreli çalışma, v1.0.7 `+ Konu`, v1.0.6 formu, v1.0.5 katalog/locator/ZIP v3, v1.0.4 tekrar geçmişi, SAF/Drive/başka cihaz ve fotoğraf okunabilirliği ayrı bekleyen testlerdir.
4. v1.0.9 (versionCode 10) olarak sürümlendi ve `arena/59c6570d-question-note` dalına push edildi; workflow bu dalı da tetikliyor. CI sonucu yeşilse `docs/DERLEME_RAPORU.md`, Release bağlantısı ve APK SHA-256 bu belgeye eklenecek. Yerel Java olmadığı için derleme/test yalnızca CI'da.

Detaylı kabul ölçütleri `ROADMAP.md` ve `docs/TEST_KONTROL_LISTESI.md` içindedir. `fallbackToDestructiveMigration` yasaktır; her Room şema değişikliği veri koruyan migration + otomatik test gerektirir.

## Kullanıcının yapacağı iş

v1.0.8 APK'yı [GitHub Release'ten indir](https://github.com/keremtulu44/Question-note/releases/download/v1.0.8/yanlis-defteri-1.0.8.apk) ve mevcut uygulamayı **kaldırmadan** üzerine kur. Eski verinin kaldığını doğrula. Filtreli çalışmada konu/kaynak/neden seç, “Kaç soru?” yaz, oturumu ilerlet ve bitiş ekranını gör; ara sıra geri dönüp seçimlerin korunduğunu kontrol et. `+ Konu` ile yeni konu ekleyip diğer derslerin listelerinin değişmediğini dene. Gerçek cihaz sonucu bildirilmeden checklist'te cihaz adımlarını geçme işaretleme.
