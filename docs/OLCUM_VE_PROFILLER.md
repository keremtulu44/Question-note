# P2/P3 — Süre ölçümü ve kişisel referanslar

**Kapsam:** Yanlış Defteri'ndeki çalışma ölçümü ve profil altyapısı. Genel sınav hızı, zekâ,
öğrenme başarısı veya bilimsel güven yüzdesi ölçülmez. **Tekrar v2 ve XP kuralları değişmez.**
Gölge/etkin v3 motor, kaynak karşılaştırma panosu ve yeni çalışma önerileri P4/P5'e aittir.

Derleme ve test kanıtı [DERLEME_RAPORU.md](DERLEME_RAPORU.md) içinde; fiziksel cihaz kabulü
[TEST_KONTROL_LISTESI.md](TEST_KONTROL_LISTESI.md) içinde ayrı izlenir.

## 1. İki faz, iki farklı saat

`AktifSureOlcer`, enjekte edilen monotonik saatin segment farklarını toplar. Android adaptörü
`SystemClock.elapsedRealtime()` kullanır. Bir saniyelik tick yalnız gösterimi yeniler; tick
sayısı kaydedilen süre değildir. Takvim günü/zaman damgası ayrı `Clock` kaynağından gelir;
`YerelTakvimSaati` cihaz saat dilimindeki değişiklikleri gözlemler.

| Olay/durum | Davranış |
|---|---|
| Soru metni veya görseli hazır, geçmiş yüklü, uygulama ön planda | Çözüm açılmadıysa deneme süresi artar |
| Çözüm ilk kez açıldı | Deneme kapanır, inceleme/destekli çalışma fazına geçilir |
| Çözüm gizlendi/yeniden açıldı | Deneme fazına geri dönülmez |
| Değerlendirmeye geçiş | Süre/deneme dondurulur; seçenek düşünme ve kaydetme eklenmez |
| Çalışmaya dön | Aynı denemede yeni aktif segment açılabilir |
| Mola, yükleme, çıkış onayı, arka plan | Süre durur |
| Tam ekran görsel/zoom | Süre durur; ölçüm kısmi olur |
| Sona bırak | Soruya ait birikim ve deneme ID'si korunur; diğer soruya taşınmaz |
| Kaydetme hatası | Aynı kimlik ve dondurulmuş ölçümle yeniden denenir |

Yakınlaştırarak çalışma gerçekten emek olabilir. İlk teslimde bu süre **ölçülmüyor**; bu yüzden
kısmi deneme hız/süre referansına katılmaz. İnceleme süresi de bütünüyle çözümü okumak anlamına
gelmez: çözüm açıldıktan sonraki aktif ekran çalışmasıdır.

## 2. Kalite, sınırlar, devam noktası

- `YOK`: süre alanları **null**. Eski tekrar veya ölçümsüz ayrıntı ekranı; sıfır saniye değil.
- `TAM`: tanımlı ekran ölçümünün kapsamı tam. Gerçek dikkat/başarı garantisi değildir.
- `KISMI`: bilinen birikim var ama süreç kurtarma, tam ekran, eksik görsel veya eski oturum nedeniyle
  kapsam eksik. Ham birikim korunur; referans dışındadır.
- `GECERSIZ`: tutarsız monotonik saat, alan/sürüm hatası veya güvenlik sınırı. Referans dışındadır.
- Faz başına **24 saat** üst sınırı vardır; taşma/üst sınır aşıldığında ölçüm tam diye kırpılmaz,
  geçersiz işaretlenir. Geçerli uç değer ham kayıtta silinmez/değiştirilmez.
- Pozitif tam ölçüm referansa adaydır. Tam ama sıfır süre profil oluşturmaz; olmayan süre null kalır.
- Ölçüm hatası geçerli öz değerlendirme, karne veya XP kaydını engellemez.

Devam JSON'u sürüm 2; soru başına `OlcumDevamNoktasi`: deneme ID'si, birikimler, kalite/nedenler,
çözüm açma, dondurma, seçim ve revizyon. **Monotonik başlangıç anı saklanmaz.** Yeni süreç yalnız
birikimi alır ve kısmi işaretler; kapalı geçen zamanı eklemez. Uygulama içindeki döndürme aynı
`AppContainer`/devam deposunu paylaşır; lifecycle duruşunda alınan birikim kullanılır.

Durum geçişlerinde ve aktif ön planda yaklaşık 5 saniyede bir devam noktası IO'da yazılır.
Revizyon kontrolü eski yazının yeni konumu geri almasını; oturum kontrolü ise temizlenen veya
başka soruya geçmiş kaydı yeniden diriltmesini engeller. Restore temizliği kalıcı olmadan
recovery işaretleri kaldırılmaz. Disk hatasında uyarı vardır; son bilinmeyen segment uydurulmaz.

## 3. Önizleme ve atomik kayıt

`TekrarDenemesi` + `TekrarOnizlemesi`, hem Odak hem ölçümsüz soru ayrıntısının ortak sözleşmesidir.
Önizleme gerçek DB geçmişi, kayıt bağlamı, yerel gün/saat dilimi ve mevcut deneme hariç profille
üretilir. V2 temel planı ve sınırlı profil özeti beraber dondurulur.

Kaydetme transaction'ında:

1. Aynı deneme ID'si zaten kaydedildiyse aynı soru/sonuç/çözüm/oturum için başarı döner;
   ikinci tekrar, ölçüm veya XP oluşmaz. Çelişen kimlik kullanımı reddedilir.
2. Soru, aktif plan ve aynı-gün sınırı denetlenir.
3. Önizleme yeniden hesaplanıp imza/plan karşılaştırılır. Gün, saat dilimi, geçmiş, ilgili
   bağlam veya profil değişmişse yazılmaz; kullanıcı yeni önizlemeyi kontrol edip yeniden kaydeder.
4. **Tekrar + ölçüm/bağlam + sorunun v2 planı tek transaction'da** yazılır. Ölçüm ekleme hatası
   hepsini geri alır.

Çözüm açma işareti, içerik gösterilmeden önce en güncel devam noktasıyla IO'da kalıcılaştırılır.
Superseded eski yazı başarı/onay sayılmaz. Başarısızsa çözüm açma yeniden denenir.

Room commit'i ile prefs devam yazısı arasındaki kesinti, tekrar geçmişindeki deneme/oturum kimliğiyle
uzlaştırılır; ilk soru checkpoint'i yetişmemiş ve duvar saati geri alınmış olsa bile oturum kimliği korunur. Eski kimliksiz kayıtlar için mevcut zaman damgalı uzlaştırma korunur. Prefs, SQLite
transaction'ına dahilmiş gibi sunulmaz; tekilleştirme ve kurtarma bu iki depoyu uzlaştırır.

## 4. Room v4 ve ZIP v4

`MIGRATION_3_4`, `tekrar_olcumleri` tablosunu ve indeksleri ekler; eski soru/tekrar/tarihlere
UPDATE yapmaz. 1→2→3→4 zinciri korunur; yıkıcı migration veya korumasız downgrade yoktur.

Yeni tablo:

- `tekrar_id`: PK, `tekrarlar.id` FK, silmede cascade; ilişki 0..1.
- `deneme_id`: benzersiz; `oturum_id`: nullable.
- Nullable deneme/inceleme ms, kalite, nedenler JSON'u, ölçüm sürümü 1.
- Kayıt anı ders/konu ID ve etiketleri, normalize kaynak anahtarı/etiketi.
- Boyutu sınırlı karar JSON'u: sürüm 1, **V2** modu, tarih/saat dilimi, temel/final aralık,
  sonraki gün, profil politikası/örneklemi/varsa dağılım. P4 kararı verilmiş gibi gösterilmez.

Bağlam etiketleri tarihsel snapshot'tır; konu/kaynak sonradan değişince geçmiş sessizce yeniden
etiketlenmez. Bu etiketlerin güncel kataloğa FK'sı yoktur; soru/tekrar silinince ölçüm de silinir.

ZIP v4: `data.json.measurements`, `csv/measurements.csv`, manifest ölçüm sayısı ve dosya
boyut/hash envanteri. CSV insan içindir; kanonik içe alma JSON'dur. Formül önekleri CSV'de
nötrleştirilir. Yeni tablo da eski güvenlik kopyası/journal/commit protokolüne dahildir.

Okuyucular açıkça **v2 / v3 / v4** seçer. V2 katalog yeniden kurulumunu; v3 katalog/locator'ı
korur. V2/v3'ten süre/bağlam uydurulmaz. V4'te yetim/yinelenen ilişki, negatif/taşmış süre,
bozuk kalite veya snapshot, sürüm/şema/CSV uyuşmazlığı ve checksum hatası reddedilir.

Rozet, hedef, sınav formatı ve cihaz-yerel devam noktası ZIP'e eklenmedi. **Tamamlanmış yeni
ölçüm ve kayıt bağlamı ZIP'e dahildir.** Yeni v4 yedeği eski uygulamaya verme/downgrade desteklenmez.

## 5. Kişisel referans politikası — sürüm 1

- Pencere: kaydedilmiş yerel günler üzerinden **son 60 gün**; gelecekteki günler hariç.
- Yardımsız: `TAM`, pozitif deneme süresi, `RAHAT`, çözüm açılmamış.
- İş yükü: tüm sonuçlardaki pozitif `TAM` deneme + inceleme; yalnız rahat/kolay kayıtlarla daraltılmaz.
- Mevcut deneme, yeterlilik ve dağılım hesabından **önce** çıkarılır.
- Aynı soru/gün tek örnek; sonra soru başına medyan. Dağılım bu soru özetlerinden hesaplanır,
  tek kolay sorunun çok tekrarı profili ele geçiremez.
- Medyan, çeyrekler (doğrusal ara değer) ve MAD (medyandan mutlak sapmaların medyanı).
  MAD/IQR sıfır olabilir; oran/sapma puanına bölme yapılmaz.

| Düzey | Geçerli ölçüm | Farklı soru | Farklı gün |
|---|---:|---:|---:|
| Ders | 20 | 10 | 5 |
| Ders içindeki konu/kaynak | 30 | 15 | 7 |

Üç eşik **birlikte** aranır. Bunlar test edilip ayarlanabilecek ürün varsayımlarıdır, bilimsel
kalibrasyon veya güven yüzdesi değildir. Az veride dağılım/referans null; örneklem ve eksikler
sunulur. Konu/kaynak yetersizse yalnız **aynı dersin** yeterli referansına dönülür; ders de
yetersizse referans yoktur. Farklı dersler aynı kitap ismi üzerinden birleştirilmez.

Kaynak anahtarı: NFC, Türkçe küçük harf, kenar/tekrarlı boşluk temizliği. Farklı yazılmış kitap
adları benzerlik algoritmasıyla birleştirilmez. Grup seçimi kayıt anı bağlamına dayanır.

Profil kalıcı cache değildir: tarih/silme/restore sonrası DB'den yeniden türetilir. İlgili pencere
tek toplu sorguyla alınır; ders/konu/kaynak ve gün indeksleri vardır. Compose gövdesinde sorgu veya
medyan hesabı yoktur. Saniyelik sayaç analitik hesaplatmaz.

## 6. Görünüm ve sınırlar

İlerleme, tarih aralığıyla tam/kısmi/ölçülmemiş/geçersiz kapsamı gösterir. Ders referansı oluşunca
medyan/çeyrek/MAD ve örneklem gelir; oluşmadıysa **ölçüm birikiyor**. Tamamlanmış tekrar geçmişi
iki fazı ve kaliteyi gösterir. Eski kayıtta **süre ölçülmedi** yazar.

P3 altyapısı konu/kaynak profili ve ders fallback'i de hesaplar; kaynakların başarı sıralaması,
nedensel kitap kalitesi iddiası ve kaynak/konudan çalışma önerisi UI'sı **P5 kapsamıdır; henüz yoktur**.
Süre nedeniyle ödül/ceza, aday aralık veya v3 plan yazılmaz. Fiziksel cihaz, gerçek süreç ölümü,
TalkBack ve büyük arşiv performans kabulü otomatik testlerden ayrı tutulur.
