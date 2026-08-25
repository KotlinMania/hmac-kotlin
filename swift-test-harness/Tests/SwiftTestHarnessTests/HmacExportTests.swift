import Testing
import Hmac

@Suite("Hmac Export Tests")
struct HmacExportTests {
    @Test("swift module imports cleanly")
    func swiftModuleLoads() throws {
        #expect(Bool(true))
    }
}
