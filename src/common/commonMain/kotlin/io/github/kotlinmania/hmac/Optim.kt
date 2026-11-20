// port-lint: source optim.rs
package io.github.kotlinmania.hmac

import io.github.kotlinmania.digest.Block
import io.github.kotlinmania.digest.BlockSizeUser
import io.github.kotlinmania.digest.CtOutput
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
import io.github.kotlinmania.digest.coreapi.AlgorithmName
import io.github.kotlinmania.digest.coreapi.Buffer
import io.github.kotlinmania.digest.coreapi.BufferKindUser
import io.github.kotlinmania.digest.coreapi.CoreProxy
import io.github.kotlinmania.digest.coreapi.CoreWrapper
import io.github.kotlinmania.digest.coreapi.Eager
import io.github.kotlinmania.digest.coreapi.FixedOutputCore
import io.github.kotlinmania.digest.coreapi.UpdateCore
import io.github.kotlinmania.digest.fmt.FmtResult
import io.github.kotlinmania.digest.fmt.Formatter

typealias BufferKind = Eager
typealias KeySize = Int
typealias BlockSize = Int
typealias OutputSize = Int

/**
 * Generic HMAC instance using block-level core optimization.
 */
class Hmac<C : Any> internal constructor(
    internal val wrapper: CoreWrapper<HmacCore<C>>,
) : Mac,
    KeyInit<Hmac<C>>,
    Update,
    FixedOutput,
    FixedOutputReset,
    Reset,
    OutputSizeUser,
    BlockSizeUser,
    MacMarker,
    CoreProxy {
    override val core: Any get() = wrapper.core

    override val outputSize: Int get() = wrapper.outputSize

    override val blockSize: Int get() = wrapper.blockSize

    override fun new(key: Key<*>): Hmac<C> = newFromSlice(key.asSlice()).getOrThrow()

    override fun newFromSlice(key: ByteArray): Result<Hmac<C>> {
        @Suppress("UNCHECKED_CAST")
        val hmacCore = wrapper.core as HmacCore<C>
        return hmacCore.newFromSlice(key).map { Hmac(CoreWrapper.fromCore(it)) }
    }

    /** Returns an independent copy preserving compressed and buffered message state. */
    fun clone(): Hmac<C> {
        val (innerCore, buffer) = wrapper.decompose()
        val clonedWrapper = CoreWrapper.fromCore(innerCore.clone())
        val remaining = buffer.remainingBytes()
        if (remaining.isNotEmpty()) {
            clonedWrapper.update(remaining)
        }
        return Hmac(clonedWrapper)
    }

    override fun update(data: ByteArray) {
        wrapper.update(data)
    }

    override fun chain(data: ByteArray): Hmac<C> {
        update(data)
        return this
    }

    override fun finalize(): CtOutput<*> = CtOutput.new<Hmac<C>>(wrapper.finalizeFixed())

    override fun finalizeReset(): CtOutput<*> = CtOutput.new<Hmac<C>>(wrapper.finalizeFixedReset())

    override fun finalizeInto(out: Output<*>) {
        wrapper.finalizeInto(out)
    }

    override fun finalizeIntoReset(out: Output<*>) {
        wrapper.finalizeIntoReset(out)
    }

    override fun finalizeFixed(): ByteArray = wrapper.finalizeFixed()

    override fun finalizeFixedReset(): ByteArray = wrapper.finalizeFixedReset()

    override fun reset() {
        wrapper.reset()
    }

    override fun toString(): String = wrapper.toString()

    companion object {
        /** Creates a new [Hmac] instance from a fixed-size or arbitrary [key]. */
        fun <C> new(
            key: ByteArray,
            defaultCore: () -> C,
            cloneCore: (C) -> C,
        ): Hmac<C> where C : UpdateCore, C : FixedOutputCore, C : BufferKindUser, C : Reset =
            newFromSlice(key, defaultCore, cloneCore).getOrThrow()

        /** Creates a new [Hmac] instance from a byte slice [key]. */
        fun <C> newFromSlice(
            key: ByteArray,
            defaultCore: () -> C,
            cloneCore: (C) -> C,
        ): Result<Hmac<C>> where C : UpdateCore, C : FixedOutputCore, C : BufferKindUser, C : Reset =
            HmacCore.newFromSlice(key, defaultCore, cloneCore).map { Hmac(CoreWrapper.fromCore(it)) }

        /** Creates a concrete [Hmac] backed by a caller-supplied one-shot [digest] closure. */
        fun fromDigest(
            blockSize: Int,
            outputSize: Int,
            algName: String,
            key: ByteArray,
            digest: (ByteArray) -> ByteArray,
        ): Hmac<ClosureBlockCore> =
            newFromSlice(
                key = key,
                defaultCore = { ClosureBlockCore(blockSize, outputSize, algName, digest) },
                cloneCore = { it.copy() },
            ).getOrThrow()
    }
}

/**
 * Generic core HMAC instance, which operates over blocks.
 */
class HmacCore<C : Any> internal constructor(
    private var digest: FixedOutputCore,
    private val opadDigest: FixedOutputCore,
    private val ipadDigest: FixedOutputCore,
    private val cloneCore: (FixedOutputCore) -> FixedOutputCore,
    private val createFromSlice: (ByteArray) -> Result<HmacCore<C>>,
) : MacMarker,
    BufferKindUser,
    KeySizeUser,
    BlockSizeUser,
    OutputSizeUser,
    KeyInit<HmacCore<C>>,
    UpdateCore,
    FixedOutputCore,
    Reset,
    AlgorithmName,
    CoreProxy {
    override val core: Any get() = this

    override val bufferKind: BufferKind get() = Eager

    override val keySize: KeySize get() = digest.blockSize

    override val blockSize: BlockSize get() = digest.blockSize

    override val outputSize: OutputSize get() = digest.outputSize

    override fun new(key: Key<*>): HmacCore<C> = newFromSlice(key.asSlice()).getOrThrow()

    override fun newFromSlice(key: ByteArray): Result<HmacCore<C>> = createFromSlice(key)

    /** Returns an independent copy of this HMAC core preserving compressed state. */
    fun clone(): HmacCore<C> =
        HmacCore(
            digest = cloneCore(digest),
            opadDigest = cloneCore(opadDigest),
            ipadDigest = cloneCore(ipadDigest),
            cloneCore = cloneCore,
            createFromSlice = createFromSlice,
        )

    override fun updateBlocks(blocks: List<Block<*>>) {
        digest.updateBlocks(blocks)
    }

    override fun finalizeFixedCore(
        buffer: Buffer<*>,
        out: Output<*>,
    ) {
        val hash = ByteArray(outputSize)
        digest.finalizeFixedCore(buffer, hash)
        // finalizeFixedCore should reset the buffer as well, but
        // to be extra safe we reset it explicitly again.
        buffer.reset()
        val h = cloneCore(opadDigest)
        buffer.digestBlocks(hash) { b -> h.updateBlocks(b) }
        h.finalizeFixedCore(buffer, out)
    }

    override fun reset() {
        digest = cloneCore(ipadDigest)
    }

    override fun writeAlgName(formatter: Formatter): FmtResult {
        formatter.writeString("Hmac<").getOrThrow()
        (digest as? AlgorithmName)?.writeAlgName(formatter)?.getOrThrow()
            ?: formatter.writeString(digest::class.simpleName ?: "Core").getOrThrow()
        return formatter.writeString(">")
    }

    /** Formats a debug representation matching `HmacCore<Alg> { ... }`. */
    fun fmt(formatter: Formatter): FmtResult {
        formatter.writeString("HmacCore<").getOrThrow()
        (digest as? AlgorithmName)?.writeAlgName(formatter)?.getOrThrow()
            ?: formatter.writeString(digest::class.simpleName ?: "Core").getOrThrow()
        return formatter.writeString("> { ... }")
    }

    override fun toString(): String {
        val formatter = Formatter()
        fmt(formatter)
        return formatter.toString()
    }

    companion object {
        /** Creates a new [HmacCore] instance from [key]. */
        fun <C> new(
            key: Key<HmacCore<C>>,
            defaultCore: () -> C,
            cloneCore: (C) -> C,
        ): HmacCore<C> where C : UpdateCore, C : FixedOutputCore, C : BufferKindUser, C : Reset =
            newFromSlice(key.asSlice(), defaultCore, cloneCore).getOrThrow()

        /** Creates a new [HmacCore] instance from a byte slice [key]. */
        fun <C> newFromSlice(
            key: ByteArray,
            defaultCore: () -> C,
            cloneCore: (C) -> C,
        ): Result<HmacCore<C>> where C : UpdateCore, C : FixedOutputCore, C : BufferKindUser, C : Reset {
            val proto = defaultCore()
            val buf =
                getDerKey<HmacCore<C>>(key, proto.blockSize) { data ->
                    val wrapper = CoreWrapper.fromCore(defaultCore())
                    wrapper.update(data)
                    wrapper.finalizeFixed()
                }
            for (idx in buf.indices) {
                buf[idx] = (buf[idx].toInt() xor IPAD.toInt()).toByte()
            }
            val digest = defaultCore()
            digest.updateBlocks(listOf(buf.copyOf()))

            for (idx in buf.indices) {
                buf[idx] = (buf[idx].toInt() xor (IPAD.toInt() xor OPAD.toInt())).toByte()
            }
            val opadDigest = defaultCore()
            opadDigest.updateBlocks(listOf(buf.copyOf()))

            val ipadDigest = cloneCore(digest)
            val typedClone: (FixedOutputCore) -> FixedOutputCore = { core ->
                @Suppress("UNCHECKED_CAST")
                cloneCore(core as C)
            }
            return Result.success(
                HmacCore(
                    digest = digest,
                    opadDigest = opadDigest,
                    ipadDigest = ipadDigest,
                    cloneCore = typedClone,
                    createFromSlice = { nextKey -> newFromSlice(nextKey, defaultCore, cloneCore) },
                ),
            )
        }

        /** Creates a concrete [HmacCore] backed by a caller-supplied one-shot [digest] closure. */
        fun fromDigest(
            blockSize: Int,
            outputSize: Int,
            algName: String,
            key: ByteArray,
            digest: (ByteArray) -> ByteArray,
        ): HmacCore<ClosureBlockCore> =
            newFromSlice(
                key = key,
                defaultCore = { ClosureBlockCore(blockSize, outputSize, algName, digest) },
                cloneCore = { it.copy() },
            ).getOrThrow()
    }
}
