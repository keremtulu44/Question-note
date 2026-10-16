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
