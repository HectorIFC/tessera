package dev.tessera.internal

import dev.tessera.BpeTokenizer
import dev.tessera.SpecialTokens
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json { prettyPrint = true }

@Serializable
private data class MergeEntry(val a: Int, val b: Int, val id: Int)

@Serializable
private data class TokenizerFile(
    val version: Int,
    val name: String,
    val specialTokens: Map<String, Int>,
    val merges: List<MergeEntry>
)

internal object Persistence {

    fun save(tokenizer: BpeTokenizer, file: java.io.File) {
        val mergeList = tokenizer.merges.entries
            .sortedBy { it.value }
            .map { (pair, id) -> MergeEntry(pair.first, pair.second, id) }

        val data = TokenizerFile(
            version = 1,
            name = "tessera",
            specialTokens = tokenizer.specialTokens.tokens,
            merges = mergeList
        )
        file.writeText(json.encodeToString(TokenizerFile.serializer(), data), Charsets.UTF_8)
    }

    fun load(file: java.io.File): BpeTokenizer {
        val data = json.decodeFromString(TokenizerFile.serializer(), file.readText(Charsets.UTF_8))

        val merges = data.merges.associate { (a, b, id) -> (a to b) to id }

        // Reconstruct vocab from base bytes + merges in order
        val vocab = mutableMapOf<Int, ByteArray>()
        for (i in 0..255) vocab[i] = byteArrayOf(ByteUtils.idToByte(i))
        for (entry in data.merges.sortedBy { it.id }) {
            vocab[entry.id] = vocab[entry.a]!! + vocab[entry.b]!!
        }

        return BpeTokenizer(merges, vocab, SpecialTokens(data.specialTokens))
    }
}
