# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- `tessera-samples`: `QuickStartSample` — minimal train → encode → decode → round-trip
- `tessera-samples`: `TrainingSample` — training config options and progress callback
- `tessera-samples`: `SpecialTokensSample` — custom special tokens, allowed vs. disallowed
- `tessera-samples`: `PersistenceSample` — train, save to disk, load, verify identical encoding
- `tessera-samples/README.md` with run instructions for each sample
- `tessera-samples/build.gradle.kts` configured with `application` plugin + `-PmainClass` support

### Phase 1 (core lib)
- `tessera-core`: full byte-level BPE trainer with -1 sentinel, greedy encode by lowest rank
- `tessera-core`: `BpeTokenizer` encode/decode with special token handling
- `tessera-core`: `Persistence` save/load JSON via `kotlinx.serialization`
- `tessera-core`: 36 tests passing across 5 test classes (round-trip, BPE, persistence, pre-tokenizer, API contract)

### Phase 0 (setup)
- Gradle multi-module project setup (`tessera-core`, `tessera-cli`, `tessera-samples`)
- Root build config: Kotlin 2.1.0, JVM 21 toolchain
- `tessera-core`: `maven-publish` + `java-library` plugins, `explicitApi()` enforced
- `.gitignore` for Gradle multi-module + Tessera-specific artefacts
- `corpus/.gitkeep`
