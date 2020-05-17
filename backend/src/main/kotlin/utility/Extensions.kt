package utility

import com.github.javaparser.ast.Node
import com.github.javaparser.resolution.declarations.AssociableToAST
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration
import kotlinx.coroutines.*
import java.util.*


fun <T : Any> Optional<T>.toNullable(): T? = this.orElse(null)

inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Number): Long {
    var sum: Long = 0
    for (element in this) {
        sum += selector(element).toLong()
    }
    return sum
}

suspend fun <A, B> Iterable<A>.mapConcurrently(dispatcher: CoroutineDispatcher = Dispatchers.Default, transform: suspend (A) -> B): List<B> {
    return coroutineScope {
        map { async(dispatcher) { transform(it) } }.awaitAll()
    }
}

suspend fun <A, B> Iterable<A>.forEachConcurrently(dispatcher: CoroutineDispatcher = Dispatchers.Default, transform: suspend (A) -> B) {
    coroutineScope {
        map { async(dispatcher) { transform(it) } }.awaitAll()
    }
}

suspend fun <K, V, A, B> Map<out K, V>.mapConcurrently(dispatcher: CoroutineDispatcher = Dispatchers.Default, transform: suspend (Map.Entry<K, V>) -> Pair<A, B>): Map<A, B> {
    return coroutineScope {
        map { async(dispatcher) { transform(it) } }.awaitAll().toMap()
    }
}

suspend fun <K, V> Map<out K, V>.forEachConcurrently(dispatcher: CoroutineDispatcher = Dispatchers.Default, transform: suspend (Map.Entry<K, V>) -> Unit) {
    coroutineScope {
        map { async(dispatcher) { transform(it) } }.awaitAll()
    }
}

fun String.countJavaSourceCharacters(): Int = this.filterOutBlankLinesAndJavaComments().replace("\n", "").count()

fun String.isSingleLineJavaCommentLine(): Boolean = this.trim().startsWith(SinglelineCommentToken)

fun String.filterOutBlankLinesAndJavaComments(): String {
    return this.replace("\r", "")
        .replace(MultilineCommentRegex, "")
        .split("\n")
        .filter { !it.isBlank() }
        .filter { !it.isSingleLineJavaCommentLine() }
        .joinToString("\n")
}

fun ResolvedReferenceTypeDeclaration.retrieveAstNode(): Node? {
    val node: AssociableToAST<out Node>? = when {
        this.isClass     -> this.asClass()
        this.isInterface -> this.asInterface()
        else             -> null
    }
    return node?.toAst()?.toNullable()
}
