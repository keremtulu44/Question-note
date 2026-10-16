# CHANGELOG — Yanlış Defteri

Bu dosya kullanıcıya yansıyan sürüm değişikliklerini ve aşağıda tarih sırasıyla proje geçmişini içerir.

## [1.0.1] — 2026-10-16

- Açık/koyu tema ve 7 ders kartlı ana ekran eklendi: Edebiyat, Matematik, Biyoloji, Kimya, Fizik, Coğrafya, Tarih.
- Bu sürüm arayüz iskeletidir; soru ekleme/veritabanı özellikleri henüz yoktur.
- [GitHub Release v1.0.1](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.1) — versionCode 2.

## [1.0.0] — 2026-10-16

- Sabit anahtarla imzalanmış ilk kurulabilir iskelet sürümü; uygulama ekranı bilerek boştur.
- CI, imzayı ve sertifika SHA-256 parmak izini doğruladı.
- [GitHub Release v1.0.0](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.0) — versionCode 1.

## Proje ve belge geçmişi

### Aşama 2 başlangıç — 2026-10-16 (yayımlanmadı)

- Room v1 şeması, açık ilişkiler, yedi ders seed'i, repository/AppContainer bağlantısı ve Room'dan beslenen ders ana ekranı eklendi; etkin ders soru sayıları da DB sorgusundan geliyor. Beş Robolectric testi CI'da geçti (run `38946633709`).
- CI derleme ve imza doğrulaması başarılı; v1.0.1 Release zaten bulunduğundan yeni Release/APK yayımlanmadı. Bu CI kodu gerçek cihazda denenmedi; konu/soru/görsel akışları ve dışa aktarma henüz yok.

### Belgeler — 2026-10-16

- Proje belgeleri oluşturuldu: README, PROJECT_BRIEF, ROADMAP, HANDOFF, BUILDING, CHANGELOG.
- Ayrıntılı belgeler: veri modeli, yedek formatı, test kontrol listesi (`docs/`).
- Kesinleşen kararlar kayda geçirildi (7 ders, isteğe bağlı konu, çok görsel desteği, çözümün gizli
  tutulması, 3 seçenekli tekrar değerlendirmesi, ZIP yedeği, sabit imzalama anahtarı).
- Uygulama kodu, derleme yapılandırması veya workflow **eklenmedi**; kurulabilir sürüm yok.

### Dış inceleme sonrası belge güncellemeleri — 2026-10-16

- Sorunun **plan durumu** (aktif / askıda / tamamlandı) öğrenme durumundan ayrıldı; bir soruyu tekrar
  planından geçici olarak çıkarmak veya bitirmek mümkün olacak (kayıt silinmez).
- Çözüm açılarak yapılan tekrar **bağımsız sayılmayacak** ve sonraki tekrar kısa tutulacak (bu madde
  aşağıda yeniden düzenlendi — bkz. "Kullanıcı kararları").
- Uzun bir aradan sonra hiçbir soru otomatik sıfırlanmayacak.
- Tekrar aralığı kuralı sürümlü hâle getirildi; ileride algoritma değişse bile geçmiş korunacak.
- Geri yüklemede **kesintiye dayanıklılık** (işlem sürerken uygulama kapanırsa sonraki açılışta
  tamamlama/geri alma) ve **zip bomb / yol kaçışı korumaları** eklendi.
- Şema değişiklikleri için **otomatik migration testleri** plana alındı.
- Release çıktısına sürüm, commit kimliği ve APK SHA-256 bilgisi eklendi.
- Reddedilen öneriler ve gerekçeleri [HANDOFF.md](HANDOFF.md) içinde kayda geçti.

### Kullanıcı kararları — 2026-10-16

- **Görsel kalite: otomatik optimize.** Fotoğraflar kaydedilirken küçültülür (uzun kenar ~3000 px,
  JPEG ~%90); ölçüt okunabilirlik. Depolama tasarrufu sağlar.
- **"Çözümü kapat ve tekrar dene" düğmesi olmayacak.** Çözüm görüldükten sonra aynı gün yapılan
  çözüm gerçek hatırlama değildir; çözüm açılmışsa soru **1 gün sonra** yeniden karşımıza çıkar.
- **Soruyu askıya alma ve "tamamlandı" işaretleme 1. sürümde olacak** (kayıt silinmez, geri alınır).

### Kodlama planı — 2026-10-16

- [docs/KODLAMA_PLANI.md](docs/KODLAMA_PLANI.md) eklendi: kodlama ilkeleri, sürüm sabitleme yöntemi,
  imzalama kurulumu (openssl ile PKCS12), Aşama 1 görev sırası (G1–G6) ve hedef dosya ağacı,
  Aşama 2 paket yapısı ve görev tablosu, test stratejisi, derleme bütçesi, riskler, iş bölümü.
- [docs/IMZALAMA.md](docs/IMZALAMA.md) eklendi: anahtar prosedürü, sertifika parmak izi alanı,
  CI doğrulaması ve kullanıcı kontrol listesi. İlk taslakta anahtar henüz üretilmemişti; sonradan
  fingerprint eklendi ve G3/G4 CI imza doğrulaması başarılı oldu.
- Ölçülen kısıtlar belgelendi: token'da depo admin'i var ama **Actions Secrets erişimi 403**;
  sandbox'ta Java/keytool yok, **openssl var**; Maven/Gradle depoları erişilemez.

### Aşama 1 / G1 — yetki ve ortam ölçümü — 2026-10-16

- **CI çalışıyor:** ilk iş akışı yazıldı ve push ile otomatik tetiklenerek **iki kez başarıyla** çalıştı.
- **Ölçülen CI ortamı:** Temurin JDK 17 (8/11/21/25 de kurulu), Android SDK (build-tools 34→37.0.0,
  platformlar android-34→37.2), apksigner 0.9, Gradle 9.8.1 hazır kurulu.
- **Workflow token'ının yazma yetkisi doğrulandı**; G3/G4'te imzalı v1.0.0 ve v1.0.1 Releases başarıyla yayımlandı.
- **Derleme logları indirilemiyor** (log sunucusu erişilemez) → CI artık ölçüm sonucunu repoya yazıyor
  (`docs/ORTAM_RAPORU.md`).
- **İmzalama anahtarı üretildi:** RSA 2048, PKCS12, 30 yıl geçerli, alias `yanlisdefteri`;
  parmak izi `FA:13:DE:D7:…:89:DE:54`. Anahtar git dışı `anahtar/` klasöründe.
- G1 sırasında secret'lar doğrulanmamıştı; kullanıcı daha sonra iki secret ekledi ve G3 CI imzalama/parmak izi kontrolüyle doğrulandı.

### Kullanıcı kararları — 2026-10-16 (ikinci tur)

- **Uygulama kimliği: `com.keremtulu.questionnote`** (A1 kapandı, değiştirilemez).
- **İmzalama secret'ları:** Arena secret uçları **403** verdi; kullanıcı iki secret'ı web arayüzünden ekledi. G3 CI keystore'u çözerek APK'yı imzaladı ve beklenen sertifika SHA-256 parmak izini doğruladı.

### Aşama 1 / G2 — Android proje iskeleti — 2026-10-16

- Tek modüllü Kotlin + Compose Android projesi, `libs.versions.toml` sürüm kataloğu ve Gradle 9.8.1 wrapper eklendi.
- `applicationId=com.keremtulu.questionnote`, minSdk 26, compile/target API 37 sabitlendi.
- Arayüzsüz `assembleRelease` CI'da başarıyla derlendi. O sürümde APK imzasızdı; imzalı v1.0.0 ve G5 testi daha sonra tamamlandı.

### Aşama 1 / G5 — gerçek cihaz güncelleme testi — 2026-10-16

- Kullanıcı v1.0.0'dan v1.0.1'e uygulamayı kaldırmadan güncellemenin başarılı olduğunu bildirdi.
- Uygulama açıldı ve yedi ders kartı göründü. Bu, kullanıcı tarafından bildirilen cihaz testidir; CI testi değildir.
- Aşama 1'in çıkış koşulu geçti; sıradaki aşama yerel soru arşividir.
