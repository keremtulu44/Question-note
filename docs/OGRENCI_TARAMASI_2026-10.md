# Öğrenci akışı incelemesi — 2026-10-28

Bu inceleme deneme takibi ve öğrencinin soru çözme/öz değerlendirme akışına odaklanır.
Öğretmen çalışma alanına özellik eklenmedi; verileri ve mevcut testleri korundu. Room şeması,
tekrar algoritması ve XP kuralları değiştirilmedi. Normal ZIP sürümü 5 olarak kalır; bozuk
ham denemeleri içeren geri yükleme güvenlik kopyası, eski okuyucuların sessizce eksik
kayıt yüklememesi için 6 numaralı özel kurtarma biçimini kullanır.

## Düzeltilen bulgular

1. **Yanıltıcı deneme karşılaştırması:** Farklı puanlama kuralları veya farklı ders/soru
   dağılımları aynı net çizgisinde ve değişim rozetlerinde karşılaştırılabiliyordu. Grafik,
   ortalama ve farklar artık aynı kural ve ders başına soru sayısına sahip kayıtları kullanır.
   Farklı kapsamlar seçilebilir; tüm kayıtlar listede kalır. İki denemenin eşleşmesi,
   zorluklarının veya resmî sınav türlerinin aynı olduğu iddiası değildir.
2. **Gelecekten geçmişe ortalama:** Ders grafiği seçili denemeden daha sonraki kayıtları da
   “önceki ortalama”ya katabiliyordu. Artık yalnız gerçekten önceki, aynı kapsamdaki kayıtlar
   kullanılır; örnek sayısı gösterilir. Sıfır ve negatif netler çizimde korunur.
3. **Belirsiz deneme–soru bağı:** Soru kaynağı hâlâ deneme adıyla eşleştiği için yeni
   yinelenen adlar ve kataloğa sığmayan yeni adlar engellendi. Önceden yinelenmiş adlar
   silinmez; ortak ismi yeniden adlandırmadan önce kayıtlar kontrol edilmelidir.
4. **Hatalı veri ve yanıltıcı sayaç:** Büyük tamsayılarda taşarak geçerli görünen yanlış skor
   reddedilir; eski kayıtlarda dersler arası büyük toplamlar taşmadan gösterilir. Soru akışı
   okunamazsa sıfır yüklenmiş gibi gösterilmez; hata belirtilir ve
   fotoğraf düğmesi bilinmeyen bir kalan sayı uydurmaz. Deneme kaydetme hatası kullanıcıya
   gösterilir.
5. **Çalışma hiyerarşisi:** Odak ekranında soru ambiyans ayarının önüne alındı; görseli olmayan
   soruda yakınlaştırma ipucu kaldırıldı. Değerlendirme seçenekleri eşit, seçilebilir kartlar ve
   gerçek tekrar önizlemeleriyle sunuluyor. Henüz seçilmemiş “Rahat” sonucu için karar
   açıklaması gösterilmiyor. Tamamlanan çubuğu atlanan soru sayısını tamamlanmış saymıyor.

6. **Bozuk deneme verisi ve yedekleme:** Okunamayan, bilinmeyen alan/kural veya geçersiz
   kayıt içeren deneme verisi artık boş ve yazılabilir sanılmıyor. Yeni kayıt/silme ve normal
   dışa aktarma durur; ekranda yedekten geri yükleme ve açık onaylı sıfırlama sunulur. Sağlam
   bir yedekten geri yüklerken ham veri türüyle birlikte güvenlik ZIP'inde saklanır. Bu özel
   ZIP geri yüklenirse ham veriyi aynı hâliyle geri getirir; işlem günlüğü kesinti sonrasında
   tekrar oynatılabilir. Sıfırlama seçilirse ham kopya yalnız cihazda tutulur ve normal ZIP'e
   dahil edilmez; soru, fotoğraf ve öğretmen verisine dokunulmaz. Tam yedek geri yükleme ise
   önceki davranışındaki gibi tüm arşivi seçilen yedekle değiştirir.

## Açık riskler ve öneriler

- **Bozuk veri otomatik onarılmaz:** Ham veriyi korumak kayıtları onarmak demek değildir;
  kullanılabilir denemeler için sağlam bir yedek gerekir. Ham kurtarma verisi 4 MiB sınırını
  aşarsa güvenlik kopyası oluşturulamaz ve geri yükleme mevcut verileri değiştirmeden durur.
  Kullanıcı onaylı sıfırlamada cihazdaki ham kopya uygulama kaldırılırsa kaybolabilir.
- **Çapraz-depo işlem sınırı:** Deneme adı SharedPreferences'ta, soru kaynakları Room'da
  tutuluyor. Yeniden adlandırma sırasında iki depodan biri başarısız olursa tam atomiklik
  garanti edilemiyor. Kalıcı deneme kimliğiyle ilişkilendirme ve veri/ZIP göçü ayrı bir iş
  olarak, eski kayıtlar ve öğretmen verileri korunarak ele alınmalı.
- **Daha doğru gruplama:** Aynı ders/soru sayısı, farklı sınav türlerinin veya zorlukların
  gerçekte eşdeğer olduğunu kanıtlamaz. İhtiyaç doğrulanırsa isteğe bağlı sınav türü/seri
  alanı eklenebilir; eski kayıtlara tahminde bulunulmadan göç yapılmalıdır.
- **Fiziksel cihaz kabulü:** Deneme seçimleri, negatif net, büyük yazı, koyu tema, TalkBack,
  dar/yatay ekran, kamera, süreç öldürme ve gerçek SAF/Drive yedek turu gerçek cihazda ayrıca
  denenmelidir. Emülatör/host testi cihaz kabulünün yerine geçmez.

## Doğrulama

- Deneme hesabı ve sınırları için 9 saf Kotlin/JUnit testi yerel Kotlin 2.4.0/JDK 17
  ortamında başarıyla çalıştırıldı.
- Puanlama/kapsam seçimi, grafik açıklaması, soru sayısı hata durumu ve odak ekranı için
  Robolectric/Compose testleri eklendi. Bozuk veri/yazma engeli, sıfırlama, eski yedekler,
  ham veri güvenlik ZIP'i, geri yükleme günlüğü ve ortak yedekte öğretmen verisinin korunması
  için yeni testler de eklendi. PR'daki en güncel Android CI sonucu esas alınmalıdır.
  Fiziksel cihaz, gerçek kamera/SAF/Drive ve TalkBack kabulü henüz yapılmadı.
