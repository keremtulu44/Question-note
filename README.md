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

**v1.1.1 (versionCode 13) yayımlandı.** Yan menü kaldırıldı; gezinme artık alt menüde: Bugün ·
Dersler · Filtreli çalışma · **Seviye** · **Ayarlar**. Seviye sekmesi XP tabanlı ilerleme getirir:
soru ekleme +10, her tekrar +5, bağımsız çözüm +5, günlük hedef +25, seri kilometre taşları ve rozet
başına +50 XP. Eşikler sonsuz ve artan (100 · 250 · 450 · 700 · 1000…), 9 rütbe (Başlangıç → Şampiyon).
Amblemler uygulama için üretilip saydam logo olarak eklendi; rozetler Canvas ile çizilir. Seviye
sekmesinde rütbe amblemi, XP çubuğu, XP kırılımı, toplam istatistik, 11 rozet galerisi, ders bazında
pekişme halkaları ve 7 günlük tekrar yükü var. Geçmiş XP'si geriye dönük hesaplanır; ayrı XP tablosu
yoktur. Ayarlar'a günlük hedef seçimi (5/10/15/20/30), yedek al/geri yükle (Drive/Downloads SAF hedefi),
görsel bakımı ve kaynak/neden yönetimi taşındı. **Silme eklendi:** konu ve kaynak/neden kayıtları
kullanımdayken silinmez (uyarı + arşivleme önerisi), hazır seçenekler silinemez. v1.0.10 karne/rozet/
hedef akışı ve önceki sürümler korunur. Room v3 ve yedek biçimi değişmedi.

CI run `39053319900` **86 testi** geçti; Release derlemesi, imza doğrulaması ve beklenen sertifika
parmak izi eşleşti. [v1.1.0 Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.1.0)
v1.1.1 aynı özelliklerle yalnız görsel boyutu küçültür; release bağlantısı aşağıdadır.

**Cihaz kabulü ayrıdır:** v1.0.3 ana akış adımlarını kullanıcı geçti bildirdi. v1.0.4 tekrar geçmişi,
v1.0.5 katalog/ZIP akışı, v1.0.6 form yenilemesi, v1.0.7 ders bazında konu ekleme, v1.0.8 filtreli
çalışma, v1.0.9 düzeltmeleri, v1.0.10 karne/rozet/hedef akışı ve v1.1.x alt menü/seviye/ayarlar/silme
henüz cihazda kabul edilmedi. SAF/Drive ve fotoğraf okunabilirliği de bekliyor. v1.0.2 “3.
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
| [docs/SEVIYE_SISTEMI.md](docs/SEVIYE_SISTEMI.md) | Seviye, rütbe, XP, rozet, Ayarlar ve silme kuralları |
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
[v1.1.1 APK'yı indir](https://github.com/keremtulu44/Question-note/releases/download/v1.1.1/yanlis-defteri-1.1.1.apk).
Mevcut uygulamayı **kaldırmadan** üzerine kur; eski kayıtların korunduğunu doğrula. Alt menüdeki beş
sekmeyi gez; **Seviye** sekmesinde rütbe amblemini, XP çubuğunu, rozet galerisini ve tahmini kontrol et.
**Ayarlar**'da günlük hedefi değiştir, yedek al. Konu ve kaynak/neden silmeyi dene (kullanımdaysa
uyarı çıkmalı). Ardından filtreli bir oturum tamamlayıp karnenin hâlâ çalıştığını doğrula.
İndirme/kurulum ayrıntıları [BUILDING.md](BUILDING.md) içinde.

---

## Önemli uyarılar

- Uygulama **kaldırılırsa** telefondaki veriler kaybolur. Bu nedenle düzenli yedek almak gerekir.
- Güncelleme, uygulamayı **kaldırmadan** üzerine kurulur. Aynı imzalama anahtarıyla imzalanmış
  yeni APK, mevcut verileri koruyarak kurulur.
- Yedek, kişisel notlar ve soru fotoğrafları içerir. Yedeğin saklandığı yerin gizliliği
  kullanıcının sorumluluğundadır.
