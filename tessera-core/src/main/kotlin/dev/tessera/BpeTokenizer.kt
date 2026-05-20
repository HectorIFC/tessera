package dev.tessera

import dev.tessera.internal.ByteUtils
import dev.tessera.internal.Persistence
import dev.tessera.internal.PreTokenizer

/**
 * A trained BPE tokenizer.
 *
 * Use [Trainer] to train a new tokenizer from a corpus, or [BpeTokenizer.load]
 * to load one from disk.
 */
public class BpeTokenizer internal constructor(
    internal val merges: Map<Pair<Int, Int>, Int>,
    internal val vocab: Map<Int, ByteArray>,
    /** The special tokens registered with this tokenizer. */
    public val specialTokens: SpecialTokens,
) {
    /** Total number of tokens in the vocabulary (base bytes + merges + special tokens). */
    public val vocabSize: Int get() = vocab.size + specialTokens.tokens.size

    /**
     * Encodes [text] into a sequence of token IDs.
     *
     * Special tokens are only recognized when their string is present in [allowedSpecialTokens].
     * By default, special token strings are treated as plain text (safety against injection).
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

    /** Decodes a sequence of token IDs back into text. */
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

    /** Returns the string representation of the token with the given [id]. */
    public fun tokenAsString(id: Int): String = ByteUtils.bytesToString(tokenAsBytes(id))

    /** Returns the raw bytes of the token with the given [id]. */
    public fun tokenAsBytes(id: Int): ByteArray {
        specialTokens.tokens.entries.find { it.value == id }
            ?.let { return it.key.toByteArray(Charsets.UTF_8) }
        return vocab[id] ?: error("Unknown token ID: $id")
    }

    /** Saves this tokenizer to the file at [path]. */
    public fun save(path: String): Unit = save(java.io.File(path))

    /** Saves this tokenizer to [file]. */
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
        /** Loads a tokenizer from the file at [path]. */
        public fun load(path: String): BpeTokenizer = load(java.io.File(path))

        /** Loads a tokenizer from [file]. */
        public fun load(file: java.io.File): BpeTokenizer = Persistence.load(file)
    }
}
