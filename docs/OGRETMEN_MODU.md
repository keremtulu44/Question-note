# Öğretmen modu — uygulama sözleşmesi (v1.6.0)

> Öğretmen çalışma alanı **Room v5** üzerinde tutulur; JSON yalnız yedek/aktarım biçimidir.
> Rol seçimi aynı cihazda, aynı uygulama içindedir: öğrenci tarafı defteri ile öğretmen defteri
> aynı veritabanını paylaşır, ancak tabloları ve fotoğraf klasörleri ayrıdır.
> Son güncelleme: 2026-10-26.

## 1. Neden bu yapı

Önceki sürümde öğretmen verisi tek bir JSON dosyasında (`ogretmen_calisma_alani.json`) tutuluyordu.
Dosya her değişiklikte baştan yazıldığı için:

- yazma sırasında uygulama öldürülürse dosya yarım kalıyor, açılışta "boş çalışma alanı" görünüyordu,
- tek bir bozuk kayıt tüm listeyi okuyamaz hâle getirebiliyordu,
- gruplar/sorular üzerinde toplu işlem (ör. 30 soruyu "çözüldü" yapmak) tüm dosyayı yeniden yazıyordu.

Room v5 bu üç sorunu da kapatır: her işlem ya tamamen yazılır ya hiç yazılmaz, okuma indekslidir,
bozuk tek satır diğerlerini etkilemez. Eski JSON dosyası **bir kez** toleranslı biçimde içe
aktarılır ve `ogretmen_calisma_alani.ice-aktarildi.json` adıyla arşivlenir.

## 2. Veri modeli (kısa)

| Tablo | Rolü |
|---|---|
| `ogretmen_gruplari` | Sınıf/özel ders grubu: `ad`, `aciklama`, isteğe bağlı `ders_id`, `arsivli`, `sira` |
| `ogretmen_sorulari` | Soru: `grup_id` (CASCADE), isteğe bağlı `ders_id`/`konu_id` (SET NULL), `kaynak`, `kim_sordu`, `plan_gun`, `zorluk`, `cevap`, `durum`, `cozulme` |
| `ogretmen_soru_gorselleri` | Soru ve çözüm fotoğrafları: `soru_id` (CASCADE), `tur` (`SORU`/`COZUM`), `sira`, dosya adı + sha256 |

- **Ders ve konu kataloğu ortaktır.** Öğrenci tarafında oluşturulan ders/konu, öğretmen formunda
  anında seçilebilir; tersi de geçerlidir. Öğretmen tarafı katalog satırı **oluşturmaz**, yalnız
  id ile bağlanır. Katalogdan ders silinirse etiket `NULL` olur, soru silinmez.
- `durum` üç değerlidir: `BEKLIYOR` (kuyrukta), `COZULDU`, `ARSIV`. Arşiv kaydı **silmez**;
  fotoğrafları ve geçmişi yerinde kalır, yalnız kuyruktan ve gruptan çıkar.
- `plan_gun` yerel takvim günüdür (epoch gün). Yalnız sıralama ve "geciken" filtresi için kullanılır;
  hiçbir soru tarihi geçti diye silinmez veya sıfırlanmaz.

## 3. Ekranlar ve akış

### Gruplarım (açılış sekmesi)
Mevcut kullanıcı ilk ekranını kaybetmez. Her kartta grup adı, not, bekleyen/çözülen sayısı ve
çözülme oranı görünür. Grup ekleme sihirbaz değil tek diyalogdur (ad + not + isteğe bağlı ders).
Grup düzenleme/arşivleme/silme grup detayındaki `⋮` menüsündedir.

### Bugün (kuyruk)
Tüm gruplardan **çözülmemiş** sorular tek listede, en uzun bekleyen en üstte. Gecikenler filtresi
`plan_gun < bugün` koşulunu kullanır. Grup çipleriyle tek sınıfa daraltılabilir. Bu ekran
"bugün ne çözeceğim" sorusunun cevabıdır; önceki sürümde beş grubu tek tek açmak gerekiyordu.

### Grup detayı
Üç sekme: **Çözmediklerim / Çözdüklerim / Arşiv**. Üstteki menüden grup düzenlenir, arşivlenir
veya silinir. Grup silme onayı **kaç sorunun** silineceğini ve **son yedeğin tarihini** yazar;
yedek hiç alınmadıysa bunu açıkça söyler.

### Sunum (tam ekran)
Karta dokununca açılır, sınıfta göstermek için tasarlandı:

- Ekran sunum boyunca açık kalır (`keepScreenOn`).
- Cevap **varsayılan olarak gizlidir**; "Cevabı göster" ile açılır. Uzun cevap kutuyu taşırmaz,
  kendi içinde kaydırılır.
- Çözüm fotoğrafları ayrı düğmeyle açılır; öğrenciye soru gösterilirken görünmez.
- "Çözdüm, sıradaki" tek dokunuşla durumu değiştirir ve listede ilerler — öğretmen öğrencinin
  yanındayken menü aramaz.
- Önceki/sonraki, çift dokunuşla yakınlaştırma, "başka gruba kopyala".

### Form
Fotoğraf(lar) + isteğe bağlı başlık, zorluk, cevap, **ders, konu, kaynak, kim sordu, çözüm günü**,
çözüm fotoğrafları. "Kaydet ve yeni çek" bir soruyu kaydedip formu temizler: arka arkaya gelen
soruları tek oturumda girmek için.

### Kamera
`ACTION_IMAGE_CAPTURE` + kendi `FileProvider` yolu kullanılır; manifestte `CAMERA` izni **yoktur**
ve gerekmemez. Önceki sürümdeki "izin verildi mi?" kapısı her zaman başarısız olduğu için kamera
düğmesi ölüydü; bu kapı kaldırıldı.

### Pencere ve geçiş kuralları

- Onay/seçim/form pencereleri `OgretmenDiyalogu` ile çizilir (düz `Dialog` + yüzey). Material3
  `AlertDialog` Robolectric Compose testlerinde boşta kalmıyor (Espresso `AppNotIdle`), bu yüzden
  kullanılmıyor. Etiketler (`teacher_archive_confirm`, `hedef_grup_<id>`, ...) aynı kaldı.
- Host ekranları `AnimatedContent` kullanmaz: ekranlar kendi `Dialog` pencerelerini taşıdığı için
  geçiş animasyonu pencereyi her karede yeniden kuruyor ve testler boşta kalamıyordu.
- Bilgi bandı (içe aktarma) ve alt menü kaydırma ile sekme değiştirme korunur.

## 4. Toplu işlem, arşiv ve geri alma

- **Toplu seçim:** çözüldü yap / çözülmediye al / arşivle / sil. Durum değişimi tek SQL ifadesidir.
- **Silme geri alınabilir:** satırlar silinir, dosyalar kısa süre diskte kalır; snackbar'daki
  "Geri al" aynı id ve aynı fotoğrafla kaydı geri getirir. Kullanılmayan dosyalar daha sonra
  Ayarlar → Depolama ve bakım ekranından, **yaş ≥ 7 gün** ve kullanıcı onayıyla temizlenir.
- **Grup silme geri alınamaz** (fotoğraflar dahil); bu yüzden onay metni soru sayısını ve son yedek
  tarihini taşır.

## 5. Yedekleme

ZIP sürümü **5**'tir. Öğretmen çalışma alanı `data.json.teacher_workspace` altında, fotoğraflar
öğrenci fotoğraflarıyla aynı `images/` klasöründe durur (dosya adı çakışması yoktur: her dosya
kendi UUID'siyle adlandırılır). Okuma v2/v3/v4/v5 yedeklerini destekler. Ayrıntı:
[YEDEK_FORMATI.md](YEDEK_FORMATI.md).

## 6. Sınırlar ve bilinçli olarak yapılmayanlar

- Şifre, biyometrik kilit, yedek şifreleme **yoktur** (kullanıcı kararı).
- Bulut/hesap senkronizasyonu, Drive API/OAuth **yoktur**; yedek, sistem dosya seçicisiyle
  kullanıcının seçtiği klasöre yazılır (izin bir kez verilir, sonra sessizce kullanılır).
- OCR/yapay zekâ ile otomatik çözüm, öğretmene XP/oyunlaştırma, doğrudan WhatsApp entegrasyonu
  kapsam dışıdır.
- Öğrenci fotoğrafı tarayıcı bakımı (`img/`) ile öğretmen klasörü (`ogretmen-img/`) ayrıdır; bakım
  ekranı öğrenci dosyalarına dokunmaz.

## 7. Test kapsamı

| Test | Kapsam |
|---|---|
| `OgretmenMigrationTest` | Gerçek Room v4 şemasından 4→5 göçü, eski satırların korunması, yeni indeksler, CASCADE/SET NULL davranışı |
| `OgretmenDeposuRoomTest` | Eski JSON'un tek seferlik ve toleranslı içe aktarımı, toplu durum, taşıma, kopyalama, silme geri alma |
| `OgretmenVeSorulacaklarVeriTest` | Öğretmen↔öğrenci köprüsü ve ZIP gidiş-dönüşü |
| `KirpmaSorulacakVeOgretmenArayuzTest` | Kuyruk/grup/sunum etkileşimi, gizli cevap, başka gruba kopyalama |
| `GorselButunlukVeYerlesimArayuzTest` | Büyük yazı ölçeğinde taşma, paylaşılan snackbar |

**Doğrulanmış paket (CI `40280978018`):** Debug 351/0/0/0 · Release 277/0/0/0; `assembleRelease`,
imza ve beklenen sertifika parmak izi eşleşti, Release `v1.6.0` yayımlandı. Ortak testler iki
varyantta tekrar eder; sayılar toplanmaz. Fiziksel cihaz kabulü hâlâ açık (`ROADMAP.md`).

**Test kuralları:** öğretmen pencereleri `OgretmenDiyalogu` ile çizildiği için testler pencere
kabuğunu (`ogretmen_kopyala_dialog`) bekler; kart içi aksiyonlar dokunma yönlendirmesine bağlı
kalmadan semantik `OnClick` ile tetiklenir.
