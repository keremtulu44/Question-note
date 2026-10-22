# 1.5.1 — sade İlerleme, native Compose önizlemeleri

Gerçek uygulama bileşenleri, Robolectric SDK35/native renderer, 393×852dp. **Sentetik debug
verisi**; cihaz fotoğrafı, üretim geçmişi, AI tasarım görseli veya golden-pixel testi değildir.
40 soru/200 tekrar fixture’ı P6 ile aynı analizden gelir. Hedef sayısı bugün 0/10’dur; mevcut
geçmiş ekranda yeni kazanılmış XP gibi oynatılmaz.

Son üretim CI **`39158221022` / `c25b32b`**: **Debug312/0 + Release258/0**, APK/imza/sertifika/yayın başarılı.
Sekiz görünüm incelendi. Önizleme kabuğu gerçek uygulamadaki `padding + consumeWindowInsets`
düzenini kullanır; büyük yazı eylemi tamamıyla alt menünün üstündedir. Alt menüde seçili etiket
tek satır, diğer dört simgede tam semantik ad; beş hedef de 48dp üzerindedir.

Dosyalar:

| Dosya | Görünüm |
|---|---|
| [overview-light.png](overview-light.png) | Açık tema, rütbe + bugün/tek eylem + iki geçiş |
| [overview-dark.png](overview-dark.png) | Aynı düzen, koyu tema |
| [overview-large.png](overview-large.png) | %200 yazı, eyleme kaydırılmış görünüm; seçili alt sekme tam etiket, diğer simgeler tam semantik ad |
| [overview-empty.png](overview-empty.png) | İlk gün: tek soru ekleme eylemi, analitik yetersizlik yığını yok |
| [overview-error.png](overview-error.png) | Veri uydurmayan hata/yeniden deneme |
| [details.png](details.png) | İsteğe bağlı bölüm seçimi; tüm analizler üst üste değil |
| [comparison.png](comparison.png) | İki dönem, 40 ortak soru ama 2+2 gün nedeniyle yeterli olmayan karşılaştırma |
| [plan.png](plan.png) | Yedi kısa tarih/adet satırı; süre ve kapsam ancak gün seçilince |

`./gradlew :app:testDebugUnitTest --tests '*IlerlemeGorselOnizlemeTest'` çıktısı
`app/build/outputs/progress-previews/`. CI sekiz dosyayı ve test sınıfının yürütülmesini
zorunlu tutar; bu küçük PNG’ler dışında rastgele build verisi toplanmaz.

`IlerlemeSadelikTest` ayrıca metin bütçesini, kapalı teknik panelleri, gerçek filtreyi,
geri/saved-state akışını, gerçek günlük sayıyı, %200/48dp ve gizli koşulları denetler.
Fiziksel S24 FE, TalkBack, süreç ölümü, SAF ve frame-p95 kabulü açık kalır.
