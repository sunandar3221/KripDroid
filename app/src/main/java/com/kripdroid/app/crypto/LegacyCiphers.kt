package com.kripdroid.app.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.SecureRandom
import java.security.Security
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object LegacyCiphers {

    init {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(BouncyCastleProvider())
        }
    }

    private val secureRandom = SecureRandom()

    fun desEncrypt(plainText: ByteArray, key: ByteArray, customIv: ByteArray?): Pair<ByteArray, ByteArray> {
        require(key.size == 8) { "Kunci DES harus tepat 8 byte" }
        val iv = if (customIv == null || customIv.isEmpty()) {
            ByteArray(8).also { secureRandom.nextBytes(it) }
        } else {
            require(customIv.size == 8) { "IV DES harus 8 byte" }
            customIv
        }

        val secretKey = SecretKeySpec(key, "DES")
        val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        val cipherText = cipher.doFinal(plainText)
        return Pair(cipherText, iv)
    }

    fun desDecrypt(cipherText: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        require(key.size == 8) { "Kunci DES harus tepat 8 byte" }
        require(iv.size == 8) { "IV DES harus 8 byte" }
        val secretKey = SecretKeySpec(key, "DES")
        val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        return cipher.doFinal(cipherText)
    }

    fun tripleDesEncrypt(plainText: ByteArray, key: ByteArray, customIv: ByteArray?): Pair<ByteArray, ByteArray> {
        require(key.size == 24) { "Kunci Triple DES harus tepat 24 byte" }
        val iv = if (customIv == null || customIv.isEmpty()) {
            ByteArray(8).also { secureRandom.nextBytes(it) }
        } else {
            require(customIv.size == 8) { "IV Triple DES harus 8 byte" }
            customIv
        }

        val secretKey = SecretKeySpec(key, "DESede")
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        val cipherText = cipher.doFinal(plainText)
        return Pair(cipherText, iv)
    }

    fun tripleDesDecrypt(cipherText: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        require(key.size == 24) { "Kunci Triple DES harus tepat 24 byte" }
        require(iv.size == 8) { "IV Triple DES harus 8 byte" }
        val secretKey = SecretKeySpec(key, "DESede")
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        return cipher.doFinal(cipherText)
    }

    fun blowfishEncrypt(plainText: ByteArray, key: ByteArray, customIv: ByteArray?): Pair<ByteArray, ByteArray> {
        require(key.isNotEmpty() && key.size <= 56) { "Kunci Blowfish harus antara 1 dan 56 byte" }
        val iv = if (customIv == null || customIv.isEmpty()) {
            ByteArray(8).also { secureRandom.nextBytes(it) }
        } else {
            require(customIv.size == 8) { "IV Blowfish harus 8 byte" }
            customIv
        }

        val secretKey = SecretKeySpec(key, "Blowfish")
        val cipher = Cipher.getInstance("Blowfish/CBC/PKCS5Padding", BouncyCastleProvider.PROVIDER_NAME)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
        val cipherText = cipher.doFinal(plainText)
        return Pair(cipherText, iv)
    }

    fun blowfishDecrypt(cipherText: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        require(key.isNotEmpty() && key.size <= 56) { "Kunci Blowfish harus antara 1 dan 56 byte" }
        require(iv.size == 8) { "IV Blowfish harus 8 byte" }
        val secretKey = SecretKeySpec(key, "Blowfish")
        val cipher = Cipher.getInstance("Blowfish/CBC/PKCS5Padding", BouncyCastleProvider.PROVIDER_NAME)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
        return cipher.doFinal(cipherText)
    }
}
