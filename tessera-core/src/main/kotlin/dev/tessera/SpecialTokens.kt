package dev.tessera

/**
 * A set of special tokens that are never split by BPE.
 *
 * Special tokens are detected in the raw input text *before* pre-tokenization and injected
 * directly as their assigned IDs, bypassing the BPE algorithm entirely. This guarantees that
 * strings like `<|endoftext|>` always map to a single, stable token regardless of training.
 *
 * ### Security
 * Special tokens must be **explicitly allowed** at encode time via
 * [BpeTokenizer.encode]'s `allowedSpecialTokens` parameter. When a special token string appears
 * in untrusted input and is not listed in `allowedSpecialTokens`, it is treated as ordinary text.
 * This prevents injection attacks where user input could be mistaken for a control token.
 *
 * ### ID assignment
 * Special token IDs start at 256 (immediately after the 256 base byte tokens) and increment by 1.
 * Merge IDs are assigned starting at `256 + specialTokens.tokens.size`, so the ID spaces never
 * overlap.
 *
 * @param tokens Map of token string to its integer ID.
 * @see BpeTokenizer.encode
 */
public class SpecialTokens(public val tokens: Map<String, Int>) {

    public companion object {
        private const val END_OF_TEXT = "<|endoftext|>"

        /**
         * Returns the default set containing only `<|endoftext|>` at ID 256.
         *
         * This mirrors the GPT-2/GPT-4 convention for marking document boundaries.
         */
        public fun default(): SpecialTokens = SpecialTokens(mapOf(END_OF_TEXT to 256))

        /**
         * Creates a [SpecialTokens] instance from a list of token names.
         *
         * IDs are assigned sequentially starting at 256. The order of [names] determines
         * the IDs: `names[0]` → 256, `names[1]` → 257, and so on.
         *
         * @param names The special token strings (e.g. `"<|endoftext|>"`, `"<|user|>"`).
         * @return A new [SpecialTokens] with the given tokens mapped to sequential IDs.
         */
        public fun of(vararg names: String): SpecialTokens =
            SpecialTokens(names.mapIndexed { i, name -> name to (256 + i) }.toMap())
    }
}
