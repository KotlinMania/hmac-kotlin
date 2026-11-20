// port-lint: source simple.rs
package io.github.kotlinmania.hmac

import io.github.kotlinmania.digest.Block
import io.github.kotlinmania.digest.BlockSizeUser
import io.github.kotlinmania.digest.CtOutput
import io.github.kotlinmania.digest.Digest
import io.github.kotlinmania.digest.FixedOutput
import io.github.kotlinmania.digest.FixedOutputReset
import io.github.kotlinmania.digest.Key
import io.github.kotlinmania.digest.KeyInit
import io.github.kotlinmania.digest.KeySizeUser
import io.github.kotlinmania.digest.Mac
import io.github.kotlinmania.digest.MacMarker
import io.github.kotlinmania.digest.Output
import io.github.kotlinmania.digest.OutputSizeUser
import io.github.kotlinmania.digest.Reset
import io.github.kotlinmania.digest.Update
import io.github.kotlinmania.digest.asSlice
import io.github.kotlinmania.digest.fmt.FmtResult
import io.github.kotlinmania.digest.fmt.Formatter

/**
 * Simplified HMAC instance able to operate over hash functions
 * which do not expose block-level API and hash functions which
 * process blocks lazily (e.g. BLAKE2).
 */
class SimpleHmac<D : Any> internal constructor(
    private var digest: Digest,
    private val opadKey: Block<D>,
    private val ipadKey: Block<D>,
    private val newDigest: () -> Digest,
    private val cloneDigest: (Digest) -> Digest,
) : Mac,
    KeySizeUser,
    BlockSizeUser,
    MacMarker,
    KeyInit<SimpleHmac<D>>,
    Update,
    OutputSizeUser,
    FixedOutput,
    Reset,
    FixedOutputReset {
    override val keySize: KeySize get() = opadKey.size

    override val blockSize: BlockSize get() = opadKey.size

    override val outputSize: OutputSize get() = digest.outputSize

    override fun new(key: Key<*>): SimpleHmac<D> = newFromSlice(key.asSlice()).getOrThrow()

    override fun newFromSlice(key: ByteArray): Result<SimpleHmac<D>> =
        buildFromSlice(key, opadKey.size, newDigest, cloneDigest)

    /** Returns an independent copy preserving any compressed or buffered hash state. */
    fun clone(): SimpleHmac<D> =
        SimpleHmac(
            digest = cloneDigest(digest),
            opadKey = opadKey.copyOf(),
            ipadKey = ipadKey.copyOf(),
            newDigest = newDigest,
            cloneDigest = cloneDigest,
        )

    override fun update(data: ByteArray) {
        digest.update(data)
    }

    override fun chain(data: ByteArray): SimpleHmac<D> {
        update(data)
        return this
    }

    override fun chainUpdate(data: ByteArray): SimpleHmac<D> {
        update(data)
        return this
    }

    override fun finalize(): CtOutput<*> = CtOutput.new<SimpleHmac<D>>(finalizeFixed())

    override fun finalizeReset(): CtOutput<*> = CtOutput.new<SimpleHmac<D>>(finalizeFixedReset())

    override fun finalizeInto(out: Output<*>) {
        val h = newDigest()
        h.update(opadKey)
        h.update(digest.finalize())
        h.finalizeInto(out)
    }

    override fun reset() {
        digest.reset()
        digest.update(ipadKey)
    }

    override fun finalizeIntoReset(out: Output<*>) {
        val h = newDigest()
        h.update(opadKey)
        h.update(digest.finalizeReset())
        digest.update(ipadKey)
        h.finalizeInto(out)
    }

    /** Formats a debug representation matching `SimpleHmac { digest: ..., .. }`. */
    fun fmt(formatter: Formatter): FmtResult =
        formatter
            .debugStruct("SimpleHmac")
            .field("digest", digest)
            .field("..", "..")
            .finish()

    override fun toString(): String {
        val formatter = Formatter()
        fmt(formatter)
        return formatter.toString()
    }

    companion object {
        /** Creates a new [SimpleHmac] instance from [key]. */
        fun <D> new(
            key: ByteArray,
            newDigest: () -> D,
            cloneDigest: (D) -> D,
        ): SimpleHmac<D> where D : Digest, D : BlockSizeUser =
            newFromSlice(key, newDigest, cloneDigest).getOrThrow()

        /** Creates a new [SimpleHmac] instance from a byte slice [key]. */
        fun <D> newFromSlice(
            key: ByteArray,
            newDigest: () -> D,
            cloneDigest: (D) -> D,
        ): Result<SimpleHmac<D>> where D : Digest, D : BlockSizeUser {
            val proto = newDigest()
            val typedClone: (Digest) -> Digest = { d ->
                @Suppress("UNCHECKED_CAST")
                cloneDigest(d as D)
            }
            return buildFromSlice(key, proto.blockSize, newDigest, typedClone)
        }

        private fun <D : Any> buildFromSlice(
            key: ByteArray,
            blockSize: Int,
            newDigest: () -> Digest,
            cloneDigest: (Digest) -> Digest,
        ): Result<SimpleHmac<D>> {
            val derKey =
                getDerKey<D>(key, blockSize) { data ->
                    val h = newDigest()
                    h.update(data)
                    h.finalize()
                }
            val ipadKey = derKey.copyOf()
            for (idx in ipadKey.indices) {
                ipadKey[idx] = (ipadKey[idx].toInt() xor IPAD.toInt()).toByte()
            }
            val digest = newDigest()
            digest.update(ipadKey)

            val opadKey = derKey
            for (idx in opadKey.indices) {
                opadKey[idx] = (opadKey[idx].toInt() xor OPAD.toInt()).toByte()
            }

            return Result.success(
                SimpleHmac(
                    digest = digest,
                    opadKey = opadKey,
                    ipadKey = ipadKey,
                    newDigest = newDigest,
                    cloneDigest = cloneDigest,
                ),
            )
        }

        /** Creates a concrete [SimpleHmac] backed by a caller-supplied one-shot [digest] closure. */
        fun fromDigest(
            blockSize: Int,
            outputSize: Int,
            algName: String,
            key: ByteArray,
            digest: (ByteArray) -> ByteArray,
        ): SimpleHmac<ClosureDigest> {
            require(algName.isNotEmpty()) { "algName must not be empty" }
            return newFromSlice(
                key = key,
                newDigest = { ClosureDigest(blockSize, outputSize, digest) },
                cloneDigest = { it.copy() },
            ).getOrThrow()
        }
    }
}
