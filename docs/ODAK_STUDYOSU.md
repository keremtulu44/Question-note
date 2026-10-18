# Odak Stüdyosu ve güvenli gölge motor — v1.5.x

## Kullanıcı akışı

1. Bugün ekranında **Bugünün oturumuna başla** veya Filtreli ekranda hazır **5/10/20** soru seçimi;
   özel soru sayısı ve mevcut konu/kaynak/neden filtreleri de kullanılabilir.
2. **Akıllı sıra** (varsayılan) veya **En eski önce** seçilir. Bugün/Filtreli listesindeki tek
   soruya dokunmak da aynı odak ekranını bir soruluk oturumla açar.
3. Odak ekranı ders, oturum ilerlemesi, ayrı **deneme/inceleme süreleri**, tamamlanan sayı ve oturum emeği
   XP'sini gösterir. Fotoğrafa dokunarak tam ekran yakınlaştırılır; eski soru notları okunabilir.
4. **Çözümü incele** isteğe bağlıdır. **Cevabımı değerlendir** çözüm açılmasını zorunlu kılmaz.
5. Çözemedim / Zorlandım / Rahat için bir sonraki tekrar aralığı **kaydetmeden önce** görünür.
   Sonuç seçimi tek başına kayıt değildir; **Kaydet ve ilerle** gerekir. Yanlışlıkla dokunmada
   seçenek değiştirmek mümkündür. Kayıt başarısızsa soru ve seçim korunur.
6. **Sona bırak** soru sırasını değiştirir, XP/tekrar oluşturmaz. Çözüm daha önce açılmışsa işaret
   korunur; soruyu sona bırakıp bağımsız bonusunu yeniden elde etmek mümkün değildir.
7. **Mola** süreyi durdurur. Uygulama arka plandayken ve tam ekran görsel açıkken de süre artmaz.
   Tam ekran ölçümü kısmi yapar; referansa katılmaz. Değerlendirme/kaydetmede süre dondurulur.
   Süre puanı veya hız cezası yoktur.
8. Son sorudan sonra mevcut karne açılır. Yeni rozetler tam ekran ve sırayla kutlanır.

Bu bir **öz değerlendirme ve tekrar planlama** sistemidir; fotoğraftaki soruya yapay zekâyla cevap
üretmez ve yanıtın doğruluğunu otomatik saptadığını iddia etmez. "Premium" görsel/akış niteliğidir;
ücret, abonelik, internet veya hesap gerektirmez.

## Sıralama

`CalismaSorulariniSirala.akilliOncelik` deterministiktir:

1. Zamanı gelmiş/gecikmiş sorular;
2. Planlanan tekrar günü (erken olan önce);
3. Aynı günde Öğreniyor → Yeni → Pekişiyor → Sağlam;
4. Son tekrar günü, oluşturma zamanı ve ID ile kararlı eşitlik çözümü.

Arşivli dersler, aktif olmayan soru planları, seçili sınav formatının dışındaki dersler ve bugün
zaten tekrarlanan sorular yeni oturuma alınmaz. Manuel "En eski önce" sırası korunur. Kullanıcının
"Kaç soru?" girişi otomatik değiştirilmez; fazlası istenirse mevcut eşleşmeler kullanılır.

## Algoritma v2

Tek kaynak `domain/repeat/AkilliTekrarKurali.kt`. Önizleme ve Room transaction'ı aynı fonksiyonu
kullanır. Kural sezgiseldir; klinik/akademik olarak kişiselleştirilmiş öğrenme ölçümü iddiası yoktur.

### Tarihçeden hafıza durumu

- Aralık = son geçerli tekrarın `sonraki_aralik` değeri, yoksa **1**.
- Kolaylık başlangıcı **250** (%250); yalnız `algoritma_surumu >= 2` kayıtlarıyla güncellenir.
- Çözüm açılmamış desteklenen v2/v3 kayıtta: Rahat +5, Zorlandım −15, Çözemedim −20. Sınır **130…280**.
- Çözüm açılmışsa kolaylık yargısı verilmez.
- Unutma sayısı = çözüm açılmadan Çözemedim verilen kayıtların sayısı (eski kayıtlar dahil).
  **4 ve üzeri** destekleyici "biraz daha ilgi istiyor" kartını açar; otomatik askıya alma yoktur.

### Yeni aralık

```
çözüm açıldı veya Çözemedim → 1 gün
Zorlandım                   → max(önceki + 1, round(önceki × 1.2))
Rahat                       → round(önceki × kolaylık / 100)

gecikme > 2 × önceki ise başarılı/yarım sonuç aralığı round(yeni × 0.8)
sonuç                       → 1…180 güne sınırla
sonraki tarih               → bugünün yerel takvim tarihi + aralık
```

Tam sayı yuvarlama yarımda yukarıdır. Örnekler: ilk Rahat **3 gün**, ilk Zorlandım **2 gün**,
7 günden kolaylık 250 ile Rahat **18 gün**; 21 günden kolaylık 255 ile 50 gün gecikmiş Rahat
**43 gün**. Çözüm açılan her sonuç ertesi güne gelir. Aynı soru için aynı yerel günde ikinci
kayıt hâlâ engellenir; ne plan ne XP iki kez artar.

### Tarihsel v1.2.0 kararı: neden o aşamada Room v4 gerekmedi?

Önceki tasarım `aralik_gun/kolaylik_yuz/unutma_sayisi` kolonları önermişti. Uygulamada hepsi
zaten tutulan değişmez tekrar geçmişinden hesaplanabildiği için **çift durum saklamak yerine
geçmişten türetme** seçildi. Bu v1.2.0 mimari revizyonudur:

- `TekrarKurali` v1 fonksiyonu ve testleri eski davranışın referansı olarak kalır.
- Eski tekrarlar yeniden yazılmaz. Yeni kayıtlar `algoritma_surumu = 2` kullanır.
- `TekrarKaydi` artık zaten tabloda olan algoritma sürümü ve zaman damgasını domain'e taşır.
- v1.2/v1.3 algoritma durumu için şema değişmemişti. P2 ölçümleri geçmişten türetilemediğinden
  v1.4.x’te **Room/ZIP v4 uygulandı**; v2/v3 yedek okuma korunur. Algoritma hâlâ v2’dir.

## Kapanma, çıkış ve devam

`OdakOturumuDeposu` oturum/soru/deneme ID'leri, konum, sonuçlar, çözüm açma ve soru bazında
birikmiş ölçüm/kalite/seçim bilgisini cihazda tutar; monotonik başlangıç, fotoğraf/not kopyalamaz. Kalıcı tekrarın tek kaynağı Room'dur.

- Aktif ön planda yaklaşık 5 saniyede bir ve geçişlerde IO checkpoint alınır.
- Çözüm gösterilmeden önce açma işareti en güncel snapshot ile kalıcılaştırılır.
- Sonuç + ölçüm + soru planı atomik Room kaydıdır; devam noktası bunun ardından ilerler.
- Uygulama bu iki adım arasında kapanırsa kurtarma deneme/oturum kimliğiyle Room kaydını eşleştirir;
  ilk soru checkpoint'i eksik ve duvar saati geri alınmış olsa da tamamlanan tekrar tanınır.
  Eski kimliksiz kayıtlarda tarih kontrolü sürer. İkinci XP/tekrar yazılmaz.
- Bilinçli çıkıştan sonra Bugün/Filtreli sekmesinde **Yarım kalan oturuma dön** bulunur.
- Süreç yeniden başlarsa devam noktası yüklenir; eksik/silinmiş sorular ayıklanır.
- Bitmiş oturum, kullanıcı karneden ayrılıncaya kadar kurtarılabilir. Bu, kalıcı geçmiş karne
  arşivi değildir; yalnız son oturum devam noktasıdır.
- Bozuk devam noktası uygulamayı çökertmez; soru/tekrar arşivini etkilemeden göz ardı edilir.
- ZIP geri yüklemede (v1.3.0/P0) devam noktası yalnız doğrulanmış commit sonrasında,
  recovery işaretleri kaldırılmadan temizlenir. Commit öncesi başarısız girişim/rollback eski
  devam noktasını korur; temizlik başarısızsa startup recovery sınırı kapanmaz.
  Rozet/hedef/format tercihleri cihaz-yereldir.
- Değerlendirme alanına kaydırma ortak hareket politikasına uyar. P2 ölçer tick değil gerçek
  monotonik farkları toplar; yeni süreçte bilinmeyen zaman eklenmez, ölçüm kısmi olur.
- Gün/saat dilimi/geçmiş/profil değişirse önizleme yenilenir; görünmeyen yeni tarih sessizce kaydedilmez.
- Ölçüm ve P3 profil sözleşmesi: [OLCUM_VE_PROFILLER.md](OLCUM_VE_PROFILLER.md).

## Doğrulama ve sınırlar

Otomatik testler: adaptif eşikler/gecikme/kolaylık/1–180 gün sınırı; önizleme ile yazılan planın
aynılığı; aynı gün kayıt engeli; checkpoint gidiş-dönüş, çözüm işaretinin sona bırakmada korunması,
commit sonrası süreç kaybı; formatta veri koruma; bağımsız çözüm sorgularının karneyle uyumu.

Fiziksel cihazda üç-düğmeli/jest gezinme, yatay ekran, büyük yazı, tam ekran kutlama, gerçek
fotoğraf yakınlaştırma ve arka plan/süreç kapatma kabulü ayrı işlerdir. CI başarısı bunları
geçtiğimiz anlamına gelmez; `TEST_KONTROL_LISTESI.md` içinde açık bırakılır.


## P4–P6 ek sözleşmesi

V2 yukarıdaki referans kuralıdır; üretim varsayılanı **Gölge** aynı tarihi uygular, ayrı v3 adayını
ve gerekçesini yerel snapshot2'de saklar. **Etkin v3 kapalıdır.** Son 90 günde en çok 5 ayrı gün;
son iki bağımsız başarı güçlük uyarısını kaldırır. Yavaşlık tek başına ceza, hız XP/uzatma değildir.
Süre düzeltmesi yalnız Rahat/çözüm açılmamış/güvenilir tam ölçüm + tekrarlayan yakın güçlük + yeterli
kişisel referansla aday olabilir; gerçek indirim en çok %10, gecikme indirimiyle birikmez.

İlerleme önerisi görünür ders/vade/kaynak filtresine gider; Odak kullanıcı başlatırsa açılır.
Gün/profil/plan/gerçek aynı-gün uygunluğu başlatma ve kayıt öncesi yeniden kontrol edilir. Önizleme
politikası/bağlamı değişirse sessiz başka tarih yazılmaz. Ayrıntı ve kabul kanıtı:
[P4_P5_P6_UYGULAMA.md](P4_P5_P6_UYGULAMA.md).
