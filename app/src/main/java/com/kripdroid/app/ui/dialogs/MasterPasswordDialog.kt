package com.kripdroid.app.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kripdroid.app.security.BiometricHelper
import com.kripdroid.app.security.KeystoreManager
import com.kripdroid.app.ui.theme.SurfaceContainerHigh

@Composable
fun MasterPasswordDialog(
    onDismiss: () -> Unit,
    onSuccess: (masterKey: String) -> Unit,
    onBiometricRequested: () -> Unit
) {
    val context = LocalContext.current
    val hasPass = remember { KeystoreManager.hasMasterPassword(context) }
    val biometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerHigh,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = if (hasPass) "Otentikasi Master Key" else "Buat Master Key Baru",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                Text(
                    text = if (hasPass)
                        "Masukkan Master Password Anda untuk membuka sesi enkripsi / dekripsi (Proteksi Zero-Knowledge RAM)."
                    else
                        "Buat kata sandi master baru (minimal 4 karakter) untuk mengamankan data Anda dengan proteksi anti-root & UBL.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = ""
                    },
                    label = { Text(if (hasPass) "Kata Sandi Master" else "Kata Sandi Master Baru") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    }
                )

                if (!hasPass) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = ""
                        },
                        label = { Text("Konfirmasi Kata Sandi Baru") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }

                if (hasPass && biometricAvailable) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onBiometricRequested,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Verifikasi dengan Biometrik", fontSize = 12.sp)
                    }
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (hasPass) {
                        if (KeystoreManager.verifyMasterPassword(context, password)) {
                            try {
                                val key = KeystoreManager.getDeviceMasterKey(context, password)
                                onSuccess(key)
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Gagal menurunkan kunci master"
                            }
                        } else {
                            errorMessage = "Kata sandi master salah"
                        }
                    } else {
                        if (password.length < 4) {
                            errorMessage = "Kata sandi minimal 4 karakter"
                        } else if (password != confirmPassword) {
                            errorMessage = "Konfirmasi kata sandi tidak cocok"
                        } else {
                            try {
                                KeystoreManager.setMasterPassword(context, password)
                                val key = KeystoreManager.getDeviceMasterKey(context, password)
                                onSuccess(key)
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Gagal menyimpan kata sandi master"
                            }
                        }
                    }
                }
            ) {
                Text(if (hasPass) "Buka Kunci" else "Simpan & Aktifkan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
