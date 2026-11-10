package com.kripdroid.app.crypto

import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.encoders.Hex
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

object EncodingAndHash {

    // --- KODE MORSE ---
    private val CHAR_TO_MORSE = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".",
        'F' to "..-.", 'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---",
        'K' to "-.-", 'L' to ".-..", 'M' to "--", 'N' to "-.", 'O' to "---",
        'P' to ".--.", 'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-",
        'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-", 'Y' to "-.--",
        'Z' to "--..",
        '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-",
        '5' to ".....", '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----.",
        '.' to ".-.-.-", ',' to "--..--", '?' to "..--..", '\'' to ".----.",
        '!' to "-.-.--", '/' to "-..-.", '(' to "-.--.", ')' to "-.--.-",
        '&' to ".-...", ':' to "---...", ';' to "-.-.-.", '=' to "-...-",
        '+' to ".-.-.", '-' to "-....-", '_' to "..--.-", '"' to ".-..-.",
        '$' to "...-..-", '@' to ".--.-."
    )
    private val MORSE_TO_CHAR = CHAR_TO_MORSE.entries.associate { (k, v) -> v to k }

    fun morseEncode(text: String): String {
        val words = text.trim().split(Regex("\\s+"))
        return words.joinToString(" / ") { word ->
            word.uppercase().map { c ->
                CHAR_TO_MORSE[c] ?: c.toString()
            }.joinToString(" ")
        }
    }

    fun morseDecode(morse: String): String {
        val words = morse.trim().split(Regex("\\s*/\\s*|\\n+"))
        return words.joinToString(" ") { word ->
            val tokens = word.trim().split(Regex("\\s+"))
            tokens.map { token ->
                MORSE_TO_CHAR[token]?.toString() ?: token
            }.joinToString("")
        }
    }

    fun morseBytes(data: ByteArray, decrypt: Boolean): ByteArray {
        val str = String(data, Charsets.UTF_8)
        val res = if (decrypt) morseDecode(str) else morseEncode(str)
        return res.toByteArray(Charsets.UTF_8)
    }

    // --- BINER (BINARY 8-BIT) ---
    fun binaryEncode(text: String): String {
        return text.toByteArray(Charsets.UTF_8).joinToString(" ") { b ->
            String.format("%8s", Integer.toBinaryString(b.toInt() and 0xFF)).replace(' ', '0')
        }
    }

    fun binaryDecode(binaryStr: String): String {
        val clean = binaryStr.trim().replace(Regex("[^01\\s]"), "")
        val tokens = clean.split(Regex("\\s+")).filter { it.isNotEmpty() }
        val bytes = ByteArray(tokens.size) { i ->
            tokens[i].toInt(2).toByte()
        }
        return String(bytes, Charsets.UTF_8)
    }

    fun binaryBytes(data: ByteArray, decrypt: Boolean): ByteArray {
        return if (decrypt) {
            val str = String(data, Charsets.UTF_8).trim()
            val tokens = str.split(Regex("\\s+")).filter { it.isNotEmpty() }
            val res = ByteArray(tokens.size)
            for (i in tokens.indices) {
                res[i] = tokens[i].toInt(2).toByte()
            }
            res
        } else {
            data.joinToString(" ") { b ->
                String.format("%8s", Integer.toBinaryString(b.toInt() and 0xFF)).replace(' ', '0')
            }.toByteArray(Charsets.UTF_8)
        }
    }

    // --- BASE64 ---
    fun base64Encode(text: String): String {
        return Base64.toBase64String(text.toByteArray(Charsets.UTF_8))
    }

    fun base64Decode(b64Str: String): String {
        val clean = b64Str.trim().replace(Regex("[^A-Za-z0-9+/=]"), "")
        return String(Base64.decode(clean), Charsets.UTF_8)
    }

    fun base64Bytes(data: ByteArray, decrypt: Boolean): ByteArray {
        return if (decrypt) Base64.decode(data) else Base64.encode(data)
    }

    // --- HEXADECIMAL (BASE16) ---
    fun hexEncode(text: String): String {
        return Hex.toHexString(text.toByteArray(Charsets.UTF_8))
    }

    fun hexDecode(hexStr: String): String {
        val clean = hexStr.trim().replace(Regex("[^0-9a-fA-F]"), "")
        return String(Hex.decode(clean), Charsets.UTF_8)
    }

    fun hexBytes(data: ByteArray, decrypt: Boolean): ByteArray {
        return if (decrypt) Hex.decode(data) else Hex.encode(data)
    }

    // --- BASE32 (RFC 4648) ---
    private const val BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun base32Encode(text: String): String = base32Bytes(text.toByteArray(Charsets.UTF_8), decrypt = false).decodeToString()

    fun base32Decode(input: String): String = base32Bytes(input.toByteArray(Charsets.UTF_8), decrypt = true).decodeToString()

    fun base32Bytes(data: ByteArray, decrypt: Boolean): ByteArray {
        if (decrypt) {
            val clean = String(data, Charsets.UTF_8).trim().uppercase().replace("=", "")
            val bos = ByteArrayOutputStream()
            var buffer = 0
            var bitsLeft = 0
            for (c in clean) {
                val idx = BASE32_CHARS.indexOf(c)
                if (idx < 0) continue
                buffer = (buffer shl 5) or idx
                bitsLeft += 5
                if (bitsLeft >= 8) {
                    bitsLeft -= 8
                    bos.write((buffer shr bitsLeft) and 0xFF)
                }
            }
            return bos.toByteArray()
        } else {
            val out = StringBuilder()
            var buffer = 0
            var bitsLeft = 0
            for (b in data) {
                buffer = (buffer shl 8) or (b.toInt() and 0xFF)
                bitsLeft += 8
                while (bitsLeft >= 5) {
                    bitsLeft -= 5
                    val idx = (buffer shr bitsLeft) and 0x1F
                    out.append(BASE32_CHARS[idx])
                }
            }
            if (bitsLeft > 0) {
                val idx = (buffer shl (5 - bitsLeft)) and 0x1F
                out.append(BASE32_CHARS[idx])
            }
            while (out.length % 8 != 0) {
                out.append('=')
            }
            return out.toString().toByteArray(Charsets.UTF_8)
        }
    }

    // --- HASHING DIGEST (ONE-WAY) ---
    fun hashText(algo: String, text: String): String {
        val md = MessageDigest.getInstance(algo)
        val digest = md.digest(text.toByteArray(Charsets.UTF_8))
        return Hex.toHexString(digest)
    }

    fun hashBytes(algo: String, data: ByteArray): ByteArray {
        val md = MessageDigest.getInstance(algo)
        val digest = md.digest(data)
        return Hex.encode(digest)
    }

    // --- RAIL FENCE CIPHER (SANDI ZIG-ZAG) ---
    private fun parseRails(keyStr: String): Int {
        val trimmed = keyStr.trim()
        val num = trimmed.toIntOrNull()
        if (num != null && num >= 2) return num
        val sum = trimmed.fold(0) { acc, c -> acc + c.code }
        return if (sum <= 0) 3 else ((sum % 5) + 2)
    }

    fun railFenceEncrypt(text: String, keyStr: String): String {
        val rails = parseRails(keyStr)
        if (rails <= 1 || text.length <= rails) return text

        val fence = Array(rails) { StringBuilder() }
        var rail = 0
        var direction = 1

        for (c in text) {
            fence[rail].append(c)
            rail += direction
            if (rail == 0 || rail == rails - 1) {
                direction = -direction
            }
        }
        return fence.joinToString("") { it.toString() }
    }

    fun railFenceDecrypt(cipherText: String, keyStr: String): String {
        val rails = parseRails(keyStr)
        if (rails <= 1 || cipherText.length <= rails) return cipherText

        val pattern = Array(rails) { BooleanArray(cipherText.length) }
        var rail = 0
        var direction = 1

        for (i in cipherText.indices) {
            pattern[rail][i] = true
            rail += direction
            if (rail == 0 || rail == rails - 1) {
                direction = -direction
            }
        }

        var idx = 0
        val filled = Array(rails) { CharArray(cipherText.length) }
        for (r in 0 until rails) {
            for (c in cipherText.indices) {
                if (pattern[r][c] && idx < cipherText.length) {
                    filled[r][c] = cipherText[idx++]
                }
            }
        }

        val sb = StringBuilder()
        rail = 0
        direction = 1
        for (i in cipherText.indices) {
            sb.append(filled[rail][i])
            rail += direction
            if (rail == 0 || rail == rails - 1) {
                direction = -direction
            }
        }
        return sb.toString()
    }

    fun railFenceBytes(data: ByteArray, keyStr: String, decrypt: Boolean): ByteArray {
        val str = String(data, Charsets.UTF_8)
        val res = if (decrypt) railFenceDecrypt(str, keyStr) else railFenceEncrypt(str, keyStr)
        return res.toByteArray(Charsets.UTF_8)
    }

    // --- AFFINE CIPHER (E(x) = (ax + b) mod 26) ---
    private fun parseAffineKey(keyStr: String): Pair<Int, Int> {
        val trimmed = keyStr.trim()
        val parts = trimmed.split(Regex("[,\\s;]+"))
        if (parts.size >= 2) {
            val a = parts[0].toIntOrNull() ?: 5
            val b = parts[1].toIntOrNull() ?: 8
            val validA = if (gcd(a, 26) == 1) a else 5
            return Pair(validA, (b % 26 + 26) % 26)
        }
        val sum = trimmed.fold(0) { acc, c -> acc + c.code }
        val validAs = listOf(1, 3, 5, 7, 9, 11, 15, 17, 19, 21, 23, 25)
        val a = validAs[(sum % validAs.size + validAs.size) % validAs.size]
        val b = (sum % 26 + 26) % 26
        return Pair(a, b)
    }

    private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

    private fun modInverse(a: Int, m: Int): Int {
        for (x in 1 until m) {
            if ((a * x) % m == 1) return x
        }
        return 1
    }

    fun affineEncrypt(text: String, keyStr: String): String {
        val (a, b) = parseAffineKey(keyStr)
        val sb = StringBuilder()
        for (c in text) {
            when (c) {
                in 'A'..'Z' -> {
                    val x = c.code - 'A'.code
                    sb.append(('A'.code + (a * x + b) % 26).toChar())
                }
                in 'a'..'z' -> {
                    val x = c.code - 'a'.code
                    sb.append(('a'.code + (a * x + b) % 26).toChar())
                }
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    fun affineDecrypt(cipherText: String, keyStr: String): String {
        val (a, b) = parseAffineKey(keyStr)
        val aInv = modInverse(a, 26)
        val sb = StringBuilder()
        for (c in cipherText) {
            when (c) {
                in 'A'..'Z' -> {
                    val y = c.code - 'A'.code
                    val x = (aInv * (y - b + 26)) % 26
                    sb.append(('A'.code + (x + 26) % 26).toChar())
                }
                in 'a'..'z' -> {
                    val y = c.code - 'a'.code
                    val x = (aInv * (y - b + 26)) % 26
                    sb.append(('a'.code + (x + 26) % 26).toChar())
                }
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    fun affineBytes(data: ByteArray, keyStr: String, decrypt: Boolean): ByteArray {
        val str = String(data, Charsets.UTF_8)
        val res = if (decrypt) affineDecrypt(str, keyStr) else affineEncrypt(str, keyStr)
        return res.toByteArray(Charsets.UTF_8)
    }

    // --- BACON'S CIPHER ---
    private val BACON_MAP = mapOf(
        'A' to "AAAAA", 'B' to "AAAAB", 'C' to "AAABA", 'D' to "AAABB", 'E' to "AABAA",
        'F' to "AABAB", 'G' to "AABBA", 'H' to "AABBB", 'I' to "ABAAA", 'J' to "ABAAB",
        'K' to "ABABA", 'L' to "ABABB", 'M' to "ABBAA", 'N' to "ABBAB", 'O' to "ABBBA",
        'P' to "ABBBB", 'Q' to "BAAAA", 'R' to "BAAAB", 'S' to "BAABA", 'T' to "BAABB",
        'U' to "BABAA", 'V' to "BABAB", 'W' to "BABBA", 'X' to "BABBB", 'Y' to "BBAAA",
        'Z' to "BBAAB"
    )
    private val BACON_INV = BACON_MAP.entries.associate { (k, v) -> v to k }

    fun baconEncrypt(text: String): String {
        val words = text.trim().split(Regex("\\s+"))
        return words.joinToString(" / ") { word ->
            word.uppercase().map { c ->
                BACON_MAP[c] ?: c.toString()
            }.joinToString(" ")
        }
    }

    fun baconDecrypt(text: String): String {
        val words = text.trim().split(Regex("\\s*/\\s*|\\n+"))
        return words.joinToString(" ") { word ->
            val tokens = word.trim().split(Regex("\\s+"))
            tokens.map { token ->
                val upper = token.uppercase()
                BACON_INV[upper]?.toString() ?: token
            }.joinToString("")
        }
    }

    fun baconBytes(data: ByteArray, decrypt: Boolean): ByteArray {
        val str = String(data, Charsets.UTF_8)
        val res = if (decrypt) baconDecrypt(str) else baconEncrypt(str)
        return res.toByteArray(Charsets.UTF_8)
    }
}
