# İmzalama Prosedürü

> Bu belge **parmak izini** içerir, anahtarı veya parolayı **içermez**. Anahtar ve parola yalnızca
> GitHub Secret'larında ve kullanıcının kendi yedeklerinde bulunur.

- Son güncelleme: 2026-10-16
- Durum: **Anahtar henüz üretilmedi.** Aşama 1, G3 görevi sırasında doldurulacak.

---

## 1. Değerlerin nerede durduğu

| Değer | Nerede |
|---|---|
| Keystore dosyası (`.p12`) | GitHub Secret `SIGNING_KEYSTORE_BASE64` (base64) + kullanıcının 2 bağımsız yedeği |
| Keystore parolası | GitHub Secret `SIGNING_STORE_PASSWORD` + kullanıcının 2 bağımsız yedeği |
| Anahtar takma adı (alias) | GitHub Secret `SIGNING_KEY_ALIAS` (gizli değil, `yanlisdefteri`) |
| Anahtar parolası | GitHub Secret `SIGNING_KEY_PASSWORD` |
| Sertifika SHA-256 parmak izi | **Bu belge** (gizli değildir) |

## 2. Sertifika parmak izi

```
SHA-256 (sertifika): <Aşama 1, G3'te doldurulacak>
Anahtar takma adı   : yanlisdefteri
Algoritma           : RSA 4096
Geçerlilik          : 30 yıl
Anahtar türü        : PKCS12
```

## 3. CI doğrulaması

Her release derlemesinde:

1. Keystore, secret'tan çözülür.
2. `apksigner verify --print-certs app-release.apk` çalıştırılır.
3. Çıktıdaki sertifika SHA-256 parmak izi, yukarıdaki değerle **karşılaştırılır**.
4. Eşleşmezse **derleme başarısız sayılır** ve Release oluşturulmaz.

Bu kontrol olmadan, yanlış (veya üretilmiş) bir anahtarla imzalanmış bir APK sessizce yayımlanabilir;
kullanıcı da üzerine kuramayıp uygulamayı kaldırmak zorunda kalırdı ki bu yerel veriyi siler.

## 4. Kullanıcı kontrol listesi (anahtar üretildiğinde)

- [ ] Anahtar dosyası indirildi ve **şifre yöneticisine** eklendi
- [ ] Aynı dosya **Drive'da** ayrı bir klasöre (yalnızca hesap sahibinin erişebildiği) kaydedildi
- [ ] Parola iki yerde de kayıtlı (şifre yöneticisi + Drive notu)
- [ ] Anahtarın 30 yıl geçerli olduğu ve uygulama kimliğiyle birlikte belgelendiği doğrulandı
- [ ] Parmak izi bu belgedeki değerle aynı

## 5. Anahtar kaybedilirse ne olur?

- Yeni APK'lar mevcut kurulumun üzerine **kurulamaz** (Android imza uyuşmazlığını reddeder).
- Tek çare uygulamayı kaldırıp yeniden kurmaktır → **telefondaki yerel veri silinir.**
- Bu nedenle güncel bir ZIP yedeği ve iki bağımsız anahtar kopyası zorunludur.

## 6. Debug ve release ayrımı

- Günlük kullanımda **her zaman** sabit anahtarla imzalanmış release APK kullanılır.
- CI'da üretilen debug/test çıktıları yalnızca geliştirme içindir; gerçek arşivin olduğu telefonun
  üzerine kurulmaz.
