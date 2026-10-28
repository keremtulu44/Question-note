# P6 sentetik analitik ölçümü · release

Kaynak: `6085996c1114e3a4a6f1635491136b5a77d4cd2a`

Robolectric 4.15.1 / SDK 35 / yerel SQLite / OpenJDK 64-Bit Server VM 17.0.20.1 / Linux amd64

1.000 soru, 20.000 tekrar ve bağlam/ölçüm; 4 ders, 40 konu, 100 ders/kaynak grubu, 20 gün; tüm sonuç/kalite türleri. Veri yalnız testte üretildi, Git'e yazılmadı.

Analiz başına **4 uygulama SELECT**; karar JSON'u taşınmadı (veritabanındaki toplam karar yükü: 22749680 bayt). Room iç invalidation sorguları bu sayıya dahil değildir.

| İşlem | Örnek | medyan ms | p95 ms* | ham ms |
|---|---:|---:|---:|---|
| Sentetik veri üretimi + transaction yükleme | 1 | 1311.17 | 1311.17 | 1311.17 |
| İlk analiz çağrısı (DB kurulumu hariç) | 1 | 579.99 | 579.99 | 579.99 |
| Isınmış toplu analiz (5 tekrar) | 5 | 342.37 | 434.65 | 434.65, 420.35, 342.37, 332.26, 317.48 |
| Tek ders profili (ince projeksiyon) | 1 | 27.95 | 27.95 | 27.95 |
| Tüm ders süre kapsamı | 1 | 244.60 | 244.60 | 244.60 |
| Tam İlerleme özeti (XP + profil + analiz) | 1 | 619.02 | 619.02 | 619.02 |

*Az sayıda host örneğinin nearest-rank yüzdeliği; kare/frame p95 değildir. İlk çağrı gerçek telefon soğuk açılışı değildir. GC/CI paylaşımı sonuçları etkileyebilir; bu sayılar cihaz performansı veya S24 FE kabulü diye yorumlanamaz.

## Toplu tarihsel analiz

```text
SCAN t USING INDEX index_tekrarlar_gun
SEARCH m USING INDEX sqlite_autoindex_tekrar_olcumleri_1 (tekrar_id=?) LEFT-JOIN
USE TEMP B-TREE FOR RIGHT PART OF ORDER BY
```

## Ders/pencere profili

```text
SEARCH m USING INDEX index_tekrar_olcumleri_ders_id_kaynak_anahtari (ders_id=?)
SEARCH t USING INDEX sqlite_autoindex_tekrarlar_1 (id=?)
USE TEMP B-TREE FOR ORDER BY
```

## Uygun çalışma + gerçek aynı-gün dışlama

```text
SEARCH sorular USING INDEX index_sorular_plan_durumu (plan_durumu=?)
CORRELATED SCALAR SUBQUERY 1
SEARCH t USING COVERING INDEX index_tekrarlar_soru_id_gun (soru_id=? AND gun=?)
SEARCH dersler USING INDEX sqlite_autoindex_dersler_1 (id=?)
USE TEMP B-TREE FOR ORDER BY
```

Plan okuması: tüm-arşiv analizinde tarama beklenir; PK ile bağlam eşleşir. Ders/pencere ve aynı-gün kontrolünün mevcut indeksleri EXPLAIN çıktısında gösterilir. Yeni bir şema/indeks gereksinimi cihaz ölçümüyle yeniden değerlendirilecek.

Bekleyen fiziksel kapılar: S24 FE ve düşük güçlü cihazda frame-p95/jank, TalkBack, gerçek süreç ölümü, SAF/Drive ve düşük depolama. **Etkin v3 kapalı.**
