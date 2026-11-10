package com.kripdroid.app.crypto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CryptoEngineTest {

    @Test
    fun testAllAlgorithmsText() {
        val testKey = "SuperSecretKey123!@#"
        val samplePlain = "Hello KripDroid Security 123"

        for (algo in CryptoAlgorithms.list) {
            if (algo.isOneWay) {
                val (digest, _) = CryptoEngine.encryptText(algo.id, samplePlain, testKey, "")
                assertTrue("Digest output is empty for ${algo.id}", digest.isNotEmpty())
            } else {
                val (enc, _) = CryptoEngine.encryptText(algo.id, samplePlain, testKey, "")
                assertTrue("Encrypted output is empty for ${algo.id}", enc.isNotEmpty())

                val dec = CryptoEngine.decryptText(algo.id, enc, testKey, "")
                if (algo.id == "morse" || algo.id == "bacon") {
                    assertEquals("Decrypted mismatch for ${algo.id}", samplePlain.uppercase(), dec.uppercase())
                } else {
                    assertEquals("Decrypted mismatch for ${algo.id}", samplePlain, dec)
                }
            }
        }
    }

    @Test
    fun testMorseCode() {
        val plain = "SOS KRIPDROID"
        val encoded = EncodingAndHash.morseEncode(plain)
        assertEquals("... --- ... / -.- .-. .. .--. -.. .-. --- .. -..", encoded)
        val decoded = EncodingAndHash.morseDecode(encoded)
        assertEquals(plain, decoded)
    }

    @Test
    fun testBinaryEncoding() {
        val plain = "Hi"
        val encoded = EncodingAndHash.binaryEncode(plain)
        assertEquals("01001000 01101001", encoded)
        val decoded = EncodingAndHash.binaryDecode(encoded)
        assertEquals(plain, decoded)
    }

    @Test
    fun testBase64Encoding() {
        val plain = "Hello KripDroid World!"
        val encoded = EncodingAndHash.base64Encode(plain)
        val decoded = EncodingAndHash.base64Decode(encoded)
        assertEquals(plain, decoded)
    }

    @Test
    fun testHexEncoding() {
        val plain = "Hello"
        val encoded = EncodingAndHash.hexEncode(plain)
        assertEquals("48656c6c6f", encoded)
        val decoded = EncodingAndHash.hexDecode(encoded)
        assertEquals(plain, decoded)
    }

    @Test
    fun testBase32Encoding() {
        val plain = "Hello Base32"
        val encoded = EncodingAndHash.base32Encode(plain)
        val decoded = EncodingAndHash.base32Decode(encoded)
        assertEquals(plain, decoded)
    }

    @Test
    fun testSha256AndMd5() {
        val plain = "hello"
        val sha256 = EncodingAndHash.hashText("SHA-256", plain)
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", sha256)

        val md5 = EncodingAndHash.hashText("MD5", plain)
        assertEquals("5d41402abc4b2a76b9719d911017c592", md5)
    }

    @Test
    fun testRailFenceAndAffine() {
        // Rail Fence
        val rfPlain = "WEAREDISCOVEREDFLEEATONCE"
        val rfEnc = EncodingAndHash.railFenceEncrypt(rfPlain, "3")
        assertEquals("WECRLTEERDSOEEFEAOCAIVDEN", rfEnc)
        val rfDec = EncodingAndHash.railFenceDecrypt(rfEnc, "3")
        assertEquals(rfPlain, rfDec)

        // Affine
        val afPlain = "AFFINECIPHER"
        val afEnc = EncodingAndHash.affineEncrypt(afPlain, "5,8")
        val afDec = EncodingAndHash.affineDecrypt(afEnc, "5,8")
        assertEquals(afPlain, afDec)
    }

    @Test
    fun testBaconCipher() {
        val plain = "SECURITY"
        val enc = EncodingAndHash.baconEncrypt(plain)
        val dec = EncodingAndHash.baconDecrypt(enc)
        assertEquals(plain, dec)
    }

    @Test
    fun testClassicSpecifics() {
        // Caesar
        val cEnc = ClassicCiphers.caesarEncrypt("Hello World", "3")
        assertEquals("Khoor Zruog", cEnc)
        val cDec = ClassicCiphers.caesarDecrypt(cEnc, "3")
        assertEquals("Hello World", cDec)

        // Rot13
        val rEnc = ClassicCiphers.rot13("Hello World")
        assertEquals("Uryyb Jbeyq", rEnc)
        val rDec = ClassicCiphers.rot13(rEnc)
        assertEquals("Hello World", rDec)

        // Atbash
        val aEnc = ClassicCiphers.atbash("Hello World")
        assertEquals("Svool Dliow", aEnc)
        val aDec = ClassicCiphers.atbash(aEnc)
        assertEquals("Hello World", aDec)

        // Vigenere
        val vEnc = ClassicCiphers.vigenereEncrypt("ATTACKATDAWN", "LEMON")
        assertEquals("LXFOPVEFRNHR", vEnc)
        val vDec = ClassicCiphers.vigenereDecrypt(vEnc, "LEMON")
        assertEquals("ATTACKATDAWN", vDec)

        // XOR
        val xKey = "SECRET"
        val plainBytes = "Hello XOR".toByteArray(Charsets.UTF_8)
        val xEnc = ClassicCiphers.xorCipher(plainBytes, xKey.toByteArray(Charsets.UTF_8))
        val xDec = ClassicCiphers.xorCipher(xEnc, xKey.toByteArray(Charsets.UTF_8))
        assertEquals("Hello XOR", String(xDec, Charsets.UTF_8))
    }

    @Test
    fun testFileEncryptionDecryption() {
        val originalData = "Video MP4 stream binary simulated payload test data.".toByteArray(Charsets.UTF_8)
        val originalExt = ".mp4"
        val testAlgos = listOf("aes256gcm", "chacha20poly1305", "aes128cbc", "blowfish", "tripledes", "des", "xor", "base64", "hex", "binary", "morse")

        for (algoId in testAlgos) {
            val encBytes = FileCrypto.encryptFileData(
                plainData = originalData,
                algoId = algoId,
                keyStr = "MasterPassphrase!123",
                customIv = null,
                originalExt = originalExt
            )
            assertTrue("Encrypted file data is empty for $algoId", encBytes.isNotEmpty())

            val decResult = FileCrypto.decryptFileData(
                rawData = encBytes,
                keyStr = "MasterPassphrase!123"
            )

            assertEquals(algoId, decResult.algoId)
            assertEquals(originalExt, decResult.originalExt)
            if (algoId == "morse") {
                assertEquals(String(originalData, Charsets.UTF_8).uppercase(), String(decResult.plainData, Charsets.UTF_8).uppercase())
            } else {
                assertTrue("Decrypted data mismatch for $algoId", originalData.contentEquals(decResult.plainData))
            }
        }
    }

    @Test
    fun testFileDataExtensionPreservation() {
        val plain = "secret document content".toByteArray(Charsets.UTF_8)
        val encBytes = FileCrypto.encryptFileData(
            plainData = plain,
            algoId = "aes256gcm",
            keyStr = "key123",
            customIv = null,
            originalExt = ".pdf"
        )

        val decResult = FileCrypto.decryptFileData(encBytes, "key123")
        assertEquals("aes256gcm", decResult.algoId)
        assertEquals(".pdf", decResult.originalExt)
        assertTrue(plain.contentEquals(decResult.plainData))
    }

    @Test
    fun testZeroizeMemoryScrubbing() {
        val sensitive = byteArrayOf(1, 2, 3, 4, 5, 99, 127)
        CryptoEngine.zeroize(sensitive)
        for (b in sensitive) {
            assertEquals(0.toByte(), b)
        }

        val sensitiveChars = charArrayOf('p', 'a', 's', 's', 'w', 'o', 'r', 'd')
        CryptoEngine.zeroize(sensitiveChars)
        for (c in sensitiveChars) {
            assertEquals('\u0000', c)
        }
    }

    @Test
    fun testArgon2idDerivation() {
        val salt = "FixedSalt16Bytes".toByteArray(Charsets.UTF_8)
        val key1 = CryptoEngine.deriveKeyArgon2id("MyMasterPass", salt, 32)
        val key2 = CryptoEngine.deriveKeyArgon2id("MyMasterPass", salt, 32)
        val key3 = CryptoEngine.deriveKeyArgon2id("DifferentPass", salt, 32)

        assertEquals(32, key1.size)
        assertTrue("Argon2id should be deterministic for same pass and salt", key1.contentEquals(key2))
        assertTrue("Argon2id should differ for different pass", !key1.contentEquals(key3))
    }
}
