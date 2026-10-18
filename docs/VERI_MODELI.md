# Veri Modeli (Room şema v4)

> Room v4: önceki soru/katalog/recovery şemasına 0..1 tekrar ölçümü ve tarihsel bağlam eklenir.
> `MIGRATION_3_4` yalnız yeni tablo/indeks ekler; eski tekrarlar, plan tarihleri ve notlar değişmez.
> 1→2→3→4 zinciri korunur. Güncel CI kanıtı `DERLEME_RAPORU.md` içinde; fiziksel cihaz kabulü ayrıdır.
> Son güncelleme: 2026-10-18.

Veritabanı: **Room / SQLite**. Yerel veri tek doğruluk kaynağıdır.

---

## 1. Genel kurallar

| Kural | Gerekçe |
|---|---|
| Kimlikler **UUID metin** (otomatik artan sayı değil) | İleride içe aktarma veya "birleştir" yapılırsa kimlik çakışması olmaz |
| Veritabanında **dosya yolu tutulmaz**, yalnızca dosya adı tutulur | Görseller her zaman uygulamanın kalıcı klasörüne göre çözülür; yeni telefonda geri yükleme bozulmaz |
| Kayıt anları: **epoch milisaniye** (UTC) | Anlık zaman damgası |
| Planlanan/gerçekleşen tekrar tarihleri: **yerel takvim günü** (epoch'tan bu yana gün sayısı, kayıt anında cihazın yerel tarihinden hesaplanır) | Saat dilimi değişse bile geçmişteki tarih kendiliğinden değişmez |
| Kullanılan ders/konu silinmez; arşivlenebilir. Kullanılmayan konu Ayarlar’dan silinebilir | Mevcut soruların ilişkileri bozulmaz |
| Yabancı anahtarlar **açıkça tanımlanır**; silme davranışı `CASCADE` ile soru silmeyi tetiklemez | Beklenmeyen veri kaybını engeller |
| **Türetilmiş alanlar ham geçmişin yerini almaz** | Ham `tekrarlar` kayıtları her zaman doğruluk kaynağıdır |
| Tablo/kolon adları ASCII Türkçe | Kod okunabilirliği ve belge uyumu |

---

## 2. Tablolar

### `dersler`
| Kolon | Tip | Not |
|---|---|---|
| `id` | TEXT PK | UUID |
| `ad` | TEXT | Benzersiz olması önerilir (uygulama tarafında kontrol) |
| `sira` | INTEGER | Kart/liste sırası |
| `arsivli` | INTEGER | 0/1 — arşivli ders yeni kayıtta varsayılan seçenek olarak sunulmaz; soruları görünür kalır |
| `olusturma` / `guncelleme` | INTEGER | epoch ms |

Başlangıç verisi: Edebiyat, Matematik, Biyoloji, Kimya, Fizik, Coğrafya, Tarih.

### `konular`
| Kolon | Tip | Not |
|---|---|---|
| `id` | TEXT PK | UUID |
| `ders_id` | TEXT FK → `dersler.id` | Zorunlu |
| `ad` | TEXT | |
| `sira` | INTEGER | |
| `arsivli` | INTEGER | 0/1 — arşivli konu yeni soruda seçenek olarak çıkmaz, mevcut ilişkisi korunur |
| `olusturma` / `guncelleme` | INTEGER | epoch ms |

### `sorular`
| Kolon | Tip | Not |
|---|---|---|
| `id` | TEXT PK | UUID |
| `ders_id` | TEXT FK → `dersler.id` | **Zorunlu** |
| `konu_id` | TEXT FK → `konular.id`, NULL olabilir | NULL = "Konusuz" — geçerli bir durumdur, eksik veri sayılmaz |
| `soru_notu` | TEXT, NULL | Eski kayıtlarla uyumluluk ve yedek için korunur; yeni soru arayüzünde alan gösterilmez. Düzenleme eski değeri silmez |
| `cozum_notu` | TEXT, NULL | Çözüm açıklaması |
| `kaynak` | TEXT, NULL | Katalogdan seçilen kaynak adı (eski serbest metin değerleri korunur) |
| `kaynak_konumu` | TEXT, NULL | Bu soruya özel sayfa / test / soru numarası |
| `yanlis_nedeni` | TEXT, NULL | Tek ana neden; hazır veya kullanıcı eklemesi |
| `favori` | INTEGER | 0/1 |
| `plan_durumu` | TEXT | **Kullanıcı kontrolünde**: `AKTIF` (tekrar planında), `ASKIDA` (geçici olarak çıkarıldı), `TAMAMLANDI` (artık tekrar planlanmıyor) |
| `ogrenme_durumu` | TEXT | **Türetilmiş önbellek**: `YENI`, `OGRENIYOR`, `PEKISIYOR`, `SAGLAM` |
| `son_tekrar_gun` | INTEGER, NULL | Son tekrarın gerçekleştiği yerel gün (özet alan) |
| `sonraki_tekrar_gun` | INTEGER, NULL | Planlanan yerel gün; NULL = plan yok (özet alan) |
| `olusturma` / `guncelleme` | INTEGER | epoch ms |

> **İki farklı kavram karıştırılmaz:**
> - `plan_durumu` **kullanıcının kararıdır** ("bu soru tekrar planımda mı?"). Öğrenme düzeyi değildir.
>   Bir soru pekişmiş olsa bile `AKTIF` kalabilir; kullanıcı isterse geçici olarak `ASKIDA` yapar veya
>   `TAMAMLANDI` işaretler. Bu işlemler **kaydı veya geçmişi silmez**, tek dokunuşla geri alınabilir.
> - `ogrenme_durumu` **türetilmiştir**; gerçek kaynak `tekrarlar` tablosudur ve her zaman yeniden
>   hesaplanabilir. `son_tekrar_gun` / `sonraki_tekrar_gun` de yalnızca hız için tutulan özet
>   alanlardır; ham geçmişin yerini almazlar.

### `soru_secenekleri` (derslerden bağımsız global katalog)
| Kolon | Tip | Not |
|---|---|---|
| `id` | TEXT PK | UUID ya da sabit hazır neden kimliği |
| `tur` | TEXT | `KAYNAK` veya `YANLIS_NEDENI` |
| `ad` | TEXT | Kullanıcıya gösterilen ad |
| `normalize_ad` | TEXT | Türkçe küçük harf/boşluk normalizasyonu; `tur` ile birlikte benzersiz |
| `varsayilan` | INTEGER | 0/1; hazır nedenlerde 1 |
| `sira` | INTEGER | Hazır yanlış nedenlerinin sabit sırası; diğer kayıtlar ada göre sıralanır |
| `olusturma` | INTEGER | epoch ms |

Kaynak kataloğu tüm derslerde ortaktır. Soruda seçilen ad `sorular.kaynak` içinde, o soruya özel
konum ise `kaynak_konumu` içinde saklanır. Yanlış nedeni tek seçimdir; başlangıç listesi Yanlış
okuma, Soruyu yanlış anlama, Bilgi eksiği, Kural/formül karışıklığı, İşlem/hesap hatası,
Dikkat/acele, Zaman baskısı ve Hatırlayamama seçeneklerini içerir. Kullanıcı yeni kaynak ve neden
ekleyebilir. Katalog satırları soru silinse bile bağımsız kalır ve tam ZIP yedeğine girer.

### `gorseller`
| Kolon | Tip | Not |
|---|---|---|
| `id` | TEXT PK | UUID |
| `soru_id` | TEXT FK → `sorular.id` | Zorunlu |
| `tur` | TEXT | `SORU` veya `COZUM` |
| `sira` | INTEGER | Çok sayfalı görsellerde sıra |
| `dosya_adi` | TEXT | Kalıcı klasöre **göreli** ad, ör. `img/3f2a….jpg` |
| `mime_type` | TEXT | Kaydetmeden önce JPEG'e dönüştürüldüğü için `image/jpeg` |
| `genislik` / `yukseklik` | INTEGER | Önizleme ve yerleşim için |
| `boyut` | INTEGER | Bayt — depolama kullanımı ve yedek doğrulaması için |
| `sha256` | TEXT | Dosya içeriği özeti — bozulma/yanlış eşleşme tespiti için |
| `olusturma` | INTEGER | epoch ms |

Bir soru için en az bir `tur = SORU` kaydı bulunmalıdır. Çözüm görselleri isteğe bağlıdır ve
sonradan eklenebilir.

**Önizlemeler (küçük görseller) veritabanında ve kalıcı klasörde tutulmaz.** Küçültülmüş görüntüler
her gösterimde asıl JPEG dosyasından `BitmapFactory` ile `inSampleSize` kullanılarak üretilir ve
composable ekrandan çıktığında `recycle()` ile bırakılır; ayrı bir önizleme dosyası/önbelleği yoktur.
Böylece yedek boyutu şişmez ve türetilmiş veri için ayrıca silme/geri getirme mantığı yazılmaz.
Liste kaydırması ölçümde yetersiz kalırsa bir görsel önbelleği eklenmesi yeniden değerlendirilir
(ROADMAP Aşama 5).

### `tekrarlar` (tekrar geçmişi — ham veri, üzerine yazılmaz)
| Kolon | Tip | Not |
|---|---|---|
| `id` | TEXT PK | UUID |
| `soru_id` | TEXT FK → `sorular.id` | Zorunlu |
| `zaman` | INTEGER | epoch ms — tekrarın yapıldığı an |
| `gun` | INTEGER | **Tekrarın yapıldığı yerel takvim günü** (kayıt anında hesaplanır, sonradan değişmez) |
| `planlanan_gun` | INTEGER, NULL | Bu tekrarın **planlandığı** gün; gerçekleşen günden ayrıdır → gecikme ölçülebilir |
| `sonuc` | TEXT | `COZEMEDIM`, `ZORLANDIM`, `RAHAT` |
| `cozum_acildi` | INTEGER | 0/1 — kullanıcı "Çözümü göster"e bastı mı (v1'de "destekli deneme" işaretidir) |
| `onceki_aralik` | INTEGER, NULL | **Gün cinsinden** uygulanan planın uzunluğu |
| `sonraki_aralik` | INTEGER, NULL | **Gün cinsinden** yeni aralık |
| `algoritma_surumu` | INTEGER | Aralığı hesaplayan kural setinin sürümü (ör. 1) |
| `elle_atandi` | INTEGER | 0/1 — sonraki tarih elle mi belirlendi |

> `algoritma_surumu` sayesinde aralık kuralı ileride değiştirilse bile eski kayıtlar doğru
> yorumlanır; geçmişi yeniden yazmak veya silmek gerekmez.

### `tekrar_olcumleri` (v4)

| Alan | Tip | Sözleşme |
|---|---|---|
| `tekrar_id` | TEXT PK/FK | `tekrarlar.id`; silmede CASCADE; tekrar başına 0..1 |
| `deneme_id` | TEXT UNIQUE | Aynı denemeyi ikinci kez kaydetmeme kimliği |
| `oturum_id` | TEXT nullable | Duvar saati değişse de commit–checkpoint uzlaştırma |
| `deneme_aktif_ms`, `inceleme_aktif_ms` | INTEGER nullable | Gerçek aktif segmentler; yoksa null, sıfırla backfill yok |
| `kalite`, `nedenler_json`, `olcum_surumu` | TEXT/TEXT/INTEGER | YOK/TAM/KISMI/GECERSIZ; nedenler; sürüm 1 |
| `ders_id`, `ders_adi`, `konu_id`, `konu_adi` | TEXT; konu nullable | Kayıt anı kimlik/etiket; güncel katalog FK'sı değil |
| `kaynak_anahtari`, `kaynak_etiketi` | TEXT nullable | NFC/Türkçe normalizasyon; tarihsel kaynak |
| `karar_json` | TEXT | ≤16 KiB; snapshot1/V2 veya snapshot2/Gölge–Etkin; temel/aday/uygulanan plan, gün/saat dilimi, gerekçe ve profil dayanağı |

Tekrar, ölçüm/karar ve uygulanacak yeni plan aynı Room transaction'ındadır; varsayılan Gölge v2 uygular. Eski satırların ek kaydı yoktur;
ölçümsüz yeni kayıt `YOK` ve nullable süreyle bağlam alabilir. İndeksler: benzersiz deneme ID'si,
`(ders_id, konu_id)`, `(ders_id, kaynak_anahtari)` ve tekrarlar üzerinde `gun`.
Profiller bu verilerden türetilir; kalıcı ikinci bir profil/XP tablosu yoktur.
Ayrıntı: [OLCUM_VE_PROFILLER.md](OLCUM_VE_PROFILLER.md).

### `geri_yukleme_isaretleri` (v2)

| Kolon | Tip | Not |
|---|---|---|
| `id` | TEXT PK | Tek geri yükleme transaction'ının UUID işareti; dışa aktarılan kullanıcı verisi değildir |

Bu tablo, app-private işlem günlüğüyle birlikte kesinti sonrası commit/rollback kararını verir.

---

## 3. İndeksler

- `sorular(ders_id)`, `sorular(konu_id)`, `sorular(sonraki_tekrar_gun)`, `sorular(plan_durumu)`,
  `sorular(guncelleme)`
- `konular(ders_id)`, `gorseller(soru_id)`, `tekrarlar(soru_id, gun)`
- `soru_secenekleri(tur, normalize_ad)` **UNIQUE**; ayrıca liste sıralaması için `(tur, varsayilan, sira)`

"Bugün" sorgusu: `plan_durumu = 'AKTIF' AND sonraki_tekrar_gun <= <bugün>` — gecikmişler aynı
sorgudan gelir, ayrıca işaretlenir.

---

## 4. Silme ve arşivleme davranışı

| İşlem | Davranış |
|---|---|
| **Konu arşivle** | Konu yeni kayıtlarda seçenek olarak çıkmaz; mevcut soruların `konu_id` değeri korunur; istenirse yeniden etkinleştirilir |
| **Ders arşivle** | Aynı şekilde; sorular görünür kalır ("arşivli ders" altında), yeniden etkinleştirilebilir |
| **Ders sil** | Doğrudan silme yok. Soruları varsa "N soru etkilenecek" uyarısı verilir ve önce taşıma önerilir |
| **Soru sil** | Onay diyaloğu ile (kaç görsel silineceği yazılı). Önce veritabanı kaydı (tek transaction), sonra görsel dosyaları silinir; dosya silinemezse öksüz kalır ve temizlik işine düşer |
| **Soru askıya al / tamamla** | Veri silinmez; yalnızca `plan_durumu` değişir ve soru "Bugün" listesinden çıkar |
| **Geri al** | Öneri: silme işleminden sonra kısa süre "Geri al" imkânı (Aşama 2'de değerlendirilecek) |

Hiçbir işlem kullanıcıya sürpriz veri kaybı yaşatmaz; toplu ve sessiz silme yapılmaz.

### Yabancı anahtarlar

- Room'da ilişkiler **açıkça** tanımlanır ve yabancı anahtar kısıtları etkin tutulur.
- `gorseller(soru_id)` ve `tekrarlar(soru_id)` için silme davranışı **`CASCADE` seçilirse**, silme
  zinciri yalnızca kullanıcı soruyu sildiğinde ve onayla çalışır — başka hiçbir işlem soru silmez.
- Ders/konu için `CASCADE` **kullanılmaz** (arşivleme tercih edilir; silme yalnızca soruları
  taşıdıktan sonra).

### Tutarlılık denetimi (dosya ↔ veritabanı)

Veritabanı işlemi ile dosya sistemi işlemi tek atomik işlem değildir. Bu yüzden:

- **Eksik dosya:** `gorseller` kaydı var ama dosya yok → kayıt kırılmaz; arayüz "görsel bulunamadı"
  durumu gösterir ve kullanıcıya bildirilir (sessizce silinmez).
- **Kullanılmayan dosya:** Dosya var ama hiçbir kayıt ona bağlı değil → **hemen silinmez.** Böyle bir
  dosya, devam eden bir içe aktarma işlemine ait olabilir. Silme işi yalnızca: (a) belirli bir
  yaştan eski dosyalar için, (b) kullanıcının onayıyla veya raporla, (c) tek tek değil toplu olarak
  uygulanır ve kaç dosyanın silindiği kullanıcıya gösterilir.
- Bu denetim uygulama açılışında **yıkıcı biçimde** çalışmaz; Ayarlar içinden "bakım" işi olarak
  sunulur.

---

## 5. Öğrenme durumu ve tekrar hesabı

- Başlangıç aralıkları: **1 · 3 · 7 · 21 · 45 · 90 gün**, üst sınır ~180 gün.
- `COZEMEDIM` → aralık 1 güne döner.
- `ZORLANDIM` → aralık daha yavaş büyür (mevcut aralıkta kalır veya bir kademe ilerler).
- `RAHAT` → aralık bir kademe büyür.
- **`cozum_acildi = 1` olan tekrar bağımsız deneme sayılmaz:** sonuç ne olursa olsun sonraki tekrar
  **1 gün** sonradır. Amaç unutulanı hatırlamak olduğu için soru kısa süre sonra yeniden karşımıza
  çıkar ve bu kez bağımsız olarak çözülür. **Aynı gün ikinci deneme kaydedilmez;** çözüm görüldükten
  sonra aynı oturumda yapılan çözüm gerçek hatırlama sayılmaz.
- Aralık kuralı **tek bir sınıfta** ve **sürümlü** olarak yazılır; kayıtlar `algoritma_surumu` ile
  etiketlenir. Sabit aralıklardan SM-2 benzeri bir modele geçmek istenirse geçmiş yeniden yazılmaz.
- Kullanıcı sonucu seçtikten sonra sonraki tarihi elle değiştirebilir (`elle_atandi = 1`).
- `plan_durumu` etkisi: `AKTIF` → "Bugün" listesine girer; `ASKIDA` / `TAMAMLANDI` → girmez, kayıt ve
  geçmiş korunur.
- **Uzun bir aradan sonra hiçbir soru otomatik olarak yeniden başlatılmaz.** Gecikmiş sorular tek
  listede toplanır; kullanıcı bunları tek tek veya toplu olarak ele alır.
- `ogrenme_durumu` türetme önerisi: hiç tekrar yok → `YENI`; son sonuç `COZEMEDIM` veya aralık ≤ 3 →
  `OGRENIYOR`; aralık ≤ 21 → `PEKISIYOR`; aksi hâlde `SAGLAM`. (Eşikler Aşama 4'te ayarlanabilir.)

---

## 6. Şema sürümleme ve migration testleri

- Room `version` alanı her şema değişikliğinde artırılır ve **veri koruyan `Migration`** yazılır.
- `fallbackToDestructiveMigration` **kullanılmaz**. Hatalı geçişte veritabanını silmek normal
  güncelleme çözümü değildir.
- Şema JSON'ları sürüm kontrolünde saklanır (Room `exportSchema`).
- **Her migration için otomatik test yazılır**; şema ve veri koruması test edilir. `MIGRATION_1_2` yalnızca `geri_yukleme_isaretleri` tablosunu ekler. `MIGRATION_2_3`, `kaynak_konumu` kolonunu ve global katalog tablosunu ekler; mevcut soru notu/kaynak/neden değerleri korunur, kaynak ve özel nedenler yeniden kullanılabilir kataloğa alınır.
- Room v1/v2/v3/v4 şemaları `app/schemas/com.keremtulu.questionnote.data.local.YanlisDefteriDatabase/` altında korunur. `MIGRATION_3_4` ölçüm tablosu ve indeksleri ekler; 1/2/3→4 yolları ve null tarihsel ölçümler test edilir. v3 tarihsel CI kanıtı `39037429421`, v4 ana paket kanıtı `39135928734` (217/0).
- Testler JVM/Robolectric üzerinde çalışır; CI sonucu fiziksel cihaz güncelleme kabulü değildir.


## Tarihsel v1.2.0: şema değiştirmeden adaptif tekrar ve sınav formatları

`AkilliTekrarKurali` hafıza durumunu `tekrarlar` tablosundaki mevcut aralık/sonuç/algoritma sürümü
alanlarından türetir. Yeni kayıtlar sürüm 2, eski kayıtlar özgün sürümde kalır. Domain `TekrarKaydi`
artık mevcut `zaman` ve `algoritma_surumu` değerlerini de taşır; bunlar yeni DB kolonları değildir.

Sınav formatları eksik dersleri normal `dersler` satırları olarak ekler, ad eşleşen ID'yi tekrar
kullanır. Profil tercihi, rozet sahipliği/kutlama kuyruğu ve son oturum devam noktası prefs'tedir.
Bağımsız çözüm sorguları `sonuc = 'RAHAT' AND cozum_acildi = 0` kullanır.

## v1.4.x / P2–P3

Süre geçmişten türetilemediği için bu pakette Room/ZIP v4 uygulandı. Yukarıdaki v1.2.0 kararı
yalnız o sürümün v2 algoritma durumunu anlatır. Tekrar algoritması 2 olarak kalır; daha hızlı
çözene bonus veya yavaşlığa ceza eklenmez. Yeni ölçüm/bağlam ZIP v4 ile taşınır.


## v1.5.x / P4–P6

Room4/ZIP4 kolonları aynı; yeni şema veya profil cache tablosu eklenmedi. `karar_json` okuyucusu
snapshot1/2'yi açıkça seçer. Geçmiş algoritma 1/2/3 yönlendirmesi, v3 ease ve gelecekteki v2'ye
geri dönüş desteklenir; eski satırlar yeniden yazılmaz. Etkin v3 cihaz/gölge kabul kapıları kapalıdır.
Analitik ince projeksiyonla 4 toplu SELECT; ayar tanısı yalnız en yeni 100 kararın içeriğini doğrular.
[P4–P6 sözleşmesi ve kanıtı](P4_P5_P6_UYGULAMA.md).
