package com.kripdroid.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kripdroid.app.R
import com.kripdroid.app.security.BiometricHelper
import com.kripdroid.app.security.KeystoreManager
import com.kripdroid.app.ui.components.SectionTitle
import com.kripdroid.app.ui.theme.SurfaceContainerHigh
import com.kripdroid.app.ui.theme.SurfaceContainerLow

@Composable
fun SettingsScreen(
    onChangeMasterPassRequested: () -> Unit,
    onTestBiometricRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showLicenseDialog by remember { mutableStateOf(false) }

    val devName = "sunandar3221"
    val repoUrl = "https://github.com/sunandar3221/KripDroid"
    val devProfileUrl = "https://github.com/sunandar3221"

    if (showLicenseDialog) {
        AlertDialog(
            onDismissRequest = { showLicenseDialog = false },
            containerColor = SurfaceContainerHigh,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "GNU General Public License v3.0 (GPLv3)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = """
                            KripDroid - Suite Kriptografi Material You
                            Hak Cipta (C) 2026 sunandar3221 & Kontributor
                            
                            Perangkat lunak ini adalah perangkat lunak bebas: Anda dapat mendistribusikan ulang dan/atau memodifikasinya di bawah ketentuan GNU General Public License sebagaimana dipublikasikan oleh Free Software Foundation, baik versi 3 dari Lisensi, atau (sesuai pilihan Anda) versi yang lebih baru.
                            
                            Program ini didistribusikan dengan harapan dapat bermanfaat, namun TANPA JAMINAN APAPUN; bahkan tanpa jaminan tersirat mengenai KELAYAKAN UNTUK DIPERDAGANGKAN atau KESESUAIAN UNTUK TUJUAN TERTENTU. Lihat Lisensi Publik Umum GNU untuk rincian lebih lanjut.
                            
                            Anda seharusnya telah menerima salinan GNU General Public License bersama program ini. Jika tidak, lihat <https://www.gnu.org/licenses/>.
                        """.trimIndent(),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showLicenseDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // App Branding Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.appicon),
                    contentDescription = "Logo KripDroid",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "KripDroid",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Versi 9.0.0 (Kotlin Jetpack Compose)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Material 3 • AndroidKeyStore • Biometric",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Developer Section
        SectionTitle(title = "PENGEMBANG & KONTRIBUTOR")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SettingsItemRow(
                    icon = Icons.Default.Person,
                    title = "Developer",
                    subtitle = devName,
                    action = {
                        OutlinedButton(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(devProfileUrl))
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Profil", fontSize = 11.sp)
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                SettingsItemRow(
                    icon = Icons.Default.Code,
                    title = "Repositori GitHub",
                    subtitle = repoUrl,
                    action = {
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("KripDroid Repo", repoUrl)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Tautan repositori disalin!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Salin Tautan", modifier = Modifier.size(18.dp))
                        }
                    }
                )
            }
        }

        // License & Security Section
        SectionTitle(title = "LISENSI & KEAMANAN SISTEM")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SettingsItemRow(
                    icon = Icons.Default.Policy,
                    title = "Lisensi Perangkat Lunak",
                    subtitle = "GNU General Public License v3.0 (GPLv3)",
                    action = {
                        OutlinedButton(onClick = { showLicenseDialog = true }) {
                            Text("Lihat", fontSize = 11.sp)
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                SettingsItemRow(
                    icon = Icons.Default.Security,
                    title = "Android KeyStore",
                    subtitle = "Hardware-backed AES-256 Master Key",
                    action = {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                SettingsItemRow(
                    icon = Icons.Default.Fingerprint,
                    title = "Sensor Biometrik Perangkat",
                    subtitle = if (BiometricHelper.isBiometricAvailable(context)) "Didukung & Siap Digunakan" else "Tidak Tersedia",
                    action = {
                        OutlinedButton(onClick = onTestBiometricRequested) {
                            Text("Uji Coba", fontSize = 11.sp)
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                SettingsItemRow(
                    icon = Icons.Default.Key,
                    title = "Master Password",
                    subtitle = if (KeystoreManager.hasMasterPassword(context)) "Sudah Dikonfigurasi" else "Belum Dibuat",
                    action = {
                        Button(onClick = onChangeMasterPassRequested) {
                            Text(if (KeystoreManager.hasMasterPassword(context)) "Ubah" else "Buat", fontSize = 11.sp)
                        }
                    }
                )
            }
        }

        // About Description Card
        SectionTitle(title = "TENTANG KRIPDROID")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "KripDroid adalah suite kriptografi berstandar militer yang mendukung 13 spektrum algoritma kriptografi dari tingkat edukasi klasik hingga cipher modern seperti AES-256-GCM dan ChaCha20-Poly1305 yang diproteksi derivasi memori Argon2id. Dilengkapi fitur Pemulihan Ekstensi Otomatis untuk dekripsi file biner (.mp4, .pdf, .docx, .zip, dll.) secara aman tanpa koneksi internet.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        action()
    }
}
