# Tessera — Benchmarks

> Results measured in Phase 4. Update before each release using a real corpus.

---

## 1. Test environment

| Item | Value |
|---|---|
| JDK | OpenJDK 21 |
| Kotlin | 2.1.0 |
| Hardware | macOS (Apple Silicon) |
| Training corpus (tests) | ~145 KB generated in-memory |
| Training corpus (production) | TBD — Wikipedia PT-BR + EN recommended |

---

## 2. Round-trip (decode(encode(x)) == x)

Validated via `RoundTripFuzzTest` and `RoundTripTest`.

| Category | Strings tested | Result |
|---|---|---|
| Random ASCII (32–126) | 1,000 | ✅ 100% |
| Latin Extended (accents, diacritics) | 500 | ✅ 100% |
| CJK (U+4E00–U+9FFF) | 300 | ✅ 100% |
| Emoji (via surrogate pairs) | 200 | ✅ 100% |
| Mixed Unicode | 200 | ✅ 100% |
| Edge cases (empty, whitespace, newlines) | 12 | ✅ 100% |
| **Total** | **2,212** | **✅ 100%** |

---

## 3. Code coverage

Measured with Kover on `tessera-core`.

| Metric | Result | PRD §3.4 threshold |
|---|---|---|
| Line coverage | **95%** | ≥ 80% ✅ |

---

## 4. Granularity vs tiktoken cl100k_base

tiktoken reference counts measured at [tiktokenizer.vercel.app](https://tiktokenizer.vercel.app).

### 4.1 In-memory test corpus (~145 KB, 1,000 merges)

| Language | Tessera tok/word | tiktoken tok/word | Ratio Tessera/tiktoken |
|---|---|---|---|
| English | 2.69 | 1.14 | 2.36x |
| Portuguese | 3.10 | 1.40 | 2.22x |

**Note:** PRD §3.2 sets the ≤ 1.5x ratio criterion for corpus ≥ 100 MB. With only ~145 KB of training data the tokenizer has not seen enough vocabulary to compress diverse text well — words like "artificial", "intelligence", "seashells" were absent from training. This is expected and correct.

### 4.2 Well-known sentence from training corpus

| Sentence | Tessera | tiktoken | Ratio |
|---|---|---|---|
| "the quick brown fox jumps over the lazy dog" | **9 tokens** | 10 tokens | **0.9x** ✅ |

For sentences present in the training corpus, Tessera outperforms tiktoken.

### 4.3 Compression vs raw bytes (English sentences)

| Metric | Value |
|---|---|
| Total tokens (Tessera) | 179 |
| Total bytes (raw UTF-8) | 339 |
| Token/byte ratio | **0.53** (47% compression) |

### 4.4 Effect of merge count

Sentence: "the quick brown fox jumps over the lazy dog"

| Merges | Tokens |
|---|---|
| 500 | 9 |
| 1,000 | 9 |
| 2,000 | 9 |

Already stable at 500 merges for this sentence — corpus words are well learned.

---

## 5. Planned manual validation

To meet PRD §3.2 (ratio ≤ 1.5x), train on corpus ≥ 100 MB and verify manually:

```bash
# Download corpus
wget https://dumps.wikimedia.org/ptwiki/latest/ptwiki-latest-pages-articles.xml.bz2

# Train with 30,000 merges
./gradlew :tessera-cli:run --args="train --corpus /path/to/corpus.txt --merges 30000 --output tessera-30k.json"

# Verify granularity
./gradlew :tessera-cli:run --args="encode --tokenizer tessera-30k.json --text 'the quick brown fox'"
# Compare against tiktokenizer.vercel.app
```

Criteria to verify manually:
- [ ] Tessera/tiktoken ratio EN: between 0.7x and 1.5x
- [ ] Tessera/tiktoken ratio PT: between 0.7x and 1.5x
- [ ] `the`, `de`, `and`, `que` → 1 token each
- [ ] Full training on 50 MB corpus completes without errors
- [ ] Encoding 1 MB of text runs in under 30 seconds

---

## 6. Total tests

| Suite | Tests | Status |
|---|---|---|
| HelloWorldTest | 3 | ✅ |
| PreTokenizerTest | 6 | ✅ |
| BpeTokenizerTest | 7 | ✅ |
| RoundTripTest | 10 | ✅ |
| PersistenceTest | 4 | ✅ |
| ApiContractTest | 5 | ✅ |
| RoundTripFuzzTest | 6 | ✅ |
| ComparisonTest | 6 | ✅ |
| **Total** | **47** | **✅ 100%** |
