# Yedek Formatı (v1 taslağı)

> Aşama 3'te uygulanacak yedek yapısının taslağıdır. Son güncelleme: 2026-10-16 (dış inceleme
> notları işlendi)

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
- Yedek, **tam** yedektir: dersler, konular, sorular, soru ve çözüm görselleri, notlar, plan durumu,
  tekrar planı ve tekrar geçmişi dâhildir.
- Yedek **hiçbir cihaz kimliğine, dosya yoluna veya URI'ye bağlı değildir**; yeni telefonda aynı
  uygulama sürümü yedeği eksiksiz geri yükleyebilir.
- Veriler ayrı JSON dosyalarında tutulur: büyük arşivde tek tek incelenebilir, iki yedek
  karşılaştırılabilir ve tek bir dosya bozulsa bile kalanı okunabilir kalır. (Tek `data.json`
  alternatifi değerlendirildi; bu avantajlar nedeniyle ayrı dosyalar tercih edildi.)
- Önizleme/thumbnail dosyaları yedeğe **konmaz** (önbellekte, asıl görselden yeniden üretilebilir).
- CSV yalnızca başka programlarda inceleme içindir; tam veri aktarımı JSON iledir.
- PDF gibi ek çıktılar ileride eklenebilir ama **tam yedek yerine geçmez**.

## 2. `manifest.json` alanları (taslak)

| Alan | Açıklama |
|---|---|
| `yedek_format_surumu` | Ör. `1`. Dosya yapısı değiştiğinde artar. **Uygulama sürümünden bağımsızdır.** |
| `uygulama_surumu` | `versionName` |
| `uygulama_surum_kodu` | `versionCode` |
| `olusturma_zamani` | ISO 8601 |
| `veritabani_sema_surumu` | Room şema sürümü |
| `sayilar` | ders, konu, soru, görsel, tekrar kayıt sayıları |
| `dosyalar` | Her dosya için `ad` (göreli yol), `boyut`, `sha256` |
| `toplam_boyut` | Sıkıştırılmamış toplam bayt |

## 3. Geri yükleme kuralları

1. **Doğrulama:** ZIP okunabilirliği; `manifest.json` varlığı ve okunabilirliği; format sürümü
   destekleniyor mu; her dosyanın `sha256` ve boyut kontrolü; JSON içeriklerinin ayrıştırılması;
   ilişkilerin tutarlılığı (her görselin bir sorusu var mı, `konu_id` değerleri var olan konulara
   mı işaret ediyor).
2. **Anlamsız/boş yedek koruması:** Yedekteki kayıt sayıları bildirilir ve mevcut arşivle
   karşılaştırılır. Kullanıcı **onaylamadan** hiçbir şey değiştirilmez.
3. **Boş alan ve boyut kontrolü:** Hedef için yeterli boş alan yoksa işlem başlatılmaz.
4. **Geçici dizine açma:** İçerik önce geçici dizine çıkarılır; mevcut arşive hiç dokunulmaz.
5. **Yeni arşivi hazırlama ve bütünlük kontrolü:** Veritabanı dosyası geçici alanda oluşturulur ve
   açılabildiği, tabloların beklendiği gibi olduğu doğrulanır.
6. **Güvenlik kopyası:** Mevcut veritabanı ve görseller, `yanlis-defteri-guvenlik-yedegi-<tarih>.zip`
   olarak otomatik yedeklenir (kullanıcı onayı olmadan silinmez).
7. **Değiştirme:** Yalnızca **"tamamen değiştir"** desteklenir. Yeni içerik yerleştirilir, veritabanı
   yeni veriyle açılır ve sonuç doğrulanır (soru/görsel sayıları).
8. **Hata durumu:** Herhangi bir adım başarısız olursa güvenlik kopyasından geri dönülür; mevcut
   çalışan arşiv korunur ve kullanıcıya anlaşılır hata mesajı gösterilir.

### 3.1 Kesintiye dayanıklılık (uygulama işlem sırasında kapanırsa)

- Geri yükleme başlamadan önce küçük bir **"işlem sürüyor" işaret dosyası** yazılır (seçilen yedek,
  hangi aşamada olunduğu).
- Uygulama işlem sırasında kapatılır veya öldürülürse, **bir sonraki açılışta** işlem ya
  tamamlanır ya da güvenlik kopyasından geri alınır. Kullanıcıya ne olduğu açıkça bildirilir.
- Yarım kalmış bir arşiv hiçbir zaman "etkin" hâle getirilmez.

### 3.2 Bozuk veya kötü niyetli ZIP korumaları

- Mutlak yol ve `..` içeren ZIP girdileri reddedilir (yol kaçışı).
- Dosya sayısı, tek dosya boyutu ve **toplam açılmış boyut** için üst sınır uygulanır.
- Sıkıştırma oranı anormal olan arşivler (zip bomb) açılmadan reddedilir.
- Beklenmeyen dosya türleri (görsel değil, JSON değil) yok sayılmaz; rapor edilir.
- Desteklenmeyen **daha yeni** format sürümü, anlaşılır mesajla reddedilir.

**v1'de desteklenmeyen:** "Birleştir" tipi geri yükleme. Kimlik çakışması ve yinelenen kayıt tasarımı
gerektirdiği için sonraya bırakıldı.

**Politika:** Geri yükleme, bozuk/eksik/desteklenmeyen yedekleri **açık hata mesajıyla reddeder**.
Hiçbir koşulda mevcut veriler sessizce silinmez.

## 4. Yedek üretme kuralları

- Önce **yeni** yedek başarıyla tamamlanır ve doğrulanır; ancak ondan sonra eski geçici/artık yedekler
  silinebilir.
- Yeterli boş alan yoksa işlem başlatılmaz, kullanıcı bilgilendirilir.
- Yedek yazılamaz veya doğrulanamazsa işlem **başarısız** olarak raporlanır; arayüz asla
  "yedeklendi" demez.
- Yedek, kullanıcının seçtiği konuma yazılır: cihazdaki bir klasör veya Android dosya seçicisindeki
  bir sağlayıcı (ör. Drive).
- **Son yedek bilgisi** saklanır ve gösterilir: tarih, dosya boyutu, dosya adı, kaydedilen konum.

## 5. Drive ile ilgili açık ayrım

Android dosya seçicisi üzerinden bir belge sağlayıcısına yazmak, **buluta yüklemeyi tamamlamış olmak
anlamına gelmez.** Sağlayıcı yüklemeyi arka planda yapar ve sonucu bildirmez.

Arayüz üç durumu karıştırmaz:

| Durum | Gösterilebilir mi? |
|---|---|
| **Yedek oluşturuldu** (ZIP başarıyla hazırlandı) | Evet |
| **Konuma kaydedildi** (sağlayıcı yazmayı tamamladı) | Evet |
| **Bulutta doğrulandı** (yükleme gerçekten tamamlandı) | Hayır — uygulama bunu doğrulayamaz |

Önerilen metin: *"Yedek seçilen konuma kaydedildi. Bulut yüklemesinin tamamlandığını ayrıca kontrol
edin."*

- **Öneri:** Yedeği önce cihazdaki İndirilenler klasörüne yazmak; böylece her yedeğin garantili yerel
  bir kopyası bulunur. Drive'a kopyalama kullanıcının tercihine bırakılır.
- Çoklu cihaz eşzamanlı düzenleme ve çakışma çözümü kapsam dışıdır. Drive bir senkronizasyon sistemi
  değildir.

## 6. Sürümleme ve eski yedekler

| Yedek format sürümü | Okuma politikası |
|---|---|
| 1 | Geçerli sürüm |
| Gelecek sürümler | Yeni alanlar eklendiğinde sürüm artırılır; uygulama **eski** sürümleri okumayı destekler, kendi sürümünden yeni olan yedekleri anlaşılır hata ile reddeder |

Eski yedekleri okuma politikası her format değişikliğinde bu belgeye işlenir. Yedek format sürümü
uygulama sürümünden bağımsız yürür: uygulama 1.5 aynı formatı kullanabilir; 3.0 sürümü eski formatı
okuyabilir.

## 7. Şifreleme

v1'de **yoktur**. Eklenirse:

- Yedek, üçüncü parti programlarda incelenemez hâle gelir (CSV'nin amacına ters).
- Parola kaybı = yedek kaybı.
- Kendi kripto kodu yazılmaz; olgun bir araç/format kullanılır.

Karar kullanıcıya bırakılmıştır (bkz. PROJECT_BRIEF §8, A5).

## 8. Gizlilik uyarısı

Yedek; kişisel notlar, çözüm açıklamaları ve soru fotoğraflarını içerir. Yedeğin saklandığı yerin
gizliliği kullanıcının sorumluluğundadır; arayüz bu konuda kısa bir uyarı gösterir. Uygulama
kaldırılırsa yerel veri kaybolur; **yedek, cihaz değiştirmenin ve veri kaybına karşı korunmanın ana
yoludur.**
