# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] — 2026-05-20

### Added

#### Phase 5 — Publication & polish
- `ARCHITECTURE.md`: detailed explanation of byte-level BPE, pre-tokenization, training, encoding, decoding, persistence, and module structure
- `tessera-core/README.md`: module-specific docs with dependency snippets (JitPack + GitHub Packages), full API reference, and local build instructions
- Complete KDoc on all public API symbols (`BpeTokenizer`, `Trainer`, `TrainingConfig`, `TrainingProgress`, `SpecialTokens`) with `@param`, `@return`, `@throws`, and usage examples
- `README.md` translated to English, roadmap and status updated to v1.0.0
- All documentation files translated to English (`BENCHMARKS.md`, `BOOTSTRAP.md`, `.gitignore`, PR template)

#### Phase 4 — Validation & quality
- GitHub Actions CI workflow: tests, coverage ≥ 80%, ktlint, detekt (parallel jobs on every PR)
- GitHub Actions release workflow: SemVer bump via conventional commits, git tag, publish to GitHub Packages
- `.github/dependabot.yml`: weekly updates for Actions + Gradle deps (grouped: kotlin, kotest, kotlinx)
- `.github/pull_request_template.md`: standardized PR checklist
- ktlint 12.1.2 + detekt 1.23.7 integrated into Gradle build
- `.editorconfig` for consistent formatting (IntelliJ code style, max line 120)
- `config/detekt/detekt.yml` with project-specific thresholds
- Kover 0.8.3 coverage plugin — 95% line coverage on `tessera-core`
- `RoundTripFuzzTest`: 2,212 random UTF-8 strings (ASCII, Latin, CJK, emoji, mixed), all passing
- `ComparisonTest`: granularity benchmarks vs tiktoken cl100k_base, compression ratio validation
- `BENCHMARKS.md`: documented test results, coverage, granularity numbers, manual validation checklist

#### Phase 3 — CLI
- `tessera-cli`: `train`, `encode`, `decode`, `inspect` commands with manual arg parser
- `tessera-cli`: `installDist` support via `application` plugin
- `tessera-cli/README.md` with usage instructions
- `tessera-core`: `BpeTokenizer.specialTokens` exposed as public property

#### Phase 2 — Samples
- `tessera-samples`: `QuickStartSample` — minimal train → encode → decode → round-trip
- `tessera-samples`: `TrainingSample` — training config options and progress callback
- `tessera-samples`: `SpecialTokensSample` — custom special tokens, allowed vs. disallowed
- `tessera-samples`: `PersistenceSample` — train, save to disk, load, verify identical encoding
- `tessera-samples/README.md` with run instructions for each sample

#### Phase 1 — Core library
- `tessera-core`: full byte-level BPE trainer with -1 sentinel, greedy encode by lowest rank
- `tessera-core`: `BpeTokenizer` encode/decode with special token handling
- `tessera-core`: `Persistence` save/load JSON via `kotlinx.serialization`
- `tessera-core`: 48 tests passing across 8 test classes

#### Phase 0 — Setup
- Gradle 8.14.1 multi-module project (`tessera-core`, `tessera-cli`, `tessera-samples`)
- Root build config: Kotlin 2.1.0, JVM 21 toolchain
- `tessera-core`: `maven-publish` + `java-library` + `explicitApi()` enforced
- `.gitignore` for Gradle multi-module + Tessera-specific artifacts
- `corpus/.gitkeep`, `gradle.properties` (`version=1.0.0`), initial `CHANGELOG.md`
