package dev.tessera

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class RoundTripTest : StringSpec({
    val corpus = buildString {
        repeat(200) {
            append("the quick brown fox jumps over the lazy dog. ")
            append("Olá mundo! Como vai você? ")
            append("日本語テスト。 ")
            append("hello world 1234 ")
        }
    }

    lateinit var tokenizer: BpeTokenizer

    beforeSpec {
        tokenizer = Trainer(TrainingConfig(numMerges = 200, verbose = false)).train(corpus)
    }

    "round-trip: ASCII plain text" {
        val text = "Hello, world!"
        tokenizer.decode(tokenizer.encode(text)) shouldBe text
    }

    "round-trip: text with accents" {
        val text = "Olá, você está bem?"
        tokenizer.decode(tokenizer.encode(text)) shouldBe text
    }

    "round-trip: emoji" {
        val text = "Hello 🎉 World 🌍"
        tokenizer.decode(tokenizer.encode(text)) shouldBe text
    }

    "round-trip: CJK characters" {
        val text = "日本語テスト"
        tokenizer.decode(tokenizer.encode(text)) shouldBe text
    }

    "round-trip: Arabic" {
        val text = "مرحبا بالعالم"
        tokenizer.decode(tokenizer.encode(text)) shouldBe text
    }

    "round-trip: Hebrew" {
        val text = "שלום עולם"
        tokenizer.decode(tokenizer.encode(text)) shouldBe text
    }

    "round-trip: math symbols" {
        val text = "∑∫∂∇ ≤ ≥ ≠ ∞"
        tokenizer.decode(tokenizer.encode(text)) shouldBe text
    }

    "round-trip: empty string" {
        tokenizer.decode(tokenizer.encode("")) shouldBe ""
    }

    "round-trip: newlines and whitespace" {
        val text = "line1\nline2\ttabbed\r\ncrlf"
        tokenizer.decode(tokenizer.encode(text)) shouldBe text
    }

    "round-trip: special token in allowed set" {
        val text = "start<|endoftext|>end"
        val allowed = setOf("<|endoftext|>")
        tokenizer.decode(tokenizer.encode(text, allowed)) shouldBe text
    }

    "round-trip: 100 mixed strings" {
        val samples = listOf(
            "The quick brown fox", "jumps over the lazy dog",
            "Olá mundo", "Comment ça va?", "Привет мир",
            "مرحبا", "שלום", "日本語", "한국어", "中文",
            "emoji: 🎉🌍🤖", "math: x² + y² = z²",
            "numbers: 123 456 789", "mixed: abc123def",
            "punctuation: !@#\$%^&*()", "newline\ntest",
            "tab\there", "quote: \"hello\"", "apostrophe: it's",
            "long: " + "a".repeat(100),
        )
        for (text in samples) {
            tokenizer.decode(tokenizer.encode(text)) shouldBe text
        }
    }
})
