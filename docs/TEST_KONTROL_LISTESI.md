# Test Kontrol Listesi

## 1.5.1 — sade İlerleme, otomatik doğrulama

[CI `39158221022`](https://github.com/keremtulu44/Question-note/actions/runs/39158221022), kaynak `c25b32bce6c20b0f2c39ff27af6990d13c300074`: **Debug312/0/0/0, Release258/0/0/0**;
APK/imza/sertifika/yayın, API digest/tag ve sekiz native çıktı doğrulandı. Sayılar toplanmaz.

- [x] Ana görünümde tek eylem/metin bütçesi; teknik paneller ve gizli koşullar varsayılan kapalı.
- [x] Doğru kapsamlı filtre, bölüm/geri/saved-state/yenileme; bilinmeyen sayı sıfır olmaz, hedef üstü gerçek sayı korunur.
- [x] %200 eylem tamamen alt menünün üzerinde; 320dp’de beş 48dp+ sekme ve tek satır seçili etiket.
- [x] Sıfır kapsamda dakika yok; tam kapsamda gereksiz aynı ara toplam yok; karşılaştırmanın gerçek 2+2 gün gerekçesi isteğe bağlı.
- [ ] S24 FE’de 1.5.0 üstüne kurulum, ana görünüm/ayrıntı/geri, sistem yazı ölçeği ve gerçek TalkBack doğrulaması.
- [ ] Aşağıdaki fiziksel SAF/süreç ölümü/depolama/frame-p95 ve gölge kapıları hâlâ açık. Native host testleri bunların yerine geçmez.


> **v1.4.1 / P0–P3:** CI `39136991000` **221 testle başarılı**; Release, imza ve asset SHA-256 doğrulandı.
> Beş native Compose çizimi sentetik önizlemedir; fiziksel test değildir. Aşağıdaki fiziksel cihaz satırları otomatik olarak işaretlenmez.


## 2026-10-27 — üst panel ve Bugün (derleme bekliyor)

> Bu bölümdeki otomatik testler **yazıldı**, ancak bu ortamda JDK/Android SDK olmadığı için
> **çalıştırılmadı**. `[ ]` = CI'da koşacak. Hiçbir satır "geçti" sayılmaz.

- [x] **YZ (CI)** Run `40534071050` (commit `52f27e0`): Debug **388/0/0/0**, Release ortak
      **303/0/0/0**; aşağıdaki otomatik satırlar bu koşuda geçti. Fiziksel cihaz satırları açık.
- [ ] **YZ (CI)** `UstPanelArayuzTest.hedefSeriVeTarihGorunurTekDokunuslaIlerlemeyeGider`:
      `1 / 10`, `1 günlük seri`, tr-TR tarih; XP/para/enerji/elmas metni yok; hedef düğmesi
      ≥48dp; tıklama İlerleme yönlendirmesi.
- [ ] **YZ (CI)** `UstPanelArayuzTest.buyukYazidaHedefDugmesiHalaOkunurVeDokunulabilir`:
      2× yazı ölçeğinde hedef düğmesi ≥48dp ve ≥88dp genişlik.
- [ ] **YZ (CI)** Mevcut `IlerlemeSadelikTest`, `PremiumIlerlemeTest`,
      `KirpmaSorulacakVeOgretmenArayuzTest`, `OneridenOdagaAkisTest` regresyonları:
      metin bütçesi, `progress-xp-bar`, `bugun_sorulacaklar_karti`, akış.
- [ ] **YZ (CI)** `BugunUstPanelOnizlemeTest`: üst panel + alt menü + gerçek Bugün kabuğu,
      `docs/ui/today/bugun-light.png` ve `bugun-dark.png` üretimi (6 soru, 3/10 hedef, gecikenler).
- [ ] **KULLANICI (S24 FE)** Üst panel: çentik/durum çubuğu çakışması, tarih/seri/hedef
      okunurluğu, 3 tuşlu navigasyon ve hareket azaltma açık/kapalı.
- [ ] **KULLANICI (S24 FE)** Sekme köklerinde alt menü üstünde ekstra boşluk kalmadığı;
      detay/form ekranlarında panelin görünmediği; Filtreli'de geri düğmesinin yokluğu.
- [ ] **KULLANICI (S24 FE)** Bugün: boş havuz, hata, yükleniyor, hedef tamam ve devam eden
      oturum durumları; `+` düğmesi ve kuyruk satırına dokunma.
- [ ] **KULLANICI (S24 FE)** İlerleme: rütbe madalyonu yayı, rozet halkası, rütbe kutlaması
      9 noktalı yol, rütbe galerisi 15 noktalı yol; TalkBack `stateDescription`.

> Her testte **kim** yaptığı açıkça yazılır. Yapay zekâ, çalıştırmadığı bir testi "geçti" olarak
> işaretleyemez.
>
> - **YZ** → yapay zekâ tarafından gerçekten çalıştırılan/otomatik test veya kod incelemesi
> - **KULLANICI** → gerçek cihazda (Samsung Galaxy S24 FE) kullanıcı tarafından yapılacak test
> - Durum: `[ ]` bekliyor · `[x]` geçti · `[!]` başarısız · `[-]` uygulanamaz
>
> Test yaklaşımı: hızlı geri bildirim için **JVM birim testleri** (aralık hesabı, yedek doğrulama,
> ZIP yol güvenliği) ve **Robolectric** (şema/migration testleri). Emülatör yalnızca gerçekten
> gerekirse kullanılır; asıl kabul testleri gerçek cihazda yapılır.

Son güncelleme: 2026-10-18. **v1.4.1 (versionCode 17)** yayımlandı. CI `39136991000`: 221 test,
0 başarısız/hata/atlanan; Release/apksigner/sertifika ve asset SHA-256 eşleşmesi doğrulandı.
Fiziksel cihaz kabulü henüz tamamlanmadı. Aşağıdaki tarihsel kullanıcı işaretleri korunur;
yeni testlerin yeşil olması geçmişteki cihaz bekleyenlerini tamamlanmış yapmaz.


---

## P4–P6 — otomatik kanıt ve ayrı fiziksel kabul

Yayımlanan sürüm v1.5.0/code18. P4 CI `39139377311` 240/0, P5 `39140443680` 274/0;
P6 **son yayın CI `39144684877`: Debug298/0/0/0 + Release258/0/0/0**, APK/imza/sertifika başarılı. Nihai yayın, tag/source ve GitHub asset digest CI raporuyla eşleşti. Ayrı varyantlar aynı ortak
testleri tekrar eder; sayılar toplanmaz. [Koşu/başarısızlık günlüğü](P4_P5_P6_UYGULAMA.md).

| Kontrol | Kanıt / sorumlu | Durum |
|---|---|---|
| V2 korunur; gölge aday, süre+güçlük birlikte, eksik veri fallback, çift indirim yok, ≤%10 gerçek indirim | P4 saf/Room, son Debug 298/0 | [x] |
| Desteklenen 1/2/3 geçmişi, iki bağımsız günle toparlanma, bilinmeyen sürüm fallback, önizleme/save eşitliği | P4 saf/Room | [x] |
| Tarihsel kaynak/konu; doğru payda, eksik/bilinmeyen bağlam, gerçek ritim ve aynı-soru/gün karşılaştırması | P5 domain/Room + Debug UI | [x] |
| Plan gerçek; tüm-sonuç iş yükü, %80 kapısı, sıfır kapsamada dakika yok | P5 saf + P6 UI | [x] |
| Öneri → Filtreli → Odak → Karne → İlerleme; askı/ders arşivi/profil/gerçek same-day dışlama | P6 gerçek shell + Room, Debug | [x] |
| Kalıcı birleşik rütbe, sessiz startup/hedef/restore, eski rozet sahipliği; tek fullscreen host | P5 prefs/Room + P6 UI | [x] |
| Snapshot2 gölge/etkin3 ZIP turu, gelecek v2'ye rollback, doğru hash'li semantik bozulma reddi | P6 ortak ZIP, Debug | [x] |
| Restore callback kesintisi ve anlık recovery boyunca marker korunur; kozmetik kuyruklar önce temizlenir | P6 ortak ZIP, Debug; simülasyon | [x] |
| Tanı: toplam ≠ örnek, en yeni ≤100, bozuk ayrı, silmede pencere yenilenir; batched relation, N+1 yok | P6 Room, Debug | [x] |
| Time/timezone broadcast, bir sonraki gece yarısı, Activity resume gün yenilemesi | P6 Robolectric; cihaz değildir | [x] |
| Açık/koyu, %200, hareket kapalı, ≥48dp eylem, gizli maske/semantik; native bileşen çizimleri | Debug-only Compose | [x] |
| 1.000 soru/20.000 tekrar, dört ince SELECT, EXPLAIN indeksleri ve ham host süre raporu | `PERFORMANS_DEBUG.md` | [x] |
| Ortak testlerin ayrıca **Release** derlenmesi/çalışması ve Release benchmark | CI `39143916007` / Release258/0 + rapor | [x] |
| Nihai release derlemesi, imza/sertifika ve yayımlanan asset SHA-256 eşleşmesi | CI `39144684877` + Release/tag/asset API kontrolü | [x] |

### P6 fiziksel kapıları — otomatik test sonucu ile işaretlenmez

| # | Gerçek cihaz senaryosu / beklenen | Sorumlu | Durum |
|---|---|---|---|
| P6.D1 | ZIP yedek al; v1.4.1 üzerine kaldırmadan güncelle. Soru/görsel/tekrar/tarih ve sahiplik korunur; ilk açılış kutlama yağmuru üretmez | S24 FE | [ ] |
| P6.D2 | Gölge/Kapalı seçiminde sonraki plan v2; Etkin düğmesi kapalı. Tanı örnek/total/fark ayrımı dürüst, yenileme çalışır | S24 FE | [ ] |
| P6.D3 | Gerçek soru/çözüm fotoğrafı; deneme → çözüm → değerlendirme. Zoom/mola/kilit/arka plan süreye eklenmez; çözüm gizlenince bağımsızlığa dönmez | S24 FE | [ ] |
| P6.D4 | Sona bırak/döndür/uygulamayı öldür/reboot; doğru soru/faz ve kısmi süre kurtulur, aynı gün ikinci tekrar/XP yok | S24 FE; gerçek süreç | [ ] |
| P6.D5 | Öneriden kaynak/konu/vade filtresi; LGS/TYT/Tümü değişimi; askı/arşiv/same-day sınırı; uygun soru yoksa boş eylem | S24 FE | [ ] |
| P6.D6 | Gece yarısı açık ekran ve arka plandan ertesi gün dönüş; saat/saat dilimi değişimi; önizleme değişince açık yeniden onay | S24 FE | [ ] |
| P6.D7 | Açık/koyu, yatay/dikey, %200 yazı, jest/üç düğme, azaltılmış hareket. Sistem çubuğu çakışmaz, yatay taşma/ulaşılamayan eylem yok | S24 FE | [ ] |
| P6.D8 | TalkBack başlık/eylem/örneklem/kapsama okuma sırası, büyük butonlar, gizli rozet maskesi ve dialog odağı/kapatma | Gerçek TalkBack | [ ] |
| P6.D9 | Aynı ilerlemede çok rozet+rütbe: sırayla tek fullscreen; kapat/aç replay yok; hedef değişimi/restore toplu kutlama yapmaz | S24 FE | [ ] |
| P6.D10 | Gerçek SAF/Drive ZIP dışa/içe aktarımı, bulut yüklemesini sağlayıcıda doğrulama; kesinti/bozuk arşiv eski veriyi korur | Cihaz/sağlayıcı | [ ] |
| P6.D11 | Gerçek düşük depolamada kayıt/ZIP/kalıcı prefs hatası açıkça görünür; yanlış başarı/boş yedek yok, recovery marker korunur | Cihaz; güvenlik yedeğiyle | [ ] |
| P6.D12 | 1.000 soru/20.000 tekrar sentetik arşiv: analitik ilk/sıcak yükleme, kaydırma frame-p95/jank (60Hz bütçesi16,7ms), kaydetme gecikmesi; S24 FE + mümkünse düşük güçlü cihaz | Gerçek cihaz profilleme | [ ] |
| P6.D13 | Gerçek yerel gölge fark/nedenleri, eksik veri/fail/gecikme/yuvarlama ve sonraki farklı-gün denemeleri incelenir; fayda sayısı uydurulmaz | Uzunlamasına gölge kabulü | [ ] |

**D12 host benchmark değildir; D13 100 kayıt sayacıyla otomatik açılmaz. Etkin v3'ün iki kod kapısı kapalı kalır.**

## P2/P3 — ölçüm, uyumluluk ve kişisel referans

| Test | Sorumlu | Durum |
|---|---|---|
| 157-test tabanı + 64 yeni = 221/0/0/0; Release, imza ve asset digest eşleşmesi | YZ / CI `39136991000` | [x] |
| Tick olmadan monotonik toplam; mola/loading/arka plan; 2 faz, değerlendirmede donma, tam ekran kısmi | YZ / saf saat testleri | [x] |
| Döndürme benzetimi, sona bırakma, süreç/reboot benzetimi; eski başlangıç kullanılmaz, bilinmeyen segment eklenmez | YZ / saf model + prefs | [x] |
| Aynı deneme retry, aynı gün, çelişen ID; kayıt/ölçüm/plan rollback ve yeniden deneme | YZ / Room | [x] |
| Gün, saat dilimi, yazma sırasında saat atlaması, bağlam/profil değişiminde açık önizleme yenileme | YZ / enjekte Clock + Room | [x] |
| İlk checkpoint yokken/duvar saati geriyken oturum ID'siyle commit kurtarma; başka oturumu sahiplenmeme | YZ / oturum modeli | [x] |
| En güncel çözüm işareti yazılmadan içerik açılmaz; eski/yanlış owner veya superseded yazı izin değildir | YZ / prefs + Compose | [x] |
| Gerçek v3 schema JSON'u ile 3→4; mevcut 1/2→4 zinciri; eski tarih/sonuç/süre-null korunması | YZ / Room migration | [x] |
| Gün/bağlam sorgusu planında yeni indeksler; soru silinince ölçüm cascade ve profilsiz cache | YZ / SQLite + Room | [x] |
| ZIP v2/v3 import, v4 kalite/bağlam/karar tam tur; CSV formül koruması; checksum geçerli olsa da bozuk ölçüm reddi | YZ / ZIP testleri | [x] |
| Negatif/taşmış/metin süre, bilinmeyen kalite/sürüm, yetim/çift deneme, bozuk/büyük snapshot | YZ / ZIP testleri | [x] |
| Mevcut deneme hariç; 20/10/5 ve 30/15/7; tek soru/gün; uç değer, MAD=0, soru başına ağırlık | YZ / saf profil | [x] |
| Yardımsız/iş yükü ayrımı, eksik/kısmi/sıfır hariç, 60 gün, dersler karışmaz, topic/source fallback | YZ / saf profil + Room | [x] |
| Konu/kaynak değişince eski snapshot etiketi korunur; eski kayıtta hayalî süre/bağlam yok | YZ / Room | [x] |
| Boş/az verili profil; %200 koyu; gerçek Odak → dondur → seçim → atomik ölçüm kaydı | YZ / Compose + Room | [x] |
| v1.4.1'i kaldırmadan güncelle; eski arşiv/rozetler ve yarım oturum gerçek telefonda korunur | KULLANICI / S24 FE | [ ] |
| Mola, kilit, arka plan, bildirim perdesi/çoklu pencere, döndürme; süre/faz sınırları | KULLANICI / S24 FE | [ ] |
| Gerçek süreç öldürme/reboot; son bilinmeyen bölüm kısmi, çözüm işareti ve commit kurtarması | KULLANICI / S24 FE | [ ] |
| Tam ekran zoom, TalkBack, yatay/%200 yazı, jest/3 düğmeli nav | KULLANICI / S24 FE | [ ] |
| Gerçek düşük depolama, başarısız kayıt/restore; SAF/bulut aktarımı | KULLANICI / S24 FE | [ ] |
| 1.000 soru/20.000 tekrar ölçeğinde gecikme/bellek/frame ölçümü | YZ + KULLANICI / henüz çalıştırılmadı | [ ] |

Ana v1.4.0 paketi `39135928734` ile 217/0; son sertleştirme v1.4.1 ile 221/0 doğrulandı.
Sanal saat ve Robolectric süreci gerçek cihaz/süreç öldürme testi diye sunulmaz. P4 v3/gölge
karar ve P5 kaynak/konu panosu/öneri eylemleri bu teslimde yoktur.

## P0/P1 — yeni regresyonlar ve ayrı cihaz kabulü

| Test | Sorumlu | Durum |
|---|---|---|
| Eski 130 test + 27 yeni; 157/0/0/0; Release, imza ve GitHub digest eşleşmesi | YZ / CI `39131225744` | [x] |
| Canlı karne rozet toplamı; aynı sahiplik snapshot'ından XP/galeri | YZ / Room + Compose | [x] |
| Akışın geçici/kalıcı hatası, tekrar sınırı, yeniden abonelik, cancellation/downstream ayrımı | YZ / Flow testleri | [x] |
| Hedef değişince tekrar eklemeden XP ve rozet yenileme | YZ / Room + Flow | [x] |
| Precommit hata/rollback devamı korur; commit aynı ID'lerle bile temizler | YZ / restore testleri | [x] |
| Prefs temizleme hatası işaretleri korur; startup recovery güvenli temizliği tekrarlar | YZ / restore testleri | [x] |
| Boş arşiv/tekrarsız arşiv, gerçek eylem, loading/hata/yenileme, gizli maskeleme | YZ / Compose | [x] |
| Normal ilk girişte gerçek XP; değişiklik sonlu, hareket kapalı güncelleme animasyon beklemez | YZ / Compose sanal zaman | [x] |
| Açık/koyu, %200 yazı, 5 native örnek veri çizimi ve 6 Studio Preview tanımı | YZ / Robolectric + kaynak | [x] |
| v1.3.0'ı kaldırmadan güncelle; mevcut soru/tekrar/rozetler ve yarım oturum korunur | KULLANICI / S24 FE | [ ] |
| Gerçek 3-düğmeli/jest nav, yatay ekran, %200 yazı, TalkBack, büyük arşivde kaydırma | KULLANICI / S24 FE | [ ] |
| Sistem animasyon ayarını açık ekranda değiştir; XP/karne/kutlama/otomatik kaydırmayı kontrol et | KULLANICI / S24 FE | [ ] |
| Restore sırasında kesinti, devam noktası, arka plan/süreç ölümü; gerçek performans | KULLANICI / S24 FE | [ ] |

Bu tarihsel P0/P1 tablosu ölçüm/kişisel profil kabulü değildir; bunlar yukarıdaki P2/P3 tablosunda
izlenir. V3 motor ve kaynak/konu karşılaştırma panosu hâlâ ileriki paketlerdir. Önizlemeler: [ui/p1](ui/p1/README.md). Aşağıdaki tarihsel cihaz
kayıtlarının işaretleri korunur; bu sürümün otomatik testleri onları tamamlamış saymaz.

## Aşama 1 — Derleme ve güncelleme zinciri

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| 1.1 | Actions workflow'u yazılabiliyor ve push ile otomatik çalışıyor | YZ | [x] |
| 1.2 | G2 arayüzsüz `assembleRelease` derlemesi başarılı | YZ | [x] |
| 1.3 | v1.0.0/v1.0.1/v1.0.2/v1.0.3 imzalı APK Release'e eklendi; her sürümde sertifika parmak izi eşleşti | YZ | [x] |
| 1.4 | Yayımlanan her Release notunda sürüm, commit kimliği ve APK SHA-256 bulunuyor | YZ | [x] |
| 1.5 | v1.0.0 APK telefona indirilip kuruluyor ve açılıyor | KULLANICI | [x] |
| 1.6 | v1.0.1 güncellemesinden sonra uygulama açılıyor ve 7 ders kartı görünüyor | KULLANICI | [x] |
| 1.7 | v1.0.1, v1.0.0 üzerine **uygulamayı kaldırmadan** kuruluyor | KULLANICI | [x] |
| 1.8 | CI derleme özeti, hata çıktısı ve APK SHA-256 repoda okunabiliyor | YZ | [x] |
| 1.9 | VersionCode 1 → 2 artışı ve v1.0.1 Release CI'da doğrulandı | YZ | [x] |
| 1.10 | VersionCode 2 → 3; v1.0.2 Release, APK imzası ve fingerprint CI'da doğrulandı | YZ (CI run `39027221270`) | [x] |
| 1.11 | v1.0.2 mevcut kurulumun üzerine yüklendi; kullanıcı uygulamanın açıldığını bildirdi | KULLANICI (2026-10-17) | [x] |
| 1.12 | v1.0.3 mevcut uygulamanın üzerine kuruldu; kullanıcı açılışı, önceki verilerin korunmasını, Bugün ekranı ve menü adımlarını geçti bildirdi | KULLANICI (2026-10-17) | [x] |
| 1.13 | VersionCode 3 → 4; v1.0.3 Release, APK imzası ve fingerprint CI'da doğrulandı | YZ (CI run `39030667492`) | [x] |
| 1.14 | v1.0.4 mevcut uygulamanın üzerine yükleniyor; veri ve tekrar geçmişi korunuyor/görünüyor | KULLANICI | [ ] |
| 1.15 | VersionCode 4 → 5; v1.0.4 Release, APK imzası ve fingerprint CI'da doğrulandı | YZ (CI run `39033698686`) | [x] |
| 1.16 | VersionCode 5 → 6; v1.0.5 Release ve imza doğrulaması | YZ (CI run `39037429421`) | [x] |
| 1.17 | v1.0.5 mevcut uygulamanın üstüne kaldırmadan kurulur; eski soru/not/veri kaybolmaz | KULLANICI | [ ] |
| 1.18 | VersionCode 6 → 7; v1.0.6 Release, APK imzası ve fingerprint CI'da doğrulandı | YZ (CI run `39039791751`) | [x] |
| 1.19 | v1.0.6 mevcut uygulamanın üstüne kaldırmadan kurulur; önceki veriler korunur | KULLANICI | [ ] |
| 1.20 | VersionCode 7 → 8; v1.0.7 Release, APK imzası ve fingerprint CI'da doğrulandı | YZ (CI run `39041684003`) | [x] |
| 1.21 | v1.0.7 mevcut uygulamanın üstüne kaldırmadan kurulur; önceki veriler korunur | KULLANICI | [ ] |
| 1.22 | VersionCode 8 → 9; v1.0.8 Release, APK imzası ve fingerprint CI'da doğrulandı | YZ (CI run `39044777998`) | [x] |
| 1.23 | v1.0.8 mevcut uygulamanın üstüne kaldırmadan kurulur; önceki veriler korunur | KULLANICI | [ ] |
| 1.24 | VersionCode 9 → 10; v1.0.9 Release, APK imzası ve fingerprint CI'da doğrulandı | YZ (CI run `39048703584`) | [x] |
| 1.26 | VersionCode 10 → 11; v1.0.10 Release, APK imzası ve fingerprint CI'da doğrulandı | YZ (CI run `39051284547`) | [x] |
| 1.28 | VersionCode 11 → 12; v1.1.0 Release, APK imzası ve fingerprint CI'da doğrulandı | YZ (CI run `39053319900`) | [x] |
| 1.25 | v1.0.9 mevcut uygulamanın üstüne kaldırmadan kurulur; önceki veriler korunur | KULLANICI | [x] |
| 1.27 | v1.0.10 mevcut uygulamanın üstüne kaldırmadan kurulur; önceki veriler korunur | KULLANICI | [ ] |
| 1.29 | v1.1.x mevcut uygulamanın üstüne kaldırmadan kurulur; önceki veriler korunur | KULLANICI | [ ] |

## Aşama 2 — Yerel arşiv

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| 2.1 | Uçak modunda soru eklenebiliyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.2 | Uygulama kapanıp açıldığında kayıtlar duruyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.3 | Telefon yeniden başlatıldığında veriler duruyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.4 | Galerideki kaynak görsel silinse de soru açılıyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.5 | Kamera/fotoğraf seçme iptal edilince uygulama bozulmuyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.6 | Konusuz soru eklenebiliyor ve "Konusuz" listesinde görünüyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.7 | Soru ve çözüm ayrı gösteriliyor (çözüm varsayılan gizli) | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.8 | Çözüm sonradan eklenebiliyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.9 | Çok görsel: "bir sayfa daha ekle" çalışıyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.10 | Görsel büyütme/yakınlaştırma çalışıyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.11 | Soru düzenleme veri ilişkilerini bozmuyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.12 | Silme davranışı açık ve güvenli (onay diyaloğu, sonuç bildirimi) | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.13 | Ders/konu arşivleme mevcut soruları bozmuyor, yeni kayıtta seçenek olarak çıkmıyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.14 | Görseller kalıcı alanda, önbellekte tek kopya yok (dosya sistemi incelenir) | YZ + KULLANICI | [ ] |
| 2.15 | Yarım kalan işlem bozuk kayıt oluşturmuyor (kod incelemesi + senaryo denemesi) | YZ | [ ] |
| 2.16 | Kullanılmayan dosya temizliği onaysız/yıkıcı biçimde çalışmıyor, ne sildiğini bildiriyor | YZ + KULLANICI | [ ] |
| 2.17 | Eksik görsel (dosya yok) durumunda kayıt kırılmıyor, anlaşılır mesaj gösteriliyor | YZ + KULLANICI | [ ] |
| 2.18 | Sıkıştırma testi: 30–50 gerçek soru fotoğrafında küçük formüller/kök içi ifadeler okunuyor | KULLANICI (v1.0.2 bildirimi 2026-10-17) | [x] |
| 2.19 | Room v3 şeması, v1 fixture'ı v1→v2→v3 zinciriyle ve v2 fixture'ı v2→v3 ile veri kaybetmeden taşınıyor | YZ (CI run `39037429421`, Robolectric) | [x] |
| 2.20 | Yeni veritabanında yedi ders, sabit sırada ve arşivsiz oluşturuluyor | YZ (Robolectric) | [x] |
| 2.21 | Soru/ders/konu yabancı anahtarları ve görsel/geçmiş CASCADE kuralları açık | YZ (Robolectric) | [x] |
| 2.22 | Repository yalnızca etkin dersleri, sabit sırada ve doğru soru sayılarıyla yayımlıyor | YZ (Robolectric) | [x] |
| 2.23 | Dosya tabanlı veritabanı yeniden açılınca yedi seed dersi koruyor, çoğaltmıyor | YZ (Robolectric) | [x] |
| 2.24 | Konu ekleme adı trimler; yinelenen Türkçe büyük/küçük harf varyantını reddeder | YZ (Robolectric) | [x] |
| 2.25 | Konu yeniden adlandırma boş/tekrarlı adı reddeder, geçerli adı ve sırayı korur | YZ (Robolectric) | [x] |
| 2.26 | Konu arşivleme/geri getirme soruyu veya `konu_id` ilişkisini silmez | YZ (Robolectric) | [x] |
| 2.27 | Konu ekle/düzenle/arşivle/geri getir akışı gerçek cihazda anlaşılır ve kalıcı çalışır | KULLANICI | [ ] |
| 2.28 | Ders ekle/yeniden adlandır/sıra/arşivleme ve geri getirme repository testleri | YZ (Robolectric) | [x] |
| 2.29 | Soru oluştur/düzenle/filtrele/arşivle/sil, ders-konu ilişkisi ve tekrar planı testleri | YZ (Robolectric) | [x] |
| 2.30 | JPEG optimizasyonu, EXIF yönü, SHA-256, bozuk/non-image girdi ve geçici dosya yaşı testleri | YZ (Robolectric) | [x] |
| 2.31 | Onaylı yetim temizliği, dosya yaşı ve DB referansını silme öncesi yeniden denetleme testleri | YZ (Robolectric) | [x] |
| 2.32 | Repository Bugün akışı dersler arası gecikmiş/vadesi gelen aktif soruları içeriyor; gelecek tarihli, askıdaki ve arşiv ders sorularını dışlıyor | YZ (CI run `39030667492`) | [x] |
| 2.33 | Yeni Bugün/gecikenler ekranı, ders seçerek soru ekleme ve yan menü adımları gerçek cihazda geçti | KULLANICI (2026-10-17, v1.0.3) | [x] |
| 2.34 | Tekrar sonucu kaydediliyor; uygulama Bugün'e dönüyor ve tamamlanan soru bugünkü listeden çıkıyor | KULLANICI (2026-10-17, v1.0.3) | [x] |
| 2.35 | Tekrar geçmişi boş durumu, newest-first sırası ve planlanan/gerçekleşen gün/sonuç/çözüm alanları repository'den doğru geliyor | YZ (CI run `39033698686`) | [x] |
| 2.36 | v1.0.4 soru ayrıntısında tekrar geçmişi cihazda okunaklı ve güncel gösteriliyor | KULLANICI (v1.0.4) | [ ] |
| 2.37 | Kaynak kataloğu derslerden bağımsız, yeni kaynak bir derste eklenince diğer derste listeleniyor | YZ (CI run `39037429421`) | [x] |
| 2.38 | Her soru için kaynak seçimi ve isteğe bağlı sayfa/test/soru konumu kaydedilip tekrar açılıyor | YZ (CI run `39037429421`) | [x] |
| 2.39 | Hazır/özel yanlış nedeni tek seçim; özel neden katalogda kalıcı ve global | YZ (CI run `39037429421`) | [x] |
| 2.40 | Soru notu form/detay UI'sinde yok; eski not Room ve ZIP yedeğinde korunuyor, düzenleme sırasında silinmiyor | YZ (kod incelemesi + CI run `39037429421`) | [x] |
| 2.41 | v1.0.5 kaynak/katalog akışı ve locator gerçek cihazda anlaşılır, seçilebilir ve kalıcı | KULLANICI | [ ] |
| 2.42 | v1.0.6 ekleme/düzenleme formu; kartlar, kamera/galeri, görsel sıralama/silme ve sabit kaydetme alanı cihazda anlaşılır çalışır | KULLANICI | [ ] |
| 2.43 | Soru formundaki `+ Konu` yalnızca seçili derse ekler; o dersin ve diğer derslerin mevcut konuları korunur | KULLANICI | [ ] |
| 2.44 | Yeni konu ekleme repository testi yedi dersin eski konularını korur; yeni konuyu yalnızca seçilen derse ekler | YZ (CI run `39041684003`) | [x] |
| 2.45 | Filtreli çalışma sorgusu konu/kaynak/neden filtrelerini AND ile birleştirir; ASKIDA/TAMAMLANDI, arşivli ders ve bugün çözülmüş sorular dışlanır | YZ (CI run `39044777998`) | [x] |
| 2.46 | Filtreli oturum sırası en uzun süredir çalışılmayandan başlar; eşitlikte sonraki plan günü/oluşturma/id kullanılır | YZ (CI run `39044777998`) | [x] |
| 2.47 | v1.0.8 filtreli çalışma cihazda: filtre seçimleri, “Kaç soru?”, sıralı ilerleme, sonuç kaydı ve bitiş ekranı | KULLANICI | [ ] |

## Aşama 3 — Yedek ve geri yükleme

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| 3.1 | ZIP v3: manifest/6 CSV/JSON, katalog sayısı ve tüm envanter SHA-256/uzunluk doğrulaması | YZ (CI run `39037429421`) | [x] |
| 3.2 | Yedek cihazdaki bir klasöre kaydediliyor | KULLANICI | [ ] |
| 3.3 | Yedek Drive'a kaydediliyor (dosya seçici akışı) | KULLANICI | [ ] |
| 3.4 | Temiz kurulumda yedekten geri yükleme çalışıyor | KULLANICI | [ ] |
| 3.5 | Yedek görselleri kapsıyor (geri yükleme sonrası görseller açılıyor) | KULLANICI | [ ] |
| 3.6 | Yedek tekrar geçmişini ve plan durumunu kapsıyor | KULLANICI | [ ] |
| 3.7 | Checksum'u bozulmuş ZIP önizleme aşamasında reddediliyor; DB/görsel kayıtları değişmiyor | YZ (CI run `39030667492`) | [x] |
| 3.8 | Desteklenmeyen format sürümü reddediliyor; DB değişmiyor | YZ (CI run `39030667492`) | [x] |
| 3.9 | Başarısız yedek işlemi başarı gibi gösterilmiyor | YZ | [ ] |
| 3.10 | Tam geri yükleme öncesi doğrulanmış güvenlik ZIP'i oluşuyor, eski not/görsel verisi korunuyor | YZ (CI run `39030667492`) | [x] |
| 3.11 | v1.0.3 güncellemesi mevcut verileri koruyor (gerçek cihaz verisiyle) | KULLANICI (2026-10-17) | [x] |
| 3.12 | ZIP içi yol güvenliği: `..` içeren ZIP hiçbir dosya çıkarmadan reddediliyor | YZ (CI run `39030667492`) | [x] |
| 3.13 | Günlük/Room işareti simülasyonunda transaction öncesi rollback ve commit sonrası tamamlatma doğru çalışıyor | YZ (CI run `39030667492`) | [x] |
| 3.14 | ZIP bomb / aşırı büyüme / çok fazla dosya içeren arşiv açılmadan reddediliyor | YZ | [ ] |
| 3.15 | Yedek **başka bir cihazda** (cihaz bağlılığı olmadan) eksiksiz geri yükleniyor | KULLANICI | [ ] |
| 3.16 | Anlamsız/boş yedek seçilince kullanıcı onayı olmadan hiçbir şey değişmiyor | YZ + KULLANICI | [ ] |
| 3.17 | Son yedek bilgisi (tarih, boyut, dosya adı) doğru gösteriliyor | KULLANICI | [ ] |
| 3.18 | Gerçek cihazda geri yükleme sırasında uygulama kapatılır; sonraki açılışın kurtarması doğrulanır | KULLANICI | [ ] |
| 3.19 | ZIP v3 tam geri yüklemesi global kaynak/neden kataloğunu ve soruya özel konumu saklar; eski kataloğu tam değiştirir | YZ (CI run `39037429421`) | [x] |
| 3.20 | Format v2 ZIP'i içe alınır; eski kaynak/özel neden kataloğa eklenir, not korunur, olmayan locator null kalır | YZ (CI run `39037429421`) | [x] |
| 3.21 | Temiz cihazda v3 yedeği geri yüklenince kaynak ve neden seçenekleri sonraki sorularda yeniden kullanılabilir | KULLANICI | [ ] |

## Aşama 4 — Tekrar sistemi

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| 4.1 | "Bugün" listesi vadesi gelen soruları gösteriyor | KULLANICI (2026-10-17, v1.0.3) | [x] |
| 4.2 | Gecikmiş sorular gün sayısı/etiketiyle doğru gösteriliyor | KULLANICI (2026-10-17, v1.0.3) | [x] |
| 4.3 | Birim testinde "Çözemedim" sonrası aralık 1 güne dönüyor | YZ (CI run `39030667492`) | [x] |
| 4.4 | Birim testinde "Rahat çözdüm" sonrası aralık büyüyor ve 180 günde sınırlanıyor | YZ (CI run `39030667492`) | [x] |
| 4.5 | Çözüm açıldığında bu bilgi gerçek cihazdaki tekrar akışında kaydediliyor | KULLANICI (v1.0.3) | [ ] |
| 4.6 | Birim testinde çözüm açılmış tekrarın sonraki tarihi 1 gün sonrası oluyor | YZ (CI run `39030667492`) | [x] |
| 4.7 | Çözüm açıldıktan sonra aynı gün ikinci deneme kaydı oluşmuyor | YZ | [ ] |
| 4.8 | v1.0.4 soru ayrıntısında tekrar geçmişi gösteriliyor (planlanan/gerçekleşen gün ve sonuç) | KULLANICI | [ ] |
| 4.9 | Bildirim izni verilmese de "Bugün" listesi çalışıyor | KULLANICI | [ ] |
| 4.10 | Elle tarih atama çalışıyor | KULLANICI | [ ] |
| 4.11 | Saat dilimi değişiminde gün hesabı bozulmuyor (kayıtlı gün değişmiyor) | YZ + KULLANICI | [ ] |
| 4.12 | `ASKIDA` / `TAMAMLANDI` sorular "Bugün" listesinden çıkıyor, kayıt silinmiyor, geri alınabiliyor | KULLANICI | [ ] |
| 4.13 | Uzun aradan sonra hiçbir soru otomatik sıfırlanmıyor; gecikmişler tek listede toplanıyor | YZ + KULLANICI | [ ] |
| 4.14 | Aralık hesabı unit testleri (kolayda artış/üst sınır, zorlanma/başarısızlık ve çözüm açmada yarına planlama) | YZ (CI run `39030667492`) | [x] |

## Aşama 5 — Dayanıklılık

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| 5.1 | Binlerce kayıtta listeleme kabul edilebilir hızda (ölçüm yapılır, tahmin yazılmaz) | YZ + KULLANICI | [ ] |
| 5.2 | Önizleme stratejisi ölçümle doğrulanıyor (Coil önbelleği yeterli mi) | YZ | [ ] |
| 5.3 | Büyük görseller bellek sorununa yol açmıyor | YZ + KULLANICI | [ ] |
| 5.4 | Depolama kullanımı görüntülenebiliyor (kaç soru/görsel, ne kadar yer) | KULLANICI | [ ] |
| 5.5 | Düşük depolama alanında anlaşılır hata mesajı | KULLANICI | [ ] |
| 5.6 | İzin reddi durumlarında uygulama çökmüyor | KULLANICI | [ ] |
| 5.7 | Geri tuşu beklenen şekilde çalışıyor | KULLANICI | [ ] |
| 5.8 | Sistem çubukları ve ekran boşlukları düzgün (çentik, gezinme çubuğu) | KULLANICI | [ ] |
| 5.9 | Açık/koyu tema okunaklı | KULLANICI | [ ] |

## v1.0.9 düzeltmeleri

> v1.0.9 (versionCode 10) CI run `39048703584` ile derlendi (44 test). **Cihaz kabulü henüz yapılmadı.**

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| D.1 | Ekran döndürüldüğünde açık ekran, filtre seçimleri ve süren oturum korunur; yedek alma kesilmez | KULLANICI | [ ] |
| D.2 | Filtreli oturumdan geri/menüyle çıkışta kalan soru sayısını gösteren onay çıkar; “Devam et” oturumda kalır | KULLANICI | [ ] |
| D.3 | “Kaç soru?” alanı yazılan değeri ezmez; eşleşenden büyük değerde tüm eşleşenler çalışır ve bilgi metni görünür | KULLANICI | [ ] |
| D.4 | Soru ayrıntısında çok sayfalı görseller bellek hatası olmadan açılır; tam ekran yakınlaştırma okunaklı kalır | KULLANICI | [ ] |
| D.5 | Arşivli konuyla aynı ad denenince “arşivden etkinleştir” açıklaması görünür | KULLANICI (CI derlemesi geçti) | [ ] |
| D.6 | Arşivli konu çakışması repository testi geçer (yeni test eklendi) | YZ (CI run `39048703584`) | [x] |
| D.7 | Yedek yazımı başarısız olduğunda hedefte yarım ZIP kalmaz | KULLANICI | [ ] |
| D.8 | CSV'de `=`, `+`, `@` ile başlayan hücreler formülsüz görünür; `data.json` değeri değişmez | YZ (CI run `39048703584`) | [x] |
| D.9 | Soru bulunamadı/hata ekranındaki geri düğmesi “Geri” etiketini kullanır | KULLANICI (CI derlemesi geçti) | [ ] |

## v1.0.10 sonuç karnesi, rozet ve hedef

> v1.0.10 (versionCode 11) CI run `39051284547` ile derlendi (72 test). **Cihaz kabulü henüz yapılmadı.**

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| E.1 | Karne kovaları: RAHAT→bağımsız, ZORLANDIM→yarım, ÇÖZEMEDİM→çözemedi, çözüm açıldı→çözümle; oran yukarı yuvarlanır | YZ (birim testi) | [x] |
| E.2 | Kısa oturum (5'in altı) sadeleşmiş kart gösterir; 5 ve üzeri tam karne | YZ (birim testi) | [x] |
| E.3 | Seri: bugün veya dün başlar; iki gün boşlukta sıfırlanır; hafta penceresi Pzt–Paz | YZ (birim testi) | [x] |
| E.4 | Haftalık fark: geçen hafta <5 tekrarda gizlenir; bu hafta boşsa “—” | YZ (birim testi) | [x] |
| E.5 | Rozet kuralları idempotent; zaten sahip olunan rozet yeniden verilmez | YZ (birim testi) | [x] |
| E.6 | Filtreli oturum tamamlanınca karne otomatik açılır; başlıkta “X sorudan Y tanesini bağımsız çözdün” yazar | KULLANICI | [ ] |
| E.7 | Karne geri düğmesi filtre ekranına, “Bugün” düğmesi Bugün ekranına gider | KULLANICI | [ ] |
| E.8 | “Bu konudan çalış” yalnız o konuyu filtreler, diğer filtreleri temizler ve filtreli çalışmayı açar | KULLANICI | [ ] |
| E.9 | Konusuz/arşivli konu satırında “Bu konudan çalış” düğmesi görünmez | KULLANICI | [ ] |
| E.10 | ≥%70 bağımsız oturumda konfeti bir kez oynar; <%70'te üzgün animasyon yok | KULLANICI | [ ] |
| E.11 | Hareket azaltma açıkken (Ayarlar → Animasyon süresi 0) animasyonlar atlanır, içerik doğrudan görünür | KULLANICI | [ ] |
| E.12 | Yeni rozet karnede en çok 2 adet gösterilir; “Kazandığın rozetler: N/11” satırı doğru | KULLANICI | [ ] |
| E.13 | Bugün ekranında hedef halkası (x/10) ve seri doğru; hedef dolunca metin değişir | KULLANICI | [ ] |
| E.14 | Karne verileri uygulama yeniden açıldığında da tutarlı (tekrarlar tablosundan hesaplanır) | KULLANICI | [ ] |

## v1.1.x alt menü, seviye, ayarlar ve silme

> v1.1.0 (versionCode 12) CI run `39053319900` ile derlendi (86 test). **Cihaz kabulü henüz yapılmadı.**

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| F.1 | Seviye eşikleri ve rütbe seçimi: 100/250/450/700 XP, 9 rütbe, son rütbeden sonra seviye artar | YZ (birim testi) | [x] |
| F.2 | XP kırılımı ve seri bonusu (3/7/30 gün) doğru hesaplanır; geçmiş geriye dönük sayılır | YZ (birim testi) | [x] |
| F.3 | Alt menüde 5 sekme var; yan menü açılmıyor; sekme köklerinde alt menü görünür | KULLANICI | [ ] |
| F.4 | Seviye sekmesinde rütbe amblemi, seviye numarası, XP çubuğu ve kalan XP doğru | KULLANICI | [ ] |
| F.5 | 11 rozetin tamamı galeride görünür; kazanılanlar renkli, kazanılmayanlar soluk/kilitli | KULLANICI | [ ] |
| F.6 | Ders bazında pekişme halkaları ve 7 günlük tekrar yükü grafiği doğru | KULLANICI | [ ] |
| F.7 | Ayarlar'da günlük hedef 5/10/15/20/30 arasında değişir; Bugün halkası yeni hedefi kullanır | KULLANICI | [ ] |
| F.8 | Ayarlar'dan yedek alınır, geri yüklenir; son yedek bilgisi görünür | KULLANICI | [ ] |
| F.9 | Konu silme boş konuda çalışır; kullanımdaki konuda "kullanımda" uyarısı ve arşivleme seçeneği çıkar | KULLANICI | [ ] |
| F.10 | Kaynak/neden silme kullanıcı kaydında çalışır; hazır seçenekte "silinemez" uyarısı çıkar | KULLANICI | [ ] |
| F.11 | Soru formu/ayrıntısı ve filtreli oturumda alt menü gizlenir, geri düğmesi doğru yere döner | KULLANICI | [ ] |
| F.12 | Karne ekranı v1.0.10'daki gibi çalışmaya devam eder; rozet görselleri görünür | KULLANICI | [ ] |

## Genel / sürekli

| # | Test | Sorumlu | Durum |
|---|---|---|---|
| G.1 | Hiçbir gizli anahtar, parola veya token repoda değil | YZ | [x] |
| G.2 | ZIP/APK kullanıcı yedekleri repoya işlenmiyor (`.gitignore` + `git check-ignore`) | YZ | [x] |
| G.3 | İmzalama anahtarının iki ayrı yedek kopyası kullanıcıda mevcut | KULLANICI | [ ] |
| G.4 | Uygulama kaldırılırsa verilerin kaybolacağı kullanıcı tarafından biliniyor | KULLANICI | [ ] |
| G.5 | Room v1→v2 migration ve veri koruması otomatik test edildi; gelecek migration'larda zincir korunacak | YZ (son CI run `39030667492`) | [x] |
| G.6 | Debug ve release APK'ları karıştırılmıyor; günlük kullanımda hep release APK | KULLANICI | [ ] |
| G.7 | Derleme, sabitlenmiş sürümlerle tekrar edilebiliyor (bir yıl sonra da) | YZ | [ ] |

---

## Raporlama kuralı

Her geliştirme adımının sonunda şu ayrım yazılır:

1. **Gerçekten çalıştırılan testler** (komut/çıktı ile),
2. **Yalnızca gözden geçirilenler** (kod okuması, statik değerlendirme),
3. **Kullanıcının cihazında test etmesi gerekenler**.


## v1.2.0 — odak, format, gizli rozet ve sistem çubuğu

| # | Kontrol | Sorumlu | Durum |
|---|---|---|---|
| G.1 | 30 benzersiz rozet, 5 sır, tüm eşikler, idempotent kazanım; 15 rütbe sınırları ve yüksek XP taşması | CI (çekirdek run `39067149944`) | [x] |
| G.2 | Sahiplik + kutlama kuyruğu store yeniden yaratılınca korunur; paralel kazanım çift ödül oluşturmaz | CI | [x] |
| G.3 | LGS altı ders; profil geçişi mevcut soru/konu/ID'yi korur, Tümü eski dersleri gösterir | CI | [x] |
| G.4 | Kullanılan konu/katalog seçeneği silinmez, varsayılan neden korunur; boş kayıt silinebilir | CI | [x] |
| G.5 | Adaptif v2, 1–180 gün sınırı, gecikme, kolaylık; önizleme ve Room planı aynı | CI | [x] |
| G.6 | Bağımsız sorgular yalnız RAHAT + açılmamış çözüm; v2 tekrar ZIP v3 turunda korunur | CI | [x] |
| G.7 | Sona bırakmada çözüm işareti korunur; commit-checkpoint arası kesinti uzlaştırılır, eski/silinmiş kayıtlar doğru ele alınır | CI | [x] |
| G.8 | Compose: büyük yazı + sentetik alt/yatay insets ile beş sekme sistem alanının dışında; dokunma hedefi ≥48 dp (test yoğunluğu 1) | CI UI `39067624050` | [x] |
| G.9 | Compose: gizli galeri/detay gerçek adı ve koşulu sızdırmaz; kazanımda açılır, kapatınca sahiplik kalır | CI UI `39067624050` | [x] |
| G.10 | S24 FE üç düğmeli gezinmede alt menünün tamamı görünür ve tıklanır; sistem çubuğuyla çakışmaz | Cihaz | [ ] |
| G.11 | Jest modu, yatay ekran, büyük yazı, açık/koyu tema; üst sağda Ayarlar tekrarı yok | Cihaz | [ ] |
| G.12 | LGS → TYT → AYT → KPSS → Tümü; ders listesi/bugün/filtreli güncellenir, önceki veriler durur | Cihaz | [ ] |
| G.13 | Ayarlar → Konu yönetimi → ders ara → boş konu sil; kullanılanda uyarı + arşivleme, geri gezinme doğru | Cihaz | [ ] |
| G.14 | 30 rozetin açıklaması/filtreleri, 5 gizlinin maskesi, 15 amblem ve eşik detayı doğru | Cihaz | [ ] |
| G.15 | Yeni rozet tam ekran; birden fazla ödül sırayla, geri/Devam ile kapanır; yeniden açmada tekrar kutlanmaz | Cihaz | [ ] |
| G.16 | Animasyon sistemde kapalıyken kutlama hareket etmez, içerik ve kapatma hâlâ kullanılabilir | Cihaz | [ ] |
| G.17 | Tek soru/5/10/20/özel tur, değerlendirme seçimleri, aralık önizlemesi, ayrı Kaydet, karne | Cihaz | [ ] |
| G.18 | Çözümü aç → sona bırak → geri gelen soruyu Rahat seç: bağımsız bonus almamalı | Cihaz | [ ] |
| G.19 | Mola/arka plan/görselde süre durur; devamda soru ve seçim korunur | Cihaz | [ ] |
| G.20 | Süreç kapat → aç: doğru soruya/karneye dön; aynı günlük tekrar veya XP ikinci kez yazılmasın | Cihaz | [ ] |
| G.21 | v1.1.1 üzerine kaldırmadan v1.2.0 kurulumu ve gerçek ZIP/Drive restore sonrası veri/format/odak kontrolü | Cihaz | [ ] |
