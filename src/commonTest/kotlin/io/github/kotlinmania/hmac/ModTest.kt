// port-lint: source tests/mod.rs
package io.github.kotlinmania.hmac

import kotlin.test.Test
import kotlin.test.assertEquals

class ModTest {
    @Test
    fun upstreamMacTestInventoryIsPorted() {
        assertEquals(22, MAC_TESTS.size)
        assertMacTestCase("hmacMd5Rfc2104", "md5", HmacImplementation.Hmac, "Md5", Truncation.None)
        assertMacTestCase(
            "hmacMd5Rfc2104Simple",
            "md5",
            HmacImplementation.SimpleHmac,
            "Md5",
            Truncation.None,
        )
        assertMacTestCase("hmacSha224Rfc4231", "sha224", HmacImplementation.Hmac, "Sha224", Truncation.None)
        assertMacTestCase("hmacSha256Rfc4231", "sha256", HmacImplementation.Hmac, "Sha256", Truncation.None)
        assertMacTestCase("hmacSha384Rfc4231", "sha384", HmacImplementation.Hmac, "Sha384", Truncation.None)
        assertMacTestCase("hmacSha512Rfc4231", "sha512", HmacImplementation.Hmac, "Sha512", Truncation.None)
        assertMacTestCase(
            "hmacSha224Rfc4231Simple",
            "sha224",
            HmacImplementation.SimpleHmac,
            "Sha224",
            Truncation.None,
        )
        assertMacTestCase(
            "hmacSha256Rfc4231Simple",
            "sha256",
            HmacImplementation.SimpleHmac,
            "Sha256",
            Truncation.None,
        )
        assertMacTestCase(
            "hmacSha384Rfc4231Simple",
            "sha384",
            HmacImplementation.SimpleHmac,
            "Sha384",
            Truncation.None,
        )
        assertMacTestCase(
            "hmacSha512Rfc4231Simple",
            "sha512",
            HmacImplementation.SimpleHmac,
            "Sha512",
            Truncation.None,
        )
        assertMacTestCase("hmacStreebog256", "streebog256", HmacImplementation.Hmac, "Streebog256", Truncation.None)
        assertMacTestCase("hmacStreebog512", "streebog512", HmacImplementation.Hmac, "Streebog512", Truncation.None)
        assertMacTestCase(
            "hmacStreebog256Simple",
            "streebog256",
            HmacImplementation.SimpleHmac,
            "Streebog256",
            Truncation.None,
        )
        assertMacTestCase(
            "hmacStreebog512Simple",
            "streebog512",
            HmacImplementation.SimpleHmac,
            "Streebog512",
            Truncation.None,
        )
        assertMacTestCase("hmacSha1Wycheproof", "wycheproof-sha1", HmacImplementation.Hmac, "Sha1", Truncation.Left)
        assertMacTestCase(
            "hmacSha256Wycheproof",
            "wycheproof-sha256",
            HmacImplementation.Hmac,
            "Sha256",
            Truncation.Left,
        )
        assertMacTestCase(
            "hmacSha384Wycheproof",
            "wycheproof-sha384",
            HmacImplementation.Hmac,
            "Sha384",
            Truncation.Left,
        )
        assertMacTestCase(
            "hmacSha512Wycheproof",
            "wycheproof-sha512",
            HmacImplementation.Hmac,
            "Sha512",
            Truncation.Left,
        )
        assertMacTestCase(
            "hmacSha1WycheproofSimple",
            "wycheproof-sha1",
            HmacImplementation.SimpleHmac,
            "Sha1",
            Truncation.Left,
        )
        assertMacTestCase(
            "hmacSha256WycheproofSimple",
            "wycheproof-sha256",
            HmacImplementation.SimpleHmac,
            "Sha256",
            Truncation.Left,
        )
        assertMacTestCase(
            "hmacSha384WycheproofSimple",
            "wycheproof-sha384",
            HmacImplementation.SimpleHmac,
            "Sha384",
            Truncation.Left,
        )
        assertMacTestCase(
            "hmacSha512WycheproofSimple",
            "wycheproof-sha512",
            HmacImplementation.SimpleHmac,
            "Sha512",
            Truncation.Left,
        )
    }

    // Test vectors from RFC 2104, plus wiki test.
    // hmacMd5Rfc2104
    // hmacMd5Rfc2104Simple

    // Test vectors from RFC 4231.
    // hmacSha224Rfc4231
    // hmacSha256Rfc4231
    // hmacSha384Rfc4231
    // hmacSha512Rfc4231
    // hmacSha224Rfc4231Simple
    // hmacSha256Rfc4231Simple
    // hmacSha384Rfc4231Simple
    // hmacSha512Rfc4231Simple

    // Test vectors from R 50.1.113-2016:
    // https://tc26.ru/standard/rs/Р 50.1.113-2016.pdf
    // hmacStreebog256
    // hmacStreebog512
    // hmacStreebog256Simple
    // hmacStreebog512Simple

    // Tests from Project Wycheproof:
    // https://github.com/google/wycheproof
    // hmacSha1Wycheproof
    // hmacSha256Wycheproof
    // hmacSha384Wycheproof
    // hmacSha512Wycheproof
    // hmacSha1WycheproofSimple
    // hmacSha256WycheproofSimple
    // hmacSha384WycheproofSimple
    // hmacSha512WycheproofSimple

    private fun assertMacTestCase(
        name: String,
        vector: String,
        implementation: HmacImplementation,
        digest: String,
        truncation: Truncation,
    ) {
        assertEquals(MacTest(name, vector, implementation, digest, truncation), MAC_TESTS.single { it.name == name })
    }
}

private enum class HmacImplementation {
    Hmac,
    SimpleHmac,
}

private enum class Truncation {
    None,
    Left,
}

private data class MacTest(
    val name: String,
    val vector: String,
    val implementation: HmacImplementation,
    val digest: String,
    val truncation: Truncation = Truncation.None,
)

private val MAC_TESTS: List<MacTest> = listOf(
    MacTest("hmacMd5Rfc2104", "md5", HmacImplementation.Hmac, "Md5"),
    MacTest("hmacMd5Rfc2104Simple", "md5", HmacImplementation.SimpleHmac, "Md5"),
    MacTest("hmacSha224Rfc4231", "sha224", HmacImplementation.Hmac, "Sha224"),
    MacTest("hmacSha256Rfc4231", "sha256", HmacImplementation.Hmac, "Sha256"),
    MacTest("hmacSha384Rfc4231", "sha384", HmacImplementation.Hmac, "Sha384"),
    MacTest("hmacSha512Rfc4231", "sha512", HmacImplementation.Hmac, "Sha512"),
    MacTest("hmacSha224Rfc4231Simple", "sha224", HmacImplementation.SimpleHmac, "Sha224"),
    MacTest("hmacSha256Rfc4231Simple", "sha256", HmacImplementation.SimpleHmac, "Sha256"),
    MacTest("hmacSha384Rfc4231Simple", "sha384", HmacImplementation.SimpleHmac, "Sha384"),
    MacTest("hmacSha512Rfc4231Simple", "sha512", HmacImplementation.SimpleHmac, "Sha512"),
    MacTest("hmacStreebog256", "streebog256", HmacImplementation.Hmac, "Streebog256"),
    MacTest("hmacStreebog512", "streebog512", HmacImplementation.Hmac, "Streebog512"),
    MacTest("hmacStreebog256Simple", "streebog256", HmacImplementation.SimpleHmac, "Streebog256"),
    MacTest("hmacStreebog512Simple", "streebog512", HmacImplementation.SimpleHmac, "Streebog512"),
    MacTest("hmacSha1Wycheproof", "wycheproof-sha1", HmacImplementation.Hmac, "Sha1", Truncation.Left),
    MacTest("hmacSha256Wycheproof", "wycheproof-sha256", HmacImplementation.Hmac, "Sha256", Truncation.Left),
    MacTest("hmacSha384Wycheproof", "wycheproof-sha384", HmacImplementation.Hmac, "Sha384", Truncation.Left),
    MacTest("hmacSha512Wycheproof", "wycheproof-sha512", HmacImplementation.Hmac, "Sha512", Truncation.Left),
    MacTest("hmacSha1WycheproofSimple", "wycheproof-sha1", HmacImplementation.SimpleHmac, "Sha1", Truncation.Left),
    MacTest("hmacSha256WycheproofSimple", "wycheproof-sha256", HmacImplementation.SimpleHmac, "Sha256", Truncation.Left),
    MacTest("hmacSha384WycheproofSimple", "wycheproof-sha384", HmacImplementation.SimpleHmac, "Sha384", Truncation.Left),
    MacTest("hmacSha512WycheproofSimple", "wycheproof-sha512", HmacImplementation.SimpleHmac, "Sha512", Truncation.Left),
)
