package dev.tessera.cli

import dev.tessera.BpeTokenizer

internal object DecodeCommand {
    fun run(args: Array<String>) {
        val params = parseArgs(args)
        val tokenizerPath = requireArg(params, "tokenizer")
        val idsStr = requireArg(params, "ids")

        val ids = idsStr.split(",")
            .map { it.trim().toIntOrNull() ?: run {
                System.err.println("Invalid ID: '${it.trim()}' — expected integers separated by commas")
                System.exit(1)
                error("unreachable")
            }}
            .toIntArray()

        val tokenizer = BpeTokenizer.load(tokenizerPath)
        val text = tokenizer.decode(ids)

        println("IDs   : ${ids.joinToString(",")}")
        println("Output: \"$text\"")
    }
}
