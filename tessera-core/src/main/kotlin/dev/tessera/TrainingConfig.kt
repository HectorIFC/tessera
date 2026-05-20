package dev.tessera

/**
 * Configuration for BPE training.
 *
 * @param numMerges Number of merge operations to learn.
 * @param specialTokens Special tokens that bypass BPE.
 * @param verbose Whether to log progress to stdout.
 * @param progressCallback Optional callback invoked after each merge.
 */
public data class TrainingConfig(
    val numMerges: Int = 5000,
    val specialTokens: SpecialTokens = SpecialTokens.default(),
    val verbose: Boolean = true,
    val progressCallback: ((TrainingProgress) -> Unit)? = null
)

/** Progress snapshot emitted after each merge during training. */
public data class TrainingProgress(
    val mergesCompleted: Int,
    val totalMerges: Int,
    val lastMergeTokens: Pair<String, String>?
)
