package dev.tessera.internal

internal object PreTokenizer {
    fun split(text: String): List<String> = GPT4_PATTERN.findAll(text).map { it.value }.toList()
}
