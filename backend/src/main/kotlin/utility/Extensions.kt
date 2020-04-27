package utility

import kotlinx.coroutines.*
import java.util.*


fun <T : Any> Optional<T>.toNullable(): T? = this.orElse(null)

suspend fun <A, B> Iterable<A>.mapConcurrently(dispatcher: CoroutineDispatcher = Dispatchers.Default, transform: suspend (A) -> B): List<B> {
    return coroutineScope {
        map { async(dispatcher) { transform(it) } }.awaitAll()
    }
}

fun String.isSingleLineJavaCommentLine(): Boolean = this.trim().startsWith(SinglelineCommentToken)

fun String.filterOutBlankLinesAndJavaComments(): String {
    return this.replace("\r", "")
        .replace(MultilineCommentRegex, "")
        .split("\n")
        .filter { !it.isBlank() }
        .filter { !it.isSingleLineJavaCommentLine() }
        .joinToString("\n")
}

fun String.countJavaSourceCharacters(): Int = this.filterOutBlankLinesAndJavaComments().replace("\n", "").count()
