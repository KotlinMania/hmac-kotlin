// port-lint: source src/optim.rs
package io.github.kotlinmania.hmac

import io.github.kotlinmania.digest.AlgorithmName
import io.github.kotlinmania.digest.Block
import io.github.kotlinmania.digest.BlockSizeUser
import io.github.kotlinmania.digest.Buffer
import io.github.kotlinmania.digest.BufferKindUser
import io.github.kotlinmania.digest.CoreOf
import io.github.kotlinmania.digest.CoreProxy
import io.github.kotlinmania.digest.CoreWrapper
import io.github.kotlinmania.digest.Eager
import io.github.kotlinmania.digest.FixedOutputCore
import io.github.kotlinmania.digest.HashMarker
import io.github.kotlinmania.digest.InvalidLength
import io.github.kotlinmania.digest.Key
import io.github.kotlinmania.digest.KeyInit
import io.github.kotlinmania.digest.KeySizeUser
import io.github.kotlinmania.digest.MacMarker
import io.github.kotlinmania.digest.Output
import io.github.kotlinmania.digest.OutputSizeUser
import io.github.kotlinmania.digest.Reset
import io.github.kotlinmania.digest.UpdateCore
import io.github.kotlinmania.digest.fmt.FmtResult
import io.github.kotlinmania.digest.fmt.Formatter

/** Generic HMAC instance. */
typealias Hmac<D> = CoreWrapper<HmacCore<D>>

/** Generic core HMAC instance, which operates over blocks. */
class HmacCore<D> private constructor(
    private var digest: CoreOf<D>,
    private var opadDigest: CoreOf<D>,
    private var ipadDigest: CoreOf<D>,
) : MacMarker,
    BufferKindUser,
    KeySizeUser,
    BlockSizeUser,
    OutputSizeUser,
    KeyInit<HmacCore<D>>,
    UpdateCore,
    FixedOutputCore,
    Reset,
    AlgorithmName
where
    D : CoreProxy
{
    fun clone(): HmacCore<D> =
        HmacCore(
            digest = digest.clone(),
            opadDigest = opadDigest.clone(),
            ipadDigest = ipadDigest.clone(),
        )

    override val bufferKind: Eager
        get() = Eager

    override val keySize: Int
        get() = CoreOf.blockSize<D>()

    override val blockSize: Int
        get() = CoreOf.blockSize<D>()

    override val outputSize: Int
        get() = CoreOf.outputSize<D>()

    override fun new(key: Key<HmacCore<D>>): HmacCore<D> =
        newFromSlice(key.asSlice()).getOrThrow()

    override fun newFromSlice(key: ByteArray): Result<HmacCore<D>> {
        val buf = getDerKey<CoreWrapper<CoreOf<D>>>(key)
        for (index in buf.indices) {
            buf[index] = (buf[index].toInt() xor IPAD.toInt()).toByte()
        }
        val digest = CoreOf.default<D>()
        digest.updateBlocks(listOf(buf))

        for (index in buf.indices) {
            buf[index] = (buf[index].toInt() xor IPAD.toInt() xor OPAD.toInt()).toByte()
        }

        val opadDigest = CoreOf.default<D>()
        opadDigest.updateBlocks(listOf(buf))

        return Result.success(
            HmacCore(
                ipadDigest = digest.clone(),
                opadDigest = opadDigest,
                digest = digest,
            ),
        )
    }

    override fun updateBlocks(blocks: List<Block<HmacCore<D>>>) {
        digest.updateBlocks(blocks)
    }

    override fun finalizeFixedCore(buffer: Buffer<HmacCore<D>>, out: Output<HmacCore<D>>) {
        val hash = Output.default<CoreOf<D>>()
        digest.finalizeFixedCore(buffer, hash)
        // finalizeFixedCore should reset the buffer as well, but
        // to be extra safe we reset it explicitly again.
        buffer.reset()
        val h = opadDigest.clone()
        buffer.digestBlocks(hash) { block -> h.updateBlocks(block) }
        h.finalizeFixedCore(buffer, out)
    }

    override fun reset() {
        digest = ipadDigest.clone()
    }

    override fun writeAlgName(formatter: Formatter): FmtResult {
        formatter.writeString("Hmac<").getOrThrow()
        CoreOf.writeAlgName<D>(formatter).getOrThrow()
        return formatter.writeString(">")
    }

    fun fmt(formatter: Formatter): FmtResult {
        formatter.writeString("HmacCore<").getOrThrow()
        CoreOf.writeAlgName<D>(formatter).getOrThrow()
        return formatter.writeString("> { ... }")
    }
}
