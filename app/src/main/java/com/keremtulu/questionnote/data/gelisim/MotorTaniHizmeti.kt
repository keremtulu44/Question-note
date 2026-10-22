package com.keremtulu.questionnote.data.gelisim

import androidx.room.withTransaction
import com.keremtulu.questionnote.data.local.YanlisDefteriDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class MotorTanisi(val toplamKarar: Int, val incelenen: Int, val v2: Int, val golge: Int,
    val farkliAday: Int, val etkin: Int, val gecersiz: Int)

/** Bounded, explicit settings refresh: at most 100 snapshots, not every analytics row. No learning-benefit claim. */
class MotorTaniHizmeti(private val db: YanlisDefteriDatabase) {
    suspend fun oku(): MotorTanisi = withContext(Dispatchers.IO) {
        db.withTransaction {
            val toplam = db.tekrarOlcumDao().toplamKararSayisi()
            val rows = db.tekrarOlcumDao().sonKararlar(100)
            var v2 = 0; var golge = 0; var fark = 0; var etkin = 0; var gecersiz = 0
            rows.forEach { row ->
                val m = row.olcum
                val j = runCatching {
                    requireNotNull(m)
                    OlcumSerilestirme.dogrula(m, row.tekrar)
                    JSONObject(m.kararJson)
                }.getOrNull()
                if (j == null) gecersiz++ else when (j.getString("mode")) {
                    "V2" -> v2++
                    "GOLGE" -> {
                        golge++
                        if (j.getInt("candidate_interval") != j.getInt("base_interval")) fark++
                    }
                    "ETKIN" -> etkin++
                    else -> gecersiz++
                }
            }
            MotorTanisi(toplam, rows.size, v2, golge, fark, etkin, gecersiz)
        }
    }
}
