# P6 — gerçek Compose bileşenlerinden sentetik önizlemeler

Bunlar tasarım çizimi/AI görseli değil, uygulamanın gerçek Compose bileşenlerinin
Robolectric **native renderer / SDK35 / 393×852dp** çıktılarıdır. **Fiziksel S24 FE ekran
fotoğrafı veya golden-image kıyaslaması değildir.** Veri yalnız `src/debug` fixture'ında:
40 soru/200 tekrar; boş sürümde 0/0. Üretim arşivine eklenmez.

| Dosya | Kontrol |
|---|---|
| [p6-action-light.png](p6-action-light.png) | Açık tema, görünür LGS/vade kapsamı, kullanıcı başlatmalı eylem |
| [p6-action-dark.png](p6-action-dark.png) | Koyu tema, aynı eylem ve okunur açıklama |
| [p6-analysis.png](p6-analysis.png) | Yalnız seçilen ritim bölümü: gerçek sayılar, bugün eksik; diğer analizler istiflenmez |
| [p6-large.png](p6-large.png) | %200 yazı, koyu tema, hareket kapalı; çok satırlı erişilebilir eylem |
| [p6-empty.png](p6-empty.png) | Veri/uygun soru yok; sahte öneri veya dakika yok |

Sayfa dikey kaydırılabilir; PNG yalnız ilk viewport'u içerir. Native görüntülerdeki koyu
fixture açıklaması kontrastı düzeltilmiş, analiz yüzeyleri P1 petrol/bakır paletine uyarlanmıştır.
Karşılaştırma örneğinde 40 ortak soru bulunmasına rağmen son değerlendirmeler her dönemde yalnız
2 güne yayılır; bu yüzden yeterlilik açıkça sağlanmaz. Grafik/sayı, öğrenme yararı iddiası değildir.

Testler `app/src/testDebug/java/.../ui/P6GorselOnizlemeTest.kt`; veriler
`app/src/debug/java/.../ui/seviye/P6Onizlemeleri.kt`; ana bileşen
`app/src/main/java/.../ui/seviye/GelisimAnaliziKarti.kt`. Debug kaynak setinin keşfi ayrıca CI kapısıdır.
1.5.1 sadeleştirmesi bu bileşenlere de yansır. Son güncel çıktı: CI `39158221022`, kaynak `c25b32b`, Debug312/0.
Tam ana görünüm ve ayrı karşılaştırma/plan sayfaları [progress](../progress/README.md) klasöründedir.
[P4–P6 hesaplama sözleşmesi](../../P4_P5_P6_UYGULAMA.md) değişmez.

TalkBack konuşma sırası, gerçek sistem çubukları, dokunma hissi, cihaz frame-p95 ve gerçek süreç
ölümü [fiziksel kabul listesinde](../../TEST_KONTROL_LISTESI.md) açık kalır.
