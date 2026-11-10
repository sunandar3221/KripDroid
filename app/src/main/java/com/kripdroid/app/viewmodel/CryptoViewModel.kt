package com.kripdroid.app.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.kripdroid.app.crypto.FileProcessResult
import java.io.File

class CryptoViewModel : ViewModel() {

    // ==========================================
    // TEXT CRYPTO STATE
    // ==========================================
    var textSelectedAlgoId by mutableStateOf("aes256gcm")
    var textInput by mutableStateOf("")
    var textKey by mutableStateOf("")
    var textIv by mutableStateOf("")
    var textOutput by mutableStateOf("")

    var textIsKeyMasked by mutableStateOf(false)
    var textShowAdvancedIv by mutableStateOf(false)
    var textUseDeviceKey by mutableStateOf(false)

    var textIsProcessing by mutableStateOf(false)
    var textStatusMessage by mutableStateOf("")
    var textIsError by mutableStateOf(false)

    // ==========================================
    // FILE CRYPTO STATE
    // ==========================================
    var fileSelectedAlgoId by mutableStateOf("aes256gcm")

    var loadedUri by mutableStateOf<Uri?>(null)
    var loadedFileName by mutableStateOf("")
    var loadedFileSize by mutableStateOf(0L)

    var processedOutputFile by mutableStateOf<File?>(null)
    var processedResult by mutableStateOf<FileProcessResult?>(null)
    var suggestedExportFileName by mutableStateOf("")

    var fileKey by mutableStateOf("")
    var fileIsKeyMasked by mutableStateOf(false)
    var fileUseDeviceKey by mutableStateOf(false)

    var fileIsProcessing by mutableStateOf(false)
    var fileProgress by mutableFloatStateOf(0f)
    var fileProgressStatusText by mutableStateOf("")
    var fileStatusMessage by mutableStateOf("")
    var fileIsError by mutableStateOf(false)
}
