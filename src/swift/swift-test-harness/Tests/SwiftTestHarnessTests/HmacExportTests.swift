import CryptoKit
import Foundation
import Hmac
import KotlinStdlib
import Testing

private func makeKotlinByteArray(_ bytes: [UInt8]) -> ExportedKotlinPackages.kotlin.ByteArray {
    ExportedKotlinPackages.kotlin.ByteArray(size: Int32(bytes.count)) { idx in
        Int8(bitPattern: bytes[Int(idx)])
    }
}

private func toSwiftBytes(_ array: ExportedKotlinPackages.kotlin.ByteArray) -> [UInt8] {
    (0 ..< Int(array.size)).map { idx in
        UInt8(bitPattern: array._get(index: Int32(idx)))
    }
}

private func hexBytes(_ hex: String) -> [UInt8] {
    precondition(hex.count % 2 == 0)
    var result: [UInt8] = []
    result.reserveCapacity(hex.count / 2)
    var index = hex.startIndex
    while index < hex.endIndex {
        let next = hex.index(index, offsetBy: 2)
        result.append(UInt8(hex[index ..< next], radix: 16)!)
        index = next
    }
    return result
}

private func sha256(_ input: ExportedKotlinPackages.kotlin.ByteArray) -> ExportedKotlinPackages.kotlin.ByteArray {
    let digest = SHA256.hash(data: Data(toSwiftBytes(input)))
    return makeKotlinByteArray(Array(digest))
}

@Suite("Hmac Export Tests")
struct HmacExportTests {
    @Test("Hmac SHA-256 matches RFC 4231 Test Case 2, resets, and clones mid-stream")
    func hmacSha256Rfc4231TestCase2AndClone() throws {
        let key = makeKotlinByteArray(Array("Jefe".utf8))
        let msgPart1 = makeKotlinByteArray(Array("what do ya want ".utf8))
        let msgPart2 = makeKotlinByteArray(Array("for nothing?".utf8))
        let expected = hexBytes("5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843")

        let mac = Hmac.Companion.shared.fromDigest(
            blockSize: 64,
            outputSize: 32,
            algName: "Sha256",
            key: key,
            digest: sha256
        )
        #expect(mac.blockSize == 64)
        #expect(mac.outputSize == 32)

        mac.update(data: msgPart1)
        let midCloned = mac.clone()
        mac.update(data: makeKotlinByteArray(Array("ignored".utf8)))
        let chained = midCloned.chain(data: msgPart2)
        #expect(toSwiftBytes(chained.finalizeFixed()) == expected)

        mac.reset()
        mac.update(data: msgPart1)
        mac.update(data: msgPart2)
        #expect(toSwiftBytes(mac.finalizeFixedReset()) == expected)
    }

    @Test("SimpleHmac SHA-256 matches RFC 4231 Test Case 1 and clones mid-stream")
    func simpleHmacSha256Rfc4231TestCase1AndClone() throws {
        let key = makeKotlinByteArray([UInt8](repeating: 0x0B, count: 20))
        let part1 = makeKotlinByteArray(Array("Hi ".utf8))
        let part2 = makeKotlinByteArray(Array("There".utf8))
        let expected = hexBytes("b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9376c2e32cff7")

        let mac = SimpleHmac.Companion.shared.fromDigest(
            blockSize: 64,
            outputSize: 32,
            algName: "Sha256",
            key: key,
            digest: sha256
        )
        #expect(mac.keySize == 64)
        #expect(mac.blockSize == 64)
        #expect(mac.outputSize == 32)

        mac.update(data: part1)
        let cloned = mac.clone()
        mac.update(data: makeKotlinByteArray(Array("ignored".utf8)))
        cloned.update(data: part2)
        #expect(toSwiftBytes(cloned.finalizeFixed()) == expected)
    }

    @Test("HmacCore handles keys longer than block size and clones")
    func hmacCoreLongKeyAndClone() throws {
        let longKey = makeKotlinByteArray([UInt8](repeating: 0xAA, count: 131))
        let core = HmacCore.Companion.shared.fromDigest(
            blockSize: 64,
            outputSize: 32,
            algName: "Sha256",
            key: longKey,
            digest: sha256
        )
        #expect(core.keySize == 64)
        #expect(core.blockSize == 64)
        #expect(core.outputSize == 32)

        let cloned = core.clone()
        #expect(cloned.outputSize == 32)
    }
}
