package dev.tessera.internal

internal object ByteUtils {
    fun stringToBytes(text: String): ByteArray = text.toByteArray(Charsets.UTF_8)

    fun bytesToString(bytes: ByteArray): String = bytes.toString(Charsets.UTF_8)

    // Kotlin Byte is signed (-128..127); BPE needs unsigned IDs (0..255)
    fun byteToId(b: Byte): Int = b.toUByte().toInt()

    fun idToByte(id: Int): Byte = id.toUByte().toByte()
}
