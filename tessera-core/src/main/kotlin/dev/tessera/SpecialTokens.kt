package dev.tessera

/**
 * Special tokens that are never split by BPE.
 *
 * Special tokens must be explicitly allowed at encode time via
 * [BpeTokenizer.encode]'s `allowedSpecialTokens` parameter.
 */
public class SpecialTokens(public val tokens: Map<String, Int>) {

    public companion object {
        private const val END_OF_TEXT = "<|endoftext|>"

        /** Returns the default set containing only [END_OF_TEXT] (ID = 256). */
        public fun default(): SpecialTokens = SpecialTokens(mapOf(END_OF_TEXT to 256))

        /** Creates a [SpecialTokens] instance from a list of token names, assigned sequential IDs starting at 256. */
        public fun of(vararg names: String): SpecialTokens =
            SpecialTokens(names.mapIndexed { i, name -> name to (256 + i) }.toMap())
    }
}
