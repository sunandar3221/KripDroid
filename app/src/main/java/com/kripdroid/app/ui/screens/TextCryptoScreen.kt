package com.kripdroid.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kripdroid.app.crypto.CryptoAlgorithms
import com.kripdroid.app.crypto.CryptoEngine
import com.kripdroid.app.crypto.SecurityCategory
import com.kripdroid.app.security.KeystoreManager
import com.kripdroid.app.ui.components.AlgorithmInfoCard
import com.kripdroid.app.ui.components.SectionTitle
import com.kripdroid.app.ui.components.StatusBanner
import com.kripdroid.app.ui.theme.SurfaceContainerHigh
import com.kripdroid.app.viewmodel.CryptoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextCryptoScreen(
    viewModel: CryptoViewModel,
    onRequestMasterPasswordDialog: (onSuccess: (String) -> Unit) -> Unit,
    onTriggerBiometrics: (onSuccess: (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    val selectedAlgo = remember(viewModel.textSelectedAlgoId) { CryptoAlgorithms.getAlgorithm(viewModel.textSelectedAlgoId) }

    fun executeEncrypt(effectiveKey: String) {
        if (viewModel.textInput.isEmpty()) {
            viewModel.textStatusMessage = "Teks masukan tidak boleh kosong"
            viewModel.textIsError = true
            return
        }
        if (selectedAlgo.needsKey && effectiveKey.isEmpty()) {
            viewModel.textStatusMessage = "Kunci/Kata sandi enkripsi tidak boleh kosong!"
            viewModel.textIsError = true
            return
        }

        viewModel.textIsProcessing = true
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val (enc, usedIv) = CryptoEngine.encryptText(
                    algoId = selectedAlgo.id,
                    plainText = viewModel.textInput,
                    keyStr = effectiveKey,
                    ivStr = viewModel.textIv
                )
                withContext(Dispatchers.Main) {
                    viewModel.textOutput = enc
                    if (usedIv.isNotEmpty()) {
                        viewModel.textIv = usedIv
                    }
                    viewModel.textStatusMessage = "Enkripsi / Proses berhasil (${selectedAlgo.name})"
                    viewModel.textIsError = false
                    viewModel.textIsProcessing = false
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    viewModel.textStatusMessage = "Gagal Enkripsi: ${e.message}"
                    viewModel.textIsError = true
                    viewModel.textIsProcessing = false
                }
            }
        }
    }

    fun executeDecrypt(effectiveKey: String) {
        if (viewModel.textInput.isEmpty()) {
            viewModel.textStatusMessage = "Teks ciphertext tidak boleh kosong"
            viewModel.textIsError = true
            return
        }
        if (selectedAlgo.needsKey && effectiveKey.isEmpty()) {
            viewModel.textStatusMessage = "Kunci/Kata sandi dekripsi tidak boleh kosong!"
            viewModel.textIsError = true
            return
        }

        viewModel.textIsProcessing = true
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val dec = CryptoEngine.decryptText(
                    algoId = selectedAlgo.id,
                    cipherText = viewModel.textInput,
                    keyStr = effectiveKey,
                    ivStr = viewModel.textIv
                )
                withContext(Dispatchers.Main) {
                    viewModel.textOutput = dec
                    viewModel.textStatusMessage = "Dekripsi / Decode berhasil (${selectedAlgo.name})"
                    viewModel.textIsError = false
                    viewModel.textIsProcessing = false
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    viewModel.textStatusMessage = "Gagal Dekripsi: ${e.message}"
                    viewModel.textIsError = true
                    viewModel.textIsProcessing = false
                }
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Algorithm Selector
        SectionTitle(title = "PILIH ALGORITMA KRIPTOGRAFI")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CryptoAlgorithms.list.forEach { algo ->
                FilterChip(
                    selected = algo.id == viewModel.textSelectedAlgoId,
                    onClick = {
                        viewModel.textSelectedAlgoId = algo.id
                        viewModel.textStatusMessage = ""
                    },
                    label = { Text(algo.name, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Algorithm Details Card
        AlgorithmInfoCard(algo = selectedAlgo)

        // Text Input Field
        SectionTitle(title = "MASUKAN TEKS")
        OutlinedTextField(
            value = viewModel.textInput,
            onValueChange = {
                viewModel.textInput = it
                viewModel.textStatusMessage = ""
            },
            label = { Text("Ketik atau tempel teks di sini...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp),
            supportingText = {
                Text(
                    text = "${viewModel.textInput.length} karakter | ${viewModel.textInput.toByteArray(Charsets.UTF_8).size} byte",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (viewModel.textInput.isNotEmpty()) {
                    IconButton(onClick = { viewModel.textInput = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus")
                    }
                }
            }
        )

        // Key Input Field (Only if algorithm needs key)
        if (selectedAlgo.needsKey) {
            SectionTitle(title = "KUNCI RAHASIA / KATA SANDI")

            if (viewModel.textUseDeviceKey) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Kunci Biometrik / Android KeyStore",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "Aman terlindungi hardware chip keystore",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.textUseDeviceKey = false
                                viewModel.textKey = ""
                            }
                        ) {
                            Text("Manual", fontSize = 11.sp)
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = viewModel.textKey,
                    onValueChange = {
                        viewModel.textKey = it
                        viewModel.textStatusMessage = ""
                    },
                    label = { Text(selectedAlgo.keyDescription) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (viewModel.textIsKeyMasked) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (viewModel.textIsKeyMasked) KeyboardType.Password else KeyboardType.Text
                    ),
                    trailingIcon = {
                        IconButton(onClick = { viewModel.textIsKeyMasked = !viewModel.textIsKeyMasked }) {
                            Icon(
                                imageVector = if (viewModel.textIsKeyMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Mask"
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (!KeystoreManager.hasMasterPassword(context)) {
                                onRequestMasterPasswordDialog { _ ->
                                    viewModel.textUseDeviceKey = true
                                    viewModel.textKey = KeystoreManager.MASTER_KEY_SENTINEL
                                    viewModel.textStatusMessage = "Master Key dibuat & diaktifkan"
                                    viewModel.textIsError = false
                                }
                            } else {
                                viewModel.textUseDeviceKey = true
                                viewModel.textKey = KeystoreManager.MASTER_KEY_SENTINEL
                                viewModel.textStatusMessage = "Mode Master Key aktif (Wajib verifikasi saat eksekusi)"
                                viewModel.textIsError = false
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Master Key", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            onTriggerBiometrics { _ ->
                                viewModel.textUseDeviceKey = true
                                viewModel.textKey = KeystoreManager.DEVICE_KEY_SENTINEL
                                viewModel.textStatusMessage = "Kunci Biometrik (Android KeyStore) diaktifkan"
                                viewModel.textIsError = false
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kunci Biometrik", fontSize = 11.sp)
                    }
                }
            }
        }

        // Advanced IV Toggle
        if (selectedAlgo.needsIV) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { viewModel.textShowAdvancedIv = !viewModel.textShowAdvancedIv }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (viewModel.textShowAdvancedIv) "Sembunyikan Opsi Lanjutan IV/Nonce" else "Tampilkan Opsi Lanjutan IV/Nonce (Otomatis Acak)",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            AnimatedVisibility(visible = viewModel.textShowAdvancedIv) {
                OutlinedTextField(
                    value = viewModel.textIv,
                    onValueChange = {
                        viewModel.textIv = it
                        viewModel.textStatusMessage = ""
                    },
                    label = { Text("Kustom IV / Nonce (${selectedAlgo.ivSize} byte / Hex - Kosongkan untuk Acak)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Progress indicator for Text Crypto
        AnimatedVisibility(visible = viewModel.textIsProcessing) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Memproses kriptografi (${selectedAlgo.name})...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Action Buttons Row: Standard Manual Buttons
        SectionTitle(title = "AKSI KRIPTOGRAFI TEKS")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (viewModel.textInput.isEmpty()) {
                        viewModel.textStatusMessage = "Teks masukan tidak boleh kosong"
                        viewModel.textIsError = true
                        return@Button
                    }
                    if (selectedAlgo.needsKey) {
                        if (viewModel.textUseDeviceKey || KeystoreManager.isDeviceKey(viewModel.textKey) || viewModel.textKey.trim().isEmpty()) {
                            onRequestMasterPasswordDialog { effectiveKey ->
                                executeEncrypt(effectiveKey)
                            }
                        } else {
                            executeEncrypt(viewModel.textKey)
                        }
                    } else {
                        executeEncrypt("")
                    }
                },
                enabled = !viewModel.textIsProcessing,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when {
                        selectedAlgo.isOneWay -> "Hitung Hash"
                        selectedAlgo.category == SecurityCategory.Encoding -> "Encode Teks"
                        else -> "Enkripsi Teks"
                    },
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = {
                    if (viewModel.textInput.isEmpty()) {
                        viewModel.textStatusMessage = "Teks masukan tidak boleh kosong"
                        viewModel.textIsError = true
                        return@Button
                    }
                    if (selectedAlgo.isOneWay) {
                        viewModel.textStatusMessage = "Fungsi Hash (${selectedAlgo.name}) adalah satu arah (one-way digest) dan tidak dapat didekripsi."
                        viewModel.textIsError = true
                        return@Button
                    }
                    if (selectedAlgo.needsKey) {
                        if (viewModel.textUseDeviceKey || KeystoreManager.isDeviceKey(viewModel.textKey) || viewModel.textKey.trim().isEmpty()) {
                            onRequestMasterPasswordDialog { effectiveKey ->
                                executeDecrypt(effectiveKey)
                            }
                        } else {
                            executeDecrypt(viewModel.textKey)
                        }
                    } else {
                        executeDecrypt("")
                    }
                },
                enabled = !viewModel.textIsProcessing,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when {
                        selectedAlgo.isOneWay -> "Info Hash"
                        selectedAlgo.category == SecurityCategory.Encoding -> "Decode Teks"
                        else -> "Dekripsi Teks"
                    },
                    fontSize = 12.sp
                )
            }
        }

        // Dedicated Biometric Action Buttons (Only for algorithms that need keys)
        if (selectedAlgo.needsKey) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (viewModel.textInput.isEmpty()) {
                            viewModel.textStatusMessage = "Teks masukan tidak boleh kosong"
                            viewModel.textIsError = true
                            return@Button
                        }
                        onTriggerBiometrics { hwKey ->
                            viewModel.textUseDeviceKey = true
                            viewModel.textKey = KeystoreManager.DEVICE_KEY_SENTINEL
                            executeEncrypt(hwKey)
                        }
                    },
                    enabled = !viewModel.textIsProcessing,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Enkripsi Biometrik", fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        if (viewModel.textInput.isEmpty()) {
                            viewModel.textStatusMessage = "Teks masukan (ciphertext) tidak boleh kosong"
                            viewModel.textIsError = true
                            return@Button
                        }
                        onTriggerBiometrics { hwKey ->
                            viewModel.textUseDeviceKey = true
                            viewModel.textKey = KeystoreManager.DEVICE_KEY_SENTINEL
                            executeDecrypt(hwKey)
                        }
                    },
                    enabled = !viewModel.textIsProcessing,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dekripsi Biometrik", fontSize = 11.sp)
                }
            }
        }

        // Secondary Utility Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val temp = viewModel.textInput
                    viewModel.textInput = viewModel.textOutput
                    viewModel.textOutput = temp
                    viewModel.textStatusMessage = "Teks masukan dan keluaran berhasil ditukar"
                    viewModel.textIsError = false
                },
                enabled = viewModel.textOutput.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.SwapVert, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tukar Teks", fontSize = 11.sp)
            }

            OutlinedButton(
                onClick = {
                    viewModel.textInput = ""
                    viewModel.textOutput = ""
                    viewModel.textStatusMessage = "Semua bidang teks berhasil dibersihkan"
                    viewModel.textIsError = false
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Bersihkan", fontSize = 11.sp)
            }
        }

        // Output Text Field
        SectionTitle(title = "HASIL KRIPTOGRAFI")
        OutlinedTextField(
            value = viewModel.textOutput,
            onValueChange = { viewModel.textOutput = it },
            readOnly = true,
            label = { Text("Keluaran teks akan muncul di sini") },
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            supportingText = {
                Text(
                    text = "${viewModel.textOutput.length} karakter | ${viewModel.textOutput.toByteArray(Charsets.UTF_8).size} byte",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

        // Copy & Share Output Buttons Row
        if (viewModel.textOutput.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(viewModel.textOutput))
                        Toast.makeText(context, "Teks hasil disalin ke papan klip!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salin Hasil", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        val shareIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, viewModel.textOutput)
                            type = "text/plain"
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Bagikan Hasil KripDroid"))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bagikan", fontSize = 12.sp)
                }
            }
        }

        // Status Message Banner
        StatusBanner(message = viewModel.textStatusMessage, isError = viewModel.textIsError)

        Spacer(modifier = Modifier.height(24.dp))
    }
}
