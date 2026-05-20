package dev.tessera

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class HelloWorldTest : StringSpec({
    "tessera-core module compiles and tests run" {
        val result = 1 + 1
        result shouldBe 2
    }

    "SpecialTokens default contains endoftext" {
        val st = SpecialTokens.default()
        st.tokens.containsKey("<|endoftext|>") shouldBe true
        st.tokens["<|endoftext|>"] shouldBe 256
    }

    "TrainingConfig defaults are sensible" {
        val config = TrainingConfig()
        config.numMerges shouldBe 5000
        config.verbose shouldBe true
    }
})
