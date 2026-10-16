# İmzalama Prosedürü

> Bu belge **parmak izini** içerir, anahtarı veya parolayı **içermez**. Anahtar ve parola yalnızca
> GitHub Secret'larında ve kullanıcının kendi yedeklerinde bulunur.

- Son güncelleme: 2026-10-16
- Durum: **Anahtar üretildi** (2026-10-16). Secret'ların eklenmesi ve ilk imzalı derlemenin
  doğrulanması bekleniyor.

---

## 1. Değerlerin nerede durduğu

| Değer | Nerede |
|---|---|
| Keystore dosyası (`.p12`) | GitHub Secret `SIGNING_KEYSTORE_BASE64` (base64) + kullanıcının 2 bağımsız yedeği |
| Keystore **ve anahtar** parolası | GitHub Secret `SIGNING_PASSWORD` + kullanıcının 2 bağımsız yedeği |
| Anahtar takma adı (alias) | **Secret değil**; gizli bilgi olmadığı için workflow içinde sabit: `yanlisdefteri` |
| Sertifika SHA-256 parmak izi | **Bu belge** (gizli değildir) |

Yalnızca **2 secret** gerekir. Secret'ları yalnızca kullanıcı ekleyebilir (Settings → Secrets and
variables → Actions); workflow kendi secret'ını oluşturamaz. Adımlar git dışı
`anahtar/KURULUM_ADIMLARI.md` dosyasında.

## 2. Sertifika parmak izi

```
SHA-256 (sertifika): FA:13:DE:D7:3C:B4:6E:DE:9E:36:37:47:EE:38:95:F2:B6:37:14:9C:16:57:90:A2:E4:67:78:F1:30:89:DE:54
Anahtar takma adı   : yanlisdefteri
Algoritma           : RSA 2048
Geçerlilik          : 30 yıl (2056-10-08'e kadar)
Anahtar türü        : PKCS12 (PBE-SHA1-3DES, evrensel JDK uyumluluğu için)
Base64 uzunluğu     : 3404 karakter
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
