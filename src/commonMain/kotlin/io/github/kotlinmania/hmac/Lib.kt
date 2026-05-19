// port-lint: source src/lib.rs
package io.github.kotlinmania.hmac

import io.github.kotlinmania.digest.Block
import io.github.kotlinmania.digest.BlockSizeUser
import io.github.kotlinmania.digest.Digest

/**
 * Generic implementation of Hash-based Message Authentication Code (HMAC).
 *
 * To use it you will need a cryptographic hash function implementation which
 * implements the [Digest] package traits. You can find compatible packages
 * such as `sha2` in the RustCrypto hashes repository.
 *
 * This package provides two HMAC implementations, [Hmac] and [SimpleHmac].
 * The first one is a buffered wrapper around block-level [HmacCore].
 * Internally it uses efficient state representation, but works only with
 * hash functions which expose block-level API and consume blocks eagerly
 * (for example, it will not work with the BLAKE2 family of hash functions).
 * On the other hand, [SimpleHmac] is a bit less efficient memory-wise,
 * but works with all hash functions which implement the [Digest] trait.
 *
 * # Examples
 *
 * Let us demonstrate how to use HMAC using the SHA-256 hash function.
 *
 * In the following examples [Hmac] is interchangeable with [SimpleHmac].
 *
 * To get authentication code:
 *
 * ```kotlin
 * val mac = HmacSha256.newFromSlice("my secret and secure key".encodeToByteArray())
 *     .getOrThrow()
 * mac.update("input message".encodeToByteArray())
 *
 * val result = mac.finalize()
 * val codeBytes = result.intoBytes()
 * val expected = hex(
 *     "97d2a569059bbcd8ead4444ff99071f4" +
 *         "c01d005bcefe0d3567e1be628e5fdcd9",
 * )
 * assertContentEquals(expected, codeBytes)
 * ```
 *
 * To verify the message:
 *
 * ```kotlin
 * val mac = HmacSha256.newFromSlice("my secret and secure key".encodeToByteArray())
 *     .getOrThrow()
 *
 * mac.update("input message".encodeToByteArray())
 *
 * val codeBytes = hex(
 *     "97d2a569059bbcd8ead4444ff99071f4" +
 *         "c01d005bcefe0d3567e1be628e5fdcd9",
 * )
 * mac.verifySlice(codeBytes).getOrThrow()
 * ```
 *
 * # Block and input sizes
 *
 * Usually it is assumed that block size is larger than output size. Due to the
 * generic nature of the implementation, this edge case must be handled as well
 * to remove potential panic. This is done by truncating hash output to the hash
 * block size if needed.
 */
internal const val IPAD: Byte = 0x36
internal const val OPAD: Byte = 0x5C

internal fun <D> getDerKey(key: ByteArray): Block<D> {
    val derKey = Block.default<D>()
    // The key that HMAC processes must be the same as the block size of the
    // underlying hash function. If the provided key is smaller than that,
    // we just pad it with zeros. If its larger, we hash it and then pad it
    // with zeros.
    if (key.size <= derKey.size) {
        key.copyInto(derKey, endIndex = key.size)
    } else {
        val hash = Digest.digest<D>(key)
        // All commonly used hash functions have block size bigger
        // than output hash size, but to be extra rigorous we
        // handle the potential uncommon cases as well.
        // The condition is calcualted at compile time, so this
        // branch gets removed from the final binary.
        if (hash.size <= derKey.size) {
            hash.copyInto(derKey, endIndex = hash.size)
        } else {
            val n = derKey.size
            hash.copyInto(derKey, endIndex = n)
        }
    }
    return derKey
}
