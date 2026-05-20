# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Gradle multi-module project setup (`tessera-core`, `tessera-cli`, `tessera-samples`)
- Root build config: Kotlin 2.1.0, JVM 21 toolchain
- `tessera-core`: `maven-publish` + `java-library` plugins, `explicitApi()` enforced
- `tessera-core`: public API skeletons — `BpeTokenizer`, `Trainer`, `TrainingConfig`, `TrainingProgress`, `SpecialTokens`
- `tessera-core`: internal placeholders — `PreTokenizer`, `Persistence`, `ByteUtils`, `Gpt4Pattern`
- `tessera-core`: hello-world test suite passing
- `.gitignore` for Gradle multi-module + Tessera-specific artefacts
- `corpus/.gitkeep`
