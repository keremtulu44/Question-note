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

## Doğrulama durumu

Aday **1.5.1 / code19**. İlk CI `39155923794`, taşınırken eksik kalan dört yerel renk tanımında derleme hatası verdi (testler çalışmadı); tanımlar geri eklendi. İkinci CI `39156298092` / `7f8c3d3`: Debug311/0/0/0, Release258/0/0/0; APK/imza/sertifika başarılı, yayın kapalı.
Sekiz ana native görünüm ve P6 ritmi incelendi: normal yazıda ana görünüm tek viewport'a sığıyor.
%200'de eski alt menünün kelime bölünmesi görüldü; seçili etikete alan, diğer dört simgeye tam
semantik ad + 48dp üzeri dokunma alanı veren uyarlama ve gerçek text-layout regresyonu eklendi.
Son üretim kodu `1c429d0` / CI `39156980764`: **Debug312/0/0/0, Release258/0/0/0**, APK/imza/sertifika başarılı.
Alt menüde %200 tek satır ve 320dp/48dp hedef testleri geçti; yeni normal/koyu/boş/ayrıntı görünümleri incelendi.
Son yayın koşusuna yalnız debug önizleme kabuğu düzeltmesi/katı dikey sınır kontrolü eklendi: üretimde zaten
kullanılan `padding + consumeWindowInsets` düzeni fixture’a da uygulandı. Böylece büyük-yazı görüntüsü
"görünüyor" semantiğine değil, düğmenin alt menünün tamamen üstünde olmasına dayanır.
Üretim kodu değişmeden yayın koşusu başlatılıyor; gerçek cihaz kabulü açık.
Yerelde 147 Kotlin sözdizimi ağacı, XML, workflow YAML/Bash ve kaynak referansları kontrol edildi;
bu, Kotlin/Android derlemesinin veya cihaz kabulünün yerine geçmez.

- Yeni `IlerlemeSadelikTest`: ana sayfa metin bütçesi, tek eylem, kapalı teknik paneller,
  gerçek filtre, geri/saved-state, güncel/eksik/hedef üstü sayılar, %200/48dp ve gizli koşullar.
- Var olan XP/animasyon, filtre→Odak→karne→İlerleme, kapsam ve karşılaştırma testleri yeni
  ayrıntı gezinmesi üzerinden korunur; veri kuralları gevşetilmez.
- Native çıktılar `docs/ui/progress/`: açık/koyu/büyük yazı/boş/hata/menü/karşılaştırma/plan.
  Gerçek Compose bileşeni + sentetik debug verisi; telefon ekran görüntüsü değildir.
- S24 FE, TalkBack, gerçek süreç ölümü/SAF/depolama/frame-p95 kabulü hâlâ ayrıdır.
