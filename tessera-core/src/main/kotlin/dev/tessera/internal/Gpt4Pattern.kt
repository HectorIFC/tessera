@file:Suppress("ktlint:standard:max-line-length", "MaxLineLength")

package dev.tessera.internal

// Exact cl100k_base regex from GPT-4 — do not modify, length exceeds line limit by design.
internal val GPT4_PATTERN = Regex(
    """(?i:'s|'t|'re|'ve|'m|'ll|'d)|[^\r\n\p{L}\p{N}]?\p{L}+|\p{N}{1,3}| ?[^\s\p{L}\p{N}]+[\r\n]*|\s*[\r\n]+|\s+(?!\S)|\s+""",
)
