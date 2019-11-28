package utility

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.*


fun <T : Any> Optional<T>.toNullable(): T? = this.orElse(null)

suspend fun <A, B> Iterable<A>.mapConcurrently(transform: suspend (A) -> B): List<B> = coroutineScope {
    map { async { transform(it) } }.awaitAll()
}
