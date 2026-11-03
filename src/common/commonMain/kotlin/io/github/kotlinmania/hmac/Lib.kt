// port-lint: source lib.rs
package io.github.kotlinmania.hmac

import io.github.kotlinmania.digest.Digest
import kotlin.reflect.KClass

internal const val IPAD: Byte = 0x36
internal const val OPAD: Byte = 0x5C

/**
 * Derives the block-sized key by padding or hashing when key size exceeds block size.
 */
internal fun getDerKey(
    key: ByteArray,
    blockSize: Int,
    hasher: (ByteArray) -> ByteArray,
): ByteArray {
    val derKey = ByteArray(blockSize)
    if (key.size <= blockSize) {
        key.copyInto(derKey, 0, 0, key.size)
    } else {
        val hash = hasher(key)
        if (hash.size <= blockSize) {
            hash.copyInto(derKey, 0, 0, hash.size)
        } else {
            hash.copyInto(derKey, 0, 0, blockSize)
        }
    }
    return derKey
}

@PublishedApi
internal class HasherAdapter(
    val blockSize: Int,
    val outputSize: Int,
    val algName: String,
    val create: () -> Any,
    val update: (Any, ByteArray) -> Unit,
    val finalize: (Any) -> ByteArray,
    val reset: (Any) -> Unit,
    val digest: (ByteArray) -> ByteArray,
)

@PublishedApi
internal val customAdapters: MutableMap<KClass<*>, HasherAdapter> = mutableMapOf()

/**
 * Registers a custom hasher adapter for use with [Hmac] and [SimpleHmac].
 *
 * Generic implementation of Hash-based Message Authentication Code (HMAC).
 *
 * @param type The KClass of the hasher/digest type.
 * @param blockSize The block size in bytes of the underlying hash function.
 * @param outputSize The output digest size in bytes.
 * @param algName The algorithm name string for diagnostics.
 * @param create Factory creating a new hasher instance.
 * @param update Consumes byte array into hasher instance.
 * @param finalize Finalizes and produces output digest bytes.
 * @param reset Resets hasher state.
 * @param digest One-shot digest convenience function.
 */
@Suppress("UNCHECKED_CAST")
fun <D : Any> registerHasher(
    type: KClass<D>,
    blockSize: Int,
    outputSize: Int,
    algName: String,
    create: () -> D,
    update: (D, ByteArray) -> Unit,
    finalize: (D) -> ByteArray,
    reset: (D) -> Unit,
    digest: (ByteArray) -> ByteArray,
) {
    customAdapters[type] =
        HasherAdapter(
            blockSize = blockSize,
            outputSize = outputSize,
            algName = algName,
            create = create,
            update = { h, d -> update(h as D, d) },
            finalize = { h -> finalize(h as D) },
            reset = { h -> reset(h as D) },
            digest = digest,
        )
}

@PublishedApi
@Suppress("UNCHECKED_CAST")
internal fun <D : Any> getAdapter(type: KClass<D>): HasherAdapter? {
    customAdapters[type]?.let { return it }
    return try {
        val digestType = type as KClass<out Digest>
        val blockSize = Digest.blockSize(digestType)
        val outputSize = Digest.outputSize(digestType)
        HasherAdapter(
            blockSize = blockSize,
            outputSize = outputSize,
            algName = type.simpleName ?: "Digest",
            create = { Digest.new(digestType) },
            update = { h, d -> (h as Digest).update(d) },
            finalize = { h -> (h as Digest).finalize() },
            reset = { h -> (h as Digest).reset() },
            digest = { d -> Digest.digest(digestType, d) },
        )
    } catch (_: Throwable) {
        null
    }
}
