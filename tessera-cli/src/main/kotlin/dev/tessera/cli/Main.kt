package dev.tessera.cli

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        printUsage()
        return
    }

    val command = args[0]
    val rest = args.drop(1).toTypedArray()

    when (command) {
        "train" -> TrainCommand.run(rest)
        "encode" -> EncodeCommand.run(rest)
        "decode" -> DecodeCommand.run(rest)
        "inspect" -> InspectCommand.run(rest)
        "--help", "-h", "help" -> printUsage()
        else -> {
            System.err.println("Unknown command: $command")
            printUsage()
            System.exit(1)
        }
    }
}

private fun printUsage() {
    println(
        """
        Tessera CLI — byte-level BPE tokenizer

        Usage:
          tessera train    --corpus <file> [--merges <n>] [--output <file>]
          tessera encode   --tokenizer <file> --text <text>
          tessera decode   --tokenizer <file> --ids <id,id,...>
          tessera inspect  --tokenizer <file> [--limit <n>]

        Examples:
          tessera train --corpus corpus/text.txt --merges 5000 --output tessera.json
          tessera encode --tokenizer tessera.json --text "Hello, world!"
          tessera decode --tokenizer tessera.json --ids "256,104,101,108"
          tessera inspect --tokenizer tessera.json --limit 20
        """.trimIndent(),
    )
}

/** Parses "--key value" pairs from an args array. */
internal fun parseArgs(args: Array<String>): Map<String, String> {
    val result = mutableMapOf<String, String>()
    var i = 0
    while (i < args.size) {
        val key = args[i]
        if (key.startsWith("--") && i + 1 < args.size) {
            result[key.removePrefix("--")] = args[i + 1]
            i += 2
        } else {
            i++
        }
    }
    return result
}

internal fun requireArg(args: Map<String, String>, key: String): String = args[key] ?: run {
    System.err.println("Missing required argument: --$key")
    System.exit(1)
    error("unreachable")
}
