# İlerleme, rütbe ve rozetler — v1.3.0

Kod: `domain/seviye`, `domain/karne/Rozet.kt`, `data/ilerleme/RozetHizmeti.kt`,
`data/seviye/SeviyeHizmeti.kt`. Bu belge uygulanan kuralları anlatır; cihaz performansı ölçülmüş değildir.

## Gezinme ve ekran sınırları

**Bugün · Dersler · Filtreli · İlerleme · Ayarlar.** Yan menü yoktur. Sağ üstteki tekrarlı Ayarlar
kısayolları kaldırıldı; Ayarlar yalnız alt sekmededir. Alt menü, Android gezinme çubuğu ve ekran
çentiği inset'lerini kendisi uygular. Dış Scaffold bu boşluğu içeriğe aktarır ve tüketir; iç ekranlar
aynı alt boşluğu ikinci kez eklemez. Üç düğmeli gezinme, jest modu, yatay ekran ve büyük yazı cihaz
kontrol listesinde ayrıca doğrulanmalıdır.

## P0/P1: gerçek verili yeni sunum

- Rütbe → seviye → toplam/seviye içi XP → sonraki rütbe hiyerarşisi. Petrol/lacivert kart,
  bakır vurgular, mevcut saydam amblemler; ağır blur veya sonsuz parıltı yok.
- Normal ölçekte 2×2, büyük yazıda tek sütun istatistikler. Rozet/rütbe koleksiyonu ve XP
  dökümü isteğe bağlı açılır; ilk ekran bütün galeriyi çizmez.
- **Emek ≠ öğrenme:** XP çalışma kaydıdır. Ders pekişmesi mevcut tekrar planından türetilen
  göstergedir, sınav başarısı değildir. 7 günlük grafikte soru adedi vardır; gecikmiş kayıtlar
  dahil değildir. Süre, kaynak/konu başarısı ve kişisel profil sayısı üretilmez.
- `SeviyeDurumuUi`: yükleniyor, hata/yeniden dene, tekrar verisi yetersiz, hazır. Boş arşivden
  soru eklemeye; sorusu olup tekrarı olmayan kullanıcı Bugün'e yönlendirilir.
- XP ilk gösterimde gerçek değerden başlar. Yalnız açık ekrandaki gerçek değişiklik animasyon
  alır. Sistem animasyon ayarı canlı izlenir; kapalıyken sayılar/çubuklar animasyon karelerini
  beklemeden hedef değeri gösterir. Karne, kutlama ve Odak otomatik kaydırması aynı politikayı kullanır.
- Karne rozet toplamı sahiplik akışından canlı okunur. Gözlem geçici hatada 1/2/3 saniye
  aralıkla en fazla üç kez yeniden kurulur; kalıcı hatada yenileme eylemi ve Bugün/İlerleme'ye
  dönüş yeni gözlem başlatır. İptal, veri hatası olarak yutulmaz.
- İlerleme özeti tek Room transaction'ında okunur; sahiplik, gün ve hedef snapshot'ı galeri/XP
  için ortaktır. Günlük hedef değişikliği başka tekrar gerektirmeden özeti/rozetleri yeniler.
- [Debug-only tasarım önizlemeleri](ui/p1/README.md) gerçek cihaz kabulü değildir.

## XP: eklemek de çalışmak da değerli

| Eylem | XP |
|---|---:|
| Defterdeki her kayıtlı soru | 10 |
| Her tekrar, sonuç ne olursa olsun | 5 |
| Bağımsız çözüm: **RAHAT ve çözüm açılmamış** | +5 |
| Seçili günlük hedefe ulaşılan her farklı gün | 25 |
| Bir kesintisiz serinin 3. / 7. / 30. gününe ulaşılması | 30 / 70 / 300 |
| Her kazanılan rozet, yalnız bir kez | 50 |

- Bir kesintisiz seride her kilometre taşı **bir kez** ödenir. Örneğin 10 gün = 30 + 70;
  6. günde bir ikinci üç-gün bonusu verilmez. Seri kesilir ve yeniden başlarsa kilometre taşları
  tekrar sayılır.
- v1.2.0 düzeltmesi: bazı eski toplu sorgular `ZORLANDIM` sonucunu da bağımsız sayıyordu. Artık
  XP, haftalık özet ve rozet hesabı karnedeki katı tanımla aynıdır. Tekrar emeği +5 korunur;
  yanlış verilmiş bağımsız bonusu yeniden hesaplanır. Geçmiş tekrar kayıtları değiştirilmez,
  önceden kazanılan rozetler geri alınmaz.
- Ayrı XP tablosu yoktur. Soru/tekrar geçmişi geriye dönük sayılır. Bir soru ve bağlı geçmişi
  silinirse türetilen XP de değişebilir; günlük hedef değişimi geçmiş hedef-günü hesabını da etkiler.
- Rozet bonusu kalıcı, cihaz-yerel sahiplikten okunur. Yeni galeri eski rozet anahtarlarını korur.

## Seviyeler ve 15 rütbe

İlk geçişler **100, 250, 450, 700, 1000 XP** gerektirir; kümülatif eşikler 0, 100, 350, 800,
1500, 2500… şeklindedir. `n → n+1` için gereken XP:
`100 + 100·(n−1) + 25·(n−1)·n`.

| Başlangıç seviyesi | Rütbe | Amblem |
|---:|---|---|
| 1 | Başlangıç | Filiz |
| 2 | Meraklı | Ampul |
| 3 | Düzenli | Kitaplar |
| 4 | Kararlı | Hedef |
| 6 | Disiplinli | Kalkan |
| 8 | Azimli | Zirve |
| 10 | Odaklı | Göz |
| 12 | Usta | Yıldız |
| 15 | Şampiyon | Madalya |
| 18 | Kâşif | Pusula |
| 22 | Stratejist | Satranç atı |
| 28 | Bilge | Baykuş ve kitap |
| 35 | Efsane | Anka |
| 50 | Kozmik | Gezegen |
| 75 | Sonsuz | Sonsuzluk ve defne |

Son rütbe son seviye değildir. Eski gizli 200-seviye sınırı kaldırıldı; hesap Long ara değerlerle
Int XP kapasitesi içinde taşmadan ilerler. Galeride her rütbenin seviyesi, toplam XP eşiği ve
kalan XP'si görülebilir. Altı yeni amblem uygulama için üretildi; saydam, 96 dp WebP, beş yoğunluk.
Tüm rütbeler toplam 15 ayrı görseldir. Rozetlerin 30 ölçeklenebilir çizimi Compose Canvas kullanır.

## Rozet koleksiyonu: 30 rozet, 5 sır

Galeri filtreleri: **Tümü / Kazanılan / Kilitli / Gizli**. Açık rozetin kartı ve detayında kazanma
koşulu vardır. Birikimli rozetlerde sayısal ilerleme gösterilir. Oturum rozetleri için "tek bir
tamamlanmış oturum" koşulu açıkça yazılır; geçmiş oturum rekoru varmış gibi sayı üretilmez.

| Açık rozet / grup | Koşul |
|---|---|
| İlk adım | En az bir soruluk oturum tamamla |
| Kusursuz tur | 5+ soruluk oturumun tamamı bağımsız |
| Onluk / Yirmilik | Bir oturumda 10 bağımsız / 20 toplam soru |
| Kendi başına | 5+ soruluk oturumda hiç çözüm açma; sonuç türü önemli değil |
| İstikrar | Son 5 günün her birinde en az 5 tekrar, en az yüzde 70 bağımsız |
| Üç gün / Haftalık seri / İki haftalık ritim / Otuz gün / Altmış gün / Yüz gün | 3 / 7 / 14 / 30 / 60 / 100 günlük kesintisiz seri |
| Yüz / Beş yüz / Binyıldız | 100 / 500 / 1000 bağımsız çözüm |
| İlk sayfa / Arşivci / Kütüphane / Koleksiyoncu | Defterde 1 / 25 / 100 / 250 kayıtlı soru |
| Isındın / Emek ustası / Bin adım | 50 / 200 / 1000 tekrar |
| Hedefe vardın / Yedi zafer / Hedef mimarı | 1 / 7 / 30 farklı günde seçili günlük hedefe ulaş |
| 5 gizli rozet | Kazanılana kadar koşul, ad ve gerçek çizim gösterilmez |

Gizli rozetin kilitli kartı ve detayında yalnız nötr kilit çizimi ve sürpriz açıklaması vardır;
TalkBack'e de gerçek adı verilmez. Kazanıldığında adı, çizimi ve koşulu açılır.
Teknik koşulların kaynağı `RozetKurallari`; galeri ilerlemesi ile ödül koşulu **aynı fonksiyonu** kullanır.

### Tam ekran kutlama

- Yeni rozet önce sahiplik ve **bekleyen kutlama kuyruğuyla birlikte** kalıcı kaydedilir.
- Güvenli ekranda tam ekran madalyon, isim, açıklama ve +50 XP gösterilir. Çözme/soru formu
  sırasında kesinti yapılmaz; kutlama oturum çıkışına veya karneye bırakılır.
- Birden çok rozet sırayla gösterilir. "Harika, devam et" veya geri düğmesi yalnız o kutlamayı
  tamamlar; rozet sahipliği silinmez. Kendiliğinden kapanan süre sınırı yoktur.
- Uygulama kapanırsa gösterilmemiş kutlamalar kaybolmaz. Daha önce kazanılan eski rozetler
  güncellemede yeniden kutlanmaz. Paralel değerlendirme aynı rozeti iki kez vermez.
- Konfeti sonludur; sistem animasyon ölçeği sıfırsa hareket uygulanmaz. Titreşim sistemin
  haptik ayarına uyar.

## Ayarlar: sınav formatı ve konu silme

- Formatlar: **LGS, TYT, AYT, KPSS Genel Yetenek/Genel Kültür, Tümü / özel**.
- Eksik dersler eklenir; ad eşleşiyorsa mevcut ders ID'si kullanılır. Soru, konu, görsel ve tekrar
  kayıtları silinmez/arşivlenmez. Kullanıcının arşivlediği dersler kendiliğinden açılmaz.
- Seçili format Bugün, Dersler ve Filtreli çalışma görünümünü sınırlar. Tümü / özel tüm aktif
  dersleri yeniden gösterir. XP ve kazanılmış rozetler bütün arşiv için ortaktır.
- **Konu yönetimi ve silme** tüm dersleri, format dışında kalanları ve arşivli dersleri de açar.
  Ders ara → konu seç → sil. Kullanılan konu silinmez; soru sayısı ve Arşivle alternatifi gösterilir.
- Kaynak/yanlış nedeni yönetimi ve Drive/SAF yedekleme yine Ayarlar'dadır.

## Veri sınırı

Room **v3**, taşınabilir ZIP **v3** (v2 içe alma korunur). Yeni tablo veya kolon yoktur. Rozetler,
hedef, format tercihi ve yarım oturum cihaz-yerel prefs'tedir; ZIP'e girmez. Çözme sistemi:
[ODAK_STUDYOSU.md](ODAK_STUDYOSU.md). Test kanıtı: [DERLEME_RAPORU.md](DERLEME_RAPORU.md).
