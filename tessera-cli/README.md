# tessera-cli

Command-line interface for Tessera. Demonstrates `tessera-core` in a real usage context.

## Commands

### train

Trains a new tokenizer from a text corpus and saves it to disk.

```bash
./gradlew :tessera-cli:run --args="train --corpus corpus/text.txt --merges 5000 --output tessera.json"
```

| Flag | Default | Description |
|---|---|---|
| `--corpus` | required | Path to the training corpus (UTF-8 text file) |
| `--merges` | `5000` | Number of BPE merge operations to learn |
| `--output` | `tessera.json` | Output path for the trained tokenizer |

### encode

Encodes a text string into token IDs using a saved tokenizer.

```bash
./gradlew :tessera-cli:run --args="encode --tokenizer tessera.json --text 'Hello, world!'"
```

### decode

Decodes a comma-separated list of token IDs back into text.

```bash
./gradlew :tessera-cli:run --args="decode --tokenizer tessera.json --ids '256,104,101,108'"
```

### inspect

Shows tokenizer metadata and the first N learned merges.

```bash
./gradlew :tessera-cli:run --args="inspect --tokenizer tessera.json --limit 20"
```

## Installing as a standalone distribution

```bash
./gradlew :tessera-cli:installDist

# Then run directly (no Gradle needed):
./tessera-cli/build/install/tessera-cli/bin/tessera-cli train --corpus corpus/text.txt
```
