# tessera-core

The published library module of Tessera — a byte-level BPE tokenizer in pure Kotlin.

## Adding the dependency

### Gradle (Kotlin DSL) — via JitPack

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts
dependencies {
    implementation("com.github.HectorIFC:tessera:tessera-core-v0.0.6")
}
```

### Gradle (Kotlin DSL) — via GitHub Packages

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/HectorIFC/tessera")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

// build.gradle.kts
dependencies {
    implementation("dev.tessera:tessera-core:0.0.6")
}
```

## Public API

### BpeTokenizer

```kotlin
// Encode text → token IDs
val ids: IntArray = tokenizer.encode("Hello, world!")

// Encode with special tokens
val ids = tokenizer.encode(
    text = "Hello<|endoftext|>",
    allowedSpecialTokens = setOf("<|endoftext|>")
)

// Decode token IDs → text
val text: String = tokenizer.decode(ids)

// Inspect a token
val bytes: ByteArray = tokenizer.tokenAsBytes(id)
val str: String = tokenizer.tokenAsString(id)

// Vocabulary size
val size: Int = tokenizer.vocabSize

// Save and load
tokenizer.save("tessera.json")
val loaded = BpeTokenizer.load("tessera.json")
```

### Trainer

```kotlin
val tokenizer = Trainer(
    TrainingConfig(
        numMerges = 10_000,
        verbose = false,
        progressCallback = { p ->
            println("${p.mergesCompleted}/${p.totalMerges}")
        }
    )
).trainFromFile("corpus/text.txt")
```

### SpecialTokens

```kotlin
// Default: only <|endoftext|> at ID 256
val st = SpecialTokens.default()

// Custom set for a chat model
val st = SpecialTokens.of("<|endoftext|>", "<|user|>", "<|assistant|>")
// → IDs: 256, 257, 258

val config = TrainingConfig(specialTokens = st)
```

## Runtime dependencies

`tessera-core` has exactly two runtime dependencies:

| Dependency | Version | Purpose |
|---|---|---|
| `org.jetbrains.kotlin:kotlin-stdlib` | 2.1.0 | Kotlin runtime |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | 1.7.3 | JSON save/load |

Verify at any time:
```bash
./gradlew :tessera-core:dependencies --configuration runtimeClasspath
```

## Building locally

```bash
# Build and test
./gradlew :tessera-core:build

# Install to Maven Local
./gradlew :tessera-core:publishToMavenLocal

# Generate coverage report
./gradlew :tessera-core:koverHtmlReport
# → tessera-core/build/reports/kover/html/index.html
```

## See also

- [ARCHITECTURE.md](../ARCHITECTURE.md) — detailed explanation of the BPE algorithm and internals
- [BENCHMARKS.md](../BENCHMARKS.md) — round-trip results, coverage, and granularity numbers
- [tessera-samples](../tessera-samples/) — runnable usage examples
