package com.kripdroid.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kripdroid.app.crypto.CryptoAlgorithms
import com.kripdroid.app.crypto.CryptoEngine
import com.kripdroid.app.security.KeystoreManager
import com.kripdroid.app.ui.components.AlgorithmInfoCard
import com.kripdroid.app.ui.components.SectionTitle
import com.kripdroid.app.ui.components.StatusBanner
import com.kripdroid.app.ui.theme.PrimaryContainer
import com.kripdroid.app.ui.theme.SurfaceContainerHigh

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextCryptoScreen(
    onRequestMasterPasswordDialog: (onSuccess: (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedAlgoId by remember { mutableStateOf("aes256gcm") }
    val selectedAlgo = remember(selectedAlgoId) { CryptoAlgorithms.getAlgorithm(selectedAlgoId) }

    var inputText by remember { mutableStateOf("") }
    var keyText by remember { mutableStateOf("") }
    var ivText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }

    var isKeyMasked by remember { mutableStateOf(false) }
    var showAdvancedIv by remember { mutableStateOf(false) }
    var useDeviceKey by remember { mutableStateOf(false) }

    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Algorithm Selector Tabs / Horizontal scroll
        SectionTitle(title = "PILIH ALGORITMA KRIPTOGRAFI")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CryptoAlgorithms.list.forEach { algo ->
                FilterChip(
                    selected = algo.id == selectedAlgoId,
                    onClick = {
                        selectedAlgoId = algo.id
                        statusMessage = ""
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
            value = inputText,
            onValueChange = {
                inputText = it
                statusMessage = ""
            },
            label = { Text("Ketik atau tempel teks di sini...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            supportingText = {
                Text(
                    text = "${inputText.length} karakter | ${inputText.toByteArray(Charsets.UTF_8).size} byte",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (inputText.isNotEmpty()) {
                    IconButton(onClick = { inputText = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus")
                    }
                }
            }
        )

        // Key Input Field (if needed)
        if (selectedAlgo.needsKey) {
            SectionTitle(title = "KUNCI RAHASIA / KATA SANDI")
            OutlinedTextField(
                value = if (useDeviceKey) KeystoreManager.DEVICE_KEY_SENTINEL else keyText,
                onValueChange = {
                    if (!useDeviceKey) {
                        keyText = it
                        statusMessage = ""
                    }
                },
                readOnly = useDeviceKey,
                label = { Text(selectedAlgo.keyDescription) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (isKeyMasked && !useDeviceKey) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isKeyMasked) KeyboardType.Password else KeyboardType.Text
                ),
                leadingIcon = {
                    IconButton(onClick = {
                        if (useDeviceKey) {
                            useDeviceKey = false
                            keyText = ""
                        } else {
                            onRequestMasterPasswordDialog { key ->
                                keyText = key
                                useDeviceKey = true
                                statusMessage = "Kunci Perangkat (Android Keystore) diaktifkan"
                                isError = false
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if (useDeviceKey) Icons.Default.Fingerprint else Icons.Default.Lock,
                            contentDescription = "Kunci Perangkat",
                            tint = if (useDeviceKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { isKeyMasked = !isKeyMasked }) {
                        Icon(
                            imageVector = if (isKeyMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Mask"
                        )
                    }
                }
            )
        }

        // Advanced IV Toggle
        if (selectedAlgo.needsIV) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showAdvancedIv = !showAdvancedIv }
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
                    text = if (showAdvancedIv) "Sembunyikan Opsi Lanjutan IV/Nonce" else "Tampilkan Opsi Lanjutan IV/Nonce (Otomatis Acak)",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            AnimatedVisibility(visible = showAdvancedIv) {
                OutlinedTextField(
                    value = ivText,
                    onValueChange = {
                        ivText = it
                        statusMessage = ""
                    },
                    label = { Text("Kustom IV / Nonce (${selectedAlgo.ivSize} byte / Hex - Kosongkan untuk Acak)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (inputText.isEmpty()) {
                        statusMessage = "Teks masukan tidak boleh kosong"
                        isError = true
                        return@Button
                    }
                    try {
                        val effectiveKey = if (useDeviceKey) {
                            KeystoreManager.resolveEffectiveKey(context, keyText)
                        } else {
                            keyText
                        }
                        val (enc, usedIv) = CryptoEngine.encryptText(
                            algoId = selectedAlgo.id,
                            plainText = inputText,
                            keyStr = effectiveKey,
                            ivStr = ivText
                        )
                        outputText = enc
                        if (usedIv.isNotEmpty()) {
                            ivText = usedIv
                        }
                        statusMessage = "Enkripsi berhasil diselesaikan (${selectedAlgo.name})"
                        isError = false
                    } catch (e: Exception) {
                        statusMessage = "Gagal Enkripsi: ${e.message}"
                        isError = true
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Enkripsi Teks", fontSize = 13.sp)
            }

            Button(
                onClick = {
                    if (inputText.isEmpty()) {
                        statusMessage = "Teks ciphertext tidak boleh kosong"
                        isError = true
                        return@Button
                    }
                    try {
                        val effectiveKey = if (useDeviceKey) {
                            KeystoreManager.resolveEffectiveKey(context, keyText)
                        } else {
                            keyText
                        }
                        val dec = CryptoEngine.decryptText(
                            algoId = selectedAlgo.id,
                            cipherText = inputText,
                            keyStr = effectiveKey,
                            ivStr = ivText
                        )
                        outputText = dec
                        statusMessage = "Dekripsi berhasil diselesaikan (${selectedAlgo.name})"
                        isError = false
                    } catch (e: Exception) {
                        statusMessage = "Gagal Dekripsi: ${e.message}"
                        isError = true
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Dekripsi Teks", fontSize = 13.sp)
            }
        }

        // Secondary Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val temp = inputText
                    inputText = outputText
                    outputText = temp
                    statusMessage = "Masukan dan Keluaran telah ditukar"
                    isError = false
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.SwapVert, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tukar", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = {
                    inputText = "Halo KripDroid! Ini contoh pesan rahasia dengan keamanan tingkat tinggi."
                    if (selectedAlgo.needsKey && keyText.isEmpty() && !useDeviceKey) {
                        keyText = "KunciRahasia123!"
                    }
                    statusMessage = "Contoh teks telah dimuat"
                    isError = false
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Contoh Teks", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = {
                    inputText = ""
                    outputText = ""
                    ivText = ""
                    statusMessage = ""
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Bersihkan", fontSize = 12.sp)
            }
        }

        // Status Message
        StatusBanner(message = statusMessage, isError = isError)

        // Output Result Field
        SectionTitle(title = "HASIL KELUARAN")
        OutlinedTextField(
            value = outputText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Ciphertext / Teks Hasil") },
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            supportingText = {
                Text(
                    text = "${outputText.length} karakter | ${outputText.toByteArray(Charsets.UTF_8).size} byte",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (outputText.isNotEmpty()) {
                    IconButton(onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("KripDroid Result", outputText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Hasil disalin ke papan klip!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Salin")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
