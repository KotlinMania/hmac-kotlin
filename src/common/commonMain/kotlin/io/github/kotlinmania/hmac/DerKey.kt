package io.github.kotlinmania.hmac

import io.github.kotlinmania.digest.Block

internal const val IPAD: Byte = 0x36
internal const val OPAD: Byte = 0x5C

/**
 * Derives a block-sized HMAC key from [key] by zero-padding short keys or
 * hashing oversized keys with [digestFn] first.
 */
internal fun <D> getDerKey(
    key: ByteArray,
    blockSize: Int,
    digestFn: (ByteArray) -> ByteArray,
): Block<D> {
    val derKey = ByteArray(blockSize)
    if (key.size <= derKey.size) {
        key.copyInto(derKey, destinationOffset = 0, startIndex = 0, endIndex = key.size)
    } else {
        val hash = digestFn(key)
        if (hash.size <= derKey.size) {
            hash.copyInto(derKey, destinationOffset = 0, startIndex = 0, endIndex = hash.size)
        } else {
            hash.copyInto(derKey, destinationOffset = 0, startIndex = 0, endIndex = derKey.size)
        }
    }
    return derKey
}
