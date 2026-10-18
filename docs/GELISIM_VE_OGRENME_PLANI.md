# Yanlış Defteri — Premium deneyim ve kişiselleşen öğrenme sistemi

**Tarih:** 2026-10-18
**Durum:** P0/P1 uygulandı ve v1.3.0 ile yayımlandı; CI `39131225744` 157/0. Fiziksel cihaz kabulü bekler. P2–P6 aşağıda öneri olarak kalır.
**Başlangıç:** v1.2.0 / code 14, Room v3, ZIP v3, tekrar algoritması v2. Başlangıç CI kanıtı: `39067624050`, 130 başarılı test; fiziksel cihaz kabulü ayrı ve bekliyor.

## P0/P1 uygulama notu — 2026-10-18

- **P0:** Canlı karne rozet sayısı; sınırlandırılmış otomatik tekrar/manual yenileme; tek DB snapshot’ı ve ortak sahiplik/hedef girdisi; hedef değişince XP/rozet yenileme.
- **Restore:** Devam noktası yalnız doğrulanmış commit sonrasında, kalıcı kurtarma işaretleri kaldırılmadan temizlenir. Başarısız ön doğrulama/rollback oturumu korur; temizleme hatasında işaretler kalır. Aynı soru ID’leri kullanılsa bile eski oturum başarıyla değişmiş arşive taşınmaz.
- **P1:** Stateless durum modeli; petrol/lacivert-bakır rütbe kartı; gerçek başlangıç XP’si; kompakt/adaptif sayılar; emek ile pekişme ayrımı; kapalı açılan koleksiyon; mevcut gerçek ders/plan verileri; boş/hata/yenileme eylemleri.
- **Hareket:** Canlı sistem ölçeği gözlemi, ortak politika, sonlu animasyonlar. Hareket kapalıyken değerler/eylemler kaybolmaz. Odak’taki otomatik değerlendirme kaydırması da bu politikaya uyar; sayaç/ölçüm mantığı değişmedi.
- **Örnek veri:** Yalnız `src/debug` Preview/test fixture’larında; release veri yolunda yok. Native Robolectric görüntüleri sentetik önizlemedir, fiziksel cihaz kanıtı değildir.
- **Değişmeyenler:** Room/ZIP v3, tekrar v2, XP katsayıları, 15 rütbe/30 rozet kuralları. Süre kalıcılığı, kişisel profiller, v3 motor, kaynak/konu analizleri **uygulanmadı**.
- **Doğrulama:** CI `39131225744`: 157 test / 0 başarısız / 0 hata / 0 atlanan; Release/imza/sertifika ve yayımlanan APK digest eşleşmesi başarılı. Eski 130 + 27 yeni test. [Sentetik önizlemeler](ui/p1/README.md) ve sanal animasyon zamanı fiziksel cihaz/performans kabulü değildir.

## 1. Hedef ve ana karar

Tek ürün hattı kurulacak:

**Deneme → güvenilir ölçüm → kişisel profil → açıklanabilir tekrar kararı → gelişim özeti → çalışmaya dönüş.**

Görsel prototip erken hazırlanabilir. Gerçek süre grafikleri, kaynak profilleri ve kişiselleştirilmiş kararlar ise ölçüm doğrulanmadan açılmaz. Üretim ekranları örnek/sahte verilerle doldurulmaz.

İki kavram özellikle ayrılır:

- **Emek:** mevcut XP, seviye, seri, rütbe ve rozetler. Soru eklemek de emektir.
- **Öğrenme göstergeleri:** yardımsız çözüm, farklı günlerdeki tekrarlar, toparlanma ve ölçülebilen süre eğilimi. Bunlar öz değerlendirmeden türetilir; doğruluğu dışarıdan denetlenmiş sınav sonucu değildir.

İlk teslimlerde XP katsayıları, 15 rütbe ve 30 rozetin sahipliği korunur. Daha hızlı çözene XP verilmez; yavaş çözen cezalandırılmaz. Arayüzde XP, “konuya hâkimiyet puanı” diye sunulmaz.

## 2. Kod tabanında doğrulanan başlangıç

| Mevcut parça | Bugünkü davranış | Yeni iş |
|---|---|---|
| `ui/calisma/OdakOturumuEkrani.kt` | `delay(1000)` ile ekranda sayaç; mola/arka plan/tam ekran görsel/kaydetmede duruyor | Monotonik saatli ölçüm; deneme, çözüm sonrası çalışma ve değerlendirme ayrımı |
| `TekrarEntity`, `TekrarKaydi` | Sonuç, çözüm açma, tarih, önceki/sonraki aralık var; aktif süre yok | Süre ve karar dayanağı için ek kalıcı veri |
| `OdakOturumu`, `OdakOturumuDeposu` | Soru ID'leri, sonuçlar, konum ve çözüm açma devam noktası | Soru bazında biriken süre, ölçüm kalitesi ve sürümlü kurtarma |
| `AkilliTekrarKurali` | Geçmişten hesaplanan adaptif v2; kişisel süre profili kullanmıyor | v2'yi koruyan, ayrı ve sınırlı karar katmanı |
| `SoruRepository.tekrarKaydet` | Yalnız soru ID'si, sonuç ve çözüm açma bilgisi alıyor | Ortak ölçüm/önizleme/kayıt isteği |
| `SoruEntity.kaynak` | Kaynak adı metin; konu ise ID ilişkisi | Kaynakları mevcut etiketlerle gruplama; kayıt anı bağlamını koruma |
| `SeviyeHizmeti` / `SeviyeEkrani` | XP, rütbeler, rozetler, ders dağılımı, 7 günlük planlı soru adedi | Ayrı gelişim servisi, karar odaklı ekran ve süre kapsamı |
| `RozetDeposu` / `RozetKutlamaHost` | Kalıcı rozet sahipliği ve kutlama kuyruğu | Mevcut sahipliği bozmadan kontrollü rütbe kutlaması |
| `YerelYedeklemeHizmeti` | ZIP v2/v3 okuyor; biçim sürümüne göre sıkı doğrulama yapıyor | Açık v2/v3/v4 okuyucuları ve yeni ölçümlerin gidiş-dönüş testi |

**Önemli fark:** Mevcut ekrandaki sayaç analitik çözüm süresi değildir. Çözüm ekranda açıkken ve değerlendirme seçilirken geçen süreyi bağımsız denemeden ayırmaz; tekrar kaydında da saklamaz. Eski `zaman` alanı süre değil, zaman damgasıdır.

## 3. Değişmeyecek ürün kuralları

1. Bağımsız = **Rahat + çözüm açılmamış**. Süre bu tanımı veya karne kovasını değiştirmez.
2. Çözüm açıldıysa veya Çözemedim seçildiyse sonraki tekrar **1 gün**. Yeni katman bunu uzatmaz.
3. Aynı soru için aynı yerel günde ikinci tekrar/XP oluşmaz.
4. Profil değişimi, analiz veya öneri hiçbir soruyu silmez, otomatik askıya almaz veya arşivden çıkarmaz.
5. Eski kayıtlar yeniden yazılmaz; geçmişe hayalî süre eklenmez.
6. Ölçüm eksikliği sonuç/XP kaydını engellemez; yalnız süreye bağlı çıkarımı kapatır.
7. Veri yetersizse açıkça söylenir. Kaynak için “kötü kitap”, öğrenci için “başarısız” etiketi üretilmez.
8. Hesaplama cihazda yapılır. API, sunucu, OCR, otomatik cevap doğrulama ve yeni konu taksonomisi bu işin parçası değildir.
9. Çalışma mevcut `arena/59c6570d-question-note` dalında sürer; main'e merge veya PR yapılmaz.

## 4. Mimari ve ortak sözleşmeler

Önerilen yeni parçalar; adları uygulama sırasında kesinleştirilecek, bugün mevcut API değiller:

```text
Odak ekranı / eski soru ayrıntısı
       │
       ▼
TekrarDenemesi + AktifSureOlcer
       │
       ▼
TekrarKararHizmeti
  ├─ geçmiş + kişisel profil (mevcut deneme hariç)
  ├─ mevcut v2 kuralı
  └─ yeni karar katmanı (kapalı / gölge / etkin)
       │
       ├─ aynı girdilerle önizleme
       ▼
Room transaction: tekrar + ölçüm/karar + yeni soru planı
       │
       ▼
GelisimHizmeti + mevcut SeviyeHizmeti
       │
       ▼
İlerleme / kaynak-konular / çalışma önerisi / kutlama
```

- `TekrarDenemesi`: kalıcı deneme kimliği, soru/oturum kimliği, sonuç, çözüm açma, ölçüm özeti.
- `SureOlcumu`: deneme süresi, çözüm sonrası aktif süre, kalite, kalite nedenleri, ölçüm sürümü.
- `TekrarKarari`: temel/final aralık, tarih, öğrenme durumu, kural sürümü, gerekçe kodları, veri dayanağı, önizleme bağlamı.
- `GelisimOzeti`: gerçek değerler, örneklem/kapsam, tarih aralığı, karşılaştırılabilirlik ve isteğe bağlı çalışma önerisi.

Hesaplar saf Kotlin fonksiyonlarına ayrılır; Compose sadece gösterir. Android saati ve yerel takvim saati enjekte edilir. Tek Gradle modülü ve mevcut `AppContainer` korunur; bunun için Hilt veya çevrimiçi analitik altyapısı eklenmez.

**Önizleme/kayıt eşitliği:** Değerlendirmeye geçerken süre dondurulur. Önizleme ve kayıt aynı servis ve aynı girdileri kullanır. Gün/geçmiş/profil değişmişse görünmeyen bir tarih kaydedilmez; önizleme yenilenir. Hata sonrası aynı deneme kimliğiyle tekrar denemek ikinci kayıt üretmez. Eski `SoruDetayEkrani` çağrıları da yeni sözleşmeye uyarlanır; ölçüm yoksa `YOK` ile v2 davranışı sürer.

## 5. Güvenilir süre ölçümü

### 5.1 Ölçüm modeli

Sayaç artırmak yerine `SystemClock.elapsedRealtime()` tabanlı segment farkları toplanır. Ekranı yenileyen saniyelik tick yalnız görüntü içindir; gerçek süreyi belirlemez.

| Durum | Deneme süresi | Çözüm sonrası aktif süre |
|---|---:|---:|
| Soru hazır/görünür; çözüm hiç açılmamış | Artar | Durur |
| Çözüm ilk kez açılmış; destekli çalışma sürüyor | Durur | Artar |
| Çözüm tekrar gizlenmiş | Yeniden başlamaz | Aktif çalışmada artar |
| Değerlendirme / kaydetme / yükleme / mola / arka plan | Durur | Durur |
| Tam ekran görsel | Durur | Durur |
| Sona bırakma | Birikim korunur | Birikim korunur |

Çözüm sonrası zaman “tamamı çözümü okuyarak geçti” diye adlandırılmaz; **inceleme/destekli çalışma** süresidir. Çözüm açıldıktan sonra deneme bağımsız hâle geri dönmez.

**Tam ekran görsel uyarısı:** Soruya yakınlaştırarak bakmak gerçekte çalışma olabilir. Mevcut durdurma davranışını ilk teslimde korumak, bu sürenin ölçülmediğini kabul etmeyi gerektirir. Böyle bir deneme `KISMI` işaretlenir; kişiyi “çok hızlı çözdü” diye sınıflandırmak için kullanılmaz. Soru yakınlaştırmasını ileride süreye dahil etmek ayrı, açık bir UX kararıdır.

### 5.2 Kalite ve kurtarma

- Kalite: `YOK`, `TAM`, `KISMI`, `GECERSIZ`; yanında nedenler (süreç kurtarma, tam ekran kesintisi, tutarsız saat vb.). `TAM`, yalnız tanımlı ekran ölçümünün eksiksizliğidir; öğrencinin gerçekten odaklandığı garantisi değildir.
- Eski kayıtta süre **null**; sıfır saniye sayılmaz.
- Soru/deneme kimliğiyle sahiplenilen durum, yeniden çizimde sıfırlanmaz; sona bırakınca başka soruya taşınmaz.
- Monotonik başlangıç anı süreç/cihaz yeniden başlatıldıktan sonra kullanılmaz. Yalnız birikmiş süre kurtarılır; kapalı geçen zaman eklenmez.
- Durum geçişlerinde ve aktifken sınırlı sıklıkta (başlangıç önerisi: 5 saniye) IO üzerinde devam noktası. Zorla kapanmada bilinmeyen son segment uydurulmaz; ölçüm kısmi olur.
- Kaydet başarısızsa dondurulmuş ölçüm/deneme korunur. Tekrar kaydı başarılıysa süre de aynı transaction'da kalıcıdır.
- Kalite yalnız süre analizini sınırlar; geçerli öz değerlendirme, karne ve emek XP'si korunur.

## 6. Kalıcı veri, migration ve ZIP

### 6.1 Tercih: tekrar başına bir ek kayıt

Önerilen `tekrar_olcumleri` tablosu, `tekrar_id` üzerinden `tekrarlar` ile 0..1 ilişki kurar. Eski satırlar olduğu gibi kalır; yeni tekrarlar süre ölçülmemiş olsa da bağlam kaydı alabilir.

| Alan grubu | İçerik |
|---|---|
| Kimlik | `tekrar_id` PK/FK (silmede cascade), benzersiz `deneme_id`, isteğe bağlı `oturum_id` |
| Ölçüm | Nullable `deneme_aktif_ms`, `inceleme_aktif_ms`, kalite/nedenler, `olcum_surumu` |
| Kayıt anı bağlamı | Ders/konu kimliği ve etiketi; normalize kaynak anahtarı ve kaynak etiketi |
| Karar dayanağı | Sürümlü, boyutu sınırlandırılmış snapshot: profil örneklemi/medyanı, kullanılan nedenler, temel ve aday/final aralık; gölge/etkin modu |

Sonuç, tarih ve algoritma sürümünün asıl kaynağı mevcut `tekrarlar` tablosudur. Tam karar geçmişi veya ham saniyelik olay günlüğü ikinci kez depolanmaz. Analiz için aralık/filtre sorgularına gerekli indeksler ve join planı ölçülür.

**Kaynak kimliği:** Bugün soruda kaynak metin tutuluyor. İlk aşamada yeni bir kitap kataloğu/migration zinciri kurmak yerine NFC + Türkçe harf normalizasyonuyla anahtar üretilir. Farklı yazılmış isimler kendiliğinden “aynı kitap” diye birleştirilmez. Yeni kayıtta bağlam snapshot'ı, konu/kaynak sonradan değişince geçmiş trendin sessizce yeniden etiketlenmesini önler. Eski kayıtların o günkü kaynak/konu bilgisi bilinmiyorsa uydurulmaz; mevcut etiketle gruplanan eski arşiv istatistiği tarihsel profilden açıkça ayrılır.

### 6.2 Sürüm kararları

- Bu yeni ölçümler geçmişten türetilemediği için **Room v4 migration gerekir**. v1.2.0'daki eski “v4 gerekmez” kararı yalnız o sürümün adaptif v2 durumu içindi.
- Taşınabilir yeni ölçümler için **ZIP v4** önerilir. Room sürümü, ZIP sürümü ve algoritma sürümü ayrı kavramlardır.
- `MIGRATION_3_4`; mevcut 1→2→3 zinciriyle beraber 1/2/3→4 yolları test edilir. Yıkıcı migration yok.
- Eski tekrarlar ve plan tarihleri değişmez. Olmayan ölçümler sıfırla doldurulmaz.
- Yeni ZIP, ölçümleri/karar snapshot'ını JSON ve yardımcı CSV'de taşır; manifest envanteri, sayı/hash kontrolleri ve FK doğrulaması genişletilir.
- Okuma açık biçimde **v2 / v3 / v4** dallarına ayrılır. Sadece sabitleri 3→4 yapmak yeterli değil: mevcut parser eski biçimi `if/else` ile ayırıyor ve katalog/CSV kontrollerini güncel sürüme bağlıyor.
- v2/v3 import: süre yok. v4 import: süre/kalite/bağlam/karar roundtrip korunur. Yetim ölçüm, negatif/taşan süre, bozuk snapshot ve desteklenmeyen sürüm açık hata verir.
- Mevcut rozet/hedef/profil prefs'ini ZIP'e alma bu paketin otomatik yan işi değildir; bu sınırlılık açık kalır. Yeni analiz verisi ise yedeksiz bırakılmaz.
- Geri yüklemede yeni cache ve devam noktaları veri kümesiyle uzlaştırılır. Soru silinince bağlı ölçümler de silinir. Korumasız veritabanı downgrade yapılmaz.

## 7. Kişisel profiller ve veri yeterliliği

İlk profil **“Yanlış Defteri'ndeki çalışma profilin”**dir; genel sınav hızını veya zekâyı ölçmez.

İki ayrı dağılım gerekir:

1. **Yardımsız deneme referansı:** geçerli tam ölçüm, çözüm açılmamış Rahat. Ders bazında medyan, çeyrekler ve MAD; mevcut deneme hariç.
2. **Aktif iş yükü referansı:** geçerli tam ölçümlerde sonuçtan bağımsız deneme + inceleme süresi. Gelecek çalışma yükünde yalnız Rahat sürelerini kullanıp tahmini gereksiz düşürmez.

Ham süre silinmez/değiştirilmez. Negatif/taşmış/ölçülmemiş değerler analize girmez. Geçerli ama uç değerler, medyan ve dayanıklı dağılım ile etkisizleştirilir; tek uzun soru başarısızlık sayılmaz. MAD/IQR sıfırsa bölme hatası veya aşırı sapma üretilmez. Tekrarlanan bir kolay soru profili ele geçirmesin diye kişisel süre referansında soru başına özet kullanılır; tekrar sayısı ve benzersiz soru sayısı ayrı tutulur.

**Başlangıç eşikleri — bilimsel kesinlik değil, test edilip ayarlanacak ürün varsayımı:**

| Profil | Asgari geçerli ölçüm | Farklı soru | Farklı gün |
|---|---:|---:|---:|
| Ders | 20 | 10 | 5 |
| Ders içindeki konu veya kaynak | 30 | 15 | 7 |

- İlk hareketli pencere önerisi 60 gün; eşikler tek `ProfilPolitikasi` altında sürümlenir.
- Konu/kaynak yetersizse ders profiline dönülür; ders de yetersizse süreye bağlı karar verilmez.
- Her göstergede tarih aralığı, örneklem ve kapsama yer verilir. Yetersiz grupta “ölçüm birikiyor”; sahte yüzde veya genel hız etiketi yok.
- İstatistiksel olarak kalibre edilmemiş `%92 güven` gibi değerler kullanılmaz. “Veri dayanağı: yetersiz / sınırlı / yeterli” ve örneklem gösterilir.
- Kaynaklar farklı derslerle veya farklı deneme aşamalarıyla tek bir hız sıralamasına konmaz. Soru zorluğu otomatik bilinmediği için kaynaklar arası nedensel kalite çıkarımı yapılmaz.

## 8. Tekrar motoru v3: ayrı, sınırlı ve açıklanabilir

### 8.1 Gölge çalışma önce gelir

`AkilliTekrarKurali` v2 referansı korunur. Yeni `UyarlanabilirTekrarKurali` aynı girdiden aday karar üretir; **gölge modunda uygulanan tarih hâlâ v2'dir**. Aday farkı ve gerekçesi cihazda saklanır, gönderilmez.

Gölge çalışma sınırları ve karar farklarını doğrular; tek başına öğrenmenin arttığını kanıtlamaz. Gerçek fayda, zaman içinde sonraki farklı-gün bağımsız denemelerle ve örneklem sınırlarıyla değerlendirilir.

### 8.2 İlk etkin politikanın korumaları

1. Önce v2 temel planı hesaplanır.
2. Çözüm açılması/Çözemedim → 1 gün; değiştirilemez.
3. Eksik/yetersiz süre verisi → v2 ile aynı aralık; geçmişten uyarı/öneri yine üretilebilir.
4. Yavaşlık tek başına aralığı kısaltmaz. Hızlılık tek başına uzatmaz veya XP vermez.
5. Yakın geçmiş sinyali ilk öneride en fazla son 5 ayrı tekrar gününden ve en fazla 90 günlük pencereden üretilir. Aynı günün eski mükerrerleri tek gün sayılır.
6. Son iki ayrı gün bağımsız başarı varsa eski “zorlanma” uyarısı pasifleşir; ömür boyu başarısızlık toplamı kalıcı etiket olmaz.
7. Deneysel ilk değişiklik yalnız mevcut **Rahat** sonucunda, tekrarlayan yakın güçlük + güvenilir belirgin uzun süre birlikteyse düşünülür. Uzunluk başlangıç adayı: medyanın 1,5 katını ve dayanıklı dağılım eşiğini birlikte aşmak; profil elverişsizse değişiklik yok.
8. Süreçte tek ek düzeltme seçilir; katsayılar çarpılarak birikmez. v2 gecikme indirimi uygulamışsa ikinci indirim yapılmaz. Zorlandım zaten kendi kuralıyla işlendiğinden tekrar cezalandırılmaz.
9. İlk deneysel ek kısaltma en fazla %10; tam sayıya yuvarlama sonrası gerçek azaltma da %10'u aşamaz (küçük aralıkta gerekirse hiç değişmez). 1–180 gün sınırı korunur.
10. Uyarıdan çıkışta ek ceza kalkar; v2'nin normal büyümesi devam eder. “Hızlandın” diye yapay süre bonusu verilmez.

Bu sayılar ilk kod/test politikasını somutlaştırır; veri görmeden bilimsel optimum veya kesin yayın kararı sayılmaz.

Odak ekranındaki zor soru kartı da yeni yakın-geçmiş özetini kullanır; süre yetersizliğinde v2 tarihine dönmek, ömür boyu zorluk etiketi göstermek zorunda değildir. Bu değişiklik eski rozetlerin tarihsel kazanma koşullarını veya sahipliğini değiştirmez.

**Sürüm güvenliği:** `AkilliTekrarKurali.hafiza` bugün `algoritmaSurumu >= 2` kullanıyor. v3 kayıtlarını gelecekteki tüm sürümlerle aynı varsaymak yerine desteklenen sürümler açıkça ele alınmalı. 1/2 geçmişinin aralıkları değişmez; v3 etkisi ve kolaylık güncellemesinin ortak/ayrı kuralları testle sabitlenir.

**Geri dönüş:** Yeni katman kapatılınca gelecekteki denemeler v2 temel kararına döner. Kaydedilmiş sonuçlar, ölçümler ve geçmiş planlar topluca yeniden yazılmaz; bu bir APK/DB downgrade değildir.

## 9. Analitikten çalışma eylemine

### 9.1 Kaynak / konu profili

- Farklı soru sayısı ve tekrar sayısı ayrı.
- Bağımsız oranının paydası açık: bağımsız tekrar / toplam tekrar. Geçersiz süre, geçerli sonucu bu paydadan çıkarmaz.
- Ders içindeki süre referansına göre karşılaştırma; kapsanmayan denemeler görünür.
- Tekrarlayan güçlük ve toparlanma, farklı günlerdeki geçmişten türetilir.
- Boş kaynak/konu ayrı grup; yeni taksonomi, otomatik içerik sınıflandırması yok.
- Mevcut etiket değişimi tarihsel bağlamı bozmaz; eski bağlamı bilinmeyen kayıtlar gizlice kesin trend üretmez.

### 9.2 Gelişim özeti

- Son 7 günün gerçek ritmi; bugünün henüz tamamlanmadığı açıkça işaretlenir.
- Dönem karşılaştırması için başlangıç tercihi: bugün hariç tamamlanmış son 7 gün ve önceki 7 gün. Karne ekranındaki takvim-haftası karşılaştırması ayrı adlandırılır.
- Her iki dönemde yeterli veri ve karşılaştırılabilir soru grubu yoksa “iyileştin/geriledin” hükmü yok. Uygunsa aynı soruların iki dönemdeki gelişimi ayrıca gösterilir.
- Soru karışımı veya deneme sayısı değişmişse ham yüzde değişimi öğrenme artışı diye sunulmaz.
- Gelecek 7 günün soru yükü her zaman gerçek plandan gelir. Süre tahmini, aktif iş yükü dağılımından yaklaşık bir aralık olarak üretilir; kalibre edilmiş güven aralığı değildir.
- Başlangıç önerisi: en az %80 kapsama olmadan tüm yük için toplam dakika gösterme. Kısmi tahminde yalnız kapsanan soru sayısı ve bilinmeyen kısım gösterilir. Yetersiz veride yalnız soru adedi.

### 9.3 “Bir sonraki adım”

Deterministik öncelik: zamanı gelenler → yakın geçmişte tekrar güçlük yaşayan uygun konu/kaynak → küçük, kullanıcı tarafından başlatılan bir tur.

Öneri bir gerekçe ve tek eylem verir. “Bu konudan çalış” mevcut Filtreli/Odak hattına bağlanır. Gereken ders/vade kapsamı `SoruCalismaFiltresi` ve ortak uygunluk sorgusuna eklenir. Aynı gün tekrarlanan, askıdaki, arşivli veya seçili format dışındaki soru öneriyle gizlice aktifleşmez. Veri veya uygun soru yoksa öneri uydurulmaz; günlük hedef otomatik yükseltilmez.

## 10. Premium görsel sistem

### 10.1 Kimlik

Mevcut petrol mavisi, sıcak açık zemin ve bakır vurguyu güçlendirmek önerilir. Açık/koyu tema birlikte tasarlanır. Altın/ışıltı yalnız başarı anlarında kullanılır; sürekli parlayan neon katmanlar yok.

- 4/8/12/16/24 dp boşluk ölçeği; kontrollü kart köşeleri ve tipografi.
- Mevcut 15 amblem/30 çizim önce değerlendirilir; hepsi gereksiz yere yeniden üretilmez.
- Büyük rütbe kartı ilk ekranı tek başına kaplamaz. Görsel hiyerarşi: rütbe → seviye → XP → sonraki rütbe.
- Kilitli ve gizli ayrımı korunur. Gizli çizim/isim erişilebilirlik metninde de sızmaz.

### 10.2 İlerleme ekranı sırası

1. **Rütbe kahraman kartı:** mevcut unvan/amblem, seviye, XP, sonraki seviye/rütbe.
2. **Bir sonraki adım:** kısa gerekçe + çalışmaya geçiş.
3. **Küçük istatistikler:** seri, tekrar, bağımsız çözüm; emek ile öğrenme ayrı başlıklarla.
4. **Gelişim:** 7 günlük ritim ve yeterli veride eğilim.
5. **Ders/kaynak/konu:** veriye dayalı özet; ayrıntıya geçiş.
6. **Tekrar yükü:** 7 günlük takvim/çubuklar; varsa yaklaşık süre ve kapsama.
7. **Koleksiyon:** seçili rozetler/rütbeler ve tümünü açan galeri; ana sayfaya bütün koleksiyon yüklenmez.

İlk çizimlerde `YetersizVeri`, `Hazir`, `Yukleniyor`, `Hata` durumları birlikte tasarlanır. Örnek değerler yalnız Preview/test fixture'ında bulunur; üretimde hayalî 64 soru/%68 grafiği yok.

### 10.3 Hareket sözleşmesi

| Olay | Başlangıç hedefi | Kural |
|---|---|---|
| Buton/kart geri bildirimi | 120–180 ms | Dokunma anlaşılır; akış bekletilmez |
| Sayfa/kart girişleri | 180–280 ms | Her kaydırmada tekrarlamaz |
| Gerçek XP değişimi | 400–600 ms | Yeni veri farkı; sayfaya her giriş XP kazanımı değil |
| Grafik çizimi | 300–500 ms | Değeri okunur kılar, sonucu süslemez |
| Rütbe/rozet kutlaması | ~900–1200 ms efekt | Efekt biter; kullanıcı kapatana kadar bilgi kalır |

`HareketPolitikasi` P1’de İlerleme/karne/kutlama/Odak kaydırması için ortak kaynak olarak eklendi; sonraki bileşenler de aynı politikayı kullanır. Sistem hareket azaltma/animasyon kapalı tercihinde anlık son durum; işlev kaybı yok. Sürekli sonsuz animasyon veya ağır blur yok.

Rütbe kutlaması için kalıcı görülme/kuyruk takibi gerekir. İlk kurulum, migration, geri yükleme veya hedef değişiminin XP yeniden hesabı toplu “yeni başarı” yağmuruna dönüştürülmez. Tek ilerlemede çok eşik geçilirse kısa bir özet/son rütbe gösterilir. Sahiplik mevcut rozet deposunda korunur; ortak sunum kuyruğu iki tam ekran kutlamayı üst üste açmaz. Rütbe atlamak ayrıca XP üretmez.

## 11. Kod paketleri ve bağımlılıklar

**P0 ve P1 kod/otomatik test bakımından tamamlandı; fiziksel kabul açık. P2–P6 planlandı, uygulanmadı.** “Bitti” için kodun yazılması değil ilgili kabul kapısının geçilmesi gerekir.

| Paket | Başlıca görevler / dosyalar | Bağımlılık | Kabul kapısı |
|---|---|---|---|
| P0 — Sağlam başlangıç (uygulandı) | Mevcut test tabanı; örnek senaryolar; `KarneEkrani` rozet snapshot tutarlılığı, ödül gözlem hatası/yenileme ve restore sonrası state sınırlarının regresyonları | Yok | Eski davranış doğrulanır; mevcut 130 test korunur, gerçek cihaz bekleyenleri gizlenmez |
| P1 — Görsel sistem/prototip (uygulandı) | `ui/tema/{Renk,Tipografi}.kt`, yeni `HareketPolitikasi`, `SeviyeEkrani` durum modelleri ve tasarım Preview'ları | P0; P2 ile bağımsız çalışabilir | Yeni/yoğun/verisiz/hatalı durum; koyu tema, büyük yazı, hareket kapalı |
| P2 — Ölçüm ve veri güvenliği | Yeni `domain/calisma/AktifSureOlcer.kt`, ölçüm modelleri; Odak ve devam deposu; `SoruRepository`/`RoomSoruRepository`; yeni entity/DAO; Database/Provider/Migrations; export/import | P0 | Saat/lifecycle/kurtarma, atomik kayıt, migration ve ZIP roundtrip yeşil; planlar hâlâ v2 |
| P3 — Kişisel profil | Yeni `domain/analiz/KisiselSureProfili.kt`, `ProfilPolitikasi.kt`, `data/analiz/GelisimHizmeti.kt` ve toplu sorgular | P2 | Az veri, uç değer, aynı soru baskısı, tarihsel bağlam ve soğuk başlangıç testleri |
| P4 — Karar v3 / gölge | Yeni `domain/repeat/UyarlanabilirTekrarKurali.kt`, ortak karar servisi ve sürüm yönlendirme; Odak önizlemesi | P3 | Önizleme=yazma; çift ceza yok; eski sonuçlar aynı; gölgede tarih değişmiyor |
| P5 — Gerçek veriyle arayüz/eylem | `SeviyeHizmeti`, `SeviyeEkrani`, yeni `ui/ilerleme` bileşenleri; kaynak/konu detayları; filtre ve root route; rütbe kutlama host'u | P1 + P3; aday motor açık olmak zorunda değil | Gerçek veri/boş durum; kaynak veya konudan doğru ve uygun oturuma geçiş |
| P6 — Etkinleştirme ve kalite | Politika modları; sorgu/cache iyileştirmesi; erişilebilirlik; release/device ölçümü; belgeler | P4 + P5 | Tüm regresyonlar, cihaz kanıtı, imza ve uyumluluk; karar katmanı kontrollü açılır |

Önemli ayrım: **P5, v3'ü etkinleştirmeyi beklemek zorunda değil.** Doğru ölçülmüş profiller ve güzel arayüz, mevcut güvenli v2 planlamayla da değer sağlar. Tek birleşik veri modeli, tek seferde büyük ve riskli yayın demek değildir.

## 12. Test matrisi

| Alan | Zorunlu örnekler |
|---|---|
| Saat | 12 saniye deneme + 30 saniye mola + 8 saniye deneme = 20; cihaz saati geri alınırsa aktif süre değişmez |
| Faz ayrımı | 20 saniye deneme + 15 saniye çözüm inceleme = 20/15; değerlendirme bekleme süresi eklenmez |
| Kurtarma | Döndürme, arka plan, tam ekran görsel, sona bırak, zorla kapatma, yeniden başlatma; birikim karışmaz, belirsiz bölüm kısmi |
| Kayıt | Çift dokunma, aynı gün, hata sonrası retry; tekrar/ölçüm/plan atomik; commit–checkpoint arası kesintide çoğalma yok |
| Migration | 1/2/3→4; eski soru/tekrar/plan aynı, süre yok; boş DB ve dolu DB |
| ZIP | v2/v3 okuma; v4 tam tur; yetim ilişki, bozuk süre/snapshot, bilinmeyen sürüm, başarısız restore, kapsam/hash kontrolleri |
| Profil | 0/1 örnek, çok örnek ama tek soru/tek gün, eşik sınırları, uç değer, MAD=0, eksik süre, mevcut denemenin profile sızmaması |
| Analiz | Kaynak/konu değişimi, konusuz/kaynaksız, karışan dersler, farklı dönem uzunluğu, aynı-soru karşılaştırması, profil değişimi, kısmi süre tahmini |
| Motor | Uzun süre tek başına değişiklik yok; hızlılık tek başına bonus yok; eksik süre v2; çözüm/fail 1 gün; aynı-gün mükerrerleri tek say; iki başarı toparlanması; gecikme + ikinci ceza yok; yuvarlama/bant sınırı |
| Önizleme | Kaydetmeden önce gece yarısı, saat dilimi/geçmiş/profil değişimi; güncellenecek karar açıkça görünür |
| UI | Boş/hatalı/az verili ekran; TalkBack, 48 dp hedefler, büyük yazı, açık/koyu, gizli isim/koşul sızıntısı yok, hareket kapalı |
| Kutlama | Tekrar açmada replay yok; restore/hedef hesabı toplu kutlama değil; çok eşikte özet; rozet sahipliği korunur |
| Akış | Öneri → filtre → odak → karne → ilerleme; askı/arşiv/profil/same-day sınırları bütün yollarda aynı |

Yeni test sayısı önceden başarı kanıtı gibi ilan edilmez. Önce mevcut 130 test korunur, yeni davranışlar bu matrise göre kapsanır.

## 13. Performans ve doğrulama

- Compose gövdesinde DB sorgusu/medyan hesabı yok. Sorgular IO'da, saf dağılım hesapları ana iş parçacığı dışında.
- Profil/özet cache'i veri değişimi, gün geçişi, ilgili filtre/tercih ve restore sonrasında geçersizleşir. Saniyelik sayaç bütün analitiği yeniden hesaplatmaz.
- Kaynak başına N+1 sorgu yerine pencere/filtre bazlı toplu sorgular; indeksler gerçek query plan ile ölçülür.
- Grafiklerin metinsel özeti ve TalkBack açıklaması vardır; renk tek bilgi taşıyıcısı olmaz.
- Benchmark hedef verisi: örneğin 1.000 soru/20.000 tekrar; sentetik veri yalnız testte, Git'e büyük veri eklenmez.
- Cihaz: S24 FE + mümkünse daha düşük güçlü bir Android. Üç düğmeli/jest nav, yatay ekran, %200 yazı ve animasyonlar kapalı.
- Hedef 60 Hz'de akıcı kaydırma (16,7 ms kare bütçesi); p95 kare süresi/jank ve analitik ilk yükleme ayrıca raporlanır. Bunlar ölçülmüş sonuç değil, kabul hedefidir; yalnız güçlü cihazda yeşil olmak yeterli sayılmaz.
- Çalışma ortamının Java/SDK/Maven sınırları nedeniyle gerçek build/test mevcut CI hattından; bash çağrıları sınırlı, uzun `watch` yerine kısa durum sorguları. Cihaz testi yapılmadıysa yapılmış denmez.

## 14. Teslim sınırları ve zamanlama

Takvimden önce kabul kapıları esas alınır. Tek geliştirici için kabaca planlama aralıkları, çalışma saati/teslim sözü değildir:

| Paket | Kaba iş günü aralığı |
|---|---:|
| P0 | 1–2 |
| P1 | 2–4 |
| P2 (ölçüm + migration + yedek dahil) | 4–7 |
| P3 | 3–5 |
| P4 | 3–5 |
| P5 | 3–5 |
| P6 | 2–4 |

Toplam yaklaşık **18–32 iş günü**; tek geliştiricide P1'i diğer işle aynı anda bitmiş sayarak süre düşürülmez. Gölge modunun gerçek kullanım verisi biriktirmesi bu mühendislik süresinden ayrı takvim süresidir. 3–6 hafta hedef olarak konuşulabilir ama garanti değildir; kod ve cihaz bulguları sonrası yeniden tahmin edilir.

Önerilen yayın dilimleri:

- **Teslim A:** görsel temel + güvenilir ölçüm + Room/ZIP v4. Planlama hâlâ v2; öğrenme grafiği veri yeterli değilse kapalı/boş.
- **Teslim B:** gerçek kaynak/konu profilleri + gelişim özeti + eyleme geçiş + kontrollü animasyonlar. Aday motor gölgede kalabilir.
- **Teslim C:** testler ve veri dayanağı uygunsa v3'ü koşullu etkinleştir; sorun halinde yalnız yeni katmanı kapat.

İlk kodlanacak dikey dilim: **tek sorunun zamanını doğru ölç → aynı transaction'da kaydet → geçmişte göster → ZIP'ten geri getir → v2 tarihinin değişmediğini doğrula.** Buna paralel olarak izole İlerleme tasarım prototipi hazırlanır. Bu dilim geçmeden süreye dayalı puan/karar eklenmez.

## 15. Bu plana eklemediğimiz işler

Yeni rozet/rütbe sayısını artırma, XP ekonomisini değiştirme, mola günü jetonu, geçmiş karne arşivi, bildirimler, otomatik soru çözme, bulut senkronizasyonu ve kaynak kalitesi sıralaması bu paketlerin kapsamı dışındadır. Daha önce konuşulmuş işler mevcutmuş gibi işaretlenmez.

**Başarı ölçütü:** Kullanıcı güzel bir ekrana bakmakla kalmıyor; hangi bilgiye dayanarak ne önerildiğini anlıyor, uygun soruya tek adımda geçiyor ve geçmiş verisi/öğrenme planı beklenmedik biçimde değişmiyor.
