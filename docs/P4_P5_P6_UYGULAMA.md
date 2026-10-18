# P4–P6 uygulama ve kabul kaydı

**18 Ekim 2026.** Tek görev/bağımlılık planı [GELISIM_VE_OGRENME_PLANI.md](GELISIM_VE_OGRENME_PLANI.md).
Bu belge uygulanan sözleşmeyi ve gerçekten gözlenen kapıları kaydeder; test yazılması, testin geçmesi değildir.

## Güncel durum

- **P4 ve P5 otomatik kapıları geçti.** P6 kodu ve ek kontrolleri uygulanıyor; son birleşik Debug/Release kapısı henüz yeşil değil.
- Aday sürüm **1.5.0 / code18**; ara APK yayını kapalı. Son doğrulanmış yayımlanan sürüm hâlâ **1.4.1 / code17**.
- Üretim varsayılanı **Gölge**: kaydedilen tarih v2; ayrı v3 adayının farkı/nedenleri yerel kayıttadır.
- **Etkin v3 kapalı.** Cihaz ve gölge kabul kapıları kodda ayrı ve varsayılan false. Tercih dosyası bu kapıları atlayamaz.
- Fiziksel S24 FE / daha düşük güçlü cihaz, TalkBack, SAF/bulut, düşük depolama, gerçek süreç ölümü/reboot ve frame-p95 kabulü yapılmadı. Robolectric bunu ikame etmez.

## P4 — ayrı, sınırlı karar katmanı

- `AkilliTekrarKurali` v2 referansı; `UyarlanabilirTekrarKurali` v3 adayıdır. Kapalı → v2/karar snapshot1;
  Gölge → uygulanmış v2 + aday3/karar snapshot2; Etkin → yalnız iki kapı da açıkken v3.
- Çözüm açılması veya Çözemedim daima **1 gün**. Eksik/kısmi/geçersiz süre, yetersiz/bozuk profil veya
  desteklenmeyen geçmiş sürümü süre düzeltmesi üretmez. Hız tek başına bonus/uzatma, yavaşlık tek başına ceza değildir.
- Yakın geçmiş **en fazla 5 ayrı gün / 90 gün**; aynı günün eski mükerrerlerinde son kayıt bir gün sayılır.
  Son iki ayrı gün bağımsızsa eski güçlük uyarısı kalkar. Ömür boyu başarısızlık etiketi kullanılmaz.
- Aday yalnız güncel **Rahat + çözüm açılmamış + pozitif TAM ölçüm**, en az iki yakın güçlük günü ve yeterli
  kişisel referansla değişebilir. Süre hem **1,5 × medyanı**, hem **medyan + max(3 MAD, 1,5 IQR)** eşiğini aşmalıdır.
- İndirim `floor(v2Aralık / 10)` gündür: yuvarlama dahil en çok **%10**, küçük aralıkta sıfır olabilir.
  Sonuç 1–180 gün. V2 gecikme indirimiyle birikmez; Zorlandım zaten kendi kuralını kullanır.
- Geçmiş algoritmaları açıkça **1/2/3** yönlendirilir. V3 ease geçmişi okunur; Kapalı'ya dönüş yalnız
  gelecekteki kayıtları v2 yapar, eski v3 satır/tarihlerini yeniden yazmaz.
- Önizleme ve kayıt aynı servis/girdilerden gelir; gün, saat dilimi, politika, geçmiş, bağlam veya profil
  değişmişse kullanıcı yeni önizlemeyi görüp yeniden kaydeder. Sessiz tarih değişikliği yapılmaz.

## P5 — gerçek analitik ve çalışmaya dönüş

- Soru ve tekrar sayısı ayrıdır. Bağımsız paydasına süre geçersiz/eksik olsa da tüm geçerli sonuçlar girer.
  Eski bilinmeyen bağlam, bilinen fakat konu/kaynağı boş bağlamdan ayrılır; geçmiş güncel etiketle yeniden yazılmaz.
- Tüm-arşiv analitiği ve XP seçili sınavla gizlenmez. **Plan/eylem** seçili sınav görünümündedir.
  Kitap kalitesi veya genel sınav yeteneği sıralaması yoktur.
- Ritim son 7 gerçek yerel gündür; bugün henüz tamamlanmadı. Karşılaştırma **−14…−8** ile **−7…−1** günleridir;
  bugün hariçtir. Aynı sorunun her penceredeki son değerlendirmesi, aynı yakalanmış ders/konu/kaynakla eşleştirilir.
  En az **10 ortak soru** ve bu son değerlendirmelerde her dönemde **3 ayrı gün** gerekir. Gerçek eşleşen gün sayıları da gösterilir. Hacim/soru karışımı
  değişimi öğrenme kazanımı sayılmaz; yeterli eşleşme de yalnız betimsel özettir.
- 7 günlük plan sayısı gerçek uygun sorulardan; gecikenler bugüne. Süre, yalnız rahat sorulardan değil,
  **tüm sonuçların TAM aktif iş yükünden** Q1–Q3 yaklaşık aralığıdır. %80 altında toplam ölçekleme yok;
  kapsanan/bilinmeyen soru ve yalnız kapsananların ara toplamı görünür. Kapsanan sıfırsa dakika gösterilmez.
- Öneri: vadesi gelen → uygun yakın-güçlük konu/kaynağı → isteğe bağlı en fazla 5 soruluk tur.
  Filtrede ders/vade/kaynak kapsamı görünür ve kaldırılabilir. Odak kendiliğinden başlamaz, hedef artırılmaz.
- Aktif plan, arşivli olmayan ders, seçili profil ve **gerçek aynı-gün tekrar kaydı** uygunlukta ortaktır.
  Başlatma ve kayıt öncesi tazelenir; eski plan özeti same-day kontrolünü aşamaz. Konu arşivleme katalog
  davranışını korur; eski sorunun konu kimliği veya arşiv durumu eylem tarafından değiştirilmez.
- Ayrıntılar kapalı başlar, grup listesi altılı sayfalanır. Sonlu ortak hareket politikası; azaltılmış harekette
  doğrudan son durum, gizli rozet adı/çizimi/erişilebilirlik maskesi korunur.
- Rütbe deposu kalıcı üst eşik/görülme/tek birleşik kuyruk tutar. Startup/restore/hedef yeniden hesabı sessiz tabandır.
  Rozet sahipliği korunur; badge-priority ortak host aynı anda yalnız bir fullscreen açar. Rütbe ek XP vermez.

## P6 — teknik kapılar ve tanı

- UI/Preview testleri `src/testDebug`; ortak JVM/Room/ZIP testleri Debug **ve** Release'de. Release'e debug
  fixture, Preview veya test Activity eklenmedi. Varyant test sayıları toplanmaz: ortak testler iki kez çalışır.
- Analiz 4 toplu, ince projeksiyonlu SELECT kullanır; karar JSON'u/not/görsel taşımaz. Saf hesap `Default`'ta,
  DB erişimi Room'da; Compose gövdesinde DB/medyan yok. Kalıcı analitik cache yok; veri/profil/hedef/ödül
  invalidation'ı, gece yarısı, resume ve saat/saat dilimi değişimiyle yeniden türetme vardır. Saniyelik poll yok.
- Ayarlar tanısı toplam ölçüm/karar sayısını **zaman damgasına göre en yeni en fazla 100** doğrulanmış/bozuk kayıt örnekleminden ayırır.
  V2/Gölge/Etkin/farklı aday/geçersiz sayıları; açık yenileme ve hata durumu. Bu sayaç öğrenme yararı veya
  etkinleştirme izni değildir. Eski ölçümsüz tekrarlar karar snapshot'ı varmış gibi sayılmaz.
- Room4 / ZIP4 sürümleri aynı. Snapshot1/2 sıkı okuyucu; gölge ve testte açık kapılı etkin3 roundtrip;
  v2'ye geri dönüş, yeniden hesaplanmış hash'le semantik bozulma reddi. Politika/rütbe/rozet/hedef/profil/
  checkpoint prefs'i ZIP'e dahil değil. Restore commit sonrası kozmetik kuyruklar marker kaldırılmadan temizlenir.
- Native SQLite host benchmark'ı: **1.000 soru / 20.000 tekrar**, 4 ders, 40 konu, 100 ders-kaynak grubu;
  5 sıcak örnek, profil/tam özet süreleri ve EXPLAIN. Sentetik veri Git'e/üretime girmez. İlk ölçüm raporu
  [PERFORMANS_DEBUG.md](PERFORMANS_DEBUG.md); her rapor ölçülen kaynak SHA'sını taşır. Bu kare-p95 veya telefon soğuk açılışı değildir.
- Native Compose açık/koyu/%200/hareket kapalı/boş önizlemeler, ≥48dp eylem, anlamlı metin/semantik,
  tek kutlama host'u ve gerçek shell→Room öneri/Filtreli/Odak/Karne/İlerleme akışı otomatik kapsamdadır.

## Gözlenen CI günlüğü

| Aşama / kaynak | Koşu | Gerçek sonuç |
|---|---|---|
| P4 ilk `3304902` | `39139076925` | JUnit dönüş tipi `Boolean` nedeniyle sınıf başlangıcı reddedildi; `Unit` düzeltildi |
| P4 `35896fe` | `39139377311` | **240 / 0 / 0 / 0**, release/imza/sertifika başarılı; yayın kapalı |
| P5 ilk `eb9a5b6` | `39140225921` | **274 / 3 / 0 / 0**; Room iç sorgusu ayrımı, eski rozet birliği ve hedefin sessiz kutlama beklentisi düzeltildi |
| P5 `522b009` | `39140443680` | **274 / 0 / 0 / 0**, release/imza/sertifika başarılı; yayın kapalı |
| P6 ilk `bb6df62` | `39141311070` | Yeni restore testinde eksik yerel `context`; testler başlamadı, Release kapısı çalışmadı |
| P6 `ffd8437` | `39141497735` | Debug **292 / 1 / 0 / 0**; tüm UI/benchmark geçti, restore fixture tek-sefer hata verdiği için anlık recovery marker'ı kaldırdı; sürekli hata senaryosuna düzeltildi. Release çalışmadı |
| P6 `abc0fb0` | `39141755041` | Debug **297 / 1 / 0 / 0**; restore ve midnight/resume geçti. Tanı doğru 100 kaydı verdi; Room Relation cursor tekrarını hesaba katmayan 3-sorgu beklentisi düzeltildi (1 COUNT + 1–2 sınırlı parent + tek 100-ID child; N+1 yasak). Release çalışmadı |

Sütun sırası: **test / başarısız / hata / atlanan**. Derleme/parsing ile çalışmış test sonucu birbirinin yerine yazılmaz.
Güncel otomatik rapor [DERLEME_RAPORU.md](DERLEME_RAPORU.md). İlk başarısızlıkları gizlemek için test/koruma silinmedi.

## Etkinleştirme / geri dönüş runbook'u

1. Debug/Release testleri, migration/ZIP ve imza kapıları yeşil olmalı; kaynak/sürüm/çıktı ilişkisi doğrulanmalı.
2. S24 FE ve mümkünse daha düşük güçlü cihazda [kabul listesi](TEST_KONTROL_LISTESI.md) tamamlanmalı;
   gerçek süreç/SAF/depolama/frame ölçümleri ayrı kanıtlanmalı.
3. Gerçek yerel gölge kararlarında farklar, gerekçeler, eksik veri, çözüm/fail, mükerrer gün, toparlanma,
   gecikme ve yuvarlama sınırları incelenmeli. 100 kayıtta fark görülmesi otomatik kabul veya öğrenme artışı değildir.
4. Bu kanıtlar yoksa iki kapı kapalı kalır. İleride kabul sonrası kodda kapılar açılıp yeniden CI/release doğrulaması
   yapılmadan yalnız prefs değiştirerek etkinleştirme yapılamaz.
5. Sorun halinde Ayarlar → **Kapalı / v2**. Bundan sonraki kayıt v2; eski geçmiş/ölçüm/snapshot korunur.
   Eski APK'ya DB downgrade veya tarihleri geriye dönük yeniden yazma geri dönüş yöntemi değildir.

Native önizlemeler incelendi: koyu debug fixture açıklamasındaki kontrast düzeltildi; yeni analiz panelleri mevcut petrol/bakır yüzey paletine uyarlandı. Eşleşen soru çok olsa da gün eşiği sağlanmayan örnekte gerçek gün adetleri eklendi; yeterlilik kuralı değiştirilmedi. Bu son değişiklikler yeniden CI bekliyor.
