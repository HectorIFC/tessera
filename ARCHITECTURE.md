# Tessera — Architecture

This document explains how Tessera works internally: the BPE algorithm, the pre-tokenization strategy, the encoding pipeline, and the module structure. It is aimed at contributors and at developers who want to understand the mechanics before using the library.

---

## 1. What is byte-level BPE?

**Byte-Pair Encoding (BPE)** is a data compression algorithm adapted for NLP tokenization (Sennrich et al., 2016). Starting from a base vocabulary of individual bytes, it iteratively merges the most frequent adjacent pair into a new token, growing the vocabulary by one token per merge.

**Byte-level** means the base vocabulary is the 256 possible byte values (0–255), not Unicode characters or words. This gives Tessera two important properties:

1. **Complete coverage** — any UTF-8 string can always be encoded, even with characters never seen during training. Unknown characters fall back to their individual bytes.
2. **Encoding-agnostic** — the algorithm never needs to know about Unicode code points, surrogate pairs, or character categories. It sees only bytes.

This is the same approach used by GPT-2 and GPT-4 (`cl100k_base`).

---

## 2. Vocabulary structure

| ID range | Content |
|---|---|
| 0–255 | Base byte tokens (one per possible byte value) |
| 256–(256 + S − 1) | Special tokens, where S = `SpecialTokens.tokens.size` |
| (256 + S)–(256 + S + M − 1) | Learned merge tokens, where M = `TrainingConfig.numMerges` |

The default configuration has S = 1 (`<|endoftext|>` = 256), so the first merge token gets ID 257.

**Vocabulary reconstruction** — the vocab is never serialized directly. Given the ordered merge list, it can always be rebuilt:
```
vocab[i] = [byte(i)]          for i in 0..255
vocab[newId] = vocab[a] + vocab[b]   for each merge (a, b) → newId in order
```

---

## 3. Pre-tokenization

Before BPE is applied, the text is split into independent **chunks** by the GPT-4 `cl100k_base` regex:

```
(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\r\n\p{L}\p{N}]?\p{L}+|\p{N}{1,3}| ?[^\s\p{L}\p{N}]+[\r\n]*|\s*[\r\n]+|\s+(?!\S)|\s+
```

This pattern splits on:
- English contractions (`'s`, `'t`, `'re`, …)
- Words (with optional leading non-letter/non-digit prefix, e.g. `" Hello"`)
- Short number runs (1–3 digits)
- Punctuation and symbols
- Line endings and trailing whitespace

**Why pre-tokenize?** Without it, BPE could merge bytes that span a word boundary (e.g. the final `e` of `"the"` with the leading space of `" quick"`), producing tokens that straddle semantic units. Pre-tokenization prevents this: each chunk is encoded independently.

Implementation: `internal/PreTokenizer.kt` and `internal/Gpt4Pattern.kt`.

---

## 4. Training algorithm

`Trainer.train(corpus)` executes the following steps:

```
1. Pre-tokenize corpus → [chunk₁, chunk₂, …, chunkₙ]
2. Convert to byte IDs, insert -1 sentinels between chunks:
   [b₁ b₂ … bₖ -1 b₁ b₂ … -1 …]
3. Repeat numMerges times:
   a. Count frequency of all consecutive (a, b) pairs, skipping pairs that cross -1.
   b. Select the pair with the highest frequency.
      Tie-break: prefer the pair with the lowest (a, b) value lexicographically.
   c. Assign newId = 256 + S + step.
   d. Replace every occurrence of (a, b) in the ID list with newId.
   e. Record merge: merges[(a, b)] = newId.
   f. Extend vocab: vocab[newId] = vocab[a] + vocab[b].
   g. Invoke progressCallback if set.
```

**Sentinel (`-1`)** — prevents any merge from crossing a chunk boundary. The `getStats` function simply skips any pair where either element is `-1`.

**Deterministic tie-breaking** — when two pairs have equal frequency, the one with the smaller `(a, b)` tuple wins. This ensures training is fully deterministic for a given corpus.

---

## 5. Encoding algorithm

`BpeTokenizer.encode(text)` applies the learned merges to new text:

```
1. Handle special tokens:
   - Scan text for any strings in allowedSpecialTokens.
   - Split text into alternating (plain, special, plain, …) segments.
   - Special segments → emit their ID directly.

2. For each plain segment:
   a. Pre-tokenize with the same cl100k_base regex → chunks.
   b. For each chunk:
      i.  Convert to initial byte ID list.
      ii. Greedy merge loop:
          - Find the pair (a, b) in the current ID list whose merges[(a,b)] rank is lowest.
          - Apply that merge everywhere in the list (single pass).
          - Repeat until no known pair remains.
      iii. Append resulting IDs to output.
```

**Greedy by lowest rank** means "apply the merge that was learned earliest first". This is equivalent to applying the highest-priority merge, and it guarantees that encode produces the same segmentation the training algorithm would have settled on.

This differs from applying the *most frequent* pair: frequency is a training-time metric, but at encode time only the merge table is available.

---

## 6. Decoding algorithm

`BpeTokenizer.decode(ids)` is the inverse of encoding:

```
1. For each id in ids:
   - Check if id is a special token → append its UTF-8 bytes.
   - Otherwise look up vocab[id] → append the stored bytes.
2. Concatenate all bytes into a ByteArray.
3. Decode ByteArray as UTF-8.
```

Because every merge token's bytes are fully stored in the vocab, decoding is O(n) in the number of tokens and never requires the merge table.

---

## 7. Persistence format

Tokenizers are saved as JSON (written by `internal/Persistence.kt`):

```json
{
  "version": 1,
  "name": "tessera",
  "specialTokens": {
    "<|endoftext|>": 256
  },
  "merges": [
    { "a": 116, "b": 104, "id": 257 },
    { "a": 257, "b": 101, "id": 258 }
  ]
}
```

- `version` — format version for forward compatibility.
- `specialTokens` — map of token string → ID.
- `merges` — ordered list of merge operations. Order is critical: vocab reconstruction replays them in sequence.
- `vocab` is intentionally **not stored** — it is fully reconstructible from `merges` (see §2).

---

## 8. Module structure

```
tessera/
├── tessera-core/          ← Published library JAR
│   └── dev.tessera
│       ├── BpeTokenizer   ← Public: encode, decode, save, load
│       ├── Trainer        ← Public: train, trainFromFile
│       ├── TrainingConfig ← Public: data class, hyperparameters
│       ├── TrainingProgress ← Public: progress snapshot
│       ├── SpecialTokens  ← Public: token registry
│       └── internal/      ← Not part of the public API
│           ├── ByteUtils      ← UTF-8 ↔ byte[] conversions
│           ├── Gpt4Pattern    ← cl100k_base regex constant
│           ├── PreTokenizer   ← Applies regex to produce chunks
│           └── Persistence    ← JSON save/load
│
├── tessera-cli/           ← Runnable CLI (consumes tessera-core)
│   └── dev.tessera.cli
│       ├── Main           ← Argument dispatcher
│       ├── TrainCommand   ← train subcommand
│       ├── EncodeCommand  ← encode subcommand
│       ├── DecodeCommand  ← decode subcommand
│       └── InspectCommand ← inspect subcommand
│
└── tessera-samples/       ← Usage examples (consume tessera-core)
    └── dev.tessera.samples
        ├── QuickStartSample
        ├── TrainingSample
        ├── SpecialTokensSample
        └── PersistenceSample
```

**Visibility discipline** — `explicitApi()` is enforced in `tessera-core`. Every symbol that is not intended to be part of the public API is declared `internal`. The `tessera-cli` and `tessera-samples` modules serve as integration tests for the public API surface: they cannot reference `internal` symbols.

---

## 9. Key design decisions

| Decision | Rationale |
|---|---|
| Byte-level base vocabulary | Universal UTF-8 coverage with no unknown-token fallback |
| cl100k_base pre-tokenization regex | Prevents cross-boundary merges; matches GPT-4 behaviour |
| Greedy encode by lowest rank | Deterministic; matches training-time merge priority |
| `‑1` sentinel between chunks | Simple, zero-overhead way to prevent cross-chunk merges |
| Vocab reconstructed from merges | Smaller save files; single source of truth |
| `internal` for all non-API code | Stable public API surface; freedom to refactor internals |
| `explicitApi()` in tessera-core | Compiler-enforced API discipline; prevents accidental exposure |

---

## 10. References

- Sennrich, R., Haddow, B., & Birch, A. (2016). *Neural Machine Translation of Rare Words with Subword Units.* ACL 2016.
- Radford, A. et al. (2019). *Language Models are Unsupervised Multitask Learners.* (GPT-2, byte-level BPE)
- Karpathy, A. [minbpe](https://github.com/karpathy/minbpe) — minimal reference implementation in Python
- OpenAI [tiktoken](https://github.com/openai/tiktoken) — production tokenizer used by GPT-4
