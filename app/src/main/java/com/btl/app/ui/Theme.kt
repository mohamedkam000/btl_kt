package com.btl.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun BTLTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val dark = dynamicDarkColorScheme(context)
    val light = dynamicLightColorScheme(context)
    MaterialTheme(colorScheme = light, content = content)
}