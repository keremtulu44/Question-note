package com.keremtulu.questionnote.data.ilerleme

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen

sealed interface IzlemeDurumu<out T> {
    data class Hazir<T>(val deger: T) : IzlemeDurumu<T>
    data class Hata(val yenidenDeneniyor: Boolean) : IzlemeDurumu<Nothing>
}

/** Cold, bounded retry. Restore/navigation cancellation is never reported as a data error. */
fun <T> dayanikliIzle(
    enFazlaYenidenDeneme: Int = 3,
    bekle: suspend (Long) -> Unit = { delay(it) },
    kaynak: () -> Flow<T>,
): Flow<IzlemeDurumu<T>> {
    require(enFazlaYenidenDeneme in 0..10)
    return flow { emitAll(kaynak()) }
        .map<T, IzlemeDurumu<T>> { IzlemeDurumu.Hazir(it) }
        .retryWhen { neden, deneme ->
            if (neden is CancellationException) throw neden
            if (deneme < enFazlaYenidenDeneme) {
                emit(IzlemeDurumu.Hata(yenidenDeneniyor = true))
                bekle((deneme + 1L) * 1_000L)
                true
            } else false
        }
        .catch { neden ->
            if (neden is CancellationException) throw neden
            emit(IzlemeDurumu.Hata(yenidenDeneniyor = false))
        }
}
