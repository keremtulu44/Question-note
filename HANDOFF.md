# HANDOFF — Yanlış Defteri

> Yeni bir geliştirme oturumu önce [PROJECT_BRIEF.md](PROJECT_BRIEF.md) ve bu belgeyi okur.

- Son güncelleme: 2026-10-16
- Aktif aşama: **Aşama 0 tamamlandı**, Aşama 1 için kullanıcı onayı bekleniyor
- Son başarılı derleme: **yok** (henüz derleme yapılmadı)

---

## En son ne yapıldı?

- Proje brifi kullanıcıyla birlikte değerlendirildi; teknik riskler ve gereksiz karmaşıklıklar konuşuldu.
- Kesinleşen kararlar alındı (bkz. PROJECT_BRIEF §4, §5).
- Aşama 0 belgeleri oluşturuldu: README, PROJECT_BRIEF, ROADMAP, HANDOFF, BUILDING, CHANGELOG ve
  `docs/` altında veri modeli, yedek formatı, test kontrol listesi.
- **Kod yazılmadı.** Depoda uygulama kaynağı, Gradle dosyası veya workflow yok.

## Kullanıcıyla netleşen ek kararlar (Aşama 0 sırasında)

1. Çok görsel desteği hem şemada hem arayüzde olacak.
2. Ders ve konular veritabanında, düzenlenebilir; silme yerine arşivleme.
3. Tekrar değerlendirmesi 3 buton + "çözümü açtım" ayrı işaret.
4. Geri yükleme yalnızca "tamamen değiştir" + otomatik güvenlik kopyası.
5. Sabit imzalama anahtarı Aşama 1'de kurulacak (kullanıcı onayladı).

## Dış inceleme — ikinci görüş (2026-10-16)

Proje, bağımsız bir yapay zekâya teknik inceleme için gönderildi (`docs/FIKIR_ALMA_METNI.md`).
Değerlendirme sonucu: teknoloji seçimi onaylandı; önerilerin bir kısmı kabul, bir kısmı gerekçeyle
reddedildi.

**Kabul edilenler (belgelere işlendi):**

| Öneri | Nereye işlendi |
|---|---|
| Sorunun **plan durumu** (aktif/askıda/tamamlandı) öğrenme düzeyinden ayrılmalı | PROJECT_BRIEF K17, VERI_MODELI §2 |
| Aralık kuralı **sürümlü tek sınıf** olmalı (`algoritma_surumu`) | K18, VERI_MODELI §2/§5 |
| Çözüm açılarak yapılan tekrar bağımsız sayılmamalı (tavan 1–3 gün) + ikinci bağımsız deneme | K19, VERI_MODELI §5 |
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

## Ne test edildi?

**Gerçekten çalıştırılan:** Yok. Bu aşama yalnızca planlama ve belge yazımı içeriyor.

**İncelenen / doğrulanan (salt okuma):**

- Depo: `keremtulu44/Question-note`, **public**, geçmişte tek commit (`Initial commit`), workflow yok.
- Yapay zekâ ortamı: Java/Android SDK **yok**; `maven.google.com`, `repo1.maven.org`,
  `plugins.gradle.org`, `dl.google.com`, `services.gradle.org` erişimi **kapalı** (HTTP 000).
  → Bu ortamda APK derlenemez.
- GitHub bağlantısı: `keremtulu44` olarak kimliği doğrulanmış token mevcut. Ancak
  `GET /repos/.../actions/permissions` isteği **403** döndü; **workflow yazma ve Secret ekleme
  yetkisi doğrulanamadı**.

## Bilinen hatalar / engeller

- **E1 (engel, Aşama 1'i etkiler):** GitHub Actions ve Secret yazma yetkisi doğrulanamadı.
  Aşama 1'in ilk işi bunu denemek. Gerekirse kullanıcıdan repo Ayarlar'ından Actions'ı etkinleştirmesi
  veya Arena'daki GitHub bağlantısına izin eklemesi istenecek.
- **E2 (kısıt, çözümü yok):** Yapay zekâ ortamında derleme/test yapılamaz. Cihaz testleri kullanıcıya ait.
- **E3 (izlenecek):** `hasFragileUserData` davranışı gerçek cihazda doğrulanmadı.
- **E4 (izlenecek):** Android Auto Backup, uygulama başına **25 MB** ile sınırlıdır; fotoğraf arşivi bu
  kotayı ilk günden aşar. Bu nedenle sistem yedeklemesi birincil mekanizma olarak görülmez ve
  `allowBackup=false` planlanır.

## Sıradaki somut adım

**Aşama 1, adım 1:** Kullanıcıdan "Aşama 1'e başla" onayı alındıktan sonra:

1. GitHub Actions + Secret yazma yetkisini dene (küçük bir deneme workflow'u ile).
2. Yetki yoksa kullanıcıya tek ve net bir adım söyle.
3. Yetki varsa: proje iskeleti + imzalama + Release dağıtımı ve ilk APK.

## Kullanıcının yapacağı iş (Aşama 1)

- İmzalama anahtarını ve parolasını **iki ayrı yerde** saklamak (şifre yöneticisi + Drive'da bir dosya).
  Bu, güncellemelerin veriyi koruyarak üzerine kurulabilmesi için şarttır.
- İlk APK'yı telefondan indirip kurmak ve 7 ders kartını görmek.
- İkinci APK geldiğinde **kaldırmadan** üzerine kurmak ve çalıştığını bildirmek.
