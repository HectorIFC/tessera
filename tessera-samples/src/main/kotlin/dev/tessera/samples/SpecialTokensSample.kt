package dev.tessera.samples

import dev.tessera.SpecialTokens
import dev.tessera.Trainer
import dev.tessera.TrainingConfig

/**
 * Demonstrates how to define and use special tokens.
 *
 * Run: ./gradlew :tessera-samples:run -PmainClass=dev.tessera.samples.SpecialTokensSampleKt
 */
fun main() {
    println("=== Tessera — Special Tokens Sample ===\n")

    // Define a custom set of special tokens for a chat model format
    val specialTokens = SpecialTokens.of(
        "<|endoftext|>",
        "<|user|>",
        "<|assistant|>",
        "<|system|>"
    )

    println("Special tokens:")
    for ((token, id) in specialTokens.tokens) {
        println("  \"$token\" → ID $id")
    }

    val corpus = "hello world the quick brown fox ".repeat(100)
    val tokenizer = Trainer(
        TrainingConfig(numMerges = 50, verbose = false, specialTokens = specialTokens)
    ).train(corpus)

    println("\nvocabSize: ${tokenizer.vocabSize}")

    // Special tokens must be explicitly allowed at encode time
    val chatText = "<|user|>Hello!<|endoftext|>"
    val allowed = setOf("<|user|>", "<|endoftext|>")

    val idsWithSpecial = tokenizer.encode(chatText, allowedSpecialTokens = allowed)
    val idsWithout = tokenizer.encode(chatText, allowedSpecialTokens = emptySet())

    println("\nWith allowedSpecialTokens:")
    println("  input  : \"$chatText\"")
    println("  ids    : ${idsWithSpecial.toList()}")
    println("  decoded: \"${tokenizer.decode(idsWithSpecial)}\"")

    println("\nWithout allowedSpecialTokens (treated as plain text):")
    println("  ids    : ${idsWithout.toList()}")
    println("  decoded: \"${tokenizer.decode(idsWithout)}\"")

    println("\nRound-trip matches: ${tokenizer.decode(idsWithSpecial) == chatText}")
}
