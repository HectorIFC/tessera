package dev.tessera.samples

import dev.tessera.BpeTokenizer
import dev.tessera.Trainer
import dev.tessera.TrainingConfig
import java.io.File

/**
 * Demonstrates save and load — train once, reuse many times.
 *
 * Run: ./gradlew :tessera-samples:run -PmainClass=dev.tessera.samples.PersistenceSampleKt
 */
fun main() {
    println("=== Tessera — Persistence Sample ===\n")

    val corpus = buildString {
        repeat(100) {
            appendLine("the quick brown fox jumps over the lazy dog")
            appendLine("hello world this is a tokenizer test")
            appendLine("kotlin programming language for the jvm")
        }
    }

    // Train
    println("Training...")
    val tokenizer = Trainer(TrainingConfig(numMerges = 100, verbose = false)).train(corpus)
    println("Done — vocab size: ${tokenizer.vocabSize}")

    // Save
    val file = File("tessera-sample.json")
    tokenizer.save(file)
    println("\nSaved to: ${file.absolutePath} (${file.length()} bytes)")

    // Load
    val loaded = BpeTokenizer.load(file)
    println("Loaded tokenizer — vocab size: ${loaded.vocabSize}")

    // Verify identical encoding
    val probes = listOf(
        "hello world",
        "the quick brown fox",
        "kotlin programming",
        "Olá, mundo! 🌍",
    )

    println("\nVerifying encode consistency:")
    var allMatch = true
    for (text in probes) {
        val original = tokenizer.encode(text).toList()
        val fromDisk = loaded.encode(text).toList()
        val match = original == fromDisk
        if (!match) allMatch = false
        println("  \"$text\" → match=$match  ids=$original")
    }

    println("\nAll encodings match: $allMatch")
    println("Round-trip check: ${loaded.decode(loaded.encode("hello world"))} == hello world")

    file.delete()
}
