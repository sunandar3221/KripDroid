package com.kripdroid.app.crypto

enum class SecurityCategory(val displayName: String) {
    Classic("Klasik"),
    Legacy("Legacy"),
    Modern("Modern")
}

data class AlgorithmInfo(
    val id: String,
    val name: String,
    val category: SecurityCategory,
    val securityLevel: String,
    val securityRating: Int,
    val keyDescription: String,
    val needsKey: Boolean,
    val needsIV: Boolean,
    val keySize: Int,
    val ivSize: Int,
    val description: String
)

object CryptoAlgorithms {
    val list = listOf(
        AlgorithmInfo(
            id = "caesar",
            name = "Caesar Cipher",
            category = SecurityCategory.Classic,
            securityLevel = "Lemah / Edukasi",
            securityRating = 1,
            keyDescription = "Angka Pergeseran (cth: 3) atau Kata Sandi",
            needsKey = true,
            needsIV = false,
            keySize = 0,
            ivSize = 0,
            description = "Cipher substitusi monoalfabetik dengan menggeser huruf sesuai nilai kunci integer."
        ),
        AlgorithmInfo(
            id = "rot13",
            name = "ROT13",
            category = SecurityCategory.Classic,
            securityLevel = "Lemah / Edukasi",
            securityRating = 1,
            keyDescription = "Tanpa Kunci (Pergeseran Tetap 13 Posisi)",
            needsKey = false,
            needsIV = false,
            keySize = 0,
            ivSize = 0,
            description = "Varian khusus Caesar Cipher dengan rotasi alfabet tetap sebesar 13 langkah."
        ),
        AlgorithmInfo(
            id = "atbash",
            name = "Atbash Cipher",
            category = SecurityCategory.Classic,
            securityLevel = "Lemah / Edukasi",
            securityRating = 1,
            keyDescription = "Tanpa Kunci (Pembalikan Urutan Alfabet)",
            needsKey = false,
            needsIV = false,
            keySize = 0,
            ivSize = 0,
            description = "Cipher substitusi kuno yang membalik susunan alfabet (A-Z, B-Y, C-X)."
        ),
        AlgorithmInfo(
            id = "vigenere",
            name = "Vigenere Cipher",
            category = SecurityCategory.Classic,
            securityLevel = "Lemah / Edukasi",
            securityRating = 2,
            keyDescription = "Kata Kunci / Frasa (cth: RAHASIA)",
            needsKey = true,
            needsIV = false,
            keySize = 0,
            ivSize = 0,
            description = "Cipher substitusi polialfabetik menggunakan tabel pergeseran kata kunci berulang."
        ),
        AlgorithmInfo(
            id = "xor",
            name = "Simple XOR Cipher",
            category = SecurityCategory.Classic,
            securityLevel = "Lemah / Edukasi",
            securityRating = 2,
            keyDescription = "Kunci / Kata Sandi Bebas",
            needsKey = true,
            needsIV = false,
            keySize = 0,
            ivSize = 0,
            description = "Operasi bitwise Exclusive-OR (XOR) biner cepat dengan pengulangan kunci."
        ),
        AlgorithmInfo(
            id = "des",
            name = "DES (Data Encryption Standard)",
            category = SecurityCategory.Legacy,
            securityLevel = "Menengah / Legacy",
            securityRating = 3,
            keyDescription = "Kunci 56-bit (8 karakter / Kata Sandi)",
            needsKey = true,
            needsIV = true,
            keySize = 8,
            ivSize = 8,
            description = "Cipher blok 64-bit simetris dalam mode CBC dengan bantalan PKCS7."
        ),
        AlgorithmInfo(
            id = "tripledes",
            name = "Triple DES (3DES / EDE)",
            category = SecurityCategory.Legacy,
            securityLevel = "Menengah / Legacy",
            securityRating = 4,
            keyDescription = "Kunci 168-bit (24 karakter / Kata Sandi)",
            needsKey = true,
            needsIV = true,
            keySize = 24,
            ivSize = 8,
            description = "Penerapan algoritma DES tiga kali berurutan per blok data dalam mode CBC."
        ),
        AlgorithmInfo(
            id = "blowfish",
            name = "Blowfish",
            category = SecurityCategory.Legacy,
            securityLevel = "Menengah / Legacy",
            securityRating = 4,
            keyDescription = "Kunci Variabel 32-448 bit (Kata Sandi)",
            needsKey = true,
            needsIV = true,
            keySize = 16,
            ivSize = 8,
            description = "Cipher blok 64-bit berkecepatan tinggi rancangan Bruce Schneier."
        ),
        AlgorithmInfo(
            id = "aes128cbc",
            name = "AES-128 (Mode CBC)",
            category = SecurityCategory.Modern,
            securityLevel = "Standar Keamanan Tinggi",
            securityRating = 5,
            keyDescription = "Kunci 128-bit (16 karakter / Kata Sandi)",
            needsKey = true,
            needsIV = true,
            keySize = 16,
            ivSize = 16,
            description = "Advanced Encryption Standard dengan kunci 128-bit dalam mode Cipher Block Chaining."
        ),
        AlgorithmInfo(
            id = "aes128gcm",
            name = "AES-128 (GCM AEAD)",
            category = SecurityCategory.Modern,
            securityLevel = "Standar Keamanan Tinggi",
            securityRating = 5,
            keyDescription = "Kunci 128-bit (16 karakter / Kata Sandi)",
            needsKey = true,
            needsIV = true,
            keySize = 16,
            ivSize = 12,
            description = "Mode Galois/Counter dengan otentikasi data (AEAD) dan throughput sangat tinggi."
        ),
        AlgorithmInfo(
            id = "aes256cbc",
            name = "AES-256 (Mode CBC)",
            category = SecurityCategory.Modern,
            securityLevel = "Standar Militer & Rahasia",
            securityRating = 5,
            keyDescription = "Kunci 256-bit (32 karakter / Kata Sandi)",
            needsKey = true,
            needsIV = true,
            keySize = 32,
            ivSize = 16,
            description = "Cipher blok 256-bit standar industri dengan ketahanan maksimal terhadap brute-force."
        ),
        AlgorithmInfo(
            id = "aes256gcm",
            name = "AES-256 (GCM AEAD)",
            category = SecurityCategory.Modern,
            securityLevel = "Standar Militer & Rahasia",
            securityRating = 5,
            keyDescription = "Kunci 256-bit (32 karakter / Kata Sandi)",
            needsKey = true,
            needsIV = true,
            keySize = 32,
            ivSize = 12,
            description = "Enkripsi terotentikasi 256-bit AEAD menjamin kerahasiaan dan integritas data penuh."
        ),
        AlgorithmInfo(
            id = "chacha20poly1305",
            name = "ChaCha20-Poly1305",
            category = SecurityCategory.Modern,
            securityLevel = "Standar Militer & TLS 1.3",
            securityRating = 5,
            keyDescription = "Kunci 256-bit (32 karakter / Kata Sandi)",
            needsKey = true,
            needsIV = true,
            keySize = 32,
            ivSize = 12,
            description = "Stream cipher modern berkecepatan tinggi dengan autentikator Poly1305 (RFC 8439)."
        )
    )

    fun getAlgorithm(id: String): AlgorithmInfo {
        return list.find { it.id.equals(id, ignoreCase = true) }
            ?: throw IllegalArgumentException("Algoritma tidak dikenal: $id")
    }
}
