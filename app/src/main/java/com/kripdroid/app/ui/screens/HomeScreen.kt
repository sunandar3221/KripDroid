package com.kripdroid.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.kripdroid.app.R
import com.kripdroid.app.security.BiometricHelper
import com.kripdroid.app.security.KeystoreManager
import com.kripdroid.app.ui.dialogs.ChangeMasterPasswordDialog
import com.kripdroid.app.ui.dialogs.MasterPasswordDialog
import com.kripdroid.app.ui.theme.SurfaceContainerHigh
import com.kripdroid.app.ui.theme.SurfaceContainerLow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var currentTab by remember { mutableIntStateOf(0) }
    var showMasterPassDialog by remember { mutableStateOf(false) }
    var showChangeMasterPassDialog by remember { mutableStateOf(false) }
    var pendingMasterKeyCallback by remember { mutableStateOf<((String) -> Unit)?>(null) }

    fun triggerBiometrics(onSuccess: (String) -> Unit) {
        val activity = with(BiometricHelper) { context.findFragmentActivity() }
        if (activity != null) {
            if (BiometricHelper.isBiometricAvailable(context)) {
                BiometricHelper.authenticate(
                    activity = activity,
                    title = "Otentikasi Kunci Perangkat",
                    subtitle = "Pindai sidik jari, wajah, atau PIN/Pola Anda",
                    onSuccess = {
                        val hwKey = KeystoreManager.getDeviceHardwareKey(context)
                        onSuccess(hwKey)
                        Toast.makeText(context, "Otentikasi biometrik berhasil!", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, "Gagal biometrik: $err", Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(context, "Biometrik belum diaktifkan di sistem. Silakan gunakan Master Password.", Toast.LENGTH_LONG).show()
                pendingMasterKeyCallback = onSuccess
                showMasterPassDialog = true
            }
        } else {
            Toast.makeText(context, "Tidak dapat mengakses aktivitas sistem", Toast.LENGTH_SHORT).show()
        }
    }

    if (showMasterPassDialog) {
        MasterPasswordDialog(
            onDismiss = {
                showMasterPassDialog = false
                pendingMasterKeyCallback = null
            },
            onSuccess = { key ->
                showMasterPassDialog = false
                pendingMasterKeyCallback?.invoke(key)
                pendingMasterKeyCallback = null
            },
            onBiometricRequested = {
                showMasterPassDialog = false
                pendingMasterKeyCallback?.let { cb ->
                    triggerBiometrics(cb)
                }
                pendingMasterKeyCallback = null
            }
        )
    }

    if (showChangeMasterPassDialog) {
        ChangeMasterPasswordDialog(
            onDismiss = { showChangeMasterPassDialog = false },
            onSuccess = {
                showChangeMasterPassDialog = false
                Toast.makeText(context, "Kata sandi master berhasil diubah!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = SurfaceContainerLow,
                modifier = Modifier.width(300.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.appicon),
                            contentDescription = "KripDroid Logo",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "KripDroid",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Suite Kriptografi v9.0",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Info, contentDescription = null) },
                        label = { Text("Panduan Keamanan") },
                        selected = currentTab == 0,
                        onClick = {
                            currentTab = 0
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.TextFields, contentDescription = null) },
                        label = { Text("Mode Teks") },
                        selected = currentTab == 1,
                        onClick = {
                            currentTab = 1
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                        label = { Text("Mode File") },
                        selected = currentTab == 2,
                        onClick = {
                            currentTab = 2
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Pengaturan") },
                        selected = currentTab == 3,
                        onClick = {
                            currentTab = 3
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Key, contentDescription = null) },
                        label = { Text("Ubah Kata Sandi Master") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            showChangeMasterPassDialog = true
                        }
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Fingerprint, contentDescription = null) },
                        label = { Text("Tes Kunci Biometrik") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { drawerState.close() }
                            triggerBiometrics {
                                // Verified
                            }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "KripDroid",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when (currentTab) {
                                    0 -> "• Panduan"
                                    1 -> "• Mode Teks"
                                    2 -> "• Mode File"
                                    3 -> "• Pengaturan"
                                    else -> ""
                                },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = SurfaceContainerHigh
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        alwaysShowLabel = true,
                        icon = { Icon(Icons.Default.Info, contentDescription = "Panduan") },
                        label = { Text("Panduan", fontSize = 11.sp, fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        alwaysShowLabel = true,
                        icon = { Icon(Icons.Default.TextFields, contentDescription = "Teks") },
                        label = { Text("Teks", fontSize = 11.sp, fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        alwaysShowLabel = true,
                        icon = { Icon(Icons.Default.Folder, contentDescription = "File") },
                        label = { Text("File", fontSize = 11.sp, fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal) }
                    )
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { currentTab = 3 },
                        alwaysShowLabel = true,
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
                        label = { Text("Pengaturan", fontSize = 11.sp, fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentTab) {
                    0 -> GuideScreen()
                    1 -> TextCryptoScreen(
                        onRequestMasterPasswordDialog = { callback ->
                            pendingMasterKeyCallback = callback
                            showMasterPassDialog = true
                        },
                        onTriggerBiometrics = { callback ->
                            triggerBiometrics(callback)
                        }
                    )
                    2 -> FileCryptoScreen(
                        onRequestMasterPasswordDialog = { callback ->
                            pendingMasterKeyCallback = callback
                            showMasterPassDialog = true
                        },
                        onTriggerBiometrics = { callback ->
                            triggerBiometrics(callback)
                        }
                    )
                    3 -> SettingsScreen(
                        onChangeMasterPassRequested = {
                            showChangeMasterPassDialog = true
                        },
                        onTestBiometricRequested = {
                            triggerBiometrics {
                                // Verified
                            }
                        }
                    )
                }
            }
        }
    }
}
