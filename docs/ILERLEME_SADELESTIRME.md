# İlerleme — 1.5.1 sadeleştirmesi

## İstek ve sınır

Kullanıcı 1.5.0 İlerleme ekranını fazla metinli ve karmaşık buldu. Amaç daha fazla veri göstermek
veya toplamak değil; mevcut bilgiyi daha sakin, seçici ve görsel bir düzene taşımak.
**Hesaplama/Room/ZIP/XP/tekrar motoru kuralları değiştirilmez. Etkin v3 kapalı kalır.**

## Yeni bilgi düzeni

- Ana görünüm dört blok: kısa başlık; amblem/rütbe/seviye/XP; bugünkü gerçek hedef + tek uygun
  çalışma eylemi; Koleksiyon ve Ayrıntılar geçişleri.
- Büyük sloganlar, ömür boyu dört sayaç, XP dökümü, yedi günlük metin listeleri, karşılaştırma,
  plan/iş yükü ve süre profili artık ana sayfanın altına istiflenmez. Kapalı ayrıntılar Compose ağacına da eklenmez.
- Ayrıntılar menüsünde yalnız seçilen bölüm açılır: emek, ritim, konu/kaynak, dönem, plan,
  ders veya süre. Ekran içi/Android geri doğru üst sayfaya döner; ana liste konumu korunur. Geçici yükleme/yenileme de seçili sayfayı sıfırlamaz.
- Ritim çubuk grafikle; plan yalnız tarih/soru adediyle başlar. Bir güne dokununca o günün
  kapsam/ara toplam/tahmin bilgisi açılır. Karşılaştırmada iki küçük dönem kartı vardır;
  yetersizlik kısa görünür, gerçek gün/örneklem dayanağı “Neden?” altında korunur.
- Rozetler ayrı koleksiyondadır. Kartlarda uzun kazanma yönergeleri yerine görsel/ad/durum;
  koşullar mevcut detay penceresinde. Gizli rozet adı ve koşulu sahip olunmadan açılmaz.
- Tek çalışma eylemi önce mevcut filtreye gider; kendiliğinden tur başlatmaz. Profil/vade/askı/
  arşiv/aynı gün kuralları ve kayıt anı bağlamı aynıdır. Hedef artırılmaz, yeni puan/tahmin üretilmez.
- Günlük sayı mevcut hesaplanmış özetten gelir; bilinmeyen sayı 0 diye sunulmaz. Hedef üstü
  gerçek sayı korunur; yalnız dekoratif halka %100'de sınırlanır.
- Font küçültülerek sığdırma yapılmaz. %200 yazı, dikey kaydırma, 48dp eylemler ve azaltılmış hareket korunur.

## Doğrulama ve teslim

**v1.5.1 / code19 yayımlandı.** [Release](https://github.com/keremtulu44/Question-note/releases/tag/v1.5.1) · [APK](https://github.com/keremtulu44/Question-note/releases/download/v1.5.1/yanlis-defteri-1.5.1.apk) · [CI `39158221022`](https://github.com/keremtulu44/Question-note/actions/runs/39158221022)
**Debug312/0/0/0 · Release258/0/0/0**; APK/imza/sertifika başarılı. Ortak testler iki varyantta tekrar eder, sayılar toplanmaz.
Kaynak/tag `c25b32bce6c20b0f2c39ff27af6990d13c300074`; asset **654278786**, **10.819.807 bayt**; yayımlanma `2026-10-18T19:22:49Z`.
SHA-256 `c8d1d45cfd7ad55ab6d3ebba6bf52464fd772d16d23929cbcb406fe8b2a9a053`; GitHub asset digest ve tag referansı CI ile bağımsız eşleştirildi.

- İlk `39155923794`/`8bfdfe8` derlemesi dört yerel renk tanımı eksik olduğu için testlere ulaşmadı; tanımlar geri eklendi.
- `39156298092`/`7f8c3d3`: Debug311/0 + Release258/0, imza; sekiz native görünüm incelendi.
- `39156980764`/`1c429d0`: Debug312/0 + Release258/0, imza; %200 menü bölünmesi giderildi.
- `39157747382`/`b6903c4`: yalnız Debug test derlemesinde DpRect boyut özelliği hatası; Release258/0. Yayın engellendi, kontrol kenar farklarıyla düzeltildi.
- Finalde üretim kodu `1c429d0` ile aynı. Debug önizlemenin viewport’u gerçek uygulama kabuğuyla
  eşlendi; eylemin tamamının menünün üstünde olması unclipped bounds ile doğrulandı. Yayın bütün kapıları yeniden geçti.
- Dokuz sadelik testi; sekiz native sahne; mevcut XP/motion/filtre→Odak→karne/kapsam/ZIP/motor
  regresyonları korunur. Test atlama/sıfır rapor/kayıp sınıf/kayıp önizleme başarı sayılmaz.
- **Fiziksel S24 FE, TalkBack, gerçek süreç ölümü/SAF/depolama/frame-p95 ve uzunlamasına gölge kabulü açık.**
  Native renderer/host benchmark telefon kabulü değildir. Etkin v3 açılmadı. Merge/PR yapılmadı.
