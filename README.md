# hmac-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fhmac--kotlin-blue.svg)](https://github.com/KotlinMania/hmac-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/hmac-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/hmac-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/hmac-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/hmac-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`RustCrypto/MACs`](https://github.com/RustCrypto/MACs).

**Original Project:** This port is based on [`RustCrypto/MACs`](https://github.com/RustCrypto/MACs). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `RustCrypto/MACs`

> The text below is reproduced and lightly edited from [`https://github.com/RustCrypto/MACs`](https://github.com/RustCrypto/MACs). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

## RustCrypto: Message Authentication Codes

[![Project Chat][chat-image]][chat-link]
[![dependency status][deps-image]][deps-link]
![Apache2/MIT licensed][license-image]

Collection of [Message Authentication Code][1] (MAC) algorithms written in pure Rust.

## Supported Algorithms

| Algorithm    | Crate          | Crates.io | Documentation | MSRV |
|--------------|----------------|:---------:|:-------------:|:----:|
| [BelT MAC]   | [`belt-mac`]   |   [![crates.io](https://img.shields.io/crates/v/belt-mac.svg)](https://crates.io/crates/belt-mac)   |   [![Documentation](https://docs.rs/belt-mac/badge.svg)](https://docs.rs/belt-mac)   | ![MSRV 1.85][msrv-1.85] |
| [CBC-MAC]    | [`cbc-mac`]    |    [![crates.io](https://img.shields.io/crates/v/cbc-mac.svg)](https://crates.io/crates/cbc-mac)    |    [![Documentation](https://docs.rs/cbc-mac/badge.svg)](https://docs.rs/cbc-mac)    | ![MSRV 1.85][msrv-1.85] |
| [CMAC]       | [`cmac`]       |       [![crates.io](https://img.shields.io/crates/v/cmac.svg)](https://crates.io/crates/cmac)       |       [![Documentation](https://docs.rs/cmac/badge.svg)](https://docs.rs/cmac)       | ![MSRV 1.85][msrv-1.85] |
| [HMAC]       | [`hmac`]       |       [![crates.io](https://img.shields.io/crates/v/hmac.svg)](https://crates.io/crates/hmac)       |       [![Documentation](https://docs.rs/hmac/badge.svg)](https://docs.rs/hmac)       | ![MSRV 1.85][msrv-1.85] |
| [PMAC]       | [`pmac`]       |       [![crates.io](https://img.shields.io/crates/v/pmac.svg)](https://crates.io/crates/pmac)       |       [![Documentation](https://docs.rs/pmac/badge.svg)](https://docs.rs/pmac)       | ![MSRV 1.85][msrv-1.85] |
| [Retail MAC] | [`retail-mac`] | [![crates.io](https://img.shields.io/crates/v/retail-mac.svg)](https://crates.io/crates/retail-mac) | [![Documentation](https://docs.rs/retail-mac/badge.svg)](https://docs.rs/retail-mac) | ![MSRV 1.85][msrv-1.85] |

## License

All crates licensed under either of

* [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
* [MIT license](http://opensource.org/licenses/MIT)

at your option.

### Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted for inclusion in the work by you, as defined in the Apache-2.0 license, shall be dual licensed as above, without any additional terms or conditions.

[//]: # (badges)

[chat-image]: https://img.shields.io/badge/zulip-join_chat-blue.svg
[chat-link]: https://rustcrypto.zulipchat.com/#narrow/stream/260044-MACs
[license-image]: https://img.shields.io/badge/license-Apache2.0/MIT-blue.svg
[deps-image]: https://deps.rs/repo/github/RustCrypto/MACs/status.svg
[deps-link]: https://deps.rs/repo/github/RustCrypto/MACs
[msrv-1.85]: https://img.shields.io/badge/rustc-1.85+-blue.svg

[//]: # (crates)

[`belt-mac`]: ./belt-mac
[`cbc-mac`]: ./cbc-mac
[`cmac`]: ./cmac
[`hmac`]: ./hmac
[`pmac`]: ./pmac
[`retail-mac`]: ./retail-mac

[//]: # (footnotes)

[1]: https://en.wikipedia.org/wiki/Message_authentication_code

[//]: # (algorithms)

[BelT MAC]: https://apmi.bsu.by/assets/files/std/belt-spec371.pdf
[CBC-MAC]: https://en.wikipedia.org/wiki/CBC-MAC
[CMAC]: https://en.wikipedia.org/wiki/One-key_MAC
[HMAC]: https://en.wikipedia.org/wiki/HMAC
[PMAC]: https://en.wikipedia.org/wiki/PMAC_(cryptography)
[Retail MAC]: https://en.wikipedia.org/wiki/ISO/IEC_9797-1#MAC_algorithm_3

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:hmac-kotlin:0.1.0")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same MIT license as the upstream [`RustCrypto/MACs`](https://github.com/RustCrypto/MACs). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the MACs authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`RustCrypto/MACs`](https://github.com/RustCrypto/MACs) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
