package dev.tessera

import dev.tessera.internal.ByteUtils
import dev.tessera.internal.Persistence
import dev.tessera.internal.PreTokenizer

/**
 * A trained byte-level BPE tokenizer.
 *
 * Instances are immutable and thread-safe once constructed. Obtain one via [Trainer.train],
 * [Trainer.trainFromFile], or [BpeTokenizer.load].
 *
 * ### Encoding
 * Text is first split into chunks by the cl100k_base pre-tokenization regex (preventing merges
 * across word boundaries), then each chunk is encoded greedily by applying the learned merge with
 * the lowest rank first.
 *
 * ### Special tokens
 * Strings registered as special tokens (e.g. `<|endoftext|>`) bypass BPE entirely.
 * They must be explicitly unlocked at call site via [encode]'s `allowedSpecialTokens` parameter.
 * This prevents accidental injection when encoding untrusted input.
 *
 * ### Round-trip guarantee
 * `decode(encode(text)) == text` holds for any valid UTF-8 string.
 *
 * @see Trainer
 * @see SpecialTokens
 */
public class BpeTokenizer internal constructor(
    internal val merges: Map<Pair<Int, Int>, Int>,
    internal val vocab: Map<Int, ByteArray>,
    /** The special tokens registered with this tokenizer. */
    public val specialTokens: SpecialTokens,
) {
    /**
     * Total number of tokens in the vocabulary.
     *
     * Equals 256 (base bytes) + number of learned merges + number of special tokens.
     */
    public val vocabSize: Int get() = vocab.size + specialTokens.tokens.size

    /**
     * Encodes [text] into a sequence of token IDs.
     *
     * Pre-tokenizes with the cl100k_base regex, then applies BPE greedily (lowest-rank merge first)
     * to each chunk independently.
     *
     * Special tokens are only recognised when their exact string is listed in [allowedSpecialTokens].
     * By default they are treated as plain text — this prevents injection from untrusted input.
     *
     * @param text The UTF-8 text to encode.
     * @param allowedSpecialTokens Set of special token strings to recognise during encoding.
     *   Defaults to empty (special tokens are treated as plain text).
     * @return An [IntArray] of token IDs. Empty when [text] is empty.
     */
    public fun encode(text: String, allowedSpecialTokens: Set<String> = emptySet()): IntArray {
        val result = mutableListOf<Int>()

        // Split text around any allowed special tokens, then encode each segment
        val segments = splitOnSpecialTokens(text, allowedSpecialTokens)
        for (segment in segments) {
            if (segment.isSpecial) {
                result.add(specialTokens.tokens[segment.text]!!)
            } else {
                val chunks = PreTokenizer.split(segment.text)
                for (chunk in chunks) {
                    val bytes = ByteUtils.stringToBytes(chunk)
                    val ids = bytes.map { ByteUtils.byteToId(it) }.toMutableList()
                    result.addAll(encodeChunk(ids))
                }
            }
        }
        return result.toIntArray()
    }

    /**
     * Decodes a sequence of token IDs back into text.
     *
     * Looks up each ID in the vocabulary (or special tokens map), concatenates the raw bytes,
     * and converts the result to a UTF-8 string.
     *
     * @param ids Token IDs produced by [encode].
     * @return The original UTF-8 text.
     * @throws IllegalStateException if any ID is not present in the vocabulary.
     */
    public fun decode(ids: IntArray): String {
        val bytes = mutableListOf<Byte>()
        for (id in ids) {
            val tokenBytes = specialTokens.tokens.entries.find { it.value == id }
                ?.key?.toByteArray(Charsets.UTF_8)
                ?: vocab[id]
                ?: error("Unknown token ID: $id")
            bytes.addAll(tokenBytes.toList())
        }
        return ByteUtils.bytesToString(bytes.toByteArray())
    }

    /**
     * Returns the string representation of the token with the given [id].
     *
     * For tokens that map to valid UTF-8 byte sequences this is the readable text fragment.
     * For mid-word byte tokens the result may not be valid UTF-8 on its own.
     *
     * @param id A token ID in the range `[0, vocabSize)`.
     * @throws IllegalStateException if [id] is unknown.
     */
    public fun tokenAsString(id: Int): String = ByteUtils.bytesToString(tokenAsBytes(id))

    /**
     * Returns the raw UTF-8 bytes of the token with the given [id].
     *
     * @param id A token ID in the range `[0, vocabSize)`.
     * @throws IllegalStateException if [id] is unknown.
     */
    public fun tokenAsBytes(id: Int): ByteArray {
        specialTokens.tokens.entries.find { it.value == id }
            ?.let { return it.key.toByteArray(Charsets.UTF_8) }
        return vocab[id] ?: error("Unknown token ID: $id")
    }

    /**
     * Saves this tokenizer to the file at [path].
     *
     * The file is written in the Tessera JSON format (see [load]). Existing content is overwritten.
     *
     * @param path Filesystem path for the output file.
     */
    public fun save(path: String): Unit = save(java.io.File(path))

    /**
     * Saves this tokenizer to [file].
     *
     * The file is written in the Tessera JSON format (see [load]). Existing content is overwritten.
     *
     * @param file The output file.
     */
    public fun save(file: java.io.File): Unit = Persistence.save(this, file)

    // Greedy encode: always apply the merge with the lowest rank (learned earliest).
    private fun encodeChunk(ids: MutableList<Int>): List<Int> {
        while (ids.size >= 2) {
            var bestPair: Pair<Int, Int>? = null
            var bestRank = Int.MAX_VALUE
            for (i in 0 until ids.size - 1) {
                val pair = ids[i] to ids[i + 1]
                val rank = merges[pair] ?: continue
                if (rank < bestRank) {
                    bestRank = rank
                    bestPair = pair
                }
            }
            bestPair ?: break

            // Apply the merge in a single pass
            var i = 0
            while (i < ids.size - 1) {
                if (ids[i] == bestPair.first && ids[i + 1] == bestPair.second) {
                    ids[i] = bestRank
                    ids.removeAt(i + 1)
                } else {
                    i++
                }
            }
        }
        return ids
    }

    private data class Segment(val text: String, val isSpecial: Boolean)

    private fun splitOnSpecialTokens(text: String, allowed: Set<String>): List<Segment> {
        if (allowed.isEmpty()) return listOf(Segment(text, false))

        val pattern = allowed.sortedByDescending { it.length }
            .joinToString("|") { Regex.escape(it) }
        val regex = Regex(pattern)

        val segments = mutableListOf<Segment>()
        var lastEnd = 0
        for (match in regex.findAll(text)) {
            if (match.range.first > lastEnd) {
                segments.add(Segment(text.substring(lastEnd, match.range.first), false))
            }
            segments.add(Segment(match.value, true))
            lastEnd = match.range.last + 1
        }
        if (lastEnd < text.length) segments.add(Segment(text.substring(lastEnd), false))
        return segments
    }

    public companion object {
        /**
         * Loads a tokenizer from the file at [path].
         *
         * Reconstructs the full vocabulary from the merge list in the JSON file —
         * no separate vocab serialization is required.
         *
         * @param path Filesystem path of a file previously written by [save].
         * @return The restored [BpeTokenizer].
         */
        public fun load(path: String): BpeTokenizer = load(java.io.File(path))

        /**
         * Loads a tokenizer from [file].
         *
         * @param file A file previously written by [save].
         * @return The restored [BpeTokenizer].
         */
        public fun load(file: java.io.File): BpeTokenizer = Persistence.load(file)
    }
}
