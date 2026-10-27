# P6 sentetik analitik ölçümü · release

Kaynak: `e51ab43352238f5ded48c85e6155b50b5cf2e8e5`

Robolectric 4.15.1 / SDK 35 / yerel SQLite / OpenJDK 64-Bit Server VM 17.0.21 / Linux amd64

1.000 soru, 20.000 tekrar ve bağlam/ölçüm; 4 ders, 40 konu, 100 ders/kaynak grubu, 20 gün; tüm sonuç/kalite türleri. Veri yalnız testte üretildi, Git'e yazılmadı.

Analiz başına **4 uygulama SELECT**; karar JSON'u taşınmadı (veritabanındaki toplam karar yükü: 22749680 bayt). Room iç invalidation sorguları bu sayıya dahil değildir.

| İşlem | Örnek | medyan ms | p95 ms* | ham ms |
|---|---:|---:|---:|---|
| Sentetik veri üretimi + transaction yükleme | 1 | 1157.66 | 1157.66 | 1157.66 |
| İlk analiz çağrısı (DB kurulumu hariç) | 1 | 530.16 | 530.16 | 530.16 |
| Isınmış toplu analiz (5 tekrar) | 5 | 313.19 | 404.15 | 364.67, 404.15, 313.19, 292.42, 273.58 |
| Tek ders profili (ince projeksiyon) | 1 | 26.15 | 26.15 | 26.15 |
| Tüm ders süre kapsamı | 1 | 210.98 | 210.98 | 210.98 |
| Tam İlerleme özeti (XP + profil + analiz) | 1 | 484.10 | 484.10 | 484.10 |

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
