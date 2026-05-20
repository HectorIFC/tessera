package dev.tessera

/**
 * Trains a new [BpeTokenizer] from a text corpus.
 */
public class Trainer(public val config: TrainingConfig = TrainingConfig()) {

    /** Trains a tokenizer from a corpus string. */
    public fun train(corpus: String): BpeTokenizer = TODO("Implemented in Phase 1")

    /** Trains a tokenizer from the file at [path]. */
    public fun trainFromFile(path: String): BpeTokenizer = TODO("Implemented in Phase 1")

    /** Trains a tokenizer from [file]. */
    public fun trainFromFile(file: java.io.File): BpeTokenizer = TODO("Implemented in Phase 1")
}
