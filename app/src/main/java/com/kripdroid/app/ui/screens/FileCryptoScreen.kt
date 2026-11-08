package com.kripdroid.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.ui.draw.clip
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
import java.io.File
import java.io.FileInputStream

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FileCryptoScreen(
    onRequestMasterPasswordDialog: (onSuccess: (String) -> Unit) -> Unit,
    onTriggerBiometrics: (onSuccess: (String) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedAlgoId by remember { mutableStateOf("aes256gcm") }
    val selectedAlgo = remember(selectedAlgoId) { CryptoAlgorithms.getAlgorithm(selectedAlgoId) }

    var loadedUri by remember { mutableStateOf<Uri?>(null) }
    var loadedFileName by remember { mutableStateOf("") }
    var loadedFileSize by remember { mutableStateOf(0L) }

    var processedOutputFile by remember { mutableStateOf<File?>(null) }
    var processedResult by remember { mutableStateOf<FileProcessResult?>(null) }
    var suggestedExportFileName by remember { mutableStateOf("") }

    var keyText by remember { mutableStateOf("") }
    var isKeyMasked by remember { mutableStateOf(false) }
    var useDeviceKey by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var progressStatusText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    fun handleSelectedUri(uri: Uri) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                var name = "file_${System.currentTimeMillis()}"
                var size = 0L

                // 1. Safely query name and size without crashing on custom ROMs
                try {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIdx != -1) {
                                val fetched = cursor.getString(nameIdx)
                                if (!fetched.isNullOrBlank()) name = fetched
                            }
                            val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                            if (sizeIdx != -1) {
                                size = cursor.getLong(sizeIdx)
                            }
                        }
                    }
                } catch (_: Throwable) {}

                // Fallback size detection
                if (size <= 0L) {
                    try {
                        context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                            size = pfd.statSize
                        }
                    } catch (_: Throwable) {}
                }

                withContext(Dispatchers.Main) {
                    loadedUri = uri
                    loadedFileName = name
                    loadedFileSize = size
                    processedOutputFile = null
                    processedResult = null
                    val sizeText = if (size > 0L) " (${formatSize(size)})" else ""
                    statusMessage = "File berhasil dipilih: $name$sizeText"
                    isError = false
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    statusMessage = "Gagal memuat file: ${e.message}"
                    isError = true
                }
            }
        }
    }

    // Universal File Picker with cascading fallback
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data ?: result.data?.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.uri
        if (uri != null) {
            handleSelectedUri(uri)
        }
    }

    fun openUniversalFilePicker() {
        val openDocIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }
        val getContentIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        val plainContentIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
        }
        val chooserIntent = Intent.createChooser(openDocIntent, "Pilih File").apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(getContentIntent, plainContentIntent))
        }

        var launched = false
        val attempts = listOf(
            { filePickerLauncher.launch(chooserIntent) },
            { filePickerLauncher.launch(openDocIntent) },
            { filePickerLauncher.launch(getContentIntent) },
            { filePickerLauncher.launch(plainContentIntent) },
            { filePickerLauncher.launch(Intent.createChooser(plainContentIntent, "Pilih File")) }
        )
        for (attempt in attempts) {
            try {
                attempt()
                launched = true
                break
            } catch (_: Throwable) {}
        }
        if (!launched) {
            statusMessage = "Gagal membuka pemilih berkas: Aplikasi pengelola berkas tidak ditemukan di sistem ini."
            isError = true
        }
    }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { saveUri: Uri? ->
        saveUri?.let { uri ->
            val srcFile = processedOutputFile
            if (srcFile != null && srcFile.exists()) {
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            FileInputStream(srcFile).use { input ->
                                input.copyTo(out)
                            }
                            out.flush()
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "File berhasil disimpan ke memori perangkat!", Toast.LENGTH_SHORT).show()
                            statusMessage = "File hasil telah sukses diekspor."
                            isError = false
                        }
                    } catch (e: Throwable) {
                        withContext(Dispatchers.Main) {
                            statusMessage = "Gagal mengekspor file: ${e.message}"
                            isError = true
                        }
                    }
                }
            }
        }
    }

    fun executeFileEncrypt(effectiveKey: String) {
        val uri = loadedUri
        if (uri == null) {
            statusMessage = "Pilih file terlebih dahulu!"
            isError = true
            return
        }
        if (selectedAlgo.needsKey && effectiveKey.isEmpty()) {
            statusMessage = "Kunci/Kata sandi enkripsi tidak boleh kosong!"
            isError = true
            return
        }

        isProcessing = true
        progress = 0.05f
        progressStatusText = "Mempersiapkan enkripsi..."

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val dotIndex = loadedFileName.lastIndexOf('.')
                val ext = if (dotIndex != -1) loadedFileName.substring(dotIndex) else ""
                val tempOut = File(context.cacheDir, "krip_encrypted_${System.currentTimeMillis()}.krip")

                val result = FileCrypto.processUriEncrypt(
                    context = context,
                    inputUri = uri,
                    outputFile = tempOut,
                    algoId = selectedAlgo.id,
                    keyStr = effectiveKey,
                    customIv = null,
                    originalExt = ext
                ) { prog, status ->
                    progress = prog
                    progressStatusText = status
                }

                withContext(Dispatchers.Main) {
                    isProcessing = false
                    processedOutputFile = tempOut
                    processedResult = result
                    suggestedExportFileName = "$loadedFileName.krip"
                    statusMessage = "Enkripsi Selesai! Ukuran hasil: ${formatSize(tempOut.length())} (${result.durationMs} ms)"
                    isError = false
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    isProcessing = false
                    statusMessage = "Gagal Enkripsi File: ${e.message}"
                    isError = true
                }
            }
        }
    }

    fun executeFileDecrypt(effectiveKey: String) {
        val uri = loadedUri
        if (uri == null) {
            statusMessage = "Pilih file terenkripsi terlebih dahulu!"
            isError = true
            return
        }
        if (selectedAlgo.needsKey && effectiveKey.isEmpty()) {
            statusMessage = "Kunci/Kata sandi dekripsi tidak boleh kosong!"
            isError = true
            return
        }

        isProcessing = true
        progress = 0.05f
        progressStatusText = "Mempersiapkan dekripsi..."

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val tempOut = File(context.cacheDir, "krip_decrypted_${System.currentTimeMillis()}.bin")

                val result = FileCrypto.processUriDecrypt(
                    context = context,
                    inputUri = uri,
                    outputFile = tempOut,
                    keyStr = effectiveKey,
                    fallbackAlgoId = selectedAlgo.id
                ) { prog, status ->
                    progress = prog
                    progressStatusText = status
                }

                val recoveredExt = result.originalExt
                val baseName = loadedFileName.removeSuffix(".krip")
                val exportName = if (recoveredExt.isNotEmpty()) {
                    if (baseName.endsWith(recoveredExt)) baseName else "$baseName$recoveredExt"
                } else {
                    if (loadedFileName.endsWith(".krip")) baseName else "$loadedFileName.dec"
                }

                withContext(Dispatchers.Main) {
                    isProcessing = false
                    processedOutputFile = tempOut
                    processedResult = result
                    suggestedExportFileName = exportName
                    val extNotice = if (recoveredExt.isNotEmpty()) " (Ekstensi: $recoveredExt)" else ""
                    statusMessage = "Dekripsi Selesai!$extNotice Ukuran: ${formatSize(tempOut.length())} (${result.durationMs} ms)"
                    isError = false
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    isProcessing = false
                    statusMessage = "Gagal Dekripsi File: ${e.message}"
                    isError = true
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

        // File Selector Card
        SectionTitle(title = "FILE SUMBER")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (loadedFileName.isNotEmpty()) loadedFileName else "Belum ada file yang dipilih",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (loadedFileSize > 0L) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ukuran: ${formatSize(loadedFileSize)} (${loadedFileSize} bytes)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { openUniversalFilePicker() },
                        enabled = !isProcessing,
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pilih File...", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val sampleContent = "KripDroid Sample Document Test Payload: 1234567890 ABCDEFGHIJKLMNOPQRSTUVWXYZ\nKripDroid Material You Cryptography Suite Android Kotlin."
                                    val sampleFile = File(context.cacheDir, "sample_kripdroid.txt")
                                    sampleFile.writeText(sampleContent)
                                    val sampleUri = Uri.fromFile(sampleFile)
                                    withContext(Dispatchers.Main) {
                                        loadedUri = sampleUri
                                        loadedFileName = sampleFile.name
                                        loadedFileSize = sampleFile.length()
                                        processedOutputFile = null
                                        processedResult = null
                                        if (keyText.isEmpty() && !useDeviceKey) {
                                            keyText = "SampleMasterKey!123"
                                        }
                                        statusMessage = "File sampel berhasil disiapkan!"
                                        isError = false
                                    }
                                } catch (e: Throwable) {
                                    withContext(Dispatchers.Main) {
                                        statusMessage = "Gagal membuat sampel: ${e.message}"
                                        isError = true
                                    }
                                }
                            }
                        },
                        enabled = !isProcessing,
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

        // Key Input & Biometric Selector
        if (selectedAlgo.needsKey) {
            SectionTitle(title = "KUNCI RAHASIA / KATA SANDI")

            if (useDeviceKey) {
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
                                useDeviceKey = false
                                keyText = ""
                            }
                        ) {
                            Text("Manual", fontSize = 11.sp)
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = keyText,
                    onValueChange = {
                        keyText = it
                        statusMessage = ""
                    },
                    label = { Text(selectedAlgo.keyDescription) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (isKeyMasked) PasswordVisualTransformation() else VisualTransformation.None,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (isKeyMasked) KeyboardType.Password else KeyboardType.Text
                    ),
                    trailingIcon = {
                        IconButton(onClick = { isKeyMasked = !isKeyMasked }) {
                            Icon(
                                imageVector = if (isKeyMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
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
                            onRequestMasterPasswordDialog { key ->
                                keyText = key
                                useDeviceKey = false
                                statusMessage = "Kata sandi master diterapkan"
                                isError = false
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Master Password", fontSize = 11.sp, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = {
                            onTriggerBiometrics { _ ->
                                useDeviceKey = true
                                keyText = KeystoreManager.DEVICE_KEY_SENTINEL
                                statusMessage = "Kunci Biometrik (Android KeyStore) diaktifkan"
                                isError = false
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kunci Biometrik", fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }

        // Dedicated Granular Progress Bar Card
        AnimatedVisibility(visible = isProcessing) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (progressStatusText.isNotEmpty()) progressStatusText else "Sedang memproses kriptografi...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }

        // Action Buttons Row: Standard Manual Buttons
        SectionTitle(title = "AKSI KRIPTOGRAFI FILE")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val effectiveKey = if (useDeviceKey) {
                        KeystoreManager.resolveEffectiveKey(context, keyText)
                    } else {
                        keyText
                    }
                    executeFileEncrypt(effectiveKey)
                },
                enabled = !isProcessing,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Enkripsi File", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    val effectiveKey = if (useDeviceKey) {
                        KeystoreManager.resolveEffectiveKey(context, keyText)
                    } else {
                        keyText
                    }
                    executeFileDecrypt(effectiveKey)
                },
                enabled = !isProcessing,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Dekripsi File", fontSize = 12.sp)
            }
        }

        // Dedicated Biometric Action Buttons (User requested: "tombol enkripsi dekripsi pakai kunci biometrik")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val uri = loadedUri
                    if (uri == null) {
                        statusMessage = "Pilih file terlebih dahulu!"
                        isError = true
                        return@Button
                    }
                    onTriggerBiometrics { hwKey ->
                        useDeviceKey = true
                        keyText = KeystoreManager.DEVICE_KEY_SENTINEL
                        executeFileEncrypt(hwKey)
                    }
                },
                enabled = !isProcessing,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
            ) {
                Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Enkripsi (Biometrik)", fontSize = 11.sp, maxLines = 1)
            }

            Button(
                onClick = {
                    val uri = loadedUri
                    if (uri == null) {
                        statusMessage = "Pilih file terenkripsi terlebih dahulu!"
                        isError = true
                        return@Button
                    }
                    onTriggerBiometrics { hwKey ->
                        useDeviceKey = true
                        keyText = KeystoreManager.DEVICE_KEY_SENTINEL
                        executeFileDecrypt(hwKey)
                    }
                },
                enabled = !isProcessing,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            ) {
                Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Dekripsi (Biometrik)", fontSize = 11.sp, maxLines = 1)
            }
        }

        // Status Banner
        StatusBanner(message = statusMessage, isError = isError)

        // Result Card & Save Actions
        processedOutputFile?.let { out ->
            if (out.exists()) {
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
                            text = "Ukuran: ${formatSize(out.length())} | Durasi: ${processedResult?.durationMs ?: 0} ms",
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
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        saveFileLauncher.launch(suggestedExportFileName)
                                    } catch (_: Throwable) {
                                        statusMessage = "Tidak dapat membuka dialog penyimpanan di perangkat ini"
                                        isError = true
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Simpan (SAF)", fontSize = 10.sp, maxLines = 1)
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        try {
                                            val msg = saveToPublicDownloads(context, out, suggestedExportFileName)
                                            withContext(Dispatchers.Main) {
                                                statusMessage = msg
                                                isError = false
                                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            }
                                        } catch (e: Throwable) {
                                            withContext(Dispatchers.Main) {
                                                statusMessage = "Gagal menyimpan ke folder Downloads: ${e.message}"
                                                isError = true
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Ke Downloads", fontSize = 10.sp, maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        try {
                                            if (out.length() > 5 * 1024 * 1024) {
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(context, "File terlalu besar untuk disalin sebagai Base64 (>5MB)", Toast.LENGTH_SHORT).show()
                                                }
                                                return@launch
                                            }
                                            val fileBytes = out.readBytes()
                                            val b64 = Base64.toBase64String(fileBytes)
                                            withContext(Dispatchers.Main) {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("KripDroid Base64", b64)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "Hasil disalin sebagai Base64!", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Throwable) {
                                            withContext(Dispatchers.Main) {
                                                statusMessage = "Gagal menyalin Base64: ${e.message}"
                                                isError = true
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(0.9f)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Base64", fontSize = 10.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

fun saveToPublicDownloads(context: Context, sourceFile: File, suggestedName: String): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, suggestedName)
            put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("Gagal membuat entri penyimpanan MediaStore")
        resolver.openOutputStream(uri)?.use { out ->
            FileInputStream(sourceFile).use { it.copyTo(out) }
        }
        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        "Berhasil disimpan di folder Unduhan (Downloads/$suggestedName)"
    } else {
        @Suppress("DEPRECATION")
        val targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!targetDir.exists()) targetDir.mkdirs()
        val destFile = File(targetDir, suggestedName)
        sourceFile.copyTo(destFile, overwrite = true)
        "Berhasil disimpan di: ${destFile.absolutePath}"
    }
}

fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    if (bytes < 1024) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format("%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
}
