package com.kripdroid.app.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.kripdroid.app.crypto.CryptoEngine
import org.bouncycastle.util.encoders.Hex
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeystoreManager {

    const val DEVICE_KEY_SENTINEL = "[KUNCI-PERANGKAT-ANDROID-KEYSTORE]"
    const val MASTER_KEY_SENTINEL = "[KUNCI-MASTER-PASSWORD]"

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "KripDroidHardwareMasterKey"
    private const val MASTER_PASS_FILENAME = ".krip_masterpass.json"
    private const val HW_KEYSTORE_ENC_FILENAME = ".krip_hw_keystore.enc"

    private val secureRandom = SecureRandom()

    private fun getMasterPassFile(context: Context): File {
        return File(context.filesDir, MASTER_PASS_FILENAME)
    }

    private fun cleanLegacyPlaintextSeed(context: Context) {
        try {
            val legacy = File(context.filesDir, ".krip_keystore_seed")
            if (legacy.exists()) {
                legacy.delete()
            }
        } catch (_: Exception) {}
    }

    fun hasMasterPassword(context: Context): Boolean {
        val file = getMasterPassFile(context)
        if (!file.exists() || file.length() == 0L) return false
        return try {
            val json = JSONObject(file.readText())
            json.optString("hash").isNotEmpty() && json.optString("salt").isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

    private fun hashMasterPassword(password: String, salt: ByteArray): ByteArray {
        return CryptoEngine.deriveKeyArgon2id(password, salt, 32)
    }

    fun setMasterPassword(context: Context, password: String) {
        val trimmed = password.trim()
        require(trimmed.length >= 4) { "Kata sandi master minimal 4 karakter" }

        val salt = ByteArray(16).also { secureRandom.nextBytes(it) }
        val seedSalt = ByteArray(16).also { secureRandom.nextBytes(it) }
        val hash = hashMasterPassword(trimmed, salt)

        val json = JSONObject().apply {
            put("salt", Hex.toHexString(salt))
            put("hash", Hex.toHexString(hash))
            put("seed_salt", Hex.toHexString(seedSalt))
        }

        val file = getMasterPassFile(context)
        file.writeText(json.toString())
        CryptoEngine.zeroize(hash)
    }

    fun verifyMasterPassword(context: Context, password: String): Boolean {
        val file = getMasterPassFile(context)
        if (!file.exists()) return false
        return try {
            val json = JSONObject(file.readText())
            val saltHex = json.getString("salt")
            val expectedHashHex = json.getString("hash")

            val salt = Hex.decode(saltHex)
            val expectedHash = Hex.decode(expectedHashHex)
            val actualHash = hashMasterPassword(password.trim(), salt)

            val matches = MessageDigest.isEqual(actualHash, expectedHash)
            CryptoEngine.zeroize(actualHash)
            matches
        } catch (_: Exception) {
            false
        }
    }

    fun changeMasterPassword(context: Context, oldPass: String, newPass: String) {
        if (!verifyMasterPassword(context, oldPass)) {
            throw IllegalArgumentException("Master password lama salah")
        }
        val trimmedNew = newPass.trim()
        require(trimmedNew.length >= 4) { "Master password baru minimal 4 karakter" }
        setMasterPassword(context, trimmedNew)
    }

    fun getDeviceMasterKey(context: Context, masterPass: String): String {
        if (!verifyMasterPassword(context, masterPass)) {
            throw IllegalArgumentException("Master password salah atau belum diatur")
        }
        val file = getMasterPassFile(context)
        val json = JSONObject(file.readText())
        val seedSalt = Hex.decode(json.getString("seed_salt"))
        val derived = CryptoEngine.deriveKeyArgon2id(masterPass, seedSalt, 32)
        val hex = Hex.toHexString(derived)
        CryptoEngine.zeroize(derived)
        return hex
    }

    fun isDeviceKey(keyStr: String): Boolean {
        val trimmed = keyStr.trim()
        return trimmed == DEVICE_KEY_SENTINEL ||
                trimmed == MASTER_KEY_SENTINEL ||
                trimmed.startsWith("[KUNCI-PERANGKAT") ||
                trimmed.startsWith("[KUNCI-MASTER")
    }

    fun getHardwareProtectedKey(context: Context): String {
        cleanLegacyPlaintextSeed(context)
        val encFile = File(context.filesDir, HW_KEYSTORE_ENC_FILENAME)

        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()

                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }

            val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey

            if (encFile.exists() && encFile.length() > 12) {
                val allBytes = encFile.readBytes()
                val iv = allBytes.copyOfRange(0, 12)
                val cipherBytes = allBytes.copyOfRange(12, allBytes.size)

                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val spec = GCMParameterSpec(128, iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
                val decryptedSeed = cipher.doFinal(cipherBytes)
                val hex = Hex.toHexString(decryptedSeed)
                CryptoEngine.zeroize(decryptedSeed)
                return hex
            }

            // Generate a fresh 32-byte hardware seed and encrypt it into hardware TEE storage
            val freshSeed = ByteArray(32).also { secureRandom.nextBytes(it) }
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedBlob = cipher.doFinal(freshSeed)

            val out = ByteArray(iv.size + encryptedBlob.size)
            System.arraycopy(iv, 0, out, 0, iv.size)
            System.arraycopy(encryptedBlob, 0, out, iv.size, encryptedBlob.size)
            encFile.writeBytes(out)

            val hex = Hex.toHexString(freshSeed)
            CryptoEngine.zeroize(freshSeed)
            hex
        } catch (_: Exception) {
            // Fallback derivation for tests/emulators where AndroidKeyStore hardware is absent
            val fallbackSeed = ByteArray(32).also { secureRandom.nextBytes(it) }
            val hex = Hex.toHexString(fallbackSeed)
            CryptoEngine.zeroize(fallbackSeed)
            hex
        }
    }

    fun getDeviceHardwareKey(context: Context): String {
        return getHardwareProtectedKey(context)
    }

    fun resolveEffectiveKey(context: Context, keyStr: String): String {
        if (isDeviceKey(keyStr)) {
            return getHardwareProtectedKey(context)
        }
        return keyStr
    }
}
