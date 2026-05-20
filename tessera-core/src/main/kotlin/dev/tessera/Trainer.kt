package dev.tessera

import dev.tessera.internal.ByteUtils
import dev.tessera.internal.PreTokenizer

/**
 * Trains a new [BpeTokenizer] from a text corpus.
 */
public class Trainer(public val config: TrainingConfig = TrainingConfig()) {

    /** Trains a tokenizer from a corpus string. */
    public fun train(corpus: String): BpeTokenizer {
        val chunks = PreTokenizer.split(corpus)
        val ids = chunksToIds(chunks)
        return runBpe(ids)
    }

    /** Trains a tokenizer from the file at [path]. */
    public fun trainFromFile(path: String): BpeTokenizer = trainFromFile(java.io.File(path))

    /** Trains a tokenizer from [file]. */
    public fun trainFromFile(file: java.io.File): BpeTokenizer = train(file.readText(Charsets.UTF_8))

    // Converts pre-tokenized chunks into a flat ID list with -1 sentinels between chunks.
    private fun chunksToIds(chunks: List<String>): MutableList<Int> {
        val result = mutableListOf<Int>()
        for ((index, chunk) in chunks.withIndex()) {
            ByteUtils.stringToBytes(chunk).forEach { b -> result.add(ByteUtils.byteToId(b)) }
            if (index < chunks.lastIndex) result.add(-1)
        }
        return result
    }

    private fun runBpe(ids: MutableList<Int>): BpeTokenizer {
        val merges = mutableMapOf<Pair<Int, Int>, Int>()
        val vocab = buildBaseVocab()
        val firstMergeId = 256 + config.specialTokens.tokens.size

        repeat(config.numMerges) { step ->
            val stats = getStats(ids)
            if (stats.isEmpty()) return@repeat

            // Deterministic tie-break: lowest pair (a, b) lexicographically
            val best = stats.maxByOrNull { (pair, freq) ->
                freq.toLong() * FREQ_MULTIPLIER - pair.first.toLong() * FIRST_ID_MULTIPLIER - pair.second.toLong()
            }!!.key

            val newId = firstMergeId + step
            mergeIds(ids, best, newId)
            merges[best] = newId
            vocab[newId] = vocab[best.first]!! + vocab[best.second]!!

            if (config.verbose && (step + 1) % VERBOSE_INTERVAL == 0) {
                println("merge ${step + 1}/${config.numMerges}: $best -> $newId")
            }
            config.progressCallback?.invoke(
                TrainingProgress(
                    mergesCompleted = step + 1,
                    totalMerges = config.numMerges,
                    lastMergeTokens = ByteUtils.bytesToString(vocab[best.first]!!) to
                        ByteUtils.bytesToString(vocab[best.second]!!),
                ),
            )
        }

        return BpeTokenizer(merges, vocab, config.specialTokens)
    }

    // Counts frequencies of consecutive pairs, skipping pairs that cross the -1 sentinel.
    private fun getStats(ids: List<Int>): Map<Pair<Int, Int>, Int> {
        val counts = mutableMapOf<Pair<Int, Int>, Int>()
        for (i in 0 until ids.size - 1) {
            val a = ids[i]
            val b = ids[i + 1]
            if (a == -1 || b == -1) continue
            val pair = a to b
            counts[pair] = (counts[pair] ?: 0) + 1
        }
        return counts
    }

    private fun mergeIds(ids: MutableList<Int>, pair: Pair<Int, Int>, newId: Int) {
        var i = 0
        while (i < ids.size - 1) {
            if (ids[i] == pair.first && ids[i + 1] == pair.second) {
                ids[i] = newId
                ids.removeAt(i + 1)
            } else {
                i++
            }
        }
    }

    private fun buildBaseVocab(): MutableMap<Int, ByteArray> {
        val vocab = mutableMapOf<Int, ByteArray>()
        for (i in 0..255) vocab[i] = byteArrayOf(ByteUtils.idToByte(i))
        return vocab
    }

    private companion object {
        // Multipliers spread frequency and pair IDs into non-overlapping ranges for deterministic sort.
        const val FREQ_MULTIPLIER = 1_000_000_000L
        const val FIRST_ID_MULTIPLIER = 1_000_000L
        const val VERBOSE_INTERVAL = 100
    }
}
