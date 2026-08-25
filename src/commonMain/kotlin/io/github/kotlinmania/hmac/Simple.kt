// port-lint: source src/simple.rs
package io.github.kotlinmania.hmac

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
import io.github.kotlinmania.digest.fmt.FmtResult
import io.github.kotlinmania.digest.fmt.Formatter
import kotlin.reflect.KClass

/**
 * Simplified HMAC instance able to operate over hash functions
 * which do not expose block-level API and hash functions which
 * process blocks lazily.
 */
class SimpleHmac<D : Any>
    @PublishedApi
    internal constructor(
        private val type: KClass<D>,
        private val adapter: HasherAdapter,
        private var digest: Any,
        private var opadKey: ByteArray,
        private var ipadKey: ByteArray,
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
        override val keySize: Int
            get() = adapter.blockSize

        override val blockSize: Int
            get() = adapter.blockSize

        override val outputSize: Int
            get() = adapter.outputSize

        override fun new(key: Key<*>): SimpleHmac<D> = newFromSlice(key).getOrThrow()

        override fun newFromSlice(key: ByteArray): Result<SimpleHmac<D>> = newFromSlice(type, key)

        override fun update(data: ByteArray) {
            adapter.update(digest, data)
        }

        override fun chain(data: ByteArray): SimpleHmac<D> {
            update(data)
            return this
        }

        override fun chainUpdate(data: ByteArray): SimpleHmac<D> {
            update(data)
            return this
        }

        override fun finalize(): CtOutput<*> {
            val out = ByteArray(outputSize)
            finalizeInto(out)
            return CtOutput.new<SimpleHmac<D>>(out)
        }

        override fun finalizeReset(): CtOutput<*> {
            val out = ByteArray(outputSize)
            finalizeIntoReset(out)
            return CtOutput.new<SimpleHmac<D>>(out)
        }

        override fun finalizeFixed(): ByteArray {
            val out = ByteArray(outputSize)
            finalizeInto(out)
            return out
        }

        override fun finalizeFixedReset(): ByteArray {
            val out = ByteArray(outputSize)
            finalizeIntoReset(out)
            return out
        }

        override fun finalizeInto(out: Output<*>) {
            val h = adapter.create()
            adapter.update(h, opadKey)
            val innerHash = adapter.finalize(digest)
            adapter.update(h, innerHash)
            val res = adapter.finalize(h)
            res.copyInto(out, 0, 0, minOf(out.size, res.size))
        }

        override fun reset() {
            adapter.reset(digest)
            adapter.update(digest, ipadKey)
        }

        override fun finalizeIntoReset(out: Output<*>) {
            val h = adapter.create()
            adapter.update(h, opadKey)
            val innerHash = adapter.finalize(digest)
            adapter.update(h, innerHash)
            val res = adapter.finalize(h)
            res.copyInto(out, 0, 0, minOf(out.size, res.size))
            reset()
        }

        fun fmt(formatter: Formatter): FmtResult =
            formatter
                .debugStruct("SimpleHmac")
                .field("digest", digest)
                .field("..", "..")
                .finish()

        companion object {
            inline fun <reified D : Any> new(key: ByteArray): SimpleHmac<D> = newFromSlice<D>(key).getOrThrow()

            inline fun <reified D : Any> newFromSlice(key: ByteArray): Result<SimpleHmac<D>> = newFromSlice(D::class, key)

            fun <D : Any> newFromSlice(
                type: KClass<D>,
                key: ByteArray,
            ): Result<SimpleHmac<D>> {
                val adapter = getAdapter(type) ?: return Result.failure(InvalidLength())
                val derKey = getDerKey(key, adapter.blockSize, adapter.digest)
                val ipadKey = derKey.copyOf()
                for (index in ipadKey.indices) {
                    ipadKey[index] = (ipadKey[index].toInt() xor IPAD.toInt()).toByte()
                }
                val digest = adapter.create()
                adapter.update(digest, ipadKey)

                val opadKey = derKey.copyOf()
                for (index in opadKey.indices) {
                    opadKey[index] = (opadKey[index].toInt() xor OPAD.toInt()).toByte()
                }

                return Result.success(
                    SimpleHmac(
                        type = type,
                        adapter = adapter,
                        digest = digest,
                        opadKey = opadKey,
                        ipadKey = ipadKey,
                    ),
                )
            }
        }
    }
