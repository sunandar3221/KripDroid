package com.kripdroid.app.crypto

import org.bouncycastle.crypto.modes.ChaCha20Poly1305
import org.bouncycastle.crypto.params.AEADParameters
import org.bouncycastle.crypto.params.KeyParameter
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object ModernCiphers {

    private val secureRandom = SecureRandom()

    fun aesCbcEncrypt(plainText: ByteArray, key: ByteArray, customIv: ByteArray?): Pair<ByteArray, ByteArray> {
        require(key.size == 16 || key.size == 32) { "Kunci AES harus 16 atau 32 byte" }
        val iv = if (customIv == null || customIv.isEmpty()) {
            ByteArray(16).also { secureRandom.nextBytes(it) }
        } else {
            require(customIv.size == 16) { "IV AES harus 16 byte" }
            customIv
        }

        val secretKey = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        val cipherText = cipher.doFinal(plainText)
        return Pair(cipherText, iv)
    }

    fun aesCbcDecrypt(cipherText: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        require(key.size == 16 || key.size == 32) { "Kunci AES harus 16 atau 32 byte" }
        require(iv.size == 16) { "IV AES harus 16 byte" }
        val secretKey = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        return cipher.doFinal(cipherText)
    }

    fun aesGcmEncrypt(plainText: ByteArray, key: ByteArray, customNonce: ByteArray?): Pair<ByteArray, ByteArray> {
        require(key.size == 16 || key.size == 32) { "Kunci AES harus 16 atau 32 byte" }
        val nonce = if (customNonce == null || customNonce.isEmpty()) {
            ByteArray(12).also { secureRandom.nextBytes(it) }
        } else {
            require(customNonce.size == 12) { "Nonce AES GCM harus 12 byte" }
            customNonce
        }

        val secretKey = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, nonce)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)
        val cipherText = cipher.doFinal(plainText)
        return Pair(cipherText, nonce)
    }

    fun aesGcmDecrypt(cipherText: ByteArray, key: ByteArray, nonce: ByteArray): ByteArray {
        require(key.size == 16 || key.size == 32) { "Kunci AES harus 16 atau 32 byte" }
        require(nonce.size == 12) { "Nonce AES GCM harus 12 byte" }
        val secretKey = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, nonce)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(cipherText)
    }

    fun chaCha20Poly1305Encrypt(plainText: ByteArray, key: ByteArray, customNonce: ByteArray?): Pair<ByteArray, ByteArray> {
        require(key.size == 32) { "Kunci ChaCha20Poly1305 harus 32 byte" }
        val nonce = if (customNonce == null || customNonce.isEmpty()) {
            ByteArray(12).also { secureRandom.nextBytes(it) }
        } else {
            require(customNonce.size == 12) { "Nonce ChaCha20Poly1305 harus 12 byte" }
            customNonce
        }

        val cipher = ChaCha20Poly1305()
        val params = AEADParameters(KeyParameter(key), 128, nonce)
        cipher.init(true, params)
        val out = ByteArray(cipher.getOutputSize(plainText.size))
        val len1 = cipher.processBytes(plainText, 0, plainText.size, out, 0)
        val len2 = cipher.doFinal(out, len1)
        val fullCipher = if (len1 + len2 == out.size) out else out.copyOf(len1 + len2)
        return Pair(fullCipher, nonce)
    }

    fun chaCha20Poly1305Decrypt(cipherText: ByteArray, key: ByteArray, nonce: ByteArray): ByteArray {
        require(key.size == 32) { "Kunci ChaCha20Poly1305 harus 32 byte" }
        require(nonce.size == 12) { "Nonce ChaCha20Poly1305 harus 12 byte" }

        val cipher = ChaCha20Poly1305()
        val params = AEADParameters(KeyParameter(key), 128, nonce)
        cipher.init(false, params)
        val out = ByteArray(cipher.getOutputSize(cipherText.size))
        val len1 = cipher.processBytes(cipherText, 0, cipherText.size, out, 0)
        val len2 = cipher.doFinal(out, len1)
        return out.copyOf(len1 + len2)
    }
}
