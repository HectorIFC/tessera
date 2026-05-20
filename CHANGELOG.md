# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- GitHub Actions CI workflow: tests, coverage ≥ 80%, ktlint, detekt (parallel jobs on every PR)
- GitHub Actions release workflow: SemVer bump via conventional commits, git tag, publish to GitHub Packages
- `.github/dependabot.yml`: weekly updates for Actions + Gradle deps (grouped: kotlin, kotest, kotlinx)
- `.github/pull_request_template.md`: standardized PR checklist
- ktlint 12.1.2 + detekt 1.23.7 integrated into Gradle build (`./gradlew ktlintCheck detekt`)
- `.editorconfig` for consistent formatting (IntelliJ code style, max line 120)
- `config/detekt/detekt.yml` with project-specific thresholds
- GitHub Packages repository configured in `tessera-core` publishing

### Phase 4 (validation)
- Kover 0.8.3 coverage plugin — 95% line coverage on `tessera-core`
- `RoundTripFuzzTest`: 2.212 random UTF-8 strings (ASCII, Latin, CJK, emoji, mixed), all passing
- `ComparisonTest`: granularity benchmarks vs tiktoken cl100k_base, compression ratio validation
- `BENCHMARKS.md`: documented test results, coverage, granularity numbers, manual validation checklist

### Phase 3 (CLI)
- `tessera-cli`: `train`, `encode`, `decode`, `inspect` commands with manual arg parser
- `tessera-cli`: `installDist` support via `application` plugin
- `tessera-cli/README.md` with usage instructions
- `tessera-core`: `BpeTokenizer.specialTokens` exposed as public property

### Phase 2 (samples)
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
