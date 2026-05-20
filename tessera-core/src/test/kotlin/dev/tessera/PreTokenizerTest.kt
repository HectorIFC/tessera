package dev.tessera

import dev.tessera.internal.PreTokenizer
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class PreTokenizerTest : StringSpec({
    "splits simple English sentence into words and spaces" {
        val chunks = PreTokenizer.split("Hello world")
        chunks shouldBe listOf("Hello", " world")
    }

    "splits contractions" {
        val chunks = PreTokenizer.split("I'm don't")
        // regex matches the word part first, then the contraction suffix
        chunks.joinToString("|") shouldBe "I|'m| don|'t"
    }

    "handles punctuation" {
        val chunks = PreTokenizer.split("Hello, world!")
        chunks shouldBe listOf("Hello", ",", " world", "!")
    }

    "handles numbers separately" {
        val chunks = PreTokenizer.split("abc 123 def")
        chunks shouldBe listOf("abc", " ", "123", " def")
    }

    "empty string returns empty list" {
        PreTokenizer.split("") shouldHaveSize 0
    }

    "handles unicode text" {
        val chunks = PreTokenizer.split("Olá mundo")
        chunks shouldBe listOf("Olá", " mundo")
    }
})
