# port-lint Proposed Changes

**Generated:** 2026-05-19
**Source:** tmp/hmac/src
**Target:** src/commonMain/kotlin/io/github/kotlinmania/hmac

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `src/commonMain/kotlin/io/github/kotlinmania/hmac/Optim.kt` | `// port-lint: source src/optim.rs` | `// port-lint: source optim.rs` | `optim.rs` | `port-lint provenance header matched only after fallback normalization: 'src/optim.rs' vs expected 'optim.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/hmac/Simple.kt` | `// port-lint: source src/simple.rs` | `// port-lint: source simple.rs` | `simple.rs` | `port-lint provenance header matched only after fallback normalization: 'src/simple.rs' vs expected 'simple.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/hmac/Lib.kt` | `// port-lint: source src/lib.rs` | `// port-lint: source lib.rs` | `lib.rs` | `port-lint provenance header matched only after fallback normalization: 'src/lib.rs' vs expected 'lib.rs'` |
