// port-lint: source src/simple.rs
package io.github.kotlinmania.hmac

import io.github.kotlinmania.digest.Block
import io.github.kotlinmania.digest.BlockSizeUser
import io.github.kotlinmania.digest.Digest
import io.github.kotlinmania.digest.FixedOutput
import io.github.kotlinmania.digest.FixedOutputReset
import io.github.kotlinmania.digest.InvalidLength
import io.github.kotlinmania.digest.Key
import io.github.kotlinmania.digest.KeyInit
import io.github.kotlinmania.digest.KeySizeUser
import io.github.kotlinmania.digest.MacMarker
import io.github.kotlinmania.digest.Output
import io.github.kotlinmania.digest.OutputSizeUser
import io.github.kotlinmania.digest.Reset
import io.github.kotlinmania.digest.Update
import io.github.kotlinmania.digest.fmt.FmtResult
import io.github.kotlinmania.digest.fmt.Formatter

/**
 * Simplified HMAC instance able to operate over hash functions
 * which do not expose block-level API and hash functions which
 * process blocks lazily (for example, BLAKE2).
 */
class SimpleHmac<D> private constructor(
    private var digest: D,
    private var opadKey: Block<D>,
    private var ipadKey: Block<D>,
) : KeySizeUser,
    MacMarker,
    KeyInit<SimpleHmac<D>>,
    Update,
    OutputSizeUser,
    FixedOutput,
    Reset,
    FixedOutputReset
where
    D : Digest,
    D : BlockSizeUser
{
    override val keySize: Int
        get() = Digest.blockSize<D>()

    override fun new(key: Key<SimpleHmac<D>>): SimpleHmac<D> =
        newFromSlice(key.asSlice()).getOrThrow()

    override fun newFromSlice(key: ByteArray): Result<SimpleHmac<D>> {
        val derKey = getDerKey<D>(key)
        val ipadKey = derKey.clone()
        for (index in ipadKey.indices) {
            ipadKey[index] = (ipadKey[index].toInt() xor IPAD.toInt()).toByte()
        }
        val digest = Digest.new<D>()
        digest.update(ipadKey)

        val opadKey = derKey
        for (index in opadKey.indices) {
            opadKey[index] = (opadKey[index].toInt() xor OPAD.toInt()).toByte()
        }

        return Result.success(
            SimpleHmac(
                digest = digest,
                opadKey = opadKey,
                ipadKey = ipadKey,
            ),
        )
    }

    override fun update(data: ByteArray) {
        digest.update(data)
    }

    override val outputSize: Int
        get() = Digest.outputSize<D>()

    override fun finalizeInto(out: Output<SimpleHmac<D>>) {
        val h = Digest.new<D>()
        h.update(opadKey)
        h.update(digest.finalize())
        h.finalizeInto(out)
    }

    fun fmt(formatter: Formatter): FmtResult =
        formatter
            .debugStruct("SimpleHmac")
            .field("digest", digest)
            // Replace with finishNonExhaustive on MSRV
            // bump to 1.53
            .field("..", "..")
            .finish()

    override fun reset() {
        digest.reset()
        digest.update(ipadKey)
    }

    override fun finalizeIntoReset(out: Output<SimpleHmac<D>>) {
        val h = Digest.new<D>()
        h.update(opadKey)
        h.update(digest.finalizeReset())
        digest.update(ipadKey)
        h.finalizeInto(out)
    }
}
