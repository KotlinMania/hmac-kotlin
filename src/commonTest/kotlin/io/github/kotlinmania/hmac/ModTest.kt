// port-lint: source tests/mod.rs
package io.github.kotlinmania.hmac

import io.github.kotlinmania.digest.newMacTest
import io.github.kotlinmania.digest.truncLeft
import io.github.kotlinmania.md5.Md5
import io.github.kotlinmania.sha1.Sha1
import io.github.kotlinmania.sha2.Sha224
import io.github.kotlinmania.sha2.Sha256
import io.github.kotlinmania.sha2.Sha384
import io.github.kotlinmania.sha2.Sha512
import io.github.kotlinmania.streebog.Streebog256
import io.github.kotlinmania.streebog.Streebog512
import kotlin.test.Test

class ModTest {
    // Test vectors from RFC 2104, plus wiki test
    @Test
    fun hmacMd5Rfc2104() {
        newMacTest<Hmac<Md5>>("md5")
    }

    @Test
    fun hmacMd5Rfc2104Simple() {
        newMacTest<SimpleHmac<Md5>>("md5")
    }

    // Test vectors from RFC 4231
    @Test
    fun hmacSha224Rfc4231() {
        newMacTest<Hmac<Sha224>>("sha224")
    }

    @Test
    fun hmacSha256Rfc4231() {
        newMacTest<Hmac<Sha256>>("sha256")
    }

    @Test
    fun hmacSha384Rfc4231() {
        newMacTest<Hmac<Sha384>>("sha384")
    }

    @Test
    fun hmacSha512Rfc4231() {
        newMacTest<Hmac<Sha512>>("sha512")
    }

    @Test
    fun hmacSha224Rfc4231Simple() {
        newMacTest<SimpleHmac<Sha224>>("sha224")
    }

    @Test
    fun hmacSha256Rfc4231Simple() {
        newMacTest<SimpleHmac<Sha256>>("sha256")
    }

    @Test
    fun hmacSha384Rfc4231Simple() {
        newMacTest<SimpleHmac<Sha384>>("sha384")
    }

    @Test
    fun hmacSha512Rfc4231Simple() {
        newMacTest<SimpleHmac<Sha512>>("sha512")
    }

    // Test vectors from R 50.1.113-2016:
    // https://tc26.ru/standard/rs/Р 50.1.113-2016.pdf
    @Test
    fun hmacStreebog256() {
        newMacTest<Hmac<Streebog256>>("streebog256")
    }

    @Test
    fun hmacStreebog512() {
        newMacTest<Hmac<Streebog512>>("streebog512")
    }

    @Test
    fun hmacStreebog256Simple() {
        newMacTest<SimpleHmac<Streebog256>>("streebog256")
    }

    @Test
    fun hmacStreebog512Simple() {
        newMacTest<SimpleHmac<Streebog512>>("streebog512")
    }

    // Tests from Project Wycheproof:
    // https://github.com/google/wycheproof
    @Test
    fun hmacSha1Wycheproof() {
        newMacTest<Hmac<Sha1>>("wycheproof-sha1", ::truncLeft)
    }

    @Test
    fun hmacSha256Wycheproof() {
        newMacTest<Hmac<Sha256>>("wycheproof-sha256", ::truncLeft)
    }

    @Test
    fun hmacSha384Wycheproof() {
        newMacTest<Hmac<Sha384>>("wycheproof-sha384", ::truncLeft)
    }

    @Test
    fun hmacSha512Wycheproof() {
        newMacTest<Hmac<Sha512>>("wycheproof-sha512", ::truncLeft)
    }

    @Test
    fun hmacSha1WycheproofSimple() {
        newMacTest<SimpleHmac<Sha1>>("wycheproof-sha1", ::truncLeft)
    }

    @Test
    fun hmacSha256WycheproofSimple() {
        newMacTest<SimpleHmac<Sha256>>("wycheproof-sha256", ::truncLeft)
    }

    @Test
    fun hmacSha384WycheproofSimple() {
        newMacTest<SimpleHmac<Sha384>>("wycheproof-sha384", ::truncLeft)
    }

    @Test
    fun hmacSha512WycheproofSimple() {
        newMacTest<SimpleHmac<Sha512>>("wycheproof-sha512", ::truncLeft)
    }
}
