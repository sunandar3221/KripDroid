package com.kripdroid.app.crypto

enum class SecurityCategory(val displayName: String) {
    Classic("Klasik"),
    Encoding("Encoding"),
    Hash("Hash"),
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
    val description: String,
    val isOneWay: Boolean = false
)

object CryptoAlgorithms {
    val list = listOf(
        // --- MODERN CIPHERS ---
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

        // --- ENCODING & SANDI ---
        AlgorithmInfo(
            id = "morse",
            name = "Kode Morse (Morse Code)",
            category = SecurityCategory.Encoding,
            securityLevel = "Sandi Telekomunikasi / Edukasi",
            securityRating = 1,
            keyDescription = "Tanpa Kunci (Representasi Titik dan Garis)",
            needsKey = false,
            needsIV = false,
            keySize = 0,
            ivSize = 0,
            description = "Metode transmisi informasi tekstual menggunakan rangkaian ketukan nada pendek dan panjang (titik '.' dan garis '-')."
        ),
        AlgorithmInfo(
            id = "binary",
            name = "Binary (Biner 8-bit)",
            category = SecurityCategory.Encoding,
            securityLevel = "Encoding Biner / Komputasi",
            securityRating = 1,
            keyDescription = "Tanpa Kunci (Format Angka 0 dan 1)",
            needsKey = false,
            needsIV = false,
            keySize = 0,
            ivSize = 0,
            description = "Representasi biner 8-bit (byte) dari karakter teks atau byte data mentah."
        ),
        AlgorithmInfo(
            id = "base64",
            name = "Base64 (RFC 4648)",
            category = SecurityCategory.Encoding,
            securityLevel = "Encoding ASCII / Standar Web",
            securityRating = 1,
            keyDescription = "Tanpa Kunci (Karakter Radix-64 A-Z, a-z, 0-9, +, /)",
            needsKey = false,
            needsIV = false,
            keySize = 0,
            ivSize = 0,
            description = "Skema pengkodean biner ke teks menggunakan 64 karakter representasi untuk transmisi data media."
        ),
        AlgorithmInfo(
            id = "base32",
            name = "Base32 (RFC 4648)",
            category = SecurityCategory.Encoding,
            securityLevel = "Encoding Human-Readable / OTP",
            securityRating = 1,
            keyDescription = "Tanpa Kunci (Karakter A-Z, 2-7)",
            needsKey = false,
            needsIV = false,
            keySize = 0,
            ivSize = 0,
            description = "Notasi pengkodean 32-karakter case-insensitive yang ramah mata manusia, lazim digunakan pada secret OTP."
        ),
        AlgorithmInfo(
            id = "hex",
            name = "Hexadecimal (Base16 / Hex)",
            category = SecurityCategory.Encoding,
            securityLevel = "Encoding Heksadesimal",
            securityRating = 1,
            keyDescription = "Tanpa Kunci (Format Digit 0-9, A-F)",
            needsKey = false,
            needsIV = false,
            keySize = 0,
            ivSize = 0,
            description = "Representasi basis 16 dari setiap byte data menjadi dua digit heksadesimal."
        ),
        AlgorithmInfo(
            id = "bacon",
            name = "Sandi Bacon (Baconian)",
            category = SecurityCategory.Encoding,
            securityLevel = "Steganografi / Sandi Klasik",
            securityRating = 1,
            keyDescription = "Tanpa Kunci (Pola 5-huruf A dan B)",
            needsKey = false,
            needsIV = false,
            keySize = 0,
            ivSize = 0,
            description = "Metode steganografi ciptaan Francis Bacon yang menyandikan tiap huruf ke dalam 5 bit biner 'A' dan 'B'."
        ),

        // --- HASH KRIPTOGRAFIS (ONE-WAY) ---
        AlgorithmInfo(
            id = "sha256",
            name = "SHA-256 (Secure Hash)",
            category = SecurityCategory.Hash,
            securityLevel = "Integritas Kriptografis Tinggi",
            securityRating = 5,
            keyDescription = "Fungsi Satu Arah (One-Way Digest)",
            needsKey = false,
            needsIV = false,
            keySize = 0,
            ivSize = 0,
            description = "Algoritma hash kriptografi satu arah 256-bit standar NIST (FIPS 180-4) yang digunakan pada Bitcoin dan TLS.",
            isOneWay = true
        ),
        AlgorithmInfo(
            id = "sha512",
            name = "SHA-512 (Secure Hash)",
            category = SecurityCategory.Hash,
            securityLevel = "Integritas Kriptografis Maksimal",
            securityRating = 5,
            keyDescription = "Fungsi Satu Arah (One-Way Digest)",
            needsKey = false,
            needsIV = false,
            keySize = 0,
            ivSize = 0,
            description = "Fungsi hash 512-bit dengan ruang pencarian astronomis, tahan serangan tabrakan (collision-resistant).",
            isOneWay = true
        ),
        AlgorithmInfo(
            id = "sha1",
            name = "SHA-1 (Legacy Hash)",
            category = SecurityCategory.Hash,
            securityLevel = "Integritas Legacy / Git Hash",
            securityRating = 2,
            keyDescription = "Fungsi Satu Arah (One-Way Digest)",
            needsKey = false,
            needsIV = false,
            keySize = 0,
            ivSize = 0,
            description = "Fungsi hash 160-bit warisan yang populer digunakan pada checksum berkas dan commit Git.",
            isOneWay = true
        ),
        AlgorithmInfo(
            id = "md5",
            name = "MD5 (Message Digest 5)",
            category = SecurityCategory.Hash,
            securityLevel = "Checksum / Non-Kripto",
            securityRating = 2,
            keyDescription = "Fungsi Satu Arah (One-Way Digest)",
            needsKey = false,
            needsIV = false,
            keySize = 0,
            ivSize = 0,
            description = "Fungsi digest 128-bit yang sangat cepat untuk memverifikasi keutuhan berkas dan deduplikasi data.",
            isOneWay = true
        ),

        // --- CIPHER KLASIK ---
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
            name = "Vigenère Cipher",
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
            id = "railfence",
            name = "Rail Fence (Sandi Zig-Zag)",
            category = SecurityCategory.Classic,
            securityLevel = "Transposisi / Edukasi",
            securityRating = 2,
            keyDescription = "Jumlah Rel / Kedalaman (Angka, cth: 3)",
            needsKey = true,
            needsIV = false,
            keySize = 0,
            ivSize = 0,
            description = "Cipher transposisi yang menuliskan karakter secara zig-zag pada beberapa rel sebelum dibaca berurutan."
        ),
        AlgorithmInfo(
            id = "affine",
            name = "Affine Cipher",
            category = SecurityCategory.Classic,
            securityLevel = "Matematika / Edukasi",
            securityRating = 2,
            keyDescription = "Parameter a,b (cth: 5,8 atau Kata Kunci)",
            needsKey = true,
            needsIV = false,
            keySize = 0,
            ivSize = 0,
            description = "Cipher substitusi matematis monoalfabetik dengan fungsi transformasi E(x) = (a*x + b) mod 26."
        ),
        AlgorithmInfo(
            id = "xor",
            name = "Simple XOR Cipher",
            category = SecurityCategory.Classic,
            securityLevel = "Operasi Bitwise Cepat",
            securityRating = 2,
            keyDescription = "Kunci / Kata Sandi Bebas",
            needsKey = true,
            needsIV = false,
            keySize = 0,
            ivSize = 0,
            description = "Operasi bitwise Exclusive-OR (XOR) biner cepat dengan pengulangan kunci."
        ),

        // --- LEGACY CIPHERS ---
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
        )
    )

    fun getAlgorithm(id: String): AlgorithmInfo {
        return list.find { it.id.equals(id, ignoreCase = true) }
            ?: throw IllegalArgumentException("Algoritma tidak dikenal: $id")
    }
}
