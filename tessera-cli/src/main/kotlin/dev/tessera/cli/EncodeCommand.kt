package dev.tessera.cli

import dev.tessera.BpeTokenizer

internal object EncodeCommand {
    fun run(args: Array<String>) {
        val params = parseArgs(args)
        val tokenizerPath = requireArg(params, "tokenizer")
        val text = requireArg(params, "text")

        val tokenizer = BpeTokenizer.load(tokenizerPath)
        val ids = tokenizer.encode(text)

        println("Input : \"$text\"")
        println("Tokens: ${ids.size}")
        println("IDs   : ${ids.joinToString(",")}")
    }
}
