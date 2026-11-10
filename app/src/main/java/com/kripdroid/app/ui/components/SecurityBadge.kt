package com.kripdroid.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kripdroid.app.crypto.SecurityCategory
import com.kripdroid.app.ui.theme.BadgeEncodingBg
import com.kripdroid.app.ui.theme.BadgeEncodingFg
import com.kripdroid.app.ui.theme.BadgeHashBg
import com.kripdroid.app.ui.theme.BadgeHashFg
import com.kripdroid.app.ui.theme.BadgeMediumBg
import com.kripdroid.app.ui.theme.BadgeMediumFg
import com.kripdroid.app.ui.theme.BadgeStrongBg
import com.kripdroid.app.ui.theme.BadgeStrongFg
import com.kripdroid.app.ui.theme.BadgeWeakBg
import com.kripdroid.app.ui.theme.BadgeWeakFg

@Composable
fun SecurityBadge(
    category: SecurityCategory,
    rating: Int,
    modifier: Modifier = Modifier
) {
    val (bgColor, fgColor) = when (category) {
        SecurityCategory.Classic -> Pair(BadgeWeakBg, BadgeWeakFg)
        SecurityCategory.Encoding -> Pair(BadgeEncodingBg, BadgeEncodingFg)
        SecurityCategory.Hash -> Pair(BadgeHashBg, BadgeHashFg)
        SecurityCategory.Legacy -> Pair(BadgeMediumBg, BadgeMediumFg)
        SecurityCategory.Modern -> Pair(BadgeStrongBg, BadgeStrongFg)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = category.displayName.uppercase(),
                color = fgColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Row {
                repeat(rating) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = fgColor,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}
