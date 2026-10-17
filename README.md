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

**v1.0.9 (versionCode 10) yayımlandı.** v1.0.8'in filtreli çalışması korunur; bu sürüm
döndürmede durum koruma, filtreli oturumdan çıkış onayı, "Kaç soru?" alanı, görsel çözme belleği,
arşivli konu adı çakışması, yarım yedek dosyası, CSV formül nötrleştirmesi ve bağlama uygun geri
etiketi düzeltmelerini getirir. v1.0.7 `+ Konu` akışı, v1.0.6 form yenilemesi, v1.0.5 ortak kaynak
kataloğu/soru başına konum/tek ana yanlış nedeni ve eski not verisini koruyan not UI'si kaldırması da
korunur. Room v3 migration'ı veriyi korur; tam ZIP yedeği v3'tür ve v2 yedeklerini içe alır. Ürün
lise odağındadır; TYT/AYT ayrımı yoktur.

CI run `39048703584` **44 testi** geçti; Release derlemesi, imza doğrulaması ve beklenen sertifika
parmak izi eşleşti. [v1.0.9 Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.9)
CI raporundaki APK SHA-256: `c6f1fd47b1bfae06e77758e91ed33ec69ad81262208e541080477269191b0f38`.

**Cihaz kabulü ayrıdır:** v1.0.3 ana akış adımlarını kullanıcı geçti bildirdi. v1.0.4 tekrar geçmişi,
v1.0.5 katalog/ZIP akışı, v1.0.6 form yenilemesi, v1.0.7 ders bazında konu ekleme, v1.0.8 filtreli
çalışma ve v1.0.9 düzeltmeleri henüz cihazda kabul edilmedi. SAF/Drive ve fotoğraf okunabilirliği de bekliyor. v1.0.2 “3.
turu denemedim” ifadesinin checklist eşlemesi hâlâ teyitsiz; ayrıntı
[TEST_KONTROL_LISTESI.md](docs/TEST_KONTROL_LISTESI.md) içinde.
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
| [docs/SONUC_KARNESI_VE_ILERLEME.md](docs/SONUC_KARNESI_VE_ILERLEME.md) | Sonuç karnesi, kutlama/rozet ve ilerleme tasarımı; tekrar algoritması v2 |
| [docs/YEDEK_FORMATI.md](docs/YEDEK_FORMATI.md) | Yedek ZIP yapısı, manifest, geri yükleme kuralları |
| [docs/TEST_KONTROL_LISTESI.md](docs/TEST_KONTROL_LISTESI.md) | Kabul kriterleri ve test sorumluları |
| [docs/FIKIR_ALMA_METNI.md](docs/FIKIR_ALMA_METNI.md) | Dışarıdan görüş almak için tek başına okunabilen proje tanıtım metni |
| [docs/KODLAMA_PLANI.md](docs/KODLAMA_PLANI.md) | Kodlama sırası, hedef dosya yapısı, imzalama kurulumu, test stratejisi |
| [docs/IMZALAMA.md](docs/IMZALAMA.md) | İmzalama anahtarı prosedürü ve sertifika parmak izi (değerler değil, parmak izi) |

---

## Kullanım / APK erişimi

İmzalı APK'lar GitHub [Releases](https://github.com/keremtulu44/Question-note/releases) bölümünde.
[v1.0.9 APK'yı indir](https://github.com/keremtulu44/Question-note/releases/download/v1.0.9/yanlis-defteri-1.0.9.apk).
Mevcut uygulamayı **kaldırmadan** üzerine kur; eski kayıtların korunduğunu doğrula. Filtreli
çalışmada konu/kaynak/neden seçimini, soru sayısını, sıralı ilerlemeyi ve bitiş ekranını dene.
Ardından `+ Konu`, soru ekleme/düzenleme, kamera/galeri ve v3 yedek/geri yüklemeyi kontrol et.
İndirme/kurulum ayrıntıları [BUILDING.md](BUILDING.md) içinde.

---

## Önemli uyarılar

- Uygulama **kaldırılırsa** telefondaki veriler kaybolur. Bu nedenle düzenli yedek almak gerekir.
- Güncelleme, uygulamayı **kaldırmadan** üzerine kurulur. Aynı imzalama anahtarıyla imzalanmış
  yeni APK, mevcut verileri koruyarak kurulur.
- Yedek, kişisel notlar ve soru fotoğrafları içerir. Yedeğin saklandığı yerin gizliliği
  kullanıcının sorumluluğundadır.
