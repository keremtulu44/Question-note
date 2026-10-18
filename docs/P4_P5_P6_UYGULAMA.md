# P4–P6 uygulama ve kabul kaydı

Bu kayıt `GELISIM_VE_OGRENME_PLANI.md` içindeki P4 → P5 → P6 sırasını izler.

## Ara durum — 18 Ekim 2026

- P4 kodu ve regresyonları eklendi; ilk CI doğrulaması henüz bekleniyor.
- Üretim varsayılanı **Gölge**: uygulanan tarihler v2; ayrı, açıklanabilir v3 adayının farkı yerel karar snapshot 2 içinde saklanır. Kapalı yol snapshot 1 uyumluluğunu korur.
- Etkin v3 iki ayrı kabul kapısı (cihaz + gölge) ister; ikisi de bu sürümde kapalıdır. Tercih değiştirerek bu kapı atlanamaz.
- Yakın geçmiş en fazla 5 ayrı gün / 90 gün; son iki bağımsız başarı güçlük uyarısını kaldırır. En fazla yüzde 10 **gerçek** indirim (aşağı yuvarlama); gecikme indirimiyle birikmez.
- Desteklenen geçmiş algoritmaları açıkça 1/2/3; v3 kayıtları v2'ye geri dönüşte aynı kolaylık kuralıyla okunur. Eski satırlar yeniden yazılmaz.
- Room 4 / ZIP 4 korunur. Karar snapshot 1 okuyucusu korunur; snapshot 2 girişleri aday ve uygulanmış plan/nedenlerle yeniden doğrulanır.
- Ara CI yayın kapısı **kapalı**: test/build/imza doğrulaması çalışır, ara APK GitHub Release olarak yayımlanmaz.
- P5 ve P6 devam ediyor; henüz tamamlandı veya test edildi sayılmaz.

## Fiziksel kabul

Gerçek S24 FE, TalkBack, SAF/Drive, düşük depolama, gerçek süreç ölümü ve cihaz frame-p95 ölçümleri henüz yapılmadı. CI/Robolectric çıktıları fiziksel cihaz kabulü veya v3'ün öğrenme yararı kanıtı değildir. Etkin v3 bu sonuçlar varmış gibi açılmayacak.
