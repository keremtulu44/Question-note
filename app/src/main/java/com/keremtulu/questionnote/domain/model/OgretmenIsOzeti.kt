package com.keremtulu.questionnote.domain.model

/** Workload, not student achievement. Disjoint date buckets include only waiting questions. */
data class OgretmenIsOzeti(val geciken: Int, val bugun: Int, val ileri: Int, val plansiz: Int) {
    val toplam: Int get() = geciken + bugun + ileri + plansiz

    companion object {
        fun hesapla(sorular: List<OgretmenSorusu>, gun: Long): OgretmenIsOzeti {
            var geciken = 0; var bugun = 0; var ileri = 0; var plansiz = 0
            sorular.filter { it.kuyrukta }.forEach { soru ->
                val plan = soru.planGun
                when {
                    plan == null -> plansiz++
                    plan < gun -> geciken++
                    plan == gun -> bugun++
                    else -> ileri++
                }
            }
            return OgretmenIsOzeti(geciken, bugun, ileri, plansiz)
        }
    }
}
