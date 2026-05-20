package dev.tessera

/**
 * Configuration for BPE training.
 *
 * Pass to [Trainer] to control how the tokenizer is trained.
 *
 * ```kotlin
 * val config = TrainingConfig(
 *     numMerges = 10_000,
 *     verbose = false,
 *     progressCallback = { p -> println("${p.mergesCompleted}/${p.totalMerges}") }
 * )
 * val tokenizer = Trainer(config).trainFromFile("corpus.txt")
 * ```
 *
 * @param numMerges Number of BPE merge operations to learn. Higher values produce smaller
 *   vocabularies for common subwords at the cost of training time. Typical range: 5,000–50,000.
 * @param specialTokens Special tokens that bypass BPE and are recognised before pre-tokenization.
 *   Defaults to [SpecialTokens.default] (just `<|endoftext|>`).
 * @param verbose Whether to print merge progress to stdout every 100 steps.
 * @param progressCallback Optional callback invoked after every single merge with a [TrainingProgress]
 *   snapshot. Use this for custom progress UIs or early stopping.
 */
public data class TrainingConfig(
    val numMerges: Int = 5000,
    val specialTokens: SpecialTokens = SpecialTokens.default(),
    val verbose: Boolean = true,
    val progressCallback: ((TrainingProgress) -> Unit)? = null,
)

/**
 * Progress snapshot emitted after each merge during training.
 *
 * Delivered via [TrainingConfig.progressCallback].
 *
 * @param mergesCompleted Number of merge operations completed so far.
 * @param totalMerges Total merge operations requested ([TrainingConfig.numMerges]).
 * @param lastMergeTokens The string representations of the two tokens that were just merged,
 *   or `null` if the strings are not valid UTF-8 on their own.
 */
public data class TrainingProgress(
    val mergesCompleted: Int,
    val totalMerges: Int,
    val lastMergeTokens: Pair<String, String>?,
)
