package com.kripdroid.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
    val classicList = CryptoAlgorithms.list.filter { it.category == SecurityCategory.Classic }
    val legacyList = CryptoAlgorithms.list.filter { it.category == SecurityCategory.Legacy }
    val modernList = CryptoAlgorithms.list.filter { it.category == SecurityCategory.Modern }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
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
                        text = "PANDUAN KEAMANAN KRIPTOGRAFI",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Untuk dokumen penting, foto, atau video rahasia, gunakan AES-256 atau ChaCha20-Poly1305 yang dilindungi Argon2id. Sandi klasik hanya untuk pembelajaran.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            SectionTitle(title = "1. TINGKAT DASAR (LATIHAN & BELAJAR)")
        }

        items(classicList) { algo ->
            AlgorithmInfoCard(algo = algo)
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            SectionTitle(title = "2. TINGKAT MENENGAH (STANDAR LAMA)")
        }

        items(legacyList) { algo ->
            AlgorithmInfoCard(algo = algo)
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            SectionTitle(title = "3. TINGKAT TERTINGGI (STANDAR MILITER - REKOMENDASI)")
        }

        items(modernList) { algo ->
            AlgorithmInfoCard(algo = algo)
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
