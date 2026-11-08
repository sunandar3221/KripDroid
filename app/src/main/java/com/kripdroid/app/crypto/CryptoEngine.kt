package com.kripdroid.app.crypto

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.encoders.Hex
import java.security.MessageDigest
import java.security.SecureRandom

object CryptoEngine {

    private val secureRandom = SecureRandom()

    fun deriveKeyArgon2id(passphrase: String, salt: ByteArray?, targetBytes: Int): ByteArray {
        if (targetBytes <= 0) return passphrase.toByteArray(Charsets.UTF_8)
        val effectiveSalt = if (salt == null || salt.isEmpty()) {
            "KripDroidArgon2idSalt2026".toByteArray(Charsets.UTF_8)
        } else {
            salt
        }

        val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(2)
            .withMemoryAsKB(64 * 1024)
            .withParallelism(4)
            .withSalt(effectiveSalt)
            .build()

        val generator = Argon2BytesGenerator()
        generator.init(params)
        val result = ByteArray(targetBytes)
        generator.generateBytes(passphrase.toByteArray(Charsets.UTF_8), result, 0, result.size)
        return result
    }

    fun deriveKey(passphrase: String, targetBytes: Int): ByteArray {
        if (targetBytes <= 0) return passphrase.toByteArray(Charsets.UTF_8)
        val trimmed = passphrase.trim()
        if (trimmed.length == targetBytes * 2) {
            try {
                val hx = Hex.decode(trimmed)
                if (hx.size == targetBytes) return hx
            } catch (_: Exception) {}
        }
        val raw = passphrase.toByteArray(Charsets.UTF_8)
        if (raw.size == targetBytes) {
            return raw
        }
        return if (targetBytes <= 32) {
            val md = MessageDigest.getInstance("SHA-256")
            md.digest(raw).copyOf(targetBytes)
        } else {
            val md = MessageDigest.getInstance("SHA-512")
            md.digest(raw).copyOf(targetBytes)
        }
    }

    fun parseIV(ivStr: String, targetBytes: Int): ByteArray? {
        if (targetBytes <= 0) return null
        val trimmed = ivStr.trim()
        if (trimmed.isEmpty()) return null
        try {
            val hx = Hex.decode(trimmed)
            if (hx.size == targetBytes) return hx
        } catch (_: Exception) {}

        val raw = trimmed.toByteArray(Charsets.UTF_8)
        if (raw.size == targetBytes) {
            return raw
        }
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(raw).copyOf(targetBytes)
    }

    fun encryptBytesWithSalt(
        algoId: String,
        plain: ByteArray,
        keyStr: String,
        customIv: ByteArray?,
        salt: ByteArray?
    ): Pair<ByteArray, ByteArray> {
        val algo = CryptoAlgorithms.getAlgorithm(algoId)
        if (algo.needsKey && keyStr.trim().isEmpty()) {
            throw IllegalArgumentException("Kunci rahasia wajib diisi untuk algoritma ini")
        }

        val key = if (algo.needsKey) {
            if (salt != null && salt.isNotEmpty()) {
                deriveKeyArgon2id(keyStr, salt, algo.keySize)
            } else {
                deriveKey(keyStr, algo.keySize)
            }
        } else {
            ByteArray(0)
        }

        return when (algoId.lowercase()) {
            "caesar" -> Pair(ClassicCiphers.caesarBytes(plain, keyStr, decrypt = false), ByteArray(0))
            "rot13" -> Pair(ClassicCiphers.rot13Bytes(plain, decrypt = false), ByteArray(0))
            "atbash" -> Pair(ClassicCiphers.atbashBytes(plain), ByteArray(0))
            "vigenere" -> Pair(ClassicCiphers.vigenereBytes(plain, keyStr, decrypt = false), ByteArray(0))
            "xor" -> Pair(ClassicCiphers.xorCipher(plain, keyStr.toByteArray(Charsets.UTF_8)), ByteArray(0))
            "des" -> LegacyCiphers.desEncrypt(plain, key, customIv)
            "tripledes" -> LegacyCiphers.tripleDesEncrypt(plain, key, customIv)
            "blowfish" -> LegacyCiphers.blowfishEncrypt(plain, key, customIv)
            "aes128cbc", "aes256cbc" -> ModernCiphers.aesCbcEncrypt(plain, key, customIv)
            "aes128gcm", "aes256gcm" -> ModernCiphers.aesGcmEncrypt(plain, key, customIv)
            "chacha20poly1305" -> ModernCiphers.chaCha20Poly1305Encrypt(plain, key, customIv)
            else -> throw IllegalArgumentException("Algoritma belum didukung: $algoId")
        }
    }

    fun decryptBytesWithSalt(
        algoId: String,
        cipherData: ByteArray,
        keyStr: String,
        iv: ByteArray?,
        salt: ByteArray?
    ): ByteArray {
        val algo = CryptoAlgorithms.getAlgorithm(algoId)
        if (algo.needsKey && keyStr.trim().isEmpty()) {
            throw IllegalArgumentException("Kunci rahasia wajib diisi untuk algoritma ini")
        }

        val key = if (algo.needsKey) {
            if (salt != null && salt.isNotEmpty()) {
                deriveKeyArgon2id(keyStr, salt, algo.keySize)
            } else {
                deriveKey(keyStr, algo.keySize)
            }
        } else {
            ByteArray(0)
        }

        val safeIv = iv ?: ByteArray(0)

        return when (algoId.lowercase()) {
            "caesar" -> ClassicCiphers.caesarBytes(cipherData, keyStr, decrypt = true)
            "rot13" -> ClassicCiphers.rot13Bytes(cipherData, decrypt = true)
            "atbash" -> ClassicCiphers.atbashBytes(cipherData)
            "vigenere" -> ClassicCiphers.vigenereBytes(cipherData, keyStr, decrypt = true)
            "xor" -> ClassicCiphers.xorCipher(cipherData, keyStr.toByteArray(Charsets.UTF_8))
            "des" -> LegacyCiphers.desDecrypt(cipherData, key, safeIv)
            "tripledes" -> LegacyCiphers.tripleDesDecrypt(cipherData, key, safeIv)
            "blowfish" -> LegacyCiphers.blowfishDecrypt(cipherData, key, safeIv)
            "aes128cbc", "aes256cbc" -> ModernCiphers.aesCbcDecrypt(cipherData, key, safeIv)
            "aes128gcm", "aes256gcm" -> ModernCiphers.aesGcmDecrypt(cipherData, key, safeIv)
            "chacha20poly1305" -> ModernCiphers.chaCha20Poly1305Decrypt(cipherData, key, safeIv)
            else -> throw IllegalArgumentException("Algoritma belum didukung: $algoId")
        }
    }

    fun encryptText(algoId: String, plainText: String, keyStr: String, ivStr: String): Pair<String, String> {
        val algo = CryptoAlgorithms.getAlgorithm(algoId)
        if (algo.category == SecurityCategory.Classic && algoId != "xor") {
            return when (algoId) {
                "caesar" -> Pair(ClassicCiphers.caesarEncrypt(plainText, keyStr), "")
                "rot13" -> Pair(ClassicCiphers.rot13(plainText), "")
                "atbash" -> Pair(ClassicCiphers.atbash(plainText), "")
                "vigenere" -> Pair(ClassicCiphers.vigenereEncrypt(plainText, keyStr), "")
                else -> throw IllegalArgumentException("Unknown classic algorithm: $algoId")
            }
        }

        val ivBytes = parseIV(ivStr, algo.ivSize)
        val salt = ByteArray(16).also { secureRandom.nextBytes(it) }

        val (cipherBytes, usedIv) = encryptBytesWithSalt(
            algoId = algoId,
            plain = plainText.toByteArray(Charsets.UTF_8),
            keyStr = keyStr,
            customIv = ivBytes,
            salt = salt
        )

        val ivHex = if (usedIv.isNotEmpty()) Hex.toHexString(usedIv) else ""

        if (algoId == "xor") {
            return Pair(Base64.toBase64String(cipherBytes), "")
        }

        val payload = ByteArray(1 + salt.size + usedIv.size + cipherBytes.size)
        payload[0] = salt.size.toByte()
        System.arraycopy(salt, 0, payload, 1, salt.size)
        if (usedIv.isNotEmpty()) {
            System.arraycopy(usedIv, 0, payload, 1 + salt.size, usedIv.size)
        }
        System.arraycopy(cipherBytes, 0, payload, 1 + salt.size + usedIv.size, cipherBytes.size)

        return Pair(Base64.toBase64String(payload), ivHex)
    }

    fun decryptText(algoId: String, cipherText: String, keyStr: String, ivStr: String): String {
        val algo = CryptoAlgorithms.getAlgorithm(algoId)
        if (algo.category == SecurityCategory.Classic && algoId != "xor") {
            return when (algoId) {
                "caesar" -> ClassicCiphers.caesarDecrypt(cipherText, keyStr)
                "rot13" -> ClassicCiphers.rot13(cipherText)
                "atbash" -> ClassicCiphers.atbash(cipherText)
                "vigenere" -> ClassicCiphers.vigenereDecrypt(cipherText, keyStr)
                else -> throw IllegalArgumentException("Unknown classic algorithm: $algoId")
            }
        }

        val trimmed = cipherText.trim()
        val rawData = try {
            Base64.decode(trimmed)
        } catch (_: Exception) {
            try {
                Hex.decode(trimmed)
            } catch (_: Exception) {
                trimmed.toByteArray(Charsets.UTF_8)
            }
        }

        if (algoId == "xor") {
            val dec = decryptBytesWithSalt(algoId, rawData, keyStr, null, null)
            return String(dec, Charsets.UTF_8)
        }

        if (rawData.size > 1 + 16 + algo.ivSize && rawData[0] == 16.toByte()) {
            val salt = rawData.copyOfRange(1, 17)
            val offset = 17
            val ivBytes = if (ivStr.trim().isNotEmpty()) {
                parseIV(ivStr, algo.ivSize) ?: rawData.copyOfRange(offset, offset + algo.ivSize)
            } else {
                rawData.copyOfRange(offset, offset + algo.ivSize)
            }
            val cipherBytes = rawData.copyOfRange(offset + algo.ivSize, rawData.size)
            try {
                val dec = decryptBytesWithSalt(algoId, cipherBytes, keyStr, ivBytes, salt)
                return String(dec, Charsets.UTF_8)
            } catch (_: Exception) {
                // Fall through to legacy format fallback
            }
        }

        val ivBytes: ByteArray
        val cipherBytes: ByteArray
        if (ivStr.trim().isNotEmpty()) {
            ivBytes = parseIV(ivStr, algo.ivSize) ?: ByteArray(0)
            cipherBytes = rawData
        } else {
            if (rawData.size < algo.ivSize) {
                throw IllegalArgumentException("Ciphertext terlalu pendek untuk mengekstrak IV/nonce")
            }
            ivBytes = if (algo.ivSize > 0) rawData.copyOfRange(0, algo.ivSize) else ByteArray(0)
            cipherBytes = if (algo.ivSize > 0) rawData.copyOfRange(algo.ivSize, rawData.size) else rawData
        }

        val dec = decryptBytesWithSalt(algoId, cipherBytes, keyStr, ivBytes, null)
        return String(dec, Charsets.UTF_8)
    }
}
