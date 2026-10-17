# Tam ZIP yedek biçimi — v3

> Güncel biçim: `yanlis-defteri-backup`, format sürümü **3**, Room şema sürümü **3**. Uygulama v2
> yedeklerini de geri yükleyebilir; yeni yedekler v3 olarak yazılır. Bu belge kodla günceldir.
> Son güncelleme: 2026-10-17.

## 1. Arşiv yapısı

```text
yanlis-defteri-yedek.zip
├── manifest.json
├── data.json                 # Geri yüklemenin kanonik kaynağı
├── csv/
│   ├── lessons.csv
│   ├── topics.csv
│   ├── questions.csv
│   ├── catalog_options.csv   # Global kaynaklar ve yanlış nedenleri
│   ├── images.csv
│   └── reviews.csv           # İnceleme/tablo uygulamalarına yönelik kopyalar
└── images/
    └── <uuid>.jpg            # Soru ve çözüm görselleri
```

Yedek; ders, konu, soru ve çözüm notları (eski soru notları dâhil), plan/öğrenme durumu, tekrar
geçmişi, global kaynak ve yanlış nedeni kataloğu, soruya özel kaynak konumu ve soru/çözüm görsellerini
içerir. CSV dosyaları yardımcı kopyadır; geri yükleme ilişkileri JSON üzerinden doğrular ve CSV'yi
veri kaynağı olarak kullanmaz. Cihaz içindeki gerçek dosya yolu yedeğe konmaz; geri yüklemede yeni
kalıcı dosya adları üretilir. Önizleme/küçük resimler dâhil değildir.

## 2. Manifest, bütünlük ve sınırlar

`manifest.json` en az biçim kimliği/sürümü, Room şema sürümü, oluşturulma zamanı, `data.json` ve
`images/` yolları, temel tablolar ile katalog seçeneklerinin satır sayıları, zorunlu CSV yolları ve
her yük için göreli yol, byte uzunluğu ve SHA-256 içerir. JSON, ilişki, katalog türü ve enum alanları
ayrıca doğrulanır; görsellerin boyutu, SHA-256'sı, JPEG MIME türü ve çözünürlük metadata ile
eşleştirilir.

Uygulanan üst sınırlar: ZIP 512 MiB; açılmış toplam içerik 1 GiB; tek girdi 128 MiB; `data.json`
64 MiB; manifest 1 MiB; 10.007 ZIP girdisi (manifest dâhil); 10.000 görsel; tablo başına 200.000
kayıt; görsel kenarı en çok 3.000 px; sıkıştırma oranı en çok 1.000:1. İçe aktarma, arşivi app-private
güvenli hazırlık dizinine sınırlandırılmış şekilde kopyalar, SHA-256 ve ilişki kontrollerini yapar,
yol kaçışı/tekrarlı/yabancı ZIP girdilerini reddeder ve çıkarma öncesi yaklaşık boş alanı denetler.
Dışa aktarma ve görsel kopyalama öncesinde de boş alan kontrolü uygulanır. SHA-256 bozulmayı
saptar; **şifreleme veya kaynağın kimliğini doğrulama sağlamaz**.

Yeni v3 yedeklerinde katalog seçenekleri `catalog_options` JSON listesi ve CSV kopyası olarak
saklanır; sorunun kaynak adı `source`, o soruya özel sayfa/test/soru bilgisi `source_location`
alanındadır. Uygulama v2 yedeklerini de kabul eder: eski soru kaynakları ve özel yanlış nedenleri
soru kayıtlarından yeniden kataloğa eklenir, hazır nedenler seed edilir; v2'de konum alanı olmadığı
için boş kalır. Daha yeni format veya Room şema sürümü açık hata ile reddedilir; v2'den eski biçimler
desteklenmez. Format sürümü ve Room migration'ı bağımsız yönetilir; yeni alan/format değişiminde bu
belge ve testler birlikte güncellenmelidir.

## 3. Dışa aktarma ve SAF/Drive

Kullanıcı Android belge seçicisinde hedefi seçer. ZIP önce cache içinde geçici dosyaya yazılır; her
yükün boyutu/hash'i kaynağa karşı kontrol edilir, dosya `fsync` ve atomik taşıma ile tamamlanır;
ancak bundan sonra `ContentResolver` üzerinden seçilen hedefe kopyalanır ve son yedek özeti
kaydedilir. Başarısız işlem başarı olarak gösterilmez. Son özet dosya adı, tarih, boyut ve bunun
seçilen konum mu yoksa güvenlik kopyası mı olduğunu gösterir.

Android belge sağlayıcısına yazmanın tamamlanması Drive/bulut yüklemesinin bittiğini kanıtlamaz.
Arayüz kullanıcıya bulut sağlayıcısındaki yüklemeyi ayrıca doğrulaması gerektiğini söyler.
Uygulama kaldırılırsa cihaz içi veriler ve uygulamanın özel alanındaki güvenlik kopyası silinebilir;
önemli yedek ayrıca kullanıcının seçtiği kalıcı konuma kaydedilmelidir.

## 4. Tam geri yükleme

Geri yükleme **mevcut arşivin tamamını değiştirir**; birleştirme desteklenmez.

1. SAF'ten seçilen ZIP sınırlı biçimde özel hazırlık dizinine kopyalanır. Manifest, sürüm, girdi
   yolları, sayılar, dosya boyutları/SHA-256, JSON ilişkileri, JPEG metadata'sı ve boş alan kontrolü
   tamamlanmadan onay gösterilmez.
2. Önizleme yedekteki ve mevcut arşivdeki kayıt/görsel sayılarını gösterir. Kullanıcının açık
   onayı olmadan veritabanı veya kalıcı görseller değiştirilmez.
3. Mevcut veritabanı ve görsellerden doğrulanmış `safety-<uuid>.zip` güvenlik kopyası oluşturulur;
   oluşturulamıyorsa işlem başlamaz. Kopya `files/yedekler/` altında tutulur.
4. İşlem günlüğü yazılır, yeni görseller farklı kalıcı adlarla kopyalanıp doğrulanır. Sonra Room
   transaction'ında eski tekrar/görsel/soru/katalog seçeneği/konu/ders tabloları silinir,
   doğrulanmış içerik sırayla eklenir ve geri yükleme işareti yazılır.
5. Yeni satırlar ve görsel dosyaları tekrar doğrulanır. Başarılıysa eskiye ait görseller silinir,
   Room işareti ve günlük temizlenir. Soru/görsel referanslarının yanlışlıkla aynı dosyayı
   göstermemesi için yeni kalıcı adlar kullanılır.

Kesinti kurtarması için app-private JSON işlem günlüğü ile Room v1→v2 migration'ında oluşturulan
`geri_yukleme_isaretleri` tablosu birlikte kullanılır. Room v2→v3 migration'ı kaynak konumu kolonunu
ve katalog tablosunu ekler; eski soru kaynakları/özel nedenleri ile hazır nedenler kaybolmadan taşınır. Uygulama normal ekranları açmadan önce
kurtarmayı çalıştırır: Room işareti yoksa işlem transaction'a ulaşmamıştır ve yalnızca yeni dosyalar
silinerek eski arşiv bırakılır; işaret varsa yeni arşiv commit edilmiştir ve eski dosya temizliği
idempotent biçimde tamamlanır. Günlük/işaret tutarsızlığı veya yeni görsel doğrulama hatası varsa
uygulama verileri sessizce değiştirmek yerine normal kullanımı kilitler ve tekrar kurtarma seçeneği
gösterir.

## 5. Test ve cihaz kabulü

Robolectric/JVM testleri ZIP üretimini, her envanter SHA/uzunluğunu, katalog ve kaynak konumu
dahil tam geri yüklemeyi, eski v2 yedeği geri yüklemeyi, dosya adı yeniden üretimini, mevcut verinin
güvenlik kopyasını, checksum bozulmasını, path-traversal reddini, kesinti kurtarmasında
commit/rollback ayrımını, Room v1→v2→v3 migration zincirini ve mevcut soru verisinin korunmasını kapsar. Bunlar
CI'da gerçek çalıştırılmadan geçilmiş sayılmaz. CI testi fiziksel cihaz/SAF sağlayıcı davranışını
kanıtlamaz.

Kullanıcı kabulü ayrıca gerekir: cihazda seçilen Downloads/Drive hedefi, uygulama kapanıp açılması,
yedek seçme-önizleme-onay/iptal akışları ve başka/temiz kurulumda geri yükleme kontrol edilmelidir.
Bulut yükleme durumu Drive arayüzünden ayrıca doğrulanır. 30–50 fotoğraflı okunabilirlik ve Aşama 2
kamera/galeri testleri de gerçek cihazda bekliyor.

## 6. Şifreleme ve gizlilik

v3 (ve geriye uyumlu v2) yedeği şifrelenmez. ZIP; soru/çözüm notları, kaynak kataloğu ve soru fotoğrafları içerebilir. Yedeği
kullanıcının seçtiği saklama yerinde koruması gerekir. İleride şifreleme eklenirse parola kaybı riski,
CSV'nin incelenebilirliği ve olgun bir standart kullanımı ayrıca kararlaştırılmalıdır.
