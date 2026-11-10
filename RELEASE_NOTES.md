### 🚀 KripDroid v9.0.0 (Kotlin Jetpack Compose Edition)

Selamat datang di pembaruan besar KripDroid v9.0.0! Aplikasi telah dimigrasi sepenuhnya dari Go ke Pure Kotlin menggunakan Google Jetpack Compose (Material You / Material Design 3).

---

### ✨ Fitur & Pembaruan Utama di v9.0.0:

• **Arsitektur Pure Kotlin & Jetpack Compose**: Antarmuka modern Material 3 bertema Dark Obsidian & Teal, animasi responsif, dan navigasi bottom bar & drawer yang elegan serta adaptif di semua resolusi HP.
• **25 Spektrum Algoritma Kriptografi, Sandi & Encoding Lengkap**:
  1. **Cipher Modern (Standar Militer & AEAD)**:
     - `AES-256-GCM` (Galois/Counter Mode terotentikasi 256-bit)
     - `ChaCha20-Poly1305` (RFC 8439 Stream cipher modern)
     - `AES-256-CBC` (Blok 256-bit standar industri)
     - `AES-128-GCM` (AEAD 128-bit)
     - `AES-128-CBC` (CBC 128-bit)
  2. **Encoding & Sandi Komunikasi**:
     - `Kode Morse` (International Morse Code titik '.' dan garis '-')
     - `Binary` (Format Biner 8-bit ASCII)
     - `Base64` (RFC 4648 ASCII encoding)
     - `Base32` (RFC 4648 format ramah manusia)
     - `Hexadecimal` (Base16 format heksadesimal)
     - `Sandi Bacon` (Baconian 5-bit binary steganografi)
  3. **Fungsi Hash Kriptografis (One-Way Digest & Checksum)**:
     - `SHA-256` (NIST FIPS 180-4 standard 256-bit)
     - `SHA-512` (512-bit collision-resistant digest)
     - `SHA-1` (160-bit legacy checksum)
     - `MD5` (128-bit verification digest)
  4. **Cipher Klasik & Edukasi**:
     - `Caesar Cipher` (Monoalfabetik shift)
     - `ROT13` (Fixed 13-step rotation)
     - `Atbash Cipher` (Pembalikan urutan alfabet)
     - `Vigenère Cipher` (Polialfabetik berulang)
     - `Rail Fence Cipher` (Sandi Pagar / Zig-Zag transposition)
     - `Affine Cipher` (Transformasi matematis monoalfabetik)
     - `Simple XOR Cipher` (Bitwise XOR biner cepat)
  5. **Cipher Legacy**:
     - `DES` (Data Encryption Standard 56-bit)
     - `Triple DES` (3DES / EDE 168-bit)
     - `Blowfish` (Bruce Schneier 128-bit block cipher)

• **Proteksi Zero-Knowledge RAM & Anti-Root / Anti-UBL**:
  - Kunci diturunkan dinamis di volatile RAM via Argon2id (64MB memory-hard).
  - Tidak ada berkas kunci/seed mentah di disk.
  - Pembersihan memori otomatis (*RAM zeroization*) via `CryptoEngine.zeroize()`.
  - Android KeyStore Hardware-backed AES-256 di dalam Secure Element / TEE chip.

• **Autentikasi Master Key & Biometrik**:
  - Pembuatan Master Key yang mulus tanpa menanyakan kata sandi lama.
  - Fallback otomatis dari sensor biometrik ke Master Key jika biometrik tidak tersedia atau gagal.

• **Pemulihan Ekstensi Berkas Otomatis**:
  - Enkripsi dan dekripsi file biner (.mp4, .pdf, .docx, .zip, dll.) secara aman dengan format header protokol `KRIPDROID\x03`.

• **Tab Pengaturan Lengkap**:
  - Pengembang: sunandar3221
  - Repositori: https://github.com/sunandar3221/KripDroid
  - Lisensi: GNU General Public License v3 (GPLv3)

---

### 📦 Aset Rilis (Download):

1. **`KripDroid-v9.0.0-signed.apk`**: APK rilis bertanda tangan resmi (Signed Keystore Scheme v2), siap diinstal langsung di Android.
2. **`KripDroid-v9.0.0-unsigned.apk`**: APK rilis unsigned untuk kebutuhan kustomisasi mandiri.
