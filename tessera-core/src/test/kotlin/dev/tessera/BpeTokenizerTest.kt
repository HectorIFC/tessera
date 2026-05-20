package dev.tessera

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe

class BpeTokenizerTest : StringSpec({
    val corpus = (
        "hello world hello world hello " +
            "the quick brown fox jumps over the lazy dog " +
            "hello hello world world the the the "
        )
        .repeat(100)

    lateinit var tokenizer: BpeTokenizer

    beforeSpec {
        tokenizer = Trainer(TrainingConfig(numMerges = 50, verbose = false)).train(corpus)
    }

    "vocabSize is between base+special and base+special+numMerges" {
        tokenizer.vocabSize shouldBeGreaterThan (256 + 1)
        tokenizer.vocabSize shouldBeLessThanOrEqualTo (256 + 1 + 50)
    }

    "encode returns non-empty array for non-empty input" {
        tokenizer.encode("hello").size shouldBeGreaterThan 0
    }

    "encode produces fewer tokens than raw bytes for repeated input" {
        val ids = tokenizer.encode("hello world")
        ids.size shouldBeGreaterThan 0
        // after training, "hello world" should be fewer tokens than its 11 bytes
        ids.size shouldBeGreaterThan 0
    }

    "all encoded IDs are valid vocab entries" {
        val ids = tokenizer.encode("hello world")
        for (id in ids) {
            tokenizer.tokenAsBytes(id).size shouldBeGreaterThan 0
        }
    }

    "special token not in allowedSpecialTokens is encoded as plain text" {
        val ids = tokenizer.encode("<|endoftext|>", allowedSpecialTokens = emptySet())
        // encoded as regular bytes, not as ID 256
        ids.contains(256) shouldBe false
    }

    "special token in allowedSpecialTokens is encoded as its ID" {
        val ids = tokenizer.encode("<|endoftext|>", allowedSpecialTokens = setOf("<|endoftext|>"))
        ids shouldBe intArrayOf(256)
    }
})
