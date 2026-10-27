# Tam ZIP yedek biçimi — v5

Güncel yazım: `yanlis-defteri-backup`, **ZIP v5 / Room v5**. Okuma: **v2, v3, v4, v5**; her biri
kendi şema/CSV/alan sözleşmesiyle doğrulanır. Algoritma ve karar snapshot sürümleri bunlardan ayrıdır.
Varsayılan **Gölge**, v2 planını uygular ve ayrı v3 adayı saklar. Son güncelleme: 2026-10-18.

## 1. Arşiv yapısı

```text
yanlis-defteri-yedek.zip
├── manifest.json
├── data.json                 # kanonik geri yükleme kaynağı (öğretmen çalışma alanı dahil)
├── csv/
│   ├── lessons.csv
│   ├── topics.csv
│   ├── questions.csv
│   ├── catalog_options.csv
│   ├── images.csv
│   ├── reviews.csv
│   └── measurements.csv      # süre/kalite, kayıt bağlamı, karar özeti
└── images/
    └── <uuid>.jpg
```

Ders/konu, eski soru/çözüm notları, plan/öğrenme durumu, tekrar geçmişi, katalog, kaynak konumu
ve fotoğraflar korunur. V4 ayrıca `data.json.measurements` dizisini içerir. Tamamlanmış tekrarın
ölçümü, kalite/nedenler, kayıt anındaki ders/konu/kaynak ve sınırlı karar snapshot'ı taşınır.
CSV yardımcı insan kopyasıdır; geri yükleme JSON'u kullanır. Formül önekleri CSV'de nötrleştirilir.
Cihazdaki mutlak dosya yolu, bitmap önizlemesi veya saniyelik olay günlüğü taşınmaz.

## 2. Sürüm ve ölçüm sözleşmesi

| ZIP | Room alanı | CSV/JSON farkı |
|---|---:|---|
| v2 | 2 | 5 CSV; kaynak/neden kataloğu sorulardan yeniden kurulur; locator/ölçüm yok |
| v3 | 3 | 6 CSV; katalog ve `source_location` korunur; ölçüm yok |
| v4 | 4 | 7 CSV; `measurements` JSON/CSV, manifest ölçüm sayısı |
| v5 | 5 | 7 CSV; `teacher_workspace` JSON'da (öğretmen grupları/soruları); öğretmen fotoğrafları paylaşılan `images/` altında |

Eski yedekten süre veya o günkü bilinmeyen konu/kaynak bilgisi uydurulmaz; ölçüm tablosu boş
kalır. Yeni ölçümsüz tekrarın bağlamı kaydedilebilir ama süreleri null, kalitesi `YOK` olur.
V4 ölçüm başına `review_id` FK/PK, benzersiz `attempt_id`, nullable `session_id`, iki nullable ms,
kalite/nedenler, ölçüm sürümü 1, tarihsel bağlam ve karar JSON'u vardır. Ayrıntı:
[OLCUM_VE_PROFILLER.md](OLCUM_VE_PROFILLER.md).

Yetim/yinelenen ölçüm/deneme kimliği, negatif/taşan süre, desteklenmeyen kalite/sürüm,
normalize kaynak uyuşmazlığı, bozuk/çok büyük karar snapshot'ı ve tekrar/karar aralığı uyuşmazlığı
reddedilir. Süreler tam sayı ms'dir; sayısal taşma veya metin/ondalık coercion ile kabul edilmez.
Karar snapshot **1 / V2** ve **2 / GOLGE–ETKIN** açıkça doğrulanır. Snapshot2'de temel/aday/uygulanan
aralık, algoritma sürümü, yakın günler ve gerekçeler yeniden hesaplanan kararla tutarlı olmalıdır.
Etkin3 geçmişi içe alınabilir; bu, mevcut cihazın etkinleştirme kapısını açmaz. Kapalı'ya dönüş
eski v3 geçmişini korur, yalnız sonraki kayıtları v2 yapar. Bilinmeyen snapshot/aday sürümü reddedilir.

## 3. Manifest, bütünlük ve sınırlar

Manifest: format/Room sürümleri, tarih, yollar, temel tablolar/katalog/ölçüm satır sayıları,
beklenen CSV kümesi ve her payload için göreli yol, byte uzunluğu, SHA-256. Envanter sürüme
göre tam eşleşir. Görsellerin JPEG türü, boyutu, SHA-256'sı ve çözünürlüğü ayrıca doğrulanır.

Üst sınırlar: ZIP **512 MiB**; açılmış toplam **1 GiB**; tek girdi **128 MiB**; JSON **64 MiB**;
manifest **1 MiB**; manifest dahil **10.009 girdi**; **10.000 görsel**; tablo başına **200.000 satır**;
görsel kenarı **3.000 px**; sıkıştırma oranı **1.000:1**. Karar JSON'u en çok **16 KiB**;
ölçüm fazı başına **24 saat**. İçe/dışa aktarma ve görsel kopyalamada boş alan kontrol edilir.

Yol kaçışı, tekrarlı/yabancı girdiler, kesik dosya, yanlış sayı/hash ve desteklenmeyen format/şema
normal arşive dokunmadan reddedilir. SHA-256 bozulmayı saptar; **kaynağın kimliğini doğrulamaz**.
Geçerli hash'lerle yeniden yazılmış ama anlamı bozuk ölçümlerin reddi de test edilir.

## 4. Dışa aktarma ve SAF/Drive

Kullanıcı Android belge seçicisinde hedefi seçer. ZIP önce cache'te hazırlanır; içerik, boyut/hash,
`fsync` ve atomik taşıma tamamlandıktan sonra seçilen hedefe kopyalanır. Başarısızlık başarı gibi
gösterilmez. Son yedek özeti dosya adı, tarih, boyut ve güvenlik kopyası ayrımını gösterir.

Android belge sağlayıcısına yazmak bulut yüklemesinin bittiğini kanıtlamaz. Drive/bulut durumunu
sağlayıcı arayüzünde ayrıca doğrulamak gerekir. Uygulama kaldırılınca cihaz-özel güvenlik kopyası
da silinebilir; önemli yedeği kullanıcının seçtiği kalıcı konuma ayrıca kaydetmek gerekir.

## 5. Tam geri yükleme ve kurtarma

**Tam değiştirme; birleştirme veya senkronizasyon değil.**

1. Seçilen ZIP sınırlandırılmış biçimde özel hazırlık dizinine kopyalanır; tamamı doğrulanır.
2. Yedek/mevcut soru/görsel ve yeni ölçüm sayısı gösterilir; açık onay olmadan arşiv değiştirilmez.
3. Mevcut arşivin ölçümleri de içeren doğrulanmış `safety-<uuid>.zip` kopyası alınır.
4. Journal yazılır; yeni görseller ayrı adlarla kalıcılaştırılır. Tek Room transaction'ında eski
   arşiv değiştirilir, tekrarlar ve ölçümler eklenir, commit işareti yazılır. Review silinmesi
   eski ölçümleri cascade ile siler. Bir adım başarısızsa DB transaction'ı geri alınır.
5. Commit kanıtı ve yeni görseller doğrulanır. Eski devam noktası **kalıcı temizlenir**; üretim callback'i
   rozet/rütbe bekleyen kutlamalarını da sıfırlar, sahipliği korur ve sessiz yeni taban ister. Ardından
   eski görseller, Room işareti ve journal kaldırılır. Temizlik başarısızsa recovery sınırı korunur;
   servis anlık recovery dener, hâlâ başarısızsa startup'da tekrar eder.

Startup recovery normal ekranlardan önce çalışır: commit işareti yoksa yeni dosyalar geri
alınır ve eski arşiv/oturum kalır; işaret varsa yeni arşivin temizliği tamamlanır. İşaret/journal
uyuşmazlığında veya doğrulama hatasında normal kullanım kilitlenir; kullanıcı yeniden deneyebilir.
Geç kalan checkpoint IO işi, silinen oturumu diriltemez. Profiller kalıcı cache olmadığından
restore sonrasında yeni veri kümesinden yeniden hesaplanır.

## 5b. Öğretmen çalışma alanı (v5)

Öğretmen verisi Room v5'te tutulur ve ZIP'e `data.json` içindeki `teacher_workspace` nesnesiyle girer:

```json
"teacher_workspace": {
  "schema_version": 2,
  "groups": [{ "id", "name", "description", "lesson_id", "archived", "order", "created_at" }],
  "questions": [{
    "id", "group_id", "title", "lesson_id", "topic_id", "source", "asked_by",
    "due_day", "difficulty", "answer", "status", "solved", "solved_at",
    "created_at", "updated_at",
    "images": [{ "id", "order", "kind", "file_name", "zip_file_name", "mime_type",
                 "width", "height", "size_bytes", "sha256" }]
  }]
}
```

- `status` üç değerlidir (`BEKLIYOR`/`COZULDU`/`ARSIV`); `solved` alanı eski okuyucular için
  senkron tutulur (`status != BEKLIYOR`).
- `teacher_workspace_version` manifest/veri anahtarı sürümü doğrular; ileri sürüm reddedilir.
- Öğretmen fotoğrafları öğrenci fotoğraflarıyla **aynı `images/` klasöründe** durur; her dosya kendi
  UUID adıyla yazıldığı için çakışma yoktur. Aynı payload iki kez eklenmez.
- Doğrulama: her soru var olan bir gruba işaret eder, en az bir `SORU` görseli taşır, görsel türü
  sayıları toplamla uyuşur, `file_name` yolu `img/` veya `ogretmen-img/` ile başlar.
- v1/v2 JSON çalışma alanı dosyaları da okunur (toleranslı: bozuk kayıt atlanır ve sayılır).

## 6. Dahil olmayanlar ve gizlilik

Rozet/rütbe sahiplik/görülme/kutlama durumu, motor politikası, günlük hedef, seçili sınav formatı ve **yarım oturumun cihaz-yerel
checkpoint'i** ZIP'e dahil değildir. Sınav profiliyle eklenen dersler normal ders kayıtlarıdır ve
yedekte bulunur. Tamamlanmış ölçüm ve bağlam ise v4'te yedeklidir.

ZIP şifreli değildir; fotoğraf/not/katalog yanında çalışma süreleri ve bağlam da içerebilir.
Güvenli saklama kullanıcıya aittir. Daha yeni ZIP'i eski APK'ya verme veya DB downgrade desteklenmez.

## 7. Doğrulama

Otomatik kapsam: v2/v3 import; v4 tam tur; v5 öğretmen çalışma alanı ve Room 4→5 göçü; ölçüm null/kalite, snapshot1/2 ve v3 geçmişinin korunması; hash/envanter;
semantik bozulma/taşma/orphan/tekrar reddi; safety kopyası; commit/rollback ve checkpoint sınırı;
Room 1/2/3→4→5. Güncel sonuç [DERLEME_RAPORU.md](DERLEME_RAPORU.md) içindedir.

Bunlar fiziksel telefon, SAF/bulut sağlayıcısı, gerçek düşük depolama veya süreç öldürme kabulü
değildir. [TEST_KONTROL_LISTESI.md](TEST_KONTROL_LISTESI.md) içindeki cihaz kapıları açık tutulur.
