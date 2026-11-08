package com.kripdroid.app.crypto

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.security.SecureRandom

data class FileProcessResult(
    val algoId: String,
    val originalExt: String,
    val inputSize: Long,
    val outputSize: Long,
    val durationMs: Long
)

object FileCrypto {

    val FileMagicV1 = "KRIPDROID\u0001".toByteArray(Charsets.ISO_8859_1)
    val FileMagicV2 = "KRIPDROID\u0002".toByteArray(Charsets.ISO_8859_1)
    val FileMagicV3 = "KRIPDROID\u0003".toByteArray(Charsets.ISO_8859_1)

    private val secureRandom = SecureRandom()

    fun encryptFileData(
        plainData: ByteArray,
        algoId: String,
        keyStr: String,
        customIv: ByteArray?,
        originalExt: String
    ): ByteArray {
        val salt = ByteArray(16).also { secureRandom.nextBytes(it) }
        val (cipherData, usedIv) = CryptoEngine.encryptBytesWithSalt(
            algoId = algoId,
            plain = plainData,
            keyStr = keyStr,
            customIv = customIv,
            salt = salt
        )

        val bos = ByteArrayOutputStream()
        bos.write(FileMagicV3)

        val algoBytes = algoId.toByteArray(Charsets.UTF_8)
        bos.write(algoBytes.size)
        bos.write(algoBytes)

        var cleanExt = originalExt.trim()
        if (cleanExt.isNotEmpty() && !cleanExt.startsWith(".")) {
            cleanExt = ".$cleanExt"
        }
        val extBytes = cleanExt.toByteArray(Charsets.UTF_8)
        bos.write(extBytes.size)
        if (extBytes.isNotEmpty()) {
            bos.write(extBytes)
        }

        bos.write(salt.size)
        if (salt.isNotEmpty()) {
            bos.write(salt)
        }

        bos.write(usedIv.size)
        if (usedIv.isNotEmpty()) {
            bos.write(usedIv)
        }

        val sizeBuf = ByteBuffer.allocate(8).putLong(plainData.size.toLong()).array()
        bos.write(sizeBuf)
        bos.write(cipherData)

        return bos.toByteArray()
    }

    data class DecryptedFileData(
        val plainData: ByteArray,
        val algoId: String,
        val originalExt: String
    )

    fun decryptFileData(
        rawData: ByteArray,
        keyStr: String,
        fallbackAlgoId: String = ""
    ): DecryptedFileData {
        val magicLen = 10
        if (rawData.size >= magicLen && startsWith(rawData, FileMagicV3)) {
            var offset = magicLen
            if (offset >= rawData.size) throw IllegalArgumentException("Header file rusak")
            val algoLen = rawData[offset].toInt() and 0xFF
            offset++
            if (offset + algoLen > rawData.size) throw IllegalArgumentException("Header algoritma rusak")
            val algoId = String(rawData, offset, algoLen, Charsets.UTF_8)
            offset += algoLen

            if (offset >= rawData.size) throw IllegalArgumentException("Header format rusak")
            val extLen = rawData[offset].toInt() and 0xFF
            offset++
            if (offset + extLen > rawData.size) throw IllegalArgumentException("Data format rusak")
            var originalExt = ""
            if (extLen > 0) {
                originalExt = String(rawData, offset, extLen, Charsets.UTF_8)
                offset += extLen
            }

            if (offset >= rawData.size) throw IllegalArgumentException("Header salt rusak")
            val saltLen = rawData[offset].toInt() and 0xFF
            offset++
            if (offset + saltLen > rawData.size) throw IllegalArgumentException("Data salt rusak")
            var salt: ByteArray? = null
            if (saltLen > 0) {
                salt = rawData.copyOfRange(offset, offset + saltLen)
                offset += saltLen
            }

            if (offset >= rawData.size) throw IllegalArgumentException("Header IV rusak")
            val ivLen = rawData[offset].toInt() and 0xFF
            offset++
            if (offset + ivLen > rawData.size) throw IllegalArgumentException("Byte IV rusak")
            var iv: ByteArray? = null
            if (ivLen > 0) {
                iv = rawData.copyOfRange(offset, offset + ivLen)
                offset += ivLen
            }

            if (offset + 8 > rawData.size) throw IllegalArgumentException("Header ukuran file rusak")
            offset += 8 // Skip original size uint64
            val cipherData = rawData.copyOfRange(offset, rawData.size)

            val plain = CryptoEngine.decryptBytesWithSalt(algoId, cipherData, keyStr, iv, salt)
            return DecryptedFileData(plain, algoId, originalExt)

        } else if (rawData.size >= magicLen && startsWith(rawData, FileMagicV2)) {
            var offset = magicLen
            if (offset >= rawData.size) throw IllegalArgumentException("Header file rusak")
            val algoLen = rawData[offset].toInt() and 0xFF
            offset++
            if (offset + algoLen > rawData.size) throw IllegalArgumentException("Header algoritma rusak")
            val algoId = String(rawData, offset, algoLen, Charsets.UTF_8)
            offset += algoLen

            if (offset >= rawData.size) throw IllegalArgumentException("Header ekstensi rusak")
            val extLen = rawData[offset].toInt() and 0xFF
            offset++
            if (offset + extLen > rawData.size) throw IllegalArgumentException("Data ekstensi rusak")
            var originalExt = ""
            if (extLen > 0) {
                originalExt = String(rawData, offset, extLen, Charsets.UTF_8)
                offset += extLen
            }

            if (offset >= rawData.size) throw IllegalArgumentException("Header IV rusak")
            val ivLen = rawData[offset].toInt() and 0xFF
            offset++
            if (offset + ivLen > rawData.size) throw IllegalArgumentException("Byte IV rusak")
            var iv: ByteArray? = null
            if (ivLen > 0) {
                iv = rawData.copyOfRange(offset, offset + ivLen)
                offset += ivLen
            }

            if (offset + 8 > rawData.size) throw IllegalArgumentException("Header ukuran file rusak")
            offset += 8
            val cipherData = rawData.copyOfRange(offset, rawData.size)

            val plain = CryptoEngine.decryptBytesWithSalt(algoId, cipherData, keyStr, iv, null)
            return DecryptedFileData(plain, algoId, originalExt)

        } else if (rawData.size >= magicLen && startsWith(rawData, FileMagicV1)) {
            var offset = magicLen
            if (offset >= rawData.size) throw IllegalArgumentException("Header file rusak")
            val algoLen = rawData[offset].toInt() and 0xFF
            offset++
            if (offset + algoLen > rawData.size) throw IllegalArgumentException("Header algoritma rusak")
            val algoId = String(rawData, offset, algoLen, Charsets.UTF_8)
            offset += algoLen

            if (offset >= rawData.size) throw IllegalArgumentException("Header IV rusak")
            val ivLen = rawData[offset].toInt() and 0xFF
            offset++
            if (offset + ivLen > rawData.size) throw IllegalArgumentException("Byte IV rusak")
            var iv: ByteArray? = null
            if (ivLen > 0) {
                iv = rawData.copyOfRange(offset, offset + ivLen)
                offset += ivLen
            }

            if (offset + 8 > rawData.size) throw IllegalArgumentException("Header ukuran file rusak")
            offset += 8
            val cipherData = rawData.copyOfRange(offset, rawData.size)

            val plain = CryptoEngine.decryptBytesWithSalt(algoId, cipherData, keyStr, iv, null)
            return DecryptedFileData(plain, algoId, "")

        } else {
            if (fallbackAlgoId.isEmpty()) {
                throw IllegalArgumentException("Format file tidak dikenali dan algoritma fallback tidak ditentukan")
            }
            val algo = CryptoAlgorithms.getAlgorithm(fallbackAlgoId)
            val iv: ByteArray?
            val cipherData: ByteArray
            if (algo.ivSize > 0 && rawData.size >= algo.ivSize) {
                iv = rawData.copyOfRange(0, algo.ivSize)
                cipherData = rawData.copyOfRange(algo.ivSize, rawData.size)
            } else {
                iv = null
                cipherData = rawData
            }
            val plain = CryptoEngine.decryptBytesWithSalt(fallbackAlgoId, cipherData, keyStr, iv, null)
            return DecryptedFileData(plain, fallbackAlgoId, "")
        }
    }

    private fun startsWith(src: ByteArray, prefix: ByteArray): Boolean {
        if (src.size < prefix.size) return false
        for (i in prefix.indices) {
            if (src[i] != prefix[i]) return false
        }
        return true
    }

    fun processStreamEncrypt(
        input: InputStream,
        output: OutputStream,
        algoId: String,
        keyStr: String,
        customIv: ByteArray?,
        originalExt: String,
        onProgress: ((Float) -> Unit)? = null
    ): FileProcessResult {
        val startTime = System.currentTimeMillis()
        val plainData = input.readBytes()
        onProgress?.invoke(0.5f)

        val encryptedBytes = encryptFileData(
            plainData = plainData,
            algoId = algoId,
            keyStr = keyStr,
            customIv = customIv,
            originalExt = originalExt
        )
        output.write(encryptedBytes)
        output.flush()
        onProgress?.invoke(1.0f)

        val duration = System.currentTimeMillis() - startTime
        return FileProcessResult(
            algoId = algoId,
            originalExt = originalExt,
            inputSize = plainData.size.toLong(),
            outputSize = encryptedBytes.size.toLong(),
            durationMs = duration
        )
    }

    fun processStreamDecrypt(
        input: InputStream,
        output: OutputStream,
        keyStr: String,
        fallbackAlgoId: String = "",
        onProgress: ((Float) -> Unit)? = null
    ): FileProcessResult {
        val startTime = System.currentTimeMillis()
        val rawData = input.readBytes()
        onProgress?.invoke(0.5f)

        val decrypted = decryptFileData(
            rawData = rawData,
            keyStr = keyStr,
            fallbackAlgoId = fallbackAlgoId
        )

        output.write(decrypted.plainData)
        output.flush()
        onProgress?.invoke(1.0f)

        val duration = System.currentTimeMillis() - startTime
        return FileProcessResult(
            algoId = decrypted.algoId,
            originalExt = decrypted.originalExt,
            inputSize = rawData.size.toLong(),
            outputSize = decrypted.plainData.size.toLong(),
            durationMs = duration
        )
    }
}
