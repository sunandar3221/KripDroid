package com.kripdroid.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kripdroid.app.crypto.CryptoAlgorithms
import com.kripdroid.app.crypto.FileCrypto
import com.kripdroid.app.crypto.FileProcessResult
import com.kripdroid.app.security.KeystoreManager
import com.kripdroid.app.ui.components.AlgorithmInfoCard
import com.kripdroid.app.ui.components.SectionTitle
import com.kripdroid.app.ui.components.StatusBanner
import com.kripdroid.app.ui.theme.SurfaceContainerHigh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bouncycastle.util.encoders.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

@Composable
fun FileCryptoScreen(
    onRequestMasterPasswordDialog: (onSuccess: (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedAlgoId by remember { mutableStateOf("aes256gcm") }
    val selectedAlgo = remember(selectedAlgoId) { CryptoAlgorithms.getAlgorithm(selectedAlgoId) }

    var loadedUri by remember { mutableStateOf<Uri?>(null) }
    var loadedFileName by remember { mutableStateOf("") }
    var loadedFileSize by remember { mutableStateOf(0L) }
    var loadedBytes by remember { mutableStateOf<ByteArray?>(null) }

    var processedBytes by remember { mutableStateOf<ByteArray?>(null) }
    var processedResult by remember { mutableStateOf<FileProcessResult?>(null) }
    var suggestedExportFileName by remember { mutableStateOf("") }

    var keyText by remember { mutableStateOf("") }
    var isKeyMasked by remember { mutableStateOf(false) }
    var useDeviceKey by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            loadedUri = it
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    var name = "file"
                    var size = 0L
                    context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                            if (nameIdx != -1) name = cursor.getString(nameIdx)
                            if (sizeIdx != -1) size = cursor.getLong(sizeIdx)
                        }
                    }
                    val stream = context.contentResolver.openInputStream(it)
                    val bytes = stream?.readBytes() ?: ByteArray(0)
                    stream?.close()
                    if (size == 0L) size = bytes.size.toLong()

                    withContext(Dispatchers.Main) {
                        loadedFileName = name
                        loadedFileSize = size
                        loadedBytes = bytes
                        processedBytes = null
                        processedResult = null
                        statusMessage = "File berhasil dimuat: $name (${formatSize(size)})"
                        isError = false
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        statusMessage = "Gagal membaca file: ${e.message}"
                        isError = true
                    }
                }
            }
        }
    }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { saveUri: Uri? ->
        saveUri?.let { uri ->
            processedBytes?.let { bytes ->
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(bytes)
                            out.flush()
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "File berhasil disimpan!", Toast.LENGTH_SHORT).show()
                            statusMessage = "File hasil telah disimpan dengan sukses."
                            isError = false
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            statusMessage = "Gagal menyimpan file: ${e.message}"
                            isError = true
                        }
                    }
                }
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // File Selector Card
        SectionTitle(title = "FILE SUMBER")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (loadedFileName.isNotEmpty()) loadedFileName else "Belum ada file yang dipilih",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (loadedFileSize > 0L) {
                            Text(
                                text = "Ukuran: ${formatSize(loadedFileSize)} (${loadedFileSize} bytes)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pilih File...", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val sample = "KripDroid Sample Test Binary Stream Document: 1234567890 ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                            loadedBytes = sample.toByteArray(Charsets.UTF_8)
                            loadedFileName = "contoh_dokumen.txt"
                            loadedFileSize = loadedBytes!!.size.toLong()
                            processedBytes = null
                            processedResult = null
                            if (keyText.isEmpty() && !useDeviceKey) {
                                keyText = "SampleMasterKey!123"
                            }
                            statusMessage = "File sampel berhasil dibuat!"
                            isError = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Buat Sampel", fontSize = 12.sp)
                    }
                }
            }
        }

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

        AlgorithmInfoCard(algo = selectedAlgo)

        // Key Input Field
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

        // Progress bar
        AnimatedVisibility(visible = isProcessing) {
            Column {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sedang memproses kriptografi...",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
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
                    val bytes = loadedBytes
                    if (bytes == null || bytes.isEmpty()) {
                        statusMessage = "Pilih file terlebih dahulu!"
                        isError = true
                        return@Button
                    }
                    val effectiveKey = if (useDeviceKey) {
                        KeystoreManager.resolveEffectiveKey(context, keyText)
                    } else {
                        keyText
                    }

                    isProcessing = true
                    progress = 0.2f
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val dotIndex = loadedFileName.lastIndexOf('.')
                            val ext = if (dotIndex != -1) loadedFileName.substring(dotIndex) else ""
                            val inStream = ByteArrayInputStream(bytes)
                            val outStream = ByteArrayOutputStream()

                            val result = FileCrypto.processStreamEncrypt(
                                input = inStream,
                                output = outStream,
                                algoId = selectedAlgo.id,
                                keyStr = effectiveKey,
                                customIv = null,
                                originalExt = ext
                            ) { prog ->
                                progress = prog
                            }

                            val outBytes = outStream.toByteArray()
                            withContext(Dispatchers.Main) {
                                isProcessing = false
                                processedBytes = outBytes
                                processedResult = result
                                suggestedExportFileName = "$loadedFileName.krip"
                                statusMessage = "Enkripsi File Selesai! Ukuran hasil: ${formatSize(outBytes.size.toLong())}"
                                isError = false
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isProcessing = false
                                statusMessage = "Gagal Enkripsi File: ${e.message}"
                                isError = true
                            }
                        }
                    }
                },
                enabled = !isProcessing,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Enkripsi File", fontSize = 13.sp)
            }

            Button(
                onClick = {
                    val bytes = loadedBytes
                    if (bytes == null || bytes.isEmpty()) {
                        statusMessage = "Pilih file terenkripsi terlebih dahulu!"
                        isError = true
                        return@Button
                    }
                    val effectiveKey = if (useDeviceKey) {
                        KeystoreManager.resolveEffectiveKey(context, keyText)
                    } else {
                        keyText
                    }

                    isProcessing = true
                    progress = 0.2f
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val inStream = ByteArrayInputStream(bytes)
                            val outStream = ByteArrayOutputStream()

                            val result = FileCrypto.processStreamDecrypt(
                                input = inStream,
                                output = outStream,
                                keyStr = effectiveKey,
                                fallbackAlgoId = selectedAlgo.id
                            ) { prog ->
                                progress = prog
                            }

                            val outBytes = outStream.toByteArray()
                            val recoveredExt = result.originalExt
                            val baseName = loadedFileName.removeSuffix(".krip")
                            val exportName = if (recoveredExt.isNotEmpty()) {
                                if (baseName.endsWith(recoveredExt)) baseName else "$baseName$recoveredExt"
                            } else {
                                if (loadedFileName.endsWith(".krip")) baseName else "$loadedFileName.dec"
                            }

                            withContext(Dispatchers.Main) {
                                isProcessing = false
                                processedBytes = outBytes
                                processedResult = result
                                suggestedExportFileName = exportName
                                val extNotice = if (recoveredExt.isNotEmpty()) " (Ekstensi Asli: $recoveredExt)" else ""
                                statusMessage = "Dekripsi File Selesai!$extNotice Ukuran: ${formatSize(outBytes.size.toLong())}"
                                isError = false
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isProcessing = false
                                statusMessage = "Gagal Dekripsi File: ${e.message}"
                                isError = true
                            }
                        }
                    }
                },
                enabled = !isProcessing,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Dekripsi File", fontSize = 13.sp)
            }
        }

        // Status Banner
        StatusBanner(message = statusMessage, isError = isError)

        // Result Card & Save Actions
        processedBytes?.let { outBytes ->
            SectionTitle(title = "HASIL PROSES FILE")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "File Hasil: $suggestedExportFileName",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ukuran Hasil: ${formatSize(outBytes.size.toLong())} | Durasi: ${processedResult?.durationMs ?: 0} ms",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    processedResult?.let { res ->
                        if (res.originalExt.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ekstensi Asli Terpulihkan: ${res.originalExt}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { saveFileLauncher.launch(suggestedExportFileName) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simpan File", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val b64 = Base64.toBase64String(outBytes)
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("KripDroid Base64", b64)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Hasil disalin sebagai Base64!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin Base64", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun formatSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format("%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
}
