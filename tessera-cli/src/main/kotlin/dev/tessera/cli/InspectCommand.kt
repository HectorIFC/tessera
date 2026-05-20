package dev.tessera.cli

import dev.tessera.BpeTokenizer

internal object InspectCommand {
    fun run(args: Array<String>) {
        val params = parseArgs(args)
        val tokenizerPath = requireArg(params, "tokenizer")
        val limit = params["limit"]?.toIntOrNull() ?: 30

        val tokenizer = BpeTokenizer.load(tokenizerPath)

        println("Tokenizer: $tokenizerPath")
        println("Vocab size: ${tokenizer.vocabSize}")
        println()

        println("Special tokens:")
        tokenizer.specialTokens.tokens.entries
            .sortedBy { it.value }
            .forEach { (token, id) -> println("  [$id] \"$token\"") }
        println()

        println("First $limit learned merges (ID → bytes):")
        val baseSize = 256 + tokenizer.specialTokens.tokens.size
        for (id in baseSize until (baseSize + limit).coerceAtMost(tokenizer.vocabSize)) {
            val repr = tokenizer.tokenAsString(id)
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
            println("  [$id] \"$repr\"")
        }
    }
}
