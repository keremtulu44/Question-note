# CHANGELOG — Yanlış Defteri

Bu dosya **kullanıcıya yansıyan** değişiklikleri içerir. Henüz yayımlanmış bir sürüm yoktur.

## [Yayımlanmadı]

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
  CI doğrulaması, kullanıcı kontrol listesi (anahtar üretilmedi; Aşama 1 G3'te doldurulacak).
- Ölçülen kısıtlar belgelendi: token'da depo admin'i var ama **Actions Secrets erişimi 403**;
  sandbox'ta Java/keytool yok, **openssl var**; Maven/Gradle depoları erişilemez.

### Aşama 1 / G1 — yetki ve ortam ölçümü — 2026-10-16

- **CI çalışıyor:** ilk iş akışı yazıldı ve push ile otomatik tetiklenerek **iki kez başarıyla** çalıştı.
- **Ölçülen CI ortamı:** Temurin JDK 17 (8/11/21/25 de kurulu), Android SDK (build-tools 34→37.0.0,
  platformlar android-34→37.2), apksigner 0.9, Gradle 9.8.1 hazır kurulu.
- **Workflow token'ının yazma yetkisi doğrulandı** → ileride APK'yı Release olarak yayımlayabilir.
- **Derleme logları indirilemiyor** (log sunucusu erişilemez) → CI artık ölçüm sonucunu repoya yazıyor
  (`docs/ORTAM_RAPORU.md`).
- **İmzalama anahtarı üretildi:** RSA 2048, PKCS12, 30 yıl geçerli, alias `yanlisdefteri`;
  parmak izi `FA:13:DE:D7:…:89:DE:54`. Anahtar git dışı `anahtar/` klasöründe.
- **Kalan tek engel:** 2 secret'ın GitHub'a eklenmesi (yalnızca kullanıcı yapabilir).

### Kullanıcı kararları — 2026-10-16 (ikinci tur)

- **Uygulama kimliği: `com.keremtulu.questionnote`** (A1 kapandı, değiştirilemez).
- **İmzalama secret'ları için yol A seçildi:** Arena'daki GitHub bağlantısına Actions/Secrets yazma
  izni eklenecek; ardından 4 secret komutla eklenecek. İzin gelmezse yol B'ye (web arayüzünden
  ekleme) düşülecek. Ölçüm: `gh secret list` ve `actions/secrets/public-key` şu an **403**.
