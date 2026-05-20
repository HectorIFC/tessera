package dev.tessera

/**
 * A trained BPE tokenizer.
 *
 * Use [Trainer] to train a new tokenizer from a corpus, or [BpeTokenizer.load]
 * to load one from disk.
 */
public class BpeTokenizer internal constructor(
    private val merges: Map<Pair<Int, Int>, Int>,
    private val vocab: Map<Int, ByteArray>,
    private val specialTokens: SpecialTokens
) {
    /** Total number of tokens in the vocabulary (base bytes + merges + special tokens). */
    public val vocabSize: Int get() = vocab.size + specialTokens.tokens.size

    /** Encodes [text] into a sequence of token IDs. */
    public fun encode(
        text: String,
        allowedSpecialTokens: Set<String> = emptySet()
    ): IntArray = TODO("Implemented in Phase 1")

    /** Decodes a sequence of token IDs back into text. */
    public fun decode(ids: IntArray): String = TODO("Implemented in Phase 1")

    /** Returns the string representation of a token by its [id]. */
    public fun tokenAsString(id: Int): String = TODO("Implemented in Phase 1")

    /** Returns the raw bytes of a token by its [id]. */
    public fun tokenAsBytes(id: Int): ByteArray = TODO("Implemented in Phase 1")

    /** Saves this tokenizer to the file at [path]. */
    public fun save(path: String): Unit = TODO("Implemented in Phase 1")

    /** Saves this tokenizer to [file]. */
    public fun save(file: java.io.File): Unit = TODO("Implemented in Phase 1")

    public companion object {
        /** Loads a tokenizer from the file at [path]. */
        public fun load(path: String): BpeTokenizer = TODO("Implemented in Phase 1")

        /** Loads a tokenizer from [file]. */
        public fun load(file: java.io.File): BpeTokenizer = TODO("Implemented in Phase 1")
    }
}
