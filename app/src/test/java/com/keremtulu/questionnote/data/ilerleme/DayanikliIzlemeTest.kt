package com.keremtulu.questionnote.data.ilerleme

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class DayanikliIzlemeTest {
    @Test fun transientFailuresResubscribeAndPublishSuccess() = runBlocking {
        var deneme = 0
        val beklemeler = mutableListOf<Long>()
        val durumlar = dayanikliIzle(bekle = { beklemeler += it }) {
            flow { if (++deneme < 3) throw IOException("temporary") else emit(42) }
        }.toList()
        assertEquals(3, deneme)
        assertEquals(listOf(1_000L, 2_000L), beklemeler)
        assertEquals(listOf(IzlemeDurumu.Hata(true), IzlemeDurumu.Hata(true), IzlemeDurumu.Hazir(42)), durumlar)
    }

    @Test fun persistentFailureStopsAfterBoundedRetriesAndAllowsANewSubscription() = runBlocking {
        var deneme = 0
        var bozuk = true
        val akis = dayanikliIzle(bekle = {}) {
            flow { deneme++; if (bozuk) throw IOException("offline") else emit(7) }
        }
        val ilk = akis.toList()
        assertEquals(4, deneme)
        assertEquals(IzlemeDurumu.Hata(false), ilk.last())
        bozuk = false
        assertEquals(listOf(IzlemeDurumu.Hazir(7)), akis.toList())
        assertEquals(5, deneme)
    }

    @Test fun failureWhileCreatingTheFlowIsAlsoRetried() = runBlocking {
        var deneme = 0
        val sonuc = dayanikliIzle(bekle = {}) {
            if (++deneme == 1) throw IOException("create")
            flowOf(1)
        }.toList()
        assertEquals(2, deneme)
        assertEquals(IzlemeDurumu.Hazir(1), sonuc.last())
    }

    @Test fun cancellationDoesNotRetryOrEmitAnError() = runBlocking {
        var deneme = 0
        var bekleme = 0
        val durumlar = mutableListOf<IzlemeDurumu<Int>>()
        try {
            dayanikliIzle<Int>(bekle = { bekleme++ }) {
                flow { deneme++; throw CancellationException("restore started") }
            }.collect { durumlar += it }
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { /* Expected. */ }
        assertEquals(1, deneme)
        assertEquals(0, bekleme)
        assertTrue(durumlar.isEmpty())
    }

    @Test fun downstreamFailuresAreNotMisreportedAsRepositoryErrors() = runBlocking {
        var deneme = 0
        try {
            dayanikliIzle(bekle = {}) { deneme++; flowOf(1) }
                .collect { throw IllegalStateException("consumer") }
            fail("Consumer error must propagate")
        } catch (error: IllegalStateException) { assertEquals("consumer", error.message) }
        assertEquals(1, deneme)
    }
}
