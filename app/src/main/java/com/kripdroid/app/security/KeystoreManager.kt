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
import javax.crypto.KeyGenerator

object KeystoreManager {

    const val DEVICE_KEY_SENTINEL = "[KUNCI-PERANGKAT-ANDROID-KEYSTORE]"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "KripDroidMasterKey"
    private const val MASTER_PASS_FILENAME = ".krip_masterpass.json"
    private val secureRandom = SecureRandom()

    private fun getMasterPassFile(context: Context): File {
        return File(context.filesDir, MASTER_PASS_FILENAME)
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

            MessageDigest.isEqual(actualHash, expectedHash)
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
        return Hex.toHexString(derived)
    }

    fun isDeviceKey(keyStr: String): Boolean {
        val trimmed = keyStr.trim()
        return trimmed == DEVICE_KEY_SENTINEL || trimmed.startsWith("[KUNCI-PERANGKAT")
    }

    fun getDeviceHardwareKey(context: Context): String {
        val keyFile = File(context.filesDir, ".krip_keystore_seed")
        if (keyFile.exists() && keyFile.length() > 0) {
            return keyFile.readText()
        }

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

            val md = MessageDigest.getInstance("SHA-256")
            val seedInput = "AndroidKeyStore_${KEY_ALIAS}_${context.packageName}".toByteArray(Charsets.UTF_8)
            val seedHex = Hex.toHexString(md.digest(seedInput))
            keyFile.writeText(seedHex)
            seedHex
        } catch (_: Exception) {
            val fallbackBytes = ByteArray(32).also { secureRandom.nextBytes(it) }
            val fallbackHex = Hex.toHexString(fallbackBytes)
            keyFile.writeText(fallbackHex)
            fallbackHex
        }
    }

    fun resolveEffectiveKey(context: Context, keyStr: String): String {
        if (isDeviceKey(keyStr)) {
            return getDeviceHardwareKey(context)
        }
        return keyStr
    }
}
