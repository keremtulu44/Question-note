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

**v1.4.1 yayımlandı** (versionCode 17). [CI `39136991000`](https://github.com/keremtulu44/Question-note/actions/runs/39136991000): **221 test geçti**, Release derlemesi, imza ve sertifika doğrulaması başarılı.

**Geliştirme dalında 1.5.0 adayı:** P4/P5 uygulandı; gölge v3, tarihsel kaynak/konu analitiği,
ritim/iş yükü, öneriden Odak’a geçiş ve kalıcı rütbe kutlaması. P6 Debug **298/0**; Release 258/0 geçti. Son birleşik
CI kapısı doğrulanıyor, aday yayımlanmadı. [Kanıt ve açık kapılar](docs/P4_P5_P6_UYGULAMA.md).

- **P2/P3:** Monotonik deneme/inceleme ölçümü, dondurulan değerlendirme, güvenli devam ve atomik
  tekrar kaydı. Yeterli yeni ölçümde kişisel süre referansı; eski kayıttan hayalî süre yok.
  Ders için 20 ölçüm/10 soru/5 gün birlikte aranır. Profil, tekrar v2 tarihini veya XP'yi değiştirmez.
- **P0/P1:** Yeni petrol/lacivert-bakır İlerleme ekranı; rütbe odağı, gerçek XP, uyarlanan sayılar,
  açılır koleksiyonlar, boş/yükleniyor/hata durumları ve sistem ayarına uyan sonlu hareket.
  Karne sahiplik sayımı, ödül gözlemi ve restore/devam noktası sınırları güçlendirildi.
- **İlerleme:** 30 rozet (5 gizli), kazanma açıklamaları, ilerleme çubukları, 15 rütbe ve
  saydam amblemler. Yeni rozetler tam ekran kutlanır; kutlama kuyruğu kapanmada kaybolmaz.
- **Gezinme:** alt menü Android sistem çubuğu/çentiğine göre yerleşir. Sağ üst Ayarlar kısayolları kaldırıldı.
- **Sınav formatı:** Ayarlar → LGS / TYT / AYT / KPSS / Tümü. İlgili dersler hazırlanır; eski veriler
  silinmez. Ayarlar'da tüm dersleri kapsayan **konu yönetimi ve silme** bulunur.
- **Odak Stüdyosu:** akıllı sıra, 5/10/20 soruluk hazır turlar, mola, sona bırak, kapanma sonrası
  devam ve bir sonraki tekrarın önizlemesi. Çözümü açmak zorunlu değildir.
- **Adaptif tekrar v2:** geçmişten kolaylık hesabı, ilerleyen Zorlandım aralığı, gecikme düzeltmesi
  ve zor soruda destekleyici yönlendirme. Bu öz değerlendirmedir; otomatik cevap doğrulama değildir.
- XP ekleme ve çözme emeğinden gelir. Bağımsız = yalnız **Rahat + çözüm açılmamış**.
- **Room/ZIP v4:** veri koruyan 1/2/3→4 geçişi; v2/v3 yedek okuma sürer. Yeni ölçümler ve kayıt
  bağlamı yedeklenir. Hesap/sunucu/abonelik yok; çevrimdışı çalışır.

**Cihaz kabulü ayrıdır:** v1.0.3 ana akış adımlarını kullanıcı geçti bildirdi. v1.0.4 tekrar geçmişi,
v1.0.5 katalog/ZIP akışı, v1.0.6 form yenilemesi, v1.0.7 ders bazında konu ekleme, v1.0.8 filtreli
çalışma, v1.0.9 düzeltmeleri, v1.0.10 karne/rozet/hedef akışı ve v1.1.x alt menü/seviye/ayarlar/silme
ile v1.2.0 odak/profil/rozet, v1.3.0 P0/P1, v1.4.x P2/P3 ve 1.5.0 P4–P6 yenilemeleri henüz cihazda kabul edilmedi. SAF/Drive ve fotoğraf okunabilirliği de bekliyor. v1.0.2 “3.
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
| [docs/ODAK_STUDYOSU.md](docs/ODAK_STUDYOSU.md) | Adaptif tekrar v2, odak akışı ve oturum kurtarma |
| [docs/VERI_MODELI.md](docs/VERI_MODELI.md) | Veritabanı şeması ve kuralları |
| [docs/SONUC_KARNESI_VE_ILERLEME.md](docs/SONUC_KARNESI_VE_ILERLEME.md) | Sonuç karnesi, kutlama/rozet ve ilerleme tasarımı; tekrar algoritması v2 |
| [docs/YEDEK_FORMATI.md](docs/YEDEK_FORMATI.md) | Yedek ZIP yapısı, manifest, geri yükleme kuralları |
| [docs/TEST_KONTROL_LISTESI.md](docs/TEST_KONTROL_LISTESI.md) | Kabul kriterleri ve test sorumluları |
| [docs/FIKIR_ALMA_METNI.md](docs/FIKIR_ALMA_METNI.md) | Dışarıdan görüş almak için tek başına okunabilen proje tanıtım metni |
| [docs/GELISIM_VE_OGRENME_PLANI.md](docs/GELISIM_VE_OGRENME_PLANI.md) | Yeni geliştirme planı: premium arayüz, süre ölçümü, kişisel profiller, karar motoru ve kabul kapıları (P0–P5 uygulandı; P6 otomatik/cihaz kapıları ayrı) |
| [docs/OLCUM_VE_PROFILLER.md](docs/OLCUM_VE_PROFILLER.md) | P2/P3 süre, kalite, atomik kayıt, kişisel referans ve v4 uyumluluk sözleşmesi |
| [docs/P4_P5_P6_UYGULAMA.md](docs/P4_P5_P6_UYGULAMA.md) | P4–P6 uygulanan motor/analitik/eylem sözleşmesi, CI kanıtı ve etkinleştirme/rollback kapıları |
| [docs/ui/p6/README.md](docs/ui/p6/README.md) | P5/P6 açık/koyu/%200/boş durum native Compose önizlemeleri; yalnız sentetik veri |
| [docs/PERFORMANS_DEBUG.md](docs/PERFORMANS_DEBUG.md) · [Release](docs/PERFORMANS_RELEASE.md) | 1.000 soru/20.000 tekrar için kaynak SHA, ham host süreleri ve gerçek sorgu planları; telefon ölçümü değil |
| [docs/ui/p1/README.md](docs/ui/p1/README.md) | Gerçek Compose bileşenlerinden örnek verili önizlemeler; cihaz testi değildir |
| [docs/KODLAMA_PLANI.md](docs/KODLAMA_PLANI.md) | Kodlama sırası, hedef dosya yapısı, imzalama kurulumu, test stratejisi |
| [docs/IMZALAMA.md](docs/IMZALAMA.md) | İmzalama anahtarı prosedürü ve sertifika parmak izi (değerler değil, parmak izi) |

---

## Kullanım / APK erişimi

İmzalı APK'lar GitHub [Releases](https://github.com/keremtulu44/Question-note/releases) bölümünde.
[v1.4.1 APK'yı indir](https://github.com/keremtulu44/Question-note/releases/download/v1.4.1/yanlis-defteri-1.4.1.apk).

APK: 10.651.871 bayt. SHA-256: `9634c5bb59ea574f27c101a8332ed2689719ddf95e31a5a102b6165e48180057`. CI raporu ile GitHub asset digest eşleşiyor.
Mevcut uygulamayı **kaldırmadan** üzerine kur; eski kayıtların korunduğunu doğrula. Alt menüdeki beş
sekmeyi gez; **İlerleme** sekmesinde rütbe amblemini, XP çubuğunu, rozet galerisini ve tahmini kontrol et.
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
