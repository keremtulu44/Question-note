# Android 1.6.1 — doğrulanmış yayın özeti

Bu özet GitHub Actions işinin tamamlanmış durumu ve yayımlanan Release notlarıyla
kontrol edilmiştir. Önceki başarısız denemenin dalda kalmış raporunun yerine geçer.

- Sürüm: **1.6.1**, versionCode **40**
- Derlenen commit: `f4de076cb4a48ab1f2e9f09c06a0e7e6cf1b10f4`
- Dal: `arena/263f59f4-question-note`
- Sonuç: **BAŞARILI**
- Actions: https://github.com/keremtulu44/Question-note/actions/runs/40384089478
- Debug: **380 test, 0 başarısız, 0 hata, 0 atlandı**
- Release ortak testleri: **300 test, 0 başarısız, 0 hata, 0 atlandı**
  (Ortak testler tekrar çalışır; iki sayı toplanmaz.)
- İmzalı APK: https://github.com/keremtulu44/Question-note/releases/download/v1.6.1/yanlis-defteri-1.6.1.apk
- APK SHA-256: `e11e85f04d8a648ab24be0ebb83961fa9f4de8b60e2a0b42a682f3a77bf76abe`
- Sertifika SHA-256: `FA:13:DE:D7:3C:B4:6E:DE:9E:36:37:47:EE:38:95:F2:B6:37:14:9C:16:57:90:A2:E4:67:78:F1:30:89:DE:54`

İlk denemede yeni testteki DpRect ölçümü düzeltildi. İkinci denemede test saatinin
kaydırma sırasında dondurulması giderildi. Güncel iş testleri, derlemeyi, imzalamayı
ve yayını başarıyla tamamladı. Fiziksel cihaz, TalkBack ve SAF kabulü ayrıca yapılmalıdır.
