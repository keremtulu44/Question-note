# Öğrenci akışı incelemesi — 2026-10-28

Bu inceleme deneme takibi ve öğrencinin soru çözme/öz değerlendirme akışına odaklanır.
Öğretmen çalışma alanı, verileri ve testleri değiştirilmedi. Room şeması, ZIP biçimi,
tekrar algoritması ve XP kuralları değiştirilmedi.

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
   reddedilir. Soru akışı okunamazsa sıfır yüklenmiş gibi gösterilmez; hata belirtilir ve
   fotoğraf düğmesi bilinmeyen bir kalan sayı uydurmaz. Deneme kaydetme hatası kullanıcıya
   gösterilir.
5. **Çalışma hiyerarşisi:** Odak ekranında soru ambiyans ayarının önüne alındı; görseli olmayan
   soruda yakınlaştırma ipucu kaldırıldı. Değerlendirme seçenekleri eşit, seçilebilir kartlar ve
   gerçek tekrar önizlemeleriyle sunuluyor. Henüz seçilmemiş “Rahat” sonucu için karar
   açıklaması gösterilmiyor. Tamamlanan çubuğu atlanan soru sayısını tamamlanmış saymıyor.

## Açık riskler ve öneriler

- **Öncelikli veri güvenliği:** `DenemeDeposu.oku()` bozuk JSON'u boş listeye çeviriyor;
  ardından yeni kayıt yazılması bozuk ham verinin üzerine yazabilir. Bir yedek de bu kayıtları
  eksik alabilir. Bu turda kurtarma/ZIP davranışı değiştirilmedi: güvenli çözüm, ham veriyi
  koruyan hata durumu, yazma engeli ve hem yedek alma hem geri yüklemede açık kurtarma
  senaryolarının birlikte tasarlanıp test edilmesidir. Böyle bir durumdan şüphelenildiğinde
  yeni deneme kaydetmeden mevcut yedekler korunmalıdır.
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

- Yeni deneme hesabı sınırları için 8 saf Kotlin/JUnit testi eklendi veya güncellendi; bunlar
  yerel Kotlin 2.4.0/JDK 17 ortamında çalıştırıldı.
- Puanlama/kapsam seçimi, grafik açıklaması, soru sayısı hata durumu ve odak ekranı için
  Robolectric/Compose testleri eklendi. Bunların sonucu Android test ortamında ayrıca
  doğrulanmalıdır; kaynakta bulunmaları geçmiş CI sonucunu bu değişikliklerin kanıtı yapmaz.
