// port-lint: tests tests/mod.rs
@file:OptIn(ExperimentalUnsignedTypes::class)

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
import io.github.kotlinmania.sha1.Sha1
import io.github.kotlinmania.sha1.Sha1Core
import io.github.kotlinmania.sha2.Sha224
import io.github.kotlinmania.sha2.Sha256
import io.github.kotlinmania.sha2.Sha256VarCore
import io.github.kotlinmania.sha2.Sha384
import io.github.kotlinmania.sha2.Sha512
import io.github.kotlinmania.sha2.Sha512VarCore
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

/**
 * Adapts published sha2-kotlin:0.1.0 [Sha256VarCore]/[Sha512VarCore] (which predate
 * digest-kotlin interface conformance) to [FixedOutputCore] for upstream test parity.
 */
private class Sha2VarCoreAdapter private constructor(
    private val is512: Boolean,
    private val fullOutputSize: Int,
    override val outputSize: Int,
    override val blockSize: Int,
    private val algName: String,
    private var core256: Sha256VarCore?,
    private var core512: Sha512VarCore?,
    private var blocksProcessed: ULong = 0uL,
) : HashMarker,
    UpdateCore,
    FixedOutputCore,
    BufferKindUser,
    OutputSizeUser,
    Reset,
    AlgorithmName {
    override val bufferKind: Eager get() = Eager

    override fun updateBlocks(blocks: List<Block<*>>) {
        val array = blocks.toTypedArray()
        blocksProcessed += array.size.toULong()
        core256?.updateBlocks(array)
        core512?.updateBlocks(array)
    }

    override fun finalizeFixedCore(
        buffer: Buffer<*>,
        out: Output<*>,
    ) {
        val rem = buffer.remainingBytes()
        val padded = ByteArray(blockSize)
        rem.copyInto(padded)
        val full = ByteArray(fullOutputSize)
        core256?.finalizeVariableCore(padded, rem.size, full)
        core512?.finalizeVariableCore(padded, rem.size, full)
        buffer.reset()
        full.copyInto(out, destinationOffset = 0, startIndex = 0, endIndex = minOf(out.size, outputSize))
    }

    override fun reset() {
        blocksProcessed = 0uL
        if (is512) {
            core512 = Sha512VarCore.new(outputSize)
        } else {
            core256 = Sha256VarCore.new(outputSize)
        }
    }

    override fun writeAlgName(formatter: Formatter): FmtResult = formatter.writeString(algName)

    fun copy(): Sha2VarCoreAdapter =
        Sha2VarCoreAdapter(
            is512 = is512,
            fullOutputSize = fullOutputSize,
            outputSize = outputSize,
            blockSize = blockSize,
            algName = algName,
            core256 = core256?.let { Sha256VarCore(it.stateCopy(), blocksProcessed) },
            core512 = core512?.let { Sha512VarCore(it.stateCopy(), blocksProcessed) },
            blocksProcessed = blocksProcessed,
        )

    companion object {
        fun sha224(): Sha2VarCoreAdapter =
            Sha2VarCoreAdapter(
                is512 = false,
                fullOutputSize = 32,
                outputSize = 28,
                blockSize = 64,
                algName = "Sha224",
                core256 = Sha256VarCore.new(28),
                core512 = null,
            )

        fun sha256(): Sha2VarCoreAdapter =
            Sha2VarCoreAdapter(
                is512 = false,
                fullOutputSize = 32,
                outputSize = 32,
                blockSize = 64,
                algName = "Sha256",
                core256 = Sha256VarCore.new(32),
                core512 = null,
            )

        fun sha384(): Sha2VarCoreAdapter =
            Sha2VarCoreAdapter(
                is512 = true,
                fullOutputSize = 64,
                outputSize = 48,
                blockSize = 128,
                algName = "Sha384",
                core256 = null,
                core512 = Sha512VarCore.new(48),
            )

        fun sha512(): Sha2VarCoreAdapter =
            Sha2VarCoreAdapter(
                is512 = true,
                fullOutputSize = 64,
                outputSize = 64,
                blockSize = 128,
                algName = "Sha512",
                core256 = null,
                core512 = Sha512VarCore.new(64),
            )
    }
}

/**
 * Adapts published sha2-kotlin:0.1.0 high-level hashers to [Digest] + [BlockSizeUser].
 */
private class Sha2TestDigest private constructor(
    override val blockSize: Int,
    override val outputSize: Int,
    private val factory: () -> Any,
    private val updateFn: (Any, ByteArray) -> Unit,
    private val finalizeFn: (Any) -> ByteArray,
    private val finalizeResetFn: (Any) -> ByteArray,
    private val resetFn: (Any) -> Unit,
    private val recorded: MutableList<ByteArray>,
) : Digest,
    BlockSizeUser {
    private val inner: Any = factory()

    init {
        for (chunk in recorded) {
            updateFn(inner, chunk)
        }
    }

    override fun update(data: ByteArray) {
        if (data.isNotEmpty()) {
            recorded.add(data.copyOf())
        }
        updateFn(inner, data)
    }

    override fun finalizeInto(out: Output<*>) {
        val digest = finalizeFn(inner)
        recorded.clear()
        digest.copyInto(out, destinationOffset = 0, startIndex = 0, endIndex = minOf(out.size, digest.size))
    }

    override fun finalizeIntoReset(out: Output<*>) {
        val digest = finalizeResetFn(inner)
        recorded.clear()
        digest.copyInto(out, destinationOffset = 0, startIndex = 0, endIndex = minOf(out.size, digest.size))
    }

    override fun reset() {
        recorded.clear()
        resetFn(inner)
    }

    fun copy(): Sha2TestDigest =
        Sha2TestDigest(
            blockSize = blockSize,
            outputSize = outputSize,
            factory = factory,
            updateFn = updateFn,
            finalizeFn = finalizeFn,
            finalizeResetFn = finalizeResetFn,
            resetFn = resetFn,
            recorded = recorded.mapTo(mutableListOf()) { it.copyOf() },
        )

    companion object {
        fun sha224(): Sha2TestDigest =
            Sha2TestDigest(
                blockSize = 64,
                outputSize = 28,
                factory = { Sha224() },
                updateFn = { h, d -> (h as Sha224).update(d) },
                finalizeFn = { h -> (h as Sha224).finalize() },
                finalizeResetFn = { h -> (h as Sha224).finalizeReset() },
                resetFn = { h -> (h as Sha224).reset() },
                recorded = mutableListOf(),
            )

        fun sha256(): Sha2TestDigest =
            Sha2TestDigest(
                blockSize = 64,
                outputSize = 32,
                factory = { Sha256() },
                updateFn = { h, d -> (h as Sha256).update(d) },
                finalizeFn = { h -> (h as Sha256).finalize() },
                finalizeResetFn = { h -> (h as Sha256).finalizeReset() },
                resetFn = { h -> (h as Sha256).reset() },
                recorded = mutableListOf(),
            )

        fun sha384(): Sha2TestDigest =
            Sha2TestDigest(
                blockSize = 128,
                outputSize = 48,
                factory = { Sha384() },
                updateFn = { h, d -> (h as Sha384).update(d) },
                finalizeFn = { h -> (h as Sha384).finalize() },
                finalizeResetFn = { h -> (h as Sha384).finalizeReset() },
                resetFn = { h -> (h as Sha384).reset() },
                recorded = mutableListOf(),
            )

        fun sha512(): Sha2TestDigest =
            Sha2TestDigest(
                blockSize = 128,
                outputSize = 64,
                factory = { Sha512() },
                updateFn = { h, d -> (h as Sha512).update(d) },
                finalizeFn = { h -> (h as Sha512).finalize() },
                finalizeResetFn = { h -> (h as Sha512).finalizeReset() },
                resetFn = { h -> (h as Sha512).reset() },
                recorded = mutableListOf(),
            )
    }
}

class ModTest {
    private fun hmacSha1(key: ByteArray): Hmac<Sha1Core> =
        Hmac.newFromSlice(key, { Sha1Core.default() }, { it.copy() }).getOrThrow()

    private fun hmacSha224(key: ByteArray): Hmac<Sha2VarCoreAdapter> =
        Hmac.newFromSlice(key, { Sha2VarCoreAdapter.sha224() }, { it.copy() }).getOrThrow()

    private fun hmacSha256(key: ByteArray): Hmac<Sha2VarCoreAdapter> =
        Hmac.newFromSlice(key, { Sha2VarCoreAdapter.sha256() }, { it.copy() }).getOrThrow()

    private fun hmacSha384(key: ByteArray): Hmac<Sha2VarCoreAdapter> =
        Hmac.newFromSlice(key, { Sha2VarCoreAdapter.sha384() }, { it.copy() }).getOrThrow()

    private fun hmacSha512(key: ByteArray): Hmac<Sha2VarCoreAdapter> =
        Hmac.newFromSlice(key, { Sha2VarCoreAdapter.sha512() }, { it.copy() }).getOrThrow()

    // RFC 2104 (MD5) tests unported: md5 crate is not yet ported to KotlinMania
    // test!(hmac_md5_rfc2104, "md5", Hmac<md5::Md5>);
    // test!(hmac_md5_rfc2104_simple, "md5", SimpleHmac<md5::Md5>);

    // Test vectors from RFC 4231
    @Test
    fun hmacSha224Rfc4231() {
        runMacSuite(TestData.sha224, ::hmacSha224)
    }

    @Test
    fun hmacSha256Rfc4231() {
        runMacSuite(TestData.sha256, ::hmacSha256)
    }

    @Test
    fun hmacSha384Rfc4231() {
        runMacSuite(TestData.sha384, ::hmacSha384)
    }

    @Test
    fun hmacSha512Rfc4231() {
        runMacSuite(TestData.sha512, ::hmacSha512)
    }

    @Test
    fun hmacSha224Rfc4231Simple() {
        runMacSuite(TestData.sha224, { SimpleHmac.newFromSlice(it, { Sha2TestDigest.sha224() }, { d -> d.copy() }).getOrThrow() })
    }

    @Test
    fun hmacSha256Rfc4231Simple() {
        runMacSuite(TestData.sha256, { SimpleHmac.newFromSlice(it, { Sha2TestDigest.sha256() }, { d -> d.copy() }).getOrThrow() })
    }

    @Test
    fun hmacSha384Rfc4231Simple() {
        runMacSuite(TestData.sha384, { SimpleHmac.newFromSlice(it, { Sha2TestDigest.sha384() }, { d -> d.copy() }).getOrThrow() })
    }

    @Test
    fun hmacSha512Rfc4231Simple() {
        runMacSuite(TestData.sha512, { SimpleHmac.newFromSlice(it, { Sha2TestDigest.sha512() }, { d -> d.copy() }).getOrThrow() })
    }

    // R 50.1.113-2016 (Streebog) tests unported: streebog crate is not yet ported to KotlinMania
    // test!(hmac_streebog256, "streebog256", Hmac<Streebog256>);
    // test!(hmac_streebog512, "streebog512", Hmac<Streebog512>);
    // test!(hmac_streebog256_simple, "streebog256", SimpleHmac<Streebog256>);
    // test!(hmac_streebog512_simple, "streebog512", SimpleHmac<Streebog512>);

    // Tests from Project Wycheproof:
    // https://github.com/google/wycheproof
    @Test
    fun hmacSha1Wycheproof() {
        runMacSuite(TestData.wycheproofSha1, ::hmacSha1, truncSide = "left")
    }

    @Test
    fun hmacSha256Wycheproof() {
        runMacSuite(TestData.wycheproofSha256, ::hmacSha256, truncSide = "left")
    }

    @Test
    fun hmacSha384Wycheproof() {
        runMacSuite(TestData.wycheproofSha384, ::hmacSha384, truncSide = "left")
    }

    @Test
    fun hmacSha512Wycheproof() {
        runMacSuite(TestData.wycheproofSha512, ::hmacSha512, truncSide = "left")
    }

    @Test
    fun hmacSha1WycheproofSimple() {
        runMacSuite(
            TestData.wycheproofSha1,
            { SimpleHmac.newFromSlice(it, { Sha1() }, { d -> d.copy() }).getOrThrow() },
            truncSide = "left",
        )
    }

    @Test
    fun hmacSha256WycheproofSimple() {
        runMacSuite(
            TestData.wycheproofSha256,
            { SimpleHmac.newFromSlice(it, { Sha2TestDigest.sha256() }, { d -> d.copy() }).getOrThrow() },
            truncSide = "left",
        )
    }

    @Test
    fun hmacSha384WycheproofSimple() {
        runMacSuite(
            TestData.wycheproofSha384,
            { SimpleHmac.newFromSlice(it, { Sha2TestDigest.sha384() }, { d -> d.copy() }).getOrThrow() },
            truncSide = "left",
        )
    }

    @Test
    fun hmacSha512WycheproofSimple() {
        runMacSuite(
            TestData.wycheproofSha512,
            { SimpleHmac.newFromSlice(it, { Sha2TestDigest.sha512() }, { d -> d.copy() }).getOrThrow() },
            truncSide = "left",
        )
    }

    @Test
    fun midStreamClonePreservesPrefixState() {
        val key = "Jefe".encodeToByteArray()
        val prefix = ByteArray(70) { (it and 0x7F).toByte() }
        val suffixA = "alpha".encodeToByteArray()
        val suffixB = "bravo".encodeToByteArray()

        val expectedA = hmacSha256(key).apply { update(prefix + suffixA) }.finalizeFixed()
        val expectedB = hmacSha256(key).apply { update(prefix + suffixB) }.finalizeFixed()

        val baseHmac = hmacSha256(key)
        baseHmac.update(prefix)
        val clonedHmac = baseHmac.clone()
        baseHmac.update(suffixA)
        clonedHmac.update(suffixB)
        assertContentEquals(expectedA, baseHmac.finalizeFixed())
        assertContentEquals(expectedB, clonedHmac.finalizeFixed())

        val sha1A = hmacSha1(key).apply { update(prefix + suffixA) }.finalizeFixed()
        val sha1B = hmacSha1(key).apply { update(prefix + suffixB) }.finalizeFixed()
        val baseSimpleSha1 = SimpleHmac.newFromSlice(key, { Sha1() }, { it.copy() }).getOrThrow()
        baseSimpleSha1.update(prefix)
        val clonedSimpleSha1 = baseSimpleSha1.clone()
        baseSimpleSha1.update(suffixA)
        clonedSimpleSha1.update(suffixB)
        assertContentEquals(sha1A, baseSimpleSha1.finalizeFixed())
        assertContentEquals(sha1B, clonedSimpleSha1.finalizeFixed())
    }

    @Test
    fun fromDigestRfc4231TestCase2AndFormatting() {
        val key = "Jefe".encodeToByteArray()
        val part1 = "what do ya want ".encodeToByteArray()
        val part2 = "for nothing?".encodeToByteArray()
        val expected =
            "5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843"
                .chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()

        val hmac = Hmac.fromDigest(64, 32, "Sha256", key, Sha256::digest)
        hmac.update(part1)
        val clonedHmac = hmac.clone()
        hmac.update("ignored".encodeToByteArray())
        clonedHmac.update(part2)
        assertContentEquals(expected, clonedHmac.finalizeFixed())
        assertEquals("Hmac<Sha256> { .. }", hmac.toString())

        hmac.reset()
        hmac.update(part1 + part2)
        assertContentEquals(expected, hmac.finalizeFixedReset())

        val simple = SimpleHmac.fromDigest(64, 32, "Sha256", key, Sha256::digest)
        simple.update(part1)
        val clonedSimple = simple.clone()
        simple.update("ignored".encodeToByteArray())
        clonedSimple.update(part2)
        assertContentEquals(expected, clonedSimple.finalizeFixed())
    }
}
