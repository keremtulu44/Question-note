# Yedek Formatı (v1 taslağı)

> Aşama 3'te uygulanacak yedek yapısının taslağıdır. Son güncelleme: 2026-10-16

---

## 1. Dosya yapısı

```
yanlis-defteri-yedek-YYYYMMDD-HHMM.zip
├── manifest.json
├── dersler.json
├── konular.json
├── sorular.json
├── tekrarlar.json
├── sorular.csv              (yalnızca okuma/inceleme kolaylığı; geri yüklemede kullanılmaz)
└── gorseller/
    ├── <uuid>.jpg
    └── ...
```

- `gorseller/` içindeki dosya adları veritabanındaki `dosya_adi` alanıyla **birebir** eşleşir.
- Yedek, **tam** yedektir: dersler, konular, sorular, soru ve çözüm görselleri, notlar, tekrar planı ve
  tekrar geçmişi dâhildir.
- CSV yalnızca başka programlarda inceleme içindir; tam veri aktarımı JSON iledir.
- PDF gibi ek çıktılar ileride eklenebilir ama **tam yedek yerine geçmez**.

## 2. `manifest.json` alanları (taslak)

| Alan | Açıklama |
|---|---|
| `yedek_format_surumu` | Ör. `1`. Dosya yapısı değiştiğinde artar |
| `uygulama_surumu` | `versionName` |
| `uygulama_surum_kodu` | `versionCode` |
| `olusturma_zamani` | ISO 8601 |
| `veritabani_sema_surumu` | Room şema sürümü |
| `sayilar` | ders, konu, soru, görsel, tekrar kayıt sayıları |
| `dosyalar` | Her dosya için `ad`, `boyut`, `sha256` |
| `toplam_boyut` | Sıkıştırılmamış toplam bayt |
| `icerik_ozeti` | Sorular ve görseller üzerinden hesaplanan özet (isteğe bağlı) |

## 3. Geri yükleme kuralları

1. **Doğrulama:** ZIP açılmadan önce okunabilirlik; `manifest.json` varlığı ve okunabilirliği;
   format sürümü destekleniyor mu; her dosyanın `sha256` ve boyut kontrolü; ZIP içi yolların güvenli
   olması (mutlak yol yok, `..` yok, yalnızca beklenen klasör ve dosyalar).
2. **Boş alan ve boyut kontrolü:** Hedef için yeterli boş alan yoksa işlem başlatılmaz.
3. **Geçici dizine açma:** İçerik önce geçici dizine çıkarılır; mevcut arşive hiç dokunulmaz.
4. **Önizleme özeti:** Kullanıcıya "bu yedeği geri yüklersen şunlar gelecek: N soru, M görsel,
   tarih" bilgisi ve "mevcut kayıtların (X soru) yerini alacak" uyarısı gösterilir.
5. **Güvenlik kopyası:** Mevcut veritabanı ve görseller, `yanlis-defteri-guvenlik-yedegi-<tarih>.zip`
   olarak otomatik yedeklenir (kullanıcı onayı olmadan silinmez).
6. **Değiştirme:** Yalnızca **"tamamen değiştir"** desteklenir. Yeni içerik yerleştirilir, veritabanı
   yeni veriyle açılır.
7. **Hata durumu:** Herhangi bir adım başarısız olursa güvenlik kopyasından geri dönülür; mevcut
   çalışan arşiv korunur. Kullanıcıya anlaşılır hata mesajı gösterilir.

**v1'de desteklenmeyen:** "Birleştir" tipi geri yükleme. Kimlik çakışması ve yinelenen kayıt
tasarımı gerektirdiği için sonraya bırakıldı.

**Politika:** Geri yükleme, bozuk/eksik/desteklenmeyen sürümlü yedekleri **açık hata mesajıyla
reddeder**. Hiçbir koşulda mevcut veriler sessizce silinmez.

## 4. Yedek üretme kuralları

- Önce **yeni** yedek başarıyla tamamlanır; ancak ondan sonra eski geçici/artık yedekler silinebilir.
- Yeterli boş alan yoksa işlem başlatılmaz, kullanıcı bilgilendirilir.
- Yedek yazılamaz veya doğrulanamazsa işlem **başarısız** olarak raporlanır; arayüz asla
  "yedeklendi" demez.
- Yedek, kullanıcının seçtiği konuma yazılır: cihazdaki bir klasör veya Android dosya seçicisindeki
  bir sağlayıcı (ör. Drive).

## 5. Drive ile ilgili açık ayrım

Android dosya seçicisi üzerinden bir belge sağlayıcısına yazmak, **buluta yüklemeyi tamamlamış olmak
anlamına gelmez.** Sağlayıcı yüklemeyi arka planda yapar ve sonucu bildirmez.

- Arayüz hiçbir yerde doğrulanamayan bir "Drive'a yüklendi" mesajı göstermez.
- Bunun yerine: "Seçtiğin konuma kaydedildi" ifadesi kullanılır.
- **Öneri:** Yedeği önce cihazdaki İndirilenler klasörüne yazmak, böylece her yedeğin garantili yerel
  bir kopyasının bulunmasını sağlamak. Drive'a kopyalama kullanıcının tercihine bırakılır.
- Çoklu cihaz eşzamanlı düzenleme ve çakışma çözümü kapsam dışıdır. Drive bir senkronizasyon sistemi
  değildir.

## 6. Sürümleme ve eski yedekler

| Yedek format sürümü | Okuma politikası |
|---|---|
| 1 | Geçerli sürüm |
| Gelecek sürümler | Yeni alanlar eklendiğinde sürüm artırılır; uygulama **eski** sürümleri okumayı destekler, kendi sürümünden yeni olan yedekleri anlaşılır hata ile reddeder |

Eski yedekleri okuma politikası her format değişikliğinde bu belgeye işlenir.

## 7. Şifreleme

v1'de **yoktur**. Eklenirse:

- Yedek, üçüncü parti programlarda incelenemez hâle gelir (CSV'nin amacına ters).
- Parola kaybı = yedek kaybı.
- Kendi kripto kodu yazılmaz; olgun bir araç/format kullanılır.

Karar kullanıcıya bırakılmıştır (bkz. PROJECT_BRIEF §8, A5).

## 8. Gizlilik uyarısı

Yedek; kişisel notlar, çözüm açıklamaları ve soru fotoğraflarını içerir. Yedeğin saklandığı yerin
gizliliği kullanıcının sorumluluğundadır; arayüz bu konuda kısa bir uyarı gösterir.
