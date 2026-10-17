# Seviye, rütbe, XP ve rozet sistemi (v1.1.0)

Bu belge, alt menüyle gelen ilerleme sisteminin kurallarını tek yerde toplar. Kod tarafında tek
doğruluk kaynağı `domain/seviye/SeviyeHesabi.kt`, `domain/seviye/Rutbe.kt` ve
`data/seviye/SeviyeHizmeti.kt` dosyalarıdır.

## 1. Gezinme

Yan menü (drawer) **kaldırıldı**. Alt menüde beş sekme vardır:

| Sekme | Ekran | Not |
|---|---|---|
| Bugün | Günlük tekrarlar, hedef halkası, seri | Soru ekleme düğmesi burada |
| Dersler | Ders → konu → soru ağacı | Soru ekleme akışının girişi |
| Filtreli | Konu/kaynak/neden filtreli oturum | Oturum bitince karne açılır |
| Seviye | XP, rütbe, rozet galerisi, istatistik | Bu belgenin konusu |
| Ayarlar | Hedef, yedek, bakım, katalog, hakkında | Eski yan menünün tamamı |

Alt menü yalnız **sekme köklerinde** görünür. Soru formu, soru ayrıntısı, filtreli oturum ve karne
tam ekran çalışır; geri düğmesi bir üst ekrana döner.

## 2. XP kazanma kuralları

| Eylem | XP | Açıklama |
|---|---|---|
| Soru ekleme | **+10** | Her kayıtlı soru (fotoğraf/not fark etmez) |
| Tekrar (her sonuç) | **+5** | Çözemedim de olsa emek sayılır |
| Bağımsız çözüm bonusu | **+5** | `RAHAT` ve çözüm açılmamış (`ZORLANDIM` bonus almaz) |
| Günlük hedef tamamlandı | **+25** | Hedefe ulaşılan her gün için bir kez |
| Seri 3 gün | **+30** | Her 3 günlük seride yeniden |
| Seri 7 gün | **+70** | Her 7 günlük seride yeniden |
| Seri 30 gün | **+300** | Her 30 günlük seride yeniden |
| Rozet | **+50** | Kazanılan her rozet için bir kez (11 rozet → en çok 550 XP) |

**Ayrı XP tablosu yoktur.** XP her açılışta `sorular` ve `tekrarlar` tablolarından yeniden hesaplanır.
Bu sayede geçmiş geriye dönük olarak sayılır ve gösterilen sayı ile veri asla çelişmez.

## 3. Seviye eşikleri

Seviye 1'den itibaren gereken XP:

| Geçiş | Gereken | Toplam |
|---|---|---|
| 1 → 2 | 100 | 100 |
| 2 → 3 | 250 | 350 |
| 3 → 4 | 450 | 800 |
| 4 → 5 | 700 | 1.500 |
| 5 → 6 | 1.000 | 2.500 |
| n → n+1 | `100 + 100·(n−1) + 25·(n−1)·n` | — |

Fark her seviyede 50 XP artar; üst sınır yoktur. Eşiğe tam ulaşıldığında seviye atlanır.

## 4. Rütbeler

| Seviye | Rütbe | Amblem |
|---|---|---|
| 1 | Başlangıç | filiz |
| 2 | Meraklı | ampul |
| 3–4 | Düzenli | kitap yığını |
| 5 | Kararlı | hedef tahtası |
| 6–7 | Disiplinli | kalkan + onay |
| 8–9 | Azimli | zirvede bayrak |
| 10–11 | Odaklı | göz |
| 12–14 | Usta | yıldız madalyon |
| 15+ | Şampiyon | defne + madalya |

Amblemler uygulama için üretildi, beyaz arka planı temizlendi ve saydam logo olarak
`drawable-*/rank_*.webp` (5 yoğunluk, 96 dp) eklendi. Son rütbeden sonra seviye artmaya devam eder,
amblem Şampiyon olarak kalır.

## 5. Rozetler

Rozetler cihaz-yereldir (`SharedPreferences`, `karne_rozetleri/kazanilanlar`), ZIP yedeğine girmez ve
kazanım idempotenttir. Görselleri Compose `Canvas` ile çizilir (dosya yok, her boyutta net).

| Rozet | Koşul |
|---|---|
| İlk adım | İlk tamamlanan oturum |
| Kusursuz tur | 5+ soruda %100 bağımsız |
| Onluk | Tek oturumda 10 bağımsız |
| Yirmilik | Tek oturumda 20 soru |
| Kendi başına | 5+ soruluk oturumda hiç çözüm açmamak |
| İstikrar | Üst üste 5 gün, her gün ≥5 soru ve ≥%70 bağımsız |
| Üç gün / Haftalık seri / Otuz gün | 3 / 7 / 30 günlük seri |
| Yüz / Beş yüz | Toplam 100 / 500 bağımsız çözüm |

## 6. Ayarlar

| Bölüm | İçerik |
|---|---|
| HEDEF | Günlük hedef seçimi: 5 / 10 / 15 / 20 / 30 (`TercihGunlukHedefDeposu`) |
| VERİ | Yedek oluştur, yedekten geri yükle, son yedek bilgisi (Drive/Downloads dahil SAF hedefi) |
| DİĞER | Görsel bakımı, kaynak/neden yönetimi, öğrenim seviyesi notu |
| Hakkında | Sürüm, veri saklama uyarısı |

## 7. Silme kuralları

| Kayıt | Kural |
|---|---|
| Konu | Soruda kullanılmıyorsa silinir. Kullanılıyorsa **silinmez**; "N soruda kullanılıyor" uyarısı ve **Arşivle** düğmesi çıkar |
| Kaynak / yanlış nedeni | Kullanıcının eklediği kayıt, hiçbir soruda kullanılmıyorsa silinir. Kullanımdaysa engellenir |
| Hazır katalog seçenekleri | Hiçbir zaman silinemez (uygulamayla gelir) |

Sessiz veri kaybı hiçbir durumda olmaz: soru metinleri silinmez, yalnızca kullanılmayan katalog
kayıtları kaldırılır.

## 8. Veri ve migration

- Yeni tablo/kolon **yok**; Room şeması **v3** olarak kalır.
- Yedek biçimi değişmedi (v3).
- Rozetler ve günlük hedef `SharedPreferences` içinde tutulur.
- Bu sürümde `fallbackToDestructiveMigration` kullanılmaz.

## 9. Test

`SeviyeHesabiTest` ve `XpHesabiTest` (14 test) eşikleri, rütbe seçimini, XP ağırlıklarını, seri
bonuslarını ve monotonluğu doğrular. CI toplamı: **86 test** (run `39053319900`).
