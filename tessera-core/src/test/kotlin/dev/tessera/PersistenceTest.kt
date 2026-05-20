package dev.tessera

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PersistenceTest : StringSpec({
    val corpus = "hello world the quick brown fox ".repeat(50)

    lateinit var tokenizer: BpeTokenizer

    beforeSpec {
        tokenizer = Trainer(TrainingConfig(numMerges = 30, verbose = false)).train(corpus)
    }

    "save and load produces identical encoding" {
        val tmpFile = java.io.File.createTempFile("tessera-test", ".json")
        try {
            tokenizer.save(tmpFile)
            val loaded = BpeTokenizer.load(tmpFile)

            val text = "hello world"
            loaded.encode(text).toList() shouldBe tokenizer.encode(text).toList()
        } finally {
            tmpFile.delete()
        }
    }

    "loaded tokenizer has same vocabSize" {
        val tmpFile = java.io.File.createTempFile("tessera-test", ".json")
        try {
            tokenizer.save(tmpFile)
            val loaded = BpeTokenizer.load(tmpFile)
            loaded.vocabSize shouldBe tokenizer.vocabSize
        } finally {
            tmpFile.delete()
        }
    }

    "round-trip preserved after save and load" {
        val tmpFile = java.io.File.createTempFile("tessera-test", ".json")
        try {
            tokenizer.save(tmpFile)
            val loaded = BpeTokenizer.load(tmpFile)

            val texts = listOf("hello world", "Olá mundo!", "the quick brown fox 🦊")
            for (text in texts) {
                loaded.decode(loaded.encode(text)) shouldBe text
            }
        } finally {
            tmpFile.delete()
        }
    }

    "saved JSON contains expected fields" {
        val tmpFile = java.io.File.createTempFile("tessera-test", ".json")
        try {
            tokenizer.save(tmpFile)
            val content = tmpFile.readText()
            (content.contains("\"version\"")) shouldBe true
            (content.contains("\"merges\"")) shouldBe true
            (content.contains("\"specialTokens\"")) shouldBe true
        } finally {
            tmpFile.delete()
        }
    }
})
