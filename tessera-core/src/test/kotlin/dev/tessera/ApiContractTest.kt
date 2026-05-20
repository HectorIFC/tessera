package dev.tessera

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * Verifies that the public API is stable and usable as documented in the PRD.
 * Only touches public symbols — no internal classes.
 */
class ApiContractTest : StringSpec({
    "TrainingConfig has correct defaults" {
        val config = TrainingConfig()
        config.numMerges shouldBe 5000
        config.verbose shouldBe true
        config.progressCallback shouldBe null
    }

    "SpecialTokens.default contains endoftext at ID 256" {
        val st = SpecialTokens.default()
        st.tokens["<|endoftext|>"] shouldBe 256
    }

    "SpecialTokens.of assigns sequential IDs from 256" {
        val st = SpecialTokens.of("<|start|>", "<|end|>")
        st.tokens["<|start|>"] shouldBe 256
        st.tokens["<|end|>"] shouldBe 257
    }

    "Trainer accepts custom TrainingConfig" {
        val config = TrainingConfig(numMerges = 10, verbose = false)
        val trainer = Trainer(config)
        trainer.config.numMerges shouldBe 10
    }

    "full API contract: train -> encode -> decode -> save -> load" {
        val tokenizer = Trainer(TrainingConfig(numMerges = 20, verbose = false))
            .train("hello world the quick brown fox ".repeat(30))

        tokenizer shouldNotBe null
        tokenizer.vocabSize shouldBeGreaterThan 256

        val ids = tokenizer.encode("hello world")
        ids.size shouldBeGreaterThan 0

        val decoded = tokenizer.decode(ids)
        decoded shouldBe "hello world"

        val tmpFile = java.io.File.createTempFile("api-contract", ".json")
        try {
            tokenizer.save(tmpFile)
            val loaded = BpeTokenizer.load(tmpFile)
            loaded.decode(loaded.encode("hello world")) shouldBe "hello world"
        } finally {
            tmpFile.delete()
        }
    }

    "progressCallback is invoked during training" {
        var callCount = 0
        val config = TrainingConfig(
            numMerges = 10,
            verbose = false,
            progressCallback = { callCount++ },
        )
        Trainer(config).train("hello world ".repeat(50))
        callCount shouldBe 10
    }
})
