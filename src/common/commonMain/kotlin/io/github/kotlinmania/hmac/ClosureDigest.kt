package io.github.kotlinmania.hmac

import io.github.kotlinmania.digest.Block
import io.github.kotlinmania.digest.BlockSizeUser
import io.github.kotlinmania.digest.Digest
import io.github.kotlinmania.digest.HashMarker
import io.github.kotlinmania.digest.Output
import io.github.kotlinmania.digest.OutputSizeUser
import io.github.kotlinmania.digest.Reset
import io.github.kotlinmania.digest.coreapi.AlgorithmName
import io.github.kotlinmania.digest.coreapi.Buffer
import io.github.kotlinmania.digest.coreapi.BufferKindUser
import io.github.kotlinmania.digest.coreapi.Eager
import io.github.kotlinmania.digest.coreapi.FixedOutputCore
import io.github.kotlinmania.digest.coreapi.UpdateCore
import io.github.kotlinmania.digest.fmt.FmtResult
import io.github.kotlinmania.digest.fmt.Formatter

internal class BufferedChunkAccumulator(
    private val chunks: MutableList<ByteArray> = mutableListOf(),
) {
    fun append(data: ByteArray) {
        if (data.isNotEmpty()) {
            chunks.add(data.copyOf())
        }
    }

    fun toByteArray(): ByteArray {
        if (chunks.isEmpty()) {
            return ByteArray(0)
        }
        val total = chunks.sumOf { it.size }
        val result = ByteArray(total)
        var offset = 0
        for (chunk in chunks) {
            chunk.copyInto(result, offset)
            offset += chunk.size
        }
        return result
    }

    fun clear() {
        chunks.clear()
    }

    fun copy(): BufferedChunkAccumulator =
        BufferedChunkAccumulator(chunks.mapTo(mutableListOf()) { it.copyOf() })
}

/**
 * Concrete [Digest] implementation backed by a caller-provided one-shot hash function.
 *
 * Used by foreign/Swift callers that supply a platform hash closure (such as Apple CryptoKit)
 * rather than a pure-Kotlin [Digest] subclass.
 */
class ClosureDigest private constructor(
    override val blockSize: Int,
    override val outputSize: Int,
    private val hashFn: (ByteArray) -> ByteArray,
    private val accumulator: BufferedChunkAccumulator,
) : Digest,
    BlockSizeUser {
    constructor(
        blockSize: Int,
        outputSize: Int,
        hashFn: (ByteArray) -> ByteArray,
    ) : this(blockSize, outputSize, hashFn, BufferedChunkAccumulator()) {
        require(blockSize > 0) { "blockSize must be positive: $blockSize" }
        require(outputSize > 0) { "outputSize must be positive: $outputSize" }
    }

    override fun update(data: ByteArray) {
        accumulator.append(data)
    }

    override fun finalizeInto(out: Output<*>) {
        val digest = hashFn(accumulator.toByteArray())
        digest.copyInto(out, destinationOffset = 0, startIndex = 0, endIndex = minOf(out.size, digest.size))
    }

    override fun finalizeIntoReset(out: Output<*>) {
        finalizeInto(out)
        reset()
    }

    override fun reset() {
        accumulator.clear()
    }

    /** Returns an independent copy of this digest including any uncommitted input bytes. */
    fun copy(): ClosureDigest = ClosureDigest(blockSize, outputSize, hashFn, accumulator.copy())
}

/**
 * Concrete block-level core implementation backed by a caller-provided one-shot hash function.
 *
 * Used by foreign/Swift callers with [Hmac] and [HmacCore].
 */
class ClosureBlockCore private constructor(
    override val blockSize: Int,
    override val outputSize: Int,
    private val algName: String,
    private val hashFn: (ByteArray) -> ByteArray,
    private val accumulator: BufferedChunkAccumulator,
) : HashMarker,
    UpdateCore,
    FixedOutputCore,
    BufferKindUser,
    OutputSizeUser,
    Reset,
    AlgorithmName {
    constructor(
        blockSize: Int,
        outputSize: Int,
        algName: String,
        hashFn: (ByteArray) -> ByteArray,
    ) : this(blockSize, outputSize, algName, hashFn, BufferedChunkAccumulator()) {
        require(blockSize > 0) { "blockSize must be positive: $blockSize" }
        require(outputSize > 0) { "outputSize must be positive: $outputSize" }
    }

    override val bufferKind: Eager get() = Eager

    override fun updateBlocks(blocks: List<Block<*>>) {
        for (block in blocks) {
            accumulator.append(block)
        }
    }

    override fun finalizeFixedCore(
        buffer: Buffer<*>,
        out: Output<*>,
    ) {
        val remaining = buffer.remainingBytes()
        if (remaining.isNotEmpty()) {
            accumulator.append(remaining)
        }
        buffer.reset()
        val digest = hashFn(accumulator.toByteArray())
        digest.copyInto(out, destinationOffset = 0, startIndex = 0, endIndex = minOf(out.size, digest.size))
    }

    override fun reset() {
        accumulator.clear()
    }

    override fun writeAlgName(formatter: Formatter): FmtResult = formatter.writeString(algName)

    /** Returns an independent copy of this core including all accumulated block input. */
    fun copy(): ClosureBlockCore = ClosureBlockCore(blockSize, outputSize, algName, hashFn, accumulator.copy())
}
