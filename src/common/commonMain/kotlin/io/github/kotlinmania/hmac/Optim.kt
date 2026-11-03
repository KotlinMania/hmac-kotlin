// port-lint: source optim.rs
package io.github.kotlinmania.hmac

import io.github.kotlinmania.digest.Block
import io.github.kotlinmania.digest.BlockSizeUser
import io.github.kotlinmania.digest.CtOutput
import io.github.kotlinmania.digest.FixedOutput
import io.github.kotlinmania.digest.FixedOutputReset
import io.github.kotlinmania.digest.InvalidLength
import io.github.kotlinmania.digest.Key
import io.github.kotlinmania.digest.KeyInit
import io.github.kotlinmania.digest.KeySizeUser
import io.github.kotlinmania.digest.Mac
import io.github.kotlinmania.digest.MacMarker
import io.github.kotlinmania.digest.Output
import io.github.kotlinmania.digest.OutputSizeUser
import io.github.kotlinmania.digest.Reset
import io.github.kotlinmania.digest.Update
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
import kotlin.reflect.KClass

/**
 * Generic HMAC instance using block-level core optimization.
 */
class Hmac<D : Any>
    @PublishedApi
    internal constructor(
        @PublishedApi internal val wrapper: CoreWrapper<HmacCore<D>>,
    ) : Mac,
        KeyInit<Hmac<D>>,
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

        override fun new(key: Key<*>): Hmac<D> = newFromSlice(key).getOrThrow()

        override fun newFromSlice(key: ByteArray): Result<Hmac<D>> {
            @Suppress("UNCHECKED_CAST")
            val core = wrapper.core as HmacCore<D>
            return core.newFromSlice(key).map { Hmac(CoreWrapper.fromCore(it)) }
        }

        override fun update(data: ByteArray) {
            wrapper.update(data)
        }

        override fun chain(data: ByteArray): Hmac<D> {
            update(data)
            return this
        }

        override fun finalize(): CtOutput<*> = CtOutput.new<Hmac<D>>(wrapper.finalizeFixed())

        override fun finalizeReset(): CtOutput<*> = CtOutput.new<Hmac<D>>(wrapper.finalizeFixedReset())

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

        companion object {
            inline fun <reified D : Any> new(key: ByteArray): Hmac<D> = newFromSlice<D>(key).getOrThrow()

            inline fun <reified D : Any> newFromSlice(key: ByteArray): Result<Hmac<D>> =
                HmacCore.newFromSlice<D>(key).map { Hmac(CoreWrapper.fromCore(it)) }

            fun <D : Any> newFromSlice(
                type: KClass<D>,
                key: ByteArray,
            ): Result<Hmac<D>> = HmacCore.newFromSlice(type, key).map { Hmac(CoreWrapper.fromCore(it)) }
        }
    }

/**
 * Generic core HMAC instance, which operates over blocks.
 */
class HmacCore<D : Any>
    @PublishedApi
    internal constructor(
        private val type: KClass<D>,
        private val adapter: HasherAdapter,
        private val opadKey: ByteArray,
        private val ipadKey: ByteArray,
        private var digest: Any,
    ) : MacMarker,
        BufferKindUser,
        KeySizeUser,
        BlockSizeUser,
        OutputSizeUser,
        KeyInit<HmacCore<D>>,
        UpdateCore,
        FixedOutputCore,
        Reset,
        AlgorithmName,
        CoreProxy {
        override val core: Any get() = this

        override val bufferKind: Eager get() = Eager

        override val keySize: Int get() = adapter.blockSize

        override val blockSize: Int get() = adapter.blockSize

        override val outputSize: Int get() = adapter.outputSize

        override fun new(key: Key<*>): HmacCore<D> = newFromSlice(key).getOrThrow()

        override fun newFromSlice(key: ByteArray): Result<HmacCore<D>> = newFromSlice(type, key)

        fun clone(): HmacCore<D> {
            val newDigest = adapter.create()
            adapter.update(newDigest, ipadKey)
            return HmacCore(
                type = type,
                adapter = adapter,
                opadKey = opadKey.copyOf(),
                ipadKey = ipadKey.copyOf(),
                digest = newDigest,
            )
        }

        override fun updateBlocks(blocks: List<Block<*>>) {
            for (block in blocks) {
                adapter.update(digest, block)
            }
        }

        override fun finalizeFixedCore(
            buffer: Buffer<*>,
            out: Output<*>,
        ) {
            val remaining = buffer.remainingBytes()
            if (remaining.isNotEmpty()) {
                adapter.update(digest, remaining)
            }
            val innerHash = adapter.finalize(digest)
            buffer.reset()
            val outerHasher = adapter.create()
            adapter.update(outerHasher, opadKey)
            adapter.update(outerHasher, innerHash)
            val finalHash = adapter.finalize(outerHasher)
            finalHash.copyInto(out, 0, 0, minOf(out.size, finalHash.size))
        }

        override fun reset() {
            adapter.reset(digest)
            adapter.update(digest, ipadKey)
        }

        override fun writeAlgName(formatter: Formatter): FmtResult {
            formatter.writeString("Hmac<").getOrThrow()
            formatter.writeString(adapter.algName).getOrThrow()
            return formatter.writeString(">")
        }

        fun fmt(formatter: Formatter): FmtResult {
            formatter.writeString("HmacCore<").getOrThrow()
            formatter.writeString(adapter.algName).getOrThrow()
            return formatter.writeString("> { ... }")
        }

        companion object {
            inline fun <reified D : Any> newFromSlice(key: ByteArray): Result<HmacCore<D>> = newFromSlice(D::class, key)

            fun <D : Any> newFromSlice(
                type: KClass<D>,
                key: ByteArray,
            ): Result<HmacCore<D>> {
                val adapter = getAdapter(type) ?: return Result.failure(InvalidLength())
                val derKey = getDerKey(key, adapter.blockSize, adapter.digest)
                val ipadKey = derKey.copyOf()
                for (i in ipadKey.indices) {
                    ipadKey[i] = (ipadKey[i].toInt() xor IPAD.toInt()).toByte()
                }
                val opadKey = derKey.copyOf()
                for (i in opadKey.indices) {
                    opadKey[i] = (opadKey[i].toInt() xor OPAD.toInt()).toByte()
                }

                val digest = adapter.create()
                adapter.update(digest, ipadKey)

                return Result.success(
                    HmacCore(
                        type = type,
                        adapter = adapter,
                        opadKey = opadKey,
                        ipadKey = ipadKey,
                        digest = digest,
                    ),
                )
            }
        }
    }
