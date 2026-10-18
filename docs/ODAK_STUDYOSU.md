# Odak Stüdyosu ve adaptif tekrar v2 — v1.2.0

## Kullanıcı akışı

1. Bugün ekranında **Bugünün oturumuna başla** veya Filtreli ekranda hazır **5/10/20** soru seçimi;
   özel soru sayısı ve mevcut konu/kaynak/neden filtreleri de kullanılabilir.
2. **Akıllı sıra** (varsayılan) veya **En eski önce** seçilir. Bugün/Filtreli listesindeki tek
   soruya dokunmak da aynı odak ekranını bir soruluk oturumla açar.
3. Odak ekranı ders, oturum ilerlemesi, soru başına aktif süre, tamamlanan sayı ve oturum emeği
   XP'sini gösterir. Fotoğrafa dokunarak tam ekran yakınlaştırılır; eski soru notları okunabilir.
4. **Çözümü incele** isteğe bağlıdır. **Cevabımı değerlendir** çözüm açılmasını zorunlu kılmaz.
5. Çözemedim / Zorlandım / Rahat için bir sonraki tekrar aralığı **kaydetmeden önce** görünür.
   Sonuç seçimi tek başına kayıt değildir; **Kaydet ve ilerle** gerekir. Yanlışlıkla dokunmada
   seçenek değiştirmek mümkündür. Kayıt başarısızsa soru ve seçim korunur.
6. **Sona bırak** soru sırasını değiştirir, XP/tekrar oluşturmaz. Çözüm daha önce açılmışsa işaret
   korunur; soruyu sona bırakıp bağımsız bonusunu yeniden elde etmek mümkün değildir.
7. **Mola** süreyi durdurur. Uygulama arka plandayken ve tam ekran görsel açıkken de süre artmaz.
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
- Çözüm açılmamış v2 kayıtta: Rahat +5, Zorlandım −15, Çözemedim −20. Sınır **130…280**.
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

### Neden Room v4 yok?

Önceki tasarım `aralik_gun/kolaylik_yuz/unutma_sayisi` kolonları önermişti. Uygulamada hepsi
zaten tutulan değişmez tekrar geçmişinden hesaplanabildiği için **çift durum saklamak yerine
geçmişten türetme** seçildi. Bu v1.2.0 mimari revizyonudur:

- `TekrarKurali` v1 fonksiyonu ve testleri eski davranışın referansı olarak kalır.
- Eski tekrarlar yeniden yazılmaz. Yeni kayıtlar `algoritma_surumu = 2` kullanır.
- `TekrarKaydi` artık zaten tabloda olan algoritma sürümü ve zaman damgasını domain'e taşır.
- Room v3 / ZIP v3 değişmez. v2 algoritma kaydının export → restore turu ayrı test edilir.
- Önceki şartnamedeki v4 migration/yedek planı bu sürüm için **uygulanmadı ve gerekmiyor**.

## Kapanma, çıkış ve devam

`OdakOturumuDeposu` yalnız oturum ID'si, sıralı soru ID'leri, konum, sonuçlar ve çözüm açma
işaretlerini cihazda tutar; fotoğraf/not kopyalamaz. Kalıcı tekrarın tek kaynağı Room'dur.

- Sonuç Room'a yazıldıktan sonra devam noktası kaydedilir.
- Uygulama bu iki adım arasında kapanırsa kurtarma, oturum başlangıcından sonra yazılmış Room
  kayıtlarını kontrol eder ve konumu ilerletir. İkinci XP/tekrar yazmaz.
- Bilinçli çıkıştan sonra Bugün/Filtreli sekmesinde **Yarım kalan oturuma dön** bulunur.
- Süreç yeniden başlarsa devam noktası yüklenir; eksik/silinmiş sorular ayıklanır.
- Bitmiş oturum, kullanıcı karneden ayrılıncaya kadar kurtarılabilir. Bu, kalıcı geçmiş karne
  arşivi değildir; yalnız son oturum devam noktasıdır.
- Bozuk devam noktası uygulamayı çökertmez; soru/tekrar arşivini etkilemeden göz ardı edilir.
- ZIP geri yüklemede (v1.3.0/P0) devam noktası yalnız doğrulanmış commit sonrasında,
  recovery işaretleri kaldırılmadan temizlenir. Commit öncesi başarısız girişim/rollback eski
  devam noktasını korur; temizlik başarısızsa startup recovery sınırı kapanmaz.
  Rozet/hedef/format tercihleri cihaz-yereldir.
- Değerlendirme alanına otomatik kaydırma ortak hareket politikasına uyar. Bu değişiklik
  sayaç doğruluğu veya süre kalıcılığı eklemez; bunlar Gelişim Planı'nın P2 paketindedir.

## Doğrulama ve sınırlar

Otomatik testler: adaptif eşikler/gecikme/kolaylık/1–180 gün sınırı; önizleme ile yazılan planın
aynılığı; aynı gün kayıt engeli; checkpoint gidiş-dönüş, çözüm işaretinin sona bırakmada korunması,
commit sonrası süreç kaybı; formatta veri koruma; bağımsız çözüm sorgularının karneyle uyumu.

Fiziksel cihazda üç-düğmeli/jest gezinme, yatay ekran, büyük yazı, tam ekran kutlama, gerçek
fotoğraf yakınlaştırma ve arka plan/süreç kapatma kabulü ayrı işlerdir. CI başarısı bunları
geçtiğimiz anlamına gelmez; `TEST_KONTROL_LISTESI.md` içinde açık bırakılır.
