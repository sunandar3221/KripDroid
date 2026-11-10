package com.kripdroid.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kripdroid.app.crypto.CryptoAlgorithms
import com.kripdroid.app.crypto.SecurityCategory
import com.kripdroid.app.ui.components.AlgorithmInfoCard
import com.kripdroid.app.ui.components.SectionTitle
import com.kripdroid.app.ui.theme.SurfaceContainerHigh

@Composable
fun GuideScreen(
    modifier: Modifier = Modifier
) {
    val modernList = CryptoAlgorithms.list.filter { it.category == SecurityCategory.Modern }
    val encodingList = CryptoAlgorithms.list.filter { it.category == SecurityCategory.Encoding }
    val hashList = CryptoAlgorithms.list.filter { it.category == SecurityCategory.Hash }
    val classicList = CryptoAlgorithms.list.filter { it.category == SecurityCategory.Classic }
    val legacyList = CryptoAlgorithms.list.filter { it.category == SecurityCategory.Legacy }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "PANDUAN SPEKTRUM KRIPTOGRAFI LENGKAP",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "KripDroid mendukung 25 algoritma: Cipher Modern Standar Militer (AES-256, ChaCha20-Poly1305), Encoding & Sandi Populer (Kode Morse, Binary, Base64/32, Hex, Bacon), Fungsi Hash Satu Arah (SHA-256, SHA-512, MD5), serta Sandi Klasik & Legacy.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            SectionTitle(title = "1. STANDAR MILITER & REKOMENDASI UTAMA")
        }

        items(modernList) { algo ->
            AlgorithmInfoCard(algo = algo)
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            SectionTitle(title = "2. ENCODING & SANDI KOMUNIKASI")
        }

        items(encodingList) { algo ->
            AlgorithmInfoCard(algo = algo)
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            SectionTitle(title = "3. HASH KRIPTOGRAFIS (SATU ARAH / CHECKSUM)")
        }

        items(hashList) { algo ->
            AlgorithmInfoCard(algo = algo)
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            SectionTitle(title = "4. SANDI KLASIK (EDUKASI & SEJARAH)")
        }

        items(classicList) { algo ->
            AlgorithmInfoCard(algo = algo)
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            SectionTitle(title = "5. SANDI LEGACY (WARISAN)")
        }

        items(legacyList) { algo ->
            AlgorithmInfoCard(algo = algo)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
