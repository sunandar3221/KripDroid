package com.kripdroid.app.crypto

object ClassicCiphers {

    fun parseShift(key: String): Int {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) return 3
        trimmed.toIntOrNull()?.let { return it }
        val total = trimmed.fold(0) { acc, c -> acc + c.code }
        return if (total == 0) 3 else total
    }

    fun caesarEncrypt(plainText: String, key: String): String {
        val shift = (parseShift(key) % 26 + 26) % 26
        val sb = java.lang.StringBuilder()
        for (c in plainText) {
            when {
                c in 'A'..'Z' -> sb.append(('A'.code + (c.code - 'A'.code + shift) % 26).toChar())
                c in 'a'..'z' -> sb.append(('a'.code + (c.code - 'a'.code + shift) % 26).toChar())
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    fun caesarDecrypt(cipherText: String, key: String): String {
        val shift = (parseShift(key) % 26 + 26) % 26
        val invShift = (26 - shift) % 26
        val sb = java.lang.StringBuilder()
        for (c in cipherText) {
            when {
                c in 'A'..'Z' -> sb.append(('A'.code + (c.code - 'A'.code + invShift) % 26).toChar())
                c in 'a'..'z' -> sb.append(('a'.code + (c.code - 'a'.code + invShift) % 26).toChar())
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    fun caesarBytes(data: ByteArray, key: String, decrypt: Boolean): ByteArray {
        var shift = parseShift(key)
        if (decrypt) shift = -shift
        val s = (shift % 256 + 256) % 256
        val res = ByteArray(data.size)
        for (i in data.indices) {
            res[i] = (data[i].toInt() + s).toByte()
        }
        return res
    }

    fun rot13(input: String): String {
        val sb = java.lang.StringBuilder()
        for (c in input) {
            when {
                c in 'A'..'Z' -> sb.append(('A'.code + (c.code - 'A'.code + 13) % 26).toChar())
                c in 'a'..'z' -> sb.append(('a'.code + (c.code - 'a'.code + 13) % 26).toChar())
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    fun rot13Bytes(data: ByteArray, decrypt: Boolean = false): ByteArray {
        val res = ByteArray(data.size)
        val shift = if (decrypt) -13 else 13
        for (i in data.indices) {
            val b = data[i].toInt() and 0xFF
            when (b.toChar()) {
                in 'A'..'Z' -> res[i] = ('A'.code + (b - 'A'.code + 13) % 26).toByte()
                in 'a'..'z' -> res[i] = ('a'.code + (b - 'a'.code + 13) % 26).toByte()
                else -> res[i] = ((b + shift) and 0xFF).toByte()
            }
        }
        return res
    }

    fun atbash(input: String): String {
        val sb = java.lang.StringBuilder()
        for (c in input) {
            when {
                c in 'A'..'Z' -> sb.append(('Z'.code - (c.code - 'A'.code)).toChar())
                c in 'a'..'z' -> sb.append(('z'.code - (c.code - 'a'.code)).toChar())
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    fun atbashBytes(data: ByteArray): ByteArray {
        val res = ByteArray(data.size)
        for (i in data.indices) {
            val b = data[i].toInt() and 0xFF
            when (b.toChar()) {
                in 'A'..'Z' -> res[i] = ('Z'.code - (b - 'A'.code)).toByte()
                in 'a'..'z' -> res[i] = ('z'.code - (b - 'a'.code)).toByte()
                else -> res[i] = (255 - b).toByte()
            }
        }
        return res
    }

    fun vigenereEncrypt(plainText: String, key: String): String {
        val k = key.trim()
        require(k.isNotEmpty()) { "Kunci Vigenere tidak boleh kosong" }
        val letters = k.filter { it.isLetter() }.map { it.uppercaseChar() }
        val cleanKey = if (letters.isNotEmpty()) letters else k.map { it.uppercaseChar() }

        val sb = java.lang.StringBuilder()
        var keyIdx = 0
        for (c in plainText) {
            when {
                c in 'A'..'Z' -> {
                    val shift = cleanKey[keyIdx % cleanKey.size].code - 'A'.code
                    sb.append(('A'.code + (c.code - 'A'.code + shift) % 26).toChar())
                    keyIdx++
                }
                c in 'a'..'z' -> {
                    val shift = cleanKey[keyIdx % cleanKey.size].code - 'A'.code
                    sb.append(('a'.code + (c.code - 'a'.code + shift) % 26).toChar())
                    keyIdx++
                }
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    fun vigenereDecrypt(cipherText: String, key: String): String {
        val k = key.trim()
        require(k.isNotEmpty()) { "Kunci Vigenere tidak boleh kosong" }
        val letters = k.filter { it.isLetter() }.map { it.uppercaseChar() }
        val cleanKey = if (letters.isNotEmpty()) letters else k.map { it.uppercaseChar() }

        val sb = java.lang.StringBuilder()
        var keyIdx = 0
        for (c in cipherText) {
            when {
                c in 'A'..'Z' -> {
                    val shift = (cleanKey[keyIdx % cleanKey.size].code - 'A'.code) % 26
                    sb.append(('A'.code + (c.code - 'A'.code - shift + 26) % 26).toChar())
                    keyIdx++
                }
                c in 'a'..'z' -> {
                    val shift = (cleanKey[keyIdx % cleanKey.size].code - 'A'.code) % 26
                    sb.append(('a'.code + (c.code - 'a'.code - shift + 26) % 26).toChar())
                    keyIdx++
                }
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    fun vigenereBytes(data: ByteArray, key: String, decrypt: Boolean): ByteArray {
        val k = key.toByteArray(Charsets.UTF_8)
        require(k.isNotEmpty()) { "Kunci Vigenere tidak boleh kosong" }
        val res = ByteArray(data.size)
        for (i in data.indices) {
            val shift = k[i % k.size].toInt()
            res[i] = if (decrypt) (data[i].toInt() - shift).toByte() else (data[i].toInt() + shift).toByte()
        }
        return res
    }

    fun xorCipher(data: ByteArray, key: ByteArray): ByteArray {
        require(key.isNotEmpty()) { "Kunci XOR tidak boleh kosong" }
        val res = ByteArray(data.size)
        for (i in data.indices) {
            res[i] = (data[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
        return res
    }
}
