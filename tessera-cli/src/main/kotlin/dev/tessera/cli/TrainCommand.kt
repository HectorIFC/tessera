package dev.tessera.cli

import dev.tessera.Trainer
import dev.tessera.TrainingConfig
import java.io.File

internal object TrainCommand {
    private const val DEFAULT_MERGES = 5000
    private const val PROGRESS_INTERVAL = 500
    private const val BYTES_PER_KB = 1024

    fun run(args: Array<String>) {
        val params = parseArgs(args)
        val corpusPath = requireArg(params, "corpus")
        val numMerges = params["merges"]?.toIntOrNull() ?: DEFAULT_MERGES
        val outputPath = params["output"] ?: "tessera.json"

        val corpusFile = File(corpusPath)
        if (!corpusFile.exists()) {
            System.err.println("Corpus file not found: $corpusPath")
            System.exit(1)
        }

        println("Training on: $corpusPath (${corpusFile.length() / BYTES_PER_KB} KB)")
        println("Merges: $numMerges  →  output: $outputPath")

        val config = TrainingConfig(
            numMerges = numMerges,
            verbose = false,
            progressCallback = { progress ->
                if (progress.mergesCompleted % PROGRESS_INTERVAL == 0 ||
                    progress.mergesCompleted == progress.totalMerges
                ) {
                    val pct = progress.mergesCompleted * 100 / progress.totalMerges
                    print("\r  progress: ${progress.mergesCompleted}/${progress.totalMerges} ($pct%)")
                    System.out.flush()
                }
            },
        )

        val tokenizer = Trainer(config).trainFromFile(corpusFile)
        println()
        tokenizer.save(outputPath)

        println("Saved to: $outputPath")
        println("Vocab size: ${tokenizer.vocabSize}")
    }
}
