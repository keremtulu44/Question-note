# CHANGELOG — Yanlış Defteri

Bu dosya **kullanıcıya yansıyan** değişiklikleri içerir. Henüz yayımlanmış bir sürüm yoktur.

## [Yayımlanmadı]

### Belgeler — 2026-10-16

- Proje belgeleri oluşturuldu: README, PROJECT_BRIEF, ROADMAP, HANDOFF, BUILDING, CHANGELOG.
- Ayrıntılı belgeler: veri modeli, yedek formatı, test kontrol listesi (`docs/`).
- Kesinleşen kararlar kayda geçirildi (7 ders, isteğe bağlı konu, çok görsel desteği, çözümün gizli
  tutulması, 3 seçenekli tekrar değerlendirmesi, ZIP yedeği, sabit imzalama anahtarı).
- Uygulama kodu, derleme yapılandırması veya workflow **eklenmedi**; kurulabilir sürüm yok.

### Dış inceleme sonrası belge güncellemeleri — 2026-10-16

- Sorunun **plan durumu** (aktif / askıda / tamamlandı) öğrenme durumundan ayrıldı; bir soruyu tekrar
  planından geçici olarak çıkarmak veya bitirmek mümkün olacak (kayıt silinmez).
- Çözüm açılarak yapılan tekrar **bağımsız sayılmayacak**; aralık en fazla 1–3 gün uzayacak ve
  istenirse "çözümü kapat ve tekrar dene" ile aynı gün ikinci bir bağımsız deneme kaydedilebilecek.
- Uzun bir aradan sonra hiçbir soru otomatik sıfırlanmayacak.
- Tekrar aralığı kuralı sürümlü hâle getirildi; ileride algoritma değişse bile geçmiş korunacak.
- Geri yüklemede **kesintiye dayanıklılık** (işlem sürerken uygulama kapanırsa sonraki açılışta
  tamamlama/geri alma) ve **zip bomb / yol kaçışı korumaları** eklendi.
- Şema değişiklikleri için **otomatik migration testleri** plana alındı.
- Release çıktısına sürüm, commit kimliği ve APK SHA-256 bilgisi eklendi.
- Reddedilen öneriler ve gerekçeleri [HANDOFF.md](HANDOFF.md) içinde kayda geçti.
