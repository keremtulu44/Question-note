package com.keremtulu.questionnote.domain.calisma

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/** Calendar clock only. Unlike a cached systemDefaultZone clock, it observes device zone changes. */
object YerelTakvimSaati : Clock() {
    override fun getZone(): ZoneId = ZoneId.systemDefault()
    override fun withZone(zone: ZoneId): Clock = Clock.system(zone)
    override fun instant(): Instant = Instant.now()
}
