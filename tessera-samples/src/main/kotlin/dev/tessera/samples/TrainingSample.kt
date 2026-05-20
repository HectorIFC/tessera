package dev.tessera.samples

import dev.tessera.Trainer
import dev.tessera.TrainingConfig
import dev.tessera.TrainingProgress

/**
 * Demonstrates training configuration options including the progress callback.
 *
 * Run: ./gradlew :tessera-samples:run -PmainClass=dev.tessera.samples.TrainingSampleKt
 */
fun main() {
    val corpus = buildString {
        repeat(100) {
            appendLine("The quick brown fox jumps over the lazy dog.")
            appendLine("Pack my box with five dozen liquor jugs.")
            appendLine("How vexingly quick daft zebras jump!")
            appendLine("Kotlin is concise, expressive, and interoperable.")
        }
    }

    println("=== Tessera — Training Sample ===\n")
    println("Corpus size: ${corpus.length} chars")

    var lastProgress: TrainingProgress? = null

    val config = TrainingConfig(
        numMerges = 200,
        verbose = false,
        progressCallback = { progress ->
            lastProgress = progress
            if (progress.mergesCompleted % 50 == 0) {
                val (a, b) = progress.lastMergeTokens ?: ("?" to "?")
                println(
                    "  merge ${progress.mergesCompleted}/${progress.totalMerges}" +
                        " — last: \"$a\" + \"$b\"",
                )
            }
        },
    )

    val tokenizer = Trainer(config).train(corpus)

    println("\nTraining complete!")
    println("  vocab size  : ${tokenizer.vocabSize}")
    println("  merges done : ${lastProgress?.mergesCompleted ?: 0}")

    val sample = "How vexingly quick daft zebras jump!"
    val ids = tokenizer.encode(sample)
    println("\nSample encode:")
    println("  \"$sample\"")
    println("  ${ids.size} tokens (raw bytes would be ${sample.toByteArray().size})")
    println("  compression: ${"%.1f".format(ids.size.toDouble() / sample.toByteArray().size)}x")
}
