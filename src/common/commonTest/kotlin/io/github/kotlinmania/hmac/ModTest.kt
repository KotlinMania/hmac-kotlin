// port-lint: tests tests/mod.rs
package io.github.kotlinmania.hmac

import io.github.kotlinmania.sha1.Sha1
import io.github.kotlinmania.sha2.Sha224
import io.github.kotlinmania.sha2.Sha256
import io.github.kotlinmania.sha2.Sha384
import io.github.kotlinmania.sha2.Sha512
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals

class ModTest {
    @BeforeTest
    fun setUp() {
        registerHasher(
            type = Sha1::class,
            blockSize = 64,
            outputSize = 20,
            algName = "Sha1",
            create = { Sha1() },
            update = { h, d -> h.update(d) },
            finalize = { h -> h.finalize() },
            reset = { h -> h.reset() },
            digest = { d -> Sha1.digest(d) },
        )
        registerHasher(
            type = Sha224::class,
            blockSize = 64,
            outputSize = 28,
            algName = "Sha224",
            create = { Sha224() },
            update = { h, d -> h.update(d) },
            finalize = { h -> h.finalize() },
            reset = { h -> h.reset() },
            digest = { d -> Sha224.digest(d) },
        )
        registerHasher(
            type = Sha256::class,
            blockSize = 64,
            outputSize = 32,
            algName = "Sha256",
            create = { Sha256() },
            update = { h, d -> h.update(d) },
            finalize = { h -> h.finalize() },
            reset = { h -> h.reset() },
            digest = { d -> Sha256.digest(d) },
        )
        registerHasher(
            type = Sha384::class,
            blockSize = 128,
            outputSize = 48,
            algName = "Sha384",
            create = { Sha384() },
            update = { h, d -> h.update(d) },
            finalize = { h -> h.finalize() },
            reset = { h -> h.reset() },
            digest = { d -> Sha384.digest(d) },
        )
        registerHasher(
            type = Sha512::class,
            blockSize = 128,
            outputSize = 64,
            algName = "Sha512",
            create = { Sha512() },
            update = { h, d -> h.update(d) },
            finalize = { h -> h.finalize() },
            reset = { h -> h.reset() },
            digest = { d -> Sha512.digest(d) },
        )
    }

    // RFC 2104 (MD5) tests unported: md5 crate is not yet ported to KotlinMania
    // test!(hmac_md5_rfc2104, "md5", Hmac<md5::Md5>);
    // test!(hmac_md5_rfc2104_simple, "md5", SimpleHmac<md5::Md5>);

    // Test vectors from RFC 4231
    @Test
    fun hmacSha224Rfc4231() {
        runMacSuite(TestData.sha224, { Hmac.newFromSlice<Sha224>(it).getOrThrow() })
    }

    @Test
    fun hmacSha256Rfc4231() {
        runMacSuite(TestData.sha256, { Hmac.newFromSlice<Sha256>(it).getOrThrow() })
    }

    @Test
    fun hmacSha384Rfc4231() {
        runMacSuite(TestData.sha384, { Hmac.newFromSlice<Sha384>(it).getOrThrow() })
    }

    @Test
    fun hmacSha512Rfc4231() {
        runMacSuite(TestData.sha512, { Hmac.newFromSlice<Sha512>(it).getOrThrow() })
    }

    @Test
    fun hmacSha224Rfc4231Simple() {
        runMacSuite(TestData.sha224, { SimpleHmac.newFromSlice<Sha224>(it).getOrThrow() })
    }

    @Test
    fun hmacSha256Rfc4231Simple() {
        runMacSuite(TestData.sha256, { SimpleHmac.newFromSlice<Sha256>(it).getOrThrow() })
    }

    @Test
    fun hmacSha384Rfc4231Simple() {
        runMacSuite(TestData.sha384, { SimpleHmac.newFromSlice<Sha384>(it).getOrThrow() })
    }

    @Test
    fun hmacSha512Rfc4231Simple() {
        runMacSuite(TestData.sha512, { SimpleHmac.newFromSlice<Sha512>(it).getOrThrow() })
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
        runMacSuite(TestData.wycheproofSha1, { Hmac.newFromSlice<Sha1>(it).getOrThrow() }, truncSide = "left")
    }

    @Test
    fun hmacSha256Wycheproof() {
        runMacSuite(TestData.wycheproofSha256, { Hmac.newFromSlice<Sha256>(it).getOrThrow() }, truncSide = "left")
    }

    @Test
    fun hmacSha384Wycheproof() {
        runMacSuite(TestData.wycheproofSha384, { Hmac.newFromSlice<Sha384>(it).getOrThrow() }, truncSide = "left")
    }

    @Test
    fun hmacSha512Wycheproof() {
        runMacSuite(TestData.wycheproofSha512, { Hmac.newFromSlice<Sha512>(it).getOrThrow() }, truncSide = "left")
    }

    @Test
    fun hmacSha1WycheproofSimple() {
        runMacSuite(TestData.wycheproofSha1, { SimpleHmac.newFromSlice<Sha1>(it).getOrThrow() }, truncSide = "left")
    }

    @Test
    fun hmacSha256WycheproofSimple() {
        runMacSuite(TestData.wycheproofSha256, { SimpleHmac.newFromSlice<Sha256>(it).getOrThrow() }, truncSide = "left")
    }

    @Test
    fun hmacSha384WycheproofSimple() {
        runMacSuite(TestData.wycheproofSha384, { SimpleHmac.newFromSlice<Sha384>(it).getOrThrow() }, truncSide = "left")
    }

    @Test
    fun hmacSha512WycheproofSimple() {
        runMacSuite(TestData.wycheproofSha512, { SimpleHmac.newFromSlice<Sha512>(it).getOrThrow() }, truncSide = "left")
    }

    @Test
    fun fromDigestRfc4231TestCase2() {
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
        hmac.update(part2)
        assertContentEquals(expected, hmac.finalizeFixed())
        hmac.reset()
        hmac.update(part1 + part2)
        assertContentEquals(expected, hmac.finalizeFixedReset())

        val simple = SimpleHmac.fromDigest(64, 32, "Sha256", key, Sha256::digest)
        val cloned = simple.clone()
        simple.update("ignored".encodeToByteArray())
        cloned.update(part1)
        cloned.update(part2)
        assertContentEquals(expected, cloned.finalizeFixed())
    }
}
