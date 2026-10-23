# CHANGELOG — Yanlış Defteri

Bu dosya kullanıcıya yansıyan sürüm değişikliklerini ve aşağıda tarih sırasıyla proje geçmişini içerir.

## [1.5.95] — 2026-10-23

- **Çözüm tahtası kullanılabilirliği:** yatay ekranda soru ve tahta yan yana; dikey ekranda küçük üst simgeden açılan, kaydırılabilir ve ekranı tamamen kaplamayan soru paneli. Soru sayfasına dokununca aynı tahta üzerinde çizim yapılabilir; simgeyle tekrar tahtaya dönülür.
- Boş tahta ve her soru görselinin çizimleri ayrı tutulur; geri al/yinele aktif yüzeye, temizleme bu soru için tüm geçici çizimlere uygulanır. Özgün görseller, soru verileri, tekrar aralığı ve haftalık inceleme davranışı değiştirilmedi.
- Sürüm: `versionCode 25` / `versionName 1.5.95` — v1.5.94 üzerine, aynı uygulama kimliği ve imzalama anahtarıyla güncelleme.

## [1.5.94] — 2026-10-23

- **Kırpma ve çözüm tahtası:** eksik görsel ve küçük kırpma alanı durumları mevcut Türkçe kaynak metinlerle gösterilir; ek yinelenen metin kaynakları kaldırıldı. `whiteboard_question_missing`, çizim alanı açıklamasının yanında tutulur.
- Görsel kırpma özgün dosyayı değiştirmez; beyaz tahta çizimleri geçicidir. Tekrar planı, Room verileri ve yedek biçimi değişmedi.
- Sürüm: `versionCode 24` / `versionName 1.5.94` — v1.5.93 üzerine, aynı uygulama kimliği ve imzalama anahtarıyla güncelleme.

## [1.5.93] — 2026-10-23

- **Görsel kırpma:** soru ve çözüm görselleri ekleme/düzenleme formunda kırpılabilir. Kırpma ayrı bir geçici kopya oluşturur; kaydedilmiş özgün görsel üzerine yazılmaz.
- **Çözüm tahtası:** odaklı çözüm ve soru ayrıntılarından açılan tahta; serbest kalem, çizgi, dikdörtgen, üçgen, silgi, üç çizgi kalınlığı ve altı renk. Geri al/yinele/temizle; yatay ekranda soru ve çalışma alanı yan yana. Soru üstü çizimler ayrı katmandır ve özgün görseli değiştirmez.
- Tekrar aralıkları, haftalık inceleme davranışı, Room verileri ve yedek biçimi değiştirilmedi.
- Sürüm: `versionCode 23` / `versionName 1.5.93` — v1.5.92 üzerine, aynı uygulama kimliği ve imzalama anahtarıyla güncelleme.

## [1.5.92] — 2026-10-22

- **Premium deneyim:** sistem azaltılmış-hareket ayarına uyan ekran geçişleri, kademeli liste girişleri, mikro-etkileşimler, shimmer/iskele yükleme durumları ve başarı parçacıkları; cam/gradient/gölge yüzeyler.
- **Ses ve dokunsal geri bildirim:** kısa UI/başarı sesi kitaplığı genişletildi, stereo yön hissi ve isteğe bağlı Odak ambiyansı eklendi. Farklı dokunsal desenler sistem titreşim ayarına uyar; Ayarlar ve Odak'ta kullanıcı kontrolü vardır.
- **Gezinme ve demo:** alt menüde kaydırma ile sekme geçişi; Ayarlar'da etkileşimleri deneyen Premium demo. Mevcut ekranlar ve çalışma akışları korunur.
- **Veri güvenliği:** veritabanı şeması, çalışma kayıtları, yedekler ve öğrenme kuralları değiştirilmedi; yeni bağımlılık eklenmedi.
- Sürüm: `versionCode 22` / `versionName 1.5.92` — v1.5.91'in üzerine, aynı uygulama kimliği ve imzalama anahtarıyla güncelleme.
- CI `39839941144`: **Debug315/0/0/0 + Release261/0/0/0**; Release APK, `apksigner` ve beklenen sertifika doğrulandı.
- [İmzalı v1.5.92 APK](https://github.com/keremtulu44/Question-note/releases/download/v1.5.92/yanlis-defteri-1.5.92.apk): **12.636.911 bayt**; SHA-256 `5cbc479d31cddfb56499c2978893556195138a1e644bbf6bd4a256374f272071`. Derlenen commit `682986cef4371a9e01f393f27efd21e1e51a09f2`; sertifika SHA-256 `FA:13:DE:D7:3C:B4:6E:DE:9E:36:37:47:EE:38:95:F2:B6:37:14:9C:16:57:90:A2:E4:67:78:F1:30:89:DE:54`.
- Fiziksel cihaz/TalkBack/SAF/frame-p95 kabulü CI testlerinden ayrıdır; henüz yapılmadı.


## [1.5.91] — 2026-10-22

- **Uygulama adı: "Yanlış Defterim"** — launcher etiketi (`app_name`), README başlığı ve Release
  başlıkları bu adı kullanır. `applicationId` değişmez (`com.keremtulu.questionnote`).
- **Uygulama ikonu eklendi** (önce yoktu): petrol-teal defter + kırmızı X, yazısız; beş yoğunlukta
  `mipmap-*` (48→192 px), `android:icon` / `roundIcon` manifest'e bağlandı. Kaynak görsel:
  `assets/ic_launcher_source.png`.

- **"Geri al" kutucuğu:** tekrar değerlendirmesinden sonra altta kısa onay kutucuğu görünür
  ("Tekrar kaydedildi. Sıradaki tekrar: …") ve **Geri al** düğmesi vardır. Geri al yalnızca
  **az önce yapılan kaydı** siler (10 dakika penceresi, en yeni kayıt); plan alanları kayıttan
  önceki hâline döner, eski geçmişe dokunulmaz. XP/karne sayaçları kayıttan türetildiği için
  kendiliğinden güncellenir (kazanılmış rozet/rütbe ödül olarak geri alınmaz).
- **Ses efektleri:** kısa çan sesleri — tekrar kaydı, günlük hedef tamamlandı, rozet/rütbe
  kutlaması. Ayarlar → **Ses ve geri bildirim** ile açılıp kapatılabilir; sistem sessiz/vibrasyon
  modunda çalmaz. Varsayılan açık; yeni bağımlılık yok (dahili SoundPool + gömülü WAV).
- Sürüm: `versionCode 21` / `versionName 1.5.91` — v1.5.1 ve v1.5.90 Release'lerine dokunulmadan
  yeni etiket (1.5.90 bu oturdaki ilk `Geri al` + ses paketiydi).
- CI doğrulaması: Debug315/0/0/0, Release261/0/0/0; APK SHA-256 `b627e1ad6a6eb4e8e9b5da871a459f6d36c879dfbcd119c2a2d4c6bb58343acf`, sertifika `FA:13:DE:D7:3C:B4:6E:DE:9E:36:37:47:EE:38:95:F2:B6:37:14:9C:16:57:90:A2:E4:67:78:F1:30:89:DE:54`.
- [İmzalı v1.5.91 APK](https://github.com/keremtulu44/Question-note/releases/download/v1.5.91/yanlis-defteri-1.5.91.apk): 11.054.007 bayt.
- Not: parmak izi/açılış kilidi kararı **kalıcı olarak reddedildi**; yapılmayacak.

## [1.5.1] — 2026-10-18

- İlerleme ana sayfası rütbe, bugünkü hedef ve tek çalışma eylemine indirildi; uzun açıklamalar ve üst üste analitik kartlar kaldırıldı.
- Emek/XP, ritim, plan, konu/kaynak ve karşılaştırma ayrı isteğe bağlı sayfalarda. Planın süre kapsamı güne dokununca, karşılaştırmanın dayanağı “Neden?” altında açılır.
- Koleksiyon ayrıldı; rozet yönergeleri kartlardan detay penceresine taşındı. Gizli rozet maskelemesi, geri gezinme, büyük yazı ve azaltılmış hareket korunur.
- Yeni veri, puan, şema veya tekrar kuralı yok. 1.5.0 APK/tag değiştirilmedi; etkin v3 kapalı.
- CI `39158221022`: **Debug312/0 + Release258/0**, APK/imza/sertifika/yayın başarılı. Sekiz native görünüm ve yeni sadelik/viewport/etiket regresyonları. Fiziksel cihaz kabulü ayrı.
- [İmzalı 1.5.1 APK](https://github.com/keremtulu44/Question-note/releases/download/v1.5.1/yanlis-defteri-1.5.1.apk): 10.819.807 bayt; SHA-256 `c8d1d45cfd7ad55ab6d3ebba6bf52464fd772d16d23929cbcb406fe8b2a9a053`. API digest ve tag/source `c25b32b` CI ile eşleşti.

## [1.5.0] — 2026-10-18

- P4: v2 tarihini koruyan varsayılan **Gölge** ve açıklanabilir, en çok %10 azaltan aday3; yakın güçlük ve güvenilir uzun süre birlikte gerekir. Hız XP/bonus değildir. Etkin v3 cihaz/gölge kapıları kapalı.
- P5: tarihsel kaynak/konu, soru/tekrar/payda/kapsama, gerçek 7 günlük ritim, tamamlanmış iki hafta ve aynı-soru karşılaştırması. Gün eşiği sağlanmıyorsa gerçek gün sayısı gösterilir; öğrenme/yetenek hükmü yoktur.
- Gerçek uygun 7 günlük plan; tüm sonuçların TAM iş yükünden yaklaşık süre. %80 altında yalnız kapsananların ara toplamı, sıfır kapsamada dakika yok.
- Öneri → görünür filtre → kullanıcı başlatırsa Odak; askı/ders arşivi/profil/same-day uygunluğu yeniden kontrol edilir. Kalıcı, tek birleşik rütbe kuyruğu; rozetle üst üste açılmaz, ek XP yoktur. Startup/hedef/restore sessiz tabandır.
- P6: Ayarlar’da en yeni 100 karar için dürüst tanı, gece yarısı/resume/saat dilimi yenileme; kaynak seti ayrımı, native 1.000 soru/20.000 tekrar host ölçümü ve beş sentetik Compose önizlemesi. Birleşik final CI `39144684877`: Debug298/0 + Release258/0; APK/imza/sertifika/yayın başarılı.
- Room4/ZIP4; snapshot1/2, geçmiş1/2/3 ve v2'ye gelecek kayıt rollback'i. Eski kayıtlar yeniden yazılmaz; XP/15 rütbe/30 rozet katsayıları korunur.
- Fiziksel S24 FE, TalkBack, gerçek süreç ölümü, düşük depolama, SAF ve cihaz frame-p95 kabulü yapılmadı. Etkin v3 açılmadı.

- Yayımlanan APK **10.791.135 bayt**, SHA-256 `3e7158345d0e0c88bf03698b8ba21db1fedef6a123758de5e7b13f8e5a5470db`. CI/API digest ve tag `9c3c51f34aa8419c2cab948601823f6fc58351e3` eşleşti.

## [1.4.1] — 2026-10-18

- Çözüm, açılma işaretinin en güncel devam noktasına kalıcı yazılmasından sonra gösterilir. Geciken eski IO yazısı onay sayılmaz; sahiplik/oturum sınırı korunur.
- İlk soru checkpoint'i yetişmemiş olsa bile commit sonrası kurtarma oturum ID'siyle yapılır; cihazın duvar saati geri alınsa da kayıt kaybolmuş sanılmaz.
- Hızlı seçim/çözüm/kaydetme etkileşimlerinde güncel dondurulmuş istek kontrol edilir. Ölçümsüz ayrıntı ekranı eski bir başka sorunun saveable kimliğini devralmaz.
- Room/ZIP v4 ve tekrar v2 aynı. Ana v1.4.0 paketi CI `39135928734` ile 217/0 doğrulandı; ek güvencelerle son CI `39136991000`: **221/0/0/0**; Release, apksigner ve sertifika başarılı.

- [İmzalı v1.4.1 APK](https://github.com/keremtulu44/Question-note/releases/tag/v1.4.1), 10.651.871 bayt; SHA-256 `9634c5bb59ea574f27c101a8332ed2689719ddf95e31a5a102b6165e48180057`. Yayımlanan asset digest'i CI ile eşleşti. Fiziksel cihaz kabulü bekler.

## [1.4.0] — 2026-10-18

- P2: gerçek monotonik deneme/inceleme ölçümü, dondurulan değerlendirme, sürümlü devam noktası ve idempotent/atomik tekrar kaydı.
- Room v4 ve ZIP v4: eski tarihler/satırlar korunur, v2/v3 yedekler okunur; yeni süre/kalite/kayıt anı bağlamı ve sınırlı karar özeti taşınır.
- P3: 60 günlük, soru başına ağırlıklı medyan/çeyrek/MAD; veri yeterliliği ve ders/konu/kaynak ayrımı. İlerleme'de ölçüm kapsamı ve ders referansı; eski/eksik veriden hayalî süre yok.
- Tekrar v2 ve XP/rozet/rütbe kuralları aynı. P4 karar motoru ve P5 kaynak/konu panosu bu sürüme dahil değil.
- CI `39135928734`: **217 test, 0 başarısız/hata/atlanan**; Release/imza/sertifika doğrulandı. İlk ön kontroldeki test importu ve indeks alanı olmayan eski şema fixture'ı düzeltildi. Fiziksel cihaz kabulü bekler.

## [1.3.0] — 2026-10-18

- **P0/P1:** İlerleme ekranında yeni rütbe odağı, gerçek XP, kompakt sayılar, açılır XP/rozet/rütbe koleksiyonları; boş, yükleniyor, hata ve yeniden deneme durumları.
- Açık/koyu tema ve büyük yazıda uyarlanan yerleşim; ortak, sistem ayarını canlı izleyen azaltılmış hareket politikası. Ekranı açmak sahte XP artışı üretmez.
- Karne rozet sayısı güncel sahiplikten izlenir. Ödül gözleminde üç sınırlı yeniden deneme; tükenirse kullanıcı yenilemesi/sekme dönüşünde yeniden başlama. Hedef değişiklikleri XP ve rozetlere yansır.
- Geri yüklemede yarım oturum önceden silinmez. Başarılı commit veya startup recovery’de devam noktası, kurtarma işaretleri kaldırılmadan geçersizleştirilir; temizleme hatası güvenli recovery sınırında kalır.
- Debug-only Preview’lar ve sentetik Robolectric arayüz/görsel regresyonları. Fiziksel cihaz kabulü yapılmadı; örnek veriler release veri yoluna girmez.
- **Room/ZIP v3, tekrar v2, XP katsayıları ve rozet/rütbe kuralları aynı.** Yeni süre verisi, öğrenme profili veya kaynak analizi eklenmedi.

- **CI `39131225744`: 157 test geçti** (eski 130 + 27 yeni; 0 başarısız/hata/atlanan). Release, apksigner ve beklenen sertifika doğrulandı. İlk iki kontroldeki yeni manuel-saatli UI testi, redraw senkronizasyonu ve sanal zaman sınırlarıyla düzeltildi.
- [İmzalı v1.3.0 APK](https://github.com/keremtulu44/Question-note/releases/tag/v1.3.0), **10.574.047 bayt**; SHA-256 `a232e7759e8f98398449295f32e524565d9d917bb5e3644881908a8f85a96b8f`. GitHub asset digest'i CI ile eşleşti.

## [1.2.0] — 2026-10-17

- **İlerleme büyüdü:** 11 → **30 rozet**, 9 → **15 rütbe**. Altı yeni, saydam WebP amblem; tüm rütbeleri ve XP eşiklerini gösteren yolculuk galerisi. Eski 200-seviye sınırı ve yüksek XP taşma riski kaldırıldı.
- Rozet kartları/detayları kazanma koşulunu ve birikimli ilerlemeyi gösterir. **5 gizli rozetin adı, koşulu ve çizimi kazanılana kadar saklıdır.** Tümü/Kazanılan/Kilitli/Gizli filtreleri eklendi.
- Yeni rozetlerde **tam ekran kutlama**: büyük madalyon, koşul açıklaması, +50 XP ve sonlu konfeti. Kalıcı kutlama kuyruğu kapanma/yeniden açılmada korunur; eski rozetler tekrar verilmez. Sistem hareket ayarına uyulur.
- **Alt menü sistem gezinme çubuğunun üstüne alındı:** gerçek `navigationBars`/çentik inset'leri kullanılır ve iç ekranlarda çift boşluk tüketimi engellenir. Sekmeler esnek genişliklidir. Üst sağdaki Ayarlar kısayolları kaldırıldı.
- **Sınav formatları:** Ayarlar → LGS, TYT, AYT, KPSS (Genel Yetenek/Genel Kültür), Tümü / özel. Eksik dersler eklenir; eski ders, konu, soru ve tekrarlar silinmez. Bugün/Dersler/Filtreli seçili formata uyar.
- **Ayarlar → Konu yönetimi ve silme:** tüm derslerde arama, konu ekleme/düzenleme/arşivleme/silme. Kullanılan konu silinmez; arşivleme alternatifi sunulur. Kaynak/neden korumaları da sürer.
- **Odak Stüdyosu:** Bugün ve Filtreli için tek/sıralı soru akışı, hazır 5/10/20 seçimleri, akıllı/en eski sıra, soru başına aktif süre, mola, görsel yakınlaştırma, sona bırak, çözüm açmadan değerlendirme, seçimden sonra ayrı kaydetme ve otomatik karne.
- **Adaptif tekrar v2:** Zorlandım artık aralığı artırır (ilkinde 2 gün); kolaylık geçmişe göre 130–280 arasında değişir, gecikme hesaba katılır, 4 unutma sonrası destekleyici açıklama gösterilir. Önizleme ile kaydetme aynı kuralı kullanır; otomatik cevap doğrulama yoktur.
- Yarım oturum kapanma sonrası kurtarılır. Room kaydı ile devam noktası arasındaki kesinti uzlaştırılır; çözüm açma işareti sona bırakmada kaybolmaz, aynı gün ikinci tekrar/XP engellenir.
- **Tutarlılık düzeltmesi:** XP/haftalık özet/rozet sorgularında yalnız `RAHAT + çözüm açılmamış` bağımsızdır. `ZORLANDIM` için normal emek XP'si korunur; eski tekrar kayıtları yeniden yazılmaz.
- **Room v3 ve ZIP v3 korunur.** Algoritma durumu mevcut tekrar geçmişinden türetildiği için v4 migration gerekmez. Yeni kayıtların `algoritma_surumu = 2` değeri mevcut yedek biçimiyle taşınır.
- CI `39067624050`: **130 test geçti** (0 başarısız/hata/atlanan); Release, apksigner ve beklenen sertifika doğrulandı. Üç yeni Compose/Robolectric testi sentetik sistem boşluğu/büyük yazı, gizli rozet maskeleme ve kazanım/kapatmayı kapsar.
- [İmzalı v1.2.0 APK](https://github.com/keremtulu44/Question-note/releases/tag/v1.2.0), 10.524.895 bayt; SHA-256 `eb8ab5b3e678a6015c54c7597b8bc93508f82ab3291a6dd06a290c7f4fabcea1` (CI ve GitHub asset digest eşleşti). Fiziksel cihaz gezinme/kutlama/odak kabulü ayrıca açık tutuldu.

## [1.1.1] — 2026-10-17

- Rank görselleri WebP'ye çevrildi ve 96 dp'ye ölçeklendi: drawable boyutu 2.487 KB → 626 KB, APK ~11,8 MB → ~9,9 MB.
- Sürüm numarası dışında v1.1.0 ile aynı özellikler.

## [1.1.0] — 2026-10-17

- **Yan menü kaldırıldı, alt menü geldi:** Bugün · Dersler · Filtreli çalışma · Seviye · Ayarlar. Alt menü yalnız sekme köklerinde görünür; soru formu/ayrıntı ve filtreli oturum tam ekran kalır.
- **Seviye / XP sistemi:** XP gerçek emekten hesaplanır — soru ekleme +10, her tekrar +5, bağımsız çözüm +5, tamamlanan günlük hedef +25, seri kilometre taşları (3 gün +30, 7 gün +70, 30 gün +300), rozet başına +50.
- Seviye eşikleri sonsuz ve artan: 100, 250, 450, 700, 1000 XP… (fark her seviyede +50). Seviye 1'den itibaren 9 rütbe: Başlangıç, Meraklı, Düzenli, Kararlı, Disiplinli, Azimli, Odaklı, Usta, Şampiyon; son rütbeden sonra seviye artmaya devam eder.
- **Rütbe amblemleri uygulama için üretildi**, beyaz arka planı temizlenip saydam logo olarak eklendi (`rank_*.webp`, 5 yoğunluk).
- **Seviye sekmesi:** rütbe amblemi + seviye + XP çubuğu + sonraki seviyeye kalan XP; XP kırılımı; toplam istatistik (soru, tekrar, bağımsız, en uzun seri); **11 rozetin galerisi** (kendi çizdiğimiz rozet görselleri, kazanılan/kilitli); ders bazında pekişme halkaları; 7 günlük tekrar yükü tahmini.
- **Geçmiş geriye dönük hesaplanır:** mevcut sorular ve tekrarlar aynı kurallarla XP'ye çevrilir; kullanıcı 1. seviyeden değil hak ettiği yerden başlar. Ayrı XP tablosu yoktur, her şey `sorular`/`tekrarlar` tablolarından anlık hesaplanır.
- **Ayarlar ekranı:** günlük hedef seçimi (5/10/15/20/30), yedek oluştur/geri yükle + son yedek bilgisi (Drive/Downloads dahil SAF hedefi), görsel bakımı, kaynak/neden yönetimi, sürüm ve veri bilgisi.
- **Silme eklendi:** konu silme (soruda kullanılıyorsa engellenir ve arşivleme önerilir), kaynak/yanlış nedeni silme (kullanımdaysa engellenir; hazır seçenekler silinemez). Hiçbir durumda sessiz veri kaybı yoktur.
- Rozet görselleri karnede de kullanılıyor; rozetler ayrı bir "Rozetler" ekranı yerine Seviye sekmesindeki galeride.
- 14 yeni birim testi (seviye eşikleri, rütbe seçimi, XP kırılımı, seri bonusu, en uzun seri) — toplam **86 test**.

## [1.0.10] — 2026-10-17

- Filtreli oturum bitince **sonuç karnesi** açılır: "%1$d sorudan %2$d tanesini bağımsız çözdün" başlığı, oran halkası ve dört kova (bağımsız / yarım / çözemedi / çözümle). Çözümü açılan soru, RAHAT işaretlense bile bağımsız sayılmaz.
- 5 sorunun altındaki kısa oturumlarda kart sadeleşir ve teşvik edici bir cümle gösterilir; üzgün animasyon hiç kullanılmaz.
- Karnede "En çok şundan" (en az 2 kez geçen yanlış nedenleri, en çok 3 satır), "En çok zorlandığın konular" ve **"Bu konudan çalış"** kısayolu eklendi; kısayol yalnız o konuyu filtreleyip filtreli çalışmayı açar.
- **Haftalık karşılaştırma**: bu hafta (Pzt–Paz) ile önceki takvim haftası; "Bu hafta %85 · geçen hafta %76 (+9 puan)". Geçen hafta 5'ten az tekrar içeriyorsa satır gizlenir ve açıklama gösterilir.
- **Günlük hedef 10 soru** (sabit) ve **gün serisi** karnede ve Bugün ekranında görünür; Bugün ekranına hedef halkası eklendi. Hedef seçimi/ayar ekranı ileri sürümde.
- **11 rozet** eklendi (İlk adım, Kusursuz tur, Onluk, Yirmilik, Kendi başına, İstikrar, Üç gün, Haftalık seri, Otuz gün, Yüz, Beş yüz). Rozetler cihaz-yerel `SharedPreferences` içinde tutulur, ZIP yedeğine girmez; kazanım idempotenttir ve karnede yalnız yeni kazanılanlar (en çok 2) gösterilir.
- Animasyonlar: başlık sayıları 0'dan sayar, halka dolar, ≥%70 bağımsızda tek seferlik konfeti, rozet 0.6→1.0 ölçeklenir. Hareket azaltma açıksa (`ANIMATOR_DURATION_SCALE = 0`) tüm animasyonlar atlanır; yeni bağımlılık yoktur.
- Karne verileri `tekrarlar` tablosundan anlık hesaplanır; **Room şeması değişmedi (v3)** ve yedek biçimi değişmedi.
- 28 yeni birim testi eklendi (karne kovaları/oran/eşikler, seri, hafta penceresi, haftalık fark, rozet kuralları): toplam **72 test**.
- CI run `39051284547`: 72 test, `assembleRelease`, imza/apksigner ve sertifika fingerprint doğrulaması geçti; [v1.0.10 Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.10) yayımlandı.
- CI raporundaki APK SHA-256: `dfb7fdbcceb792e568f4f5d1cf4d5844be138460098ccb325e510cdbb0cfd8a7`. Fiziksel cihaz kabulü bekliyor.

## [1.0.9] — 2026-10-17

- Ekran döndürmede uygulama durumu (açık ekran, filtre seçimleri, süren filtreli oturum) korunur; yedek alma döndürme sırasında yarıda kesilmez.
- Filtreli oturumdan geri/menüyle çıkarken kalan soru sayısını gösteren onay istenir; yanlışlıkla oturum atılmaz.
- “Kaç soru?” alanı kullanıcının yazdığını ezmez; istenen sayı eşleşenden büyükse tüm eşleşenlerin çalışılacağı yazılır ve “Başlat” etkin kalır.
- Soru ayrıntısındaki görseller 1200 px'e küçültülerek çözülür; örnekleme hedefi aşmayacak biçimde düzeltildi (bellek kullanımı düşer, tam ekranda ayrıntı korunur).
- Arşivli bir konuyla aynı adı kullanma denemesinde “arşivden etkinleştir” açıklaması gösterilir.
- Yedek yazımı başarısız olursa hedefteki yarım dosya silinir; geçerli yedek sanılması önlenir.
- ZIP içindeki CSV dosyalarında `=`, `+`, `@` ile başlayan hücreler formül olarak çalışmayacak biçimde nötrleştirilir; `data.json` değişmez.
- Soru bulunamadı/hata ekranındaki geri düğmesi bağlama uygun “Geri” etiketini kullanır.
- Arşivli konu adı çakışması için repository testi ve CSV formül nötrleştirme testi eklendi (toplam 44 test).
- CI run `39048703584`: 44 test, `assembleRelease`, imza/apksigner ve sertifika fingerprint doğrulaması geçti; [v1.0.9 Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.9) yayımlandı.
- CI raporundaki APK SHA-256: `c6f1fd47b1bfae06e77758e91ed33ec69ad81262208e541080477269191b0f38`. Fiziksel cihaz kabulü ayrıca bekliyor.

## [1.0.8] — 2026-10-17

- Yan menüye “Filtreli çalışma” eklendi: konu, kaynak ve ana yanlış nedenine göre seçim; tekrar planında etkin ve bugün çözülmemiş sorular en uzun süredir çalışılmayandan başlayarak sıralı oturumda sunulur.
- Oturum ilerlemesi (`Filtreli çalışma · 1 / N`) ve bitiş ekranı eklendi; filtre seçimleri uygulama kabuğunda tutulduğu için soru açıp dönünce korunur.
- Filtreler AND ile birleşir; “Belirtilmemiş” seçenekleri boş kaynak/neden alanlarını bulur. Sonuç kaydı mevcut tekrar akışını kullanır.
- İlk CI koşusu `39044614950` yanlış `layout.weight` import'uyla derlenemedi; `bef4bd41` düzeltmesinden sonra run `39044777998` geçti: 42 test, `assembleRelease`, imza/apksigner ve sertifika fingerprint doğrulaması başarılı; [v1.0.8 Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.8) yayımlandı.
- CI raporundaki APK SHA-256: `66563dc1b04ffe632ec5285baaefa92fbd9f5a068e8fab2726c4b85480b76b3e`. Fiziksel cihaz kabulü ayrıca bekliyor.

## [1.0.7] — 2026-10-17

- Soru formundaki konu seçicisine kaynak/neden seçicileriyle aynı tarzda `+ Konu` eklendi; yeni konu seçili derse eklenir ve o dersin/diğer derslerin mevcut konu kayıtları korunur.
- Konu ekleme başarılı olunca yeni konu seçilir; yinelenen ad hatası gösterilir ve konu listesi yüklenemezse yeniden deneme sunulur.
- CI run `39041684003`: 37 test, `assembleRelease`, imza/apksigner ve sertifika fingerprint doğrulaması geçti; [v1.0.7 Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.7) yayımlandı.
- CI raporundaki APK SHA-256: `3d10f48d7c6db6ff246d0942fa958df974ac3c87e182b2627855f192035f99c4`. Fiziksel cihaz kabulü ayrıca bekliyor.

## [1.0.6] — 2026-10-17

- Soru ekleme/düzenleme formu; belirgin başlık, numaralı yuvarlak kart bölümleri, daha okunaklı konu/kaynak seçicileri ve kamera/galeri aksiyonlarıyla yenilendi.
- Kaydet düğmesi ekranda sabit kalır; form hatası kaydetme alanında görünür ve klavye açıldığında düğme erişilebilir kalır.
- CI run `39039791751`: 36 test, `assembleRelease`, imza/apksigner ve sertifika fingerprint doğrulaması geçti; [v1.0.6 Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.6) yayımlandı.
- CI raporundaki APK SHA-256: `dde5f837a926bf906e164c202a77a85c6bf2ffadce1c16b98cf8d8c8bbc5332a`. Fiziksel cihaz kabulü ayrıca bekliyor.

## [1.0.5] — 2026-10-17

- Kaynaklar tüm derslerde ortak katalogdan seçilir; kaynak bir kez `+ Kaynak` ile eklenebilir ve her soruya isteğe bağlı sayfa/test/soru konumu yazılır.
- Yanlış nedeni hazır listeden tek seçimdir; `+ Yeni neden` ile özel seçenek eklenebilir.
- Soru notu giriş/gösterim alanı kaldırıldı. Eski not değerleri veritabanında ve ZIP yedeğinde korunur; soru düzenleme bunları silmez.
- Room v3 için v2→v3 veri koruyan migration eklendi. ZIP biçimi v3'e yükseltildi; eski v2 ZIP yedekleri de içe alınır ve eski kaynak/neden metinlerinden katalog oluşturulur.
- CI run `39037429421`: 36 test, Release derlemesi, imza/apksigner ve sertifika parmak izi doğrulaması geçti. [v1.0.5 Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.5).
- CI raporundaki APK SHA-256: `00f0b3b33f1efa81156043ef38a31640f57ce3ba34cf608505eb3164e0a8bedb`. Fiziksel cihaz kabulü ayrıca bekliyor.

## [1.0.4] — 2026-10-17

- Soru ayrıntısına tekrar geçmişi eklendi: en yeni kayıt üstte; gerçekleşen/planlanan gün, sonuç, çözüm açılıp açılmadığı ve aralık görünür.
- Tekrar geçmişi akışı repository üzerinden Room'dan okunur; 33 otomatik test geçti. Bu değişiklik veritabanı şemasını değiştirmez.
- CI run `39033698686`: Release derlemesi, sabit anahtarla imza ve beklenen sertifika parmak izi başarılı; [v1.0.4 Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.4) yayımlandı.
- APK SHA-256: `bf6978a506a472834d7c7a952ac59818d69b0d9c205772d4c02ce2767cc27468`.
- v1.0.4'teki yeni geçmiş arayüzü henüz cihazda test edilmedi. Kullanıcı v1.0.3 Bugün/menü/tekrar adımlarını geçti bildirdi.

## [1.0.3] — 2026-10-17

- Ana ekran sadeleştirilip **Bugün** odağına alındı: tüm derslerden bugünkü ve gecikmiş tekrarlar tek akışta listelenir; “Soru ekle” belirgindir.
- İkincil alanlar (ders/konu gezinmesi, yedekleme/geri yükleme, görsel bakımı) yan menüye taşındı; soru eklerken ders seçimi korunur.
- Soru ayrıntısında çözüm açma ve tekrar sonucu (Çözemedim / Zorlanarak çözdüm / Rahat çözdüm) kaydedilir.
- Mevcut ürün lise içindir; TYT/AYT ayrımı yok. Ortaokul/LGS ancak gelecekteki Ayarlar seçenekleri olarak değerlendirilebilir.
- CI run `39030667492`: 32 test geçti; Release derlemesi, imza ve beklenen sertifika parmak izi doğrulandı. [v1.0.3 Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.3) yayımlandı.
- APK SHA-256: `4641aff60fb38aeb114c765a9721d50849156e9f094b9b99662807cc41724108`.
- Kullanıcı 2026-10-17'de mevcut uygulamayı koruyarak güncelleme, Bugün/gecikenler, menü ve tekrar sonucu adımlarının tümünü geçti bildirdi. Yedek oluşturma/gerçek geri yükleme bu adımlar arasında yoktu; v1.0.4 geçmiş ekranı ayrıca test bekliyor.

## [1.0.2] — 2026-10-17

- Ders/konu yönetimi, soru ekleme-düzenleme-silme, filtre/liste/ayrıntı, çoklu kamera/galeri görselleri ve kalıcı görsel bakımı eklendi.
- Room şeması v1→v2 veri koruyan migration ile güncellendi; migration ve veri akışı testleri eklendi.
- Tam ZIP yedekleme/geri yükleme: JSON + beş CSV + JPEG görseller, SHA-256 doğrulama, SAF hedefi, önizleme/onay, güvenlik kopyası ve kesinti kurtarma eklendi.
- CI run `39027221270`: 31 test, Release derlemesi, imza ve sertifika parmak izi doğrulaması başarılı; [v1.0.2 test Release'i](https://github.com/keremtulu44/Question-note/releases/tag/v1.0.2) yayımlandı.
- APK SHA-256: `264a15082587d3ed4883c783d3d94fea964d0472cf857e802fac1276f77729ac`.
- Cihaz kabulü hakkında kullanıcı “3. turu denemedim, onun dışında hepsi geçti” dedi; bunun bölüm 3 yedek/geri yükleme testinin denenmediği ve diğer checklist bölümlerinin geçtiği anlamına geldiği yorum teyit edilmedi.

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

- Room v1 şeması, ders ekranı ve repository bağlantısına ek olarak konu ekleme, yeniden adlandırma, arşivleme ve geri getirme eklendi. Arşivleme kayıtları/soru ilişkilerini silmiyor; 8 Robolectric testi CI'da geçti (run `38948106275`).
- CI derleme ve imza doğrulaması başarılı; v1.0.1 Release zaten bulunduğundan yeni Release/APK yayımlanmadı. Bu CI kodu gerçek cihazda denenmedi; ders yönetimi, soru/görsel akışları ve dışa aktarma henüz yok.

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
