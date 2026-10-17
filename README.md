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

**v1.0.4 (versionCode 5) yayımlandı.** Bugün ekranı bugünkü ve gecikmiş tekrarları dersler arası
tek listede toplar; ders seçerek soru ekleme ve yan menü korunur. Soru ayrıntısında tekrar geçmişi,
planlanan/gerçekleşen gün, sonuç, çözüm açma bilgisi ve aralık gösterilir. Ürün lise odağındadır;
TYT/AYT ayrımı yoktur.

CI run `39033698686` (kaynak commit `3d3cfd5d103c1a3f3f578e4e197722d12ab347e2`) 33 testi,
Release derlemesini, imzayı ve beklenen sertifika parmak izini geçti. [v1.0.4 Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.4)
yayımlandı. APK SHA-256: `bf6978a506a472834d7c7a952ac59818d69b0d9c205772d4c02ce2767cc27468`.

**Cihaz kabulü ayrı:** Kullanıcı v1.0.3'te güncelleme, Bugün/menü ve tekrar sonucu adımlarının
tümünü geçti bildirdi. Yeni v1.0.4 tekrar geçmişi cihazda henüz denenmedi; SAF/Drive yedek-geri
yükleme ve fotoğraf okunabilirliği de bekliyor. v1.0.2 için “3. turu denemedim” sözünün checklist
bölümleriyle önceki eşlemesi hâlâ teyitsiz; ayrıntı [TEST_KONTROL_LISTESI.md](docs/TEST_KONTROL_LISTESI.md) içinde.

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
[v1.0.4 APK'yı indir](https://github.com/keremtulu44/Question-note/releases/download/v1.0.4/yanlis-defteri-1.0.4.apk).
Mevcut uygulamayı **kaldırmadan** üzerine kur; özellikle soru ayrıntısındaki yeni tekrar geçmişini
gerçek cihazda kontrol et. İndirme/kurulum ayrıntıları [BUILDING.md](BUILDING.md) içinde.

---

## Önemli uyarılar

- Uygulama **kaldırılırsa** telefondaki veriler kaybolur. Bu nedenle düzenli yedek almak gerekir.
- Güncelleme, uygulamayı **kaldırmadan** üzerine kurulur. Aynı imzalama anahtarıyla imzalanmış
  yeni APK, mevcut verileri koruyarak kurulur.
- Yedek, kişisel notlar ve soru fotoğrafları içerir. Yedeğin saklandığı yerin gizliliği
  kullanıcının sorumluluğundadır.
