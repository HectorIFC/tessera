package dev.tessera.samples

import dev.tessera.BpeTokenizer
import dev.tessera.Trainer
import dev.tessera.TrainingConfig

/**
 * Minimal encode/decode example — the simplest way to use Tessera.
 *
 * Run: ./gradlew :tessera-samples:run -PmainClass=dev.tessera.samples.QuickStartSampleKt
 */
fun main() {
    val corpus = """
        The quick brown fox jumps over the lazy dog.
        A fast red cat leaps over a slow brown dog.
        Hello world! This is a simple tokenizer test.
        Kotlin is a modern programming language for the JVM.
    """.trimIndent().repeat(50)

    println("=== Tessera — Quick Start ===\n")

    val tokenizer = Trainer(TrainingConfig(numMerges = 100, verbose = false)).train(corpus)
    println("Trained tokenizer — vocab size: ${tokenizer.vocabSize}")

    val texts = listOf(
        "Hello world!",
        "The quick brown fox",
        "Kotlin programming",
    )

    for (text in texts) {
        val ids = tokenizer.encode(text)
        val decoded = tokenizer.decode(ids)
        println("\n  text   : \"$text\"")
        println("  ids    : ${ids.toList()}")
        println("  decoded: \"$decoded\"")
        println("  match  : ${text == decoded}")
    }
}
