# Veri Modeli (v1)

> Şema v1 Room'da uygulanıp CI'da doğrulandı; bu belge veri sözleşmesidir. Uygulama akışları, kalıcı görsel yaşam döngüsü ve migration'lar henüz tamamlanmadı.
> Son güncelleme: 2026-10-16 (Room v1 şema CI run `38944293610` ile doğrulandı)

Veritabanı: **Room / SQLite**. Yerel veri tek doğruluk kaynağıdır.

---

## 1. Genel kurallar

| Kural | Gerekçe |
|---|---|
| Kimlikler **UUID metin** (otomatik artan sayı değil) | İleride içe aktarma veya "birleştir" yapılırsa kimlik çakışması olmaz |
| Veritabanında **dosya yolu tutulmaz**, yalnızca dosya adı tutulur | Görseller her zaman uygulamanın kalıcı klasörüne göre çözülür; yeni telefonda geri yükleme bozulmaz |
| Kayıt anları: **epoch milisaniye** (UTC) | Anlık zaman damgası |
| Planlanan/gerçekleşen tekrar tarihleri: **yerel takvim günü** (epoch'tan bu yana gün sayısı, kayıt anında cihazın yerel tarihinden hesaplanır) | Saat dilimi değişse bile geçmişteki tarih kendiliğinden değişmez |
| Ders/konu silme = **arşivleme** | Mevcut soruların ilişkileri bozulmaz |
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
| `soru_notu` | TEXT, NULL | |
| `cozum_notu` | TEXT, NULL | Çözüm açıklaması |
| `kaynak` | TEXT, NULL | Kitap / sayfa / soru no |
| `yanlis_nedeni` | TEXT, NULL | |
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

### `gorseller`
| Kolon | Tip | Not |
|---|---|---|
| `id` | TEXT PK | UUID |
| `soru_id` | TEXT FK → `sorular.id` | Zorunlu |
| `tur` | TEXT | `SORU` veya `COZUM` |
| `sira` | INTEGER | Çok sayfalı görsellerde sıra |
| `dosya_adi` | TEXT | Kalıcı klasöre **göreli** ad, ör. `img/3f2a….jpg` |
| `mime_type` | TEXT | `image/jpeg`, `image/png`, `image/webp` — dönüştürme ve geri yükleme doğrulaması için |
| `genislik` / `yukseklik` | INTEGER | Önizleme ve yerleşim için |
| `boyut` | INTEGER | Bayt — depolama kullanımı ve yedek doğrulaması için |
| `sha256` | TEXT | Dosya içeriği özeti — bozulma/yanlış eşleşme tespiti için |
| `olusturma` | INTEGER | epoch ms |

Bir soru için en az bir `tur = SORU` kaydı bulunmalıdır. Çözüm görselleri isteğe bağlıdır ve
sonradan eklenebilir.

**Önizlemeler (küçük görseller) veritabanında ve kalıcı klasörde tutulmaz.** Liste ekranlarındaki
küçültülmüş görüntüler Coil'in bellek/disk önbelleğiyle asıl dosyadan üretilir; kaybolursa yeniden
üretilebilir. Böylece yedek boyutu şişmez ve türetilmiş veri için ayrıca silme/geri getirme mantığı
yazılmaz. Liste kaydırması ölçümde yetersiz kalırsa bu karar yeniden değerlendirilir (ROADMAP Aşama 5).

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

---

## 3. İndeksler

- `sorular(ders_id)`, `sorular(konu_id)`, `sorular(sonraki_tekrar_gun)`, `sorular(plan_durumu)`,
  `sorular(guncelleme)`
- `konular(ders_id)`, `gorseller(soru_id)`, `tekrarlar(soru_id, gun)`

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
- **Her migration için otomatik test yazılır** (Room `MigrationTestHelper`); yalnızca uygulamanın
  açılması değil, soru sayısı, görsel ilişkileri ve tekrar geçmişi de doğrulanır. Zincir testi
  (`v1 → vN`) Aşama 2'den itibaren korunur.
- Bu testler JVM üzerinde (Robolectric) çalıştırılır; hızlı geri bildirim için emülatör beklenmez.
- Kullanıcı güncellemeden önce yedek alması konusunda uygulama içinde hatırlatılabilir (Aşama 5).
