# Proje çalışma kuralları

## Ürün odağı: öğrenci. Öğretmen modu ASKIDA / GİZLİ

Kullanıcının güncel kararı (2026-10-27): **Öğretmen tarafını gizle ve geliştirmeyi
askıya al.** Bu karar, eski belgelerdeki öğretmen geliştirme planlarının ve
"öğretmen moduna geçişi koru" notlarının yerini alır.

Sonraki oturumlarda:
- Kullanıcı açıkça yeniden istemedikçe öğretmen çalışma alanına özellik, görsel
  yenileme, refaktör veya başka ürün geliştirmesi yapma.
- Modu tekrar açma; ayarlara veya kuruluma öğretmen girişini geri ekleme.
- `UrunKapsami.OGRETMEN_MODU_GORUNUR = false` kararını koru. Bu bir gizli dokunma,
  şifre veya debug menüsüyle açılabilen özellik değildir.
- Öğretmen kodunu, Room tablolarını, görsellerini ve yedek desteğini silme.
  Gizlemek veri silmek değildir. Kayıtlı öğretmen rolünü/verisini değiştirme;
  uygulama görünür yönlendirmede öğrenci rolünü kullanır.
- Mevcut öğretmen testlerini kaldırma. Ortak altyapı değişikliklerinde eski
  verinin okunması, yedeklenmesi ve geri yüklenmesi korunmalı.
- Öğretmene özgü zorunlu bir güvenlik/veri kaybı düzeltmesi gerekirse, bunu yeni
  özellik geliştirmesiyle karıştırma; kapsamı ve gerekçeyi kullanıcıya açıkla.

**İstisna / ayrım:** Öğrencinin “öğretmene sorulacaklar”, “çözdürdüm” ve öğrenci
tekrar planına dönüş akışları aktif öğrenci ürünüdür. Bunları öğretmen moduyla
birlikte gizleme veya durdurma. Öğrenci tarafı geliştirilmeye devam eder.

İlk okunacak belgeler: bu dosya, `HANDOFF.md`, `docs/DERLEME_RAPORU.md`.
