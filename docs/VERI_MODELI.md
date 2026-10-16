# Veri Modeli (v1 taslağı)

> Aşama 2'de uygulanacak şemanın taslağıdır. Kod yazılmadan önce gözden geçirilebilir.
> Son güncelleme: 2026-10-16

Veritabanı: **Room / SQLite**. Yerel veri tek doğruluk kaynağıdır.

---

## 1. Genel kurallar

| Kural | Gerekçe |
|---|---|
| Kimlikler **UUID metin** (otomatik artan sayı değil) | İleride içe aktarma veya "birleştir" yapılırsa kimlik çakışması olmaz |
| Veritabanında **dosya yolu tutulmaz**, yalnızca dosya adı tutulur | Görseller her zaman uygulamanın kalıcı klasörüne göre çözülür; temiz kurulumda geri yükleme bozulmaz |
| Kayıt/güncelleme anları: **epoch milisaniye** (UTC) | Anlık zaman damgası |
| Tekrar günleri: **epoch gün** (yerel tarihe göre hesaplanan gün numarası) | Saat dilimi ve saat kaymaları tekrar hesabını bozmaz |
| Ders/konu silme = **arşivleme** (`arsivli`) | Mevcut soruların ilişkileri bozulmaz |
| Yabancı anahtarlar `ON DELETE` ile **soru silmeyi tetiklemez** | Beklenmeyen veri kaybını engeller |
| Tablo/kolon adları ASCII Türkçe | Kod okunabilirliği ve belge uyumu |

---

## 2. Tablolar

### `dersler`
| Kolon | Tip | Not |
|---|---|---|
| `id` | TEXT PK | UUID |
| `ad` | TEXT | Benzersiz olması önerilir (uygulama tarafında kontrol) |
| `sira` | INTEGER | Kart/liste sırası |
| `arsivli` | INTEGER | 0/1 — arşivlenen ders listelerde gizlenir, soruları durur |
| `olusturma` / `guncelleme` | INTEGER | epoch ms |

Başlangıç verisi: Edebiyat, Matematik, Biyoloji, Kimya, Fizik, Coğrafya, Tarih.

### `konular`
| Kolon | Tip | Not |
|---|---|---|
| `id` | TEXT PK | UUID |
| `ders_id` | TEXT FK → `dersler.id` | Zorunlu |
| `ad` | TEXT | |
| `sira` | INTEGER | |
| `arsivli` | INTEGER | 0/1 |
| `olusturma` / `guncelleme` | INTEGER | epoch ms |

### `sorular`
| Kolon | Tip | Not |
|---|---|---|
| `id` | TEXT PK | UUID |
| `ders_id` | TEXT FK → `dersler.id` | **Zorunlu** |
| `konu_id` | TEXT FK → `konular.id`, NULL olabilir | NULL = "Konusuz" |
| `soru_notu` | TEXT, NULL | |
| `cozum_notu` | TEXT, NULL | Çözüm açıklaması |
| `kaynak` | TEXT, NULL | Kitap / sayfa / soru no |
| `yanlis_nedeni` | TEXT, NULL | |
| `favori` | INTEGER | 0/1 |
| `durum` | TEXT | Türetilmiş önbellek: `YENI`, `OGRENIYOR`, `PEKISIYOR`, `SAGLAM` |
| `son_tekrar_gun` | INTEGER, NULL | Son tekrarın gün numarası |
| `sonraki_tekrar_gun` | INTEGER, NULL | NULL = tekrar planı yok |
| `olusturma` / `guncelleme` | INTEGER | epoch ms |

> **`durum` alanı türetilmiştir.** Gerçek kaynak `tekrarlar` tablosudur; `durum` yalnızca hızlı
> listeleme için tutulur ve `tekrarlar` kayıtlarından her zaman yeniden hesaplanabilir.

### `gorseller`
| Kolon | Tip | Not |
|---|---|---|
| `id` | TEXT PK | UUID |
| `soru_id` | TEXT FK → `sorular.id` | Zorunlu |
| `tur` | TEXT | `SORU` veya `COZUM` |
| `sira` | INTEGER | Çok sayfalı görsellerde sıra |
| `dosya_adi` | TEXT | Kalıcı klasöre **göreli** ad, ör. `img/3f2a....jpg` |
| `genislik` / `yukseklik` | INTEGER | Önizleme ve yerleşim için |
| `boyut` | INTEGER | Bayt — yedek doğrulaması ve istatistik için |
| `olusturma` | INTEGER | epoch ms |

Bir soru için en az bir `tur = SORU` kaydı bulunmalıdır. Çözüm görselleri isteğe bağlıdır ve
sonradan eklenebilir.

### `tekrarlar` (tekrar geçmişi — ham veri, üzerine yazılmaz)
| Kolon | Tip | Not |
|---|---|---|
| `id` | TEXT PK | UUID |
| `soru_id` | TEXT FK → `sorular.id` | Zorunlu |
| `zaman` | INTEGER | epoch ms — tekrarın yapıldığı an |
| `gun` | INTEGER | epoch gün (listeleme ve raporlama) |
| `sonuc` | TEXT | `COZEMEDIM`, `ZORLANDIM`, `RAHAT` |
| `cozum_acildi` | INTEGER | 0/1 — kullanıcı "Çözümü göster"e bastı mı |
| `onceki_aralik` | INTEGER, NULL | Gün cinsinden önceki aralık |
| `sonraki_aralik` | INTEGER, NULL | Gün cinsinden yeni aralık |
| `elle_atandi` | INTEGER | 0/1 — sonraki tarih elle mi belirlendi |

> "Çözümü açtım" bilgisi ayrı tutulduğu için **"çözümü görünce anladım"** ile **"yardımsız çözdüm"**
> aynı kabul edilmez.

---

## 3. İndeksler

- `sorular(ders_id)`, `sorular(konu_id)`, `sorular(sonraki_tekrar_gun)`, `sorular(durum)`,
  `sorular(guncelleme)`
- `konular(ders_id)`, `gorseller(soru_id)`, `tekrarlar(soru_id, gun)`

## 4. Silme ve arşivleme davranışı

| İşlem | Davranış |
|---|---|
| **Konu arşivle** | Konu yeni kayıtlarda seçilemez; mevcut soruların `konu_id` değeri korunur |
| **Ders arşivle** | Aynı şekilde; sorular görünür kalır, "arşivli ders" altında listelenir |
| **Ders sil** | Doğrudan silme yok. Soruları varsa uygulama "N soru etkilenecek" uyarısı verir ve önce taşımayı önerir |
| **Soru sil** | Onay diyaloğu ile (kaç görsel silineceği yazılı). Onay sonrası: **önce veritabanı kaydı** (tek transaction), sonra görsel dosyaları silinir. Dosya silinemezse öksüz kalır ve arka planda temizlenir |
| **Geri al** | Öneri: silme işleminden sonra kısa süre "Geri al" imkânı sunulması (Aşama 2'de değerlendirilecek) |

Hiçbir işlem kullanıcıya sürpriz veri kaybı yaşatmaz; toplu ve sessiz silme yapılmaz.

## 5. Öğrenme durumu ve tekrar hesabı

- Başlangıç aralıkları: **1 · 3 · 7 · 21 · 45 · 90 gün**, üst sınır ~180 gün.
- `COZEMEDIM` → aralık 1 güne döner.
- `ZORLANDIM` → aralık daha yavaş büyür (ör. mevcut aralıkta kalır veya bir kademe ilerler).
- `RAHAT` → aralık bir kademe büyür.
- `cozum_acildi = 1` ise sonuç ne olursa olsun aralık kısalır.
- Kullanıcı sonucu seçtikten sonra sonraki tarihi elle değiştirebilir (`elle_atandi = 1`).
- `durum` türetme önerisi: hiç tekrar yok → `YENI`; sonuç `COZEMEDIM` veya aralık ≤ 3 → `OGRENIYOR`;
  aralık ≤ 21 → `PEKISIYOR`; aksi hâlde `SAGLAM`. (Kesin eşikler Aşama 4'te ayarlanabilir.)

## 6. Şema sürümleme

- Room `version` alanı her şema değişikliğinde artırılır ve **veri koruyan `Migration`** yazılır.
- `fallbackToDestructiveMigration` **kullanılmaz**. Hatalı geçişte veritabanını silmek normal
  güncelleme çözümü değildir.
- Kullanıcı güncellemeden önce yedek alması konusunda uygulama içinde hatırlatılabilir (Aşama 5).
