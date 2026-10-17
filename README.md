# Yanlış Defteri

Android telefon için kişisel **dijital yanlış defteri**.

Yanlış yapılan veya tekrar çözülmek istenen soruların fotoğrafı eklenir, ders (isteğe bağlı konu)
atanır ve bu sorular aralıklı tekrar yöntemiyle yeniden çözülür. Çözüm, sorudan **ayrı** tutulur ve
varsayılan olarak gizlidir.

Projenin amacı fotoğraf arşivi olmak değil; **"bugün neyi tekrar çözmeliyim?"** sorusunu kolay
cevaplamak ve çözümü öğrenme sürecinden ayrı saklamaktır.

---

## Temel ilkeler

- **Çevrimdışı öncelikli:** Uygulamanın temel işlevleri internet olmadan çalışır.
- **Yerel veri tek doğruluk kaynağı:** Veriler telefonda tutulur. Uzak sunucu, uzak veritabanı veya
  üyelik sistemi yoktur.
- **Taşınabilir yedek:** Uygulama, tam bir ZIP yedeği üretir; yedek kullanıcı tarafından seçilen
  konuma (ör. cihazdaki bir klasöre veya Drive'a) kaydedilir.
- **Tek kullanıcı:** Çok kullanıcılı yapı, sosyal özellik ve senkronizasyon kapsam dışıdır.
- **Uzun ömür hedefi:** En az 2–3 yıl sorunsuz kullanım için sürüm/göç (migration) disiplini.

---

## Durum

**v1.0.7 (versionCode 8) yayımlandı.** Soru formundaki konu alanına, kaynak/neden seçimleriyle
aynı biçimde `+ Konu` eklendi. Yeni konu yalnızca seçili derse kaydedilir; o dersin eski konuları ve
diğer derslerin konu listeleri korunur. Başarılı eklemeden sonra yeni konu seçilir; aynı addaki konu
ikinci kez oluşturulamaz. v1.0.6 formun numaralı kartlarını, belirgin seçim kontrollerini ve sabit
kaydetme alanını getirmişti. v1.0.5 ortak kaynak kataloğu, soru başına sayfa/test/soru konumu, tek
ana yanlış nedeni ve eski not verisini koruyarak not UI'sinin kaldırılmasını içerir. Room v3
migration'ı veriyi korur; tam ZIP yedeği v3'tür ve v2 yedeklerini içe alır. Ürün lise odağındadır;
TYT/AYT ayrımı yoktur.

CI run `39041684003` **37 testi** geçti; Release derlemesi, imza doğrulaması ve beklenen sertifika
parmak izi eşleşti. [v1.0.7 Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.7)
CI raporundaki APK SHA-256: `3d10f48d7c6db6ff246d0942fa958df974ac3c87e182b2627855f192035f99c4`.

**Cihaz kabulü ayrıdır:** v1.0.3 ana akış adımlarını kullanıcı geçti bildirdi. v1.0.4 tekrar geçmişi,
v1.0.5 katalog/ZIP akışı, v1.0.6 form yenilemesi ve v1.0.7 ders bazında konu ekleme henüz cihazda
kabul edilmedi. SAF/Drive ve fotoğraf okunabilirliği de bekliyor. v1.0.2 “3. turu denemedim”
ifadesinin checklist eşlemesi hâlâ teyitsiz; ayrıntı [TEST_KONTROL_LISTESI.md](docs/TEST_KONTROL_LISTESI.md) içinde.
---

## Belgeler

| Belge | İçerik |
|---|---|
| [PROJECT_BRIEF.md](PROJECT_BRIEF.md) | Kullanıcı ihtiyaçları, kesinleşen kararlar, kapsam dışı, açık sorular |
| [ROADMAP.md](ROADMAP.md) | Aşamalar, tamamlanan ve sıradaki işler |
| [HANDOFF.md](HANDOFF.md) | En son ne yapıldı, ne test edildi, sıradaki somut adım |
| [BUILDING.md](BUILDING.md) | Derleme yöntemi, GitHub Actions, imzalama düzeni, APK'ya erişim |
| [CHANGELOG.md](CHANGELOG.md) | Kullanıcıya yansıyan sürüm değişiklikleri |
| [docs/VERI_MODELI.md](docs/VERI_MODELI.md) | Veritabanı şeması ve kuralları |
| [docs/YEDEK_FORMATI.md](docs/YEDEK_FORMATI.md) | Yedek ZIP yapısı, manifest, geri yükleme kuralları |
| [docs/TEST_KONTROL_LISTESI.md](docs/TEST_KONTROL_LISTESI.md) | Kabul kriterleri ve test sorumluları |
| [docs/FIKIR_ALMA_METNI.md](docs/FIKIR_ALMA_METNI.md) | Dışarıdan görüş almak için tek başına okunabilen proje tanıtım metni |
| [docs/KODLAMA_PLANI.md](docs/KODLAMA_PLANI.md) | Kodlama sırası, hedef dosya yapısı, imzalama kurulumu, test stratejisi |
| [docs/IMZALAMA.md](docs/IMZALAMA.md) | İmzalama anahtarı prosedürü ve sertifika parmak izi (değerler değil, parmak izi) |

---

## Kullanım / APK erişimi

İmzalı APK'lar GitHub [Releases](https://github.com/keremtulu44/Question-note/releases) bölümünde.
[v1.0.7 APK'yı indir](https://github.com/keremtulu44/Question-note/releases/download/v1.0.7/yanlis-defteri-1.0.7.apk).
Mevcut uygulamayı **kaldırmadan** üzerine kur; eski kayıtların korunduğunu, `+ Konu` ile seçili
ders için konu eklenip eski konuların tüm derslerde kaldığını doğrula. Soru ekleme/düzenleme, kamera/
galeri, görsel sıralama ve kaydetmeyi de dene; ardından kaynak/neden seçimini ve v3 yedek/geri
yüklemeyi kontrol et. İndirme/kurulum ayrıntıları
[BUILDING.md](BUILDING.md) içinde.

---

## Önemli uyarılar

- Uygulama **kaldırılırsa** telefondaki veriler kaybolur. Bu nedenle düzenli yedek almak gerekir.
- Güncelleme, uygulamayı **kaldırmadan** üzerine kurulur. Aynı imzalama anahtarıyla imzalanmış
  yeni APK, mevcut verileri koruyarak kurulur.
- Yedek, kişisel notlar ve soru fotoğrafları içerir. Yedeğin saklandığı yerin gizliliği
  kullanıcının sorumluluğundadır.
