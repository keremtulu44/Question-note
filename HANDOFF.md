# HANDOFF — Yanlış Defteri

> Yeni bir geliştirme oturumu önce [PROJECT_BRIEF.md](PROJECT_BRIEF.md) ve bu belgeyi okur.

- Son güncelleme: 2026-10-16
- Aktif aşama: **Aşama 0 tamamlandı**, Aşama 1 için kullanıcı onayı bekleniyor
- Son başarılı derleme: **yok** (henüz derleme yapılmadı)

---

## En son ne yapıldı?

- Proje brifi kullanıcıyla birlikte değerlendirildi; teknik riskler ve gereksiz karmaşıklıklar konuşuldu.
- Kesinleşen kararlar alındı (bkz. PROJECT_BRIEF §4, §5).
- Aşama 0 belgeleri oluşturuldu: README, PROJECT_BRIEF, ROADMAP, HANDOFF, BUILDING, CHANGELOG,
  `docs/` altında veri modeli, yedek formatı, test listesi, kodlama planı, imzalama prosedürü.
- **Aşama 1 / G1 tamamlandı:** yetki ve CI ortamı ölçüldü; ilk workflow yazıldı ve **çalıştırıldı**
  (2 başarılı koşu); ölçüm sonucu `docs/ORTAM_RAPORU.md` olarak repoda.
- **İmzalama anahtarı üretildi** (git dışı `anahtar/` klasöründe); secret'ların eklenmesi bekleniyor.
- **Uygulama kodu hâlâ yok.** Depoda Gradle dosyası veya `app/` klasörü bulunmuyor.

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

**Kritik sonuç:** Elle tetikleme izni gerekmiyor. Workflow **push ile otomatik** çalıştırıldığında
hiçbir özel izin gerekmiyor → **CI çalışıyor.** Denendi ve kanıtlandı: iki başarılı koşu
(`38919720842`, `38920099577`).

**İkinci kritik sonuç:** Workflow'un kendi token'ı `permissions: contents: write` ile **repoya commit
atabildi** (`docs/ORTAM_RAPORU.md` oluştu). Aynı yetki **Release oluşturmak** için de gerekli →
ileride APK'yı Release'e koyabileceğiz. Arena token'ına ihtiyaç yok.

**Log okuma çözümü:** Derleme çıktısını göremesem de CI kendi raporunu repoya yazıyor
(`docs/ORTAM_RAPORU.md`). Teşhis döngüsü: CI ölçer → repoya yazar → ben okurum.

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

→ `compileSdk`/`targetSdk` 37.x seçilebilir; sürümler G2'de derlemeyle doğrulanacak.

### İmzalama anahtarı (üretildi, secret bekliyor)

- RSA 2048, PKCS12, alias `yanlisdefteri`, 30 yıl (2056'ya kadar)
- SHA-256 parmak izi: `FA:13:DE:D7:3C:B4:6E:DE:9E:36:37:47:EE:38:95:F2:B6:37:14:9C:16:57:90:A2:E4:67:78:F1:30:89:DE:54`
- Dosyalar **git dışı** `anahtar/` klasöründe: `yanlis-defteri.p12`, `PAROLA.txt`, `keystore.b64`
  (puantaj: `git status` temiz, klasör `.gitignore`'da)
- Ölçüm: base64 uzunluğu **3404 karakter** (RSA 2048). RSA 4096 olsaydı 5696 olurdu → 2048 tercih edildi.
- Kullanıcıya adım adım talimat: `anahtar/KURULUM_ADIMLARI.md` (git dışı)

**E1 güncel durum:** Secret'ları ben ekleyemiyorum (403). Kullanıcı **yol B**'yi uygulayacak:
GitHub web → Settings → Secrets and variables → Actions → 2 secret ekleme
(`SIGNING_KEYSTORE_BASE64`, `SIGNING_PASSWORD`). Yol A (Arena bağlantı izni) uygulamanın izin
tanımına bağlı olduğu için kullanıcının vermesiyle değişmeyebilir; pratik yol B.

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
- Workflow token'ı repoya **commit atabildi** → `docs/ORTAM_RAPORU.md` CI tarafından üretildi
  (contents:write yetkisi doğrulandı; Release oluşturmak için de aynı yetki gerekir).
- Runner ortamı ölçüldü: Temurin JDK 17 (8/11/21/25 de kurulu), Android SDK (`build-tools` 34→37.0.0,
  `platforms` android-34→37.2), apksigner 0.9, Gradle 9.8.1 hazır, openssl 3.0.13.
- Sandbox yetenekleri: `openssl` var, `java`/`keytool` **yok**; Maven/Gradle depoları **kapalı**.
- Arena token'ı: içerik yazma ✅, workflow dosyası yazma ✅, PR açma/birleştirme ✅;
  workflow dispatch ❌ 403, secrets ❌ 403, Actions ayarları ❌ 403.
- İmzalama anahtarı üretildi ve parola ile açılabildiği doğrulandı (parmak izi okundu).

**Yalnızca incelenen / henüz doğrulanmayan:**

- Aday sürümler (AGP 9.2.x, Kotlin 2.4.x, Compose BOM 2026.09.00, activity-compose 1.13.0,
  lifecycle 2.11.0): kaynaklardan derlendi, **derlemeyle doğrulanmadı** → G2'de ölçülecek.
- APK'nın telefondan kurulup güncellenmesi → G3/G5'te kullanıcı testi.

## Bilinen hatalar / engeller

- **E1 (G1'de çözüldü, kısmi):** Arena token'ında **Actions/Secrets yazma izni yok** (403).
  Buna karşılık **gerek olmadığı ölçüldü**: workflow push ile otomatik çalışıyor ve workflow'un kendi
  token'ı repoya yazabiliyor (Release dâhil). Tek eksik: **secret'ları yalnızca kullanıcı ekleyebilir**
  (web arayüzü, 2 alan). Adımlar: `anahtar/KURULUM_ADIMLARI.md` (git dışı).
- **E2 (kısıt, çözümü yok):** Yapay zekâ ortamında derleme/test yapılamaz; ayrıca **derleme logları
  indirilemiyor** (log sunucusu sandbox'tan erişilemez). Çözüm: CI kendi ölçüm/rapor çıktısını repoya
  yazar (`docs/ORTAM_RAPORU.md` yaklaşımı) → yeni iş akışları da bu deseni kullanacak.
- **E3 (izlenecek):** `hasFragileUserData` davranışı gerçek cihazda doğrulanmadı.
- **E4 (izlenecek):** Android Auto Backup, uygulama başına **25 MB** ile sınırlıdır; fotoğraf arşivi bu
  kotayı ilk günden aşar. Bu nedenle sistem yedeklemesi birincil mekanizma olarak görülmez ve
  `allowBackup=false` planlanır.

## Sıradaki somut adım

**Sıra:**

1. **Kullanıcı** 2 secret'ı GitHub web arayüzünden ekler
   (`SIGNING_KEYSTORE_BASE64`, `SIGNING_PASSWORD`) — `anahtar/KURULUM_ADIMLARI.md`.
2. **Ben** `gh secret list` ile isimleri ölçerim (değerler görünmez, isimler yeter).
3. **G2:** Proje iskeleti + ilk CI derlemesi (arayüzsüz) → sürümler doğrulanır.
4. **G3:** İmzalı APK + Release akışı + parmak izi doğrulaması.

Ayrıntılı sıra: [docs/KODLAMA_PLANI.md](docs/KODLAMA_PLANI.md) §4 (G1…G6).

**Kapananlar:** A1 (uygulama kimliği: `com.keremtulu.questionnote`), G1 (yetki + ortam ölçümü).

## Kullanıcının yapacağı iş (Aşama 1)

**Şimdi (5 dakika):**

- 2 secret'ı GitHub'da ekle: `SIGNING_KEYSTORE_BASE64`, `SIGNING_PASSWORD`
  → adımlar: `anahtar/KURULUM_ADIMLARI.md` (git dışı klasörde).
- İmzalama anahtarını ve parolasını **iki bağımsız yerde** sakla (şifre yöneticisi + Drive).
  Bu, güncellemelerin veriyi koruyarak üzerine kurulabilmesi için şarttır.

**Sonra (APK geldiğinde):**

- İlk APK'yı telefondan indirip kurmak ve 7 ders kartını görmek.
- İkinci APK geldiğinde **kaldırmadan** üzerine kurmak ve çalıştığını bildirmek.
