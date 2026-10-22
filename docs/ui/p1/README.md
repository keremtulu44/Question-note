# P1 — arşivlenmiş Compose önizlemeleri

Bu klasör önceki 1.5.0 döneminin yerleşimini korur; **güncel arayüz değildir**.
Gerçek Compose bileşenlerinin native Robolectric çizimidir, fiziksel cihaz fotoğrafı veya
AI tasarım görseli değildir. 72 soru/144 tekrar gibi örnekler yalnız eski debug fixture’ındadır.

- [Açık tema](p1-light.png)
- [Koyu tema](p1-dark.png)
- [İlk gün](p1-empty.png)
- [Hata](p1-error.png)
- [%200 yazı](p1-large.png)

Güncel ana görünüm ve ayrıntı sayfaları [1.5.1 önizlemelerindedir](../progress/README.md).
`IlerlemeGorselOnizlemeTest` artık `app/build/outputs/progress-previews/` üretir; CI bu eski
PNG’leri güncellemez. Eski kompozisyon yalnız 1.5.0 kaynak/tag geçmişinde bulunur.

Bu görseller piksel karşılaştırmalı golden testler değildir. Fiziksel S24 FE/TalkBack/SAF/
süreç ölümü ve frame-p95 kabulünü karşılamaz.
