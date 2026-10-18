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

## P4 otomatik kapısı

- İlk CI `39139076925`: üretim kodu derlendi; bir yeni testin Kotlin dönüşü `Boolean` olduğu için JUnit sınıf başlangıcı reddedildi. Test `Unit` dönüşüne düzeltildi; doğrulama azaltılmadı.
- Kaynak `35896feb05ff3ca75fbbd339cd30ec5df4db10d8`, CI `39139377311`: **240 test / 0 başarısız / 0 hata / 0 atlandı**. `assembleRelease`, apksigner ve sabit sertifika doğrulaması başarılı. Yayın kasıtlı kapalı; bu ara APK teslim sürümü değildir.

## P5 ilk uygulama (doğrulama bekliyor)

- Karar JSON'u/not/görselleri taşımayan 4 toplu analitik sorgusu; P3 profil sorguları da ince projeksiyon kullanır.
- Tüm arşivde gerçek soru/tekrar/payda/kapsama; tarihsel konu/kaynak adları, ayrı eksik ve bilinmeyen bağlam grupları.
- Tamamlanmış iki 7 gün; aynı soruda aynı yakalanmış ders/konu/kaynak bağlamı. En az 10 eşleşen soru ve her dönemde 3 ayrı gün yoksa karşılaştırma yetersizdir. Genel öğrenme/yetenek hükmü yok.
- 7 günlük gerçek ritim; bugün eksik. Plan ve öneriler seçili sınav görünümünde; geçmiş analizi ve XP tüm arşivde.
- Plan iş yükünde tüm sonuçlar kullanılır; %80 altı yalnız kapsanan soruların ara toplamı (kapsanan sıfırsa dakika yok). %80 ve üzerindeki toplam ölçeklemesi açıkça yaklaşık ve belirsizdir.
- Öneri → kapsamı görünür Filtreli → kullanıcı başlatırsa Odak. Ders/vade/normalize kaynak ve gerçek aynı-gün kaydı ortak uygunlukta; başlatma ve kayıt anında tekrar kontrol.
- Rütbe için kalıcı üst eşik/görülme/tek birleşik kutlama; startup/restore/hedef hesabında sessiz taban; rozet sahipliği silinmez. Ortak host aynı anda yalnız bir fullscreen açar. Rütbenin XP etkisi yoktur.

## P5 otomatik kapısı

- İlk CI `39140225921`: **274 test / 3 başarısız**. İki yeni testte fixture beklentisi düzeltildi: Room'un iç invalidation sorguları 4 uygulama sorgusundan ayrıldı; eski rozetin korunması sayıya katıldı. Eski hedef testi, yeni P5 sözleşmesine göre sahiplik/XP korunarak **kutlama kuyruğuna eklenmeme** davranışını doğrular hâle getirildi; arşiv/ödül idempotensi kontrolleri kaldırılmadı.
- Kaynak `522b0094a5fe01a7df1de23dd45b5af4c0413706`, CI `39140443680`: **274 / 0 / 0 / 0**; release build ve sertifika başarılı. Ara yayın kapısı hâlâ kapalı.

## P6 doğrulama hazırlığı (sonuç henüz yok)

- UI/Preview testleri `src/testDebug`'a ayrıldı; ortak testler hem Debug hem Release varyantında derlenip çalıştırılacak. Release'e debug fixture/Preview/test Activity eklenmedi.
- CI'da iki ayrı JUnit özeti, gerçek 1.000 soru / 20.000 tekrar host benchmark'ı, SQLite EXPLAIN planları ve 5 yeni sentetik native Compose önizlemesi için çıktı toplama eklendi. Ölçülmeden süre/başarı sonucu yazılmadı.
- Uygulama shell'i üzerinde öneri → filtre → Odak → karne → İlerleme smoke regresyonu, rozet/rütbe tek-host testi, %200 / kapalı hareket / 48dp / gölge-ayarı UI kontrolleri eklendi.
- Gün/saat dilimi değişimi ve resume ile gün tabanlı ekranlar yenilenir; analitik saniyelik poll yapmaz.
- Ayarlar tanısı en yeni 100 karar için katı doğrulama uygular; toplam/örnek/gölge/farklı aday/etkin/bozuk sayıları açıkça ayrılır. Bu örneklem etkinleştirme kanıtı diye sunulmaz.
- ZIP4 içinde karar1/karar2 ve testte açık kapılı v3 roundtrip/rollback; semantik bozulma doğru hash ile de reddedilir. Restore commit/marker sınırında kozmetik kuyrukların sıfırlanması regresyonu eklendi.
