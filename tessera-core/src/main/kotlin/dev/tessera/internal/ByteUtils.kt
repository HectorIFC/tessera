package dev.tessera.internal

internal object ByteUtils {
    fun stringToBytes(text: String): ByteArray = text.toByteArray(Charsets.UTF_8)
    fun bytesToString(bytes: ByteArray): String = bytes.toString(Charsets.UTF_8)
    fun byteToId(byte: Byte): Int = byte.toUByte().toInt()
}
